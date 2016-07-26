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

package com.norcane.noble.utils

import java.time.{LocalDate, ZoneId}
import java.util.Date

import play.api.Logger

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag
import scala.util.Try

/**
  * Represents parsed ''YAML''.
  *
  * @param map raw parsed ''YAML'' data
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class Yaml(map: Map[String, AnyRef]) {

  private val log: Logger = Logger(getClass)

  /**
    * Returns the value for the specified ''key''. For each used value type, an instance of the
    * ''typeclass'' [[YamlValue]] must be available in scope. Ready-to-use typeclasses for most
    * common values are defined in the [[Yaml.Defaults]] object.
    *
    * @param key key of the desired value
    * @tparam T type of the value
    * @return value (if found)
    */
  def get[T: YamlValue](key: String): Option[T] = map.get(key) flatMap implicitly[YamlValue[T]].parse
}

/**
  * Companion object for the [[Yaml]] class, provides methods to parse raw ''YAML'' data.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
object Yaml {
  val empty: Yaml = Yaml(Map())

  /**
    * Parses the ''YAML'' from the given string. In case of success, instance of [[Yaml]] class is
    * returned, otherwise error cause is provided.
    *
    * @param yaml raw ''YAML'' input to parse
    * @return parsed ''YAML'' represented by the [[Yaml]] instance
    */
  def parse(yaml: String): Try[Yaml] = {

    import scala.collection.JavaConverters._

    def yaml2scala(obj: AnyRef): AnyRef = obj match {
      case map: java.util.Map[String, AnyRef]@unchecked =>
        new Yaml(map.asScala.toMap.mapValues(yaml2scala))
      case list: java.util.List[AnyRef@unchecked] => list.asScala.toList map yaml2scala
      case string: String => string
      case number: Number => number
      case boolean: java.lang.Boolean => boolean
      case date: Date => date
      case null => null
      case other =>
        Logger.warn(s"unexpected YAML object of type ${other.getClass}")
        other.toString
    }

    Try {
      yaml2scala(new org.yaml.snakeyaml.Yaml().load(yaml)) match {
        case yaml: Yaml => yaml
        case other =>
          Logger.warn("parsed YAML is not object, is the YAML content valid?")
          Yaml.empty
      }
    }
  }

  /**
    * Set of ready-to-use, common ''YAML'' value parsers.
    */
  object Defaults {

    /**
      * Provides support for reading ''YAML'' value of type string.
      */
    implicit val stringValue: YamlValue[String] = YamlValue {
      case string: String => string
    }

    /**
      * Provides support for reading ''YAML'' value of type integer.
      */
    implicit val intValue: YamlValue[Int] = YamlValue {
      case int: Int => int
    }

    /**
      * Provides support for reading ''YAML'' value of type list.
      *
      * @tparam T type of list values
      * @return instance of [[YamlValue]] typeclass
      */
    implicit def listValue[T]: YamlValue[List[T]] = YamlValue {
      case list: List[T@unchecked] => list
    }

    /**
      * Provides support for reading nested ''YAML'' objects.
      */
    implicit val yamlValue: YamlValue[Yaml] = YamlValue {
      case yaml: Yaml => yaml
    }

    /**
      * Provides support for reading Java 8's `LocalDate` date value.
      */
    implicit val localDateValue: YamlValue[LocalDate] = YamlValue {
      case date: Date => date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
    }
  }

}

/**
  * ''Typeclass'' used to parse ''YAML'' value of specific type.
  *
  * @tparam T type of the parsed value
  */
@implicitNotFound("Cannot find typeclass for YAML value of type ${T}")
trait YamlValue[T] {

  /**
    * Parses the value of required type from the raw value.
    *
    * @param value raw value
    * @return parsed value (or `None` if not possible to parse)
    */
  def parse(value: Any): Option[T]
}

/**
  * Companion object of the [[YamlValue]] class.
  */
object YamlValue {

  private val log: Logger = Logger(getClass)

  /**
    * Method defining comfortable way of creating new instance of [[YamlValue]], based on the
    * given ''partial function''.
    *
    * == Example of use ==
    * {{{
    *   implicit val stringValue: YamlValue[String] = YamlValue {
    *     case s: String => s
    *   }
    * }}}
    *
    * @param f partial function used to parse the raw ''YAML'' value
    * @tparam T type of the parsed ''YAML'' value
    * @return new instance of [[Yaml]] typeclass
    */
  def apply[T: ClassTag](f: PartialFunction[Any, T]): YamlValue[T] = new YamlValue[T] {
    override def parse(value: Any): Option[T] = f.lift(value) match {
      case Some(v) => Some(v)
      case None =>
        log.warn(s"value $value is not of declared type ${implicitly[ClassTag[T]].runtimeClass}")
        None
    }
  }
}
