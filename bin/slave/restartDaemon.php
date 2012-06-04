#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	$result = shell_exec("ps axuwww | grep java | grep -v grep | grep daemon.jar");
	$mode = $argv[1];
	if ($mode <= 0 || $mode > 6 ) {
		echo "No mode number passed as parameter. Stopping\n";
		exit;
	}
	echo "==== ".date("Ymd H:i:s").": ";
	preg_match_all("/\s*lrd900\s*(\d*).*/", $result, $matches);
	if (is_array($matches[1])) {
		echo "Restarting Daemon ====\n";
		foreach ($matches[1] as $match) {
			shell_exec("kill ".(int)$match);
		}
	} else {
		echo "Starting Daemon ====\n";
	}
	$result = shell_exec("java -jar /usr/local/share/syncProject/daemon.jar --mode ".$mode." >> /usr/local/share/syncProject/logs/daemon.txt &");
