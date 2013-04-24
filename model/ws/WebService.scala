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
case class WebService
( @(XmlAttribute @beanGetter) @BeanProperty val interfaceName : String,
  @(XmlAttribute @beanGetter) @BeanProperty val implementationName : String,
  @(XmlAttribute @beanGetter) @BeanProperty val initMethodName : String = "",
  @(XmlAttribute @beanGetter) @BeanProperty val destroyMethodName : String ="",
  @(XmlAttribute @beanGetter) @BeanProperty val name : String ="",
  @(XmlAttribute @beanGetter) @BeanProperty val targetNamespace : String ="",
  @(XmlAttribute @beanGetter) @BeanProperty val serviceName : String ="",
  @(XmlAttribute @beanGetter) @BeanProperty val wsdlLocation : String ="",
  @(XmlAttribute @beanGetter) @BeanProperty val portName : String =""
  ){

  //Required by Jax-WB
  def this() = this("","","","","","","","","")

}
