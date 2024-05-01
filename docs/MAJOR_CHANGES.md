
# Major Changes

This page documents major changes, including possible breaking changes, that consumers of this library
should be aware of

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
   - jt400Servlet
   
   If you need these packages, please acquire older versions from the [archive site on sourceforge](http://jt400.sourceforge.net)
   (**breaking change**)

1. Publication of "native" form to Maven Central (see [Download Information](INSTALL.md#download-information)). This allows Maven-based
   applications running on IBM i to take advantage of extra optimizations present in the operating system
