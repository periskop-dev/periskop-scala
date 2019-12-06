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
