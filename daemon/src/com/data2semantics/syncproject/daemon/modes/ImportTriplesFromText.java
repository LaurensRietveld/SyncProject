package com.data2semantics.syncproject.daemon.modes;

import java.io.File;
import com.data2semantics.syncproject.daemon.util.Util;
import com.typesafe.config.Config;

public class ImportTriplesFromText extends Mode implements ModeInterface {
	public static int MODE = 3;
	private File dumpFile;
	private String updateUri;
	private long fileLastModified = 0;
	public ImportTriplesFromText(Config config) throws Exception {
		super(config);
		this.dumpFile = new File(config.getString("slave.serializationDir") + "/" + config.getString("serializationMode.dumpFile"));
		this.updateUri = config.getString("slave.tripleStore.updateUri");
		runDaemon();
	}
	
	
	/**
	 * Process xml graph dump. Always inserts graph on first run. After that, check if file has been modified, and ignores it if not.
	 * @throws Exception 
	 */
	public void process() throws Exception {
		if (!dumpFile.exists()) {
			//System.out.println("WARNING: dump file does not exist");
		} else if (dumpFile.length() > 0 && dumpFile.lastModified() != this.fileLastModified) {
			System.out.print(".");
			Util.importDumpFile(dumpFile, updateUri);
			System.out.println(".");
			storeExperimentInfo(MODE);
			this.fileLastModified = dumpFile.lastModified();
		}
	}
	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println("Running slave daemon in mode: " + Integer.toString(MODE));
		while (true) {
			process();
			sleep(this.sleepInterval);
		}
	}
}
