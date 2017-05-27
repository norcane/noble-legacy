/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016-2017 norcane
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

import javax.inject.{Inject, Singleton}

import com.norcane.noble.api.astral.Astral
import com.norcane.noble.api.models._
import com.norcane.noble.api.{BlogReverseRouter, BlogTheme, BlogThemeFactory}
import com.norcane.noble.services.MarkdownService
import com.norcane.noble.themes.HumaneTheme.{HumaneProps, Toolbar}
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.twirl.api.Html

/**
  * Factory for the [[HumaneTheme]]. Used by ''Guice'' to get and create the instance with
  * all required dependencies.
  *
  * @param markdownService ''markdown'' service reference
  */
@Singleton
class HumaneThemeFactory @Inject()(markdownService: MarkdownService) extends BlogThemeFactory {
  override def name: String = HumaneTheme.ThemeName

  override def create: BlogTheme = new HumaneTheme(markdownService)
}

/**
  * ''Humane'', default theme bundled with ''noble''.
  *
  * @param markdownService ''markdown'' service reference
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class HumaneTheme(markdownService: MarkdownService) extends BlogTheme {

  override def name: String = HumaneTheme.ThemeName

  override def blogPosts(blog: Blog, router: BlogReverseRouter, title: Option[String],
                         posts: Seq[BlogPost], pagination: Pagination, filter: Option[PostsFilter])
                        (implicit header: RequestHeader, messages: Messages): Html = {

    val author = singleAuthor(blog)
    com.norcane.noble.themes.humane.html.blogPosts(
      blog, router, title, posts, pagination,
      HumaneProps(author, author.flatMap(aboutPage), Toolbar()))
  }

  override def blogPost(blog: Blog, router: BlogReverseRouter, post: BlogPost)
                       (implicit header: RequestHeader, messages: Messages): Html = {

    com.norcane.noble.themes.humane.html.blogPost(blog, router, post,
      HumaneProps(Some(post.author), aboutPage(post.author), Toolbar(displayOnBottom = false)))
  }


  override def page(blog: Blog, router: BlogReverseRouter, page: StaticPage)
                   (implicit header: RequestHeader, messages: Messages): Html = {

    com.norcane.noble.themes.humane.html.page(blog, router, page,
      HumaneProps(None, None, Toolbar(displayOnBottom = false)))
  }

  override def notFound(blog: Blog, router: BlogReverseRouter)
                       (implicit header: RequestHeader, messages: Messages): Html = {

    val author = singleAuthor(blog)
    com.norcane.noble.themes.humane.html.notFound(blog, router,
      HumaneProps(author, author.flatMap(aboutPage), Toolbar(displayOnBottom = false)))
  }

  private def singleAuthor(blog: Blog): Option[BlogAuthor] = for {
    author <- blog.info.authors.headOption if blog.info.authors.size == 1
  } yield author.copy(biography = author.biography map markdownService.parseToHtml)

  private def aboutPage(author: BlogAuthor): Option[String] = {
    import Astral.Defaults._

    author.properties.get[String]("about-page")
  }

}

object HumaneTheme {

  val ThemeName: String = "humane"

  case class HumaneProps(singleAuthor: Option[BlogAuthor], aboutPage: Option[String], toolbar: Toolbar)

  case class Toolbar(displayOnTop: Boolean = true, displayOnBottom: Boolean = true)

}
