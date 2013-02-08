package soot.jimple.toolkits.javaee.detectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;
import soot.jimple.toolkits.javaee.Signatures;
import soot.jimple.toolkits.javaee.model.servlet.FileLoader;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.io.WebXMLReader;

/**
 * Generic servlet detector.
 * 
 * @author Bernhard Berger
 */
public class GenericServletDetector extends AbstractServletDetector implements Signatures {
	/**
	 * Logger.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(GenericServletDetector.class);
	
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
}
