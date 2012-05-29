<?php
require_once("lib/sparqllib.php");
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

function emptyTripleStore($uri) {
	$sparql = sparql_connect($uri);
	if(!$sparql) {
		echo sparql_errno().": ".sparql_error()."\n";
		exit;
	}
	$sparql = "SELECT * WHERE { ?person a foaf:Person . ?person foaf:name ?name } LIMIT 5";
	$result = sparql_query($sparql);
	if(!$result) {
		print sparql_errno().": ".sparql_error()."\n";
		exit;
	}
}