package soot.jimple.toolkits.javaee.model.servlet.http.io;

import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import soot.jimple.toolkits.javaee.model.servlet.Filter;
import soot.jimple.toolkits.javaee.model.servlet.FilterMapping;
import soot.jimple.toolkits.javaee.model.servlet.Listener;
import soot.jimple.toolkits.javaee.model.servlet.Parameter;
import soot.jimple.toolkits.javaee.model.servlet.Servlet;
import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.http.FileLoader;
import soot.jimple.toolkits.javaee.model.servlet.http.HttpServlet;

/**
 * A reader for {@code web.xml} files.
 * 
 * @author Bernhard Berger
 */
public class WebXMLReader {
	private static final Logger LOG = LoggerFactory.getLogger(WebXMLReader.class);
	
	public static Web readWebXML(final FileLoader loader, final Web web) throws Exception {
	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    Document doc = builder.parse(loader.getInputStream("WEB-INF/web.xml"));
		
		readFilters(doc, web);
	    
		readServlets(doc, web.getServlets(), loader);
		
		readServletMappings(doc, web);
		
		readListeners(doc, web);
		
		return web;
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
	        		LOG.warn("Unknown listener attribute {}.", attrName);
	        	}
	        }
	        
	        web.getListeners().add(listener);
	        
	        // check validity
	        if(listener.getClazz() == null) {
	        	LOG.warn("Listeners not configured correctly {}. ", listener);
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
	        		LOG.warn("Unknown filter attribute {}.", attrName);
	        	}
	        }
	        
	        web.getFilters().add(filter);
	        
	        // check validity
	        if(filter.getName() == null || filter.getClazz() == null) {
	        	LOG.error("Filter not configured correctly {}.", filter);
	        }
	    }
	    
	    final XPathExpression filterMappingExpr = xpath.compile("//web-app/filter-mapping");
	    
	    final NodeList mappingNodes = (NodeList)filterMappingExpr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < mappingNodes.getLength(); i++) {
	        final Element node = (Element)mappingNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();
	        final FilterMapping mapping = new FilterMapping();
	        
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}
	        	final Element child = (Element) children.item(j);
	        	
	        	final String attrName  = child.getNodeName();
	        	final String attrValue = child.getFirstChild().getNodeValue();
	        	
	        	if(attrName.equals("filter-name")) {
	        		mapping.setFilter(web.getFilter(attrValue));
	        	} else if(attrName.equals("url-pattern")) {
	        		mapping.setURLPattern(attrValue.replace("*", ".*"));
	        	} else {
	        		LOG.warn("Unknown filter-mapping attribute {}.", attrName);
	        	}
	        }
	        
	        web.getFilterMappings().add(mapping);
	        LOG.info("Found filter mapping {}.", mapping);
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
	        		LOG.warn("Unknown servlet-mapping attribute {}.", attrName);
	        	}
	        }

	        try {
	        	final Servlet servlet = web.getServlet(name);

	        	web.bindServlet(servlet, url);
	        } catch(final IllegalArgumentException e) {
	        	LOG.warn("Cannot bind wildcard urls");
	        }
	    }
	}

	/**
	 * Reads all servlets
	 * @param loader 
	 */
	private static void readServlets(final Document doc, final Set<Servlet> servlets, FileLoader loader) throws XPathException {
	    final XPathFactory factory = XPathFactory.newInstance();
	    final XPath xpath = factory.newXPath();
	    final XPathExpression servletExpr = xpath.compile("//web-app/servlet");

	    final NodeList servletNodes = (NodeList)servletExpr.evaluate(doc, XPathConstants.NODESET);

	    for (int i = 0; i < servletNodes.getLength(); i++) {
	        final Element node = (Element)servletNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();
	        
	        HttpServlet servlet = new HttpServlet();
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
				} else if(attrName.equals("init-param")) {
					final Parameter parameter = new Parameter();
					final NodeList paramNodes = child.getChildNodes();
					for(int k = 0; k < paramNodes.getLength(); ++k) {
						if(!(paramNodes.item(k) instanceof Element)) {
							continue;
						}

						final Element paramNode = (Element)paramNodes.item(k);

						if(paramNode.getNodeName().equals("param-name")) {
							parameter.setName(paramNode.getFirstChild().getNodeValue());
						} else if(paramNode.getNodeName().equals("param-value")) {
							parameter.setValue(paramNode.getFirstChild().getNodeValue());
						}
					}
					servlet.getParameters().add(parameter);
	        	} else {
	        		LOG.warn("Unknown servlet attribute {}.", attrName);
	        	}
	        }
			servlet.setLoader(loader);	        
	        servlets.add(servlet);
	        
	        // check validity
	        if(servlet.getName() == null || servlet.getClazz() == null) {
	        	LOG.error("Servlet not configured correctly {}.", servlet);
	        }
	    }
	}
}
