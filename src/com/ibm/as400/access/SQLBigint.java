///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLBigint.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
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

final class SQLBigint
extends SQLDataBase
{
    static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    private static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger LONG_MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);

    // Private data.
    private long value_     = 0;
    private int vrm_;            //@trunc3

    SQLBigint(int vrm, SQLConversionSettings settings)           //@trunc3
    {                            //@trunc3
      super(settings);
        vrm_ = vrm;              //@trunc3
    }                            //@trunc3
    
    public Object clone()
    {
        return new SQLBigint(vrm_, settings_);  //@trunc3
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter, boolean ignoreConversionErrors) //@P0C
    throws SQLException
    {
        value_ = BinaryConverter.byteArrayToLong(rawBytes, offset);
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        BinaryConverter.longToByteArray(value_, rawBytes, offset);
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
            // @BWS - changed how we parse this because people want to pass in 111222.333 for an int
            //        and using a double is causing rounding errors.  We use a BigDecimal for the
            //        case that we have to "try again" because if people were interested in performance
            //        they would not set a floating point value on an integer field and expect it to work
            //        so we would never hit the BigDecimal code path...

            try
            {
                value_ = Long.parseLong((String)object);
            }
            catch(NumberFormatException nfe)
            {
                try
                {
                    BigInteger bigInteger = new BigDecimal((String)object).toBigInteger();
                    /* For a numeric range, we see to the maximum value and set */
                    /* outOfBounds_.  OutOfBounds_ is checked later to determine */
                    /* if an error, warning, or neither should be reported  @R3A*/ 
                    if(bigInteger.compareTo(LONG_MAX_VALUE) > 0) {
                      truncated_ = bigInteger.toByteArray().length - 8;
                      outOfBounds_ = true; 
                      value_ = Long.MAX_VALUE; 
                    } else if (bigInteger.compareTo(LONG_MIN_VALUE) < 0) {
                    
                      truncated_ = bigInteger.toByteArray().length - 8;
                      outOfBounds_ = true; 
                      value_ = Long.MIN_VALUE; 
                    } else { 
                       value_ = bigInteger.longValue();
                    }
                }
                catch(NumberFormatException e)
                {
                    JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, e);
                }
            }
        }

        else if(object instanceof Number)
        {
            // Compute truncation by getting the value as a double
            // and comparing it against MAX_VALUE/MIN_VALUE.
            long   longValue = ((Number)object).longValue();
            double doubleValue = ((Number)object).doubleValue();

            if((doubleValue > Long.MAX_VALUE) || 
                // if long value is negative, but doubleValue is positive
                // When we have overflowed 
               ((doubleValue > 0) && (longValue <= 0))) {
              truncated_ = 1;                                                       // @D9a
              outOfBounds_ = true;
              value_ = Long.MAX_VALUE; 
            } else if ((doubleValue < Long.MIN_VALUE) || 
                ( (doubleValue < 0) && (longValue >= 0) )) {
                // Note:  Truncated here is set to 1 byte because the
                //        value has to be something positive in order
                //        for the code that checks it to do the right
                //        thing.
                truncated_ = 1;                                                       // @D9a
                outOfBounds_ = true;
                value_ = Long.MIN_VALUE; 

            } else { 
              
            value_ = ((Number)object).longValue();
            }

            // @D9d
            // Compute truncation. @Wz put the following three lines back in
            // double doubleValue = ((Number) object).doubleValue();
            // if(doubleValue != value_)
            //    truncated_ = Double.toString(doubleValue - value_).length() / 2;
        }

        else if(object instanceof Boolean)
            value_ = (((Boolean)object).booleanValue() == true) ? 1 : 0;

        else {
          if (JDTrace.isTraceOn()) {
              if (object == null) { 
                  JDTrace.logInformation(this, "Unable to assign null object");
                } else { 
                    JDTrace.logInformation(this, "Unable to assign object("+object+") of class("+object.getClass().toString()+")");
                }
          }
          JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);

        }
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.BIGINT;
    }

    //@E1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.lang.Long";
    }

    public String getCreateParameters()
    {
        return null;
    }

    public int getDisplaySize()
    {
        return 20;
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
        return "BIGINT";
    }

    public int getMaximumPrecision()
    {
        return 19;
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
        return 492;                 // @A1C
    }

    public int getPrecision()
    {
        return 19;
    }

    public int getRadix()
    {
        return 10;
    }

    public int getScale()
    {
        return 0;
    }

    public int getType()
    {
        return java.sql.Types.BIGINT;
    }

    public String getTypeName()
    {
        return "BIGINT";
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
        return 8;
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
        if(scale <= 0)
            return BigDecimal.valueOf(value_);
        else
            return BigDecimal.valueOf(value_).setScale(scale);
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
        return(value_ != 0);
    }

    public byte getByte()
    throws SQLException
    {
        if(value_ > Byte.MAX_VALUE || value_ < Byte.MIN_VALUE)
        {
            if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
            {
                if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
                {
                    truncated_ = 7;   outOfBounds_ = true; 


                }
                else
                {
                    truncated_ = 3;   outOfBounds_ = true; 

                }
            }
            else
            {
                truncated_ = 1;   outOfBounds_ = true; 

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
        if(value_ > Float.MAX_VALUE || value_ < -Float.MAX_VALUE)  //@trunc min_val is a positive number. //Float.MIN_VALUE)
        {
            truncated_ = 4;   outOfBounds_ = true; 
        }
        else
        {
            truncated_ = 0; outOfBounds_ = false; 
        }

        return(float) value_;
    }

    public int getInt()
    throws SQLException
    {
        if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
        {
            truncated_ = 4;   outOfBounds_ = true; 
        }
        else
        {
            truncated_ = 0; outOfBounds_ = false; 
        }

        return(int) value_;
    }

    public long getLong()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return value_;
    }

    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return new Long(value_);
    }

    public short getShort()
    throws SQLException
    {
        if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
        {
            if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
            {
                truncated_ = 6;   outOfBounds_ = true; 


            }
            else
            {
                truncated_ = 2;   outOfBounds_ = true;
            }
        }

        return(short) value_;
    }

    public String getString()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return Long.toString(value_);
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
        return Long.toString(value_);
    }

    //@pda jdbc40
    /* ifdef JDBC40 
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
endif */     

    //@pda jdbc40
    /* ifdef JDBC40 
    public SQLXML getSQLXML() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
endif */     
    // @array
    
    
    public void saveValue() {
      savedValue_ = new Long(value_); 
   }
}

