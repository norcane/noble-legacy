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

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.norcane.api.models.{Blog, BlogConfig, BlogPost}
import com.norcane.api.{BlogStorageFactory, FormatSupport}
import com.norcane.noble.actors.BlogActor.GetBlog
import com.norcane.noble.actors.BlogLoaderActor.{BlogLoaded, BlogLoadingFailed, LoadBlog}

class BlogActor(storageFactory: BlogStorageFactory,
                blogConfig: BlogConfig,
                formatSupports: Map[String, FormatSupport]) extends Actor with ActorLogging {

  private val blogLoaderActor: ActorRef = context.actorOf(
    BlogLoaderActor.props(storageFactory, blogConfig.storageConfig, formatSupports))


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
  }

}

object BlogActor {
  def props(storageFactory: BlogStorageFactory,
            blogConfig: BlogConfig,
            formatSupports: Map[String, FormatSupport]): Props =
    Props(new BlogActor(storageFactory, blogConfig, formatSupports))

  trait BlogActorProtocol

  case object GetBlog extends BlogActorProtocol

  case class RenderPostContent(blog: Blog, post: BlogPost)

}