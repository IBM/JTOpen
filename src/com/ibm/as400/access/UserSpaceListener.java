///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserSpaceListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;
/**
   The UserSpaceListener interface provides
   an interface for receiving UserSpaceEvents.
**/

public interface UserSpaceListener extends java.util.EventListener
{
  /**
   Invoked when a create has been performed.
   @param event The user space event.
   **/
  public void created( UserSpaceEvent event );

  /**
   Invoked when a delete has been performed.
   @param event The user space event.
   **/
  public void deleted( UserSpaceEvent event );

  /**
   Invoked when a read has been performed.
   @param event The user space event.
   **/
  public void read( UserSpaceEvent event );

  /**
   Invoked when a write has been performed.
   @param event The user space event.
   **/
  public void written( UserSpaceEvent event );
}
