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

    Copyright 2013-2015 Ecole Polytechnique de Montreal & Tata Consultancy Services
 */
package soot.jimple.toolkits.javaee

import javax.xml.namespace.QName

import ca.polymtl.gigl.casi.Logging
import soot.jimple.toolkits.javaee.model.ws.{WebMethod, WebService}
import soot.util.ScalaWrappers._
import soot.{SootClass, SootMethod}

import scala.collection.JavaConverters._
import scala.collection._



/**
 * A registry of the web services detected in the application under analysis
 * @author Marc-André Laverdière-Papineau
 */
object WebServiceRegistry extends Logging {

  private var _services: Traversable[WebService] = Array[WebService]()
  def services: Traversable[WebService] = _services
  def services_=(update: Traversable[WebService]): Unit = {
    _services = update
  }

  /**
   * Lookup a service by qualified name.
   * @param nameSpace the name space for the qualified name
   * @param localName the local name of the qualified name
   * @return a possibly empty set of web services found for that qualified name
   */
  def findService(nameSpace: String, localName: String): Set[WebService] =
    findService(new QName(nameSpace, localName))

  /**
   * Lookup a service by qualified name.
   * @param nameSpace the name space for the qualified name
   * @param localName the local name of the qualified name
   * @return `Some` set of web service metadata if it exists, `None` otherwise
   */
  def findServiceOpt(nameSpace: String, localName: String): Option[Set[WebService]] =
    findServiceOpt(new QName(nameSpace, localName))

  /**
   * Lookup a service by qualified name.
   * @param qName the qualified name
   * @return a possibly empty set of web services found for that qualified name
   */
  def findService(qName: QName): Set[WebService] = {
    findServiceOpt(qName).getOrElse(Set())
  }

  /**
   * Lookup a service by qualified name.
   * @param qName the qualified name
   * @return `Some` set of web service metadata if it exists, `None` otherwise
   */
  def findServiceOpt(qName: QName): Option[Set[WebService]] =
    _services.filter(ws => ws.targetNamespace == qName.getNamespaceURI && ws.name == qName.getLocalPart) match {
      case coll if coll.isEmpty => None
      case coll => Some(coll.toSet)
    }

  /**
   * Find a service by its interface
   * @param iface the interface class
   * @return a possibly empty set of `WebService` that represent the services implementing that interface (in the WS sense)
   */
  def findServiceByInterface(iface: SootClass): Set[WebService] =
    findServiceByInterfaceOpt(iface).getOrElse(Set())

  /**
   * Find a service by its interface
   * @param iface the interface class
   * @return `Some` set of web service metadata if it exists, `None` otherwise
   */
  def findServiceByInterfaceOpt(iface: SootClass): Option[Set[WebService]] =
    _services.filter(_.interfaceName == iface.name) match {
      case coll if coll.isEmpty => None
      case coll => Some(coll.toSet)
    }


  /**
   * Find a service by its implementation class
   * @param impl the implementation class
   * @return an Option for the service
   */
  def findServiceByImplementation(impl: SootClass): Option[WebService] =
    _services.find(_.implementationName == impl.name)

  /**
   * Checks if this Soot method is actually a service method
   * @param sm the Soot method
   * @return `true` if this method is a service method, `false` otherwise
   */
  def isServiceImplementationMethod(sm : SootMethod) : Boolean = {
    logger.debug("All services: {}", _services.mkString(","))
    findServiceByImplementation(sm.declaringClass) match {
      case None => logger.debug("Method {} is not an service implementation - wrong class", sm); false
      case Some(srv) =>
        val thisServiceMethods: Traversable[WebMethod] = srv.methods.asScala
        logger.trace("Possible methods: {}", thisServiceMethods.mkString(", "))
        val found = thisServiceMethods.exists(_.isSameAs(sm))
        logger.debug("Method {} is a WS implementation? {}", sm, found)
        found
    }
  }

  private implicit class RichWebMethod(wm: WebMethod) extends AnyRef {
    def isSameAs(sm : SootMethod) : Boolean = {
      val nameIsSame = wm.targetMethodName == sm.name
      val retIsSame = wm.retType.toString == sm.returnType.toString
      val argIsSame = wm.argTypes.asScala.map(_.toString) == sm.parameterTypes.map(_.toString) //toString fixes this check, that returns false in maven

      logger.trace("({}) is same as {}? Name: {} Ret: {} Args: {}", wm, sm, nameIsSame, retIsSame, argIsSame)

      nameIsSame && retIsSame && argIsSame

    }

  }

}
