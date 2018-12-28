lazy val `izanami-clients` = (project in file("."))
  .aggregate(jvm, `scala-cats`)
  .enablePlugins(NoPublish)
  .disablePlugins(BintrayPlugin)

lazy val jvm = project

lazy val `scala-cats` = project
