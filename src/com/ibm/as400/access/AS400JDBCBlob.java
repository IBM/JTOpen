///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCBlob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;            //@G4A
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
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




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



    //@G4A  JDBC 3.0
    /**
    Retrieves a stream that can be used to write to the BLOB value that this Blob object 
    represents. The stream begins at position pos.

    @param pos the position in the BLOB value at which to start writing.
    @return a java.io.OutputStream object to which data can be written.
    @exception SQLException if there is an error accessing the BLOB value.
    
    @since Modification 5
    **/
    public OutputStream setBinaryStream(long pos)
    throws SQLException
    {
        if ((pos <= 0) || (pos > length_))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        //Currently, this method is not supported for lob locators
        JDError.throwSQLException (JDError.EXC_FUNCTION_NOT_SUPPORTED);

        return new AS400JDBCLobOutputStream (this, pos);
    }



    //@G4A  JDBC 3.0
    /**
    Writes the array of bytes to this Blob object starting at position pos.

    @param pos The position in the BLOB object at which to start writing (1-based).
    @param bytes The array of bytes to be written to the BLOB value that this Blob object 
    represents.
    @return the number of bytes written.

    @exception SQLException if there is an error accessing the BLOB value.
    
    @since Modification 5
    **/
    public int setBytes (long pos, byte[] bytes)
    throws SQLException
    {
        pos--;
        if ((pos >= length_) || (pos < 0) || (bytes == null) || (bytes.length < 0))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        length_ = (int)pos + bytes.length + 1;

        if ((pos >= length_))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        byte[] data = new byte[length_];
        System.arraycopy (data_.getRawBytes(), data_.getOffset(), data, 0, (int)pos);
        System.arraycopy (bytes, 0, data, (int)pos, bytes.length);
        data_.overlay(data, 0);
        data_.setLength(length_);
        return bytes.length;
    }



    //@G4A  JDBC 3.0
    /**
    Writes all or part of the given byte array to the BLOB value that this Blob object
    represents.  Writing starts at position pos in the BLOB value; len bytes
    from the given byte array are written.

    @param pos the position in the BLOB object at which to start writing (1-based).
    @param bytes the array of bytes to be written to the BLOB value that this Blob object represents.
    @param offset the offset into the array bytes at which to start reading the bytes to be set 
    (1-based).
    @param len the number of bytes to be written to the BLOB value from the array of bytes bytes.
    @return the number of bytes written.

    @exception SQLException if there is an error accessing the BLOB value.
    
    @since Modification 5
    **/
    public int setBytes (long pos, byte[] bytes, int offset, int len)
    throws SQLException
    {
        offset--;
        if ((len < 0) || (offset < 0) || (bytes == null) || (bytes.length < 0))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);
        byte[] newData = new byte[len];
        System.arraycopy(bytes, offset, newData, 0, len);
        return setBytes(pos, newData);
    }



    //@G4A  JDBC 3.0
    /**
    Truncates the BLOB value that this Blob object represents to be len bytes in length.
     
    @param len the length, in bytes, to which the BLOB value that this Blob object 
    represents should be truncated.
     
    @exception SQLException if there is an error accessing the BLOB value. 
    
    @since Modification 5   
    **/
    public void truncate(long len)
    throws SQLException
    {
        if ((len < 0) || (len > length_))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        byte[] newData = new byte[(int)len];
        System.arraycopy(data_.getRawBytes(), data_.getOffset(), newData, 0, (int)len);
        length_ = newData.length;
        data_.overlay(newData, 0);
        data_.setLength(length_);
    }


}
