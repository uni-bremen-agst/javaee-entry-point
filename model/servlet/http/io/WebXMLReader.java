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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.javaee.model.servlet.*;
import soot.jimple.toolkits.javaee.model.servlet.http.*;
import soot.jimple.toolkits.javaee.model.ws.WebMethod;
import soot.jimple.toolkits.javaee.model.ws.WebService;
import soot.jimple.toolkits.javaee.model.ws.WebService$;
import soot.jimple.toolkits.javaee.model.ws.WsServlet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A reader for {@code web.xml} files.
 * 
 * @author Bernhard Berger
 */
public class WebXMLReader implements ServletSignatures {
	/**
	 * Logger instance.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebXMLReader.class);
	
	/**
	 * XML document.
	 */
	private Document doc;

	/**
	 * Provider for all necessary XPaths.
	 */
	private XPathExpressionProvider provider;

	/**
	 * Model to fill.
	 */
	private Web web;
	
	public Web readWebXML(final FileLoader loader, final Web web) throws Exception {
	    final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); // never forget this!
	    domFactory.setValidating(false);
	    domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	    final DocumentBuilder builder = domFactory.newDocumentBuilder();
        final String rootPackage = web.getGeneratorInfos().getRootPackage();

	    try {
		    final InputStream is = loader.getInputStream("WEB-INF/web.xml");
		    doc = builder.parse(is);
		    provider = ProviderFactory.create(doc);
			this.web = web;
			
			readFilters();
			readSecurityConstraints();
			readServlets(loader);
			readServletMappings();
			readListeners();
            readServices(rootPackage);
	    } catch(final FileNotFoundException e) {
	    	LOG.error("Cannot find web.xml in {}.", loader);
	    }
		
		return web;
	}

	private void readSecurityConstraints() throws XPathException {
		LOG.info("Reading security constraints from web.xml.");
		
		final XPathExpression constraintExpression = provider.getSecurityConstraintExpression();	
	    final NodeList constraintNodes = (NodeList)constraintExpression.evaluate(doc, XPathConstants.NODESET);
	    
		LOG.info("Found {} security-constraint nodes.", constraintNodes.getLength());

		for (int i = 0; i < constraintNodes.getLength(); i++) {
            SecurityConstraint constraint = new SecurityConstraint();
	        final Element node = (Element)constraintNodes.item(i);

	        final NodeList children = node.getChildNodes();
	        for(int j = 0; j < children.getLength(); j++) {
	        	if(!(children.item(j) instanceof Element)) {
	        		continue;
	        	}

	        	final Element childNode = (Element)children.item(j);
		        if(childNode.getNodeName().equals("auth-constraint")) {
		        	readAuthConstraint(childNode, constraint);
		        } else if (childNode.getNodeName().equals("display-name")) {
		        	constraint.setName(childNode.getFirstChild().getNodeValue());
		        } else if(childNode.getNodeName().equals("web-resource-collection")) {
		        	final WebResourceCollection collection = new WebResourceCollection();
		        	readWebResourceCollection(childNode, collection);
		        	constraint.getWebResourceCollections().add(collection);
		        } else {
		        	LOG.info("Unhandled child of a security-constraint {}.", childNode.getNodeName());
		        }
	        }
            web.getSecurityConstraints().add(constraint);
	    }

	}

	private void readWebResourceCollection(final Element collectionNode, final WebResourceCollection collection) {
		final NodeList children = collectionNode.getChildNodes();
		
		for(int i = 0; i < children.getLength(); ++i) {
			if(!(children.item(i) instanceof Element)) {
				continue;
			}
			
			final Element child = (Element)children.item(i);
			
			if(child.getLocalName().equals("web-resource-name")) {
				collection.setName(child.getFirstChild().getNodeValue());
			} else if(child.getLocalName().equals("url-pattern")) {
				collection.getUrlPatterns().add(child.getFirstChild().getNodeValue());
			} else if(child.getLocalName().equals("http-method")) {
				collection.getHttpMethods().add(child.getFirstChild().getNodeValue());
			} else {
				LOG.info("Unhandled child '{}' in web-resource-name.", child.getLocalName());
			}
		}
	}

	private void readAuthConstraint(final Element authNode, SecurityConstraint constraint) {
		final NodeList children = authNode.getChildNodes();
		
		for(int i = 0; i < children.getLength(); ++i) {
			if(!(children.item(i) instanceof Element)) {
				continue;
			}
			
			final Element child = (Element)children.item(i);
			
			if(child.getLocalName().equals("role-name")) {
				LOG.info("Found required role '{}'.",  child.getFirstChild().getNodeValue());
				constraint.getRequiredRoles().add(child.getFirstChild().getNodeValue());
			} else {
				LOG.info("unhandled child '{}' in auth-constraint.", child.getLocalName());
			}
		}
	}

	private void readListeners() throws XPathException {
	    final XPathExpression listenerExpr = provider.getListenerExpression();

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
	private void readFilters() throws XPathException {
		LOG.info("Reading filters from web.xml");
		
		final XPathExpression filterExpr = provider.getFilterExpression();

		final NodeList filterNodes = (NodeList)filterExpr.evaluate(doc, XPathConstants.NODESET);

		LOG.info("Found {} filter nodes.", filterNodes.getLength());

		for (int i = 0; i < filterNodes.getLength(); i++) {
			final Element node = (Element)filterNodes.item(i);
			final NodeList children = node.getChildNodes();

			final Filter  filter = new Filter();
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
				} else if(attrName.equals("display-name") || attrName.equals("description")) {
					// ignore
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

		final XPathExpression filterMappingExpr = provider.getFilterMappingExpression();
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
     * Reads the service declarations
     * TODO.
     */
    private void readServices(final String rootPackage) throws XPathException {
        final XPathExpression servletMappingExpr = provider.getServletMappingExpression();

        final NodeList mappingNodes = (NodeList)servletMappingExpr.evaluate(doc, XPathConstants.NODESET);


        final List<WebService> foundServices = new ArrayList<WebService>();

        for (int i = 0; i < mappingNodes.getLength(); i++) {
            final Element node = (Element)mappingNodes.item(i);

            final NodeList children = node.getChildNodes();

            String name = null;
            String iface = null;
            String type = null;
            String wsdl = null;
            //We ignore jaxrpc-mapping-file, port-component-ref , handler, handler-chains
            String qName = null;

            for(int j = 0; j < children.getLength(); j++) {
                if(!(children.item(j) instanceof Element)) {
                    continue;
                }
                final Element child = (Element) children.item(j);

                final String attrName  = child.getNodeName();
                final String attrValue = child.getFirstChild().getNodeValue();

                if(attrName.equals("service-ref-name")) {
                    name = attrValue;
                } else if(attrName.equals("service-interface")) {
                    iface = attrValue;
                } else if(attrName.equals("service-ref-type")) {
                    type = attrValue;
                } else if(attrName.equals("wsdl-file")) {
                    wsdl = attrValue;
                } else if(attrName.equals("service-qname")) {
                    qName = attrValue;
                } else if(attrName.equals("port-component-ref")) {
                    LOG.warn("Skipped service-ref/{}",attrName);
                } else if(attrName.equals("handler")) {
                    LOG.warn("Skipped service-ref/{}",attrName);
                } else if(attrName.equals("handler-chains")) {
                    LOG.warn("Skipped service-ref/{}",attrName);
                } else if(attrName.equals("jaxrpc-mapping-file")) {
                    LOG.warn("Skipped service-ref/{}",attrName);
                } else {
                    LOG.warn("Unknown servlet-mapping attribute {}.", attrName);
                }
            }

            //TODO add other information found there to WebService
            final WebService service = new WebService(iface, type, WebService$.MODULE$.wrapperName(rootPackage,type) ,"", "","", "","", "","", Collections.<String>emptyList(), Collections.<WebMethod>emptyList(),false);

            foundServices.add(service);
            LOG.info("Found Web Service binding: {} -> {}, {}", name, iface, type);
        }

        WsServlet serv = new WsServlet(foundServices);
        web.bindServlet(serv, "/wscaller");
    }

	/**
	 * Reads the servlet-mappings.
	 */
	private void readServletMappings() throws XPathException {
	    final XPathExpression servletMappingExpr = provider.getServletMappingExpression();

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
	private void readServlets(FileLoader loader) throws XPathException {
		LOG.info("Reading servlets from web.xml");
		
		final Set<Servlet> servlets = web.getServlets();
		
	    final XPathExpression servletExpr = provider.getServletExpression();

	    final NodeList servletNodes = (NodeList)servletExpr.evaluate(doc, XPathConstants.NODESET);

	    LOG.info("Found {} servlet nodes.", servletNodes.getLength());
	    for (int i = 0; i < servletNodes.getLength(); i++) {
	        final Element node = (Element)servletNodes.item(i);
	        
	        final NodeList children = node.getChildNodes();
	        final AbstractServlet servlet = newServlet(node);

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

	/**
	 * Creates a new model servlet instance (either GenericServlet or HttpServlet).
	 * 
	 * @param servletNode XML servlet node.
	 * 
	 * @return A new instance.
	 */
	private static AbstractServlet newServlet(final Element servletNode) {
		final NodeList classNodes = servletNode.getElementsByTagName("servlet-class");
		
		if(classNodes.getLength() == 0) {
			LOG.error("Cannot find servlet-class for servlet {}.", servletNode);
			return new GenericServlet();
		}
		
		if(classNodes.getLength() > 1) {
			LOG.warn("Multiple classes for servlet {} found. Choosing first one.", servletNode);
		}
		
    	final String className = classNodes.item(0).getFirstChild().getNodeValue();

    	final SootClass implementationClass = Scene.v().forceResolve(className, SootClass.HIERARCHY);

	    final SootClass servletClass = Scene.v().getSootClass(HTTP_SERVLET_CLASS_NAME);
	    final SootClass genericClass = Scene.v().getSootClass(GENERIC_SERVLET_CLASS_NAME);
	    final Hierarchy cha = Scene.v().getActiveHierarchy();
	    
        if (cha.isClassSubclassOf(implementationClass, servletClass)){
        	LOG.info("Found http servlet class {}.", implementationClass);
            final HttpServlet servlet = new HttpServlet();
            
            scanMethods(implementationClass, servlet);
            
            return servlet;
        } else if(cha.isClassSubclassOf(implementationClass, genericClass)) {
        	LOG.info("Found generic servlet class {}.", implementationClass);
            return new GenericServlet();
        } else {
        	LOG.warn("Class '{}' is neither a http nor a generic servlet.", implementationClass);
        	return new GenericServlet();
        }
	}

	public static void scanMethods(final SootClass clazz, final HttpServlet servlet) {
		final Scene scene = Scene.v();

        if (scene.getSootClass("javax.servlet.GenericServlet").isPhantom()){
            LOG.info("Skipping - GenericServlet is phantom - this shouldn't be a problem on the first stub creation step");
            return;
        }

        if (scene.getSootClass("javax.servlet.http.HttpServlet - this shouldn't be a problem on the first stub creation step").isPhantom()){
            LOG.info("Skipping - HttpServlet is phantom");
            return;
        }

		final SootMethod serviceMethod1 = scene.getMethod("<javax.servlet.GenericServlet: void service(javax.servlet.ServletRequest,javax.servlet.ServletResponse)>");
		final SootMethod serviceMethod2 = scene.getMethod("<javax.servlet.http.HttpServlet: void service(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		final SootMethod doGetMethod = scene.getMethod("<javax.servlet.http.HttpServlet: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		final SootMethod doHeadMethod = scene.getMethod("<javax.servlet.http.HttpServlet: void doHead(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		final SootMethod doPostMethod = scene.getMethod("<javax.servlet.http.HttpServlet: void doPost(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		final SootMethod doPutMethod = scene.getMethod("<javax.servlet.http.HttpServlet: void doPut(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		final SootMethod doDeleteMethod = scene.getMethod("<javax.servlet.http.HttpServlet: void doDelete(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		final SootMethod doOptionsMethod = scene.getMethod("<javax.servlet.http.HttpServlet: void doOptions(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		final SootMethod doTraceMethod = scene.getMethod("<javax.servlet.http.HttpServlet: void doTrace(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");

		Hierarchy hierarchy = scene.getActiveHierarchy();
		SootMethod callee = hierarchy.resolveConcreteDispatch(clazz, serviceMethod1);
		
		if(hierarchy.isClassSubclassOf(callee.getDeclaringClass(), doGetMethod.getDeclaringClass())) {
			LOG.info("Have to call service for " + callee);
			servlet.getMethods().add(callee.getSignature());
			return;
		}
		
		callee = hierarchy.resolveConcreteDispatch(clazz, serviceMethod2);

		if(hierarchy.isClassSubclassOf(callee.getDeclaringClass(), serviceMethod2.getDeclaringClass())) {
			LOG.info("Have to call service2 for " + callee);
			servlet.getMethods().add(callee.getSignature());
		} else {
			checkForMethod(clazz, doGetMethod, servlet);
			checkForMethod(clazz, doHeadMethod, servlet);
			checkForMethod(clazz, doPostMethod, servlet);
			checkForMethod(clazz, doPutMethod, servlet);
			checkForMethod(clazz, doDeleteMethod, servlet);
			checkForMethod(clazz, doOptionsMethod, servlet);
			checkForMethod(clazz, doTraceMethod, servlet);
		}
	}

	private static void checkForMethod(final SootClass clazz, final SootMethod method, final HttpServlet servlet) {
		SootMethod callee = Scene.v().getActiveHierarchy().resolveConcreteDispatch(clazz, method);
		if(!callee.getDeclaringClass().equals(method.getDeclaringClass())) {
			LOG.info("Method {} is overriden.", method);
			servlet.getMethods().add(callee.getSignature());
		}
	}
}
