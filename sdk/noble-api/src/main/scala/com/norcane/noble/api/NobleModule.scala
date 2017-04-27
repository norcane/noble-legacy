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

package com.norcane.noble.api

import com.google.inject.{AbstractModule, Binder}
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder, ScalaOptionBinder}

/**
  * Base trait for Noble modules. The Noble module represents the way of extending the default
  * functionality of the Noble application, such as adding support for different blog storages,
  * blog post formats or blog templates.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait NobleModule extends AbstractModule with ScalaModule {

  /**
    * Registers the new blog storage, using its [[BlogStorageFactory]] implementation.
    *
    * = Example of use =
    * {{{
    *   registerBlogStorage[MyBlogStorageFactory]()
    * }}}
    *
    * @tparam T blog storage factory type
    */
  protected def registerBlogStorage[T <: BlogStorageFactory : Manifest](): Unit =
    ScalaMultibinder.newSetBinder[BlogStorageFactory](binder).addBinding.to[T]

  /**
    * Registers new format support, using its [[FormatSupportFactory]] implementation.
    *
    * = Example of use =
    * {{{
    *   registerFormatSupport[MyFormatSupportFactory]()
    * }}}
    *
    * @tparam T format support factory type
    */
  protected def registerFormatSupport[T <: FormatSupportFactory : Manifest](): Unit =
    ScalaMultibinder.newSetBinder[FormatSupportFactory](binder).addBinding.to[T]

  /**
    * Registers new blog theme, using its [[BlogThemeFactory]] implementation.
    *
    * = Example of use =
    * {{{
    *   registerBlogTheme[MyBlogThemeFactory]()
    * }}}
    *
    * @tparam T blog theme factory type
    */
  protected def registerBlogTheme[T <: BlogThemeFactory : Manifest](): Unit =
    ScalaMultibinder.newSetBinder[BlogThemeFactory](binder).addBinding.to[T]

  /**
    * Registers the default binding for selected service interface, using the ''Guice'' option
    * binding. This binding can be override by client blog by [[overrideBinding]] method.
    *
    * = Example of use =
    * {{{
    *   defaultBinding[MarkdownService].to[DefaultMarkdownService]
    * }}}
    *
    * @tparam T type of the service
    * @return binding builder
    */
  protected def defaultBinding[T: Manifest]: ScalaModule.ScalaLinkedBindingBuilder[T] =
    ScalaOptionBinder.newOptionBinder[T](binder).setDefault

  /**
    * Overrides the default binding of selected service, previously bound by [[defaultBinding]]
    * method.
    *
    * = Example of use =
    * {{{
    *   overrideBinding[MarkdownService].to[MyCustomMarkdownService]
    * }}}
    *
    * @tparam T type of the service
    * @return binding builder
    */
  protected def overrideBinding[T: Manifest]: ScalaModule.ScalaLinkedBindingBuilder[T] =
    ScalaOptionBinder.newOptionBinder[T](binder).setBinding

  override protected def binder: Binder = super.binder()
}