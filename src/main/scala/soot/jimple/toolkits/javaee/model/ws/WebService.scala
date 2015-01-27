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

    Copyright 2013 Ecole Polytechnique de Montreal & Tata Consultancy Services
 */
package soot.jimple.toolkits.javaee.model.ws

import javax.xml.bind.annotation.XmlRootElement

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

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
( @BeanProperty interfaceName : String,
  @BeanProperty implementationName : String,
  @BeanProperty wrapperName : String,
  @BeanProperty initMethodName : String = "",
  @BeanProperty destroyMethodName : String ="",
  @BeanProperty name : String ="",
  @BeanProperty targetNamespace : String ="",
  @BeanProperty serviceName : String ="",
  @BeanProperty wsdlLocation : String ="",
  @BeanProperty portName : String ="",
  @BeanProperty handlerChain : java.util.List[String] = List[String]().asJava,
  @BeanProperty methods : java.util.Collection[WebMethod] = List[WebMethod]().asJava,
  @BeanProperty hasAsyncAlready : Boolean = false
  ){

  //Required by Jax-WB
  def this() = this("","","","","","","","","","", List[String]().asJava, List[WebMethod]().asJava,false)

}
