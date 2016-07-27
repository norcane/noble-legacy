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

package com.norcane.noble.modules

import com.norcane.api.{BlogStorageFactory, FormatSupportFactory, NobleModule}
import com.norcane.noble.formatsupport.MarkdownFormatSupportFactory
import com.norcane.noble.storages.GitBlogStorageFactory
import net.codingwell.scalaguice.ScalaMultibinder

class NobleDefaultsModule extends NobleModule {

  override def configure(): Unit = {
    // add default support for Git blog storage
    ScalaMultibinder.newSetBinder[BlogStorageFactory](binder)
      .addBinding.to[GitBlogStorageFactory]

    // add default support for Markdown blog post/page format
    ScalaMultibinder.newSetBinder[FormatSupportFactory](binder)
      .addBinding.to[MarkdownFormatSupportFactory]
  }
}
