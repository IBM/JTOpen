# JTOpen Tracing Guide

## Toolbox Trace Categories

The IBM toolbox for Java provides several categories of tracing that can be enabled. In nearly all cases, the value of "all" is
recommended. If the trace is growing too quickly when all categories are enabled, select specific categories. By default, the output
of toolbox tracing goes to standard out; however, there is an option to direct to a specific location. See the "Trace Output" section
for more details. The categories are listed in the following table:

**Table 1**
| Category        | Description   |
| --------------- | ------------- |
| conversion      | This category is used by toolbox classes to log character set conversions between Unicode and native code pages.
| datastream      | This category is used by toolbox classes to log data flow between the local host and the remote system.
| diagnostic      | This category is used to log object state information.
| error           | This category is used to log errors that cause an exception.
| information     | This category is used to track the flow of control through the code.
| jdbc            | This category is used to include JDBC data in the standard toolbox trace.
| pcml            | This category is used to determine how PCML interprets the data that is sent to and from the system.
| proxy           | This category is used by toolbox classes to log data flow between the client and the proxy server.
| thread          | This category adds thread information to trace points from other categories.
| warning         | This category is used to log errors that are recoverable.
| all             | This category enables all other categories.
| none            | This category disables all tracing.


## Server Trace Options


The toolbox JDBC driver provides a server trace property that allows the driver to initiate server-based traces at connection time.
If it is determined that server-side traces are required, the value of the property is determined by adding the numbers assigned to
each server trace that is wanted. The server traces available, and their assigned values, are listed in the following table:

**Table 2**
| Server Trace 	             | Option Value  | Description | 
| -------------------------- | ------------- | ------------ |
| Database Monitor           | 2             | Start the database monitor on the JDBC server job.
| Debug Job                  | 4             | Start debug on the JDBC server job.
| Save Joblog                | 8 	           | Save the job log when the JDBC server job ends.
| Job Trace                  | 16 	         | Start job trace on the JDBC server job.
| Save SQL                   | 32            | Print a spooled file containing SQL statements for the job when the JDBC server job ends.
| Database Host Server Trace | 64            | Start a trace for the database host server component.

When you use the server trace options, the user profile used to make the JDBC connection must have the proper authority to the commands
used or the trace cannot start. For example, to use the option to start a database monitor, the profile must have use authority to the
`STRDBMON` command. In addition, for options that produce output in the QUSRSYS library, the user profile must have write access to the
library.
For more information about database monitor impacts, see:
[Impact of collecting a DBMON trace - Start Database Monitor - STRDBMON](https://www.ibm.com/support/pages/node/882914)

The `STRTRC` and `STRDBG` commands require that the user profile has *SERVICE special authority. The debug job log and job trace options
require that the user has this special authority.
 

## Java Virtual Machine (JVM) Properties

When a JVM first starts, a set of system properties is loaded containing information about the version, platform, and other settings.
The IBM toolbox for Java defines JVM properties that it looks for in order to determine whether tracing is loaded. To enable tracing by
using a JVM property, it must be introduced into the JVM at start time. There are multiple ways to enable tracing by using a JVM property,
depending on the environment. In a WebSphere application, for example, there is a page in the admin console where JVM properties can be
added. In a stand-alone Java application, JVM properties might be passed as arguments to the "Java" command. The following table lists the
properties defined by toolbox, the purpose of each, and the allowed values.

**Table 3**
| Property                                | Description                                                    | Allowed Values |
| --------------------------------------- | -------------------------------------------------------------- | -------------- |
| `com.ibm.as400.access.Trace.category`   | Turns on tracing for specific toolbox categories. 	           | Comma-delimited list of [categories](#toolbox-trace-categories)
| `com.ibm.as400.access.Trace.file`       | Directs the output of the toolbox trace to the specified file. | Any path that is valid for the platform where the JVM is run. Directory must exist; however, the file does not need to exist.
| `com.ibm.as400.access.ServerTrace.JDBC` | Turns on the available server-side traces for the JDBC driver. | Any number produced by summing the values of the wanted options in Table 2.
 

## JDBC Data Source Properties

The toolbox data source implementation class provides properties that can be set to control toolbox tracing. These properties can be set
programmatically (uncommon) or by using the administration of an application server, such as WebSphere. Note, in the data source properties, 
there is no provision for directing the trace output. One of the other techniques must be used to direct tracing to a file.

**Table 4**
| Data Source Property                    | Description                                                          | Allowed Values |
| --------------------------------------- | -------------------------------------------------------------------- | -------------- |
| `toolboxTrace`                          | Turns on tracing for specific toolbox categories.                    | Comma-delimited list of the categories listed in Table 1.
| `trace`                                 | Turns on JDBC specific tracing. Same as the "jdbc" toolbox category. | True or false.
| `serverTraceCategories`                 | Turns on the available server-side traces for the JDBC driver.       | Any number produced by summing the values of the wanted options in Table 2.
 
## JDBC Connection String

The toolbox JDBC driver provides keywords that can be used in the connection string passed to the JDBC driver manager. These properties can
be used to enable toolbox tracing and the server-side traces. As with data source properties, there is not a keyword to direct output to a
specific file.

**Table 5**
| Connection Keyword      | Description                              | Allowed Values |
| ----------------------- | ---------------------------------------- | -------------- |
| `toolbox trace`         | Turns on tracing for specific toolbox categories. | Comma-delimited list of the categories listed in Table 1.
| `trace`                 | Turns on JDBC specific tracing. Same as the "jdbc" toolbox category. | True or false.
| `server trace`          | Turns on the available server-side traces for the JDBC driver.       | Any number produced by summing the values of the wanted options in Table 2.

The following is an example of a connection string that could be used to turn on toolbox tracing and server traces.
```pascal
"jdbc:as400://mysystem;toolbox trace=all;trace=true;server trace=126;"
```
 

## Toolbox Properties File


When the toolbox driver is first initialized, it searches for an optional properties file that might contain system properties that determine
what type of tracing is started. This file must have a specific name (jt400.properties) and must be in the appropriate subdirectory of the class
path (com/ibm/as400/access). For example, if the class path points to a directory named "/mydir", then create a file named
"/mydir/com/ibm/as400/access/jt400.properties".

The file is a text file that contains properties from Table 3 followed by an equals sign (=) and then the appropriate values. Comments in the
properties are preceded by a pound sign (`#`). If there are backslashes (`\`) in the file, they must be escaped with another backslash. Following
is an example of what the properties file might look like:

```properties
#=========================================================#
# IBM toolbox for Java                                    #
#---------------------------------------------------------#
# Sample properties file                                  #
#                                                         #
# Name this file jt400.properties and store it in a       #
# com/ibm/as400/access directory that is pointed to by    #
# the class path.                                          #
#=========================================================#
com.ibm.as400.access.Trace.category=all
com.ibm.as400.access.Trace.file=c:\\temp\\ToolboxTrace.txt
com.ibm.as400.access.ServerTrace.JDBC=62
```
 

## Java Methods To Enable Traces Programmatically


The IBM toolbox for Java provides public methods in the Trace class that can be used to turn toolbox tracing on or off from within a
Java program. The methods to change server trace settings (DBMON, debug, and so on) are not public, and must be enabled with one of
the previously listed methods. There are methods available to toggle the setting for each trace category (as found in Table 1), as
well as a method to toggle the trace output on and off. Note, turning on a category does not cause trace data to be logged until the
overall trace method is turned on. Even using the category method to turn on all categories do not start logging until the general
trace method to enable traces is called.

**Note: Toolbox tracing options are static to the JVM. There is only one current trace setting for toolbox, regardless of how many connections
are active. Because it is static, the last method to touch the toolbox trace properties is the one in effect.**

For more information about database monitor impacts, see:
[Impact of collecting a DBMON trace - Start Database Monitor - STRDBMON](https://www.ibm.com/support/pages/node/882914)

The following table shows the available methods in the Trace class for starting traces:

**Table 6**
| General Trace Method        | 	Purpose | 
| --------------------------- | ----------|
| `Trace.setTraceOn(boolean)` | Turns tracing for all currently enabled categories on or off.
| `Trace.setFileName(String)` | Directs toolbox trace output to the specified file.

| Trace Category Method       |  	Purpose | 
| --------------------------- | ----------|
| `Trace.setTraceAllOn(boolean)`         | Turns all categories of toolbox tracing on or off.
| `Trace.setTraceConversionOn(boolean)`  | Turns the "conversion" toolbox trace category on or off.
| `Trace.setTraceDatastreamOn(boolean)`  | Turns the "datastream" toolbox trace category.
| `Trace.setTraceDiagnosticOn(boolean)`  | Turns the "diagnostic" toolbox trace category.
| `Trace.setTraceErrorOn(boolean)`       | Turns the "error" toolbox trace category on or off.
| `Trace.setTraceInformationOn(boolean)` | Turns the "information" toolbox trace category on or off.
| `Trace.setTraceJDBCOn(boolean)`        | Turns the "jdbc" toolbox trace category on or off.
| `Trace.setTracePCMLOn(boolean)`        | Turns the "pcml" toolbox trace category on or off.
| `Trace.setTraceProxyOn(boolean)`       | Turns the "proxy" toolbox trace category on or off.
| `Trace.setTraceThreadOn(boolean)`      | Turns the "thread" toolbox trace category on or off.
| `Trace.setTraceWarningOn(boolean)`     | Turns the "warning" toolbox trace category on or off.

Note: most commonly, trace is gathered for all categories. The programmer must use both the general
trace method (to turn on trace) as well as a category method. That is, most use cases will contain the
following two lines, at minimum: 
```java
Trace.setTraceAllOn(true);
Trace.setTraceOn(true);
```

Following is an example of Java source code that can turn on tracing programmatically:

```java
import com.ibm.as400.access.*;

// Java code

Trace.setTraceAllOn(true);
Trace.setFileName("/home/mydir/ToolboxTrace.txt");
Trace.setTraceOn(true);

// API calls here

Trace.setTraceOn(false);
```
 
## Trace Output

The following table lists where to find the traces that are produced by the toolbox and Server Trace options.

**Table 7**
| Trace Type 	               | Output | 
| -------------------------- | -------- |
| Toolbox Trace              | Defaults to Java standard output stream, unless trace file is specified.* 
| Database Monitor (DBMON)   | Monitor file created named QUSRSYS/QJTxxxxxx, where xxxxxx is the job number of the QZDASOINIT server job. For more information about database monitor impacts, see: [Impact of collecting a DBMON trace - Start Database Monitor - STRDBMON](https://www.ibm.com/support/pages/node/882914)
| Debug Job                  | Debug messages are posted to the job log of the QZDASOINIT server job.
| Save Joblog                | Spooled file named QPJOBLOG generated at disconnect time. Associated with the QZDASOINIT server job and the user profile that established the connection.
| Job Trace                  | Spooled file named QPSRVTRCJ generated at disconnect time. Associated with the QZDASOINIT server job and the user profile that established the connection.
| Save SQL                   | Spooled file named JOBSQL generated at disconnect time. Associated with the QZDASOINIT server job and the user profile that established the connection.
| Database Host Server Trace | Same as Job Trace. If both are active, trace points are logged to the same spooled file.

_*The toolbox trace defaults to the standard output stream. In a console application, this information would typically
be sent back to the screen. In WebSphere, it would be directed to the SystemOut.log. In most cases, it is recommended
to use the property to direct output to a specific file to eliminate "clutter" from the trace. However, in a long running
scenario, the automatic wrapping of WebSphere logs could be leveraged to avoid large trace sizes. If a specific file is to
be created, the options are to use a JVM property, the jt400.properties file, or the Trace.setFileName() method._
 
## Suggested Methods

This section describes possible environments where toolbox tracing might be required, and the suggested methods for enabling tracing in each.
 

### Stand-alone Java Application


If the application to be traced is a stand-alone Java program launched with the "Java" command, the recommended method for enabling
traces is with JVM properties provided as arguments to the "Java" command. It is recommended that the categories and output file properties
both are specified. The following is what the command line might look like to launch a Java program with all tracing categories and
directed to an output file:
```bash
java -Dcom.ibm.as400.access.Trace.category=all -Dcom.ibm.as400.access.Trace.file=/home/mydir/ToolboxTrace.txt MyJavaProgram
```

If the application is being launched with the IBM i `RUNJVA` command, the command can be updated to contain the additional properties:
```clle
RUNJVA CLASS(MyJavaProgram) PROP((com.ibm.as400.access.Trace.category all) (com.ibm.as400.access.Trace.file '/home/mydir/ToolboxTrace.txt'))
````

### WebSphere Application Server

If the application to be traced is a toolbox or JDBC application running on WebSphere, the recommended action depends on if any toolbox
JDBC data sources are defined in WebSphere. If a toolbox data source is defined, even if it is not used, it overrides any generic JVM
argument settings when it is loaded. Any changes to toolbox trace settings in WebSphere require a restart of the application server.

For all of the following sections, the document refers to setting generic JVM properties or data source custom properties by using the
WebSphere Admin Console. If multiple JVM arguments are specified in the WebSphere Admin Console, they must be separated by spaces. For
screen captures of the console for these settings on various versions of WebSphere, see
[WebSphere Screenshots for Toolbox Tracing](https://www.ibm.com/support/pages/node/643287).

#### WebSphere Toolbox/JDBC Application Without Data Sources Defined

For this environment, the recommended method to turn on tracing is to set the trace category and output file JVM properties in the
generic arguments of WebSphere.

The following settings are set in generic JVM arguments of WebSphere:
```properties
-Dcom.ibm.as400.access.Trace.category=all
-Dcom.ibm.as400.access.Trace.file=/home/mydir/ToolboxTrace.txt
```

If server traces are requested, also add:
```properties
-Dcom.ibm.as400.access.ServerTrace.JDBC=126
```

#### WebSphere JDBC Application With Data Sources Defined

If a WebSphere application is using the toolbox JDBC driver, and data sources are defined, the recommended action is to set toolbox trace
categories and server traces (if requested) in the data source custom properties. The trace output file must be specified in generic JVM
arguments. Note, EVERY toolbox data source must have its custom properties updated, as the last one loaded is the one that determines
which trace settings are in use.

The following data source custom properties can be set for every toolbox data source:
```properties
toolboxTrace=all
trace=true
```

If server traces are requested, also add:
```properties
serverTraceCategories=126
```

The following property can be specified in generic JVM arguments of WebSphere:
```properties
-Dcom.ibm.as400.access.Trace.file=/home/mydir/ToolboxTrace.txt
```

#### WebSphere Application with Toolbox AND JDBC with Data Sources Defined

If an application in WebSphere is using toolbox (non-JDBC) classes and JDBC data sources, the recommended action is to define the custom
properties for tracing in every toolbox data source, and specify the JVM properties in generic JVM arguments to start tracing. The custom
data source properties and the JVM properties in the previous two sections guarantee that tracing is in place when toolbox classes are
first used.
 
#### Toolbox Application in an RPG Invocated JVM

It is possible to call Java methods from RPG. In this environment, the JVM is implicitly or explicitly started by the RPG program when the
Java method is called. An IBM i environment variable is defined that can be used to specify JVM properties that the invoked JVM uses. Use
the following command to add the environment variable when tracing needs to be done in this kind of environment. The environment variable
must be present in the job before the JVM is started.
```clle
ADDENVVAR ENVVAR(QIBM_RPG_JAVA_PROPERTIES)
VALUE('-Dcom.ibm.as400.access.Trace.category=all;-Dcom.ibm.as400.access.Trace.file=/home/mydir/ToolboxTrace.txt;')
LEVEL(*SYS)
```

If server traces are requested, use the following command:
```clle
ADDENVVAR ENVVAR(QIBM_RPG_JAVA_PROPERTIES)
VALUE('-Dcom.ibm.as400.access.Trace.category=all;-Dcom.ibm.as400.access.Trace.file=/home/mydir/ToolboxTrace.txt;-Dcom.ibm.as400.access.ServerTrace.JDBC=126;')
LEVEL(*SYS)
```
 
#### Rational Developer for IBM i (RDI)

Check RDI shortcut properties to determine which directory it is being launched from. In that directory, you find a file named eclipse.ini.
Edit the eclipse.ini file to add the JVM properties for the traces. For example, add the following lines for a typical toolbox trace that
creates a trace file in the `c:\temp` directory:
```properties
-Dcom.ibm.as400.access.Trace.category=all
-Dcom.ibm.as400.access.Trace.file=c:\temp\ToolboxTrace.txt
```

To disable the tracing after the data is collected, you can either delete these lines from the eclipse.ini file or you can comment them out
by adding a hash sign as the first character on the line, for example:
```properties
#-Dcom.ibm.as400.access.Trace.category=all
#-Dcom.ibm.as400.access.Trace.file=c:\temp\ToolboxTrace.txt
```

It is sometimes preferrable to comment out those lines because it makes it easy to add the trace feature back by simply removing the hash sign.

Alternatively the tracing can be enabled in RDi in "Run Configurations". Place the toolbox properties in the Arguments tab.
Now when you run the Java application the trace is stored in the Trace file that was specified.



## Example Toolbox Trace Output

The following shows the start of a toolbox log. If tracing is properly configured, output similar to the following is observed.
```pascal
Toolbox for Java - Open Source Software, JTOpen 6.2, codebase 5761-JC1 V6R1M0.4
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:592 CDT 2008  Getting system property: 'com.ibm.as400.access.Trace.enabled'
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:592 CDT 2008  Value found in system properties:  'null'
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Loading Properties class: 'com.ibm.as400.access.Properties'
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Unable to load class: com.ibm.as400.access.Properties
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Loading jt400.properties file: 'com.ibm.as400.access.jt400.properties'
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Trying with Class.getResourceAsStream(): false
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Trying with ClassLoader.getSystemResourceAsStream(): false
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Load of jt400.properties failed.
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Value found in jt400.properties file: 'null'
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  Value not found.
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : access = "all".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : block size = "32".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : block criteria = "2".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : date format = "".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : date separator = "".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : decimal separator = "".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : errors = "basic".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : extended dynamic = "false".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : libraries = "".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : naming = "sql".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : package = "".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : package add = "true".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : package cache = "false".
Thread[AWT-EventQueue-0,6,main]  Wed May 28 14:58:24:602 CDT 2008  as400: Properties  (9826960) : package clear = "false".
```

## Dynamic Toolbox Tracing


There are times when a problem is intermittent, and it is not feasible to leave a toolbox trace on indefinitely. In these cases,
it might be appropriate to use the dynamic tracing capability. In these cases, the JVM is started with the property to enable a
trace monitor, and tracing can be toggled when symptoms to be trace arise. For more information, see 
[this document](http://www.ibm.com/support/docview.wss?uid=nas8N1012340).
 

## Submitting the Trace to IBM Support

The trace can be compressed to reduce its size with any common compression tool. Compression speeds up the transmission of the
trace to IBM. The trace can be uploaded to IBM by using the Enhanced Customer Data Repository tools available at
[ECuRep](http://www.ecurep.ibm.com). This site has secure and standard upload web applications as well as tools that can be
installed on your computers to upload data by using multiple parallel connections, which are useful for large uploads from networks
were individual connections have limited bandwidth. To get to those tools, click the ECuRep tab, which redirects you to a new page,
then the 'Send data' tab.

----------------------------------
_Attribution: this page is derived from "The All-In-One Toolbox Tracing Guide" published by IBM support_
