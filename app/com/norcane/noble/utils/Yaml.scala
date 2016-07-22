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

import java.util.Date

import org.joda.time.DateTime
import play.api.Logger

import scala.reflect.ClassTag
import scala.util.Try

case class Yaml(map: Map[String, AnyRef]) {
  private val log: Logger = Logger(getClass)

  def get[T](key: String)(implicit ct: ClassTag[T]): Option[T] = map.get(key) flatMap {
    case value if ct.runtimeClass.isInstance(value) => Some(value.asInstanceOf[T])
    case other =>
      log.warn(s"ingoring value for key '$key', expected type $ct but was $other")
      None
  }
}

object Yaml {
  val empty: Yaml = Yaml(Map())

  def parse(yaml: String): Try[Yaml] = {
    import scala.collection.JavaConverters._

    def yaml2scala(obj: AnyRef): AnyRef = obj match {
      case map: java.util.Map[String, AnyRef] => new Yaml(map.asScala.toMap.mapValues(yaml2scala))
      case list: java.util.List[AnyRef] => list.asScala.toList map yaml2scala
      case string: String => string
      case number: Number => number
      case boolean: java.lang.Boolean => boolean
      case date: Date => new DateTime(date)
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
}
