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

import com.data2semantics.syncproject.daemon.modes.SlaveDb;
import com.data2semantics.syncproject.daemon.modes.SlaveGit;
import com.data2semantics.syncproject.daemon.modes.SlaveImportDump;
import com.data2semantics.syncproject.daemon.modes.SlaveTextQuery;
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
	
	public void initDaemon()throws Exception {
		this.loadConfigFile();
		if (mode == SlaveGit.MODE) {
			new SlaveGit(config);
		} else if (mode == SlaveTextQuery.MODE) {
			new SlaveTextQuery(config);
		} else if (mode == SlaveImportDump.MODE) {
			new SlaveImportDump(config);
		} else if (mode == SlaveDb.MODE) {
			new SlaveDb(config);
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
		options.addOption(OptionBuilder.withArgName("mode").hasArg().withDescription("Mode to run: (1) sync text queries; (2) use DB; (3) sync graph; (4) central (git) server").create("mode"));
		
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
	    	if (mode < 1 || mode > 4) {
	    		System.out.println("Incorrect mode passed as parameter. Currently implemented: 1,2 and 3");
	    		System.exit(1);
	    	}
	    }
        Daemon daemon = new Daemon(mode);
        
		try {
			daemon.initDaemon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
