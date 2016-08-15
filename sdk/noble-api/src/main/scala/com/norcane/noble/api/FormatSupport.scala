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

import java.io.InputStream
import java.time.LocalDate

import cats.data.Xor
import com.norcane.noble.api.models.BlogPostMeta


trait FormatSupportFactory {
  def postType: String

  def create: FormatSupport
}

trait FormatSupport {

  def extractPostMetadata(is: InputStream, record: BlogPostRecord): FormatSupportError Xor BlogPostMeta

  def extractPostContent(is: InputStream, post: BlogPostMeta): FormatSupportError Xor String

}

case class FormatSupportError(message: String, cause: Option[Throwable] = None)

case class BlogPostRecord(id: String, date: LocalDate, title: String, permalinkTitle: String,
                          postType: String)