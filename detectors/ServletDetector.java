package soot.jimple.toolkits.javaee.detectors;

import java.util.List;
import java.util.Map;

import soot.jimple.toolkits.javaee.model.servlet.Web;

/**
 * A servlet detector detects and processes framework specific servlets.
 * 
 * @author Bernhard Berger
 */
public interface ServletDetector {
	/**
	 * If you subclass a class belonging to the internal model we have to
	 *   register the classes for serialization. This method is called by the
	 *   framework to do the necessary work.
	 * 
	 * @return A list of classes that extend the internal data model.
	 */
	public List<Class<?>> getModelExtensions();
	
	public void setOptions(@SuppressWarnings("rawtypes") final Map options);
	
	public void detectFromSource(final Web web);

	public void detectFromConfig(Web web);

}
