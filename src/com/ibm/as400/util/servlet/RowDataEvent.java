///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RowDataEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import com.ibm.as400.access.Copyright;
/**
*  The RowDataEvent class represents a row data event.
**/
public class RowDataEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private String s = Copyright.copyright;
   
   /**
   *  Event ID indicating that a row has been added.
   **/
   public static final int ROW_ADDED = 0;

   /**
   *  Event ID indicating that a row has been changed.
   **/
   public static final int ROW_CHANGED = 1;

   /**
   *  Event ID indicating that a row has been removed.
   **/
   public static final int ROW_REMOVED = 2;

   private int id_; // event identifier


   /**
   *  Constructs a RowDataEvent object. It uses the specified source and ID.
   *  @param source The object where the event originated.
   *  @param id The event identifier.
   **/
   public RowDataEvent(Object source, int id)
   {
     super(source);

     if (id < ROW_ADDED || id > ROW_REMOVED)
     {
       throw new IndexOutOfBoundsException("id");
     }

     id_ = id;
   }

   /**
   *  Returns the identifier for this event.
   *  @return The identifier for this event.
   **/
   public int getID()
   {
     return id_;
   }
}
