///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SSTS0200Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class SSTS0200Format extends SystemStatusFormat
{
  static final long serialVersionUID = 4L;

  SSTS0200Format(AS400 sys)
  {
    super(sys);
    setName("SSTS0200");
    addChar(6, "elapsedTime");
    addChar(1, "restrictedStateFlag");
    addChar(1, "reserved");
    addBin4("percentProcessingUnitUsed");
    addBin4("jobsInSystem");
    addBin4("percentPermanentAddresses");
    addBin4("percentTemporaryAddresses");
    addBin4("systemASP");
    addBin4("percentSystemASPUsed");
    addBin4("totalAuxiliaryStorage");
    addBin4("currentUnprotectedStorageUsed");
    addBin4("maximumUnprotectedStorageUsed");
    addBin4("percentDBCapability");
    addBin4("mainStorageSize");
    addBin4("numberOfPartitions");
    addBin4("partitionIdentifier");
    addBin4("reserved1");
    addBin4("currentProcessingCapacity");
    addChar(1, "processorSharingAttribute");
    addChar(3, "reserved2");
    addBin4("numberOfProcessors");
    addBin4("activeJobsInSystem");
    addBin4("activeThreadsInSystem");
    addBin4("maximumJobsInSystem");
    addBin4("percentTemporary256MBSegmentsUsed");
    addBin4("percentTemporary4GBSegmentsUsed");
    addBin4("percentPermanent256MBSegmentsUsed");
    addBin4("percentPermanent4GBSegmentsUsed");
    addBin4("percentCurrentInteractivePerformance");
    addBin4("percentUncappedCPUCapacityUsed");
    addBin4("percentSharedProcessorPoolUsed");
  }
}  
