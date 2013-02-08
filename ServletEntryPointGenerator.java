package soot.jimple.toolkits.javaee;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.common.tools.FileTool;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.Singletons;
import soot.SootClass;
import soot.jimple.toolkits.javaee.detectors.GenericServletDetector;
import soot.jimple.toolkits.javaee.detectors.ServletDetector;
import soot.jimple.toolkits.javaee.detectors.WebServiceDetector;
import soot.jimple.toolkits.javaee.model.servlet.Web;

/**
 * This class drives the generation of a main method that creates and calls
 *   all configured servlets. This is necessary to calculate a correct call
 *   graph for web projects.
 *
 * @author Bernhard Berger
 */
public class ServletEntryPointGenerator extends SceneTransformer implements Signatures {
 
	/**
	 * Logging facility.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(ServletEntryPointGenerator.class);
	
	/**
	 * List of all available servlet detectors.
	 */
	private final List<ServletDetector> servletDetectors = new ArrayList<ServletDetector>();
	
	/**
	 * @return The singleton instance.
	 */
	public static ServletEntryPointGenerator v() {
    	return G.v().soot_jimple_toolkits_javaee_ServletEntryPointGenerator();
    }

	/**
	 * Shall all servlets within the application be handled or just the ones
	 *   that are configured within a {@code web.xml}. Can be set with the
	 *   commandline parameter {@code consider-all-servlets}.
	 */
	private boolean considerAllServlets = false;

	/**
	 * Scene for lookup of classes.
	 */
	private final Scene scene = Scene.v();
	
	public ServletEntryPointGenerator(final Singletons.Global g) {
		servletDetectors.add(new GenericServletDetector());
		servletDetectors.add(new WebServiceDetector());
	}
	
	/**
	 * The model.
	 */
	private Web web = new Web();

	/**
	 * Loads the {@code web.xml} or fakes it if the corresponding command line
	 *   parameter was given.
	 *   
	 * @todo Implement proper war support for locator.
	 */
	private void loadWebXML() {
		if(considerAllServlets) {
			for(final ServletDetector dector : servletDetectors) {
				dector.detectFromSource(web);
			}
		} else {
			for(final ServletDetector dector : servletDetectors) {
				dector.detectFromConfig(web);
			}
		}
	}

	@Override
	protected void internalTransform(final String phaseName, @SuppressWarnings("rawtypes") final Map options) {
		// configure logging		
		LOG.info("Running " + phaseName);
		
		considerAllServlets = PhaseOptions.getBoolean(options, "consider-all-servlets");
		
		loadWebXML();
		
		final String modelDestination = PhaseOptions.getString(options, "dump-model");
		if(!modelDestination.isEmpty()) {
			storeModel(modelDestination);
		}

		try {
			LOG.info("Processing templates");
			processTemplate(options);
		} catch(final ResourceNotFoundException e) {
			LOG.error("Could not find template file.");
		} catch(final ParseErrorException e) {
			LOG.error("Failed to parse the template.");
		} catch(final MethodInvocationException e) {
			LOG.error("Error while calling Java code from template.");
			e.printStackTrace();
		}
		
		LOG.info("Loading main class.");
		final SootClass sootClass = scene.forceResolve(PhaseOptions.getString(options, "root-package") + "." + PhaseOptions.getString(options, "main-class"), SootClass.BODIES);
		scene.setMainClass(sootClass);
	}

	/**
	 * Processes all templates.
	 */
	private void processTemplate(@SuppressWarnings("rawtypes") final Map options) {
		final VelocityEngine engine = new VelocityEngine();
		engine.setProperty("resource.loader", "class");
		//engine.setProperty("file.resource.loader.path", "/");
		engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		
		engine.init();
				
		final VelocityContext context = new VelocityContext();

		context.put("root", web);
		context.put("root-package", PhaseOptions.getString(options, "root-package"));
		context.put("main-class", PhaseOptions.getString(options, "main-class"));
		context.put("output-dir", PhaseOptions.getString(options, "output-dir"));	
		context.put("FileTool", FileTool.class);
		context.put("filter-config-impl", PhaseOptions.getString(options, "filter-config-class"));
		context.put("servlet-config-impl", PhaseOptions.getString(options, "servlet-config-class"));
		context.put("servlet-request-impl", PhaseOptions.getString(options, "servlet-request-class"));
		context.put("servlet-response-impl", PhaseOptions.getString(options, "servlet-response-class"));
		
	    final InputStream input = getClass().getClassLoader().getResourceAsStream("soot/jimple/toolkits/javaee/templates/root.vm");
	    if (input == null) {
	        throw new RuntimeException("Template file doesn't exist");           
	    }

	    final InputStreamReader reader = new InputStreamReader(input); 

        if (!engine.evaluate(context, new NullWriter(), "root", reader)) {
            throw new RuntimeException("Failed to convert the template into html.");
        }
	}

	/**
	 * Stores the model into a file name {@code modelName}.
	 * 
	 * @param modelName Name of the resulting file.
	 */
	private void storeModel(final String modelName) {
		try {
			final JAXBContext context = JAXBContext.newInstance( Web.class ); 
			final Marshaller marshaller = context.createMarshaller();
			Writer writer = null; 
			
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			
			try { 
			  writer = new FileWriter(modelName); 
			  marshaller.marshal( web, writer ); 
			} catch(final IOException e) {
				
			}
			finally { 
			  try {
				  writer.close();
			  } catch ( Exception e ) {
				  LOG.error("Unable to dump model to " + modelName);
			  } 
			}
		} catch(JAXBException e) {
			LOG.error("Unable to dump model to " + modelName);
			LOG.error(e.toString());
		}
	}
	
	public void addServletDetector(final ServletDetector detector) {
		servletDetectors.add(detector);
	}
}
