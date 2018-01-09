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

import scala.collection.immutable.SortedMap

/**
  * Represents collection of all blog posts, published in a particular year.
  *
  * @param year   year of publication
  * @param months blog posts sorted by months
  * @param posts  collection of blog posts
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class Year(year: Int, months: SortedMap[Int, Month], posts: Seq[BlogPostMeta]) {

  /**
    * Returns collection of all blog posts, published in a particular year and month.
    *
    * @param month a month (in the range 1 to 12)
    * @return collection of blog posts
    */
  def forMonth(month: Int): Month = months.getOrElse(month, Month(year, month, SortedMap.empty, Nil))
}

/**
  * Companion object for the [[Year]] class.
  */
object Year {
  implicit val ordering: Ordering[Year] = Ordering.by((year: Year) => year.year)

  /**
    * Returns new instance of [[Year]] class with no blog posts set.
    *
    * @param year year for which create the instance
    * @return newly created instance
    */
  def empty(year: Int): Year = Year(year, SortedMap.empty, Nil)
}
