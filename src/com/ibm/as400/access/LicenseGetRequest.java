///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseGetRequest.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.CharConversionException;


/** 
*<p>This class is used to set up the request license datastream. </p> 
**/

class LicenseGetRequest extends LicenseBaseRequest
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /** <p>RETRIEVE_LICENSE_INFO_DATASTREAM_SIZE - size of the retrieve licnese information datastream.
    *  Includes the header and template (there currently are no optional parameters. </p> 
    **/
    static final int REQUEST_LICENSE_DATASTREAM_SIZE = 324; 

    /** <p>CLIENT_UNIQUE_NAME_CCSID_LOCATION - Location of the CCSID for client workstation name in the datastream.</p> **/
     static final int CLIENT_UNIQUE_NAME_CCSID_LOCATION = 51;    
    
     /** <p>PRODUCT_ID_LOCATION - Location of the client workstation name (unique to its domain) in the datastream.</p> **/
    static final int CLIENT_UNIQUE_NAME_LOCATION = 55;    
    
    /** <p>USER_HANDLE_CCSID_LOCATION - Location of the CCSID for user handle in the datastream.</p> **/
    static final int USER_HANDLE_CCSID_LOCATION = 310;    

    /** <p>FEATURE_LOCATION - Location of user handle in the datastream. It is used to ensure that the 
    * license requester and license releaser are the same</p> **/
    static final int USER_HANDLE_LOCATION = 314;    
    


    /** <p> The constructor sets up the header for the license request datastream. </p>**/
    LicenseGetRequest(AS400 system)
    {
        super(REQUEST_LICENSE_DATASTREAM_SIZE, system);
        setReqRepID(0x1001);

        int i;
        // blank fill unique name
        for (i=CLIENT_UNIQUE_NAME_LOCATION; i<310; i++) 
        //  310 = CLIENT_UNIQUE_NAME_LOCATION + the unique name size of 255
        {
            data_[i] = (byte)0x40;
        }

        // blank fill license user handle
        for (i=USER_HANDLE_LOCATION; i<322; i++)
        //  322 = USER_HANDLE_LOCATION + the user handle size of 8
        {
            data_[i] = (byte)0x40;
        }

        set32bit(37, CLIENT_UNIQUE_NAME_CCSID_LOCATION);         // unique name ccsid
        set32bit(37, USER_HANDLE_CCSID_LOCATION);                // user handle ccsid

        try
        {
            String localHostName = InetAddress.getLocalHost().getHostName();
            try
            {
                conv_.stringToByteArray(localHostName, data_, CLIENT_UNIQUE_NAME_LOCATION, 255);      // unique name
            }
            catch (CharConversionException cce)
            {
                if(Trace.isTraceOn())
                {
                    Trace.log(Trace.ERROR, "Character conversion exception - localHostName: " + localHostName);    
                }
            }
        }
        catch (UnknownHostException e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "UnknownHostException.");   
            }
        }

        try
        {
            conv_.stringToByteArray("JT400", data_, USER_HANDLE_LOCATION, 5);      // license user handle
        }
        catch (CharConversionException e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Character conversion exception - userHandle: JT400");    
            }

        }
    }
}

