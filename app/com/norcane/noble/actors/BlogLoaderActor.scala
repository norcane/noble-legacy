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

package com.norcane.noble.actors

import akka.actor.{Actor, Props}
import cats.data.Xor
import com.norcane.noble.api.models.{Blog, BlogInfo, BlogPostMeta}
import com.norcane.noble.api.{BlogStorage, BlogStorageError, FormatSupport}

import scala.concurrent.blocking

class BlogLoaderActor(storage: BlogStorage, formatSupports: Map[String, FormatSupport])
  extends Actor {

  import BlogLoaderActor._

  override def receive: Receive = {
    case LoadBlog() => blocking {
      loadBlog match {
        case Xor.Left(err) => sender ! err
        case Xor.Right(blog) => sender ! BlogLoaded(blog)
      }
    }
  }

  private def loadBlog: BlogLoadingFailed Xor Blog = {
    val hash: String = storage.currentHash

    def wrapErr[T](xor: BlogStorageError Xor T): BlogLoadingFailed Xor T =
      xor.leftMap(err => BlogLoadingFailed(err.message, err.cause))

    for {
      blogInfo <- wrapErr(storage.loadInfo(hash))
      blogPosts <- wrapErr(storage.loadBlogPosts(hash))
      validatedBlogPosts <- validateBlogPosts(blogPosts, blogInfo)
    } yield new Blog(hash, blogInfo, validatedBlogPosts)
  }

  private def validateBlogPosts(posts: Seq[BlogPostMeta],
                                blogInfo: BlogInfo): BlogLoadingFailed Xor Seq[BlogPostMeta] = {

    import cats.instances.list._
    import cats.syntax.traverse._

    val validated: Seq[Xor[BlogLoadingFailed, BlogPostMeta]] = posts map { post =>
      blogInfo.authors find (_.nickname == post.author) match {
        case Some(author) => Xor.right(post)
        case None => Xor.left(BlogLoadingFailed(s"Cannot find author definition for nickname " +
          s"'${post.author}' mentioned in blog post '${post.id}'"))
      }
    }

    validated.toList.sequenceU
  }
}

object BlogLoaderActor {

  def props(storage: BlogStorage, formatSupports: Map[String, FormatSupport]): Props =
    Props(new BlogLoaderActor(storage, formatSupports))

  case class LoadBlog()

  case class BlogLoaded(blog: Blog)

  case class BlogLoadingFailed(message: String, cause: Option[Throwable] = None)
    extends Error(message, cause.orNull)

}
