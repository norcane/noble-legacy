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

package com.norcane.noble.api.models

import com.norcane.noble.api.astral.Astral

/**
  * Represents the blog author. Each author must be uniquely distinguishable by his/her `nickname`.
  * As the nickname may be used in some ''URL'' parts, ''URL''-friendly form is recommended.
  *
  * @param nickname   unique nickname of the blog author
  * @param name       name (first name + last name) of the blog author
  * @param biography  optional short author biography
  * @param avatar     optional path to the author avatar/portrait image
  * @param properties other author properties
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class BlogAuthor(nickname: String, name: String, biography: Option[String],
                      avatar: Option[String], properties: Astral)
