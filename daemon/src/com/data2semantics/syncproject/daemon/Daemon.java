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
import com.data2semantics.syncproject.daemon.slave.SlaveTextQuery;
import com.data2semantics.syncproject.daemon.slave.SlaveXml;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Daemon {
	static final String DEFAULT_CONFIG_FILE = "https://raw.github.com/LaurensRietveld/SyncProject/master/restlet/WebContent/WEB-INF/config/config.conf";
	private URL configFile;
	private Config config;
	private int mode;
	private String role;
	Daemon (String role, int mode) {
		this.mode = mode;
		this.role = role;
	}
	
	public void initDaemon() {
		this.loadConfigFile();
		
		if (this.role.equals("slave")) {
			this.initSlaveDaemon();
		} else if (this.role.equals("master")) {
			this.initMasterDaemon();
		} else {
			System.out.println("No valid role passed as parameter: " + this.role);
			System.exit(1);
		}
	}
	
	private void initSlaveDaemon() {
		SlaveDaemon daemon = null;
		if (this.mode == 1) {
			daemon = new SlaveTextQuery(config);
		} else if (this.mode == 3) {
			daemon = new SlaveXml(config);
		}
		daemon.runDaemon();
	}
	
	private void initMasterDaemon() {
		MasterDaemon daemon = new MasterDaemon(config, mode);
		daemon.runDaemon();
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
		options.addOption(OptionBuilder.withArgName("role").hasArg().withDescription("Role to run in, either 'master' or 'slave'").create("role"));
		options.addOption(OptionBuilder.withArgName("mode").hasArg().withDescription("Mode to run: (1) sync text queries; (3) sync graph").create("mode"));
		
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
        
	    if (!commands.hasOption("role")) {
	    	System.out.println("No role passed as parameter");
	    	System.exit(1);
	    }
        String role = commands.getOptionValue("role");
        int mode = 0;
	    if (!commands.hasOption("mode")) {
	    	System.out.println("No mode passed as parameter");
	    	System.exit(1);
	    } else {
	    	mode = Integer.parseInt(commands.getOptionValue("mode"));
	    	if (mode != 1 && mode != 3) {
	    		System.out.println("Incorrect mode passed as parameter. Currently implemented: 1 or 3");
	    		System.exit(1);
	    	}
	    }
        Daemon daemon = new Daemon(role, mode);
        
//        if (commands.hasOption("config")) {
//        	daemon.loadConfigFile();
//        } 
		daemon.initDaemon();
	}

}
