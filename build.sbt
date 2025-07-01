import sbt.*
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / majorVersion := 1
ThisBuild / scalacOptions += "-Xfatal-warnings"

val appName = "self-assessment-biss-api"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= List(
      "-Wconf:src=routes/.*:silent",
      "-feature"
    )
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Compile / unmanagedClasspath += baseDirectory.value / "resources"
  )
  .settings(CodeCoverageSettings.settings: _*)
  .settings(PlayKeys.playDefaultPort := 9785)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings() ++ ScalafmtPlugin.scalafmtConfigSettings)
  .settings(
    Test / fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml")
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
