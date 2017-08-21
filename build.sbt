lazy val noble = (project in file(".")).enablePlugins(PlayScala)
  .dependsOn(nobleApi).aggregate(nobleApi)

name := "noble"

description := "norcane blog engine"

version in Global := "0.2.0-SNAPSHOT"

organization in Global := "com.norcane.noble"

licenses in Global += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion in Global := "2.12.3"

crossScalaVersions in Global := Seq("2.12.3", "2.11.11")

autoAPIMappings := true

bintrayOrganization := Some("norcane")

bintrayRepository := "noble"

libraryDependencies ++= Seq(
  guice, specs2 % Test,
  "org.typelevel" %% "cats" % "0.9.0",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.8.0.201706111038-r",
  "org.yaml" % "snakeyaml" % "1.18",
  "com.vladsch.flexmark" % "flexmark-all" % "0.26.4"
)

unmanagedResourceDirectories in Test += baseDirectory(_ / "target/web/public/test").value

lazy val nobleApi = (project in file("sdk/noble-api"))
  .settings(
    name := "noble-api",
    libraryDependencies ++= Seq(
      specs2 % Test,
      "net.codingwell" %% "scala-guice" % "4.1.0",
      "com.typesafe" % "config" % "1.3.1",
      "org.typelevel" %% "cats" % "0.9.0",
      "com.typesafe.play" %% "play" % "2.6.3"
    ),
    bintrayOrganization := Some("norcane"),
    bintrayRepository := "noble"
  )

lazy val minimal = (project in file("examples/minimal"))
  .enablePlugins(PlayScala)
  .dependsOn(noble).aggregate(noble)
