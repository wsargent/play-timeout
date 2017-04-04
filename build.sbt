name := """play-timeout"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.10.6"
libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.5.0"

