package soot.jimple.toolkits.javaee.model.servlet.http.io;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * The {@see WebXMLReader} uses XPath expressions to parse the {@code web.xml}. Since the newer versions use xml
 *   schemas with namespaces and the the older versions are {@code dtd}-based we have to dispatch. This interface
 *   defines the expressions all providers have to implement.
 * 
 * @author Bernhard Berger
 */
public abstract class XPathExpressionProvider {
	
	/**
	 * XPath factory
	 */
    protected final XPathFactory factory = XPathFactory.newInstance();

    /**
     * XPath instance.
     */
    protected final XPath xpath = factory.newXPath();
    
	/**
	 * @return A XPath expression to select all available filter nodes.
	 */
	public abstract XPathExpression getFilterExpression() throws XPathExpressionException;
    
	/**
	 * @return A XPath expression to select all available filter-mapping nodes.
	 */
	public abstract XPathExpression getFilterMappingExpression() throws XPathExpressionException;

	/**
	 * @return A XPath expression to select all available listener nodes.
	 */
	public abstract XPathExpression getListenerExpression() throws XPathExpressionException;

	/**
	 * @return A XPath expression to select all available security-constraint nodes.
	 */
	public abstract XPathExpression getSecurityConstraintExpression() throws XPathExpressionException;

	/**
	 * @return A XPath expression to select all available servlet nodes.
	 */
	public abstract XPathExpression getServletExpression() throws XPathExpressionException;

	/**
	 * @return A XPath expression to select all available servlet-mapping nodes.
	 */
	public abstract XPathExpression getServletMappingExpression() throws XPathExpressionException;
}
