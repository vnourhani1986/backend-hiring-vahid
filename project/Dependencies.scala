import sbt._

object Dependencies {

  object Versions {
    val circe = "0.12.3"
    val http4s = "1.0.0-M9"
    val sangria = "2.0.0"
    val scalaScraper = "2.2.1"
    val postgres = "42.2.8"
    val quillJdbc = "3.9.0"
    val logback = "1.2.3"
    val pureConfig = "0.14.0"
    val `catsEffectScalaTest` = "0.5.2"
  }

  val `cats-effect` = Seq(
    "org.typelevel" %% "cats-effect" % "2.2.0" withSources () withJavadoc (),
    "io.chrisdavenport" %% "cats-effect-time" % "0.1.2"
  )

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  val http4s = Seq(
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-dsl"
  ).map(_ % Versions.http4s)

  val log = Seq(
    "ch.qos.logback" % "logback-classic"
  ).map(_ % Versions.logback)

  val test = Seq(
    "com.codecommit" %% "cats-effect-testing-scalatest" % Versions.`catsEffectScalaTest` % "test"
  )

  val pureConfig = Seq(
    "com.github.pureconfig" %% "pureconfig",
    "com.github.pureconfig" %% "pureconfig-cats-effect"
  ).map(_ % Versions.pureConfig)

  val sangria = Seq("org.sangria-graphql" %% "sangria" % Versions.sangria)

  val scalaScraper = Seq(
    "net.ruippeixotog" %% "scala-scraper" % Versions.scalaScraper
  )

  val postgres = Seq("org.postgresql" % "postgresql" % Versions.postgres)

  val quillJdbc = Seq("io.getquill" %% "quill-jdbc" % Versions.quillJdbc)
}
