package soot.jimple.toolkits.javaee.model.servlet;

/**
 * A simple named element.
 *
 * @author Bernhard Berger
 */
public interface NamedElement {
	/**
	 * @Returns The name of the element.
	 */
	public String getName();

	/**
	 * Sets the name of the element.
	 * 
	 * @param name The new name of the element.
	 */
	public void setName(final String name);
}
