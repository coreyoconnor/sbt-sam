package com.github.dnvriend.sbt.sam.task

import sbt._
import scalaz.Show

import scala.tools.nsc.classpath.{ FileUtils, PackageNameUtils }

object ProjectClass {
  implicit val show: Show[ProjectClass] = Show.shows(m => {
    s"""
      |=====================
      |ProjectClass: ${m.cl.getName}
      |=====================
      |Class: ${m.className.value}
      |Package: ${m.packageName.value}
      |simpleName: ${m.cl.getSimpleName}
      |interfaces: ${m.interfaces.map(_.getName)}
      |superclass: ${m.cl.getSuperclass.getName}
    """.stripMargin
  })
}

final case class ProjectClass(
    packageName: PackageName,
    className: ClassName,
    interfaces: List[Class[_]],
    cl: Class[_])
final case class PackageName(value: String)
final case class ClassName(value: String)

object DiscoverProjectClasses {
  def interfaces(cl: Class[_]): List[Class[_]] = {
    val classInterfaces: List[Class[_]] = cl.getInterfaces.toList
    classInterfaces.flatMap(_.getInterfaces.toList) ++ classInterfaces
  }
  def run(
    projectClassFiles: Set[File],
    baseDir: File,
    cl: ClassLoader): Set[ProjectClass] = {

    val relativizePath = createRelativizer(baseDir)
    val allClasses: Set[(String, String)] = projectClassFiles
      .flatMap(classFile => relativizePath(classFile).toSeq)
      .map(FileUtils.stripClassExtension)
      .map(fileSyntaxToPackageSyntax)
      .map(PackageNameUtils.separatePkgAndClassNames)

    allClasses.map {
      case (packageName, className) =>
        val loadedClass = cl.loadClass(s"$packageName.$className")
        ProjectClass(
          PackageName(packageName),
          ClassName(className),
          interfaces(loadedClass),
          loadedClass
        )
    }
  }

  private def createRelativizer(baseDir: File): sbt.File => Option[String] = {
    IO.relativize(baseDir, _: File)
  }

  private val fileSyntaxToPackageSyntax: String => String = (fileSyntax: String) => fileSyntax.replace("/", ".")
}
