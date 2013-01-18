package soot.jimple.toolkits.javaee.model.servlet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

/**
 * A configured servlet.
 * 
 * @author Bernhard Berger
 */
public class Servlet implements NamedElement {
	@Override
	public int hashCode() {
		return (name == null) ? 0 : name.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Servlet other = (Servlet) obj;
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		return true;
	}

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
	@XmlID @XmlAttribute(name="name", required=true)
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
