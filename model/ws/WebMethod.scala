/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee.model.ws

import scala.beans.BeanProperty


case class WebMethod (@BeanProperty val name : String,
                      @BeanProperty val targetMethodName : String,
                      @BeanProperty val argTypes : List[String],
                      @BeanProperty val retType: String,
                      @BeanProperty val defaultParams:List[String])
{

}
