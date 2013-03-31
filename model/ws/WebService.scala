/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.model.ws

import beans.BeanProperty
import javax.xml.bind.annotation._
import annotation.meta.beanGetter

/**
 * Data holder for the data required to generate the web service caller
 * @author Marc-André Laverdière-Papineau
 */
@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
case class WebService
( @XmlAttribute val interfaceName : String, @XmlAttribute val implementationName : String,
  @XmlAttribute val initMethodName : String = "", @XmlAttribute val destroyMethodName : String ="") {

  //Required by Jax-WB
  private def this() = this("","","","")

}
