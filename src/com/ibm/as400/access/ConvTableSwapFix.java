///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTableSwapFix.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

// This class swaps 39 Japanese characters which have been identified as not mapping correctly.
class ConvTableSwapFix extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private ConvTable table;

    ConvTableSwapFix(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
    {
	super();
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for extra swap fix, ccsid = " + ccsid);
	this.table = ConvTable.getTableNoSwapChars(ccsid, system);
    }

    // Returns the ccsid of this conversion object.
    // @return  the ccsid.
    int getCcsid()
    {
	return table.getCcsid();
    }

    // Returns the encoding of this conversion object.
    // @return  the encoding.
    String getEncoding()
    {
	return table.getEncoding();
    }

    // convert to unicode then swap in correct representation
    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for extra swap fix", source, offset, length);
	}

	char[] charBuffer = table.byteArrayToString(source, offset, length).toCharArray();

	for (int i = 0; i < charBuffer.length; ++i)
	{
	    switch (charBuffer[i])
	    {
		case '\u9E7C': charBuffer[i] = '\u9E78'; break;
		case '\u9830': charBuffer[i] = '\u982C'; break;
		case '\u5861': charBuffer[i] = '\u586B'; break;
		case '\u91AC': charBuffer[i] = '\u91A4'; break;
		case '\u56CA': charBuffer[i] = '\u56A2'; break;
		case '\u91B1': charBuffer[i] = '\u9197'; break;
		case '\u9EB4': charBuffer[i] = '\u9EB9'; break;
		case '\u881F': charBuffer[i] = '\u874B'; break;
		case '\u840A': charBuffer[i] = '\u83B1'; break;
		case '\u7E61': charBuffer[i] = '\u7E4D'; break;
		case '\u4FE0': charBuffer[i] = '\u4FA0'; break;
		case '\u8EC0': charBuffer[i] = '\u8EAF'; break;
		case '\u7E6B': charBuffer[i] = '\u7E4B'; break;
		case '\u9A52': charBuffer[i] = '\u9A28'; break;
		case '\u87EC': charBuffer[i] = '\u8749'; break;
		case '\u7130': charBuffer[i] = '\u7114'; break;
		case '\u8523': charBuffer[i] = '\u848B'; break;
		case '\u5C5B': charBuffer[i] = '\u5C4F'; break;
		case '\u9DD7': charBuffer[i] = '\u9D0E'; break;
		case '\u5699': charBuffer[i] = '\u565B'; break;
		case '\u525D': charBuffer[i] = '\u5265'; break;
		case '\u6414': charBuffer[i] = '\u63BB'; break;
		case '\u7626': charBuffer[i] = '\u75E9'; break;
		case '\u7C1E': charBuffer[i] = '\u7BAA'; break;
		case '\u6451': charBuffer[i] = '\u63B4'; break;
		case '\u555E': charBuffer[i] = '\u5516'; break;
		case '\u6F51': charBuffer[i] = '\u6E8C'; break;
		case '\u7006': charBuffer[i] = '\u6D9C'; break;
		case '\u79B1': charBuffer[i] = '\u7977'; break;
		case '\u9EB5': charBuffer[i] = '\u9EBA'; break;
		case '\u5C62': charBuffer[i] = '\u5C61'; break;
		case '\u985A': charBuffer[i] = '\u985B'; break;
		case '\u6522': charBuffer[i] = '\u6505'; break;
		case '\u688E': charBuffer[i] = '\u688D'; break;
		case '\u7E48': charBuffer[i] = '\u7E66'; break;
		case '\u8141': charBuffer[i] = '\u80FC'; break;
		case '\u9839': charBuffer[i] = '\u983D'; break;
		case '\uF86F': charBuffer[i] = '\u2116'; break;

		case '\u2014': charBuffer[i] = '\u2015'; break;
	    }
	}

	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Extra swap fix byte to char output: ", ConvTable.dumpCharArray(charBuffer));
	}

	return new String(charBuffer);
    }

    // swap correct representation to what Sun expects then pass on conversion
    byte[] stringToByteArray(String source)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting char to byte for extra swap fix", ConvTable.dumpCharArray(source.toCharArray()));
	}

	char[] workBuffer = source.toCharArray();
	for (int i = 0; i < workBuffer.length; ++i)
	{
	    switch (workBuffer[i])
	    {
		case '\u9E78': workBuffer[i] = '\u9E7C'; break;
		case '\u982C': workBuffer[i] = '\u9830'; break;
		case '\u586B': workBuffer[i] = '\u5861'; break;
		case '\u91A4': workBuffer[i] = '\u91AC'; break;
		case '\u56A2': workBuffer[i] = '\u56CA'; break;
		case '\u9197': workBuffer[i] = '\u91B1'; break;
		case '\u9EB9': workBuffer[i] = '\u9EB4'; break;
		case '\u874B': workBuffer[i] = '\u881F'; break;
		case '\u83B1': workBuffer[i] = '\u840A'; break;
		case '\u7E4D': workBuffer[i] = '\u7E61'; break;
		case '\u4FA0': workBuffer[i] = '\u4FE0'; break;
		case '\u8EAF': workBuffer[i] = '\u8EC0'; break;
		case '\u7E4B': workBuffer[i] = '\u7E6B'; break;
		case '\u9A28': workBuffer[i] = '\u9A52'; break;
		case '\u8749': workBuffer[i] = '\u87EC'; break;
		case '\u7114': workBuffer[i] = '\u7130'; break;
		case '\u848B': workBuffer[i] = '\u8523'; break;
		case '\u5C4F': workBuffer[i] = '\u5C5B'; break;
		case '\u9D0E': workBuffer[i] = '\u9DD7'; break;
		case '\u565B': workBuffer[i] = '\u5699'; break;
		case '\u5265': workBuffer[i] = '\u525D'; break;
		case '\u63BB': workBuffer[i] = '\u6414'; break;
		case '\u75E9': workBuffer[i] = '\u7626'; break;
		case '\u7BAA': workBuffer[i] = '\u7C1E'; break;
		case '\u63B4': workBuffer[i] = '\u6451'; break;
		case '\u5516': workBuffer[i] = '\u555E'; break;
		case '\u6E8C': workBuffer[i] = '\u6F51'; break;
		case '\u6D9C': workBuffer[i] = '\u7006'; break;
		case '\u7977': workBuffer[i] = '\u79B1'; break;
		case '\u9EBA': workBuffer[i] = '\u9EB5'; break;
		case '\u5C61': workBuffer[i] = '\u5C62'; break;
		case '\u985B': workBuffer[i] = '\u985A'; break;
		case '\u6505': workBuffer[i] = '\u6522'; break;
		case '\u688D': workBuffer[i] = '\u688E'; break;
		case '\u7E66': workBuffer[i] = '\u7E48'; break;
		case '\u80FC': workBuffer[i] = '\u8141'; break;
		case '\u983D': workBuffer[i] = '\u9839'; break;
		case '\u2116': workBuffer[i] = '\uF86F'; break;

		case '\u2015': workBuffer[i] = '\u2014'; break;
	    }
	}
	return table.stringToByteArray(new String(workBuffer));
    }
}
