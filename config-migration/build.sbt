scalaVersion := "2.9.2"

name := "config-migration"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.10", 
  "net.sf.saxon" % "saxon" % "9.1.0.8b-patch",
  "net.sf.saxon" % "saxon-dom" % "9.1.0.8b",
  "xalan" % "xalan" % "2.7.1",
  "commons-io" % "commons-io" % "2.1",
  "log4j" % "log4j" % "1.2.13",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1-seq"
  )

resolvers <+= (baseDirectory) { (b) =>  "local" at "file://"+b+"/../maven_repo" }