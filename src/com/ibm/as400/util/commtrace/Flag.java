///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Flag.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * Represents a user definable flag.<br>
 * True and false values are overrideable to produce specific output. 
 */
class Flag extends Field {
	String truevalue;
	String falsevalue;
	boolean usetext;

	/**
	 * Base constructor which creates a default flag.  
	 * @param data         BitBuf which represents this flag field.      
	 */
	public Flag(BitBuf data) {
		super(data);
	}
	/**
	 * Constructor which creates a default flag with the specified true and false values.  
	 * @param BitBuf         BitBuf which represents this flag field.
	 * @param truevalue		the true value of this flag.
	 * @param falsevalue	the false value of thie flag.
	 */
	public Flag(BitBuf b, String tv, String fv) {
		super(b);
		truevalue = tv;
		falsevalue = fv;
		usetext = true;
	}

	/**
	 * Creates a String representation of this flag.   
	 * @return String representing this flag.
	 */
	public String toString() {
		byte x = data.getBitAsByte(0);
		if (usetext == false)
			return Byte.toString(x);
		else
			if (x == 1)
				return truevalue;
			else
				return falsevalue;
	}
}