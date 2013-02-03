package soot.jimple.toolkits.javaee.model.servlet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class Address implements NamedElement{
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
		
		Address other = (Address) obj;
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "Address [name=" + name + "]";
	}
	private Set<Address> children = new TreeSet<Address>(new NamedElementComparator<Address>());
	private String name;
	
	private Servlet servlet = null;
	
	private List<Filter> filters = new LinkedList<Filter>();
	
	@XmlIDREF
	@XmlElementWrapper(name="filters")
	@XmlElement(name="filter")
	public List<Filter> getFilters() {
		return filters;
	}

	@XmlElement(name="child")
	@XmlElementWrapper(name="children")
	public Set<Address> getChildren() {
		return children;
	}

	@Override
	@XmlAttribute(name="name")
	public String getName() {
		return name;
	}

	@XmlIDREF
	public Servlet getServlet() {
		return servlet;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setServlet(Servlet servlet) {
		this.servlet = servlet;
	}

	public Address getChild(final String name) {
		for(final Address address : children) {
			if(address.getName().equals(name)) {
				return address;
			}
		}
		
		return null;
	}
	
	public <T> void add(final String [] pattern, final T element) {
		if(pattern.length == 1 && pattern[0].equals("*")) {
			for(final Address child : children) {
				child.add(pattern, element);
			}
			
			// TODO hacky region ahead
			if(element instanceof Filter) {
				filters.add((Filter)element);
			} else {
				System.err.println("Unhandled element type " + element.getClass());
			}
		} else if(!pattern[0].contains("*")) {
			Address child = getChild(pattern[0]);
			
			if(child == null) {
				return; // no match
			} else {
				String [] newPattern = new String[pattern.length - 1];
				System.arraycopy(pattern, 1, newPattern, 0, newPattern.length);
				child.add(newPattern, element);
			}
		} else {
			System.err.println("Not a simple wildcard : " + pattern[0]);
		}
	}
}
