package com.ibm.jtopenlite.database.jdbc;

import com.ibm.jtopenlite.Conv;
import java.io.*;
import java.sql.*;

public class JDBCClob implements Clob
{
  private final byte[] data_;
  private final int offset_;
  private int length_;
  private final int ccsid_;

  public JDBCClob(byte[] data, int offset, int len, int ccsid)
  {
    data_ = data;
    offset_ = offset;
    length_ = len;
    ccsid_ = ccsid;
  }

  private static final class JDBCClobOutputStream extends OutputStream
  {
    private final JDBCClob clob_;
    private int next_;

    JDBCClobOutputStream(JDBCClob clob, int start) // 0-based.
    {
      clob_ = clob;
      next_ = start+clob_.offset_;
    }

    public void write(int b) throws IOException
    {
      if (next_ < clob_.offset_+clob_.length_)
      {
        clob_.data_[next_++] = (byte)b;
      }
    }
  };

  private static final class JDBCClobWriter extends Writer
  {
    private final JDBCClob clob_;
    private int next_;

    JDBCClobWriter(JDBCClob clob, int start) // 0-based.
    {
      clob_ = clob;
      next_ = start+clob_.offset_;
    }

    public void close()
    {
    }

    public void flush()
    {
    }

    public void write(char[] buf, int off, int len) throws IOException
    {
      for (int i=off; i<off+len; ++i)
      {
        if (next_ < clob_.offset_+clob_.length_)
        {
          clob_.data_[next_++] = (byte)buf[i]; //TODO
        }
      }
    }
  };

  /**
   * This is a ByteArrayInputStream wrapper around String.getBytes("ASCII").
  **/
  public InputStream getAsciiStream() throws SQLException
  {
    try
    {
      return new ByteArrayInputStream(Conv.ebcdicByteArrayToString(data_, offset_, length_, ccsid_).getBytes("ASCII"));
    }
    catch (UnsupportedEncodingException uee)
    {
      SQLException sql = new SQLException(uee.toString());
      sql.initCause(uee);
      throw sql;
    }
  }

  /**
   * This is a StringReader wrapper.
  **/
  public Reader getCharacterStream() throws SQLException
  {
    try
    {
      return new StringReader(Conv.ebcdicByteArrayToString(data_, offset_, length_, ccsid_));
    }
    catch (UnsupportedEncodingException uee)
    {
      SQLException sql = new SQLException(uee.toString());
      sql.initCause(uee);
      throw sql;
    }
  }

  // pos is 1-based!
  public String getSubString(long pos, int length) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    int total = length_-ipos+1;
    if (total > length) total = length;
    byte[] data = new byte[total];
    System.arraycopy(data_, offset_+ipos-1, data, 0, total);
    try
    {
      return Conv.ebcdicByteArrayToString(data_, offset_+ipos-1, total, ccsid_);
    }
    catch (UnsupportedEncodingException uee)
    {
      SQLException sql = new SQLException(uee.toString());
      sql.initCause(uee);
      throw sql;
    }
  }

  public long length() throws SQLException
  {
    return length_;
  }

  public long position(Clob pattern, long start) throws SQLException
  {
    if (start <= 0) throw new SQLException("Bad start: "+start);
    String patternString = pattern.getSubString(0, (int)(pattern.length() & 0x7FFFFFFF));
    return position(patternString, start);
  }

  public long position(String patternString, long start) throws SQLException
  {
    if (start <= 0) throw new SQLException("Bad start: "+start);
    try
    {
      final byte[] pattern = Conv.stringToEBCDICByteArray(patternString, ccsid_);
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
    catch (UnsupportedEncodingException uee)
    {
      SQLException sql = new SQLException(uee.toString());
      sql.initCause(uee);
      throw sql;
    }
  }

  public OutputStream setAsciiStream(long pos) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    return new JDBCClobOutputStream(this, ipos-1);
  }

  public Writer setCharacterStream(long pos) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    return new JDBCClobWriter(this, ipos-1);
  }

  public int setString(long pos, String str) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    return setString(pos, str, 0, str.length());
  }

  public int setString(long pos, String str, int offset, int len) throws SQLException
  {
    if (pos <= 0) throw new SQLException("Bad position: "+pos);
    int ipos = (int)(pos & 0x7FFFFFFF);
    int total = length_-ipos+1;
    if (total > len) total = len;
    try
    {
      final byte[] bytes = Conv.stringToEBCDICByteArray(str, ccsid_);
      System.arraycopy(bytes, offset, data_, offset_+ipos-1, total);
      return total;
    }
    catch (UnsupportedEncodingException uee)
    {
      SQLException sql = new SQLException(uee.toString());
      sql.initCause(uee);
      throw sql;
    }
  }

  public void truncate(long len) throws SQLException
  {
    length_ = (len < 0) ? 0 : (int)(len & 0x7FFFFFFF);
  }
}



