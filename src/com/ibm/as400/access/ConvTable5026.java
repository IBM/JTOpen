///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable5026.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

// table for CCSID 5026
class ConvTable5026 extends ConvTableRedundant
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private final static int ccsid = 5026;
    private ConvTable table;

    ConvTable5026()
    {
	// 5026 is a subset of 930
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 5026 (using CCSID 930)");
    }

    void setSystem(AS400ImplRemote system) throws UnsupportedEncodingException
    {
	this.table = ConvTable.getTable(930, system);
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

    // use the 930 table
    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 5026 (will use CCSID 930)", source, offset, length);
	}
	return table.byteArrayToString(source, offset, length);
    }

    // use 930 table
    byte[] stringToByteArray(String source)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Char to byte output (for CCSID 5026): ");
	}
	return table.stringToByteArray(source);
    }
}
