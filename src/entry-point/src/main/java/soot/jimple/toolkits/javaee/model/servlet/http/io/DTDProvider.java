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
