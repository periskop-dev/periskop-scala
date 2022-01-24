lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.4"

val versions = new {
  val specs2 = "4.10.5"
  val jackson = "2.11.3"
  val collectionCompat = "2.3.0"
  val scalaj = "2.4.2"
}

organization := "io.github.periskop-dev"
organizationName := "Periskop"
organizationHomepage := Some(url("https://periskop.io/"))
name := "periskop-scala"
description := "Scala low level client for Periskop"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % versions.scalaj,
  "org.scala-lang.modules" %% "scala-collection-compat" % versions.collectionCompat,
  "com.fasterxml.jackson.core" % "jackson-databind" % versions.jackson,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jackson,
  "org.specs2" %% "specs2-core" % versions.specs2 % "test",
  "org.specs2" %% "specs2-mock" % versions.specs2 % "test"
)
crossScalaVersions := Seq(scala212, scala213)
scalaVersion := scala213

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard"
)

// publishing to maven central
scmInfo := Some(
  ScmInfo(
    url("https://github.com/periskop-dev/periskop-scala"),
    "scm:git@github.com:periskop-dev/periskop-scala.git"
  )
)
developers := List(
  Developer(
    id    = "jcreixell",
    name  = "Jorge Creixell",
    email = "jorge.creixell@soundcloud.com",
    url   = url("https://github.com/jcreixell")
  ),
  Developer(
    id    = "marctc",
    name  = "Marc Tuduri",
    email = "marctc@protonmail.com",
    url   = url("https://github.com/marctc")
  )
)

licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/periskop-dev/periskop-scala"))

// Remove all additional repository other than Maven Central from POM
pomIncludeRepository := { _ => false }
publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
publishMavenStyle := true
publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
