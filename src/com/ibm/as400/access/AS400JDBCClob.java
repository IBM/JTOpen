///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCClob.java
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
import java.io.OutputStream;      //@G4A
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;            //@G4A
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
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private String              data_;
    private int                 length_;



/**
Constructs an AS400JDBCClob object.  The data is contained
in the String.  No further server communication is necessary.

@param  data     The CLOB data.
**/
    AS400JDBCClob (String data)
    {
        data_       = data;
        length_     = data.length ();
    }



/**
Returns the entire CLOB as a stream of ASCII characters.

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
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);    // @C2C
            return null;
        }
    }



/**
Returns the entire CLOB as a character stream.

@return The stream.

@exception  SQLException    If an error occurs.
**/
    public Reader getCharacterStream ()
    throws SQLException
    {
        return new StringReader (data_);
    }



// @B1C
/**
Returns part of the contents of the CLOB.

@param  start       The start position within the CLOB (1-based).
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
Returns the length of the CLOB.

@return     The length of the CLOB, in characters.

@exception SQLException     If an error occurs.
**/
    public long length ()
    throws SQLException
    {
        return length_;
    }



// @B1C
/**
Returns the position at which a pattern is found in the CLOB.

@param  pattern     The pattern.
@param  start       The position within the CLOB to begin
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
Returns the position at which a pattern is found in the CLOB.

@param  pattern     The pattern.
@param  start       The position within the CLOB to begin
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

        return data_.indexOf (pattern.getSubString (1, (int) pattern.length ()), (int) start); // @C1C
    }


    //@G4A  JDBC 3.0
    /**
    Returns a stream that an application can use to write Ascii characters to this CLOB.
    The stream begins at position <i>positionToStartWriting</i>, and the CLOB will be truncated 
    after the last character of the write.
    
    @param positionToStartWriting The position (1-based) in the CLOB where writes should start.
    @return An OutputStream object to which data can be written by an application.
    @exception SQLException If there is an error accessing the CLOB or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public OutputStream setAsciiStream(long positionToStartWriting)
    throws SQLException
    {
        if (positionToStartWriting <= 0 || positionToStartWriting > length_)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        //Didn't decrement the position here even though clobs are 1-based and output streams
        //are 0-based because output stream uses this number for what position to call
        //setString() on in the clob, which is 1-based.
        return new AS400JDBCLobOutputStream (this, positionToStartWriting); 
    }



    //@G4A  JDBC 3.0
    /**
    Returns a stream that an application can use to write a stream of Unicode characters to 
    this CLOB.  The stream begins at position <i>positionToStartWriting</i>, and the CLOB will 
    be truncated after the last character of the write.

    @param positionToStartWriting The position (1-based) in the CLOB where writes should start.
    @return An OutputStream object to which data can be written by an application.
    @exception SQLException If there is an error accessing the CLOB or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public Writer setCharacterStream (long positionToStartWriting)
    throws SQLException
    {
        if (positionToStartWriting <= 0 || positionToStartWriting > length_)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        //Didn't decrement the position here even though clobs are 1-based and writers
        //are 0-based because writer uses this number for what position to call
        //setString() on in the clob, which is 1-based.
        return new AS400JDBCLobWriter (this, positionToStartWriting);  
    }



    //@G4A  JDBC 3.0
    /**
    Writes a String to this CLOB, starting at position <i>positionToStartWriting</i>.  The CLOB 
    will be truncated after the last character written.

    @param positionToStartWriting The position (1-based) in the CLOB where writes should start.
    @param stringToWrite The string that will be written to the CLOB.
    @return The number of characters that were written.

    @exception SQLException If there is an error accessing the CLOB or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public int setString (long positionToStartWriting, String stringToWrite)
    throws SQLException
    {
        // Validate the parameters.
        if ((positionToStartWriting > length_) || (positionToStartWriting <= 0) || (stringToWrite == null))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        positionToStartWriting--;

        data_ = data_.substring(0, (int)positionToStartWriting) + stringToWrite;

        length_ = data_.length();

        return stringToWrite.length();
    }



    //@G4A  JDBC 3.0
    /**
    Writes a String to this CLOB, starting at position <i>positionToStartWriting</i> in the CLOB.  
    The CLOB will be truncated after the last character written.  The <i>lengthOfWrite</i>
    characters written will start from <i>offset</i> in the string that was provided by the
    application.

    @param positionToStartWriting The position (1-based) in the CLOB where writes should start.
    @param string The string that will be written to the CLOB.
    @param offset The offset into string to start reading characters (0-based).
    @param lengthOfWrite The number of characters to write.
    @return The number of characters written.

    @exception SQLException If there is an error accessing the CLOB value or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public int setString (long positionToStartWriting, String string, int offset, int lengthOfWrite)
    throws SQLException
    {
        if ((lengthOfWrite < 0) || (offset < 0) || (string == null))            //@H2C
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        //@H2D offset--;

        return setString(positionToStartWriting, string.substring(offset, lengthOfWrite));
    }



    //@G4A  JDBC 3.0
    /**
    Truncates this CLOB to a length of <i>lengthOfCLOB</i> characters.
     
    @param lengthOfCLOB The length, in characters, that this CLOB should be after 
    truncation.
     
    @exception SQLException If there is an error accessing the CLOB or if the length
    specified is greater than the length of the CLOB. 
    
    @since Modification 5   
    **/
    public void truncate(long lengthOfCLOB)
    throws SQLException
    {
        //parameter checking will be done in setString method 
        setString(lengthOfCLOB+1, "");  //@G5C length should be 1 more since setString decrements
        //@G5C by 1 since it assumes 1-based positions are passed in
    }




}
