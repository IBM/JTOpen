///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: FTPListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
/**
   The FTPListener interface provides
   an interface for receiving FTPEvents.
**/

public interface FTPListener extends java.util.EventListener
{
  /**
   * Invoked after a connection has been established.
   * @param event The ftp event.
   **/
  public void connected( FTPEvent event );

  /**
   * Invoked after the connection has been disconnected.
   * @param event The ftp event.
   **/
  public void disconnected( FTPEvent event );

  /**
   * Invoked after a file has been retrieved from the server.
   * @param event The ftp event.
   **/
  public void retrieved( FTPEvent event );

  /**
   * Invoked after a file has been put to the server.
   * @param event The ftp event.
   **/
  public void put( FTPEvent event );

  /**
   * Invoked after a list of files on the server has been retrieved.
   * @param event The ftp event.
   **/
  public void listed( FTPEvent event );

}

