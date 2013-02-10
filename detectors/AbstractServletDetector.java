package soot.jimple.toolkits.javaee.detectors;

import java.util.Map;

/**
 * An abstract servlet detector with some additional functions.
 * 
 * @author Bernhard Berger
 */
public abstract class AbstractServletDetector implements ServletDetector {
	/**
	 * Phase options for servlet detection.
	 */
	@SuppressWarnings("rawtypes")
	protected Map options;
	
	@Override
	public void setOptions(@SuppressWarnings("rawtypes") final Map options) {
		this.options = options;
	}
}
