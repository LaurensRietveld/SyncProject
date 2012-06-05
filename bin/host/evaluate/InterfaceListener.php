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
		if (!file_exists($this->logDir)) {
			mkdir($this->logDir, 0777, true);
		}
		$storeInFile = $this->logDir."/i-".$this->iteration.".log";
		$cmd = "sudo tcpdump -nq -i vboxnet0 > ".$storeInFile." &";
		//echo $cmd;exit;
		shell_exec($cmd);
		echo "\tStarted tcpdump as daemon\n";
		
	}
	
	public function stop() {
		$result = shell_exec("ps axuwww | grep tcpdump | grep -v grep");
		preg_match_all("/\s*root\s*(\d*).*/", $result, $matches);
		if (is_array($matches[1])) {
			echo "\tStopped tcp dump instances\n";
			foreach ($matches[1] as $match) {
				shell_exec("sudo kill ".(int)$match);
			}
			$this->storeResults();
		} else {
			echo "\tNo tcp dump instance to kill";
		}
		
	}
	private function storeResults() {
		$files = scandir($this->logDir);
		foreach ($files as $filename) {
			$handle = @fopen($filename, "r");
			if ($handle) {
				while (($line = fgets($handle, 4096)) !== false) {
					if (strlen(trim($line))) {
						$time = "([\d:]*)[\d\.]* IP ";
						$fromIp = "(\d*\.\d*\.\d*\.\d*)\.";
						$fromPort = "(\d*) > ";
						$toIp = "(\d*\.\d*\.\d*\.\d*)";
						$toPort = "(\d*): ";
						$protocol = "(tcp|UDP)";
						$size = "(, length (\d*)| (\d*))";
			
						$pattern = "/".$time.$fromIp.$fromPort.$toIp.$toPort.$protocol.$size."/";
						preg_match($pattern, $line, $matches);
						$row = array();
						if (count($matches) == 10) {
							list(,$row['Time'], $row['FromIp'],$row['FromPort'], $row['ToIp'], $row['ToPort'], $row['Protocol'],,,$row['Size']) = $matches;
						} else if (count($matches) == 9) {
							list(,$row['Time'], $row['FromIp'], $row['FromPort'], $row['ToIp'], $row['ToPort'], $row['Protocol'], ,$row['Size']) = $matches;
						} else {
							echo "Strange... Incorrect matches from preg match: \n";
							var_export($matches);
							var_export($line);
							exit;
						}
						$query = "INSERT INTO Packets (".implode(", ", array_keys($row)).") VALUES (".implode(", ", $row).")";
						mysql_query($query);
					}
				}
				if (!feof($handle)) {
					echo "Error: unexpected fgets() fail on $filename\n";
					exit;
				}
			}
			fclose($handle);
		}
	}
	
}	

