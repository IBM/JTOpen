///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SectionCompletedSupport.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

import java.util.Vector;

class SectionCompletedSupport
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   transient private Vector sectionListeners_;      // The list of row listeners.
   transient private Object source_;

   public SectionCompletedSupport(Object source)
   {
      sectionListeners_ = new Vector();
      source_ = source;
   }
   
   /**
   *  Adds an SectionCompletedListener.
   *  The SectionCompletedListener object is added to an internal list of SectionCompletedListeners;
   *  it can be removed with removeSectionCompletedListener.
   *
   *  @param listener The SectionCompletedListener.
   **/
   public void addSectionCompletedListener(SectionCompletedListener listener)
   {
      if (listener == null) 
         throw new NullPointerException("listener");
      
      sectionListeners_.addElement(listener);
   }

   /**
   *  Fires a section completed event to notify that a section has been converted.
   *  @param section The completed section of data.
   **/
   public void fireSectionCompleted(String section)
   {
      Vector targets = (Vector) sectionListeners_.clone();
      SectionCompletedEvent event = new SectionCompletedEvent(source_, section);
      for (int i = 0; i < targets.size(); i++) {
          SectionCompletedListener target = (SectionCompletedListener)targets.elementAt(i);
          target.sectionCompleted(event);
      }
   }

   /**
   *  Removes this SectionCompletedListener from the internal list.
   *  If the SectionCompletedListener is not on the list, nothing is done.
   *  @param listener The SectionCompletedListener.
   **/
   public void removeSectionCompletedListener(SectionCompletedListener listener)
   {
      if (listener == null)
         throw new NullPointerException("listener");
      
      sectionListeners_.removeElement(listener);
   }
}
