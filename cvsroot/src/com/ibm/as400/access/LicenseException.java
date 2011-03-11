///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseException.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;


/**
*   Indicates an error condition that occured while
*   trying to retrieve a license.
**/
public class LicenseException extends Exception
                              implements ReturnCodeException
{                                                 
    static final long serialVersionUID = 4L;


    /** 
    *<p>INVALID_REQUEST_CLIENT_NAME - Return code from the Central server when
    * the client name is not valid. </p> 
    **/
    // The primary return code is 0001, the secondary return code is 0002
    public static final int INVALID_REQUEST_CLIENT_NAME = 0x00010002; // 65538
    
    /** 
    *<p>REQUEST_NOT_VALID_LICENSE_USER_HANDLE - Return code from the Central 
    * server when the license user handle is not valid. </p> 
    **/
    //The primary return code is 0001, the secondary return code is 0020 
    public static final int REQUEST_NOT_VALID_LICENSE_USER_HANDLE = 0x00010020; // 65568

    /** 
    *<p>REQUEST_NOT_VALID_PRODUCT_ID - Return code from the Central server when
    * the product ID is not valid.  </p> 
    **/
    // The primary return code is 0001, the secondary return code is 0021
    public static final int REQUEST_NOT_VALID_PRODUCT_ID = 0x00010021; // 65569
    
    /** 
    *<p>REQUEST_NOT_VALID_RELEASE - Return code from the Central server when
    * the product release is not valid.  </p>  
    **/
    // The primary return code is 0001, the secondary return code is 0022
    public static final int REQUEST_NOT_VALID_RELEASE = 0x00010022; // 65570
    
    /** 
    *<p>REQUEST_NOT_VALID_FEATURE - Return code from the Central server when
    * the product feature ID is not valid. </p> 
    **/
    // The primary return code is 0001, the secondary return code is 0023 
    public static final int REQUEST_NOT_VALID_FEATURE = 0x00010023; // 65571
    
    /** 
    *<p>REQUEST_NOT_VALID_TYPE_OF_LICENSE_INFO - Return code from the Central server when
    * the type of license information requested is not valid. </p> 
    **/
    // The primary return code is 0001, the secondary return code is 0024 
    public static final int REQUEST_NOT_VALID_TYPE_OF_LICENSE_INFO = 0x00010024; // 65572 
    
    /** 
    *<p>DATA_CONVERSION_CLIENT_NAME - Return code from the Central server when
    * the client name can not be converted to the job CCSID.  </p> 
    **/
    // The primary return code is 0002, the secondary return code is 0002
    public static final int DATA_CONVERSION_CLIENT_NAME = 0x00020002; // 131074
    
    /** 
    *<p>DATA_CONVERSION_LICENSE_USER_HANDLE - Return code from the Central server when
    * the license handle is not valid.  </p> 
    **/
    // The primary return code is 0002, the secondary return code is 0020
    public static final int DATA_CONVERSION_LICENSE_USER_HANDLE = 0x00020020; // 131104
    
    /** 
    *<p>DATA_CONVERSION_PRODUCT_ID - Return code from the Central server when the
    * product ID can not be converted to the job CCSID. </p> 
    **/
    // The primary return code is 0002, the secondary return code is 0021
    public static final int DATA_CONVERSION_PRODUCT_ID = 0x00020021; // 131105
    
    /** 
    *<p>DATA_CONVERSION_RELEASE - Return code from the Central server when the 
    * product release can not be converted to the job CCSID. </p> 
    **/
    // The primary return code is 0002, the secondary return code is 0022
    public static final int DATA_CONVERSION_RELEASE = 0x00020022; // 131106
    
    /** 
    *<p>DATA_CONVERSION_FEATURE - Return code from the Central server when the 
    * product feature can not be converted to the job CCSID. </p> 
    **/
    // The primary return code is 0002, the secondary return code is 0023
    public static final int DATA_CONVERSION_FEATURE = 0x00020023; // 131107

    /** 
    *<p>REQUEST_NOT_VALID - Return code from the Central server when the datastream does 
    * not match the expected datastream. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0001
    public static final int REQUEST_NOT_VALID = 0x00030001; // 196609
    
    /** 
    *<p>ERROR_CALLING_EXIT_PROGRAM - Return code from the Central server when there was
    * an error calling the regestered exit program. For example, does not exist.  </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0003
    public static final int ERROR_CALLING_EXIT_PROGRAM = 0x00030003; // 196611
    
    /** 
    *<p>REJECTED_BY_EXIT_PROGRAM - Return code from the Central server when the 
    * regestered exit program rejects the request. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0004
    public static final int REJECTED_BY_EXIT_PROGRAM = 0x00030004; // 196612
    
    /** 
    *<p>REQUEST_LICENSE_ERROR - Return code from the Central server when
    * an unexpected error is encountered when requesting a license. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0025 
    public static final int REQUEST_LICENSE_ERROR = 0x00030025; // 196645
    
    /** 
    *<p>RELEASE_LICENSE_ERROR - Return code from the Central server when
    * an unexpected error is encountered when releasing a license. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0026 
    public static final int RELEASE_LICENSE_ERROR = 0x00030026; // 196646
    
    /** 
    *<p>RETRIEVE_LICENSE_INFORMATION_ERROR - Return code from the Central server when
    * an unexpected error is encountered when requesting information on a license. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0027
    public static final int RETRIEVE_LICENSE_INFORMATION_ERROR = 0x00030027; // 196647
    
    /** 
    *<p>LICENSE_INFORMATION_NOT_FOUND - Return code from the Central server when
    * the license information for the product is not available (for example,
    * product for specified feature and release was not found.) </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0028 
    public static final int LICENSE_INFORMATION_NOT_FOUND = 0x00030028; // 196648
    
    /** 
    *<p>LICENSE_INFORMATION_NOT_AVAILABLE - Return code from the Central server when
    * an error occurred.  </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0029
    public static final int LICENSE_INFORMATION_NOT_AVAILABLE = 0x00030029; // 196649
    
    /** 
    *<p>MISMATCH_RELEASE_REQUEST_HANDLE - Return code from the Central server when
    * the handle used to attempt to release a license is not the handle that the 
    * under which the request license was issued. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 002A
    public static final int MISMATCH_RELEASE_REQUEST_HANDLE = 0x0003002a; // 196650
    
    /** 
    *<p>USAGE_LIMIT_EXCEEDED - Return code from the Central server when the usage limit
    * for the product license being requested is exceeded and a license was not granted. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 002D
    public static final int USAGE_LIMIT_EXCEEDED = 0x0003002d; // 196653
    
    /** 
    *<p>GRACE_PERIOD_EXPIRED - Return code from the Central server when the grace period
    * for the product license being requested is exceeded and a license was not granted. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 002F
    public static final int GRACE_PERIOD_EXPIRED = 0x0003002f; // 196655
    
    /** 
    *<p>EXPIRATION_DATE_REACHED - Return code from the Central server when the expiration
    * date for the product license being requested is reached and a license was not granted. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0030 
    public static final int EXPIRATION_DATE_REACHED = 0x00030030; // 196656
    
    /** 
    *<p>ERROR_CALLING_USER_EXIT_PROGRAM - Return code from the Central server when in incurred an error from a user exit program. </p> 
    **/
    // The primary return code is 0003, the secondary return code is 0033 
    public static final int ERROR_CALLING_USER_EXIT_PROGRAM = 0x00030033; // 196659

    private int rc;

    /**
    *   Constructs a LicenseException object.
    *   @param primaryRC    The primary return code returned by the server.
    *   @param secondaryRC  The secondary return code returned by the server.
    **/
    LicenseException(int primaryRC, int secondaryRC)
    {
        super(buildMessage(primaryRC, secondaryRC));
        rc = (primaryRC * 0x10000) + secondaryRC;
    }

    private static String buildMessage(int primaryRC, int secondaryRC)
    {
        return ResourceBundleLoader.getText("LM_EXCEPTION", 
                                            new Integer(primaryRC), 
                                            new Integer(secondaryRC));
    }

    /**
    *   Returns the return code.
    **/
    
    public int getReturnCode()
    {
        return rc;
    }
}
