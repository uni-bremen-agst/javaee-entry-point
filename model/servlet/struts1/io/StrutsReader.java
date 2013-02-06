package soot.jimple.toolkits.javaee.model.servlet.struts1.io;

import soot.jimple.toolkits.javaee.model.servlet.Parameter;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;

public class StrutsReader {
	public static void readStrutsConfig(final Web web) {
		for(final Servlet servlet : web.getServlets()) {
			if(!servlet.getClazz().equals("org.apache.struts.action.ActionServlet")) {
				continue;
			}
			
			for(final Parameter parameter : servlet.getParameters()) {
				if(parameter.getName().equals("config")) {
					for(final String xmlFile : parameter.getValue().split(",")) {
						parseStrutsConfig(web, xmlFile);
					}
				}
			}
		}
		
	}

	private static void parseStrutsConfig(Web web, String xmlFile) {
		// TODO Auto-generated method stub
		
	}
}
