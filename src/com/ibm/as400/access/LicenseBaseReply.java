///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseBaseReply.java
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
class LicenseBaseReply extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    protected int primaryRC_=0;            // return code returned by server
    protected int secondaryRC_=0;          // return code returned by server


    /** <p>REPLY_PRIMARY_RETURN_CODE_LOCATION - Location of the primary return code in the reply datastream.</p> **/
    static final int REPLY_PRIMARY_RETURN_CODE_LOCATION = 22;    
    
    /** <p>REPLY_SECONDARY_RETURN_CODE_LOCATION - Location of the secondary return code in the reply datastream.</p> **/
    static final int REPLY_SECONDARY_RETURN_CODE_LOCATION = 24;    
    


    LicenseBaseReply()
    {
        super();
    }

    /** <p> This method creates a new LicenseReleaseReply object. </p>
    **/
    public Object getNewDataStream()
    {
        return new LicenseBaseReply();
    }

    /** <p> This method returns the primary retrun code for the license release datastream. </p>
    **/
    public int getPrimaryRC()
    {
        return get16bit(REPLY_PRIMARY_RETURN_CODE_LOCATION);
    }
    
    /** <p> This method returns the secondary retrun code for the license release datastream. </p>
    **/
    public int getSecondaryRC()
    {
        return get16bit(REPLY_SECONDARY_RETURN_CODE_LOCATION);
    }
}

