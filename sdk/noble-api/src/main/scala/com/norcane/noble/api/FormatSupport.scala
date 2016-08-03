package com.norcane.noble.api

import java.io.InputStream
import java.time.LocalDate

import cats.data.Xor
import com.norcane.noble.api.models.BlogPost


trait FormatSupportFactory {
  def postType: String

  def create: FormatSupport
}

trait FormatSupport {

  def extractPostMetadata(is: InputStream, record: BlogPostRecord): FormatSupportError Xor BlogPost

  def extractPostContent(is: InputStream, post: BlogPost): FormatSupportError Xor String

}

case class FormatSupportError(message: String, cause: Option[Throwable] = None)

case class BlogPostRecord(id: String, date: LocalDate, title: String, postType: String)