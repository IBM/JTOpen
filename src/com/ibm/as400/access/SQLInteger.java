///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLInteger.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
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



class SQLInteger
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private int                 truncated_;
    // @D0D private static AS400Bin4    typeConverter_;
    private int                 value_;
    private int                 scale_;                              // @A0A
    private BigDecimal          bigDecimalValue_ = null;             // @A0A



    // @D0D static
    // @D0D {
    // @D0D     typeConverter_ = new AS400Bin4 ();
    // @D0D }



    SQLInteger ()
    {
        this (0);
    }



    SQLInteger (int scale)                     // @A0A
    {
        truncated_          = 0;
        value_              = 0;
        scale_              = scale;                                      // @A0A
        if (scale_ > 0)                                                   // @C0A
            bigDecimalValue_    = new BigDecimal (Integer.toString (value_)); // @A0A
    }



    public Object clone ()
    {
        return new SQLInteger (scale_);
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        value_ = BinaryConverter.byteArrayToInt(rawBytes, offset);                               // @D0C

        if (scale_ > 0) {                                                                        // @C0A
            bigDecimalValue_ = (new BigDecimal(Integer.toString(value_))).movePointLeft(scale_); // @A0A
            value_ = bigDecimalValue_.intValue();                                                // @A0A
        }                                                                                        // @C0A
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        BinaryConverter.intToByteArray(value_, rawBytes, offset);                                // @D0C
    }



//---------------------------------------------------------//
//                                                         //
// SET METHODS                                             //
//                                                         //
//---------------------------------------------------------//



    public void set (Object object, Calendar calendar, int scale)
        throws SQLException
    {
        if (object instanceof String) {
            try {
                value_ = Integer.parseInt ((String) object);
            }
            catch (NumberFormatException e) {
                JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        else if (object instanceof Number) {
            value_ = ((Number) object).intValue();

            // @E3D // Compute truncation.
            // @E3D double doubleValue = ((Number) object).doubleValue ();
            // @E3D if (doubleValue != value_)
            // @E3D     truncated_ = Double.toString (doubleValue - value_).length () / 2;
        }

        else if (object instanceof Boolean)
            value_ = (((Boolean) object).booleanValue() == true) ? 1 : 0;

        else
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);

        if (scale_ > 0) {                                                                        // @C0A
            bigDecimalValue_ = (new BigDecimal(Integer.toString(value_))).movePointLeft(scale_); // @A0A
            value_ = bigDecimalValue_.intValue();                                                // @A0A
        }                                                                                        // @C0A
    }


    public void set(int value)                                                          // @E2A
    {                                                                                   // @E2A
        value_ = value;                                                                 // @E2A
    }                                                                                   // @E2A


//---------------------------------------------------------//
//                                                         //
// DESCRIPTION OF SQL TYPE                                 //
//                                                         //
//---------------------------------------------------------//



    public String getCreateParameters ()
    {
        return null;
    }


    public int getDisplaySize ()
    {
        return 11;
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
        return "INTEGER";
    }


    public int getMaximumPrecision ()
    {
        return 10;
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
        return 496;
    }


    public int getPrecision ()
    {
        return 10;
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
		return java.sql.Types.INTEGER;
	}


	public String getTypeName ()
	{
		return "INTEGER";
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
        return 4; // @D0C
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
        if (scale_ > 0) {                                                   // @C0A
	        if (scale >= 0)
                return bigDecimalValue_.setScale(scale);                    // @A0A
            else
                return bigDecimalValue_;
        }                                                                   // @C0A
        else {                                                              // @C0A
            if (scale <= 0)                                                 // @C0A
                return BigDecimal.valueOf ((long) value_);                  // @C0A
            else                                                            // @C0A
                return BigDecimal.valueOf ((long) value_).setScale (scale); // @C0A
        }                                                                   // @C0A
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
	    return (value_ != 0);
	}



	public byte toByte ()
	    throws SQLException
	{
	    return (byte) value_;
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
        if (scale_ > 0)                                 // @C0A
            return bigDecimalValue_.doubleValue();      // @A0A
        else                                            // @C0A
            return (double) value_;                     // @A0D @C0A
	}



	public float toFloat ()
	    throws SQLException
	{
        if (scale_ > 0)                                 // @C0A
            return bigDecimalValue_.floatValue();       // @A0A
        else                                            // @C0A
            return (float) value_;                      // @A0D @C0A
	}



	public int toInt ()
	    throws SQLException
	{
	    return (int) value_;
	}



	public long toLong ()
	    throws SQLException
	{
	    return value_;
	}



	public Object toObject ()
	{
	    return new Integer ((int) value_);
	}



	public short toShort ()
	    throws SQLException
	{
	    return (short) value_;
	}



	public String toString ()
	{
        if (scale_ > 0)                                 // @C0A
            return bigDecimalValue_.toString();         // @A0A
        else                                            // @C0A
            return Integer.toString (value_);           // @A0D @C0A
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



	public InputStream	toUnicodeStream ()
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



}

