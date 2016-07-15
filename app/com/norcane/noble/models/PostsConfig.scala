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
  * Model class representing the configuration of blog's posts (e.g. post format type, etc). The
  * main idea is to provide modular architecture for adding support for various blog post types
  * (e.g. ''markdown'', ''HTML'', ''Wiki'') using the 3rd party modules. Each blog's post
  * configuration consists of `format` field, specifying uniquely the exact blog post format (using
  * this field the appropriate blog post format provider will be loaded) and optional configuration
  * block, which will be passed to the selected blog post format provider.
  *
  * @param format blog post format
  * @param config blog post format provider optional configuration
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class PostsConfig(format: String, config: Option[Configuration])

/**
  * Companion object for the [[PostsConfig]] model class.
  */
object PostsConfig {

  /**
    * Constructs new instance of [[PostsConfig]] based on the given configuration object. The root
    * of this configuration must be the blog posts config configuration block.
    *
    * @param config configuration to parse
    * @return instance of [[PostsConfig]] or error message in case of failure
    */
  def fromConfig(config: Configuration): String Xor PostsConfig = {
    val postTypeXor: String Xor String = Xor.fromOption(config.getString("type"),
      "missing blog posts type configuration")
    val postsConfig: Option[Configuration] = config.getConfig("config")

    for (postType <- postTypeXor) yield PostsConfig(postType, postsConfig)
  }
}