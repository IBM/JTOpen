///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConvTableJavaMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayOutputStream;
import java.io.CharConversionException; //@P0A
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * This is the class representing a Java converter table. Each instance
 * of this class handles an encoding that the Toolbox does not support and
 * Java does. Hence, multiple instances of this class could be cached in
 * the converter pool.
**/
class ConvTableJavaMap extends ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  
  InputStreamReader reader_; // The stream that does the actual conversion
  OutputStreamWriter writer_; // The stream that does the actual conversion
  ByteArrayOutputStream outBuffer_;
  ConvTableInputStream inBuffer_;  

  
  /**
   * Constructor.
  **/
  ConvTableJavaMap(String encoding) throws UnsupportedEncodingException
  {
    super(0); // Just for compatibility
    encoding_ = encoding;
    String ccsid = ConversionMaps.encodingToCcsidString(encoding);
    try
    {
      ccsid_ = (new Integer(ccsid)).intValue();
    }
    catch(Exception e)
    {
      if (Trace.traceOn_) //@P0A
      {
      Trace.log(Trace.CONVERSION, "No associated CCSID for encoding '"+encoding+"'. Lookup returned "+ccsid+".");
      }
      ccsid_ = 0;
    }

//@B0D    try
//@B0D    {
      inBuffer_ = new ConvTableInputStream();
      outBuffer_ = new ByteArrayOutputStream();
//@B0D      writer_ = new OutputStreamWriter(outBuffer_, encoding_);
//@B0D      reader_ = new InputStreamReader(inBuffer_, encoding_);
//@B0D    }
//@B0D    catch(IOException e)
//@B0D    {
//@B0D      Trace.log(Trace.ERROR, "Caught IOException while instantiating converter table for encoding: "+encoding, e);
//@B0D      throw new UnsupportedEncodingException();
//@B0D    }
      
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Successfully loaded Java map for encoding: " + encoding_);
    }
  }
  
  
  /**
   * Let Java perform an Encoding to Unicode conversion.
  **/
  final String byteArrayToString(byte[] buf, int offset, int length, int type) //@P0C
  {
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Converting byte array to string for encoding: " + encoding_, buf, offset, length);
    }
    char[] dest = new char[length];
    int count = 0;
    synchronized(inBuffer_)
    {
      inBuffer_.setContents(buf, offset, length);
      try
      {
        if (reader_ == null) //@B0A
        {
          reader_ = new InputStreamReader(inBuffer_, encoding_); //@B0A
        }
        count = reader_.read(dest);
      }
      catch(IOException e)
      {
        if (Trace.traceOn_) //@P0C
        {
          Trace.log(Trace.ERROR, "IOException occurred on byteArrayToString for encoding "+encoding_, e);
        }
        reader_ = null; //@B0A
      }
      if (Trace.traceOn_) //@P0C
      {
        Trace.log(Trace.CONVERSION, "Destination string for encoding: " + encoding_ + " ("+count+")", ConvTable.dumpCharArray(dest));
      }
    }
    return String.copyValueOf(dest, 0, count);
  }
    
  
  /**
   * Let Java perform a Unicode to Encoding conversion.
  **/
  final byte[] stringToByteArray(String source, int type) //@P0C
  {
    if (Trace.traceOn_) //@P0C
    {
      Trace.log(Trace.CONVERSION, "Converting string to byte array for encoding: " + encoding_, ConvTable.dumpCharArray(source.toCharArray()));
    }
    byte[] ret = null;
    synchronized(outBuffer_)
    {
      try
      {
        outBuffer_.reset();
        if (writer_ == null) //@B0A
        {
          writer_ = new OutputStreamWriter(outBuffer_, encoding_); //@B0A
        }
        writer_.write(source);
        writer_.flush();
      }
      catch(IOException e)
      {
        if (Trace.traceOn_) //@P0C
        {
          Trace.log(Trace.ERROR, "IOException occurred on stringToByteArray for encoding "+encoding_, e);
        }
        writer_ = null; //@B0A
      }
      ret = outBuffer_.toByteArray();
      if (Trace.traceOn_) //@P0C
      {
        Trace.log(Trace.CONVERSION, "Destination byte array for encoding: " + encoding_, ret);
      }
    }
    return ret;
  }
}


