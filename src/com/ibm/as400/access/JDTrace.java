///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDTrace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.sql.DriverManager;



/**
<p>Manage tracing for the driver.  This class provides a
consistent way for classes to log trace messages.

<p>Note that tracing is turned on or off on a DriverManager
basis, not on a driver or connection basis.
**/
//
// Implementation notes:
//
// 1. The tracing strategy for the driver is that when
//    tracing is turned on at the DriverManager level,
//    the driver will put out various messages to the
//    specified PrintStream.
//
//    Within the driver, all tracing should only be done
//    after checking isTraceOn().  This will
//    help to relieve much in the way of String construction
//    and concatenation when tracing is turned off.
//
//    Note that not all requests and replies need to be
//    documented, since this information is available via
//    an AS/400 comm trace.
//
// 2. We used to trace every public API call, but it was
//    determined that a check for tracing on every API
//    call was slowing performance and not necessarily
//    helpful for debugging.
//
// @D0A: 3.  We now synchronize all calls to
//    DriverManager.println(), since we had some garbled
//    traces for multiple-threaded applications.  Now
//    there is yet another reason to make sure and call
//    isTraceOn() before any of the logXXX() methods,
//    since we have a synchronized block - and synchronized
//    blocks are slow.  We only want to incur that
//    performance penalty when tracing is on.
//
// @CRS: 4. To provide compatibility between JDK 1.1 and JDK 1.2+,
//    we now handle both DriverManager.getLogStream() and DriverManager.getLogWriter().
//    In JDK 1.4.1, if a log writer is set, getLogStream() returns null, but if a
//    log stream is set, getLogWriter() still returns a writer.  This is confusing.
//    We attempt to treat the log writer and the log stream separately, and we log
//    to both of them if they are available. This is to maintain backwards-compatibility
//    with apps that are still using the deprecated log stream methods.
//    Note that we also treat Toolbox JDBC tracing separately now.

final class JDTrace
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  // This code copied from JDUtilities so we don't reference that class, 
  // in case we are running with the proxy jar file.
  private static int JDBCLevel_ = 10;
  static
  {
     try 
     { 
        Class.forName("java.sql.Blob"); 
        JDBCLevel_ = 20;

        Class.forName("java.sql.Savepoint"); 
        JDBCLevel_ = 30;
     }                                         
     catch (Throwable e) { }   
  }                          


/**
Private constructor to prevent instantiation.  All methods in
this class are static.
**/
  private JDTrace()
  {
  }




/**
Indicates if tracing is turned on?

@return true or false
**/
  static boolean isTraceOn()
  {
    // If there is a log writer or log stream in the DriverManager, then
    // according to the DriverManager javadoc, all JDBC drivers should be
    // logging.  If there is not one set (e.g. set to null), all drivers
    // should not be logging. However, since we have our own Toolbox tracing, we are
    // nicer than that.  We let the user turn off Toolbox JDBC tracing without
    // turning off DriverManager tracing. And vice-versa: If Toolbox JDBC tracing is
    // turned on via the Trace class, we will log our JDBC traces there... we don't 
    // muck with the DriverManager logging until someone sets a log writer or log
    // stream into it.
    
    // Is Toolbox tracing on?
    if (Trace.traceOn_ && Trace.traceJDBC_) return true;

    // Is DriverManager tracing on?
    if (JDBCLevel_ > 10) // We don't reference JDUtilities here in case we are running proxified.
    {
      if (DriverManager.getLogWriter() != null) return true;
    }
    return (DriverManager.getLogStream() != null);
  }



// @j1a new method
/**
Logs information about tracing.

@param  object          The object
@param  information     The information.
**/
  static void logDataEvenIfTracingIsOff(Object object, String information)
  {
    // We will force tracing on for this information.  If we turned
    // tracing just to log this debug info we will turn it back off
    // at the end of the routine.

    //@CRS - Not sure why we have this method.  The whole point of turning trace
    // on or off is whether or not you want data logged.  I've changed this method
    // to just call logInformation().

    logInformation(object, information);
  }





// @J2 new method                                       
/**
Logs an information trace message.

@param  information     The information.
**/
  static void logInformation(String information)
  {
    if (isTraceOn())
    {
      String data = "as400: " + information;

      log(data);
    }
  }


// @J3 new method.
/**
Logs an information trace message.

@param  object          The object
@param  information     The information.
@param  exception       The exception.
**/
  static void logException(Object object, String information, Exception e)
  {
    if (isTraceOn())
    {
      StringWriter sw = new StringWriter();
      PrintWriter buffer = new PrintWriter(sw);
      buffer.write("as400: ");

      if (object != null)
      {
        buffer.write(objectToString(object));
      }
      else
      {
        buffer.write("static method");
      }

      buffer.write(": ");
      buffer.write(information);
      buffer.write(".");

      e.printStackTrace(buffer);

      log(sw.toString());
    }
  }

// @J4 new method.
/**
Logs an information trace message.

@param  object          The object throwing the exception
@param  object2         Additional object information (probably the connection object)
@param  information     The information.
@param  exception       The exception.
**/
  static void logException(Object object,                                          
                           Object object2,
                           String information, 
                           Exception e)
  {
    if (isTraceOn())
    {
      StringWriter sw = new StringWriter();
      PrintWriter buffer = new PrintWriter(sw);
      buffer.write("as400: ");

      if (object != null)
      {
        buffer.write(objectToString(object));
      }
      else
      {
        buffer.write("static method ");
      }

      if (object2 != null)
      {
        buffer.write(objectToString(object2));
      }

      buffer.write(": ");
      buffer.write(information);
      buffer.write(".");

      e.printStackTrace(buffer);

      log(sw.toString());
    }                                                             
  }




/**
Logs an information trace message.

@param  object          The object
@param  information     The information.
**/
  static void logInformation(Object object, String information)
  {
    if (isTraceOn())
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append("as400: ");
      buffer.append(objectToString(object));
      buffer.append(": ");
      buffer.append(information);
      buffer.append(".");

      log(buffer.toString());
    }
  }



/**
Logs a property trace message.

@param  object          The object
@param  propertyName    The property name.
@param  propertyValue   The property value.
**/
  static void logProperty(Object object, String propertyName, String propertyValue)
  {
    if (isTraceOn())
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append("as400: ");
      buffer.append(objectToString(object));
      buffer.append(": ");
      buffer.append(propertyName);
      buffer.append(" = \"");
      buffer.append(propertyValue);
      buffer.append("\".");

      log(buffer.toString());
    }
  }



/**
Logs a property trace message.

@param  object          The object
@param  propertyName    The property name.
@param  propertyValue   The property value.
**/
  static void logProperty(Object object, String propertyName, boolean propertyValue)
  {
    if (isTraceOn())
    {
      Boolean b = new Boolean(propertyValue);
      logProperty(object, propertyName, b.toString());
    }
  }



/**
Logs a property trace message.

@param  object          The object
@param  propertyName    The property name.
@param  propertyValue   The property value.
**/
  static void logProperty(Object object, String propertyName, int propertyValue)
  {
    if (isTraceOn())
    {
      logProperty(object, propertyName, Integer.toString(propertyValue));
    }
  }



/**
Logs an open trace message.

@param  object          The object
**/
  static void logOpen(Object object, Object parent)
  {
    if (isTraceOn())
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append("as400: ");
      buffer.append(objectToString(object));
      buffer.append(" open.");                    

      if (parent != null)                                        // @J3a
      {
        // @J3a
        buffer.append(" Parent: ");                             // @J3a
        buffer.append(objectToString(parent));                  // @J3a
        buffer.append(".");                                     // @J3a
      }                                                          // @J3a

      log(buffer.toString());
    }
  }


  // Logs the data to the DriverManager and/or Toolbox trace.
  private static void log(String data)
  {
    // No need to synchronize, as our operations are atomic... one call to each stream/writer.
    // The streams/writers themselves are synchronized internally.
    if (JDBCLevel_ > 10) // We don't reference JDUtilities here in case we are running proxified.
    {
      // Log to the DriverManager writer.
      PrintWriter pw = DriverManager.getLogWriter();
      if (pw != null)
      {
        pw.println(data);
      }
    }
    // Log to the DriverManager stream.
    PrintStream ps = DriverManager.getLogStream();
    if (ps != null)
    {
      ps.println(data);
    }
    // Log to Toolbox trace.
    Trace.log(Trace.JDBC, data);
  }


/**
Logs a close trace message.

@param  object          The object
**/
  static void logClose(Object object)
  {
    if (isTraceOn())
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append("as400: ");
      buffer.append(objectToString(object));
      buffer.append(" closed.");
      log(buffer.toString());
    }
  }



/**
Maps an object to a string.

@param  object      The object.
@return             The string.
**/
  static String objectToString(Object object)               // @J3c (no longer private)
  {
    // Determine the class name.
    String clazz = object.getClass().getName();     // @D3C
    String className;
    if (clazz.startsWith("com.ibm.as400.access.AS400JDBC")) // @D3A
      className = clazz.substring(30);                    // @D3A
    else if (clazz.startsWith("com.ibm.as400.access.JD"))   // @D3A
      className = clazz.substring(23);                    // @D3A
    else
      className = "Unknown";

    StringBuffer buffer = new StringBuffer();
    buffer.append(className);
    buffer.append(" ");
    buffer.append(object.toString());
    buffer.append(" (");                            // @J3a
    buffer.append(object.hashCode());               // @J3a 
    buffer.append(") ");                            // @J3a

    return buffer.toString();
  }



/**
Turns trace on, to System.out or what it was previously set to if it was turned off
by this method.  This method will not initialize trace again if trace is already set
on by another method.
**/
  static void setTraceOn(boolean traceOn)
  {
    Trace.setTraceJDBCOn(traceOn);
    if(traceOn)
        Trace.setTraceOn(traceOn);
  }
}

