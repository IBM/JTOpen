///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableMixedMap.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

/** This is the parent class for all ConvTableXXX classes that represent mixed-byte ccsids.
 * 
 */
public abstract class ConvTableMixedMap extends ConvTable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";


    public ConvTableSingleMap sbTable_ = null; // The single-byte portion of this mixed-byte table.
    public ConvTableDoubleMap dbTable_ = null; // The double-byte portion of this mixed-byte table.


    static final byte shiftOut_ = 0x0E; // Byte used to shift-out of single byte mode.
    static final byte shiftIn_ = 0x0F;  // Byte used to shift-in to single byte mode.
    static final byte ebcdicSpace_ = 0x40;  
    
    // Constructor.
    ConvTableMixedMap(int ccsid, int sbCcsid, int dbCcsid) throws UnsupportedEncodingException
    {
        super(ccsid);
        sbTable_ = (ConvTableSingleMap)ConvTable.getTable(sbCcsid, null);
        dbTable_ = (ConvTableDoubleMap)ConvTable.getTable(dbCcsid, null);
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Successfully loaded mixed-byte map for ccsid: " + ccsid_);
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    final String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        char[] dest = new char[length];
        boolean inSBMode = true;

        int destPos = 0;

        for (int srcPos = offset; srcPos < offset + length; ++srcPos)
        {
            byte curByte = buf[srcPos];
            if (inSBMode)
            {
                // In single byte mode.
                if (curByte == shiftOut_)
                {
                    // Shift out character. Switch to double byte mode.
                    inSBMode = false;
                }
                else
                {
                    // Normal character. Perform single-byte lookup.
                    dest[destPos++] = sbTable_.toUnicode_[(0x00FF & curByte)];
                }
            }
            else
            {
                // In double byte mode.
                if (curByte == shiftIn_)
                {
                    // Shift in character. Switch to single byte mode.
                    inSBMode = true;
                }
                else
                {
                    try
                    {
                        if ((srcPos+1) < offset + length) { 
                           // Normal character. Perform double-byte lookup.
                           int unicodeLength = dbTable_.toUnicode(dest, destPos, ((0x00FF & curByte) << 8) + (0x00FF & buf[++srcPos])); /*@KDC*/
                           destPos += unicodeLength; 
                           // dest[destPos++] = dbTable_.toUnicode_[((0x00FF & curByte) << 8) + (0x00FF & buf[++srcPos])];
                        } else { 
                          
                           if (CharConverter.isFaultTolerantConversion()) {
                             // Do not add the replacement character to behave as before (when ArrayIndexOutOfBoundsException used to be caught). 
                           } else { 
                             // Only half a character at the end -- add substitution character 
                             dest[destPos++] = '\ufffd';
                           }
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException aioobe)
                    {
                        // Swallow this if we are doing fault-tolerant conversion.
                        if(!CharConverter.isFaultTolerantConversion())
                        {
                            throw aioobe;
                        }
                        // No need to decrement because increment in dest[destPost++] not longer occurs 
                        // --destPos;
                    }
                }
            }
        }

        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_ + ": 0," + destPos, ConvTable.dumpCharArray(dest));
        return String.copyValueOf(dest, 0, destPos);
    }

    // Perform a Unicode to OS/400 CCSID conversion.
    final byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        char[] src = source.toCharArray();
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        // Max possible length for mixed-byte byte array.
        byte[] dest = new byte[(src.length * 5 + 3) / 2];

        boolean inSBMode = true;

        byte sbLookup = 0x00;
        char dbLookup = '\u0000';
        int[] increment = new int[1];   /*@KDA*/
        int destPos = 0;                /*@KDA*/
        for (int srcPos = 0; srcPos < src.length; ++srcPos)
        {
            char curChar = src[srcPos];

            // There is no concept of mode context when converting a String to a byte array.
            // The table we look at first is only based on the current character in the String.
            if ((curChar & 0xFF00) == 0x0000 || curChar == euro_)
            {
                // Use single-byte table first.
                sbLookup = sbTable_.fromUnicode_[curChar];
                if (sbLookup == sbSubChar_ && curChar != sbSubUnic_)
                {
                    // Character wasn't in single-byte table. Check double-byte table next.
                    dbLookup = dbTable_.fromUnicode(src,srcPos,increment);
                    if (increment[0] == 2) {
                      // move an extra byte in the source @KDA 
                      srcPos++; 
                    }
                    // dbLookup = dbTable_.fromUnicode_[curChar]; @KDC
                    if (dbLookup == dbSubChar_)
                    {
                        // Character wasn't in the double-byte table either, so use single-byte substitution character.
                        if (!inSBMode)
                        {
                            inSBMode = true;
                            dest[destPos++] = shiftIn_;
                        }
                        dest[destPos++] = sbSubChar_;
                    }
                    else
                    {
                        // Character found in double-byte table.
                        if (inSBMode)
                        {
                            inSBMode = false;
                            dest[destPos++] = shiftOut_;
                        }
                        dest[destPos++] = (byte)((0xFFFF & dbLookup) >>> 8);
                        dest[destPos++] = (byte)(0x00FF & dbLookup);
                    }
                }
                else
                {
                    // Character found in single-byte table.
                    if (!inSBMode)
                    {
                        inSBMode = true;
                        dest[destPos++] = shiftIn_;
                    }
                    dest[destPos++] = sbLookup;
                }
            }
            else
            {
                // Use double-byte table first.
                // dbLookup = dbTable_.fromUnicode_[curChar];
              dbLookup = dbTable_.fromUnicode(src,srcPos,increment);
              if (increment[0] == 2) {
                // move an extra byte in the source @KDA 
                srcPos++; 
              }
                if (dbLookup == dbSubChar_ && curChar != dbSubUnic_)
                {
                    // Character wasn't in double-byte table. Check single-byte table next.
                    sbLookup = sbTable_.fromUnicode_[curChar];
                    if (sbLookup == sbSubChar_)
                    {
                      // Use the subsitition character value for the current mode @V5A
                      if (inSBMode) { 
                        dest[destPos++] = sbSubChar_;
                      } else {
                        dest[destPos++] = (byte) ((0xFFFF & dbSubChar_) >>> 8);
                        dest[destPos++] = (byte) (0x00FF & dbSubChar_);
                      }
  
                    }
                    else
                    {
                        // Character found in single-byte table.
                        if (!inSBMode)
                        {
                            inSBMode = true;
                            dest[destPos++] = shiftIn_;
                        }
                        dest[destPos++] = sbLookup;
                    }
                }
                else
                {
                    // Character found in double-byte table.
                    if (inSBMode)
                    {
                        inSBMode = false;
                        dest[destPos++] = shiftOut_;
                    }
                    dest[destPos++] = (byte)((0xFFFF & dbLookup) >>> 8);
                    dest[destPos++] = (byte)(0x00FF & dbLookup);
                }
            }
        }

        // Write final shift in, just in case.
        if (!inSBMode)
        {
            dest[destPos++] = shiftIn_;
        }

        byte[] ret = new byte[destPos];
        System.arraycopy(dest, 0, ret, 0, destPos);

        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, ret);
        return ret;
    }
    
    // Scan the data.  If valid return length, otherwise fixup and return the changed length, 
    // padding with spaces as needed. 
    // @X4A
    public int validateData( byte[] buf, int offset, int length) {
      int endOffset = offset+length; 
      int doubleByteStart = -1; 
      boolean doubleByte = false; 
      for (int i = offset; i < endOffset;  i++) { 
        // Not in double byte 
        if (doubleByteStart == -1 ) {
          if (buf[i] == shiftOut_) {
            doubleByteStart = i+1; 
          }
        } else {
          if (buf[i] == shiftIn_) {
            doubleByteStart = -1; 
          } else {
            i++;    // Skip over an extra byte 
          }
        }
      }

      // If there are double bytes then we need to fix up. 
      if (doubleByteStart >= 0)  {
        // Easy case, there is just a half left, replace it with shiftIn
        if (((endOffset - doubleByteStart) % 2) == 1) {
          //  Check to see if we would put a ShiftIn by a shiftOut_
          if (buf[endOffset-2 ] == shiftOut_) {
            length = length - 2;
            buf[endOffset-2] = ebcdicSpace_; 
            buf[endOffset-1] = ebcdicSpace_; 
          } else { 
            buf[endOffset -1 ] = shiftIn_;
          }
        } else {
          // Check to see if ended with shiftOut
          if (doubleByteStart == endOffset) {
             // Just get rid of the shiftOut and reduce the size
             buf[endOffset -1 ] = ebcdicSpace_; 
             length --; 
          } else {
            // Check to see if we would put a ShiftIn by a shiftOut_
            if (buf[endOffset-3] == shiftOut_) {
              length = length - 3;
              buf[endOffset-3] = ebcdicSpace_; 
              buf[endOffset-2] = ebcdicSpace_; 
              buf[endOffset-1] = ebcdicSpace_; 
            } else { 
              // This case there is a two byte character at the end. 
              // Add the shiftin. 
              buf[endOffset-2]= shiftIn_; 
              buf[endOffset-1]= ebcdicSpace_; 
              length --;
            }
            
          }
        }
        
      }
      
      
      return length; 
    }

}
