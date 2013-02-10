package soot.jimple.toolkits.javaee.model.servlet.struts1.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soot.jimple.toolkits.javaee.model.servlet.Web;
import soot.jimple.toolkits.javaee.model.servlet.struts1.ActionForward;
import soot.jimple.toolkits.javaee.model.servlet.struts1.ActionServlet;
import soot.jimple.toolkits.javaee.model.servlet.struts1.FormBean;
import soot.jimple.toolkits.javaee.model.servlet.struts1.FormProperty;

/**
 * Reader for Struts 1 configuration files.
 * 
 * @author Bernhard Berger
 */
public class StrutsReader {
	private final static Logger LOG = LoggerFactory
			.getLogger(StrutsReader.class);

	public static void readStrutsConfig(final Web web,
			final InputStream configFileStream) {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(configFileStream);

			readFormBeans(doc, web);
			readGlobalForwards(doc, web);
			readActions(doc, web);

		} catch (final ParserConfigurationException e) {

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	private static void readActions(Document doc, Web web) throws XPathExpressionException {
		final XPathFactory factory = XPathFactory.newInstance();
		final XPath xpath = factory.newXPath();
		final XPathExpression actionExpr = xpath.compile("//action-mappings/action");

		final NodeList actionNodes = (NodeList) actionExpr.evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < actionNodes.getLength(); i++) {
			final Element actionNode = (Element) actionNodes.item(i);
			final ActionServlet servlet = new ActionServlet();
			
			String path = actionNode.getAttribute("path");
			String type = actionNode.getAttribute("type");
			String name = actionNode.getAttribute("name");
			String scope = actionNode.getAttribute("scope");
			String validate = actionNode.getAttribute("validate");
			
			servlet.setName(name);
			servlet.setClazz(type);
			servlet.setScope(scope);
			servlet.setValidate(validate.toLowerCase().equals("true"));
			
			if(actionNode.hasAttribute("parameter")) {
				servlet.setParameter(actionNode.getAttribute("parameter"));
			}
			
			web.getServlets().add(servlet);
			web.bindServlet(servlet, path);	
			
			final NodeList children = actionNode.getChildNodes();
			for(int j = 0; j < children.getLength(); ++j) {
				if(!(children.item(j) instanceof Element)) {
					continue;
				}
				
				final Element child = (Element)children.item(j);
				if(child.getNodeName().equals("forward")) {
					ActionForward forward = new ActionForward();
					forward.setName(child.getAttribute("name"));
					
					String [] dest = child.getAttribute("path").split("\\?");
					
					forward.setDestination(web.resolveAddress(dest[0]));
					if(dest.length > 1) {
						forward.setParameter(dest[1]);
					}
					
					servlet.getForwards().add(forward);
				} else {
					LOG.warn("Found child with unknown name {}.", child.getNodeName());
				}
			}
		}
	}

	private static void readGlobalForwards(Document doc, Web web) {
		// TODO Auto-generated method stub

	}

	private static void readFormBeans(final Document doc, final Web web)
			throws XPathExpressionException {
		final XPathFactory factory = XPathFactory.newInstance();
		final XPath xpath = factory.newXPath();
		final XPathExpression listenerExpr = xpath
				.compile("//struts-config/form-beans/form-bean");

		final NodeList formBeanNodes = (NodeList) listenerExpr.evaluate(doc,
				XPathConstants.NODESET);

		for (int i = 0; i < formBeanNodes.getLength(); i++) {
			final Element node = (Element) formBeanNodes.item(i);

			final FormBean formbean = new FormBean();
			formbean.setName(node.getAttribute("name"));
			formbean.setClazz(node.getAttribute("type"));

			final NodeList beanChildren = node.getChildNodes();
			for (int j = 0; j < beanChildren.getLength(); ++j) {
				if (!(beanChildren.item(j) instanceof Element)) {
					continue;
				}

				final Element element = (Element) beanChildren.item(j);

				if (element.getNodeName().equals("form-property")) {
					final FormProperty property = new FormProperty();
					property.setName(element.getAttribute("name"));
					property.setClazz(element.getAttribute("type"));

					formbean.getProperties().add(property);
				} else {
					LOG.warn("Unknown node type {}.", element.getNodeName());
				}
			}
		}
	}
}
