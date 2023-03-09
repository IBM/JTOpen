///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Char.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import com.ibm.as400.access.CharConverter;

/**
 * Represents an ascii character array.
 */
class Char extends Field {
	private static CharConverter conv= new CharConverter();

	/**
	 * Base constructor which creates a default ascii character field.  
	 * @param data BitBuf(in Ebcdic) which represents this flag field.     
	 */
	public Char(BitBuf data) {
		super(data);
	}

	/**
	 * Creates a String representation of this field.<br>
	 * Converts the BitBuf from Ebcdic to Ascii before returning.
	 * @return  String representing this character
	 */
	public String toString() {
		String field= conv.byteArrayToString(data.getBytes());
		char[] array= field.toCharArray();
		for (int i= 0; i < array.length; i++) {
			if (!Character.isLowerCase(array[i])
				&& !Character.isUpperCase(array[i])
				&& !Character.isDigit(array[i])
				&& !Character.isSpaceChar(array[i])) {
				array[i]= '.';
			}
		}
		return String.valueOf(array);
	}
}