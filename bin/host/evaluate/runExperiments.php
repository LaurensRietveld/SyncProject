#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	$config['args'] = loadArguments();
	
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	mysql_select_db("Experiments");
	
	$runningVMs = shell_exec("VBoxManage list runningvms");
	if (strpos($runningVMs, "Git Server") === false || strpos($runningVMs, "Debian Master") === false || strpos($runningVMs, "Debian Slave") === false) {
		echo "Not all node are running. Required nodes: 'Debian Git Server', 'Debian Master', 'Debian Slave'. Exiting...\n";
		exit;
	}
	
	resetNodes($config['args']['nTriples']); //reset node before loading mappings, as resetting als insert triples in master triple store, which is needed to create the mappings.
	$mappings = loadChangesToExecute($config);
	if (!count($mappings)) {
		echo "No mappings loaded from triplestore. Exiting...\n";
		exit;
	}
	
	//Write mappings to file, so we can always check whether they are actually executed
	file_put_contents(__DIR__."/mappings.txt", var_export($mappings, true));
	
	foreach ($config['args']['mode'] AS $mode) {
		shell_exec("ssh slave /home/lrd900/gitCode/bin/slave/restartDaemon.php ".$mode);
		//Do n number of test runs, and for each run, execute n queries
		$nQueries = 1;
		while ($nQueries <= $config['args']['nChanges']) {
			resetNodes($config['args']['nTriples']);
			prepareExports($config, $mode);
			
			sleep(1);
			echo "Mode: ".$mode." - nChanges: ".$nQueries;
			
			$queriesToExecute = array();
			//For run n, perform n number of queries
			$n = 0;
			while (count($queriesToExecute) < $nQueries) {
				$queriesToExecute[] = getDeleteInsertQuery($mappings[$n]);
				$n++;
			}
			echo ".";
			mysql_query("INSERT INTO Experiments (Mode, nChanges, nTriples, RunId) VALUES (".(int)$mode.", ".(int)$nQueries.", ".(int)$config['args']['nTriples'].", '".$config['args']['runId']."');");
			//startInterfaceListener($config, $mode, $nQueries);
			$fields = array(
					"mode" => $mode,
					"query" => $queriesToExecute
			);
			doPost($config['master']['restlet']['updateUri'], $fields);
			//executeQueries($config['master']['restlet']['updateUri'], $queriesToExecute, $mode);
			waitForFinish($mode, (int)$nQueries, $config['args']['runId']);
			//stopInterfaceListener();
			$nQueries++;
		}
	}
	
	
	function prepareExports($config, $mode) {
		//These mode serialized the triple stores, and then sync them
		//The 'resetNode' functionality cleared the slave and master nodes, which means there are no existing serializations on disc
		//To simulate a proper use case scenario as much as possible, we need to make sure there already exists a serialization of the triplestore as it is now
		//Otherwise, we would measure how much time it costs to copy the serialization, while we want to know whether for instance rsync can optimize the syncing when just a part of the serialization has changed
		//To serialize the triple (without actually changing the triple store, we need to send an update statement which will fail (thus not change the triplestore)
		$fields = array(
				"mode" => $mode,
				"query" => "bla" //will fail, but will also make sure the graph is serialized
		);
		//First get current timestamp from db. This way we can wait for the processing of this post, before continuing
		$query = "SELECT NOW() FROM Daemon LIMIT 1";
		$result = mysql_query($query);
		$timestamp = reset(mysql_fetch_array($result));
		doPost($config['master']['restlet']['updateUri'], $fields);
	
		while (true) {
			if (daemonFinished($mode, $timestamp)) {
				echo "\n";
				break;
			} else {
				echo "w";
				sleep(3);
			}
		}
	}
	
	function startInterfaceListener($config, $mode, $nQueries) {
		$logdir = $config['experiments']['netStats']."/".$config['args']['runId']."/mode".$mode;
		//var_export($logdir);exit;
		if (!file_exists($logdir)) {
			mkdir($logdir, 0777, true);
		}
		$storeInFile = $logdir."/".$nQueries.".log";
// 		$cmd = "sudo tcpdump -nq -i vboxnet0 > ".$storeInFile." &";
		shell_exec($cmd);
		echo "\tStarted tcpdump as daemon\n";
		
	}
	
	function stopInterfaceListener() {
		$result = shell_exec("ps axuwww | grep tcpdump | grep -v grep");
		preg_match_all("/\s*root\s*(\d*).*/", $result, $matches);
		if (is_array($matches[1])) {
			echo "\tStopped tcp dump instances\n";
			foreach ($matches[1] as $match) {
				shell_exec("sudo kill ".(int)$match);
			}
		} else {
			echo "\tNo tcp dump instance to kill";
		}
	}
	
	function waitForFinish($mode, $nQueries, $runId) {
		//Get timestamp from before this experiment
		$query = "SELECT MAX(Timestamp) FROM Experiments WHERE  Mode = ".(int)$mode." AND nChanges = ".(int)$nQueries." AND RunId = '".$runId."'";
		$result = mysql_query($query);
		$timestamp = reset(mysql_fetch_array($result));
		if (!$timestamp) {
			echo "No timestamp inserted by experiment. Strange. Stopping...\n";
			exit;
		}
		while (true) {
			echo "w";
			sleep(3);
			if (daemonFinished) {
				break;
				echo "\n";
			}
			
		}
	}
	
	function daemonFinished($mode, $timestamp) {
		$finished = false;
		$query = "SELECT Timestamp FROM Daemon WHERE  Mode = ".(int)$mode." AND Timestamp > '".$timestamp."'";
		$result = mysql_query($query);
		if (mysql_num_rows($result) > 0) {
			$finished = true;
		}
		return $finished;
	}
 	
	function getDeleteInsertQuery($mapping) {
		if (!is_array($mapping)) {
			echo "Something is wrong. Can't create query from empty mapping. exiting...\n";
			exit;
		}
		$before = parseQueryItem($mapping['original']['subject'], $mapping['original']['subject type']);
		$before .= " ".parseQueryItem($mapping['original']['predicate'], $mapping['original']['predicate type']);
		$before .= " ".parseQueryItem($mapping['original']['object'], $mapping['original']['object type']);

		$after = parseQueryItem($mapping['new']['subject'], $mapping['new']['subject type']);
		$after .= " ".parseQueryItem($mapping['new']['predicate'], $mapping['new']['predicate type']);
		$after .= " ".parseQueryItem($mapping['new']['object'], $mapping['new']['object type']);
		
		$query = "".
			"DELETE { ".$before." }\n".
			"INSERT { ".$after." }\n".
			"WHERE {}";
		return $query;
	}
	
	function parseQueryItem($value, $type) {
		$result;
		if ($type == "literal") {
			if (is_numeric($value)) {
				$result = $value;
			} else {
				$result = "'".$value."'";
			}
		} else if ($type == "uri") {
			$result = "<".$value.">";
		} else if ($type == "bnode") {
			$result = $value;
		}
		return $result;
	}
	
	/**
	 * Reset nodes to initial state by deleting/emptying dirs, db, and reinserting data in triplestore
	 */
	function resetNodes($nTriples) {
		echo shell_exec("ssh master /home/lrd900/gitCode/bin/master/resetNode.php");
		echo shell_exec("ssh slave /home/lrd900/gitCode/bin/slave/resetNode.php");
		echo shell_exec(__DIR__."/../management/insertSP2Data.php ".$nTriples);
	}
	
	
	/**
	 * Load triples
	 * Randomize them
	 * Select n
	 * 
	 * For each of these triples:
	 * 	Select randomly 1 item of the triple to remove
	 * 	Find another item (same type) to add to this triple
	 *  Store this mapping, so we can recreate a delete + insert query on this triple
	 *  
	 *  @param array $config
	 *  
	 *  @return array
	 */
	function loadChangesToExecute($config) {
		include_once(__DIR__.'/../../lib/semsol-arc2/ARC2.php');
		$arc2Config = array('remote_store_endpoint' => $config['master']['tripleStore']['selectUri']);
		$store = ARC2::getRemoteStore($arc2Config);
		
		//Get all triples from triple store
		$query = 'SELECT DISTINCT * WHERE {
			?subject ?predicate ?object.
		}'; //No limit: we want to randomize triples in php first (sparql doesnt do that)
		$results = $store->query($query, 'rows');
		
		//Randomize results
		shuffle($results);
		
		//Take the first n
		$selectedTriples = array_slice($results, 0, $config['args']['nChanges']);
		$remainingTriples = array_slice($results, (int)$config['args']['nChanges']);
		
		//For each selected triple, create a matching triple in which 1 value is changed
		$mappings = array(); //array containing info on what to change of a triple
		$choices = array('subject','predicate', 'object');//Used to randomly select which item to replace of a triple
		foreach ($selectedTriples AS $triple) {
			$replace = $choices[array_rand($choices)];
			$oldTriple = $triple;
			$newTriple = $triple;
			//get type of value to replace
			$type = $oldTriple[$replace." type"];
			//Randomly get value from remaining triples which matches this type
			shuffle($remainingTriples);
			foreach ($remainingTriples as $remainingTriple) {
				//We have a match for the new triple if the types are the same, and value is different (avoid deleting/inserting same value)			
				if ($remainingTriple[$replace." type"] === $type && $remainingTriple[$replace] !== $newTriples[$replace]) {
					//echo "Replace ".$newTriple[$replace]." with ".$remainingTriple[$replace]."\n";
					$newTriple[$replace] = $remainingTriple[$replace];
					break;
				}
			}
			$mappings[] = array(
				'original' => $oldTriple,
				'new' => $newTriple
			);
		}
		return $mappings;
	}
	
	/**
	 * Parse arguments, and if neccesary set defauls if no arg is provided
	 * @return array
	 */
	function loadArguments() {
		$longArgs  = array(
				"help" => "Show help info",
				"mode:" => "Mode to run experiments in: \n\t  (1) sync text queries; \n\t  (2) use DB; \n\t  (3) sync graph; \n\t  (4) central (git) server. Use comma seperated to run for multiple modes",
				"nChanges:" => "How many changes to execute per iteration (default 100).",
				"nTriples:" => "How many triples to execute experiment on (default 1000).", 
				"runId:" => "Id to run experiment for. Uses timestamp if none provided",
		);
		//: => required value, :: => optional value, no semicolon => no value (boolean)
		$args = getopt("", array_keys($longArgs));
		foreach ($args AS $arg => $option) {
			//Whenever an argument without argument name is passed, it automatically gets as value 'false'. Don't want this.
			if ($option === false) {
				$args[$arg] = true;
			}
		}
		if (empty($args) || array_key_exists('help', $args)) {
			echo "Available arguments: \n";
			foreach ($longArgs AS $arg => $description) {
				echo "\t--".str_replace(":", "", $arg)." - ".$description."\n";
				
			}
			exit;
		}
		
		if ((int)$args['mode'] === 0) {
			echo "No valid mode provided. Exiting\n";
			exit;
		}
		$modes = explode(",", $args['mode']);
		foreach ($modes as $mode) {
			if ((int)$mode < 1 || (int)$mode > 6) {
				echo "No valid mode provided. Exiting\n";
				exit;
			}
		}
		$args['mode'] = $modes;
		
		if (!$args['nChanges']) {
			$args['nChanges'] = 100;
		}
		
		if (!strlen($args['runId'])) {
			$args['runId'] = date("Ymd H:i");
		}
		
		if (!$args['nTriples']) {
			$args['nTriples'] = 1000;
		}
		return $args;
	}
