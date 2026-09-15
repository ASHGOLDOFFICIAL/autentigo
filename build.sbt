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
  )
}


lazy val app = (project in file("."))
  .aggregate(domain)
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


val catsVersion = "2.13.0"
val jmailVersion = "2.2.2"
