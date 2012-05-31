package com.data2semantics.syncproject;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Resource;
import org.restlet.service.StatusService;

import com.data2semantics.syncproject.util.Util;


public class ErrorStatusService extends StatusService {
	EntryPoint entryPoint;
	
	ErrorStatusService(EntryPoint entryPoint) {
		this.entryPoint = entryPoint; 
	}
	
	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
	    String text;
	    if (status.getDescription() != null) {
	        text = status.getDescription();
	    } else {
	        text = "unknown error";
	    }
	    return Util.getErrorPage(entryPoint, text);
	}
	@Override
	public Status getStatus(Throwable throwable, Resource resource) {
        Status status = super.getStatus(throwable, resource);
		StringWriter sw = new StringWriter(2000);
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		this.entryPoint.getLogger().severe("Exception: " + throwable.getMessage());
	    return new Status(status, sw.getBuffer().toString());
	}
}