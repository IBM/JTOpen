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
    char[][] toUnicodeQuad_ = null; 
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
    char[][][][] fromUnicodeQuad_ = null;
    int firstQuadMin_;
    int secondQuadMin_; 
    int thirdQuadMin_; 
    int fourthQuadMin_; 
    
    
    // combining characters used for unicode to ebcdic conversion 
    char[] combiningCharacters_;
    char[][] combiningCombinations_; 
    

    ConvTableDoubleMap(int ccsid, char[] toUnicode, char[] fromUnicode,
        char[][] toUnicodeSurrogateMapping, char[][] toUnicodeTripleMapping) {
      this(ccsid,toUnicode,fromUnicode,toUnicodeSurrogateMapping, toUnicodeTripleMapping,null); 
    }
    
    // Constructor.
  ConvTableDoubleMap(int ccsid, char[] toUnicode, char[] fromUnicode,
      char[][] toUnicodeSurrogateMapping, 
      char[][] toUnicodeTripleMapping,
      char[][] toUnicodeQuadMapping) {
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
      } /* int j*/ 
    } /* toUnicodeTriple != null */ 

    if (toUnicodeQuadMapping != null) {
      // Determine the dimensions for each of the array mappings
      firstQuadMin_ = 0xFFFF;
      secondQuadMin_ = 0xFFFF;
      thirdQuadMin_ = 0xFFFF;
      fourthQuadMin_ = 0xFFFF;
      
      int firstQuadMax = 0; 
      int secondQuadMax = 0; 
      int thirdQuadMax = 0; 
      int fourthQuadMax = 0; 
      
      for (int j = 0; j < toUnicodeQuadMapping.length; j++) {
        char[] row = toUnicodeQuadMapping[j]; 
        int firstQuad = 0xffff & row[1]; 
        int secondQuad = 0xffff & row[2]; 
        int thirdQuad = 0xffff & row[3]; 
        int fourthQuad = 0xffff & row[4]; 
        if (firstQuad < firstQuadMin_) firstQuadMin_ = firstQuad;
        if (firstQuad > firstQuadMax ) firstQuadMax = firstQuad;
        if (secondQuad < secondQuadMin_) secondQuadMin_ = secondQuad;
        if (secondQuad > secondQuadMax ) secondQuadMax = secondQuad;
        if (thirdQuad < thirdQuadMin_) thirdQuadMin_ = thirdQuad;
        if (thirdQuad > thirdQuadMax ) thirdQuadMax = thirdQuad;
        if (fourthQuad < fourthQuadMin_) fourthQuadMin_ = fourthQuad;
        if (fourthQuad > fourthQuadMax ) fourthQuadMax = fourthQuad;
      }
      
      fromUnicodeQuad_ = new char[firstQuadMax-firstQuadMin_+1][][][];
      toUnicodeQuad_ = new char[65535][];
     
      // Populate the to and from tables 
      for (int j = 0; j < toUnicodeQuadMapping.length; j++) {
        char[] row = toUnicodeQuadMapping[j]; 
        int ebcdic      = 0xffff & row[0];
        int firstIndex  = (0xffff & row[1]) - firstQuadMin_; 
        int secondIndex = (0xffff & row[2]) - secondQuadMin_; 
        int thirdIndex  = (0xffff & row[3]) - thirdQuadMin_; 
        int fourthIndex  = (0xffff & row[4]) - fourthQuadMin_; 
        
        toUnicodeQuad_[ebcdic] = new char[4];
        toUnicodeQuad_[ebcdic][0] = row[1]; 
        toUnicodeQuad_[ebcdic][1] = row[2]; 
        toUnicodeQuad_[ebcdic][2] = row[3]; 
        toUnicodeQuad_[ebcdic][3] = row[4]; 
        
        char[][][] secondLevel = fromUnicodeQuad_[firstIndex]; 
        if (secondLevel == null) {
          secondLevel = new char[secondQuadMax-secondQuadMin_+1][][];
          fromUnicodeQuad_[firstIndex] = secondLevel; 
        }
        char[][] thirdLevel = secondLevel[secondIndex];
        if (thirdLevel == null) {
          thirdLevel = new char[thirdQuadMax-thirdQuadMin_+1][];
          secondLevel[secondIndex] = thirdLevel; 
        }
        char[] fourthLevel = thirdLevel[thirdIndex];
        if (fourthLevel == null) {
          fourthLevel = new char[fourthQuadMax-fourthQuadMin_+1];
          thirdLevel[thirdIndex] = fourthLevel; 
        }
        fourthLevel[fourthIndex] = (char) ebcdic; 
      } /* int j*/ 
    } /* toUnicodeQuad != null */ 
    

    
    
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
      } else if (dest[to] == 0xD802) {  /* check for quad */ 
        if (toUnicodeQuad_ != null) {
          char[] quad = toUnicodeQuad_[fromIndex]; 
          if (quad != null) {
            dest[to] = quad[0];
            to++;
            length++;
            dest[to] = quad[1];
            to++;
            length++;
            dest[to] = quad[2];
            to++;
            length++;
            dest[to] = quad[3];
            to++;
            length++;
          } else { 
            // triple not defined, replace with sub
               dest[to] = dbSubUnic_; 
               to++; 
               length++;
         }
        } else {
          // Not handling quad, replace with sub
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
    boolean found = false;
    /* Search the quad mappings first. For CCSID 1399 */
    /* D841 DF0E DB40 DB40 -> 0xF486 */
    /* D841 DF0E -> 0xCA47 */
    if (fromUnicodeQuad_ != null && (i + 3 < src.length)) {
      int index1 = (0xFFFF & src[i]) - firstQuadMin_;
      if (index1 >= 0 && index1 < fromUnicodeQuad_.length) {
        char[][][] secondLevel = fromUnicodeQuad_[index1];
        if (secondLevel != null) {
          int index2 = (0xFFFF & src[i + 1]) - secondQuadMin_;
          if (index2 >= 0 && index2 < secondLevel.length) {
            char[][] thirdLevel = secondLevel[index2];
            int index3 = (0xFFFF & src[i + 2]) - thirdQuadMin_;
            if (index3 >= 0 && index3 < thirdLevel.length) {
              char[] fourthLevel = thirdLevel[index3]; 
              int index4 = (0xFFFF & src[i + 3]) - fourthQuadMin_;
              if (index4 >= 0 && index4 < fourthLevel.length) {

                returnChar = fourthLevel[index4];
                if (returnChar != 0) {
                  found = true;
                  incrementValue += 3;
                } /* return Char 1 != 0 */
              } /* index4 in range */ 
            } /* index3 in range */
          } /* index2 inRange */
        } /* secondLevel is not null */ 
      } /* index1 inRange */ 
    }
    /* Search the triple mappings next */ 
    if (!found && fromUnicodeTriple_ != null) {
      if (i + 2 < src.length) {
        int index1 = (0xFFFF & src[i]) - firstTripleMin_;
        if (index1 >= 0 && index1 < fromUnicodeTriple_.length) {
          char[][] secondLevel = fromUnicodeTriple_[index1];
          if (secondLevel != null) {
            int index2 = (0xFFFF & src[i + 1]) - secondTripleMin_;
            if (index2 >= 0 && index2 < secondLevel.length) {
              char[] thirdLevel = secondLevel[index2];
              int index3 = (0xFFFF & src[i + 2]) - thirdTripleMin_;
              if (index3 >= 0 && index3 < thirdLevel.length) {
                returnChar = thirdLevel[index3];
                if (returnChar != 0) {
                  found = true;
                  incrementValue += 2;
                } /* return Char 1 != 0 */
              } /* index3 in range */
            } /* index2 inRange */
          } /* secondLevel is not null */ 
        } /* index1 inRange */ 
      } /* i + 2 < src.length */ 
    } /* fromUnicodeTriple_ != null) */ 

    if (!found) {
      
    if (currentChar < LEADING_SURROGATE_BASE
        || currentChar >= TRAILING_SURROGATE_BASE) {
      int next = i + 1;

      if ((combiningCharacters_ != null) && (next < src.length)) {
        char nextChar = src[next];
        for (int j = 0; !found && j < combiningCharacters_.length; j++) {
          if (nextChar == combiningCharacters_[j]) {
            for (int k = 0; !found && k < combiningCombinations_.length; k++) {
              if ((currentChar == combiningCombinations_[k][0])
                  && (nextChar == combiningCombinations_[k][1])) {
                found = true;
                returnChar = combiningCombinations_[k][2];
                i++; /*
                      * We handle a leading surrogate, which must be following
                      * by a trailing
                      */
                incrementValue++;

              } /* current combination */ 
            } /* for k */ 
          } /* nextChar == combiningCharacters */
        } /* for j */ 
      } /* combining characters */ 
      if (!found) {
        returnChar = fromUnicode_[src[i]];
      }
    } else {
      int leadingIndex = src[i] - LEADING_SURROGATE_BASE;
      i++; /*
            * We handle a leading surrogate, which must be following by a
            * trailing
            */
      incrementValue++;
      /*
       * We don't need to check the leadingIndex since we know it is already in
       * range
       */
      if (fromUnicodeSurrogate_ != null) {
        char[] fromUnicodeSurrogate2 = fromUnicodeSurrogate_[leadingIndex];
        if (fromUnicodeSurrogate2 != null) {
          int trailingIndex = src[i] - TRAILING_SURROGATE_BASE;
          /* Check for valid index and for existing mapping */
          if (trailingIndex >= 0
              && trailingIndex < FROM_UNICODE_SURROGATE_DIMENSION_LENGTH
              && fromUnicodeSurrogate2[trailingIndex] != 0) {
            returnChar = fromUnicodeSurrogate2[trailingIndex];
          } else {
            /* We could not handle. Add substitution character */
            returnChar = dbSubChar_;
          }
        } else {
          /* We could not handle. Add substitution character */
          returnChar = dbSubChar_;
        }
      } else {
        /* no surrogate values for this CCSID */
        returnChar = dbSubChar_;
      }
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
