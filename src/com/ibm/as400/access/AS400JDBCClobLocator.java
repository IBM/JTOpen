///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCClobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;                        // @G4A
import java.io.Reader;
import java.io.UnsupportedEncodingException;        // @A1A
import java.io.Writer;                              // @G4A
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Vector;                            // @G5A



// JDBC 2.0
/**
The AS400JDBCClobLocator class provides access to character large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCClobLocator
implements Clob
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private ConvTable converter_; //@P0C
    private JDLobLocator        locator_;
    private Vector          stringsToUpdate_;          //@G5A
    private Vector          positionsToStartUpdates_;  //@G5A
    private Object          internalLock_;             //@G5A



    /**
    Constructs an AS400JDBCClob object.  The data for the
    CLOB will be retrieved as requested, directly from the
    server, using the locator handle.
    
    @param  locator             The locator.
    @param  converter           The text converter.
    **/
    AS400JDBCClobLocator (JDLobLocator locator,
                          ConvTable converter) //@P0C
    {
        locator_  = locator;
        converter_ = converter;
        internalLock_ = new Object();       //@G5A
    }



    /**
    Returns the entire CLOB as a stream of ASCII characters.
    
    @return The stream.
    
    @exception  SQLException    If an error occurs.
    **/
    public InputStream getAsciiStream ()
    throws SQLException
    {
        return new AS400JDBCInputStream (locator_, converter_, "ISO8859_1");
    }



    /**
    Returns the entire CLOB as a character stream.
    
    @return The stream.
    
    @exception  SQLException    If an error occurs.
    **/
    public Reader getCharacterStream ()
    throws SQLException
    {
        try {                                                                   // @A1A
            //@C2D return new InputStreamReader (new AS400JDBCInputStream (locator_), converter_.getEncoding ()); // @A1C
            return new ConvTableReader (new AS400JDBCInputStream (locator_), converter_.getCcsid()); // @C2A
        }                                                                       // @A1A
        catch (UnsupportedEncodingException e) {                                // @A1A
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);                // @A1A
            return null;                                                        // @A1A
        }                                                                       // @A1A
    }



//@G5A
/**
Returns the handle to this CLOB locator in the database.

@return             The handle to this locator in the databaes.
**/
    int getHandle()
    {
        return locator_.getHandle();
    }



//@G5A
/**
Returns the internal lock to this CLOB locator so that the caller
can synchronize on it and update 
the positionsToStartUpdates_ and stringsToUpdate_ vectors.

@return             The internal lock to this CLOB locator.
**/
    Object getInternalLock()
    {
        return internalLock_;
    }



//@G5A
/**
Returns the array of positions where string updates, that are currently queued up to update
the CLOB when ResultSet.updateClob() is called, should start.  The positions 
were placed in the Vector as the user called setString on the CLOB.

@return             The current array of positions to start string updates.
**/
    Vector getPositionsToStartUpdates()
    {
        return positionsToStartUpdates_;
    }



    // @B2C
    /**
    Returns part of the contents of the CLOB.
    
    @param  start       The position within the CLOB (1-based).
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

        //@H1 This is an unnecessary flow to the server.  The server will report an error
        //@H1 if our start or length numbers are invalid.
        //@H1D long end = start + length - 1;                                   // @G7A
        //@H1D long lengthOfLocatorInChars = locator_.getLengthInCharacters();  // @G7A
        //@H1D if (end >= lengthOfLocatorInChars)                               // @G7A          
        //@H1D    || (start >= lengthOfLocatorInChars) ||                       // @G7A
        if ((start < 0) || (length < 0))                                            // @G7A
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);        // @G7A

        // @C4 A graphic locator means two bytes per character.  Locator_.retrieveData
        //     expects bytes, but this method has input parms (start/length) in 
        //     characters.  Convert them to bytes so retrieveData works properly
        if (locator_.isGraphic())      // @C4a 2
        {                              // @C4a 2
            start = start * 2;         // @C4a 2
            length = length * 2;       // @C4a 2
        }                              // @C4a 2

        DBLobData data = locator_.retrieveData ((int) start, length);           // @B1C
        String substring = converter_.byteArrayToString (data.getRawBytes (),
                                                         data.getOffset (),
                                                         data.getLength ());
        return substring;
    }



//@G5A
/**
Returns the array of strings that are currently cued up to update
the CLOB when ResultSet.updateClob() is called.  The strings 
were placed in the Vector as the user called setString on the CLOB.

@return             The current array of bytes to update.
**/
    Vector getStringsToUpdate()
    {
        return stringsToUpdate_;
    }



    /**
    Returns the length of the CLOB.
    
    @return     The length of the CLOB, in characters.
    
    @exception SQLException     If an error occurs.
    **/
    public long length ()
    throws SQLException
    {
        // @C1D // There is no way currently to efficiently compute the        @A1A
        // @C1D // actual length of the CLOB.  We have 2 choices:              @A1A
        // @C1D //                                                             @A1A
        // @C1D // 1. Retrieve the entire CLOB from 0 to max and the           @A1A
        // @C1D //    lob data will contain the actual length.                 @A1A
        // @C1D // 2. Return the max length here.                              @A1A
        // @C1D //                                                             @A1A
        // @C1D // I chose to implement 2. because 1. could be quite slow      @A1A
        // @C1D // and memory intensive.                                       @A1A

        // @C1D return locator_.getMaxLength ();                            // @A1A
        return locator_.getLengthInCharacters();                            // @C1A @C4c 2
    }



    // @B2C
    /**
    Returns the position at which a pattern is found in the CLOB.
    This method is not supported.
    
    @param  pattern     The pattern.
    @param  start       The position within the CLOB to begin
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
    Returns the position at which a pattern is found in the CLOB.
    This method is not supported.
    
    @param  pattern     The pattern.
    @param  start       The position within the CLOB to begin
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
        if (positionToStartWriting <= 0 || positionToStartWriting > locator_.getLength())
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

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
        if (positionToStartWriting <= 0 || positionToStartWriting > locator_.getLength())
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

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
    public int setString (long positionToStartWriting, String string)
    throws SQLException
    {
        // Validate the parameters.
        if ((positionToStartWriting < 1) || (string == null) || positionToStartWriting > locator_.getLength())
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        positionToStartWriting--;

        setVectors(positionToStartWriting, string);  

        return string.length();
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
        // Validate the parameters
        if ((lengthOfWrite < 0) || (offset < 0) || (string == null) || positionToStartWriting > locator_.getLength())  //@H3C
            JDError.throwSQLException (JDError.EXC_ATTRIBUTE_VALUE_INVALID);

        //@H3D offset--;

        return setString(positionToStartWriting, string.substring(offset,lengthOfWrite));
    }



//@G5A
/**
Sets a position and string pair into the vectors that will be used to update the CLOB
when ResultSet.updateClob() is called.

@param pos The position in the CLOB object at which to start writing (1-based).
@param string The string to be written to the CLOB value that this CLOB object represents.
**/
    void setVectors(long pos, String string)
    {
        synchronized (internalLock_)
        {
            if (positionsToStartUpdates_ == null)
            {
                positionsToStartUpdates_ = new Vector();
            }
            if (stringsToUpdate_ == null)
            {
                stringsToUpdate_ = new Vector();
            }
            positionsToStartUpdates_.addElement(new Long (pos));
            stringsToUpdate_.addElement(string);
        }
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
        setString(lengthOfCLOB+1, "");        //@G6C
    }


}
