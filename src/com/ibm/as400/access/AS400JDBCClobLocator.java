///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCClobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

// In the event that the column in the database is a DBCLOB, know that
// JDLobLocator knows that it is graphic and will correctly convert
// the byte offsets and lengths into character offsets and lengths.

// For the case where the column has a CCSID with variable size 
// characters, all the data will be needed to be retrieved and
// translated for request requiring the length of the lob, as 
// well as for requests requiring a character offset into the lob. 


/**
The AS400JDBCClobLocator class provides access to character large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCClobLocator implements Clob
{
  protected ConvTable converter_;       //@pdc jdbc40
  JDLobLocator locator_;

  Object savedObject_; // This is our InputStream or byte[] or whatever that needs to be written if we are batching.
  int savedScale_; // This is our length that goes with our savedObject_.

  private char[] cache_;
  private int cacheOffset_;
  private static final char[] INIT_CACHE = new char[0];

  //private int truncate_ = -1;
  protected int maxLength_; // The max length in LOB-characters. See JDLobLocator.  //@pdc jdbc40
  private boolean isXML_ = false;      //@xml3 true if this data originated from a native XML column type

  /**
  Constructs an AS400JDBCClobLocator object.  The data for the
  CLOB will be retrieved as requested, directly from the
  IBM i system, using the locator handle.
  
  @param  locator             The locator.
  @param  converter           The text converter.
  **/
  AS400JDBCClobLocator(JDLobLocator locator, ConvTable converter, Object savedObject, int savedScale)
  {
    locator_  = locator;
    converter_ = converter;
    savedObject_ = savedObject;
    savedScale_ = savedScale;
    maxLength_ = locator_.getMaxLength();
  }
  
  //@xml3 new constructor
  /**
  Constructs an AS400JDBCClobLocator object.  The data for the
  CLOB will be retrieved as requested, directly from the
  IBM i system, using the locator handle.
  If this clob has a source of a columne of type XML, then any getX method that returns xml as string will trim the xml declaration.
  
  @param  locator             The locator.
  @param  converter           The text converter.
  @param  savedObject         Input data
  @param  savedScale          Inpuat scale of data
  @param  isXML               Flag that stream is from an XML column type (needed to strip xml declaration)
  **/
  AS400JDBCClobLocator(JDLobLocator locator, ConvTable converter, Object savedObject, int savedScale, boolean isXML)
  {
    this(locator, converter, savedObject, savedScale);
    isXML_ = isXML;
  }

 
  /**
  Returns the entire CLOB as a stream of ASCII characters.
  
  @return The stream.
  
  @exception  SQLException    If an error occurs.
  **/
  public InputStream getAsciiStream() throws SQLException
  {
    //Following Native, throw HY010 after free() has been called.  Note:  NullPointerException if synchronized(null-ref)
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
      try
      {
        //@xml3 if xml column, remove xml declaration via ConvTableReader
        return new ReaderInputStream(new ConvTableReader(new AS400JDBCInputStream(locator_), converter_.getCcsid(), converter_.bidiStringType_, isXML_), 819); // ISO 8859-1.  //@xml3
      }
      catch (UnsupportedEncodingException e)
      {
        JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
        return null;
      }
    }
  }



  /**
  Returns the entire CLOB as a character stream.
  
  @return The stream.
  
  @exception  SQLException    If an error occurs.
  **/
  public Reader getCharacterStream() throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
      try
      {
        //@xml3 if xml column, remove xml declaration via ConvTableReader
        return new ConvTableReader(new AS400JDBCInputStream(locator_), converter_.getCcsid(), converter_.bidiStringType_, isXML_); //@xml3
      }
      catch (UnsupportedEncodingException e)
      {
        JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
        return null;
      }
    }
  }



/**
Returns the handle to this CLOB locator in the database.

@return             The handle to this locator in the databaes.
**/
  int getHandle()throws SQLException //@free called from rs.updateValue(), which in turn will throw exc back to rs.updateX() caller
  {
      if(locator_ == null)//@free
          JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    return locator_.getHandle();
  }



  /**
  Returns part of the contents of the CLOB.
  
  @param  position       The position within the CLOB (1-based).
  @param  length      The number of characters to return.
  @return             The contents.
  
  @exception  SQLException    If the position is not valid,
                              if the length is not valid,
                              or an error occurs.
  **/
  public String getSubString(long position, int length) throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
      int offset = (int)position-1;
      if (offset < 0 || length < 0 || (offset + length) > length())
      {
        JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }

      int lengthToUse = (int)length() - offset;
      if (lengthToUse < 0) return "";
      if (lengthToUse > length) lengthToUse = length;

     
     
      //@xml4 if xml column, remove xml declaration via ConvTableReader
      if(isXML_)
      {
          ConvTableReader r = null;
    	  try{
    		  r = new ConvTableReader(new AS400JDBCInputStream( locator_), converter_.getCcsid(), converter_.bidiStringType_, isXML_); //@xml4
    		  r.skip(offset);                     //@xml4 ConvTableReader will already have skipped XML header if column is XML type
    		  return r.read(lengthToUse);         //@xml4
    	  }
    	  catch ( Exception e)
    	  {
    		  JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
    		  return null;
    	  }
    	  finally{
    	      try{
    	          if (r != null ) r.close();
    	      }catch(Exception ee){
    	        JDTrace.logException(this, "getSubString r.close() threw exception", ee);  
    	      }
    	  }
      }
      
      DBLobData data = locator_.retrieveData(offset, lengthToUse); 
      int actualLength = data.getLength();                          
      return converter_.byteArrayToString(data.getRawBytes(), data.getOffset(), actualLength); 
    }
  }



  /**
  Returns the length of the current contents of the CLOB in characters.
  
  @return     The length of the CLOB in characters.
  
  @exception SQLException     If an error occurs.
  **/
  public long length() throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
      return locator_.getLength();
    }
  }


  // Used for position().
  private void initCache()
  {
    cacheOffset_ = 0;
    cache_ = INIT_CACHE;
  }

  // Used for position().
  private int getCachedChar(int index) throws SQLException
  {
    int realIndex = index - cacheOffset_;
    if (realIndex >= cache_.length)
    {
      int blockSize = AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
      int len = (int)length();
      if (len < 0) len = 0x7FFFFFFF;
      if ((blockSize+index) > len) blockSize = len-index;
      cache_ = getSubString(index+1, blockSize).toCharArray();
      cacheOffset_ = index;
      realIndex = 0;
    }
    if (cache_.length == 0) return -1;
    return cache_[realIndex];
  }



  /**
  Returns the position at which a pattern is found in the CLOB.
  This method is not supported.
  
  @param  pattern     The pattern.
  @param  position       The position within the CLOB to begin
                      searching (1-based).
@return             The position in the CLOB at which the pattern was found,
                    or -1 if the pattern was not found.
  
  @exception SQLException     If the pattern is null,
                              the position is not valid,
                              or an error occurs.
  **/
  public long position(String pattern, long position) throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
      int offset = (int)position-1;
      if (pattern == null || offset < 0 || offset >= length())
      {
        throw JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }

      char[] charPattern = pattern.toCharArray();
      int end = (int)length() - charPattern.length;

      // We use a cache of chars so we don't have to read in the entire
      // contents of the CLOB.
      initCache();

      for (int i=offset; i<=end; ++i)
      {
        int j = 0;
        int cachedChar = getCachedChar(i+j);
        while (j < charPattern.length && cachedChar != -1 && charPattern[j] == cachedChar)
        {
          ++j;
          cachedChar = getCachedChar(i+j);
        }
        if (j == charPattern.length) return i+1;
      }
      return -1;
    }
  }



  /**
  Returns the position at which a pattern is found in the CLOB.
  This method is not supported.
  
  @param  pattern     The pattern.
  @param  position       The position within the CLOB to begin
                      searching (1-based).
@return             The position in the CLOB at which the pattern was found,
                    or -1 if the pattern was not found.
  
  @exception SQLException     If the pattern is null,
                              the position is not valid,
                              or an error occurs.
  **/
  public long position(Clob pattern, long position) throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
      int offset = (int)position-1;
      if (pattern == null || offset < 0 || offset >= length())
      {
        throw JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }

      int patternLength = (int)pattern.length();
      int locatorLength = (int)length();
      if (patternLength > locatorLength || patternLength < 0) return -1;

      int end = locatorLength - patternLength;

      char[] charPattern = pattern.getSubString(1L, patternLength).toCharArray(); //@CRS - Get all chars for now, improve this later.

      // We use a cache of chars so we don't have to read in the entire
      // contents of the CLOB.
      initCache();

      for (int i=offset; i<=end; ++i)
      {
        int j = 0;
        int cachedChar = getCachedChar(i+j);
        while (j < patternLength && cachedChar != -1 && charPattern[j] == cachedChar)
        {
          ++j;
          cachedChar = getCachedChar(i+j);
        }
        if (j == patternLength) return i+1;
      }

      return -1;
    }
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
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    if (position <= 0 || position > maxLength_)
    {
      JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    try
    {
      return new AS400JDBCClobLocatorOutputStream(this, position, ConvTable.getTable(819, null));
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
  public Writer setCharacterStream(long position) throws SQLException
  {
    if(locator_ == null)//@free
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
  public int setString(long position, String stringToWrite) throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
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
      int numChars = newSize - offset;
      if (numChars != charsToWrite.length)
      {
        char[] temp = charsToWrite;
        charsToWrite = new char[newSize];
        System.arraycopy(temp, 0, charsToWrite, 0, numChars);
      }

      // We don't really know if all of these chars can be written until we go to
      // the system, so we just return the char[] length as the number written.
      byte[] bytesToWrite = converter_.stringToByteArray(charsToWrite, 0, charsToWrite.length);
      locator_.writeData((long)offset, bytesToWrite, false);            //@K1A
      return charsToWrite.length;
    }
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
  public int setString(long position, String string, int offset, int lengthOfWrite) throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
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
      int newSize = clobOffset + lengthOfWrite;
      if (newSize < 0) newSize = 0x7FFFFFFF; // In case the addition resulted in overflow.
      int numChars = newSize - clobOffset;
      int realLength = (numChars < lengthOfWrite ? numChars : lengthOfWrite);
      char[] charsToWrite = new char[realLength];
      string.getChars(offset, offset + numChars, charsToWrite, 0);          //@K2C

      // We don't really know if all of these chars can be written until we go to
      // the system, so we just return the char[] length as the number written.
      byte[] bytesToWrite = converter_.stringToByteArray(charsToWrite, 0, charsToWrite.length);
      locator_.writeData((long)clobOffset, bytesToWrite, false);            //@k1A
      return charsToWrite.length;
    }
  }



  /**
  Truncates this CLOB to a length of <i>lengthOfCLOB</i> characters.
   
  @param lengthOfCLOB The length, in characters, that this CLOB should be after 
  truncation.
   
  @exception SQLException If there is an error accessing the CLOB or if the length
  specified is greater than the length of the CLOB. 
  
  **/
  public void truncate(long lengthOfCLOB) throws SQLException
  {
    if(locator_ == null)//@free
        JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free
      
    synchronized(locator_)
    {
      int length = (int)lengthOfCLOB;
      if (length < 0 || length > maxLength_)
      {
        JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }
      //truncate_ = length;

      // The host server does not currently provide a way for us
      // to truncate the temp space used to hold the locator data,
      // so we just keep track of it ourselves.  This should work,
      // since the temp space on the system should only be valid
      // within the scope of our transaction/connection. That means
      // there's no reason to go to the system to update the data,
      // since no other process can get at it.
      locator_.writeData(length, new byte[0], 0, 0, true);          //@k1A
    }
  }
  
   //@PDA 550
  /**
   * This method frees the <code>Clob</code> object and releases the
   * resources that it holds. The object is invalid once the
   * <code>free</code> method is called. If <code>free</code> is called
   * multiple times, the subsequent calls to <code>free</code> are treated
   * as a no-op.
   * 
   * @throws SQLException
   *             if an error occurs releasing the Clob's resources
   */
  public void free() throws SQLException //@sync
  {
      if(locator_ == null)
          return;  //no-op
      
      synchronized(locator_) //@sync
      {   
          locator_.free();
 
          locator_  = null;  //@pda make objects available for GC
          converter_ = null;
          savedObject_ = null;
          cache_ = null;
      }
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
   * @throws SQLException
   *             if pos is less than 1 or if pos is greater than the number of
   *             characters in the <code>Clob</code> or if pos + length is
   *             greater than the number of characters in the
   *             <code>Clob</code>
   */
  public Reader getCharacterStream(long pos, long length) throws SQLException //@sync
  {
      if(locator_ == null)//@free
          JDError.throwSQLException(this, JDError.EXC_FUNCTION_SEQUENCE); //@free

      synchronized(locator_) //@sync
      {   
          if (pos < 1 || (pos - 1 + length) > locator_.getMaxLength() || length < 0 )  //@pdc change parm check like getSubString
          {
              JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
          }
          Reader r = null;

          try
          {
              //@xml3 if xml column, remove xml declaration via ConvTableReader
              r = new ConvTableReader(new AS400JDBCInputStream( locator_), converter_.getCcsid(), converter_.bidiStringType_, isXML_); //@xml3
              r.skip(pos); 
              return r;
          }
          catch (UnsupportedEncodingException e)
          {
              JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
              return null;
          }
          catch (IOException e)
          {
              JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
              return null;
          }
      }
  }
  
}
