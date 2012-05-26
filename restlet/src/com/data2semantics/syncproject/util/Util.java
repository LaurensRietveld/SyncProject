package com.data2semantics.syncproject.util;

import java.util.HashMap;
import java.util.Map;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;

import com.data2semantics.syncproject.EntryPoint;

import freemarker.template.Configuration;

public class Util  {

	
		public static Representation getQueryForm(EntryPoint entryPoint, boolean isUpdateQuery, String uri) {
		Configuration configuration = entryPoint.getFMConfiguration();
		Map<String, Object> map = new HashMap<String, Object>();
		
		String queryType = isUpdateQuery? "update": "query";
		map.put("queryType", queryType);
		map.put("action", uri.endsWith("/")? "": queryType);
		
		return new TemplateRepresentation("query.ftl", configuration, map, MediaType.TEXT_HTML);
	}
		
	public static Representation getErrorPage(EntryPoint entryPoint, String error) {
		entryPoint.getLogger().severe(error);
		Configuration configuration = entryPoint.getFMConfiguration();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", error.replace("\t", "&nbsp;&nbsp;&nbsp;").replace("\n", "<br>"));
		return new TemplateRepresentation("error.ftl", configuration, map, MediaType.TEXT_HTML);
	}
}