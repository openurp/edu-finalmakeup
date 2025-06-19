import org.openurp.parent.Dependencies.*
import org.openurp.parent.Settings.*

ThisBuild / organization := "org.openurp.edu.finalmakeup"
ThisBuild / version := "0.0.28-SNAPSHOT"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/openurp/edu-finalmakeup"),
    "scm:git@github.com:openurp/edu-finalmakeup.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "OpenURP Starter"
ThisBuild / homepage := Some(url("http://openurp.github.io/edu-finalmakeup/index.html"))

val apiVer = "0.44.0"
val starterVer = "0.3.58"
val baseVer = "0.4.51"
val coreVer = "0.3.11"
val openurp_edu_api = "org.openurp.edu" % "openurp-edu-api" % apiVer
val openurp_std_api = "org.openurp.std" % "openurp-std-api" % apiVer
val openurp_stater_web = "org.openurp.starter" % "openurp-starter-web" % starterVer
val openurp_base_tag = "org.openurp.base" % "openurp-base-tag" % baseVer
val openurp_edu_core = "org.openurp.edu" % "openurp-edu-core" % coreVer

lazy val root = (project in file("."))
  .enablePlugins(WarPlugin,TomcatPlugin,UndertowPlugin)
  .settings(
    name := "openurp-edu-finalmakeup-webapp",
    common,
    libraryDependencies ++= Seq(openurp_stater_web, openurp_base_tag,openurp_edu_core)
  )

