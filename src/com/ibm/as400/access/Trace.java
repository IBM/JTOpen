///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Trace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;                                   // $D0A
import java.util.Hashtable;                                         // $W1A
import java.util.Vector;
import java.util.Enumeration;

/**
  The Trace class logs trace points and diagnostic messages.  Each trace
  point and diagnostic message is logged by category.  The valid categories are:
  <br>
  <ul>
  <li>DATASTREAM<br>
  This category is used by Toolbox classes to log data flow between the
  local host and the remote system.  It is not intended for use by
  application classes.
  <li>DIAGNOSTIC<br>
  This category is used to log object state information.
  <li>ERROR<br>
  This category is used to log errors that cause an exception.
  <li>INFORMATION<br>
  This category is used to track the flow of control through the code.
  <li>WARNING<br>
  This category is used to log errors that are recoverable.
  <li>CONVERSION<br>
  This category is used by Toolbox classes to log character set
  conversions between Unicode and native code pages.  It is not intended
  for use by application classes.
  <li>PROXY<br>
  This category is used by Toolbox classes to log data flow between the
  client and the proxy server.  It is not intended for use by application classes.
  <li>PCML<br>
  This category is used to determine how PCML interprets the data that is 
  sent to and from the system.    
  <li>JDBC<br>
  This category is used to include JDBC data in the standard Toolbox trace.
  </ul>

  <P>
  The caller can enable or disable all tracing or specific
  trace categories.  Enabling or disabling one category does not
  affect other categories.  Once appropriate category traces
  are enabled, trace must be turned on to get trace data.  For example,

  <UL>
  <pre>
      // By default, trace is disabled for all categories and
      // tracing is off.
      ..
      Trace.setTraceErrorOn(true);          // Enable error messages
      Trace.setTraceWarningOn(true);        // Enable warning messages

      Trace.setTraceOn(true);               // Turn trace on for all
                                            // Enabled categories

      ..
      Trace.setTraceOn(false);              // Turn trace off
      ..
      Trace.setTraceOn(true);               // Turn trace back on
      ..
      Trace.setTraceInformationOn(true);    // Enable informational messages.
                                            // Since trace is still on, no
                                            // other action is required to
                                            // get this data

      ..
      Trace.setTraceWarningOn(false);       // Disable warning messages.  Since
                                            // trace is still on, the other
                                            // categories will still be logged
  </pre>
  </UL>

  The traces are logged to standard out by default.  A file name can
  be provided to log to a file.  File logging is only possible in an
  application as most browsers do not allow access to the local file system.

  <P>
  Trace data can also be specified by component.  Trace data is always
  written to the default log but component tracing provides a way
  to write trace data to a separate log.

  <P>
  The following example logs data for Function123 into log file
  c:\Function123.log, and logs data for Function456 into log file
  c:\Function456.log.  Data for these two components is also traced to
  the normal trace file (standard output in this case since that is the
  default for normal tracing).  The result is three sets of data --
  a file containing trace data for only Function123, a file containing trace
  data for only Function456, and standard output which records all trace
  data.  In the example a Java String object is used to indicate the
  component, but any object can be used.
  <UL>
  <PRE>
      // tracing is off by default
      ..
      String cmpF123 = "Function123";      // More efficient to create an object
      String cmpF456 = "Function456";      // than many String literals.

                                             // Specify where data should go
                                             // for the two components.
      Trace.setFileName(cmpF123, "c:\\Function123.log");
      Trace.setFileName(cmpF456, "c:\\Function456.log");

      Trace.setTraceInformationOn(true);     // Trace only the information category.
      Trace.setTraceOn(true);                // Turn tracing on.
      ..
      Trace.log(cmpF123, Trace.INFORMATION, "I am here");
      ..
      Trace.log(cmpF456, Trace.INFORMATION, "I am there");
      ..
      Trace.log(cmpF456, Trace.INFORMATION, "I am everywhere");
      ..
      Trace.setTraceOn(false);               // Turn tracing off.
  </PRE>
  </UL>

  <P>
  Component tracing provides an easy way to write application specific
  trace data to a log or standard output.  Any application and the
  Toolbox classes can use trace to log messages.  With component tracing,
  application data can be easily separated from other data.  For example,

  <UL>
  <pre>
      String myComponent = "com.myCompany";      // More efficient to create an object
                                                 // than many String literals.

      Trace.setFileName("c:\\bit.bucket");       // Send default trace data to
                                                 // a file.

      Trace.setTraceInformationOn(true);         // Enable information messages.
      Trace.setTraceOn(true);                    // Turn trace on.

      ...

                             // Since no file was specified, data for
                             // myComponent goes to standard output.
                             // Other information messages are sent to a file.
      Trace.log(myComponent, Trace.INFORMATION, "my trace data");

  </pre>
  </UL>


  <P>
  Two techniques can be used to log information:

  <UL>
  <pre>
      ..
      // Let the Trace method determine if logging should occur
      Trace.log(Trace.INFORMATION, "I got here...");
      ..
      // Pre-determine if we should log.  This may be more efficient
      // if a lot of processing in needed to generate the trace data.
      if (Trace.isTraceOn() && Trace.isTraceInformationOn())
      {
            Trace.log(Trace.INFORMATION, "I got here...");
      }
  </pre>
  </UL>

  <P>
  It is suggested that programs provide some mechanism to enable tracing at run-time, so
  that the modification and recompilation of code is not necessary.  Two possibilities
  for that mechanism are a command line argument (for applications) or a menu option
  (for applications and applets).

  <p>
  In addition, tracing can be set using the "com.ibm.as400.access.Trace.category"
  and "com.ibm.as400.access.Trace.file" <a href="doc-files/SystemProperties.html">system properties</a>.

  <p>
  Note: This class can exploit a standard Java Logger if one is defined in the JVM (per JSR 47, package <tt>java.util.logging</tt>, added in J2SE 1.4).
  See {@link #LOGGER_NAME LOGGER_NAME}.
 **/


public class Trace
{
  private static final String CLASSNAME = "com.ibm.as400.access.Trace";

  static boolean traceOn_; //@P0C
  static boolean traceInfo_;
  static boolean traceWarning_;
  static boolean traceError_;
  static boolean traceDiagnostic_;
  static boolean traceDatastream_;
  static boolean traceConversion_;
  static boolean traceProxy_;                           // @D0A
  static boolean traceThread_;                          // @D3A
  static boolean traceJDBC_;                            // @D5A
  static boolean tracePCML_;

  private static int mostRecentTracingChange_;  // either 0 (no action), TURNED_TRACE_OFF, or TURNED_TRACE_ON
  private static final int TURNED_TRACE_ON = 1;
  private static final int TURNED_TRACE_OFF = 2;
  private static boolean aTraceCategoryHasBeenActivated_ = false;  // goes to 'true' when any setTraceXxx() method has been called with argument 'true'

  private static String fileName_ = null;
  private static PrintWriter destination_ = new PrintWriter(System.out, true);  // never null
  private static boolean userSpecifiedDestination_ = false;  // true if setFileName() or setPrintWriter() was called with a non-null argument

  private static Hashtable printWriterHash = new Hashtable();      // @W1A
  private static Hashtable fileNameHash    = new Hashtable();      // @W1A
  private static SimpleDateFormat timeStampFormatter_ = new SimpleDateFormat( "EEE MMM d HH:mm:ss:SSS z yyyy" );


  /**
    'Data stream' trace category.  This category is used by Toolbox classes
    to log data flow between the local host and the remote system.  It is
    not intended for use by application classes.
   **/
  public  static final int DATASTREAM = 0;
  //private static final int FIRST_ONE  = 0; // @W1A

  /**
    'Diagnostic message' trace category.  This category is used to log object
    state information.
   **/
  public static final int DIAGNOSTIC = 1;
  /**
    'Error message' trace category.  This category is used to log errors that
    cause an exception.
   **/
  public static final int ERROR = 2;
  /**
    'Information message' trace category.  This category is used to track
    the flow of control through the code.
   **/
  public static final int INFORMATION = 3;
  /**
    'Warning message' trace category.  This category is used to log errors
    that are recoverable.
   **/
  public static final int WARNING = 4;
  /**
    'Character set conversion' trace category.  This category is used by Toolbox
    classes to log conversions between Unicode and native code pages.  It is
    not intended for use by application classes.
   **/
  public static final int CONVERSION = 5;

  /**
    'Proxy' trace category.  This category is used by Toolbox classes to log data
    flow between the client and the proxy server.  It is not intended for
    use by application classes.
   **/
  public static final int PROXY = 6;                           // @D0A

  /**
    'PCML' trace category.  This category is used to determine how PCML interprets
    the data that is sent to and from the system.  
   **/
  public static final int PCML = 7;                            // @D8A

  /**
    'JDBC' trace category.  This category is used by the Toolbox JDBC driver to 
    determine whether or not JDBC data should be included in the standard Toolbox trace.
    This setting is independent of what is set using the {@link java.sql.DriverManager DriverManager} class.
  **/
  public static final int JDBC = 8;


  // This is used so we don't have to change our bounds checking every time we add a new trace category.
  //private static final int LAST_ONE = 9; // @D3A @D8C

  // The following are trace categories which cannot be log()-ed to directly.
  /*
    Thread trace category.  This category is used to enable or disable tracing of thread
    information. This is useful when debugging multi-threaded applications. Trace
    information cannot be directly logged to this category.
   */
  // @E1D private static final int THREAD = 99; // @D3A

  /*
    All trace category. This category is
    used to enable or disable tracing for all of the other categories at
    once. Trace information cannot be directly logged to this category.
  */
  // @E1D private static final int ALL = 100; //@D2A


  /**
   Name of the instance of <tt>java.util.logging.Logger</tt> that the Toolbox uses.
   If no Logger by this name exists in the JVM, then traditional Toolbox tracing is done.
   To activate a Toolbox logger, the application can simply call Logger.getLogger(Trace.LOGGER_NAME).
   <br>Note: This constant resolves to the value <tt>com.ibm.as400.access</tt>.
   **/
  public static final String LOGGER_NAME = "com.ibm.as400.access";

  // Design note: We needed to segregate the Logger logic into a separate class.  The Java logging package doesn't exist prior to JDK 1.4, so if we're executing in an older JVM, we get SecurityException's if the JVM sees _any_ runtime reference to a java.util.logging class.
  private static ToolboxLogger logger_ = null;
  private static boolean firstCallToFindLogger_ = true;
  private static boolean JDK14_OR_HIGHER;

  // @D0A
  static
  {
    try {
      Class.forName("java.util.logging.LogManager"); // Class added in JDK 1.4.
      JDK14_OR_HIGHER = true;  // If we got this far, we're on JDK 1.4 or higher.
    }
    catch (Throwable e) {      // We're not on JDK 1.4 or higher,
      JDK14_OR_HIGHER = false; // so don't even try to get the Toolbox Logger.
    }

    loadTraceProperties ();
  }



  // This is only here to prevent anyone from instantiating a Trace object.
  private Trace()
  {
  }


  /**
    Returns the trace file name.
    @return  The file name if logging to file.  If logging to System.out,
    null is returned.
   **/
  public static String getFileName()
  {
    return fileName_;
  }


  /**
    Returns the trace file name for the specified component.  Null
    is returned if no file name has been set for the component.
    @return  The file name for the specified component.  Null is
             returned if no file name has been set for the component.
   **/
  public static String getFileName(Object component)
  {
    if (component == null)
      throw new NullPointerException("component");

    return(String) fileNameHash.get(component);
  }


  /**
    Returns the PrintWriter object.
    @return  The PrintWriter object for the trace data output.
   **/
  public static PrintWriter getPrintWriter()
  {
    return destination_;
  }


  /**
    Returns the print writer object for the specified component.  Null
    is returned if no writer or file name has been set.  If a file
    name for a component is set, that component automatically
    gets a print writer.
    @return  The print writer object for the specified component.
    If no writer or file name has been set, null is returned.
   **/
  public static PrintWriter getPrintWriter(Object component)
  {
    if (component == null)
      throw new NullPointerException("component");

    return(PrintWriter) printWriterHash.get(component);
  }



  //@D2A
  /**
    Indicates if all of the tracing categories are enabled.
    @return  true if all categories are traced; false otherwise.
   **/
  public static final boolean isTraceAllOn()
  {
    return traceConversion_ && traceDatastream_ && traceDiagnostic_ &&
    traceError_ && traceInfo_ && traceProxy_ &&
    traceWarning_ && traceThread_ && traceJDBC_ && tracePCML_;                 //@D3C @D5C
  }


  /**
    Indicates if character set conversion tracing is enabled.
    @return  true if conversions are traced; false otherwise.
   **/
  public static final boolean isTraceConversionOn()
  {
    return traceConversion_;
  }

  /**
    Indicates if data stream tracing is enabled.
    @return  true if data streams are traced; false otherwise.
   **/
  public static final boolean isTraceDatastreamOn()
  {
    return traceDatastream_;
  }

  /**
    Indicates if diagnostic tracing is enabled.
    @return  true if diagnostic messages are traced; false otherwise.
   **/
  public static final boolean isTraceDiagnosticOn()
  {
    return traceDiagnostic_;
  }

  /**
    Indicates if error tracing is enabled.
    @return  true if error messages are traced; false otherwise.
   **/
  public static final boolean isTraceErrorOn()
  {
    return traceError_;
  }

  /**
    Indicates if information tracing is enabled.
    @return  true if information messages are traced; false otherwise.
   **/
  public static final boolean isTraceInformationOn()
  {
    return traceInfo_;
  }


  /**
   *  Indicates if JDBC tracing is enabled.
   *  @return true if JDBC messages are traced; false otherwise.
   **/
  public static final boolean isTraceJDBCOn()             // @D5A
  {
    return traceJDBC_;
  }

  /**
    Indicates if overall tracing is enabled.  If this is false, no tracing occurs.
    @return  true if tracing is enabled; false otherwise.
   **/
  public static final boolean isTraceOn()
  {
    return traceOn_;
  }

  /**
    Indicates if tracing is enabled for the specified category.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC,
    ERROR, INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @return  true if tracing for the category is enabled; false otherwise.
   **/
  public static final boolean isTraceOn(int category)
  {
    switch (category) {
      case DATASTREAM:
        return traceDatastream_;
      case DIAGNOSTIC:
        return traceDiagnostic_;
      case ERROR:
        return traceError_;
      case INFORMATION:
        return traceInfo_;
      case WARNING:
        return traceWarning_;
      case CONVERSION:
        return traceConversion_;
      case PROXY:
        return traceProxy_;
      case PCML:
        return tracePCML_;
      case JDBC:
        return traceJDBC_;
      default:
        return false;
    }
  }


  /**
   *  Indicates if PCML tracing is enabled.
   *  @return true if PCML messages are traced; false otherwise.
   **/
  public static final boolean isTracePCMLOn()             // @D5A
  {
    return tracePCML_;
  }

  // @D0A
  /**
    Indicates if proxy tracing is enabled.
    @return  true if proxy tracing is enabled; false otherwise.
   **/
  public static final boolean isTraceProxyOn()
  {
    return traceProxy_;
  }

  // @D3A
  /**
    Indicates if thread tracing is enabled.
    @return  true if thread tracing is enabled; false otherwise.
   **/
  public static final boolean isTraceThreadOn()
  {
    return traceThread_;
  }


  /**
    Indicates if warning tracing is enabled.
    @return  true if warning messages are traced; false otherwise.
   **/
  public static final boolean isTraceWarningOn()
  {
    return traceWarning_;
  }



  // Note: If the properties file specifies the "com.ibm.as400.access.Trace.file" property, then we will disregard any existing Toolbox Logger.
  // @D0A
  static void loadTraceProperties ()
  {
    // Load and apply the trace categories system property.
    String categories = SystemProperties.getProperty(SystemProperties.TRACE_CATEGORY);
    if (categories != null)
    {
      //setTraceOn (true);  //@pdd
      StringTokenizer tokenizer = new StringTokenizer (categories, ", ;");
      if(!tokenizer.hasMoreTokens ())                       //@pda
      {                                                     //@pda
          //-Dcom.ibm.as400.access.Trace.category=""        //@pda
          setTraceOn(false);                                //@pda
      }                                                     //@pda
      else                                                  //@pda
          setTraceOn (true);                                //@pda
      
      while (tokenizer.hasMoreTokens ())
      {
        String category = tokenizer.nextToken ();
        if (category.equalsIgnoreCase ("datastream"))
          setTraceDatastreamOn (true);
        else if (category.equalsIgnoreCase ("diagnostic"))
          setTraceDiagnosticOn (true);
        else if (category.equalsIgnoreCase ("error"))
          setTraceErrorOn (true);
        else if (category.equalsIgnoreCase ("information"))
          setTraceInformationOn (true);
        else if (category.equalsIgnoreCase ("warning"))
          setTraceWarningOn (true);
        else if (category.equalsIgnoreCase ("conversion"))
          setTraceConversionOn (true);
        else if (category.equalsIgnoreCase ("proxy"))
          setTraceProxyOn (true);
        else if (category.equalsIgnoreCase ("thread")) //@D3A
          setTraceThreadOn (true); //@D3A
        else if (category.equalsIgnoreCase ("jdbc"))   // @D5A
          setTraceJDBCOn (true);   // @D5A
        else if (category.equalsIgnoreCase ("pcml"))   // @D5A
          setTracePCMLOn (true);   // @D5A
        else if (category.equalsIgnoreCase ("all")) //@D2A
          setTraceAllOn (true); //@D2A
        else if (category.equalsIgnoreCase ("none"))                                   //@pda
            setTraceOn(false);  //fix for -Dcom.ibm.as400.access.Trace.category="none" //@pda 
        else
        {
          if (isTraceOn ())
            Trace.log (Trace.WARNING, "Trace category not valid: " + category + ".");
        }
      }
    }  // categories != null

    // Load and apply the trace file system property.
    String file = SystemProperties.getProperty (SystemProperties.TRACE_FILE);
    if (file != null)
    {
      try
      {
        setFileName (file);
        destination_.println("Toolbox for Java - " + Copyright.version);
      }
      catch (IOException e)
      {
        if (isTraceOn ())
          Trace.log (Trace.WARNING, "Trace file not valid: " + file + ".", e);
      }
    }

    // Load and apply the trace enabled system property.
    String enabled = SystemProperties.getProperty (SystemProperties.TRACE_ENABLED);
    if (enabled != null)
    {
      boolean value = Boolean.valueOf(enabled).booleanValue();
      traceOn_ = value;
    }

  }

  /**
   Logs the path where the ClassLoader found the specified class.
   Each specific class is logged no more than once.
   @param className  The package-qualified class name.
   **/
  static final void logLoadPath(String className)
  {
    if (traceDiagnostic_ &&
        className != null)
    {
      String loadPath = null;
      try
      {
        ClassLoader loader =  Class.forName(className).getClassLoader();
        if (loader != null)
        {
          String resourceName = className.replace('.', '/') + ".class";
          java.net.URL resourceUrl = loader.getResource(resourceName);
          if (resourceUrl != null) {
            loadPath = resourceUrl.getPath();
            // Note: The following logic strips the entry name from the end of the path.
            // int delimiterPos = loadPath.lastIndexOf('!');
            // if (delimiterPos != -1) loadPath = loadPath.substring(0, delimiterPos);
          }
        }
      }
      catch (Throwable t) {}

      String message = "Class " + className + " was loaded from " + loadPath;
      logData(null, DIAGNOSTIC, message, null);
    }
  }

  // Log time stamp information in the trace.
  private static void logTimeStamp(Object component, PrintWriter pw) //@W1C
  {
    if (component != null)                                   //@W1A
      if (component.toString() != null)                     //@W1A
        pw.print("[" + component.toString() + "]  ");      //@W1A

    if (traceThread_)                                        //@D3A @W1C
    {
      //@D3A @W1C
      pw.print(Thread.currentThread().toString());           //@D3A @W1C
      pw.print("  ");                                        //@D3A @W1C
    }                                                        //@D3A @W1C

    synchronized (timeStampFormatter_) // date formats are not synchronized
    {
      pw.print(timeStampFormatter_.format(new Date()));             // @W1C
    }
    pw.print("  ");                                          // @W1C
  }

  // Log time stamp information in the trace.
  private static void logTimeStamp(Object component, StringBuffer buf)
  {
    if (component != null)
      if (component.toString() != null)
        buf.append("[" + component.toString() + "]  ");

    if (traceThread_)
    {
      buf.append(Thread.currentThread().toString());
      buf.append("  ");
    }

    synchronized (timeStampFormatter_) // date formats are not synchronized
    {
      buf.append(timeStampFormatter_.format(new Date()));
    }
    buf.append("  ");
  }


  // This is the routine that actually writes to the log.
  private static final void logData(Object    component,
                                    int       category,
                                    String    message,
                                    Throwable e)
  {
    // See if tracing is activated for specified category.
    if ((traceOn_ && traceCategory(category)) ||
        (findLogger() && logger_.isLoggable(category)))
    {
      // Two different cases: Either traditional Toolbox trace, or Java Logging.

      if (logger_ == null || userSpecifiedDestination_)  // traditional trace
      {
        // Validate parameters.
        //@D2 - note: It doesn't make sense to log something to Trace.ALL,
        // so we count it as an illegal argument.
        // if (category < FIRST_ONE || category > LAST_ONE) // @D0C @D3C
        // {
        //    throw new ExtendedIllegalArgumentException("category ("
        //          + Integer.toString(category)
        //          + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        // }

        // First, write to the default log
        synchronized(destination_)
        {
          // If component tracing is being used, log the component name to
          // the default log as well as the specific component log.
          if (component != null && getFileName(component) != null)        //$W2A
          {
            //$W2A
            logTimeStamp(component, destination_);                         //$W2A
            destination_.println(message);                                 //$W2A
          }                                                                 //$W2A
          else                                                              //$W2A
          {
            //$W2A
            // Only trace to the default log if we are not doing component
            // tracing.  This will avoid duplicate messages in the default
            // log.
            if (component == null)                                       //$W2A
            {
              //$W2A
              logTimeStamp(null, destination_);                           //$W2A
              destination_.println(message);                              //$W2A
            }                                                              //$W2A
          }                                                                 //$W2A

          if (e != null)
            e.printStackTrace(destination_);
          else if (category == ERROR)
            new Throwable().printStackTrace(destination_);
        }

        if (component != null)
        {
          PrintWriter pw = (PrintWriter) printWriterHash.get(component);
          if (pw == null)
          {
            pw = new PrintWriter(System.out, true);
            printWriterHash.put(component, pw);
          }
          synchronized(pw)
          {
            logTimeStamp(component, pw);
            pw.println(message);
            if (e != null)
              e.printStackTrace(pw);
            else if (category == ERROR)
              new Throwable().printStackTrace(pw);
          }
        }
      }  // traditional Toolbox tracing

      else  // We are logging to a Java Logger.
      {
        // Log to the Logger instead of to destination_.
        // Don't bother splitting up into separate component-specific trace files.
        StringBuffer buf = new StringBuffer();
        logTimeStamp(component, buf);
        buf.append(message);

        if (e != null) {
          logger_.log(category, buf.toString(), e);
        }
        else if (category == ERROR) {
          logger_.log(category, buf.toString(), new Throwable());
        }
        else {
          logger_.log(category, buf.toString());
        }

      }  // using a Java Logger
    }
    else {}  // tracing is not activated for the specified category, so do nothing
  }

  /**
    Logs a message in the specified category.  If the category is disabled,
    nothing is logged.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC,
    ERROR, INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
   **/
  public static final void log(int category, String message)
  {
    //@W1 - validating the category was moved to the common routine

    if (message == null)
    {
      throw new NullPointerException("message");
    }

    logData(null, category, message, null);
  }


  /**
    Logs a message for the specified component for the specified category.
    If the category is disabled, nothing is logged.  If no print writer
    or file name has been set for the component, nothing is logged.
    @param  component The component to trace.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC,
    ERROR, INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
   **/
  public static final void log(Object component, int category, String message)
  {
    //@W1 - validating the category was moved to the common routine

    if (message == null)
    {
      throw new NullPointerException("message");
    }
    if (component == null)
    {
      throw new NullPointerException("component");
    }

    logData(component, category, message, null);
  }


  /**
    Logs a message in the specified category.  If the category is disabled, nothing is logged.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION,
    WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  e  The Throwable object that contains the stack trace to log.
   **/
  public static final void log(int category, String message, Throwable e)
  {
    //@W1 - validating the category was moved to the common routine

    if (message == null)
    {
      throw new NullPointerException("message");
    }
    if (e == null)
    {
      throw new NullPointerException("e");
    }

    logData(null, category, message, e);
  }


  /**
    Logs a message in the specified category for the specified component.
    If the category is disabled, nothing is logged.
    @param  component The component to trace.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
    INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  e  The Throwable object that contains the stack trace to log.
   **/
  public static final void log(Object component, int category, String message, Throwable e)
  {
    //@W1 - validating the category was moved to the common routine
    if (message == null)
    {
      throw new NullPointerException("message");
    }
    if (e == null)
    {
      throw new NullPointerException("e");
    }
    if (component == null)
    {
      throw new NullPointerException("component");
    }

    logData(component, category, message, e);
  }

  /**
    Logs a message in the specified category.  If the category is disabled, nothing is logged.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
            INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  e  The Throwable object that contains the stack trace to log.
   **/
  public static final void log(int category, Throwable e)
  {
    if (e.getLocalizedMessage() == null)                           //$B2A
      log(category, "Exception does not contain a message.", e);  //$B2A
    else                                                           //$B2A
      log (category, e.getLocalizedMessage (), e);                //$B2C
  }


  /**
    Logs a message in the specified category for the specified component.
    If the category is disabled, nothing is logged.
    @param  component The component to trace.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
            INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  e  The Throwable object that contains the stack trace to log.
   **/
  public static final void log(Object component, int category, Throwable e)
  {
    if (e.getLocalizedMessage() == null)                                       //$B2A
      log(component, category, "Exception does not contain a message.", e);   //$B2A
    else                                                                       //$B2A
      log (component, category, e.getLocalizedMessage (), e);                 //$B2C
  }



  /**
    Logs a message and an integer value in the specified category.  If the
    category is disabled, nothing is logged.  The integer value is appended
    to the end of the message, preceded by two blanks.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                      INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  value  The integer value to log.
   **/
  public static final void log(int category, String message, int value)
  {
    if (message == null)
      throw new NullPointerException("message");
    else
      log(category, message + "  " + value);
  }



  /**
    Logs a message and a String value in the specified category.  If the
    category is disabled, nothing is logged.  The String value is appended
    to the end of the message, preceded by two blanks.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                      INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  value  The String value to log.
   **/
  public static final void log(int category, String message, String value)
  {
    if (message == null)
      throw new NullPointerException("message");
    else
      log(category, message + "  " + value);
  }



  /**
    Logs a message and an integer value in the specified category for the
    specified component.  If the
    category is disabled, nothing is logged.  The integer value is appended
    to the end of the message, preceded by two blanks.
    @param  component The component to trace.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                      INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  value  The integer value to log.
   **/
  public static final void log(Object component, int category, String message, int value)
  {
    if (message == null)
      throw new NullPointerException("message");
    else
      log(component, category, message + "  " + value);
  }


  /**
    Logs a SSL message based on the SSLException integer values to the specified category.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                      INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  sslCategory The SSLException category.
    @param  sslError    The SSLException error.
    @param  sslInt1     The SSLException Int1.
   **/
  static final void logSSL(int category, int sslCategory, int sslError, int sslInt1)            //$B1A
  {
    log(Trace.ERROR, "An SSLException occurred, turn on DIAGNOSITC tracing to see the details.");
    log(category, "SSL Category: " + sslCategory);
    log(category, "SSL Error: " + sslError);
    log(category, "SSL Int1: " + sslInt1);
  }


  /**
    Logs a message and a boolean value in the specified category.
    If the category is disabled, nothing is logged.  The boolean
    value is appended to the end of the message, preceded by two blanks.
    true is logged for true, and false is logged for false.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                      INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  value  The boolean data to log.
   **/
  public static final void log(int category, String message, boolean value)
  {
    if (message == null)
      throw new NullPointerException("message");
    else
      log(category, message + "  " + value);
  }

  /**
    Logs a message and a boolean value in the specified category
    for the specified component.
    If the category is disabled, nothing is logged.  The boolean
    value is appended to the end of the message, preceded by two blanks.
    true is logged for true, and false is logged for false.
    @param  component The component to trace.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                      INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  value  The boolean data to log.
   **/
  public static final void log(Object component, int category, String message, boolean value)
  {
    if (message == null)
      throw new NullPointerException("message");
    else
      log(component, category, message + "  " + value);
  }




  /**
    Logs a message and byte data in the specified category.  If the category is disabled, nothing is logged.  The byte data is appended to the end of the message, sixteen bytes per line.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR, INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  data  The bytes to log.
   **/
  public static final void log(int category, String message, byte[] data)
  {
    if (data == null)
    {
      if (message == null)                             //@D2a
        throw new NullPointerException("message");    //@D2a

      log(category, message + "  " + "(null)");
    }
    else
    {
      log(category, message, data, 0, data.length);
    }
  }




  /**
    Logs a message and byte data in the specified category for the
    specified component.  If the category is disabled, nothing is logged.
    The byte data is appended to the end of the message, sixteen bytes per line.
    @param  component The component to trace.
    @param  category  The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                      INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  data  The bytes to log.
   **/
  public static final void log(Object component, int category, String message, byte[] data)
  {
    if (data == null)
    {
      if (message == null)                             //@D2a
        throw new NullPointerException("message");    //@D2a

      log(component, category, message + "  " + "(null)");
    }
    else
    {
      log(component, category, message, data, 0, data.length);
    }
  }



  /**
    Logs a message and byte data in the specified category.  If the
    category is disabled, nothing is logged.  The byte data is
    appended to the end of the message, sixteen bytes per line.
    @param category The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                    INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  data  The bytes to log.
    @param  offset  The start offset in the data.
    @param  length  The number of bytes of data to log.
   **/

  public static final void log(int category, String message, byte[] data, int offset, int length)
  {
    // Validate parameters.
    //@D2 - note: It doesn't make sense to log something to Trace.ALL,
    //  so we count it as an illegal argument.
    //if (category < FIRST_ONE || category > LAST_ONE)                  // @D0C @D3C
    //{
    //    throw new ExtendedIllegalArgumentException("category ("
    //                  + Integer.toString(category)
    //                  + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    //}
    if (message == null)
    {
      throw new NullPointerException("message");
    }
    if (data == null)
    {
      throw new NullPointerException("data");
    }

    if ((traceOn_ && traceCategory(category)) ||
        (findLogger() && logger_.isLoggable(category)))
    {
      if (logger_ == null || userSpecifiedDestination_)  // traditional trace
      {  // log to destination_
        synchronized(destination_)
        {
          logTimeStamp(null, destination_);
          destination_.println(message);
          printByteArray(destination_, data, offset, length);
          if (category == ERROR)
          {
            new Throwable().printStackTrace(destination_);
          }
        }
      }
      else  // log to logger_
      {
        StringBuffer buf = new StringBuffer();
        logTimeStamp(null, buf);
        printByteArray(buf, data, offset, length);
        if (category == ERROR) {
          logger_.log(category, buf.toString(), new Throwable());
        }
        else {
          logger_.log(category, buf.toString());
        }
      }
    }
  }


  /**
    Logs a message and byte data in the specified category
    for the specified component.  If the
    category is disabled, nothing is logged.  The byte data is
    appended to the end of the message, sixteen bytes per line.
    @param  component The component to trace.
    @param category The message category [DATASTREAM, DIAGNOSTIC, ERROR,
                    INFORMATION, WARNING, CONVERSION, PROXY, JDBC].
    @param  message  The message to log.
    @param  data  The bytes to log.
    @param  offset  The start offset in the data.
    @param  length  The number of bytes of data to log.
   **/
  public static final void log(Object component, int category, String message,
                               byte[] data, int offset, int length)
  {
    // Validate parameters.
    //@D2 - note: It doesn't make sense to log something to Trace.ALL,
    // so we count it as an illegal argument.
    //if (category < DATASTREAM || category > LAST_ONE)                  // @D0C @D3C
    //{
    //    throw new ExtendedIllegalArgumentException("category ("
    //                  + Integer.toString(category)
    //                  + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    //}
    if (message == null)
    {
      throw new NullPointerException("message");
    }
    if (data == null)
    {
      throw new NullPointerException("data");
    }
    if (component == null)
    {
      throw new NullPointerException("category");
    }

    if ((traceOn_ && traceCategory(category)) ||
        (findLogger() && logger_.isLoggable(category)))
    {
      if (logger_ == null || userSpecifiedDestination_)  // traditional trace
      {  // log to component-specific trace file
        PrintWriter pw = (PrintWriter) printWriterHash.get(component);
        if (pw == null)
        {
          pw = new PrintWriter(System.out, true);
          printWriterHash.put(component, pw);
        }

        synchronized(pw)
        {
          logTimeStamp(component, pw);
          pw.println(message);
          printByteArray(pw, data, offset, length);
          if (category == ERROR)
          {
            new Throwable().printStackTrace(pw);
          }
        }
      }
      log(category, message, data, offset, length);
    }
  }


  // Logs data from a byte array starting at offset for the length specified.
  // Output sixteen bytes per line, two hexidecimal digits per byte, one
  // space between bytes.
  private static void printByteArray(PrintWriter pw, byte[] data, int offset, int length)
  {
    for (int i = 0; i < length; i++, offset++)
    {
      int leftDigitValue = (data[offset] >>> 4) & 0x0F;
      int rightDigitValue = data[offset] & 0x0F;
      // 0x30 = '0', 0x41 = 'A'
      char leftDigit = leftDigitValue < 0x0A ? (char)(0x30 + leftDigitValue) : (char)(leftDigitValue - 0x0A + 0x41);
      char rightDigit = rightDigitValue < 0x0A ? (char)(0x30 + rightDigitValue) : (char)(rightDigitValue - 0x0A + 0x41);
      pw.print(leftDigit);
      pw.print(rightDigit);
      pw.print(" ");

      if ((i & 0x0F ) == 0x0F)
      {
        pw.println();
      }
    }
    if (((length - 1) & 0x0F) != 0x0F)
    {
      // Finish the line of data.
      pw.println();
    }
  }


  // Logs data from a byte array starting at offset for the length specified.
  // Output sixteen bytes per line, two hexidecimal digits per byte, one
  // space between bytes.
  private static void printByteArray(StringBuffer buf, byte[] data, int offset, int length)
  {
    for (int i = 0; i < length; i++, offset++)
    {
      int leftDigitValue = (data[offset] >>> 4) & 0x0F;
      int rightDigitValue = data[offset] & 0x0F;
      // 0x30 = '0', 0x41 = 'A'
      char leftDigit = leftDigitValue < 0x0A ? (char)(0x30 + leftDigitValue) : (char)(leftDigitValue - 0x0A + 0x41);
      char rightDigit = rightDigitValue < 0x0A ? (char)(0x30 + rightDigitValue) : (char)(rightDigitValue - 0x0A + 0x41);
      buf.append(leftDigit);
      buf.append(rightDigit);
      buf.append(" ");

      if ((i & 0x0F ) == 0x0F)
      {
        buf.append("\n");
      }
    }
    if (((length - 1) & 0x0F) != 0x0F)
    {
      // Finish the line of data.
      buf.append("\n");
    }
  }


  //@D2A
  /**
    Sets tracing for all categories on or off.  The actual tracing does
    not happen unless tracing is on.
    @param  traceAll  If true, tracing for each category is on;
                      otherwise, tracing for each category is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceAllOn(boolean traceAll)
  {
    traceConversion_ = traceAll;
    traceDatastream_ = traceAll;
    traceDiagnostic_ = traceAll;
    traceError_      = traceAll;
    traceInfo_       = traceAll;
    traceJDBC_       = traceAll;
    tracePCML_       = traceAll; // @D6C @D8C
    traceProxy_      = traceAll;
    traceThread_     = traceAll; //@D3A
    traceWarning_    = traceAll;
    if (traceAll) aTraceCategoryHasBeenActivated_ = true;
    if (findLogger()) logger_.setLevel();
  }


  /**
    Sets character set conversion tracing on or off.  The actual tracing
    does not happen unless tracing is on.
    @param  traceConversion  If true, conversion tracing is on;
                             otherwise, conversion tracing is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceConversionOn(boolean traceConversion)
  {
    if (traceConversion_ != traceConversion)
    {
      traceConversion_ = traceConversion;
      if (traceConversion) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }

  /**
    Sets data stream tracing on or off.  The actual tracing does not
    happen unless tracing is on.
    @param  traceDatastream  If true, data stream tracing is on;
                             otherwise, data stream tracing is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceDatastreamOn(boolean traceDatastream)
  {
    if (traceDatastream_ != traceDatastream)
    {
      traceDatastream_ = traceDatastream;
      if (traceDatastream) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }

  /**
    Sets diagnostic tracing on or off.  The actual tracing does not
    happen unless tracing is on.
    @param  traceDiagnostic  If true, diagnostic tracing is on;
                             otherwise, diagnostic tracing is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceDiagnosticOn(boolean traceDiagnostic)
  {
    if (traceDiagnostic_ != traceDiagnostic)
    {
      traceDiagnostic_ = traceDiagnostic;
      if (traceDiagnostic) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }

  /**
    Sets error tracing on or off.  The actual tracing does not happen
    unless tracing is on.
    @param  traceError  If true, error tracing is on; otherwise,
                        error tracing is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceErrorOn(boolean traceError)
  {
    if (traceError_ != traceError)
    {
      traceError_ = traceError;
      if (traceError) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }

  // Note: If this method is called with a non-null argument, we will disregard any existing Logger.
  /**
    Sets the trace file name.  If the file exists, output is appended to
    it.  If the file does not exist, it is created.
    @param  fileName  The log file name.  If this is null, output goes to System.out.
    @exception  IOException  If an error occurs while accessing the file.
   **/
  public static synchronized void setFileName(String fileName) throws IOException
  {
    // Flush the current destination stream.
    destination_.flush();

    if (fileName != null)
    {
      // Create a FileOutputStream and PrintWriter to handle the trace data.  If the file exists we want to append to it.
      File file = new File(fileName);
      FileOutputStream os = new FileOutputStream(fileName, file.exists());
      destination_ = new PrintWriter(os, true);
      userSpecifiedDestination_ = true;

      // com.ibm.as400.data.PcmlMessageLog.setLogStream(ps); // @D5A @D8D

      fileName_ = fileName;
    }
    else  // The specified fileName is null - Use default destination.
    {
      // destination_.close();   // avoid closing System.out

      // com.ibm.as400.data.PcmlMessageLog.setLogStream(ps); // @D5A @D8D

      fileName_ = null;
      destination_ = new PrintWriter(System.out, true);
      userSpecifiedDestination_ = false;
    }
  }


  /**
    Sets the trace file name for the specified component.
    If the file exists, output is appended to
    it.  If the file does not exist, it is created.
    @param  fileName  The log file name.  If this is null, output goes to System.out.
    @param  component Trace data for this component goes to file <code>fileName</code>.
    @exception  IOException  If an error occurs while accessing the file.
   **/
  public static synchronized void setFileName(Object component, String fileName)
  throws IOException
  {
    if (component == null)
      throw new NullPointerException("component");

    PrintWriter pw = (PrintWriter) printWriterHash.remove(component);
    if (pw != null)
      pw.flush();

    String oldName = (String) fileNameHash.remove(component);
    if (oldName != null)
    {
      // This writer was pointing to a file (rather than to System.out).
      pw.close();
    }

    if (fileName != null)
    {
      // Create a FileOutputStream and PrintWriter to handle the trace data.  If the file exists we want to append to it.
      File file = new File(fileName);
      FileOutputStream os = new FileOutputStream(fileName, file.exists());
      fileNameHash.put(component, fileName);
      pw = new PrintWriter(os, true);
      printWriterHash.put(component, pw); 
    }
    else
    {
      pw = new PrintWriter(System.out, true);
      printWriterHash.put(component, pw);
    }
  }



  // Note: If this method is called with a non-null argument, we will disregard any existing Logger.
  /**
    Sets the PrintWriter object.  All further trace output is sent to it.
    @param  obj  The PrintWriter object.  If this is null, output goes to System.out.
    @exception  IOException  If an error occurs while accessing the file.
   **/
  public static synchronized void setPrintWriter(PrintWriter obj) throws IOException
  {
    // Flush the current destination stream.
    destination_.flush();

    if (fileName_ != null)
    {
      // This writer was pointing to a file (rather than to System.out).
      destination_.close();
      fileName_ = null;
    }

    if (obj != null) {
      destination_ = obj;
      userSpecifiedDestination_ = true;
    }
    else {
      destination_ = new PrintWriter(System.out, true);
      userSpecifiedDestination_ = false;
    }
  }

  /**
    Sets the PrintWriter object for the specified component.
    All further trace output for this component is sent to the writer.
    @param  component Trace data for this component goes to writer <code>obj</code>.
    @param  obj  The PrintWriter object.  If this is null, output goes to System.out.
    @exception  IOException  If an error occurs while accessing the file.
   **/
  public static synchronized void setPrintWriter(Object component, PrintWriter obj)
  throws IOException
  {
    if (component == null)
      throw new NullPointerException("component");

    PrintWriter pw = (PrintWriter) printWriterHash.remove(component);
    if (pw != null)
      pw.flush();

    String fileName = (String) fileNameHash.remove(component);
    if (fileName != null)
    {
      // This writer was pointing to a file (rather than to System.out).
      pw.close();
    }

    if (obj != null)
      pw = obj;
    else
      pw = new PrintWriter(System.out, true);

    printWriterHash.put(component, pw);
  }


  /**
    Sets information tracing on or off.  The actual tracing does
    not happen unless tracing is on.
    @param  traceInformation  If true, information tracing is on;
            otherwise, information tracing is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceInformationOn(boolean traceInformation)
  {
    if (traceInfo_ != traceInformation)
    {
      traceInfo_ = traceInformation;
      if (traceInformation) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }


  /**
   *  Sets JDBC tracing on or off.  The actual tracing does not happen
   *  unless tracing is on. If there is already a log writer or log stream
   *  registered with the DriverManager, then that one is used independent
   *  of the stream being used by this Toolbox Trace class.
   *  Replacing the log writer or log stream
   *  in use by the DriverManager after calling this method does not turn
   *  off this driver's JDBC tracing; it merely changes the destination of the output.
   *  In this way, it is possible to have JDBC trace data directed to 3 separate
   *  logging facilities: The stream used by this Trace class, the stream used by
   *  DriverManager.setLogStream(), and the writer used by DriverManager.setLogWriter().
   *  @param traceJDBC true to turn on JDBC tracing; false to turn it off.
   *  @see java.sql.DriverManager
   **/
  public static void setTraceJDBCOn(boolean traceJDBC)           // @D5A
  {
    if (traceJDBC_ != traceJDBC)
    {
      traceJDBC_ = traceJDBC;
      if (traceJDBC) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }

  /**
   Obtains the (static) Toolbox logger from the JVM, if one exists.
   To activate a Toolbox logger, the application can simply call Logger.getLogger(Trace.LOGGER_NAME).
   Note: For performance, only the first invocation of this method will check for the existence of a Toolbox Logger in the JVM.
   **/
  private static final boolean findLogger()
  {
    if (firstCallToFindLogger_)  // Just do the following checking the first time.
    {
      firstCallToFindLogger_ = false;
      if ((logger_ == null) && JDK14_OR_HIGHER)
      {
        logger_ = ToolboxLogger.getLogger(); // returns null if no Logger activated
        if (logger_ != null && logger_.isLoggingOn())
        {
          logger_.info("Toolbox for Java - " + Copyright.version);
          if (mostRecentTracingChange_ != TURNED_TRACE_OFF) {
            traceOn_ = true;
          }
        }
      }
    }

    return (logger_ != null);
  }

  /**
    Sets tracing on or off.  When this is off, nothing is logged in any
    category, even those that are on.  When this is on, tracing occurs
    for all categories that are also on.
    @param  traceOn  If true, tracing is on; otherwise, all tracing is disabled.
   **/
  public static void setTraceOn(boolean traceOn)
  {
    if (traceOn_ != traceOn)
    {
      traceOn_ = traceOn;
      mostRecentTracingChange_ = (traceOn ? TURNED_TRACE_ON : TURNED_TRACE_OFF);
      findLogger();
      if (traceOn_ &&                                    //$D1A
          (logger_ == null || userSpecifiedDestination_))
        destination_.println("Toolbox for Java - " + Copyright.version);   // @A1C //@W1A //@D4C

      // If logger exists, set its attributes accordingly.
      if (logger_ != null && aTraceCategoryHasBeenActivated_) {
        logger_.setLevel();
        logger_.config("Toolbox for Java - " + Copyright.version);
      }
    }
    // Design issue: How does the Trace class reliably become aware that a Logger exists (and therefore that traceOn_ should be set to true), if the caller prefaces each Trace method call with "if (traceOn_)"?  (Most Toolbox classes check that condition before Trace.log() calls.)  We can't rely on everything to get set up correctly at static initialization time.
  }


  /**
   *  Sets PCML tracing on or off.  The actual tracing does not happend
   *  unless tracing is on.
   *  @param tracePCML If true, PCML tracing is on; otherwise,
   *                   PCML tracing is off.
   **/
  public static void setTracePCMLOn(boolean tracePCML)           // @D5A
  {
    if (tracePCML_ != tracePCML)
    {
      tracePCML_ = tracePCML;
      if (tracePCML) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();

    // try                                                                                                   // @D7A @D8D
    // {                                                                                                     // @D7A @D8D
    //     com.ibm.as400.data.PcmlMessageLog.setTraceEnabled(tracePCML);                                     //      @D8D
    // }                                                                                                     // @D7A @D8D
    // catch (NoClassDefFoundError e)                                                                        // @D7A @D8D
    // {                                                                                                     // @D7A @D8D
    //     destination_.println("Unable to enable PCML tracing:  NoClassDefFoundError - PcmlMessageLog");    // @D7A @D8D
    // }                                                                                                     // @D7A @D8D
    }
  }


  // @D0A
  /**
    Sets proxy stream tracing on or off.  The actual tracing does not
    happen unless tracing is on.
    @param  traceProxy  If true, proxy tracing is on;
                        otherwise, proxy tracing is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceProxyOn(boolean traceProxy)
  {
    if (traceProxy_ != traceProxy)
    {
      traceProxy_ = traceProxy;
      if (traceProxy) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }

  // @D3A
  /**
    Sets thread tracing on or off.  The actual tracing does not happen
    unless tracing is on.
    <br>Note: "thread" is not a separate trace category.  That is, simply calling <tt>setTraceThreadOn(true)</tt> by itself will not cause any trace messages to be generated.
    Rather, it will cause additional thread-related information to be included in trace messages generated for other trace categories, such as "diagnostic" and "information".
    @param  traceThread  If true, thread tracing is on;
                        otherwise, thread tracing is off.
    @see  Trace#setTraceOn
   **/
  public static void setTraceThreadOn(boolean traceThread)
  {
    if (traceThread_ != traceThread)
    {
      traceThread_ = traceThread;
      if (traceThread) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }


  /**
    Sets warning tracing on or off.  The actual tracing does not happen
    unless tracing is enabled.
    @param  traceWarning  If true, warning tracing is enabled;
                          otherwise, warning tracing is disabled.
    @see  Trace#setTraceOn
   **/
  public static void setTraceWarningOn(boolean traceWarning)
  {
    if (traceWarning_ != traceWarning)
    {
      traceWarning_ = traceWarning;
      if (traceWarning) aTraceCategoryHasBeenActivated_ = true;
      if (findLogger()) logger_.setLevel();
    }
  }

  // Indicates if this category is being traced or not.
  private static boolean traceCategory(int category)        // @D5C
  {
    boolean trace = false;
    switch (category)
    {
      case INFORMATION:
        trace = traceInfo_;
        break;
      case WARNING:
        trace = traceWarning_;
        break;
      case ERROR:
        trace = traceError_;
        break;
      case DIAGNOSTIC:
        trace = traceDiagnostic_;
        break;
      case DATASTREAM:
        trace = traceDatastream_;
        break;
      case CONVERSION:
        trace = traceConversion_;
        break;
      case PROXY:                   
        trace = traceProxy_;         
        break;                       
      case PCML:                                          // @D8A
        trace = tracePCML_;                             // @D8A
        break;                                          // @D8A
      case JDBC:
        trace = traceJDBC_;
        break;
      default:
        throw new ExtendedIllegalArgumentException("category ("
                                                   + Integer.toString(category)
                                                   + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    return trace;
  }
}
