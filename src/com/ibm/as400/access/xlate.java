///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: xlate.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.applet.*;

/**
  *Translation class used to translate to/from EBCDIC and various data transforms.
  */
class xlate
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

	private static final byte a2e[] =
	{
	(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x37, (byte)0x2D, (byte)0x2E, (byte)0x2F,
	(byte)0x16, (byte)0x05, (byte)0x25, (byte)0x0B, (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F,
	(byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0xB6, (byte)0xB5, (byte)0x32, (byte)0x26,
	(byte)0x18, (byte)0x19, (byte)0x3F, (byte)0x27, (byte)0x1C, (byte)0x1D, (byte)0x1E, (byte)0x1F,
	(byte)0x40, (byte)0x5A, (byte)0x7F, (byte)0x7B, (byte)0x5B, (byte)0x6C, (byte)0x50, (byte)0x7D,
	(byte)0x4D, (byte)0x5D, (byte)0x5C, (byte)0x4E, (byte)0x6B, (byte)0x60, (byte)0x4B, (byte)0x61,
	(byte)0xF0, (byte)0xF1, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6, (byte)0xF7,
	(byte)0xF8, (byte)0xF9, (byte)0x7A, (byte)0x5E, (byte)0x4C, (byte)0x7E, (byte)0x6E, (byte)0x6F,
	(byte)0x7C, (byte)0xC1, (byte)0xC2, (byte)0xC3, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7,
	(byte)0xC8, (byte)0xC9, (byte)0xD1, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD5, (byte)0xD6,
	(byte)0xD7, (byte)0xD8, (byte)0xD9, (byte)0xE2, (byte)0xE3, (byte)0xE4, (byte)0xE5, (byte)0xE6,
	(byte)0xE7, (byte)0xE8, (byte)0xE9, (byte)0xBA, (byte)0xE0, (byte)0xBB, (byte)0xB0, (byte)0x6D,
	(byte)0x79, (byte)0x81, (byte)0x82, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87,
	(byte)0x88, (byte)0x89, (byte)0x91, (byte)0x92, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0x96,
	(byte)0x97, (byte)0x98, (byte)0x99, (byte)0xA2, (byte)0xA3, (byte)0xA4, (byte)0xA5, (byte)0xA6,
	(byte)0xA7, (byte)0xA8, (byte)0xA9, (byte)0xC0, (byte)0x4F, (byte)0xD0, (byte)0xA1, (byte)0xFF,
	(byte)0x68, (byte)0xDC, (byte)0x51, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x47, (byte)0x48,
	(byte)0x52, (byte)0x53, (byte)0x54, (byte)0x57, (byte)0x56, (byte)0x58, (byte)0x63, (byte)0x67,
	(byte)0x71, (byte)0x9C, (byte)0x9E, (byte)0xCB, (byte)0xCC, (byte)0xCD, (byte)0xDB, (byte)0xDD,
	(byte)0xDF, (byte)0xEC, (byte)0xFC, (byte)0x4A, (byte)0xB1, (byte)0xB2, (byte)0xBF, (byte)0x07,
	(byte)0x45, (byte)0x55, (byte)0xCE, (byte)0xDE, (byte)0x49, (byte)0x69, (byte)0x9A, (byte)0x9B,
	(byte)0xAB, (byte)0xAF, (byte)0x5F, (byte)0xB8, (byte)0xB7, (byte)0xAA, (byte)0x8A, (byte)0x8B,
	(byte)0x2B, (byte)0x2C, (byte)0x09,	(byte)0x21, (byte)0x28, (byte)0x65,	(byte)0x62, (byte)0x64,
	(byte)0xB4, (byte)0x38, (byte)0x31, (byte)0x34, (byte)0x33, (byte)0x70, (byte)0x80, (byte)0x24,
	(byte)0x22, (byte)0x17, (byte)0x29, (byte)0x06, (byte)0x20, (byte)0x2A, (byte)0x46, (byte)0x66,
	(byte)0x1A, (byte)0x35, (byte)0x08, (byte)0x39, (byte)0x36, (byte)0x30, (byte)0x3A, (byte)0x9F,
	(byte)0x8C, (byte)0xAC, (byte)0x72, (byte)0x73, (byte)0x74, (byte)0x0A, (byte)0x75, (byte)0x76,
	(byte)0x77, (byte)0x23, (byte)0x15, (byte)0x14, (byte)0x04, (byte)0x6A, (byte)0x78, (byte)0x3B,
	(byte)0xEE, (byte)0x59, (byte)0xEB, (byte)0xED, (byte)0xCF, (byte)0xEF, (byte)0xA0, (byte)0x8E,
	(byte)0xAE, (byte)0xFE, (byte)0xFB, (byte)0xFD, (byte)0x8D, (byte)0xAD, (byte)0xBC, (byte)0xBE,
	(byte)0xCA, (byte)0x8F, (byte)0x1B, (byte)0xB9, (byte)0x3C, (byte)0x3D, (byte)0xE1, (byte)0x9D,
	(byte)0x90, (byte)0xBD, (byte)0xB3, (byte)0xDA, (byte)0xFA, (byte)0xEA, (byte)0x3E, (byte)0x41
	};

	private static final byte e2a[] =
	{
	(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0xDC, (byte)0x09, (byte)0xC3, (byte)0x9F,
	(byte)0xCA, (byte)0xB2, (byte)0xD5, (byte)0x0B, (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F,
	(byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0xDB, (byte)0xDA, (byte)0x08, (byte)0xC1,
	(byte)0x18, (byte)0x19, (byte)0xC8, (byte)0xF2, (byte)0x1A, (byte)0x1D, (byte)0x1E, (byte)0x1F,
	(byte)0xC4, (byte)0xB3, (byte)0xC0, (byte)0xD9, (byte)0xBF, (byte)0x0A, (byte)0x17, (byte)0x1B,
	(byte)0xB4, (byte)0xC2, (byte)0xC5, (byte)0xB0, (byte)0xB1, (byte)0x05, (byte)0x06, (byte)0x07,
	(byte)0xCD, (byte)0xBA, (byte)0x16, (byte)0xBC, (byte)0xBB, (byte)0xC9, (byte)0xCC, (byte)0x04,
	(byte)0xB9, (byte)0xCB, (byte)0xCE, (byte)0xDF, (byte)0xF4, (byte)0xF5, (byte)0xFE, (byte)0x1C,
	(byte)0x20, (byte)0xFF, (byte)0x83, (byte)0x84, (byte)0x85, (byte)0xA0, (byte)0xC6, (byte)0x86,
	(byte)0x87, (byte)0xA4, (byte)0x9B, (byte)0x2E, (byte)0x3C, (byte)0x28, (byte)0x2B, (byte)0x7C,
	(byte)0x26, (byte)0x82, (byte)0x88, (byte)0x89, (byte)0x8A, (byte)0xA1, (byte)0x8C, (byte)0x8B,
	(byte)0x8D, (byte)0xE1, (byte)0x21, (byte)0x24, (byte)0x2A, (byte)0x29, (byte)0x3B, (byte)0xAA,
	(byte)0x2D, (byte)0x2F, (byte)0xB6, (byte)0x8E, (byte)0xB7, (byte)0xB5, (byte)0xC7, (byte)0x8F,
	(byte)0x80, (byte)0xA5, (byte)0xDD, (byte)0x2C, (byte)0x25, (byte)0x5F, (byte)0x3E, (byte)0x3F,
	(byte)0xBD, (byte)0x90, (byte)0xD2, (byte)0xD3, (byte)0xD4, (byte)0xD6, (byte)0xD7, (byte)0xD8,
	(byte)0xDE, (byte)0x60, (byte)0x3A, (byte)0x23, (byte)0x40, (byte)0x27, (byte)0x3D, (byte)0x22,
	(byte)0xBE, (byte)0x61, (byte)0x62, (byte)0x63, (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67,
	(byte)0x68, (byte)0x69, (byte)0xAE, (byte)0xAF, (byte)0xD0, (byte)0xEC, (byte)0xE7, (byte)0xF1,
	(byte)0xF8, (byte)0x6A, (byte)0x6B, (byte)0x6C, (byte)0x6D, (byte)0x6E, (byte)0x6F, (byte)0x70,
	(byte)0x71, (byte)0x72, (byte)0xA6, (byte)0xA7, (byte)0x91, (byte)0xF7, (byte)0x92, (byte)0xCF,
	(byte)0xE6, (byte)0x7E, (byte)0x73, (byte)0x74, (byte)0x75, (byte)0x76, (byte)0x77, (byte)0x78,
	(byte)0x79, (byte)0x7A, (byte)0xAD, (byte)0xA8, (byte)0xD1, (byte)0xED, (byte)0xE8, (byte)0xA9,
	(byte)0x5E, (byte)0x9C, (byte)0x9D, (byte)0xFA, (byte)0xB8, (byte)0x15, (byte)0x14, (byte)0xAC,
	(byte)0xAB, (byte)0xF3, (byte)0x5B, (byte)0x5D, (byte)0xEE, (byte)0xF9, (byte)0xEF, (byte)0x9E,
	(byte)0x7B, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47,
	(byte)0x48, (byte)0x49, (byte)0xF0, (byte)0x93, (byte)0x94, (byte)0x95, (byte)0xA2, (byte)0xE4,
	(byte)0x7D, (byte)0x4A, (byte)0x4B, (byte)0x4C, (byte)0x4D, (byte)0x4E, (byte)0x4F, (byte)0x50,
	(byte)0x51, (byte)0x52, (byte)0xFB, (byte)0x96, (byte)0x81, (byte)0x97, (byte)0xA3, (byte)0x98,
	(byte)0x5C, (byte)0xF6, (byte)0x53, (byte)0x54, (byte)0x55, (byte)0x56, (byte)0x57, (byte)0x58,
	(byte)0x59, (byte)0x5A, (byte)0xFD, (byte)0xE2, (byte)0x99, (byte)0xE3, (byte)0xE0, (byte)0xE5,
	(byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37,
	(byte)0x38, (byte)0x39, (byte)0xFC, (byte)0xEA, (byte)0x9A, (byte)0xEB, (byte)0xE9, (byte)0x7F
	};

	/**
	  *Constructs a translation object.
	  */
//	xlate()
//	{
//	}

	/**
	  *Translate from ASCII to EBCDIC.
	  *
	  *@param asciiString    Source ASCII string to translate.
	  *@param ebcdicString   Target EBCDIC string.
	  *@return none
	  */
	public static void AsciiToEbcdic(String asciiString, byte ebcdicString[])
	{
		AsciiToEbcdic(asciiString, ebcdicString, 0);
	}

	/**
	  *Translate from ASCII to EBCDIC.
	  *
	  *@param asciiString    Source ASCII string to translate.
	  *@param ebcdicString   Target EBCDIC string.
	  *@param start          Target index to start.
	  *@return  none
	  */
	public static void AsciiToEbcdic(String asciiString, byte ebcdicString[], int start)
	{
		int len;

		if (ebcdicString.length - start < asciiString.length())
		{
			len = ebcdicString.length;
		}
		else
		{
			len = asciiString.length();
		}

		for (int i=0; i<len; i++)
		{
			int index = (int)asciiString.charAt(i);
			if (index < 0)
			{
				index = index & 0xff;
			}
			ebcdicString[start+i] = a2e[index];
		}
	}

public static void asciiToEbcdic(byte[] source,
                          int    sourceOffset,
                          byte[] destination,
                          int    destOffset,
                          int    length)
{
  for (int i = 0; i < length; i++)
  {
    destination[destOffset+i] = a2e[source[sourceOffset+i] & 0xff];
  }
}

public static void ebcdicToAscii(byte[] source,
                          int    sourceOffset,
                          byte[] destination,
                          int    destOffset,
                          int    length)
{
  for (int i = 0; i < length; i++)
  {
    destination[destOffset+i] = e2a[source[sourceOffset+i] & 0xff];
  }
}

	/**
	  *Translate from EBCDIC to ASCII.
	  *
	  *@param ebcdicString  Source EBCDIC string.
	  *@return ASCII string.
	  */
	public static String EbcdicToAscii(byte ebcdicString[])
	{
		return(EbcdicToAscii(ebcdicString, 0, ebcdicString.length));
	}

	/**
	  *Translate from EBCDIC to ASCII.
	  *
	  *@param ebcdicString  Source EBCDIC string.
	  *@param start         Source index to start translating.
	  *@param length        Number of EBCDIC characters to translate.
	  *@return  ASCII string.
	  */
	public static String EbcdicToAscii(byte ebcdicString[], int start, int length)
	{
		byte tempAscii[] = new byte[length];
		int index;

		for (int i=0; i<length; i++)
		{
			index = ebcdicString[start+i];
			if (index < 0)
			{
				index = index & 0x000000ff;
			}
			tempAscii[i] = e2a[index];
		}

//                String asciiString = new String(tempAscii, 0);
                return(new String(tempAscii));
	}

    
	/**
	  *Set a 32-bit integer into a byte array.
	  *
	  *@param  data   Integer to set.
	  *@param  b      Target byte array.
	  *@param  offset Index into the byte array to store the data.
	  */
	public static void setInt32(int data, byte[] b, int offset)
	{
		int t;

		t = data & 0xff;
		b[offset+3] = (byte)t;

		t = (data>>>8);
		t = t & 0xff;
		b[offset+2] = (byte)t;

		t = (data>>>16);
		t = t & 0xff;
		b[offset+1] = (byte)t;

		t = (data>>>24);
		t = t & 0xff;
		b[offset] = (byte)t;
	}

	/**
	  *Retrieve a 32-bit integer from a byte array.
	  *
	  *@param  b       Source byte array to retrieve data from.
	  *@param  offset  Index into the byte array to retrieve the data.
	  *@return Integer data.
	  */
	public static int getInt32(byte[] b, int offset)
	{
		int t;
		int t1;
		int i;

		t = 0;
		for (i=0; i<4; i++)
		{
			t1 = b[offset+i];
			if (t1<0)
			{
				t1 = t1 & 0xff;
			}
			t = (t<<8) + t1;
		}

		return(t);
	}

	/**
	  *Set a 16-bit integer into a byte array.
	  *
	  *@param  data   Integer to set.
	  *@param  b      Target byte array.
	  *@param  offset Index into the byte array to store the data.
	  */
	public static void setInt16(int data, byte[] b, int offset)
	{
		int t;

		t = data & 0xff;
		b[offset+1] = (byte)t;

		t = (data>>>8);
		t = t & 0xff;
		b[offset+0] = (byte)t;
	}

	/**
	  *Retrieve a 16-bit integer from a byte array.
	  *
	  *@param  b       Source byte array to retrieve data from.
	  *@param  offset  Index into the byte array to retrieve the data.
	  *@return Integer data.
	  */
	public static int getInt16(byte[] b, int offset)
	{
		int t;
		int t1;
		int i;

		t = 0;
		for (i=0; i<2; i++)
		{
			t1 = b[offset+i];
			if (t1<0)
			{
				t1 = t1 & 0xff;
			}

			t = (t<<8) + t1;
		}

		return(t);
	}

	public static byte[] bytesForInt16( int n )
	{
	    byte[] ret = new byte[2];
	    setInt16( n, ret, 0 );
	    return ret;
	}
}
