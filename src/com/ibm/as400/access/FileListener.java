///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: FileListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
/**
    The FileListener interface provides an interface for receiving
    File events.
**/

public interface FileListener extends java.util.EventListener
{
  /**
   Invoked when a file has been closed.
   @param event The file event.
   **/
  public void fileClosed(FileEvent event);

  /**
   Invoked when a file has been created.
   @param event The file event.
   **/
  public void fileCreated(FileEvent event);

  /**
   Invoked when a file is deleted.
   @param event The file event.
   **/
  public void fileDeleted(FileEvent event);

  /**
   Invoked when a file has been modified.
   @param event The file event.
   **/
  public void fileModified(FileEvent event);

  /**
   Invoked when a file has been opened.
   @param event The file event.
   **/
  public void fileOpened(FileEvent event);
}




