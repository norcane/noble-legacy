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
import java.time.{ZoneId, ZonedDateTime}
import java.util.Date
import javax.inject.Singleton

import cats.data.Xor
import com.norcane.noble.api.astral.{Astral, AstralType}
import com.norcane.noble.api.models.BlogPostMeta
import com.norcane.noble.api.{BlogPostRecord, FormatSupport, FormatSupportError, FormatSupportFactory}
import com.norcane.noble.astral.{RawYaml, YamlParser}

import scala.io.Source
import scala.util.{Failure, Success}

@Singleton
class MarkdownFormatSupportFactory extends FormatSupportFactory {

  override def formatName: String = "md"

  override def create: FormatSupport = new MarkdownFormatSupport()
}

class MarkdownFormatSupport extends FormatSupport {

  import com.norcane.noble.utils.MarkdownProcessor.md2html

  private val FrontMatterSeparator: String = "---"

  private implicit class IteratorOps[T](iterator: Iterator[T]) {
    def nextOption = if (iterator.hasNext) Option(iterator.next()) else None
  }

  override def extractPostMetadata(is: InputStream,
                                   record: BlogPostRecord): FormatSupportError Xor BlogPostMeta = {
    for {
      frontMatter <- extractFrontMatter(is, record.title)
      blogPost <- extractBlogPost(frontMatter, record)
    } yield blogPost
  }

  override def extractPostContent(is: InputStream, post: BlogPostMeta,
                                  placeholders: Map[String, Any]): FormatSupportError Xor String = {
    for (content <- extractContent(is, post.title))
      yield markdownToHtml(replaceIn(content, placeholders))
  }

  private def replaceIn(input: String, placeholders: Map[String, Any]): String = {
    placeholders.foldLeft(input) {
      case (tmp, (key, value)) => tmp.replaceAll(placeholder(key), value.toString)
    }
  }

  private def placeholder(name: String): String = s"@@$name@@"

  private def markdownToHtml(input: String): String = md2html(input)

  private def extractContent(is: InputStream, title: String): FormatSupportError Xor String = {
    val lines: Iterator[String] = Source.fromInputStream(is).getLines().dropWhile(_.trim.isEmpty)
    lines.nextOption match {
      case Some(FrontMatterSeparator) =>
        Xor.right(lines.dropWhile(_ != FrontMatterSeparator).drop(1).mkString("\n"))
      case _ => Xor.left(FormatSupportError(s"Cannot extract content for blog post '$title'"))
    }
  }

  private def extractBlogPost(frontMatter: Astral,
                              record: BlogPostRecord): FormatSupportError Xor BlogPostMeta = {

    import Astral.Defaults._
    import MarkdownFormatSupport.zonedDateTimeValue

    val properties: Astral = Astral(
      frontMatter.underlying -- Seq("author", "title", "date", "tags"))
    val title: String = frontMatter.get[String]("title").getOrElse(record.title)
    val date: ZonedDateTime = frontMatter.get[ZonedDateTime]("date").getOrElse(record.date)
    val tags: Set[String] = frontMatter.get[String]("tags").toSet[String]
      .flatMap(_.split(" +").map(_.replace('+', ' ')))

    for {
      author <- Xor.fromOption(frontMatter.get[String]("author"),
        FormatSupportError(s"No author nickname defined for blog post '${record.id}'"))
    } yield BlogPostMeta(record.id, author, record.formatName, title, record.permalinkTitle,
      date, tags, properties)
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

/**
  * Companion object for the [[MarkdownFormatSupport]] class.
  */
object MarkdownFormatSupport {

  /**
    * Provides ''ASTral'' support for reading value of type `ZonedTimeData`.
    */
  implicit val zonedDateTimeValue: AstralType[ZonedDateTime] = AstralType {
    case date: Date => date.toInstant.atZone(ZoneId.of("UTC"))
  }
}
