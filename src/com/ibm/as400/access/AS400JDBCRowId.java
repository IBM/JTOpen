///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCRowId.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2006-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;

/* ifdef JDBC40 
import java.sql.RowId;
endif */ 

//@PDA jdbc40 new class
public class AS400JDBCRowId 
/* ifdef JDBC40 
implements RowId,Serializable
endif */ 
/* ifndef JDBC40 */
implements Serializable
/* endif */ 
{
   
    private byte[] data_;
    //put byteToHex conversion here in case BinaryConverter is not in jar.
    private static final char[] c_ = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    /**
     * Constructs an AS400JDBCRowId object.  The data is contained
     * in the raw byte array.  No further communication with the IBM i system
     * is necessary.
     *
     * @param  data     The RowId data.
     **/
    AS400JDBCRowId(byte[] data)
    {
        data_ = data;
    }

  //JDBC40DOC     /**
  //JDBC40DOC      * Compares this <code>RowId</code> to the specified object. The result is 
  //JDBC40DOC      * <code>true</code> if and only if the argument is not null and is a RowId 
  //JDBC40DOC      * object that represents the same ROWID as  this object. 
  //JDBC40DOC      * <p>
  //JDBC40DOC      * It is important 
  //JDBC40DOC      * to consider both the origin and the valid lifetime of a <code>RowId</code>
  //JDBC40DOC      * when comparing it to another <code>RowId</code>. If both are valid, and 
  //JDBC40DOC      * both are from the same table on the same data source, then if they are equal
  //JDBC40DOC      * they identify 
  //JDBC40DOC      * the same row; if one or more is no longer guaranteed to be valid, or if 
  //JDBC40DOC      * they originate from different data sources, or different tables on the
  //JDBC40DOC      * same data source, they  may be equal but still
  //JDBC40DOC      * not identify the same row. 
  //JDBC40DOC      * 
  //JDBC40DOC      * @param obj the <code>Object</code> to compare this <code>RowId</code> object
  //JDBC40DOC      *     against.
  //JDBC40DOC      * @return true if the <code>RowId</code>s are equal; false otherwise
  //JDBC40DOC      */
    /* ifdef JDBC40 
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RowId))
        {
            return false;
        }

        byte[] otherBytes = ((RowId) obj).getBytes();
        if (data_.length != otherBytes.length)
            return false;

        for (int i = 0; i < data_.length; i++)
        {
            if (data_[i] != otherBytes[i])
                return false;
        }
        return true;
    }
    endif */ 
    
    /** 
     * Returns an array of bytes representing the value of the SQL <code>ROWID</code>
     * designated by this <code>java.sql.RowId</code> object.
     * 
     * @return an array of bytes, whose length is determined by the driver supplying
     *     the connection, representing the value of the ROWID designated by this
     *     java.sql.RowId object.  
     */
    public byte[] getBytes()
    {
        return data_;
    }

    /**
     * Returns a String representing the value of the SQL ROWID designated by this
     * <code>java.sql.RowId</code> object.
     * <p>
     * Like <code>java.sql.Date.toString()</code>
     * returns the contents of its DATE as the <code>String</code> "2004-03-17" 
     * rather than as  DATE literal in SQL (which would have been the <code>String</code>
     * DATE "2004-03-17"), toString() 
     * returns the contents of its ROWID in a form specific to the driver supplying 
     * the connection, and possibly not as a <code>ROWID</code> literal. 
     * Toolbox converts RowId bytes to HEX string format.
     * 
     * @return a String whose format is determined by the driver supplying the 
     *     connection, representing the value of the <code>ROWID</code> designated
     *     by this <code>java.sql.RowId</code>  object. 
     */
    public String toString()
    {
        return bytesToString(data_);
    }


    static final String bytesToString(final byte[] b)
    {
        return bytesToString(b, 0, b.length);
    }

    static final String bytesToString(final byte[] b, int offset, int length)
    {
        char[] c = new char[length*2];
        int num = bytesToString(b, offset, length, c, 0);
        return new String(c, 0, num);
    }


    static final int bytesToString(final byte[] b, int offset, int length, final char[] c, int coffset)
    {
        for(int i=0; i<length; ++i)
        {
            final int j = i*2;
            final byte hi = (byte)((b[i+offset]>>>4) & 0x0F);
            final byte lo = (byte)((b[i+offset] & 0x0F));
            c[j+coffset] = c_[hi];
            c[j+coffset+1] = c_[lo];
        }
        return length*2;
    }
    
    
    /**
     * Returns a hash code value of this <code>RowId</code> object.
     *
     * @return a hash code for the <code>RowId</code>
     */
    public int hashCode()
    {
        //for now, use String's implementation.  seems good enough.
        return this.toString().hashCode();  
    }
}
