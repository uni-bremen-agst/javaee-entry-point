package soot.jimple.toolkits.javaee.model.servlet.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import soot.jimple.toolkits.javaee.model.servlet.Parameter;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;

@XmlRootElement(name="HttpServlet")
public class HttpServlet extends Servlet {
	public HttpServlet() {
		super();
	}
	
	public HttpServlet(String clazz, String name){
		super(clazz, name);
	}

	private List<Parameter> parameters = new ArrayList<Parameter>();

	@XmlElement(name="parameter")
	@XmlElementWrapper(name="init-parameters")
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	private FileLoader loader;
	
	@XmlElement
	public FileLoader getLoader() {
		return loader;
	}
	
	public void setLoader(final FileLoader loader) {
		this.loader = loader;
	}
}
