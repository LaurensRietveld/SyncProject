#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	$result = shell_exec("ps -C java");
	$mode = $argv[1];
	if ($mode <= 0 || $mode > 6 ) {
		echo "No mode number passed as parameter. Stopping\n";
		exit;
	}
	
	preg_match("/\s*(\d*)\spts.*/", $result, $matches);
	$pid = (int)$matches[1];
	if ($pid > 0) {
		echo "==== Restarting Daemon ====\n";
		//Daemon is running
		shell_exec("kill ".$pid);
	} else {
		echo "==== Starting Daemon ====\n";
		
	}
	
	$result = shell_exec("java -jar /usr/local/share/syncProject/daemon.jar --mode ".$mode." > /usr/local/share/syncProject/logs/daemon.txt &");
