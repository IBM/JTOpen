///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCBlob.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

import java.io.*;
import java.sql.*;

public class JDBCBlob implements Blob
{
  private final byte[] data_;
  private final int offset_;
  private int length_;

  public JDBCBlob(byte[] data, int offset, int len)
  {
    data_ = data;
    offset_ = offset;
    length_ = len;
  }

  private static final class JDBCBlobOutputStream extends OutputStream
  {
    private final JDBCBlob blob_;
    private int next_;

    JDBCBlobOutputStream(JDBCBlob blob, int start) // 0-based.
    {
      blob_ = blob;
      next_ = start+blob_.offset_;
    }

    public void write(int b) throws IOException
    {
      if (next_ < blob_.offset_+blob_.length_)
      {
        blob_.data_[next_++] = (byte)b;
      }
    }
  };

  public InputStream getBinaryStream() throws SQLException
  {
    return new ByteArrayInputStream(data_, offset_, length_);
  }

  // pos is 1-based!
  public byte[] getBytes(long pos, int length) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    int total = length_-ipos+1;
    if (total > length) total = length;
    byte[] data = new byte[total];
    System.arraycopy(data_, offset_+ipos-1, data, 0, total);
    return data;
  }

  public long length() throws SQLException
  {
    return length_;
  }

  public long position(Blob pattern, long start) throws SQLException
  {
    if (start <= 0) throw new SQLException("Bad start: "+start);
    byte[] patternBytes = pattern.getBytes(0, (int)(pattern.length() & 0x7FFFFFFF));
    return position(patternBytes, start);
  }

  public long position(byte[] pattern, long start) throws SQLException
  {
    if (start <= 0) throw new SQLException("Bad start: "+start);
    for (int i=(int)(start & 0x7FFFFFFF)+offset_-1; i<offset_+length_; ++i)
    {
      boolean match = true;
      for (int j=0; match && j<pattern.length; ++j)
      {
        if (data_[i] != pattern[j]) match = false;
      }
      if (match)
      {
        return i-offset_;
      }
    }
    return -1;
  }

  public OutputStream setBinaryStream(long pos) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    return new JDBCBlobOutputStream(this, ipos-1);
  }

  public int setBytes(long pos, byte[] bytes) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    return setBytes(pos, bytes, 0, bytes.length);
  }

  public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    int total = length_-ipos+1;
    if (total > len) total = len;
    System.arraycopy(bytes, offset, data_, offset_+ipos-1, total);
    return total;
  }

  public void truncate(long len) throws SQLException
  {
    length_ = (len < 0) ? 0 : (int)(len & 0x7FFFFFFF);
  }
}



