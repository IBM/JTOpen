///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: IFSUserHandle2Req.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2007 International Business Machines Corporation and
// others.  All rights reserved.
// @ACA Support Create userhandle2
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

public class IFSUserHandle2Req extends IFSDataStreamReq
{
	private static final int TEMPLATE_LENGTH = 2;
	private static final int LLCP_LENGTH = 6;
	
	IFSUserHandle2Req(byte[] authenticationBytes) {
	    this(authenticationBytes, null);
	}
	
	IFSUserHandle2Req(byte[] authenticationBytes, byte[] addAuthFactor)
	{
	    super(HEADER_LENGTH + TEMPLATE_LENGTH + LLCP_LENGTH + authenticationBytes.length
	            + (null != addAuthFactor && 0 < addAuthFactor.length ? 10 + addAuthFactor.length :0));
	    setLength(data_.length);
	    setTemplateLen(TEMPLATE_LENGTH);
	    setReqRepID(0x002B);
	    
	    int offset = 22;
	    
	    // Server Ticket
	    // LL
	    set32bit(authenticationBytes.length + LLCP_LENGTH, offset);
	    // CP
	    set16bit(0x0013, offset + 4);
	    // Data
	    System.arraycopy(authenticationBytes, 0, data_, offset + 6, authenticationBytes.length);
	    
	    offset += (6 + authenticationBytes.length);
	    
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
