///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SectionCompletedEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.servlet;

/**
   The SectionCompletedEvent class represents a SectionCompleted event.
**/
public class SectionCompletedEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   private String section_;		// The completed section of data.

   /**
   * Constructs a SectionCompletedEvent object.
   * It uses the specified <i>source</i> object that completed the section.
   * @param source The object where the event originated.
   **/
   public SectionCompletedEvent(Object source)
   {
      super(source);
   }

   /**
   *  Constructs a SectionCompletedEvent object with the specified data <i>section</i>.
   *  It uses the specified <i>source</i> object that completed the section.
   *  @param source The object where the event originated.
   *  @param section The completed section of data.
   **/
   public SectionCompletedEvent(Object source, String section)
   {
      this(source);
      section_ = section;
   }

   /**
   *  Returns the completed section of data.
   *  @return The section of data.
   **/
   public String getSection()
   {
      return section_;   
   }

   /**
   *  Sets the completed section of data.
   *  @param section The section of data.
   **/
   public void setSection(String section)
   {
      section_ = section;
   }
}

