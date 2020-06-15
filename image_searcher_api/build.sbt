
name := "image_searcher_api"

version := "0.1"

scalaVersion := "2.12.11"

resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val logbackV = "1.2.3"
val scalaLoggingV = "3.9.2"
val typesafeConfigV = "1.3.1"
val akkaV = "2.6.5"
val akkaHttpV = "10.1.12"
val scalatestV = "3.1.2"
val chimneyV = "0.5.0"
val slickV = "3.3.2"
val circeV = "0.12.2"
val httpCirceV = "1.32.0"
val slickPgV = "0.19.0"
val authentikatJwtV = "0.4.5"

lazy val dependencies = Seq(
  "de.heikoseeberger" %% "akka-http-circe" % httpCirceV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,
  "com.typesafe" % "config" % typesafeConfigV,
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-http" % akkaHttpV,
  "io.scalaland" %% "chimney" % chimneyV,
  "com.typesafe.slick" %% "slick" % slickV,
  "org.scalatest" %% "scalatest" % scalatestV,
  "com.github.tminglei" %% "slick-pg" % slickPgV,
  "com.github.tminglei" %% "slick-pg_circe-json" % slickPgV,
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
  "com.github.tminglei" %% "slick-pg_circe-json" % slickPgV,
  "org.postgresql" % "postgresql" % "42.2.2",
  "com.github.tminglei" %% "slick-pg" % slickPgV,
  "com.typesafe.slick" %% "slick-hikaricp" % slickV,
) ++ Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-generic-extras"
).map(_ % circeV)

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

libraryDependencies ++= dependencies

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

scalacOptions ++= compilerOptions
autoCompilerPlugins := true

assemblyJarName in assembly := name.value + ".jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case "application.conf" => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
