///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SQLDate.java
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



class SQLDate
implements SQLData
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    // Private data.
    private SQLConversionSettings   settings_;
    private int                     year_;
    private int                     month_;
    private int                     day_;



    SQLDate (SQLConversionSettings settings)
    {
        settings_   = settings;
        year_       = 0;
        month_      = 0;
        day_        = 0;
    }



    public Object clone ()
    {
        return new SQLDate (settings_);
    }



    public static Date stringToDate (String s,
                                     SQLConversionSettings settings,
                                     Calendar calendar)
        throws SQLException
    {
        // If the string is empty or set to zeros,
        // then it is likely a NULL, so just set this
        // to a default date.
        String sTrim = s.trim();
        try {
            if ((sTrim.length() == 0) || (Integer.parseInt (sTrim) == 0))
                return new Date (0);
        }
        catch (NumberFormatException e) {
            // Ignore.  This just means the value is not NULL.
        }

        try {
            // Parse the string according to the format and separator.
	        switch (settings.getDateFormat ()) {
                case SQLConversionSettings.DATE_FORMAT_USA:
                    calendar.set (Calendar.YEAR, Integer.parseInt (s.substring (6, 10)));
                    calendar.set (Calendar.MONTH, Integer.parseInt (s.substring (0, 2)) - 1);
                    calendar.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (3, 5)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_EUR:
                    calendar.set (Calendar.YEAR, Integer.parseInt (s.substring (6, 10)));
                    calendar.set (Calendar.MONTH, Integer.parseInt (s.substring (3, 5)) - 1);
                    calendar.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (0, 2)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_JULIAN:
                    calendar.set (Calendar.DAY_OF_YEAR, Integer.parseInt (s.substring (3, 6)));
                    calendar.set (Calendar.YEAR, twoDigitYearToFour (Integer.parseInt (s.substring (0, 2))));
                    break;

                case SQLConversionSettings.DATE_FORMAT_MDY:
                    calendar.set (Calendar.YEAR, twoDigitYearToFour (Integer.parseInt (s.substring (6, 8))));
                    calendar.set (Calendar.MONTH, Integer.parseInt (s.substring (0, 2)) - 1);
                    calendar.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (3, 5)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_DMY:
                    calendar.set (Calendar.YEAR, twoDigitYearToFour (Integer.parseInt (s.substring (6, 8))));
                    calendar.set (Calendar.MONTH, Integer.parseInt (s.substring (3, 5)) - 1);
                    calendar.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (0, 2)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_YMD:
                    calendar.set (Calendar.YEAR, twoDigitYearToFour (Integer.parseInt (s.substring (0, 2))));
                    calendar.set (Calendar.MONTH, Integer.parseInt (s.substring (3, 5)) - 1);
                    calendar.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (6, 8)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_JIS:
                case SQLConversionSettings.DATE_FORMAT_ISO:
                    calendar.set (Calendar.YEAR, Integer.parseInt (s.substring (0, 4)));
                    calendar.set (Calendar.MONTH, Integer.parseInt (s.substring (5, 7)) - 1);
                    calendar.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (8, 10)));
                    break;
            }

            calendar.set (Calendar.HOUR_OF_DAY, 0);
            calendar.set (Calendar.MINUTE, 0);
            calendar.set (Calendar.SECOND, 0);
            calendar.set (Calendar.MILLISECOND, 0);
        }
        catch (NumberFormatException e) {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        }
        catch (StringIndexOutOfBoundsException e) {
            JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
        }

        return new Date (calendar.getTime ().getTime ());
    }



	public static String dateToString (Date d,
	                                   SQLConversionSettings dataFormat,
	                                   Calendar calendar)
	{
	    StringBuffer buffer = new StringBuffer ();
	    String separator = dataFormat.getDateSeparator ();
	    calendar.setTime (d);

        // Note: No matter what format is being used,
        // ensure that exactly 10 characters are in the
        // buffer.

        switch (dataFormat.getDateFormat ()) {

            case SQLConversionSettings.DATE_FORMAT_USA:                          // mm/dd/yyyy
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MONTH) + 1, 2));
                buffer.append ('/');
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_MONTH), 2));
                buffer.append ('/');
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 4));
                break;

            case SQLConversionSettings.DATE_FORMAT_EUR:                          // dd.mm.yyyy
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_MONTH), 2));
                buffer.append ('.');
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MONTH) + 1, 2));
                buffer.append ('.');
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 4));
                break;

            case SQLConversionSettings.DATE_FORMAT_JULIAN:                      // yy/ddd
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 2));
                buffer.append (separator);
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_YEAR), 3));
                buffer.append ("    ");
                break;

            case SQLConversionSettings.DATE_FORMAT_MDY:                         // mm/dd/yy
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MONTH) + 1, 2));
                buffer.append (separator);
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_MONTH), 2));
                buffer.append (separator);
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 2));
                buffer.append ("  ");
                break;

            case SQLConversionSettings.DATE_FORMAT_DMY:                         // dd/mm/yy
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_MONTH), 2));
                buffer.append (separator);
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MONTH) + 1, 2));
                buffer.append (separator);
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 2));
                buffer.append ("  ");
                break;

            case SQLConversionSettings.DATE_FORMAT_YMD:                         // yy/mm/dd
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 2));
                buffer.append (separator);
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MONTH) + 1, 2));
                buffer.append (separator);
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_MONTH), 2));
                buffer.append ("  ");
                break;

            case SQLConversionSettings.DATE_FORMAT_JIS:                         // yyyy-mm-dd
            case SQLConversionSettings.DATE_FORMAT_ISO:                         // yyyy-mm-dd
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.YEAR), 4));
                buffer.append ('-');
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.MONTH) + 1, 2));
                buffer.append ('-');
                buffer.append (JDUtilities.padZeros (calendar.get (Calendar.DAY_OF_MONTH), 2));
                break;
        }

        return buffer.toString ();
	}



    private static int twoDigitYearToFour (int twoDigitYear)
    {
        return (twoDigitYear <= 39)
            ? (twoDigitYear + 2000)
            : (twoDigitYear + 1900);
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



    public void convertFromRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccisdConverter)
        throws SQLException
    {
        switch (settings_.getDateFormat ()) {

            case SQLConversionSettings.DATE_FORMAT_JULIAN:                      // yy/ddd
                year_ = twoDigitYearToFour ((rawBytes[offset+0] & 0x0f) * 10
                    + (rawBytes[offset+1] & 0x0f));
                Calendar calendar = Calendar.getInstance ();
                calendar.clear ();
                calendar.set (Calendar.YEAR, year_);
                calendar.set (Calendar.DAY_OF_YEAR,
                    (rawBytes[offset+3] & 0x0f) * 100
                    + (rawBytes[offset+4] & 0x0f) * 10
                    + (rawBytes[offset+5] & 0x0f));
                calendar.setTime (calendar.getTime ()); 
                month_ = calendar.get (Calendar.MONTH);
                day_ = calendar.get (Calendar.DAY_OF_MONTH);
                break;

            case SQLConversionSettings.DATE_FORMAT_MDY:                      // mm/dd/yy
                month_ = (rawBytes[offset+0] & 0x0f) * 10
                    + (rawBytes[offset+1] & 0x0f) - 1;
                day_ = (rawBytes[offset+3] & 0x0f) * 10
                    + (rawBytes[offset+4] & 0x0f);
                year_ = twoDigitYearToFour ((rawBytes[offset+6] & 0x0f) * 10
                    + (rawBytes[offset+7] & 0x0f));
                break;

            case SQLConversionSettings.DATE_FORMAT_DMY:                      // dd/mm/yy
                day_ = (rawBytes[offset+0] & 0x0f) * 10
                    + (rawBytes[offset+1] & 0x0f);
                month_ = (rawBytes[offset+3] & 0x0f) * 10
                    + (rawBytes[offset+4] & 0x0f) - 1;
                year_ = twoDigitYearToFour ((rawBytes[offset+6] & 0x0f) * 10
                    + (rawBytes[offset+7] & 0x0f));
                break;

            case SQLConversionSettings.DATE_FORMAT_YMD:                      // yy/mm/dd
                year_ = twoDigitYearToFour ((rawBytes[offset+0] & 0x0f) * 10
                    + (rawBytes[offset+1] & 0x0f));
                month_ = (rawBytes[offset+3] & 0x0f) * 10
                    + (rawBytes[offset+4] & 0x0f) - 1;
                day_ = (rawBytes[offset+6] & 0x0f) * 10
                    + (rawBytes[offset+7] & 0x0f);
                break;

            case SQLConversionSettings.DATE_FORMAT_USA:                      // mm/dd/yyyy
                month_ = (rawBytes[offset+0] & 0x0f) * 10
                    + (rawBytes[offset+1] & 0x0f) - 1;
                day_ = (rawBytes[offset+3] & 0x0f) * 10
                    + (rawBytes[offset+4] & 0x0f);
                year_ = (rawBytes[offset+6] & 0x0f) * 1000
                    + (rawBytes[offset+7] & 0x0f) * 100
                    + (rawBytes[offset+8] & 0x0f) * 10
                    + (rawBytes[offset+9] & 0x0f);
                break;

            case SQLConversionSettings.DATE_FORMAT_ISO:                      // yyyy-mm-dd
            case SQLConversionSettings.DATE_FORMAT_JIS:                      // yyyy-mm-dd
                year_ = (rawBytes[offset+0] & 0x0f) * 1000
                    + (rawBytes[offset+1] & 0x0f) * 100
                    + (rawBytes[offset+2] & 0x0f) * 10
                    + (rawBytes[offset+3] & 0x0f);
                month_ = (rawBytes[offset+5] & 0x0f) * 10
                    + (rawBytes[offset+6] & 0x0f) - 1;
                day_ = (rawBytes[offset+8] & 0x0f) * 10
                    + (rawBytes[offset+9] & 0x0f);
                break;

            case SQLConversionSettings.DATE_FORMAT_EUR:                      // dd.mm.yyyy
                day_ = (rawBytes[offset+0] & 0x0f) * 10
                    + (rawBytes[offset+1] & 0x0f);
                month_ = (rawBytes[offset+3] & 0x0f) * 10
                    + (rawBytes[offset+4] & 0x0f) - 1;
                year_ = (rawBytes[offset+6] & 0x0f) * 1000
                    + (rawBytes[offset+7] & 0x0f) * 100
                    + (rawBytes[offset+8] & 0x0f) * 10
                    + (rawBytes[offset+9] & 0x0f);
                break;
        }
    }



    public void convertToRawBytes (byte[] rawBytes, int offset, ConverterImplRemote ccsidConverter)
        throws SQLException
    {
        // Always use ISO format here.
        StringBuffer buffer = new StringBuffer (10);
        buffer.append (JDUtilities.padZeros (year_, 4));
        buffer.append ('-');
        buffer.append (JDUtilities.padZeros (month_ + 1, 2));
        buffer.append ('-');
        buffer.append (JDUtilities.padZeros (day_, 2));

        try {
            ccsidConverter.stringToByteArray (buffer.toString (), rawBytes, offset);
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
            stringToDate ((String) object, settings_, calendar);
            year_   = calendar.get (Calendar.YEAR);
            month_  = calendar.get (Calendar.MONTH);
            day_    = calendar.get (Calendar.DAY_OF_MONTH);
        }

        else if (object instanceof Date) {
            calendar.setTime ((Date) object);
            year_   = calendar.get (Calendar.YEAR);
            month_  = calendar.get (Calendar.MONTH);
            day_    = calendar.get (Calendar.DAY_OF_MONTH);
        }

        else if (object instanceof Timestamp) {
            calendar.setTime ((Timestamp) object);
            year_   = calendar.get (Calendar.YEAR);
            month_  = calendar.get (Calendar.MONTH);
            day_    = calendar.get (Calendar.DAY_OF_MONTH);
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
        return 10;
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
        return "DATE";
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
        return 384;
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
        return 0;
    }


	public int getType ()
	{
		return java.sql.Types.DATE;
	}



	public String getTypeName ()
	{
		return "DATE";
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
        return 10;
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
	    calendar.set (year_, month_, day_, 0, 0, 0);
        calendar.set (Calendar.MILLISECOND, 0);
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
	    Calendar calendar = Calendar.getInstance ();
	    calendar.set (year_, month_, day_, 0, 0, 0);
	    return new Date (calendar.getTime ().getTime ());
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
	    calendar.set (year_, month_, day_, 0, 0, 0);
	    Date d = new Date (calendar.getTime ().getTime ());
	    return dateToString (d, settings_, calendar);
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
	    calendar.set (year_, month_, day_, 0, 0, 0);
	    Timestamp ts = new Timestamp (calendar.getTime ().getTime ());
	    ts.setNanos (0);
	    return ts;
	}



	public InputStream	toUnicodeStream ()
	    throws SQLException
	{
	    JDError.throwSQLException (JDError.EXC_DATA_TYPE_MISMATCH);
		return null;
	}



}

