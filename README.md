# JTOpen - The IBM Toolbox for Java

---

### Contents

* [What is the Toolbox?](#what-is-the-toolbox)
* [Changes in Version 20 and Newer](#changes-in-version-20-and-newer)
* [Differences between JTOpen and the Toolbox LPP](#differences-between-jtopen-and-the-toolbox-lpp)
* [Download and installation of JTOpen](#download-and-installation-of-jtopen)
  * [Requirements](#requirements)
  * [Files](#files)
  * [Building and Using JTOpen](#building-and-using-jtopen)  <!-- * [Using the Toolbox LPP](#using-the-toolbox-lpp) -->
* [Migration to GitHub](#migration-to-github)
* [Support information](#support-information)

### What is the Toolbox?


JTOpen is the open source software product known as the "IBM Toolbox for Java." It is also commonly
referred to "jt400" or simply "the toolbox." 
In short, this package provides a set of Java classes that enable applications to integrate with IBM i

JTOpen is governed by the [IBM Public License](https://www.opensource.org/licenses/ibmpl.php). 

JTOpen is the open source counterpart to a version of the
IBM Toolbox for Java that is delivered as part of the 5770-SS1 Licensed Program Product (LPP). The
LPP version is supported by IBM and can be obtained from the
[IBM Toolbox for Java web site](https://www.ibm.com/support/pages/node/1118781).
The Toolbox is available as an installable licensed program for IBM i. Here is a breakdown of the supported Toolbox releases versus operating system versions:

| JTOpen release  | LPP release     | Installs on IBM i version | Connects to IBM i version |
| -------         |  -------------- | --------------            |  -------------- |
| JTOpen 10.0+    | 5770SS1 V7R4M0  | 7.2 or later              | 7.2 or later
| JTOpen 11.0+    | 5770SS1 V7R5M0  | 7.3 or later              | 7.3 or later
| JTOpen 20.0.0+  | 5770SS1 V7R5M0  | 7.3 or later              | 7.3 or later

## Changes in Version 20 and Newer

There are several key changes introduced into JTOpen starting with version 20.x.
These differences warranted a significant version jump to differentiate from 11.x and
earlier code streams. Key differences include: 

1. Adoption of semantic versioning, based on the guidelines published at [semver.org](http://semver.org).
   In summary, JTOpen versions now consist of three digits, `x.y.z`.
   This provides differentiation between bug fixes, new features, and breaking changes. 

2. Java 7 or later is required (**breaking change**). 

3. Breaking changes may be introduced on major version upgrades. Some examples of breaking changes
   include:
   - Newer minimum Java version requirements
   - Changes in Java classes that require source modification or recompilation
   - Dropped support for older releases of IBM i
   Note that Version 20 contains some breaking changes as documented here.

4. Changes to code hosting location and support processes (see [Migration to GitHub](#migration-to-github)
   and [Support information](#support-information))

5. **Immediate removal** of several antiquated components of JTOpen, including
   - JTOpenLite
   - jt400Android
   - jt400Micro
   - jt400Proxy
   If you need these packages, please acquire older versions from the [archive site on sourceforge](http://jt400.sourceforge.net)
   (**breaking change**)

6. Publication of "native" form to Maven Central (see [File Information](#file-information)). This allows Maven-based
   applications running on IBM i to take advantage of extra optimizations present in the operating system

### Download and installation of JTOpen

#### Maven

The recommended way to build Java applications is to use Maven, Gradle, or some other system to manage
dependencies. Manually maintaining a Java classpath is not desired. 
JTOpen is published to Maven Central as artifact ID `jt400` in group `net.sf.jt400`.
Visit specific versions on [the jtopen page on Maven Central](https://mvnrepository.com/artifact/net.sf.jt400/jt400)
for example build declarations for Maven's `pom.xml` manifest file. Configuration
text is also available for other build systems, including Gradle, SBT, Ivy, Grape, Leiningen, and Buildr.

Several coordinates are published to Maven. See [File Information](#file-information) for information about Maven coordinates

#### File Information

JTOpen [releases](https://github.com/IBM/JTOpen/releases) for versions 20 and newer include
the following files:

|Jar file | Maven Coordinate (v20)  | Contents |
| ------- | -------- | -------- |
| jtopen-x.y.z.jar         | <default>  | This is the main JTOpen jar file. It contains almost all open source code (except for the few Toolbox classes that could  not be open-sourced), including the utilities package and the JDBC driver (JDBC 3.0). This is analagous to the jar file classically named `jt400.jar`.|
| jtopen-x.y.z-native.jar  | `native`   | This is the main JTOpen jar file with support for "Native Optimizations" when running on IBM i. This is analagous to the file classically named `jt400Native.jar`.|
| jtopen-x.y.z-java8.jar   | `java8`    | This is the main JTOpen jar file but built for Java 8 (or newer). Some components (most notably the JDBC driver) may have extra capabilities available in Java 8. |
| jtopen-x.y.z-java11.jar  | `java11`   | This is the main JTOpen jar file but built for Java 8 (or newer). Some components (most notably the JDBC driver) may have extra capabilities available in Java 11. |
| jtopen-x.y.z-sources.zip | N/A        | This is a zip file of all the source files in the repository. It is not a Java jar file. |
| jtopen-x.y.z-javadoc.zip | N/A        | This is a zip file of the javadoc (in HTML format) for the JTOpen source files. It is not a Java jar file. |

### Migration to GitHub

As of April 2023, the [JTOpen site on sourceforge.net](http://jt400.sourceforge.net) is considered a historical
archive for older versions. GitHub is now used for issue tracking, release management, etc. 

### Support information

Please note the following regarding support/collaboration options:
- Documentation for JTOpen can be found on the [Toolbox web site](https://www.ibm.com/support/pages/node/1118781).
- See the "Community" section of the [IBM i open source resources page](http://ibm.biz/ibmioss) for community forums/chat/etc.
  In particular, the Ryver forums have a topic dedicated to Java development and is a good place to discuss JTOpen. 
- The JTOpen mailing list has been discontinued
- You may [submit a GitHub issue](https://github.com/IBM/JTOpen/issues/new/choose) for bug reports, enhancement requests,
  or questions. 
- In general, the version of toolbox shipped with the IBM i operating system is supported by IBM as part of 
  a software maintenance agreement ("SWMA")