///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0100Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0100Format extends JobFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // Format 0150 includes all of format 0100
  
  JobI0100Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0100");
    addChar(2, "reserved");
    addBin4("runPriority");
    addBin4("timeSlice");
    addBin4("defaultWait");
    addChar(10, "purge");
  }
}  
