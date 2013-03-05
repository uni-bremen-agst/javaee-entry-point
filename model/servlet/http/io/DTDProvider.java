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
    public XPathExpression getServletExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/servlet");
    }
    
    @Override
    public XPathExpression getServletMappingExpression() throws XPathExpressionException {
    	return xpath.compile("//web-app/servlet-mapping");
    }
}
