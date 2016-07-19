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

import akka.actor.{ActorRef, ActorSystem}
import cats.data.Xor
import com.norcane.api.BlogStorageFactory
import com.norcane.noble.actors.BlogActor
import com.norcane.noble.models.BlogDefinition
import play.api.{Configuration, Environment, Logger}

import scala.collection.immutable

@Singleton
class Noble @Inject()(actorSystem: ActorSystem, configuration: Configuration,
                      environment: Environment, storages: immutable.Set[BlogStorageFactory]) {

  private val logger: Logger = Logger(getClass)

  lazy val blogs: Seq[BlogDefinition] = loadBlogDefinitions

  private def loadBlogDefinitions: Seq[BlogDefinition] = {
    logger.info("Loading blog configurations")
    val blogsConfigKey: String = s"${Keys.ConfigPrefix}.blogs"
    val blogsConfigXor: String Xor Configuration = Xor.fromOption(
      configuration.getConfig(s"${Keys.ConfigPrefix}.blogs"),
      s"missing blogs configuration under the '$blogsConfigKey'")

    val blogDefinitionsXor: Xor[String, Seq[BlogDefinition]] = blogsConfigXor map { blogsConfig =>
      blogsConfig.subKeys.toSeq map { blogName =>
        val blogCfgXor: String Xor Configuration = Xor.fromOption(
          blogsConfig.getConfig(blogName), s"invalid configuration for blog '$blogName'")
        val blogActor: ActorRef = actorSystem.actorOf(BlogActor.props)

        val blogDefinitionXor: Xor[String, BlogDefinition] = for {
          blogCfg <- blogCfgXor
          blogConfig <- ConfigParser.parseBlogConfig(blogName, blogCfg)
        } yield BlogDefinition(blogConfig, blogActor)

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
}

case class InvalidBlogConfigError(message: String) extends Error(message)
