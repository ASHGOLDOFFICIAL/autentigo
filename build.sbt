import sbtassembly.MergeStrategy

excludeLintKeys in Global ++= Set(idePackagePrefix)


inThisBuild {
  List(
    organization := "org.aulune",
    scalaVersion := "3.3.6",
    semanticdbEnabled := true,
    version := "0.1.0-SNAPSHOT",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Wnonunit-statement",
      "-Werror",
      "-Xmax-inlines:64",
    ),
    assembly / assemblyMergeStrategy := mergeStrategy,
  )
}


def mergeStrategy: String => MergeStrategy = {
  case PathList("META-INF", "services", _*) => MergeStrategy.concat
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "module-info.class"      => MergeStrategy.discard
  case x                        => MergeStrategy.defaultMergeStrategy(x)
}


lazy val app = (project in file("."))
  .aggregate(domain, errors, application, api)
  .settings(
    name := "app",
    idePackagePrefix := Some("org.aulune.authentigo"),
    assembly / mainClass := Some("org.aulune.authentigo.App"),
  )


lazy val domain = (project in file("domain"))
  .settings(
    name := "domain",
    idePackagePrefix := Some("org.aulune.authentigo.domain"),
    libraryDependencies ++= Seq(
      "com.sanctionco.jmail" % "jmail"     % jmailVersion,
      "org.typelevel"       %% "cats-core" % catsVersion withSources () withJavadoc (),
    ),
  )


lazy val errors = (project in file("commons/errors"))
  .settings(
    name := "errors",
    idePackagePrefix := Some("org.aulune.commons.errors"),
    libraryDependencies ++= circeDeps ++ tapirCoreDeps ++ Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion,
      "org.typelevel" %% "cats-core" % catsVersion withSources () withJavadoc (),
    ),
  )


lazy val application = (project in file("application"))
  .dependsOn(errors)
  .settings(
    name := "application",
    idePackagePrefix := Some("org.aulune.authentigo.application"),
  )


lazy val api = (project in file("api"))
  .dependsOn(application)
  .settings(
    name := "api",
    idePackagePrefix := Some("org.aulune.authentigo.api"),
    libraryDependencies ++= circeDeps ++ tapirCoreDeps ++ Seq(
      "org.typelevel" %% "cats-core" % catsVersion withSources () withJavadoc (),
    ),
  )


val catsVersion = "2.13.0"
val circeGenericExtras = "0.14.5-RC1"
val circeVersion = "0.14.14"
val jmailVersion = "2.2.2"
val scalatestVersion = "3.2.19"
val tapirVersion = "1.11.40"

resolvers += Resolver.sonatypeCentralSnapshots

val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
).map(_ % circeVersion) ++ Seq(
  "io.circe" %% "circe-generic-extras" % circeGenericExtras,
)

val tapirCoreDeps = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-core",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
).map(_ % tapirVersion)
