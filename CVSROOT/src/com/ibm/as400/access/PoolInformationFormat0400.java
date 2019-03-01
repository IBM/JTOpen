///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PoolInformationFormat0400.java
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
 * information portion of the SSTS0400 format on the
 * QWCRSSTS API.
**/
class PoolInformationFormat0400 extends PoolInformationFormat
{
  private static final String copyright = "Copyright (C) 2007-2007 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;



  PoolInformationFormat0400(AS400 sys)
  {
    super(sys);
    AS400Text text1  = new AS400Text( 1, sys.getCcsid(), sys);
    AS400Text text50 = new AS400Text(50, sys.getCcsid(), sys);

    // Note that this class extends class PoolInformationFormat, which represents
    // the SSTS0300 format.  The SSTS0400 format contains the same fields as SSTS0300,
    // in addition to the following fields.

    addFieldDescription(new BinaryFieldDescription(bin4, "definedSize"));
       // The size of the pool, in kilobytes, as defined in the shared pool, subsystem description, or system value QMCHPOOL. -1 will be returned for pools without a defined size.
    addFieldDescription(new BinaryFieldDescription(bin4, "currentThreads"));
    addFieldDescription(new BinaryFieldDescription(bin4, "currentIneligibleThreads"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningPriority"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningMinimumPoolSizePercentage"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningMaximumPoolSizePercentage"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningMinimumFaults"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningPerThreadFaults"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningMaximumFaults"));
    addFieldDescription(new CharacterFieldDescription(text50, "description"));
    addFieldDescription(new CharacterFieldDescription(text1, "status"));
    addFieldDescription(new CharacterFieldDescription(text1, "reserved"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningMinimumActivityLevel"));
    addFieldDescription(new BinaryFieldDescription(bin4, "tuningMaximumActivityLevel"));

  }
}
                

