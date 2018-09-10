///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  SingleByteConversionTable.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class SingleByteConversionTable implements SingleByteConversion {
	int ccsid_;
	char[] toUnicode_;
	byte[] fromUnicode_;


	public int getCcsid() {
		return ccsid_;
	}

	/* returns the table mapping from ccsid value to unicode */
	public char[] returnToUnicode() { return toUnicode_; };

	/* returns the table mapping from unicode to the ccsid value */
	public byte[] returnFromUnicode() { return fromUnicode_; }



	public static byte[] generateFromUnicode(char[] toUnicode) {
		int maxUnicodeValue = 0;
		for (int i = 0; i < toUnicode.length; i++) {
			if ( toUnicode[i] > maxUnicodeValue ) {
				maxUnicodeValue = toUnicode[i] ;
			}
		}
		byte[] fromUnicode = new byte[maxUnicodeValue+1];
		for (int i = 0; i < maxUnicodeValue+1; i++) {
			fromUnicode[i] = 0x3f;
		}
		for (int i = 0; i < toUnicode.length; i++) {
			fromUnicode[toUnicode[i]] = (byte) i;
		}

		return fromUnicode;
	};
};

