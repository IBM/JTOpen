///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLChar.java
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

final class SQLChar
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;
    private int                     maxLength_;
    private int                     truncated_;
    private String                  value_;

    SQLChar(int maxLength, SQLConversionSettings settings)
    {
        settings_       = settings;
        maxLength_      = maxLength;
        truncated_      = 0;
        value_          = ""; // @C4C
    }

    public Object clone()
    {
        return new SQLChar(maxLength_,settings_);
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
        String value = null;                                                        // @C2A

        if(object instanceof String)
            value = (String)object;                                                 // @C2C

        else if(object instanceof Number)
            value = object.toString();                                              // @C2C

        else if(object instanceof Boolean)
            value = object.toString();                                              // @C2C

        else if(object instanceof Time)
            value = SQLTime.timeToString((Time)object, settings_, calendar);        // @C2C

        else if(object instanceof Timestamp)
            value = SQLTimestamp.timestampToString((Timestamp)object, calendar);    // @C2C

        else if(object instanceof java.util.Date)                                   // @F5M @F5C
            value = SQLDate.dateToString((java.util.Date)object, settings_, calendar); // @C2C @F5C

        else if(JDUtilities.JDBCLevel_ >= 20 && object instanceof Clob)
        {
            Clob clob = (Clob)object;
            value = clob.getSubString(1, (int)clob.length());
        }                                                                           // @C2C

        if(value == null)                                                           // @C2C
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        value_ = value;                                                             // @C2A

        // Set to the exact length.
        int valueLength = value_.length();
        int exactLength = getDisplaySize();                         // @C1A
        if(valueLength < exactLength)                               // @C1C
        {
            StringBuffer buffer = new StringBuffer(value_);
            char c = '\u0020';                                      // @F66c - Pad with single byte space for now
            for(int i = valueLength; i < exactLength; ++i)          // @C1C
                buffer.append(c);                                   //@F6C
            value_ = buffer.toString();
            truncated_ = 0;
        }
        else if(valueLength > exactLength)
        {                                                           // @C1C @F6C
            value_ = value_.substring(0, exactLength);              // @C1C
            truncated_ = valueLength - exactLength;                 // @C1C
        }
        else
            truncated_ = 0;
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.CHAR;
    }

    public String getCreateParameters()
    {
        return AS400JDBCDriver.getResource("MAXLENGTH");
    }

    public int getDisplaySize()
    {
        return maxLength_;
    }

    //@F1A JDBC 3.0
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
        return "CHAR";
    }

    public int getMaximumPrecision()
    {
        return 32765;
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
        return 452;
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
        return "CHAR";
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
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(value_));
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
            BigDecimal bigDecimal = new BigDecimal(SQLDataFactory.convertScientificNotation(value_.trim())); // @F3C
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
        try
        {
            return new AS400JDBCBlob(BinaryConverter.stringToBytes(value_), maxLength_);
        }
        catch(NumberFormatException nfe)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
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
        try
        {
            return BinaryConverter.stringToBytes(value_);
        }
        catch(NumberFormatException nfe)
        {
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe);
            return null;
        }
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
        // Do not signal a DataTruncation per the spec. @B1A
        int maxFieldSize = settings_.getMaxFieldSize();
        if((value_.length() > maxFieldSize) && (maxFieldSize > 0))
        {
            // @B1D truncated_ = value_.length() - maxFieldSize;
            return value_.substring(0, maxFieldSize);
        }
        else
        {
            // @B1D truncated_ = 0;
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

    // @A1A
    // Added method trim() to trim the string.
    public void trim()                               // @A1A
    {                                                // @A1A
        value_ = value_.trim();                      // @A1A
    }                                                // @A1A
}

