#!/usr/bin/php
<?php
	//$jsonString = var_export(file_get_contents("../../config/config.conf"));
	include_once(__DIR__."/../..//util.php");
	$config = getConfig();
	
	echo "==== Creating SP2Benchmark turtle file ====\n";
	$numberOfTriples = 1000;
	$dir = __DIR__."/../SP2Benchmark";
	$turtleFile = __DIR__."/output.n3";
	shell_exec("cd ".$dir." && sp2b_gen -t ".$numberOfTriples." ".$turtleFile);

	echo "==== Importing to slave ====\n";
	//Could also use php curl, but commandline is easy:
	//curl -T output.n3 -H "Content-Type: text/rdf+n3;charset=UTF-8" http://localhost:8080/openrdf-sesame/repositories/master/statements
	shell_exec('curl -T '.$turtleFile.' -H "Content-Type: text/rdf+n3;charset=UTF-8" '.$config['slave']['tripleStore']['importDataUri']);

	echo "==== Importing to master ====\n";
	//Could also use php curl, but commandline is easy:
	//curl -T output.n3 -H "Content-Type: text/rdf+n3;charset=UTF-8" http://localhost:8080/openrdf-sesame/repositories/master/statements
	shell_exec('curl -T '.$turtleFile.' -H "Content-Type: text/rdf+n3;charset=UTF-8" '.$config['slave']['tripleStore']['importDataUri']);