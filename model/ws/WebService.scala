/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.model.ws

import beans.BeanProperty

/**
 * @author Marc-André Laverdière-Papineau
 */
case class WebService
(@BeanProperty val interfaceName : String, @BeanProperty val implementationName : String,
 @BeanProperty val initMethodName : String = "", @BeanProperty val destroyMethodName : String ="") {

}
