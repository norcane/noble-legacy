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

case class StorageConfig(storageType: String, config: Option[Configuration])

object StorageConfig {
  def fromConfig(config: Configuration): String Xor StorageConfig = {
    val storageTypeXor: String Xor String = Xor.fromOption(config.getString("type"),
      "missing storage type configuration")
    val storageConfig: Option[Configuration] = config.getConfig("config")

    for (storageType <- storageTypeXor) yield StorageConfig(storageType, storageConfig)
  }
}