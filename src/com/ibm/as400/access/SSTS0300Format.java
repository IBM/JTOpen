///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SSTS0300Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class SSTS0300Format extends SystemStatusFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  SSTS0300Format(AS400 sys)
  {
    super(sys);
    setName("SSTS0300");
    addChar(6, "elapsedTime");
    addChar(2, "reserved");
    addBin4("numberOfPools");
    addBin4("offsetToPoolInformation");
    addBin4("lengthOfPoolInformationEntry");
    // The rest of this format is dynamic depending on
    // the above 3 fields and must be built at runtime.
  }
}  
