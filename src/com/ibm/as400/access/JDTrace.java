///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDTrace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.io.PrintStream;  //@E1A



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
class JDTrace
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    private static PrintStream previousTraceInfo_ = null;   //@E1A



/**
Private constructor to prevent instantiation.  All methods in
this class are static.
**/
    private JDTrace () { }




/**
Indicates if tracing is turned on?

@return true or false
**/
    static boolean isTraceOn ()
    {
        return (DriverManager.getLogStream() != null);
        // When we move to JDK 1.2, we can use:
        // return (DriverManager.getLogWriter() != null);
    }



// @j1a new method
/**
Logs information about tracing.

@param  object          The object
@param  information     The information.
**/
    static void logDataEvenIfTracingIsOff(Object object,
                                          String information)
    {
       // We will force tracing on for this information.  If we turned
       // tracing just to log this debug info we will turn it back off
       // at the end of the routine
       boolean turnTraceOff = ! isTraceOn();
       setTraceOn(true);
       logInformation(object, information);
       if (turnTraceOff)
          setTraceOn(false);
    }




                                       
// @J2 new method                                       
/**
Logs an information trace message.

@param  information     The information.
**/
    static void logInformation (String information)
    {
        String data = "as400: " + information;

        synchronized (DriverManager.class) 
        {                            
            DriverManager.println (data);
        }                                                               
    }


// @J3 new method.
/**
Logs an information trace message.

@param  object          The object
@param  information     The information.
@param  exception       The exception.
**/
    static void logException (Object object,
                              String information, 
                              Exception e)
    {
        StringBuffer buffer = new StringBuffer ();
        buffer.append ("as400: ");
 
        if (object != null)
           buffer.append (objectToString (object));
        else
           buffer.append ("static method");
 
        buffer.append (": ");
        buffer.append (information);
        buffer.append (".");

        synchronized (DriverManager.class)
        {                        
            DriverManager.println (buffer.toString());   
            e.printStackTrace (DriverManager.getLogStream ());
        }                                                             
    }




/**
Logs an information trace message.

@param  object          The object
@param  information     The information.
**/
    static void logInformation (Object object,
                                String information)
    {
        StringBuffer buffer = new StringBuffer ();
        buffer.append ("as400: ");
        buffer.append (objectToString (object));
        buffer.append (": ");
        buffer.append (information);
        buffer.append (".");

        synchronized (DriverManager.class) {                            // @D0A
            DriverManager.println (buffer.toString());
        }                                                               // @D0A
    }



/**
Logs a property trace message.

@param  object          The object
@param  propertyName    The property name.
@param  propertyValue   The property value.
**/
    static void logProperty (Object object,
                             String propertyName,
                             String propertyValue)
    {
        StringBuffer buffer = new StringBuffer ();
        buffer.append ("as400: ");
        buffer.append (objectToString (object));
        buffer.append (": ");
        buffer.append (propertyName);
        buffer.append (" = \"");
        buffer.append (propertyValue);
        buffer.append ("\".");

        synchronized (DriverManager.class) {                            // @D0A
            DriverManager.println (buffer.toString());
        }                                                               // @D0A
    }



/**
Logs a property trace message.

@param  object          The object
@param  propertyName    The property name.
@param  propertyValue   The property value.
**/
    static void logProperty (Object object,
                             String propertyName,
                             boolean propertyValue)
    {
        Boolean b = new Boolean (propertyValue);
        logProperty (object, propertyName, b.toString ());
    }



/**
Logs a property trace message.

@param  object          The object
@param  propertyName    The property name.
@param  propertyValue   The property value.
**/
    static void logProperty (Object object,
                             String propertyName,
                             int propertyValue)
    {
        logProperty (object, propertyName, Integer.toString (propertyValue));
    }



/**
Logs an open trace message.

@param  object          The object
**/
    static void logOpen (Object object, Object parent)
    {
        StringBuffer buffer = new StringBuffer ();
        buffer.append ("as400: ");
        buffer.append (objectToString (object));
        buffer.append (" open.");                    
        
        if (parent != null)                                        // @J3a
        {                                                          // @J3a
           buffer.append(" Parent: ");                             // @J3a
           buffer.append(objectToString(parent));                  // @J3a
           buffer.append(".");                                     // @J3a
        }                                                          // @J3a

        synchronized (DriverManager.class) {                            // @D0A
            DriverManager.println (buffer.toString());
        }                                                               // @D0A
    }



/**
Logs a close trace message.

@param  object          The object
**/
    static void logClose (Object object)
    {
        StringBuffer buffer = new StringBuffer ();
        buffer.append ("as400: ");
        buffer.append (objectToString (object));
        buffer.append (" closed.");

        synchronized (DriverManager.class) {                            // @D0A
            DriverManager.println (buffer.toString());
        }                                                               // @D0A
    }



/**
Maps an object to a string.

@param  object      The object.
@return             The string.
**/
    static String objectToString (Object object)               // @J3c (no longer private)
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

        StringBuffer buffer = new StringBuffer ();
        buffer.append (className);
        buffer.append (" ");
        buffer.append (object.toString ());
        buffer.append (" (");                            // @J3a
        buffer.append (object.hashCode());               // @J3a 
        buffer.append (") ");                            // @J3a

        return buffer.toString ();
    }



/**
Turns trace on, to System.out or what it was previously set to if it was turned off
by this method.  This method will not initialize trace again if trace is already set
on by another method.
**/
    static void setTraceOn (boolean traceOn)
    {
     if (traceOn)                               //@E1A
     {                                                       //@E1A
         if (previousTraceInfo_ == null)                     //@E1A
         {                                                   //@E1A
          if (DriverManager.getLogStream() == null)       //@E1A
          {                                               //@E1A
              DriverManager.setLogStream (System.out);    //@E1A
          }                                               //@E1A
         }                                                   //@E1A
         else if (DriverManager.getLogStream() == null)      //@E1A
         {                                                   //@E1A
          DriverManager.setLogStream(previousTraceInfo_); //@E1A
         }                                                   //@E1A
     }                                                       //@E1A
     else                                                    //@E1A
     {                                                       //@E1A
         previousTraceInfo_ = DriverManager.getLogStream();  //@E1A
         if (previousTraceInfo_ != null)               //@E2A
            previousTraceInfo_.flush();                   //@E1A
         DriverManager.setLogStream(null);                   //@E1A
     }                                                       //@E1A
        //@E1D if (traceOn == true)   // @D1C
        //@E1D    DriverManager.setLogStream (System.out);
        //@E1D else
        //@E1D    DriverManager.setLogStream (null);
        // When we move to JDK 1.2, we can change this to:
        // DriverManager.setLogWriter (new PrintWriter (System.out));
    }



}

