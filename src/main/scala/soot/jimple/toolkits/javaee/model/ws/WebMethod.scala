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

import soot.Type

import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.util.hashing.MurmurHash3

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
    import scala.util.hashing.MurmurHash3._
    var tmp = MurmurHash3.mix(symmetricSeed,stringHash(name))
    tmp = MurmurHash3.mix(tmp, stringHash(targetMethodName))
    tmp = MurmurHash3.mix(tmp,seqHash(argTypes.asScala))
    tmp = MurmurHash3.mixLast(tmp,retType.hashCode())
    MurmurHash3.finalizeHash(tmp,3)

  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: WebMethod =>
        name == other.name && targetMethodName == other.targetMethodName &&
          argTypes == other.argTypes && retType == other.retType
      case _ => false
    }
  }

  override def toString: String =
    s"name: ${name} target method: ${targetMethodName} argument types: ${argTypes.asScala.mkString(",")} return type: ${retType}"
}
