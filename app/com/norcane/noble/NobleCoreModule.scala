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

package com.norcane.noble

import com.norcane.api.{BlogStorageFactory, NobleModule}
import net.codingwell.scalaguice.ScalaMultibinder

/**
  * This Noble module prepares the environment for another 3rd party modules and registers some of
  * the built-in modules.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class NobleCoreModule extends NobleModule {

  override def configure(): Unit = {
    ScalaMultibinder.newSetBinder[BlogStorageFactory](binder)
  }
}
