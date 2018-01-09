/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016-2018 norcane
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
import cats.syntax.either._
import com.norcane.noble.actors.BlogActor
import com.norcane.noble.api._
import com.norcane.noble.api.models.StorageConfig
import com.norcane.noble.models.BlogDefinition
import com.norcane.noble.utils.extendedConfiguration._
import play.api.{Configuration, Environment, Logger}

import scala.collection.immutable

/**
  * Core part of the *noble*, that performs all required bootstrap steps, such as loading blog
  * definitions, discovers available ''blog storages'', ''format supports'' and ''blog themes''.
  *
  * @param actorSystem            Akka actor system
  * @param configuration          Play's configuration
  * @param environment            Play's environment
  * @param storageFactories       set of all available ''blog storage'' factories
  * @param formatSupportFactories set of all available ''format support'' factories
  * @param themeFactories         set of all available ''blog theme'' factories
  */
@Singleton
class Noble @Inject()(actorSystem: ActorSystem, configuration: Configuration,
                      environment: Environment, storageFactories: immutable.Set[BlogStorageFactory],
                      formatSupportFactories: immutable.Set[FormatSupportFactory],
                      themeFactories: immutable.Set[BlogThemeFactory]) {

  private val logger = Logger(getClass)

  lazy val blogs: Seq[BlogDefinition] = loadBlogDefinitions
  lazy val themes: Set[BlogTheme] = loadThemes

  private def loadThemes: immutable.Set[BlogTheme] = themeFactories map (_.create)

  private def loadBlogDefinitions: Seq[BlogDefinition] = {
    logger.info("Loading blog configurations")
    val blogsConfigKey = s"${Keys.Namespace}.blogs"
    val blogsConfigE = configuration.getE[Configuration](s"${Keys.Namespace}.blogs",
      s"missing blogs configuration under the '$blogsConfigKey'")

    val blogDefinitionsE = blogsConfigE map { blogsConfig =>
      blogsConfig.subKeys.toSeq map { blogName =>
        val blogCfgE = blogsConfig.getE[Configuration](blogName,
          s"invalid configuration for blog '$blogName'")

        val blogDefinitionE = for {
          blogCfg <- blogCfgE
          blogConfig <- ConfigParser.parseBlogConfig(blogName, blogCfg)
          storage <- findStorage(blogConfig.storageConfig)
        } yield BlogDefinition(blogConfig, storage, actorSystem.actorOf(
          BlogActor.props(storage, blogConfig, formatSupports)))

        blogDefinitionE.fold(
          error => throw InvalidBlogConfigError(s"cannot initialize blog '$blogName': $error"),
          identity)
      }
    }

    blogDefinitionsE.fold(error => throw InvalidBlogConfigError(error), definitions => {
      val blogNames = definitions map (_.config.name)
      logger.info(s"Following blogs were successfully loaded: ${blogNames.mkString(",")}")
      definitions
    })
  }

  private def formatSupports: Map[String, FormatSupport] =
    (formatSupportFactories map (factory => factory.formatName -> factory.create)).toMap

  private def findStorage(config: StorageConfig): Either[String, BlogStorage] = {
    def available = if (storageFactories.nonEmpty) {
      storageFactories.map(_.storageType).mkString(",")
    } else "-no storages registered-"

    storageFactories.find(_.storageType == config.storageType) match {
      case Some(factory) =>
        factory.create(config, formatSupports).leftMap(_.message)
      case None => Left(
        s"""Cannot find any registered blog storage factory for type
           |'${config.storageType}' (available types are: $available)"""
          .stripMargin.replaceAll("\n", " "))
    }
  }
}

case class InvalidBlogConfigError(message: String) extends Error(message)
