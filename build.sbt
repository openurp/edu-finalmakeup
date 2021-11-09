import org.openurp.parent.Settings._
import org.openurp.parent.Dependencies._
import org.beangle.tools.sbt.Sas

ThisBuild / organization := "org.openurp.edu.finalmakeup"
ThisBuild / version := "0.0.20"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/openurp/edu-finalmakeup"),
    "scm:git@github.com:openurp/edu-finalmakeup.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "chaostone",
    name  = "Tihua Duan",
    email = "duantihua@gmail.com",
    url   = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "OpenURP Starter"
ThisBuild / homepage := Some(url("http://openurp.github.io/edu-finalmakeup/index.html"))

val apiVer = "0.23.4"
val starterVer = "0.0.13"
val baseVer = "0.1.22"
val openurp_edu_api = "org.openurp.edu" % "openurp-edu-api" % apiVer
val openurp_std_api = "org.openurp.std" % "openurp-std-api" % apiVer
val openurp_stater_web = "org.openurp.starter" % "openurp-starter-web" % starterVer
val openurp_base_tag = "org.openurp.base" % "openurp-base-tag" % baseVer
val openurp_edu_grade_core = "org.openurp.edu.grade" % "openurp-edu-grade-core" % "0.0.15"

lazy val root = (project in file("."))
  .settings()
  .aggregate(core,web,adminapp)

lazy val core = (project in file("core"))
  .settings(
    name := "openurp-edu-finalmakeup-core",
    common,
    libraryDependencies ++= Seq(openurp_edu_api,openurp_std_api,beangle_ems_app,openurp_edu_grade_core)
  )

lazy val web = (project in file("web"))
  .settings(
    name := "openurp-edu-finalmakeup-web",
    common,
    libraryDependencies ++= Seq(openurp_stater_web,openurp_base_tag)
  ).dependsOn(core)

lazy val adminapp = (project in file("adminapp"))
  .enablePlugins(WarPlugin)
  .settings(
    name := "openurp-edu-finalmakeup-adminapp",
    common,
    libraryDependencies ++= Seq(Sas.Tomcat % "test")
  ).dependsOn(web)

publish / skip := true
