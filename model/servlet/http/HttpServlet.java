package soot.jimple.toolkits.javaee.model.servlet.http;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="HttpServlet")
public class HttpServlet extends AbstractServlet {
	public HttpServlet() {
		super();
	}
	
	public HttpServlet(String clazz, String name){
		super(clazz, name);
	}
}
