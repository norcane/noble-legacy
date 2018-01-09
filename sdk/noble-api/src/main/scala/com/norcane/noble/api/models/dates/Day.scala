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

package com.norcane.noble.api.models.dates

import com.norcane.noble.api.models.BlogPostMeta

/**
  * Represents collection of all blog posts, published in a particular day.
  *
  * @param year  year of publication
  * @param month month of publication (in the range 1 to 12)
  * @param day   day of publication (in the range 1 to 31)
  * @param posts collection of blog posts
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class Day(year: Int, month: Int, day: Int, posts: Seq[BlogPostMeta]) {

  /**
    * Returns the blog post for specific unique permalink title.
    *
    * @param permalinkTitle unique permalink title
    * @return blog post (if found)
    */
  def forPermalink(permalinkTitle: String): Option[BlogPostMeta] =
    posts find (_.permalinkTitle equalsIgnoreCase permalinkTitle)

  val orderKey: Int = year * 400 + month * 31 + day
}

/**
  * Companion object for the [[Day]] class.
  */
object Day {
  implicit val ordering: Ordering[Day] = Ordering.by((day: Day) => day.orderKey)
}
