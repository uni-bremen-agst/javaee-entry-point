/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.detectors

import java.io.{IOException, File}
import soot.jimple.toolkits.javaee.model.servlet.Web
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import soot.{SourceLocator, SootClass, Scene}
import soot.util.SootAnnotationUtils
import soot.jimple.toolkits.javaee.model.ws.{WsServlet, WebService}
import JaxWsServiceDetector._
import soot.jimple.toolkits.javaee.model.servlet.http.FileLoader
import soot.jimple.toolkits.javaee.model.servlet.http.io.WebXMLReader

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