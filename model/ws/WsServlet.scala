/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.model.ws

import java.util.LinkedList
import java.util.List
import javax.xml.bind.annotation._
import soot.jimple.toolkits.javaee.model.servlet.Servlet
import beans.BeanProperty
import annotation.meta.beanGetter
import scala.collection.JavaConversions._
import soot.jimple.toolkits.javaee.model.ws.WebService
import java.util

/**
 * Representation of a servlet for web services.
 *
 * @author Marc-André Laverdière-Papineau
 */
@XmlRootElement(name = "WsServlet")
case class WsServlet( services : java.util.List[WebService]) extends Servlet {

  // JAXB-specific
  def this() = this(new java.util.ArrayList[WebService](0))

  //We shouldn't need this, but the annotation magic is harder to handle
  //than going for the simple def
  @XmlElementWrapper
  @XmlElement(name="service")
  def getServices() : java.util.List[WebService] = services
}
