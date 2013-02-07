package soot.jimple.toolkits.javaee.detectors;

import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SourceLocator;
import soot.jimple.toolkits.javaee.ClassLoader;
import soot.jimple.toolkits.javaee.Signatures;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.io.WebXMLReader;

public class GenericServletDetector extends AbstractServletDetector implements Signatures {
	@Override
	public void detectFromSource(final Web web) {
	    final Hierarchy cha = Scene.v().getActiveHierarchy();
	    final SootClass servletClass = Scene.v().getSootClass(HTTP_SERVLET_CLASS_NAME);

	    for(final SootClass clazz : Scene.v().getApplicationClasses()) {
	        if (!clazz.isConcrete()) //ignore interfaces and abstract classes
	            continue;
	        
	        if (cha.isClassSubclassOf(clazz, servletClass)){
	            registerServlet(web, clazz);
	        }
		}
	}


	@Override
	public void detectFromConfig(final Web web) {
		SourceLocator locator = SourceLocator.v();
		
		for(String part : locator.classPath()) {
			if(!part.endsWith("WEB-INF/classes")) {
				continue;
			}
			final ClassLoader loader = new ClassLoader(part.substring(0, part.length() - 15));

			try {
				WebXMLReader.readWebXML(loader, web);
			} catch (final Exception e) {
				//LOG.error("Cannot read web.xml: " + e.getMessage());
			}
		}
	}
}
