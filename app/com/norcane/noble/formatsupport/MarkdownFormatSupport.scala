package com.norcane.noble.formatsupport

import java.io.InputStream
import java.time.LocalDate
import javax.inject.Singleton

import cats.data.Xor
import com.norcane.noble.api.models.BlogPost
import com.norcane.noble.api.{BlogPostRecord, FormatSupport, FormatSupportError, FormatSupportFactory}
import com.norcane.noble.utils.{Yaml, YamlValue}

import scala.io.Source
import scala.util.{Failure, Success}

@Singleton
class MarkdownFormatSupportFactory extends FormatSupportFactory {

  override def postType: String = "md"

  override def create: FormatSupport = new MarkdownFormatSupport()
}

class MarkdownFormatSupport extends FormatSupport {

  private val FrontMatterSeparator: String = "---"

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

  private def extractBlogPost(yaml: Yaml,
                              record: BlogPostRecord): FormatSupportError Xor BlogPost = {

    import Yaml.Defaults._

    def asXor[T: YamlValue](key: String, errMsg: String): FormatSupportError Xor T =
      Xor.fromOption(yaml.get[T](key), FormatSupportError(errMsg))

    val title: String = yaml.get[String]("title").getOrElse(record.title)
    val date: LocalDate = yaml.get[LocalDate]("date").getOrElse(record.date)
    val tags: Set[String] = yaml.get[String]("tags").toSet[String]
      .flatMap(_.split(" +").map(_.replace('+', ' ')))

    Xor.right(BlogPost(title, date, tags))
  }

  private def extractFrontMatter(is: InputStream, title: String): FormatSupportError Xor Yaml = {
    val lines: Iterator[String] = Source.fromInputStream(is).getLines()
      .map(_.trim).dropWhile(_.isEmpty)

    lines.nextOption match {
      case Some(FrontMatterSeparator) =>
        val frontMatter: String = lines.takeWhile(_ != FrontMatterSeparator).mkString("\n")
        Yaml.parse(frontMatter) match {
          case Success(yaml) => Xor.right(yaml)
          case Failure(th) =>
            Xor.left(FormatSupportError("Cannot parse YAML front matter", Some(th)))
        }
      case _ => Xor.left(FormatSupportError(s"Missing YAML front matter in blog post '$title'"))
    }
  }
}
