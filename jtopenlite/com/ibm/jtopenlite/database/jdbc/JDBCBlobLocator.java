///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCBlobLocator.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import com.ibm.jtopenlite.database.*;
import java.io.*;
import java.sql.*;

public final class JDBCBlobLocator implements Blob, DatabaseLOBDataCallback
{
  private final DatabaseConnection conn_;
  private final DatabaseRetrieveLOBDataAttributes attribs_;

  private long length_ = -1;

  private final byte[] currentBuffer_ = new byte[8192];

  private ByteArrayOutputStream tempOutput_;

  public JDBCBlobLocator(DatabaseConnection conn, DatabaseRetrieveLOBDataAttributes attrib)
  {
    conn_ = conn;
    attribs_ = attrib;
  }

  public void newLOBLength(long length)
  {
    length_ = length;
  }

  public void newLOBData(int ccsid, int length)
  {
  }

  public byte[] getLOBBuffer()
  {
    return currentBuffer_;
  }

  public void newLOBSegment(byte[] buffer, int offset, int length)
  {
    if (tempOutput_ != null)
    {
      tempOutput_.write(buffer, offset, length);
    }
  }

  public InputStream getBinaryStream() throws SQLException
  {
    return new JDBCBlobLocatorInputStream(conn_, attribs_, length_);
  }

  public InputStream getBinaryStream(long pos, long length) throws SQLException {
    // JDBC 4.0 method not yet implemented

    JDBCError.throwSQLException(JDBCError.EXC_FUNCTION_NOT_SUPPORTED);
      return null;
  }

  // pos is 1-based!
  public byte[] getBytes(long pos, int length) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    attribs_.setStartOffset(ipos-1);
    attribs_.setRequestedSize(length);
    attribs_.setReturnCurrentLengthIndicator(0xF1);
    try
    {
      tempOutput_ = new ByteArrayOutputStream();
      conn_.retrieveLOBData(attribs_, this);
      tempOutput_.flush();
      tempOutput_.close();
      return tempOutput_.toByteArray();
    }
    catch (IOException io)
    {
      SQLException sql = new SQLException(io.toString());
      sql.initCause(io);
      throw sql;
    }
  }

  public void refreshLength() throws SQLException
  {
    length_ = -1;
    length();
  }

  public long length() throws SQLException
  {
    if (length_ == -1)
    {
      attribs_.setStartOffset(0);
      attribs_.setRequestedSize(0);
      attribs_.setReturnCurrentLengthIndicator(0xF1);
      try
      {
        length_ = -1;
        conn_.retrieveLOBData(attribs_, this);
        if (length_ == -1)
        {
          throw new IOException("LOB length not retrieved.");
        }
      }
      catch (IOException io)
      {
        SQLException sql = new SQLException(io.toString());
        sql.initCause(io);
        throw sql;
      }
    }
    return length_;
  }

  /**
   * Not implemented.
  **/
  public long position(Blob pattern, long start) throws SQLException
  {
    if (start <= 0) throw new SQLException("Bad start: "+start);
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public long position(byte[] pattern, long start) throws SQLException
  {
    if (start <= 0) throw new SQLException("Bad start: "+start);
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public OutputStream setBinaryStream(long pos) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public int setBytes(long pos, byte[] bytes) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    throw new NotImplementedException();
  }

  /**
   * Not implemented.
  **/
  public void truncate(long len) throws SQLException
  {
    throw new NotImplementedException();
  }


  public void free() throws SQLException {
    // TODO  -- Not implemented yet.  Just no-op for now.

  }



}



