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

package com.norcane.noble.api.models

import com.norcane.noble.api.astral.Astral

/**
  * Represents main info about the blog, such as blog title, subtitle, author, etc. Blog theme must
  * be specified by the theme name of one of registered [[com.norcane.noble.api.BlogTheme]]
  * instances. As the fields of this class are fixed and the blog user may want to provide
  * additional configuration values (e.g. for template used), all available blog properties are
  * available via the `properties` field.
  *
  * @param title       blog title
  * @param subtitle    blog subtitle (optional)
  * @param authors     blog authors (at least one)
  * @param description short blog description (optional)
  * @param copyright   copyright info (optional)
  * @param themeName   name of the theme to use
  * @param properties  blog properties (includes the values extracted into fields of this class)
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class BlogInfo(title: String, subtitle: Option[String], authors: Seq[BlogAuthor],
                    description: Option[String], copyright: Option[String],
                    themeName: String, properties: Astral)
