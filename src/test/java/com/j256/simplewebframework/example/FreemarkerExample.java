package com.j256.simplewebframework.example;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import com.j256.simplewebframework.displayer.FileResultDisplayer;
import com.j256.simplewebframework.displayer.ResultDisplayer;
import com.j256.simplewebframework.displayer.StringResultDisplayer;
import com.j256.simplewebframework.freemarker.FreemarkerHtmlDisplayer;
import com.j256.simplewebframework.handler.LoggingHandler;
import com.j256.simplewebframework.handler.ServiceHandler;
import com.j256.simplewebframework.resource.FileLocator;
import com.j256.simplewebframework.resource.LocalResourceHandler;

import freemarker.template.Configuration;
import freemarker.template.Version;

/**
 * Little sample program which starts a test web-server up on port 8080 which demonstrates some of the features of the
 * SimpleWebFramework.
 * 
 * @author graywatson
 */
public class FreemarkerExample {

	/** default web port that we will server jetty results on */
	private static final int DEFAULT_WEB_PORT = 8080;

	public static void main(String[] args) throws Exception {

		// create jetty server
		Server server = new Server();
		// create the connector which receives HTTPD connections
		SelectChannelConnector connector = new SelectChannelConnector();
		// start it on the default port
		connector.setPort(DEFAULT_WEB_PORT);
		connector.setReuseAddress(true);
		server.addConnector(connector);

		// display freemarker files
		FreemarkerHtmlDisplayer htmlDisplayer = new FreemarkerHtmlDisplayer();
		// find templates in this directory
		File templateDir = new File("src/test/resources/com/j256/simplewebframework/example");
		FileLocator fileLocator = new FileLocator(templateDir, new String[] { "index.html" });
		htmlDisplayer.setFileLocator(fileLocator);
		// freemarker configuration on where to find templates
		Configuration freeMarkerConfig = new Configuration(new Version("2.3.22"));
		freeMarkerConfig.setDirectoryForTemplateLoading(templateDir);
		htmlDisplayer.setTemplateConfig(freeMarkerConfig);

		/*
		 * setup the handlers which handle the request
		 */

		// create a service handler
		ServiceHandler serviceHandler = new ServiceHandler();
		// register our service
		serviceHandler.registerWebService(new OurService());
		// register a displayer of String results
		serviceHandler.registerResultDisplayer(new StringResultDisplayer());
		serviceHandler.registerResultDisplayer(htmlDisplayer);

		// handles CSS, JS, images and other files served raw
		LocalResourceHandler resourceHandler = new LocalResourceHandler();
		// process the html files using the freemarker displayer
		Map<String, ResultDisplayer> displayerMap = new HashMap<String, ResultDisplayer>();
		displayerMap.put("html", htmlDisplayer);
		resourceHandler.setFileExtensionDisplayers(displayerMap);
		// everything else uses the file result displayer
		resourceHandler.setDefaultDisplayer(new FileResultDisplayer());
		resourceHandler.setFileLocator(fileLocator);

		// a collection of handlers which are tried one after another
		HandlerCollection handlerCollection = new HandlerCollection();
		handlerCollection.addHandler(serviceHandler);
		handlerCollection.addHandler(resourceHandler);

		// wrap the handler collection in a logging handler so we can see the page-view logs
		LoggingHandler loggingHandler = new LoggingHandler();
		loggingHandler.setHandler(handlerCollection);

		// this could be a collection of handlers or ...
		server.setHandler(loggingHandler);
		server.start();

		System.out.println("Web-server running on port " + DEFAULT_WEB_PORT);

		/*
		 * The main thread stops but the application keeps on running because of the jetty threads. To stop the
		 * application, you will need to kill it
		 */
	}

	/**
	 * Small web service which presents a simple form to the user and displays the results if any.
	 */
	@WebService
	@Produces({ "text/html" })
	protected static class OurService {

		@Path("/")
		@GET
		@WebMethod
		public String root(//
				@QueryParam("value")//
				String value) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><body>\n");
			sb.append("<h1> OurService Web Server </h1>\n");
			if (value != null) {
				sb.append("<p> value is '" + value + "' </p>\n");
			}
			sb.append("<p><form>\n");
			sb.append("Please enter value: <input name='value' type='text'");
			if (value != null) {
				sb.append(" value='").append(value).append("'");
			}
			sb.append(" />");
			sb.append("<input type='submit' />\n");
			sb.append("</form></p>\n");
			sb.append("</body></html>\n");
			return sb.toString();
		}
	}
}