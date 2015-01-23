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

    Copyright 2015 Ecole Polytechnique de Montreal & Tata Consultancy Services
 */
package soot.jimple.toolkits.javaee

import soot.util.SootAnnotationUtils._
object JaxWsAnnotations {
  final val WEBSERVICE_ANNOTATION: String = javaToSootForm("javax.jws.WebService")
  final val WEBMETHOD_ANNOTATION: String = javaToSootForm("javax.jws.WebMethod")
  final val WEBSERVICE_REF_ANNOTATION: String = javaToSootForm("javax.jws.WebServiceRef")
  final val WEBENDPOINT_ANNOTATION: String = javaToSootForm("javax.xml.ws.WebEndpoint")
  final val WEBSERVICE_POSTINIT_ANNOTATION: String = javaToSootForm("javax.annotation.PostConstruct")
  final val WEBSERVICE_PREDESTROY_ANNOTATION: String = javaToSootForm("javax.annotation.PreDestroy")
  final val WEBSERVICE_CLIENT_ANNOTATION: String = javaToSootForm("javax.xml.ws.WebServiceClient")
  final val HANDLER_CHAIN_ANNOTATION: String = javaToSootForm("javax.jws.HandlerChain")
  final val WEBFAULT_ANNOTATION : String = javaToSootForm("javax.xml.ws.WebFault")

}
