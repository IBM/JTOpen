///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ConvTable.java
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Internal class representing a character set conversion table.
**/
abstract class ConvTable
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

  final static char cic_  = '\uFFFF'; // Used for decompression
  final static char ric_  = '\uFFFE'; // Used for decompression
  final static char hbic_ = '\u0000'; // Used for decompression
  final static char pad_  = '\u0000'; // Used for decompression
    
  String encoding_;
  int ccsid_ = -1;
  int bidiStringType_ = BidiStringType.DEFAULT; //@P0A
  
  //@P0A - The highest number of all our supported CCSIDs. There's
  // no point in making the pool larger than it needs to be.
  // We only have a handful of CCSIDs in the 62000 range, so we
  // could use a smaller number to save space and those CCSIDs outside
  // the range just wouldn't get cached. However, 61952 is used
  // extensively, so we might as well max it out.
  private static final int LARGEST_CCSID = 62251;
  private static final ConvTable[] ccsidPool_ = new ConvTable[LARGEST_CCSID+1]; //@P0A

  private static final Hashtable converterPool_ = new Hashtable();
  
  //@E2D static final boolean debug_ = Trace.isTraceOn() && Trace.isTraceConversionOn();
  
  private static final String prefix_ = "com.ibm.as400.access.ConvTable";
  
  
  /**
   * Constructor.
  **/
  ConvTable(int ccsid)
  {
    ccsid_ = ccsid;
    encoding_ = ConversionMaps.ccsidToEncoding(ccsid_); //@E1C
    if (encoding_ == null) encoding_ = ""+ccsid_; //@E4A
    
    if (Trace.traceOn_) //@E2C @P0C
    {
      Trace.log(Trace.CONVERSION, "Constructing conversion table for ccsid/encoding: " + ccsid_ + "/" + encoding_);
      if (ccsid_ == 0) //@E4A - see ConvTableJavaMap
      {
        if (this instanceof ConvTableJavaMap) //@E4A
        {
          Trace.log(Trace.CONVERSION, "This table is a wrapper around a Java table."); //@E4A
        }
        else
        {
          Trace.log(Trace.CONVERSION, "Warning: 0 specified for CCSID when table is not a Java table."); //@E4A
        }
      }
    }
  }
  
  
  /**
   * Perform an OS/400 CCSID to Unicode conversion.
  **/
  abstract String byteArrayToString(byte[] source, int offset, int length, int type);  //@E0C

  
  //@P0A
  // This method can be overridden by subclasses for better performance.
  String byteArrayToString(byte[] source, int offset, int length)
  {
    return byteArrayToString(source, offset, length, bidiStringType_);
  }
  
  
  //@E4A
  /**
   * Helper method used to decompress conversion tables when they are initialized.
   * Note that this method also converts the char[] into a byte[] since these are single-byte tables.
  **/
  byte[] decompressSB(char[] arr, byte subPad)
  {
    byte[] buf = new byte[65536];
    int c = 0;

    for (int i=0; i<arr.length; ++i)
    {
      if (arr[i] == cic_)
      {
        if (arr.length > i+1 && arr[i+1] == pad_)
        {
          buf[c++] = (byte)(arr[i]/256);
          buf[c++] = (byte)(arr[i++]%256);
        }
        else
        {
          long max = (0xFFFF & arr[i+1]*2) + (0xFFFF & c);
          char ch = arr[i+2];
          while (c < max)
          {
            buf[c++] = (byte)(ch/256);
            buf[c++] = (byte)(ch%256);
          }
          i = i + 2;
        }
      }
      else if (arr[i] == ric_)
      {
        if (arr.length > i+1 && arr[i+1] == pad_)
        {
          buf[c++] = (byte)(arr[i]/256);
          buf[c++] = (byte)(arr[i++]%256);
        }
        else
        {
          int start = (0xFFFF & arr[i+2]);
          int num = (0xFFFF & arr[i+1]);
          for (int j=start; j<(num+start); ++j)
          {
            buf[c++] = (byte)(j/256);
            buf[c++] = (byte)(j%256);
          }
          i = i + 2;
        }
      }
      else if (arr[i] == hbic_)
      {
        if (arr.length > i+1 && arr[i+1] == pad_)
        {
          buf[c++] = (byte)(arr[i]/256);
          buf[c++] = (byte)(arr[i++]%256);
        }
        else
        {
          int hbNum = (0x0000FFFF & arr[++i]);
          char firstChar = arr[++i];
          char highByteMask = (char)(0xFF00 & firstChar);
          buf[c++] = (byte)(firstChar/256);
          buf[c++] = (byte)(firstChar%256);
          ++i;
          for (int j=0; j<hbNum; ++j)
          {
            char both = arr[i+j];
            char c1 = (char)(highByteMask + ((0xFF00 & both) >>> 8));
            char c2 = (char)(highByteMask + (0x00FF & both));
            buf[c++] = (byte)(c1/256);
            buf[c++] = (byte)(c1%256);
            buf[c++] = (byte)(c2/256);
            buf[c++] = (byte)(c2%256);
          }
          i = i + hbNum - 1;
        }
      }
      else
      { // regular character
        buf[c++] = (byte)(arr[i]/256);
        buf[c++] = (byte)(arr[i]%256);
      }            
    }
    for (int i=c; i<buf.length; ++i)
    {
      buf[i] = subPad;
    }
    return buf;
  }

  
  /**
   * Convenience function for tracing character strings.
  **/
  static final byte[] dumpCharArray(char[] charArray, int offset, int length) //@G0C
  {
    byte[] retData = new byte[length*2]; //@G0C
    int inPos = offset; //@G0C
    int outPos = 0;
    while(inPos < length)
    {
      retData[outPos++] = (byte)(charArray[inPos] >> 8);
      retData[outPos++] = (byte)charArray[inPos++];
    }
    return retData;
  }

  static final byte[] dumpCharArray(char[] charArray, int numChars) //@G0M
  {
    return dumpCharArray(charArray, 0, numChars); //@G0A
  }

  static final byte[] dumpCharArray(char[] charArray)
  {
    return dumpCharArray(charArray, charArray.length);
  }

  /**
   * Returns the ccsid of this conversion object.
   * @return  the ccsid.
  **/
  int getCcsid()
  {
    //return Integer.parseInt(CcsidEncodingMap.encodingToCcidString(encoding_));
    return ccsid_;
  }


  /**
   * Returns the encoding of this conversion object.
   * @return  the encoding.
  **/
  String getEncoding()
  {
    return encoding_;
  }


  /**
   * Factory method for finding appropriate table based on encoding name.
  **/
  static final ConvTable getTable(String encoding) throws UnsupportedEncodingException
  {
    String className = null;

    if (NLS.forceJavaTables_)
    {
      className = encoding;
    }
    else
    {
      className = prefix_ + ConversionMaps.encodingToCcsidString(encoding); //@E1C
    }
    
    // First, see if we've already loaded the table.
    ConvTable newTable = (ConvTable)converterPool_.get(className);
    if (newTable != null)
    {
      if (Trace.traceOn_) //@E2C @P0C
      {
        Trace.log(Trace.CONVERSION, "Reusing previously loaded conversion table for encoding: "+encoding);
      }
      return newTable;
    }
    
    // If we haven't, then we need to load it now.
    try
    {
      if (NLS.forceJavaTables_) throw new CharConversionException("User set to force loading Java tables.");
      newTable = (ConvTable)Class.forName(className).newInstance();
    }
    catch(Throwable e) //@E5C
    {
      if (Trace.traceOn_) //@E3A @P0C
      {
        Trace.log(Trace.CONVERSION, "Could not load conversion table class for encoding: "+encoding+". Will attempt to let Java do the conversion.", e);
      }
      // Need to load a JavaMap
      className = encoding; //@E3A
      newTable = (ConvTable)converterPool_.get(className); //@E3A
      if (newTable != null) //@E3A
      {
        if (Trace.traceOn_) //@E3A @P0C
        {
          Trace.log(Trace.CONVERSION, "Reusing previously loaded Java conversion table for encoding: "+encoding); //@E3A
        }
        return newTable; //@E3A
      }
      // It's not cached, so we can try to instantiate one.
      newTable = new ConvTableJavaMap(encoding); //@E3A
      
//@E3D      throw new UnsupportedEncodingException();
    }
      
    if(Trace.traceOn_) //@E2C @P0C
    {
      Trace.log(Trace.CONVERSION, "Successfully loaded conversion table for encoding: "+encoding);
    }
    converterPool_.put(className, newTable);
    return newTable;
  }


  /**
   * Factory method for finding appropriate table based on ccsid number.
   * system may be null if no system was provided.
  **/
  static final ConvTable getTable(int ccsid, AS400ImplRemote system) throws UnsupportedEncodingException
  {
    ccsid = ccsid & 0x00FFFF; // Remove sign-extended shorts that JDBC gives us.

    if (ccsid <= LARGEST_CCSID) //@P0A - If it's negative, too bad...
    {
      ConvTable cachedTable = ccsidPool_[ccsid]; //@P0A
      if (cachedTable != null) return cachedTable; //@P0A
    }

    String className = null;
    if (NLS.forceJavaTables_)
    {
      className = ConversionMaps.ccsidToEncoding(ccsid);
      if (className == null) className = "";
    }
    else
    {
      className = prefix_ + String.valueOf(ccsid);
    }
    
    // First, see if we've already loaded the table.
    ConvTable newTable = (ConvTable)converterPool_.get(className);
    if (newTable != null)
    {
      if (Trace.traceOn_) //@E2C @P0C
      {
        Trace.log(Trace.CONVERSION, "Reusing previously loaded conversion table for ccsid: "+ccsid);
      }
      if (ccsid <= LARGEST_CCSID) ccsidPool_[ccsid] = newTable; //@P0A
      return newTable;
    }
    
    // If we haven't, then we need to load it now.
    try
    {
      if (NLS.forceJavaTables_) throw new CharConversionException("User set to force loading Java tables.");
      newTable = (ConvTable)Class.forName(className).newInstance();
    }
    catch(Throwable e) //@E5C
    {
      if (Trace.traceOn_) //@E3A @P0C
      {
        Trace.log(Trace.CONVERSION, "Could not load conversion table class for ccsid: "+ccsid+". Will attempt to let Java do the conversion.", e);
      }
      // Need to load a JavaMap
      className = ConversionMaps.ccsidToEncoding(ccsid); //@E3A
      if (className == null) //@E3A
      {
        if (Trace.traceOn_) //@P0C
        {
          Trace.log(Trace.CONVERSION, "Could not find an encoding that matches ccsid: "+ccsid); //@E3A
        }
        throw new UnsupportedEncodingException("CCSID "+ccsid); //@E3A
      }
      newTable = (ConvTable)converterPool_.get(className); //@E3A
      if (newTable != null) //@E3A
      {
        if (Trace.traceOn_) //@E3A @P0C
        {
          Trace.log(Trace.CONVERSION, "Reusing previously loaded Java conversion table for ccsid: "+ccsid); //@E3A
        }
        if (ccsid <= LARGEST_CCSID) ccsidPool_[ccsid] = newTable; //@P0A
        return newTable; //@E3A
      }
      // It's not cached, so we can try to instantiate one.
      newTable = new ConvTableJavaMap(className); //@E3A
      
//@E3D      throw new UnsupportedEncodingException();
    }
    
    if(Trace.traceOn_) //@E2C @P0C
    {
      Trace.log(Trace.CONVERSION, "Successfully loaded conversion table for ccsid: "+ccsid);
    }
    converterPool_.put(className, newTable); //@E3A
    if (ccsid <= LARGEST_CCSID) ccsidPool_[ccsid] = newTable; //@P0A
    return newTable;
  }


  /**
   * Perform a Unicode to OS/400 CCSID conversion.
  **/
  abstract byte[] stringToByteArray(String source, int type);   //@E0C

  
  //@P0A
  // This method can be overridden by subclasses for better performance.
  byte[] stringToByteArray(String source)
  {
    return stringToByteArray(source, bidiStringType_);
  }

  //@G0A
  // Subclasses should override this to avoid creating superflous String objects and char arrays.
  byte[] stringToByteArray(char[] source, int offset, int length)
  {
    return stringToByteArray(new String(source, offset, length));
  }

  //@P0A
  // Subclasses should override this to avoid creating superfluous byte arrays.
  void stringToByteArray(String source, byte[] buf, int offset) throws CharConversionException
  {
    byte[] b = stringToByteArray(source, bidiStringType_);
    try
    {
      System.arraycopy(b, 0, buf, offset, b.length);
    }
    catch(ArrayIndexOutOfBoundsException aioobe)
    {
      throw new CharConversionException();
    }
  }

  
  //@P0A
  // This method can be overridden by subclasses for better performance.
  void stringToByteArray(String source, byte[] buf, int offset, int length) throws CharConversionException
  {
    stringToByteArray(source, buf, offset, length, bidiStringType_);
  }

  
  //@P0A
  // Subclasses should override this to avoid creating superfluous byte arrays.
  void stringToByteArray(String source, byte[] buf, int offset, int length, int type) throws CharConversionException
  {
    byte[] b = stringToByteArray(source, type);
    if (length > b.length) length = b.length;
    try
    {
      System.arraycopy(b, 0, buf, offset, length);
    }
    catch(ArrayIndexOutOfBoundsException aioobe)
    {
      Trace.log(Trace.CONVERSION, "Source length: "+b.length+"; Source offset: 0; Destination length: "+buf.length+"; Destination offset: "+offset+"; Number of bytes to copy: "+length, aioobe);
      throw new CharConversionException();
    }
  }

}
