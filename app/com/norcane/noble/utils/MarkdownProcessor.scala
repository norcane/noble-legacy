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

package com.norcane.noble.utils

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.{MutableDataHolder, MutableDataSet}

/**
  * Simple utility object, providing support for parsing the ''Markdown'' text into the ''HTML''
  * output. Based on the [[https://github.com/vsch/flexmark-java flexmark-java]] library.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
object MarkdownProcessor {

  val options: MutableDataHolder = new MutableDataSet()
    .set[java.lang.Boolean](Parser.FENCED_CODE_BLOCK_PARSER, true)
  val parser: Parser = Parser.builder(options).build()
  val htmlRenderer: HtmlRenderer = HtmlRenderer.builder(options).build()


  /**
    * Takes the input ''Markdown'' text and processes resulting ''HTML'' output.
    *
    * @param input input ''Markdown'' text
    * @return ''HTML'' output
    */
  def md2html(input: String): String = {
    htmlRenderer.render(parser.parse(input))
  }
}
