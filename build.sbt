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
  .settings(
    name := "app",
    idePackagePrefix := Some("org.aulune.authentigo"),
    assembly / mainClass := Some("org.aulune.authentigo.App"),
  )
