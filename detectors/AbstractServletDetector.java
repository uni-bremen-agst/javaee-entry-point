package soot.jimple.toolkits.javaee.detectors;

import java.util.Map;

import soot.SootClass;

/**
 * An abstract servlet detector with some additional functions.
 * 
 * @author Bernhard Berger
 */
public abstract class AbstractServletDetector implements ServletDetector {
	@SuppressWarnings("rawtypes")
	protected Map options;
	
	@Override
	public void setOptions(@SuppressWarnings("rawtypes") final Map options) {
		this.options = options;
	}

	/**
	 * Checks if {@code clazz} inherits from {@code baseClassName}.
	 */
	protected boolean classExtends(SootClass clazz, final String baseClassName) {
		while (clazz != null) {
			if (clazz.isPhantom()) {
				// TODO
				// LOG.warn("Found phantom class. Maybe it is impossible to detect all servlets.");
			}

			if (clazz.getName().equals(baseClassName)) {
				return true;
			}

			clazz = clazz.hasSuperclass() ? clazz.getSuperclass() : null;
		}

		return false;
	}
}
