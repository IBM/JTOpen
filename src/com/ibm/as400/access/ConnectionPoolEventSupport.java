///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConnectionPoolEventSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;

/**
*  The ConnectionPoolEventSupport class represents an event support 
*  facility for maintaining and notifying listeners of connection pool events.
**/
class ConnectionPoolEventSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private Vector connectionListeners_;              // The listener list.
   
   /**
   *  Constructs a default ConnectionPoolEventSupport object.
   **/
   public ConnectionPoolEventSupport()
   {
      connectionListeners_ = new Vector();
   }


   /**
   *  Adds a ConnectionPoolListener to the internal list.
   *  @param listener The listener.
   **/
   public void addConnectionPoolListener(ConnectionPoolListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      connectionListeners_.addElement(listener);
   }

   /**
   *  Fires the connection pool closed event.
   *  @param event The ConnectionPoolEvent.
   **/
   public void fireClosedEvent(ConnectionPoolEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionPoolListener target = (ConnectionPoolListener) targets.elementAt(i);
         target.connectionPoolClosed(event);
      }
   }
   
   /**
   *  Fires the connection created event.
   *  @param event The ConnectionPoolEvent.
   **/
   public void fireConnectionCreatedEvent(ConnectionPoolEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionPoolListener target = (ConnectionPoolListener) targets.elementAt(i);
         target.connectionCreated(event);
      }
   }


   /**
   *  Fires the connection expired event.
   *  @param event The ConnectionPoolEvent.
   **/
   public void fireConnectionExpiredEvent(ConnectionPoolEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionPoolListener target = (ConnectionPoolListener) targets.elementAt(i);
         target.connectionExpired(event);
      }
   }
   

   /**
   *  Fires the maintenance thread run event.
   *  @param event The ConnectionPoolEvent.
   **/
   public void fireMaintenanceThreadRun(ConnectionPoolEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionPoolListener target = (ConnectionPoolListener) targets.elementAt(i);
         target.maintenanceThreadRun(event);
      }
   }

   /**
   *  Fires the connection released event.
   *  @param event The ConnectionPoolEvent.
   **/
   public void fireConnectionReleasedEvent(ConnectionPoolEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionPoolListener target = (ConnectionPoolListener) targets.elementAt(i);
         target.connectionReleased(event);
      }
   }


    /**
   *  Fires the connection returned event.
   *  @param event The ConnectionPoolEvent.
   **/
   public void fireConnectionReturnedEvent(ConnectionPoolEvent event)
   {
      Vector targets = (Vector) connectionListeners_.clone();
      for (int i=0; i< targets.size(); i++) 
      {
         ConnectionPoolListener target = (ConnectionPoolListener) targets.elementAt(i);
         target.connectionReturned(event);
      }
   }


   /**
   *  Removes a ConnectionPoolListener from the internal list.
   *  @param listener The listener to be removed.
   **/
   public void removeConnectionPoolListener(ConnectionPoolListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      connectionListeners_.removeElement(listener);
   }

}
