logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.13")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.12")

// allows to check outdated dependencies using 'sbt dependencyUpdates'
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")

// allows to list the dependency tree using the 'sbt dependencyTree'
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.3")