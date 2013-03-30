/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.detectors

import soot.jimple.toolkits.javaee.model.servlet.Web
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import soot.{SootClass, Scene}
import soot.util.SootAnnotationUtils
import soot.jimple.toolkits.javaee.model.ws.WebService
import JaxWsServiceDetector._

/**
 * Detector for Jax-WS 2.0 Web Services
 * @author Marc-André Laverdière-Papineau
 */
class JaxWsServiceDetector extends AbstractServletDetector with Logging{

  override def detectFromSource(web: Web) {
    val foundWs = findWSInApplication
    if (! foundWs.isEmpty){
      web.addWebServices(findWSInApplication)
      HttpServletDetector.registerServlet(web, web.getGeneratorInfos.getRootPackage + "." + GENERATED_CLASS_NAME)
    }
  }

  override def detectFromConfig(web: Web) {
    //TODO
  }

  // ----------------------- Template part of the interface
  override def getModelExtensions: java.util.List[Class[_]] = List[Class[_]]()

  override def getTemplateFile: String = {
    throw new RuntimeException("Not implemented.")
  }

  override def isXpandTemplate: Boolean = true


  override def getCheckFiles: java.util.List[String] = return List[String]()

  override def getTemplateFiles: java.util.List[String] =
    List[String]("soot::jimple::toolkits::javaee::templates::ws::WSWrapper::main")

  // ------------------------ Implementation

  def findWSInApplication() :  List[WebService] = {
    Scene.v().getApplicationClasses.par.filter(_.isConcrete).
      filter(SootAnnotationUtils.hasSootAnnotation(_, SootAnnotationUtils.WEBSERVICE_ANNOTATION)).
      map((extractWsInformation(_))).seq.toList
  }

  def extractWsInformation(sc : SootClass) : WebService = {
    val init : Option[String] = sc.getMethods.par.find(SootAnnotationUtils.hasSootAnnotation(_, SootAnnotationUtils.WEBSERVICE_POSTINIT_ANNOTATION)).map(_.getName)
    val destroy : Option[String] = sc.getMethods.par.find(SootAnnotationUtils.hasSootAnnotation(_, SootAnnotationUtils.WEBSERVICE_PREDESTROY_ANNOTATION)).map(_.getName)

    val serviceInterface = sc.getInterfaces.par.find(SootAnnotationUtils.hasSootAnnotation(_, SootAnnotationUtils.WEBSERVICE_ANNOTATION))

    return new WebService(
      serviceInterface.getOrElse(sc).getName,
      sc.getName,
      init.getOrElse(""),
      destroy.getOrElse("")
    )

  }

}


object JaxWsServiceDetector {
  final val GENERATED_CLASS_NAME: String = "WSCaller"
}