package soot.jimple.toolkits.javaee.detectors;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;
import soot.jimple.toolkits.javaee.Signatures;
import soot.jimple.toolkits.javaee.model.servlet.Address;
import soot.jimple.toolkits.javaee.model.servlet.FileLoader;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.http.HttpServlet;
import soot.jimple.toolkits.javaee.model.servlet.http.io.WebXMLReader;

/**
 * Generic servlet detector.
 * 
 * @author Bernhard Berger
 */
public class HttpServletDetector extends AbstractServletDetector implements Signatures {
	/**
	 * Logger.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(HttpServletDetector.class);
	
	@Override
	public void detectFromSource(final Web web) {
		LOG.info("Detecting servlets from source code.");
	    final Hierarchy cha = Scene.v().getActiveHierarchy();
	    final SootClass servletClass = Scene.v().getSootClass(HTTP_SERVLET_CLASS_NAME);

	    for(final SootClass clazz : Scene.v().getApplicationClasses()) {
	        if (!clazz.isConcrete()) //ignore interfaces and abstract classes
	            continue;
	        
	        if (cha.isClassSubclassOf(clazz, servletClass)){
	        	LOG.info("Found servlet class {}.", servletClass);
	            registerServlet(web, clazz);
	        }
		}
	}


	@Override
	public void detectFromConfig(final Web web) {
		LOG.info("Detecting servlets from web.xml.");
		SourceLocator locator = SourceLocator.v();
		
		for(String part : locator.classPath()) {
			if(!part.endsWith("WEB-INF/classes")) {
				continue;
			}
			
			final FileLoader loader = new FileLoader(part.substring(0, part.length() - 15));

			try {
				WebXMLReader.readWebXML(loader, web);
			} catch (final Exception e) {
				LOG.error("Cannot read web.xml.", e);
			}
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Class<?>> getModelExtensions() {
		return (List<Class<?>>)(List<?>)Collections.singletonList(HttpServlet.class);
	}
	
	/**
	 * Registers a servlet as if it was declared in web.xml
	 * 
	 * @param clazz
	 *            the class
	 */
	public static void registerServlet(final Web web, final SootClass clazz) {
		final HttpServlet servlet = new HttpServlet(clazz.getName(), clazz.getName());
		web.getServlets().add(servlet);

		final Address address = new Address();
		address.setName(clazz.getName());
		address.setServlet(servlet);
		web.getRoot().getChildren().add(address);
	}
}
