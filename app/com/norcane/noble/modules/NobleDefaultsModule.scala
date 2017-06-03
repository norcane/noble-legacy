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

package com.norcane.noble.modules

import com.norcane.noble.api.NobleModule
import com.norcane.noble.formatsupport.MarkdownFormatSupportFactory
import com.norcane.noble.services.MarkdownService
import com.norcane.noble.services.impl.FlexmarkMarkdownService
import com.norcane.noble.storages.GitBlogStorageFactory
import com.norcane.noble.themes.HumaneThemeFactory

/**
  * This *Noble module* registers the default implementation of various services, bundled with
  * standard *noble* distribution.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class NobleDefaultsModule extends NobleModule {

  override def configure(): Unit = {
    // register the implementation of BlogStorage, providing support for Git-based storage
    registerBlogStorage[GitBlogStorageFactory]()

    // register the implementation of FormatSupport, providing support for Markdown-based posts
    registerFormatSupport[MarkdownFormatSupportFactory]()

    // registers the default theme 'Humane'
    registerBlogTheme[HumaneThemeFactory]()

    // default services bindings (can be override in client blog)
    defaultBinding[MarkdownService].to[FlexmarkMarkdownService]
  }
}
