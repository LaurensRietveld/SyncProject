#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	
	$storeInFile = $argv[1];
	if (!$storeInFile) {
		echo "Need to pass filename as parameter, to which results are stored";
	}
 	$cmd = "sudo tcpdump -nq -i vboxnet0 >> ".$storeInFile." &";
	shell_exec($cmd);
	echo "\tStarted tcpdump as daemon\n";
