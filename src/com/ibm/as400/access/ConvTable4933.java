///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable4933.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

// Table for CCSID 4933
class ConvTable4933 extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private final static int ccsid = 4933;
    private ConvTable1388 table = new ConvTable1388();

    ConvTable4933() throws UnsupportedEncodingException
    {
	// CCSID 4933 is the DBCS portion of the mixed byte CCSID 1388
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 4933 (using CCSID 1388)");
    }

    /**
     * Returns the ccsid of this conversion object.
     * @return the ccsid.
     **/
    int getCcsid()
    {
	return ccsid;
    }

    /**
     * Returns the encoding of this conversion object.
     * @return the encoding.
     **/
    String getEncoding()
    {
	return String.valueOf(ccsid);
    }

    // use the 1388 table ofter adding shift-out/shift-in
    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 4933 (will use CCSID 1388)", source, offset, length);
	}
	byte[] newSource = new byte[length+2];
	newSource[0] = 0x0E;
	newSource[length+1] = 0x0F;
	System.arraycopy(source, offset, newSource, 1, length);
	return table.byteArrayToString(newSource, 0, length+2);
    }

    // use 1388 table, then remove shift-out/shift-in
    byte[] stringToByteArray(String source)
    {
	byte[] dest = table.stringToPureDBByteArray(source);
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Char to byte output (for CCSID 4933): ", dest);
	}
	return dest;
    }
}
