///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ErrorEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.Trace;



/**
   The ErrorEvent class represents an error event.
   @deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
**/
public class ErrorEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  private Exception exception_;


  /**
   * Constructs an ErrorEvent object. 
   * @param source The object where the event originated.
   * @param exception The exception object that further describes the error.
   **/
  public ErrorEvent(Object source,
                    Exception exception)
  {
    super(source);
    exception_ = exception;

    Trace.log (Trace.DIAGNOSTIC, "Error event: (" + exception.getClass () + ") " + exception.getMessage ()); // @B0C
  }

  /**
   * Returns the exception which triggered this event.
   * @return The exception which triggered this event.
   **/
  public Exception getException()
  {
    return exception_;
  }
}
