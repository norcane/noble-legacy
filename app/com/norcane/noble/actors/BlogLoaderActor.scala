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
import com.norcane.noble.api.models.Blog
import com.norcane.noble.api.{BlogStorage, FormatSupport}

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
    for {
      blogInfo <- storage.loadInfo(hash)
        .leftMap(err => BlogLoadingFailed(err.message, err.cause))
      blogPosts <- storage.loadBlogPosts(hash)
        .leftMap(err => BlogLoadingFailed(err.message, err.cause))
    } yield new Blog(hash, blogInfo, blogPosts)
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
