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
  .aggregate(domain, errors, testing, migrations, application, adapters, api)
  .dependsOn(adapters, api, migrations)
  .settings(
    name := "app",
    idePackagePrefix := Some("org.aulune.authentigo"),
    assembly / mainClass := Some("org.aulune.authentigo.App"),
    libraryDependencies ++= http4sDeps ++ tapirDeps ++ Seq(
      "ch.qos.logback"         % "logback-classic" % logbackVersion,
      "com.github.pureconfig" %% "pureconfig-core" % pureconfigVersion,
      "org.typelevel" %% "cats-core" % catsVersion withSources () withJavadoc (),
      "org.typelevel" %% "cats-effect" % catsEffectVersion withSources () withJavadoc (),
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    ),
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


lazy val migrations = (project in file("migrations")).settings(
  name := "migrations",
  idePackagePrefix := Some("org.aulune.authentigo.migrations"),
  libraryDependencies ++= Seq(
    "org.liquibase" % "liquibase-core" % liquibaseVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion withSources () withJavadoc (),
  ),
)


lazy val testing = (project in file("commons/testing"))
  .dependsOn(migrations)
  .settings(
    name := "testing",
    idePackagePrefix := Some("org.aulune.commons.testing"),
    libraryDependencies ++= Seq(
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersVersion,
      "com.dimafeng" %% "testcontainers-scala-scalatest"  % testcontainersVersion,
      "org.postgresql" % "postgresql" % postgresqlVersion,
      "org.scalatest" %% "scalatest"  % scalatestVersion,
      "org.tpolecat" %% "doobie-core"   % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion withSources () withJavadoc (),
      "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingVersion,
    ),
  )


lazy val adapters = (project in file("adapters"))
  .dependsOn(domain, application)
  .dependsOn(testing % Test)
  .settings(
    name := "adapters",
    idePackagePrefix := Some("org.aulune.authentigo.adapters"),
    libraryDependencies ++= circeDeps ++ Seq(
      "ch.qos.logback"        % "logback-classic" % logbackVersion % Test,
      "com.github.jwt-scala" %% "jwt-circe"        % jwtVersion,
      "de.mkammerer"          % "argon2-jvm-nolibs" % argon2Version,
      "org.postgresql"        % "postgresql"       % postgresqlVersion,
      "org.scalamock" %% "scalamock" % scalamockVersion % Test,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.typelevel" %% "cats-core" % catsVersion withSources () withJavadoc (),
      "org.typelevel" %% "cats-effect" % catsEffectVersion withSources () withJavadoc (),
      "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingVersion % Test,
      "org.typelevel" %% "log4cats-core"   % log4catsVersion,
      "org.typelevel" %% "log4cats-slf4j"  % log4catsVersion % Test,
    ),
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


val argon2Version = "2.12"
val catsEffectTestingVersion = "1.6.0"
val catsEffectVersion = "3.6.3"
val catsVersion = "2.13.0"
val circeGenericExtras = "0.14.5-RC1"
val circeVersion = "0.14.14"
val doobieVersion = "1.0.0-RC9"
val jmailVersion = "2.2.2"
val jwtVersion = "11.0.2"
val liquibaseVersion = "4.29.2"
val http4sVersion = "0.23.30"
val log4catsVersion = "2.7.1"
val logbackVersion = "1.5.18"
val postgresqlVersion = "42.7.7"
val pureconfigVersion = "0.17.9"
val scalamockVersion = "7.4.1"
val scalatestVersion = "3.2.19"
val tapirVersion = "1.11.40"
val testcontainersVersion = "0.44.1"

resolvers += Resolver.sonatypeCentralSnapshots

val http4sDeps = Seq(
  "org.http4s" %% "http4s-ember-server",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-circe",
).map(_ % http4sVersion)

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

val tapirDeps = tapirCoreDeps ++ Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle",
).map(_ % tapirVersion)
