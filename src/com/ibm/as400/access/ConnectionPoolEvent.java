///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConnectionPoolEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The ConnectionPoolEvent class represents a connection pool event.
**/
public class ConnectionPoolEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /**
   Event ID indicating that a connection pool has been closed.
   **/
  public static final int CONNECTION_POOL_CLOSED = 0;

  /**
   Event ID indicating that a connection has been created.
   **/
  public static final int CONNECTION_CREATED = 1;

  /**
   Event ID indicating that a connection has been given to an application.
   **/
  public static final int CONNECTION_RELEASED = 2;

  /**
   Event ID indicating that a connection has been returned to the pool.
   **/
  public static final int CONNECTION_RETURNED = 3;

  /**
   Event ID indicating that a connection has been cleaned up by the 
   maintenance thread because one or more of its properties expired.
   **/
  public static final int CONNECTION_EXPIRED = 4;

  /**
   Event ID indicating that the maintenance thread is running.
   **/
  public static final int MAINTENANCE_THREAD_RUN = 5;


  private int eventID_;   // event identifier
   

 /**
   * Constructs a ConnectionPoolEvent object. It uses the specified source and
   * eventID.
   * @param source The object where the event originated.
   * @param eventID The event identifier.
   **/
  public ConnectionPoolEvent(Object source,
                             int eventID)
  {
	 super(source);
	 eventID_ = eventID;
  }

  
  /**
   * Returns the identifier for this event.
   * @return The identifier for this event.
   **/
  public int getID()
  {
    return eventID_;
  }
}
