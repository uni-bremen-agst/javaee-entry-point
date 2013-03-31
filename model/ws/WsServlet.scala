/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.model.ws

import java.util.LinkedList
import scala.collection.immutable.List
import javax.xml.bind.annotation._
import soot.jimple.toolkits.javaee.model.servlet.Servlet
import beans.BeanProperty
import annotation.meta.beanGetter
import scala.collection.JavaConversions._
import soot.jimple.toolkits.javaee.model.ws.WebService

/**
 * Representation of a servlet for web services.
 *
 * @author Marc-André Laverdière-Papineau
 */
@XmlRootElement(name = "WsServlet")
@XmlAccessorType(XmlAccessType.FIELD)
case class WsServlet(@XmlElementWrapper(name="services") @XmlElement
                     /*@(XmlElementWrapper @beanGetter)(name="services")
                     @(XmlElement @beanGetter)(name="service")*/ services : List[WebService])
  extends Servlet {

  //For future reference: http://krasserm.blogspot.ca/2012/02/using-jaxb-for-xml-and-json-apis-in.html

  // JAXB-specific
  private def this() = this(List[WebService]())
}
