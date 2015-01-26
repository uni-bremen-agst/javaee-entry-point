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
*/
package soot.jimple.toolkits.javaee.model.servlet;

import java.util.Map;

import soot.PhaseOptions;

/**
 * A class that contains some additional information for the generator.
 * 
 * @author Bernhard Berger
 */
public class GeneratorInfos {
	private String rootPackage;
	private String mainclass;
	private String filterConfigImpl;
	private String servletConfigImpl;
	private String servletRequestImpl;
	private String servletResponseImpl;
	
	public void initializeFromOptions(@SuppressWarnings("rawtypes") final Map options) {		
		rootPackage =  PhaseOptions.getString(options, "root-package");
		mainclass = PhaseOptions.getString(options, "main-class");
		filterConfigImpl = PhaseOptions.getString(options, "filter-config-class");
		servletConfigImpl = PhaseOptions.getString(options, "servlet-config-class");
		servletRequestImpl = PhaseOptions.getString(options, "servlet-request-class");
		servletResponseImpl = PhaseOptions.getString(options, "servlet-response-class");
	}
	
	public String getRootPackage() {
		return rootPackage;
	}
	public void setRootPackage(String rootPackage) {
		this.rootPackage = rootPackage;
	}
	public String getMainclass() {
		return mainclass;
	}
	public void setMainclass(String mainclass) {
		this.mainclass = mainclass;
	}
	public String getFilterConfigImpl() {
		return filterConfigImpl;
	}
	public void setFilterConfigImpl(String filterConfigImpl) {
		this.filterConfigImpl = filterConfigImpl;
	}
	public String getServletConfigImpl() {
		return servletConfigImpl;
	}
	public void setServletConfigImpl(String servletConfigImpl) {
		this.servletConfigImpl = servletConfigImpl;
	}
	public String getServletRequestImpl() {
		return servletRequestImpl;
	}
	public void setServletRequestImpl(String servletRequestImpl) {
		this.servletRequestImpl = servletRequestImpl;
	}
	public String getServletResponseImpl() {
		return servletResponseImpl;
	}
	public void setServletResponseImpl(String servletResponseImpl) {
		this.servletResponseImpl = servletResponseImpl;
	}

}
