#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	
	$filename = $argv[1];
	if (!strlen($filename)){
		echo "No file passed as parameter to get timings from. Exiting\n";
		exit;
	}
	
	$handle = @fopen($filename, "r");
	$results = array();
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
				$results[] = $row;
			}
		}
		if (!feof($handle)) {
			echo "Error: unexpected fgets() fail\n";
			exit;
		}
		fclose($handle);
	}
	
	
	
	$size = 0;
	foreach ($results as $result) {
		$size += (int)$result['size'];
	}
	echo "total size: ".$size;