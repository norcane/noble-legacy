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

package com.norcane.noble.storages

import java.io.{File, InputStream}
import javax.inject.Singleton

import cats.data.Xor
import com.norcane.api.models.{BlogInfo, StorageConfig}
import com.norcane.api.{BlogStorage, BlogStorageError, BlogStorageFactory}
import com.norcane.noble.utils.{Yaml, YamlValue}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ObjectId, Repository, RepositoryBuilder}
import org.eclipse.jgit.revwalk.{RevTree, RevWalk}
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.{PathFilter, TreeFilter}
import play.api.Configuration

import scala.io.Source


@Singleton
class GitBlogStorageFactory extends BlogStorageFactory {
  override def storageType: String = "git"

  override def create(config: StorageConfig): BlogStorageError Xor BlogStorage = {
    (for {
      cfg <- Xor.fromOption(config.config.map(Configuration(_)),
        s"no storage config for storage type '${config.storageType}")
      repoPath <- Xor.fromOption(cfg.getString("repoPath"),
        s"no Git repo path found for storage type '${config.storageType}")
    } yield new GitBlogStorage(GitStorageConfig(
      gitRepo = new File(repoPath),
      blogPath = cfg.getString("blogPath").getOrElse("."),
      branch = cfg.getString("branch").getOrElse("master"),
      remote = cfg.getString("remote").getOrElse("origin")
    ))) leftMap (BlogStorageError(_))
  }
}

class GitBlogStorage(config: GitStorageConfig) extends BlogStorage {

  import Yaml.Defaults._

  val ConfigFileName = "_config.yml"

  private val repository: Repository = new RepositoryBuilder()
    .setGitDir(new File(config.gitRepo, ".git")).build()
  private val git: Git = new Git(repository)

  override val usedHash: String = currentHash

  override def currentHash: String = {
    val ref: String = "refs/remotes/" + config.remote + "/" + config.branch
    Option(repository.findRef(ref)).map(_.getObjectId.name()).getOrElse(
      throw new RuntimeException(s"Cannot find ref '$ref' in repository '${config.gitRepo}'"))
  }

  override def loadInfo: BlogStorageError Xor BlogInfo = {
    val yaml: Yaml = loadContent(ConfigFileName)
      .flatMap(content => Yaml.parse(content).toOption).getOrElse(Yaml.empty)
    def asXor[T: YamlValue](key: String, errMsg: String): BlogStorageError Xor T =
      Xor.fromOption(yaml.get[T](key), BlogStorageError(errMsg))

    for {
      title <- asXor[String]("title", "no blog title specified")
      author <- asXor[String]("author", "no blog author specified")
      themeName <- asXor[String]("author", "no blog theme name specified")
    } yield BlogInfo(
      title = title,
      subtitle = yaml.get[String]("subtitle"),
      author = author,
      description = yaml.get[String]("description"),
      themeName = themeName
    )
  }

  private def loadContent(path: String): Option[String] = loadStream(path) map {
    case ContentStream(stream, length) => try {
      Source.fromInputStream(stream).mkString
    } finally {
      stream.close()
    }
  }

  private def loadStream(path: String): Option[ContentStream] = {
    scanFiles(PathFilter.create(config.blogPath + path)) { treeWalk =>
      if (!treeWalk.next()) {
        None
      } else {
        val file = repository.open(treeWalk.getObjectId(0))
        Some(ContentStream(file.openStream(), file.getSize))
      }
    }
  }

  private def scanFiles[T](filter: TreeFilter)(block: TreeWalk => Option[T]): Option[T] = {
    val revWalk: RevWalk = new RevWalk(repository)

    try {
      val tree: RevTree = revWalk.parseCommit(ObjectId.fromString(usedHash)).getTree
      val treeWalk: TreeWalk = new TreeWalk(repository)
      try {
        treeWalk.addTree(tree)
        treeWalk.setRecursive(true)
        treeWalk.setFilter(filter)
        block(treeWalk)
      } finally {
        treeWalk.close()
      }
    } finally {
      revWalk.dispose()
    }
  }

  private case class ContentStream(stream: InputStream, length: Long)

}

case class GitStorageConfig(gitRepo: File, blogPath: String, branch: String, remote: String)
