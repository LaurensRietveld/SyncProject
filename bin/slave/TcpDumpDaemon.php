#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	$startTcp = $config['experiments']['experimentCacheDir']."/s_startTcpDump";
	$storeFile = $config['experiments']['experimentCacheDir']."/network/s_dump.txt";
	while (true) {
		//check whether I need to start daemon
		usleep(500000); //sleep 0.5 sec
		if (file_exists($startTcp)) {
			startDaemon($storeFile);
			shell_exec("rm ".$startTcp);
		}
	}
	
	function startDaemon($storeInFile) {
		$cmd = "sudo tcpdump -nq -i eth1 \(\(src host 192.168.56.122 or src host 192.168.56.133\) and \(dst host 192.168.56.122 or dst host 192.168.56.133\)\) >> ".$storeInFile." &";
		shell_exec($cmd);
		echo "\tStarted tcpdump\n";
	}

