#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	echo "==== ".basename(__DIR__).": Resetting ====\n";
	echo "\tquery log dir\n";
	shell_exec("echo '' > ".$config['master']['queryLogDir']."/".$config['queryLogMode']['updateFile']);
	echo "\txml dump dir\n";
	shell_exec("echo '' > ".$config['master']['serializationDir']."/".$config['serializationMode']['dumpFile']);
	echo "\tGIT dir (incl push/commit)\n";
	$gitDir = $config['master']['git']['dir']."/".$config['master']['git']['repoDir'];
	`cd $gitDir; git reset .; git checkout .`;
	foreach (scandir($gitDir) as $item) {
		if ($item == '.' || $item == '..' || $item == '.git') continue;
		unlink($gitDir.DIRECTORY_SEPARATOR.$item);
	}
	`cd $gitDir; git pull -q;git add .; git commit -qam "cleaning dir"; git push -q origin master`;
	echo "\tDB\n";
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	mysql_select_db("SyncProject");
	mysql_query("TRUNCATE TABLE `QueryLog`");
	
	echo "\ttriple store\n";
	$uri = $config['master']['tripleStore']['clearStoreUri'];
	$fields = array('context' => '');
	doPost($uri, $fields);
