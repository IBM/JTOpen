///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: Field.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import com.ibm.as400.access.AS400;

/**
 * Represents a field in the trace. Used to make converting easy between a BitBuff and readable/printable text.
 */
class Field {
    BitBuf data;

    /**
     * Returns a copy of the raw data that makes up this field.
     * @return The raw data for this field.
     */
    public BitBuf getData() {
		return (BitBuf) data.clone();
    }

    /**
     * Field constructor comment.
     */
    public Field() {
        super();
    }

    /**
     * Default constructor which creates field.  
     * @param data         BitBuf which represents this field      
     */
    public Field(BitBuf data) {
        this.data = data;
    }
}