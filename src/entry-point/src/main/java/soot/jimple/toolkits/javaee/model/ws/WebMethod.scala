/**
# * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee.model.ws

import scala.beans.BeanProperty
import soot.Type
import scala.util.hashing.MurmurHash3

import scala.collection.JavaConversions._

/**
 * Represents a method of a web service
 * @param service back-pointer to the service
 * @param name name of the method from the client side
 * @param targetMethodName the name of the method executed in the web service
 * @param argTypes the arguments' type
 * @param retType the return type
 *
 * @author Marc-André Laverdière-Papineau
 */
case class WebMethod (@BeanProperty var service : WebService,
                      @BeanProperty name : String,
                      @BeanProperty targetMethodName : String,
                      @BeanProperty argTypes : java.util.List[Type],
                      @BeanProperty retType: Type)
{
  override def hashCode(): Int = {
    import MurmurHash3._
    var tmp = MurmurHash3.mix(symmetricSeed,stringHash(name))
    tmp = MurmurHash3.mix(tmp, stringHash(targetMethodName))
    tmp = MurmurHash3.mix(tmp,seqHash(argTypes))
    tmp = MurmurHash3.mixLast(tmp,retType.hashCode())
    MurmurHash3.finalizeHash(tmp,3)

  }

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[WebMethod]) {
      val other = obj.asInstanceOf[WebMethod]
      name == other.name && targetMethodName == other.targetMethodName &&
      argTypes == other.argTypes && retType == other.retType
    } else
      false
  }
}
