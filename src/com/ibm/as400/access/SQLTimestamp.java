///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SQLTimestamp.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
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



class SQLTimestamp
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private SQLConversionSettings   settings_;
    private int                     truncated_;
    private int                     year_;
    private int                     month_;
    private int                     day_;
    private int                     hour_;
    private int                     minute_;
    private int                     second_;
    private int                     nanos_;



    SQLTimestamp (SQLConversionSettings settings)
    {
        settings_   = settings;
        truncated_  = 0;
        year_       = 0;
        month_      = 0;
        day_        = 0;
        hour_       = 0;
        minute_     = 0;
        second_     = 0;
        nanos_      = 0;
    }



    public Object clone ()
    {
        return new SQLTimestamp (settings_);
    }



    public static Timestamp stringToTimestamp (String s,
                                               Calendar calendar)
        throws SQLException
    {
        try {
            // @E3D // If the string has a year 1, then it is likely a NULL, so
            // @E3D // just set this to a default date.
            int year = Integer.parseInt (s.substring (0, 4));
            // @E3D if (year == 1) {
            // @E3D     return new Timestamp (0);
            // @E3D }

            // Parse the string .
            // @E3D else {
                calendar.set (Calendar.YEAR, year);
                calendar.set (Calendar.MONTH, Integer.parseInt (s.substring (5, 7)) - 1);
                calendar.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (8, 10)));
                calendar.set (Calendar.HOUR_OF_DAY, Integer.parseInt (s.substring (11, 13)));
                calendar.set (Calendar.MINUTE, Integer.parseInt (s.substring (14, 16)));
                calendar.set (Calendar.SECOND, Integer.parseInt (s.substring (17, 19)));

                String nanos = s.substring (20).trim () + "000000000";
                Timestamp ts = new Timestamp (calendar.getTime ().getTime ());
                ts.setNanos (Integer.parseInt (nanos.substring (0, 6)) * 1000);
                return ts;
            // @E3D }
        }
        catch (NumberFormatException e) {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        }
        catch (StringIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        }

        return new Timestamp (0);
    }



	public static String timestampToString (Timestamp ts,
	                                        Calendar calendar)
	{
	    StringBuffer buffer = new StringBuffer ();
	    calendar.setTime (ts);

	    buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 4));
	    buffer.append ('-');
	    buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MONTH) + 1, 2));
	    buffer.append ('-');
	    buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_MONTH), 2));
	    buffer.append (' ');
	    buffer.append (JDUtilities.padZeros (calendar.get (Calendar.HOUR_OF_DAY), 2));
	    buffer.append (':');
	    buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MINUTE), 2));
	    buffer.append (':');
	    buffer.append (JDUtilities.padZeros (calendar.get (Calendar.SECOND), 2));
	    buffer.append ('.');
	    buffer.append (JDUtilities.padZeros (ts.getNanos (), 9)); // @B1C

        // Ensure that exactly 26 characters are returned.
        String tempString = buffer.toString() + "000000";
	    return tempString.substring (0, 26);
	}



//---------------------------------------------------------//
//                                                         //
// CONVERSION TO AND FROM RAW BYTES                        //
//                                                         //
//---------------------------------------------------------//



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        year_ = (rawBytes[offset] & 0x0f) * 1000
            + (rawBytes[offset+1] & 0x0f) * 100
            + (rawBytes[offset+2] & 0x0f) * 10
            + (rawBytes[offset+3] & 0x0f);

        // @E3D // If the string has a year 1, then it is likely a NULL, so
        // @E3D // just set this to a default date.
        // @E3D if (year_ != 1) {
            month_ = (rawBytes[offset+5] & 0x0f) * 10
                + (rawBytes[offset+6] & 0x0f) - 1;
            day_ = (rawBytes[offset+8] & 0x0f) * 10
                + (rawBytes[offset+9] & 0x0f);
            hour_ = (rawBytes[offset+11] & 0x0f) * 10
                + (rawBytes[offset+12] & 0x0f);
            minute_ = (rawBytes[offset+14] & 0x0f) * 10
                + (rawBytes[offset+15] & 0x0f);
            second_ = (rawBytes[offset+17] & 0x0f) * 10
                + (rawBytes[offset+18] & 0x0f);
            nanos_ =
                ((rawBytes[offset+20] & 0x0f) * 100000
                + (rawBytes[offset+21] & 0x0f) * 10000
                + (rawBytes[offset+22] & 0x0f) * 1000
                + (rawBytes[offset+23] & 0x0f) * 100
                + (rawBytes[offset+24] & 0x0f) * 10
                + (rawBytes[offset+25] & 0x0f) ) * 1000;
        // @E3D }
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        StringBuffer buffer = new StringBuffer (toString().replace (':', '.'));
        buffer.setCharAt (10, '-');

        try {
            ccsidConverter.stringToByteArray (buffer.toString(), rawBytes, offset);
        }
        catch (CharConversionException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL, e);    // @E2C
        }
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
            Timestamp ts = stringToTimestamp ((String) object, calendar);
            year_   = calendar.get (Calendar.YEAR);
            month_  = calendar.get (Calendar.MONTH);
            day_    = calendar.get (Calendar.DAY_OF_MONTH);
            hour_   = calendar.get (Calendar.HOUR_OF_DAY);
            minute_ = calendar.get (Calendar.MINUTE);
            second_ = calendar.get (Calendar.SECOND);
            nanos_  = ts.getNanos ();
        }

        else if (object instanceof Date) {
            calendar.setTime ((Date) object);
            year_   = calendar.get (Calendar.YEAR);
            month_  = calendar.get (Calendar.MONTH);
            day_    = calendar.get (Calendar.DAY_OF_MONTH);
            hour_   = calendar.get (Calendar.HOUR_OF_DAY);
            minute_ = calendar.get (Calendar.MINUTE);
            second_ = calendar.get (Calendar.SECOND);
            nanos_  = calendar.get (Calendar.MILLISECOND) * 1000000;
        }

        else if (object instanceof Timestamp) {
            calendar.setTime ((Timestamp) object);
            year_   = calendar.get (Calendar.YEAR);
            month_  = calendar.get (Calendar.MONTH);
            day_    = calendar.get (Calendar.DAY_OF_MONTH);
            hour_   = calendar.get (Calendar.HOUR_OF_DAY);
            minute_ = calendar.get (Calendar.MINUTE);
            second_ = calendar.get (Calendar.SECOND);
            nanos_  = ((Timestamp) object).getNanos ();
        }

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
        return null;
    }


    public int getDisplaySize ()
    {
        return 26;
    }


    public String getLiteralPrefix ()
    {
        return "\'";
    }


    public String getLiteralSuffix ()
    {
        return "\'";
    }



    public String getLocalName ()
    {
        return "TIMESTAMP";
    }


    public int getMaximumPrecision ()
    {
        return 26;
    }


    public int getMaximumScale ()
    {
        return 6;
    }


    public int getMinimumScale ()
    {
        return 6;
    }


    public int getNativeType ()
    {
        return 392;
    }


    public int getPrecision ()
    {
        return 26;
    }



    public int getRadix ()
    {
        return 10;
    }



    public int getScale ()
    {
        return 6;
    }



	public int getType ()
	{
		return java.sql.Types.TIMESTAMP;
	}



	public String getTypeName ()
	{
		return "TIMESTAMP";
	}


// @E1D    public boolean isGraphic ()
// @E1D    {
// @E1D        return false;
// @E1D    }



    public boolean isSigned ()
    {
        return false;
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
        return 26;
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
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public InputStream toBinaryStream ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public boolean toBoolean ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return false;
	}



	public Blob toBlob ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



	public byte toByte ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
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
	    truncated_ = 16;
	    calendar.set (year_, month_, day_, 0, 0, 0);
	    return new Date (calendar.getTime ().getTime ());
	}



	public double toDouble ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public float toFloat ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public int toInt ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public long toLong ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public Object toObject ()
	{
	    truncated_ = 0;
	    Calendar calendar = Calendar.getInstance ();
	    calendar.set (year_, month_, day_, hour_, minute_, second_);
	    Timestamp ts = new Timestamp  (calendar.getTime ().getTime ());
	    ts.setNanos (nanos_);
	    return ts;
	}



	public short toShort ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public String toString ()
	{
	    truncated_ = 0;
	    Calendar calendar = Calendar.getInstance ();
	    calendar.set (year_, month_, day_, hour_, minute_, second_);
	    Timestamp ts = new Timestamp  (calendar.getTime ().getTime ());
	    ts.setNanos (nanos_);
        return timestampToString (ts, calendar);
	}



	public Time toTime (Calendar calendar)
	    throws SQLException
	{
	    truncated_ = 18;
	    calendar.set (0, 0, 0, hour_, minute_, second_);
	    return new Time (calendar.getTime ().getTime ());
	}



	public Timestamp toTimestamp (Calendar calendar)
	    throws SQLException
	{
	    truncated_ = 0;
	    calendar.set (year_, month_, day_, hour_, minute_, second_);
	    Timestamp ts = new Timestamp  (calendar.getTime ().getTime ());
	    ts.setNanos (nanos_);
	    return ts;
	}



	public InputStream	toUnicodeStream ()
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
	}



}

