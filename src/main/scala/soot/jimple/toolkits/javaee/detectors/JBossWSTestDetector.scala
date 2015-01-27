/*
    This file is part of Soot entry point creator.

    Soot entry point creator is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Soot entry point creator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Soot entry point creator.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2013 Ecole Polytechnique de Montreal & Tata Consultancy Services
 */

package soot.jimple.toolkits.javaee.detectors

import soot.jimple.toolkits.javaee.model.servlet.Web
import soot.jimple.toolkits.javaee.model.servlet.jboss.JBossWSTestServlet
import soot.util.ScalaWrappers._
import soot.{Scene, SootClass, SootMethod}

import scala.collection.JavaConverters._

object JBossWSTestDetector {
  final val GENERATED_CLASS_NAME = "JBossWSTestServlet"
}

import soot.jimple.toolkits.javaee.detectors.JBossWSTestDetector._

/**
 * Detector for the JBoss-WS test cases, which are relying on reflection-fu
 *
 * @author Marc-André Laverdière-Papineau
 */
class JBossWSTestDetector extends AbstractServletDetector with Logging {

  override def detectFromSource(web: Web) {

    val jBossWsSuperClass = Scene.v.forceResolve("org.jboss.wsf.test.JBossWSTest", SootClass.HIERARCHY) //otherwise we can't load the type
    val jBossWsSuperType = jBossWsSuperClass.getType

    val nonDandling : Seq[SootClass] = Scene.v.applicationClasses.par.filter(_.resolvingLevel() > SootClass.DANGLING).seq.toSeq
    logger.trace("Non-dandling classes ({}): {}", nonDandling.size : Integer, nonDandling.map(_.name))

    val fastHierarchy = Scene.v.fastHierarchy //make sure it is created before the parallel computations steps in
    //For some odd reason, a bunch of Java library classes are considered subtypes of the JBoss type, so we filter them out.
    val jBossWsClients : Seq[SootClass]= nonDandling.par.filter(_.isConcrete).filterNot(_.isJavaLibraryClass).
      filter(sc=>fastHierarchy.canStoreType(sc.getType,jBossWsSuperType)).seq
    jBossWsClients.foreach(logger.info("Found JBoss WS Test Client: {}", _))

    val testMethods : Seq[SootMethod]= jBossWsClients.par.flatMap(_.methods).filter(_.name.startsWith("test")).seq
    testMethods.foreach(logger.debug("Test method found: {}", _))

    if (!testMethods.isEmpty){
      val fakeServlet = new JBossWSTestServlet(jBossWsClients.asJava, testMethods.asJava)

      val fullName = web.getGeneratorInfos.getRootPackage + "." + GENERATED_CLASS_NAME
      fakeServlet.setClazz(fullName)
      fakeServlet.setName(GENERATED_CLASS_NAME)
      web.getServlets.add(fakeServlet)
      web.bindServlet(fakeServlet, "/jbosstester")
    }
  }

  override def detectFromConfig(web: Web) {
    logger.warn("Detecting Web services from configuration files is not supported yet - switching to detection from source")
    detectFromSource(web) //No config available for that case
  }

  // ----------------------- Template part of the interface
  override def getModelExtensions: java.util.List[Class[_]] = List[Class[_]]().asJava

  override def getCheckFiles: java.util.List[String] = return List[String]().asJava

  override def getTemplateFiles: java.util.List[String] =
    List[String]("soot::jimple::toolkits::javaee::templates::jboss::JBossTestWSWrapper::main",
      "soot::jimple::toolkits::javaee::templates::ws::JaxWsServiceWrapper::main").asJava
}
