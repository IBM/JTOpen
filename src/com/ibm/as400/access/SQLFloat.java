///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLFloat.java
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
/*ifdef JDBC40 
import java.sql.NClob;
import java.sql.RowId;
endif */ 
import java.sql.SQLException;
/*ifdef JDBC40 
import java.sql.SQLXML;
endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLFloat
extends SQLDataBase
{
    
	static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private double                 value_;

    SQLFloat(SQLConversionSettings settings)
    {
        super(settings); 
        value_      = 0.0d;
    }

    public Object clone()
    {
        return new SQLFloat(settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        value_ = BinaryConverter.byteArrayToDouble(rawBytes, offset);                   // @D0C
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        BinaryConverter.doubleToByteArray(value_, rawBytes, offset);                    // @D0C
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 

        if(object instanceof String)
        {
            try
            {
                value_ = Double.valueOf((String) object).doubleValue();
                // You can't test for data truncation of a number by testing
                // the lengths of two string versions of it.
                // Example string that should work but will fail:
                //      "4.749000000000E+00"
                //@E2D int objectLength = ((String) object).length();
                //@E2D int valueLength = Double.toString(value_).length();
                //@E2D if(valueLength < objectLength)
                //@E2D    truncated_ = objectLength - valueLength;
            }
            catch(NumberFormatException e)
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        //else if(object instanceof BigDecimal) {
        //    value_ = ((BigDecimal) object).doubleValue();
        //    int objectLength = SQLDataFactory.getPrecision((BigDecimal) object);
        //    int valueLength = SQLDataFactory.getPrecision(new BigDecimal(value_));
        //    if(valueLength < objectLength)
        //        truncated_ = objectLength - valueLength;
        //}

        else if(object instanceof Number)
        {
            // Set the value to the right type.
            value_ = ((Number) object).doubleValue();   // @D9c

            // Get the whole number portion of that value.
            //long value = (long) value_;                 // @D9a //@bigdectrunc change to follow native driver

            // Get the original value as a long.  This is the
            // largest precision we can test for for a truncation.
           // long truncTest = ((Number) object).longValue();  // @D9a //@bigdectrunc 

            // If they are not equal, then we truncated significant
            // data from the original value the user wanted us to insert.
            //if(truncTest != value)                          // @D9a //@bigdectrunc
                //truncated_ = 1; //@bigdectrunc 
        }

        else if(object instanceof Boolean)
            value_ = (((Boolean) object).booleanValue() == true) ? 1d : 0d;

        else
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.FLOAT;
    }

    public String getCreateParameters()
    {
        return null;
    }

    public int getDisplaySize()
    {
        return 22;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.lang.Float";
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
        return "FLOAT";
    }

    public int getMaximumPrecision()
    {
        return 53;
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
        return 480;
    }

    public int getPrecision()
    {
        return 53;
    }

    public int getRadix()
    {
        return 2;           //@K1C changed from 10
    }

    public int getScale()
    {
        return 0;
    }

    public int getType()
    {
        return java.sql.Types.FLOAT;
    }

    public String getTypeName()
    {
        return "FLOAT";
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
        return SQLDataFactory.getPrecision(Double.toString(value_));
    }

    public int getTruncated()
    {
        return truncated_;
    }

    public boolean getOutOfBounds() {
      return outOfBounds_; 
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//


    public BigDecimal getBigDecimal(int scale)
    throws SQLException
    {
        // Convert the value to a String before creating the
        // BigDecimal.  This will create the exact BigDecimal
        // that we want.  If you pass the value directly to
        // BigDecimal, then the value is not exact, and the
        // scale becomes bigger than expected.

        // @A0A
        // Modified the code to deal with numbers in scientific
        // notations. The numbers that are in scientific notation
        // are parsed to a base (the part before 'E') and an
        // exponent (the part after 'E'). The base is then used
        // to construct the BigDecimal object and then the exponent
        // is used to shift the decimal point to its rightful place.

        // BigDecimal bigDecimal = new BigDecimal(Double.toString(value_))    // @A0D

        truncated_ = 0; outOfBounds_ = false; 

        BigDecimal bigDecimal = null;                                           // @A0A

        String numString = Double.toString(value_);                             // @A0A
        int eIndex = numString.indexOf("E");                                    // @A0A
        if(eIndex == -1)
        {                                                     // @A0A
            bigDecimal = new BigDecimal(numString);                             // @A0A
        }                                                                       // @A0A
        else
        {                                                                  // @A0A
            String base = numString.substring(0, eIndex);                       // @A0A
            int exponent = Integer.parseInt(numString.substring(eIndex+1));     // @A0A
            bigDecimal = new BigDecimal(base);                                  // @A0A
            bigDecimal = bigDecimal.movePointRight(exponent);                   // @A0A
        }                                                                       // @A0A

        if(scale >= 0)
        {
            if(scale >= bigDecimal.scale())
            {
                truncated_ = 0; outOfBounds_ = false; 
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

    public InputStream getBinaryStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Blob getBlob()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public boolean getBoolean()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return(value_ != 0.0d);
    }

    public byte getByte()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Byte.MAX_VALUE || value_ < Byte.MIN_VALUE)
        {
            if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
            {
                truncated_ = 3; outOfBounds_=true;
            }
            else
            {
                truncated_ = 1; outOfBounds_=true;
            }
        }
        return(byte) value_;
    }

    public byte[] getBytes()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Reader getCharacterStream()
    throws SQLException
    {
      return new java.io.StringReader(getString());
        
    }

    public Clob getClob()
    throws SQLException
    {
      String string = getString(); 
      return new AS400JDBCClob(string, string.length());
    }

    public Date getDate(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double getDouble()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return(double) value_;
    }

    public float getFloat()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return(float) value_;
    }

    public int getInt()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
        {
            truncated_ = 1; // this may not be accurate but we know truncation will occur
            outOfBounds_=true;
        }
        return(int) value_;
    }

    public long getLong()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Long.MAX_VALUE || value_ < Long.MIN_VALUE)
        {
            truncated_ = 1; // this is not necessarily correct, but we know there is truncation
            outOfBounds_=true;
        }
        return(long) value_;
    }

    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return new Double(value_);
    }

    public short getShort()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
        {
            if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
            {
                truncated_ = 3;
                outOfBounds_=true;
            }
            else
            {
                truncated_ = 2;
                outOfBounds_=true;
            }
        }
        return(short) value_;
    }

    public String getString()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        String stringRep = Double.toString(value_);
        int decimal = stringRep.indexOf('.');
        if(decimal == -1)
            return stringRep;
        else
            return stringRep.substring(0, decimal)
            + settings_.getDecimalSeparator()
            + stringRep.substring(decimal+1);
    }

    public Time getTime(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    

    /* ifdef JDBC40 
    //@pda jdbc40
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@pda jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
    endif */ 
    
    // @array
}
