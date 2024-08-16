///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSCreateUserHandlerReq.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2016-2016 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

public class IFSCreateUserHandlerReq extends IFSDataStreamReq
{
    private static final int TEMPLATE_LENGTH = 12;

    protected IFSCreateUserHandlerReq(byte[] userID, byte[] password) {
        this(userID, password, null);
    }
    
    protected IFSCreateUserHandlerReq(byte[] userID, byte[] password, byte[] addAuthFactor)
    {
        super(HEADER_LENGTH + TEMPLATE_LENGTH + password.length
                + (null != addAuthFactor && 0 < addAuthFactor.length ? 10 + addAuthFactor.length :0));
        setLength(data_.length);
        setTemplateLen(TEMPLATE_LENGTH + password.length);
        setReqRepID(0x0024);

        int offset = 22;
        
        // Data - user ID
        System.arraycopy(userID, 0, data_, offset, userID.length);
        offset += 10; 

        // Data - substituted password
        System.arraycopy(password, 0, data_, offset, password.length);
        offset += password.length; 
        
        // Authentication factor
        if (addAuthFactor != null && addAuthFactor.length > 0)
        {
            //LL
            set32bit(addAuthFactor.length + 4 + 2 + 4, offset);
            // CP
            set16bit(0x0015, offset + 4);
            // CCSID
            set32bit(1208, offset + 6);
            // data 
            System.arraycopy(addAuthFactor, 0, data_, offset + 10, addAuthFactor.length);
            
            offset += 10 + addAuthFactor.length;
        }
    }
}
