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
import soot.util.ScalaWrappers._

object JBossWSTestDetector {
  final val GENERATED_CLASS_NAME = "JBossWSTestServlet"
}

import JBossWSTestDetector._

class JBossWSTestDetector extends AbstractServletDetector with Logging{

  def minimalWorkingExample(){
    val fastHierarchy = Scene.v.getOrMakeFastHierarchy //make sure it is created before the parallel computations steps in
    val sbType = Scene.v.getRefType("java.lang.StringBuffer")
    val jBossWsSuperClass = Scene.v.forceResolve("org.jboss.wsf.test.JBossWSTest", SootClass.HIERARCHY) //otherwise we can't load the type
    val jBossWsSuperType = jBossWsSuperClass.getType

    System.err.println("org.jboss.wsf.test.JBossWSTest is a supertype of StringBuffer? " +
      fastHierarchy.canStoreType(sbType, jBossWsSuperType)
    )
  }

  override def detectFromSource(web: Web) {
    minimalWorkingExample()

    val jBossWsSuperClass = Scene.v.forceResolve("org.jboss.wsf.test.JBossWSTest", SootClass.HIERARCHY) //otherwise we can't load the type
    val jBossWsSuperType = jBossWsSuperClass.getType

    //We use getClasses because of the Flowdroid integration
    val nonDandling = Scene.v.getApplicationClasses.par.filter(_.resolvingLevel() > SootClass.DANGLING).seq
    logger.info("Number of non-dandling classes: {}", nonDandling.size : Integer)
    logger.debug("Non-dandling classes: {}", nonDandling.map(_.getName))

    val fastHierarchy = Scene.v.getOrMakeFastHierarchy //make sure it is created before the parallel computations steps in
    val jBossWsClients = nonDandling.par.filter(_.isConcrete).
      filter(sc=>fastHierarchy.canStoreType(sc.getType,jBossWsSuperType)).seq.toList
    jBossWsClients.foreach(logger.info("Found JBoss WS Test Client: {}", _))

    val testMethods = jBossWsClients.par.flatMap(_.getMethods).filter(_.getName.startsWith("test")).seq.toList
    testMethods.foreach(logger.debug("Test method found: {}", _))

    if (!testMethods.isEmpty){
      val fakeServlet = new JBossWSTestServlet(jBossWsClients, testMethods)

      val fullName = web.getGeneratorInfos.getRootPackage + "." + GENERATED_CLASS_NAME
      fakeServlet.setClazz(fullName)
      fakeServlet.setName(GENERATED_CLASS_NAME)
      web.getServlets.add(fakeServlet)
      web.bindServlet(fakeServlet, "/jbosstester")
    }
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
