package soot.jimple.toolkits.javaee;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.common.tools.FileTool;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import soot.G;
import soot.PhaseOptions;
import soot.Scene;
import soot.SceneTransformer;
import soot.Singletons;
import soot.SootClass;
import soot.SourceLocator;
import soot.jimple.toolkits.javaee.model.servlet.Address;
import soot.jimple.toolkits.javaee.model.servlet.Filter;
import soot.jimple.toolkits.javaee.model.servlet.Listener;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.io.WebXMLReader;

/**
 * This class drives the generation of a main method that creates and calls
 *   all configured servlets. This is necessary to calculate a correct call
 *   graph with soot.
 *
 * @author Bernhard Berger
 */
public class ServletEntryPointGenerator extends SceneTransformer implements Signatures {
 
	/**
	 * Logging facility.
	 */
	private final static SootLogger LOG = new SootLogger();

	/**
	 * @return Whether {@code clazz} is an application class.
	 */
	private static boolean isApplicationClass(final SootClass clazz) {
		return clazz.isApplicationClass();
	}

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
		LOG.setPhase("wjpp.seg");
	}


	private void loadClassesFromModel() {
		for(final Filter filter : this.web.getFilters()) {
			LOG.debug("Loading " + filter.getClazz());
			scene.forceResolve(filter.getClazz(), SootClass.SIGNATURES);
		}

		for(final Listener listener : this.web.getListeners()) {
			LOG.debug("Loading " + listener.getClazz());
			scene.forceResolve(listener.getClazz(), SootClass.SIGNATURES);
		}

		for(final Servlet servlet : this.web.getServlets()) {
			LOG.debug("Loading " + servlet.getClazz());
			scene.forceResolve(servlet.getClazz(), SootClass.SIGNATURES);
		}
	}

	/**
	 * Checks if {@code clazz} inherits from {@code baseClassName}.
	 */
	private boolean classExtends(SootClass clazz, final String baseClassName) {
		while(clazz != null) {
			if(clazz.isPhantom()) {
				LOG.warn("Found phantom class. Maybe it is impossible to detect all servlets.");
			}
			
			if(clazz.getName().equals(baseClassName)) {
				return true;
			}
			
			clazz = clazz.hasSuperclass() ? clazz.getSuperclass() : null;
		}
		
		return false;
	}
	
	private Web web = new Web();

	/**
	 * Loads the {@code web.xml} or fakes it if the corresponding command line
	 *   parameter was given.
	 *   
	 * @todo Implement proper war support for locator.
	 */
	private void loadWebXML() {
		if(considerAllServlets) {
			configureAllServlets();
		} else {
			SourceLocator locator = SourceLocator.v();
			
			for(String part : locator.classPath()) {
				if(!part.endsWith("WEB-INF/classes")) {
					continue;
				}
				
				part = part.substring(0, part.length() - 7) + "web.xml";
				try {
					web = WebXMLReader.readWebXML(new FileInputStream(part));
				} catch (Exception e) {
					LOG.error("Cannot read web.xml.");
				}
			}
		}
	}

	/**
	 * Assumes that all servlets in the application scope are configured and
	 *   creates a fake configuration.
	 */
	private void configureAllServlets() {
		for(final SootClass clazz : Scene.v().getClasses()) {
			if(isApplicationClass(clazz) && isServlet(clazz)) {
				final Servlet servlet = new Servlet();
				servlet.setName(clazz.getName());
				servlet.setClazz(clazz.getName());
				web.getServlets().add(servlet);
				
				final Address address = new Address();
				address.setName(clazz.getName());
				address.setServlet(servlet);
				web.getRoot().getChildren().add(address);
			}
		}
	}
	
	/**
	 * @todo Should we use GenericServlet?
	 * 
	 * @return {@code true} if {@code clazz} is a servlet.
	 */
	private boolean isServlet(final SootClass clazz) {
		return classExtends(clazz, HTTP_SERVLET_CLASS_NAME);
	}

	@Override
	protected void internalTransform(final String phaseName, @SuppressWarnings("rawtypes") final Map options) {
		// configure logging
		LOG.setPhase(phaseName);
		LOG.setOptions(options);
		
		LOG.info("Running " + phaseName);
		
		considerAllServlets = PhaseOptions.getBoolean(options, "consider-all-servlets");
		
		loadWebXML();
		
		final String modelDestination = PhaseOptions.getString(options, "dump-model");
		if(!modelDestination.isEmpty()) {
			storeModel(modelDestination);
		}

		try {
			LOG.info("Processing templates");
			final VelocityContext context = setupTemplateEngine(options);
			final Template template = Velocity.getTemplate("/soot/jimple/toolkits/javaee/templates/root.vm");

			template.merge(context, new NullWriter());
		} catch(final ResourceNotFoundException e) {
			LOG.error("Could not find template file.");
		} catch(final ParseErrorException e) {
			LOG.error("Failed to parse the template.");
		} catch(final MethodInvocationException e) {
			LOG.error("Error while calling Java code from template.");
			e.printStackTrace();
		}
		
		final SootClass sootClass = scene.forceResolve(PhaseOptions.getString(options, "root-package") + "." + PhaseOptions.getString(options, "main-class"), SootClass.BODIES);
		scene.setMainClass(sootClass);
	}

	/**
	 * Sets up and configures the template engine.
	 * 
	 * @return Velocity context.
	 */
	private VelocityContext setupTemplateEngine(@SuppressWarnings("rawtypes") final Map options) {
		final Properties properties = new Properties();
		properties.put("resource.loader", "class,file");
		properties.put("file.resource.loader.path", "/");
		Velocity.init(properties);
		
		final VelocityContext context = new VelocityContext();

		context.put("root", web);
		context.put("root-package", PhaseOptions.getString(options, "root-package"));
		context.put("main-class", PhaseOptions.getString(options, "main-class"));
		context.put("output-dir", PhaseOptions.getString(options, "output-dir"));	
		context.put("FileTool", FileTool.class);
		context.put("filter-config-impl", PhaseOptions.getString(options, "filter-config-impl"));
		context.put("servlet-config-impl", PhaseOptions.getString(options, "servlet-config-impl"));
		context.put("servlet-request-impl", PhaseOptions.getString(options, "servlet-request-impl"));
		context.put("servlet-response-impl", PhaseOptions.getString(options, "servlet-response-impl"));
		
		return context;
	}

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

	public void loadEntryPoints() {
		// the following code allows us to run in normal mode (consider-all-servlets = false)
		// without using the -process-xxx option. We will parse the web.xml in this early
		// step to load all servlet, filter, listener and so forth classes before the scene
		// is sealed. If consider-all-servlets is specified we will discard the container
		// model during the setup and since the -process-xxx option is necessary in this
		// case these classes will be loaded nevertheless.
		loadWebXML();
		loadClassesFromModel();
	}
}
