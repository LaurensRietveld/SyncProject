package com.data2semantics.syncproject.daemon;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.data2semantics.syncproject.daemon.modes.ExecuteQueriesFromDb;
import com.data2semantics.syncproject.daemon.modes.ExecuteQueriesFromGit;
import com.data2semantics.syncproject.daemon.modes.ImportTriplesFromDb;
import com.data2semantics.syncproject.daemon.modes.ImportTriplesFromGit;
import com.data2semantics.syncproject.daemon.modes.ImportTriplesFromText;
import com.data2semantics.syncproject.daemon.modes.ExecuteQueriesFromText;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Daemon {
	static final String DEFAULT_CONFIG_FILE = "https://raw.github.com/LaurensRietveld/SyncProject/master/config/config.conf";
	private URL configFile;
	private Config config;
	private int mode;
	Daemon (int mode) {
		this.mode = mode;
	}
	
	public void initDaemon(String key, int experimentId)throws Exception {
		this.loadConfigFile();
		if (mode == ExecuteQueriesFromGit.MODE) {
			new ExecuteQueriesFromGit(config, key, experimentId);
		} else if (mode == ExecuteQueriesFromText.MODE) {
			new ExecuteQueriesFromText(config, key, experimentId);
		} else if (mode == ImportTriplesFromText.MODE) {
			new ImportTriplesFromText(config, key, experimentId);
		} else if (mode == ExecuteQueriesFromDb.MODE) {
			new ExecuteQueriesFromDb(config, key, experimentId);
		} else if (mode == ImportTriplesFromGit.MODE) {
			new ImportTriplesFromGit(config, key, experimentId);
		} else if (mode == ImportTriplesFromDb.MODE) {
			new ImportTriplesFromDb(config, key, experimentId);
		}
	}
	
	
	
	public void loadConfigFile() {
		//Load typesafe config
		try {
			configFile = new URL(DEFAULT_CONFIG_FILE);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		try {
			this.config = ConfigFactory.parseURL(configFile);
		} catch (Exception e) {
			System.out.println("ERROR: Failed loading config from " + this.configFile.toString() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption(new Option("help", "print this message"));
		options.addOption(OptionBuilder.withArgName("mode").hasArg().withDescription("Mode to run: (1) sync text queries; (2) use DB to sync text queries; (3) sync graph complete graph using rsync; (4) use central (git) server to sync queries; (5) Use central git server to sync graphs; (6) Use DB to sync triples of graph").create("mode"));
		options.addOption(OptionBuilder.withArgName("key").hasArg().withDescription("Used for experiment setup. This key is stored in experiments database, so the script running the experiments can check when the daemon has started.").create("key"));
		options.addOption(OptionBuilder.withArgName("experimentId").hasArg().withDescription("Used for experiment setup. This way we can link the daemon to an experiment.").create("experimentId"));
		
		CommandLineParser parser = new GnuParser();
		CommandLine commands = null;
	    try {
	        // parse the command line arguments
	        commands = parser.parse(options, args);
	    }
	    catch(ParseException exp) {
	        System.err.println( "Argument parsing failed.  Reason: " + exp.getMessage() );
	        System.exit(1);
	    }
        if (commands.hasOption("help") || args.length == 0) {
    		HelpFormatter formatter = new HelpFormatter();
    		formatter.printHelp("How to start this daemon", options);
    		System.exit(0);
        }
        
        int mode = 0;
	    if (!commands.hasOption("mode")) {
	    	System.out.println("No mode passed as parameter");
	    	System.exit(1);
	    } else {
	    	mode = Integer.parseInt(commands.getOptionValue("mode"));
	    	if (mode < 1 || mode > 6) {
	    		System.out.println("Incorrect mode passed as parameter. Currently implemented: 1,2 and 3");
	    		System.exit(1);
	    	}
	    }
	    
        Daemon daemon = new Daemon(mode);
        String key = "";
        if (commands.hasOption("key")) {
        	key = commands.getOptionValue("key");
        }
        int experimentId = 0;
        if (commands.hasOption("experimentId")) {
        	experimentId = Integer.parseInt(commands.getOptionValue("experimentId"));
        }
		try {
			daemon.initDaemon(key, experimentId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
