///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCBlobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;



// JDBC 2.0
/**
The AS400JDBCBlobLocator class provides access to binary large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCBlobLocator
implements Blob
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private JDLobLocator    locator_;



/**
Constructs an AS400JDBCBlob object.  The data for the
blob will be retrieved as requested, directly from the
server, using the locator handle.

@param  locator             The locator.
**/
    AS400JDBCBlobLocator (JDLobLocator locator)
    {
        locator_  = locator;
    }



/**
Returns the entire blob as a stream of uninterpreted bytes.

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
Returns part of the contents of the blob.

@param  start       The position within the blob (1-based).
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
        DBLobData data = locator_.retrieveData ((int) start, length);   // @B1C
        int actualLength = data.getLength ();
        byte[] bytes = new byte[actualLength];
        System.arraycopy (data.getRawBytes (), data.getOffset (),
                          bytes, 0, actualLength);
        return bytes;
    }



/**
Returns the length of the blob.

@return     The length of the blob, in bytes.

@exception SQLException     If an error occurs.
**/
    public long length ()
        throws SQLException
    {
        // @C1D // There is no way currently to efficiently compute the        @A1A
        // @C1D // actual length of the blob.  We have 2 choices:              @A1A
        // @C1D //                                                             @A1A
        // @C1D // 1. Retrieve the entire blob from 0 to max and the           @A1A
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
Returns the position at which a pattern is found in the blob.
This method is not supported.

@param  pattern     The pattern.
@param  start       The position within the blob to begin
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
Returns the position at which a pattern is found in the blob.
This method is not supported.

@param  pattern     The pattern.
@param  start       The position within the blob to begin
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



}
