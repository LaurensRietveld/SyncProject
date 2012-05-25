package com.data2semantics.syncproject;


import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;

import freemarker.template.Configuration;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.representation.Representation;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import com.data2semantics.syncproject.resources.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


public class EntryPoint extends Application {
	private Configuration fmConfiguration; //Freemarker Configuration
	private Config config;//Typesafe config
	
	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createInboundRoot() {
		Handler fileHandler = null;
		try {
			fileHandler = new FileHandler("/usr/local/share/syncProject/logs/temp.txt");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getLogger().addHandler(fileHandler);
		getLogger().severe("In entrypoint");
		loadConfigurations();
		// Create a router Restlet that routes each call
		Router router = new Router(getContext());
		//Order is important here (especially since we use 'MODE_STARTS_WITH'
		router.attach("/query/{param}", QuerySelect.class, Template.MODE_STARTS_WITH);
		router.attach("/query", QuerySelect.class, Template.MODE_STARTS_WITH);
		router.attach("/query/", QuerySelect.class, Template.MODE_STARTS_WITH);
		
		router.attach("/update/{param}", QueryUpdate.class, Template.MODE_STARTS_WITH);
		router.attach("/update", QueryUpdate.class, Template.MODE_STARTS_WITH);
		router.attach("/update/", QueryUpdate.class, Template.MODE_STARTS_WITH);
		
		Redirector redirector = new Redirector(getContext(), "war:///index.html");
		router.attachDefault(redirector);
		
		//Activate content filtering based on extensions. Trick from Christophe for content negotiation. Not needed for now
		//getTunnelService().setExtensionsTunnel(true);
		return router;
	}
	
	/**
	 * Returns the Freemarker's configuration.
	 * 
	 * @return The Freemarker's configuration.
	 */
	public Configuration getFMConfiguration() {
		return fmConfiguration;
	}
	
	/**
	 * Get the Typesafe config object
	 * 
	 * @return
	 */
	public Config getConfig() {
		return config;
	}
	
	/**
	 * Load both the typesafe config, and the freemarker template config
	 */
	private void loadConfigurations() {
		//Load typesafe config
		Restlet client = getContext().getClientDispatcher();
		String configFile = "war:///WEB-INF/config/config.conf";
		try {
			Response response = client.handle(new Request(Method.GET, configFile));
			Representation rep = response.getEntity();
			this.config = ConfigFactory.parseString(rep.getText());
		} catch (Exception e) {
			getLogger().severe("Unable to load config file " + configFile + ": " + e.getMessage());
		}
	
		//Load freemarker config
		fmConfiguration = new Configuration();
		fmConfiguration.setTemplateLoader(new ContextTemplateLoader(getContext(), getConfig().getString("master.restlet.templatesDir")));
	}

	
}