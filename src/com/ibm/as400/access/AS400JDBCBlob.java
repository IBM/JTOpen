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

@param  data     The BLOB data.
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

@param  data     The BLOB data.
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
Returns the entire BLOB as a stream of uninterpreted bytes.

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
Returns part of the contents of the BLOB.

@param  start       The start position within the BLOB (1-based).
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
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

	    // Copy the bytes.
        byte[] result = new byte[length];
        System.arraycopy (data_.getRawBytes (), data_.getOffset () + (int) start, result, 0, length);
	    return result;
    }



/**
Returns the length of the BLOB.

@return     The length of the BLOB, in bytes.

@exception SQLException     If an error occurs.
**/
    public long length ()
        throws SQLException
    {
        return length_;
    }



// @B1C
/**
Returns the position at which a pattern is found in the BLOB.

@param  pattern     The pattern.
@param  start       The position within the BLOB to begin
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
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return byteSearch (data_.getRawBytes (), data_.getOffset (), pattern, (int) start);
    }



// @B1C
/**
Returns the position at which a pattern is found in the BLOB.

@param  pattern     The pattern.
@param  start       The position within the BLOB to begin
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
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return byteSearch (data_.getRawBytes (), data_.getOffset (),
                           pattern.getBytes (0, (int) pattern.length ()), 
                           (int) start);
    }



    //@G4A  JDBC 3.0
    /**
    Returns a stream that an application can use to write to this BLOB.
    The stream begins at position <i>positionToStartWriting</i>, and the BLOB will be truncated 
    after the last byte of the write.

    @param positionToStartWriting The position (1-based) in the BLOB where writes should start.
    @return An OutputStream object to which data can be written by an application.
    @exception SQLException If there is an error accessing the BLOB or if the position
    specified is greater than the length of the BLOB.
    
    @since Modification 5
    **/
    public OutputStream setBinaryStream(long positionToStartWriting)
    throws SQLException
    {
        if ((positionToStartWriting <= 0) || (positionToStartWriting > length_))
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return new AS400JDBCLobOutputStream (this, positionToStartWriting);
    }



    //@G4A  JDBC 3.0
    /**
    Writes an array of bytes to this BLOB, starting at position <i>positionToStartWriting</i> 
    in the BLOB.  The BLOB will be truncated after the last byte written.  
    
    @param positionToStartWriting The position (1-based) in the BLOB where writes should start.
    @param bytesToWrite The array of bytes to be written to this BLOB.
    @return The number of bytes written to the BLOB.

    @exception SQLException If there is an error accessing the BLOB or if the position
    specified is greater than the length of the BLOB.
    
    @since Modification 5
    **/
    public int setBytes (long positionToStartWriting, byte[] bytesToWrite)
    throws SQLException
    {
        // Validate parameters.
        if ((positionToStartWriting > length_) || (positionToStartWriting <= 0) || 
            (bytesToWrite == null) || (bytesToWrite.length < 0))
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        positionToStartWriting--;

        length_ = (int)positionToStartWriting + bytesToWrite.length;  //@G5D + 1; Do not add one to this length.  

        if ((positionToStartWriting >= length_))
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        byte[] data = new byte[length_];
        System.arraycopy (data_.getRawBytes(), data_.getOffset(), data, 0, (int)positionToStartWriting);
        System.arraycopy (bytesToWrite, 0, data, (int)positionToStartWriting, bytesToWrite.length);
        data_.overlay(data, 0);
        data_.setLength(length_);
        return bytesToWrite.length;
    }



    //@G4A  JDBC 3.0
    /**
    Writes all or part of the byte array the application passes in to this BLOB, 
    starting at position <i>positionToStartWriting</i> in the BLOB.  
    The BLOB will be truncated after the last byte written.  The <i>lengthOfWrite</i>
    bytes written will start from <i>offset</i> in the bytes that were provided by the
    application.

    @param positionToStartWriting The position (1-based) in the BLOB where writes should start.
    @param bytesToWrite The array of bytes to be written to this BLOB.
    @param offset The offset into the array at which to start reading bytes (0-based).
    @param length The number of bytes to be written to the BLOB from the array of bytes.
    @return The number of bytes written.

    @exception SQLException If there is an error accessing the BLOB or if the position
    specified is greater than the length of the BLOB.
    
    @since Modification 5
    **/
    public int setBytes (long positionToStartWriting, byte[] bytesToWrite, int offset, 
                         int lengthOfWrite)
    throws SQLException
    {
        // Validate parameters
        if ((lengthOfWrite < 0) || (offset < 0) || (bytesToWrite == null) ||        //@H2C
            (bytesToWrite.length < 0) 
            || (positionToStartWriting <= 0) ||           //@H3A Added cases
            (positionToStartWriting > length_) || (offset + lengthOfWrite > bytesToWrite.length)) //@H3A Added cases
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID); 

        //@H2D offset--;
        byte[] newData = new byte[lengthOfWrite];
        System.arraycopy(bytesToWrite, offset, newData, 0, lengthOfWrite);
        return setBytes(positionToStartWriting, newData);
    }



    //@G4A  JDBC 3.0
    /**
    Truncates this BLOB to a length of <i>lengthOfBLOB</i> bytes.
     
    @param lengthOfBLOB The length, in bytes, that this BLOB should be after 
    truncation.

    @exception SQLException If there is an error accessing the BLOB or if the length
    specified is greater than the length of the BLOB. 
    
    @since Modification 5   
    **/
    public void truncate(long lengthOfBLOB)
    throws SQLException
    {
        if ((lengthOfBLOB < 0) || (lengthOfBLOB > length_))
            JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        byte[] newData = new byte[(int)lengthOfBLOB];
        System.arraycopy(data_.getRawBytes(), data_.getOffset(), newData, 0, (int)lengthOfBLOB);
        length_ = newData.length;
        data_.overlay(newData, 0);
        data_.setLength(length_);
    }


}
