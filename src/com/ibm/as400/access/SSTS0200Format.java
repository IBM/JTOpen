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
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
  }
}  
