lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.4"

val versions = new {
  val specs2 = "4.7.0"
  val jackson = "2.10.1"
}

name := "periskop-scala"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % versions.jackson,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % versions.jackson,
  "org.specs2" %% "specs2-core" % versions.specs2 % "test",
  "org.specs2" %% "specs2-mock" % versions.specs2 % "test"
)
crossScalaVersions := Seq(scala212, scala213)
scalaVersion := scala213

// publishing to maven central
ThisBuild / organization := "com.soundcloud"
ThisBuild / organizationName := "SoundCloud"
ThisBuild / organizationHomepage := Some(url("https://developers.soundcloud.com/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/soundcloud/periskop-scala"),
    "scm:git@github.com:soundcloud/periskop-scala.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "jcreixell",
    name  = "Jorge Creixell",
    email = "jorge.creixell@soundcloud.com",
    url   = url("https://github.com/jcreixell")
  ),
  Developer(
    id    = "dziemba",
    name  = "Niko Dziemba",
    email = "niko.dziemba@soundcloud.com",
    url   = url("https://github.com/dziemba")
  )
)

usePgpKeyHex("612C04F1EFE66FB7")
ThisBuild / description := "Scala low level client for Periskop"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/soundcloud/periskop-scala"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / publishMavenStyle := true


import ReleaseTransformations._

releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  // For non cross-build projects, use releaseStepCommand("publishSigned")
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
