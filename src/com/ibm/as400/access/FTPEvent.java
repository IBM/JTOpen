///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FTPEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The FTPEvent class represents an ftp event.
**/

public class FTPEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


  /**
   Event ID indicating that a connection to the server has been created.
   **/
  public  static final int FTP_CONNECTED = 0;
  private static final int FIRST_EVENT   = 0;

  /**
   Event ID indicating that the connection to the server has been disconnected.
   **/
  public static final int FTP_DISCONNECTED = 1;

  /**
   Event ID indicating that a file has been retrieved from the server.
   **/
  public static final int FTP_RETRIEVED = 2;

  /**
   Event ID indicating that a file has been put to the server.
   **/
  public static final int FTP_PUT = 3;

  /**
   Event ID indicating that a list of files on the server has been retrieved.
   **/
  public  static final int FTP_LISTED = 4;
  private static final int LAST_EVENT = 4;


  private int id_ = -1;


  /**
   Constructs an FTPEvent object.
   @param source The object where the event originated.
   @param id The event identifier.
   **/
  public FTPEvent(Object source,
                  int    id)
  {
     super(source);

     if (id < FIRST_EVENT || id > LAST_EVENT)
     {
        throw new IllegalArgumentException("identifier");
     }

     id_ = id;
  }









  /**
   * Returns the identifier for this event.
   * @return The identifier for this event.
   **/
  public int getID()
  {
     return id_;
  }

}

