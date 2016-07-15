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

package com.norcane.noble.models

import cats.data.Xor
import org.specs2.mutable.Specification
import play.api.Configuration

class BlogConfigSpec extends Specification {

  val className: String = BlogConfig.getClass.getSimpleName

  s"This is the specification for the $className".txt

  s"The $className should" >> {
    "properly initialize itself from configuration" >> {
      BlogConfig.fromConfig(initTestConfiguration) must beEqualTo(Xor.right(initTestBlogConfig))
    }
  }

  private val testPath: String = "testPath"
  private val testType: String = "testType"
  private val testValue: String = "testValue"

  private def initTestBlogConfig: BlogConfig = BlogConfig(
    path = testPath, storageConfig = StorageConfig(
      storageType = testType, config = Some(Configuration("testKey" -> testValue))
    )
  )

  private def initTestConfiguration: Configuration = Configuration(
    "path" -> testPath,
    "storage" -> Map(
      "type" -> testType,
      "config" -> Map(
        "testKey" -> testValue
      )
    )
  )
}