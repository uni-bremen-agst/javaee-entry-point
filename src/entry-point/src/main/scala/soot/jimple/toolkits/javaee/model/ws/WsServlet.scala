/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.model.ws

import javax.xml.bind.annotation._

import soot.jimple.toolkits.javaee.model.servlet.http.GenericServlet

import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty
import scala.collection.JavaConversions._

/**
 * Representation of a servlet for web services.
 *
 * @author Marc-André Laverdière-Papineau
 */
@XmlRootElement(name = "WsServlet")
case class WsServlet(
                      @(XmlElementWrapper@beanGetter) @(XmlElement@beanGetter)(name = "service") @BeanProperty services: java.util.List[WebService])
  extends GenericServlet {

  // JAXB-specific
  def this() = this(new java.util.ArrayList[WebService](0))

  @BeanProperty
  lazy val operations: java.util.List[WebMethod] = services.flatMap(_.methods)
}
