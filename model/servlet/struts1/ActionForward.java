package soot.jimple.toolkits.javaee.model.servlet.struts1;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

import soot.jimple.toolkits.javaee.model.servlet.Address;
import soot.jimple.toolkits.javaee.model.servlet.NamedElement;

public class ActionForward implements NamedElement {
	private String name;
	private String parameter;
	private Address destination;

	@Override
	@XmlAttribute(name="name", required=true)
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setDestination(final Address destination) {
		this.destination = destination;
	}
	
	@XmlIDREF
	@XmlAttribute
	public Address getDestination() {
		return destination;
	}
	
	@XmlAttribute
	public String getParameter() {
		return parameter;
	}
	
	public void setParameter(final String parameter) {
		this.parameter = parameter;
	}
}
