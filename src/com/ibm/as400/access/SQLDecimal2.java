///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDecimal2.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.io.ByteArrayInputStream;
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

class SQLDecimal2
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;
    private int                     precision_;
    private int                     scale_;
    private AS400PackedDecimal      typeConverter_;
    private double                  value_;
    private JDProperties            properties_;  // @M0A - added JDProperties so we can get the scale & precision
    private int                     vrm_;         // @M0A

    SQLDecimal2(int precision,
                int scale,
                SQLConversionSettings settings,
                int vrm,                  // @M0C
                JDProperties properties)  // @M0C
    {
        settings_       = settings;
        precision_      = precision;
        scale_          = scale;
        typeConverter_  = new AS400PackedDecimal(precision_, scale_);
        value_          = 0;
        vrm_            = vrm;         // @M0A
        properties_     = properties;  // @M0A
    }

    public Object clone()
    {
        return new SQLDecimal2(precision_, scale_, settings_, vrm_, properties_);  // @M0C
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccisdConverter) //@P0C
    throws SQLException
    {
        value_ = typeConverter_.toDouble(rawBytes, offset);
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        typeConverter_.toBytes(value_, rawBytes, offset);
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
        {
            try
            {
                value_ = Double.valueOf((String)object).doubleValue();
            }
            catch(NumberFormatException nfe)
            {                                // @E4A
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe); // @E4A
            }
        }

        else if(object instanceof Number)
            value_ = ((Number)object).doubleValue();

        else if(object instanceof Boolean)
            value_ = ((Boolean)object).booleanValue() ? 1 : 0;

        else
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public String getCreateParameters()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(AS400JDBCDriver.getResource("PRECISION"));
        buffer.append(",");
        buffer.append(AS400JDBCDriver.getResource("SCALE"));
        return buffer.toString();
    }

    public int getDisplaySize()
    {
        return precision_ + 2;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.math.BigDecimal";   
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
        return "DECIMAL";
    }

    public int getMaximumPrecision()
    {
        // @M0C - change to check vrm and JDProperties
        if(vrm_ >= JDUtilities.vrm530)
            return properties_.getInt(JDProperties.MAXIMUM_PRECISION);
        else
            return 31;
    }

    public int getMaximumScale()
    {
        // @M0C - change to check vrm and JDProperties
        if(vrm_ >= JDUtilities.vrm530)
            return properties_.getInt(JDProperties.MAXIMUM_SCALE);
        else
            return 31;
    }

    public int getMinimumScale()
    {
        return 0;
    }

    public int getNativeType()
    {
        return 484;
    }

    public int getPrecision()
    {
        return precision_;
    }

    public int getRadix()
    {
        return 10;
    }

    public int getScale()
    {
        return scale_;
    }

    public int getType()
    {
        return java.sql.Types.DECIMAL;
    }

    public String getTypeName()
    {
        return "DECIMAL";
    }

    public boolean isSigned()
    {
        return true;
    }

    public boolean isText()
    {
        return false;
    }

    public int getActualSize()
    {
        return precision_;
    }

    public int getTruncated()
    {
        return 0;
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
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(toString()));
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
        return new BigDecimal(value_);
    }

    public InputStream toBinaryStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Blob toBlob()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public boolean toBoolean()
    throws SQLException
    {
        return(value_ != 0);
    }

    public byte toByte()
    throws SQLException
    {
        return(byte)value_;
    }

    public byte[] toBytes()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Reader toCharacterStream()
    throws SQLException
    {
        return new StringReader(toString());
    }

    public Clob toClob()
    throws SQLException
    {
        String string = toString();
        return new AS400JDBCClob(string, string.length());
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
        return value_;
    }

    public float toFloat()
    throws SQLException
    {
        return(float)value_;
    }

    public int toInt()
    throws SQLException
    {
        return(int)value_;
    }

    public long toLong()
    throws SQLException
    {
        return(long)value_;
    }

    public Object toObject()
    {
        return new Double(value_);
    }

    public short toShort()
    throws SQLException
    {
        return(short)value_;
    }

    public String toString()
    {
        String stringRep = Double.toString(value_);
        int decimal = stringRep.indexOf('.');
        if(decimal == -1)
            return stringRep;
        else
            return stringRep.substring(0, decimal)
            + settings_.getDecimalSeparator()
            + stringRep.substring(decimal+1);
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

    public InputStream  toUnicodeStream()
    throws SQLException
    {
        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(13488, null).stringToByteArray(toString()));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
}
