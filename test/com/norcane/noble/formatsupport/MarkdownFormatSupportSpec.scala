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

package com.norcane.noble.formatsupport

import java.time.{LocalDate, ZoneId, ZonedDateTime}

import cats.syntax.either._
import com.norcane.noble.api.astral.Astral
import com.norcane.noble.api.models.BlogPostMeta
import com.norcane.noble.api.{BlogPostRecord, FormatSupportError}
import org.specs2.matcher.Matchers
import org.specs2.mutable


/**
  * ''Specs2'' specification for the [[MarkdownFormatSupport]].
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class MarkdownFormatSupportSpec extends mutable.Specification with Matchers {

  private val testBlogPostPath: String = "/2016-07-26-test-article.md"
  private val formatSupport: MarkdownFormatSupport = new MarkdownFormatSupport()
  private val testDate: ZonedDateTime = LocalDate.of(2016, 7, 26).atStartOfDay(ZoneId.of("UTC"))
  private val testTitle: String = "test-article"
  private val metadata: Either[FormatSupportError, BlogPostMeta] = formatSupport.extractPostMetadata(
    getClass.getResourceAsStream(testBlogPostPath),
    BlogPostRecord("2016-07-26-test-article.md", testDate, testTitle, testTitle, "md")
  )
  private val testBlogPost: BlogPostMeta = BlogPostMeta(
    id = "2016-07-26-test-article.md",
    author = "john.smith",
    format = "md",
    title = "Test blog post title",
    permalinkTitle = testTitle,
    date = testDate,
    tags = Set("first", "second", "multi word"),
    Astral(Map[String, Any]("testProp" -> 42))
  )

  "The Markdown format support should" >> {

    "properly extract YAML front matter" >> {
      metadata.isRight must beTrue
    }

    "properly extract blog metadata from YAML front matter" >> {
      metadata.toOption.get must beEqualTo(testBlogPost)
    }
  }

}
