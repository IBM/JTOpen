///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DataAreaEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The DataAreaEvent class represents a data area event.
**/

public class DataAreaEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;

  /**
   Event ID indicating that a data area has been cleared.
   **/
  public static final int DA_CLEARED = 0;

  /**
   Event ID indicating that a data area has been created.
   **/
  public static final int DA_CREATED = 1;

  /**
   Event ID indicating that a data area has been deleted.
   **/
  public static final int DA_DELETED = 2;

  /**
   Event ID indicating that a data area has been read.
   **/
  public static final int DA_READ = 3;

  /**
   Event ID indicating that a data area has been written.
   **/
  public static final int DA_WRITTEN = 4;


  private int id_; // event identifier


  /**
  Constructs a DataAreaEvent object.
    @param source The object where the event originated.
    @param id The event identifier.
  **/
  public DataAreaEvent(Object source, int id)
  {
    super(source);

    if (id < DA_CLEARED || id > DA_WRITTEN)
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
