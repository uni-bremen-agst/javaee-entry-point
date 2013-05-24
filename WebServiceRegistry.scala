/**
 * (c) 2013 Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee

import soot.jimple.toolkits.javaee.model.ws.WebService
import javax.xml.namespace.QName
import scala.collection._

/**
 * A registry of the web services detected in the application under analysis
 */
object WebServiceRegistry {

  private var _services : Traversable[WebService] = Array[WebService]()
  private lazy val _qnameLookup : Map[QName, Set[WebService]] = {
    val multimap = new mutable.HashMap[QName, mutable.Set[WebService]] with mutable.MultiMap[QName,WebService]
    _services.foreach(ws => multimap.addBinding(new QName(ws.targetNamespace, ws.serviceName), ws))
    multimap
  }

  def services : Traversable[WebService] = _services

  def services(update : Traversable[WebService]) : Unit = {
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

}
