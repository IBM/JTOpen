///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLVarbinary.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
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

final class SQLVarbinary
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private static final byte[]     default_    = new byte[0];

    private SQLConversionSettings   settings_;
    private int                     length_;
    private boolean                 longValue_;
    private int                     maxLength_;
    private int                     truncated_;
    private byte[]                  value_;

    SQLVarbinary(int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        length_         = 0;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = default_;
    }

    public Object clone()
    {
        return new SQLVarbinary(maxLength_, settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        length_ = BinaryConverter.byteArrayToUnsignedShort(rawBytes, offset);;
        AS400ByteArray typeConverter = new AS400ByteArray(length_);
        value_ = (byte[])typeConverter.toObject(rawBytes, offset+2);
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        AS400ByteArray typeConverter = new AS400ByteArray(length_);
        BinaryConverter.unsignedShortToByteArray(length_, rawBytes, offset);
        typeConverter.toBytes(value_, rawBytes, offset + 2);
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        if(object instanceof String)
            value_ = SQLBinary.stringToBytes((String)object);

        else if(object instanceof byte[])
            value_ = (byte[])object;

        else if(object instanceof InputStream)
        {
            //value_ = JDUtilities.streamToBytes((InputStream)object, scale);

            int length = scale; // hack to get the length into the set method
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
                    JDError.throwSQLException(this, JDError.EXC_INTERNAL, ie);
                }
                value_ = baos.toByteArray();
                if(value_.length < length)
                {
                    // a length longer than the stream was specified
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                }
            }
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        else if(object instanceof Reader)
        {
            // value_ = SQLBinary.stringToBytes(JDUtilities.readerToString((Reader)object, scale));

            int length = scale; // hack to get the length into the set method
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
                    value_ = baos.toByteArray();
                    if(value_.length < length)
                    {
                        // a length longer than the stream was specified
                        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
                    }
                }
                catch(IOException ie)
                {
                    JDError.throwSQLException(this, JDError.EXC_INTERNAL, ie);
                }
            }
            else
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Blob)
            value_ = ((Blob)object).getBytes(1, (int)((Blob)object).length());

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
            value_ = SQLBinary.stringToBytes(((Clob)object).getSubString(1, (int)((Clob)object).length()));

        else
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        // Truncate if necessary.
        int valueLength = value_.length;
        if(valueLength > maxLength_)
        {
            byte[] newValue = new byte[maxLength_];
            System.arraycopy(value_, 0, newValue, 0, maxLength_);
            value_ = newValue;
            truncated_ = valueLength - maxLength_;
        }
        else
            truncated_ = 0;

        length_ = value_.length;
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.VARBINARY;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH");
    }

    public int getDisplaySize()
    {
        return maxLength_ ;
    }

    public String getJavaClassName()
    {
        return "[B";
    }

    public String getLiteralPrefix()
    {
        return "X\'";
    }

    public String getLiteralSuffix()
    {
        return "\'";
    }

    public String getLocalName()
    {
        return "VARBINARY";
    }

    public int getMaximumPrecision()
    {
        return 32739;
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
        return 908;
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
        return java.sql.Types.VARBINARY;
    }

    public String getTypeName()
    {
        return "VARBINARY";
    }

    public boolean isSigned()
    {
        return false;
    }

    public boolean isText()
    {
        return true;
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

    public InputStream toAsciiStream()
    throws SQLException
    {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(SQLBinary.bytesToString(toBytes())));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    public BigDecimal toBigDecimal(int scale)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream toBinaryStream()
    throws SQLException
    {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new ByteArrayInputStream(toBytes());
    }

    public Blob toBlob()
    throws SQLException
    {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new AS400JDBCBlob(toBytes(), maxLength_);
    }

    public boolean toBoolean()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }

    public byte toByte()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public byte[] toBytes()
    {
        // Truncate to the max field size if needed.
        // Do not signal a DataTruncation per the spec.
        int maxFieldSize = settings_.getMaxFieldSize();
        if((value_.length > maxFieldSize) && (maxFieldSize > 0))
        {
            byte[] truncatedValue = new byte[maxFieldSize];
            System.arraycopy(value_, 0, truncatedValue, 0, maxFieldSize);
            return truncatedValue;
        }
        else
        {
            return value_;
        }
    }

    public Reader toCharacterStream()
    throws SQLException
    {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(SQLBinary.bytesToString(toBytes()));
    }

    public Clob toClob()
    throws SQLException
    {
        // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        return new AS400JDBCClob(SQLBinary.bytesToString(toBytes()), maxLength_);
    }

    public Date toDate(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double toDouble()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public float toFloat()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public int toInt()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public long toLong()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public Object toObject()
    {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return toBytes();
    }

    public short toShort()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public String toString()
    {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        return SQLBinary.bytesToString(toBytes());
    }

    public Time toTime(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp toTimestamp(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream toUnicodeStream()
    throws SQLException
    {
        // This is written in terms of toBytes(), since it will
        // handle truncating to the max field size if needed.
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(13488, null).stringToByteArray(SQLBinary.bytesToString(toBytes())));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
}

