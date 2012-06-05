#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	$config['args'] = loadArguments();
	
	
	
	
	
	
	
	
	
	
	$line = trim('15:41:50.563342 IP 192.168.56.111.8080 > 192.168.56.1.39234: tcp 255');
	$time = "([\d:]*)[\d\.]* IP ";
	$fromIp = "(\d*\.\d*\.\d*\.\d*)\.";
	$fromPort = "(\d*) > ";
	$toIp = "(\d*\.\d*\.\d*\.\d*)\.";
	$toPort = "(\d*): ";
	$protocol = "(tcp|UDP)";
	$size = "(, length (\d*)| (\d*))";
		
	$pattern = "/".$time.$fromIp.$fromPort.$toIp.$toPort.$protocol.$size.".*/";
	preg_match($pattern, $line, $matches);
	var_export($matches);exit;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	$statDir = $filename = $config['experiments']['netStats'];
	$results = array();
	//$logdir = $config['experiments']['netStats']."/".$config['args']['runId']."/mode_".$mode."/nTriples_".$nTriples."/nChanges".$nQueries;
	$runDirs = getRunDirs($config);
	foreach ($runDirs as $runDir) {
		$modeDirs = getModeDirs($config, $runDir);
		foreach ($modeDirs as $modeDir) {
			$nTriplesDirs = getnTriplesDirs($config, $modeDir);
			foreach ($nTriplesDirs as $nTriplesDir) {
				$nChangesDirs = getnChangesDirs($config, $nTriplesDir);
				foreach ($nChangesDirs as $nChangesDir) {
					parseFilesInDir($results, $config, $nChangesDir);
				}
			}
		}
	}
	function getnChangesDirs($config, $parentDir) {
		$changesDirs = array();
		if (strlen($config['args']['nChanges']) && is_dir($parentDir."/nChanges_".$config['args']['nChanges'])) {
			$changesDirs[] = $parentDir."/nChanges_".$config['args']['nChanges'];
		} else {
			$changesDirs = glob($parentDir."/*", GLOB_ONLYDIR);
		}
		return $changesDirs;
	}
	
	function getnTriplesDirs($config, $parentDir) {
		$triplesDirs = array();
		if (strlen($config['args']['nTriples']) && is_dir($parentDir."/nTriples_".$config['args']['nTriples'])) {
			$triplesDirs[] = $parentDir."/nTriples_".$config['args']['nTriples'];
		} else {
			$triplesDirs = glob($parentDir."/*", GLOB_ONLYDIR);
		}
		return $triplesDirs;
	}
	
	
	function getModeDirs($config, $parentDir) {
		$modeDirs = array();
		if (count($config['args']['mode'])) {
			foreach ($config['args']['mode'] as $mode) {
				$modeDir = $parentDir."/mode_".$mode;
				if (is_dir($modeDir)) $modeDirs[] = $modeDir;
			}
				
		} else {
			$modeDirs = glob($parentDir."/*", GLOB_ONLYDIR);
		}
		return $modeDirs;
	}
	
	
	function getRunDirs($config) {
		$runDirs = array();
		if (strlen($config['args']['runId']) && is_dir($statDir."/".$config['args']['runId'])) {
			$runDirs[] = $statDir."/".$config['args']['runId'];
		} else {
			$runDirs = glob($statDir."/*", GLOB_ONLYDIR);
		}
		return $runDirs;
	}
	
	function parseFilesInDir(&$results, $config, $dir) {
		$files = scandir($dir);
		$fileResults = array();
		foreach ($files as $filename) {
			$result = parseFile($filename);
			if ($config['args']['aggregate']) {
			}
		}
		//$config['experiments']['netStats']."/".$config['args']['runId']."/mode_".$mode."/nTriples_".$nTriples."/nChanges".$nQueries;
	}
	
	function parseFile($filename) {
		$fileResults = array();
		$handle = @fopen($filename, "r");
		if ($handle) {
			while (($line = fgets($handle, 4096)) !== false) {
				if (strlen(trim($line))) {
					$time = "([\d:\.]*) IP ";
					$fromIp = "([\d\.]*) > ";
					$toIp = "([\d\.]*): ";
					$protocol = "(tcp|UDP)";
					$size = "(, length (\d*)| (\d*))";
		
					$pattern = "/".$time.$fromIp.$toIp.$protocol.$size."/";
					preg_match($pattern, $line, $matches);
					$row = array();
					if (count($matches) == 8) {
						list(,$row['time'], $row['fromIp'], $row['toIp'], $row['protocol'],,,$row['size']) = $matches;
					} else if (count($matches) == 7) {
						list(,$row['time'], $row['fromIp'], $row['toIp'], $row['protocol'], ,$row['size']) = $matches;
					} else {
						echo "Strange... Incorrect matches from preg match: \n";
						var_export($matches);
						var_export($line);
						exit;
					}
					$fileResults[] = $row;
				}
			}
			if (!feof($handle)) {
				echo "Error: unexpected fgets() fail on $filename\n";
				exit;
			}
			fclose($handle);
		}
	}
	
	
	echo "=== Summary ===\n";
	$size = 0;
	foreach ($results as $result) {
		$size += (int)$result['size'];
	}
	echo "total size: ".$size;
	
	function loadArguments() {
		$longArgs  = array(
				"help" => "Show help info",
				"mode:" => "Mode to get info: \n\t  (1) Log Queries (rsync); \n\t  (2) Log Queries (DB); \n\t  (3) Serialize Graph (rsync); \n\t  (4) Log Queries (GIT); \n\t  (5) Serialize Graph (GIT);\n\t  (6) Serialize Graph (DB); \n\tUse comma seperated to run for multiple modes",
				"nChanges:" => "Number of changes to get info for.",
				"nTriples:" => "Number of triples to get info for.",
				"runId:" => "Run id to get info for",
				//"nRuns:" => "Number of runs to execute for each possible iteration",
		);
		//: => required value, :: => optional value, no semicolon => no value (boolean)
		$args = getopt("", array_keys($longArgs));
		foreach ($args AS $arg => $option) {
			//Whenever an argument without argument name is passed, it automatically gets as value 'false'. Don't want this.
			if ($option === false) {
				$args[$arg] = true;
			}
		}
		if (array_key_exists('help', $args)) {
			echo "Available arguments: \n";
			foreach ($longArgs AS $arg => $description) {
				echo "\t--".str_replace(":", "", $arg)." - ".$description."\n";
	
			}
			exit;
		}
		
		if (strlen($args['mode'])) {
			$modes = explode(",", $args['mode']);
		} else {
			$modes = array();
		}
		$args['mode'] = $modes;
	
// 		if (!$args['nChanges']) {
// 			$args['nChanges'][] = 100;
// 		} else {
// 			$args['nChanges'] = array($args['nChanges']);
// 		}
	
// 		if (!strlen($args['runId'])) {
// 			$args['runId'] = date("Ymd H:i");
// 		}
	
// 		if (!$args['nTriples']) {
// 			$args['nTriples'][] = 1000;
// 		} else {
// 			$args['nTriples'] = array($args['nTriples']);
// 		}
// 		if (strlen($args['changesVsTriples'])) {
// 			$args['nTriples'] = array();
// 			$args['nChanges'] = array();
// 			$sets = explode(",", $args['changesVsTriples']);
// 			foreach ($sets as $set) {
// 				$set = explode(":", $set);
// 				if (count($set) == 2) {
// 					$args['nChanges'][] = (int)reset($set);
// 					$args['nTriples'][] = (int)end($set);
// 				}
// 			}
// 		}
	
// 		if ((int)$args['nRuns'] === 0) {
// 			$args['nRuns'] = 1;
// 		}
		return $args;
	}