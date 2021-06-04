lazy val commonSettings = Seq(
  libraryDependencies += compilerPlugin(("org.typelevel" %% "kind-projector" % "0.13.0").cross(CrossVersion.patch)),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1") :: Nil
      case _ => Nil
    }
  },
  Compile / compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions := {
    val opts = scalacOptions.value :+ "-Wconf:src=src_managed/.*:s,any:wv"

    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) => opts.filterNot(Set("-Xfatal-warnings"))
      case _ => opts
    }
  },
  Test / fork := true,
  resolvers += Resolver.sonatypeRepo("releases"),
  ThisBuild / evictionErrorLevel := Level.Warn,
)

lazy val noPublishSettings =
  commonSettings ++ Seq(publish := {}, publishArtifact := false, publishTo := None, publish / skip := true)

lazy val publishSettings = commonSettings ++ Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ =>
    false
  },
  Test / publishArtifact := false
)

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .settings(name := "Trace4Cats Http4s")
  .aggregate(`http4s-client`, `http4s-common`, `http4s-server`)

lazy val `http4s-client` = (project in file("modules/http4s-client"))
  .settings(publishSettings)
  .settings(
    name := "trace4cats-http4s-client",
    libraryDependencies ++= Seq(Dependencies.trace4catsInject, Dependencies.http4sClient),
    libraryDependencies ++= Seq(Dependencies.trace4catsExporterCommon, Dependencies.trace4catsTestkit).map(_ % Test)
  )
  .dependsOn(`http4s-common` % "compile->compile;test->test")

lazy val `http4s-common` = (project in file("modules/http4s-common"))
  .settings(publishSettings)
  .settings(
    name := "trace4cats-http4s-common",
    libraryDependencies ++= Seq(Dependencies.trace4catsModel, Dependencies.http4sCore, Dependencies.http4sDsl),
    libraryDependencies ++= Seq(
      Dependencies.trace4catsBase,
      Dependencies.trace4catsCore,
      Dependencies.trace4catsTestkit
    ).map(_ % Test)
  )

lazy val `http4s-server` = (project in file("modules/http4s-server"))
  .settings(publishSettings)
  .settings(
    name := "trace4cats-http4s-server",
    libraryDependencies ++= Seq(Dependencies.trace4catsInject),
    libraryDependencies ++= Seq(
      Dependencies.http4sClient,
      Dependencies.trace4catsExporterCommon,
      Dependencies.trace4catsTestkit
    ).map(_ % Test)
  )
  .dependsOn(`http4s-common` % "compile->compile;test->test")
