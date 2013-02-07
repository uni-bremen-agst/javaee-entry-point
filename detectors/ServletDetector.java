package soot.jimple.toolkits.javaee.detectors;

import java.util.Map;

import soot.jimple.toolkits.javaee.model.servlet.Web;

/**
 * A servlet detector detects and processes framework specific servlets.
 * 
 * @author Bernhard Berger
 */
public interface ServletDetector {
	public void setOptions(@SuppressWarnings("rawtypes") final Map options);
	
	public void detectFromSource(final Web web);

	public void detectFromConfig(Web web);

}
