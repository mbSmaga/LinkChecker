name := "LinkChecker"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
  "com.ning" % "async-http-client" % "1.7.19",
  "org.jsoup" % "jsoup" % "1.8.1"
)

mainClass in (Compile, run) := Some("akka.Main")