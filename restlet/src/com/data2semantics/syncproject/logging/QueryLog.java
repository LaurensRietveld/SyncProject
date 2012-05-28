package com.data2semantics.syncproject.logging;

import java.io.File;
import com.data2semantics.syncproject.logging.modes.*;
import com.data2semantics.syncproject.resources.Query;
import com.typesafe.config.Config;

public class QueryLog {
	
	public static final int PLAIN_TEXT_FILE = 1;
	public static final int DB = 2;
	public static final int EXPORT_GRAPHS = 3;
	public static final int CENTRAL_SERVER = 4;
	
	
	public static void log(Query query) throws Exception {
		Config config = query.getApplication().getConfig();
		int logType = query.getMode();
		query.getApplication().getLogger().info("Using mode " + Integer.toString(logType));
        switch (logType) {
            case PLAIN_TEXT_FILE:
            	TextLogger.log(query);
            	break;
            case EXPORT_GRAPHS:
            	//No need to export graphs when it is a simple select query
            	if (query.getSparqlQueryType().equals("update")) {
            		SesameExportGraph.export(
            			new File(config.getString("master.exportToXmlJar")),
            			config.getString("master.tripleStore.sesameApi"),
            			config.getString("master.repoId"),
            			new File(config.getString("master.xmlDumpDir") + "/" + config.getString("mode3.dumpFile")));
            	}
            	break;
            case DB:
            	DbLogger.log(query);
            	break;
        	default:
        		throw new NoSuchFieldException("ERROR: No valid logtype provided");
        }
	}
}
