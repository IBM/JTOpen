///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DataAreaListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The DataAreaListener interface provides
   an interface for receiving DataAreaEvents.
**/

public interface DataAreaListener extends java.util.EventListener
{
  /**
   Invoked when a clear has been performed.
   @param event The data area event.
   **/
  public void cleared(DataAreaEvent event);

  /**
   Invoked when a create has been performed.
   @param event The data area event.
   **/
  public void created(DataAreaEvent event);

  /**
   Invoked when a delete has been performed.
   @param event The data area event.
   **/
  public void deleted(DataAreaEvent event);

  /**
   Invoked when a read has been performed.
   @param event The data area event.
   **/
  public void read(DataAreaEvent event);

  /**
   Invoked when a write has been performed.
   @param event The data area event.
   **/
  public void written(DataAreaEvent event);
}
