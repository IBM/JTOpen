///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0200Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0200Format extends JobFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  JobI0200Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0200");
    addChar(10, "subsystemDescriptionName");
    addBin4("runPriority");
    addBin4("systemPoolIdentifier");
    addBin4("processingUnitTimeUsed");
    addBin4("numberOfAuxiliaryIORequests");
    addBin4("numberOfInteractiveTransactions");
    addBin4("responseTimeTotal");
    addChar(1, "functionType");
    addChar(10, "functionName");
    addChar(4, "activeJobStatus");
    addChar(1, "reserved");
    addBin4("numberOfDatabaseLockWaits");
    addBin4("numberOfNondatabaseLockWaits");
    addBin4("numberOfInternalMachineLockWaits");
    addBin4("timeSpentOnDatabaseLockWaits");
    addBin4("timeSpentOnNondatabaseLockWaits");
    addBin4("timeSpentOnInternalMachineLockWaits");
    addBin4("currentSystemPoolIdentifier");
    addBin4("threadCount");
  }
}  
