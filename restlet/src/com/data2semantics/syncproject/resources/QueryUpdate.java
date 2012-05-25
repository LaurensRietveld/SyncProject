package com.data2semantics.syncproject.resources;

import java.util.Map;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import com.data2semantics.syncproject.util.QueryTypes;

/**
 * Resource which has only one representation.
 * 
 */
public class QueryUpdate extends Query {
	
	@Get
	public Representation processGet() {
		getLogger().info("Processing GET"); 
		Map<String, Object> requestAttributes = getRequestAttributes();
		String query = "";
		if (requestAttributes.containsKey("param") ) {
			String parameters = (String)getRequestAttributes().get("param");
			Form form = new Form(parameters);
			query = form.getFirstValue("query");
			this.setMode(Integer.parseInt(form.getFirstValue("mode")));
		}
		return processQuery(query, QueryTypes.UPDATE);
	}

	
	@Post
	public Representation processPost(Representation entity) {
		getLogger().info("Processing POST");
        Form form = new Form(entity);
        this.setMode(Integer.parseInt(form.getFirstValue("mode")));
        return this.processQuery(form.getFirstValue("query"), QueryTypes.UPDATE);
	}
}