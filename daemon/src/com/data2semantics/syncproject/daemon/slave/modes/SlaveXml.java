package com.data2semantics.syncproject.daemon.slave.modes;

import java.io.File;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import com.typesafe.config.Config;

public class SlaveXml extends SlaveMode implements ModeInterface {
	public static int MODE = 3;
	private File xmlFile;
	private String sesameServer;
	private String repositoryId;
	private long fileLastModified = 0;
	public SlaveXml(Config config) {
		super(config);
		this.xmlFile = new File(config.getString("slave.xmlDumpDir") + "/" + config.getString("mode3.dumpFile"));
		this.sesameServer = config.getString("slave.tripleStore.sesameApi");
		this.repositoryId = config.getString("slave.repoId");
		
	}
	
	
	/**
	 * Process xml graph dump. Always inserts graph on first run. After that, check if file has been modified, and ignores it if not.
	 * @throws Exception 
	 */
	public void process() throws Exception {
		
		if (!xmlFile.exists()) {
			System.out.println("ERROR: XML dump file does not exist. Exiting");
			System.exit(1);
		}
		if (xmlFile.length() > 0 && xmlFile.lastModified() != this.fileLastModified) {
			System.out.println("Importing graph");
			this.importXml();
			this.fileLastModified = xmlFile.lastModified();
		}
	}
	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println("Running slave daemon in mode: " + Integer.toString(MODE));
		while (true) {
			sleep(this.sleepInterval);
			process();
		}
	}
	private void importXml() throws Exception {
		Repository repo = new HTTPRepository(sesameServer, repositoryId);
		repo.initialize();
		RepositoryConnection con = repo.getConnection();
		con.clear(); //Clear content of repository
		con.add(xmlFile, "", RDFFormat.RDFXML);
	}

}
