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

package com.norcane.noble.api

import java.io.InputStream

import com.norcane.noble.api.models.{BlogInfo, BlogPostMeta, StorageConfig}

/**
  * Factory class for the concrete implementation of [[BlogStorage]]. This factory class is used to
  * register custom storage implementation into the Noble. The ''storage type'' is unique string
  * name of the concrete storage implementation, used to refer it in blog configuration file (e.g.
  * `git` for storage implementation using the Git as physical storage). For further details about
  * creating and registering custom blog storage, please refer the official project documentation.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait BlogStorageFactory {

  /**
    * ''Storage type'' represents the unique string name of the blog storage implementation (for
    * example `git` or `mongodb`). The storage type is used to refer the specific implementation in
    * blog configuration file.
    *
    * @return storage type
    */
  def storageType: String

  /**
    * Creates new instance of the specific [[BlogStorage]] implementation. As an parameters,
    * blog storage config and map of all registered [[FormatSupport]] instances.
    *
    * @param config         blog storage configuration
    * @param formatSupports map of all registered [[FormatSupport]] instances
    * @return [[BlogStorage]] instance or error details in case of failure
    */
  def create(config: StorageConfig,
             formatSupports: Map[String, FormatSupport]): Either[BlogStorageError, BlogStorage]
}

/**
  * Represents the blog storage, from which the blog info, blog posts and blog assets are loaded.
  * Please note that every implementation '''must''' satisfy the following rules:
  *
  *  - Noble's blog storage is expected to be versioned. Each version can be described by its
  * ''version ID'' (e.g. numbers from 0, Git commit ID). Current (i.e. last known) version ID must
  * be provided by the [[BlogStorage#currentVersionId]] method.
  *  - Any methods accepting `versionId` as their parameter '''must''' return always the same result
  * for the same version ID.
  *
  * @author Vaclav Svejcar (v.svejcar@norcane.cz)
  */
trait BlogStorage {

  /**
    * Returns the current (last known) version ID.
    *
    * @return current version ID
    */
  def currentVersionId: String

  /**
    * Loads the blog info (blog title, theme name, ...) from the storage, using the `versionId`
    * parameter as the ID of the content version. Blog info is loaded only during application
    * startup and when blog reloading is triggered.
    *
    * @param versionId version ID
    * @return loaded blog info or error details in case of failure
    */
  def loadInfo(versionId: String): Either[BlogStorageError, BlogInfo]

  /**
    * Loads the content for blog post specified by its metadata, using the `versionId` parameter as
    * the ID of the content version. Blog post content must be formatted either as a plain text or
    * HTML.
    *
    * @param versionId    version ID
    * @param post         blog post metadata
    * @param placeholders text placeholders (as map of their names and actual values to
    *                     replace placeholders with)
    * @return blog post content or error details in case of failure
    */
  def loadPostContent(versionId: String, post: BlogPostMeta,
                      placeholders: Map[String, Any]): Either[BlogStorageError, String]

  /**
    * Loads the collection of all blog posts (metadata only), using the `versionId` parameter as
    * the ID of the content version.
    *
    * @param versionId version ID
    * @return collection of all blog posts (metadata only)
    */
  def loadBlogPosts(versionId: String): Either[BlogStorageError, Seq[BlogPostMeta]]

  /**
    * Loads the blog asset, specified by its path, using the `versionId` parameter as
    * the ID of the content version.
    *
    * @param versionId version ID
    * @param path      blog asset path
    * @return blog asset stream or error details in case of failure
    */
  def loadAsset(versionId: String, path: String): Either[BlogStorageError, ContentStream]
}

/**
  * Simple wrapper class around the standard Java's `InputStream`, adding info about stream length
  * in bytes.
  *
  * @param stream `InputStream` instance
  * @param length length of the input stream in bytes
  */
case class ContentStream(stream: InputStream, length: Long)

/**
  * Error indicating failure occurred during some of the [[BlogStorage]] operations.
  *
  * @param message error message
  * @param cause   error cause (optional)
  */
case class BlogStorageError(message: String, cause: Option[Throwable] = None)
