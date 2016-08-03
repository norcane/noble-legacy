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
import java.time.LocalDate
import javax.inject.Singleton

import cats.data.Xor
import com.norcane.noble.api._
import com.norcane.noble.api.models.{BlogInfo, BlogPost, StorageConfig}
import com.norcane.noble.utils.{Yaml, YamlValue}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ObjectId, Repository, RepositoryBuilder}
import org.eclipse.jgit.revwalk.{RevTree, RevWalk}
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.{PathFilter, TreeFilter}
import play.api.Configuration

import scala.annotation.tailrec
import scala.io.Source
import scala.util.Try
import scala.util.matching.Regex


@Singleton
class GitBlogStorageFactory extends BlogStorageFactory {

  override def storageType: String = "git"

  override def create(config: StorageConfig,
                      formatSupports: Map[String, FormatSupport]): BlogStorageError Xor BlogStorage = {
    (for {
      cfg <- Xor.fromOption(config.config.map(Configuration(_)),
        s"no storage config for storage type '${config.storageType}")
      repoPath <- Xor.fromOption(cfg.getString("repoPath"),
        s"no Git repo path found for storage type '${config.storageType}")
    } yield new GitBlogStorage(GitStorageConfig(
      gitRepo = new File(repoPath),
      blogPath = cfg.getString("blogPath").getOrElse(""),
      branch = cfg.getString("branch").getOrElse("master"),
      remote = cfg.getString("remote")
    ), formatSupports)) leftMap (BlogStorageError(_))
  }
}

class GitBlogStorage(config: GitStorageConfig,
                     formatSupports: Map[String, FormatSupport]) extends BlogStorage {

  import Yaml.Defaults._

  val ConfigFileName = "_config.yml"
  val PostsDirName = "_posts"

  private val FilenameExtractor: Regex = """(.+)\.([^\.]+)""".r
  private val DateAndTitleExtractor: Regex = """(\d{4})-(\d{1,2})-(\d{1,2})-(.+)""".r

  private object AsInt {
    def unapply(string: String): Option[Int] = Try(string.toInt).toOption
  }

  private val repository: Repository = new RepositoryBuilder()
    .setGitDir(new File(config.gitRepo, ".git")).build()
  private val git: Git = new Git(repository)

  override def currentHash: String = {
    val ref: String = config.remote.map("refs/remotes/" + _ + "/").getOrElse("") + config.branch
    Option(repository.findRef(ref)).map(_.getObjectId.name()).getOrElse(
      throw new RuntimeException(s"Cannot find ref '$ref' in repository '${config.gitRepo}'"))
  }

  override def loadInfo(hash: String): BlogStorageError Xor BlogInfo = {
    val yaml: Yaml = loadContent(hash, ConfigFileName)
      .flatMap(content => Yaml.parse(content).toOption).getOrElse(Yaml.empty)
    def asXor[T: YamlValue](key: String, errMsg: String): BlogStorageError Xor T =
      Xor.fromOption(yaml.get[T](key), BlogStorageError(errMsg))

    for {
      title <- asXor[String]("title", s"no blog title specified in $ConfigFileName")
      author <- asXor[String]("author", s"no blog author specified in $ConfigFileName")
      themeName <- asXor[String]("theme", s"no blog theme name specified in $ConfigFileName")
    } yield BlogInfo(
      title = title,
      subtitle = yaml.get[String]("subtitle"),
      author = author,
      description = yaml.get[String]("description"),
      themeName = themeName
    )
  }

  override def loadPostContent(hash: String, post: BlogPost): BlogStorageError Xor String = {
    val Some(stream) = loadStream(hash, s"$PostsDirName/${post.id}")
    for {
      formatSupport <- selectFormatSupport(post.format)
      content <- formatSupport.extractPostContent(stream.stream, post)
        .leftMap(err => BlogStorageError(err.message, err.cause))
    } yield content
  }

  override def loadBlogPosts(hash: String): BlogStorageError Xor List[BlogPost] = {
    import cats.std.list._
    import cats.syntax.traverse._

    ((for (files <- allFilesInPath(hash, PostsDirName)) yield files map { file =>
      val path: String = s"$PostsDirName/$file"
      val Some(stream) = loadStream(hash, path)
      for {
        postRecord <- parsePostRecord(file)
        formatSupport <- selectFormatSupport(postRecord.postType)
        blogPost <- formatSupport.extractPostMetadata(stream.stream, postRecord)
          .leftMap(err => BlogStorageError(err.message, err.cause))
      } yield blogPost
    }) getOrElse Nil).toList.sequenceU
    // IntelliJ Idea will highlight an error here, but the code is compilable and working,
    // issue is reported here: https://youtrack.jetbrains.com/issue/SCL-9752
  }

  private def selectFormatSupport(postType: String): BlogStorageError Xor FormatSupport =
    Xor.fromOption(formatSupports.get(postType),
      BlogStorageError(s"no format support available for type '$postType'"))

  private def parsePostRecord(path: String): BlogStorageError Xor BlogPostRecord = {
    def parseFilename: BlogStorageError Xor (String, String) = path match {
      case FilenameExtractor(filename, extension) => Xor.right((filename, extension))
      case _ => Xor.left(BlogStorageError(s"cannot parse extension for file '$path'"))
    }
    def parseDateAndTitle(filename: String, extension: String): BlogStorageError Xor BlogPostRecord =
      filename match {
        case DateAndTitleExtractor(AsInt(year), AsInt(month), AsInt(day), title) =>
          val id: String = s"$filename.$extension"
          Xor.right(BlogPostRecord(id, LocalDate.of(year, month, day), title, extension))
        case _ => Xor.left(
          BlogStorageError(s"cannot parse date and title for file '$filename.$extension'"))
      }

    for {
      parsedFilename <- parseFilename
      postRecord <- parseDateAndTitle(parsedFilename._1, parsedFilename._2)
    } yield postRecord
  }

  private def allFilesInPath(hash: String, path: String): Option[Seq[String]] = {
    val prefixedPath: String = config.blogPath + path
    scanFiles(hash, PathFilter.create(prefixedPath)) { treeWalk =>
      @tailrec def extract(list: List[String]): List[String] = {
        if (!treeWalk.next())
          list
        else extract(treeWalk.getPathString.drop(prefixedPath.length + 1) :: list)
      }
      Some(extract(Nil))
    }
  }

  private def loadContent(hash: String, path: String): Option[String] = loadStream(hash, path) map {
    case ContentStream(stream, length) => try {
      Source.fromInputStream(stream).mkString
    } finally {
      stream.close()
    }
  }

  private def loadStream(hash: String, path: String): Option[ContentStream] = {
    scanFiles(hash, PathFilter.create(config.blogPath + path)) { treeWalk =>
      if (!treeWalk.next()) {
        None
      } else {
        val file = repository.open(treeWalk.getObjectId(0))
        Some(ContentStream(file.openStream(), file.getSize))
      }
    }
  }

  private def scanFiles[T](hash: String, filter: TreeFilter)(block: TreeWalk => Option[T]): Option[T] = {
    val revWalk: RevWalk = new RevWalk(repository)

    try {
      val tree: RevTree = revWalk.parseCommit(ObjectId.fromString(hash)).getTree
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

case class GitStorageConfig(gitRepo: File, blogPath: String, branch: String, remote: Option[String])
