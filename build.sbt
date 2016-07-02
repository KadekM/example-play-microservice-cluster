import sbt._

name := "spring-b2b-backend"

organization := "com.marekkadek"

scalaVersion in ThisBuild := "2.11.8"

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers  += "Online Play Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/"

val akkaVersion = "2.4.7"

lazy val deps = Seq(
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test
)


lazy val common = Project(id = "common", base = file("modules/common"))

lazy val fooService = Project(id = "foo", base = file("modules/foo"))
    .enablePlugins(PlayScala)
    .settings(libraryDependencies ++= deps)
      .dependsOn(common % "compile->compile")

lazy val barService = Project(id = "bar", base = file("modules/bar"))
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ++= deps)
  .dependsOn(common % "compile->compile")

lazy val tests = Project(
  id = "tests",
  base = file("modules/tests")
)
  .settings(SbtMultiJvm.multiJvmSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "2.2.6" % Test),
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target,
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    },
    licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))
  )
  .configs(MultiJvm)
  .dependsOn(fooService % "test->compile;test->test")
  .dependsOn(barService % "test->compile;test->test")
