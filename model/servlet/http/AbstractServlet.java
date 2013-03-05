package soot.jimple.toolkits.javaee.model.servlet.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import soot.jimple.toolkits.javaee.model.servlet.Parameter;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;

public abstract class AbstractServlet extends Servlet {

	private FileLoader loader;
	private List<Parameter> parameters = new ArrayList<Parameter>();

	public AbstractServlet() {
		super();
	}

	public AbstractServlet(String clazz, String name) {
		super(clazz, name);
	}

	@XmlElement
	public FileLoader getLoader() {
		return loader;
	}

	@XmlElement(name = "parameter")
	@XmlElementWrapper(name = "init-parameters")
	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setLoader(final FileLoader loader) {
		this.loader = loader;
	}
}