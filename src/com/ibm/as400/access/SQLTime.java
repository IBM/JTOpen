///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SQLTime.java
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



class SQLTime
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Private data.
    private SQLConversionSettings   settings_;
    private int                     hour_;
    private int                     minute_;
    private int                     second_;



    SQLTime (SQLConversionSettings settings)
    {
        settings_   = settings;
        hour_       = 0;
        minute_     = 0;
        second_     = 0;
    }



    public Object clone ()
    {
        return new SQLTime (settings_);
    }



    public static Time stringToTime (String s,
                                     SQLConversionSettings settings,
                                     Calendar calendar)
        throws SQLException
    {
        try {

            // If the string is empty, then it is likely a NULL, so
            // just set this to a default date.
            if (s.trim().length() == 0)
                return new Time (0);

            // Parse the string according to the format and separator.
            switch (settings.getTimeFormat ()) {                                          // @A0A
            case SQLConversionSettings.TIME_FORMAT_USA:                                   // @A0A
                int hour = Integer.parseInt (s.substring (0, 2));
                char amPm = s.charAt (6);
                if (hour == 12) {
                    if (amPm == 'A')
                        hour = 0;
                }
                else { // Hour between 1 and 11.
                    if (amPm == 'P')
                        hour += 12;
                }
                calendar.set (Calendar.HOUR_OF_DAY, hour);
                calendar.set (Calendar.MINUTE, Integer.parseInt (s.substring (3, 5)));
                calendar.set (Calendar.SECOND, 0);
                break;                                                      // @A0A

            case SQLConversionSettings.TIME_FORMAT_EUR:
            case SQLConversionSettings.TIME_FORMAT_JIS:
            case SQLConversionSettings.TIME_FORMAT_HMS:
            case SQLConversionSettings.TIME_FORMAT_ISO:
                calendar.set (Calendar.HOUR_OF_DAY, Integer.parseInt (s.substring (0, 2)));
                calendar.set (Calendar.MINUTE, Integer.parseInt (s.substring (3, 5)));
                calendar.set (Calendar.SECOND, Integer.parseInt (s.substring (6, 8)));
                break;
            }

            calendar.set (Calendar.YEAR, 0);
            calendar.set (Calendar.MONTH, 0);
            calendar.set (Calendar.DAY_OF_MONTH, 0);
            calendar.set (Calendar.MILLISECOND, 0);
        }
        catch (NumberFormatException e) {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        }
        catch (StringIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        }

        return new Time (calendar.getTime ().getTime ());
    }



	public static String timeToString (Time t,
	                                   SQLConversionSettings dataFormat,
	                                   Calendar calendar)
	{
	    StringBuffer buffer = new StringBuffer ();
        String separator = dataFormat.getTimeSeparator ();
        calendar.setTime (t);

        // Note: No matter what format is being used,
        // ensure that exactly 8 characters are in the
        // buffer.

        switch (dataFormat.getTimeFormat ()) {                                           // @A0A
        case SQLConversionSettings.TIME_FORMAT_USA:                                      // @A0A
            int hour = calendar.get (Calendar.HOUR_OF_DAY);
            char amPm;
            if (hour > 12) {
                hour -= 12;
                amPm = 'P';
            }
            else
                amPm = 'A';
            buffer.append (JDUtilities.padZeros (hour, 2));
            buffer.append (':');
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MINUTE), 2));
            buffer.append (' ');
            buffer.append (amPm);
            buffer.append ('M');
            break;                                                       // @A0A

        case SQLConversionSettings.TIME_FORMAT_EUR:                                                   // @A0A
        case SQLConversionSettings.TIME_FORMAT_ISO:                                                   // @A0A
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.HOUR_OF_DAY), 2));
            buffer.append ('.');
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MINUTE), 2));
            buffer.append ('.');
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.SECOND), 2));
            break;                                                       // @A0A

        case SQLConversionSettings.TIME_FORMAT_JIS:                                                   // @A0A
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.HOUR_OF_DAY), 2));
            buffer.append (':');
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MINUTE), 2));
            buffer.append (':');
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.SECOND), 2));
            break;                                                       // @A0A

        case SQLConversionSettings.TIME_FORMAT_HMS:                                                   // @A0A
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.HOUR_OF_DAY), 2));
            buffer.append (separator);
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MINUTE), 2));
            buffer.append (separator);
            buffer.append (JDUtilities.padZeros (calendar.get (Calendar.SECOND), 2));
            break;                                                       // @A0A
        }

	    return buffer.toString ();
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
        switch (settings_.getTimeFormat ()) {

            case SQLConversionSettings.TIME_FORMAT_USA:                      // hh:mm AM or PM
                hour_   = (rawBytes[offset] & 0x0f) * 10 + (rawBytes[offset+1] & 0x0f);
                minute_ = (rawBytes[offset+3] & 0x0f) * 10 + (rawBytes[offset+4] & 0x0f);
                second_ = 0;
                if ( ((rawBytes[offset+6] == (byte)0xd7) && (hour_ < 12)) || // xd7='P'
                     ((rawBytes[offset+6] == (byte)0xC1) && (hour_ == 12)) ) // xC1='A'
                  hour_ += 12;
                break;

            case SQLConversionSettings.TIME_FORMAT_HMS:                      // hh:mm:ss
            case SQLConversionSettings.TIME_FORMAT_ISO:                      // hh.mm.ss
            case SQLConversionSettings.TIME_FORMAT_EUR:                      // hh.mm.ss
            case SQLConversionSettings.TIME_FORMAT_JIS:                      // hh:mm:ss
                hour_   = (rawBytes[offset] & 0x0f) * 10 + (rawBytes[offset+1] & 0x0f);
                minute_ = (rawBytes[offset+3] & 0x0f) * 10 + (rawBytes[offset+4] & 0x0f);
                second_ = (rawBytes[offset+6] & 0x0f) * 10 + (rawBytes[offset+7] & 0x0f);
                break;

            default:
                break;
        }
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        StringBuffer buffer = new StringBuffer (8);

        // Always use ISO format here.
        buffer.append (JDUtilities.padZeros (hour_, 2));
        buffer.append ('.');
        buffer.append (JDUtilities.padZeros (minute_, 2));
        buffer.append ('.');
        buffer.append (JDUtilities.padZeros (second_, 2));

        try {
            ccsidConverter.stringToByteArray (buffer.toString(), rawBytes, offset);
        }
        catch (CharConversionException e) {
            JDError.throwSQLException (JDError.EXC_INTERNAL);
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
            stringToTime ((String) object, settings_, calendar);
            hour_   = calendar.get (Calendar.HOUR_OF_DAY);
            minute_ = calendar.get (Calendar.MINUTE);
            second_ = calendar.get (Calendar.SECOND);
        }

        else if (object instanceof Time) {
            calendar.setTime ((Time) object);
            hour_   = calendar.get (Calendar.HOUR_OF_DAY);
            minute_ = calendar.get (Calendar.MINUTE);
            second_ = calendar.get (Calendar.SECOND);
        }

        else if (object instanceof Timestamp) {
            calendar.setTime ((Timestamp) object);
            hour_   = calendar.get (Calendar.HOUR_OF_DAY);
            minute_ = calendar.get (Calendar.MINUTE);
            second_ = calendar.get (Calendar.SECOND);
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
        return 8;
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
        return "TIME";
    }


    public int getMaximumPrecision ()
    {
        return 8;
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
        return 388;
    }


    public int getPrecision ()
    {
        return 8;
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
		return java.sql.Types.TIME;
	}



	public String getTypeName ()
	{
		return "TIME";
	}


    public boolean isGraphic ()
    {
        return false;
    }



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
        return 8;
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
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
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
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return false;
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
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return null;
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
	    Calendar calendar = Calendar.getInstance ();
	    calendar.set (0, 0, 0, hour_, minute_, second_);
	    return new Time (calendar.getTime ().getTime ());
	}



	public short toShort ()
	    throws SQLException
	{
		JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
  		return -1;
	}



	public String toString ()
	{
	    Calendar calendar = Calendar.getInstance ();
	    calendar.set (0, 0, 0, hour_, minute_, second_);
	    Time t = new Time (calendar.getTime ().getTime ());
	    return timeToString (t, settings_, calendar);
	}



	public Time toTime (Calendar calendar)
	    throws SQLException
	{
	    calendar.set (0, 0, 0, hour_, minute_, second_);
	    return new Time (calendar.getTime ().getTime ());
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

