<?php
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
function doPost($uri, $fields) {
	//url-ify the data for the POST
	$fields_string = "";
	foreach($fields as $key=>$value) {
		$fields_string .= $key.'='.$value.'&';
	}
	rtrim($fields_string,'&');
	//$fields_string = http_build_query($fields);
	//open connection
	$ch = curl_init();
	//set the url, number of POST vars, POST data
	curl_setopt($ch,CURLOPT_URL,$uri);
	curl_setopt($ch,CURLOPT_POST,count($fields));
	//curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
	curl_setopt($ch,CURLOPT_POSTFIELDS,$fields_string);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	//curl_setopt($ch, CURLOPT_MUTE, "true");

// 	exit;
	$result = curl_exec($ch);
	if (strpos($result, "Exception")) {
		echo "==== Exception in result ====\n".$result;
		exit;
	}
	//close connection
	curl_close($ch);
}

function executeQueries($uri, $queries, $mode) {
	if (!count($queries)) {
		echo "No queries to execute. Exiting...\n";
		exit;
	}
	
	$fields = array(
		"mode" => $mode,
		"query" => ""
	);
	//echo "executing ".count($queries)." queries: \n".implode("\n", $queries)."\n";
	foreach ($queries as $query) {
		$fields["query"] = $query;
		
		doPost($uri, $fields);
	}
}
function deleteDirContent($dir) {
	foreach (scandir($dir) as $item) {
		if ($item == '.' || $item == '..') continue;
		unlink($dir.DIRECTORY_SEPARATOR.$item);
	}
}


