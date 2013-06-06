/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */

package soot.jimple.toolkits.javaee.detectors

import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import soot.jimple.toolkits.javaee.model.servlet.Web
import soot.{Unit => SootUnit, _}
import javax.xml.bind.annotation.XmlAttribute
import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty
import soot.jimple.toolkits.javaee.model.servlet.jboss.{JBossWSTestServlet, SJBossWSTestServlet}

object JBossWSTestDetector {
  final val GENERATED_CLASS_NAME = "JBossWSTestServlet"
}

import JBossWSTestDetector._

class JBossWSTestDetector extends AbstractServletDetector with Logging{

  override def detectFromSource(web: Web) {

    val fastHierarchy = Scene.v.getOrMakeFastHierarchy //make sure it is created before the parallel computations steps in
    val jBossWsSuperClass = Scene.v.forceResolve("org.jboss.wsf.test.JBossWSTest", SootClass.HIERARCHY) //otherwise we can't load the type
    val jBossWsSuperType = jBossWsSuperClass.getType
    val jBossWsClients = Scene.v().getApplicationClasses.par.filter(_.isConcrete).
      filter(sc=>fastHierarchy.canStoreType(sc.getType,jBossWsSuperType)).seq.toList
    jBossWsClients.foreach(logger.info("Found JBoss WS Test Client: {}", _))

    val testMethods = jBossWsClients.par.flatMap(_.getMethods).filter(_.getName.startsWith("test")).seq.toList
    testMethods.foreach(logger.debug("Test method found: {}", _))

    val fakeServlet = new JBossWSTestServlet(jBossWsClients, testMethods)

    val fullName = web.getGeneratorInfos.getRootPackage + "." + GENERATED_CLASS_NAME
    fakeServlet.setClazz(fullName)
    fakeServlet.setName(GENERATED_CLASS_NAME)
    web.getServlets.add(fakeServlet)
    web.bindServlet(fakeServlet, "/jbosstester")
  }

  override def detectFromConfig(web: Web) {
    detectFromSource(web) //No config available for that case
  }

  // ----------------------- Template part of the interface
  override def getModelExtensions: java.util.List[Class[_]] = List[Class[_]]()

  override def getCheckFiles: java.util.List[String] = return List[String]()

  override def getTemplateFiles: java.util.List[String] =
    List[String]("soot::jimple::toolkits::javaee::templates::jboss::JBossTestWSWrapper::main")
}
