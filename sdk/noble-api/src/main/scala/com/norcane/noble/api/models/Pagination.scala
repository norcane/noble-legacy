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

package com.norcane.noble.api.models

import play.api.mvc.{Call, RequestHeader}

/**
  * Represents the pagination options for the specific [[Page]], such as navigation to previous and
  * next page, general info about pagination such as total pages count, etc..
  *
  * @param pageNo     number of the current page (numbered from 1)
  * @param perPage    number of blog posts rendered per page
  * @param pagesTotal number of pages total
  * @param route      routing function
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class Pagination(pageNo: Int, perPage: Int, pagesTotal: Int, route: Page => Call) {

  /**
    * Returns the call to the previous page (if exists).
    *
    * @return the call to the previous page
    */
  def previous: Option[Call] = if (pageNo > 1) page(pageNo - 1) else None

  /**
    * Returns the call to the next page (if exists).
    *
    * @return the call to the next page
    */
  def next: Option[Call] = if (pageNo < pagesTotal) page(pageNo + 1) else None

  /**
    * Returns the call to the very first page (in case that current page is not the first one).
    *
    * @return call to the very first page
    */
  def first: Option[Call] = if (pageNo > 1) page(1) else None

  /**
    * Returns the call to the very last page (in case that current page is not the last one).
    *
    * @return call to the very last page
    */
  def last: Option[Call] = if (pageNo < pagesTotal) page(pagesTotal) else None

  /**
    * Returns the call to any available page.
    *
    * @param number number of requested page (in the range 1 to `pagesTotal` (including))
    * @return the call to the requested page
    */
  def page(number: Int): Option[Call] = {
    if (number >= 1 && number <= pagesTotal) Some(route(Page(number, perPage))) else None
  }
}

/**
  * Represents the specific page (i.e. its details about position in pagination and number of
  * rendered blog posts)
  *
  * @param pageNo  number of the current page (numbered from 1)
  * @param perPage number of blog posts rendered per page
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class Page(pageNo: Int, perPage: Int)

/**
  * Companion object for the [[Page]] class.
  */
object Page {

  import play.api.routing.sird._

  def unapply(header: RequestHeader): Option[Page] = {
    // TODO separate query param names into constants
    header.queryString match {
      case q_o"page=${int(page)}" & q_o"per-page=${int(perPage)}" =>
        Some(Page(page.getOrElse(1), perPage.getOrElse(5)))
      case _ => None
    }
  }
}
