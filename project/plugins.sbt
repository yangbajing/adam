// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += Resolver.sbtPluginRepo("releases")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
