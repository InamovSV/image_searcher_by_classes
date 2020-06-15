name := "Downloader"

version := "0.1"

scalaVersion := "2.12.8"

assemblyJarName in assembly := "Downloader.jar"

addCompilerPlugin(
  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
)

credentials += Credentials("Artifactory Realm",
  "maven.dataposition.com.ua", "admin", "APxM457xhoVA1TmfHXXqpJMaQFx6TKkzEQfGA")

val promoIvy: MavenRepository = "promo-ivy" at "https://maven.dataposition.com.ua/artifactory/ivy-release-local/"

resolvers += promoIvy

val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-stream" % "2.5.9"
)

val kafkaDeps = Seq(
  "org.apache.kafka" % "kafka-clients" % "0.11.0.3",
  "org.apache.kafka" % "kafka-streams" % "0.11.0.3"
)

val loggingDeps = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  //  "org.slf4j" % "slf4j-simple" % "1.6.4"
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  //  "com.typesafe.scala-logging" % "scala-logging-slf4j_2.11" % "2.1.2"
)

libraryDependencies ++=
  akkaDeps ++
    kafkaDeps

libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.19"

libraryDependencies += "ua.promo" %% "clickhouselibrary" % "0.1"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies ~= {
  _.map(_.exclude("org.slf4j", "slf4j-nop"))
}