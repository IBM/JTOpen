///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDate.java
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
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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

final class SQLDate
implements SQLData
{
    private static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    // Private data.
    private SQLConversionSettings   settings_;
    private int			    dateFormat_;	// @550A
    private int                     truncated_;
    private int                     year_;
    private int                     month_;
    private int                     day_;

    SQLDate(SQLConversionSettings settings, int dateFormat)
    {
        settings_   = settings;
        truncated_  = 0;
        year_       = 0;
        month_      = 0;
        day_        = 0;
        dateFormat_ = dateFormat;	// @550A
    }

    public Object clone()
    {
        return new SQLDate(settings_, dateFormat_);	// @550C
    }

    public static Date stringToDate(String s,
                                    SQLConversionSettings settings,
                                    Calendar calendar)
    throws SQLException
    {
        // If the string is empty or set to zeros,
        // then it is likely a NULL, so just set this
        // to a default date.
        String sTrim = s.trim();
        int sTrimLength = sTrim.length();  // @F2A
        try
        {
            if((sTrimLength == 0) || (Integer.parseInt(sTrim) == 0))  // @F2C
                return new Date(0);
        }
        catch(NumberFormatException e)
        {
            // Ignore.  This just means the value is not NULL.
        }

        if(calendar == null) calendar = Calendar.getInstance(); //@P0A

        try
        {
            // Parse the string according to the format and separator.
            switch(settings.getDateFormat())
            {
                case SQLConversionSettings.DATE_FORMAT_USA:
                    calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(6, 10)));
                    calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(0, 2)) - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(3, 5)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_EUR:
                    calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(6, 10)));
                    calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(3, 5)) - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(0, 2)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_JULIAN:
                    if(sTrimLength <= 6)
                    {  // YY/DDD      // @F2C
                        calendar.set(Calendar.DAY_OF_YEAR, Integer.parseInt(s.substring(3, 6)));
                        calendar.set(Calendar.YEAR, twoDigitYearToFour(Integer.parseInt(s.substring(0, 2))));
                    }
                    else
                    {  // Assume they've specified a 4-digit year: YYYY/DDD    // @F2A
                        calendar.set(Calendar.DAY_OF_YEAR, Integer.parseInt(s.substring(5, 8)));
                        calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(0, 4)));
                    }
                    break;

                case SQLConversionSettings.DATE_FORMAT_MDY:
                    if(sTrimLength <= 8)
                    {  // MM/DD/YY     // @F2C
                        calendar.set(Calendar.YEAR, twoDigitYearToFour(Integer.parseInt(s.substring(6, 8))));
                    }
                    else
                    {  // Assume they've specified a 4-digit year: MM/DD/YYYY  // @F2A
                        calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(6, 10)));
                    }
                    calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(0, 2)) - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(3, 5)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_DMY:
                    if(sTrimLength <= 8)
                    {  // DD/MM/YY     // @F2C
                        calendar.set(Calendar.YEAR, twoDigitYearToFour(Integer.parseInt(s.substring(6, 8))));
                    }
                    else
                    {  // Assume they've specified a 4-digit year: DD/MM/YYYY    // @F2A
                        calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(6, 10)));
                    }
                    calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(3, 5)) - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(0, 2)));
                    break;

                case SQLConversionSettings.DATE_FORMAT_YMD:
                    if(sTrimLength <= 8)
                    {  // YY/MM/DD     // @F2C
                        calendar.set(Calendar.YEAR, twoDigitYearToFour(Integer.parseInt(s.substring(0, 2))));
                        calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(3, 5)) - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(6, 8)));
                    }
                    else
                    {  // Assume they've specified a 4-digit year: YYYY/MM/DD  // @F2A
                        calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(0, 4)));
                        calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(5, 7)) - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(8, 10)));
                    }
                    break;

                case SQLConversionSettings.DATE_FORMAT_JIS:
                case SQLConversionSettings.DATE_FORMAT_ISO:
                    calendar.set(Calendar.YEAR, Integer.parseInt(s.substring(0, 4)));
                    calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(5, 7)) - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(8, 10)));
                    break;
            }

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        catch(NumberFormatException e)
        {
            if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing date "+s, e);
            JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
        }
        catch(StringIndexOutOfBoundsException e)
        {
            if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing date "+s, e);
            JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
        }

        return new Date(calendar.getTime().getTime());
    }

    public static String dateToString(java.util.Date d,              // @F5C
                                      SQLConversionSettings dataFormat,
                                      Calendar calendar)
    {
        StringBuffer buffer = new StringBuffer();
        String separator = dataFormat.getDateSeparator();
        if(calendar == null) calendar = Calendar.getInstance(); //@P0A
        calendar.setTime(d);

        // @F3D Note: No matter what format is being used, ensure that exactly 10 characters are in the buffer.

        switch(dataFormat.getDateFormat())
        {
            
            case SQLConversionSettings.DATE_FORMAT_USA:                          // mm/dd/yyyy
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.MONTH) + 1, 2));
                buffer.append('/');
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.DAY_OF_MONTH), 2));
                buffer.append('/');
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.YEAR), 4));
                break;

            case SQLConversionSettings.DATE_FORMAT_EUR:                          // dd.mm.yyyy
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.DAY_OF_MONTH), 2));
                buffer.append('.');
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.MONTH) + 1, 2));
                buffer.append('.');
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.YEAR), 4));
                break;

            case SQLConversionSettings.DATE_FORMAT_JULIAN:                      // yy/ddd
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.YEAR), 2));
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.DAY_OF_YEAR), 3));
                // @F3D buffer.append("    ");
                break;

            case SQLConversionSettings.DATE_FORMAT_MDY:                         // mm/dd/yy
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.MONTH) + 1, 2));
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.DAY_OF_MONTH), 2));
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.YEAR), 2));
                // @F3D buffer.append("  ");
                break;

            case SQLConversionSettings.DATE_FORMAT_DMY:                         // dd/mm/yy
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.DAY_OF_MONTH), 2));
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.MONTH) + 1, 2));
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.YEAR), 2));
                // @F3D buffer.append("  ");
                break;

            case SQLConversionSettings.DATE_FORMAT_YMD:                         // yy/mm/dd
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.YEAR), 2));
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.MONTH) + 1, 2));
                buffer.append(separator);
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.DAY_OF_MONTH), 2));
                // @F3D buffer.append("  ");
                break;

            case SQLConversionSettings.DATE_FORMAT_JIS:                         // yyyy-mm-dd
            case SQLConversionSettings.DATE_FORMAT_ISO:                         // yyyy-mm-dd
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.YEAR), 4));
                buffer.append('-');
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.MONTH) + 1, 2));
                buffer.append('-');
                buffer.append(JDUtilities.padZeros(calendar.get(Calendar.DAY_OF_MONTH), 2));
                break;
        }

        return buffer.toString();
    }

    private static int twoDigitYearToFour(int twoDigitYear)
    {
        return(twoDigitYear <= 39)
        ? (twoDigitYear + 2000)
        : (twoDigitYear + 1900);
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
    	int connectionDateFormat = settings_.getDateFormat();	// @550A
    	// @550 If the date is in a stored procedure result set, it could have a different date format than the format of the connection
        switch(((dateFormat_ != -1) && (dateFormat_ != connectionDateFormat)) ? dateFormat_ : connectionDateFormat)	// @550C
        {
            
            case SQLConversionSettings.DATE_FORMAT_JULIAN:                      // yy/ddd
                year_ = twoDigitYearToFour((rawBytes[offset+0] & 0x0f) * 10
                                           + (rawBytes[offset+1] & 0x0f));
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(Calendar.YEAR, year_);
                calendar.set(Calendar.DAY_OF_YEAR,
                             (rawBytes[offset+3] & 0x0f) * 100
                             + (rawBytes[offset+4] & 0x0f) * 10
                             + (rawBytes[offset+5] & 0x0f));
                calendar.setTime(calendar.getTime()); 
                month_ = calendar.get(Calendar.MONTH);
                day_ = calendar.get(Calendar.DAY_OF_MONTH);
                break;

            case SQLConversionSettings.DATE_FORMAT_MDY:                      // mm/dd/yy
                month_ = (rawBytes[offset+0] & 0x0f) * 10
                         + (rawBytes[offset+1] & 0x0f) - 1;
                day_ = (rawBytes[offset+3] & 0x0f) * 10
                       + (rawBytes[offset+4] & 0x0f);
                year_ = twoDigitYearToFour((rawBytes[offset+6] & 0x0f) * 10
                                           + (rawBytes[offset+7] & 0x0f));
                break;

            case SQLConversionSettings.DATE_FORMAT_DMY:                      // dd/mm/yy
                day_ = (rawBytes[offset+0] & 0x0f) * 10
                       + (rawBytes[offset+1] & 0x0f);
                month_ = (rawBytes[offset+3] & 0x0f) * 10
                         + (rawBytes[offset+4] & 0x0f) - 1;
                year_ = twoDigitYearToFour((rawBytes[offset+6] & 0x0f) * 10
                                           + (rawBytes[offset+7] & 0x0f));
                break;

            case SQLConversionSettings.DATE_FORMAT_YMD:                      // yy/mm/dd
                year_ = twoDigitYearToFour((rawBytes[offset+0] & 0x0f) * 10
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

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        // Always use ISO format here.
        StringBuffer buffer = new StringBuffer(10);
        buffer.append(JDUtilities.padZeros(year_, 4));
        buffer.append('-');
        buffer.append(JDUtilities.padZeros(month_ + 1, 2));
        buffer.append('-');
        buffer.append(JDUtilities.padZeros(day_, 2));

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
            stringToDate((String) object, settings_, calendar);
            year_   = calendar.get(Calendar.YEAR);
            month_  = calendar.get(Calendar.MONTH);
            day_    = calendar.get(Calendar.DAY_OF_MONTH);
        }

        else if(object instanceof Timestamp)
        {    // @F5M
            calendar.setTime((Timestamp) object);
            year_   = calendar.get(Calendar.YEAR);
            month_  = calendar.get(Calendar.MONTH);
            day_    = calendar.get(Calendar.DAY_OF_MONTH);
        }

        else if(object instanceof java.util.Date)
        {     // @F5C
            calendar.setTime((java.util.Date) object);  // @F5C
            year_   = calendar.get(Calendar.YEAR);
            month_  = calendar.get(Calendar.MONTH);
            day_    = calendar.get(Calendar.DAY_OF_MONTH);
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
        return SQLData.DATE;
    }

    public String getCreateParameters()
    {
        return null;
    }


    public int getDisplaySize()
    {
        return 10;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.sql.Date";
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
        return "DATE";
    }

    public int getMaximumPrecision()
    {
        return 10;
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
        return 384;
    }

    public int getPrecision()
    {
        return 10;
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
        return java.sql.Types.DATE;
    }

    public String getTypeName()
    {
        return "DATE";
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
        return 10;
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
        truncated_ = 0;

        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(819, null).stringToByteArray(getString()));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
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
        truncated_ = 0;
        return new StringReader(getString());
    }

    public Clob getClob()
    throws SQLException
    {
        truncated_ = 0;
        String string = getString();
        return new AS400JDBCClob(string, string.length());
    }

    public Date getDate(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0;
        if(calendar == null) calendar = Calendar.getInstance();  
        calendar.set(year_, month_, day_, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Date(calendar.getTime().getTime());
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
        return getDate(null);
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
        calendar.set(year_, month_, day_, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date d = new Date(calendar.getTime().getTime());
        return dateToString(d, settings_, calendar);
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
        truncated_ = 0;
        if(calendar == null) calendar = Calendar.getInstance(); //@P0A  
        calendar.set(year_, month_, day_, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timestamp ts = new Timestamp(calendar.getTime().getTime());
        ts.setNanos(0);
        return ts;
    }

    public InputStream  getUnicodeStream()
    throws SQLException
    {
        truncated_ = 0;

        try
        {
            return new ByteArrayInputStream(ConvTable.getTable(13488, null).stringToByteArray(getString()));
        }
        catch(UnsupportedEncodingException e)
        {
            JDError.throwSQLException(this, JDError.EXC_INTERNAL, e);
            return null;
        }
    }
    
    //@pda jdbc40
    public Reader getNCharacterStream() throws SQLException
    {
        truncated_ = 0;
        return new StringReader(getNString());
    }
    
    //@pda jdbc40
    public NClob getNClob() throws SQLException
    {
        truncated_ = 0;                                     //@pda
        String string = getString();                        //@pda
        return new AS400JDBCNClob(string, string.length()); //@pda
    }

    //@pda jdbc40
    public String getNString() throws SQLException
    {
        truncated_ = 0;
        Calendar calendar = Calendar.getInstance();  
        calendar.set(year_, month_, day_, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date d = new Date(calendar.getTime().getTime());
        return dateToString(d, settings_, calendar);
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
        truncated_ = 0;                                     //@pda
        String string = getString();                        //@pda
        return new AS400JDBCSQLXML(string, string.length()); //@pda
    }
}

