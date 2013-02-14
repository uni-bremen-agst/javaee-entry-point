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
