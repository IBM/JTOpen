///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConverterImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

class ConverterImplRemote implements ConverterImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private static String getCopyright()
    {
	return Copyright.copyright;
    }

    private static Hashtable pool = new Hashtable();  // Keep a pool of Converter Objects

    static ConverterImplRemote getConverter(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
    {
	String ccsidstr = String.valueOf(ccsid);
	ConvTable table = (ConvTable)pool.get(ccsidstr);  // look in pool for object
	if (table != null)  // if object in pool
	{
	    return new ConverterImplRemote(table);
	}
	table = ConvTable.getTable(ccsid, system);  // else construct new object
	pool.put(ccsidstr, table); // and put it in pool
	return new ConverterImplRemote(table);
    }

    ConvTable table;

    ConverterImplRemote()
    {
    }

    ConverterImplRemote(ConvTable table)
    {
	this.table = table;
    }

    public void setEncoding(String encoding) throws UnsupportedEncodingException
    {
	String ccsidstr = CcsidEncodingMap.encodingToCcidString(encoding);
	if (ccsidstr == null)
	{
	    table = ConvTable.getTable(encoding);  // try, in case this is a new encoding we don't know about yet, otherwise this line will throw the UnsupportedEncodingException
	    return;
	}
	table = (ConvTable)pool.get(ccsidstr);  // look in pool for object
	if (table != null)  // if object in pool
	{
	    return;
	}
	table = ConvTable.getTable(encoding);  // else construct new object
	pool.put(ccsidstr, table); // and put it in pool
    }

    public void setCcsid(int ccsid, AS400Impl system) throws UnsupportedEncodingException
    {
	String ccsidstr = String.valueOf(ccsid);
	table = (ConvTable)pool.get(ccsidstr);  // look in pool for object
	if (table != null)  // if object in pool
	{
	    return;
	}
	table = ConvTable.getTable(ccsid, (AS400ImplRemote)system);  // else construct new object
	pool.put(ccsidstr, table); // and put it in pool
    }

    public String getEncoding()
    {
	return table.getEncoding();
    }

    public int getCcsid()
    {
	return table.getCcsid();
    }
    String byteArrayToString(byte[] source)
    {
	return byteArrayToString(source, 0, source.length);
    }

    String byteArrayToString(byte[] source, int offset)
    {
	return byteArrayToString(source, offset, source.length-offset);
    }


    public String byteArrayToString(byte[] source, int offset, int length)
    {
	return table.byteArrayToString(source, offset, length);
    }

    public byte[] stringToByteArray(String source)
    {
	return table.stringToByteArray(source);
    }

    void stringToByteArray(String source, byte[] destination) throws CharConversionException
    {
	byte[] convertedBytes = stringToByteArray(source);
	if (convertedBytes.length > destination.length)
	{
	    // Copy as much as will fit
	    System.arraycopy(convertedBytes, 0, destination, 0, destination.length);
	    throw new CharConversionException();
	}
	System.arraycopy(convertedBytes, 0, destination, 0, convertedBytes.length);
    }

    void stringToByteArray(String source, byte[] destination, int offset) throws CharConversionException
    {
	byte[] convertedBytes = stringToByteArray(source);
	if (convertedBytes.length > destination.length - offset)
	{
	    // Copy as much as will fit
	    System.arraycopy(convertedBytes, 0, destination, offset, destination.length - offset);
	    throw new CharConversionException();
	}
	System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }

    // Converts the specified String into bytes.
    // @param  source  the String to convert.
    // @param  destination  the destination byte array.
    // @param  offset  the offset into the destination array for the start of the data.
    // @param  length  the length of data to write into the array.
    // @exception  CharConversionException  If destination is not large enough to hold the converted string.
    void stringToByteArray(String source, byte[] destination, int offset, int length) throws CharConversionException
    {
	byte[] convertedBytes = stringToByteArray(source);
	if (convertedBytes.length > length)
	{
	    // Copy as much as will fit
	    System.arraycopy(convertedBytes, 0, destination, offset, length);
	    throw new CharConversionException();
	}
	System.arraycopy(convertedBytes, 0, destination, offset, convertedBytes.length);
    }
}
