///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SQLNumeric.java
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



class SQLNumeric
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private static final BigDecimal default_ = BigDecimal.valueOf (0); // @C2A

    private SQLConversionSettings   settings_;
    private int                     precision_;
    private int                     scale_;
    private int                     truncated_;
    private AS400ZonedDecimal       typeConverter_;
	private BigDecimal              value_;



    SQLNumeric (int precision,
                int scale,
                SQLConversionSettings settings)
    {
        settings_       = settings;
        precision_      = precision;
        scale_          = scale;
        truncated_      = 0;
        typeConverter_  = new AS400ZonedDecimal (precision_, scale_);
        value_          = default_; // @C2C
    }



    public Object clone ()
    {
        return new SQLNumeric (precision_, scale_, settings_);
    }



    static private String getCopyright ()
    {
        return Copyright.copyright;
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        value_ = ((BigDecimal) typeConverter_.toObject (rawBytes, offset));
	}



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
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

        if (object instanceof String) {
            try {
                String value = (String) object;
                if (scale >= 0)
                    value = SQLDataFactory.truncateScale (value, scale);
                bigDecimal = new BigDecimal (value);
            }
            catch (NumberFormatException e) {
                JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
            }
        }

        else if (object instanceof Number) {
            String value = SQLDataFactory.convertScientificNotation (object.toString ()); // @C1C
            if (scale >= 0)
                value = SQLDataFactory.truncateScale (value, scale);
   	        bigDecimal = new BigDecimal (value);
   	    }

        else if (object instanceof Boolean)
            bigDecimal = (((Boolean) object).booleanValue() == true) ? BigDecimal.valueOf (1) : BigDecimal.valueOf (0);

        else
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);

        // Truncate if necessary.
        truncated_ = 0;
        int otherScale = bigDecimal.scale ();
        if (otherScale > scale_)
            truncated_ += otherScale - scale_;
        value_ = bigDecimal.setScale (scale_, BigDecimal.ROUND_HALF_UP);

        int otherPrecision = SQLDataFactory.getPrecision (value_);
        if (otherPrecision > precision_) {
            int digits = otherPrecision - precision_;
            truncated_ += digits;
            value_ = SQLDataFactory.truncatePrecision (value_, digits);
        }
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
        return "NUMERIC";
    }


    public int getMaximumPrecision ()
    {
        return 31;
    }


    public int getMaximumScale ()
    {
        return 31;
    }


    public int getMinimumScale ()
    {
        return 0;
    }


    public int getNativeType ()
    {
        return 488;
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
		return java.sql.Types.NUMERIC;
	}



	public String getTypeName ()
	{
		return "NUMERIC";
	}


    public boolean isGraphic ()
    {
        return false;
    }



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
	    if (scale >= 0) {
            if (scale >= value_.scale()) {
                truncated_ = 0;
                return value_.setScale (scale);
            }
            else {
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
	    return (value_.compareTo (BigDecimal.valueOf (0)) != 0);
	}



	public byte toByte ()
	    throws SQLException
	{
	    truncated_ = value_.scale();
	    return (byte) value_.byteValue();
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
	    truncated_ = 0;
	    return value_;
	}



	public short toShort ()
	    throws SQLException
	{
	    truncated_ = value_.scale();
	    return (short) value_.shortValue ();
	}



	public String toString ()
	{
	    truncated_ = 0;
	    String stringRep = value_.toString ();
	    int decimal = stringRep.indexOf ('.');
	    if (decimal == -1)
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



	public InputStream	toUnicodeStream ()
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



}

