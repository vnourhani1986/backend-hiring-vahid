import Dependencies.{test, _}

name := "backend-hiring-vahid"

version := "0.1"

scalaVersion := "2.13.6"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++=
      catsEffect ++
        monix ++
        zio ++
        circe ++
        http4s ++
        sangria ++
        scalaScraper ++
        postgres ++
        quillJdbc ++
        pureConfig ++
        log ++
        test
  )

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-encoding",
  "UTF-8",
  "-Xfatal-warnings",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ylog-classpath"
//  "-Ypartial-unification"
)
