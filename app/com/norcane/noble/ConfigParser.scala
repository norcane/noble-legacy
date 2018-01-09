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

package com.norcane.noble

import cats.syntax.either._
import com.norcane.noble.api.models.{BlogConfig, StorageConfig}
import com.norcane.noble.utils.extendedConfiguration._
import play.api.Configuration

/**
  * Set of functions used to instantiate configuration model classes using the given configuration.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
object ConfigParser {

  /**
    * Constructs new instance of `StorageConfig` based on the given configuration object. The root
    * of this configuration must be the blog storage config configuration block.
    *
    * @param config configuration to parse
    * @return instance of `StorageConfig` or error message in case of failure
    */
  def parseStorageConfig(config: Configuration): Either[String, StorageConfig] = {
    val storageTypeE = config.getE[String]("type", "missing storage type configuration")
    val storageConfig = config.getO[Configuration]("config") map (_.underlying)
    storageTypeE map (storageType => StorageConfig(storageType, storageConfig))
  }

  /**
    * Constructs new instance of `BlogConfig` based on the given configuration object. The root
    * of this configuration must be the blog config configuration block.
    *
    * @param config configuration to parse
    * @return instance of `BlogConfig` or error message in case of failure
    */
  def parseBlogConfig(blogName: String, config: Configuration): Either[String, BlogConfig] = {
    val path = config.getO[String]("path").getOrElse("/blog")
    val reloadToken = config.getO[String]("reloadToken")
    val storageCfgE = config.getE[Configuration]("storage", "missing storage configuration")

    for {
      storageCfg <- storageCfgE
      storageConfig <- parseStorageConfig(storageCfg)
    } yield BlogConfig(blogName, path, reloadToken, storageConfig)
  }
}
