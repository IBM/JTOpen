///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Log.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  *  The Log interface defines a mechanism for exception and message logging
  *  in a device-independent  manner.
  *  <p>
  *  Implementations of this interface can direct the logged
  *  information to the appropriate file or output device.  Event logging
  *  is intended for end-user information.  In contrast,
  *  {@link com.ibm.as400.access.Trace Trace} is intended for
  *  debugging information for developers.
  *
  **/

public interface Log
{
   /**
    * Logs a message.
    *
    * @param   msg  The message to log.
    **/
   public void log(String msg);


     /**
      * Logs an exception and message.
      *
      * @param   msg  The message to log.
      * @param   exception  The exception to log.
      */
   public void log(String msg, Throwable exception);
}

