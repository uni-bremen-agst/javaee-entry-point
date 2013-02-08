package soot.jimple.toolkits.javaee.detectors;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.toolkits.javaee.model.servlet.FileLoader;
import soot.jimple.toolkits.javaee.model.servlet.Parameter;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.struts1.io.StrutsReader;

/**
 * Detects Struts 1 servlets and creates servlets for actions.
 * 
 * @author Bernhard Berger
 */
public class StrutsServletDetector extends AbstractServletDetector {
	/**
	 * Logger
	 */
	private final static Logger LOG = LoggerFactory.getLogger(StrutsServletDetector.class);

	@Override
	public void detectFromSource(Web web) {
		LOG.warn("Detecting struts actions from source is not yet implemented.");
	}

	@Override
	public void detectFromConfig(final Web web) {
		LOG.info("Detecting struts actions from configuration file");
		for(final Servlet servlet : web.getServlets()) {
			if(!servlet.getClazz().equals("org.apache.struts.action.ActionServlet")) {
				continue;
			}
			
			LOG.info("Found action servlet {}", servlet);
			
			final FileLoader fileLoader = servlet.getLoader();

			for(final Parameter parameter : servlet.getParameters()) {
				if(parameter.getName().equals("config")) {
					for(final String xmlFile : parameter.getValue().split(",")) {
						LOG.info("Found configuration file {}.", xmlFile);
						try {
							final InputStream inputStream = fileLoader.getInputStream(xmlFile);
							StrutsReader.readStrutsConfig(web, inputStream);
						} catch(final IOException e) {
							LOG.error("Unable to load configuration file.", e);
						}
					}
				}
			}
		}
	}
}
