package soot.jimple.toolkits.javaee.model.servlet;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;

public class SecurityConstraint implements NamedElement {
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
		
		SecurityConstraint other = (SecurityConstraint) obj;
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "Filter [name=" + name + "]";
	}

	private String name;


	@Override
	@XmlID @XmlAttribute(name="name", required=true)
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	private Set<String> requiresRoles = new HashSet<String>();

	@XmlElementWrapper(name="requiredRoles")
	@XmlElement(name="role")
	public Set<String> getRequiredRoles() {
		return requiresRoles;
	}
	
	private Set<WebResourceCollection> webResourceCollections = new HashSet<WebResourceCollection>();
	
	@XmlElementWrapper(name="webResourceCollections")
	@XmlElement(name="webResourceCollection")
	public Set<WebResourceCollection> getWebResourceCollections() {
		return webResourceCollections;
	}
}
