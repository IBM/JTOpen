///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLBlob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;


final class SQLBlob implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  private static final byte[] default_ = new byte[0];

  private int maxLength_;
  private int truncated_;
  private byte[] value_ = default_;
  private Object savedObject_; // This is our byte[] or InputStream or whatever that we save to convert to bytes until we really need to.
  private int scale_ = -1; // This is our length.

  SQLBlob(int maxLength, SQLConversionSettings settings)
  {
    maxLength_ = maxLength;
  }


  public Object clone()
  {
    return new SQLBlob(maxLength_, null);
  }


  public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) throws SQLException
  {
    int length = BinaryConverter.byteArrayToInt(rawBytes, offset);
    value_ = new byte[length];
    System.arraycopy(rawBytes, offset+4, value_, 0, value_.length);
    savedObject_ = null;
  }


  public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) throws SQLException
  {
    if (savedObject_ != null) doConversion();

    BinaryConverter.intToByteArray(value_.length, rawBytes, offset);
    System.arraycopy(value_, 0, rawBytes, offset+4, value_.length);
  }

  public void set(Object object, Calendar calendar, int scale) throws SQLException
  {
    // If it's a byte[] we check for data truncation.
    if (object instanceof byte[])
    {
      byte[] bytes = (byte[])object;
      truncated_ = (bytes.length > maxLength_ ? bytes.length-maxLength_ : 0);
    }
    else if (!(object instanceof String) &&
             !(object instanceof Blob) &&
             !(object instanceof Reader) &&
             !(object instanceof InputStream))
    {
      JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    }

    savedObject_ = object;
    if (scale != -1) scale_ = scale;
  }

  private void doConversion() throws SQLException
  {
    try
    {
      Object object = savedObject_;
      if (object instanceof byte[])
      {
        value_ = (byte[]) object;
        int objectLength = value_.length;
        if (value_.length > maxLength_)
        {
          byte[] newValue = new byte[maxLength_];
          System.arraycopy(value_, 0, newValue, 0, maxLength_);
          value_ = newValue;
        }
        truncated_ = objectLength - value_.length;
      }
      else if (savedObject_ instanceof String)
      {
        String s = (String)savedObject_;
        value_ = SQLBinary.stringToBytes(s);
        truncated_ = 0;
      }
      else if (savedObject_ instanceof Reader)
      {
        int length = scale_; // hack to get the length into the set method
        if (length >= 0)
        {
          try
          {
            int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            HexReaderInputStream stream = new HexReaderInputStream((Reader)savedObject_);
            byte[] byteBuffer = new byte[blockSize];
            int totalBytesRead = 0;
            int bytesRead = stream.read(byteBuffer, 0, blockSize);
            while (bytesRead > -1 && totalBytesRead < length)
            {
              baos.write(byteBuffer, 0, bytesRead);
              totalBytesRead += bytesRead;
              int bytesRemaining = length - totalBytesRead;
              if (bytesRemaining < blockSize)
              {
                blockSize = bytesRemaining;
              }
              bytesRead = stream.read(byteBuffer, 0, blockSize);
            }
            value_ = baos.toByteArray();
            int objectLength = value_.length;
            if (value_.length > maxLength_)
            {
              byte[] newValue = new byte[maxLength_];
              System.arraycopy(value_, 0, newValue, 0, maxLength_);
              value_ = newValue;
            }
            truncated_ = objectLength - value_.length;
          }
          catch (IOException ie)
          {
            JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
          }
        }
        else
        {
          JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        }
      }
      else if (object instanceof Blob)
      {
        Blob blob = (Blob) object;
        int blobLength = (int)blob.length();
        int lengthToUse = blobLength < 0 ? 0x7FFFFFFF : blobLength;
        if (lengthToUse > maxLength_) lengthToUse = maxLength_;
        value_ = blob.getBytes(1, lengthToUse);
        truncated_ = blobLength - lengthToUse;
      }
      else if (object instanceof InputStream)
      {
        int length = scale_; // hack to get the length into the set method
        if (length >= 0)
        {
          InputStream stream = (InputStream)object;
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
          byte[] byteBuffer = new byte[blockSize];
          try
          {
            int totalBytesRead = 0;
            int bytesRead = stream.read(byteBuffer, 0, blockSize);
            while (bytesRead > -1 && totalBytesRead < length)
            {
              baos.write(byteBuffer, 0, bytesRead);
              totalBytesRead += bytesRead;
              int bytesRemaining = length - totalBytesRead;
              if (bytesRemaining < blockSize)
              {
                blockSize = bytesRemaining;
              }
              bytesRead = stream.read(byteBuffer, 0, blockSize);
            }
          }
          catch (IOException ie)
          {
            JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
          }
          value_ = baos.toByteArray();
          int objectLength = value_.length;
          if (value_.length > maxLength_)
          {
            byte[] newValue = new byte[maxLength_];
            System.arraycopy(value_, 0, newValue, 0, maxLength_);
            value_ = newValue;
          }
          truncated_ = objectLength - value_.length;
        }
        else
        {
          JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
        }
      }
      else
      {
        JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
      }
    }
    finally
    {
      savedObject_ = null;
    }
  }



//---------------------------------------------------------//
//                                                         //
// DESCRIPTION OF SQL TYPE                                 //
//                                                         //
//---------------------------------------------------------//



  public String getCreateParameters()
  {
    return AS400JDBCDriver.getResource("MAXLENGTH"); 
  }



  public int getDisplaySize()
  {
    return maxLength_;
  }


  public String getJavaClassName()
  {
    return "com.ibm.as400.access.AS400JDBCBlob";
  }


  public String getLiteralPrefix()
  {
    return null;
  }



  public String getLiteralSuffix()
  {
    return null;
  }



  public String getLocalName()
  {
    return "BLOB"; 
  }



  public int getMaximumPrecision()
  {
    return 15728640; //@CRS - Should this be changed to 2 GB?
  }



  public int getMaximumScale()
  {
    return 0;
  }



  public int getMinimumScale()
  {
    return 0;
  }



  public int getNativeType()
  {
    return 404;
  }



  public int getPrecision()
  {
    return maxLength_;
  }


  public int getRadix()
  {
    return 0;
  }


  public int getScale()
  {
    return 0;
  }


  public int getType()
  {
		return java.sql.Types.BLOB;
	}



  public String getTypeName()
  {
		return "BLOB"; 
	}


  public boolean isSigned()
  {
    return false;
  }



  public boolean isText()
  {
    return true; //@CRS - Why is this true?
  }


  public int getActualSize()
  {
    return value_.length;
  }



  public int getTruncated()
  {
    return truncated_;
  }



  public InputStream toAsciiStream() throws SQLException
  {
    if (savedObject_ != null) doConversion();
    try
    {
      return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(SQLBinary.bytesToString(value_)));
    }
    catch(UnsupportedEncodingException e)
    {
      JDError.throwSQLException(JDError.EXC_INTERNAL, e);
  		return null;
    }
  }



  public BigDecimal toBigDecimal(int scale) throws SQLException //@CRS - Could use a Converter here to make this work.
	{
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public InputStream toBinaryStream() throws SQLException
	{
    if (savedObject_ != null) doConversion();
    return new ByteArrayInputStream(value_);
  }



  public Blob toBlob() throws SQLException
	{
    if (savedObject_ != null) doConversion();
    return new AS400JDBCBlob(value_, maxLength_);
	}


  public boolean toBoolean() throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return false;
  }



  public byte toByte() throws SQLException
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public byte[] toBytes() throws SQLException
  {
    if (savedObject_ != null) doConversion();
    return value_;
  }



  public Reader toCharacterStream() throws SQLException
  {
    if (savedObject_ != null) doConversion();
    return new StringReader(SQLBinary.bytesToString(value_));
  }



  public Clob toClob() throws SQLException
  {
    if (savedObject_ != null) doConversion();
    return new AS400JDBCClob(SQLBinary.bytesToString(value_), maxLength_);
  }



  public Date toDate(Calendar calendar) throws SQLException //@CRS - Could use toLong() to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public double toDouble() throws SQLException //@CRS - Could use a Converter here to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public float toFloat() throws SQLException //@CRS - Could use a Converter here to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public int toInt() throws SQLException //@CRS - Could use a Converter here to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public long toLong() throws SQLException //@CRS - Could use a Converter here to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public Object toObject()
  {
    try
    {
      if (savedObject_ != null) doConversion();
    }
    catch (SQLException sqle)
    {
      value_ = default_;
    }
    return new AS400JDBCBlob(value_, maxLength_);
  }



  public short toShort() throws SQLException //@CRS - Could use a Converter here to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return -1;
  }



  public String toString()
  {
    try
    {
      if (savedObject_ != null) doConversion();
    }
    catch (SQLException sqle)
    {
      value_ = default_;
    }
    return SQLBinary.bytesToString(value_);
  }



  public Time toTime(Calendar calendar) throws SQLException //@CRS - Could use toLong() to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public Timestamp toTimestamp(Calendar calendar) throws SQLException //@CRS - Could use toLong() to make this work.
  {
    JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
    return null;
  }



  public InputStream toUnicodeStream() throws SQLException
  {
    if (savedObject_ != null) doConversion();
    return new ByteArrayInputStream(value_);
  }
}

