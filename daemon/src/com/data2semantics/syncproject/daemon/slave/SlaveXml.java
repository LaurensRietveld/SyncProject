package com.data2semantics.syncproject.daemon.slave;

import java.io.File;
import com.typesafe.config.Config;

public class SlaveXml extends SlaveDaemon {
	public static int MODE = 3;
	File xmlFile;
	public SlaveXml(Config config) {
		super(config, MODE);
		this.xmlFile = new File(config.getString("slave.queryLogDir") + "/" + config.getString("mode2.dumpFile"));
	}
	
	
	public void processFiles() {
		
		if (!xmlFile.exists()) {
			System.out.println("ERROR: XML dump file does not exist. Exiting");
			System.exit(1);
		}
		this.importXml();
	}
	
	private void importXml() {
		//con.add(file, baseURI, RDFFormat.RDFXML);
	}
	
	
}
