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

import akka.actor.ActorSystem
import cats.data.Xor
import com.norcane.noble.actors.BlogActor
import com.norcane.noble.api._
import com.norcane.noble.api.models.StorageConfig
import com.norcane.noble.models.BlogDefinition
import play.api.{Configuration, Environment, Logger}

import scala.collection.immutable

@Singleton
class Noble @Inject()(actorSystem: ActorSystem, configuration: Configuration,
                      environment: Environment, storageFactories: immutable.Set[BlogStorageFactory],
                      formatSupportFactories: immutable.Set[FormatSupportFactory],
                      themeFactories: immutable.Set[BlogThemeFactory]) {

  private val logger: Logger = Logger(getClass)

  lazy val blogs: Seq[BlogDefinition] = loadBlogDefinitions
  lazy val themes: immutable.Set[BlogTheme] = loadThemes

  private def loadThemes: immutable.Set[BlogTheme] = themeFactories map (_.create)

  private def loadBlogDefinitions: Seq[BlogDefinition] = {
    logger.info("Loading blog configurations")
    val blogsConfigKey: String = s"${Keys.Namespace}.blogs"
    val blogsConfigXor: String Xor Configuration = Xor.fromOption(
      configuration.getConfig(s"${Keys.Namespace}.blogs"),
      s"missing blogs configuration under the '$blogsConfigKey'")

    val blogDefinitionsXor: Xor[String, Seq[BlogDefinition]] = blogsConfigXor map { blogsConfig =>
      blogsConfig.subKeys.toSeq map { blogName =>
        val blogCfgXor: String Xor Configuration = Xor.fromOption(
          blogsConfig.getConfig(blogName), s"invalid configuration for blog '$blogName'")

        val blogDefinitionXor: Xor[String, BlogDefinition] = for {
          blogCfg <- blogCfgXor
          blogConfig <- ConfigParser.parseBlogConfig(blogName, blogCfg)
          storage <- findStorage(blogConfig.storageConfig)
        } yield BlogDefinition(blogConfig, storage, actorSystem.actorOf(
          BlogActor.props(storage, blogConfig, formatSupports)))

        blogDefinitionXor.fold(
          error => throw InvalidBlogConfigError(s"cannot initialize blog '$blogName': $error"),
          identity)
      }
    }

    blogDefinitionsXor.fold(error => throw InvalidBlogConfigError(error), definitions => {
      val blogNames: Seq[String] = definitions map (_.config.name)
      logger.info(s"Following blogs were successfully loaded: ${blogNames.mkString(",")}")
      definitions
    })
  }

  private def formatSupports: Map[String, FormatSupport] =
    (formatSupportFactories map (factory => factory.postType -> factory.create)).toMap

  private def findStorage(config: StorageConfig): String Xor BlogStorage = {
    def available: String = if (storageFactories.nonEmpty)
      storageFactories.map(_.storageType).mkString(",")
    else "-no storages registered-"

    storageFactories.find(_.storageType == config.storageType) match {
      case Some(factory) =>
        factory.create(config, formatSupports).leftMap(_.message)
      case None => Xor.left(
        s"""Cannot find any registered blog storage factory for type
            |'${config.storageType}' (available types are: $available)"""
          .stripMargin.replaceAll("\n", " "))
    }
  }
}

case class InvalidBlogConfigError(message: String) extends Error(message)
