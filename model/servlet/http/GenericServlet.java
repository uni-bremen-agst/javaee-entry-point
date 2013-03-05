package soot.jimple.toolkits.javaee.model.servlet.http;


import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="GenericServlet")
public class GenericServlet extends AbstractServlet {
	public GenericServlet() {
		super();
	}
	
	public GenericServlet(String clazz, String name){
		super(clazz, name);
	}
}
