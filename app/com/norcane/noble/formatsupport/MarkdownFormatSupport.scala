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

package com.norcane.noble.formatsupport

import java.io.InputStream
import java.time.{ZoneId, ZonedDateTime}
import java.util.Date
import javax.inject.{Inject, Singleton}

import cats.syntax.either._
import com.norcane.noble.api._
import com.norcane.noble.api.astral.{Astral, AstralType}
import com.norcane.noble.api.models.{BlogPostMeta, StaticPageMeta}
import com.norcane.noble.astral.{RawYaml, YamlParser}
import com.norcane.noble.services.MarkdownService

import scala.io.{Codec, Source}
import scala.util.{Failure, Success}

/**
  * Factory for the [[MarkdownFormatSupport]]. Used by ''Guice'' to get and create the instance with
  * all required dependencies.
  *
  * @param markdownService ''markdown'' service reference
  */
@Singleton
class MarkdownFormatSupportFactory @Inject()(markdownService: MarkdownService)
  extends FormatSupportFactory {

  override def formatName: String = "md"

  override def create: FormatSupport = new MarkdownFormatSupport(markdownService)
}

/**
  * Provides ''format support'' for ''Markdown'' documents. By default, this ''format support''
  * will be used for all files that uses the `md` as their format name.
  *
  * @param markdownService ''markdown'' service reference
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
class MarkdownFormatSupport(markdownService: MarkdownService) extends FormatSupport {

  private val FrontMatterSeparator: String = "---"

  private implicit class IteratorOps[T](iterator: Iterator[T]) {
    def nextOption: Option[T] = if (iterator.hasNext) Option(iterator.next()) else None
  }

  override def extractPostMetadata(is: InputStream, record: BlogPostRecord
                                  ): Either[FormatSupportError, BlogPostMeta] = {
    for {
      frontMatter <- extractFrontMatter(is, record.title)
      blogPost <- extractBlogPost(frontMatter, record)
    } yield blogPost
  }

  override def extractPostContent(is: InputStream, post: BlogPostMeta, placeholders: Map[String, Any]
                                 ): Either[FormatSupportError, String] = {
    for (content <- extractContent(is, post.title))
      yield markdownToHtml(replaceIn(content, placeholders))
  }


  override def extractPageMetadata(is: InputStream, record: StaticPageRecord
                                  ): Either[FormatSupportError, StaticPageMeta] = {
    for {
      frontMatter <- extractFrontMatter(is, record.permalink)
      staticPage <- extractStaticPage(frontMatter, record)
    } yield staticPage
  }

  override def extractPageContent(is: InputStream, page: StaticPageMeta,
                                  placeholders: Map[String, Any]): Either[FormatSupportError, String] = {
    for (content <- extractContent(is, page.title))
      yield markdownToHtml(replaceIn(content, placeholders))
  }

  private def replaceIn(input: String, placeholders: Map[String, Any]): String = {
    placeholders.foldLeft(input) {
      case (tmp, (key, value)) => tmp.replaceAll(placeholder(key), value.toString)
    }
  }

  private def placeholder(name: String): String = s"@@$name@@"

  private def markdownToHtml(input: String): String = markdownService.parseToHtml(input)

  private def extractContent(is: InputStream, title: String): Either[FormatSupportError, String] = {
    val lines = Source.fromInputStream(is)(Codec.UTF8).getLines().dropWhile(_.trim.isEmpty)
    lines.nextOption match {
      case Some(FrontMatterSeparator) =>
        Right(lines.dropWhile(_ != FrontMatterSeparator).drop(1).mkString("\n"))
      case _ => Left(FormatSupportError(s"Cannot extract content for document '$title'"))
    }
  }

  private def extractBlogPost(frontMatter: Astral,
                              record: BlogPostRecord): Either[FormatSupportError, BlogPostMeta] = {

    import Astral.Defaults._
    import MarkdownFormatSupport.zonedDateTimeValue

    val properties = Astral(frontMatter.underlying -- Seq("author", "title", "date", "tags"))
    val title = frontMatter.get[String]("title").getOrElse(record.title)
    val date = frontMatter.get[ZonedDateTime]("date").getOrElse(record.date)
    val tags = frontMatter.get[String]("tags").toSet[String]
      .flatMap(_.split(" +").map(_.replace('+', ' ')))

    for {
      author <- Either.fromOption(frontMatter.get[String]("author"),
        FormatSupportError(s"No author nickname defined for blog post '${record.id}'"))
    } yield BlogPostMeta(record.id, author, record.formatName, title, record.permalinkTitle,
      date, tags, properties)
  }

  private def extractStaticPage(frontMatter: Astral,
                                record: StaticPageRecord): Either[FormatSupportError, StaticPageMeta] = {
    import Astral.Defaults._

    val properties = Astral(frontMatter.underlying -- Seq("author", "permalink", "title"))
    val title = frontMatter.get[String]("title").getOrElse(record.permalink)
    val permalink = frontMatter.get[String]("permalink").getOrElse(record.permalink)

    Right(StaticPageMeta(record.id, permalink, title, record.formatName, properties))
  }

  private def extractFrontMatter(is: InputStream, title: String
                                ): Either[FormatSupportError, Astral] = {

    implicit val yamlParser = YamlParser.parser

    val lines = Source.fromInputStream(is)(Codec.UTF8).getLines().map(_.trim).dropWhile(_.isEmpty)

    lines.nextOption match {
      case Some(FrontMatterSeparator) =>
        val frontMatter = lines.takeWhile(_ != FrontMatterSeparator).mkString("\n")
        Astral.parse(RawYaml(frontMatter)) match {
          case Success(yaml) => Right(yaml)
          case Failure(th) => Left(FormatSupportError("Cannot parse YAML front matter", Some(th)))
        }
      case _ => Left(FormatSupportError(s"Missing YAML front matter in document '$title'"))
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
