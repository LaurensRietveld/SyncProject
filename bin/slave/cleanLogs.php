#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	
	echo "==== ".basename(__DIR__).": Emptying querylog dir ====\n";
	deleteDirContent($config['slave']['queryLogDir']);
	echo "==== ".basename(__DIR__).":  Emptying XML dump dir ====\n";
	deleteDirContent($config['slave']['xmlDumpDir']);
	echo "==== ".basename(__DIR__).":  Cleaning GIT (Just pulling. Run master before to empty repo) ====\n";
	$gitDir = $config['slave']['gitDir']."/".$config['mode4']['repoDir'];
	`cd $gitDir; git pull -q;`;
	echo "==== ".basename(__DIR__).":  Removing log with already executed git queries ====\n";
	$executedQueries = $config['slave']['gitDir']."/".$config['mode4']['executedQueries'];
	if (file_exists($executedQueries)) {
		unset($executedQueries);
	}

