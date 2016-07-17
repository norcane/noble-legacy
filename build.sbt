lazy val noble = (project in file(".")).enablePlugins(PlayScala)
  .dependsOn(nobleApi).aggregate(nobleApi)

name := "noble"

description := "norcane blog engine"

version := "0.1.0-SNAPSHOT"

organization := "com.norcane"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion in Global := "2.11.8"

libraryDependencies ++= Seq(
  jdbc, cache, ws, specs2 % Test,
  "org.typelevel" %% "cats" % "0.6.1"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

lazy val nobleApi = (project in file("sdk/noble-api"))
  .settings(
    libraryDependencies ++= Seq(
      "net.codingwell" %% "scala-guice" % "4.0.1",
      "com.typesafe" % "config" % "1.3.0"
    )
  )

lazy val minimal = (project in file("examples/minimal"))
  .enablePlugins(PlayScala)
  .dependsOn(noble).aggregate(noble)