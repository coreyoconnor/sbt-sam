import sbt.Keys._
import sbt.ScriptedPlugin.autoImport.{scriptedBufferLog, scriptedLaunchOpts}
import sbt._
import LibraryDependencies._
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoOption, BuildInfoPlugin}

object SbtAwsPluginSettings extends AutoPlugin {
  override def trigger = noTrigger
  override def requires = plugins.JvmPlugin && ScriptedPlugin && BuildInfoPlugin

  override def projectSettings = Seq(
    sbtPlugin := true,
    libraryDependencies += libPlayJson,
    libraryDependencies += libAwsJavaSdk,
    libraryDependencies += libScalazScalaTest % Test,
    libraryDependencies += libScalaTest % Test,

    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
  ) ++
    buildInfoSettings

  lazy val buildInfoSettings = Seq(
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.github.dnvriend.sbt.aws",
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime
  )
}
