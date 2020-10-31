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

public class IFSUserHandle2Req extends IFSDataStreamReq {
	private static final int TEMPLATE_LENGTH = 10;
	private static final int LLCP_LENGTH = 6;
	private static final int SERVER_TICKET_LL_OFFSET = 22;
	private static final int SERVER_TICKET_CP_OFFSET = 26;
	private static final int SERVER_TICKET_OFFSET = 28;
	
	IFSUserHandle2Req(byte[] authenticationBytes) {
	    super(HEADER_LENGTH + TEMPLATE_LENGTH + LLCP_LENGTH + authenticationBytes.length);
	    setLength(data_.length);
	    setTemplateLen(TEMPLATE_LENGTH);
	    setReqRepID(0x0023);
	    
	    // Set the 'Server Ticket' LL.
	    set32bit(authenticationBytes.length + LLCP_LENGTH, SERVER_TICKET_LL_OFFSET);

	    // Set the 'filename' code point.
	    set16bit(0x0013, SERVER_TICKET_CP_OFFSET);

	    // Set the 'filename' value.
	    System.arraycopy(authenticationBytes, 0, data_, SERVER_TICKET_OFFSET, authenticationBytes.length);
	}

}
