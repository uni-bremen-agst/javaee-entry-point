/**
# * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.toolkits.javaee.model.ws

import scala.beans.BeanProperty
import soot.{Type, Value}
import scala.runtime.ScalaRunTime
import scala.util.hashing.MurmurHash3

import scala.collection.JavaConversions._

case class WebMethod (@BeanProperty var service : WebService,
                      @BeanProperty val name : String,
                      @BeanProperty val targetMethodName : String,
                      @BeanProperty val argTypes : java.util.List[Type],
                      @BeanProperty val retType: Type,
                      @BeanProperty val defaultParams: java.util.List[Value])
{
  override def hashCode(): Int = {
    import MurmurHash3._
    var tmp = MurmurHash3.mix(symmetricSeed,stringHash(name))
    tmp = MurmurHash3.mix(tmp, stringHash(targetMethodName))
    tmp = MurmurHash3.mix(tmp,seqHash(argTypes))
    tmp = MurmurHash3.mix(tmp,retType.hashCode())
    tmp = MurmurHash3.mixLast(tmp,seqHash(defaultParams))
    MurmurHash3.finalizeHash(tmp,3)

  }

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[WebMethod]) {
      val other = obj.asInstanceOf[WebMethod]
      name == other.name && targetMethodName == other.targetMethodName &&
      argTypes == other.argTypes && retType == other.retType &&
      defaultParams == other.defaultParams
    } else
      false
  }
}
