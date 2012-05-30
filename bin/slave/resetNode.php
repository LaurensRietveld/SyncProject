#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	
	echo "==== ".basename(__DIR__).": Emptying querylog dir ====\n";
	deleteDirContent($config['slave']['queryLogDir']);
	echo "==== ".basename(__DIR__).": Emptying XML dump dir ====\n";
	deleteDirContent($config['slave']['xmlDumpDir']);
	echo "==== ".basename(__DIR__).": Cleaning GIT (Just pulling. Run master beforehand to empty repo) ====\n";
	$gitDir = $config['slave']['gitDir']."/".$config['mode4']['repoDir'];
	`cd $gitDir; git pull -q;`;
	echo "==== ".basename(__DIR__).": Removing log with already executed git queries ====\n";
	$executedQueries = $config['slave']['gitDir']."/".$config['mode4']['executedQueries'];
	if (file_exists($executedQueries)) {
		unset($executedQueries);
	}
	echo "==== ".basename(__DIR__).":  Cleaning DB (only 'already executed' DB. Run master beforehand to empty main QueryLog table) ====\n";
	mysql_connect("localhost:3306", "syncProject");
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	mysql_select_db("QueryLog");
	mysql_query("TRUNCATE TABLE `ExecutedOnSlave`");

	echo "==== ".basename(__DIR__).":  Emptying triple store ====\n";
	$uri = $config['slave']['tripleStore']['clearStoreUri'];
	$fields = array('context' => '');
	doPost($uri, $fields);