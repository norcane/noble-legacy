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

package com.norcane.noble.api

import com.norcane.noble.api.models.{BlogPostMeta, Page}
import controllers.AssetsFinder
import play.api.mvc.Call
import play.utils.UriEncoding

/**
  * ''Play reverse router'', used to generate a URL from within a Scala call for specified blog
  * action.
  *
  * @param path             blog path
  * @param globalAssetsPath global assets (i.e. Play's assets) path
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class BlogReverseRouter(assetsFinder: AssetsFinder, path: => String, globalAssetsPath: => String) {

  /**
    * Creates call to blog index page, which displays all published blogposts (paginated). If no
    * `page` param specifying the pagination settings is provided,
    * [[com.norcane.noble.api.models.Page.Default]] is used.
    *
    * @param page page specifying the pagination settings (e.g. page number and number of blog posts
    *             per page)
    * @return call to blog index page
    */
  def index(page: Page = Page.Default): Call = Call("GET", withPaging(s"$path/", page))

  /**
    * Creates call to blog post page, which displays the full content for blog posts specified by
    * its metadata.
    *
    * @param blogPost blog post metadata
    * @return call to blog post page
    */
  def blogPost(blogPost: BlogPostMeta): Call = Call("GET", path + blogPost.permalink)

  /**
    * Creates call to page, which displays all blog posts for specified tag (paginated). If no
    * `page` param specifying the pagination settings is provided,
    * [[com.norcane.noble.api.models.Page.Default]] is used.
    *
    * @param name name of the tag
    * @param page page specifying the pagination settings (e.g. page number and number of blog posts
    *             per page)
    * @return call to page displaying all blog posts for specified tag
    */
  def tag(name: String, page: Page = Page.Default): Call =
    Call("GET", withPaging(s"$path/tags/${encode(name)}", page))

  /**
    * Creates call to page, which displays all blog posts published by author, specified by his/her
    * `ID` (paginated). If no `page` param specifying the pagination settings is provided,
    * [[com.norcane.noble.api.models.Page.Default]] is used.
    *
    * @param authorId unique ID of the author (i.e. nickname)
    * @param page     page specifying the pagination settings (e.g. page number and number of blog
    *                 posts per page)
    * @return call to page displaying all blog posts by author
    */
  def author(authorId: String, page: Page = Page.Default): Call =
    Call("GET", withPaging(s"$path/author/${encode(authorId)}", page))

  /**
    * Creates call to page, which displays all blog posts published in specified year (paginated).
    * If no `page` param specifying the pagination settings is provided,
    * [[com.norcane.noble.api.models.Page.Default]] is used.
    *
    * @param year year (e.g. 1984)
    * @param page page specifying the pagination settings (e.g. page number and number of blog posts
    *             per page)
    * @return call to page displaying all blog posts published in specified year
    */
  def year(year: Int, page: Page = Page.Default): Call =
    Call("GET", withPaging(s"$path/$year", page))

  /**
    * Creates call to page, which displays all blog posts published in specified month and year
    * (paginated). If no `page` param specifying the pagination settings is provided,
    * [[com.norcane.noble.api.models.Page.Default]] is used.
    *
    * @param year  year (e.g. 1984)
    * @param month month (as number from 1 to 12)
    * @param page  page specifying the pagination settings (e.g. page number and number of blog
    *              posts per page)
    * @return call to page displaying all blog posts published in specified year
    */
  def month(year: Int, month: Int, page: Page = Page.Default) =
    Call("GET", withPaging(f"$path%s/$year%d/$month%02d", page))

  /**
    * Creates call to page, which displays all blog posts published in specified day, month and year
    * (paginated). If no `page` param specifying the pagination settings is provided,
    * [[com.norcane.noble.api.models.Page.Default]] is used.
    *
    * @param year  year (e.g. 1984)
    * @param month month (as number from 1 to 12)
    * @param day   day (as number from 1 to 31)
    * @param page  page specifying the pagination settings (e.g. page number and number of blog
    *              posts per page)
    * @return call to page displaying all blog posts published in specified year
    */
  def day(year: Int, month: Int, day: Int, page: Page = Page.Default) =
    Call("GET", withPaging(f"$path%s/$year%d/$month%02d/$day%02d", page))

  /**
    * Creates call to blog asset file, specified by its path.
    *
    * @param filePath path of the blog asset file
    * @return call to blog asset file
    */
  def asset(filePath: String): Call = Call("GET", s"$path/blog-assets/$filePath")

  /**
    * Creates call to static page, specified by its permanent link.
    *
    * @param permalink permanent link of the static page
    * @return call to static page
    */
  def page(permalink: String): Call = Call("GET", s"$path/$permalink")

  /**
    * Creates call to Atom RSS feed.
    *
    * @return call to Atom RSS feed
    */
  def atom: Call = Call("GET", s"$path/atom.xml")

  /**
    * Creates call to page, which triggers the blog reloading. The same `reloadToken` as the one
    * defined in blog configuration must be provided, otherwise the action will fail.
    *
    * @param reloadToken blog reload token
    * @return call to page triggering the blog reloading
    */
  def reload(reloadToken: String): Call = Call("POST", s"$path/reload/$reloadToken")

  /**
    * Creates call to ''WebJAR'' asset file, specified by its path.
    *
    * @param filePath path of the ''WebJAR'' asset file
    * @return call to ''WebJAR'' asset file
    */
  def webJarAsset(filePath: String): Call = {
    val assetPath = assetsFinder.findAssetPath("/public/lib", "/public/lib/" + filePath)
    Call("GET", s"$globalAssetsPath/lib/$assetPath")
  }

  private def withPaging(path: String, page: Page) = {
    val queryString: String = (Nil ++
      (if (page.pageNo != Page.DefaultPageNo) Seq("page=" + page.pageNo) else Nil) ++
      (if (page.perPage != Page.DefaultPageSize) Seq("per-page=" + page.perPage) else Nil)).mkString("&")
    if (queryString.isEmpty) path else path + "?" + queryString
  }

  private def encode(s: String) = UriEncoding.encodePathSegment(s, "UTF-8")
}
