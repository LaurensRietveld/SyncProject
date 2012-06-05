#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	$config['args'] = loadArguments();
	
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	
	mysql_select_db("Experiments");
	
	getTimeDifferences($config);
	
	function getTimeDifferences($config) {
		$query = "SELECT * FROM Experiments WHERE Mode =".(int)$config['args']['mode']." AND RunId = '".$config['args']['runId']."' AND nTriples = ".$config['args']['nTriples']." ORDER BY ExperimentId ASC";
		$result = mysql_query($query);
		while ($row = mysql_fetch_array($result)) {
			$query = "SELECT Timestamp FROM Daemon WHERE
			Node = 'slave'
			AND Mode = ".(int)$config['args']['mode']."
			AND Timestamp >= '".$row['Timestamp']."'
			ORDER BY Timestamp ASC LIMIT 1";
			$result2 = mysql_query($query);
			$row2 = mysql_fetch_array($result2);
				
			//Difference between executing queries on restlet, and the first time after that the slave finished executing it's import
			echo ($row['Timestamp']) ." < ". ($row2['Timestamp'])." -- diff: ".(strtotime($row2['Timestamp']) - strtotime($row['Timestamp']))." sec.\n";
				
		}
	}
	
	
	/**
	 * Parse arguments, and if neccesary set defauls if no arg is provided
	 * @return array
	 */
	function loadArguments() {
		$longArgs  = array(
				"help" => "Show help info",
				"mode:" => "Mode to check experiments for: \n\t  (1) Log Queries (rsync); \n\t  (2) Log Queries (DB); \n\t  (3) Serialize Graph (rsync); \n\t  (4) Log Queries (GIT); \n\t  (5) Serialize Graph (GIT);\n\t  (6) Serialize Graph (DB);",
				"runId:" => "Id to run experiment for. Uses timestamp if none provided",
				"nTriples:" => "Id to run experiment for. Uses timestamp if none provided",
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
		if (!strlen($args['runId'])) {
			echo "No run id provided. Exiting\n";
			exit;
		}
		if ((int)$args['nTriples'] === 0) {
			echo "No number of triples to get results for provided. Exiting\n";
			exit;
		}
		return $args;
	}
	
