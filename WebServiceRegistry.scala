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
  private lazy val _qnameLookup : Map[QName, Set[WebService]] = populateLookup()


  def services(update : Traversable[WebService]) : Unit = {
    _services = update
  }

  /**
   * Lookup a service by qualified name.
   * @param nameSpace the name space for the qualified name
   * @param localName the local name of the qualified name
   * @return an option for a WebService
   */
  def findService(nameSpace : String , localName : String ) : Option[Set[WebService]] = {
    _qnameLookup.get(new QName(nameSpace, localName))
  }

  /**
   * Lookup a service by qualified name.
   * @param qName the qualified name
   * @return an option for a WebService
   */
  def findService(qName : QName) : Option[Set[WebService]] = {
    _qnameLookup.get(qName)
  }

  private def populateLookup() : Map[QName, Set[WebService]] = { //TODO deal with collisions on the implementation of the service.

    val multimap = new mutable.HashMap[QName, mutable.Set[WebService]] with mutable.MultiMap[QName,WebService]
    _services.foreach(ws => multimap.addBinding(new QName(ws.targetNamespace, ws.serviceName), ws))
    multimap
  }

}
