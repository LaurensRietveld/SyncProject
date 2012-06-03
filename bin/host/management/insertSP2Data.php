#!/usr/bin/php
<?php
	//$jsonString = var_export(file_get_contents("../../config/config.conf"));
	include_once(__DIR__."/../../util.php");
	$config = getConfig();
	$numberOfTriples = 1000;
	$dir = __DIR__."/../SP2Benchmark";
	$turtleFile = __DIR__."/output.n3";
	
	echo "==== Processing SP2Benchmark turtle file ====\n";
	if (!file_exists($turtleFile)) {
		echo "\tGenerating file\n";
		shell_exec("cd ".$dir." && sp2b_gen -t ".$numberOfTriples." ".$turtleFile.".bak");
		echo "\tReplacing blank nodes into URIs\n";
		//Do this, because the experiment tries to change triples on two triple stores using this same dataset
		//Because blank nodes have no persistent identifier, this because a bit more difficult. 
		//Therefore, change blank nodes into uris
		$prefix = "@prefix bn: <http://blanknode/> .\n";
		$triples = file_get_contents($turtleFile.".bak");
		$triples = $prefix.str_replace("_:", "bn:", $triples);
		file_put_contents($turtleFile, $triples);
		shell_exec("rm ".$turtleFile.".bak");
	} else {
		echo "\tSP2 file already exists. Using that one.\n";
	}

	echo "\tImporting to slave\n";
	//Could also use php curl, but commandline is easy:
	//curl -T output.n3 -H "Content-Type: text/rdf+n3;charset=UTF-8" http://localhost:8080/openrdf-sesame/repositories/master/statements
	shell_exec('curl --silent -T '.$turtleFile.' -H "Content-Type: text/rdf+n3;charset=UTF-8" '.$config['slave']['tripleStore']['importDataUri']);

	echo "\tImporting to master\n";
	//Could also use php curl, but commandline is easy:
	//curl -T output.n3 -H "Content-Type: text/rdf+n3;charset=UTF-8" http://localhost:8080/openrdf-sesame/repositories/master/statements
	shell_exec('curl --silent -T '.$turtleFile.' -H "Content-Type: text/rdf+n3;charset=UTF-8" '.$config['master']['tripleStore']['importDataUri']);