///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserSpaceEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The UserSpaceEvent class represents a user space event.
**/

public class UserSpaceEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /**
   Event ID indicating that a user space has been created.
   **/
  public static final int US_CREATED = 0;

  /**
   Event ID indicating that a user space has been deleted.
   **/
  public static final int US_DELETED = 1;

  /**
   Event ID indicating that a user space has been read.
   **/
  public static final int US_READ = 2;

  /**
   Event ID indicating that a user space has been written.
   **/
  public static final int US_WRITTEN = 3;


  private int id_; // event identifier


  /**
   Constructs a UserSpaceEvent object. It uses the specified source and ID.
   @param source The object where the event originated.
   @param id The event identifier.
   **/
  public UserSpaceEvent(Object source,
                   int    id)
  {
    super(source);

    if (id < US_CREATED || id > US_WRITTEN)
    {
      throw new ExtendedIllegalArgumentException("id", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    id_ = id;
  }

  
  /**
   Returns the identifier for this event.
   @return The identifier for this event.
   **/
  public int getID()
  {
    return id_;
  }
}
