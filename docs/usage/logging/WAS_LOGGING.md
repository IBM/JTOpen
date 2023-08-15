# Logging with WebSphere Application Server (WAS)



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