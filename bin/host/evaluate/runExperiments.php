#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	$config['args'] = loadArguments();
	
	
	
// 	doPost("http://localhost:9080/syncRestlet/update", array("mode" => 2, "query" => "INSERT {<http://example/sub> <http://example/bla> \"test\"} WHERE {} "));
	
// 	echo "done test";exit;
	
	
	$runningVMs = shell_exec("VBoxManage list runningvms");
	if (strpos($runningVMs, "Git Server") === false || strpos($runningVMs, "Debian Master") === false || strpos($runningVMs, "Debian Slave") === false) {
		echo "Not all node are running. Required nodes: 'Debian Git Server', 'Debian Master', 'Debian Slave'. Exiting...\n";
		exit;
	}
	
	resetNodes();
	
	
	
	
	
	$mappings = loadChangesToExecute($config);
	
	
	foreach ($config['args']['mode'] AS $mode) {
		//Do n number of test runs, and for each run, execute n queries
		$nQueries = 1;
		while ($nQueries <= $config['args']['nChanges']) {
			echo "Experiment for mode: ".$mode." and nChanges ".$nQueries."\n";
			$queriesToExecute = array();
			//For run n, perform n number of queries
			$n = 0;
			while (count($queriesToExecute) < $nQueries) {
				$queriesToExecute[] = getDeleteInsertQuery($mappings[$n]);
				$n++;
			}
			executeQueries($config['master']['restlet']['updateUri'], $queriesToExecute, $mode);
			$nQueries++;
		}
		
	}
	
	function getDeleteInsertQuery($mapping) {
		var_export($mapping);
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
			$result = "'".$value."'";
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
	function resetNodes() {
		echo shell_exec("ssh master /home/lrd900/gitCode/bin/master/resetNode.php");
		echo shell_exec("ssh slave /home/lrd900/gitCode/bin/slave/resetNode.php");
		echo shell_exec(__DIR__."/../management/insertSP2Data.php");
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
				"nChanges:" => "How many changes to execute per iteration (default 100)"
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
			if ((int)$mode < 1 || (int)$mode > 4) {
				echo "No valid mode provided. Exiting\n";
				exit;
			}
		}
		$args['mode'] = $modes;
		if (!$args['nChanges']) {
			$args['nChanges'] = 100;
		}
		return $args;
	}
