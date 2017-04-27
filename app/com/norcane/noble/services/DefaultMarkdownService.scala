package com.norcane.noble.services

import javax.inject.Singleton

import com.norcane.noble.api.services.MarkdownService
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.{MutableDataHolder, MutableDataSet}

/**
  * Default implementation of [[MarkdownService]] which uses the
  * [[https://github.com/vsch/flexmark-java flexmark-java]] library.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
@Singleton
class DefaultMarkdownService extends MarkdownService {

  import java.lang.{Boolean => JBoolean}

  val options: MutableDataHolder = new MutableDataSet()
    .set[JBoolean](Parser.FENCED_CODE_BLOCK_PARSER, true)

  val parser: Parser = Parser.builder(options).build()
  val htmlRenderer: HtmlRenderer = HtmlRenderer.builder(options).build()

  override def parseToHtml(input: String): String = htmlRenderer.render(parser.parse(input))
}
