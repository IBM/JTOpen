///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SSTS0400Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class SSTS0400Format extends SystemStatusFormat
{
  static final long serialVersionUID = 4L;
  private static final String copyright = "Copyright (C) 2007-2007 International Business Machines Corporation and others.";

  // Design note: Whereas the SSTS0300 format returns info on _active_ pools only, this format returns info on _all_ pools (active or inactive).
  SSTS0400Format(AS400 sys)
  {
    super(sys);
    setName("SSTS0400");
    addChar(6, "elapsedTime");
    addChar(2, "reserved");
    addBin4("mainStorageSize");
    addBin4("minimumMachinePoolSize");
    addBin4("minimumBasePoolSize");
    addBin4("numberOfPools");
    addBin4("offsetToPoolInformation");
    addBin4("lengthOfPoolInformationEntry");
    addBin8("mainStorageSize");
    // The rest of this format is dynamic depending on
    // the above fields and must be built at runtime.
  }
}  
