#!/usr/bin/php
<?php
	include(__DIR__."/../../util.php");
	$config = getConfig();
	$result = shell_exec("ps axuwww | grep tcpdump | grep -v grep");
	preg_match_all("/\s*root\s*(\d*).*/", $result, $matches);
	if (is_array($matches[1])) {
		echo "\tStopped tcp dump instances\n";
		foreach ($matches[1] as $match) {
			shell_exec("sudo kill ".(int)$match);
		}
	} else {
		echo "\tNo tcp dump instance to kill";
	}
