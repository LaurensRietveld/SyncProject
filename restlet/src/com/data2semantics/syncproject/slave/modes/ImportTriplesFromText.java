package com.data2semantics.syncproject.slave.modes;

import java.io.File;

import com.data2semantics.syncproject.resources.UpdateSlave;
import com.data2semantics.syncproject.util.Util;
import com.typesafe.config.Config;

public class ImportTriplesFromText extends Mode implements ModeInterface {
	public static int MODE = 3;
	private File dumpFile;
	private String updateUri;
	private long fileLastModified = 0;
	public ImportTriplesFromText(UpdateSlave main, Config config, String key) throws Exception {
		super(main, config, key);
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
			System.out.println(Util.getTime() + "importing (mode" + Integer.toString(MODE) +")");
			Util.importDumpFile(dumpFile, updateUri);
			System.out.println(Util.getTime() + "done");
			storeExperimentInfo(MODE);
			this.fileLastModified = dumpFile.lastModified();
		}
	}
	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println(Util.getTime() + "- Running slave daemon in mode: " + Integer.toString(MODE));
		storeKey();
		while (true) {
			process();
			sleep(this.sleepInterval);
		}
	}
}
