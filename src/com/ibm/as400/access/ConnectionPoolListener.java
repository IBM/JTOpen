///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConnectionPoolListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
/**
   The ConnectionPoolListener interface provides a listener
   interface for receiving Connection Pool events.
**/

public interface ConnectionPoolListener extends java.util.EventListener
{
  /**
   * Invoked when a connection has been created.
   * @param event The connection pool event.
   **/
  public void connectionCreated(ConnectionPoolEvent event);

  /**
   * Invoked when a connection is cleaned up by the maintenance thread
   * because one or more of its properties has expired.
   * @param event The connection pool event.
   **/
  public void connectionExpired(ConnectionPoolEvent event);
    
   /**
   * Invoked when a pool has been closed.
   * @param event The connection pool event.
   **/
  public void connectionPoolClosed(ConnectionPoolEvent event);

  /**
   * Invoked when a connection has been given out.
   * @param event The connection pool event.
   **/
  public void connectionReleased(ConnectionPoolEvent event);

  /**
   * Invoked when a connection has been returned.
   * @param event The connection pool event.
   **/
  public void connectionReturned(ConnectionPoolEvent event);

  /**
   * Invoked when the maintenance thread runs.
   * @param event The connection pool event.
   **/
  public void maintenanceThreadRun(ConnectionPoolEvent event);
}
