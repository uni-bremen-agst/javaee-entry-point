package soot.jimple.toolkits.javaee.model.servlet.struts1.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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


import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
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
	/**
	 * Logger.
	 */
	private final static Logger LOG = LoggerFactory.getLogger(StrutsReader.class);

	/**
	 * The web instance we are going to populate.
	 */
	private final Web web;
	
	/**
	 * The input stream of the configuration file.
	 */
	private final InputStream configFileStream;

	/**
	 * Command-line options.
	 */
	@SuppressWarnings("rawtypes")
	private Map options;

	public StrutsReader(@SuppressWarnings("rawtypes") final Map options, final Web web, final InputStream configFileStream) {
		this.options = options;
		this.web = web;
		this.configFileStream = configFileStream;
	}

	/**
	 * Maps a form bean name to its form bean.
	 */
	private final Map<String, FormBean> formBeanMapping = new HashMap<String, FormBean>();

	/**
	 * XML document.
	 */
	private Document doc;

	/**
	 * XPath factory.
	 */
	private final XPathFactory factory = XPathFactory.newInstance();

	/**
	 * XPath.
	 */
	private final XPath xpath = factory.newXPath();

	/**
	 * Reads the configuration file and adds all information to {@code web}.
	 */
	public void readStrutsConfig() {
		try {
			final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			final DocumentBuilder builder = domFactory.newDocumentBuilder();
			doc = builder.parse(configFileStream);

			readFormBeans();
			readGlobalForwards();
			readActions();

		} catch (final ParserConfigurationException e) {
			LOG.error("Unable to create DOM.", e);
		} catch (SAXException e) {
			LOG.error("Unable to parse XML.", e);
		} catch (IOException e) {
			LOG.error("Unable to read file.", e);
		} catch (final XPathExpressionException e) {
			LOG.error("Unable to create XPath expression.", e);
		}
	}

	/**
	 * Reads all actions from {@link doc} and stores them in {@link web}.
	 */
	private void readActions() throws XPathExpressionException {
		final XPathExpression actionExpr = xpath.compile("//struts-config/action-mappings/action");

		final NodeList actionNodes = (NodeList) actionExpr.evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < actionNodes.getLength(); i++) {
			final Element actionNode = (Element) actionNodes.item(i);
			final ActionServlet servlet = new ActionServlet();

			final String path = actionNode.getAttribute("path");
			final String type = actionNode.getAttribute("type");
			final String name = actionNode.getAttribute("name");
			final String scope = actionNode.getAttribute("scope");
			final String validate = actionNode.getAttribute("validate");

			if(type == null || type.isEmpty()) {
				LOG.warn("Action for path {} has no type - Skipping.", path);
				continue;
			}

			if(path == null || path.isEmpty()) {
				LOG.warn("Action {} has no associated path - Skipping.", type);
				continue;
			}

			if(SourceLocator.v().getClassSource(type) == null) {
				LOG.warn("Failed to resolve class " + type + " - Skipping.");
				continue;
			}

			servlet.setName(type); // set unique name to type name
			servlet.setActionClass(type);
			servlet.setClazz(PhaseOptions.getString(options, "root-package") + "." + type + "Servlet");
			servlet.setScope(scope);
			servlet.setValidate(validate.toLowerCase().equals("true"));
			servlet.setFormBean(formBeanMapping.get(name));

			if(actionNode.hasAttribute("parameter")) {
				servlet.setParameter(actionNode.getAttribute("parameter"));
			}

			// scan for methods
			final FormBean formbean = servlet.getFormBean();
			SootClass actionClass = Scene.v().forceResolve(type, SootClass.SIGNATURES);
			for(final SootMethod method : actionClass.getMethods()) {
				if(isActionMethod(method, formbean)) {
					servlet.getMethods().add(method.getSubSignature());
				}
			}

			web.getServlets().add(servlet);
			web.bindServlet(servlet, path);	

			servlet.getForwards().addAll(globalForwards);
			readForwards(actionNode, servlet.getForwards());
		}
	}

	/**
	 * Checks if method is a valid action method that can be called by a client.
	 * 
	 * @param method Soots method representation.
	 * @param formbean The formbean parameter if present. Otherwise {@code null}.
	 */
	private static boolean isActionMethod(final SootMethod method, final FormBean formbean) {
		return method.isPublic()
				&& method.getParameterCount() == 4
				&& method.getReturnType().toString().equals("org.apache.struts.action.ActionForward")
				&& method.getParameterType(0).toString().equals("org.apache.struts.action.ActionMapping")
				&& (method.getParameterType(1).toString().equals("org.apache.struts.action.ActionForm")
						|| (formbean != null && method.getParameterType(1).toString().equals(formbean.getClazz())))
				&& method.getParameterType(2).toString().equals("javax.servlet.http.HttpServletRequest")
				&& method.getParameterType(3).toString().equals("javax.servlet.http.HttpServletResponse");
	}

	/**
	 * Read all forwards from {@code node} and stores them in {@code forwads}.
	 * 
	 * @param node The DOM node containing forwards.
	 * @param forwards The target for the forwards.
	 */
	private void readForwards(final Element actionNode, final List<ActionForward> forwards) {
		final NodeList children = actionNode.getChildNodes();
		for(int j = 0; j < children.getLength(); ++j) {
			if(!(children.item(j) instanceof Element)) {
				continue;
			}

			final Element child = (Element)children.item(j);
			if(child.getNodeName().equals("forward")) {
				final ActionForward forward = new ActionForward();
				forward.setName(child.getAttribute("name"));

				// forward may consist of an destination and additional parameters
				final String [] dest = child.getAttribute("path").split("\\?");				

				forward.setDestination(web.resolveAddress(dest[0]));
				if(dest.length > 1) {
					forward.setParameter(dest[1]);
				}

				forwards.add(forward);
			} else {
				LOG.warn("Found child with unknown name {}.", child.getNodeName());
			}
		}
	}

	/**
	 * List of all global forwards.
	 */
	private final List<ActionForward> globalForwards = new LinkedList<ActionForward>();

	/**
	 * Reads all global forward entries and stores them in {@link #globalForwards}.
	 */
	private void readGlobalForwards() throws XPathExpressionException {
		final XPathExpression globalForwardExpr = xpath.compile("//struts-config/global-forwards");

		final NodeList globalForwardNodes = (NodeList) globalForwardExpr.evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < globalForwardNodes.getLength(); i++) {
			final Element globalForwardNode = (Element) globalForwardNodes.item(i);

			readForwards(globalForwardNode, globalForwards);
		}
	}

	/**
	 * Reads all form beans and stores them in {@link #formBeanMapping}.
	 */
	private void readFormBeans() throws XPathExpressionException {
		final XPathExpression listenerExpr = xpath.compile("//struts-config/form-beans/form-bean");

		final NodeList formBeanNodes = (NodeList) listenerExpr.evaluate(doc, XPathConstants.NODESET);

		for (int i = 0; i < formBeanNodes.getLength(); i++) {
			final Element node = (Element) formBeanNodes.item(i);

			final FormBean formbean = new FormBean();
			formbean.setName(node.getAttribute("name"));
			formbean.setClazz(node.getAttribute("type"));
			
			if(formbean.getName() == null || formbean.getName().isEmpty()) {
				LOG.warn("Found form bean without name - Skipping.");
				continue;
			}

			if(formbean.getClazz() == null || formbean.getClazz().isEmpty()) {
				LOG.warn("Found form bean {} without type - Skipping.", formbean.getName());
				continue;
			} else {
				if(SourceLocator.v().getClassSource(formbean.getClazz()) == null) {
					LOG.warn("Failed to resolve form bean class " + formbean.getClazz() + " - Skipping.");
					continue;
				}
			}

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
			
			formBeanMapping.put(formbean.getName(), formbean);
		}
	}
}
