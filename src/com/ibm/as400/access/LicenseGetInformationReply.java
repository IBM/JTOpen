///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseGetInformationReply.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
*<p>This class provides the interface to retrieve the various information returned in the 
* get license information reply datastream. <b> This class will need to be fleshed out more.</b>
* </p>
**/

class LicenseGetInformationReply extends LicenseBaseReply
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    LicenseGetInformationReply()
    {
        super();
    }

    /**
    *<p>This method returns a new LicenseGetInformationReply object
    * </p>
    **/
    public Object getNewDataStream()
    {
        return new LicenseGetInformationReply();
    }

    /**
    *<p>This method returns the code point for the retrieve license information datastream.
    * </p>
    **/
    public int hashCode()
    {
        return 0x1003;  // returns the reply ID
    }
}
