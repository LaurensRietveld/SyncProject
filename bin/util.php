<?php
require_once("lib/sparqllib.php");


// is curl installed?
if (!function_exists('curl_init')){
	die('CURL is not installed!');
}


function getConfig() {
	$lines = file(__DIR__.'/../config/config.conf');
	$jsonString = "";
	// Loop through our array, show HTML source as HTML source; and line numbers too.
	foreach ($lines as $line) {
		if (substr(trim($line), 0, 2) == "//") {
			//Is a comment: skip
			continue;
		}
		$jsonString .= $line."\n";
	}
	$config = json_decode($jsonString, true);
	if ($config == null) {
		echo "Parsing config failed. Syntax error.\n";
		exit;
	}
	return $config;
}

function deleteDirContent($dir) {
	foreach (scandir($dir) as $item) {
		if ($item == '.' || $item == '..') continue;
		unlink($dir.DIRECTORY_SEPARATOR.$item);
	}
}
