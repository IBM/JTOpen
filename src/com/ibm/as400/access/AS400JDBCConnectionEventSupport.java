///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCConnectionEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import java.util.Vector;

/**
*  The AS400JDBCConnectionEventSupport class represents an event support 
*  facility for maintaining and notifying listeners of JDBC connection events.
**/
class AS400JDBCConnectionEventSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private Vector connectionListeners_;              // The listener list.
   
   /**
   *  Constructs a default AS400JDBCConnectionEventSupport object.
   **/
   public AS400JDBCConnectionEventSupport()
   {
      connectionListeners_ = new Vector();
   }


   /**
   *  Adds a AS400JDBCConnectionEventListener.
   *  @param listener The listener.
   **/
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      connectionListeners_.addElement(listener);
   }

   
   /**
   *  Fires the connection closed event.
   *  @param event The ConnectionEvent.
   **/
   public void fireCloseEvent(ConnectionEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionEventListener target = (ConnectionEventListener) targets.elementAt(i);
         target.connectionClosed(event);
      }
   }
   
   /**
   *  Fires the connection error event.
   *  @param event The ConnectionEvent.
   **/
   public void fireErrorEvent(ConnectionEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionEventListener target = (ConnectionEventListener) targets.elementAt(i);
         target.connectionErrorOccurred(event);
      }
   }

   /**
   *  Removes a ConnectionEventListener.
   *  @param listener The listener to be removed.
   **/
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      connectionListeners_.removeElement(listener);
   }

}
