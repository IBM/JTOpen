# JTOpen README

## IBM Toolbox for Java - JTOpen Version

Copyright (C) 1997-2023 International Business Machines Corporation and others. All rights reserved.

File: README.md
Last updated: 2023-03-05

The term "JTOpen" refers to the open source software product "IBM Toolbox for Java" plus any additional Enhancements provided by the open source community.

JTOpen, which is governed by the [IBM Public License](https://www.opensource.org/licenses/ibmpl.php), as well as its Java source code,
is contained in the GitHub open source repository [IBM/JTOpen](https://github.com/IBM/JTOpen).

The terms "Toolbox" and "Toolbox LPP" refer to the IBM Licensed Program Product "IBM Toolbox for Java", which is supported by IBM and can be obtained from the
[IBM Toolbox for Java web site](https://www.ibm.com/support/pages/node/1118781).

---

### Contents

* [What is the Toolbox?](#what-is-the-toolbox)
* [Why Open Source?](#why-open-source)
* [Differences between JTOpen and the Toolbox LPP](#differences-between-jtopen-and-the-toolbox-lpp)
* [Download and installation of JTOpen](#download-and-installation-of-jtopen)
  * [Requirements](#requirements)
  * [Files](#files)
  * [Building and Using JTOpen](#building-and-using-jtopen)  <!-- * [Using the Toolbox LPP](#using-the-toolbox-lpp) -->
* [Support information](#support-information)

### What is the Toolbox?

The IBM Toolbox for Java is a set of Java classes that allow you to
access IBM i, i5/OS, or OS/400 data through a Java program. With these classes,
you can write client/server applications, applets, and servlets that work
with data on your IBM i, i5/OS, or OS/400 system. You can also run Java
applications that use the IBM Toolbox for Java on the IBM i, i5/OS, or
OS/400 Java Virtual Machine.

The Toolbox is available as an installable licensed program for IBM i as of OS/400 V5R1. Here is a breakdown of the supported Toolbox releases versus operating system versions:

| Toolbox release | LPP release | Installs on OS/400 version | Connects to  OS/400 version |
| -------  |  -------------- | -------------- |  -------------- |
| JTOpen 6.1+ | 5761JC1 V6R1M0 | V5R3 and up | V5R3 and up
| JTOpen 7.0+ | 5770SS1 V7R1M0 | V5R4 and up | V5R4 and up
| JTOpen 8.0+ | 5770SS1 V7R2M0 | V6R1 and up | V6R1 and up
| JTOpen 9.0+ | 5770SS1 V7R3M0 | V7R1 and up | V7R1 and up
| JTOpen 10.0+ | 5770SS1 V7R4M0 | V7R2 and up | V7R2 and up
| JTOpen 11.0+ | 5770SS1 V7R5M0 | V7R3 and up | V7R3 and up

_Note: As of IBM i 7.1 (V7R1), the "JC1" product is no longer shipped. The Toolbox LPP is available under product 5770SS1 Option 3._

Newer versions of the Toolbox are backwards-compatible with earlier
versions. Upgrading to a newer version is usually recommended, with the one
main exception being that the Toolbox is only supported for connection
to servers running an in-service release of i5/OS or OS/400.

### Why Open Source?

We have chosen to make the Toolbox code open source for the following reasons:

1. To obtain new functions and features from the Toolbox user community.

2. To respond with customer and business partners requirements as rapidly
   as possible.

3. To improve the ability for our customers to build and debug their own
   applications when using the Toolbox functions.

4. To continue the drive to keep the IBM i platform a leader with Java technology
   for application development.

### Differences between JTOpen and the Toolbox LPP

1. The initial release of the source code for JTOpen used the V4R5
   Toolbox codebase. That is, the Java code in the open source repository
   is the same code that was used to build the V4R5 Toolbox LPP. JTOpen 2.0x
   uses the V5R1 Toolbox as its codebase. The latest release of JTOpen
   (JTOpen 7.x) uses the V7R1 Toolbox as its codebase.

2. When bugs are reported, fixes will be committed to JTOpen as soon as
   they are realized. When applicable, these same fixes will be committed
   to the Toolbox LPP and made available in a future PTF or service pack.

3. Any new functionality that is committed to JTOpen will be subsequently
   added, when applicable, into the Toolbox LPP and made available in a
   future PTF or service pack.

   Note: All changes to JTOpen are made under the control and at the
   discretion of the JTOpen Core Team. All changes to the Toolbox LPP
   are made under the control and at the discretion of IBM.

4. In general, the Toolbox LPP is supported directly by IBM. JTOpen
   is supported by the JTOpen user community, in which IBM participates
   and contributes, specifically through developer email, the JTOpen
   mailing list, and the[JTOpen Web forum](https://www.ibm.com/mysupport/s/forumsquestion?language=en_US&id=0D50z000060GKjGCAW)

5. Pursuant to the [IBM Public License](http://www.opensource.org/licenses/ibmpl.php), programmers are free to alter the
   JTOpen source code and to distribute it with their own applications.

### Download and installation of JTOpen

#### Requirements

Briefly, the requirements for using the JTOpen code are as follows:

1. A Java Virtual Machine (JVM) with a 1.4.x or higher JDK/JRE. The JTOpen
   team highly recommends moving to the latest supported release of the JDK/JRE, as
   maintaining backwards-compatible code with older releases becomes
   increasingly difficult to support over time.

2. Almost all JTOpen functions require a TCP/IP connection to an IBM i server.

   a. The server must be IBM i, i5/OS, or OS/400 V4R3 or higher.

   b. The server must have the Option 12 Host Servers installed and
      running. JTOpen uses the host servers as TCP/IP endpoints to
      communicate with the server from a client.

   c. If the Java application using JTOpen is being run directly on a server
      running IBM i, i5/OS, or OS/400, then certain JTOpen functions might use
      native API and/or local socket calls to improve performance.
      This will only occur under certain conditions, and only if the file
      jt400Native.jar is included in the application's CLASSPATH.

3. For GUI programming, either Java 2 or Swing 1.1 or higher is required.

#### Files

JTOpen is comprised of the following files:

|Jar file | Contents |
| ------- | -------- |
| jtopen_x_x_source.zip | This is a zip file of all the source files in the repository. It is not a Java jar file. |
| jtopen_x_x_javadoc.zip | This is a zip file of the javadoc (in HTML format) for the JTOpen source files. It is not a Java jar file. |
| jt400.jar\(\*\) | This is the main JTOpen jar file. It contains almost all open source code (except for the few Toolbox classes that could  not be open-sourced), including the utilities package and the JDBC driver (JDBC 3.0).|
| jt400android.jar\(\*\) | This is the main JTOpen jar file, compiled for use on android.  Some Java features that are not supported on android are disabled. |
| java6/jt400.jar\(\*\) | This is the main JTOpen jar file compiled for Java 6. A Java 6 JVM is required to use this class. |
| java8/jt400.jar\(\*\) | This is the main JTOpen jar file compiled for Java 8. A Java 8 JVM is required to use this class. |
| java9/jt400.jar\(\*\) | This is the main JTOpen jar file compiled for Java 9.  A Java 9 JVM is required to use this class. |
| jtopenlite.jar\(\*\) | This contains the JTOpenLite classes, optimized for small devices. |
| jt400Micro.jar\(\*\) | This contains the deprecated ToolboxME (Micro Edition) classes for use on a handheld device. This will be removed in a future JTOpen release. |
| jt400Native.jar |  Previously known as jt400Access.zip, this jar does not contain the vaccess package. It does contain the native optimization classes necessary for running performance-enhanced applications on IBM i, i5/OS, or OS/400 JVM.  This is shipped via PTF on the IBM i. |
| jt400Proxy.jar\(\*\) | This jar contains just the classes needed to run a client application using the Toolbox proxy server. It is especially useful in environments where a smaller jar is needed. |
| jt400Servlet.jar\(\*\) | This jar contains the html and servlet packages. |
| jui400.jar\(\*\) | Contains the PDML runtime packages as provided by the Graphical Toolbox. |
| uitools.jar\(\*\) | Contains the GUI Builder packages as provided by the Graphical Toolbox. |
| util400.jar\(\*\) | Contains various utilities. |
| composer.jar\(\*\) | Contains XSL stylesheet composer classes. |
| reportwriter.jar\(\*\) | Contains XSL report processor classes. |
| outputwriters.jar\(\*\) | Contains PDF, PCL, and font classes. |
| tes.jar\(\*\) | Contains the IBM i Graphical Debugger. |

\(\*\) Files contained in file jtopen_x_x.zip.

There are additional jar files shipped in the download for JTOpen off of the Toolbox
downloads page. Those jar files do not contain JTOpen code. They are
provided as a convenience to the developer and to mirror the objects that ship
with the Toolbox LPP.

<!--
As of JTOpen 3.3, MD5 checksums are provided to verify download integrity.
-->

#### Building and Using JTOpen

Instructions for using JTOpen:

1. Download the JTOpen jt400.jar.
2. Add it to your CLASSPATH.
3. Run your application.

Instructions for building all or part of the JTOpen source code:
__Note: Complete build instructions are specified in the commentary in file /build/build.xml__

1. Download the JTOpen jt400.jar.
2. Download any source files you want to change. The src.zip file includes
   all of the source in the repository for convenience.
3. Set up your CLASSPATH to include everything in the correct order:
   a. Your source files should be in your CLASSPATH first. Note that you
      cannot point to src.zip - you must unzip it, since it contains only
      source files.
   b. Next, add jt400.jar.
   c. Your CLASSPATH should look something like this:
      CLASSPATH=C:\jt400\MyApp;C:\jt400\jt400.jar;...
4. Compile your source.
5. Run your application.

### Support information

Documentation for JTOpen can be found on the [Toolbox web site](https://www.ibm.com/support/pages/node/1118781).

The [JTOpen Web forum](https://www.ibm.com/mysupport/s/forumsquestion?language=en_US&id=0D50z000060GKjGCAW) is available in the community IBM Toolbox for Java and JTOpen.

Subscribe/unsubscribe or view information about the [JTOpen mailing list](https://lists.sourceforge.net/lists/listinfo/jt400-news).
Please do not send messages to the JTOpen mailing list, as they will be bounced.
Instead, post your comments and questions to the [JTOpen Web forum](https://www.ibm.com/mysupport/s/forumsquestion?language=en_US&id=0D50z000060GKjGCAW) .

For bug reports, please use the [JTOpen bug database](https://sourceforge.net/tracker/?group_id=128806&atid=712772);
Other enhancements or fixes can be sent to[rchjt400@us.ibm.com](mailto:rchjt400@us.ibm.com) (monitored weekly),
or posted to the [JTOpen Web forum](https://www.ibm.com/mysupport/s/forumsquestion?language=en_US&id=0D50z000060GKjGCAW) (monitored daily).

<!--
The preferred channel for questions and comments is the Web forum.
The following individuals are the JTOpen Core Team members:

  Name                 E-mail                       Company
  -------------------  ---------------------------  ----------------------
  Steve Johnson-Evers  severs@everbrite.com         <A HREF="http://www.everbrite.com">Everbrite, Inc.</A>
  John Eberhard        jeber@us.ibm.com             <A HREF="http://www.ibm.com">IBM Corporation</A>
  Wang Hui Qin         wanghuiq@cn.ibm.com          <A HREF="http://www.ibm.com">IBM Corporation</A>
  Joe Pluta            joepluta@plutabrothers.com   <A HREF="http://www.plutabrothers.com">Pluta Brothers Design</A>
-->
