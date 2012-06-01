#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	echo "==== ".basename(__DIR__).": Resetting ====\n";
	echo "\tquery log dir\n";
	deleteDirContent($config['slave']['queryLogDir']);
	echo "\txml dump dir\n";
	deleteDirContent($config['slave']['xmlDumpDir']);
	echo "\tGIT dir (only pull. run master beforehand)\n";
	$gitDir = $config['slave']['gitDir']."/".$config['mode4']['repoDir'];
	`cd $gitDir; git pull -q;`;
	echo "\texecuted query log file\n";
	$executedQueries = $config['slave']['gitDir']."/".$config['mode4']['executedQueries'];
	if (file_exists($executedQueries)) {
		unset($executedQueries);
	}
	echo "\texecuted query log DB table\n";
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	mysql_select_db("QueryLog");
	mysql_query("TRUNCATE TABLE `ExecutedOnSlave`");

	echo "\ttriple store\n";
	$uri = $config['slave']['tripleStore']['clearStoreUri'];
	$fields = array('context' => '');
	doPost($uri, $fields);