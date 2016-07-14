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

case class BlogConfig(path: String, storageConfig: StorageConfig)

object BlogConfig {
  def fromConfig(config: Configuration): String Xor BlogConfig = {
    val path: String = config.getString("path").getOrElse("/blog")
    val storageCfgXor: String Xor Configuration = Xor.fromOption(config.getConfig("storage"),
      "missing storage configuration")

    for {
      storageCfg <- storageCfgXor
      storageConfig <- StorageConfig.fromConfig(storageCfg)
    } yield BlogConfig(path, storageConfig)
  }
}
