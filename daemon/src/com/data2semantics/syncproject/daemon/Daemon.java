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
import com.data2semantics.syncproject.daemon.master.MasterDaemon;
import com.data2semantics.syncproject.daemon.slave.SlaveDaemon;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Daemon {
	static final String DEFAULT_CONFIG_FILE = "https://raw.github.com/LaurensRietveld/SyncProject/master/restlet/WebContent/WEB-INF/config/config.conf";
	private URL configFile;
	private Config config;
	private String mode;
	Daemon (String mode) {
		this.mode = mode;
	}
	
	public void runDaemon() {
		this.loadConfigFile();
		
		
		if (this.mode.equals("slave")) {
			SlaveDaemon daemon = new SlaveDaemon(config);
			daemon.runDaemon();
		} else if (this.mode.equals("master")) {
			MasterDaemon daemon = new MasterDaemon(config);
			daemon.runDaemon();
		} else {
			System.out.println("No valid mode passed as parameter: " + this.mode);
			System.exit(1);
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
		options.addOption(OptionBuilder.withArgName("file").hasArg().withDescription("Use this typesafe configuration file").create("config"));
		options.addOption(OptionBuilder.withArgName("mode").hasArg().withDescription("Daemon Mode to run in, either 'master' or 'slave'").create("mode"));
		
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
        
	    if (!commands.hasOption("mode")) {
	    	System.out.println("No mode passed as parameter");
	    	System.exit(1);
	    }
        String mode = commands.getOptionValue("mode");

        Daemon daemon = new Daemon(mode);
        
//        if (commands.hasOption("config")) {
//        	daemon.loadConfigFile();
//        } 
		daemon.runDaemon();
	}

}
