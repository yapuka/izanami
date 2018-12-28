import sbt.Keys.{organization, scalacOptions}
import sbtrelease.ReleaseStateTransformations._

val disabledPlugins = if (sys.env.get("TRAVIS_TAG").filterNot(_.isEmpty).isDefined) {
  Seq(RevolverPlugin)
} else {
  Seq(RevolverPlugin, BintrayPlugin)
}
val Http4sVersion = "0.20.0-M4"

lazy val `scala-cats` = (project in file("."))
  .disablePlugins(disabledPlugins: _*)
  .settings(
    organization := "fr.maif",
    name := "izanami-scala-client",
    crossScalaVersions := Seq(scalaVersion.value, "2.11.11"),
    libraryDependencies ++= Seq(
      "org.typelevel"          %% "cats-core"           % "1.5.0",
      "org.typelevel"          %% "cats-effect"         % "1.1.0",
      "org.http4s"             %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"             %% "http4s-play-json"    % Http4sVersion,
      "org.http4s"             %% "http4s-dsl"          % Http4sVersion,
      "com.typesafe.play"      %% "play-json"           % "2.6.9",
      "junit"                  % "junit"                % "4.12" % Test,
      "com.novocode"           % "junit-interface"      % "0.11" % Test,
      "org.scalatest"          %% "scalatest"           % "3.0.1" % Test,
      "org.mockito"            % "mockito-core"         % "2.12.0" % Test,
      "com.github.tomakehurst" % "wiremock"             % "2.12.0" % Test,
      "org.assertj"            % "assertj-core"         % "3.8.0" % Test
    ),
    scalacOptions ++= Seq(
      "-Ypartial-unification",
      "-Xfatal-warnings",
      "-feature",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:existentials",
      "-Xfatal-warnings"
    ),
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      Resolver.bintrayRepo("larousso", "maven")
    ),
    scalafmtOnCompile in ThisBuild := true,
    scalafmtTestOnCompile in ThisBuild := true,
    scalafmtVersion in ThisBuild := "1.2.0"
  )
  .settings(publishSettings: _*)

lazy val githubRepo = "maif/izanami"

lazy val publishCommonsSettings = Seq(
  homepage := Some(url(s"https://github.com/$githubRepo")),
  startYear := Some(2017),
  licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
  scmInfo := Some(
    ScmInfo(
      url(s"https://github.com/$githubRepo"),
      s"scm:git:https://github.com/$githubRepo.git",
      Some(s"scm:git:git@github.com:$githubRepo.git")
    )
  ),
  developers := List(
    Developer("alexandre.delegue", "Alexandre Delègue", "", url(s"https://github.com/larousso"))
  ),
  releaseCrossBuild := true,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  bintrayVcsUrl := Some(s"scm:git:git@github.com:$githubRepo.git")
)

lazy val publishSettings =
  if (sys.env.get("TRAVIS_TAG").filterNot(_.isEmpty).isDefined) {
    publishCommonsSettings ++ Seq(
      bintrayOrganization := Some("maif"),
      pomIncludeRepository := { _ =>
        false
      }
    )
  } else {
    publishCommonsSettings ++ Seq(
      publishTo := Some(
        "Artifactory Realm" at "http://oss.jfrog.org/artifactory/oss-snapshot-local"
      ),
      bintrayReleaseOnPublish := false,
      credentials := List(
        Credentials("Artifactory Realm",
                    "oss.jfrog.org",
                    sys.env.getOrElse("BINTRAY_USER", ""),
                    sys.env.getOrElse("BINTRAY_PASS", ""))
      )
    )
  }
