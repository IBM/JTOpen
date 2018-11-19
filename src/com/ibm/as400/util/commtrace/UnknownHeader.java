///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: UnknownHeader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Properties;
import com.ibm.as400.access.Trace;

/**
 * A Unknown Header.<br>
 * Extends Header's methods to parse, print, and allow easy access to the Unknown Header.
 */
public class UnknownHeader extends Header {
	private final static String UNKHDR= "Unknown Header";
	private final static String CLASS= "UnknownHeader";

	/**
	 * Creates and parses the data of this header.  
	 * @param data  The raw data of this header. 
	 */
	UnknownHeader(BitBuf data) {
		super(data);
	}

    /**
     * Returns a printable representation of this header.
     * @param filter	    FormatProperties object for filtering this header.
     * @return	    Returns a string representation of this header.
     */
	public String toString(FormatProperties filter) {
		// Check for IP filtering
		if (filter!=null) { // If filter is enabled
			boolean print= false;
			if(filter.getIPAddress()==null && filter.getSecondIPAddress()==null && filter.getPort()==null) {
				// None of the filters apply at the Header level so print out the Unknown Header
				print=true;
			}
			if (!print) { // Don't print the packet
				if (Trace.isTraceOn() && Trace.isTraceInformationOn()) {
					Trace.log(Trace.INFORMATION,CLASS + ".toString() " + "Not printing record");
				}
				return "";
			}
		}
		return "\t    " + UNKHDR + ":\n" + (new Data(rawpayload)).toString();
	}
}
