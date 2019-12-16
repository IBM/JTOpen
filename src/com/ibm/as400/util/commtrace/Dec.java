///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Dec.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;
/**
 * A decimal field.
 */
class Dec extends Field {
	/**
	 * Base constructor which creates a default decimal field.  
	 * @param data         BitBuf which contains this decimal field.     
	 */
	public Dec(BitBuf data) {
		super(data);
	}
	/**
	 * Creates a String representation of this decimal field.   
	 * @return          A String representing this decimal field. 
	 */
	public String toString() {
		return Long.toString(data.toLong());
	}
}