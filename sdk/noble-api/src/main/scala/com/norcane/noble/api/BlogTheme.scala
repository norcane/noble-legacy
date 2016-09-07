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

import com.norcane.noble.api.models.{Blog, BlogPost, Pagination}
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.twirl.api.Html

/**
  * Factory class used by the ''Noble'' to create instance of the [[BlogTheme]] implementation. The
  * `name` represents unique blog theme name, and is used in individual blog posts to refer the
  * used blog theme. For further details about creating custom blog theme, please refer the official
  * project documentation.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait BlogThemeFactory {

  /**
    * Returns unique blog theme name.
    *
    * @return unique blog theme name
    */
  def name: String

  /**
    * Creates new instance of [[BlogTheme]] class.
    *
    * @return instance of [[BlogTheme]] class
    */
  def create: BlogTheme
}

/**
  * Represents the blog theme. Any blog theme consists of method `name`, returning the unique
  * theme name and several methods returning different HTML content for different contexts.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait BlogTheme {

  /**
    * Returns unique blog theme name.
    *
    * @return unique blog theme name
    */
  def name: String

  /**
    * Renders HTML content for page displaying collection of blog posts (for example index page,
    * blog posts for specific tag or date). As ''Noble'' is based on ''Play framework'', using the
    * ''Twirl'' templates is the easiest way of achieving this. Because displaying of blog post
    * collection is paginated, additional `pagination` instance is provided, allowing simple
    * navigation in available pages.
    *
    * @param blog       blog instance
    * @param router     blog reverse router
    * @param title      page title (optional)
    * @param posts      collection of blog posts
    * @param pagination pagination object allowing simple navigation in available pages
    * @param header     request header
    * @param messages   localization messages
    * @return HTML content
    */
  def blogPosts(blog: Blog, router: BlogReverseRouter, title: Option[String],
                posts: Seq[BlogPost], pagination: Pagination)
               (implicit header: RequestHeader, messages: Messages): Html

  /**
    * Renders HTML content for selected single blog post.
    *
    * @param blog     blog instance
    * @param router   blog reverse router
    * @param post     blog post to render
    * @param header   request header
    * @param messages localization messages
    * @return HTML content
    */
  def blogPost(blog: Blog, router: BlogReverseRouter, post: BlogPost)
              (implicit header: RequestHeader, messages: Messages): Html

  /**
    * Renders HTML content for ''404 not found'' page.
    *
    * @param blog     blog instance
    * @param router   blog reverse router
    * @param header   request header
    * @param messages localization messages
    * @return HTML content
    */
  def notFound(blog: Blog, router: BlogReverseRouter)
              (implicit header: RequestHeader, messages: Messages): Html
}
