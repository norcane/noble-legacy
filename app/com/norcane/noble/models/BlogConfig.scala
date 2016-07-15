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
  * Model class representing the configuration of one blog.
  *
  * @param path          the path of the blog
  * @param postsConfig   configuration of blog posts
  * @param storageConfig configuration of blog storage
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class BlogConfig(path: String, postsConfig: PostsConfig, storageConfig: StorageConfig)

/**
  * Companion object for the [[BlogConfig]] model class.
  */
object BlogConfig {

  /**
    * Constructs new instance of [[BlogConfig]] based on the given configuration object. The root
    * of this configuration must be the blog config configuration block.
    *
    * @param config configuration to parse
    * @return instance of [[BlogConfig]] or error message in case of failure
    */
  def fromConfig(config: Configuration): String Xor BlogConfig = {
    val path: String = config.getString("path").getOrElse("/blog")
    val postsCfgXor: String Xor Configuration = Xor.fromOption(config.getConfig("posts"),
      "missing blog posts configuration")
    val storageCfgXor: String Xor Configuration = Xor.fromOption(config.getConfig("storage"),
      "missing storage configuration")

    for {
      storageCfg <- storageCfgXor
      postsCfg <- postsCfgXor
      storageConfig <- StorageConfig.fromConfig(storageCfg)
      postsConfig <- PostsConfig.fromConfig(postsCfg)
    } yield BlogConfig(path, postsConfig, storageConfig)
  }
}
