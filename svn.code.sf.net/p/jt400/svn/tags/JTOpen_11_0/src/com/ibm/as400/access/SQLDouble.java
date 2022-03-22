///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDouble.java
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
import java.sql.Blob;
import java.sql.Date;
/*ifdef JDBC40 
import java.sql.NClob;
import java.sql.RowId;
endif */ 
import java.sql.SQLException;
/*ifdef JDBC40 
import java.sql.SQLXML;
endif*/ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLDouble
extends  SQLDataBase
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Private data.
    private double                 value_;

    SQLDouble(SQLConversionSettings settings)
    {
      super(settings);
        truncated_ = 0; outOfBounds_ = false; 
        value_      = 0.0d;
    }

    public Object clone()
    {
        return new SQLDouble(settings_);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter, boolean ignoreConversionErrors) //@P0C
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
                if (settings_.getDecimalSeparator().equals(",")){
                  object = ((String) object).replace(',','.'); 
                }

                value_ = Double.valueOf((String) object).doubleValue();
                if (value_ == Double.POSITIVE_INFINITY) {
                  // Check to see if overflow
                  if (!((String)object).toLowerCase().trim().equals("infinity")) {
                     truncated_ = 1; outOfBounds_ = true; 
                     value_ = Double.MAX_VALUE; 
                  }
                } else if (value_ == Double.NEGATIVE_INFINITY) {
                  // Check to see if underflow
                  if (((String)object).toLowerCase().trim().indexOf("infinity")< 0) {
                    truncated_ = 1; outOfBounds_ = true; 
                    value_ = -Double.MAX_VALUE; 
                 }
                }
                
                // You can't test for data truncation of a number by testing
                // the lengths of two string versions of it.
                // Example string that should work but will fail:
                //      "4.749000000000E+00"
                //@E2D int objectLength = ((String) object).length();
                //@E2D int valueLength = Double.toString(value_).length();
                //@E2D if(valueLength < objectLength)
                //@E2D     truncated_ = objectLength - valueLength;
            }
            catch(NumberFormatException e)
            {
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        // @ D9d
        // else if(object instanceof BigDecimal) {
        //     value_ = ((BigDecimal) object).doubleValue();
        //     int objectLength = SQLDataFactory.getPrecision((BigDecimal) object);
        //     int valueLength = SQLDataFactory.getPrecision(new BigDecimal(value_));
        //     if(valueLength < objectLength)
        //         truncated_ = objectLength - valueLength;
        // }

        else if(object instanceof Number)
        {
          Number number = (Number) object; 
          value_ = number.doubleValue();
          if (value_ == Double.POSITIVE_INFINITY) {
             String stringValue = number.toString().toLowerCase();
             if (stringValue.indexOf("inf") == 0) {
               // It really is infinity.  Accept it. 
             } else { 
               truncated_ = 1; outOfBounds_ = true; 
               value_ = Double.MAX_VALUE;
             }
          } else if (value_ == Double.NEGATIVE_INFINITY) {
            String stringValue = number.toString().toLowerCase();
            if (stringValue.indexOf("inf") > 0) {
              // It really is infinity.  Accept it. 
            } else {
              truncated_ = 1; outOfBounds_ = true; 
              value_ = - Double.MAX_VALUE;
            }
          }          
        }

        else if(object instanceof Boolean)
            value_ = (((Boolean) object).booleanValue() == true) ? 1d : 0d;

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
        return SQLData.DOUBLE;
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
        return "java.lang.Double";
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
        // Use "FLOAT" not "DOUBLE".  See ODBC SQLGetTypeInfo().
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
        return 2;           //@K1C Changed from 10
    }

    public int getScale()
    {
        return 0;
    }

    public int getType()
    {
        return java.sql.Types.DOUBLE;
    }

    public String getTypeName()
    {
        return "DOUBLE";
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
                outOfBounds_=false; 
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
                if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
                {
                    truncated_ = 7; outOfBounds_=true;
                }
                else
                {
                    truncated_ = 3; outOfBounds_=true;
                }
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
        if(Double.isInfinite(value_)) {   //@tr3a
            truncated_ = 0; outOfBounds_ = false;               //@tr3a
        } else if(value_ > Float.MAX_VALUE || value_ < -Float.MAX_VALUE)  //@trunc min_val is a posative number. //Float.MIN_VALUE)  //@tr3c
        {         
            truncated_ = 4; outOfBounds_=true;
        }
        return(float) value_;
    }

    public int getInt()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
        {
            truncated_ = 4; outOfBounds_=true;
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
                truncated_ = 6;
                outOfBounds_=true;
            }
            else
            {
                truncated_ = 2; outOfBounds_=true;
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
    
    
    public void saveValue() {
      savedValue_ = new Double(value_); 
   }
}

