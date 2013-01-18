package soot.jimple.toolkits.javaee.model.servlet;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root element of the model.
 * 
 * @author Bernhard Berger
 */
@XmlRootElement( namespace = "http://informatik.uni-bremen.de/st/javaee/servlet" ) 
public class Web {
	private Address root = new Address();
	
	private Set<Filter> filters = new TreeSet<Filter>(new NamedElementComparator<Filter>());

	private Set<Servlet> servlets = new TreeSet<Servlet>(new NamedElementComparator<Servlet>());

	private Set<Listener> listeners = new HashSet<Listener>();
	
	@XmlElement(name="root")
	public Address getRoot() {
		return root;
	}

	@XmlElement(name="servlet")
	@XmlElementWrapper(name="servlets")
	public Set<Servlet> getServlets() {
		return servlets;
	}

	public Servlet getServlet(final String name) {
		for(final Servlet servlet : servlets) {
			if(servlet.getName().equals(name)) {
				return servlet;
			}
		}
		
		return null;
	}

	@XmlElement(name="filter")
	@XmlElementWrapper(name="filters")
	public Set<Filter> getFilters() {
		return filters;
	}
	
	@XmlElement(name="listener")
	@XmlElementWrapper(name="listeners")
	public Set<Listener> getListeners() {
		return listeners;
	}

	public Filter getFilter(final String name) {
		for(final Filter filter : filters) {
			if(filter.getName().equals(name)) {
				return filter;
			}
		}
		
		return null;
	}
}
