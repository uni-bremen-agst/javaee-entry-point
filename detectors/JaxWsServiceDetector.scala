/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.detectors

import java.io.{IOException, File}
import soot.jimple.toolkits.javaee.model.servlet.Web
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.Map
import scala.collection.JavaConversions._
import soot.{FastHierarchy, SourceLocator, SootClass, Scene}
import soot.util.SootAnnotationUtils._
import soot.jimple.toolkits.javaee.model.ws.{WsServlet, WebService}
import JaxWsServiceDetector._
import soot.jimple.toolkits.javaee.model.servlet.http.FileLoader
import soot.jimple.toolkits.javaee.model.servlet.http.io.WebXMLReader
import soot.tagkit.AnnotationTag
import soot.jimple.toolkits.javaee.WebServiceRegistry

/**
 * Detector for Jax-WS 2.0 Web Services
 * @author Marc-André Laverdière-Papineau
 */
class JaxWsServiceDetector extends AbstractServletDetector with Logging{

  override def detectFromSource(web: Web) {
    val foundWs = findWSInApplication
    if (! foundWs.isEmpty){
      val newServlet = new WsServlet(foundWs)
      val fullName = web.getGeneratorInfos.getRootPackage + "." + GENERATED_CLASS_NAME
      newServlet.setClazz(fullName)
      newServlet.setName(GENERATED_CLASS_NAME)
      web.getServlets.add(newServlet)
      web.bindServlet(newServlet, "/wscaller")
    }
    WebServiceRegistry.services(foundWs)
  }

  override def detectFromConfig(web: Web) {
    //TODO avoid redundancy with HTTPServletDetector
    //TODO handle other config files


    logger.info("Detecting web services from web.xml.")
    val webInfClassFolders = SourceLocator.v.classPath.filter(_.endsWith("WEB-INF/classes"))
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
  override def getModelExtensions: java.util.List[Class[_]] = List[Class[_]](classOf[WsServlet], classOf[WebService])

  override def getCheckFiles: java.util.List[String] = return List[String]()

  override def getTemplateFiles: java.util.List[String] =
    List[String]("soot::jimple::toolkits::javaee::templates::ws::WSWrapper::main")

  // ------------------------ Implementation

  def findWSInApplication() :  List[WebService] = {
    val fastHierarchy = Scene.v.getOrMakeFastHierarchy //make sure it is created before the parallel computations steps in

    val wsImplementationClasses = Scene.v().getApplicationClasses.par.filter(_.isConcrete).
      filter(hasSootAnnotation(_, WEBSERVICE_ANNOTATION))
    wsImplementationClasses.flatMap((extractWsInformation(_, fastHierarchy))).seq.toList
  }

  def extractWsInformation(sc : SootClass, fastHierarchy: FastHierarchy) : Option[WebService] = {
    val init : Option[String] = sc.getMethods.par.find(hasSootAnnotation(_, WEBSERVICE_POSTINIT_ANNOTATION)).map(_.getName)
    val destroy : Option[String] = sc.getMethods.par.find(hasSootAnnotation(_, WEBSERVICE_PREDESTROY_ANNOTATION)).map(_.getName)

    val annotationElems : Map[String,Any] = elementsForAnnotation(sc,WEBSERVICE_ANNOTATION)

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
        if (hasSootAnnotation(iface, WEBSERVICE_ANNOTATION) &&
          (fastHierarchy.canStoreType(sc.getType, iface.getType) || implementsAllMethods(sc, iface))){
          //All good. The specified interface is implemented and it has the annotation
          iface
        } else { //Non-conforming
          logger.error("Cannot process service {} because the specified interface is not implemented or not annotated", sc.getName)
          return None
        }
      }

    val serviceInterfaceAnnotationElems : Map[String,Any] = elementsForAnnotation(serviceInterface, WEBSERVICE_ANNOTATION)

    //JSR 181 section 4.1.1 Default: short name of the class or interface
    val name : String = annotationElems.getOrElse("name",
                          serviceInterfaceAnnotationElems.getOrElse("name",
                          sc.getShortName)).asInstanceOf[String]

    //JAX-WS 2.2 Rev a sec 3.11 p.52 Default: the name of the implementation class with the “Service”suffix appended to it.
    val serviceName : String = annotationElems.get("serviceName").getOrElse(sc.getShortName+"Service").asInstanceOf[String]

    //JAX-WS 2.2 Rev a sec 3.11 p.54 In the absence of a portName element, an implementation
    // MUST use the value of the name element of the WebService annotation, if present, suffixed with
    //“Port”. Otherwise, an implementation MUST use the simple name of the class annotated with WebService
    //suffixed with “Port”.
    val portName : String = annotationElems.get("portName").getOrElse(name+"Port").asInstanceOf[String]


    //If the namespace is not specified for the service name, check for the service interface
    //If it is not defined there, then it is the reversed package name
    val targetNamespace : String = annotationElems.getOrElse("targetNamespace",
          serviceInterfaceAnnotationElems.getOrElse("targetNamespace",
          reversePackageName(sc.getPackageName)) ).asInstanceOf[String]

    //JAX-WS 2.2 Rev a sec 5.2.5 p.71 Default is the empty string
    // 5.2.5.1 p.77 WSDL needed only if SOAP 1.1/HTTP Binding
    // 5.2.5.3 WSDL is not generated on the fly, but package with the application
    //Practically, it gets mapped to http://host:port/approot/servicename?wsdl
    //TODO: find the official spec for that
    val wsdlLocation : String = annotationElems.get("wsdlLocation").getOrElse(serviceName+"?wsdl").asInstanceOf[String]

    logger.info("Found WS. Interface: {} Implementation: {} Init: {} Destroy: {} Name: {} Namespace: {} " +
      "ServiceName: {} wsdl: {} port: {}",
      serviceInterface.getName, sc.getName, init.getOrElse(""), destroy.getOrElse(""), name,targetNamespace,
      serviceName,wsdlLocation,portName
    )

    return Option(new WebService(
      serviceInterface.getName,
      sc.getName,
      init.getOrElse(""),
      destroy.getOrElse(""),
      name,targetNamespace,serviceName,wsdlLocation,portName
    ))

  }



}


object JaxWsServiceDetector {
  final val GENERATED_CLASS_NAME: String = "WSCaller"

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
    val referenceMethodSignatures = reference.getMethods.map(_.getSubSignature)
    val implementorMethodSignatures = implementor.getMethods.map(_.getSubSignature).toSet
    referenceMethodSignatures.forall(implementorMethodSignatures.contains(_))
  }
}