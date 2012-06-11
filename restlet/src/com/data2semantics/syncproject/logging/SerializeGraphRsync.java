package com.data2semantics.syncproject.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.util.QueryTypes;
import com.data2semantics.syncproject.util.Util;
import com.hp.hpl.jena.query.ResultSet;
import com.typesafe.config.Config;

public class SerializeGraphRsync extends GenericLogger{
	
	public SerializeGraphRsync(boolean batchLogging, MainServerResource main) {
		super(batchLogging, main);
	}
	

    public void log(com.data2semantics.syncproject.resources.Query query) throws Exception {
    	//Do nothing. No need to perform logging for each query, as the query itself is not logged.
    }
    
    
	public void loggingCallback() throws Exception {
		//No need to export when data hasnt changed (i.e. only on update)
		if (getMain().getSparqlQueryType() == QueryTypes.UPDATE) {
			Config config = getMain().getApplication().getConfig();
			
	    	String endpoint = config.getString("master.tripleStore.selectUri");
			File exportFile = new File(config.getString("master.serializationDir") + "/" + config.getString("serializationMode.dumpFile"));
	    	//empty file if it exists (do not create new one: file permissions get screwed up)
	    	if (exportFile.exists()) {
	            BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
	            bw.write("");
	            bw.flush();
	            bw.close();
	    	}
	    	ResultSet result = Util.query(endpoint, "SELECT * WHERE {?subject ?predicate ?object}");
	    	getMain().getLogger().info("going to process query and write all results to file");
	    	
	    	Util.writeSerializationToFile(result, exportFile);
	    	String destFile = "sproject@" + config.getString("slave.serverLocation") + ":" + config.getString("slave.serializationDir") + "/" + config.getString("serializationMode.dumpFile");
	    	Util.rsync(exportFile, destFile);
		}
	}
	
}
