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
package soot.jimple.toolkits.javaee.model.ws

import javax.xml.bind.annotation.{XmlElement, XmlElementWrapper, XmlRootElement}

import soot.jimple.toolkits.javaee.model.servlet.http.GenericServlet

import scala.annotation.meta.beanGetter
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

/**
 * Representation of a servlet for web services.
 *
 * @author Marc-André Laverdière-Papineau
 */
@XmlRootElement(name = "WsServlet")
case class WsServlet(
                      @(XmlElementWrapper@beanGetter)
                      @(XmlElement@beanGetter)(name = "service")
                      @BeanProperty services: java.util.List[WebService])
  extends GenericServlet {

  // JAXB-specific
  def this() = this(new java.util.ArrayList[WebService](0))

  @BeanProperty
  lazy val operations: java.util.List[WebMethod] = services.asScala.flatMap(_.methods.asScala).asJava
}
