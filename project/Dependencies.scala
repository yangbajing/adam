import sbt._

object Dependencies {
  val versionScala = "2.12.6"

  lazy val _scalaXml = ("org.scala-lang.modules" %% "scala-xml" % "1.1.0").exclude("org.scala-lang", "scala-library")

  lazy val _scalatest = "org.scalatest" %% "scalatest" % "3.0.5"

  val versionAkka = "2.5.12"

  lazy val _akkas = Seq(
    "com.typesafe.akka" %% "akka-slf4j" % versionAkka,
    "com.typesafe.akka" %% "akka-stream" % versionAkka,
    "com.typesafe.akka" %% "akka-remote" % versionAkka,
    "com.typesafe.akka" %% "akka-testkit" % versionAkka % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % versionAkka % Test
  ).map(_.exclude("org.scala-lang.modules", s"scala-java8-compat").cross(CrossVersion.binary))

  val versionAkkaHttp = "10.1.1"
  lazy val _akkaHttps = Seq(
    "com.typesafe.akka" %% "akka-http" % versionAkkaHttp,
    "com.typesafe.akka" %% "akka-http-testkit" % versionAkkaHttp % Test
  ).map(_
    .exclude("com.typesafe.akka", "akka-stream").withCrossVersion(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-stream-testkit").withCrossVersion(CrossVersion.binary))

  lazy val _akkaClusters = Seq(
    "com.typesafe.akka" %% "akka-cluster" % versionAkka,
//    "com.typesafe.akka" %% "akka-cluster-metrics" % versionAkka,
//    "com.typesafe.akka" %% "akka-cluster-sharding" % versionAkka,
  "com.typesafe.akka" %% "akka-cluster-tools" % versionAkka
  )

  lazy val _akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % versionAkka % Test

  lazy val _jetcdCore = "com.coreos" % "jetcd-core" % "0.0.2"

  lazy val _fastjson = "com.alibaba" % "fastjson" % "1.2.46"

  lazy val _config = "com.typesafe" % "config" % "1.3.3"

  lazy val _scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"

  lazy val _logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

}