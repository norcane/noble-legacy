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

/**
  * Represents the single blog. Each blog consists of unique version ID, blog info and collection of
  * blog posts. Blog version ID is immutable and is used during blog reloading to compare whether
  * any part of the blog (blog posts, configuration) has changed from actual state. Blog info
  * represents further details about the blog, such as blog title, used theme, author(s), etc.
  * Collection of blog posts holds loaded metadata for all blog posts available for this blog.
  *
  * @param versionId unique version ID of the current blog instance
  * @param info      blog info (e.g. title, used theme)
  * @param blogPosts collection of all blog's posts (as metadata only)
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class Blog(val versionId: String, val info: BlogInfo, blogPosts: Seq[BlogPostMeta]) {

  /**
    * Sorted collection of all blog posts (metadata only).
    */
  val posts: Seq[BlogPostMeta] = blogPosts.sorted.reverse

  /**
    * Collection of blog posts, sorted by years of publishing.
    */
  val years: Seq[Year] = posts.groupBy(_.date.getYear).map { byYear =>
    val (year, yearPosts) = byYear

    val months: SortedMap[Int, Month] =
      SortedMap.empty[Int, Month] ++ yearPosts.groupBy(_.date.getMonthValue).map { byMonth =>
        val (month, monthPosts) = byMonth

        val days: SortedMap[Int, Day] =
          SortedMap.empty[Int, Day] ++ monthPosts.groupBy(_.date.getDayOfMonth).map { byDay =>
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
  val tags: Map[String, Seq[BlogPostMeta]] = posts
    .flatMap(postMeta => postMeta.tags map (_ -> postMeta))
    .groupBy(_._1).mapValues(_ map (_._2))

  /**
    * Sequence of tags, represented by the [[Tag]] object. The tag weight is calculated for each tag
    * and its represented as an integer number from 1 to 10, where 1 is the lowest value.
    */
  val tagCloud: Seq[Tag] = {
    val rankFactor: Double = Math.max(1.0, 10.0 / tags.map(_._2.size).fold(0)(Math.max))

    tags.map {
      case (tag, postsMeta) => Tag(tag, postsMeta.size, Math.ceil(rankFactor * postsMeta.size).toInt)
    }.toSeq.sortBy(_.name)
  }

  /**
    * Returns collection of all blog posts, published in the specified year.
    *
    * @param year year of publication
    * @return collection of all blog posts, published in the specified year
    */
  def forYear(year: Int): Year = years.find(_.year == year).getOrElse(Year.empty(year))

  /**
    * Returns collection of all blog posts for specified tag.
    *
    * @param name name of the tag
    * @return collection of all blog posts for specified tag
    */
  def forTag(name: String): Option[Seq[BlogPostMeta]] = tags.get(name)
}
