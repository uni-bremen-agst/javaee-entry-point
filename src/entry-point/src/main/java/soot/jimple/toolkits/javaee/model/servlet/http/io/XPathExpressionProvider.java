/**
 * Copyright 2013 Bernhard Berger - Universität Bremen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    /**
     * @return A XPath expression to select all available servlet-mapping nodes.
     */
    public abstract XPathExpression getServiceExpression() throws XPathExpressionException;

}
