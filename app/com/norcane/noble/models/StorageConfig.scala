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
import play.api.Configuration

/**
  * Model class representing the configuration of blog's storage (i.e. place from blog posts
  * and assets are loaded). The main idea is to provide modular architecture for adding support for
  * various blog storage types (e.g. ''Git'' repository, local filesystem, database, cloud storage)
  * using the 3rd party modules. Each blog storage configuration consists of  `format` field,
  * specifying uniquely the exact blog storage type (using this field the appropriate blog storage
  * provider will be loaded) and optional configuration block, which will be passed to the selected
  * blog storage provider.
  *
  * @param storageType blog storage type
  * @param config      blog storage provider optional configuration
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class StorageConfig(storageType: String, config: Option[Configuration])

/**
  * Companion object for the [[StorageConfig]] model class.
  */
object StorageConfig {

  /**
    * Constructs new instance of [[StorageConfig]] based on the given configuration object. The root
    * of this configuration must be the blog storage config configuration block.
    *
    * @param config configuration to parse
    * @return instance of [[StorageConfig]] or error message in case of failure
    */
  def fromConfig(config: Configuration): String Xor StorageConfig = {
    val storageTypeXor: String Xor String = Xor.fromOption(config.getString("type"),
      "missing storage type configuration")
    val storageConfig: Option[Configuration] = config.getConfig("config")

    for (storageType <- storageTypeXor) yield StorageConfig(storageType, storageConfig)
  }
}