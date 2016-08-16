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

package com.norcane.noble

import java.io.File

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.StreamConverters
import akka.util.Timeout
import com.norcane.noble.actors.BlogActor.{GetBlog, LoadAsset, RenderPostContent}
import com.norcane.noble.api.models.{Blog, BlogPost, BlogPostMeta, Page}
import com.norcane.noble.api.{BlogReverseRouter, BlogTheme, ContentStream}
import play.api.http.HttpEntity
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.MimeTypes
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._

class BlogController(blogActor: ActorRef, themes: Set[BlogTheme], router: BlogReverseRouter,
                     val messagesApi: MessagesApi)
  extends I18nSupport with Results {

  private implicit val defaultTimeout = Timeout(10.seconds)

  def index(page: Page) = BlogAction.async { implicit req =>
    paged(req.blog.posts, page, None)(router.index)
  }

  def post(year: Int, month: Int, day: Int, permalink: String) = BlogAction.async { implicit req =>
    req.blog.forYear(year).forMonth(month).forDay(day).forPermalink(permalink) match {
      case Some(postMeta) =>
        (blogActor ? RenderPostContent(req.blog, postMeta))
          .mapTo[Option[String]].map(createBlogPost(postMeta, _, req.blog)) map {

          case Some(blogPost) =>
            Ok(themeByName(req.blog.info.themeName).blogPost(req.blog, router, blogPost))
          case None => notFound
        }
      case None => Future.successful(notFound)
    }
  }

  def tag(name: String, page: Page) = BlogAction.async { implicit req =>
    paged(req.blog.forTag(name).getOrElse(Nil), page,
      Some(message("noble.posts-by-tag", name)))(p => router.tag(name, p))
  }

  def asset(path: String) = BlogAction.async { implicit req =>
    val safePath: String = new File(s"/$path").getCanonicalPath

    (blogActor ? LoadAsset(req.blog, safePath)).mapTo[Option[ContentStream]] flatMap {
      case Some(ContentStream(stream, length)) =>
        Future.successful(Ok.sendEntity(HttpEntity.Streamed(
          StreamConverters.fromInputStream(() => stream),
          Some(length),
          MimeTypes.forFileName(path)
        )))
      case _ => Future.successful(NotFound)
    }
  }

  def notFound = NotFound

  private def message(key: String, args: Any*)(implicit header: RequestHeader): String =
    messagesApi.preferred(header).apply(key, args: _*)

  private def createBlogPost(meta: BlogPostMeta, contentOpt: Option[String],
                             blog: Blog): Option[BlogPost] = for {
    content <- contentOpt
    author <- blog.info.authors.find(_.nickname == meta.author)
  } yield BlogPost(meta, author, content)

  private def paged[A](allPosts: Seq[BlogPostMeta], page: Page, title: Option[String])
                      (route: Page => Call)(implicit req: BlogRequest[A]): Future[Result] = {

    val pageNo: Int = if (page.pageNo < 1) 1 else page.pageNo
    val perPage: Int = if (page.perPage < 1) 1 else if (page.perPage > 10) 10 else page.perPage
    val zeroBasedPageNo: Int = pageNo - 1
    val startPageNo: Int = zeroBasedPageNo * perPage
    val posts: Seq[BlogPostMeta] = allPosts.slice(startPageNo, startPageNo + perPage)
    val lastPageNo: Int = allPosts.size / perPage
    val previous: Option[Call] = if (pageNo > 1) Some(route(Page(pageNo - 1, perPage))) else None
    val next: Option[Call] = if (pageNo < lastPageNo) Some(route(Page(pageNo + 1, perPage))) else None

    // load blog posts content
    Future.sequence(posts.map { postMeta =>
      (blogActor ? RenderPostContent(req.blog, postMeta)).mapTo[Option[String]]
        .map(createBlogPost(postMeta, _, req.blog))
    }).map { loaded =>
      val theme: BlogTheme = themeByName(req.blog.info.themeName)
      val posts: Seq[BlogPost] = loaded flatMap (_.toSeq)
      Ok(theme.blogPosts(req.blog, router, title, posts, previous, next))
    }
  }

  @throws(classOf[ThemeNotFoundError])
  private def themeByName(name: String): BlogTheme = themes.find(_.name == name) match {
    case Some(theme) => theme
    case None =>
      val available: String =
        if (themes.nonEmpty) themes.map(_.name).mkString(",") else "<no themes available>"
      throw ThemeNotFoundError(s"Cannot find theme for name '$name' (available themes: $available)")
  }


  object BlogAction extends ActionBuilder[BlogRequest] {

    override def invokeBlock[A](request: Request[A],
                                block: (BlogRequest[A]) => Future[Result]): Future[Result] = {
      import akka.pattern.ask
      import play.api.http.HeaderNames._

      (blogActor ? GetBlog).mapTo[Blog] flatMap { blog =>
        val eTag: String = eTagFor(blog)
        if (request.headers.get(IF_NONE_MATCH).contains(eTag))
          Future.successful(NotModified)
        else
          block(new BlogRequest(request, blog)).map(_.withHeaders(ETAG -> eTag))
      }
    }

    private def eTagFor(blog: Blog): String =
      blog.hash.take(8) + blog.info.themeName.take(8)
  }

  case class ThemeNotFoundError(message: String, cause: Option[Throwable] = None)
    extends Error(message, cause.orNull)

}

class BlogRequest[A](request: Request[A], val blog: Blog) extends WrappedRequest[A](request)
