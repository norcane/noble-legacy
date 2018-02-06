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

package com.norcane.noble.services

/**
  * Represents service allowing to parse ''Markdown'' text into various formats. The default
  * implementation, used internally by ''Noble'', is bound using ''Guice's'' option binding and
  * can be override (for example to use different extensions and options) in client blog using the
  * following code in the ''Noble module''.
  *
  * {{{
  *   overrideBinding[MarkdownService].to[MyCustomMarkdownService]
  * }}}
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait MarkdownService {

  /**
    * Parses input ''Markdown'' text into the ''HTML''.
    *
    * @param input ''Markdown'' input text
    * @return output ''HTML'' text
    */
  def parseToHtml(input: String): String

}
