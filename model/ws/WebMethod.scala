/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee.model.ws

import scala.beans.BeanProperty
import soot.{Type, Value}


case class WebMethod (@BeanProperty val name : String,
                      @BeanProperty val targetMethodName : String,
                      @BeanProperty val argTypes : java.util.List[Type],
                      @BeanProperty val retType: Type,
                      @BeanProperty val defaultParams: java.util.List[Value])
{

}
