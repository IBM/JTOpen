///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLTime.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLTime
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;
    private int			    timeFormat_;
    private int                     hour_;
    private int                     minute_;
    private int                     second_;
    private int                     truncated_;

    SQLTime(SQLConversionSettings settings, int timeFormat)	// @550C
    {
        settings_   = settings;
        hour_       = 0;
        minute_     = 0;
        second_     = 0;
        truncated_  = 0;
        timeFormat_	= timeFormat;	// @550A
    }

    public Object clone()
    {
        return new SQLTime(settings_, timeFormat_);	// @550C
    }

    public static Time stringToTime(String s,
                                    SQLConversionSettings settings,
                                    Calendar calendar)
    throws SQLException
    {
        try
        {

            // If the string is empty, then it is likely a NULL, so
            // just set this to a default date.
            if(s.trim().length() == 0)
                return new Time(0);

            // Parse the string according to the format and separator.
            // else if(format.equalsIgnoreCase(JDProperties.TIME_FORMAT_USA)) {
            if(calendar == null) calendar = Calendar.getInstance(); //@P0A
            switch(settings.getTimeFormat())
            {                                          // @A0A
                case SQLConversionSettings.TIME_FORMAT_USA:                                                  // @A0A
                    int hour = Integer.parseInt(s.substring(0, 2));
                    char amPm = s.charAt(6);
                    if(hour == 12)
                    {
                        if(amPm == 'A')
                            hour = 0;
                    }
                    else
                    { // Hour between 1 and 11.
                        if(amPm == 'P')
                            hour += 12;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, Integer.parseInt(s.substring(3, 5)));
                    calendar.set(Calendar.SECOND, 0);
                    break;                                                      // @A0A

                case SQLConversionSettings.TIME_FORMAT_EUR:
                case SQLConversionSettings.TIME_FORMAT_JIS:
                case SQLConversionSettings.TIME_FORMAT_HMS:
                case SQLConversionSettings.TIME_FORMAT_ISO:
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(0, 2)));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(s.substring(3, 5)));
                    calendar.set(Calendar.SECOND, Integer.parseInt(s.substring(6, 8)));
                    break;
            }

            calendar.set(Calendar.YEAR, 0);
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        catch(NumberFormatException e)
        {
          if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing time "+s, e);
          JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
        }
        catch(StringIndexOutOfBoundsException e)
        {
          if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing time "+s, e);
          JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
        }

        return new Time(calendar.getTime().getTime());
    }

    public static String timeToString(Time t,
                                      SQLConversionSettings dataFormat,
                                      Calendar calendar)
    {
        return timeToString(t, dataFormat, calendar, -1);      // @E3C
    }

    // @E3A - This contains the logic from the original timeToString(), with new arg "hourIn".
    private static String timeToString(Time t,
                                       SQLConversionSettings dataFormat,
                                       Calendar calendar,
                                       int hourIn)
    {
        StringBuffer buffer = new StringBuffer();
        String separator = dataFormat.getTimeSeparator();
        if(calendar == null) calendar = Calendar.getInstance(); //@P0A
        calendar.setTime(t);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // @E3A
        int minute = calendar.get(Calendar.MINUTE);     // @E3A
        int second = 0;                                  // @E3A

        // Note: No matter what format is being used,
        // ensure that exactly 8 characters are in the
        // buffer.

        switch(dataFormat.getTimeFormat())
        {                                           // @A0A
            case SQLConversionSettings.TIME_FORMAT_USA:                                                   // @A0A
                // @E3D int hour = calendar.get(Calendar.HOUR_OF_DAY);
                char amPm;
                //@PDc - translate to ampm based on hour,min,sec
                
                //13-23 -> (x-12)pm
                //12 -> 12pm
                //1-11 -> xam
                //0 -> 12am
                //0:0:0 -> 00:00:00am (special case)
                //24:0:0 -> 12:00:00am (special case) //hour=0, hourIn=24 since 0 and 24 both map to 0 in Calendar
             
                if(hour > 12)
                {
                    hour -= 12;
                    amPm = 'P';
                }
                else if(hour == 12)
                    amPm = 'P';
                else if((hour > 0) && (hour < 12))
                    amPm = 'A';
                else if((minute == 0) && (second == 0))
                {
                    //special cases: 0:0:0 and 24:0:0
                    if(hourIn == 24)
                        hour = 12;
                    amPm = 'A';
                }
                else 
                { 
                    //0 hour case
                    hour = 12;
                    amPm = 'A';
                } 
                    
                buffer.append(JDUtilities.padZeros(hour, 2));
                buffer.append(':');
                buffer.append(JDUtilities.padZeros(minute, 2));            // @E3C
                buffer.append(' ');
                buffer.append(amPm);
                buffer.append('M');
                break;                                                       // @A0A

            case SQLConversionSettings.TIME_FORMAT_EUR:                                                   // @A0A
            case SQLConversionSettings.TIME_FORMAT_ISO:                                                   // @A0A
                second = calendar.get(Calendar.SECOND);             // @E3A
                buffer.append(JDUtilities.padZeros(hour, 2));      // @E3C
                buffer.append('.');
                buffer.append(JDUtilities.padZeros(minute, 2));    // @E3C
                buffer.append('.');
                buffer.append(JDUtilities.padZeros(second, 2));    // @E3C
                break;                                                       // @A0A

            case SQLConversionSettings.TIME_FORMAT_JIS:                                                   // @A0A
                second = calendar.get(Calendar.SECOND);             // @F3A
                buffer.append(JDUtilities.padZeros(hour, 2));      // @E3C
                buffer.append(':');
                buffer.append(JDUtilities.padZeros(minute, 2));    // @E3C
                buffer.append(':');
                buffer.append(JDUtilities.padZeros(second, 2));    // @E3C
                break;                                                       // @A0A

            case SQLConversionSettings.TIME_FORMAT_HMS:                                                   // @A0A
                second = calendar.get(Calendar.SECOND);             // @E3A
                buffer.append(JDUtilities.padZeros(hour, 2));      // @E3C
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(minute, 2));    // @E3C
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(second, 2));    // @E3C
                break;                                                       // @A0A
        }

        // The Calendar class represents 24:00:00 as 00:00:00.        // @E3A
        if(hourIn == 24 && hour==0 && minute==0 && second==0)  // @E3A //@tim3
        {
            buffer.setCharAt(0,'2');                                    // @E3A
            buffer.setCharAt(1,'4');                                    // @E3A
            // Note: StringBuffer.replace() is available in Java2.
        }

        return buffer.toString();
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
    	int connectionTimeFormat = settings_.getTimeFormat();	// @550A
    	// @550 If the time is from a stored procedure result set, it could be in a different time format than the connection's format
    	switch(((timeFormat_ != -1) && (timeFormat_ != connectionTimeFormat)) ? timeFormat_ : connectionTimeFormat)	// @550C
        {
            
            case SQLConversionSettings.TIME_FORMAT_USA:                      // hh:mm AM or PM
                hour_   = (rawBytes[offset] & 0x0f) * 10 + (rawBytes[offset+1] & 0x0f);
                minute_ = (rawBytes[offset+3] & 0x0f) * 10 + (rawBytes[offset+4] & 0x0f);
                second_ = 0;
                
                //translate from ampm to 24hour
                //since we can get back duplicate values of 00:00 (00:00am) and 24:00 (12:00am) (which map to
                //the same time of day in Calendar), we need a way to differentiate the two.
                if(((rawBytes[offset+6] == (byte)0xd7) && (hour_ < 12)) || // xd7='P'
                   ((rawBytes[offset+6] == (byte)0xC1) && (hour_ == 12))) // xC1='A'
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

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        StringBuffer buffer = new StringBuffer(8);

        // Always use ISO format here.
        buffer.append(JDUtilities.padZeros(hour_, 2));
        buffer.append('.');
        buffer.append(JDUtilities.padZeros(minute_, 2));
        buffer.append('.');
        buffer.append(JDUtilities.padZeros(second_, 2));

        try
        {
            ccsidConverter.stringToByteArray(buffer.toString(), rawBytes, offset);
        }
        catch(CharConversionException e)
        {
            JDError.throwSQLException(JDError.EXC_INTERNAL, e);        // @E2C
        }
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        if(calendar == null) calendar = Calendar.getInstance(); //@P0A  
        if(object instanceof String)
        {
            stringToTime((String) object, settings_, calendar);
            hour_   = calendar.get(Calendar.HOUR_OF_DAY);
            minute_ = calendar.get(Calendar.MINUTE);
            second_ = calendar.get(Calendar.SECOND);
        }

        else if(object instanceof Time)
        {
            calendar.setTime((Time) object);
            hour_   = calendar.get(Calendar.HOUR_OF_DAY);
            minute_ = calendar.get(Calendar.MINUTE);
            second_ = calendar.get(Calendar.SECOND);
        }

        else if(object instanceof Timestamp)
        {
            calendar.setTime((Timestamp) object);
            hour_   = calendar.get(Calendar.HOUR_OF_DAY);
            minute_ = calendar.get(Calendar.MINUTE);
            second_ = calendar.get(Calendar.SECOND);
        }

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
        return SQLData.TIME;
    }

    public String getCreateParameters()
    {
        return null;
    }

    public int getDisplaySize()
    {
        return 8;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.sql.Time";
    }

    public String getLiteralPrefix()
    {
        return "\'";
    }

    public String getLiteralSuffix()
    {
        return "\'";
    }

    public String getLocalName()
    {
        return "TIME";
    }

    public int getMaximumPrecision()
    {
        return 8;
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
        return 388;
    }

    public int getPrecision()
    {
        return 8;
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
        return java.sql.Types.TIME;
    }

    public String getTypeName()
    {
        return "TIME";
    }

    public boolean isSigned()
    {
        return false;
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

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//

    public InputStream getAsciiStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public BigDecimal getBigDecimal(int scale)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
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
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return false;
    }

    public byte getByte()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public byte[] getBytes()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Reader getCharacterStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Clob getClob()
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
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public float getFloat()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public int getInt()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public long getLong()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public Object getObject()
    throws SQLException
    {
        truncated_ = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 1, hour_, minute_, second_);
        calendar.set(calendar.MILLISECOND, 0);
        return new Time(calendar.getTime().getTime());
    }

    public short getShort()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return -1;
    }

    public String getString()
    throws SQLException
    {
        truncated_ = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 1, hour_, minute_, second_);
        calendar.set(calendar.MILLISECOND, 0);
        Time t = new Time(calendar.getTime().getTime());
        return timeToString(t, settings_, calendar, hour_);        // @E3C
    }

    public Time getTime(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        if(calendar == null) calendar = Calendar.getInstance(); //@P0A  
        // @F2A
        // You are supposed to normalize the time to the epoch,
        // not set its extra fields to 0.  This produces totally different
        // results.  See JDBC API Reference and Tutorial, pg 860 for complete
        // details.
        calendar.set(1970, Calendar.JANUARY, 1, hour_, minute_, second_);  // @F2C
        // @F2D calendar.set(0, 0, 0, hour_, minute_, second_);

        // Make sure to set the millisecond value from the time too as
        // SQL Time objects do not track this field.
        calendar.set(calendar.MILLISECOND, 0);  // @F2A

        return new Time(calendar.getTime().getTime());
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        //
        // The JDBC 1.22 specification says that this conversion
        // does not need to be supported, but the Graham Hamilton
        // book says it does.  I am going to go with the spec for
        // now, since I don't think that any such conversion
        // really makes sense.
        //
        //@54A JDBC 3.0 specification says this converstion is valid
        //@54D JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        //@54D return null;

        //@54A
        truncated_  = 0;                                                                //@54A
        if(calendar == null) calendar = Calendar.getInstance();                         //@54A
        calendar.set(1970, Calendar.JANUARY, 1, hour_, minute_, second_);               //@54A
        calendar.set(calendar.MILLISECOND, 0);                                          //@54A
        Timestamp ts = new Timestamp(calendar.getTime().getTime());                     //@54A
        ts.setNanos(0);                                                                 //@54A
        return ts;                                                                      //@54A


    }

    public InputStream  getUnicodeStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@pda jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH); //@pdc
        return null;  //@pdc
    }
    
    //@pda jdbc40
    public NClob getNClob() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@pda jdbc40
    public String getNString() throws SQLException
    {
        truncated_ = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 1, hour_, minute_, second_);
        calendar.set(calendar.MILLISECOND, 0);
        Time t = new Time(calendar.getTime().getTime());
        return timeToString(t, settings_, calendar, hour_);        // @E3C
    }

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
}

