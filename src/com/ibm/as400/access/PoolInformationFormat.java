///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PoolInformationFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * This class defines the format for the repeated pool
 * information portion of the SSTS0300 format on the
 * QWCRSSTS API.
**/
class PoolInformationFormat extends RecordFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



  PoolInformationFormat(AS400 sys)
  {
    super();
    AS400Bin4 bin4 = new AS400Bin4();
    AS400Text text10 = new AS400Text(10, sys.getCcsid(), sys);
    addFieldDescription(new BinaryFieldDescription(bin4, "poolIdentifier"));  // system-related pool ID
    addFieldDescription(new BinaryFieldDescription(bin4, "poolSize"));
    addFieldDescription(new BinaryFieldDescription(bin4, "reservedSize"));
    addFieldDescription(new BinaryFieldDescription(bin4, "activityLevel"));  // maximum active threads
    addFieldDescription(new BinaryFieldDescription(bin4, "databaseFaults"));
    addFieldDescription(new BinaryFieldDescription(bin4, "databasePages"));
    addFieldDescription(new BinaryFieldDescription(bin4, "nonDatabaseFaults"));
    addFieldDescription(new BinaryFieldDescription(bin4, "nonDatabasePages"));
    addFieldDescription(new BinaryFieldDescription(bin4, "activeToWait"));
    addFieldDescription(new BinaryFieldDescription(bin4, "waitToIneligible"));
    addFieldDescription(new BinaryFieldDescription(bin4, "activeToIneligible"));
    addFieldDescription(new CharacterFieldDescription(text10, "poolName")); // in the case of private (subsystem) pools, this will be a number (1-10)
    addFieldDescription(new CharacterFieldDescription(text10, "subsystemName"));
    addFieldDescription(new CharacterFieldDescription(text10, "subsystemLibraryName"));
    addFieldDescription(new CharacterFieldDescription(text10, "pagingOption"));
  }
}
                

