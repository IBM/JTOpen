///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseReleaseRequest.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


class LicenseReleaseRequest extends LicenseGetRequest
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    
    LicenseReleaseRequest(AS400 system)
    {
        super(system);
        setReqRepID(0x1002);
    }
}
