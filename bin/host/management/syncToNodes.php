#!/usr/bin/php
<?php
	//$jsonString = var_export(file_get_contents("../../config/config.conf"));
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	
	$rsyncCommand = "rsync -aqvz";
	$syncProject = "/home/lrd900/code/syncProject/";
	
	echo "==== Syncing builds ====\n";
	$server = $config['master']['serverLocation'].":";
	shell_exec($rsyncCommand." ".$syncProject."daemon/dist/daemon.jar ".$server.$config['master']['daemonFile']);
	shell_exec($rsyncCommand." ".$syncProject."sesameExport/dist/sesameExport.jar ".$server.$config['master']['exportToXmlJar']);
	shell_exec($rsyncCommand." ".$syncProject."restlet/dist/syncRestlet.war ".$server."/var/lib/tomcat6/webapps/");

	$server = $config['slave']['serverLocation'].":";
	shell_exec($rsyncCommand." ".$syncProject."daemon/dist/daemon.jar ".$server.$config['master']['daemonFile']);
	echo "done\n";

