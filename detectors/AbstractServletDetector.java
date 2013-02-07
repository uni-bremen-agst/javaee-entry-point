package soot.jimple.toolkits.javaee.detectors;

import java.util.Map;

import soot.SootClass;
import soot.jimple.toolkits.javaee.model.servlet.Address;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;

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

	/**
	 * Registers a servlet as if it was declared in web.xml
	 * 
	 * @param clazz
	 *            the class
	 */
	protected void registerServlet(final Web web, final SootClass clazz) {
		final Servlet servlet = new Servlet(clazz.getName(), clazz.getName());
		web.getServlets().add(servlet);

		final Address address = new Address();
		address.setName(clazz.getName());
		address.setServlet(servlet);
		web.getRoot().getChildren().add(address);
	}
}
