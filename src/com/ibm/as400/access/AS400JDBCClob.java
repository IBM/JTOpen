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
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);    // @C2C
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

        return data_.indexOf (pattern.getSubString (1, (int) pattern.length ()), (int) start); // @C1C
    }


    //@G4A  JDBC 3.0
    /**
    Returns a stream to be used to write Ascii characters to the CLOB value 
    that this CLOB designates, starting at position <i>pos</i>.
    
    @param pos The position (1-based) in the CLOB value at which to start writing.
    @return An OutputStream object to which data can be written.
    @exception SQLException If there is an error accessing the CLOB value or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public OutputStream setAsciiStream(long pos)
    throws SQLException
    {
        if (pos <= 0 || pos > length_)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID); 

        return new AS400JDBCLobOutputStream (this, pos); 
    }



    //@G4A  JDBC 3.0
    /**
    Returns a stream to be used to write a stream of Unicode characters to the CLOB value 
    that this CLOB designates, at position <i>pos</i>. The stream begins at position pos.

    @param pos The position (1-based) in the CLOB value at which to start writing.
    @return An OutputStream object to which data can be written.
    @exception SQLException If there is an error accessing the CLOB value or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public Writer setCharacterStream (long pos)
    throws SQLException
    {
        if (pos <= 0 || pos > length_)
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        return new AS400JDBCLobWriter (this, pos);  
    }



    //@G4A  JDBC 3.0
    /**
    Writes a String to the CLOB value that this CLOB object designates at the position <i>pos</i>.

    @param pos The position (1-based) in the CLOB object at which to start writing.
    @param string The string to be written to the CLOB value that this CLOB object 
    represents
    @return The number of characters written.

    @exception SQLException If there is an error accessing the CLOB value or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public int setString (long pos, String str)
    throws SQLException
    {
        // Validate the parameters.
        if ((pos > length_) || (pos <= 0) || (str == null))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        pos--;

        data_ = data_.substring(0, (int)pos) + str;

        length_ = data_.length();

        return str.length();
    }



    //@G4A  JDBC 3.0
    /**
    Writes <i>len</i> characters of a String starting at character <i>offset</i>, to the CLOB value 
    that this Clob designates.

    @param pos The position (1-based) in the CLOB object at which to start writing.
    @param string The string to be written to the CLOB value that this CLOB object 
    represents
    @param offset The offset into str to start writing the characters (1-based).
    @param len The number of characters to be written
    @return The number of characters written.

    @exception SQLException If there is an error accessing the CLOB value or if the position
    specified is greater than the length of the CLOB.
    
    @since Modification 5
    **/
    public int setString (long pos, String str, int offset, int len)
    throws SQLException
    {
        if ((len < 0) || (offset <= 0) || (str == null))
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        offset--;

        return setString(pos, str.substring(offset,len));
    }



    //@G4A  JDBC 3.0
    /**
    Truncates the CLOB value that this CLOB object represents to be <i>len</i> characters in length.
     
    @param len The length, in characters, to which the CLOB value that this CLOB object 
    represents should be truncated.
     
    @exception SQLException If there is an error accessing the CLOB value or if the length
    specified is greater than the length of the CLOB. 
    
    @since Modification 5   
    **/
    public void truncate(long len)
    throws SQLException
    {
        //parameter checking will be done in setString method 
        setString(len, "");
    }




}
