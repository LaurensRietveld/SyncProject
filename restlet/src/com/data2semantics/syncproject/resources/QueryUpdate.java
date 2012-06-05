package com.data2semantics.syncproject.resources;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Resource which has only one representation.
 * 
 */
public class QueryUpdate extends MainServerResource {
	
	@Get
	public Representation getEntryPoint() throws Exception {
		return processGet();
	}

	
	@Post
	public Representation postEntryPoint(Representation entity) throws Exception {
		return processPost(entity);
	}
}