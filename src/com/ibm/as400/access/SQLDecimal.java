///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDecimal.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;



class SQLDecimal
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private.
    private static final BigDecimal default_ = BigDecimal.valueOf (0); // @C2A

    private SQLConversionSettings   settings_;
    private int                     precision_;
    private int                     scale_;
    private int                     truncated_;
    private AS400PackedDecimal      typeConverter_;
    private BigDecimal              value_;
    private JDProperties            properties_;




    SQLDecimal (int precision,
                int scale,
                SQLConversionSettings settings,
                JDProperties properties)
    {
        settings_       = settings;
        precision_      = precision;
        scale_          = scale;
        truncated_      = 0;
        typeConverter_  = new AS400PackedDecimal (precision_, scale_);
        value_          = default_; // @C2C
        properties_     = properties;
    }



    public Object clone ()
    {
        return new SQLDecimal (precision_, scale_, settings_, properties_);
    }



    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccisdConverter) //@P0C
    throws SQLException
    {
        value_ = ((BigDecimal) typeConverter_.toObject (rawBytes, offset));
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        typeConverter_.toBytes (value_, rawBytes, offset);
    }



    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//



    public void set (Object object, Calendar calendar, int scale)
    throws SQLException
    {
        BigDecimal bigDecimal = null;

        if(object instanceof String)
        {
            try
            {
                String value = SQLDataFactory.convertScientificNotation ((String)object); // @F3C
                if(scale >= 0)
                    value = SQLDataFactory.truncateScale (value, scale);
                bigDecimal = new BigDecimal (value);
            }
            catch(NumberFormatException e)
            {
                JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        else if(object instanceof Number)
        {
            String value = SQLDataFactory.convertScientificNotation (object.toString ()); // @C1C
            if(scale >= 0)
                value = SQLDataFactory.truncateScale (value, scale);
            bigDecimal = new BigDecimal (value);
        }

        else if(object instanceof Boolean)
            bigDecimal = (((Boolean) object).booleanValue() == true) ? BigDecimal.valueOf (1) : BigDecimal.valueOf (0);

        else
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);

        // Truncate if necessary.  If we ONLY truncate on the right side, we don't    @E2C
        // need to report it.  If we truncate on the left side, then we report the    @E2A
        // number of truncated digits on both ends...this will make the dataSize      @E2A
        // and transferSize make sense on the resulting DataTruncation.               @E2A
        truncated_ = 0;
        int otherScale = bigDecimal.scale ();
        if(otherScale > scale_)
            truncated_ += otherScale - scale_;
        value_ = bigDecimal.setScale (scale_, BigDecimal.ROUND_DOWN);       // @E2C

        int otherPrecision = SQLDataFactory.getPrecision (value_);
        if(otherPrecision > precision_)
        {
            int digits = otherPrecision - precision_;
            truncated_ += digits;
            value_ = SQLDataFactory.truncatePrecision (value_, digits);
        }
        else                                                               // @E2A
            truncated_ = 0;  // No left side truncation, report nothing       @E2A
                             // (even if there was right side truncation).    @E2A
    }



    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//



    public String getCreateParameters ()
    {
        StringBuffer buffer = new StringBuffer ();
        buffer.append (AS400JDBCDriver.getResource ("PRECISION"));
        buffer.append (",");
        buffer.append (AS400JDBCDriver.getResource ("SCALE"));
        return buffer.toString ();
    }


    public int getDisplaySize ()
    {
        return precision_ + 2;
    }


    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.math.BigDecimal";
    }


    public String getLiteralPrefix ()
    {
        return null;
    }


    public String getLiteralSuffix ()
    {
        return null;
    }


    public String getLocalName ()
    {
        return "DECIMAL";
    }



    public int getMaximumPrecision ()
    {
        return properties_.getInt(JDProperties.MAXIMUM_PRECISION);
    }


    public int getMaximumScale ()
    {
        return properties_.getInt(JDProperties.MAXIMUM_SCALE);
    }


    public int getMinimumScale ()
    {
        return 0;
    }


    public int getNativeType ()
    {
        return 484;
    }



    public int getPrecision ()
    {
        return precision_;
    }


    public int getRadix ()
    {
        return 10;
    }



    public int getScale ()
    {
        return scale_;
    }


    public int getType ()
    {
        return java.sql.Types.DECIMAL;
    }



    public String getTypeName ()
    {
        return "DECIMAL";
    }


    // @E1D    public boolean isGraphic ()
    // @E1D    {
    // @E1D        return false;
    // @E1D    }



    public boolean isSigned ()
    {
        return true;
    }



    public boolean isText ()
    {
        return false;
    }



    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//



    public int getActualSize ()
    {
        return precision_;
    }



    public int getTruncated ()
    {
        return truncated_;
    }



    public InputStream toAsciiStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public BigDecimal toBigDecimal (int scale)
    throws SQLException
    {
        if(scale >= 0)
        {
            if(scale >= value_.scale())
            {
                truncated_ = 0;
                return value_.setScale (scale);
            }
            else
            {
                truncated_ = value_.scale() - scale;
                return value_.setScale (scale, BigDecimal.ROUND_HALF_UP);
            }
        }
        else
            return value_;
    }



    public InputStream toBinaryStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Blob toBlob ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public boolean toBoolean ()
    throws SQLException
    {
        truncated_ = 0;
        return(value_.compareTo (BigDecimal.valueOf (0)) != 0);
    }



    public byte toByte ()
    throws SQLException
    {
        truncated_ = value_.scale();
        return(byte) value_.byteValue();
    }



    public byte[] toBytes ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Reader toCharacterStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Clob toClob ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Date toDate (Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public double toDouble ()
    throws SQLException
    {
        truncated_ = 0;
        return value_.doubleValue ();
    }



    public float toFloat ()
    throws SQLException
    {
        truncated_ = 0;
        return value_.floatValue ();
    }



    public int toInt ()
    throws SQLException
    {
        truncated_ = value_.scale();
        return value_.intValue ();
    }



    public long toLong ()
    throws SQLException
    {
        truncated_ = value_.scale();
        return value_.longValue ();
    }



    public Object toObject ()
    {
        return value_;
    }



    public short toShort ()
    throws SQLException
    {
        truncated_ = value_.scale();
        return(short) value_.shortValue ();
    }



    public String toString ()
    {
        truncated_ = 0;
        String stringRep = value_.toString ();
        int decimal = stringRep.indexOf ('.');
        if(decimal == -1)
            return stringRep;
        else
            return stringRep.substring (0, decimal)
            + settings_.getDecimalSeparator()
            + stringRep.substring (decimal+1);
    }



    public Time toTime (Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public Timestamp toTimestamp (Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



    public InputStream  toUnicodeStream ()
    throws SQLException
    {
        JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }



}
