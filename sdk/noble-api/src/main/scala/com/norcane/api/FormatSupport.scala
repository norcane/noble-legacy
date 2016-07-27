package com.norcane.api

import java.io.InputStream
import java.time.LocalDate

import cats.data.Xor
import com.norcane.api.models.BlogPost


trait FormatSupportFactory {
  def postType: String

  def create: FormatSupport
}

trait FormatSupport {

  def extractPostMetadata(is: InputStream, record: BlogPostRecord): FormatSupportError Xor BlogPost

}

case class FormatSupportError(message: String, cause: Option[Throwable] = None)

case class BlogPostRecord(date: LocalDate, title: String, postType: String)