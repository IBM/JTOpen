///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseGetReply.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;

/**
*<p>This class provides the interface to retrieve the various information returned in the 
* request license reply datastream.
* </p>
**/

class LicenseGetReply extends LicenseBaseReply
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /** <p>REPLY_USAGE_LIMIT_LOCATION - Location of the usage limit in the reply datastream.</p> **/
    static final int REPLY_USAGE_LIMIT_LOCATION = 26;    

    /** <p>REPLY_USAGE_COUNT_LOCATION - Location of the usage count in the reply datastream.</p> **/
    static final int REPLY_USAGE_COUNT_LOCATION = 30;    
    
    /** <p>REPLY_USAGE_TYPE_LOCATION - Location of the usage type in the reply datastream.</p> **/
    static final int REPLY_USAGE_TYPE_LOCATION = 34;    
    
    /** <p>REPLY_COMPLIANCE_TYPE_LOCATION - Location of the compliance type in the reply datastream.</p> **/
    static final int REPLY_COMPLIANCE_TYPE_LOCATION = 36;    
    
    /** <p>REPLY_LICENSE_TERM_CCSID_LOCATION - Location of the license term in the reply datastream.</p> **/
    static final int REPLY_LICENSE_TERM_CCSID_LOCATION = 38;    
    
    /** <p>REPLY_LICENSE_TERM_LOCATION - Location of the license term in the reply datastream.</p> **/
    static final int REPLY_LICENSE_TERM_LOCATION = 42;    
    
    /** <p>REPLY_RELEASE_LEVEL_CCSID_LOCATION - Location of the license release level in the reply datastream.</p> **/
    static final int REPLY_RELEASE_LEVEL_CCSID_LOCATION = 48;    
    
    /** <p>REPLY_RELEASE_LEVEL_LOCATION - Location of the license release level in the reply datastream.</p> **/
    static final int REPLY_RELEASE_LEVEL_LOCATION = 52;    
    
    
    LicenseGetReply()
    {
        super();
    }

    public Object getNewDataStream()
    {
        return new LicenseGetReply();
    }

    public int hashCode()
    {
        return 0x1001;  // returns the reply ID
    }

    public int getUsageLimit()
    {
        return get32bit(REPLY_USAGE_LIMIT_LOCATION);
    }

    public int getUsageCount()
    {
        return get32bit(REPLY_USAGE_COUNT_LOCATION);
    }

    public int getUsageType()
    {
        return get16bit(REPLY_USAGE_TYPE_LOCATION);
    }

    public int getComplianceType()
    {
        return get16bit(REPLY_COMPLIANCE_TYPE_LOCATION);
    }

    public String getLicenseTerm()
    {
        try
        {
            return ConvTable.getTable(get32bit(REPLY_LICENSE_TERM_CCSID_LOCATION), null).byteArrayToString(data_, REPLY_LICENSE_TERM_LOCATION, 6, 0);
        }
        catch (UnsupportedEncodingException e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.DIAGNOSTIC, "Unable to convert the license term so return null.");
            }
            
            return null;
        }
    }  

    public String getReleaseLevel()
    {
        try
        {
            return ConvTable.getTable(get32bit(REPLY_RELEASE_LEVEL_CCSID_LOCATION),
                  null).byteArrayToString(data_, REPLY_RELEASE_LEVEL_LOCATION, 6, 0);

        }
        catch (UnsupportedEncodingException e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.DIAGNOSTIC, "Unable to convert the release level so return null.");
            }
            return null;
        }
    }
}
