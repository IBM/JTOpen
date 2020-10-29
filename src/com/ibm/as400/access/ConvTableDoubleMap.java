///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConvTableDoubleMap.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.ArrayList;
import java.util.Enumeration;

import java.util.Hashtable;

/** This is the parent class for all ConvTableXXX classes that represent double-byte ccsids.
 * 
 */
public class ConvTableDoubleMap extends ConvTable
{
    private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

    // These tables are private since there is not always a 1 to 1 lookup of the values. @KDC 
    private char[] toUnicode_ = null;
    protected char[] fromUnicode_ = null;
    
    char[][] toUnicodeSurrogate_ = null; 
    char[][] toUnicodeTriple_ = null; 
    // To convert from unicode, @KDA 
    // The first index is based off of D800
    // The second index is based off of DC00
    // The length of each dimension is 0x400
    public static final int LEADING_SURROGATE_BASE = 0xD800; 
    public static final int TRAILING_SURROGATE_BASE = 0xDC00; 
    public static final int FROM_UNICODE_SURROGATE_DIMENSION_LENGTH = 0x400; 
    char[][] fromUnicodeSurrogate_ = null;

    
    
    char[][][] fromUnicodeTriple_ = null;
    int firstTripleMin_;
    int secondTripleMin_; 
    int thirdTripleMin_; 
    
    
    // combining characters used for unicode to ebcdic conversion 
    char[] combiningCharacters_;
    char[][] combiningCombinations_; 
    

    
    // Constructor.
  ConvTableDoubleMap(int ccsid, char[] toUnicode, char[] fromUnicode,
      char[][] toUnicodeSurrogateMapping, char[][] toUnicodeTripleMapping) {
    this(ccsid, toUnicode, fromUnicode);
    toUnicodeSurrogate_ = new char[65535][];
    fromUnicodeSurrogate_ = new char[FROM_UNICODE_SURROGATE_DIMENSION_LENGTH][];
    ArrayList combiningCombinationArrayList = new ArrayList();
    Hashtable combiningCharacterHashtable  = new Hashtable(); 
    for (int i = 0; i < toUnicodeSurrogateMapping.length; i++) {
      char ebcdicChar = toUnicodeSurrogateMapping[i][0];
      char leadingSurrogate = toUnicodeSurrogateMapping[i][1];
      char trailingSurrogate = toUnicodeSurrogateMapping[i][2];

      char[] pair = new char[2];
      pair[0] = leadingSurrogate;
      pair[1] = trailingSurrogate;
      toUnicodeSurrogate_[0xffff & (int) ebcdicChar] = pair;

      
      // Create fromUnicodeSurrogate mapping @KDA
      int leadingIndex = leadingSurrogate - LEADING_SURROGATE_BASE;
      int trailingIndex = trailingSurrogate - TRAILING_SURROGATE_BASE;
      if (leadingIndex >= 0
          && leadingIndex < FROM_UNICODE_SURROGATE_DIMENSION_LENGTH) {
        if (fromUnicodeSurrogate_[leadingIndex] == null) {
          fromUnicodeSurrogate_[leadingIndex] = new char[FROM_UNICODE_SURROGATE_DIMENSION_LENGTH];
        }
        char[] fromUnicodeSurrogate2 = fromUnicodeSurrogate_[leadingIndex];
        if (trailingIndex >= 0 && trailingIndex < FROM_UNICODE_SURROGATE_DIMENSION_LENGTH) {
           fromUnicodeSurrogate2[trailingIndex] = ebcdicChar;
        } else {
          // Error case here.. Should never happen. 
        }
      } else {
        // Leading index not surrogate, must be combining combination
        char[] triplet = new char[3]; 
        triplet[0] = leadingSurrogate;
        triplet[1] = trailingSurrogate; 
        triplet[2] = ebcdicChar; 
        combiningCombinationArrayList.add(triplet); 
        combiningCharacterHashtable.put(new Integer(trailingSurrogate), pair); 
        
      }
    } /* for i */ 
    int combiningCharacterSize = combiningCharacterHashtable.size(); 
    combiningCharacters_ = new char[combiningCharacterSize]; 
    int i = 0; 
    Enumeration keys = combiningCharacterHashtable.keys();
    while (keys.hasMoreElements()) {
      Integer x = (Integer) keys.nextElement(); 
      combiningCharacters_[i] = (char) x.intValue(); 
      i++; 
    }
    
    int combiningCombinationSize = combiningCombinationArrayList.size(); 
    combiningCombinations_ = new char[combiningCombinationSize][]; 
    for (i = 0; i < combiningCombinationSize; i++) {
      combiningCombinations_[i] = (char[]) combiningCombinationArrayList.get(i); 
    }

    if (toUnicodeTripleMapping != null) {
      // Determine the dimensions for each of the array mappings
      firstTripleMin_ = 0xFFFF;
      secondTripleMin_ = 0xFFFF;
      thirdTripleMin_ = 0xFFFF;
      
      int firstTripleMax = 0; 
      int secondTripleMax = 0; 
      int thirdTripleMax = 0; 
      
      for (int j = 0; j < toUnicodeTripleMapping.length; j++) {
        char[] row = toUnicodeTripleMapping[j]; 
        int firstTriple = 0xffff & row[1]; 
        int secondTriple = 0xffff & row[2]; 
        int thirdTriple = 0xffff & row[3]; 
        if (firstTriple < firstTripleMin_) firstTripleMin_ = firstTriple;
        if (firstTriple > firstTripleMax ) firstTripleMax = firstTriple;
        if (secondTriple < secondTripleMin_) secondTripleMin_ = secondTriple;
        if (secondTriple > secondTripleMax ) secondTripleMax = secondTriple;
        if (thirdTriple < thirdTripleMin_) thirdTripleMin_ = thirdTriple;
        if (thirdTriple > thirdTripleMax ) thirdTripleMax = thirdTriple;
      }
      
      fromUnicodeTriple_ = new char[firstTripleMax-firstTripleMin_+1][][];
      toUnicodeTriple_ = new char[65535][];
     
      // Populate the to and from tables 
      for (int j = 0; j < toUnicodeTripleMapping.length; j++) {
        char[] row = toUnicodeTripleMapping[j]; 
        int ebcdic      = 0xffff & row[0];
        int firstIndex  = (0xffff & row[1]) - firstTripleMin_; 
        int secondIndex = (0xffff & row[2]) - secondTripleMin_; 
        int thirdIndex  = (0xffff & row[3]) - thirdTripleMin_; 
        
        toUnicodeTriple_[ebcdic] = new char[3];
        toUnicodeTriple_[ebcdic][0] = row[1]; 
        toUnicodeTriple_[ebcdic][1] = row[2]; 
        toUnicodeTriple_[ebcdic][2] = row[3]; 
        
        char[][] secondLevel = fromUnicodeTriple_[firstIndex]; 
        if (secondLevel == null) {
          secondLevel = new char[secondTripleMax-secondTripleMin_+1][];
          fromUnicodeTriple_[firstIndex] = secondLevel; 
        }
        char[] thirdLevel = secondLevel[secondIndex];
        if (thirdLevel == null) {
          thirdLevel = new char[thirdTripleMax-thirdTripleMin_+1];
          secondLevel[secondIndex] = thirdLevel; 
        }
        thirdLevel[thirdIndex] = (char) ebcdic; 
      }
    }
  }

    // Constructor.
    ConvTableDoubleMap(int ccsid, char[] toUnicode, char[] fromUnicode)
    {
        super(ccsid);
        toUnicode_ = decompress(toUnicode);
        fromUnicode_ = decompress(fromUnicode);
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Successfully loaded double-byte map for ccsid: " + ccsid_);
    }

    // Constructor
    ConvTableDoubleMap(ConvTableDoubleMap oldMap)
    {
        super(oldMap.ccsid_);
        toUnicode_ = oldMap.toUnicode_; 
        fromUnicode_ = oldMap.fromUnicode_;
        toUnicodeSurrogate_ = oldMap.toUnicodeSurrogate_; 
        fromUnicodeSurrogate_ = oldMap.fromUnicodeSurrogate_; 
        combiningCharacters_ = oldMap.combiningCharacters_;
        combiningCombinations_ = oldMap.combiningCombinations_;
    }
    
    
    // Helper method used to decompress conversion tables when they are initialized.
    char[] decompress(char[] arr) { 
      return decompress(arr, ccsid_); 
    }
    // @N4 make this a static method for ConvTable300 can use it
    static char[] decompress(char[] arr, int ccsid )
    {
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Decompressing double-byte conversion table for ccsid: " + ccsid, arr.length);
        char[] buf = new char[65536];
        int c = 0;
        for (int i = 0; i < arr.length; ++i)
        {
            if (arr[i] == cic_)
            {
                if (arr[i+1] == pad_)
                {
                    buf[c++] = arr[i++];
                }
                else
                {
                    long max = (0xFFFF & arr[i + 1]) + (0xFFFF & c);
                    char ch = arr[i + 2];
                    while (c < max)
                    {
                        buf[c++] = ch;
                    }
                    i += 2;
                }
            }
            else if (arr[i] == ric_)
            {
                if (arr[i + 1] == pad_)
                {
                    buf[c++] = arr[i++];
                }
                else
                {
                    int start = (0xFFFF & arr[i + 2]);
                    int num = (0xFFFF & arr[i + 1]);
                    for (int j = start; j < (num + start); ++j)
                    {
                        buf[c++] = (char)j;
                    }
                    i += 2;
                }
            }
            else if (arr[i] == hbic_)
            {
                if (arr[i+1] == pad_)
                {
                    buf[c++] = arr[i++];
                }
                else
                {
                    int hbNum = (0x0000FFFF & arr[++i]);
                    char firstChar = arr[++i];
                    char highByteMask = (char)(0xFF00 & firstChar);
                    buf[c++] = firstChar;
                    ++i;
                    for (int j=0; j<hbNum; ++j)
                    {
                        char both = arr[i+j];
                        buf[c++] = (char)(highByteMask + ((0xFF00 & both) >>> 8));
                        buf[c++] = (char)(highByteMask + (0x00FF & both));
                    }
                    i = i + hbNum - 1;
                }
            }
            else
            { // Regular character.
                buf[c++] = arr[i];
            }
        }

        return buf;
    }

    // Perform an OS/400 CCSID to Unicode conversion.
    final String byteArrayToString(byte[] buf, int offset, int length, BidiConversionProperties properties)
    {
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting byte array to string for ccsid: " + ccsid_, buf, offset, length);
        // Length could be twice as long because of surrogates
        char[] dest = new char[length ];
        int to = 0; 
        for (int i = 0; i < length / 2; ++i)
        {
            try
            { 
              int fromIndex = ((0x00FF & buf[(i * 2) + offset]) << 8) + (0x00FF & buf[(i * 2) + 1 + offset]);
              int unicodeLength = toUnicode(dest,  to,  fromIndex);
              to += unicodeLength; 
              
            }
            catch(ArrayIndexOutOfBoundsException aioobe)
            {
                // Swallow this if we are doing fault-tolerant conversion.
                if(!CharConverter.isFaultTolerantConversion())
                {
                    throw aioobe;
                }
            }
        }
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination string for ccsid: " + ccsid_, ConvTable.dumpCharArray(dest));
        return String.copyValueOf(dest,0,to);
    }

    public int toUnicode(char[] dest, int to, int fromIndex) {
      int length = 0; 
      dest[to] = toUnicode_[fromIndex];
      // Check if surrogate lookup needed. 
      if (dest[to] == 0xD800) {
        if (toUnicodeSurrogate_ != null) {
          char[] surrogates = toUnicodeSurrogate_[fromIndex];
          if (surrogates != null) {
            dest[to] = surrogates[0];
            to++;
            length++;
            dest[to] = surrogates[1];
            to++;
            length++;
          } else { 
            // surrogate not defined, replace with sub
            dest[to] = dbSubUnic_; 
            to++; 
            length++;
          }
        } else {
          // Not handling surrogates, replace with sub
          dest[to] = dbSubUnic_; 
          to++;
          length++; 
        }
      } else if (dest[to] == 0xD801) {   /* check for triplet */ 
        if (toUnicodeTriple_ != null) {
          char[] triple = toUnicodeTriple_[fromIndex]; 
          if (triple != null) {
            dest[to] = triple[0];
            to++;
            length++;
            dest[to] = triple[1];
            to++;
            length++;
            dest[to] = triple[2];
            to++;
            length++;
          } else { 
            // triple not defined, replace with sub
            if (ccsid_ == 61952) {
               // Keep the destination as D801
            } else { 
               dest[to] = dbSubUnic_; 
               to++; 
               length++;
            }
         }
        } else {
          // Not handling triplets, replace with sub
          dest[to] = dbSubUnic_; 
          to++;
          length++; 
        }
      } else {
        // Single character.  Increment counter; 
        to++;
        length++; 
      }
      return length;
    }

    
    
    // Perform a Unicode to AS/400 CCSID conversion.
    final byte[] stringToByteArray(String source, BidiConversionProperties properties)
    {
        char[] src = source.toCharArray();
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Converting string to byte array for ccsid: " + ccsid_, ConvTable.dumpCharArray(src));
        byte[] dest;
        // Note.. with surrogates, the output array can be shorter @KDA
        dest = new byte[src.length * 2];
        int destIndex = 0; 
        int[] increment = new int[1 ]; 
        for (int i = 0; i < src.length; ++i, destIndex++)
        {
          char c = fromUnicode(src, i, increment);
          dest[destIndex * 2] = (byte)(c >>> 8);
          dest[destIndex * 2 + 1] = (byte)(0x00FF & c);
          if (increment[0] > 1) {
            i++; 
          }
        }
        if (destIndex * 2 != dest.length) {
          byte[] newDest = new byte[destIndex * 2]; 
          System.arraycopy(dest, 0, newDest, 0, destIndex * 2);
          dest = newDest; 
        }
        if (Trace.traceConversion_) Trace.log(Trace.CONVERSION, "Destination byte array for ccsid: " + ccsid_, dest);
        return dest;
    }

    public char fromUnicode(char[] src, int i, int[] increment) {
      int incrementValue = 1; 
      char returnChar = 0; 
      char currentChar = src[i]; 
      if (currentChar < LEADING_SURROGATE_BASE || currentChar >= TRAILING_SURROGATE_BASE ) {
        int next = i + 1; 
        
        boolean found = false; 
        if ( (combiningCharacters_ != null) && (next < src.length) ) {
           char nextChar = src[next]; 
           for (int j = 0; !found && j < combiningCharacters_.length; j++ ) {
              if (nextChar == combiningCharacters_[j]) {
                for (int k = 0; !found && k < combiningCombinations_.length; k++) { 
                    if ((currentChar == combiningCombinations_[k][0]) &&
                        (nextChar    == combiningCombinations_[k][1])) {
                        found = true; 
                        returnChar  = combiningCombinations_[k][2]; 
                        i++;    /* We handle a leading surrogate, which must be following by a trailing */
                        incrementValue++; 
                        
                    }
                }
              }
           }
        }  
      if (!found && fromUnicodeTriple_ != null) {
        if (i + 2 < src.length) {
          int index1 = (0xFFFF & src[i]) - firstTripleMin_;
          if (index1 >= 0 && index1 < fromUnicodeTriple_.length) {
            char[][] secondLevel = fromUnicodeTriple_[index1];
            if (secondLevel != null) {
              int index2 = (0xFFFF & src[i + 1]) - secondTripleMin_;
              if (index2 >= 0 && index2 < secondLevel.length) {  
                char[] thirdLevel = secondLevel[index2];
                int index3 = (0xFFFF & src[i+2]) - thirdTripleMin_; 
                if (index3 >= 0 && index3 < thirdLevel.length) {
                  returnChar = thirdLevel[index3]; 
                  if (returnChar != 0)  {
                    found = true; 
                    incrementValue += 2; 
                  }
                }
              }
            }
          }
        }
      }
        if (!found) { 
          returnChar = fromUnicode_[src[i]];
        }
     } else { 
        int leadingIndex = src[i] - LEADING_SURROGATE_BASE;
        i++;    /* We handle a leading surrogate, which must be following by a trailing */
        incrementValue++; 
        /* We don't need to check the leadingIndex since we know it is already in range*/
        if (fromUnicodeSurrogate_ != null) {
        char[] fromUnicodeSurrogate2 = fromUnicodeSurrogate_[leadingIndex];
        if (fromUnicodeSurrogate2 != null) { 
          int trailingIndex = src[i] - TRAILING_SURROGATE_BASE;
          /* Check for valid index and for existing mapping */ 
          if (trailingIndex >= 0  && 
              trailingIndex < FROM_UNICODE_SURROGATE_DIMENSION_LENGTH
              && fromUnicodeSurrogate2[trailingIndex] != 0 ) {
            returnChar = fromUnicodeSurrogate2[trailingIndex] ; 
          } else {
            /* We could not handle.  Add substitution character */
            returnChar = dbSubChar_;
          }
        } else {
          /* We could not handle.  Add substitution character */ 
          returnChar = dbSubChar_;
        }
        } else {
          /* no surrogate values for this CCSID  */ 
          returnChar = dbSubChar_;
        }
     }

      
      increment[0] = incrementValue;
      return returnChar; 
    }

    
    public char[] getFromUnicode() {
      return fromUnicode_;
    }

    void setFromUnicode(char[] fromUnicode) { 
      fromUnicode_ = fromUnicode; 
    }

    public char[] getToUnicode() {
      return toUnicode_;
    }

    void setToUnicode(char[] toUnicode) { 
      toUnicode_ = toUnicode; 
    }



    
    
}
