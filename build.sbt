val versions = new {
  val specs2 = "4.7.0"
}

name := "periskop-scala"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % versions.specs2 % "test",
  "org.specs2" %% "specs2-mock" % versions.specs2 % "test"
)
