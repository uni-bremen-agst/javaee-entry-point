package soot.jimple.toolkits.javaee.detectors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.http.AbstractServlet;
import soot.jimple.toolkits.javaee.model.servlet.http.FileLoader;
import soot.jimple.toolkits.javaee.model.servlet.http.GenericServlet;
import soot.jimple.toolkits.javaee.model.servlet.http.HttpServlet;
import soot.jimple.toolkits.javaee.model.servlet.http.ServletSignatures;
import soot.jimple.toolkits.javaee.model.servlet.http.io.WebXMLReader;

/**
 * Servlet detector for {@code HttpServlets}. If you use source code detection
 *   it will check for each class if it extends {@code HttpServlet}. In the
 *   case that you want to detect the servlets from config files the {@code web.xml}
 *   is parsed.
 * 
 * @author Bernhard Berger
 */
public class HttpServletDetector extends AbstractServletDetector implements ServletSignatures {
	/**
	 * Logger.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(HttpServletDetector.class);
	
	@Override
	public void detectFromSource(final Web web) {
		LOG.info("Detecting servlets from source code.");
	    final Hierarchy cha = Scene.v().getActiveHierarchy();
	    final SootClass servletClass = Scene.v().getSootClass(HTTP_SERVLET_CLASS_NAME);
	    final SootClass genericClass = Scene.v().getSootClass(GENERIC_SERVLET_CLASS_NAME);
	    
	    for(final SootClass clazz : Scene.v().getApplicationClasses()) {
	        if (!clazz.isConcrete()) //ignore interfaces and abstract classes
	            continue;
	        
	        if (cha.isClassSubclassOf(clazz, servletClass)){
	        	LOG.info("Found http servlet class {}.", servletClass);
	            registerHttpServlet(web, clazz);
	        } else if(cha.isClassSubclassOf(clazz, genericClass)) {
	        	LOG.info("Found generic servlet class {}.", servletClass);
	            registerGenericServlet(web, clazz);
	        }
		}
	}

	@Override
	public void detectFromConfig(final Web web) {
		LOG.info("Detecting servlets from web.xml.");
		final SourceLocator locator = SourceLocator.v();
		
		for(final String part : locator.classPath()) {
			if(!part.endsWith("WEB-INF/classes")) {
				continue;
			}
			
			final FileLoader loader = new FileLoader(part.substring(0, part.length() - 15));

			try {
				new WebXMLReader ().readWebXML(loader, web);
			} catch (final Exception e) {
				LOG.error("Cannot read web.xml:", e);
			}
		}
	}

	@Override
	public List<Class<?>> getModelExtensions() {
		final Class<?> [] extensions = {HttpServlet.class, GenericServlet.class};
		
		return Arrays.asList(extensions);
	}
	
	/**
	 * Registers a http servlet as if it was declared in web.xml
	 * 
	 * @param clazz the class
	 */
	public static void registerHttpServlet(final Web web, final SootClass clazz) {
		final HttpServlet servlet = new HttpServlet(clazz.getName(), clazz.getName());
		web.getServlets().add(servlet);
		
		web.bindServlet(servlet, "/" + clazz.getName());
	}

	/**
	 * Registers a generic servlet as if it was declared in web.xml
	 * 
	 * @param clazz
	 *            the class
	 */
	public static void registerGenericServlet(final Web web, final SootClass clazz) {
		final AbstractServlet servlet = new GenericServlet(clazz.getName(), clazz.getName());
		web.getServlets().add(servlet);
		
		web.bindServlet(servlet, "/" + clazz.getName());
	}
	@Override
	public String getTemplateFile() {
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public boolean isXpandTemplate() {
		return true;
	}

	@Override
	public List<String> getCheckFiles() {
		return Collections.<String>emptyList();
	}

	private String [] templates = {"soot::jimple::toolkits::javaee::templates::http::MainClass::main",
			"soot::jimple::toolkits::javaee::templates::http::GenericServletWrapper::main",
			"soot::jimple::toolkits::javaee::templates::http::HttpServletWrapper::main",
			"soot::jimple::toolkits::javaee::templates::http::FilterWrapper::main",
			"soot::jimple::toolkits::javaee::templates::http::FilterChain::main"
	};
	@Override
	public List<String> getTemplateFiles() {
		return Arrays.asList(templates);
	}
}
