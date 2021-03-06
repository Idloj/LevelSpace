enablePlugins(org.nlogo.build.NetLogoExtension)

netLogoExtName := "ls"

netLogoClassManager := "org.nlogo.ls.LevelSpace"

scalaVersion := "2.11.7"

netLogoTarget := NetLogoExtension.directoryTarget(baseDirectory.value)

netLogoZipSources := false

scalaSource in Compile := baseDirectory.value / "src" / "main"

scalaSource in Test := baseDirectory.value / "src" / "test"

javaSource in Compile := baseDirectory.value / "src" / "main"

javaSource in Test := baseDirectory.value / "src" / "test"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "us-ascii")

val netLogoJarURL =
  Option(System.getProperty("netlogo.jar.url")).getOrElse("https://s3.amazonaws.com/ccl-artifacts/NetLogo-hexy-fd7cd755.jar")

val netLogoJarsOrDependencies = {
  import java.io.File
  import java.net.URI
  val urlSegments = netLogoJarURL.split("/")
  val lastSegment = urlSegments.last.replaceFirst("NetLogo", "NetLogo-tests")
  val testsUrl = (urlSegments.dropRight(1) :+ lastSegment).mkString("/")
  if (netLogoJarURL.startsWith("file:"))
    Seq(unmanagedJars in Compile ++= Seq(
      new File(new URI(netLogoJarURL)), new File(new URI(testsUrl))))
  else
    Seq(libraryDependencies ++= Seq(
      "org.nlogo" % "NetLogo" % "6.0-PREVIEW-12-15" from netLogoJarURL,
      "org.nlogo" % "NetLogo-tests" % "6.0-PREVIEW-12-15" % "test" from testsUrl))
}

netLogoJarsOrDependencies

libraryDependencies ++= Seq(
  "org.parboiled" %% "parboiled-scala" % "1.1.7",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.picocontainer" % "picocontainer" % "2.13.6" % "test",
  "org.ow2.asm" % "asm-all" % "5.0.3" % "test",
  "com.google.guava"  % "guava"         % "18.0",
  "com.google.code.findbugs" % "jsr305" % "3.0.0"
)

val moveToLsDir = taskKey[Unit]("add all resources to LS directory")

val lsDirectory = settingKey[File]("directory that extension is moved to for testing")

lsDirectory := baseDirectory.value / "extensions" / "ls"

moveToLsDir := {
  (packageBin in Compile).value
  val testTarget = NetLogoExtension.directoryTarget(lsDirectory.value)
  testTarget.create(NetLogoExtension.netLogoPackagedFiles.value)
  val testResources = (baseDirectory.value / "test" ***).filter(_.isFile)
  for (file <- testResources.get)
    IO.copyFile(file, lsDirectory.value / "test" / IO.relativize(baseDirectory.value / "test", file).get)
}

test in Test := {
  IO.createDirectory(lsDirectory.value)
  moveToLsDir.value
  (test in Test).value
  IO.delete(lsDirectory.value)
}

netLogoVersion := "6.0.0-M7"
