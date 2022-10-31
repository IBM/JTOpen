///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Data.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

/**
 * Represents the data that is contained in the packet.<br>
 */
public class Data {
	private BitBuf data;
	
	private final static String DATA = "Data";
	
    /**
     * Initializes the data. 
     * @param data      the raw packet of data.                        
     */
	Data(BitBuf data) {
		this.data = data;
	}

    /**
     * Creates a hexadecimal and Ascii representation of this data. One line is 32 characters ASCII and 64 bytes hexadecimal plus formatting characters in length.
     * @return String containing a printable representation of the data.
     */
	public String toString() {
	    if(data.getByteSize()==0) { // If there is no data return
			return "";
	    }

	    StringBuffer fmtdata = new StringBuffer(data.getByteSize()*3); // The return data 
	    StringBuffer hexdata = new StringBuffer(32); // The output hexadecimal data
	    StringBuffer chardata= new StringBuffer(32); // The output character representation
	    boolean first = true; // Set to true until the first line of data is printed.
	    int length = data.getByteSize(), i;
	    
	    fmtdata.append("\t" + DATA + " . . . . . . :  ");
	    for(i=0;i<length;i++) {
			if(i%32==0 && i!=0) { // Every 32 characters we print a line
				BitBuf b = data.slice((i-32)*8,32*8); // This line contains the previous 32 characters
			    hexdata.append(b.toHexString()); // Create the hexadecimal string
			    chardata.append((new Char(b)).toString()); // Create the ascii representation of that string
			    Object[] args = {hexdata, chardata};
			    if(first) { // If this is the first line of data we don't need to insert the tabs
					fmtdata.append(Formatter.jsprintf(
									"{0,64,L}\t*{1,32,L}*\n",
									args));
					first=false;
			    } else {
					fmtdata.append(Formatter.jsprintf(
									"\t\t\t    {0,64,L}\t*{1,32,L}*\n",
									args));
				}
			    hexdata = new StringBuffer(32); // Clear the buffers 
			    chardata = new StringBuffer(32);
			}
	    }
	    if(i<32) { // If our data is less then 32 bytes just print all the data
			i = 0;
	    } else { // Otherwise we need to print the last few bytes that haven't been printed yet.
			i = (i-(i%32))*8;
	    }
	    BitBuf b = data.slice(i); // This line contains the rest of the data
	    String hex = b.toHexString();
	    if(hex.equals("")) { // If there is no data left, then just return
	    	return fmtdata.toString();
	    }
	    hexdata.append(hex);
	    chardata.append((new Char(b)).toString()); 
	    Object[] args = {hexdata, chardata};
	    if(first) {
			fmtdata.append(Formatter.jsprintf(
							"{0,64,L}\t*{1,32,L}*\n",
							args));
	    } else {
			fmtdata.append(Formatter.jsprintf(
							"\t\t\t    {0,64,L}\t*{1,32,L}*\n",
							args));
			first=false;
	    }
	    return fmtdata.toString();
	}
}
