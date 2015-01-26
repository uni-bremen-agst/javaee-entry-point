/*
   This file is part of Soot entry point creator.

   Soot entry point creator is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Soot entry point creator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Soot entry point creator.  If not, see
<http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
   Copyright 2015 Ecole Polytechnique de Montreal & Tata Consultancy Services
*/
package soot.jimple.toolkits.javaee.model.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.toolkits.javaee.model.servlet.struts1.ActionServlet;

import javax.xml.bind.annotation.*;

import java.util.*;

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
	
	private Set<SecurityConstraint> constraints = new TreeSet<SecurityConstraint>(new NamedElementComparator<SecurityConstraint>());

	private Set<Listener> listeners = new HashSet<Listener>();

    private Set<String> applicationMainSignatures = new HashSet<String>();
	
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

	@XmlElement(name="security-constraint")
	@XmlElementWrapper(name="security-constraints")
	public Set<SecurityConstraint> getSecurityConstraints() {
		return constraints;
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

	public void bindServlet(final Servlet servlet, String url) {
		LOG.info("Binding {} to {}.", url, servlet);
		
		url = replaceWildcards(url);
		
        // TODO I think we need to filter for handled servlets, such as struts actions etc
        Address address = resolveAddress(url);
        
        address.setServlet(servlet);
	}

	private static String replaceWildcards(final String url) {
		if(!containsWildcard(url)) {
			return url;
		}
		LOG.info("  replacing wildcards in url {}.", url);
		
		return url.replaceAll("\\*", "_wildcard_");
	}

	private static boolean containsWildcard(final String url) {
		return url.contains("*");
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
	        	applySecurityConstraints(child);
        	}

        	address = child;
        }

        return address;
 	}

	/**
	 * Applies all registered filters to {@code address}.
	 */
	private void applyFilters(final Address address) {
		for(final FilterMapping mapping : filterMappings) {
			if(address.getFullPath().matches(mapping.getURLPattern())) {
				address.getFilters().add(mapping.getFilter());
			}
		}
	}

	/**
	 * Applies all registered filters to {@code address}.
	 */
	private void applySecurityConstraints(final Address address) {
		for(final SecurityConstraint constraint : constraints) {
			for(final WebResourceCollection collection : constraint.getWebResourceCollections()) {
				for(final String urlPattern : collection.getUrlPatterns()) {
					if(address.getFullPath().matches(urlPattern)) {
						address.getSecurityConstraints().add(constraint);
					}
				}
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

    @XmlElement
    public Collection<String> getApplicationMainSignatures() {
        return applicationMainSignatures;
    }

    public void addApplicationMainSignature(String applicationMainSignature) {
        this.applicationMainSignatures.add(applicationMainSignature);
    }
}
