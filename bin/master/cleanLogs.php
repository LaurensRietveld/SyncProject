#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	
	echo "==== ".basename(__DIR__).": Emptying querylog dir ====\n";
	deleteDirContent($config['master']['queryLogDir']);
	echo "==== ".basename(__DIR__).":  Emptying XML dump dir ====\n";
	deleteDirContent($config['master']['xmlDumpDir']);
	echo "==== ".basename(__DIR__).":  Cleaning GIT (including commit/push) ====\n";
	$gitDir = $config['master']['gitDir']."/".$config['mode4']['repoDir'];
	foreach (scandir($gitDir) as $item) {
		if ($item == '.' || $item == '..' || $item == '.git') continue;
		unlink($gitDir.DIRECTORY_SEPARATOR.$item);
	}
	`cd $gitDir; git pull -q;git add .; git commit -qm "cleaning dir"; git push -q origin master`;

