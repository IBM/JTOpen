///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SectionCompletedListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

/**
  The SectionCompletedListener interface provides a listener
  interface for receiving SectionCompleted events.
**/
public interface SectionCompletedListener extends java.util.EventListener
{
   /**
     Invoked when a section of data has been converted.
     @param event The event.			
   **/
   public abstract void sectionCompleted(SectionCompletedEvent event);	// @C1
}
