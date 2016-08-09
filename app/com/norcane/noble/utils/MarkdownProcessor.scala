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

package com.norcane.noble.utils

import org.pegdown.PegDownProcessor

/**
  * Simple utility object, providing support for parsing the ''Markdown'' text into the ''HTML''
  * output. Based on the [[https://github.com/sirthias/pegdown Pegdown]] library.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
object MarkdownProcessor {

  private val processor: PegDownProcessor = new PegDownProcessor()

  /**
    * Takes the input ''Markdown'' text and processes resulting ''HTML'' output.
    *
    * @param input input ''Markdown'' text
    * @return ''HTML'' output
    */
  def md2html(input: String): String = processor.markdownToHtml(input)

}
