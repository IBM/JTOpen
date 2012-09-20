///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JDBCBlobLocatorInputStream.java
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

public class JDBCBlobLocatorInputStream extends InputStream implements DatabaseLOBDataCallback
{
  private final DatabaseConnection conn_;
  private final DatabaseRetrieveLOBDataAttributes attribs_;

  private long numRead_ = 0;
  private long length_;
  private int nextRead_;
  private byte[] buffer_;

  public JDBCBlobLocatorInputStream(DatabaseConnection conn, DatabaseRetrieveLOBDataAttributes attribs, long length)
  {
    conn_ = conn;
    attribs_ = attribs; //TODO - use separate attribs?
    length_ = length;
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
    return buffer_;
  }

  public void setLOBBuffer(byte[] buf)
  {
    buffer_ = buf;
  }

  public void newLOBSegment(byte[] buffer, int offset, int length)
  {
    nextRead_ = 0;
 }

  public int read() throws IOException
  {
    if (length_ >= 0 && numRead_ >= length_) return -1;

    if (buffer_ == null) buffer_ = new byte[8192];

    if (length_ < 0 || nextRead_ == 0 || nextRead_ >= buffer_.length)
    {
      attribs_.setStartOffset((int)(numRead_ & 0x7FFFFFFF));
      attribs_.setRequestedSize(buffer_.length);
      attribs_.setReturnCurrentLengthIndicator(0xF1); // Return the current length.
      conn_.retrieveLOBData(attribs_, this);
      if (numRead_ >= length_) return -1;
    }
    ++numRead_;
    return buffer_[nextRead_++] & 0x00FF;
  }
}

