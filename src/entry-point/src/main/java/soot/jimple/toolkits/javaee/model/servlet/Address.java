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
   along with Soot entry point creator.  If not, see <http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
*/
package soot.jimple.toolkits.javaee.model.servlet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class Address implements NamedElement {
	private String fullPath;
	
	@XmlAttribute(required=true)
	@XmlID
	public String getFullPath() {
		return fullPath;
	}
	
	public void setFullPath(final String fullPath) {
		this.fullPath = fullPath;
	}
	
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
	
	private List<SecurityConstraint> securityConstraints = new ArrayList<SecurityConstraint>();
	
	@XmlElementWrapper(name="constraints")
	@XmlIDREF
	public List<SecurityConstraint> getSecurityConstraints() {
		return securityConstraints;
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

	public void collectBoundAddresses(final List<Address> result) {
		if(getServlet() != null) {
			result.add(this);
		}

		for(final Address child : children) {
			child.collectBoundAddresses(result);
		}
	}
}
