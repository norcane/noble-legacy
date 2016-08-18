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


trait BlogThemeFactory {
  def name: String

  def create: BlogTheme
}

trait BlogTheme {

  def name: String

  def blogPosts(blog: Blog, router: BlogReverseRouter, title: Option[String],
                posts: Seq[BlogPost], pagination: Pagination)
               (implicit header: RequestHeader, messages: Messages): Html

  def blogPost(blog: Blog, router: BlogReverseRouter, post: BlogPost)
              (implicit header: RequestHeader, messages: Messages): Html
}
