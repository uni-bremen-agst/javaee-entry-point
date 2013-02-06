package soot.jimple.toolkits.javaee.model.servlet.struts1;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import soot.jimple.toolkits.javaee.model.servlet.JavaType;
import soot.jimple.toolkits.javaee.model.servlet.NamedElement;

public class FormProperty implements NamedElement, JavaType {
	private String clazz;
	
	private String name;
	
	@Override
	@XmlAttribute(name="class", required=true)
	public String getClazz() {
		return clazz;
	}

	@Override
	@XmlID @XmlAttribute(name="name", required=true)
	public String getName() {
		return name;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
