#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	echo "==== ".basename(__DIR__).": Resetting ====\n";
// 	echo "\tjava log dir\n";
// 	deleteDirContent("/usr/local/share/syncProject/logs");
// 	`echo "" > /usr/local/share/syncProject/logs/placeholder`;
	echo "\tquery log dir\n";
	deleteDirContent($config['master']['queryLogDir']);
	//shell_exec("echo '' > ".$config['master']['queryLogDir']."/".$config['mode1']['updateFile']);
	echo "\txml dump dir\n";
	//deleteDirContent($config['master']['xmlDumpDir']);
	//`echo "" > /usr/local/share/syncProject/xmlDump/dump.xml`;
	//`chmod 777 /usr/local/share/syncProject/xmlDump/dump.xml`;
	shell_exec("echo '' > ".$config['master']['xmlDumpDir']."/".$config['mode3']['dumpFile']);
	echo "\tGIT dir (incl push/commit)\n";
	$gitDir = $config['master']['gitDir']."/".$config['mode4']['repoDir'];
	foreach (scandir($gitDir) as $item) {
		if ($item == '.' || $item == '..' || $item == '.git') continue;
		unlink($gitDir.DIRECTORY_SEPARATOR.$item);
	}
	`cd $gitDir; git pull -q;git add .; git commit -qam "cleaning dir"; git push -q origin master`;
	echo "\tDB\n";
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	mysql_select_db("QueryLog");
	mysql_query("TRUNCATE TABLE `QueryLog`");
	
	echo "\ttriple store\n";
	$uri = $config['master']['tripleStore']['clearStoreUri'];
	$fields = array('context' => '');
	doPost($uri, $fields);
