///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ElementListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

/**
*  The ElementListener interface provides a listener
*  interface for receiving element events.
**/
public interface ElementListener extends java.util.EventListener
{
    
   /**
   *  Invoked when an element has been added.
   *  @param event The event. 
   **/
   public abstract void elementAdded(ElementEvent event);
      
   /**
   *  Invoked when an element has been changed.
   *  @param event The event. 
   **/
   public abstract void elementChanged(ElementEvent event);

   /**
   *  Invoked when an element has been removed.
   *  @param event The event. 
   **/
   public abstract void elementRemoved(ElementEvent event);
}
