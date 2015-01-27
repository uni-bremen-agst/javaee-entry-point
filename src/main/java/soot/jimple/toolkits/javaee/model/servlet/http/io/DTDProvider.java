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
   along with Soot entry point creator.  If not, see <http://www.gnu.org/licenses/>.

   Copyright 2015 Universität Bremen, Working Group Software Engineering
   Copyright 2015 Ecole Polytechnique de Montreal & Tata Consultancy Services
*/
package soot.jimple.toolkits.javaee.model.servlet.http.io;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

public class DTDProvider extends XPathExpressionProvider {

    @Override
    public XPathExpression getFilterExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/filter");
    }

    @Override
    public XPathExpression getFilterMappingExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/filter-mapping");
    }

    @Override
    public XPathExpression getListenerExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/listener");
    }
    
    @Override
    public XPathExpression getSecurityConstraintExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/security-constraint");
    }
    
    @Override
    public XPathExpression getServletExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/servlet");
    }
    
    @Override
    public XPathExpression getServletMappingExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/servlet-mapping");
    }

    @Override
    public XPathExpression getServiceExpression() throws XPathExpressionException {
        //TODO
        return xpath.compile("//javaee:web-app/javaee:service-ref");
    }
}
