package com.data2semantics.syncproject;


import java.util.logging.Level;
import java.util.logging.Logger;
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
	// Logger
	protected static final Logger logger = Logger.getLogger(EntryPoint.class.getName());
	
	
	
	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createInboundRoot() {
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
			getLogger().log(Level.SEVERE, "Unable to load config file " + configFile + ": " + e.getMessage());
		}
	
		//Load freemarker config
		fmConfiguration = new Configuration();
		fmConfiguration.setTemplateLoader(new ContextTemplateLoader(getContext(), getConfig().getString("master.restlet.templatesDir")));
	}
	
}