///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ServletEventLog.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import java.util.*;
import javax.servlet.*;
import com.ibm.as400.access.Log;                                  //$A1C

/** 
  *  The ServletEventLog class represents an Log object.          
  **/

public class ServletEventLog implements Log	                     //$A1C
{
   private ServletContext context_;
	
	
   /**
    *  Constructs an ServletEventLog object with the specified servlet configuration.
    *
    *  @param config The servlet configuration.
    **/
   public ServletEventLog(ServletConfig config)
	{
		context_ = config.getServletContext();
	}
	
   
	/**
    * Logs a message to the event log.
    *
    * @param   msg  The message to log.
    **/
   public void log(String msg)
	{
		context_.log(logMessage(msg));		
	}

	
	/**
	 * Logs an exception and message to the event log.
	 *
	 * @param   msg  The message to log.
	 * @param   exception  The exception to log.
	 */
   public void log(String msg, Throwable exception)
	{
      if (exception instanceof Exception)
         context_.log((Exception)exception, logMessage(msg));
      else
         context_.log(logMessage(msg), exception);
	}

   
   /**
    *  Return a message with thread and date information.
    *
    *  @param msg The Message.
    **/
   private String logMessage(String msg)
	{
		String id = Thread.currentThread().getName();
		
		return " " + new Date() + " " + id + " " + msg;				
	}
}
