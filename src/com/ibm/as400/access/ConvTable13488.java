///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable13488.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// table for CCSID 13488
class ConvTable13488 extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private final static int ccsid = 13488;

    ConvTable13488()
    {
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for CCSID: 13488");
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

    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: 13488", source, offset, length);
	}

	char[] dest = new char[length/2];
	int destpos = 0;

	for (int srcpos = offset; srcpos < offset+length; srcpos+=2)
	{
	    // pass value through
	    dest[destpos++] = (char)(((source[srcpos]   & 0xFF) << 8) +
				      (source[srcpos+1] & 0xFF));
	}

	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Byte to char output: ", ConvTable.dumpCharArray(dest));
	}

	return new String(dest);
    }

    byte[] stringToByteArray(String source)
    {
	char[] src = source.toCharArray();
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting char to byte for CCSID: 13488", ConvTable.dumpCharArray(src));
	}

	byte[] dest = new byte[src.length*2];
	int destpos = 0;

	for (int srcpos = 0; srcpos < src.length; ++srcpos)
	{
	    // pass char through
	    dest[destpos++] = (byte)(src[srcpos] >>> 8);
	    dest[destpos++] = (byte)src[srcpos];
	}

	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Char to byte output: ", dest);
	}
	return dest;
    }
}
