///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDTrace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.PrintWriter;
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
class JDTrace
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




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
    static void logOpen (Object object)
    {
        StringBuffer buffer = new StringBuffer ();
        buffer.append ("as400: ");
        buffer.append (objectToString (object));
        buffer.append (" open.");

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
    private static String objectToString (Object object)
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

        return buffer.toString ();
    }



/**
Turns trace on, to System.out.
**/
    static void setTraceOn (boolean traceOn)
    {
        if (traceOn == true)   // @D1C
            DriverManager.setLogStream (System.out);
        else
            DriverManager.setLogStream (null);
        // When we move to JDK 1.2, we can change this to:
        // DriverManager.setLogWriter (new PrintWriter (System.out));
    }



}

