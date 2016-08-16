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

package com.norcane.noble.api.models.dates

import com.norcane.noble.api.models.BlogPostMeta

import scala.collection.immutable.SortedMap

/**
  * Represents collection of blog posts, published in a particular month.
  *
  * @param year  year of publication
  * @param month month of publication (in the range 1 to 12)
  * @param days  blog posts sorted by days
  * @param posts collection of blog posts
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class Month(year: Int, month: Int, days: SortedMap[Int, Day], posts: Seq[BlogPostMeta]) {

  val orderKey: Int = year * 12 + month

  /**
    * Returns collection of all blog posts, published in a particular day.
    *
    * @param day a day of month (in the range of 1 to 31)
    * @return collection of blog posts
    */
  def forDay(day: Int): Day = days.getOrElse(day, Day(year, month, day, Nil))
}

/**
  * Companion object for the [[Month]] class.
  */
object Month {
  implicit val ordering: Ordering[Month] = Ordering.by((month: Month) => month.orderKey)
}