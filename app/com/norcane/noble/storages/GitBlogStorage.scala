/*
 *              _     _
 *  _ __   ___ | |__ | | ___
 * | '_ \ / _ \| '_ \| |/ _ \       noble :: norcane blog engine
 * | | | | (_) | |_) | |  __/       Copyright (c) 2016-2017 norcane
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

import java.io.File
import java.time.{LocalDate, ZoneId, ZonedDateTime}
import javax.inject.Singleton

import cats.instances.either._
import cats.syntax.either._
import com.norcane.noble.api._
import com.norcane.noble.api.astral.{Astral, AstralType}
import com.norcane.noble.api.models.{BlogAuthor, BlogInfo, BlogPostMeta, StorageConfig}
import com.norcane.noble.astral.{RawYaml, YamlParser}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ObjectId, RepositoryBuilder}
import org.eclipse.jgit.revwalk.RevWalk
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
                      formatSupports: Map[String, FormatSupport]): Either[BlogStorageError, BlogStorage] = {
    (for {
      cfg <- Either.fromOption(config.config.map(Configuration(_)),
        s"no storage config for storage type '${config.storageType}")
      repoPath <- Either.fromOption(cfg.getString("repoPath"),
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

  import Astral.Defaults._

  val ConfigFileName: String = "_config.yml"
  val PostsDirName: String = "_posts"
  val AssetsDirName: String = "_assets"

  private val FilenameExtractor: Regex = """(.+)\.([^\.]+)""".r
  private val DateAndTitleExtractor: Regex = """(\d{4})-(\d{1,2})-(\d{1,2})-(.+)""".r

  private object AsInt {
    def unapply(string: String): Option[Int] = Try(string.toInt).toOption
  }

  private val repository = new RepositoryBuilder().setGitDir(new File(config.gitRepo, ".git")).build()
  private val git = new Git(repository)

  override def currentVersionId: String = {
    // fetch actual commit from remote (if available)
    config.remote foreach (git.fetch().setRemote(_).call())

    // and return the last commit ID
    val ref = config.remote.map("refs/remotes/" + _ + "/").getOrElse("") + config.branch
    Option(repository.findRef(ref)).map(_.getObjectId.name()).getOrElse(
      throw new RuntimeException(s"Cannot find ref '$ref' in repository '${config.gitRepo}'"))
  }

  override def loadInfo(versionId: String): Either[BlogStorageError, BlogInfo] = {
    implicit val yamlParser = YamlParser.parser

    val info = loadContent(versionId, ConfigFileName)
      .flatMap(content => Astral.parse(RawYaml(content)).toOption).getOrElse(Astral.empty)
    def asEither[T: AstralType](key: String, errMsg: String): Either[BlogStorageError, T] =
      Either.fromOption(info.get[T](key), BlogStorageError(errMsg))

    for {
      title <- asEither[String]("title", s"no blog title specified in $ConfigFileName")
      authors <- asEither[Astral]("authors", s"no authors info specified in $ConfigFileName")
        .flatMap(parseAuthors)
      themeName <- asEither[String]("theme", s"no blog theme name specified in $ConfigFileName")
    } yield BlogInfo(
      title = title,
      subtitle = info.get[String]("subtitle"),
      authors = authors,
      description = info.get[String]("description"),
      copyright = info.get[String]("copyright"),
      themeName = themeName,
      properties = info
    )
  }

  override def loadPostContent(versionId: String, post: BlogPostMeta,
                               placeholders: Map[String, Any]): Either[BlogStorageError, String] = {
    val Some(stream) = loadStream(versionId, s"$PostsDirName/${post.id}")
    for {
      formatSupport <- selectFormatSupport(post.format)
      content <- formatSupport.extractPostContent(stream.stream, post, placeholders)
        .leftMap(err => BlogStorageError(err.message, err.cause))
    } yield content
  }

  override def loadBlogPosts(versionId: String): Either[BlogStorageError, List[BlogPostMeta]] = {
    import cats.instances.list._
    import cats.syntax.traverse._

    ((for (files <- allFilesInPath(versionId, PostsDirName)) yield files map { file =>
      val path = s"$PostsDirName/$file"
      val Some(stream) = loadStream(versionId, path)
      for {
        postRecord <- parsePostRecord(file)
        formatSupport <- selectFormatSupport(postRecord.formatName)
        blogPost <- formatSupport.extractPostMetadata(stream.stream, postRecord)
          .leftMap(err => BlogStorageError(err.message, err.cause))
      } yield blogPost
    }) getOrElse Nil).toList.sequenceU
    // IntelliJ Idea will highlight an error here, but the code is compilable and working,
    // issue is reported here: https://youtrack.jetbrains.com/issue/SCL-9752
  }

  override def loadAsset(versionId: String, path: String): Either[BlogStorageError, ContentStream] = {
    Either.fromOption(loadStream(versionId, AssetsDirName + path),
      BlogStorageError(s"Cannot load asset for path '$path'"))
  }

  private def parseAuthors(authors: Astral): Either[BlogStorageError, Seq[BlogAuthor]] = {
    import cats.instances.list._
    import cats.syntax.traverse._

    if (authors.keys.nonEmpty) {
      (authors.keys map { nickname =>
        val authorAst = Either.fromOption(authors.get[Astral](nickname),
          s"invalid configuration for author with nickname '$nickname'")

        val authorE = for {
          author <- authorAst
          name <- Either.fromOption(author.get[String]("name"),
            s"missing name for author with nickname '$nickname")
        } yield BlogAuthor(
          nickname = nickname,
          name = name,
          biography = author.get[String]("biography"),
          avatar = author.get[String]("avatar"),
          properties = author
        )

        authorE.leftMap(BlogStorageError(_))
        // IntelliJ Idea will highlight an error here, but the code is compilable and working,
        // issue is reported here: https://youtrack.jetbrains.com/issue/SCL-9752
      }).toList.sequenceU
    } else Left(BlogStorageError("At least one author must be defined for each blog"))
  }

  private def selectFormatSupport(formatName: String): Either[BlogStorageError, FormatSupport] =
    Either.fromOption(formatSupports.get(formatName),
      BlogStorageError(s"no format support available for type '$formatName'"))

  private def parsePostRecord(path: String): Either[BlogStorageError, BlogPostRecord] = {
    def parseFilename: Either[BlogStorageError, (String, String)] = path match {
      case FilenameExtractor(filename, extension) => Right((filename, extension))
      case _ => Left(BlogStorageError(s"cannot parse extension for file '$path'"))
    }
    def parseDateAndTitle(filename: String, extension: String): Either[BlogStorageError, BlogPostRecord] =
      filename match {
        case DateAndTitleExtractor(AsInt(year), AsInt(month), AsInt(day), title) =>
          val id: String = s"$filename.$extension"
          val date: ZonedDateTime = LocalDate.of(year, month, day).atStartOfDay(ZoneId.of("UTC"))
          Either.right(BlogPostRecord(id, date, title, title, extension))
        case _ => Left(
          BlogStorageError(s"cannot parse date and title for file '$filename.$extension'"))
      }

    for {
      parsedFilename <- parseFilename
      postRecord <- parseDateAndTitle(parsedFilename._1, parsedFilename._2)
    } yield postRecord
  }

  private def allFilesInPath(versionId: String, path: String): Option[Seq[String]] = {
    val prefixedPath = config.blogPath + path
    scanFiles(versionId, PathFilter.create(prefixedPath)) { treeWalk =>
      @tailrec def extract(list: List[String]): List[String] = {
        if (!treeWalk.next())
          list
        else extract(treeWalk.getPathString.drop(prefixedPath.length + 1) :: list)
      }
      Some(extract(Nil))
    }
  }

  private def loadContent(versionId: String, path: String): Option[String] =
    loadStream(versionId, path) map {
      case ContentStream(stream, length) => try {
        Source.fromInputStream(stream).mkString
      } finally {
        stream.close()
      }
    }

  private def loadStream(versionId: String, path: String): Option[ContentStream] = {
    scanFiles(versionId, PathFilter.create(config.blogPath + path)) { treeWalk =>
      if (!treeWalk.next()) {
        None
      } else {
        val file = repository.open(treeWalk.getObjectId(0))
        Some(ContentStream(file.openStream(), file.getSize))
      }
    }
  }

  private def scanFiles[T](versionId: String, filter: TreeFilter)
                          (block: TreeWalk => Option[T]): Option[T] = {
    val revWalk = new RevWalk(repository)

    try {
      val tree = revWalk.parseCommit(ObjectId.fromString(versionId)).getTree
      val treeWalk = new TreeWalk(repository)
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
}

case class GitStorageConfig(gitRepo: File, blogPath: String, branch: String, remote: Option[String])
