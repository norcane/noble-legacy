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

package com.norcane.noble

import java.io.File

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.StreamConverters
import akka.util.Timeout
import com.norcane.noble.actors.BlogActor._
import com.norcane.noble.api.models._
import com.norcane.noble.api.models.dates.{Day, Month}
import com.norcane.noble.api.{BlogReverseRouter, BlogTheme, ContentStream}
import play.api.http.HttpEntity
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.MimeTypes
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._

class BlogController(blogActor: ActorRef, blogConfig: BlogConfig, themes: Set[BlogTheme],
                     router: BlogReverseRouter, blogPath: String, val messagesApi: MessagesApi)
  extends I18nSupport with Results {

  private implicit val defaultTimeout = Timeout(10.seconds)

  def index(page: Page) = BlogAction.async { implicit req =>
    paged(req.blog.posts, page, None)(router.index)
  }

  def post(year: Int, month: Int, day: Int, permalink: String) = BlogAction.async { implicit req =>
    req.blog.forYear(year).forMonth(month).forDay(day).forPermalink(permalink) match {
      case Some(postMeta) =>
        (blogActor ? RenderPostContent(req.blog, postMeta, placeholders))
          .mapTo[Option[String]].map(createBlogPost(postMeta, _, req.blog)) map {

          case Some(blogPost) =>
            Ok(themeByName(req.blog.info.themeName).blogPost(req.blog, router, blogPost))
          case None => notFoundResp(req.blog)
        }
      case None => Future.successful(notFoundResp(req.blog))
    }
  }

  def page(permalink: String) = BlogAction.async { implicit req =>
    req.blog.page(permalink) match {
      case Some(pageMeta) =>
        (blogActor ? RenderPageContent(req.blog, pageMeta, placeholders))
          .mapTo[Option[String]].map(createStaticPage(pageMeta, _)) map {

          case Some(staticPage) =>
            Ok(themeByName(req.blog.info.themeName).page(req.blog, router, staticPage))
          case None => notFoundResp(req.blog)
        }
      case None => Future.successful(notFoundResp(req.blog))
    }
  }

  def tag(name: String, page: Page) = BlogAction.async { implicit req =>
    paged(req.blog.forTag(name).getOrElse(Nil), page,
      Some(message("noble.posts-by-tag", name)))(router.tag(name, _))
  }

  def author(authorId: String, page: Page) = BlogAction.async { implicit req =>
    req.blog.info.authors.find(_.authorId == authorId) match {
      case Some(author) =>
        paged(req.blog.byAuthor(authorId).getOrElse(Nil), page,
          Some(message("noble.posts-by-author", author.name)))(router.author(authorId, _))
      case None =>
        Future.successful(notFoundResp(req.blog))
    }
  }

  def year(year: Int, page: Page) = BlogAction.async { implicit req =>
    paged(req.blog.forYear(year).posts, page,
      Some(message("noble.posts-by-year", year)))(router.year(year, _))
  }

  def month(year: Int, month: Int, page: Page) = BlogAction.async { implicit req =>
    val byMonth: Month = req.blog.forYear(year).forMonth(month)
    paged(byMonth.posts, page,
      Some(message("noble.posts-by-month", year, byMonth.name)))(router.month(year, month, _))
  }

  def day(year: Int, month: Int, day: Int, page: Page) = BlogAction.async { implicit req =>
    val byMonth: Month = req.blog.forYear(year).forMonth(month)
    val byDay: Day = byMonth.forDay(day)
    paged(byDay.posts, page,
      Some(message("noble.posts-by-day", year, byMonth.name, day)))(router.day(year, month, day, _))
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

  def atom = BlogAction.async { implicit req =>
    val posts: Seq[BlogPostMeta] = req.blog.posts.take(5)

    Future.sequence(posts map { postMeta =>
      (blogActor ? RenderPostContent(req.blog, postMeta, placeholders)).mapTo[Option[String]]
        .map(createBlogPost(postMeta, _, req.blog))
    }).map { loaded =>
      val posts: Seq[BlogPost] = loaded flatMap (_.toSeq)
      Ok(com.norcane.noble.atom.xml.atom(req.blog, router, posts))
    }
  }

  def reload(reloadToken: String) = BlogAction { implicit req =>
    blogConfig.reloadToken match {
      case Some(token) if token == reloadToken =>
        blogActor ! ReloadBlog(req.blog.versionId)
        Ok
      case _ => Forbidden
    }
  }

  def notFound = BlogAction { implicit req =>
    notFoundResp(req.blog)
  }

  private def placeholders: Map[String, Any] = Map(
    Keys.Placeholders.Assets -> s"$blogPath/blog-assets"
  )

  private def notFoundResp(blog: Blog)(implicit header: RequestHeader) =
    NotFound(themeByName(blog.info.themeName).notFound(blog, router))

  private def message(key: String, args: Any*)(implicit header: RequestHeader): String =
    messagesApi.preferred(header).apply(key, args: _*)

  private def createBlogPost(meta: BlogPostMeta, contentOpt: Option[String],
                             blog: Blog): Option[BlogPost] = for {
    content <- contentOpt
    author <- blog.info.authors.find(_.authorId == meta.author)
  } yield BlogPost(meta, author, content)

  private def createStaticPage(meta: StaticPageMeta,
                               contentOpt: Option[String]): Option[StaticPage] = {
    for (content <- contentOpt) yield StaticPage(meta, content)
  }

  private def paged[A](allPosts: Seq[BlogPostMeta], page: Page, title: Option[String])
                      (route: Page => Call)(implicit req: BlogRequest[A]): Future[Result] = {

    val pageNo: Int = if (page.pageNo < 1) 1 else page.pageNo
    val perPage: Int = if (page.perPage < 1) 1 else if (page.perPage > 10) 10 else page.perPage
    val zeroBasedPageNo: Int = pageNo - 1
    val startPageNo: Int = zeroBasedPageNo * perPage
    val posts: Seq[BlogPostMeta] = allPosts.slice(startPageNo, startPageNo + perPage)
    val lastPageNo: Int = Math.ceil(allPosts.size.toDouble / perPage.toDouble).toInt
    val pagination: Pagination = Pagination(pageNo, perPage, lastPageNo, route)

    // load blog posts content
    Future.sequence(posts.map { postMeta =>
      (blogActor ? RenderPostContent(req.blog, postMeta, placeholders)).mapTo[Option[String]]
        .map(createBlogPost(postMeta, _, req.blog))
    }).map { loaded =>
      val theme: BlogTheme = themeByName(req.blog.info.themeName)
      val posts: Seq[BlogPost] = loaded flatMap (_.toSeq)
      Ok(theme.blogPosts(req.blog, router, title, posts, pagination))
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
      blog.versionId.take(8) + blog.info.themeName.take(8)
  }

  case class ThemeNotFoundError(message: String, cause: Option[Throwable] = None)
    extends Error(message, cause.orNull)

}

class BlogRequest[A](request: Request[A], val blog: Blog) extends WrappedRequest[A](request)
