/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.model.ws

import beans.BeanProperty
import javax.xml.bind.annotation._
import annotation.meta.beanGetter
import soot.{Value, Local, SootMethod}
import scala.collection.JavaConversions._

/**
 * Data holder for the data required to generate the web service caller
 * @param interfaceName fully-qualified name of the Java class defining the interface
 * @param implementationName fully-qualified name of the Java class with the implementation
 * @param initMethodName name of the method that has the `@PostConstruct` annotation
 * @param destroyMethodName name of the method that has the `@PreDestroy` annotation
 * @param name local name of the WS (`name` attribute)
 * @param targetNamespace name space for the local name (`targetNamespace` attribute)
 * @param serviceName name of the service (not the same as the local name) (`serviceName` attribute)
 * @param wsdlLocation location of the WSDL definition file (`wsdlLocation` attribute)
 * @param portName name of the service port (`portName` attribute)
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
  @(XmlAttribute @beanGetter) @BeanProperty val portName : String ="",
  @(XmlAttribute @beanGetter) @BeanProperty val handlerChain : java.util.List[String] = List[String](),
  @(XmlAttribute @beanGetter) @BeanProperty val methods : java.util.Collection[WebMethod] = List[WebMethod]()
  ){

  //Required by Jax-WB
  def this() = this("","","","","","","","","", List[String](), List[WebMethod]())

}
