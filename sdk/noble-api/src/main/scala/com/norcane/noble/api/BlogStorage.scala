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

package com.norcane.noble.api

import java.io.InputStream

import cats.data.Xor
import com.norcane.noble.api.models.{BlogInfo, BlogPostMeta, StorageConfig}

trait BlogStorageFactory {

  def storageType: String

  def create(config: StorageConfig,
             formatSupports: Map[String, FormatSupport]): BlogStorageError Xor BlogStorage
}

trait BlogStorage {

  def currentHash: String

  def loadInfo(hash: String): BlogStorageError Xor BlogInfo

  def loadPostContent(hash: String, post: BlogPostMeta): BlogStorageError Xor String

  def loadBlogPosts(hash: String): BlogStorageError Xor Seq[BlogPostMeta]

  def loadAsset(hash: String, path: String): BlogStorageError Xor ContentStream
}

case class ContentStream(stream: InputStream, length: Long)

case class BlogStorageError(message: String, cause: Option[Throwable] = None)
