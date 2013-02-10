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
	
	/**
	 * Sets the phase options.
	 * 
	 * @param options Actual phase options.
	 */
	public void setOptions(@SuppressWarnings("rawtypes") final Map options);
	
	/**
	 * Detects the servlets from source.
	 * 
	 * @param web The web instance where the servlets have to be registered. 
	 */
	public void detectFromSource(final Web web);

	/**
	 * Detects the servlets from config files.
	 * 
	 * @param web The web instance where the servlets have to be registered. 
	 */
	public void detectFromConfig(final Web web);
}
