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

import javax.inject.{Inject, Singleton}

import com.norcane.noble.api.BlogReverseRouter
import com.norcane.noble.api.models.Page
import play.api.i18n.MessagesApi
import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.Router.Routes
import play.api.routing.{Router, SimpleRouter}

@Singleton
class NobleRouter @Inject()(messages: MessagesApi, noble: Noble) extends SimpleRouter {

  private var prefix: String = ""

  override def routes: Routes = {
    import play.api.routing._
    import play.api.routing.sird._

    val blogRouters: Seq[Router] = noble.blogs map { blog =>
      val blogPath: String = prefix + blog.config.path
      val globalAssetsPath: String = s"$prefix/${Keys.Defaults.GlobalAssetsPrefix}"
      val reverseRouter: BlogReverseRouter = new BlogReverseRouter(blogPath, globalAssetsPath)
      val controller: BlogController = new BlogController(
        blog.actor, blog.config, noble.themes, reverseRouter, blogPath, messages)
      new BlogRouter(controller).withPrefix(blog.config.path)
    }

    val blogRoutes: PartialFunction[RequestHeader, Handler] = blogRouters
      .map(_.routes)
      .foldLeft(PartialFunction.empty[RequestHeader, Handler])(_ orElse _)

    val globalRoutes: PartialFunction[RequestHeader, Handler] = {
      case GET(p"/${Keys.Defaults.GlobalAssetsPrefix}/lib/$path*") =>
        controllers.Assets.versioned("/public/lib", path)
    }

    globalRoutes orElse blogRoutes
  }

  override def withPrefix(prefix: String): Router = {
    this.prefix = if (prefix == "/") "" else prefix
    super.withPrefix(prefix)
  }

}

class BlogRouter(controller: BlogController) extends SimpleRouter {
  self =>

  import play.api.routing.sird._

  override def routes: Routes = {

    // list all blog posts
    case GET((p"/" | p"")) ? Page(page) => controller.index(page)

    // specific blog post
    case GET(p"""/${int(year)}<\d{4}>/${int(month)}<\d{2}>/${int(day)}<\d{2}>/$permalink""") =>
      controller.post(year, month, day, permalink)

    // blog posts for specific year
    case GET(p"""/${int(year)}<\d{4}>""" ? Page(page)) =>
      controller.year(year, page)

    // blog posts for specific month
    case GET(p"""/${int(year)}<\d{4}>/${int(month)}<\d{2}>""" ? Page(page)) =>
      controller.month(year, month, page)

    // blog posts for specific day
    case GET(p"""/${int(year)}<\d{4}>/${int(month)}<\d{2}>/${int(day)}<\d{2}>""" ? Page(page)) =>
      controller.day(year, month, day, page)

    // blog posts for specific tag
    case GET(p"/tags/$name" ? Page(page)) =>
      controller.tag(name, page)

    // assets
    case GET(p"/blog-assets/$path*") =>
      controller.asset(path)

    // atom feed
    case GET(p"/atom.xml") =>
      controller.atom

    // blog reload requests
    case POST(p"/reload/$reloadToken") =>
      controller.reload(reloadToken)

    // in case that none of routes match, return 404 not found
    case other =>
      controller.notFound
  }

  override def withPrefix(prefix: String): Router =
    if (prefix == "/") self
    else new Router {
      override def routes: Routes = {
        val p: String = if (prefix.endsWith("/")) prefix else prefix + "/"
        val prefixed: PartialFunction[RequestHeader, RequestHeader] = {
          case header: RequestHeader if header.path.startsWith(p) || header.path.equals(prefix) =>
            header.copy(path = header.path.drop(p.length - 1))
        }
        Function.unlift(prefixed.lift andThen (_ flatMap self.routes.lift))
      }

      override def documentation: Seq[(String, String, String)] = self.documentation

      override def withPrefix(prefix: String): Router = self.withPrefix(prefix)
    }
}