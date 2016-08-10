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

package com.norcane.noble.themes

import javax.inject.Singleton

import com.norcane.noble.api.astral.Astral
import com.norcane.noble.api.models.{Blog, BlogPost}
import com.norcane.noble.api.{BlogReverseRouter, BlogTheme, BlogThemeFactory}
import com.norcane.noble.themes.HumaneTheme.HumaneProps
import play.api.i18n.Messages
import play.api.mvc.{Call, RequestHeader}
import play.twirl.api.Html

@Singleton
class HumaneThemeFactory extends BlogThemeFactory {
  override def name: String = HumaneTheme.ThemeName

  override def create: BlogTheme = new HumaneTheme
}


class HumaneTheme extends BlogTheme {

  import com.norcane.noble.utils.MarkdownProcessor.md2html

  override def name: String = HumaneTheme.ThemeName

  override def blogPosts(blog: Blog, router: BlogReverseRouter, title: Option[String],
                         posts: Seq[(BlogPost, String)], previous: Option[Call], next: Option[Call])
                        (implicit header: RequestHeader, messages: Messages): Html = {

    import Astral.Defaults._

    val propsOpt: Option[Astral] = blog.info.properties.get[Astral]("humane")

    // author properties
    val authorBio: Option[String] = propsOpt flatMap (_.get[String]("author/bio"))
    val portraitPath: Option[String] = propsOpt flatMap (_.get[String]("author/portrait"))
    val humaneProps: HumaneProps = HumaneProps(
      authorBio = authorBio map md2html,
      portraitPath = portraitPath)

    com.norcane.noble.themes.humane.html.blogPosts(blog, router, posts, humaneProps)
  }


  override def blogPost(blog: Blog, router: BlogReverseRouter, post: BlogPost, content: String)
                       (implicit header: RequestHeader, messages: Messages): Html = ???
}

object HumaneTheme {
  val ThemeName: String = "humane"

  case class HumaneProps(authorBio: Option[String], portraitPath: Option[String])

}
