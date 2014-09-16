/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.detectors

import java.io.File
import java.net.{MalformedURLException, URL}
import javax.xml.bind.{JAXB, JAXBContext}

import ca.polymtl.gigl.casi.Logging
import org.jcp.xmlns.javaee.HandlerChainsType
import soot._
import soot.jimple.toolkits.javaee.WebServiceRegistry
import soot.jimple.toolkits.javaee.model.servlet.Web
import soot.jimple.toolkits.javaee.model.ws.{WebService, WsServlet, _}
import soot.tagkit.SourceFileTag
import soot.util.ScalaWrappers._
import soot.util.SootAnnotationUtils._

import scala.collection.JavaConverters._


/**
 * Utilities to determine the values of JAX-WS services' attributes
 * @author Marc-André Laverdière-Papineau
 **/
object JaxWSAttributeUtils extends Logging {

  private lazy val handlerChainJaxbContext = JAXBContext.newInstance("org.jcp.xmlns.javaee")

  /**
   * Reverses a package name, so that e.g. scala.collection.mutable becomes mutable.collection.scala
   * @param pkg the package name
   * @return the reversed package name
   */
  def reversePackageName(pkg: String): String = {
    pkg.split("\\.").reverse.mkString(".")
  }

  /**
   * Checks if a class has all methods implemented from another as follows:
   *
   * m = methods of the reference
   * c = concrete methods of the implementor
   *
   * This method checks that m intersection c = m.
   *
   * This definition means that the implementor may have additional methods that are not defined
   * in the reference that will not affect the result of this function.
   *
   * @param reference the reference class - all its method must be implemented by the implementor
   * @param implementor the implementor class.
   * @return true if the criteria is met, false otherwise.
   */
  def implementsAllMethods(implementor: SootClass, reference: SootClass): Boolean = {
    val referenceMethodSignatures = reference.methods.map(_.getSubSignature)
    val implementorMethodSignatures = implementor.methods.map(_.getSubSignature).toSet
    referenceMethodSignatures.forall(implementorMethodSignatures.contains(_))
  }


  /**
   * Determines the local name of the service. This is not the same as the service name
   * JSR 181 section 4.1.1 Default: short name of the class or interface
   * @param sc the implementation class
   * @param annotationElems the annotations on the class
   * @return a non-empty string with the name of the service
   */
  def localName(sc: SootClass, annotationElems: Map[String, Any]): String = {
    annotationElems.getOrElse("name", sc.getShortName).toString
  }

  /**
   * Determines the WSDL location, as a local URL only.
   *
   * JAX-WS 2.2 Rev a sec 5.2.5 p.71 Default is the empty string
   * 5.2.5.1 p.77 WSDL needed only if SOAP 1.1/HTTP Binding
   * 5.2.5.3 WSDL is not generated on the fly, but package with the application
   * Practically, it gets mapped to http://host:port/approot/servicename?wsdlTODO: find the official spec for that
   * @param serviceName the name of the service
   * @param annotationElems the annotations on the implementing class
   *
   * @return a non-empty string
   */
  def wsdlLocation(serviceName: String, annotationElems: Map[String, Any]): String = {
    annotationElems.get("wsdlLocation").getOrElse(serviceName + "?wsdl").asInstanceOf[String]
  }

  /**
   * Determines the target namespace
   *
   * JAX-WS 2.2 Rev a sec 3.2 p.33-34
   * If the namespace is not specified for the service name, check for the service interface
   * A default value for the targetNamespace attribute is derived from the package name as follows:
   * 1. The package name is tokenized using the “.” character as a delimiter.
   * 2. The order of the tokens is reversed.
   * 3. The value of the targetNamespace attribute is obtained by concatenating “http://”to the list of
   * tokens separated by “ . ”and “/”.
   *
   * @param sc the implementation class
   * @param annotationElems the annotations on the implementation class
   * @return a non-empty string
   */
  def targetNamespace(sc: SootClass, annotationElems: Map[String, Any]): String = {
    annotationElems.getOrElse("targetNamespace", "http://" + reversePackageName(sc.getPackageName) + "/").toString
  }

  /**
   * Determines the port name
   *
   * JAX-WS 2.2 Rev a sec 3.11 p.54 In the absence of a portName element, an implementation
   * MUST use the value of the name element of the WebService annotation, if present, suffixed with
   * “Port”. Otherwise, an implementation MUST use the simple name of the class annotated with WebService
   * suffixed with “Port”.
   *
   * @param name the name of the service
   * @param annotationElems the annotations on the implementing class
   *
   * @return a non-empty string
   */
  def portName(name: String, annotationElems: Map[String, Any]): String = {
    annotationElems.getOrElse("portName", name + "Port").asInstanceOf[String]
  }

  /**
   * Determines the service name
   *
   * JAX-WS 2.2 Rev a sec 3.11 p.51
   * In mapping a @WebService-annotated class (see 3.3) to a wsdl:service, the serviceName element
   * of the WebService annotation are used to derive the service name. The value of the name attribute of
   * the wsdl:service element is computed according to the JSR-181 [15] specification. It is given by the
   * serviceName element of the WebService annotation, if present with a non-default value, otherwise the
   * name of the implementation class with the “Service”suffix appended to it.
   * Translation:
   * - if serviceName is set, use that
   * - if name is set, use that + "Service"
   * - if name is not set, use the short class name + "Service"
   * Since name is defaulted to the short name, we can ignore the last rule
   *
   * @param name the name of the serice
   * @param annotationElems the annotations on the implementation class
   * @return a non-empty string
   */
  def serviceName(name: String, annotationElems: Map[String, Any]): String = {
    annotationElems.getOrElse("serviceName", name + "Service").toString
  }

  def operationName(methodName: String, annotationElems: Map[String, Any]): String = {
    annotationElems.getOrElse("operationName", methodName).toString
  }

  /**
   * Helper function that tries to read the file on that class - if it is an URL
   * @param sc the soot class we are dealing with - used in logging
   * @param file the handler configuration file location
   * @return an Option over the handler chain XML type
   **/
  private def handlerChainAsURL(sc: SootClass, file: String): Option[HandlerChainsType] = {
    try {
      val url = new URL(file)
      logger.info("For class {}, handler file is located at: {}", sc, url)
      val jc = JAXBContext.newInstance("org.jcp.xmlns.javaee")
      val unmarshaller = jc.createUnmarshaller()
      Some(unmarshaller.unmarshal(url).asInstanceOf[HandlerChainsType])
    } catch {
      case _: MalformedURLException => None
    }
  }

  /**
   * Helper function that tries to read the file on that class - and guesses where it could be
   * @param sc the soot class we are dealing with - used in logging
   * @param file the handler configuration file location
   * @return an Option over the handler chain XML type
   **/
  private def handlerChainAsFile(sc: SootClass, file: String): Option[HandlerChainsType] = {
    val location = findAnnotation(sc, classOf[SourceFileTag]).map(_.getAbsolutePath)
    val locationFile = location.map(new File(_))

    locationFile.flatMap { f: File =>
      val handlerFile = new File(f.getParent, file)
      if (handlerFile.exists()) {
        logger.info("For class {}, handler file is located at: {}", sc, handlerFile)
        val unmarshalled = JAXB.unmarshal(handlerFile, classOf[HandlerChainsType])
        Some(unmarshalled)

      }
      else {
        logger.warn("For class {}, handler file was wrongly located at: {}", sc, handlerFile)
        None
      }
    }
  }

  /**
   * Helper function that tries to read the file on that class
   * @param sc the soot class we are dealing with - used in logging
   * @param file the handler configuration file location
   * @return an Option over the handler chain XML type
   **/
  private def handlerChain(sc: SootClass, file: String): Option[HandlerChainsType] = {
    val isUrl = handlerChainAsURL(sc, file)
    if (isUrl.isDefined)
      isUrl
    else
      handlerChainAsFile(sc, file)
  }


  /**
   * Checks if the given class has the @HandlerChain annotation.
   * If so, it retrieves the file specified in the annotation
   * and tries to locate it on the file system (relatively to the class' location)
   * @param sc the class to get the handlers for
   * @return an option to the handler chains
   **/
  def handlerChainOption(sc: SootClass): Option[HandlerChainsType] = {

    for (handlerChainAnn <- findJavaAnnotation(sc, HANDLER_CHAIN_ANNOTATION);
         elements = annotationElements(handlerChainAnn);
         file <- elements.get("file").asInstanceOf[Option[String]];
         chain <- handlerChain(sc, file)
    ) yield chain

  }

}

import soot.jimple.toolkits.javaee.detectors.JaxWSAttributeUtils._

object JaxWsServiceDetector extends Logging {

  final val GENERATED_CLASS_NAME: String = "WSCaller"

  lazy private val stringType = Scene.v.refType("java.lang.Sting")
  lazy private val responseType = Scene.v.refType("javax.xml.ws.Response")
  lazy private val futureType = Scene.v.refType("java.util.concurrent.Future")

  /**
   * Generates the `WebService` model object based on all the information provided
   * @param sc implementation class
   * @param rootPackage the root package
   * @param serviceIfaceName service interface name. Idem to `sc` for self-contained services
   * @param serviceMethods all operations implemented by this service
   * @return a `WebService` object wrapping all that
   */
  private def generateModel(sc: SootClass, rootPackage: String, serviceIfaceName: String,
                            annotationChain: Map[String, Any], serviceMethods: Traversable[WebMethod]): WebService = {

    val postInitMethod: Option[String] = sc.methods.find(hasJavaAnnotation(_, WEBSERVICE_POSTINIT_ANNOTATION)).map(_.getName)
    val preDestroyMethod: Option[String] = sc.methods.find(hasJavaAnnotation(_, WEBSERVICE_PREDESTROY_ANNOTATION)).map(_.getName)

    val name: String = localName(sc, annotationChain)
    val srvcName: String = serviceName(name, annotationChain)
    val prtName: String = portName(name, annotationChain)
    val tgtNamespace: String = targetNamespace(sc, annotationChain)
    val wsdlLoc: String = wsdlLocation(srvcName, annotationChain)
    val hasAsyncAlready = serviceMethods.find(wsm => wsm.targetMethodName.endsWith("Async") && (wsm.retType == responseType || wsm.retType == futureType)).isDefined


    serviceMethods.foreach(wm => logger.trace("Web method {} hash: {}", wm, wm.hashCode(): Integer))

    // ------------- Detect handler chain on the server and parse it --------
    val handlerChainOpt = handlerChainOption(sc)
    if (handlerChainOpt.isDefined) {
      logger.warn("Service {} is using an handler chain. This is not supported by the analysis.", sc.name)
    }
    /* val chain : List[String] = for (
       handlerChain <- handlerChainOpt.toList;
       chain <- handlerChain.getHandlerChain.asScala;
       handler <- chain.getHandler.asScala
     ) yield handler.getHandlerClass.getValue

     if (!chain.isEmpty)
       logger.warn("Non-empty handler chain !!!!!!!!!!! {}", sc.getName)
      */

    val chain = List[String]()

    // ------------- Determine the name of the wrapper
    val wrapperName = WebService.wrapperName(rootPackage, sc.name)

    // ------------- Log and create holder object                    -------
    logger.debug("Found WS. Interface: {} Implementation: {}. Wrapper: {}. Init: {} Destroy: {} Name: {} Namespace: {} " +
      "ServiceName: {} wsdl: {} port: {}.\tMethods: {}",
      serviceIfaceName, sc.name, wrapperName, postInitMethod.getOrElse(""), preDestroyMethod.getOrElse(""), name, tgtNamespace,
      srvcName, wsdlLoc, prtName, serviceMethods, hasAsyncAlready: java.lang.Boolean
    )

    val ws = WebService(
      serviceIfaceName, sc.name, wrapperName, postInitMethod.getOrElse(""), preDestroyMethod.getOrElse(""), name, tgtNamespace,
      srvcName, wsdlLoc, prtName, chain.asJava, serviceMethods.toList.asJava, hasAsyncAlready
    )

    ws.methods.asScala.foreach(_.service = ws)
    ws
  }

  /**
   * Extracts web service information when the interface is known
   * @param sc the implementation class
   * @param fastHierarchy the hierarchy object
   * @param rootPackage the root package
   * @param serviceInterface the service's specification interface
   * @return
   */
  def extractWsInformationKnownIFace(sc: SootClass, fastHierarchy: FastHierarchy,
                                     rootPackage: String, serviceInterface: SootClass): WebService = {
    //Detect method names
    // JSR-181, p. 35, section 3.5 operation name is @WebMethod.operationName. Default is in Jax-WS 2.0 section 3.5
    // JAX-WS 2.2 Rev a sec 3.5 p.35 Default is the name of the method
    //TODO double-check this matching rule
    val potentialMethods = sc.methods.filterNot(_.isConstructor).filter(_.isConcrete)

    //JBOSS-WS Test case in org.jboss.test.ws.jaxws.samples.webservice has no @WebMethod annotation on either interface nor implementation class
    val serviceMethods: Traversable[WebMethod] = for (
      sm <- potentialMethods.filterNot(m => m.isConstructor || m.isClinit || m.isStatic);
      subsig = sm.getSubSignature;
      seiMethod <- serviceInterface.methodOpt(subsig);
      //if (hasJavaAnnotation(sm,WEBMETHOD_ANNOTATION) || hasJavaAnnotation(seiMethod,WEBMETHOD_ANNOTATION));
      methodAnn = elementsForJavaAnnotation(sm, WEBMETHOD_ANNOTATION) withDefault elementsForJavaAnnotation(seiMethod, WEBMETHOD_ANNOTATION);
      opName = operationName(sm.getName, methodAnn);
      targetOpName = if (opName(0).isUpper) opName(0).toLower + opName.drop(1) else opName
    ) yield new WebMethod(null, targetOpName, sm.name, sm.parameterTypes.toList.asJava, sm.returnType)

    val annotationChain: Map[String, Any] = elementsForJavaAnnotation(sc, WEBSERVICE_ANNOTATION) withDefault elementsForJavaAnnotation(serviceInterface, WEBSERVICE_ANNOTATION)
    generateModel(sc, rootPackage, serviceInterface.name, annotationChain, serviceMethods)

  }

  /**
   * Extracts web service information when the WS is self-contained (i.e. has no interface at all)
   * @param sc the implementation class
   * @param rootPackage the root package
   * @return
   */
  def extractWsInformationSelfContained(sc: SootClass, rootPackage: String): WebService = {
    val operations = sc.methods.collect {
      case sm if hasJavaAnnotation(sm, WEBMETHOD_ANNOTATION) =>
        val implAnn = elementsForJavaAnnotation(sm, WEBMETHOD_ANNOTATION);
        val opName = implAnn.getOrElse("operationName", sm.getName).asInstanceOf[String]
        val targetOpName = if (opName(0).isUpper) opName(0).toLower + opName.drop(1) else opName
        WebMethod(service = null, name = targetOpName, targetMethodName = sm.name, retType = sm.returnType, argTypes = sm.getParameterTypes)
    }

    val annotationElems: Map[String, Any] = elementsForJavaAnnotation(sc, WEBSERVICE_ANNOTATION)
    generateModel(sc, rootPackage, sc.name, annotationElems, operations)
  }

  /**
   * Determine which class is the service's interface
   * @param sc the implementation class
   * @param fastHierarchy the hierarchy object
   * @return an option to the class that specifies the WS' interface. In the case that the class is self-contained,
   *         we return Some(`sc`)
   */
  def determineSEI(sc: SootClass, fastHierarchy: FastHierarchy): Option[SootClass] = {
    val annotationElems: Map[String, Any] = elementsForJavaAnnotation(sc, WEBSERVICE_ANNOTATION)
    annotationElems.get("endpointInterface") match {
      case None =>
        //Check if the implemented interface is a WS - CXF workaround
        val interfaceWithWS: Option[SootClass] = sc.interfaces.find(hasJavaAnnotation(_, WEBSERVICE_ANNOTATION))
        interfaceWithWS orElse Some(sc)
      case Some(ei) =>
        //JSR-181, section 3.1, page 13: the implementing class only needs to implement methods in the interface
        //If it is an implementing class, then it meets that criteria for sure
        //It could also not implement the interface, but have the same signatures.
        val iface = Scene.v.getSootClass(ei.asInstanceOf[String])
        if (hasJavaAnnotation(iface, WEBSERVICE_ANNOTATION) &&
          (fastHierarchy.canStoreType(sc.getType, iface.getType) || implementsAllMethods(sc, iface))) {
          //All good. The specified interface is implemented and it has the annotation
          Some(iface)
        } else None
    }
  }

}


import soot.jimple.toolkits.javaee.detectors.JaxWsServiceDetector._

/**
 * Detector for Jax-WS 2.0 Web Services
 * @author Marc-André Laverdière-Papineau
 */
class JaxWsServiceDetector extends AbstractServletDetector with Logging {

  override def detectFromSource(web: Web) {
    val rootPackage: String = web.getGeneratorInfos.getRootPackage
    val foundWs = findWSInApplication(rootPackage)
    if (!foundWs.isEmpty) {
      val newServlet = new WsServlet(foundWs.asJava)
      val fullName = rootPackage + "." + GENERATED_CLASS_NAME
      newServlet.setClazz(fullName)
      newServlet.setName(GENERATED_CLASS_NAME)
      web.getServlets.add(newServlet)
      web.bindServlet(newServlet, "/wscaller")
    }

    logger.info("Found {} web services, representing {} operations", foundWs.size, foundWs.map(_.methods.size).sum)

    WebServiceRegistry.services = foundWs
  }

  override def detectFromConfig(web: Web) {
    //TODO avoid redundancy with HTTPServletDetector
    //TODO handle other config files

    logger.warn("Detecting Web services from configuration files is not supported yet - switching to detection from source")
    detectFromSource(web)
    /*

logger.info("Detecting web services from web.xml.")
val webInfClassFolders = SourceLocator.v.classPath.asScala.filter(_.endsWith("WEB-INF/classes"))
val webXmlFiles = webInfClassFolders.map(new File(_).getParentFile).map(new File(_, "web.xml")).filter(_.exists())
val webRootFiles = webXmlFiles.map(_.getParentFile)

try{
val fileLoaders = webRootFiles.map(new FileLoader(_))
fileLoaders.foreach(new WebXMLReader().readWebXML(_, web))
} catch {
case e: IOException => logger.info("Cannot read web.xml:", e)
}                                      */

  }

  // ----------------------- Template part of the interface
  override def getModelExtensions: java.util.List[Class[_]] = List[Class[_]](classOf[WsServlet], classOf[WebService]).asJava

  override def getCheckFiles: java.util.List[String] = return List[String]().asJava

  override def getTemplateFiles: java.util.List[String] =
    List[String]("soot::jimple::toolkits::javaee::templates::ws::WSWrapper::main",
      "soot::jimple::toolkits::javaee::templates::ws::JaxWsServiceWrapper::main").asJava

  // ------------------------ Implementation

  def findWSInApplication(rootPackage: String): List[WebService] = {
    val jaxRpcService = Scene.v.sootClass("javax.xml.rpc.Service")
    val fastHierarchy = Scene.v.getOrMakeFastHierarchy //make sure it is created before the parallel computations steps in

    //We use getClasses because of the Flowdroid integration
    val wsImplementationClasses = Scene.v().classes.filter(_.isConcrete).
      filter(hasJavaAnnotation(_, WEBSERVICE_ANNOTATION))

    val explicitImplementations = wsImplementationClasses.flatMap((extractWsInformation(_, fastHierarchy, rootPackage))).toList

    val detectedInterfaces = explicitImplementations.map(_.interfaceName).toSet

    val wsInterfaceClasses = Scene.v().applicationClasses.filter(_.isInterface).filterNot(_.isPhantom).
      filter(hasJavaAnnotation(_, WEBSERVICE_ANNOTATION)).filterNot(sc => detectedInterfaces.contains(sc.name))

    val implicitImplementations = wsInterfaceClasses.flatMap(extractWsInformationInterfaces(_, fastHierarchy, rootPackage))

    val jaxRpcServices =
      for (interface: SootClass <- fastHierarchy.allSubinterfaces(jaxRpcService) - jaxRpcService;
           impl: SootClass <- fastHierarchy.getAllImplementersOfInterface(interface).asScala
      ) yield new WebService(interface.getName, impl.getName, interface.getName + "Wrapper")

    explicitImplementations ++ implicitImplementations ++ jaxRpcServices
  }

  def extractWsInformationInterfaces(sc: SootClass, fastHierarchy: FastHierarchy, rootPackage: String): Traversable[WebService] = {
    val implementers = fastHierarchy.interfaceImplementers(sc)
    implementers.flatMap(extractWsInformation(_, fastHierarchy, rootPackage))
  }


  def extractWsInformation(sc: SootClass, fastHierarchy: FastHierarchy,
                           rootPackage: String): Option[WebService] = {


    //Ignored annotations:
    // - @SOAPBinding: This does not change the high-level behavior
    // - @WebMethod:   We expect that the type compatibility would fix that on the client side
    // - @Addressing: JAX-WS 2.2 Rev a sec 7.14.1 Looks irrelevant
    // - @WebEndpoint: JAX-WS 2.2 Rev a sec 7.6 on generated stubs only, so that is not relevant in this part.
    // - @RequestWrapper: JAX-WS 2.2 Rev a sec 7.3 ????
    // - @ResponseWrapper: JAX-WS 2.2 Rev a sec 7.4 ????

    //TODO annotations
    // - @XmlMimeType : could cause some kinds of vulnerabilities
    // - @WebParam : could it change the binding of parameters, or is it transparent?
    // - @HandlerChain : need to parse the xml file to build the chain
    // - @ServiceMode: JAX-WS 2.2 Rev a sec 7.1 Setting to MESSAGE breaks the linking?
    // - @WebFault: JAX-WS 2.2 Rev a sec 7.2 Exceptions could mean data flow, but is it transparent on the client side?

    //Reminder of attributes.
    //JAX-WS 2.2 Rev a sec 7.11.1
    //public @interface WebService {
    //  String name() default "";
    //  String targetNamespace() default "";
    //  String serviceName() default "";
    //  String wsdlLocation() default "";
    //  String endpointInterface() default "";
    //  String portName() default "";
    //};

    determineSEI(sc, fastHierarchy) match {
      case None =>
        logger.error("Cannot process service {} because the specified interface is not implemented or not annotated", sc.getName)
        None
      case Some(selfContainedClass) if selfContainedClass == sc => Some(extractWsInformationSelfContained(sc, rootPackage))
      case Some(serviceInterface) => Some(extractWsInformationKnownIFace(sc, fastHierarchy, rootPackage, serviceInterface))
    }

  }



}