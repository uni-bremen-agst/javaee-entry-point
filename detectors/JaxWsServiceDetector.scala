/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.detectors

import java.io.{IOException, File}
import soot.jimple.toolkits.javaee.model.servlet.Web
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConverters._
import soot._
import soot.jimple._
import soot.util.SootAnnotationUtils._
import soot.jimple.toolkits.javaee.model.ws._

import soot.jimple.toolkits.javaee.model.servlet.http.FileLoader
import soot.jimple.toolkits.javaee.model.servlet.http.io.WebXMLReader
import soot.tagkit.SourceFileTag
import soot.jimple.toolkits.javaee.WebServiceRegistry

import soot.util.ScalaWrappers._
import javax.xml.bind.{JAXB, JAXBContext}
import java.net.{MalformedURLException, URL}
import soot.jimple.toolkits.javaee.model.ws.WsServlet
import soot.jimple.toolkits.javaee.model.ws.WebService
import org.jcp.xmlns.javaee.HandlerChainsType


/**
 * Utilities to determine the values of JAX-WS services' attributes
 * @author Marc-André Laverdière-Papineau
 * */
object JaxWSAttributeUtils extends Logging {

  private lazy val handlerChainJaxbContext = JAXBContext.newInstance("org.jcp.xmlns.javaee")

  /**
   * Reverses a package name, so that e.g. scala.collection.mutable becomes mutable.collection.scala
   * @param pkg the package name
   * @return the reversed package name
   */
  def reversePackageName(pkg: String) : String ={
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
  def implementsAllMethods(implementor : SootClass, reference:SootClass) : Boolean = {
    val referenceMethodSignatures = reference.methods.map(_.getSubSignature)
    val implementorMethodSignatures = implementor.methods.map(_.getSubSignature).toSet
    referenceMethodSignatures.forall(implementorMethodSignatures.contains(_))
  }

  /**
   * Read an annotation element on the current class on or service interface
   * @param annName name of the annotation
   * @param default a function that gives the default value, if it is not found in the annotations
   * @param localAnn the local annotation's elements
   * @param interfaceAnn the interface annotation's elements
   * @return an option for the value of the annotation element
   */
  def readCascadedAnnotation(annName: String, default : => String, localAnn : Map[String,Any], interfaceAnn : Map[String,Any]) : String = {
    if (localAnn != interfaceAnn)
      localAnn.getOrElse(annName,interfaceAnn.getOrElse(annName, default)).asInstanceOf[String]
    else
      localAnn.getOrElse(annName,default).asInstanceOf[String]
  }

  /**
   * Determines the local name of the service. This is not the same as the service name
   * JSR 181 section 4.1.1 Default: short name of the class or interface
   * @param sc the implementation class
   * @param annotationElems the annotations on the class
   * @param serviceInterfaceAnnotationElems the annotations on the service interface
   * @return a non-empty string with the name of the service
   */
  def localName(sc: SootClass, annotationElems: Map[String, Any], serviceInterfaceAnnotationElems: Map[String, Any]): String = {
    readCascadedAnnotation("name", sc.getShortName, annotationElems, serviceInterfaceAnnotationElems)
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
   *  1. The package name is tokenized using the “.” character as a delimiter.
   *  2. The order of the tokens is reversed.
   *  3. The value of the targetNamespace attribute is obtained by concatenating “http://”to the list of
   *    tokens separated by “ . ”and “/”.
   *
   * @param sc the implementation class
   * @param annotationElems the annotations on the implementation class
   * @param serviceInterfaceAnnotationElems the annotations on the SEI
   * @return a non-empty string
   */
  def targetNamespace(sc: SootClass, annotationElems: Map[String, Any], serviceInterfaceAnnotationElems: Map[String, Any]): String = {
    readCascadedAnnotation("targetNamespace", "http://" + reversePackageName(sc.getPackageName) + "/", annotationElems, serviceInterfaceAnnotationElems)
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
    annotationElems.get("portName").getOrElse(name + "Port").asInstanceOf[String]
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
   *  - if serviceName is set, use that
   *  - if name is set, use that + "Service"
   *  - if name is not set, use the short class name + "Service"
   * Since name is defaulted to the short name, we can ignore the last rule
   *
   * @param name the name of the serice
   * @param annotationElems the annotations on the implementation class
   * @param serviceInterfaceAnnotationElems the annotations on the SEI
   * @return a non-empty string
   */
  def serviceName(name: String, annotationElems: Map[String, Any], serviceInterfaceAnnotationElems: Map[String, Any]): String = {
    readCascadedAnnotation("serviceName", name + "Service", annotationElems, serviceInterfaceAnnotationElems)
  }

  /**
   * Helper function that tries to read the file on that class - if it is an URL
   * @param sc the soot class we are dealing with - used in logging
   * @param file the handler configuration file location
   * @return an Option over the handler chain XML type
   * */
  private def handlerChainAsURL (sc: SootClass, file : String) : Option[HandlerChainsType] = {
    try{
      val url = new URL(file)
      logger.info("For class {}, handler file is located at: {}", sc, url)
      val jc = JAXBContext.newInstance("org.jcp.xmlns.javaee")
      val unmarshaller = jc.createUnmarshaller()
      Some(unmarshaller.unmarshal(url).asInstanceOf[HandlerChainsType])
    } catch {
      case _ : MalformedURLException => None
    }
  }

  /**
   * Helper function that tries to read the file on that class - and guesses where it could be
   * @param sc the soot class we are dealing with - used in logging
   * @param file the handler configuration file location
   * @return an Option over the handler chain XML type
   * */
  private def handlerChainAsFile (sc: SootClass, file : String) : Option[HandlerChainsType] = {
    val location = findAnnotation(sc,classOf[SourceFileTag]).map(_.getAbsolutePath)
    val locationFile = location.map(new File(_))

    locationFile.flatMap{f : File =>
      val handlerFile = new File(f.getParent,file)
      if (handlerFile.exists()){
        logger.info("For class {}, handler file is located at: {}", sc, handlerFile)
        val unmarshalled = JAXB.unmarshal(handlerFile, classOf[HandlerChainsType])
       Some(unmarshalled)

      }
      else{
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
   * */
  private def handlerChain(sc: SootClass, file : String) : Option[HandlerChainsType] = {
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
   * */
  def handlerChainOption(sc: SootClass) : Option[HandlerChainsType] = {

    for (handlerChainAnn <- findJavaAnnotation(sc, HANDLER_CHAIN_ANNOTATION);
         elements = annotationElements(handlerChainAnn);
         file <- elements.get("file").asInstanceOf[Option[String]];
         chain <- handlerChain(sc, file)
    ) yield chain

  }

}

import JaxWSAttributeUtils._
import JaxWsServiceDetector._

/**
 * Detector for Jax-WS 2.0 Web Services
 * @author Marc-André Laverdière-Papineau
 */
class JaxWsServiceDetector extends AbstractServletDetector with Logging{

  lazy val stringType = Scene.v.getRefType("java.lang.String")

  override def detectFromSource(web: Web) {
    val rootPackage: String = web.getGeneratorInfos.getRootPackage
    val foundWs = findWSInApplication(rootPackage)
    if (! foundWs.isEmpty){
      val newServlet = new WsServlet(foundWs.asJava)
      val fullName = rootPackage + "." + GENERATED_CLASS_NAME
      newServlet.setClazz(fullName)
      newServlet.setName(GENERATED_CLASS_NAME)
      web.getServlets.add(newServlet)
      web.bindServlet(newServlet, "/wscaller")
    }
    WebServiceRegistry.services = foundWs
  }

  override def detectFromConfig(web: Web) {
    //TODO avoid redundancy with HTTPServletDetector
    //TODO handle other config files


    logger.info("Detecting web services from web.xml.")
    val webInfClassFolders = SourceLocator.v.classPath.asScala.filter(_.endsWith("WEB-INF/classes"))
    val webXmlFiles = webInfClassFolders.map(new File(_).getParentFile).map(new File(_, "web.xml")).filter(_.exists())
    val webRootFiles = webXmlFiles.map(_.getParentFile)

    try{
      val fileLoaders = webRootFiles.map(new FileLoader(_))
      fileLoaders.foreach(new WebXMLReader().readWebXML(_, web))
    } catch {
      case e: IOException => logger.info("Cannot read web.xml:", e)
    }

  }

  // ----------------------- Template part of the interface
  override def getModelExtensions: java.util.List[Class[_]] = List[Class[_]](classOf[WsServlet], classOf[WebService]).asJava

  override def getCheckFiles: java.util.List[String] = return List[String]().asJava

  override def getTemplateFiles: java.util.List[String] =
    List[String]("soot::jimple::toolkits::javaee::templates::ws::WSWrapper::main",
      "soot::jimple::toolkits::javaee::templates::ws::JaxWsServiceWrapper::main").asJava

  // ------------------------ Implementation

  def findWSInApplication(rootPackage : String) :  List[WebService] = {
    val fastHierarchy = Scene.v.getOrMakeFastHierarchy //make sure it is created before the parallel computations steps in

    //We use getClasses because of the Flowdroid integration
    val wsImplementationClasses = Scene.v().applicationClasses.par.filter(_.isConcrete).
      filter(hasJavaAnnotation(_, WEBSERVICE_ANNOTATION))
    wsImplementationClasses.flatMap((extractWsInformation(_, fastHierarchy, rootPackage))).seq.toList
  }

  def extractWsInformation(sc : SootClass, fastHierarchy: FastHierarchy, rootPackage : String) : Option[WebService] = {
    val init : Option[String] = sc.methods.par.find(hasJavaAnnotation(_, WEBSERVICE_POSTINIT_ANNOTATION)).map(_.getName)
    val destroy : Option[String] = sc.methods.par.find(hasJavaAnnotation(_, WEBSERVICE_PREDESTROY_ANNOTATION)).map(_.getName)

    val annotationElems : Map[String,Any] = elementsForJavaAnnotation(sc,WEBSERVICE_ANNOTATION)

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

    val endpointInterface = annotationElems.get("endpointInterface")

    val serviceInterface : SootClass =
      if (endpointInterface.isEmpty)
        sc //Implementation class' name is used
      else {
        //JSR-181, section 3.1, page 13: the implementing class only needs to implement methods in the interface
        //If it is an implementing class, then it meets that criteria for sure
        //It could also not implement the interface, but have the same signatures.
        val iface = Scene.v.getSootClass(endpointInterface.get.asInstanceOf[String])
        if (hasJavaAnnotation(iface, WEBSERVICE_ANNOTATION) &&
          (fastHierarchy.canStoreType(sc.getType, iface.getType) || implementsAllMethods(sc, iface))){
          //All good. The specified interface is implemented and it has the annotation
          iface
        } else { //Non-conforming
          logger.error("Cannot process service {} because the specified interface is not implemented or not annotated", sc.getName)
          return None
        }
      }

    val serviceInterfaceAnnotationElems : Map[String,Any] = elementsForJavaAnnotation(serviceInterface, WEBSERVICE_ANNOTATION)

    val name : String = localName(sc, annotationElems, serviceInterfaceAnnotationElems)
    val srvcName : String = serviceName(name, annotationElems, serviceInterfaceAnnotationElems)
    val prtName : String = portName(name, annotationElems)
    val tgtNamespace : String = targetNamespace(sc, annotationElems, serviceInterfaceAnnotationElems)
    val wsdlLoc : String = wsdlLocation(srvcName, annotationElems)

    //Detect method names
    // JSR-181, p. 35, section 3.5 operation name is @WebMethod.operationName. Default is in Jax-WS 2.0 section 3.5
    // JAX-WS 2.2 Rev a sec 3.5 p.35 Default is the name of the method
    //TODO double-check this matching rule
    val potentialMethods = sc.methods.filterNot(_.isConstructor).filter(_.isConcrete)

    //JBOSS-WS Test case in org.jboss.test.ws.jaxws.samples.webservice has no @WebMethod annotation on either interface nor implementation class
    val serviceMethods : Traversable[WebMethod] = for (
      sm <-  potentialMethods.filterNot( m=> m.isConstructor || m.isClinit || m.isStatic);
      subsig = sm.getSubSignature;
      seiMethod <- serviceInterface.method(subsig);
      //if (hasJavaAnnotation(sm,WEBMETHOD_ANNOTATION) || hasJavaAnnotation(seiMethod,WEBMETHOD_ANNOTATION));
      implAnn = elementsForJavaAnnotation(sm, WEBMETHOD_ANNOTATION);
      seiAnn =  elementsForJavaAnnotation(serviceInterface, WEBMETHOD_ANNOTATION);
      opName = readCascadedAnnotation("operationName", sm.getName, implAnn, seiAnn)
    ) yield {
      val paramDefaults = sm.parameterTypes.collect{
        case `stringType` => StringConstant.v("abc")
        case a: IntegerType => IntConstant.v(1)
        case a: LongType => LongConstant.v(1)
        case a: FloatType => FloatConstant.v(1.0f)
        case a: DoubleType => DoubleConstant.v(1.0)
        case _ => NullConstant.v().asInstanceOf[Value] //.asInstanceOf[Value] forces the type system to be nice :)
      }

      val targetOpName = if (opName(0).isUpper) opName(0).toLower + opName.drop(1) else opName

      new WebMethod(null, targetOpName, sm.name,sm.parameterTypes.toList.asJava,sm.returnType,paramDefaults.toList.asJava)
    }

    serviceMethods.foreach(wm => logger.trace("Web method {} hash: {}", wm, wm.hashCode() : Integer))

    // ------------- Detect handler chain on the server and parse it --------
    val handlerChainOpt = handlerChainOption(sc)
    if (handlerChainOpt.isDefined){
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
      serviceInterface.name, sc.name, wrapperName, init.getOrElse(""), destroy.getOrElse(""), name,tgtNamespace,
      srvcName,wsdlLoc,prtName, serviceMethods
    )

    val ws = new WebService(
      serviceInterface.name, sc.name, wrapperName, init.getOrElse(""), destroy.getOrElse(""), name, tgtNamespace,
      srvcName, wsdlLoc, prtName, chain.asJava, serviceMethods.toList.asJava
    )
    
    ws.methods.asScala.foreach(_.service = ws)

    Some(ws)
  }
}

object JaxWsServiceDetector {
  final val GENERATED_CLASS_NAME: String = "WSCaller"

}