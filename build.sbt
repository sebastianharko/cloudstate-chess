name := "chess"

version := "0.1"

organization := "com.harko"

scalaVersion := "2.12.1"

mainClass in Compile := Some("com.harko.chess.Main")

libraryDependencies += "io.cloudstate" % "cloudstate-java-support" % "0.4.3" % "protobuf"
libraryDependencies += "io.cloudstate" % "cloudstate-java-support" % "0.4.3"

resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.bhlangonijr" % "chesslib" % "1.1.13"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"


libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)

PB.targets in Compile := Seq(
  PB.gens.java -> (sourceManaged in Compile).value,
  scalapb.gen(javaConversions = true) -> (sourceManaged in Compile).value
)

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

dockerBaseImage := "adoptopenjdk:11-jre-hotspot"