///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0500Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0500Format extends JobFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  JobI0500Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0500");
    addChar(2, "reserved");
    addBin4("endSeverity");
    addBin4("loggingSeverity");
    addChar(1, "loggingLevel");
    addChar(10, "loggingText");
  }
}  
