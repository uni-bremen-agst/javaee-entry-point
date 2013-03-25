package soot.jimple.toolkits.javaee.model.servlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.toolkits.javaee.model.servlet.struts1.ActionServlet;
import soot.jimple.toolkits.javaee.model.ws.WebService;

/**
 * Root element of the model.
 * 
 * @author Bernhard Berger
 */
@XmlRootElement( namespace = "http://informatik.uni-bremen.de/st/javaee/servlet" ) 
public class Web {
	private final static Logger LOG = LoggerFactory.getLogger(Web.class);
	
	private Address root = new Address();
	
	private Set<Filter> filters = new TreeSet<Filter>(new NamedElementComparator<Filter>());

	private Set<Servlet> servlets = new TreeSet<Servlet>(new NamedElementComparator<Servlet>());

	private Set<Listener> listeners = new HashSet<Listener>();
	
	private List<WebService> services = new ArrayList<WebService>();
	
	public Web() {
		root.setFullPath("/");
	}
	
	@XmlElement(name="root")
	public Address getRoot() {
		return root;
	}

	@XmlElementRefs({@XmlElementRef(name="servlet", type=Servlet.class),
		             @XmlElementRef(name="actionServlet", type=ActionServlet.class)})
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

	public List<Address> collectBoundAddresses() {
		List<Address> result = new LinkedList<Address>();
		root.collectBoundAddresses(result);
		return result;
	}

	public void bindServlet(Servlet servlet, String url) {
		LOG.info("Bindung {} to {}.", url, servlet);
        // TODO I think we need to filter for handled servlets, such as struts actions etc
        Address address = resolveAddress(url);
        
        address.setServlet(servlet);
	}

	public Address resolveAddress(final String url) {
        if(url.contains("*")) {
        	throw new IllegalArgumentException("Cannot handle wildcard urls.");
        }

        Address address = getRoot();

        final String [] path = url.split("/");

        int index = url.startsWith("/") ? 1 : 0;

        for(; index < path.length; ++index) {
        	Address child = address.getChild(path[index]);

        	if(child == null) {
        		child = new Address();
        		child.setName(path[index]);
        		child.setFullPath(concat(path, index));
	        	address.getChildren().add(child);
	        	
	        	applyFilters(child);
        	}

        	address = child;
        }

        return address;
 	}

	private void applyFilters(final Address address) {
		for(final FilterMapping mapping : filterMappings) {
			if(address.getFullPath().matches(mapping.getURLPattern())) {
				address.getFilters().add(mapping.getFilter());
			}
		}
	}

	private static String concat(String[] path, int lastIndex) {
		final StringBuilder builder = new StringBuilder();
		boolean notFirst = false;
		
		for(int index = 0; index <= lastIndex; ++index) {
			if(notFirst) {
				builder.append("/");
			} else {
				notFirst = true;
			}
			
			builder.append(path[index]);
		}
		
		return builder.toString();
	}
	
	private List<FilterMapping> filterMappings = new LinkedList<FilterMapping>();

	@XmlElement(name="filterMapping")
	@XmlElementWrapper(name="filterMappings")
	public List<FilterMapping> getFilterMappings() {
		return filterMappings;
	}
	
	private GeneratorInfos generatorInfos = new GeneratorInfos();

	@XmlElement
	public GeneratorInfos getGeneratorInfos() {
		return generatorInfos;
	}

	public void setGeneratorInfos(GeneratorInfos generatorInfos) {
		this.generatorInfos = generatorInfos;
	}

	public void addWebServices(List<WebService> services) {
		this.services.addAll(services);	
	}
	
	public List<WebService> getServices(){
		return services;
	}
}
