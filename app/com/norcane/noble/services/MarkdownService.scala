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
