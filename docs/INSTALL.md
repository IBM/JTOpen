# Download/Installation

The recommended way to build Java applications is to use Maven, Gradle, or some other system to manage
dependencies. Manually maintaining a Java classpath is not desired.

## Maven
 
JTOpen is published to Maven Central as artifact ID `jt400` in group `net.sf.jt400`.
Visit specific versions on [the jtopen page on Maven Central](https://mvnrepository.com/artifact/net.sf.jt400/jt400)
for example build declarations for Maven's `pom.xml` manifest file. Configuration
text is also available for other build systems, including Gradle, SBT, Ivy, Grape, Leiningen, and Buildr.

Several coordinates are published to Maven. See [Download Information](#download-information) for information about Maven coordinates

## Download Information

JTOpen [releases](https://github.com/IBM/JTOpen/releases) for versions 21 and newer include
the following files:

|Jar file                  | Maven Coordinate (v20)  | Contents  |
| -----------------------  | ----------------------  | --------  |
| jtopen-x.y.z.jar         | &lt;default&gt;  | This is the main JTOpen jar file, currently built using Java 8. It contains almost all open source code (except for the few Toolbox classes that could  not be open-sourced), including the utilities package and the JDBC driver (JDBC 4.2). This is analogous to the jar file classically named `jt400.jar`.|
| jtopen-x.y.z-native.jar  | `native`   | This is the main JTOpen jar file with support for "Native Optimizations" when running on IBM i. This is analogous to the file classically named `jt400Native.jar`.|
| jtopen-x.y.z-java11.jar  | `java11`   | This is the main JTOpen jar file but built for Java 11 (or newer). Some components (most notably the JDBC driver) may have extra capabilities available in Java 11. |
| jtopen-x.y.z-native-java11.jar  | `native-java11`   | This is the main JTOpen jar file with support for "Native Optimizations" when running on IBM i, but built for Java 11 (or newer). Some components (most notably the JDBC driver) may have extra capabilities available in Java 11. |
| jtopen-x.y.z-sources.zip | N/A        | This is a zip file of all the source files in the repository. It is not a Java jar file. |
| jtopen-x.y.z-javadoc.jar | N/A        | This is a zip file of the javadoc (in HTML format) for the JTOpen source files. It is not a Java jar file. |
