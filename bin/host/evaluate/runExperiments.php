#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	include("InterfaceListener.php");
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
	
	
	
	foreach($config['args']['nChanges'] as $key => $nChanges) {
		$nTriples = $config['args']['nTriples'][$key];
		resetNodes($nTriples); //reset node before loading mappings, as resetting als insert triples in master triple store, which is needed to create the mappings.
		$mappings = loadChangesToExecute($config, $nChanges, $nTriples);
		
		if (!count($mappings)) {
			echo "No mappings loaded from triplestore. Exiting...\n";
			exit;
		}
		foreach ($config['args']['mode'] AS $mode) {
			$uniqueKey = sha1(microtime(true).mt_rand(10000,90000));
			shell_exec("ssh slave /home/lrd900/gitCode/bin/slave/restartDaemon.php ".$mode." ".$uniqueKey);
			waitForDaemonToStart($uniqueKey, __LINE__);
			//Do n number of test runs, and for each run, execute n queries
			$nQueries = 1;
			while ($nQueries <= $nChanges) {
				$iteration = 1;
				while ($iteration <= $config['args']['nRuns']) {
					$interfaceListener = new InterfaceListener($config, $mode, $nQueries, $nTriples, $iteration);
					resetNodes($nTriples);
					$uniqueKey = sha1(microtime(true).mt_rand(10000,90000));
					shell_exec("ssh -t master 'sudo /etc/init.d/tomcat6 restart'");
					$cmd = "ssh slave /home/lrd900/gitCode/bin/slave/restartDaemon.php ".$mode." ".$uniqueKey;
					shell_exec($cmd);
					waitForDaemonToStart($uniqueKey, __LINE__);
					prepareExports($config, $mode);
					sleep(3);
					echo date("H:i:s")." - Mode: ".$mode." - nChanges: ".$nQueries." - nTriples: ".$nTriples." - iteration: ".$iteration." \n";
					
					$queriesToExecute = array();
					//For run n, perform n number of queries
					$n = 0;
					while (count($queriesToExecute) < $nQueries) {
						$queriesToExecute[] = getDeleteInsertQuery($mappings[$n]);
						$n++;
					}
					echo date("H:i:s")." - Start Experiment\n";
					mysql_query("INSERT INTO Experiments (Mode, nChanges, nTriples, Iteration, RunId) VALUES (".(int)$mode.", ".(int)$nQueries.", ".(int)$nTriples.", ".$iteration.", '".$config['args']['runId']."');");
					$interfaceListener->start();
					$fields = array(
							"mode" => $mode,
							"query" => $queriesToExecute
					);
					doPost($config['master']['restlet']['updateUri'], $fields);
					//executeQueries($config['master']['restlet']['updateUri'], $queriesToExecute, $mode);
					waitForRunToFinish($mode, (int)$nQueries, $config['args']['runId'], __LINE__);
					sleep(1); //wait, so tcp dump has really finished writing to file (probably not needed though)
					$interfaceListener->stop(mysql_insert_id());
					echo date("H:i:s")." - Stop Experiment\n";
					$iteration++;
				}
				$nQueries += 2;
			}
		}
	}
	
	
	function prepareExports($config, $mode) {
		//These mode serialized the triple stores, and then sync them
		//The 'resetNode' functionality cleared the slave and master nodes, which means there are no existing serializations and stuff on disc
		//To simulate a proper use case scenario as much as possible, we need to make sure there already exists a serialization of the triplestore as it is now
		//Otherwise, we would measure how much time it costs to copy the serialization, while we want to know whether for instance rsync can optimize the syncing when just a part of the serialization has changed
		//To serialize the triple (without actually changing the triple store, we need to send an update statement which will fail (thus not change the triplestore)
		$fields = array(
				"mode" => $mode,
				"query" => "bla" //will fail, but will also make sure the graph is serialized
		);
		//First get current timestamp from db. This way we can wait for the processing of this post, before continuing
		$query = "SELECT NOW() FROM DaemonRunning LIMIT 1";
		$result = mysql_query($query);
		$timestamp = reset(mysql_fetch_array($result));
		doPost($config['master']['restlet']['updateUri'], $fields);
	
		while (true) {
			if (daemonFinished($mode, $timestamp, __LINE__)) {
				break;
			} else {
				sleep(3);
			}
		}
	}
	

	
	function waitForRunToFinish($mode, $nQueries, $runId, $line) {
		echo date("Ymd H:i:s")." - Wait for run to finish [".($line? $line:"")."]\n";
		//Get timestamp from before this experiment
		$query = "SELECT MAX(Timestamp) FROM Experiments WHERE  Mode = ".(int)$mode." AND nChanges = ".(int)$nQueries." AND RunId = '".$runId."'";
		$result = mysql_query($query);
		$timestamp = reset(mysql_fetch_array($result));
		if (!$timestamp) {
			echo "No timestamp inserted by experiment. Strange. Stopping...\n";
			exit;
		}
		while (true) {
			sleep(3);
			if (daemonFinished) {
				break;
			}
			
		}
	}
	
	function waitForDaemonToStart($uniqueKey, $line) {
		$query = "SELECT * FROM DaemonRunning WHERE `Key` = '".$uniqueKey."'";
		echo date("H:i:s")." - Wait for daemon to start [".($line? $line:"")."]\n";
		while (true) {
			$result = mysql_query($query);
			if (mysql_num_rows($result) > 0) {
				break;
			} else {
				sleep(3);
			}
		}
	}
	
	function daemonFinished($mode, $timestamp, $line) {
		echo date("H:i:s")." - Wait for deamon to finish importing [".($line? $line:"")."]\n";
		$finished = false;
		$query = "SELECT Timestamp FROM Daemon WHERE  Mode = ".(int)$mode." AND Timestamp >= '".$timestamp."'";
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
	function loadChangesToExecute($config, $nChanges, $nTriples) {
		include_once(__DIR__.'/../../lib/semsol-arc2/ARC2.php');
		$dumpDir = $config['experiments']['experimentCacheDir']."/mappings";
		$dumpFile = $dumpDir."/mappings_".$nChanges."_".$nTriples.".txt";
		var_export($dumpFile);exit;
		if (file_exists($dumpFile)) {
			include($dumpFile);
		} else {
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
			$selectedTriples = array_slice($results, 0, $nChanges);
			$remainingTriples = array_slice($results, (int)$nChanges);
			
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
			if (!is_dir($dumpDir)) {
				mkdir($dumpDir, 0777, true);
			}
			file_put_contents($dumpFile, '<?php $mappings = '.var_export($mappings, true).';');
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
				"mode:" => "Mode to run experiments in: \n\t  (1) Log Queries (rsync); \n\t  (2) Log Queries (DB); \n\t  (3) Serialize Graph (rsync); \n\t  (4) Log Queries (GIT); \n\t  (5) Serialize Graph (GIT);\n\t  (6) Serialize Graph (DB); \n\tUse comma seperated to run for multiple modes",
				"nChanges:" => "How many changes to execute per iteration (default 100).",
				"nTriples:" => "How many triples to execute experiment on (default 1000).",
				"changesVsTriples:" => "A list (comma separated) of nChanges and nTriples to perform. Useful for executing batch experiments. Notation: '100:200,150:200' (i.e. 'nQueries:nTriples,nQueries:nTriples'. This list override the nQueries and nTriples settings",
				"runId:" => "Id to run experiment for. Uses timestamp if none provided",
				"nRuns:" => "Number of runs to execute for each possible iteration",
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
			$args['nChanges'][] = 100;
		} else {
			$args['nChanges'] = array($args['nChanges']);
		}
		
		if (!strlen($args['runId'])) {
			$args['runId'] = date("Ymd_H-i");
		}
		
		if (!$args['nTriples']) {
			$args['nTriples'][] = 1000;
		} else {
			$args['nTriples'] = array($args['nTriples']);
		}
		if (strlen($args['changesVsTriples'])) {
			$args['nTriples'] = array();
			$args['nChanges'] = array();
			$sets = explode(",", $args['changesVsTriples']);
			foreach ($sets as $set) {
				$set = explode(":", $set);
				if (count($set) == 2) {
					$args['nChanges'][] = (int)reset($set);
					$args['nTriples'][] = (int)end($set);
				}
			}
		}
		
		if ((int)$args['nRuns'] === 0) {
			$args['nRuns'] = 1;
		}
		return $args;
	}
