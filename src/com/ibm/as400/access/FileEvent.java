///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: FileEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
   The FileEvent class represents a File event.
**/

public class FileEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  /**
   The file closed event ID.  This event is delivered when a file is closed.
   **/
  public static final int FILE_CLOSED = 0;

  /**
   The file deleted event ID.  This event is delivered when a file is deleted.
   **/
  public static final int FILE_DELETED = 1;

  /**
   The file modified event ID.  This event is delivered when a file is modified.
   **/
  public static final int FILE_MODIFIED = 2;

  /**
   The file opened event ID.  This event is delivered when a file is opened.
   **/
  public static final int FILE_OPENED = 3;

  /**
   The file created event ID.  This event is delivered when a file is created.
   **/
  public static final int FILE_CREATED = 4;


  private int id_; // event identifier


  /**
   Constructs a FileEvent object. It uses the specified source and ID.
   @param source The object where the event originated.
   @param id The event identifier.
   **/
  public FileEvent(Object source,
                   int    id)
  {
    super(source);

    if (id < FILE_CLOSED || id > FILE_CREATED)
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




