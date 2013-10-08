/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee.model.servlet.jboss

import soot.jimple.toolkits.javaee.model.servlet.http.GenericServlet
import javax.xml.bind.annotation.XmlAttribute
import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty
import soot.{SootMethod, SootClass}
import scala.collection.JavaConversions._

case class SJBossWSTestServlet (
@BeanProperty jBossWsClients : java.util.List[SootClass],
@BeanProperty testMethods : java.util.List[SootMethod]
) extends GenericServlet {

  //Throw in some useless no-op to force something in clinit
  val xyz = "asdaf" + 12


  //Required by Jax-WB
  def this() = this(List(),List())

}
