lazy val noble = (project in file(".")).enablePlugins(PlayScala)
  .dependsOn(nobleApi).aggregate(nobleApi)

name := "noble"

description := "norcane blog engine"

version in Global := "0.2.0-SNAPSHOT"

organization in Global := "com.norcane.noble"

licenses in Global += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion in Global := "2.12.4"

crossScalaVersions in Global := Seq("2.12.4", "2.11.11")

autoAPIMappings := true

bintrayOrganization := Some("norcane")

bintrayRepository := "noble"

libraryDependencies ++= Seq(
  guice, specs2 % Test,
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.10.0.201712302008-r",
  "org.yaml" % "snakeyaml" % "1.19",
  "com.vladsch.flexmark" % "flexmark-all" % "0.28.34"
)

unmanagedResourceDirectories in Test += baseDirectory(_ / "target/web/public/test").value

lazy val nobleApi = (project in file("sdk/noble-api"))
  .settings(
    name := "noble-api",
    libraryDependencies ++= Seq(
      specs2 % Test,
      "net.codingwell" %% "scala-guice" % "4.1.1",
      "com.typesafe" % "config" % "1.3.2",
      "org.typelevel" %% "cats-core" % "1.0.1",
      "com.typesafe.play" %% "play" % "2.6.11"
    ),
    bintrayOrganization := Some("norcane"),
    bintrayRepository := "noble"
  )

lazy val minimal = (project in file("examples/minimal"))
  .enablePlugins(PlayScala)
  .dependsOn(noble).aggregate(noble)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.5")
