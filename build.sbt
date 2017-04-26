lazy val noble = (project in file(".")).enablePlugins(PlayScala)
  .dependsOn(nobleApi).aggregate(nobleApi)

name := "noble"

description := "norcane blog engine"

version := "0.1.0-SNAPSHOT"

organization := "com.norcane.noble"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion in Global := "2.11.11"

autoAPIMappings := true


libraryDependencies ++= Seq(
  jdbc, cache, ws, specs2 % Test,
  "org.typelevel" %% "cats" % "0.9.0",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.7.0.201704051617-r",
  "org.yaml" % "snakeyaml" % "1.18",
  "org.pegdown" % "pegdown" % "1.6.0"
)

unmanagedResourceDirectories in Test += baseDirectory(_ / "target/web/public/test").value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val nobleApi = (project in file("sdk/noble-api"))
  .settings(
    name := "noble-api",
    libraryDependencies ++= Seq(
      specs2 % Test,
      "net.codingwell" %% "scala-guice" % "4.0.1",
      "com.typesafe" % "config" % "1.3.1",
      "org.typelevel" %% "cats" % "0.9.0",
      "com.typesafe.play" %% "play" % "2.5.14"
    )
  )

lazy val minimal = (project in file("examples/minimal"))
  .enablePlugins(PlayScala)
  .dependsOn(noble).aggregate(noble)