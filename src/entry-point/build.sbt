name := "soot.plugins.entry-points"

organization := "soot.plugins"

scalaVersion := "2.10.3"

version := "0.0.1-SNAPSHOT"

libraryDependencies += "com.typesafe" % "scalalogging-slf4j_2.10" % "1.1.0"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "org.eclipse.emf.mwe" % "core" % "1.2.1" % "runtime"

libraryDependencies += "org.eclipse.emf.mwe" % "utils" % "1.3.0"

libraryDependencies += "org.eclipse" % "xpand" % "1.4.0"

libraryDependencies += "com.ibm" % "icu" % "50.1.1"

resolvers +=  "Private Maven Repository" at baseDirectory.value.toURI.toURL + "../../repository"  //baseDirectory.value / "../../repository"
