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

package com.norcane.noble.actors

import akka.actor.{Actor, ActorLogging, Props}
import cats.instances.either._
import cats.syntax.either._
import com.norcane.noble.actors.BlogActor.ReloadBlog
import com.norcane.noble.api.models.{Blog, BlogConfig, BlogInfo, BlogPostMeta}
import com.norcane.noble.api.{BlogStorage, BlogStorageError, FormatSupport}

import scala.concurrent.blocking

class BlogLoaderActor(config: BlogConfig, storage: BlogStorage,
                      formatSupports: Map[String, FormatSupport]) extends Actor with ActorLogging {

  import BlogLoaderActor._

  override def receive: Receive = {
    case LoadBlog() => blocking {
      loadBlog match {
        case Left(err) => sender ! err
        case Right(blog) => sender ! BlogLoaded(blog)
      }
    }
    case ReloadBlog(lastUsedHash) => blocking {
      if (lastUsedHash != storage.currentVersionId) {
        log.info("New blog version available, reloading blog...")
        loadBlog match {
          case Left(err) => sender ! err
          case Right(blog) => sender ! BlogLoaded(blog)
        }
      } else log.info("Already using the latest version of blog, reloading cancelled")
    }
  }

  private def loadBlog: Either[BlogLoadingFailed, Blog] = {
    val versionId: String = storage.currentVersionId

    def wrapErr[T](either: Either[BlogStorageError, T]): Either[BlogLoadingFailed, T] =
      either.leftMap(err => BlogLoadingFailed(err.message, err.cause))

    for {
      blogInfo <- wrapErr(storage.loadInfo(versionId))
      blogPosts <- wrapErr(storage.loadBlogPosts(versionId))
      pages <- wrapErr(storage.loadPages(versionId))
      validatedBlogPosts <- validateBlogPosts(blogPosts, blogInfo)
    } yield new Blog(config.name, versionId, blogInfo, validatedBlogPosts, pages)
  }

  private def validateBlogPosts(posts: Seq[BlogPostMeta], blogInfo: BlogInfo
                               ): Either[BlogLoadingFailed, Seq[BlogPostMeta]] = {

    import cats.instances.list._
    import cats.syntax.traverse._

    val validated: Seq[Either[BlogLoadingFailed, BlogPostMeta]] = posts map { post =>
      blogInfo.authors find (_.nickname == post.author) match {
        case Some(_) => Right(post)
        case None => Left(BlogLoadingFailed(s"Cannot find author definition for nickname " +
          s"'${post.author}' mentioned in blog post '${post.id}'"))
      }
    }

    validated.toList.sequenceU
  }
}

object BlogLoaderActor {

  def props(config: BlogConfig, storage: BlogStorage,
            formatSupports: Map[String, FormatSupport]): Props =
    Props(new BlogLoaderActor(config, storage, formatSupports))

  case class LoadBlog()

  case class BlogLoaded(blog: Blog)

  case class BlogLoadingFailed(message: String, cause: Option[Throwable] = None)
    extends Error(message, cause.orNull)

}
