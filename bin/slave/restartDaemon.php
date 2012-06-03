#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	$result = shell_exec("ps -C java");
	//$result = "19073 pts/2    00:00:01 j";
	preg_match("/\s*(\d*)\spts.*/", $result, $matches);
	$pid = (int)$matches[1];
	if ($pid > 0) {
		echo "==== Restarting Daemon ====\n";
		//Daemon is running
		shell_exec("kill " + $pid);
	} else {
		echo "==== Starting Daemon ====\n";
	}
	
