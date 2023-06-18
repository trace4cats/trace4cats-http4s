import sbt._

object Dependencies {
  object Versions {
    val scala212 = "2.12.18"
    val scala213 = "2.13.11"
    val scala3 = "3.3.0"

    val trace4cats = "0.14.2"

    val http4s = "0.23.21"

    val kindProjector = "0.13.2"
    val betterMonadicFor = "0.3.1"
  }

  lazy val trace4catsKernel = "io.janstenpickle"       %% "trace4cats-kernel"        % Versions.trace4cats
  lazy val trace4catsCore = "io.janstenpickle"         %% "trace4cats-core"          % Versions.trace4cats
  lazy val trace4catsContextUtils = "io.janstenpickle" %% "trace4cats-context-utils" % Versions.trace4cats
  lazy val trace4catsTestkit = "io.janstenpickle"      %% "trace4cats-testkit"       % Versions.trace4cats

  lazy val http4sClient = "org.http4s" %% "http4s-client" % Versions.http4s
  lazy val http4sCore = "org.http4s"   %% "http4s-core"   % Versions.http4s
  lazy val http4sDsl = "org.http4s"    %% "http4s-dsl"    % Versions.http4s

  lazy val kindProjector = ("org.typelevel" % "kind-projector"     % Versions.kindProjector).cross(CrossVersion.full)
  lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
}
