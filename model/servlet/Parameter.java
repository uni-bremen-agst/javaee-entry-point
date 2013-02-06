package soot.jimple.toolkits.javaee.model.servlet;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * An arbitrary parameter.
 *   
 * @author Bernhard Berger
 *
 */
public class Parameter implements NamedElement {
	private String name;

	@Override
	@XmlAttribute(name="name", required=true)
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	private String value;

	@XmlAttribute(name="value", required=true)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
