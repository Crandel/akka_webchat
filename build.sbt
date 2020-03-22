lazy val akkaHttpVersion = "10.1.11"
lazy val akkaVersion     = "2.6.4"
lazy val circeVersion    = "0.13.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "web.chat",
      scalaVersion := "2.13.1"
    )),
    name := "Test Akka Websocket",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core"       % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "de.heikoseeberger" %% "akka-http-circe"      % "1.31.0",
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % "test",
      "org.scalatest"     %% "scalatest"            % "3.1.0" % "test"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
