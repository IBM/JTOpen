# Logging with Rational Developer for IBM i (RDI)

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
![0 6F50](https://github.com/IBM/JTOpen/assets/17914061/ade744dd-1df0-4f33-8c4a-6af234a74b00)

