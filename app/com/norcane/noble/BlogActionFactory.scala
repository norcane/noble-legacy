package com.norcane.noble

import javax.inject.Inject

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.norcane.noble.actors.BlogActor.GetBlog
import com.norcane.noble.api.models._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class BlogActionFactory @Inject()(parser: BodyParsers.Default)
                                 (implicit val executionContext: ExecutionContext) {

  def blogAction(blogActor: ActorRef) = new BlogAction(parser, blogActor)
}

class BlogAction @Inject()(val parser: BodyParsers.Default, blogActor: ActorRef)
                          (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[BlogRequest, AnyContent] {

  private implicit val defaultTimeout = Timeout(10.seconds)

  override def invokeBlock[A](request: Request[A],
                              block: (BlogRequest[A]) => Future[Result]): Future[Result] = {
    import play.api.http.HeaderNames._

    (blogActor ? GetBlog).mapTo[Blog] flatMap { blog =>
      val eTag = eTagFor(blog)
      if (request.headers.get(IF_NONE_MATCH).contains(eTag))
        Future.successful(play.api.mvc.Results.NotModified)
      else
        block(new BlogRequest(request, blog)).map(_.withHeaders(ETAG -> eTag))
    }
  }

  private def eTagFor(blog: Blog): String =
    blog.versionId.take(8) + blog.info.themeName.take(8)
}

