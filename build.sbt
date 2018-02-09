
// Scala version used
scalaVersion in Global := "2.12.4"
crossScalaVersions in Global := Seq("2.12.4", "2.11.11")

// Project details
name := "noble"
description := "norcane blog engine"
version in Global := "0.2.0-SNAPSHOT"
organization in Global := "com.norcane.noble"
licenses in Global += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage in Global := Some(url("https://github.com/norcane/noble"))

// More info for Maven Central
developers in Global := List(
  Developer(
    id = "vaclav.svejcar",
    name = "Vaclav Svejcar",
    email = "vaclav.svejcar@gmail.com",
    url = url("https://github.com/vaclavsvejcar")
  )
)

scmInfo in Global := Some(
  ScmInfo(
    url("https://github.com/norcane/noble"),
    "scm:git@github.com:norcane/noble.git"
  )
)

// Bintray configuration
bintrayOrganization := Some("norcane")
bintrayRepository := "noble"

autoAPIMappings := true

unmanagedResourceDirectories in Test += baseDirectory(_ / "target/web/public/test").value

// Project definition - the root noble project
lazy val noble = (project in file(".")).enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice, specs2 % Test,
      "org.typelevel" %% "cats-core" % "1.0.1",
      "org.eclipse.jgit" % "org.eclipse.jgit" % "4.10.0.201712302008-r",
      "org.yaml" % "snakeyaml" % "1.19",
      "com.vladsch.flexmark" % "flexmark-all" % "0.30.0"
    )
  )
  .dependsOn(nobleApi).aggregate(nobleApi)

// Project definition - noble API
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

// Project definition - minimal standalone example blog
lazy val minimal = (project in file("examples/minimal"))
  .enablePlugins(PlayScala)
  .dependsOn(noble).aggregate(noble)

// We're using kind projector in order to reduce some boilerplate code
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6" cross CrossVersion.binary)

scalacOptions in Global := Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-Ypartial-unification"
)
