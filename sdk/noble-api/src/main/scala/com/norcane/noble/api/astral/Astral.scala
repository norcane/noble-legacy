/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016 norcane
 * |_| |_|\___/|_.__/|_|\___|
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.norcane.noble.api.astral

import java.time.{LocalDate, ZoneId}
import java.util.Date

import scala.annotation.implicitNotFound
import scala.util.Try

/**
  * ''ASTral'' - a missing universal ''Abstract Syntax Tree'' structure allowing to represent
  * various data formats in unified way. Parsing from any type to instance of [[Astral]] data class
  * is done using the [[AstralParser]] ''typeclass''. Retrieving a value of specific type from the
  * [[Astral]] ''AST'' is done using the [[Astral#get]] method and [[AstralType]] ''typeclass''.
  * Transforming [[Astral]] instance into specific ''case classes'' is not supported at this moment.
  *
  * Please note that this project is experimental and its ''API'' may vary between releases. Either
  * it may be replaced (if not meeting requirements) or may be moved to separate project in future.
  *
  * @param underlying underlying map holding the actual data
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class Astral(underlying: Map[String, Any]) {

  /**
    * Returns the value of type `T` for the given ''key''. For each used value type an instance of
    * the [[AstralType]] must be implicitly in scope. ''Ready-to-use'' [[AstralType]]
    * implementations are available in the [[Astral.Defaults]] object.
    *
    * @param key key for which to return the value
    * @tparam T type of the value
    * @return value (if found)
    */
  def get[T: AstralType](key: String): Option[T] =
    underlying.get(key) flatMap implicitly[AstralType[T]].read

}

/**
  * Companion object for the [[Astral]] class.
  */
object Astral {

  /**
    * Returns an empty [[Astral]] instance.
    */
  val empty: Astral = Astral(Map())

  /**
    * Parses the given input data of type `T` and creates new instance of [[Astral]]. For each used
    * input data type an instance of the [[AstralParser]] must be implicitly in scope.
    *
    * @param input input data to parse
    * @tparam T type of the input data
    * @return instance of the [[Astral]] class (or error details in case of parsing failure)
    */
  def parse[T: AstralParser](input: T): Try[Astral] = implicitly[AstralParser[T]].parse(input)

  /**
    * Object providing several implementations of the [[AstralType]] ''typeclass'' for common types.
    */
  object Defaults {

    /**
      * Provides support for reading value of type string.
      */
    implicit val stringValue: AstralType[String] = AstralType {
      case string: String => string
    }

    /**
      * Provides support for reading value of type integer.
      */
    implicit val intValue: AstralType[Int] = AstralType {
      case int: Int => int
    }

    /**
      * Provides support for reading value of type list.
      *
      * @tparam T type of list values
      * @return instance of [[AstralType]] typeclass
      */
    implicit def listValue[T]: AstralType[List[T]] = AstralType {
      case list: List[T]@unchecked => list
    }

    /**
      * Provides support for reading nested [[Astral]] objects.
      */
    implicit val astral: AstralType[Astral] = AstralType {
      case astral: Astral => astral
    }

    /**
      * Provides support for reading Java 8's `LocalDate` date value.
      */
    implicit val localDateValue: AstralType[LocalDate] = AstralType {
      case date: Date => date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
    }
  }

}

/**
  * ''Typeclass'' used to parse input data of type `T` and create new instance of [[Astral]] class.
  *
  * @tparam T type of the input data
  */
@implicitNotFound("Cannot find ASTral parser for input of type ${T}")
trait AstralParser[T] {

  /**
    * Parses input data and creates new instance of [[Astral]] class.
    *
    * @param input input data to parse
    * @return instance of the [[Astral]] class (or error details in case of parsing failure)
    */
  def parse(input: T): Try[Astral]
}

/**
  * ''Typeclass'' used to read value of type `T` from the [[Astral]] instance, using the
  * [[Astral#get]] method.
  *
  * @tparam T type of the value
  */
@implicitNotFound("Cannot find reader for ASTral value of type ${T}")
trait AstralType[T] {

  /**
    * Reads the input value and converts it to the specified type.
    *
    * @param value value to read
    * @return converted value (or nothing if cannot be converted)
    */
  def read(value: Any): Option[T]

}

/**
  * Companion object for the [[AstralType]] class.
  */
object AstralType {

  /**
    * Helper method, allowing to construct new instance of [[AstralType]] ''typeclass'' using the
    * ''partial function''.
    *
    * @param f partial function
    * @tparam T type of the value the new instance of [[AstralType]] is for
    * @return instance of [[AstralType]] class
    */
  def apply[T](f: PartialFunction[Any, T]): AstralType[T] = new AstralType[T] {

    override def read(value: Any): Option[T] = f.lift(value)
  }
}
