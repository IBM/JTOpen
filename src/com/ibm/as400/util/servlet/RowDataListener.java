///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: RowDataListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

/**
  The RowDataListener interface provides a listener
  interface for receiving row data events.
**/
public interface RowDataListener extends java.util.EventListener
{
    
   /**
     Invoked when a row of data has been added.
     @param event The event. 
   **/
   public abstract void rowAdded(RowDataEvent event);
   
   /**
     Invoked when a row of data has been changed.
     @param event The event. 
   **/
   public abstract void rowChanged(RowDataEvent event);
   
   /**
     Invoked when a row of data has been removed.
     @param event The event. 
   **/
   public abstract void rowRemoved(RowDataEvent event);
}
