package com.data2semantics.syncproject.logging;

import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.util.QueryTypes;
import com.data2semantics.syncproject.util.Util;
import com.hp.hpl.jena.query.ResultSet;
import com.typesafe.config.Config;


public class SerializeGraphDb extends GenericLogger{
	
	public SerializeGraphDb(boolean batchLogging, MainServerResource main) {
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
	    	ResultSet result = Util.query(endpoint, "SELECT * WHERE {?subject ?predicate ?object}");
	    	getMain().getLogger().info("going to process query and write all results to file");
	    	
		}
	}

}
