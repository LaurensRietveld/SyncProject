package com.data2semantics.syncproject.resources;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import com.data2semantics.syncproject.util.QueryTypes;

/**
 * Resource which has only one representation.
 * 
 */
public class QueryUpdate extends MainServerResource {
	
	@Get
	public Representation getEntryPoint() throws Exception {
		setSparqlQueryType(QueryTypes.UPDATE);
		return processGet();
	}

	
	@Post
	public Representation postEntryPoint(Representation entity) throws Exception {
		setSparqlQueryType(QueryTypes.UPDATE);
		return processPost(entity);
	}
}