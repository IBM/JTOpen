///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400JDBCClob.java
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
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Clob;
import java.sql.SQLException;



// JDBC 2.0
/**
The AS400JDBCClob class provides access to character large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCClob
implements Clob
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private String      		data_;
    private int                 length_;



/**
Constructs an AS400JDBCClob object.  The data is contained
in the String.  No further server communication is necessary.

@param  data     The clob data.
**/
    AS400JDBCClob (String data)
    {
	    data_		= data;
        length_     = data.length ();
    }



/**
Returns the entire clob as a stream of ASCII characters.

@return The stream.

@exception  SQLException    If an error occurs.
**/
    public InputStream getAsciiStream ()
        throws SQLException
    {
	    try {
            return new ByteArrayInputStream (data_.getBytes ("ISO8859_1"));
        }
        catch (UnsupportedEncodingException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL);
            return null;
        }
    }



/**
Returns the entire clob as a character stream.

@return The stream.

@exception  SQLException    If an error occurs.
**/
    public Reader getCharacterStream ()
        throws SQLException
    {
        return new StringReader (data_);
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }
    


// @B1C
/**
Returns part of the contents of the clob.

@param  start       The start position within the clob (1-based).
@param  length      The length to return.
@return             The contents.

@exception  SQLException    If the start position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
    public String getSubString (long start, int length)
        throws SQLException
    {
        // Validate the parameters.
        --start;                                                        // @B1A
        long end = start + length - 1;
        if ((start < 0) || (length < 0) || (end >= length_) || (start >= length_))    // @B2C
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

	    // Generate the substring.
        return data_.substring ((int) start, (int) start + length);
    }



/**
Returns the length of the clob.

@return     The length of the clob, in characters.

@exception SQLException     If an error occurs.
**/
    public long length ()
        throws SQLException
    {
        return length_;
    }



// @B1C
/**
Returns the position at which a pattern is found in the clob.

@param  pattern     The pattern.
@param  start       The position within the clob to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (String pattern, long start)
        throws SQLException
    {
        // Validate the parameters.
        --start;                                                            // @B1A
        if ((start < 0) || (start >= length_) || (pattern == null))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return data_.indexOf (pattern, (int) start);
    }



// @B1C
/**
Returns the position at which a pattern is found in the clob.

@param  pattern     The pattern.
@param  start       The position within the clob to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (Clob pattern, long start)
        throws SQLException
    {
        // Validate the parameters.
        --start;                                                                // @B1A
        if ((start < 0) || (start >= length_) || (pattern == null))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return data_.indexOf (pattern.getSubString (0, (int) pattern.length ()), (int) start);
    }



}
