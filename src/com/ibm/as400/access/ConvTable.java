///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: ConvTable.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

// internal class representing a character set conversion table
class ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    static boolean convDebug = Trace.isTraceOn();  // set tracing flag at class load time
    // Convenience function for tracing of Strings
    static byte[] dumpCharArray(char[] charArray)
    {
	byte[] retData = new byte[charArray.length*2];
	int inPos = 0;
	int outPos = 0;
	while (inPos < charArray.length)
	{
	    retData[outPos++] = (byte)(charArray[inPos] >> 8);
	    retData[outPos++] = (byte)charArray[inPos++];
	}
	return retData;
    }

    private static Hashtable handBuilt = new Hashtable(20);  // list of non-java tables
    static
    {
        // build list of non-java tables
	handBuilt.put("290",   "com.ibm.as400.access.ConvTable290");
	handBuilt.put("300",   "com.ibm.as400.access.ConvTable300");
	handBuilt.put("423",   "com.ibm.as400.access.ConvTable423");
	handBuilt.put("833",   "com.ibm.as400.access.ConvTable833");
	handBuilt.put("834",   "com.ibm.as400.access.ConvTable834");

	handBuilt.put("835",   "com.ibm.as400.access.ConvTable835");
	handBuilt.put("836",   "com.ibm.as400.access.ConvTable836");
	handBuilt.put("837",   "com.ibm.as400.access.ConvTable837");
	handBuilt.put("880",   "com.ibm.as400.access.ConvTable880");
	handBuilt.put("1027",  "com.ibm.as400.access.ConvTable1027");

	handBuilt.put("1130",  "com.ibm.as400.access.ConvTable1130");
	handBuilt.put("1132",  "com.ibm.as400.access.ConvTable1132");
	handBuilt.put("1388",  "com.ibm.as400.access.ConvTable1388");
	handBuilt.put("4396",  "com.ibm.as400.access.ConvTable4396");
	handBuilt.put("4933",  "com.ibm.as400.access.ConvTable4933");

	handBuilt.put("5026",  "com.ibm.as400.access.ConvTable5026");
	handBuilt.put("5035",  "com.ibm.as400.access.ConvTable5035");
	handBuilt.put("13488", "com.ibm.as400.access.ConvTable13488");
	handBuilt.put("28709", "com.ibm.as400.access.ConvTable28709");
	handBuilt.put("61952", "com.ibm.as400.access.ConvTable61952");
    }

    // factory for finding appropriate table based on encoding name
    static ConvTable getTable(String encoding) throws UnsupportedEncodingException
    {
	// Check our list of tables
	String ourTable = (String)handBuilt.get(encoding);
	if (ourTable == null)
	{
	    // Check in Java's list of tables
	    return new ConvTable(encoding); // will throw UnsupportedEncodingException
	}

	try
	{
	    return (ConvTable)Class.forName(ourTable).newInstance();
	}
	catch (ClassNotFoundException e1)
	{
	    Trace.log(Trace.ERROR, "Unexpected ClassNotFoundException" + ourTable, e1);
	    throw new UnsupportedEncodingException();
	}
	catch (IllegalAccessException e2)
	{
	    Trace.log(Trace.ERROR, "Unexpected IllegalAccessException" + ourTable, e2);
	    throw new UnsupportedEncodingException();
	}
	catch (InstantiationException e3)
	{
	    Trace.log(Trace.ERROR, "Unexpected InstantiationException" + ourTable, e3);
	    throw new UnsupportedEncodingException();
	}
    }

    // factory for finding appropriate table based on ccsid number, system may be null if no system was provided
    static ConvTable getTable(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
    {
	if (ccsid == 930 || ccsid == 939)
	{
	    // Need to swap some characters for Japanese conversions
	    return new ConvTableSwapFix(ccsid, system);
	}
	else
	{
	    // No swaps necessary
	    return getTableNoSwapChars(ccsid, system);
	}
    }

    // factory for finding appropriate table based on ccsid number, system may be null if no system was provided
    static ConvTable getTableNoSwapChars(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
    {
	// Check if we know the corresponding encoding name
	String encoding = CcsidEncodingMap.ccsidToEncoding(ccsid);
	if (encoding == null)
	{
	    if (system == null)
	    {
		throw new UnsupportedEncodingException();
	    }
	    // Download table from system
	    return new ConvTableDownload(ccsid, system);
	}

	// Check our list of tables
	String ourTable = (String)handBuilt.get(encoding);
	if (ourTable == null)
	{
	    try
	    {
		// use Java's table
		return new ConvTable(encoding);
	    }
	    catch (UnsupportedEncodingException encodingFailure)
	    {
		// If we have caught an exception here we are dealing with a JVM that did not ship all of the encoding tables that ship with Sun's JDK
		Trace.log(Trace.CONVERSION, "Attempting to download table for missing JDK encoding: " + encoding + " from system: " + system);
		if (system != null)
		{
		    try
		    {
		        // If double byte use our redundant tables
			if (ccsid == 930)
			{
			    ConvTableRedundantDB rdbTable = (ConvTableRedundantDB)Class.forName("com.ibm.as400.access.ConvTable930").newInstance();
			    // give the table the single byte portion to download
			    rdbTable.setCcsidAndSystem(290, system);
			    return rdbTable;
			}
			if (ccsid == 933)
			{
			    ConvTableRedundantDB rdbTable = (ConvTableRedundantDB)Class.forName("com.ibm.as400.access.ConvTable933").newInstance();
			    // give the table the single byte portion to download
			    rdbTable.setCcsidAndSystem(833, system);
			    return rdbTable;
			}
			if (ccsid == 935)
			{
			    return (ConvTable)Class.forName("com.ibm.as400.access.ConvTable1388").newInstance();
			}
			if (ccsid == 937)
			{
			    ConvTableRedundantDB rdbTable = (ConvTableRedundantDB)Class.forName("com.ibm.as400.access.ConvTable937").newInstance();
			    // give the table the single byte portion to download
			    rdbTable.setCcsidAndSystem(37, system);
			    return rdbTable;
			}
			if (ccsid == 939)
			{
			    ConvTableRedundantDB rdbTable = (ConvTableRedundantDB)Class.forName("com.ibm.as400.access.ConvTable930").newInstance();
			    // give the table the single byte portion to download
			    rdbTable.setCcsidAndSystem(1027, system);
			    return rdbTable;
			}
			// Try to download the table for single byte
			return new ConvTableDownload(ccsid, system);
		    }
		    catch (ClassNotFoundException e1)
		    {
			Trace.log(Trace.ERROR, "Unexpected ClassNotFoundException" + ourTable, e1);
			throw (UnsupportedEncodingException)encodingFailure.fillInStackTrace();
		    }
		    catch (IllegalAccessException e2)
		    {
			Trace.log(Trace.ERROR, "Unexpected IllegalAccessException" + ourTable, e2);
			throw (UnsupportedEncodingException)encodingFailure.fillInStackTrace();
		    }
		    catch (InstantiationException e3)
		    {
			Trace.log(Trace.ERROR, "Unexpected InstantiationException" + ourTable, e3);
			throw (UnsupportedEncodingException)encodingFailure.fillInStackTrace();
		    }
		}
		else
		{
		    throw (UnsupportedEncodingException)encodingFailure.fillInStackTrace();
		}
	    }
	}

	try
	{
	    switch (ccsid)
	    {
		case 290:
		case 300:
		case 833:
		case 834:
		case 835:
		case 836:
		case 837:
		case 1027:
		case 4396:
		case 5026:
		case 5035:
		case 28709:
	            // if single-byte or double-byte portions of mixed-byte tables, may need 400 object
		    ConvTableRedundant rTable = (ConvTableRedundant)Class.forName(ourTable).newInstance();
		    rTable.setSystem(system);
		    return rTable;
		default:
		    return (ConvTable)Class.forName(ourTable).newInstance();
	    }
	}
	catch (ClassNotFoundException e1)
	{
	    Trace.log(Trace.ERROR, "Unexpected ClassNotFoundException" + ourTable, e1);
	    throw new UnsupportedEncodingException();
	}
	catch (IllegalAccessException e2)
	{
	    Trace.log(Trace.ERROR, "Unexpected IllegalAccessException" + ourTable, e2);
	    throw new UnsupportedEncodingException();
	}
	catch (InstantiationException e3)
	{
	    Trace.log(Trace.ERROR, "Unexpected InstantiationException" + ourTable, e3);
	    throw new UnsupportedEncodingException();
	}
    }

    // For subclass use only
    ConvTable()
    {
    }

    private Hashtable inPool_ = new Hashtable();
    private Hashtable outPool_ = new Hashtable();

    private String encoding;

    // Construct a conversion object using a Java table
    ConvTable(String encoding) throws UnsupportedEncodingException
    {
	if (ConvTable.convDebug) Trace.log(Trace.CONVERSION, "Constructing Conversion Table for encoding: " + encoding);
	this.encoding = encoding;

	// Perform conversions using streams for performance.  Avoid using the java.lang.String methods.  The String method do not hold on to the internal Java conversion table.
        // Add one entry each to the pools.  This will help to validate the encoding at ConvTable construction time.
	Thread currentThread = Thread.currentThread();

	ConvTableInputStream inBytes = new ConvTableInputStream();
	inPool_.put(currentThread, new Object[] { inBytes, new InputStreamReader(inBytes, encoding) } );

	ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
	outPool_.put(currentThread, new Object[] { outBytes, new OutputStreamWriter(outBytes, encoding) } );
    }

    // Returns the ccsid of this conversion object.
    // @return  the ccsid.
    int getCcsid()
    {
	return Integer.parseInt(CcsidEncodingMap.encodingToCcidString(encoding));
    }

    // Returns the encoding of this conversion object.
    // @return  the encoding.
    String getEncoding()
    {
	return encoding;
    }

    // Perform a AS/400 CCSID to Unicode conversion
    String byteArrayToString(byte[] source, int offset, int length)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting byte to char for encoding: " + encoding, source, offset, length);
	}

        // Get new objects from the pool.  This pool stores an array for each thread.  If such an array has been created previously, then create it now.  By keying off of the thread, we reduce the need for synchronization.  The array for each thread contains 2 objects: the stream and the reader/writer.
	ConvTableInputStream inBytes;
	InputStreamReader inReader;
	Thread currentThread = Thread.currentThread();
	Object[] inStreams = (Object[])inPool_.get(currentThread);
	if (inStreams == null)
	{
	    // We need to create a new set of objects.  Lets first check to see if any threads have died and use their objects.  This also alleviates the memory leak that results when threads die, since they don't clean up after themselves.
	    synchronized(inPool_)
	    {
		Enumeration keys = inPool_.keys();
		while((keys.hasMoreElements()) && (inStreams == null))
		{
		    Thread t = (Thread)keys.nextElement();
		    if (!t.isAlive())
		    {
			inStreams = (Object[])inPool_.get(t);
			inPool_.remove(t);
			inPool_.put(currentThread, inStreams);
		    }
		}
	    }

	    if (inStreams == null)
	    {
		inBytes  = new ConvTableInputStream();
		try
		{
		    inReader = new InputStreamReader(inBytes, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
		    if (convDebug) Trace.log(Trace.ERROR, "Unsupported encoding in ConvTable", e);
		    throw new InternalErrorException(InternalErrorException.UNKNOWN);
		}
		inStreams = new Object[] { inBytes, inReader };
		inPool_.put(currentThread, inStreams);
	    }
	}

	inBytes  = (ConvTableInputStream)inStreams[0];
	inReader = (InputStreamReader)inStreams[1];

	// put bytes into stream
	inBytes.add(source, offset, length);

	char[] charBuffer = new char[length];  // at most one char for every byte
	int charCount;
	try
	{
	    // read the characters from the stream
	    charCount = inReader.read(charBuffer, 0, charBuffer.length);
	}
	catch (IOException e)
	{
	    Trace.log(Trace.ERROR, "Error reading from stream", e);
	    throw new ExtendedIllegalArgumentException("source", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}
	if (charCount == -1 && length != 0)
	{
	    Trace.log(Trace.ERROR, "Error eof returned from stream");
	    throw new ExtendedIllegalArgumentException("source", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}

	if (ConvTable.convDebug)
	{
	    char[] dumpChars = new char[charCount];
	    System.arraycopy(charBuffer, 0, dumpChars, 0, charCount);
	    Trace.log(Trace.CONVERSION, "Byte to char output: ", ConvTable.dumpCharArray(dumpChars));
	}

	// build a String with just the characters that we're actually used
	return new String(charBuffer, 0, charCount);
    }

    // Perform a Unicode to AS/400 CCSID conversion
    byte[] stringToByteArray(String source)
    {
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "Converting char to byte for encoding: " + encoding, ConvTable.dumpCharArray(source.toCharArray()));
	}

        // Get new objects from the pool.  This pool stores an array for each thread.  If such an array has been created previously, then create it now.  By keying off of the thread, we reduce the need for synchronization.  The array for each thread contains 2 objects: the stream and the reader/writer.
	ByteArrayOutputStream outBytes;
	OutputStreamWriter outWriter;
	Thread currentThread = Thread.currentThread();
	Object[] outStreams = (Object[])outPool_.get(currentThread);
	if (outStreams == null)
	{
	    // We need to create a new set of objects.  Lets first check to see if  any threads have died and use their objects.  This also alleviates the memory leak that results when threads die, since they don't clean up after themselves.
	    synchronized(outPool_)
	    {
		Enumeration keys = outPool_.keys();
		while((keys.hasMoreElements()) && (outStreams == null))
		{
		    Thread t = (Thread)keys.nextElement();
		    if (!t.isAlive())
		    {
			outStreams = (Object[])outPool_.get(t);
			outPool_.remove(t);
			outPool_.put(currentThread, outStreams);
		    }
		}
	    }

	    if (outStreams == null)
	    {
		outBytes  = new ByteArrayOutputStream();
		try
		{
		    outWriter = new OutputStreamWriter(outBytes, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
		    if (convDebug) Trace.log(Trace.ERROR, "Unsupported encoding in ConvTable", e);
		    throw new InternalErrorException(InternalErrorException.UNKNOWN);
		}
		outStreams = new Object[] { outBytes, outWriter };
		outPool_.put(currentThread, outStreams);
	    }
	}

	outBytes  = (ByteArrayOutputStream)outStreams[0];
	outWriter = (OutputStreamWriter)outStreams[1];

	try
	{
	    // write the bytes to the stream
	    outWriter.write(source, 0, source.length());
	    outWriter.flush();
	}
	catch (IOException e)
	{
	    Trace.log(Trace.ERROR, "Error writing to outputStream", e);
	    throw new ExtendedIllegalArgumentException("source", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	}

	// get the bytes from the stream
	byte[] output = outBytes.toByteArray();
	outBytes.reset();
	if (ConvTable.convDebug)
	{
	    Trace.log(Trace.CONVERSION, "char to byte output: ", output);
	}
	return output;
    }
}
