package soot.jimple.toolkits.javaee.model.servlet;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;

public class WebResourceCollection implements NamedElement {
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
		
		WebResourceCollection other = (WebResourceCollection) obj;
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "WebResourceCollection [name=" + name + "]";
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

	private Set<String> urlPatterns = new HashSet<String>();
	
	@XmlElementWrapper(name="urlPatterns")
	@XmlElement(name="urlPattern")
	public Set<String> getUrlPatterns() {
		return urlPatterns;
	}


	private Set<String> httpMethods = new HashSet<String>();
	
	@XmlElementWrapper(name="httpMethods")
	@XmlElement(name="httpMethod")
	public Set<String> getHttpMethods() {
		return httpMethods;
	}
}
