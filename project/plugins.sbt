logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.15")

//addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.1")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.8")

// allows to check outdated dependencies using 'sbt dependencyUpdates'
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.0")

// allows to list the dependency tree using the 'sbt dependencyTree'
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")