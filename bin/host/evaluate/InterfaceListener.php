<?php
class InterfaceListener {
	private $config;
	private $mode;
	private $nQueries;
	private $nTriples;
	private $iteration;
	private $logDir;
	function __construct($config, $mode, $nQueries, $nTriples, $iteration) {
		$this->config = $config;
		$this->mode = $mode;
		$this->nQueries = $nQueries;
		$this->nTriples = $nTriples;
		$this->iteration = $iteration;
		$this->logDir = $config['experiments']['experimentCacheDir']."/network/".$config['args']['runId']."/mode_".$mode."/nTriples_".$nTriples."/nChanges_".$nQueries;
	}
	public function start() {
		$nodes = array(
				'master' => "192.168.56.111",
				'slave' => "192.168.56.122",
				'gitServer' => "192.168.56.133",
		);
		//master logs every connection between itself and slave / gitserver
		$masterExpression = getExpressionForNodes($nodes);
		 
		//for slave expression, we just want to measure connection between slave/gitserver. Rest is already covered by master logging
		unset($nodes['master']); 
		$slaveExpression = getExpressionForNodes($nodes);

		if (file_exists($this->logDir)) {
			//Clean log dir, to avoid reloading old data into db
			shell_exec('rm -rf '.$this->logDir);
		}
		
		mkdir($this->logDir, 0777, true);
		$masterFile = $this->logDir."/master_i-".$this->iteration.".log";
		$slaveFile = $this->logDir."/slave_i-".$this->iteration.".log";
		$cmd = "ssh -t slave \"sudo tcpdump -nq -i eth1 ".$masterExpression." > ".$storeInFile." &\"";
		$cmd = "ssh -t master \"sudo tcpdump -nq -i eth1 ".$slaveExpression." > ".$storeInFile." &\"";
		//echo $cmd;exit;
		shell_exec($cmd);
		echo "\tStarted tcpdump as daemon\n";
		
	}
	
	public function stop($experimentId) {
		shell_exec("ssh -t slave /home/lrd900/gitCode/bin/slave/stopTcpDump.php");
		shell_exec("ssh -t master /home/lrd900/gitCode/bin/master/stopTcpDump.php");
		$this->storeResults($experimentId);
	}
	
	private function getExpressionForNodes($nodes) {
		$srcHosts = 'host '.implode(" or src host ", $nodes);
		$dstHosts = 'host '.implode(" or dst host ", $nodes);
		return '\(\(src '.$srcHosts.'\) and \(dst '.$dstHosts.'\)\)';
	}
	private function storeResults($experimentId) {
		$files = scandir($this->logDir);
		foreach ($files as $filename) {
			$filename = $this->logDir."/".$filename;
			$handle = fopen($filename, "r");
			if ($handle) {
				while (($line = fgets($handle, 4096)) !== false) {
					if (strlen(trim($line)) && !strpos("ARP. Request who-has", $line)) {
						$line = trim($line);
						
						$time = "([\d:]*)[\d\.]* IP ";
						$fromIp = "(\d*\.\d*\.\d*\.\d*)\.";
						$fromPort = "(\d*) > ";
						$toIp = "(\d*\.\d*\.\d*\.\d*)\.";
						$toPort = "(\d*): ";
						$protocol = "(tcp|UDP)";
						$size = "(, length (\d*)| (\d*))";
			
						$pattern = "/".$time.$fromIp.$fromPort.$toIp.$toPort.$protocol.$size."/";
						preg_match($pattern, $line, $matches);
						$row = array();
						if (count($matches) == 10) {
							list(,$time, $fromIp, $fromPort, $toIp, $toPort, $protocol,,,$size) = $matches;
						} else if (count($matches) == 9) {
							list(,$time, $fromIp, $fromPort, $toIp, $toPort, $protocol,,$size) = $matches;
						} else {
							echo "Strange... Incorrect matches from preg match: \n";
							var_export($matches);
							var_export($line);
							exit;
						}
						$queryArray = array(
							'Time' => "'".$time."'",
							'FromIp' => "'".$fromIp."'",
							'FromPort' => $fromPort,
							'ToIp' => "'".$toIp."'",
							'ToPort' => $toPort,
							'Protocol' => "'".$protocol."'",
							'Size' => $size,
							'ExperimentId' => $experimentId
						);
						$query = "INSERT INTO Packets (".implode(", ", array_keys($queryArray)).") VALUES (".implode(", ", $queryArray).")";
						if (!mysql_query($query)) {
							die('Error executing mysql: ' . mysql_error());
						}
					}
				}
				if (!feof($handle)) {
					echo "Error: unexpected fgets() fail on $filename\n";
					exit;
				}
				fclose($handle);
			} else {
				echo "cannot open file: ".$filename."\n";
				exit;
			}
			
		}
	}
	
}	

