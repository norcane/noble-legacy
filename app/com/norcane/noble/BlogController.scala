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
import com.norcane.noble.api.{BlogReverseRouter, BlogTheme, ContentStream}
import play.api.http.HttpEntity
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Controller processing requests for particular ''noble'' blog.
  *
  * @param blogActor  blog actor, responsible for loading blog content
  *                   (see [[com.norcane.noble.actors.BlogActor]])
  * @param blogConfig blog configuration
  * @param themes     set of all available themes
  * @param router     blog reverse router
  * @param blogPath   blog path (i.e. blog context root, relative to ''noble'' root)
  * @param cc         Play's Controller components
  */
class BlogController(blogActor: ActorRef,
                     blogConfig: BlogConfig,
                     themes: Set[BlogTheme],
                     router: BlogReverseRouter,
                     blogPath: String,
                     cc: ControllerComponents)
  extends AbstractController(cc) with I18nSupport with Results {

  private implicit val defaultTimeout: Timeout = Timeout(10.seconds)
  private implicit val eCtx: ExecutionContext = cc.executionContext
  private implicit val ma: MessagesApi = messagesApi

  /**
    * Handles request to display the blog main page, where list of all blog posts is displayed.
    *
    * @param page selected page to paginate the list of blog posts
    * @return blog action
    */
  def index(page: Page): Action[Unit] = BlogAction.async { implicit req =>
    paged(req.blog.posts, page, None, None)(router.index)
  }

  /**
    * Handles request to display selected blog post.
    *
    * @param year      year of the blog post publication
    * @param month     month of the blog post publication
    * @param day       day of month of the blog post publication
    * @param permalink blog post permalink
    * @return blog action
    */
  def post(year: Int, month: Int, day: Int, permalink: String): Action[Unit] =
    BlogAction.async { implicit req =>
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

  /**
    * Handles request to display selected static page.
    *
    * @param permalink static page permalink
    * @return blog action
    */
  def page(permalink: String): Action[Unit] = BlogAction.async { implicit req =>
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

  /**
    * Handles request to display list of all blog posts for specified tag name.
    *
    * @param name tag name
    * @param page selected page to paginate the list of blog posts
    * @return blog action
    */
  def tag(name: String, page: Page): Action[Unit] = BlogAction.async { implicit req =>
    val filter = PostsFilter.Tag(name)
    paged(req.blog.forTag(name).getOrElse(Nil), page,
      Some(message("noble.posts-by-tag", name)), Some(filter))(router.tag(name, _))
  }

  /**
    * Handles request to display list of all blog posts published by specified author.
    *
    * @param authorId author ID
    * @param page     selected page to paginate the list of blog posts
    * @return blog action
    */
  def author(authorId: String, page: Page): Action[Unit] = BlogAction.async { implicit req =>
    req.blog.info.authors.find(_.authorId == authorId) match {
      case Some(author) =>
        val filter = PostsFilter.Author(author)
        paged(req.blog.byAuthor(authorId).getOrElse(Nil), page,
          Some(message("noble.posts-by-author", author.name)), Some(filter))(router.author(authorId, _))
      case None =>
        Future.successful(notFoundResp(req.blog))
    }
  }

  /**
    * Handles request to display list of all blog posts for specified year of publication.
    *
    * @param year year of publication
    * @param page selected page to paginate the list of blog posts
    * @return blog action
    */
  def year(year: Int, page: Page): Action[Unit] = BlogAction.async { implicit req =>
    val filter = PostsFilter.Year(year)
    paged(req.blog.forYear(year).posts, page,
      Some(message("noble.posts-by-year", year)), Some(filter))(router.year(year, _))
  }

  /**
    * Handles request to display list of all blog posts for specified year and month of publication.
    *
    * @param year  year of publication
    * @param month month of publication
    * @param page  selected page to paginate the list of blog posts
    * @return blog action
    */
  def month(year: Int, month: Int, page: Page): Action[Unit] =
    BlogAction.async { implicit req =>
      implicit val lang: Lang = req.lang
      val byMonth = req.blog.forYear(year).forMonth(month)
      val filter = PostsFilter.Month(year, month)
      paged(byMonth.posts, page,
        Some(message("noble.posts-by-month", year, byMonth.name)),
        Some(filter))(router.month(year, month, _))
    }

  /**
    * Handles request to display list of all blog posts for specified year, month and day of
    * publication.
    *
    * @param year  year of publication
    * @param month month of publication
    * @param day   day of publication
    * @param page  selected page to paginate the list of blog posts
    * @return blog action
    */
  def day(year: Int, month: Int, day: Int, page: Page): Action[Unit] =
    BlogAction.async { implicit req =>
      implicit val lang: Lang = req.lang
      val byMonth = req.blog.forYear(year).forMonth(month)
      val byDay = byMonth.forDay(day)
      val filter = PostsFilter.Day(year, month, day)
      paged(byDay.posts, page,
        Some(message("noble.posts-by-day", year, byMonth.name, day)),
        Some(filter))(router.day(year, month, day, _))
    }

  /**
    * Handles request to get the blog asset, specified by its path.
    *
    * @param path path of the asset, under which can be located in selected ''blog storage''
    * @return blog action
    */
  def asset(path: String): Action[Unit] = BlogAction.async { implicit req =>
    val safePath = if (File.separator == "\\") {
      java.nio.file.Paths.get(s"/$path").normalize().toString.replace("\\", "/")
    } else {
      new File(s"/$path").getCanonicalPath
    }
    (blogActor ? LoadAsset(req.blog, safePath)).mapTo[Option[ContentStream]] flatMap {
      case Some(ContentStream(stream, length)) =>
        Future.successful(Ok.sendEntity(HttpEntity.Streamed(
          StreamConverters.fromInputStream(() => stream),
          Some(length),
          cc.fileMimeTypes.forFileName(path)
        )))
      case _ => Future.successful(NotFound)
    }
  }

  /**
    * Handles request to display the ''Atom feed'' of the current blog.
    *
    * @return blog action
    */
  def atom: Action[Unit] = BlogAction.async { implicit req =>
    val posts = req.blog.posts.take(Page.DefaultPageSize)

    Future.sequence(posts map { postMeta =>
      (blogActor ? RenderPostContent(req.blog, postMeta, placeholders)).mapTo[Option[String]]
        .map(createBlogPost(postMeta, _, req.blog))
    }).map { loaded =>
      val loadedPosts = loaded flatMap (_.toSeq)
      Ok(com.norcane.noble.atom.xml.atom(req.blog, router, loadedPosts))
    }
  }

  /**
    * Handles request that forces the blog to reload (if authorized using the given
    * ''reload token'').
    *
    * @param reloadToken reload token to authorize the request
    * @return blog action
    */
  def reload(reloadToken: String): Action[Unit] = BlogAction { implicit req =>
    blogConfig.reloadToken match {
      case Some(token) if token == reloadToken =>
        blogActor ! ReloadBlog(req.blog.versionId)
        Ok
      case _ => Forbidden
    }
  }

  /**
    * Handles the request that cannot be processed, because the resource cannot be found.
    *
    * @return blog action
    */
  def notFound: Action[Unit] = BlogAction { implicit req =>
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

  private def paged[A](allPosts: Seq[BlogPostMeta], page: Page, title: Option[String],
                       filter: Option[PostsFilter])
                      (route: Page => Call)(implicit req: BlogRequest[A]): Future[Result] = {

    val pageNo = if (page.pageNo < 1) 1 else page.pageNo
    val perPage = if (page.perPage < 1) 1 else if (page.perPage > 10) 10 else page.perPage
    val zeroBasedPageNo = pageNo - 1
    val startPageNo = zeroBasedPageNo * perPage
    val posts = allPosts.slice(startPageNo, startPageNo + perPage)
    val lastPageNo = Math.ceil(allPosts.size.toDouble / perPage.toDouble).toInt
    val pagination = Pagination(pageNo, perPage, lastPageNo, route)

    // load blog posts content
    Future.sequence(posts.map { postMeta =>
      (blogActor ? RenderPostContent(req.blog, postMeta, placeholders)).mapTo[Option[String]]
        .map(createBlogPost(postMeta, _, req.blog))
    }).map { loaded =>
      val theme = themeByName(req.blog.info.themeName)
      val posts = loaded flatMap (_.toSeq)
      Ok(theme.blogPosts(req.blog, router, title, posts, pagination, filter))
    }
  }

  @throws(classOf[ThemeNotFoundError])
  private def themeByName(name: String): BlogTheme = themes.find(_.name == name) match {
    case Some(theme) => theme
    case None =>
      val available = if (themes.nonEmpty) themes.map(_.name).mkString(",") else "<no themes available>"
      throw ThemeNotFoundError(s"Cannot find theme for name '$name' (available themes: $available)")
  }

  case class ThemeNotFoundError(message: String, cause: Option[Throwable] = None)
    extends Error(message, cause.orNull)

  object BlogAction extends ActionBuilder[BlogRequest, Unit] {

    override val parser: BodyParser[Unit] = cc.parsers.empty

    override def invokeBlock[A](request: Request[A],
                                block: BlogRequest[A] => Future[Result]): Future[Result] = {
      (blogActor ? GetBlog).mapTo[Blog] flatMap { blog =>
        val eTag = eTagFor(blog)
        if (request.headers.get(IF_NONE_MATCH).contains(eTag))
          Future.successful(play.api.mvc.Results.NotModified)
        else
          block(new BlogRequest(request, blog)).map(_.withHeaders(ETAG -> eTag))
      }
    }

    override protected def executionContext: ExecutionContext = cc.executionContext

    private def eTagFor(blog: Blog): String =
      blog.versionId.take(8) + blog.info.themeName.take(8)
  }

}

/**
  * Extends Play request, that holds the reference to the current blog.
  *
  * @param request Play request
  * @param blog    current blog
  * @tparam A request type
  */
class BlogRequest[A](request: Request[A], val blog: Blog) extends WrappedRequest[A](request)
