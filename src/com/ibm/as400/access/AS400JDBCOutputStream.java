///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCOutputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream; 
import java.sql.SQLException;
import java.sql.DriverManager;



/**
The AS400JDBCLobOutputStream class provides a stream
to write into large objects.  The data is valid only within the current
transaction.  Users get one of these objects by calling Clob.setAsciiStream() or
Blob.setBinaryStream which both return an object of type OutputStream
**/
abstract class AS400JDBCOutputStream extends OutputStream
{
  static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


  private boolean closed_ = false;
  private long position_;


  /**
   * Construct an AS400JDBCLobOutputStream object.  
  **/
  AS400JDBCOutputStream(long positionToStartWriting)
  {
    position_ = positionToStartWriting;
  }

  /**
   * Close the output stream.  
  **/
  public void close()
  {
    closed_ = true;
  }


  /**
   * Flush the output stream.  
  **/
  public void flush()
  {
    //no-op
  }


  /**
   * Write a byte array to the output stream.
   * @param byteArray The byte array the user wants written to the output stream.  
  **/
  public synchronized void write(byte[] byteArray) throws IOException
  {
    if (byteArray == null) throw new NullPointerException("byteArray");

    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    write(byteArray, 0, byteArray.length);
  }


  /**
   * Write part of a byte array to the output stream from offset <i>off</i> for len bytes.
   * @param byteArray The byte array the user wants written to the output stream.
   * @param off       The offset into the byte array that the user wants written to the 
   *                  output stream (1-based).
   * @param len       The number of bytes the user wants written to the output stream
   *                  from the byte array they passed in.  
  **/
  public synchronized void write(byte[] byteArray, int off, int len) throws IOException
  {
    if (byteArray == null) throw new NullPointerException("byteArray");
    if ((off < 0) || (off > len)) throw new ExtendedIllegalArgumentException("off", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    if (len < 0) throw new ExtendedIllegalArgumentException("len", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    try
    {
      int numWritten = doWrite(position_, byteArray, off, len);
      if (numWritten != len)
      {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "Unable to write requested number of bytes: "+numWritten+" != "+len);
        closed_ = true;
        throw new IOException();
      }
      position_ += len;
    }
    catch (SQLException e)
    {
      if (JDTrace.isTraceOn()) e.printStackTrace(DriverManager.getLogWriter());
      closed_ = true;
      throw new IOException(e.getMessage());
    }
  }


  /**
   * Write a byte to the output stream.
   * @param b         The byte the user wants written to the output stream.  The general contract 
   * for write is that one byte is written to the output stream. The byte to be written is the eight 
   * low-order bits of the argument b. The 24 high-order bits of b are ignored. 
  **/
  public synchronized void write(int b) throws IOException
  {
    if (closed_) throw new ExtendedIOException(ExtendedIOException.RESOURCE_NOT_AVAILABLE);

    try
    {
      int numWritten = doWrite(position_, (byte)b);
      if (numWritten != 1)
      {
        if (JDTrace.isTraceOn()) JDTrace.logInformation(this, "Unable to write requested number of bytes: "+numWritten+" != 1");
        closed_ = true;
        throw new IOException();
      }
      ++position_;
    }
    catch (SQLException e)
    {
      if (JDTrace.isTraceOn()) e.printStackTrace(DriverManager.getLogWriter());
      closed_ = true;
      throw new IOException(e.getMessage());
    }
  }


  /**
   * This method writes based on the actual type of LOB.
  **/
  abstract int doWrite(long position, byte[] data, int offset, int length) throws SQLException;
  abstract int doWrite(long position, byte data) throws SQLException;

}
