///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: EventLog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.PrintWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.lang.StringBuffer;


/**
  *  The EventLog class is an implementation of
  *  <a href="Log.html">Log</a> which logs
  *  exceptions and messages to the specified file or PrintStream.
  *  <p>
  *
  *  Here is an example use of EventLog, which will
  *  write a timestamp and message to the specified file:
  *  <p>
  *  <BLOCKQUOTE><PRE>
  *  EventLog myLog = new EventLog("myFile");
  *  myLog.log("You have successfully written to my log file.");
  *  myLog.log("Another log message.");
  *  </BLOCKQUOTE></PRE>
  *  <p>
  *  Each successive message will be appended to the previous
  *  messages in the log.
  *  <p>
  *  Here is what the log file will look like:
  *  <p>
  *  Mon Jan 03 09:00:00 CST 2000 Yout have successfully written to my log file.<br>
  *  Mon Jan 03 09:00:01 CST 2000 Another log message.
  *
  **/

public class EventLog implements Log
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   private PrintWriter writer_;

   /**
    *  Constructs a default EventLog object.  Using this constructor,
    *  all log output will be sent to standard out.
    *
    **/
   public EventLog()
   {
      writer_ = new PrintWriter(System.out, true);
   }


   /**
    *  Constructs an EventLog object with the specified file <i>pathname</i>.
    *  If a log file already exists with the specified <i>pathname</i>, all
    *  messages will be appended to the existing log file.
    *
    *  @param pathname The file pathname.
    *
    *  @exception  IOException  If an error occurs while accessing the file.
    **/
   public EventLog(String pathname) throws IOException
   {
      if (pathname == null)
         throw new NullPointerException("pathname");

      // Create a FileOutputStream and PrintWriter to handle the logging.
      // If the file exists we want to append to it.
      File file = new File(pathname);
      FileOutputStream os = new FileOutputStream(pathname, file.exists());
      writer_ = new PrintWriter(os, true);
   }

   /**
    *  Constructs an EventLog object with the specified OutputStream.
    *
    *  @param stream The log OutputStream.
    *
    *  @exception  IOException  If an error occurs while accessing the stream.
    **/
   public EventLog(OutputStream stream) throws IOException
   {
      if (stream == null)
         throw new NullPointerException("stream");

      // Create a PrintWriter to handle the logging.
      writer_ = new PrintWriter(stream, true);
   }

   /**
    *  Constructs an EventLog object with the specified <i>PrintWriter</i>.
    *
    *  @param out The PrintWriter.
    **/
   public EventLog(PrintWriter out)
     {
      if (out == null)
         throw new NullPointerException("out");

          writer_ = out;
     }


     /**
    * Logs a message to the event log.
    * The log will contain a timestamp
    * then the message.
    *
    * @param   msg  The message to log.
    **/
   public void log(String msg)
     {
          log(msg, null);
     }


     /**
    * Logs an exception and message to the event log.
    * The log will contain a timestamp, the message,
    * and then the exception stack trace.
      *
      * @param   msg  The message to log.
      * @param   exception  The exception to log.
      */
   public void log(String msg, Throwable exception)
     {
      StringBuffer buffer = new StringBuffer(new Date().toString());
      buffer.append(" ");
      buffer.append(msg);

      writer_.println(buffer);

      if (exception != null)
         exception.printStackTrace(writer_);

      // The writer is created with 'true' being passed in so
      // the writer automatically fushes the buffer.  But there was a
      // case where the JVM garbage collected before the buffer was
      // flushed, so I added a flush here to make sure the buffer is flushed
      // right after the message is logged.
      writer_.flush();
   }

}
