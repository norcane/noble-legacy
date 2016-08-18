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

package com.norcane.noble.api

import com.norcane.noble.api.models.{BlogPostMeta, Page}
import controllers.Assets
import play.api.mvc.Call
import play.core.routing.ReverseRouteContext
import play.utils.UriEncoding

class BlogReverseRouter(path: => String, globalAssetsPath: => String) {

  val defaultPage = Page(1, 5)

  def index(page: Page = defaultPage): Call = Call("GET", withPaging(s"$path/", page))

  def blogPost(blogPost: BlogPostMeta): Call = Call("GET", path + blogPost.permalink)

  def tag(name: String, page: Page = defaultPage): Call =
    Call("GET", withPaging(s"$path/tags/${encode(name)}", page))

  def year(year: Int, page: Page = defaultPage): Call =
    Call("GET", withPaging(s"$path/$year", page))

  def month(year: Int, month: Int, page: Page = defaultPage) =
    Call("GET", withPaging(f"$path%s/$year%d/$month%02d", page))

  def day(year: Int, month: Int, day: Int, page: Page = defaultPage) =
    Call("GET", withPaging(f"$path%s/$year%d/$month%02d/$day%02d", page))

  def asset(file: String): Call = Call("GET", s"$path/assets/$file")

  def webJarAsset(file: String): Call = {
    val path: String = Assets.Asset
      .assetPathBindable(ReverseRouteContext(Map("path" -> "/public/lib")))
      .unbind("file", Assets.Asset(file))
    Call("GET", s"$globalAssetsPath/lib/$path")
  }

  private def withPaging(path: String, page: Page) = {
    val queryString: String = (Nil ++
      (if (page.pageNo != 1) Seq("page=" + page.pageNo) else Nil) ++
      (if (page.perPage != 5) Seq("per-page=" + page.perPage) else Nil)).mkString("&")
    if (queryString.isEmpty) path else path + "?" + queryString
  }

  private def encode(s: String) = UriEncoding.encodePathSegment(s, "UTF-8")
}
