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
package com.norcane.noble.utils

import cats.syntax.either._
import play.api.{ConfigLoader, Configuration}

import scala.util.Try

/**
  * By default, the `get[T]` methods of `play.api.Configuration` doesn't provide any way to return
  * optional value for given key (if key doesn't exist, method call ends up with exception). This
  * code extends the class by new methods, using the ''Pimp My Library'' pattern.
  */
private[utils] object ConfigurationOps {

  implicit class ConfigurationOps(config: Configuration) {

    /**
      * Get the configuration value for the given path.
      *
      * @param path   path
      * @param loader implicit config loader
      * @tparam T type of the configuration value
      * @return configuration value (optional)
      */
    def getO[T](path: String)(implicit loader: ConfigLoader[T]): Option[T] =
      Try(config.get[T](path)).toOption

    /**
      * Get the configuration value for the given path, or the error message.
      *
      * @param path    path
      * @param message error message (in case that no value for given path found)
      * @param loader  implicit configuration loader
      * @tparam T type of the configuration value
      * @return configuration value or error message
      */
    def getE[T](path: String, message: String)(implicit loader: ConfigLoader[T]): Either[String, T] =
      Either.fromOption(getO(path), message)
  }

}
