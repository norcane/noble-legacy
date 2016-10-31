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
import java.time.ZonedDateTime

import com.norcane.noble.api.models.BlogPostMeta


/**
  * Factory class used by the ''Noble'' to create new instance of selected [[FormatSupport]] class.
  * The field `formatName` represents the unique name of the content format this particular
  * implementation supports (e.g. `md` for ''Markdown'') and is refered from blog posts and static
  * pages to let ''Noble'' know which format support should be selected for it.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait FormatSupportFactory {

  /**
    * Unique name of the format this concrete implementation provides support for (for example
    * `md` for ''Markdown'').
    *
    * @return name of the format
    */
  def formatName: String

  /**
    * Creates new instance of the [[FormatSupport]] class.
    *
    * @return new instance of the [[FormatSupport]] class
    */
  def create: FormatSupport
}

/**
  * Represents the support for specific blog post/static page format, such as ''Markdown'', ''Wiki''
  * syntax, etc. Main goal of this class is to extract and parse blog post content and metadata
  * from raw data stream of supported type (e.g. ''Markdown'' text) and transform it into HTML
  * output.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait FormatSupport {

  /**
    * Extracts the blog post metadata from the blog post content input stream and ''raw'' metadata,
    * as provided by the blog storage. This method can simply transform and return the raw metadata,
    * however, it provides possibility to load additional metadata for example from the blog post
    * content stream (if metadata are part of it).
    *
    * @param is     blog post content stream
    * @param record blog post ''raw'' metadata
    * @return extracted blog post metadata, or error detail in case of failure
    */
  def extractPostMetadata(is: InputStream, record: BlogPostRecord): Either[FormatSupportError, BlogPostMeta]

  /**
    * Extracts and parses the blog post content from raw stream. Output of this method should always
    * be the string with parsed HTML blog post content. Note that implementation of this method
    * must replace all placeholders with their actual values, using the given `placeholders` map
    * (as placeholder name -> actual placeholder value). Actual placeholder name syntax
    * (e.g. `@@foo@@` or `{{foo}}`) is left on this implementation.
    *
    * @param is           blog post content stream
    * @param post         blog post metadata
    * @param placeholders text placeholders (as map of their names and actual values to
    *                     replace placeholders with)
    * @return extracted and parsed blog post content (in HTML format)
    */
  def extractPostContent(is: InputStream, post: BlogPostMeta,
                         placeholders: Map[String, Any]): Either[FormatSupportError, String]

}

/**
  * Error indicating failure occured during parsing the input data in the [[FormatSupport]] class.
  *
  * @param message error message
  * @param cause   error cause (optional)
  */
case class FormatSupportError(message: String, cause: Option[Throwable] = None)

/**
  * Represents ''raw'' blog post metadata, as directly provided by the storage. Those values might
  * be overwritten by new values, extracted from the blog post stream (for example in case that
  * blog post medatata are part of the blog post document content).
  *
  * @param id             unique ID of the blog post
  * @param date           date when the blog post was published
  * @param title          blog post title
  * @param permalinkTitle permaling title (i.e. title used in URI)
  * @param formatName     format of the blog post
  */
case class BlogPostRecord(id: String, date: ZonedDateTime, title: String, permalinkTitle: String,
                          formatName: String)