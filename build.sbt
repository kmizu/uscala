organization := "com.github.kmizu"

name := "uscala"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.11.8", "2.12.1")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "junit" % "junit" % "4.7" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)


initialCommands in console += {
  Iterator().map("import "+).mkString("\n")
}
