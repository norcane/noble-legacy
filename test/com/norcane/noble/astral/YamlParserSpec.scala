/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016-2017 norcane
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

import com.norcane.noble.api.astral.Astral
import org.specs2.matcher.Matchers
import org.specs2.mutable

import scala.io.{Codec, Source}
import scala.util.Try


/**
  * ''Specs2'' specification for the [[YamlParser]] class.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class YamlParserSpec extends mutable.Specification with Matchers {

  import Astral.Defaults._

  private implicit val yamlParser: YamlParser = new YamlParser()

  private val testFile: String = "/testFile.yml"
  private val rawYaml: RawYaml = RawYaml(
    Source.fromInputStream(getClass.getResourceAsStream(testFile))(Codec.UTF8).mkString)
  private val parsedYaml: Try[Astral] = Astral.parse(rawYaml)


  "The YAML parser should" >> {

    "properly load the test file" >> {
      parsedYaml.isSuccess must beTrue
    }

    "properly parse string values" >> {
      parsedYaml.get.get[String]("testString") must beEqualTo(Some("testValue"))
    }

    "properly parse integer numbers" >> {
      parsedYaml.get.get[Int]("testInt") must beEqualTo(Some(42))
    }

    "properly parse string list" >> {
      parsedYaml.get.get[List[String]]("testList") must beEqualTo(Some(List("first", "second")))
    }

    "properly parse nested ASTral object" >> {
      parsedYaml.get.get[Astral]("testMap")
        .flatMap(yaml => yaml.get[String]("key1")) must beEqualTo(Some("value1"))
    }
  }
}
