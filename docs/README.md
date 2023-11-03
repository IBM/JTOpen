
# JTOpen, the Java library for IBM i

JTOpen is the open source software product known as the "IBM Toolbox for Java." It is also commonly
referred to "jt400" or simply "the toolbox." 
In short, this package provides a set of Java classes that enable applications to integrate with IBM i.
This includes, but is not limited to:
- Accessing the database using JDBC
- Accessing the database using record level access
- Calling CL commands
- Calling programs or service programs
- Accessing filesystem objects
- Managing object attributes and authorities

JTOpen is the open source counterpart to a version of the IBM Toolbox for Java that is delivered as part of the 5770-SS1 Licensed Program Product (LPP).

Please consult [the "major changes" documentation](MAJOR_CHANGES.md) to see if any major changes impact you!

## How to provide feedback or contribute

Feedback and contributions are in line with standard GitHub practices. Feedback can be submitted by opening an
[issue](https://github.com/IBM/JTOpen/issues/new), and contributions are accepted through standard GitHub pull
requests.

Acceptable contributions must meet the following criteria:
- The contributions must pass any Continuous Integration (BI) builds
- The contributions must include relevant in-code comments when new function is being added
- The contributions must not cause incompatibilities with Java versions still required to be supported by the library
- Coding conventions should match those of the surrounding code. 

## Migration to GitHub

As of April 2023, the [JTOpen site on sourceforge.net](http://jt400.sourceforge.net) is considered a historical
archive for older versions. GitHub is now used for issue tracking, release management, etc. 
