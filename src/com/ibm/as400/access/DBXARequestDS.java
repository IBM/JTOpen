///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBXARequestDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBXARequestDS class represents a request datastream to the 
XA server.
**/
class DBXARequestDS
extends DBBaseRequestDS
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Server id.
    private static final int SERVER_XA                      = 0xE00A;


    // Request ids.
    public static final int	REQUESTID_XA_CLOSE     	        = 0x18A0;
    public static final int	REQUESTID_XA_COMMIT    	        = 0x18A1;
    public static final int	REQUESTID_XA_COMPLETE  	        = 0x18A2;
    public static final int	REQUESTID_XA_END     	        = 0x18A3;
    public static final int	REQUESTID_XA_FORGET   	        = 0x18A4;
    public static final int	REQUESTID_XA_OPEN    	        = 0x18A5;
    public static final int	REQUESTID_XA_PREPARE  	        = 0x18A6;
    public static final int	REQUESTID_XA_RECOVER	        = 0x18A7;
    public static final int	REQUESTID_XA_ROLLBACK 	        = 0x18A8;
    public static final int	REQUESTID_XA_START   	        = 0x18A9;



/**
Constructs an XA Server request datastream.

@param requestId                        The request id.
@param rpbId                            The request parameter block id.
@param operationResultBitmap            The operation result bitmap.
@param parameterMarkerDescriptorHandle  The parameter marker descriptor handle.
**/
	public DBXARequestDS(int requestId,
		        	     int rpbId,
				         int operationResultBitmap,
				         int parameterMarkerDescriptorHandle)
    {
	   super(requestId, 
             rpbId, 
             operationResultBitmap,
		     parameterMarkerDescriptorHandle);
	   setServerID(SERVER_XA);
    }



/**
Sets the resource manager ID.

@param value The resource manager ID.
@exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setResourceManagerID(int value)
		throws DBDataStreamException
	{
		addParameter(0x38A0, value);
	}



/**
Sets the XA information.

@param value The XA information.
@exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setXAInformation(byte[] value)
		throws DBDataStreamException
	{
		addParameter(0x38A1, value);
	}



/**
Sets the Xid.

@param value The Xid.
@exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setXid(byte[] value)
		throws DBDataStreamException
	{
		addParameter(0x38A2, value);
	}



/**
Sets the handle.

@param value The handle.
@exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setHandle(int value)
		throws DBDataStreamException
	{
		addParameter(0x38A3, value);
	}



/**
Sets the flags.

@param value The flags.
@exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setFlags(int value)
		throws DBDataStreamException
	{
		addParameter(0x38A5, value);
	}



/**
Sets the count.

@param value The count.
@exception DBDataStreamException If there is not enough space left in the data byte array.
**/
	void setCount(int value)
		throws DBDataStreamException
	{
		addParameter(0x38A6, value);
	}



}







