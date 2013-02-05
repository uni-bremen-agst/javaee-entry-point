package soot.jimple.toolkits.javaee.model.servlet;

import javax.xml.bind.annotation.XmlAttribute;

public class Listener implements JavaType {
	private String clazz;
	
	@Override
	@XmlAttribute(name="class", required=true)
	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
}
