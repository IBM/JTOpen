///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ErrorListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;
/**
    The ErrorListener interface provides a listener interface
    for receiving error events.
**/

public interface ErrorListener extends java.util.EventListener
{
  /**
   * Invoked when an error has occurred.
   * @param event The error event.
   **/
  public void errorOccurred(ErrorEvent event);
}



