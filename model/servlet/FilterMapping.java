package soot.jimple.toolkits.javaee.model.servlet;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

public class FilterMapping {
	private Filter filter;
	
	@XmlIDREF
	@XmlAttribute
	public Filter getFilter() {
		return filter;
	}
	
	public void setFilter(final Filter filter) {
		this.filter = filter;
	}
	
	private String urlPattern;
	
	@XmlAttribute
	public String getURLPattern() {
		return urlPattern;
	}
	
	public void setURLPattern(final String urlPattern) {
		this.urlPattern = urlPattern;
	}
}
