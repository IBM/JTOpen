///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ToolboxLogger.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2005-2005 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.logging.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 This class is used to wrapper the (static) Toolbox Logger (java.util.logging.Logger), if one has been activated.  We need to do this indirection because the java.util.logging package didn't exist prior to JDK 1.4.
**/
class ToolboxLogger
{
  private static final String copyright = "Copyright (C) 2005-2005 International Business Machines Corporation and others.";

  private static Logger logger_ = null;
  private static Logger[] parentLoggers_ = null;
  private final static Object loggerLock_ = new Object();

  private static boolean JDK14_OR_HIGHER;
  static
  {
    try {
      Class.forName("java.util.logging.LogManager"); // Class added in JDK 1.4.
      JDK14_OR_HIGHER = true;  // If we got this far, we're on JDK 1.4 or higher.
    }
    catch (Throwable e) {      // We're not on JDK 1.4 or higher,
      JDK14_OR_HIGHER = false; // so don't even try to get the Toolbox Logger.
    }
  }

  // Private constructor to prevent instantiation by other classes.
  private ToolboxLogger() {}

  final void config(String msg)
  {
    logger_.config(msg);
  }


  /**
   Obtains the (static) Toolbox Logger from the JVM, if one has been activated, and wrappers it in a ToolboxLogger instance.
   Returns null if no Toolbox Logger has been activated.
   To set up a Toolbox logger, the calling application can call Logger.getLogger(Trace.LOGGER_NAME).
   **/
  static final ToolboxLogger getLogger()
  {
    if (logger_ == null && JDK14_OR_HIGHER)
    {
      synchronized (loggerLock_)
      {
        if (logger_ == null)
        {
          // Determine if a Toolbox Logger has been registered.

          // Note: The javadoc for LogManager.getLogger() says that it "Returns matching logger or null if none is found".  That appears to be the behavior in JDK 1.4.  However, in JDK 1.5 (J2SE 5.0) this method apparently returns a non-null value in all cases.
          // So a safer technique is to call LogManager.getLoggerNames(), and see if the Toolbox Logger is listed.


          // Get list of all registered Loggers.
          Enumeration loggerNames = LogManager.getLogManager().getLoggerNames();

          // See if the list contains the Toolbox Logger.
          while (loggerNames.hasMoreElements() && logger_ == null) {
            String name = (String)loggerNames.nextElement();
            if (name.equals(Trace.LOGGER_NAME)) { // Found the Toolbox Logger.
              logger_ = LogManager.getLogManager().getLogger(Trace.LOGGER_NAME);
            }
          }

          // Build the list of parent loggers.
          if (logger_ != null)
          {
            Vector parents = new Vector();
            Logger parent = logger_.getParent();
            while (parent != null) {
              parents.add(parent);
              parent = parent.getParent();
            }

            if (!parents.isEmpty()) {
              parentLoggers_ = (Logger[])parents.toArray(new Logger[0]);
            }
          }
        }
      }
    }

    if (logger_ != null) return new ToolboxLogger();
    else return null;
  }

  final void info(String msg)
  {
    logger_.info(msg);
  }

  final boolean isLoggable(int category)
  {
    return logger_.isLoggable(mapTracingLevel(category));
  }


  final boolean isLoggingOn()
  {
    if      (logger_ == null)                 return false;
    else if (logger_.getLevel() == Level.OFF) return false;
    else if (logger_.getLevel() != null)      return true;
    else // Level is null, indicating that the level is inherited from parent.
    {
      if (parentLoggers_ != null) // are there any parent Loggers
      {
        // Work upwards through the parents until we find a non-null Level.
        for (int i=0; i<parentLoggers_.length; i++) {
          if (parentLoggers_[i].getLevel() == null) continue; // keep looking upwards
          else return (parentLoggers_[i].getLevel() != Level.OFF);
        }
      }
      // If we got this far, then either there are no parents, or all parents have null-valued Level.
      return false;
    }
  }


  final void log(int category, String msg)
  {
    logger_.log(mapTracingLevel(category), msg);
  }

  final void log(int category,
                        String msg,
                        Throwable thrown)
  {
    logger_.log(mapTracingLevel(category), msg, thrown);
  }

  /**
   Determines all currently-effective tracing categories, and maps to the corresponding (lowest) logging Level.
   **/
  private static final Level mapTracingLevel()
  {
    // Note: Trace.traceThread_ is merely a modifier for other trace categories.
    // Therefore we can ignore it in the context of this method.
    Level level = null;
    if (Trace.traceOn_)
    {
      if (Trace.traceError_) level = Level.SEVERE;
      if (Trace.traceWarning_) level = Level.WARNING;
      if (Trace.traceInfo_) level = Level.INFO;
      if (Trace.traceDiagnostic_) level = Level.FINE;
      if (Trace.traceJDBC_ || Trace.tracePCML_ || Trace.traceProxy_) level = Level.FINER;
      if (Trace.traceConversion_ || Trace.traceDatastream_) level = Level.FINEST;
      if (level == null) level = Level.OFF;
    }
    else level = Level.OFF;

    return level;
  }


  /**
   Returns the logging Level that corresponds to the specified Toolbox tracing category.
   **/
  private static final Level mapTracingLevel(int category)
  {
    // Note: Even though the Trace class has a setTraceThreadOn() method, there is no THREAD category.  Turning on "thread" tracing merely causes other trace categories to include thread-related data in their messages.
    switch (category)
    {
      case Trace.INFORMATION: return Level.INFO;
      case Trace.WARNING: return Level.WARNING;
      case Trace.ERROR: return Level.SEVERE;
      case Trace.DIAGNOSTIC: return Level.FINE;
      case Trace.JDBC: case Trace.PCML: case Trace.PROXY: return Level.FINER;
      case Trace.CONVERSION: case Trace.DATASTREAM: return Level.FINEST;
      default:
        throw new ExtendedIllegalArgumentException("category ("
                                                   + Integer.toString(category)
                                                   + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }

  final void setLevel() throws SecurityException
  {
    logger_.setLevel(mapTracingLevel());
  }

}
