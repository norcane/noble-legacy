logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.1")

// allows to check outdated dependencies using 'sbt dependencyUpdates'
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")

// allows to list the dependency tree using the 'sbt dependencyTree'
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")