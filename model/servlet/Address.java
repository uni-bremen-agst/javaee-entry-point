package soot.jimple.toolkits.javaee.model.servlet;

import java.util.LinkedList;
import java.util.List;


public class Address implements NamedElement{
	private List<Address> children = new LinkedList<Address>();
	private String name;
	private Servlet servlet = null;

	public List<Address> getChildren() {
		return children;
	}

	@Override
	public String getName() {
		return name;
	}

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
}
