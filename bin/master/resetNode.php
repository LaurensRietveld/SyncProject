#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	echo "==== ".basename(__DIR__).": Resetting ====\n";
	//echo "\tquery log dir\n";
	shell_exec("echo '' > ".$config['master']['queryLogDir']."/".$config['queryLogMode']['updateFile']);
	//echo "\txml dump dir\n";
	shell_exec("echo '' > ".$config['master']['serializationDir']."/".$config['serializationMode']['dumpFile']);
	//echo "\tGIT dir (incl push/commit)\n";
	$gitDir = $config['master']['git']['dir']."/".$config['master']['git']['repoDir'];
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
	`cd $gitDir; touch update.log;git add update.log;git commit update.log -m "sdf";git push origin master;`;
	//echo "\tDB\n";
	$db = mysql_connect("localhost:3306", "syncProject");
	if (!$db) die('Could not connect: ' . mysql_error());
	mysql_select_db("SyncProject");
	mysql_query("TRUNCATE TABLE `QueryLog`");
	
	//echo "\ttriple store\n";
	$uri = $config['master']['tripleStore']['clearStoreUri'];
	$fields = array('context' => '');
	doPost($uri, $fields);

	
	
	
	
	
	
	
