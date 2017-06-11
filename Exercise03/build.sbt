name := "Reliable Channels and Reliable Broadcast Exercise"

version := "1.0"

scalaVersion := "2.12.2"

parallelExecution in ThisBuild := false

libraryDependencies += "com.twitter" % "chill_2.12" % "0.9.2"
libraryDependencies += "com.twitter" % "chill-bijection_2.12" % "0.9.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

libraryDependencies += "org.codehaus.groovy" % "groovy-all" % "2.4.11"