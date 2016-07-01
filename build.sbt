import sbt._

name := "spring-b2b-backend"

organization := "com.marekkadek"

scalaVersion in ThisBuild := "2.11.8"

scalafmtConfig in ThisBuild := Some(file(".scalafmt"))

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers  += "Online Play Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/"

lazy val deps = Seq("com.typesafe.akka" %% "akka-cluster" % "2.4.7",
"com.typesafe.akka" %% "akka-contrib" % "2.4.7"
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
