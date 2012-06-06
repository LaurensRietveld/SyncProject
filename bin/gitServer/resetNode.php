#!/usr/bin/php
<?php
	include(__DIR__."/../util.php");
	$config = getConfig();
	echo "==== ".basename(__DIR__).": Resetting ====\n";
	$gitDir = "/home/lrd900/syncProject";
	`cd $gitDir; rm -rf ./*; git init --bare;`;
