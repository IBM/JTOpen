///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0150Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0150Format extends JobI0100Format
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // Format 0150 includes all of format 0100
  
  JobI0150Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0150");
    addChar(10, "timeSliceEndPool");
    addBin4("processingUnitTimeUsed");
    addBin4("systemPoolIdentifier");
    addBin4("maximumProcessingUnitTime");
    addBin4("temporaryStorageUsedInKilobytes");
    addBin4("maximumTemporaryStorageUsedInKilobytes");
    addBin4("threadCount");
    addBin4("maximumThreads");
    addBin4("temporaryStorageUsedInMegabytes");
    addBin4("maximumTemporaryStorageUsedInMegabytes");
  }
}  
