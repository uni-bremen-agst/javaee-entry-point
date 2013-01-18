package soot.jimple.toolkits.javaee.model.servlet.io;

import java.io.InputStream;
import java.util.Set;

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

import soot.jimple.toolkits.javaee.model.servlet.Address;
import soot.jimple.toolkits.javaee.model.servlet.Filter;
import soot.jimple.toolkits.javaee.model.servlet.Listener;
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
		
		// read other servlets ... faces, actions and so forth
		
		readServletMappings(doc, result);

		readFilters(doc, result);
		
		readListeners(doc, result);
		
		return result;
	}

	private static void readListeners(final Document doc, final Web web) throws XPathException {
	    final XPathFactory factory = XPathFactory.newInstance();
	    final XPath xpath = factory.newXPath();
	    final XPathExpression listenerExpr = xpath.compile("//web-app/listener");

	    final NodeList listenerNodes = (NodeList)listenerExpr.evaluate(doc, XPathConstants.NODESET);
	    
	    for (int i = 0; i < listenerNodes.getLength(); i++) {
	        final Element node = (Element)listenerNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();
	        
	        Listener listener = new Listener();
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}
	        	final Element child = (Element) children.item(j);
	        	
	        	final String attrName  = child.getNodeName();
	        	final String attrValue = child.getFirstChild().getNodeValue();
	        	
	        	if(attrName.equals("listener-class")) {
	        		listener.setClazz(attrValue);
	        	} else {
	        		System.err.println("WebXMLReader: Unknown listener attribute " + attrName);
	        	}
	        }
	        
	        web.getListeners().add(listener);
	        
	        // check validity
	        if(listener.getClazz() == null) {
	        	System.err.println("WebXMLReader: Listeners not configured correctly: " + listener);
	        }
	    }
	}

	/**
	 * Reads the filter settings and their mappings.
	 */
	private static void readFilters(final Document doc, final Web web) throws XPathException {
	    final XPathFactory factory = XPathFactory.newInstance();
	    final XPath xpath = factory.newXPath();
	    final XPathExpression filterExpr = xpath.compile("//web-app/filter");

	    final NodeList filterNodes = (NodeList)filterExpr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < filterNodes.getLength(); i++) {
	        final Element node = (Element)filterNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();
	        
	        Filter  filter = new Filter();
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}
	        	final Element child = (Element) children.item(j);
	        	
	        	final String attrName  = child.getNodeName();
	        	final String attrValue = child.getFirstChild().getNodeValue();
	        	
	        	if(attrName.equals("filter-name")) {
	        		filter.setName(attrValue);
	        	} else if(attrName.equals("filter-class")) {
	        		filter.setClazz(attrValue);
	        	} else {
	        		System.err.println("WebXMLReader: Unknown filter attribute " + attrName);
	        	}
	        }
	        
	        web.getFilters().add(filter);
	        
	        // check validity
	        if(filter.getName() == null || filter.getClazz() == null) {
	        	System.err.println("WebXMLReader: Filter not configured correctly: " + filter);
	        }
	    }
	    
	    final XPathExpression filterMappingExpr = xpath.compile("//web-app/filter-mapping");
	    
	    final NodeList mappingNodes = (NodeList)filterMappingExpr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < mappingNodes.getLength(); i++) {
	        final Element node = (Element)mappingNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();

	        String name = null;
	        String url  = null;
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}
	        	final Element child = (Element) children.item(j);
	        	
	        	final String attrName  = child.getNodeName();
	        	final String attrValue = child.getFirstChild().getNodeValue();
	        	
	        	if(attrName.equals("filter-name")) {
	        		name = attrValue;
	        	} else if(attrName.equals("url-pattern")) {
	        		url = attrValue;
	        	} else {
	        		System.err.println("WebXMLReader: Unknown filter-mapping attribute " + attrName);
	        	}
	        }
	        
	        Filter filter = web.getFilter(name);
	        Address root = web.getRoot();
	        String [] pattern = url.split("/");
	        String [] subPattern = new String[pattern.length - 1];
	        System.arraycopy(pattern, 1, subPattern, 0, subPattern.length);
	        
	        root.add(subPattern, filter);
	        System.err.println("Found filter mapping " + filter + " -> " + url);
	    }
	    
	}

	/**
	 * Reads the servlet-mappings.
	 */
	private static void readServletMappings(final Document doc, final Web web) throws XPathException {
	    final XPathFactory factory = XPathFactory.newInstance();
	    final XPath xpath = factory.newXPath();
	    final XPathExpression servletMappingExpr = xpath.compile("//web-app/servlet-mapping");
	    
	    final NodeList mappingNodes = (NodeList)servletMappingExpr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < mappingNodes.getLength(); i++) {
	        final Element node = (Element)mappingNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();

	        String name = null;
	        String url  = null;
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}
	        	final Element child = (Element) children.item(j);
	        	
	        	final String attrName  = child.getNodeName();
	        	final String attrValue = child.getFirstChild().getNodeValue();
	        	
	        	if(attrName.equals("servlet-name")) {
	        		name = attrValue;
	        	} else if(attrName.equals("url-pattern")) {
	        		url = attrValue;
	        	} else {
	        		System.err.println("WebXMLReader: Unknown servlet-mapping attribute " + attrName);
	        	}
	        }

	        // TODO I think we need to filter for handled servlets, such as struts actions etc
	        if(url.contains("*")) {
	        	continue;
	        }
	        
	        Servlet servlet = web.getServlet(name);
	        Address address = web.getRoot();
	        
	        final String [] path = url.split("/");
	     
	        for(int index = 1; index < path.length; ++index) {
	        	Address child = address.getChild(path[index]);
	        	
	        	if(child == null) {
	        		child = new Address();
	        		child.setName(path[index]);
		        	address.getChildren().add(child);
	        	}
	        	
	        	
	        	address = child;
	        }
	        
	        address.setServlet(servlet);
	    }
	}

	/**
	 * Reads all servlets
	 */
	private static void readServlets(final Document doc, final Set<Servlet> servlets) throws XPathException {
	    final XPathFactory factory = XPathFactory.newInstance();
	    final XPath xpath = factory.newXPath();
	    final XPathExpression servletExpr = xpath.compile("//web-app/servlet");

	    final NodeList servletNodes = (NodeList)servletExpr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < servletNodes.getLength(); i++) {
	        final Element node = (Element)servletNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();
	        
	        Servlet servlet = new Servlet();
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}
	        	final Element child = (Element) children.item(j);
	        	
	        	final String attrName  = child.getNodeName();
	        	final String attrValue = child.getFirstChild().getNodeValue();
	        	
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
