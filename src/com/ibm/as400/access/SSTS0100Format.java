///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SSTS0100Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class SSTS0100Format extends SystemStatusFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  SSTS0100Format(AS400 sys)
  {
    super(sys);
    setName("SSTS0100");
    addBin4("usersCurrentlySignedOn");
    addBin4("usersTemporarilySignedOff");
    addBin4("usersSuspendedBySystemRequest");
    addBin4("usersSuspendedByGroupJobs");
    addBin4("usersSignedOffWithPrinterOutputWaitingToPrint");
    addBin4("batchJobsWaitingForMessages");
    addBin4("batchJobsRunning");
    addBin4("batchJobsHeldWhileRunning");
    addBin4("batchJobsEnding");
    addBin4("batchJobsWaitingToRunOrAlreadyScheduled");
    addBin4("batchJobsHeldOnAJobQueue");
    addBin4("batchJobsOnAHeldJobQueue");
    addBin4("batchJobsOnAnUnassignedJobQueue");
    addBin4("batchJobsEndedWithPrinterOutputWaitingToPrint");
  }
}  
