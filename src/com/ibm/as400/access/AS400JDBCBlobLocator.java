///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCBlobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;   //@G4A
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Vector;       //@D3A



// JDBC 2.0
/**
The AS400JDBCBlobLocator class provides access to binary large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCBlobLocator
implements Blob
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


    // Private data.
    private JDLobLocator    locator_;
    private Vector          bytesToUpdate_;            //@G5A
    private Vector          positionsToStartUpdates_;  //@G5A
    private Object          internalLock_;             //@G5A



/**
Constructs an AS400JDBCBlobLocator object.  The data for the
BLOB will be retrieved as requested, directly from the
server, using the locator handle.

@param  locator             The locator.
**/
    AS400JDBCBlobLocator (JDLobLocator locator)
    {
        locator_  = locator;
        internalLock_ = new Object();   //@G5A
    }



/**
Returns the entire BLOB as a stream of uninterpreted bytes.

@return The stream.

@exception  SQLException    If an error occurs.
**/
    public InputStream getBinaryStream ()
        throws SQLException
    {
        return new AS400JDBCInputStream (locator_);
    }



// @B3C
/**
Returns part of the contents of the BLOB.

@param  start       The position within the BLOB (1-based).
@param  length      The length to return.
@return             The contents.

@exception  SQLException    If the position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
    public byte[] getBytes (long start, int length)
        throws SQLException
    {
        --start;                                                        // @B3A
        //@H1 This is an unnecessary flow to the server.  The server will report an error
        //@H1 if our start or length numbers are invalid.
        //@H1D long end = start + length - 1;                                        // @G7A
        //@H1D long currentLengthOfLocator = locator_.getLength();  // @G7A
        //@H1D if (end >= currentLengthOfLocator)    // @G7A
        //@H1D|| (start >= currentLengthOfLocator) ||      
        if ((start < 0) || (length < 0))    //@G7A
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);  // @G7A
        DBLobData data = locator_.retrieveData ((int) start, length);   // @B1C
        int actualLength = data.getLength ();
        byte[] bytes = new byte[actualLength];
        System.arraycopy (data.getRawBytes (), data.getOffset (),
                          bytes, 0, actualLength);
        return bytes;
    }



//@G5A
/**
Returns the Vector of byte arrays that are currently cued up to update
the BLOB when ResultSet.updateBlob() is called.  The bytes 
were placed in the Vector as the user called setBytes on the BLOB.

@return             The current array of bytes to update.
**/
    Vector getBytesToUpdate()
    {
        return bytesToUpdate_;
    }



//@G5A
/**
Returns the handle to this BLOB locator in the database.

@return             The handle to this locator in the database.
**/
    int getHandle()
    {
        return locator_.getHandle();
    }



//@G5A
/**
Returns the internal lock to this BLOB locator so that the caller
can synchronize on it and update 
the positionsToStartUpdates_ and bytesToUpdates_ vectors.

@return             The internal lock to this BLOB locator.
**/
    Object getInternalLock()
    {
        return internalLock_;
    }



//@G5A
/**
Returns the Vector of positions where byte updates, that are currently cued up to update
the BLOB when ResultSet.updateBlob() is called, should start.  The positions 
were placed in the Vector as the user called setBytes on the BLOB.

@return             The current array of positions to start byte updates.
**/
    Vector getPositionsToStartUpdates()
    {
        return positionsToStartUpdates_;
    }



/**
Returns the length of the BLOB.

@return     The length of the BLOB, in bytes.

@exception SQLException     If an error occurs.
**/
    public long length ()
        throws SQLException
    {
        // @C1D // There is no way currently to efficiently compute the        @A1A
        // @C1D // actual length of the BLOB.  We have 2 choices:              @A1A
        // @C1D //                                                             @A1A
        // @C1D // 1. Retrieve the entire BLOB from 0 to max and the           @A1A
        // @C1D //    lob data will contain the actual length.                 @A1A
        // @C1D // 2. Return the max length here.                              @A1A
        // @C1D //                                                             @A1A
        // @C1D // I chose to implement 2. because 1. could be quite slow      @A1A
        // @C1D // and memory intensive.                                       @A1A

        // @C1D return locator_.getMaxLength ();                            // @A1A

        return locator_.getLength();                                        // @C1A
    }



// @B3C
/**
Returns the position at which a pattern is found in the BLOB.
This method is not supported.

@param  pattern     The pattern.
@param  start       The position within the BLOB to begin
                    searching (1-based).
@return             Always -1.  This method is not supported.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (byte[] pattern, long start)
        throws SQLException
    {
        // Validate the parameters.                                             // @B2A
        --start;                                                                // @B3A
        if ((start < 0) || (start >= length()) || (pattern == null))            // @B2A
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);    // @B2A

        return -1; // @A1C return locator_.position ("?", pattern, start);
    }



// @B3C
/**
Returns the position at which a pattern is found in the BLOB.
This method is not supported.

@param  pattern     The pattern.
@param  start       The position within the BLOB to begin
                    searching (1-based).
@return             Always -1.  This method is not supported.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (Blob pattern, long start)
        throws SQLException
    {
        // Validate the parameters.                                             // @B2A
        --start;                                                                // @B3A
        if ((start < 0) || (start >= length()) || (pattern == null))            // @B2A
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);    // @B2A

        return -1; // @A1A
        // @A1D if (pattern instanceof AS400JDBCBlobLocator)
        // @A1D     return locator_.position ("BLOB(?)", new Integer (locator_.getHandle()), start);
        // @A1D else
        // @A1D     return locator_.position ("?", pattern.getBytes(0, (int) pattern.length()), start);
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
        if ((positionToStartWriting <= 0) || (positionToStartWriting > locator_.getLength()))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

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
        if ((positionToStartWriting < 1) || (bytesToWrite == null) || (bytesToWrite.length < 0) || 
            positionToStartWriting > locator_.getLength())
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        positionToStartWriting--;

        setVectors(positionToStartWriting, bytesToWrite);

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
    public int setBytes (long positionToStartWriting, byte[] bytesToWrite, int offset, int length)
    throws SQLException
    {
        // Validate parameters
        if ((length < 0) || (offset < 0) || (bytesToWrite == null) || (bytesToWrite.length < 0)   //@H3C
            || positionToStartWriting > locator_.getLength())
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        //@H3D offset--;

        byte[] newData = new byte[length];
        System.arraycopy(bytesToWrite, offset, newData, 0, length);
        return setBytes(positionToStartWriting, newData);
    }



//@G5A
/**
Sets a position and byte array pair into the vectors that will be used to update the BLOB
when ResultSet.updateBlob() is called.

@param pos The position in the BLOB object at which to start writing (1-based).
@param bytes The array of bytes to be written to the BLOB value that this BLOB object represents.
**/
    void setVectors(long pos, byte[] bytes)
    {
        synchronized (internalLock_)
        {
            if (positionsToStartUpdates_ == null)
            {
                positionsToStartUpdates_ = new Vector();
            }
            if (bytesToUpdate_ == null)
            {
                bytesToUpdate_ = new Vector();
            }
            positionsToStartUpdates_.addElement(new Long (pos));
            bytesToUpdate_.addElement(bytes);
        }
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
        //parameter validation will be done in setBytes
        setBytes(lengthOfBLOB+1, new byte[0]);     //@G6C
    }
}
