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

package com.norcane.noble.api.models

import com.norcane.noble.api.models.dates.{Day, Month, Year}

import scala.collection.immutable.SortedMap

class Blog(val hash: String, val info: BlogInfo, blogPosts: Seq[BlogPostMeta]) {

  private val sorted: Seq[BlogPostMeta] = blogPosts.sorted.reverse

  private val years: Seq[Year] = sorted.groupBy(_.date.getYear).map { byYear =>
    val (year, yearPosts) = byYear

    val months: SortedMap[Int, Month] =
      SortedMap.empty[Int, Month] ++ yearPosts.groupBy(_.date.getMonthValue).map { byMonth =>
        val (month, monthPosts) = byMonth

        val days: SortedMap[Int, Day] =
          SortedMap.empty ++ monthPosts.groupBy(_.date.getDayOfMonth).map { byDay =>
            val (day, dayPosts) = byDay

            day -> Day(year, month, day, dayPosts)
          }

        month -> Month(year, month, days, monthPosts)
      }

    Year(year, months, yearPosts)
  }.toSeq.sorted

  def posts: Seq[BlogPostMeta] = sorted

  def forYear(year: Int): Year = years.find(_.year == year).getOrElse(Year.empty(year))
}
