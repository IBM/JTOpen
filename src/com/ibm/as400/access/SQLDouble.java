///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDouble.java
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



final class SQLDouble
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    // @D0D private static AS400Float8      typeConverter_;
    private double                 value_;



    // @D0D static
    // @D0D {
    // @D0D     typeConverter_ = new AS400Float8 ();
    // @D0D }



    SQLDouble (SQLConversionSettings settings)
    {
        settings_   = settings;
        truncated_  = 0;
        value_      = 0.0d;
    }



    public Object clone ()
    {
        return new SQLDouble (settings_);
    }



    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        value_ = BinaryConverter.byteArrayToDouble(rawBytes, offset);                   // @D0C
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        BinaryConverter.doubleToByteArray(value_, rawBytes, offset);                    // @D0C
    }



    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//



    public void set (Object object, Calendar calendar, int scale)
    throws SQLException
    {
        truncated_ = 0;

        if(object instanceof String)
        {
            try
            {
                value_ = Double.valueOf ((String) object).doubleValue ();
                // You can't test for data truncation of a number by testing
                // the lengths of two string versions of it.
                // Example string that should work but will fail:
                //      "4.749000000000E+00"
                //@E2D int objectLength = ((String) object).length ();
                //@E2D int valueLength = Double.toString (value_).length ();
                //@E2D if (valueLength < objectLength)
                //@E2D     truncated_ = objectLength - valueLength;
            }
            catch(NumberFormatException e)
            {
                JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        // @ D9d
        // else if (object instanceof BigDecimal) {
        //     value_ = ((BigDecimal) object).doubleValue ();
        //     int objectLength = SQLDataFactory.getPrecision ((BigDecimal) object);
        //     int valueLength = SQLDataFactory.getPrecision (new BigDecimal (value_));
        //     if (valueLength < objectLength)
        //         truncated_ = objectLength - valueLength;
        // }

        else if(object instanceof Number)
        {
            // Set the value to the right type.
            value_ = ((Number) object).doubleValue();

            // Get the whole number portion of that value.
            long value = (long) value_;                      // @D9a

            // Get the original value as a long.  This is the
            // largest precision we can test for for a truncation.
            long truncTest = ((Number) object).longValue();  // @D9a

            // If they are not equal, then we truncated significant
            // data from the original value the user wanted us to insert.
            if(truncTest != value)                          // @D9a
                truncated_ = 1;                              // @D9a
        }

        else if(object instanceof Boolean)
            value_ = (((Boolean) object).booleanValue() == true) ? 1d : 0d;

        else
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
    }



    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.DOUBLE;
    }

    public String getCreateParameters ()
    {
        return null;
    }


    public int getDisplaySize ()
    {
        return 22;
    }


    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.lang.Double";
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
        // Use "FLOAT" not "DOUBLE".  See ODBC SQLGetTypeInfo().
        return "FLOAT";
    }


    public int getMaximumPrecision ()
    {
        return 15;
    }


    public int getMaximumScale ()
    {
        return 0;
    }


    public int getMinimumScale ()
    {
        return 0;
    }


    public int getNativeType ()
    {
        return 480;
    }


    public int getPrecision ()
    {
        return 15;
    }


    public int getRadix ()
    {
        return 10;
    }



    public int getScale ()
    {
        return 0;
    }


    public int getType ()
    {
        return java.sql.Types.DOUBLE;
    }



    public String getTypeName ()
    {
        return "DOUBLE";
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
        return SQLDataFactory.getPrecision (Double.toString (value_));
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

        // BigDecimal bigDecimal = new BigDecimal (Double.toString (value_))    // @A0D

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
                truncated_ = 0;
                return bigDecimal.setScale (scale);
            }
            else
            {
                truncated_ = bigDecimal.scale() - scale;
                return bigDecimal.setScale (scale, BigDecimal.ROUND_HALF_UP);
            }
        }
        else
            return bigDecimal;
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
        return(value_ != 0.0d);
    }



    public byte toByte ()
    throws SQLException
    {
        truncated_ = 0;
        return(byte) value_;
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
        return(double) value_;
    }



    public float toFloat ()
    throws SQLException
    {
        truncated_ = 0;
        return(float) value_;
    }



    public int toInt ()
    throws SQLException
    {
        truncated_ = 0;
        return(int) value_;
    }



    public long toLong ()
    throws SQLException
    {
        truncated_ = 0;
        return(long) value_;
    }



    public Object toObject ()
    {
        truncated_ = 0;
        return new Double (value_);
    }



    public short toShort ()
    throws SQLException
    {
        truncated_ = 0;
        return(short) value_;
    }



    public String toString ()
    {
        truncated_ = 0;
        String stringRep = Double.toString (value_);
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

