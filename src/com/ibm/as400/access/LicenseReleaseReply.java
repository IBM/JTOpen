///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseReleaseReply.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/** <p> This class is used to access certain information from the release license reply datastream.</p>
**/
class LicenseReleaseReply extends LicenseBaseReply
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    LicenseReleaseReply()
    {
        super();
    }

    /** <p> This method creates a new LicenseReleaseReply object. </p>
    **/
    public Object getNewDataStream()
    {
        return new LicenseReleaseReply();
    }

    /** <p> This method returns the reply ID for the license release datastream. </p>
    **/
    public int hashCode()
    {
        return 0x1002;  // returns the reply ID
    }

}
