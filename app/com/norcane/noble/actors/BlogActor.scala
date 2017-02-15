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

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.SmallestMailboxPool
import com.norcane.noble.Keys
import com.norcane.noble.actors.BlogActor._
import com.norcane.noble.actors.BlogLoaderActor.{BlogLoaded, BlogLoadingFailed, LoadBlog}
import com.norcane.noble.api.models.{Blog, BlogConfig, BlogPostMeta, StaticPageMeta}
import com.norcane.noble.api.{BlogStorage, FormatSupport}

class BlogActor(storage: BlogStorage,
                blogConfig: BlogConfig,
                formatSupports: Map[String, FormatSupport]) extends Actor with ActorLogging {

  private val blogLoaderActor: ActorRef = context.actorOf(
    BlogLoaderActor.props(blogConfig, storage, formatSupports))

  private val contentLoaderActors: ActorRef = context.actorOf(
    ContentLoaderActor.props(storage)
      .withRouter(SmallestMailboxPool(nrOfInstances = 10))
      .withDispatcher(s"${Keys.Namespace}.content-loader-dispatcher")
  )


  override def preStart(): Unit = {
    blogLoaderActor ! LoadBlog()
  }

  override def receive: Receive = notLoaded()

  private def notLoaded(requests: List[(ActorRef, Any)] = Nil): Receive = {
    case BlogLoaded(blog) =>
      context.become(loaded(blog))
      requests.reverse foreach {
        case (sender, message) => self.tell(message, sender)
      }
    case failure@BlogLoadingFailed(message, cause) =>
      // TODO propagate the error to client UI
      log.error(cause.orNull, s"Error when loading blog '${blogConfig.name}': $message")
      throw failure
    case other =>
      context.become(notLoaded(sender() -> other :: requests))
  }

  private def loaded(blog: Blog): Receive = {
    case GetBlog => sender ! blog
    case BlogLoaded(newBlog) => context.become(loaded(newBlog))
    case req: ReloadBlog => blogLoaderActor ! req
    case req: RenderPostContent => contentLoaderActors.tell(req, sender())
    case req: RenderPageContent => contentLoaderActors.tell(req, sender())
    case req: LoadAsset => contentLoaderActors.tell(req, sender())
  }

}

object BlogActor {
  def props(storage: BlogStorage,
            blogConfig: BlogConfig,
            formatSupports: Map[String, FormatSupport]): Props =
    Props(new BlogActor(storage, blogConfig, formatSupports))

  case object GetBlog

  case class ReloadBlog(lastUsedHash: String)

  case class RenderPostContent(blog: Blog, post: BlogPostMeta, placeholders: Map[String, Any])

  case class RenderPageContent(blog: Blog, page: StaticPageMeta, placeholders: Map[String, Any])

  case class LoadAsset(blog: Blog, path: String)

}