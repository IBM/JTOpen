///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDecimal2.java
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



class SQLDecimal2
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private SQLConversionSettings   settings_;
    private int                     precision_;
    private int                     scale_;
    private AS400PackedDecimal      typeConverter_;
	private double                  value_;



    SQLDecimal2 (int precision,
                 int scale,
                 SQLConversionSettings settings)
    {
        settings_       = settings;
        precision_      = precision;
        scale_          = scale;
        typeConverter_  = new AS400PackedDecimal (precision_, scale_);
        value_          = 0;
    }



    public Object clone ()
    {
        return new SQLDecimal2 (precision_, scale_, settings_);
    }



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConvTable ccisdConverter) //@P0C
        throws SQLException
    {
        value_ = typeConverter_.toDouble(rawBytes, offset);
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
        if (object instanceof String)
            value_ = Double.valueOf((String)object).doubleValue();

        else if (object instanceof Number) 
            value_ = ((Number)object).doubleValue();

        else if (object instanceof Boolean)
            value_ = ((Boolean)object).booleanValue() ? 1 : 0;

        else
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
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


    // @A1D public boolean isGraphic ()
    // @A1D {
    // @A1D    return false;
    // @A1D }



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
        return 0;
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
        return new BigDecimal(value_);
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
	    return value_;
	}



	public float toFloat ()
	    throws SQLException
	{
	    return (float)value_;
	}



	public int toInt ()
	    throws SQLException
	{
	    return (int)value_;
	}



	public long toLong ()
	    throws SQLException
	{
	    return (long)value_;
	}



	public Object toObject ()
	{
	    return new Double(value_);
	}



	public short toShort ()
	    throws SQLException
	{
	    return (short) value_;
	}



	public String toString ()
	{
        return Double.toString(value_);
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
