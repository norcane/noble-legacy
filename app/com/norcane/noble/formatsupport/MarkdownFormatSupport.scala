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

import java.io.InputStream
import java.time.LocalDate
import javax.inject.Singleton

import cats.data.Xor
import com.norcane.noble.api.astral.Astral
import com.norcane.noble.api.models.BlogPost
import com.norcane.noble.api.{BlogPostRecord, FormatSupport, FormatSupportError, FormatSupportFactory}
import com.norcane.noble.astral.{RawYaml, YamlParser}
import org.pegdown.PegDownProcessor

import scala.io.Source
import scala.util.{Failure, Success}

@Singleton
class MarkdownFormatSupportFactory extends FormatSupportFactory {

  override def postType: String = "md"

  override def create: FormatSupport = new MarkdownFormatSupport()
}

class MarkdownFormatSupport extends FormatSupport {

  private val FrontMatterSeparator: String = "---"
  private val processor: PegDownProcessor = new PegDownProcessor()

  private implicit class IteratorOps[T](iterator: Iterator[T]) {
    def nextOption = if (iterator.hasNext) Option(iterator.next()) else None
  }

  override def extractPostMetadata(is: InputStream,
                                   record: BlogPostRecord): FormatSupportError Xor BlogPost = {
    for {
      frontMatter <- extractFrontMatter(is, record.title)
      blogPost <- extractBlogPost(frontMatter, record)
    } yield blogPost
  }

  override def extractPostContent(is: InputStream, post: BlogPost): FormatSupportError Xor String = {
    for (content <- extractContent(is, post.title)) yield markdownToHtml(content)
  }

  private def markdownToHtml(input: String): String = processor.markdownToHtml(input)

  private def extractContent(is: InputStream, title: String): FormatSupportError Xor String = {
    val lines: Iterator[String] = Source.fromInputStream(is).getLines()
      .map(_.trim).dropWhile(_.isEmpty)
    lines.nextOption match {
      case Some(FrontMatterSeparator) =>
        Xor.right(lines.dropWhile(_ != FrontMatterSeparator).drop(1).mkString("\n"))
      case _ => Xor.left(FormatSupportError(s"Cannot extract content for blog post '$title'"))
    }
  }

  private def extractBlogPost(frontMatter: Astral,
                              record: BlogPostRecord): FormatSupportError Xor BlogPost = {

    import Astral.Defaults._

    val properties: Astral = Astral(frontMatter.underlying -- Seq("title", "date", "tags"))
    val title: String = frontMatter.get[String]("title").getOrElse(record.title)
    val date: LocalDate = frontMatter.get[LocalDate]("date").getOrElse(record.date)
    val tags: Set[String] = frontMatter.get[String]("tags").toSet[String]
      .flatMap(_.split(" +").map(_.replace('+', ' ')))

    Xor.right(BlogPost(record.id, record.postType, title, date, tags, properties))
  }

  private def extractFrontMatter(is: InputStream, title: String): FormatSupportError Xor Astral = {
    implicit val yamlParser: YamlParser = YamlParser.parser

    val lines: Iterator[String] = Source.fromInputStream(is).getLines()
      .map(_.trim).dropWhile(_.isEmpty)

    lines.nextOption match {
      case Some(FrontMatterSeparator) =>
        val frontMatter: String = lines.takeWhile(_ != FrontMatterSeparator).mkString("\n")
        Astral.parse(RawYaml(frontMatter)) match {
          case Success(yaml) => Xor.right(yaml)
          case Failure(th) =>
            Xor.left(FormatSupportError("Cannot parse YAML front matter", Some(th)))
        }
      case _ => Xor.left(FormatSupportError(s"Missing YAML front matter in blog post '$title'"))
    }
  }
}
