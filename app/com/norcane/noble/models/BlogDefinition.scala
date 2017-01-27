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

package com.norcane.noble.models

import akka.actor.ActorRef
import com.norcane.noble.api.BlogStorage
import com.norcane.noble.api.models.BlogConfig

/**
  * Collects all the data about properly configured and loaded blog, such as blog name,
  * configuration, storage and post format provider, etc.
  *
  * @param config  blog configuration
  * @param storage blog storage
  * @param actor   blog actor used to load and access blog posts and assets
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
case class BlogDefinition(config: BlogConfig, storage: BlogStorage, actor: ActorRef)

