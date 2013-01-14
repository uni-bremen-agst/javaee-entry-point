package soot.jimple.toolkits.javaee.model.servlet;

import java.util.LinkedList;
import java.util.List;

/**
 * Root element of the model.
 * 
 * @author Bernhard Berger
 */
public class Web {
	private Address root = new Address();
	private List<Servlet> servlets = new LinkedList<Servlet>();

	public Address getRoot() {
		return root;
	}

	public List<Servlet> getServlets() {
		return servlets;
	}
}
