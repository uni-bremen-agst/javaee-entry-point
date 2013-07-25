/*
 * (c) 2013 École Polytechnique de Montréal & Tata Consultancy Services.
 * All rights reserved.
 */
package soot.jimple.toolkits.javaee.model.ws

import beans.BeanProperty
import javax.xml.bind.annotation._
import scala.collection.JavaConversions._

object WebService{
  def wrapperName(rootPackage : String, implementationClassName : String) : String = rootPackage + "." + implementationClassName + "Wrapper"
}

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
( @BeanProperty val interfaceName : String,
  @BeanProperty val implementationName : String,
  @BeanProperty val wrapperName : String,
  @BeanProperty val initMethodName : String = "",
  @BeanProperty val destroyMethodName : String ="",
  @BeanProperty val name : String ="",
  @BeanProperty val targetNamespace : String ="",
  @BeanProperty val serviceName : String ="",
  @BeanProperty val wsdlLocation : String ="",
  @BeanProperty val portName : String ="",
  @BeanProperty val handlerChain : java.util.List[String] = List[String](),
  @BeanProperty val methods : java.util.Collection[WebMethod] = List[WebMethod]()
  ){

  //Required by Jax-WB
  def this() = this("","","","","","","","","","", List[String](), List[WebMethod]())

}
