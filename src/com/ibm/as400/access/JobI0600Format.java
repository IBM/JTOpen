///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0600Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0600Format extends JobFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  JobI0600Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0600");
    addChar(8, "jobSwitches");
    addChar(1, "endStatus");
    addChar(10, "subsystemDescriptionName");
    addChar(10, "subsystemDescriptionLibraryName");
    addChar(10, "currentUserProfile");
    addChar(1, "DBCSCapable");
    addChar(1, "exitKey");
    addChar(1, "cancelKey");
    addBin4("productReturnCode");
    addBin4("userReturnCode");
    addBin4("programReturnCode");
    addChar(10, "specialEnvironment");
    addChar(10, "deviceName");
    addChar(10, "groupProfileName");
    for (int i=0; i<15; ++i)
    {
      addChar(10, "groupProfileNameSupplemental"+i);
    }
    addChar(10, "jobUserIdentity");
    addChar(1, "jobUserIdentitySetting");
  }
}  
