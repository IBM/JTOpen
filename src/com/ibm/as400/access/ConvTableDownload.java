///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTableDownload.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

// Download character set conversion tables from the 400.  This class provides only single-byte<->Unicode support.
class ConvTableDownload extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private int ccsid;
    private char[] table;

    ConvTableDownload(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
    {
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Downloading Conversion Table for CCSID: ", ccsid);
	this.ccsid = ccsid;
	try
	{
	    // Have the system object load the appropriate impl object
	    NLSImpl impl = (NLSImpl)system.loadImpl("com.ibm.as400.access.NLSImplNative", "com.ibm.as400.access.NLSImplRemote");

	    // get the ccsid from the central server
	    impl.setSystem(system);
	    impl.connect();
	    this.table = impl.getTable(ccsid, 0xF200); // Use old unicode, always available, only incorrect in Korean double byte
	    impl.disconnect();
	    Trace.log(Trace.DIAGNOSTIC, "Downloaded conversion table for ", ccsid);    //@A0A
	}
	catch (Exception e)
	{
	    Trace.log(Trace.ERROR, "Error during table download", e);
	    throw new UnsupportedEncodingException();
	}
    }

    /**
     * Returns the ccsid of this conversion object.
     * @return the ccsid.
     **/
    int getCcsid()
    {
	return this.ccsid;
    }

    /**
     * Returns the encoding of this conversion object.
     * @return the encoding.
     **/
    String getEncoding()
    {
	return String.valueOf(this.ccsid);
    }

    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for CCSID: " + this.ccsid, source, offset, length);
	}
	char[] dest = new char[length];

	for (int i=0; i<length; ++i)
	{
	    dest[i] = this.table[source[i+offset] & 0xFF];
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
	    Trace.log(Trace.CONVERSION, "Converting char to byte for CCSID: " + this.ccsid, ConvTable.dumpCharArray(src));
	}
	byte[] dest = new byte[src.length];

	for (int i=0; i<src.length; ++i)
	{
	    int ii = 0;
	    for (; ii <= 0xFF; ++ii)
	    {
		if (src[i] == this.table[ii])
		{
		    dest[i] = (byte)ii;
		    break;
		}
	    }
	    if (ii == 0x100) dest[i] = 0x6F;
	}
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Char to byte output: ", dest);
	}
	return dest;
    }
}
