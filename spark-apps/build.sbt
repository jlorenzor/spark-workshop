ThisBuild / scalaVersion := "2.12.10"
ThisBuild / version := "1.0.0"
ThisBuild / organization := "edu.uni.macrodatos"

val sparkVersion = "3.0.1"
val postgresqlVersion = "42.7.4"

lazy val root = (project in file("."))
  .settings(
    name := "jesus-maria-workshop-analysis",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.postgresql" % "postgresql" % postgresqlVersion,
      "org.scalatest" %% "scalatest" % "3.2.2" % Test
    ),
    // Configuración para assembly
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )