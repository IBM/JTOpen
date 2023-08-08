
# JTOpen, the Java library for IBM i

JTOpen is the open source software product known as the "IBM Toolbox for Java." It is also commonly
referred to "jt400" or simply "the toolbox." 
In short, this package provides a set of Java classes that enable applications to integrate with IBM i

JTOpen is governed by the [IBM Public License](LICENSE.md). 

JTOpen is the open source counterpart to a version of the
IBM Toolbox for Java that is delivered as part of the 5770-SS1 Licensed Program Product (LPP). The
LPP version is supported by IBM and can be obtained from the
[IBM Toolbox for Java web site](https://www.ibm.com/support/pages/node/1118781).
The Toolbox is available as an installable licensed program for IBM i. Here is a breakdown of the supported Toolbox releases versus operating system versions:

| JTOpen release  | Min. Java Version  | Installs on IBM i version | Connects to IBM i version |
| -------         | --------------     | --------------            |  -------------- |
| JTOpen 10.x     |   1.1              | 7.2 or later              | 7.2 or later
| JTOpen 11.x     |   1.1              | 7.3 or later              | 7.3 or later 
| JTOpen 20.x.y   |    7               | 7.3 or later              | 7.3 or later
| JTOpen 21.x.y   |    8               | 7.4 or later              | 7.3 or later


## Changes in Version 20 and Newer

There are several key changes introduced into JTOpen starting with version 20.x.
These differences warranted a significant version jump to differentiate from 11.x and
earlier code streams. Key differences include: 

1. Adoption of semantic versioning, based on the guidelines published at [semver.org](http://semver.org).
   In summary, JTOpen versions now consist of three digits, `x.y.z`.
   This provides differentiation between bug fixes, new features, and breaking changes. 

1. Java 7 or later is required (**breaking change**).

1. Function signatures may be changed from previous versions, in an effort to add typesafety. Version
   20 will remain source-compatible, but may have binary incompatibility (**breaking change**).

1. Breaking changes may be introduced on major version upgrades. Some examples of breaking changes
   include:
   - Newer minimum Java version requirements
   - Changes in Java classes that require source modification or recompilation
   - Dropped support for older releases of IBM i
   Note that Version 20 contains some breaking changes as documented here.

1. Changes to code hosting location and support processes (see [Migration to GitHub](#migration-to-github)
   and [Support information](#support-information))

1. **Immediate removal** of several antiquated components of JTOpen, including
   - JTOpenLite
   - jt400Android
   - jt400Micro
   - jt400Proxy
   If you need these packages, please acquire older versions from the [archive site on sourceforge](http://jt400.sourceforge.net)
   (**breaking change**)

1. Publication of "native" form to Maven Central (see [File Information](#file-information)). This allows Maven-based
   applications running on IBM i to take advantage of extra optimizations present in the operating system


## Migration to GitHub

As of April 2023, the [JTOpen site on sourceforge.net](http://jt400.sourceforge.net) is considered a historical
archive for older versions. GitHub is now used for issue tracking, release management, etc. 

## Support information

Please note the following regarding support/collaboration options:
- Documentation for JTOpen can be found on the [Toolbox web site](https://www.ibm.com/support/pages/node/1118781).
- See the "Community" section of the [IBM i open source resources page](http://ibm.biz/ibmioss) for community forums/chat/etc.
  In particular, the Ryver forums have a topic dedicated to Java development and is a good place to discuss JTOpen. 
- The JTOpen mailing list has been discontinued
- You may [submit a GitHub issue](https://github.com/IBM/JTOpen/issues/new/choose) for bug reports, enhancement requests,
  or questions. 
- In general, the version of toolbox shipped with the IBM i operating system is supported by IBM as part of 
  a software maintenance agreement ("SWMA")

## Javadoc

[![javadoc](https://javadoc.io/badge2/net.sf.jt400/jt400/javadoc.svg)](https://javadoc.io/doc/net.sf.jt400/jt400) 