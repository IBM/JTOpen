///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLBlob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
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
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLBlob implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

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

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        int length = BinaryConverter.byteArrayToInt(rawBytes, offset);
        value_ = new byte[length];
        System.arraycopy(rawBytes, offset+4, value_, 0, value_.length);
        savedObject_ = null;
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        if(savedObject_ != null) doConversion();

        BinaryConverter.intToByteArray(value_.length, rawBytes, offset);
        System.arraycopy(value_, 0, rawBytes, offset+4, value_.length);
    }

    public void set(Object object, Calendar calendar, int scale) throws SQLException
    {
        // If it's a byte[] we check for data truncation.
        if(object instanceof byte[])
        {
            byte[] bytes = (byte[])object;
            truncated_ = (bytes.length > maxLength_ ? bytes.length-maxLength_ : 0);
        }
        else if(object instanceof String)
        {
            byte[] bytes = null;
            try
            {
                bytes = BinaryConverter.stringToBytes((String)object);
            }
            catch(NumberFormatException nfe)
            {
                // the String contains non-hex characters
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            }
            object = bytes;
            truncated_ = 0;
        }
        else if(object instanceof Reader)
        {
            int length = scale; // hack to get the length into the set method
            byte[] bytes = null;
            if(length >= 0)
            {
                try
                {
                    int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    HexReaderInputStream stream = new HexReaderInputStream((Reader)object);
                    byte[] byteBuffer = new byte[blockSize];
                    int totalBytesRead = 0;
                    int bytesRead = stream.read(byteBuffer, 0, blockSize);
                    while(bytesRead > -1 && totalBytesRead < length)
                    {
                        baos.write(byteBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        int bytesRemaining = length - totalBytesRead;
                        if(bytesRemaining < blockSize)
                        {
                            blockSize = bytesRemaining;
                        }
                        bytesRead = stream.read(byteBuffer, 0, blockSize);
                    }

                    bytes = baos.toByteArray();

                    if(bytes.length < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }

                    int objectLength = bytes.length;
                    if(bytes.length > maxLength_)
                    {
                        byte[] newValue = new byte[maxLength_];
                        System.arraycopy(bytes, 0, newValue, 0, maxLength_);
                        bytes = newValue;
                    }
                    object = bytes;
                    truncated_ = objectLength - bytes.length;
                }
                catch(ExtendedIOException eie)
                {
                    // the Reader contains non-hex characters
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, eie);
                }
                catch(IOException ie)
                {
                    JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
                }
            }
            else
            {
                JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }
        else if((JDUtilities.JDBCLevel_ >= 20 && !(object instanceof Blob)) &&
                !(object instanceof InputStream))
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        }

        savedObject_ = object;
        if(scale != -1) scale_ = scale;
    }

    private void doConversion()
    throws SQLException
    {
        try
        {
            Object object = savedObject_;
            if(object instanceof byte[])
            {
                value_ = (byte[]) object;
                int objectLength = value_.length;
                if(value_.length > maxLength_)
                {
                    byte[] newValue = new byte[maxLength_];
                    System.arraycopy(value_, 0, newValue, 0, maxLength_);
                    value_ = newValue;
                }
                truncated_ = objectLength - value_.length;
            }
            else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Blob)
            {
                Blob blob = (Blob) object;
                int blobLength = (int)blob.length();
                int lengthToUse = blobLength < 0 ? 0x7FFFFFFF : blobLength;
                if(lengthToUse > maxLength_) lengthToUse = maxLength_;
                value_ = blob.getBytes(1, lengthToUse);
                truncated_ = blobLength - lengthToUse;
            }
            else if(object instanceof InputStream)
            {
                int length = scale_; // hack to get the length into the set method
                if(length >= 0)
                {
                    InputStream stream = (InputStream)object;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int blockSize = length < AS400JDBCPreparedStatement.LOB_BLOCK_SIZE ? length : AS400JDBCPreparedStatement.LOB_BLOCK_SIZE;
                    byte[] byteBuffer = new byte[blockSize];
                    try
                    {
                        int totalBytesRead = 0;
                        int bytesRead = stream.read(byteBuffer, 0, blockSize);
                        while(bytesRead > -1 && totalBytesRead < length)
                        {
                            baos.write(byteBuffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            int bytesRemaining = length - totalBytesRead;
                            if(bytesRemaining < blockSize)
                            {
                                blockSize = bytesRemaining;
                            }
                            bytesRead = stream.read(byteBuffer, 0, blockSize);
                        }
                    }
                    catch(IOException ie)
                    {
                        JDError.throwSQLException(JDError.EXC_INTERNAL, ie);
                    }
                    
                    value_ = baos.toByteArray();

                    if(value_.length < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }

                    int objectLength = value_.length;
                    if(value_.length > maxLength_)
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

    public int getSQLType()
    {
        return SQLData.BLOB;
    }

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
        return 2147483646; // the DB2 SQL reference says this should be 2147483647 but we return 1 less to allow for NOT NULL columns
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

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//

    public InputStream getAsciiStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();

        truncated_ = 0;
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(BinaryConverter.bytesToString(value_)));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    public BigDecimal getBigDecimal(int scale) //@CRS - Could use a Converter here to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getBinaryStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return new ByteArrayInputStream(value_);
    }

    public Blob getBlob()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return new AS400JDBCBlob(value_, maxLength_);
    }

    public boolean getBoolean()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }

    public byte getByte()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public byte[] getBytes()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return value_;
    }

    public Reader getCharacterStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return new StringReader(BinaryConverter.bytesToString(value_));
    }

    public Clob getClob()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        String string = BinaryConverter.bytesToString(value_);
        return new AS400JDBCClob(string, string.length());
    }

    public Date getDate(Calendar calendar) //@CRS - Could use toLong() to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double getDouble() //@CRS - Could use a Converter here to make this work.
    throws SQLException
   {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public float getFloat() //@CRS - Could use a Converter here to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public int getInt() //@CRS - Could use a Converter here to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public long getLong() //@CRS - Could use a Converter here to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public Object getObject()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return new AS400JDBCBlob(value_, maxLength_);
    }

    public short getShort() //@CRS - Could use a Converter here to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public String getString()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return BinaryConverter.bytesToString(value_);
    }

    public Time getTime(Calendar calendar) //@CRS - Could use toLong() to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp getTimestamp(Calendar calendar) //@CRS - Could use toLong() to make this work.
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getUnicodeStream()
    throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;

        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(13488, null).stringToByteArray(BinaryConverter.bytesToString(value_)));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
        
    //@PDA jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return new StringReader(BinaryConverter.bytesToString(value_));
    }

    //@PDA jdbc40
    public NClob getNClob() throws SQLException
    {        
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        String string = BinaryConverter.bytesToString(value_);
        return new AS400JDBCNClob(string, string.length());
    }

    //@PDA jdbc40
    public String getNString() throws SQLException
    {
        if(savedObject_ != null) doConversion();
        truncated_ = 0;
        return BinaryConverter.bytesToString(value_);
    }

    //@PDA jdbc40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@PDA jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
}

