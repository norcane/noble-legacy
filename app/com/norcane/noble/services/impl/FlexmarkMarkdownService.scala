/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016-2018 norcane
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

package com.norcane.noble.services.impl

import javax.inject.Singleton

import com.norcane.noble.services.MarkdownService
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.{MutableDataHolder, MutableDataSet}

/**
  * Implementation of [[MarkdownService]] based on the
  * [[https://github.com/vsch/flexmark-java flexmark-java]] library. In order to alter the set of
  * default ''flexmark'' options used by this implementation, extend this class and override the
  * [[flexmarkOptions]] method, as shown below:
  *
  * = #1 Extend this class =
  * {{{
  *   class MyMarkdownService extends FlexmarkMarkdownService {
  *
  *     override def flexmarkOptions: MutableDataHolder = super.flexmarkOptions
  *       .set[java.lang.Integer](Parser.LISTS_ITEM_INDENT, 2)
  *
  *   }
  * }}}
  *
  * = #2 Override the default [[MarkdownService]] in ''Noble module'' (see docs for more info) =
  * {{{
  *   overrideBinding[MarkdownService].to[MyMarkdownService]
  * }}}
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
@Singleton
class FlexmarkMarkdownService extends MarkdownService {

  import java.lang.{Boolean => JBoolean}

  private val parser = Parser.builder(flexmarkOptions).build()
  private val htmlRenderer = HtmlRenderer.builder(flexmarkOptions).build()

  override def parseToHtml(input: String): String = htmlRenderer.render(parser.parse(input))

  /**
    * Returns ''flexmark'' parser & ''HTML'' rendered options. Override this in order to alter
    * the set of enabled options and extensions.
    *
    * @return ''flexmark'' options
    */
  def flexmarkOptions: MutableDataHolder = new MutableDataSet()
    .set[JBoolean](Parser.FENCED_CODE_BLOCK_PARSER, true)
}
