///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0300Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0300Format extends JobFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  JobI0300Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0300");
    addChar(10, "jobQueueName");
    addChar(10, "jobQueueLibraryName");
    addChar(2, "jobQueuePriority");
    addChar(10, "outputQueueName");
    addChar(10, "outputQueueLibraryName");
    addChar(2, "outputQueuePriority");
    addChar(10, "printerDeviceName");
    addChar(10, "submittersJobName");
    addChar(10, "submittersUserName");
    addChar(6, "submittersJobNumber");
    addChar(10, "submittersMessageQueueName");
    addChar(10, "submittersMessageQueueLibraryName");
    addChar(10, "statusOfJobOnTheJobQueue");
    addFieldDescription(new HexFieldDescription(new AS400ByteArray(8), "dateAndTimeJobWasPutOnThisJobQueue"));
    addChar(7, "jobDate");
  }
}  
