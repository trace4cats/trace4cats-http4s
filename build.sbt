lazy val commonSettings = Seq(
  Compile / compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(compilerPlugin(Dependencies.kindProjector), compilerPlugin(Dependencies.betterMonadicFor))
      case _ => Seq.empty
    }
  },
  scalacOptions += {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => "-Wconf:any:wv"
      case _ => "-Wconf:any:v"
    }
  },
  Test / fork := true,
  resolvers ++= Resolver.sonatypeOssRepos("releases"),
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
    libraryDependencies ++= Seq(Dependencies.trace4catsCore, Dependencies.http4sClient),
    libraryDependencies ++= Seq(Dependencies.trace4catsKernel, Dependencies.trace4catsTestkit).map(_ % Test)
  )
  .dependsOn(`http4s-common` % "compile->compile;test->test")

lazy val `http4s-common` = (project in file("modules/http4s-common"))
  .settings(publishSettings)
  .settings(
    name := "trace4cats-http4s-common",
    libraryDependencies ++= Seq(Dependencies.trace4catsKernel, Dependencies.http4sCore, Dependencies.http4sDsl),
    libraryDependencies ++= Seq(
      Dependencies.trace4catsContextUtils,
      Dependencies.trace4catsCore,
      Dependencies.trace4catsTestkit
    ).map(_ % Test)
  )

lazy val `http4s-server` = (project in file("modules/http4s-server"))
  .settings(publishSettings)
  .settings(
    name := "trace4cats-http4s-server",
    libraryDependencies ++= Seq(Dependencies.trace4catsCore),
    libraryDependencies ++= Seq(Dependencies.http4sClient, Dependencies.trace4catsTestkit).map(_ % Test)
  )
  .dependsOn(`http4s-common` % "compile->compile;test->test")
