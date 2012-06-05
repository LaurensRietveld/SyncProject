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
		$this->logDir = $config['experiments']['experimentCacheDir']."/".$config['args']['runId']."/mode_".$mode."/nTriples_".$nTriples."/nChanges_".$nQueries;
	}
	public function start() {
		$nodes = array(
				'master' => "192.168.56.111",
				'slave' => "192.168.56.122",
				'gitServer' => "192.168.56.133",
		);
		$hosts = 'host '.implode(" or src host ", $nodes);
		
		//$expression = '\(\(src '.$hosts.'\) and \(src '.$hosts.'\)\)';
		
		//var_export($logdir);exit;
		if (file_exists($this->logDir)) {
			//Clean log dir, to avoid reloading old data into db
			shell_exec('rm -rf '.$this->logDir);
		}
		mkdir($this->logDir, 0777, true);
		$storeInFile = $this->logDir."/i-".$this->iteration.".log";
		$cmd = "sudo tcpdump -nq -i vboxnet0 > ".$storeInFile." &";
		//echo $cmd;exit;
		shell_exec($cmd);
		echo "\tStarted tcpdump as daemon\n";
		
	}
	
	public function stop($experimentId) {
		$result = shell_exec("ps axuwww | grep tcpdump | grep -v grep");
		preg_match_all("/\s*root\s*(\d*).*/", $result, $matches);
		if (is_array($matches[1])) {
			echo "\tStopped tcp dump instances\n";
			foreach ($matches[1] as $match) {
				shell_exec("sudo kill ".(int)$match);
			}
			$this->storeResults($experimentId);
		} else {
			echo "\tNo tcp dump instance to kill";
		}
		
	}
	private function storeResults($experimentId) {
		$files = scandir($this->logDir);
		foreach ($files as $filename) {
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

