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

/**
 This class is used to wrapper the (static) Toolbox Logger (java.util.logging.Logger), if one has been activated.  We need to do this indirection because the java.util.logging package didn't exist prior to JDK 1.4.
**/
class ToolboxLogger
{
  private static final String copyright = "Copyright (C) 2005-2005 International Business Machines Corporation and others.";

  private static Logger logger_ = null;
  private static boolean JDK14_OR_LATER = false;
  static
  {
    try {
      Class.forName("java.util.logging.LogManager");
      logger_ = LogManager.getLogManager().getLogger(Trace.LOGGER_NAME);  // Returns null unless the calling application has activated one, via Logger.getLogger(LOGGER_NAME).
      JDK14_OR_LATER = true;  // if we got this far, we're on JDK 1.4 or higher
    }
    catch (Throwable e) {}  // package java.util.logging was added in JDK 1.4
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
    if (logger_ == null && JDK14_OR_LATER) {
      logger_ = LogManager.getLogManager().getLogger(Trace.LOGGER_NAME);
      // Returns null unless the calling application has activated a Toolbox Logger.
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

  final boolean isLoggingOff()
  {
    return (logger_.getLevel() == Level.OFF);
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
    // Therefore we can ignore it.
    Level level = Level.OFF;
    if (Trace.traceOn_)
    {
      if (Trace.traceError_) level = Level.SEVERE;
      if (Trace.traceWarning_) level = Level.WARNING;
      if (Trace.traceInfo_) level = Level.INFO;
      if (Trace.traceDiagnostic_) level = Level.FINE;
      if (Trace.traceJDBC_ || Trace.tracePCML_ || Trace.traceProxy_) level = Level.FINER;
      if (Trace.traceConversion_ || Trace.traceDatastream_) level = Level.FINEST;
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
