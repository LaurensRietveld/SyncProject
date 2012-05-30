#!/usr/bin/php
<?php
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	$args = loadArguments();
	
	/**
	 * Run this once for all experiments, to make sure all nodes use the same codebase
	 */
	if ($args['prepareExtensive']) {
		echo shell_exec(__DIR__."/../management/compileSyncProject.sh");
		echo shell_exec(__DIR__."/../management/syncToNodes.php");
		echo shell_exec("ssh master:/home/lrd900/gitCode/bin/master/updateGitRepo.sh");
		echo shell_exec("ssh slave:/home/lrd900/gitCode/bin/slave/updateGitRepo.sh");
	}
	
	loadChangesToExecute($config);
	
	resetNodes();
	//For now, just 1 iteration, with 1 change
	for ($i = 0; $i < 1; $i++) {
		foreach ($args['mode'] AS $mode) {
			$uri = $config['master']['restlet']['updateUri'];
			$fields = array(
				"mode" => $mode,
				"query" => 'INSERT {<http://example/sub> <http://example/bla> "testFromExperiment"} WHERE {}',
			);
		}
	}
	
	function resetNodes() {
		echo shell_exec("ssh master /home/lrd900/gitCode/bin/master/resetNode.php");
		echo shell_exec("ssh slave /home/lrd900/gitCode/bin/slave/resetNode.php");
		echo shell_exec(__DIR__."/../management/insertSP2Data.php");
	}
	
	function loadChangesToExecute($config) {
		include_once(__DIR__.'/../../lib/semsol-arc2/ARC2.php');
		$arc2Config = array('remote_store_endpoint' => $uri);
		$store = ARC2::getRemoteStore($arc2Config);
		$query = 'SELECT * WHERE {?x ?f ?s} LIMIT 5';
		$rows = $store->query($query, 'rows');
	}
	
	function loadArguments() {
		$longArgs  = array(
				"help" => "Show help info",
				"mode:" => "Mode to run experiments in: (1) sync text queries; (2) use DB; (3) sync graph; (4) central (git) server. Use comma seperated to run for multiple modes",
				"prepareExtensive:" => "Either 0 or 1. Prepare all nodes extensively before running experiments (i.e., update codebase, compile and build). Default"
		);
		//: => required value, :: => optional value, no semicolon => no value (boolean)
		$args = getopt("", array_keys($longArgs));
		foreach ($args AS $arg => $option) {
			//Whenever an argument without argument name is passed, it automatically gets as value 'false'. Don't want this.
			if ($option === false) {
				$args[$arg] = true;
			}
		}
		if (empty($args) || array_key_exists('help', $args)) {
			echo "Available arguments: \n";
			foreach ($longArgs AS $arg => $description) {
				echo "\t".str_replace(":", "", $arg)." - ".$description."\n";
				
			}
			exit;
		}
		
		if ((int)$args['mode'] === 0) {
			echo "No valid mode provided. Exiting\n";
			exit;
		}
		$modes = explode(",", $args['mode']);
		foreach ($modes as $mode) {
			if ((int)$mode < 1 || (int)$mode > 4) {
				echo "No valid mode provided. Exiting\n";
				exit;
			}
		}
		$args['mode'] = $modes;
		//Default: prepareExtensive on
		if (!array_key_exists('prepareExtensive', $args)) {
			echo "No arg provided for 'prepareExtensive'. By default on.\n";
			$options['prepareExtensive'] = true;
		}
		return $args;
	}
