///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCBlobLocator.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Vector;

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
The AS400JDBCBlobLocator class provides access to binary large
objects.  The data is valid only within the current
transaction.
**/
public class AS400JDBCBlobLocator implements Blob
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  JDLobLocator locator_; 
  Object savedObject_; // This is our InputStream or byte[] or whatever that needs to be written if we are batching.
  int savedScale_; // This is our length that goes with our savedObject_.

  private byte[] cache_;
  private int cacheOffset_;
  private static final byte[] INIT_CACHE = new byte[0];
  private int maxLength_;

/**
Constructs an AS400JDBCBlobLocator object.  The data for the
BLOB will be retrieved as requested, directly from the
i5/OS system, using the locator handle.

@param  locator             The locator.
**/
  AS400JDBCBlobLocator(JDLobLocator locator, Object savedObject, int savedScale)
  {
    locator_  = locator;
    savedObject_ = savedObject;
    savedScale_ = savedScale;
    maxLength_ = locator_.getMaxLength();
  }



/**
Returns the entire BLOB as a stream of uninterpreted bytes.

@return The stream.

@exception  SQLException    If an error occurs.
**/
  public InputStream getBinaryStream() throws SQLException
  {
    synchronized(locator_)
    {
      return new AS400JDBCInputStream(locator_);
    }
  }



/**
Returns part of the contents of the BLOB.

@param  position       The position within the BLOB (1-based).
@param  length      The length to return.
@return             The contents.

@exception  SQLException    If the position is not valid,
                            if the length is not valid,
                            or an error occurs.
**/
  public byte[] getBytes(long position, int length) throws SQLException
  {
    synchronized(locator_)
    {
      int offset = (int)position-1;
      if (offset < 0 || length < 0 || (offset + length) > locator_.getLength())
      {
        JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }
      int lengthToUse = (int)locator_.getLength() - offset;
      if (lengthToUse <= 0) return new byte[0];
      if (lengthToUse > length) lengthToUse = length;

      DBLobData data = locator_.retrieveData(offset, lengthToUse);
      int actualLength = data.getLength();
      byte[] bytes = new byte[actualLength];
      System.arraycopy(data.getRawBytes(), data.getOffset(), bytes, 0, actualLength);
      return bytes;
    }
  }



/**
Returns the handle to this BLOB locator in the database.

@return             The handle to this locator in the database.
**/
  int getHandle()
  {
    return locator_.getHandle();
  }



/**
Returns the length of the BLOB.

@return     The length of the BLOB, in bytes.

@exception SQLException     If an error occurs.
**/
  public long length() throws SQLException
  {
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
  private int getCachedByte(int index) throws SQLException
  {
    int realIndex = index - cacheOffset_;
    if (realIndex >= cache_.length)
    {
      int blockSize = AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
      int len = (int)locator_.getLength();
      if (len < 0) len = 0x7FFFFFFF;
      if ((blockSize+index) > len) blockSize = len-index;
      cache_ = getBytes(index+1, blockSize);
      cacheOffset_ = index;
      realIndex = 0;
    }
    if (cache_.length == 0) return -1;
    return cache_[realIndex];
  }


/**
Returns the position at which a pattern is found in the BLOB.

@param  pattern     The pattern.
@param  position       The position within the BLOB to begin
                    searching (1-based).
@return             The offset into the BLOB at which the pattern was found,
                    or -1 if the pattern was not found or the pattern was a byte
                    array of length 0.

@exception SQLException     If the position is not valid or an error occurs.
**/
  public long position(byte[] pattern, long position) throws SQLException
  {
    synchronized(locator_)
    {
      int offset = (int)position-1;
      if (pattern == null || offset < 0 || offset >= locator_.getLength())
      {
        JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }

      int end = (int)locator_.getLength() - pattern.length;

      // We use a cache of bytes so we don't have to read in the entire
      // contents of the BLOB.
      initCache();

      for (int i=offset; i<=end; ++i)
      {
        int j = 0;
        int cachedByte = getCachedByte(i+j);
        while (j < pattern.length && cachedByte != -1 && pattern[j] == (byte)cachedByte)
        {
          ++j;
          cachedByte = getCachedByte(i+j);
        }
        if (j == pattern.length) return i+1;
      }
      return -1;
    }
  }



/**
Returns the position at which a pattern is found in the BLOB.

@param  pattern     The pattern.
@param  position       The position within the BLOB to begin
                    searching (1-based).
@return             The offset into the BLOB at which the pattern was found,
                    or -1 if the pattern was not found or the pattern was a byte
                    array of length 0.

@exception SQLException     If the position is not valid or an error occurs.
**/
  public long position(Blob pattern, long position) throws SQLException
  {
    synchronized(locator_)
    {
      int offset = (int)position-1;
      if (pattern == null || offset < 0 || offset >= locator_.getLength())
      {
        JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }

      int patternLength = (int)pattern.length();
      int locatorLength = (int)locator_.getLength();
      if (patternLength > locatorLength || patternLength < 0) return -1;

      int end = locatorLength - patternLength;

      byte[] bytePattern = pattern.getBytes(1L, patternLength); //@CRS - Get all bytes for now, improve this later.

      // We use a cache of bytes so we don't have to read in the entire
      // contents of the BLOB.
      initCache();

      for (int i=offset; i<=end; ++i)
      {
        int j = 0;
        int cachedByte = getCachedByte(i+j);
        while (j < patternLength && cachedByte != -1 && bytePattern[j] == (byte)cachedByte)
        {
          ++j;
          cachedByte = getCachedByte(i+j);
        }
        if (j == patternLength) return i+1;
      }

      return -1;
    }
  }


  /**
  Returns a stream that an application can use to write to this BLOB.
  The stream begins at position <i>position</i>.

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

    return new AS400JDBCBlobLocatorOutputStream(this, position);
  }



  /**
   Writes an array of bytes to this BLOB, starting at position <i>position</i> 
   in the BLOB.
   
   @param position The position (1-based) in the BLOB where writes should start.
   @param bytesToWrite The array of bytes to be written to this BLOB.
   @return The number of bytes written to the BLOB.

   @exception SQLException If there is an error accessing the BLOB or if the position
   specified is greater than the length of the BLOB.
   **/
  public int setBytes(long position, byte[] bytesToWrite) throws SQLException
  {
    synchronized(locator_)
    {
      int offset = (int)position-1;

      if (offset < 0 || offset >= maxLength_ || bytesToWrite == null)
      {
        JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }

      // We will write as many bytes as we can. If the byte array
      // would overflow past the 2 GB boundary, we don't throw an error, we just
      // return the number of bytes that could be set.
      int newSize = offset + bytesToWrite.length;
      if (newSize < 0) newSize = 0x7FFFFFFF; // In case the addition resulted in overflow.
      int numBytes = newSize - offset;
      if (numBytes != bytesToWrite.length)
      {
        byte[] temp = bytesToWrite;
        bytesToWrite = new byte[numBytes];
        System.arraycopy(temp, 0, bytesToWrite, 0, numBytes);
      }

      // We don't really know if all of these bytes can be written until we go to
      // the system, so we just return the byte[] length as the number written.
      locator_.writeData((long)offset, bytesToWrite, false);        //@K1A
      return bytesToWrite.length;
    }
  }



  /**
 Writes all or part of the byte array the application passes in to this BLOB, 
 starting at position <i>position</i> in the BLOB.  
 The <i>lengthOfWrite</i>
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
  public int setBytes(long position, byte[] bytesToWrite, int offset, int lengthOfWrite) throws SQLException
  {
    synchronized(locator_)
    {
      int blobOffset = (int)position-1;
      if (blobOffset < 0 || blobOffset >= maxLength_ ||
          bytesToWrite == null || offset < 0 || lengthOfWrite < 0 || (offset+lengthOfWrite) > bytesToWrite.length ||
          (blobOffset+lengthOfWrite) > maxLength_)
      {
        JDError.throwSQLException (this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }

      // We will write as many bytes as we can. If the byte array
      // would overflow past the 2 GB boundary, we don't throw an error, we just
      // return the number of bytes that could be set.
      int newSize = blobOffset + lengthOfWrite;
      if (newSize < 0) newSize = 0x7FFFFFFF; // In case the addition resulted in overflow.
      int numBytes = newSize - blobOffset;
      int realLength = (numBytes < lengthOfWrite ? numBytes : lengthOfWrite);
      byte[] newData = new byte[realLength];
      System.arraycopy(bytesToWrite, offset, newData, 0, lengthOfWrite);

      // We don't really know if all of these bytes can be written until we go to
      // the system, so we just return the byte[] length as the number written.
      locator_.writeData((long)blobOffset, newData, false);             //@K1A
      return newData.length;
    }
  }



  /**
  Truncates this BLOB to a length of <i>lengthOfBLOB</i> bytes.
   
  @param lengthOfBLOB The length, in bytes, that this BLOB should be after 
  truncation.

  @exception SQLException If there is an error accessing the BLOB or if the length
  specified is greater than the length of the BLOB. 
  **/
  public void truncate(long lengthOfBLOB) throws SQLException
  {
    synchronized(locator_)
    {
      int length = (int)lengthOfBLOB;
      if (length < 0 || length > maxLength_)
      {
        JDError.throwSQLException(this, JDError.EXC_ATTRIBUTE_VALUE_INVALID);
      }
      // The host server does not currently provide a way for us
      // to truncate the temp space used to hold the locator data,
      // so we just keep track of it ourselves.  This should work,
      // since the temp space on the system should only be valid
      // within the scope of our transaction/connection. That means
      // there's no reason to go to the system to update the data,
      // since no other process can get at it.
      locator_.writeData(length, new byte[0], 0, 0, true);                  //@K1A
    }
  }
}
