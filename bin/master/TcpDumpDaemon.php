#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	$startTcp = $config['experiments']['experimentCacheDir']."/m_startTcpDump";
	$stopTcp = $config['experiments']['experimentCacheDir']."/m_stopTcpDump";
	$storeFile = $config['experiments']['experimentCacheDir']."/network/m_dump.txt";
	while (true) {
		//check whether I need to start daemon
		usleep(500000); //sleep 0.5 sec
		if (file_exists($startTcp)) {
			startDaemon($storeFile);
			unlink($startTcp);
		}
		if (file_exists($stopTcp)) {
			stopDaemon();
			unlink($stopTcp);
		}
	}
	
	function stopDaemon() {
		shell_exec("/home/lrd900/gitCode/bin/master/stopTcpDump.php");
	}
	
	function startDaemon($storeInFile) {
		$cmd = "sudo tcpdump -nq -i eth1 \(\(src host 192.168.56.111 or src host 192.168.56.122 or src host 192.168.56.133\) and \(dst host 192.168.56.111 or dst host 192.168.56.122 or dst host 192.168.56.133\)\) >> ".$storeInFile." &";
		
		shell_exec($cmd);
		echo "\tStarted tcpdump\n";
	}

