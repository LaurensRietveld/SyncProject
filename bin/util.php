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

function doPost($uri, $fields) {
	//url-ify the data for the POST
	$fields_string = "";
	foreach($fields as $key=>$value) {
		$fields_string .= $key.'='.$value.'&';
	}
	rtrim($fields_string,'&');
	//open connection
	$ch = curl_init();
	//set the url, number of POST vars, POST data
	curl_setopt($ch,CURLOPT_URL,$uri);
	curl_setopt($ch,CURLOPT_POST,count($fields));
	//curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
	curl_setopt($ch,CURLOPT_POSTFIELDS,$fields_string);
	
	//execute post
	$result = curl_exec($ch);
	//close connection
	curl_close($ch);
}

function importSP2Data($uri) {
	
}