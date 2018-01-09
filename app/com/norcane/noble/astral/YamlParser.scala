/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016-2018 norcane
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

package com.norcane.noble.astral

import java.util.Date

import com.norcane.noble.api.astral.{Astral, AstralParser}
import play.api.Logger

import scala.util.Try

/**
  * Implementation of the `AstralParser` ''typeclass'', allowing to parse the input ''YAML'' data
  * and convert them into the instance of `Astral` class. This implementation uses the
  * [[http://www.snakeyaml.org SnakeYAML]] library for parsing the YAML data.
  */
class YamlParser extends AstralParser[RawYaml] {

  private val log: Logger = Logger(getClass)

  override def parse(input: RawYaml): Try[Astral] = Try {
    process(new org.yaml.snakeyaml.Yaml().load(input.yaml)) match {
      case astral: Astral => astral
      case other =>
        log.warn("parsed YAML is not object, is the YAML content valid?")
        Astral.empty
    }
  }

  import scala.collection.JavaConverters._

  private def process(obj: AnyRef): AnyRef = obj match {
    case map: java.util.Map[String, AnyRef]@unchecked =>
      Astral(map.asScala.toMap.mapValues(process))
    case list: java.util.List[AnyRef]@unchecked => list.asScala.toList map process
    case string: String => string
    case number: Number => number
    case boolean: java.lang.Boolean => boolean
    case date: Date => date
    case null => null
    case other =>
      log.warn(s"unexpected YAML object of type ${other.getClass}")
      other.toString
  }
}

/**
  * Companion object for the [[YamlParser]] class.
  */
object YamlParser {

  /**
    * Returns shared instance of [[YamlParser]] class.
    */
  lazy val parser: YamlParser = new YamlParser()
}

/**
  * Because of the ''typeclass''-based approach of parsing input data used in the ''ASTral''
  * library, input ''YAML'' data must be distinguishable by its type, hence the ordinary
  * strin type cannot be used. Therefore the [[RawYaml]] ''value class'' is introduced to wrap
  * the raw ''YAML'' input string and make the ''ASTral'' library happy.
  *
  * @param yaml raw ''YAML'' data as plain string
  */
case class RawYaml(yaml: String) extends AnyVal
