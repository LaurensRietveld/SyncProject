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
		//$this->logDir = $config['experiments']['experimentCacheDir']."/network/".$config['args']['runId']."/mode_".$mode."/nTriples_".$nTriples."/nChanges_".$nQueries;
		$this->logDir = $config['experiments']['experimentCacheDir']."/network";
	}
	public function start() {
// 		$nodes = array(
// 				'master' => "192.168.56.111",
// 				'slave' => "192.168.56.122",
// 				'gitServer' => "192.168.56.133",
// 		);
// 		//master logs every connection between itself and slave / gitserver
// 		$masterExpression = $this->getExpressionForNodes($nodes);
		 
// 		//for slave expression, we just want to measure connection between slave/gitserver. Rest is already covered by master logging
// 		unset($nodes['master']); 
// 		$slaveExpression = $this->getExpressionForNodes($nodes);

// 		if (file_exists($this->logDir)) {
// 			//Clean log dir, to avoid reloading old data into db
// 			shell_exec('rm -rf '.$this->logDir);
// 		}
		
// 		mkdir($this->logDir, 0777, true);
// 		$masterFile = $this->logDir."/master_i-".$this->iteration.".log";
// 		$slaveFile = $this->logDir."/slave_i-".$this->iteration.".log";
// 		$cmdMaster = "ssh -t slave \"nohup sudo tcpdump -nq -i eth1 ".$masterExpression." > ".$masterFile." &\"";
// 		$cmdSlave = "ssh -t master \"nohup sudo tcpdump -nq -i eth1 ".$slaveExpression." > ".$slaveFile." &\"";
// 		echo $cmdMaster."\n";
// 		echo $cmdSlave;exit;
// 		shell_exec($cmdMaster);
// 		shell_exec($cmdSlave);
// 		echo "\tStarted tcpdump as daemon\n";
		
		shell_exec('ssh slave "touch '.$this->config['experiments']['experimentCacheDir'].'/s_startTcpDump"');
		shell_exec('ssh master "touch '.$this->config['experiments']['experimentCacheDir'].'/m_startTcpDump"');
		sleep(1);
		
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
		$queryArrays = array();
		foreach ($files as $filename) {
			if ($filename != '.' && $filename != '..') {
				$filename = $this->logDir."/".$filename;
				$handle = fopen($filename, "r");
				if ($handle) {
					while (($line = fgets($handle, 4096)) !== false) {
						if (strlen(trim($line))) {
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
								//echo "Strange... Incorrect matches from preg match: \n";
								//var_export($matches);
								//var_export($line);
								//exit;
								continue;
							}
							$queryArrays[] = array(
								'Time' => "'".$time."'",
								'FromIp' => "'".$fromIp."'",
								'FromPort' => $fromPort,
								'ToIp' => "'".$toIp."'",
								'ToPort' => $toPort,
								'Protocol' => "'".$protocol."'",
								'Size' => $size,
								'ExperimentId' => $experimentId
							);
							
							
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
				shell_exec("rm ".$filename);
			}
		}
		if (count($queryArrays)) {
			$this->insertIntoDb($queryArrays);
		}
	}
	
	private function insertIntoDb($packetArrays) {
		$insertArray = array();
		
// 		'Time' => "'".$time."'",
// 		'FromIp' => "'".$fromIp."'",
// 		'FromPort' => $fromPort,
// 		'ToIp' => "'".$toIp."'",
// 		'ToPort' => $toPort,
// 		'Protocol' => "'".$protocol."'",
// 		'Size' => $size,
// 		'ExperimentId' => $experimentId
		foreach ($packetArrays as $packetArray) {
			$aggregateArray =& $insertArray[$packetArray['FromIp'].$packetArray['ToIp'].$packetArray['FromPort'].$packetArray['ToPort']];
			$aggregateArray['Time'] = $packetArray['Time'];
			$aggregateArray['FromIp'] = $packetArray['FromIp'];
			$aggregateArray['FromPort'] = $packetArray['FromPort'];
			$aggregateArray['ToIp'] = $packetArray['ToIp'];
			$aggregateArray['ToPort'] = $packetArray['ToPort'];
			$aggregateArray['Protocol'] = $packetArray['Protocol'];
			$aggregateArray['Size'] += $packetArray['Size']; //HERE: do the aggregate
			$aggregateArray['ExperimentId'] = $packetArray['ExperimentId'];
		}
		
		foreach ($insertArray as $insert) {
			
			$query = "INSERT INTO Packets (".implode(", ", array_keys($insert)).") VALUES (".implode(", ", $insert).")";
			if (!mysql_query($query)) {
				die('Error executing mysql: ' . mysql_error() ."\n query: ".$query);
			}
		}
	}
	
}	

