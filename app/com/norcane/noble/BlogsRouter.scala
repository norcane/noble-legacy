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

import play.api.mvc.{Handler, RequestHeader}
import play.api.routing.Router.Routes
import play.api.routing.{Router, SimpleRouter}

@Singleton
class BlogsRouter @Inject()(noble: Noble) extends SimpleRouter {

  private var prefix: String = ""

  override def routes: Routes = {
    val blogRouters: Seq[Router] = noble.blogs map { blog =>
      val blogPath: String = prefix + blog.config.path
      new BlogRouter(new BlogController()).withPrefix(blog.config.path)
    }

    blogRouters
      .map(_.routes)
      .foldLeft(PartialFunction.empty[RequestHeader, Handler])((r1, r2) => r1.orElse(r2))
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

    case GET((p"/" | p"")) => controller.test
  }

  override def withPrefix(prefix: String): Router =
    if (prefix == "/") self
    else new Router {
      override def routes: Routes = {
        val p: String = if (prefix.endsWith("/")) prefix else prefix + "/"
        val prefixed: PartialFunction[RequestHeader, RequestHeader] = {
          case rh: RequestHeader if rh.path.startsWith(p) || rh.path.equals(prefix) =>
            rh.copy(path = rh.path.drop(p.length - 1))
        }
        Function.unlift(prefixed.lift andThen (_ flatMap self.routes.lift))
      }

      override def documentation: Seq[(String, String, String)] = self.documentation

      override def withPrefix(prefix: String): Router = self.withPrefix(prefix)
    }
}