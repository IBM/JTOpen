///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400JDBCClobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;        // @A1A
import java.sql.Clob;
import java.sql.SQLException;



// JDBC 2.0
/**
The AS400JDBCClobLocator class provides access to character large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCClobLocator
implements Clob
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private ConverterImplRemote converter_;
    private JDLobLocator        locator_;



/**
Constructs an AS400JDBCClob object.  The data for the
clob will be retrieved as requested, directly from the
server, using the locator handle.

@param  locator             The locator.
@param  converter           The text converter.
**/
    AS400JDBCClobLocator (JDLobLocator locator,
                          ConverterImplRemote converter)
    {
        locator_  = locator;
        converter_ = converter;
    }



/**
Returns the entire clob as a stream of ASCII characters.

@return The stream.

@exception  SQLException    If an error occurs.
**/
    public InputStream getAsciiStream ()
        throws SQLException
    {
        return new AS400JDBCInputStream (locator_, converter_, "ISO8859_1");
    }



/**
Returns the entire clob as a character stream.

@return The stream.

@exception  SQLException    If an error occurs.
**/
    public Reader getCharacterStream ()
        throws SQLException
    {
        try {                                                                   // @A1A
            return new InputStreamReader (new AS400JDBCInputStream (locator_), converter_.getEncoding ()); // @A1C
        }                                                                       // @A1A
        catch (UnsupportedEncodingException e) {                                // @A1A
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);                // @A1A
            return null;                                                        // @A1A
        }                                                                       // @A1A
    }



/**
Copyright.
**/
    static private String getCopyright ()
    {
        return Copyright.copyright;
    }
    


// @B2C
/**
Returns part of the contents of the clob.

@param  start       The position within the clob (1-based).
@param  length      The length to return.
@return             The contents.

@exception  SQLException    If the position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
    public String getSubString (long start, int length)
        throws SQLException
    {
        --start;                                                                // @B2A
        DBLobData data = locator_.retrieveData ((int) start, length);           // @B1C
        String substring = converter_.byteArrayToString (data.getRawBytes (),
                                                         data.getOffset (),
                                                         data.getLength ());
        return substring;
    }



/**
Returns the length of the clob.

@return     The length of the clob, in characters.

@exception SQLException     If an error occurs.
**/
    public long length ()
        throws SQLException
    {
        // There is no way currently to efficiently compute the        @A1A
        // actual length of the clob.  We have 2 choices:              @A1A
        //                                                             @A1A
        // 1. Retrieve the entire clob from 0 to max and the           @A1A
        //    lob data will contain the actual length.                 @A1A
        // 2. Return the max length here.                              @A1A
        //                                                             @A1A
        // I chose to implement 2. because 1. could be quite slow      @A1A
        // and memory intensive.                                       @A1A

        return locator_.getMaxLength ();                            // @A1A
    }



// @B2C
/**
Returns the position at which a pattern is found in the clob.
This method is not supported.

@param  pattern     The pattern.
@param  start       The position within the clob to begin
                    searching (1-based).
@return             Always -1.  This method is not supported.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (String pattern, long start)
        throws SQLException
    {
        return -1; // @A1C return locator_.position ("?", pattern, start);
    }



// @B2C
/**
Returns the position at which a pattern is found in the clob.
This method is not supported.

@param  pattern     The pattern.
@param  start       The position within the clob to begin
                    searching (1-based).
@return             Always -1.  This method is not supported.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
    public long position (Clob pattern, long start)
        throws SQLException
    {
        return -1; // @A1A
        // @A1D if (pattern instanceof AS400JDBCClobLocator)
        // @A1D     return locator_.position ("CLOB(?)", new Integer (locator_.getHandle()), start);
        // @A1D else
        // @A1D     return locator_.position ("?", pattern.getBytes(0, (int) pattern.length()), start);
    }



}
