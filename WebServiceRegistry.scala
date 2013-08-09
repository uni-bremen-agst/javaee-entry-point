/**
 * (c) 2013 Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee

import soot.jimple.toolkits.javaee.model.ws.WebService
import javax.xml.namespace.QName
import scala.collection._
import soot.SootClass

import soot.util.ScalaWrappers._

/**
 * A registry of the web services detected in the application under analysis
 * @author Marc-André Laverdière-Papineau
 */
object WebServiceRegistry {

  private var _services : Traversable[WebService] = Array[WebService]()
  private lazy val _qnameLookup : Map[QName, Set[WebService]] = {
    val multimap = new mutable.HashMap[QName, mutable.Set[WebService]] with mutable.MultiMap[QName,WebService]
    _services.foreach(ws => multimap.addBinding(new QName(ws.targetNamespace, ws.serviceName), ws))
    multimap
  }

  private lazy val _serviceClassLookupByInterface : Map[String, Traversable[WebService]] = _services.groupBy(_.interfaceName)

  def services : Traversable[WebService] = _services

  def services_=(update : Traversable[WebService]) : Unit = {
    _services = update
  }

  /**
   * Lookup a service by qualified name.
   * @param nameSpace the name space for the qualified name
   * @param localName the local name of the qualified name
   * @return a possibly empty set of web services found for that qualified name
   */
  def findService(nameSpace : String , localName : String ) : Set[WebService] = {
    _qnameLookup.getOrElse(new QName(nameSpace, localName), Set[WebService]())
  }

  /**
   * Lookup a service by qualified name.
   * @param qName the qualified name
   * @return a possibly empty set of web services found for that qualified name
   */
  def findService(qName : QName) : Set[WebService] = {
    _qnameLookup.getOrElse(qName, Set[WebService]())
  }

  /**
   * Find a service by its interface implementation
   * @param iface the interface class
   * @return a possibly empty set of `WebService` that represent the services implementing that interface (in the WS sense)
   */
  def findServiceByInterface(iface : SootClass) : Set[WebService] =
    _serviceClassLookupByInterface.getOrElse(iface.name, Set()).toSet

}
