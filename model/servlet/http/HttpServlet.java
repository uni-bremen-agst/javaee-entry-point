package soot.jimple.toolkits.javaee.model.servlet.http;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="HttpServlet")
public class HttpServlet extends AbstractServlet {
	public HttpServlet() {
		super();
	}
	
	public HttpServlet(String clazz, String name){
		super(clazz, name);
	}
	
	private Set<String> methods = new HashSet<String>();
	
	@XmlElementWrapper(name="methods")
	@XmlElement(name="method")
	public Set<String> getMethods() {
		return methods;
	}
}
