///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseGetInformationRequest.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/** <p>This class is used to set up the request license information datastream. 
* <b>This class needs to be beefed up.</b> </p>
**/

class LicenseGetInformationRequest extends LicenseBaseRequest
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /** <p>RETRIEVE_LICENSE_INFO_DATASTREAM_SIZE - size of the retrieve licnese information datastream.
    *  Includes the header and template (there currently are no optional parameters. </p> 
    **/
    static final int RETRIEVE_LICENSE_INFO_DATASTREAM_SIZE = 22; 

     /** <p>PRODUCT_ID_LOCATION - Location of the type of information requested in the datastream.</p> **/
    static final int INFORMATON_TYPE_LOCATION = 51;    


    LicenseGetInformationRequest(AS400 system)
    {
        super(RETRIEVE_LICENSE_INFO_DATASTREAM_SIZE, system);
        setReqRepID(0x1003);                   // request id for retrieve license info. 

        set16bit(1, INFORMATON_TYPE_LOCATION); // 1 = retrieve detailed information
    }
}

