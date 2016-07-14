package com.norcane.noble

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import com.norcane.noble.actors.BlogActor
import play.api.{Configuration, Environment}

@Singleton
class Noble @Inject()(actorSystem: ActorSystem, configuration: Configuration, environment: Environment) {

  lazy val blogs: Seq[BlogDefinition] = {
    def die(message: String) = throw InvalidBlogConfigError(message)

    val blogsConfigKey: String = s"${Keys.ConfigPrefix}.blogs"
    val blogsConfig = configuration.getConfig(s"${Keys.ConfigPrefix}.blogs")
      .getOrElse(die(s"missing blogs configuration under the '$blogsConfigKey'"))
    blogsConfig.subKeys.toSeq map { blogName =>
      val blogConfig: Configuration = blogsConfig.getConfig(blogName)
        .getOrElse(die(s"invalid configuration for blog '$blogName'"))
      val path: String = blogConfig.getString("path").getOrElse("/blog")
      val storageConfig = blogConfig.getConfig("storage")
        .getOrElse(die(s"missing storage configuration for blog '$blogName'"))
      val storageType: String = storageConfig.getString("type")
        .getOrElse(die(s"missing storage type configuration for blog '$blogName'"))
      val storageCfg: Configuration = storageConfig.getConfig("config")
        .getOrElse(die(s"missing configuration for storage type '$storageType' for blog '$blogName'"))
      val blogActor: ActorRef = actorSystem.actorOf(BlogActor.props)

      BlogDefinition(BlogConfig(blogName, path,
        StorageConfig(storageType = storageType, storageCfg)),
        blogActor)
    }
  }

}

case class StorageConfig(storageType: String, config: Configuration)

case class BlogConfig(name: String, path: String, storageConfig: StorageConfig)

case class BlogDefinition(config: BlogConfig, actor: ActorRef)

case class InvalidBlogConfigError(message: String) extends Error(message)
