package com.data2semantics.syncproject.resources;

import java.util.Map;
import org.restlet.data.Form;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import com.data2semantics.syncproject.EntryPoint;
import com.data2semantics.syncproject.master.logging.*;
import com.data2semantics.syncproject.util.Util;

/**
 * Resource which has only one representation.
 * 
 */
public class MainServerResource extends ServerResource {
	private int mode = 1;
	protected String[] queries;
	private String sparqlQueryType;
	private boolean batchLogging = true;
	private GenericLogger queryLogger;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#getApplication()
	 */
	@Override
	public EntryPoint getApplication() {
		return (EntryPoint) super.getApplication();
	}
	
	/**
	 * Set mode of restlet to work in. (e.g. log text queries, dump triples as xml, use db, central server)
	 * possible values: PLAIN_TEXT_FILE = 1, DB = 2, EXPORT_GRAPHS = 3, CENTRAL_SERVER = 4
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	/**
	 * Get mode of restlet to work in. (e.g. log text queries, dump triples as xml, use db, central server)
	 * possible values: PLAIN_TEXT_FILE = 1, DB = 2, EXPORT_GRAPHS = 3, CENTRAL_SERVER = 4
	 * @return int
	 */
	public int getMode() {
		return this.mode;
	}
	
	/*
	 * Parse parameters.
	 * 
	 * @return false if no query passed as parameter, true otherwise
	 */
	protected boolean parseParams(Form form) {
		boolean result;
		if (form.getFirstValue("mode") != null) {
        	this.setMode(Integer.parseInt(form.getFirstValue("mode")));
        }
		//String query = form.getFirstValue("query");
		
        String[] formQueries = form.getValuesArray("query");
        if (formQueries != null && formQueries.length > 0) {
        	queries = formQueries;
        	result = true;
        } else {
        	result = false;
        }
		return result;
	}
	
	protected Representation processGet() throws Exception {
		Representation result = new EmptyRepresentation();
		Map<String, Object> requestAttributes = getRequestAttributes();
		boolean hasQuery = false;
		getLogger().info("Processing GET in mode " + Integer.toString(this.getMode()));
		if (requestAttributes.containsKey("param")) {
			String parameters = (String) getRequestAttributes().get("param");
			hasQuery = parseParams(new Form(parameters));
		}
		initLogger();
		if (hasQuery) {
			for (String queryString : this.queries) {
				Query query = new Query(queryString, (MainServerResource)this);
				result = query.processQuery();
			}
			if (getQueryLogger().useBatchLogging()) getQueryLogger().loggingCallback();
		} else {
			getLogger().info("no query to executy. Loading query form");
			result = Util.getQueryForm(getApplication(), false, getReference().toString());
		}
		return result;
	}
	
	protected Representation processPost(Representation entity) throws Exception {
		Representation result = new EmptyRepresentation();
        boolean hasQuery = parseParams(new Form(entity));
        initLogger();
        getLogger().info("Processing POST in mode " + Integer.toString(this.getMode()));
        if (hasQuery) {
        	getLogger().info("processing " + Integer.toString(queries.length) + " queries");
        	for (String queryString: this.queries) {
        		Query query = new Query(queryString, (MainServerResource)this);
            	result = query.processQuery();
        	}
        	if (getQueryLogger().useBatchLogging()) getQueryLogger().loggingCallback();
        } else {
        	getLogger().info("no query to executy. Loading query form");
			result = Util.getQueryForm(getApplication(), false, getReference().toString());
        }
        return result;
	}

	public String getSparqlQueryType() {
		return sparqlQueryType;
	}

	public void setSparqlQueryType(String sparqlQueryType) {
		this.sparqlQueryType = sparqlQueryType;
	}
	
	protected void initLogger() throws Exception {
		switch (mode) {
	        case GenericLogger.LOG_QUERIES_RSYNC:
	        	queryLogger = new LogQueriesRsync(batchLogging, this);
	        	break;
	        case GenericLogger.SERIALIZE_GRAPH_RSYNC:
	        	queryLogger = new SerializeGraphRsync(batchLogging, this);
	        	break;
	        case GenericLogger.LOG_QUERIES_DB:
	        	queryLogger = new LogQueriesDb(batchLogging, this);
	        	break;
	        case GenericLogger.LOG_QUERIES_GIT:
	        	queryLogger = new LogQueriesGit(batchLogging, this);
	        	break;
	        case GenericLogger.SERIALIZE_GRAPH_GIT:
	        	queryLogger = new SerializeGraphGit(batchLogging, this);
	        	break;
	        case GenericLogger.SERIALIZE_GRAPH_DB:
	        	queryLogger = new SerializeGraphDb(batchLogging, this);
	        	break;   	
	    	default:
	    		throw new NoSuchFieldException("ERROR: No valid logtype provided");
	    }
	}
	
	public GenericLogger getQueryLogger() {
		return this.queryLogger;
	}
}