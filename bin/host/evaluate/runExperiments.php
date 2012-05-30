#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	
	$args = loadArguments();
	
	
	echo shell_exec("ssh master /home/lrd900/gitCode/bin/master/resetNode.php");
	echo shell_exec("ssh slave /home/lrd900/gitCode/bin/slave/resetNode.php");
	
	echo shell_exec("../management/insertSP2Data.php");
	
	
	
	
	
	
	
	function loadArguments() {
		$longopts  = array(
				"help" => "Show help info",
				"mode:" => "Mode to run experiments in",
				
		);
		//: => required value, :: => optional value, no semicolon => no value (boolean)
		$options = getopt("", array_keys($longopts));
		if (empty($options) || array_key_exists('help', $options)) {
			echo "Available arguments: \n";
			foreach ($longopts AS $arg => $description) {
				echo "\t".str_replace(":", "", $arg)." - ".$description."\n";
				
			}
			exit;
		}
		
		if ((int)$options['mode'] < 1 || (int)$options['mode'] > 4) {
			echo "No valid mode provided. Exiting\n";
			exit;
		}
	}
	
	
	/**
	 * Options: 
	 * resetNodes ([yes]/no)
	 * experimentMode (1-4)
	 * numberOfChanges (int)
	 * 
	 * Create triples on host
	 * Import triples in master
	 * Import triples in slave
	 * Select n triples to change
	 * 
	 * 
	 */