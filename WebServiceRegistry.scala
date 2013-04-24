/**
 * (c) 2013 Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee

import soot.jimple.toolkits.javaee.model.ws.WebService

/**
 * A registry of the web services detected in the application under analysis
 */
object WebServiceRegistry {

  var services : Traversable[WebService] = Array[WebService]()

}
