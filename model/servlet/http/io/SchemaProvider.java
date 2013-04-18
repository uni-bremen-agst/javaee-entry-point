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

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchemaProvider extends XPathExpressionProvider {
	/**
	 * Logger instance.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(SchemaProvider.class);
    
    /**
     * Register {@code javaee} context.
     */
    private final NamespaceContext context = new NamespaceContext() {

		@Override
		public String getNamespaceURI(String prefix) {
			if(prefix.equals("javaee")) {
				return "http://java.sun.com/xml/ns/javaee";
			}
			LOG.warn("Looking for unbound namespace " + prefix);
			return null;
		}

		@Override
		public String getPrefix(String arg0) {
		    throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<?> getPrefixes(String arg0) {
		    throw new UnsupportedOperationException();
		}
    }; 
    
    public SchemaProvider() {
    	xpath.setNamespaceContext(context);
	}   

    @Override
    public XPathExpression getFilterExpression() throws XPathExpressionException {
    	return xpath.compile("//javaee:web-app/javaee:filter");
    }

    @Override
    public XPathExpression getFilterMappingExpression() throws XPathExpressionException {
    	return xpath.compile("//javaee:web-app/javaee:filter-mapping");
    }

    @Override
    public XPathExpression getListenerExpression() throws XPathExpressionException {
    	return xpath.compile("//javaee:web-app/javaee:listener");
    }
    
    @Override
    public XPathExpression getSecurityConstraintExpression() throws XPathExpressionException {
    	return xpath.compile("//javaee:web-app/javaee:security-constraint");
    }
    
    @Override
    public XPathExpression getServletExpression() throws XPathExpressionException {
    	return xpath.compile("//javaee:web-app/javaee:servlet");
    }
    
    @Override
    public XPathExpression getServletMappingExpression() throws XPathExpressionException {
    	return xpath.compile("//javaee:web-app/javaee:servlet-mapping");
    }

    @Override
    public XPathExpression getServiceExpression() throws XPathExpressionException {
        //Defined here: http://www.oracle.com/webfolder/technetwork/jsc/xml/ns/javaee/javaee_web_services_client_1_3.xsd
        return xpath.compile("//javaee:web-app/javaee:service-ref");
    }

}
