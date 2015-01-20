/*
 * (c) 2015 Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
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
