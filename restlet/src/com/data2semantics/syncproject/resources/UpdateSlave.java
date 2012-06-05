package com.data2semantics.syncproject.resources;

import org.restlet.data.Form;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import com.data2semantics.syncproject.EntryPoint;
import com.data2semantics.syncproject.slave.modes.ExecuteQueriesFromDb;
import com.data2semantics.syncproject.slave.modes.ExecuteQueriesFromGit;
import com.data2semantics.syncproject.slave.modes.ExecuteQueriesFromText;
import com.data2semantics.syncproject.slave.modes.ImportTriplesFromDb;
import com.data2semantics.syncproject.slave.modes.ImportTriplesFromGit;
import com.data2semantics.syncproject.slave.modes.ImportTriplesFromText;
import com.typesafe.config.Config;

/**
 * Resource which has only one representation.
 * 
 */
public class UpdateSlave extends ServerResource {
	
	private int mode;
	private String key;
	private Config config;
	@Get
	public Representation getEntryPoint() throws Exception {
		String parameters = (String)getRequestAttributes().get("param");
		parseParams(new Form(parameters));
		config = getApplication().getConfig();
		getLogger().info("Processing slave GET in mode " + Integer.toString(this.getMode()));
		
		return processUpdate();
	}

	
	@Post
	public Representation postEntryPoint(Representation entity) throws Exception {
		parseParams(new Form(entity));
		getLogger().info("Processing slave POST in mode " + Integer.toString(this.getMode()));
		config = getApplication().getConfig();
		return processUpdate();
	}
	
	public Representation processUpdate() throws Exception {
		if (mode == ExecuteQueriesFromGit.MODE) {
			new ExecuteQueriesFromGit(this, config, key);
		} else if (mode == ExecuteQueriesFromText.MODE) {
			new ExecuteQueriesFromText(this, config, key);
		} else if (mode == ImportTriplesFromText.MODE) {
			new ImportTriplesFromText(this, config, key);
		} else if (mode == ExecuteQueriesFromDb.MODE) {
			new ExecuteQueriesFromDb(this, config, key);
		} else if (mode == ImportTriplesFromGit.MODE) {
			new ImportTriplesFromGit(this, config, key);
		} else if (mode == ImportTriplesFromDb.MODE) {
			new ImportTriplesFromDb(this, config, key);
		}
		return new EmptyRepresentation();
	}
	
	private void parseParams(Form form) {
//		Map<String, Object> requestAttributes = getRequestAttributes();
//		boolean hasQuery = false;
//		getLogger().info("Processing GET in mode " + Integer.toString(this.getMode()));
//		if (requestAttributes.containsKey("param")) {
//			String parameters = (String) getRequestAttributes().get("param");
//			hasQuery = parseParams(new Form(parameters));
//		}
		
		if (form.getFirstValue("mode") != null) {
        	this.setMode(Integer.parseInt(form.getFirstValue("mode")));
        }
		if (form.getFirstValue("key") != null) {
        	this.setKey(form.getFirstValue("key"));
        }
	}
	
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
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}
	
}