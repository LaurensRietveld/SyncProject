#!/usr/bin/php
<?php
	include(__DIR__."/../../util.php");
	$config = getConfig();
	$storeInFile = __DIR__."/tmp.txt";
	$cmd = "sudo tcpdump -nq -i vboxnet0 >> ".$storeInFile." &";
	shell_exec($cmd);
	echo "\tStarted tcpdump as daemon\n";
