name := "whisperers"

version := "1.0"

scalaVersion := "2.11.8"


val akkaVersion: String = "2.4.11"
libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.5.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "org.specs2" %% "specs2-core" % "3.8.5" % "test"
)