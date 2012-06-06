#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	echo "==== ".basename(__DIR__).": Resetting ====\n";
	//echo "\tquery log dir\n";
	shell_exec("echo '' > ".$config['slave']['queryLogDir']."/".$config['queryLogMode']['updateFile']);
	shell_exec("echo '' > ".$config['slave']['queryLogDir']."/".$config['queryLogMode']['executedQueriesFile']);
	//echo "\txml dump dir\n";
	deleteDirContent($config['slave']['serializationDir']);
	//echo "\tGIT dir (only pull. run master beforehand)\n";
	$gitDir = $config['slave']['git']['dir']."/".$config['slave']['git']['repoDir'];
	`cd $gitDir; rm -rf ./* .git;git init;`;
	file_put_contents($gitDir."/.git/config", "[core]\n
repositoryformatversion = 0\n
filemode = true\n
bare = false\n
logallrefupdates = true\n
[remote \"origin\"]\n
url = lrd900@gitServer:syncProject\n
fetch = +refs/heads/*:refs/remotes/origin/*\n
[branch \"master\"]\n
remote = origin\n
merge = refs/heads/master");
	`cd $gitDir; git pull origin master`;
	//echo "\texecuted query log DB table\n";
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	mysql_select_db("SyncProject");
	mysql_query("TRUNCATE TABLE `ExecutedOnSlave`");

	//echo "\ttriple store\n";
	$uri = $config['slave']['tripleStore']['clearStoreUri'];
	$fields = array('context' => '');
	doPost($uri, $fields);
