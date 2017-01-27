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

package com.norcane.noble.models

import com.norcane.noble.ConfigParser
import com.norcane.noble.api.models.{BlogConfig, StorageConfig}
import org.specs2.matcher.Matchers
import org.specs2.mutable
import play.api.Configuration

/**
  * ''Specs2'' specification for the [[ConfigParser]].
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class ConfigParserSpec extends mutable.Specification with Matchers {
  val className: String = ConfigParser.getClass.getSimpleName

  s"The $className should" >> {
    s"properly initialize ${BlogConfig.getClass.getSimpleName} from configuration" >> {
      ConfigParser.parseBlogConfig(testName, initTestConfiguration) must
        beEqualTo(Right(initTestBlogConfig))
    }
  }

  private val testName: String = "testName"
  private val testPath: String = "testPath"
  private val testType: String = "testType"
  private val testValue: String = "testValue"

  private def initTestBlogConfig: BlogConfig = BlogConfig(
    name = testName, path = testPath, reloadToken = Some("token1"), storageConfig = StorageConfig(
      testType, Some(Configuration("testKey" -> testValue).underlying))
  )

  private def initTestConfiguration: Configuration = Configuration(
    "path" -> testPath,
    "reloadToken" -> "token1",
    "storage" -> Map(
      "type" -> testType,
      "config" -> Map(
        "testKey" -> testValue
      )
    )
  )

}
