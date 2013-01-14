package soot.jimple.toolkits.javaee.model.servlet;

/**
 * A configured servlet.
 * 
 * @author Bernhard Berger
 */
public class Servlet implements NamedElement {
	@Override
	public String toString() {
		return "Servlet [clazz=" + clazz + ", name=" + name + "]";
	}

	private String clazz;
	private String name;
	
	public String getClazz() {
		return clazz;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
