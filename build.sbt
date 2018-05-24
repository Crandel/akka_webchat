lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"
lazy val circeVersion = "0.9.3"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "test",
      scalaVersion    := "2.12.5"
    )),
    name := "Test Akka Websocket",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core"       % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "de.heikoseeberger" %% "akka-http-circe"      % "1.20.1"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
