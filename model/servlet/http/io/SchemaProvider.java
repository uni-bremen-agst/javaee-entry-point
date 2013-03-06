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
}
