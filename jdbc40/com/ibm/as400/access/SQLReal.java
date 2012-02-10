///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLReal.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Date;
/* ifdef JDBC40 */
import java.sql.NClob;
import java.sql.RowId;
/* endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
/* endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLReal
extends SQLDataBase
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private float                  value_;

    SQLReal(SQLConversionSettings settings)
    {
        super(settings);
        value_      = 0.0f;
    }

    public Object clone()
    {
        return new SQLReal(settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        value_ = BinaryConverter.byteArrayToFloat(rawBytes, offset);                    // @D0C
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        BinaryConverter.floatToByteArray(value_, rawBytes, offset);                     // @D0C
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
            if (settings_.getDecimalSeparator().equals(",")){
               object = ((String) object).replace(',','.'); 
            }
            try
            {
                value_ = Float.valueOf((String) object).floatValue();
                // @E2d int objectLength = ((String) object).length();
                // @E2d int valueLength = Float.toString(value_).length();
                // @E2d if(valueLength < objectLength)
                // @E2d     truncated_ = objectLength - valueLength;
            }
            catch(NumberFormatException e)
            {
              
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        // @D9d
        //  We do not start messing with floating point values trying
        //  to figure out if they truncate.  The performance of this
        //  in the BigDecimal case below is probably slower then creating
        //  a table on the system.
        //
        //else if(object instanceof Double) {
        //    value_ = ((Double) object).floatValue();
        //    if(((Double) object).doubleValue() > value_)
        //        truncated_ = 8;
        //}
        //
        // @D9d
        //else if(object instanceof BigDecimal) {
        //    value_ = ((BigDecimal) object).floatValue();
        //    int objectLength = SQLDataFactory.getPrecision((BigDecimal) object);
        //    int valueLength = SQLDataFactory.getPrecision(new BigDecimal(value_));
        //    if(valueLength < objectLength)
        //        truncated_ = objectLength - valueLength;
        //}

        else if(object instanceof Number)
        {
            // Set the value to the right type.
            value_ = ((Number) object).floatValue();  // @D9c

            // Get the whole number portion of that value.
            //long value = (long) value_;               // @D9c //@bigdectrunc change to follow native driver

            // Get the original value as a long.  This is the
            // largest precision we can test for for a truncation.
            //long truncTest = ((Number) object).longValue();  // @D9c //@bigdectrunc  

            // If they are not equal, then we truncated significant
            // data from the original value the user wanted us to insert.
            //if(truncTest != value)     // @D9c //@bigdectrunc 
                //truncated_ = 1;  //@bigdectrunc 
        }


        else if(object instanceof Boolean)
            value_ = (((Boolean) object).booleanValue() == true) ? 1f : 0f;

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
        return SQLData.REAL;
    }

    public String getCreateParameters()
    {
        return null;
    }


    public int getDisplaySize()
    {
        return 13;
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
        return "REAL";
    }

    public int getMaximumPrecision()
    {
        return 24;
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
        return 24;
    }

    public int getRadix()
    {
        return 2;               //@K1C Changed from 10
    }

    public int getScale()
    {
        return 0;
    }

    public int getType()
    {
        return java.sql.Types.REAL;
    }

    public String getTypeName()
    {
        return "REAL";
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
        return SQLDataFactory.getPrecision(Float.toString(value_));
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
        truncated_ = 0; outOfBounds_ = false; 
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

        // BigDecimal bigDecimal = new BigDecimal(Float.toString(value_))     // @A0D

        BigDecimal bigDecimal = null;                                           // @A0A

        String numString = Float.toString(value_);                              // @A0A
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
                outOfBounds_ = false;
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
        return(value_ != 0.0f);
    }

    public byte getByte()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Byte.MAX_VALUE || value_ < Byte.MIN_VALUE)      //@trunc
        {                                                           //@trunc
            if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)//@trunc
            {                                                       //@trunc
                truncated_ = 3;                                     //@trunc
                outOfBounds_ = true;
            }                                                       //@trunc
            else                                                    //@trunc
            {                                                       //@trunc
                truncated_ = 1;                                     //@trunc
                outOfBounds_ = true;
            }                                                       //@trunc
        }                                                           //@trunc
        return(byte) value_;
    }

    public byte[] getBytes()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
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

        if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)              //@trunc
        {                                                                         //@trunc
            truncated_ = 4;                                                       //@trunc
            outOfBounds_ = true;
        }                                                                         //@trunc
               
        return(int) value_;
    }

    public long getLong()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Long.MAX_VALUE || value_ < Long.MIN_VALUE)                    //@trunc
        {                                                                         //@trunc
            truncated_ = 8;                                                       //@trunc
            outOfBounds_ = true;
        }                                                                         //@trunc  
        return(long) value_;
    }

    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return new Float(value_);
    }

    public short getShort()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)    //@trunc
        {                                                           //@trunc
            truncated_ = 2;                                         //@trunc
            outOfBounds_ = true;
        }                                                           //@trunc
        return(short) value_;
    }

    public String getString()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        String stringRep = Float.toString(value_);
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


    
    //@pda jdbc40
    public String getNString() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        String stringRep = Float.toString(value_);
        int decimal = stringRep.indexOf('.');
        if(decimal == -1)
            return stringRep;
        else
            return stringRep.substring(0, decimal)
            + settings_.getDecimalSeparator()
            + stringRep.substring(decimal+1);
    }

/* ifdef JDBC40 */

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
/* endif */ 
    
}
