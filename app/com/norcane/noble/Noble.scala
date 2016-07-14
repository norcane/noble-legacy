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
import com.norcane.noble.actors.BlogActor
import com.norcane.noble.models.{BlogConfig, BlogDefinition}
import play.api.{Configuration, Environment}

@Singleton
class Noble @Inject()(actorSystem: ActorSystem, configuration: Configuration,
                      environment: Environment) {

  lazy val blogs: Seq[BlogDefinition] = {
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
          blogConfig <- BlogConfig.fromConfig(blogCfg)
        } yield BlogDefinition(blogConfig, blogActor)

        blogDefinitionXor.fold(
          error => throw InvalidBlogConfigError(s"cannot initialize blog '$blogName': $error"),
          identity)
      }
    }

    blogDefinitionsXor.fold(error => throw InvalidBlogConfigError(error), identity)
  }
}

case class InvalidBlogConfigError(message: String) extends Error(message)
