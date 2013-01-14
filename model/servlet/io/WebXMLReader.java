package soot.jimple.toolkits.javaee.model.servlet.io;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;

/**
 * A reader for {@code web.xml} files.
 * 
 * @author Bernhard Berger
 */
public class WebXMLReader {
	public static Web readWebXML(final InputStream iStream) throws Exception {
		final Web result = new Web();
		
	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    Document doc = builder.parse(iStream);
		
		readServlets(doc, result.getServlets());
		
		return result;
	}

	private static void readServlets(final Document doc, final List<Servlet> servlets) throws XPathException {
	    final XPathFactory factory = XPathFactory.newInstance();
	    final XPath xpath = factory.newXPath();
	    final XPathExpression servletExpr = xpath.compile("//web-app/servlet");

	    final NodeList servletNodes = (NodeList)servletExpr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < servletNodes.getLength(); i++) {
	        Element node = (Element)servletNodes.item(i);
	        
	        NodeList children = node.getChildNodes();
	        
	        Servlet servlet = new Servlet();
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}
	        	Element child = (Element) children.item(j);
	        	
	        	String attrName  = child.getNodeName();
	        	String attrValue = child.getFirstChild().getNodeValue();
	        	
	        	if(attrName.equals("servlet-name")) {
	        		servlet.setName(attrValue);
	        	} else if(attrName.equals("servlet-class")) {
	        		servlet.setClazz(attrValue);
	        	} else {
	        		System.err.println("WebXMLReader: Unknown servlet attribute " + attrName);
	        	}
	        }
	        
	        servlets.add(servlet);
	        
	        // check validity
	        if(servlet.getName() == null || servlet.getClazz() == null) {
	        	System.err.println("WebXMLReader: Servlet not configured correctly: " + servlet);
	        }
	    }
	}
}
