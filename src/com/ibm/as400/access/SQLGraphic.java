///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLGraphic.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.InputStream;
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

final class SQLGraphic
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // public static field to prevent the need to instanceof the SQLData types
    public static final int SQL_TYPE = SQLData.GRAPHIC;

    // Private data.
    private SQLConversionSettings   settings_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;
    private String                  originalValue_;

    SQLGraphic(int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = "";
        originalValue_  = "";
    }

    public Object clone()
    {
        return new SQLGraphic(maxLength_, settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;

        value_ = ccsidConverter.byteArrayToString(rawBytes, offset, maxLength_, bidiStringType);
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
    throws SQLException
    {
        //   We originally padded with a single byte space.  We now have the
        //   ccsid so we can figure out if that was right or not.  If we should
        //   have use the double byte space, re-pad.
        int ccsid = ccsidConverter.getCcsid();
        if(ccsid != 13488 && ccsid != 1200)
        {
            int valueLength = originalValue_.length();
            int exactLength = getDisplaySize();
            if(valueLength < exactLength)
            {
                StringBuffer buffer = new StringBuffer(originalValue_);
                char c = '\u3000';
                for(int i = valueLength; i < exactLength; ++i)
                    buffer.append(c);
                value_ = buffer.toString();
            }
        }

        int bidiStringType = settings_.getBidiStringType();

        // if bidiStringType is not set by user, use ccsid to get value
        if(bidiStringType == -1)
            bidiStringType = ccsidConverter.bidiStringType_;

        try
        {
            ccsidConverter.stringToByteArray(value_, rawBytes, offset, maxLength_, bidiStringType);
        }
        catch(CharConversionException e)
        {
            maxLength_ = ccsidConverter.stringToByteArray(value_, bidiStringType).length;
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
        }
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        String value = null;

        if(object instanceof String)
            value = (String) object;

        else if(object instanceof Number)
            value = object.toString();

        else if(object instanceof Boolean)
            value = object.toString();

        else if(object instanceof Time)
            value = SQLTime.timeToString((Time) object, settings_, calendar);

        else if(object instanceof Timestamp)
            value = SQLTimestamp.timestampToString((Timestamp) object, calendar);

        else if(object instanceof java.util.Date)
            value = SQLDate.dateToString((java.util.Date) object, settings_, calendar);

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
        {
            Clob clob = (Clob)object;
            value = clob.getSubString(1, (int)clob.length());
        }

        if(value == null)
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        value_ = value;
        originalValue_ = value;


        // Set to the exact length.
        int valueLength = value_.length();
        int exactLength = getDisplaySize();
        if(valueLength < exactLength)
        {
            StringBuffer buffer = new StringBuffer(value_);
            char c = '\u0020';
            for(int i = valueLength; i < exactLength; ++i)
                buffer.append(c);
            value_ = buffer.toString();
            truncated_ = 0;
        }
        else if(valueLength > exactLength)
        {
            value_ = value_.substring(0, exactLength);
            truncated_ = valueLength - exactLength;
        }
        else
            truncated_ = 0;
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
        return maxLength_ / 2;
    }

    public String getJavaClassName()
    {
        return "java.lang.String";
    }

    public String getLiteralPrefix()
    {
        return "\'";
    }

    public String getLiteralSuffix()
    {
        return "\'";
    }

    public String getLocalName()
    {
        return "GRAPHIC";
    }

    public int getMaximumPrecision()
    {
        return 16382;
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
        return 468;
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
        return java.sql.Types.CHAR;
    }

    public String getTypeName()
    {
        return "GRAPHIC";
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
        return value_.length();
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
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(13488, null).stringToByteArray(value_));
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
        try
        {
            BigDecimal bigDecimal = new BigDecimal(SQLDataFactory.convertScientificNotation(value_.trim()));
            if(scale >= 0)
            {
                if(scale >= bigDecimal.scale())
                {
                    truncated_ = 0;
                    return bigDecimal.setScale(scale);
                }
                else
                {
                    truncated_ = bigDecimal.scale() - scale;
                    return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
                }
            }
            else
                return bigDecimal;
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return null;
        }
    }

    public InputStream toBinaryStream()
    throws SQLException
    {
        return new HexReaderInputStream(new StringReader(value_));
    }

    public Blob toBlob()
    throws SQLException
    {
        return new AS400JDBCBlob(SQLBinary.stringToBytes(value_), maxLength_);
    }

    public boolean toBoolean()
    throws SQLException
    {
        truncated_ = 0;

        // If value equals "true", "false", "1", or "0", then return the
        // corresponding boolean, otherwise an empty string is
        // false, a non-empty string is true.
        String trimmedValue = value_.trim();        
        return((trimmedValue.length() > 0) 
               && (! trimmedValue.equalsIgnoreCase("false"))
               && (! trimmedValue.equals("0")));
    }

    public byte toByte()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(value_.trim())).byteValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public byte[] toBytes()
    throws SQLException
    {
        return SQLBinary.stringToBytes(value_);
    }

    public Reader toCharacterStream()
    throws SQLException
    {
        // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        return new StringReader(toString());
    }

    public Clob toClob()
    throws SQLException
    {
        // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        return new AS400JDBCClob(toString(), maxLength_);
    }

    public Date toDate(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        return SQLDate.stringToDate(value_, settings_, calendar);
    }

    public double toDouble()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(value_.trim())).doubleValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public float toFloat()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(value_.trim())).floatValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public int toInt()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(value_.trim())).intValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public long toLong()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(value_.trim())).longValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public Object toObject()
    {
        // This is written in terms of toString(), since it will
        // handle truncating to the max field size if needed.
        return toString();
    }

    public short toShort()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return(new Double(value_.trim())).shortValue();
        }
        catch(NumberFormatException e)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
            return -1;
        }
    }

    public String toString()
    {
        // Truncate to the max field size if needed.
        // Do not signal a DataTruncation per the spec.
        int maxFieldSize = settings_.getMaxFieldSize();
        if((value_.length() > maxFieldSize) && (maxFieldSize > 0))
        {
            return value_.substring(0, maxFieldSize);
        }
        else
        {
            return value_;
        }
    }

    public Time toTime(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        return SQLTime.stringToTime(value_, settings_, calendar);
    }

    public Timestamp toTimestamp(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        return SQLTimestamp.stringToTimestamp(value_, calendar);
    }

    public InputStream toUnicodeStream()
    throws SQLException
    {
        try
        {
            return new ReaderInputStream(new StringReader(value_), 13488);
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }

    // Added method trim() to trim the string.
    public void trim()
    {
        value_ = value_.trim();
    }
}

