/*
   This file is part of Soot entry point creator.

   Soot entry point creator is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Soot entry point creator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Soot entry point creator.  If not, see
<http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
   Copyright 2015 Ecole Polytechnique de Montreal & Tata Consultancy Services
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
