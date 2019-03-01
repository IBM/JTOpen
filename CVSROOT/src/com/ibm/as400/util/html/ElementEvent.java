///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ElementEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

import com.ibm.as400.access.ExtendedIllegalArgumentException;

/**
   The ElementEvent class represents an element event.
**/

public class ElementEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  
   
  /**
   Event ID indicating that an element has been added.
   **/
  public static final int ELEMENT_ADDED = 0;

  /**
   Event ID indicating that an element has been changed.
   **/
  public static final int ELEMENT_CHANGED = 1;

  /**
   Event ID indicating that an element has been removed.
   **/
  public static final int ELEMENT_REMOVED = 2;

  private int id_; // event identifier

  /**
   Constructs an ElementEvent object. It uses the specified source and ID.
   @param source The object where the event originated.
   @param id The event identifier.
   **/
  public ElementEvent(Object source, int id)
  {
    super(source);

    if (id < ELEMENT_ADDED || id > ELEMENT_REMOVED)
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
