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

  /**
    * Represents the map of tags, where key is the tag name and value collection of blog posts
    * for the tag.
    */
  val tags: Map[String, Seq[BlogPostMeta]] = sorted
    .flatMap(postMeta => postMeta.tags map (_ -> postMeta))
    .groupBy(_._1).mapValues(_ map (_._2))

  /**
    * Sequence of tags, represented by the [[Tag]] object. The tag weight is calculated for each tag
    * and its represented as an integer number from 1 to 10, where 1 is the lowest value.
    */
  val tagCloud: Seq[Tag] = {
    val rankFactor: Double = Math.max(1.0, 10.0 / tags.map(_._2.size).fold(0)(Math.max))

    tags.map {
      case (tag, posts) => Tag(tag, posts.size, Math.ceil(rankFactor * posts.size).toInt)
    }.toSeq.sortBy(_.name)
  }

  def posts: Seq[BlogPostMeta] = sorted

  def forYear(year: Int): Year = years.find(_.year == year).getOrElse(Year.empty(year))

  def forTag(name: String): Option[Seq[BlogPostMeta]] = tags.get(name)
}
