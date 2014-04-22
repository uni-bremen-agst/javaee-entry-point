name := "soot.plugins.entry-points"

organization := "soot.plugins"

version := "0.0.1-SNAPSHOT"

scalaVersion := Versions.scala

sbtVersion := Versions.sbt

Common.settings

//Faster incremental compilation. See http://scala-lang.org/news/2014/04/21/release-notes-2.11.0.html
//incOptions := incOptions.value.withNameHashing(true)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % Versions.scalaLogging

libraryDependencies += "org.slf4j" % "slf4j-api" % Versions.slf4j

libraryDependencies += "org.eclipse.emf.mwe" % "core" % "1.2.1" % "runtime"

libraryDependencies += "org.eclipse.emf.mwe" % "utils" % "1.3.0"

libraryDependencies += "org.eclipse" % "xpand" % "1.4.0"

libraryDependencies += "com.ibm" % "icu" % "50.1.1"

resolvers +=  "Private Maven Repository" at baseDirectory.value.toURI.toURL + "../../repository"  //baseDirectory.value / "../../repository"
