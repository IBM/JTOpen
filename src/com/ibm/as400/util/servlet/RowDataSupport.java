///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RowDataSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import java.util.Vector;

class RowDataSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   transient private Vector rowListeners_;      // The list of row listeners.
   transient private Object source_;

   public RowDataSupport(Object source)
   {
      rowListeners_ = new Vector();
      source_ = source;
   }
   
   /**
   *  Adds an RowDataListener.
   *  The RowDataListener object is added to an internal list of RowDataListeners;
   *  it can be removed with removeRowDataListener.
   *
   *  @param listener The RowDataListener.
   **/
   public void addRowDataListener(RowDataListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      
      rowListeners_.addElement(listener);
   }

   /**
   *  Fire an added event.
   **/
   public void fireAdded()
   {
      Vector targets;
      targets = (Vector) rowListeners_.clone();
      RowDataEvent event = new RowDataEvent(source_, RowDataEvent.ROW_ADDED);
      for (int i = 0; i < targets.size(); i++) {
          RowDataListener target = (RowDataListener)targets.elementAt(i);
          target.rowAdded(event);
      }
   }

   /**
   *  Fire a changed event.
   **/
   public void fireChanged()
   {
      Vector targets;
      targets = (Vector) rowListeners_.clone();
      RowDataEvent event = new RowDataEvent(source_, RowDataEvent.ROW_CHANGED);
      for (int i = 0; i < targets.size(); i++) {
          RowDataListener target = (RowDataListener)targets.elementAt(i);
          target.rowChanged(event);
      }
   }

   /**
   *  Fire a removed event.
   **/
   public void fireRemoved()
   {
      Vector targets;
      targets = (Vector) rowListeners_.clone();
      RowDataEvent event = new RowDataEvent(source_, RowDataEvent.ROW_REMOVED);
      for (int i = 0; i < targets.size(); i++) {
          RowDataListener target = (RowDataListener)targets.elementAt(i);
          target.rowRemoved(event);
      }
   }

   /**
   *  Removes this RowDataListener from the internal list.
   *  If the RowDataListener is not on the list, nothing is done.
   *  @param listener The RowDataListener.
   **/
   public void removeRowDataListener(RowDataListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      
      rowListeners_.removeElement(listener);
   }
}
