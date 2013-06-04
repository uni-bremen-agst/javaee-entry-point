package soot.jimple.toolkits.javaee.detectors

import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import soot.jimple.toolkits.javaee.model.servlet.Web
import soot.{Unit => SootUnit, _}
import javax.xml.bind.annotation.XmlAttribute
import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty

/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
class JBossWSTestDetector extends AbstractServletDetector with Logging{

  @(XmlAttribute @beanGetter) @BeanProperty
  var jBossWsClients : java.util.List[SootClass] = List()

  @(XmlAttribute @beanGetter) @BeanProperty
  var testMethods : java.util.List[SootMethod] = List()

  override def detectFromSource(web: Web) {
    val fastHierarchy = Scene.v.getOrMakeFastHierarchy //make sure it is created before the parallel computations steps in
    val jBossWsSuperType = Scene.v.getRefType("org.jboss.wsf.test.JBossWSTest")
    jBossWsClients = Scene.v().getApplicationClasses.par.filter(_.isConcrete).
      filter(sc=>fastHierarchy.canStoreType(sc.getType,jBossWsSuperType)).seq.toList

    testMethods = jBossWsClients.par.flatMap(_.getMethods).filter(_.getName.startsWith("test")).seq.toList
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
