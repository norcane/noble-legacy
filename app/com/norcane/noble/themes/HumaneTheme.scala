package com.norcane.noble.themes

import com.norcane.noble.api.{BlogReverseRouter, BlogTheme, BlogThemeFactory}
import com.norcane.noble.api.models.{Blog, BlogPost}
import play.api.i18n.Messages
import play.api.mvc.{Call, RequestHeader}
import play.twirl.api.Html

class HumaneThemeFactory extends BlogThemeFactory {
  override def name: String = HumaneTheme.ThemeName

  override def create: BlogTheme = new HumaneTheme
}


class HumaneTheme extends BlogTheme {

  override def name: String = HumaneTheme.ThemeName

  override def blogPosts(blog: Blog, router: BlogReverseRouter, title: Option[String],
                         posts: Seq[(BlogPost, String)], previous: Option[Call], next: Option[Call])
                        (implicit header: RequestHeader, messages: Messages): Html =
    com.norcane.noble.themes.humane.html.blogPosts(posts)

  override def blogPost(blog: Blog, router: BlogReverseRouter, post: BlogPost, content: String)
                       (implicit header: RequestHeader, messages: Messages): Html = ???
}

object HumaneTheme {
  val ThemeName: String = "humane"
}
