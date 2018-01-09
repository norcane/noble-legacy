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

package com.norcane.noble.api.models

import java.time.ZonedDateTime

import com.norcane.noble.api.astral.Astral
import play.utils.UriEncoding

/**
  * Represents the single post metadata (without the loaded content). Each blog post must define
  * its unique id, used later to fetch the blog post content (e.g. for file-based blog storages, the
  * blog post filename can be used). ''Format'' of the blog post must be specified using the name
  * of one of the registered [[com.norcane.noble.api.FormatSupport]] instances. Additional config
  * values can be provided (e.g. for the used theme) using the `properties` field.
  *
  * @param id         unique identifier of the blog post
  * @param author     nickname of the blog post author
  * @param format     used format
  * @param title      title of the blog post
  * @param date       date of the blog post
  * @param tags       set of blog post tags
  * @param properties additional blog post properties
  * @see BlogPostMeta
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class BlogPostMeta(id: String, author: String, format: String, title: String,
                        permalinkTitle: String, date: ZonedDateTime, tags: Set[String],
                        properties: Astral) {

  def permalink: String = "/%04d/%02d/%02d/%s".format(
    date.getYear, date.getMonthValue, date.getDayOfMonth,
    UriEncoding.encodePathSegment(permalinkTitle, "UTF-8"))

}

/**
  * Companion object for the [[BlogPostMeta]] class.
  */
object BlogPostMeta {
  implicit val ordering = Ordering.by((post: BlogPostMeta) => post.date.toEpochSecond)
}