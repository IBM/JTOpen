///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable1027.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

// Table for CCSID 1027
class ConvTable1027 extends ConvTableRedundant
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private final static int ccsid = 1027;
    private ConvTable table;

    ConvTable1027()
    {
	// 1027 is the single-byte portion of the mixed-byte table 939
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 1027 using CCSID 939");
    }

    void setSystem(AS400ImplRemote system) throws UnsupportedEncodingException
    {
	this.table = ConvTable.getTable(939, system);
    }

    // Returns the ccsid of this conversion object.
    // @return the ccsid.
    int getCcsid()
    {
	return ccsid;
    }

    // Returns the encoding of this conversion object.
    // @return the encoding.
    String getEncoding()
    {
	return String.valueOf(ccsid);
    }

    // use the 939 table
    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 1027 (will use 939)", source, offset, length);
	}
	return table.byteArrayToString(source, offset, length);
    }

    // use 939 table
    byte[] stringToByteArray(String source)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Char to byte output (for CCSID 1027): ");
	}
	return table.stringToByteArray(source);
    }
}
