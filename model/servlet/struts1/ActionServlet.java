package soot.jimple.toolkits.javaee.model.servlet.struts1;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


import soot.jimple.toolkits.javaee.model.servlet.Servlet;

@XmlRootElement(name="ActionServlet")
public class ActionServlet extends Servlet {
	private String parameter;
	
	@XmlAttribute
	public String getParameter() {
		return parameter;
	}
	
	public void setParameter(final String parameter) {
		this.parameter = parameter;
	}

	private String scope;
	
	@XmlAttribute
	public String getScope() {
		return scope;
	}
	
	public void setScope(final String scope) {
		this.scope = scope;
	}

	private boolean validate;
	
	@XmlAttribute
	public boolean getValidate() {
		return validate;
	}
	
	public void setValidate(final boolean validate) {
		this.validate = validate;
	}
	
	private List<ActionForward> forwards = new LinkedList<ActionForward>();

	@XmlElement(name="forward")
	@XmlElementWrapper(name="forwards")
	public List<ActionForward> getForwards() {
		return forwards;
	}
}
