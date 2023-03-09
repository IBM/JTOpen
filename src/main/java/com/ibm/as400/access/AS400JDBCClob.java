///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCClob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.CharArrayReader;
import java.sql.Clob;
import java.sql.SQLException;

// Note: This code in this class requires understanding of bit manipulation
// and sign extension. Do not attempt to rework this code if you do not
// have a grasp of these concepts.

// Currently, the database host server only supports 2 GB LOBs. Therefore,
// we validate any long parameters to make sure they are not greater than
// the maximum positive value for a 4-byte int (2 GB). This has the added
// bonus of being able to cast the long down to an int without worrying
// about sign extension. There are some cases where we could allow the
// user to pass in a long greater than 2 GB, but for consistency, we will
// throw an exception.

// Offset refers to a 0-based index. Position refers to a 1-based index.


/**
The AS400JDBCClob class provides access to character large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCClob implements Clob, Serializable
{
  static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

  protected char[] data_;                                 //@pdc jdbc40
  protected int maxLength_;                               //@pdc jdbc40
  static final int MAX_LOB_SIZE = 2147483647;   //@PDA jdbc40 same as native driver. (if column is a DBCLOB the limit is 1,073,741,823)
  private boolean isXML_ = false;      //@xmltrim true if this data originated from a native XML column type
  
/**
Constructs an AS400JDBCClob object.  The data is contained
in the String.  No further communication with the IBM i system is necessary.

@param  data     The CLOB data.
**/
  AS400JDBCClob(String data, int maxLength)
  {
    data_ = data.toCharArray();
    maxLength_ = maxLength;
  }

  AS400JDBCClob(String data)
  {
    data_ = data.toCharArray();
    maxLength_ = MAX_LOB_SIZE; 
  }


  AS400JDBCClob(char[] data)
  {
    data_ = data;
    maxLength_ = MAX_LOB_SIZE; 
  }
  
  //@xmltrim
  /**
  Constructs an AS400JDBCClob object.  The data is contained
  in the String.  No further communication with the IBM i system is necessary.
  If this clob has a source of a columne of type XML, then any getX method that returns xml as string will trim the xml declaration.
  
  @param  data     The CLOB data.
  @param maxLength
  @param isXML flag to signal if source is xml
  **/
    AS400JDBCClob(String data, int maxLength, boolean isXML)
    {
      data_ = data.toCharArray();
      maxLength_ = maxLength;
      isXML_ = isXML;
    }

   //@xmltrim
    AS400JDBCClob(char[] data, boolean isXML)
    {
      data_ = data;
      isXML_ = isXML;
    }
  


/**
Returns the entire CLOB as a stream of ASCII characters.

@return The stream.

@exception  SQLException    If an error occurs.
**/
  public synchronized InputStream getAsciiStream() throws SQLException
  {
    //Following Native, throw HY010 after free() has been called
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    try
    {
      if(isXML_)//@xmltrim
          return new ByteArrayInputStream((JDUtilities.stripXMLDeclaration(new String(data_))).getBytes("ISO8859_1"));  //@xmltrim
      else
          return new ByteArrayInputStream((new String(data_)).getBytes("ISO8859_1"));
    }
    catch (UnsupportedEncodingException e)
    {
      JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
      return null;
    }
  }



/**
Returns the entire CLOB as a character stream.

@return The stream.

@exception  SQLException    If an error occurs.
**/
  public synchronized Reader getCharacterStream() throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    if(isXML_)//@xmltrim
        return new CharArrayReader( JDUtilities.stripXMLDeclaration( new String(data_) ).toCharArray());  //@xmltrim
    else
        return new CharArrayReader(data_);
  }



/**
Returns part of the contents of the CLOB.

@param  position       The start position within the CLOB (1-based).
@param  length      The length to return.
@return             The contents.

@exception  SQLException    If the start position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
  public synchronized String getSubString(long position, int length) throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    int offset = (int)position-1;
    // Only throw exception if offset if greater than the length
    // It is valid for the requested length to be greater than the actual length
    // @J5C
    if (offset < 0 || length < 0 || (offset  > data_.length))
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    //@xmltrim
    if(isXML_)
    {
        data_ = JDUtilities.stripXMLDeclaration( new String(data_) ).toCharArray();
    }
    
    int lengthToUse = data_.length - offset;
    if (lengthToUse <= 0) return "";
    if (lengthToUse > length) lengthToUse = length;

    char[] result = new char[lengthToUse];
    System.arraycopy(data_, offset, result, 0, lengthToUse);
    return new String(result);
  }



/**
Returns the length of the CLOB.

@return     The length of the CLOB, in characters.

@exception SQLException     If an error occurs.
**/
  public synchronized long length() throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    return data_.length;
  }



/**
Returns the position at which a pattern is found in the CLOB.

@param  pattern     The pattern.
@param  position       The position within the CLOB to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
  public synchronized long position(String pattern, long position) throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    int offset = (int)position-1;
    if (pattern == null || offset < 0 || offset >= data_.length)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    } else { 

    char[] charPattern = pattern.toCharArray();
    int end = data_.length - charPattern.length;

    for (int i=offset; i<=end; ++i)
    {
      int j = 0;
      while (j < charPattern.length && data_[i+j] == charPattern[j]) ++j;
      if (j == charPattern.length) return i+1;
    }
    }
    return -1;
    
  }



/**
Returns the position at which a pattern is found in the CLOB.

@param  pattern     The pattern.
@param  position       The position within the CLOB to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
  public synchronized long position(Clob pattern, long position) throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    int offset = (int)position-1;
    if (pattern == null || offset < 0 || offset >= data_.length)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    } else { 

    int patternLength = (int)pattern.length();
    if (patternLength > data_.length || patternLength < 0) return -1;

    int end = data_.length - patternLength;

    char[] charPattern = pattern.getSubString(1L, patternLength).toCharArray(); //@CRS - Get all the chars for now, improve this later.

    for (int i=offset; i<=end; ++i)
    {
      int j = 0;
      while (j < charPattern.length && data_[i+j] == charPattern[j]) ++j;
      if (j == charPattern.length) return i+1;
    }
    }
    return -1;
  }


  /**
  Returns a stream that an application can use to write Ascii characters to this CLOB.
  The stream begins at position <i>position</i>, and the CLOB will be truncated 
  after the last character of the write.
  
  @param position The position (1-based) in the CLOB where writes should start.
  @return An OutputStream object to which data can be written by an application.
  @exception SQLException If there is an error accessing the CLOB or if the position
  specified is greater than the length of the CLOB.
  **/
  public OutputStream setAsciiStream(long position) throws SQLException
  {   
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
     
    if (position <= 0 || position > maxLength_)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    try
    {
      return new AS400JDBCClobOutputStream(this, position, ConvTable.getTable(819, null)); 
    }
    catch (UnsupportedEncodingException e)
    {
      // Should never happen.
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);
      return null;
    }
  }



  /**
  Returns a stream that an application can use to write a stream of Unicode characters to 
  this CLOB.  The stream begins at position <i>position</i>, and the CLOB will 
  be truncated after the last character of the write.

  @param position The position (1-based) in the CLOB where writes should start.
  @return An OutputStream object to which data can be written by an application.
  @exception SQLException If there is an error accessing the CLOB or if the position
  specified is greater than the length of the CLOB.
  **/
  public synchronized Writer setCharacterStream(long position) throws SQLException
  {      
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
     
    if (position <= 0 || position > maxLength_)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    return new AS400JDBCWriter(this, position);
  }



  /**
  Writes a String to this CLOB, starting at position <i>position</i>.  The CLOB 
  will be truncated after the last character written.

  @param position The position (1-based) in the CLOB where writes should start.
  @param stringToWrite The string that will be written to the CLOB.
  @return The number of characters that were written.

  @exception SQLException If there is an error accessing the CLOB or if the position
  specified is greater than the length of the CLOB.
  **/
  public synchronized int setString(long position, String stringToWrite) throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
     
    int offset = (int)position-1;

    if (offset < 0 || offset >= maxLength_ || stringToWrite == null)
    {
      throw JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    // We will write as many chars as we can. If our internal char array
    // would overflow past the 2 GB boundary, we don't throw an error, we just
    // return the number of chars that were set.
    char[] charsToWrite = stringToWrite.toCharArray();
    int newSize = offset + charsToWrite.length;
    if (newSize < 0) newSize = 0x7FFFFFFF; // In case the addition resulted in overflow.
    if (newSize > data_.length)
    {
      char[] temp = data_;
      data_ = new char[newSize];
      System.arraycopy(temp, 0, data_, 0, temp.length);
      int numPad = offset - temp.length;        //Determine if we need to Pad with single byte space before we write the new data_          @K1A
      //the number of spaces we need to pad is equal to the offset we want to start writing at minus the length of the current clob(temp)   @K1A
      for(int i=0; i<numPad; i++)                                                                                                         //@K1A
          data_[i+temp.length] = '\u0020';                                                                                                //@K1A
    }
    int numChars = newSize - offset;
    System.arraycopy(charsToWrite, 0, data_, offset, numChars);

    return numChars;
  }



  /**
  Writes a String to this CLOB, starting at position <i>position</i> in the CLOB.  
  The CLOB will be truncated after the last character written.  The <i>lengthOfWrite</i>
  characters written will start from <i>offset</i> in the string that was provided by the
  application.

  @param position The position (1-based) in the CLOB where writes should start.
  @param string The string that will be written to the CLOB.
  @param offset The offset into string to start reading characters (0-based).
  @param lengthOfWrite The number of characters to write.
  @return The number of characters written.

  @exception SQLException If there is an error accessing the CLOB value or if the position
  specified is greater than the length of the CLOB.
  **/
  public synchronized int setString(long position, String string, int offset, int lengthOfWrite) throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
     
    int clobOffset = (int)position-1;
    if (clobOffset < 0 || clobOffset >= maxLength_ ||
        string == null || offset < 0 || lengthOfWrite < 0 || (offset+lengthOfWrite) > string.length() ||
        (clobOffset+lengthOfWrite) > maxLength_)
    {
      throw JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    // We will write as many chars as we can. If our internal char array
    // would overflow past the 2 GB boundary, we don't throw an error, we just
    // return the number of chars that were set.
    char[] charsToWrite = string.toCharArray();
    int newSize = clobOffset + lengthOfWrite;
    if (newSize < 0) newSize = 0x7FFFFFFF; // In case the addition resulted in overflow.
    if (newSize > data_.length)
    {
      char[] temp = data_;
      data_ = new char[newSize];
      System.arraycopy(temp, 0, data_, 0, temp.length);
      int numPad = offset - temp.length;        //Determine if we need to Pad with single byte space before we write the new data_          @K1A
      //the number of spaces we need to pad is equal to the offset we want to start writing at minus the length of the current clob(temp)   @K1A
      for(int i=0; i<numPad; i++)                                                                                                         //@K1A
          data_[i+temp.length] = '\u0020';                                                                                                //@K1A
    }
    int numChars = newSize - clobOffset;
    System.arraycopy(charsToWrite, offset, data_, clobOffset, numChars);

    return numChars;
  }



  /**
  Truncates this CLOB to a length of <i>lengthOfCLOB</i> characters.
   
  @param lengthOfCLOB The length, in characters, that this CLOB should be after 
  truncation.
   
  @exception SQLException If there is an error accessing the CLOB or if the length
  specified is greater than the length of the CLOB. 
  **/
  public synchronized void truncate(long lengthOfCLOB) throws SQLException
  {
    if(data_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    int length = (int)lengthOfCLOB;
    if (length < 0 || length > maxLength_)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    char[] temp = data_;
    data_ = new char[length];
    int numToCopy = length < temp.length ? length : temp.length;
    System.arraycopy(temp, 0, data_, 0, numToCopy);
  }

    // @PDA jdbc40
    /**
     * This method frees the <code>Clob</code> object and releases the
     * resources the resources that it holds. The object is invalid once the
     * <code>free</code> method is called. If <code>free</code> is called
     * multiple times, the subsequent calls to <code>free</code> are treated
     * as a no-op.
     * 
     * @throws SQLException  If a database error occurs.
     *             if an error occurs releasing the Clob's resources
     */
    public synchronized void free() throws SQLException
    {
        data_ = null; //@pda make available for GC
    }

    // @PDA jdbc40
    /**
     * Returns a <code>Reader</code> object that contains a partial
     * <code>Clob</code> value, starting with the character specified by pos,
     * which is length characters in length.
     * 
     * @param pos
     *            the offset to the first character of the partial value to be
     *            retrieved. The first character in the Clob is at position 1.
     * @param length
     *            the length in characters of the partial value to be retrieved.
     * @return <code>Reader</code> through which the partial <code>Clob</code>
     *         value can be read.
     * @throws SQLException  If a database error occurs.
     *             if pos is less than 1 or if pos is greater than the number of
     *             characters in the <code>Clob</code> or if pos + length is
     *             greater than the number of characters in the
     *             <code>Clob</code>
     */
    public synchronized Reader getCharacterStream(long pos, long length) throws SQLException
    {
        if(data_ == null)//@free
            JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
        
        if(isXML_ )//@xmltrim
            return new CharArrayReader( JDUtilities.stripXMLDeclaration( new String(data_)).toCharArray(), (int) pos-1, (int)length);  //@xmltrim
        else
            return new CharArrayReader(data_, (int) pos-1, (int)length);
    }
 
}
