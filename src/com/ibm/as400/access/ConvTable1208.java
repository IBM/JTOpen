///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTable1208.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;

class ConvTable1208 extends ConvTable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    ConvTable1208()
    {
        super(1208);
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    final String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        char[] out = new char[length];
        int outCount = 0;
        for (int i=offset; i<offset+length && i < buf.length; ++i)
        {
            int b = buf[i] & 0x00FF;
            int c = 0;
            if ((b & 0x80) == 0x00)
            {
                c = b;
            }
            else if ((b & 0xE0) == 0xC0)
            {
                c = (b & 0x1F) << 6;
                i++; 
                // @J5A check i before using it
                if (i < buf.length) {
                  c |= buf[i] & 0x3F;
                } else {
                  c = dbSubUnic_; 
                }
            }
            else if ((b & 0xF0) == 0xE0)
            {
                c = (b & 0x0F) << 12;
                i++; 
                // @J5A check i before using it
                if (i < buf.length){  
                    c |= (buf[i] & 0x3F) << 6;
                    i++; 
                    // @J5A check i before using it
                    if (i < buf.length){  
                      c |= buf[i] & 0x3F;
                    } else {
                      c = dbSubUnic_; 
                    }
                } else {
                  c = dbSubUnic_; 
                }
            }
            else
      {
        c = (b & 0x07) << 18;
        i++;
        // @J5A check i before using it
        if (i < buf.length) {
          c |= (buf[i] & 0x3F) << 12;
          i++;
          // @J5A check i before using it
          if (i < buf.length) {
            c |= (buf[i] & 0x3F) << 6;
            i++;
            // @J5A check i before using it
            if (i < buf.length) {
              c |= buf[i] & 0x3F;
            } else {
              c = dbSubUnic_;
            }
          } else {
            c = dbSubUnic_;
          }
        } else {
          c = dbSubUnic_;
        }
      }
            // Create surrogate pair if necessary.
            if (c > 0x00FFFF)  // Surrogate pair.
            {
                out[outCount++] = (char)((c - 0x10000) / 0x400 + 0xD800);
                out[outCount++] = (char)((c - 0x10000) % 0x400 + 0xDC00);
            }
            else
            {
                out[outCount++] = (char)c;
            }
        }
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(out));
        return String.copyValueOf(out, 0, outCount);
    }

    // Perform a Unicode to OS/400 CCSID conversion.
    final byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(source.toCharArray(), 0, source.length()));
        int len = source.length();
        byte[] buf = new byte[len * 4];
        int bufCount = 0;
        for (int i = 0; i < len; ++i)
        {
            int c = source.charAt(i) & 0x00FFFF;
            if (c > 0xD7FF && c < 0xDC00)
            {
                if (++i < len)
                {
                    c = (c - 0xD800) * 0x400 + ((source.charAt(i) & 0x00FFFF) - 0xDC00) + 0x10000;
                }
                
                else if (!CharConverter.isFaultTolerantConversion())
                {
                    throw new ArrayIndexOutOfBoundsException();
                }
                else
                {
                    // We're fault tolerant, ignore the high surrogate and just return.
                    byte[] ret = new byte[bufCount];
                    System.arraycopy(buf, 0, ret, 0, bufCount);
                    if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Fault-tolerant in mid-surrogate. Destination byte array for ccsid: " + ccsid_, ret);
                    return ret;
                }
            }
            if (c < 0x80)
            {
                buf[bufCount++] = (byte)c;
            }
            else if (c < 0x800)
            {
                buf[bufCount++] = (byte)(0xC0 | (c >> 6));
                buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
            }
            else if (c < 0x10000)
            {
                buf[bufCount++] = (byte)(0xE0 | (c >> 12));
                buf[bufCount++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
            }
            else
            {
                buf[bufCount++] = (byte)(0xF0 | (c >> 18));
                buf[bufCount++] = (byte)(0x80 | ((c >> 12) & 0x3F));
                buf[bufCount++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
            }
        }
        byte[] ret = new byte[bufCount];
        System.arraycopy(buf, 0, ret, 0, bufCount);

        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, ret);
        return ret;
    }

    public final byte[] stringToByteArray(char[] src, int offset, int length)
    {
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src, offset, length));
        byte[] buf = new byte[src.length * 4];
        int bufCount = 0;
        int len = offset + length;
        for (int i = offset; i < len; ++i)
        {
            int c = src[i] & 0x00FFFF;
            if (c > 0xD7FF && c < 0xDC00)
            {
                if (++i < len)
                {
                    c = (c - 0xD800) * 0x400 + ((src[i] & 0x00FFFF) - 0xDC00) + 0x10000;
                }
                else if (!CharConverter.isFaultTolerantConversion())
                {
                    throw new ArrayIndexOutOfBoundsException();
                }
                else
                {
                    // We're fault tolerant, ignore the high surrogate and just return.
                    byte[] ret = new byte[bufCount];
                    System.arraycopy(buf, 0, ret, 0, bufCount);
                    if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Fault-tolerant in mid-surrogate. Destination byte array for ccsid: " + ccsid_, ret);
                    return ret;
                }
            }
            if (c < 0x80)
            {
                buf[bufCount++] = (byte)c;
            }
            else if (c < 0x800)
            {
                buf[bufCount++] = (byte)(0xC0 | (c >> 6));
                buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
            }
            else if (c < 0x10000)
            {
                buf[bufCount++] = (byte)(0xE0 | (c >> 12));
                buf[bufCount++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
            }
            else
            {
                buf[bufCount++] = (byte)(0xF0 | (c >> 18));
                buf[bufCount++] = (byte)(0x80 | ((c >> 12) & 0x3F));
                buf[bufCount++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
            }
        }
        byte[] ret = new byte[bufCount];
        System.arraycopy(buf, 0, ret, 0, bufCount);

        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, ret);
        return ret;
    }

    public final void stringToByteArray(String source, byte[] buf, int offset) throws CharConversionException
    {
        int bufCount = offset;
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(source.toCharArray()));
        try
        {
            int len = source.length();
            for (int i = 0; i < len; ++i)
            {
                int c = source.charAt(i) & 0x00FFFF;
                if (c > 0xD7FF && c < 0xDC00)
                {
                    if (++i < len)
                    {
                        c = (c - 0xD800) * 0x400 + ((source.charAt(i) & 0x00FFFF) - 0xDC00) + 0x10000;
                    }
                    else if (!CharConverter.isFaultTolerantConversion())
                    {
                        throw new CharConversionException();
                    }
                    else
                    {
                        // We're fault tolerant, ignore the high surrogate and just return.
                        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Fault-tolerant in mid-surrogate. Destination byte array for ccsid: " + ccsid_, buf, offset, bufCount - offset);
                        return;
                    }
                }
                if (c < 0x80)
                {
                    buf[bufCount++] = (byte)c;
                }
                else if (c < 0x800)
                {
                    buf[bufCount++] = (byte)(0xC0 | (c >> 6));
                    buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
                }
                else if (c < 0x10000)
                {
                    buf[bufCount++] = (byte)(0xE0 | (c >> 12));
                    buf[bufCount++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                    buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
                }
                else
                {
                    buf[bufCount++] = (byte)(0xF0 | (c >> 18));
                    buf[bufCount++] = (byte)(0x80 | ((c >> 12) & 0x3F));
                    buf[bufCount++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                    buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, bufCount - offset);
    }

    public final void stringToByteArray(String source, byte[] buf, int offset, int length) throws CharConversionException {
      stringToByteArrayTruncation(source, buf, offset, length); 
    }
    /* detected truncation  @H2C*/ 
    final int stringToByteArrayTruncation(String source, byte[] buf, int offset, int length) throws CharConversionException
    {
        int truncated = 0;                      /*@H2A*/ 
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(source.toCharArray()));
        try
        {
            int len = source.length();
            int bufCount = offset;
            int max = offset+length;
            for (int i = 0; i < len ; ++i)           /*@H2C*/
            {
                int c = source.charAt(i) & 0x00FFFF;
                if (c > 0xD7FF && c < 0xDC00)
                {
                    if (++i < len)
                    {
                        c = (c - 0xD800) * 0x400 + ((source.charAt(i) & 0x00FFFF) - 0xDC00) + 0x10000;
                    }
                    else if (!CharConverter.isFaultTolerantConversion())
                    {
                        throw new CharConversionException();
                    }
                    else
                    {
                        // We're fault tolerant, ignore the high surrogate and just return.
                        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Fault-tolerant in mid-surrogate. Destination byte array for ccsid: " + ccsid_, buf, offset, length);
                        return truncated ;   /*@H2A*/
                    }
                }
                if (c < 0x80)
                {
                    if (bufCount < max) {              /*@H2A*/
                      buf[bufCount++] = (byte)c;
                    } else {
                      // Don't report the truncation of spaces @H7A
                      if (c == ' ') {
                         // No need to count truncation 
                      } else {
                         truncated ++;
                      }
                    }
                }
                else if (c < 0x800)
                {
                  if (bufCount < max) {                /*@H2A*/ 
                    buf[bufCount++] = (byte)(0xC0 | (c >> 6));
                  } else {
                    truncated ++; 
                  }
                    
                    if (bufCount < max) {
                      buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
                    } else {
                      truncated ++;                      /*@H2A*/
                    }
                }
                else if (c < 0x10000)
                {
          if (bufCount < max) {
            buf[bufCount++] = (byte) (0xE0 | (c >> 12));
          } else {
            truncated++;                         /*@H2A*/
          }
          if (bufCount < max) {
            buf[bufCount++] = (byte) (0x80 | ((c >> 6) & 0x3F));
          } else {
            truncated++;                         /*@H2A*/
          }
          if (bufCount < max) {
            buf[bufCount++] = (byte) (0x80 | (c & 0x3F));
          } else {
            truncated++;                        /*@H2A*/
          }
                }
                else
                {
                  if (bufCount < max) {
                    buf[bufCount++] = (byte)(0xF0 | (c >> 18));
                  } else {
                    truncated++;        /*@H2A*/
                  }
                    if (bufCount < max) { buf[bufCount++] = (byte)(0x80 | ((c >> 12) & 0x3F));
                    } else {
                      truncated++;         /*@H2A*/
                    }
                    if (bufCount < max) { buf[bufCount++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                    } else {
                      truncated++;            /*@H2A*/
                    }
                    if (bufCount < max) { buf[bufCount++] = (byte)(0x80 | (c & 0x3F));
                    } else {
                      truncated++;            /*@H2A*/
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            throw new CharConversionException();
        }
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, buf, offset, length);
        return truncated;             /*@H2A*/ 
    }

    /**
     * Place the string into the specified buffer, beginning at offset for length. 
     * This returns the number of bytes that did not fit (i.e. number of bytes truncated). 
     * @param source  String to convert
     * @param buf     output buffer
     * @param offset  offset in buffer to put information
     * @param length  maximum number of bytes to add to the buffer
     * @param properties  BidiConversionProperties
     * @return  number of bytes that were truncated 
     * @throws CharConversionException  If a character conversion error occurs.
     */

    public final int stringToByteArray(String source, byte[] buf, int offset, int length, BidiConversionProperties properties) throws CharConversionException
    {
        // Don't have a Bidi string type for UTF-8.
        return stringToByteArrayTruncation(source, buf, offset, length); /*@H2C*/
    }
    
    
    
    
    // Scan the data.  If valid return length, otherwise fixup and return the changed length, 
    // padding with spaces as needed. 
    // @X4A
    public int validateData( byte[] buf, int offset, int length) {
      int endOffset = offset+length;
      int previousCharOffset = offset; 
      int nextCharOffset = offset; 
      while (nextCharOffset < endOffset) { 
          previousCharOffset = nextCharOffset; 
          int b = 0xFF & buf[nextCharOffset]; 
          if (b < 0x80) { 
            nextCharOffset++;
            
          } else if( b >= 0xC0 && b < 0xE0) {  // For two bytes, the first byte is 110xxxxx 
            nextCharOffset += 2; 
          } else if (b >= 0xE0  && b < 0xF0 ) { // For three bytes, the first byte is1110xxxx
            nextCharOffset += 3; 
          } else {
            nextCharOffset += 4; 
          }
      } 
      if (nextCharOffset > endOffset) {
        // The previous character is incomplete 
        length = previousCharOffset - offset; 
        for (int i = previousCharOffset; i < endOffset; i++) {
          buf[i] = ' '; 
        }
      }
        
      return length; 
    }

}
