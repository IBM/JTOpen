///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCRowSetEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import java.util.Vector;

/**
*  The AS400JDBCRowSetEventSupport class represents an event support 
*  facility for maintaining and notifying listeners of JDBC rowset events.
**/
class AS400JDBCRowSetEventSupport
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

   private Vector rowsetListeners_;              // The listener list.
   
   /**
   *  Constructs a default AS400JDBCRowSetEventSupport object.
   **/
   public AS400JDBCRowSetEventSupport()
   {
      rowsetListeners_ = new Vector();
   }

   /**
   *  Adds a RowSetListener.
   *  @param listener The listener.
   **/
   public void addRowSetListener(RowSetListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      rowsetListeners_.addElement(listener);
   }
   
   /**
   *  Fires the cursor moved event.
   *  @param event The RowSetEvent.
   **/
   public void fireCursorMoved(RowSetEvent event)
   {
      Vector targets = (Vector) rowsetListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         RowSetListener target = (RowSetListener) targets.elementAt(i);
         target.cursorMoved(event);
      }
   }
   
   /**
   *  Fires the row changed event.
   *  @param event The RowSetEvent.
   **/
   public void fireRowChanged(RowSetEvent event)
   {
      Vector targets = (Vector) rowsetListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         RowSetListener target = (RowSetListener) targets.elementAt(i);
         target.rowChanged(event);
      }
   }

   /**
   *  Fires the rowSetChanged event.
   *  @param event The RowSetEvent.
   **/
   public void fireRowSetChanged(RowSetEvent event)
   {
      Vector targets = (Vector) rowsetListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         RowSetListener target = (RowSetListener) targets.elementAt(i);
         target.rowSetChanged(event);
      }
   }

   /**
   *  Removes a RowSetListener.
   *  @param listener The listener to be removed.
   **/
   public void removeRowSetListener(RowSetListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      rowsetListeners_.removeElement(listener);
   }
}
