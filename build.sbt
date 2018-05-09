import Commons._
import Dependencies._

lazy val root = Project(id = "mesh-agent-root", base = file("."))
  .aggregate(agent, common)

lazy val agent = project.in(file("agent"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(basicSettings: _*)
  .settings(
    mainClass in Compile := Some("agent.AgentApp"),
    libraryDependencies ++= Seq(
      _jetcdCore
    ) ++ _akkaHttps //++ _akkaClusters
  )

lazy val common = project.in(file("common"))
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      _config,
      _scalaLogging,
      _logbackClassic,
      _fastjson,
      _scalatest % Test
    ) ++ _akkas
  )
