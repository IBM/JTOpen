///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCBlob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
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
 * The AS400JDBCBlob class provides access to binary large
 * objects.  The data is valid only within the current
 * transaction.
**/
public class AS400JDBCBlob implements Blob
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private byte[] data_;
  private int maxLength_;

/**
Constructs an AS400JDBCBlob object.  The data is contained
in the raw byte array.  No further communication with the i5/OS system
is necessary.

@param  data     The BLOB data.
**/
  AS400JDBCBlob(byte[] data, int maxLength)
  {
    data_ = data;
    maxLength_ = maxLength;
  }



  //@PDA 550
  /**
   * This method frees the <code>Blob</code> object and releases the
   * resources that it holds. The object is invalid once the <code>free</code>
   * method is called. If <code>free</code> is called multiple times, the
   * subsequent calls to <code>free</code> are treated as a no-op.
   * 
   * @throws SQLException
   *             if an error occurs releasing the Blob's resources
   */
  public synchronized void free() throws SQLException
  {
      data_ = null; //@pda make available for GC
  }
  
  
  
/**
Returns the entire BLOB as a stream of uninterpreted bytes.

@return The stream.

@exception  SQLException    If an error occurs.
**/
  public synchronized InputStream getBinaryStream() throws SQLException
  {
    return new ByteArrayInputStream(data_);
  }



/**
Returns part of the contents of the BLOB.

@param  position       The start position within the BLOB (1-based).
@param  length      The length to return.
@return             The contents.

@exception  SQLException    If the start position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
  public synchronized byte[] getBytes(long position, int length) throws SQLException
  {
    int offset = (int)position-1;
    if (offset < 0 || length < 0 || (offset + length) > data_.length)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    int lengthToUse = data_.length - offset;
    if (lengthToUse < 0) return new byte[0];
    if (lengthToUse > length) lengthToUse = length;

    byte[] result = new byte[lengthToUse];
    System.arraycopy(data_, offset, result, 0, lengthToUse);
    return result;
  }



/**
Returns the length of the BLOB.

@return     The length of the BLOB, in bytes.

@exception SQLException     If an error occurs.
**/
  public synchronized long length() throws SQLException
  {
    return data_.length;
  }


/**
Returns the position at which a pattern is found in the BLOB.

@param  pattern     The pattern.
@param  position       The position within the BLOB to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
  public synchronized long position(byte[] pattern, long position) throws SQLException
  {
    int offset = (int)position-1;
    if (pattern == null || offset < 0 || offset >= data_.length)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    int end = data_.length - pattern.length;

    for (int i=offset; i<=end; ++i)
    {
      int j = 0;
      while (j < pattern.length && data_[i+j] == pattern[j]) ++j;
      if (j == pattern.length) return i+1;
    }

    return -1;
  }


/**
Returns the position at which a pattern is found in the BLOB.

@param  pattern     The pattern.
@param  position       The position within the BLOB to begin
                    searching (1-based).
@return             The position at which the pattern
                    is found, or -1 if the pattern is not
                    found.

@exception SQLException     If the pattern is null,
                            the position is not valid,
                            or an error occurs.
**/
  public synchronized long position(Blob pattern, long position) throws SQLException
  {
    int offset = (int)position-1;
    if (pattern == null || offset < 0 || offset >= data_.length)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    int patternLength = (int)pattern.length();
    if (patternLength > data_.length || patternLength < 0) return -1;

    int end = data_.length - patternLength;

    byte[] bytePattern = pattern.getBytes(1L, patternLength); //@CRS - Get all bytes for now, improve this later.

    for (int i=offset; i<=end; ++i)
    {
      int j = 0;
      while (j < patternLength && data_[i+j] == bytePattern[j]) ++j;
      if (j == patternLength) return i+1;
    }

    return -1;
  }



  /**
  Returns a stream that an application can use to write to this BLOB.
  The stream begins at position <i>positionToStartWriting</i>.

  @param position The position (1-based) in the BLOB where writes should start.
  @return An OutputStream object to which data can be written by an application.
  @exception SQLException If there is an error accessing the BLOB or if the position
  specified is greater than the length of the BLOB.
  **/
  public OutputStream setBinaryStream(long position) throws SQLException
  {
    if (position <= 0 || position > maxLength_)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    return new AS400JDBCBlobOutputStream(this, position);
  }

  /**
   * This is not part of the JDBC interface.
  **/
  synchronized int setByte(long position, byte data) throws SQLException
  {
    int offset = (int)position-1;

    if (offset < 0 || offset >= maxLength_)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    int newSize = offset + 1;
    if (newSize < 0) newSize = 0x7FFFFFFF;
    if (newSize > data_.length)
    {
      byte[] temp = data_;
      data_ = new byte[newSize];
      System.arraycopy(temp, 0, data_, 0, temp.length);
    }
    int numBytes = newSize - offset;
    if (numBytes > 0)
    {
      data_[offset] = data;
      return 1;
    }
    return 0;
  }



  /**
  Writes an array of bytes to this BLOB, starting at position <i>positionToStartWriting</i> 
  in the BLOB.
  
  @param position The position (1-based) in the BLOB where writes should start.
  @param bytesToWrite The array of bytes to be written to this BLOB.
  @return The number of bytes written to the BLOB.

  @exception SQLException If there is an error accessing the BLOB or if the position
  specified is greater than the length of the BLOB.
  **/
  public synchronized int setBytes(long position, byte[] bytesToWrite) throws SQLException
  {
    int offset = (int)position-1;

    if (offset < 0 || offset >= maxLength_ || bytesToWrite == null)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    // We will write as many bytes as we can. If our internal byte array
    // would overflow past the 2 GB boundary, we don't throw an error, we just
    // return the number of bytes that were set.
    int newSize = offset + bytesToWrite.length;
    if (newSize < 0) newSize = 0x7FFFFFFF; // In case the addition resulted in overflow.
    if (newSize > data_.length)
    {
      byte[] temp = data_;
      data_ = new byte[newSize];
      System.arraycopy(temp, 0, data_, 0, temp.length);
    }
    int numBytes = newSize - offset;
    System.arraycopy(bytesToWrite, 0, data_, offset, numBytes);

    return numBytes;
  }



  /**
  Writes all or part of the byte array the application passes in to this BLOB, 
  starting at position <i>position</i> in the BLOB.  
  The BLOB will be truncated after the last byte written.  The <i>lengthOfWrite</i>
  bytes written will start from <i>offset</i> in the bytes that were provided by the
  application.

  @param position The position (1-based) in the BLOB where writes should start.
  @param bytesToWrite The array of bytes to be written to this BLOB.
  @param offset The offset into the array at which to start reading bytes (0-based).
  @param lengthOfWrite The number of bytes to be written to the BLOB from the array of bytes.
  @return The number of bytes written.

  @exception SQLException If there is an error accessing the BLOB or if the position
  specified is greater than the length of the BLOB.
  **/
  public synchronized int setBytes(long position, byte[] bytesToWrite, int offset, int lengthOfWrite) throws SQLException
  {
    int blobOffset = (int)position-1;
    if (blobOffset < 0 || blobOffset >= maxLength_ ||
        bytesToWrite == null || offset < 0 || lengthOfWrite < 0 || (offset+lengthOfWrite) > bytesToWrite.length ||
        (blobOffset+lengthOfWrite) > maxLength_)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    // We will write as many bytes as we can. If our internal byte array
    // would overflow past the 2 GB boundary, we don't throw an error, we just
    // return the number of bytes that were set.
    int newSize = blobOffset + lengthOfWrite;
    if (newSize < 0) newSize = 0x7FFFFFFF; // In case the addition resulted in overflow.
    if (newSize > data_.length)
    {
      byte[] temp = data_;
      data_ = new byte[newSize];
      System.arraycopy(temp, 0, data_, 0, temp.length);
    }
    int numBytes = newSize - blobOffset;
    System.arraycopy(bytesToWrite, offset, data_, blobOffset, numBytes);

    return numBytes;
  }



  /**
  Truncates this BLOB to a length of <i>lengthOfBLOB</i> bytes.
   
  @param lengthOfBLOB The length, in bytes, that this BLOB should be after 
  truncation.

  @exception SQLException If there is an error accessing the BLOB.
  **/
  public synchronized void truncate(long lengthOfBLOB) throws SQLException
  {
    int length = (int)lengthOfBLOB;
    if (length < 0 || length > maxLength_)
    {
      JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
    }

    byte[] temp = data_;
    data_ = new byte[length];
    int numToCopy = length < temp.length ? length : temp.length;
    System.arraycopy(temp, 0, data_, 0, numToCopy);
  }


}
