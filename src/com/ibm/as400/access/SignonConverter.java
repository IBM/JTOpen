///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SignonConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// Sign-on converter maps only valid user ID and DES password characters between Unicode and EBCDIC CCSID 37.  Also maps so called "special characters" where an N with tilde becomes a # character.
class SignonConverter
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // No need for instances of this class.
    private SignonConverter()
    {
    }

    // Convert EBCDIC CCSID 37 to Unicode String.
    static String byteArrayToString(byte[] source)
    {
        return new String(byteArrayToCharArray(source)).trim();
    }

    // Convert EBCDIC CCSID 37 to Unicode character array.
    static char[] byteArrayToCharArray(byte[] source)
    {
        char[] returnChars = new char[10];
        for (int i = 0; i < 10; ++i)
        {
            switch (source[i] & 0xFF)
            {
                case 0x40: returnChars[i] = 0x0020; break;  // (SP)

                case 0x5B: returnChars[i] = 0x0024; break;  // $
                case 0x6D: returnChars[i] = 0x005F; break;  // _
                case 0x7B: returnChars[i] = 0x0023; break;  // #
                case 0x7C: returnChars[i] = 0x0040; break;  // @

                case 0xC1: returnChars[i] = 0x0041; break;  // A
                case 0xC2: returnChars[i] = 0x0042; break;  // B
                case 0xC3: returnChars[i] = 0x0043; break;  // C
                case 0xC4: returnChars[i] = 0x0044; break;  // D
                case 0xC5: returnChars[i] = 0x0045; break;  // E
                case 0xC6: returnChars[i] = 0x0046; break;  // F
                case 0xC7: returnChars[i] = 0x0047; break;  // G
                case 0xC8: returnChars[i] = 0x0048; break;  // H
                case 0xC9: returnChars[i] = 0x0049; break;  // I
                case 0xD1: returnChars[i] = 0x004A; break;  // J
                case 0xD2: returnChars[i] = 0x004B; break;  // K
                case 0xD3: returnChars[i] = 0x004C; break;  // L
                case 0xD4: returnChars[i] = 0x004D; break;  // M
                case 0xD5: returnChars[i] = 0x004E; break;  // N
                case 0xD6: returnChars[i] = 0x004F; break;  // O
                case 0xD7: returnChars[i] = 0x0050; break;  // P
                case 0xD8: returnChars[i] = 0x0051; break;  // Q
                case 0xD9: returnChars[i] = 0x0052; break;  // R
                case 0xE2: returnChars[i] = 0x0053; break;  // S
                case 0xE3: returnChars[i] = 0x0054; break;  // T
                case 0xE4: returnChars[i] = 0x0055; break;  // U
                case 0xE5: returnChars[i] = 0x0056; break;  // V
                case 0xE6: returnChars[i] = 0x0057; break;  // W
                case 0xE7: returnChars[i] = 0x0058; break;  // X
                case 0xE8: returnChars[i] = 0x0059; break;  // Y
                case 0xE9: returnChars[i] = 0x005A; break;  // Z

                case 0xF0: returnChars[i] = 0x0030; break;  // 0
                case 0xF1: returnChars[i] = 0x0031; break;  // 1
                case 0xF2: returnChars[i] = 0x0032; break;  // 2
                case 0xF3: returnChars[i] = 0x0033; break;  // 3
                case 0xF4: returnChars[i] = 0x0034; break;  // 4
                case 0xF5: returnChars[i] = 0x0035; break;  // 5
                case 0xF6: returnChars[i] = 0x0036; break;  // 6
                case 0xF7: returnChars[i] = 0x0037; break;  // 7
                case 0xF8: returnChars[i] = 0x0038; break;  // 8
                case 0xF9: returnChars[i] = 0x0039; break;  // 9
                default: throw new ExtendedIllegalArgumentException("source", ExtendedIllegalArgumentException.SIGNON_CHAR_NOT_VALID);
            }
        }
        return returnChars;
    }

    // Convert Unicode string to EBCID CCSID 37 byte array.
    static byte[] stringToByteArray(String source)
    {
        char[] sourceChars = source.toCharArray();
        byte[] returnBytes = {(byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40};
        for (int i = 0; i < sourceChars.length; ++i)
        {
            switch (sourceChars[i])
            {
                case 0x0023: returnBytes[i] = (byte)0x7B; break;  // #
                case 0x0024: returnBytes[i] = (byte)0x5B; break;  // $

                case 0x0030: returnBytes[i] = (byte)0xF0; break;  // 0
                case 0x0031: returnBytes[i] = (byte)0xF1; break;  // 1
                case 0x0032: returnBytes[i] = (byte)0xF2; break;  // 2
                case 0x0033: returnBytes[i] = (byte)0xF3; break;  // 3
                case 0x0034: returnBytes[i] = (byte)0xF4; break;  // 4
                case 0x0035: returnBytes[i] = (byte)0xF5; break;  // 5
                case 0x0036: returnBytes[i] = (byte)0xF6; break;  // 6
                case 0x0037: returnBytes[i] = (byte)0xF7; break;  // 7
                case 0x0038: returnBytes[i] = (byte)0xF8; break;  // 8
                case 0x0039: returnBytes[i] = (byte)0xF9; break;  // 9

                case 0x0040: returnBytes[i] = (byte)0x7C; break;  // @

                case 0x0041: returnBytes[i] = (byte)0xC1; break;  // A
                case 0x0042: returnBytes[i] = (byte)0xC2; break;  // B
                case 0x0043: returnBytes[i] = (byte)0xC3; break;  // C
                case 0x0044: returnBytes[i] = (byte)0xC4; break;  // D
                case 0x0045: returnBytes[i] = (byte)0xC5; break;  // E
                case 0x0046: returnBytes[i] = (byte)0xC6; break;  // F
                case 0x0047: returnBytes[i] = (byte)0xC7; break;  // G
                case 0x0048: returnBytes[i] = (byte)0xC8; break;  // H
                case 0x0049: returnBytes[i] = (byte)0xC9; break;  // I
                case 0x004A: returnBytes[i] = (byte)0xD1; break;  // J
                case 0x004B: returnBytes[i] = (byte)0xD2; break;  // K
                case 0x004C: returnBytes[i] = (byte)0xD3; break;  // L
                case 0x004D: returnBytes[i] = (byte)0xD4; break;  // M
                case 0x004E: returnBytes[i] = (byte)0xD5; break;  // N
                case 0x004F: returnBytes[i] = (byte)0xD6; break;  // O
                case 0x0050: returnBytes[i] = (byte)0xD7; break;  // P
                case 0x0051: returnBytes[i] = (byte)0xD8; break;  // Q
                case 0x0052: returnBytes[i] = (byte)0xD9; break;  // R
                case 0x0053: returnBytes[i] = (byte)0xE2; break;  // S
                case 0x0054: returnBytes[i] = (byte)0xE3; break;  // T
                case 0x0055: returnBytes[i] = (byte)0xE4; break;  // U
                case 0x0056: returnBytes[i] = (byte)0xE5; break;  // V
                case 0x0057: returnBytes[i] = (byte)0xE6; break;  // W
                case 0x0058: returnBytes[i] = (byte)0xE7; break;  // X
                case 0x0059: returnBytes[i] = (byte)0xE8; break;  // Y
                case 0x005A: returnBytes[i] = (byte)0xE9; break;  // Z

                case 0x005F: returnBytes[i] = (byte)0x6D; break;  // _

                case 0x00A3: returnBytes[i] = (byte)0x7B; break;  // Cp423, pound sterling.
                case 0x00A5: returnBytes[i] = (byte)0x5B; break;  // Cp281, yen sign.
                case 0x00A7: returnBytes[i] = (byte)0x7C; break;  // Cp273, section sign.
                case 0x00C4: returnBytes[i] = (byte)0x7B; break;  // Cp278, A with dieresis.
                case 0x00C5: returnBytes[i] = (byte)0x5B; break;  // Cp277, A with ring.
                case 0x00C6: returnBytes[i] = (byte)0x7B; break;  // Cp277, ligature AE.
                case 0x00D0: returnBytes[i] = (byte)0x7C; break;  // Cp871, D with stroke.
                case 0x00D1: returnBytes[i] = (byte)0x7B; break;  // Cp284, N with tilde.
                case 0x00D6: returnBytes[i] = (byte)0x7C; break;  // Cp278, O with dieresis.
                case 0x00D8: returnBytes[i] = (byte)0x7C; break;  // Cp277, O with stroke.
                case 0x00E0: returnBytes[i] = (byte)0x7C; break;  // Cp297, a with grave.
                case 0x0130: returnBytes[i] = (byte)0x5B; break;  // Cp905, I with over dot.
                case 0x015E: returnBytes[i] = (byte)0x7C; break;  // Cp905, S with cedilla.
                default: throw new ExtendedIllegalArgumentException("source", ExtendedIllegalArgumentException.SIGNON_CHAR_NOT_VALID);
            }
        }
        return returnBytes;
    }
}
