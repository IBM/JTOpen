///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400JDBCBlob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;



// JDBC 2.0
/**
The AS400JDBCBlob class provides access to binary large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCBlob
implements Blob
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private DBByteSubarray		data_;
    private int                 length_;



/**
Constructs an AS400JDBCBlob object.  The data is contained
in the raw byte array.  No further server communication
is necessary.

@param  data     The blob data.
**/
    AS400JDBCBlob (DBByteSubarray data)
    {
	    data_		    = data;
        length_  		= data.getLength ();
    }



/**
Constructs an AS400JDBCBlob object.  The data is contained
in the raw byte array.  No further server communication
is necessary.

@param  data     The blob data.
**/
    AS400JDBCBlob (byte[] data)
    {
        length_ = data.length;
	    data_	= new DBByteSubarray (length_);
        data_.overlay (data, 0);
    }



/**
Returns the position at which a pattern is found in
a byte array.

@param  rawBytes    The raw bytes.
@param  offset      The offset into the raw bytes.
@param  pattern     The pattern.
@param  start       The position within the byte array to
                    begin searching.
@return             The position at which the pattern is
                    found, or -1 if the pattern is not found.
**/
    static int byteSearch (byte[] rawBytes,
                           int offset,
                           byte[] pattern,
                           int start)
    {
        int firstIndex = offset + start;
	    int lastIndex = offset + rawBytes.length - pattern.length;
	    loop: for (int i = firstIndex; i <= lastIndex; ++i) {
	        int match = pattern.length;
	        int j = i;
	        int k = offset;
	        while (match-- > 0) {
		        if (rawBytes[j++] != pattern[k++])
		            continue loop;
            }
    	    return i - offset;
        }
	    return -1;
    }



/**
Returns the entire blob as a stream of uninterpreted bytes.

@return The stream.

@exception  SQLException    If an error occurs.
**/
    public InputStream getBinaryStream ()
        throws SQLException
    {
        return new ByteArrayInputStream (data_.getRawBytes (), data_.getOffset (), length_);
    }



// @B1C
/**
Returns part of the contents of the blob.

@param  start       The start position within the blob (1-based).
@param  length      The length to return.
@return             The contents.

@exception  SQLException    If the start position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
    public byte[] getBytes (long start, int length)
        throws SQLException
    {
        // Validate the parameters.
        --start;                                                        // @B1A
        long end = start + length - 1;
        if ((start < 0) || (length < 0) || (end >= length_) || (start >= length_))   // @B2C
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

	    // Copy the bytes.
        byte[] result = new byte[length];
        System.arraycopy (data_.getRawBytes (), data_.getOffset () + (int) start, result, 0, length);
	    return result;
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }
    


/**
Returns the length of the blob.

@return     The length of the blob, in bytes.

@exception SQLException     If an error occurs.
**/
    public long length ()
        throws SQLException
    {
        return length_;
    }



// @B1C
/**
Returns the position at which a pattern is found in the blob.

@param  pattern     The pattern.
@param  start       The position within the blob to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (byte[] pattern, long start)
        throws SQLException
    {
        // Validate the parameters.
        --start; // @B1A
        if ((start < 0) || (start >= length_) || (pattern == null))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return byteSearch (data_.getRawBytes (), data_.getOffset (), pattern, (int) start);
    }



// @B1C
/**
Returns the position at which a pattern is found in the blob.

@param  pattern     The pattern.
@param  start       The position within the blob to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (Blob pattern, long start)
        throws SQLException
    {
        // Validate the parameters.
        --start; // @B1A
        if ((start < 0) || (start >= length_) || (pattern == null))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return byteSearch (data_.getRawBytes (), data_.getOffset (),
                           pattern.getBytes (0, (int) pattern.length ()), 
                           (int) start);
    }



}
