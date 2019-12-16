///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: SQLTimestamp.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2001 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Date;
/* ifdef JDBC40
import java.sql.NClob;
import java.sql.RowId;
endif */
import java.sql.SQLException;
/* ifdef JDBC40
import java.sql.SQLXML;
endif */
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLTimestamp
extends SQLDataBase
{
    static final String copyright2 = "Copyright (C) 1997-2013 International Business Machines Corporation and others.";
    static boolean jdk14 = false;
    static {
      jdk14 = JVMInfo.isJDK14();
    }

    // Private data.
    private int                     year_;
    private int                     month_;  /* zero based, per Java convention */ 
    private int                     day_;
    private int                     hour_;
    private int                     minute_;
    private int                     second_;
    private long                    picos_;   /*@H3C*/
    private int                     length_; 

    SQLTimestamp(int length, SQLConversionSettings settings)
    {
        super(settings);
        length_ = length; 
        year_       = 0;
        month_      = 0;
        day_        = 0;
        hour_       = 0;
        minute_     = 0;
        second_     = 0;
        picos_      = 0;
    }

    public Object clone()
    {
        return new SQLTimestamp(length_, settings_);
    }

    public void setTimestampFieldsFromString(String s) 
        throws SQLException
        {
            try
            {
                int stringLength = s.length(); 
                // Check for a valid length 
                if (stringLength < 10) {
                  if (JDTrace.isTraceOn()) JDTrace.logInformation((Object)null, "Invalid timestamp length "+s); 
                  JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s); 
                }
                if ( (s.charAt(4) != '-' ) ||
                     (s.charAt(7) != '-' ) ) {
                  if (JDTrace.isTraceOn()) JDTrace.logInformation((Object)null, "Timestamp missing - "+s); //@dat1
                  JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s); //@dat1
                }
                // @E3D // If the string has a year 1, then it is likely a NULL, so
                // @E3D // just set this to a default date.
                year_ = Integer.parseInt(s.substring(0, 4));
                // @E3D if(year == 1) {
                // @E3D     return new Timestamp(0);
                // @E3D }

                month_ = Integer.parseInt(s.substring(5, 7)) - 1; 
                day_ =  Integer.parseInt(s.substring(8, 10));
                if (stringLength >=13) {
                  hour_= Integer.parseInt(s.substring(11, 13));
                } else {
                  hour_ = 0; 
                }
                if (stringLength >= 16) { 
                  minute_ = Integer.parseInt(s.substring(14, 16));
                } else {
                  minute_ =  0; 
                }
                if (stringLength >= 19) {
                  second_ =  Integer.parseInt(s.substring(17, 19));
                }  else {
                  second_ = 0; 
                }
                long picos = 0; 
                if(stringLength > 20)
                {                                             
                    String picosString = s.substring(20).trim() + "00000000000000";            
                    picos_ = Long.parseLong(picosString.substring(0, 12))  ;
                }
                else
                {
                    picos_ = 0;  
                }

            }
            catch(NumberFormatException e)
            {
              if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing timestamp "+s, e);
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
            }
            catch(StringIndexOutOfBoundsException e)
            {
              if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing timestamp "+s, e);
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
            }

        }
    
    public static Timestamp stringToTimestamp(String s, Calendar calendar)
    throws SQLException
    {
        try
        {
            int stringLength = s.length(); 
            // Check for a valid length 
            if (stringLength < 10) {
              if (JDTrace.isTraceOn()) JDTrace.logInformation((Object)null, "Invalid timestamp length "+s); 
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s); 
            }
            if ( (s.charAt(4) != '-' ) ||
                 (s.charAt(7) != '-' ) ) {
              if (JDTrace.isTraceOn()) JDTrace.logInformation((Object)null, "Timestamp missing - "+s); //@dat1
              JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s); //@dat1
            }
            // @E3D // If the string has a year 1, then it is likely a NULL, so
            // @E3D // just set this to a default date.
            int year = Integer.parseInt(s.substring(0, 4));
            // @E3D if(year == 1) {
            // @E3D     return new Timestamp(0);
            // @E3D }

            // Parse the string .
            // @E3D else {

            if(calendar == null)
            {
                calendar = AS400Calendar.getGregorianInstance(); // @F5A
                calendar.setLenient(false); //@dat1
            }
            else {
              calendar = AS400Calendar.getConversionCalendar(calendar);
            }
            
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, Integer.parseInt(s.substring(5, 7)) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(8, 10)));
            if (stringLength >=13) {
              calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(11, 13)));
            } else {
              calendar.set(Calendar.HOUR_OF_DAY, 0);
            }
            if (stringLength >= 16) { 
              calendar.set(Calendar.MINUTE, Integer.parseInt(s.substring(14, 16)));
            } else {
              calendar.set(Calendar.MINUTE, 0); 
            }
            if (stringLength >= 19) {
              calendar.set(Calendar.SECOND, Integer.parseInt(s.substring(17, 19)));
            }  else {
              calendar.set(Calendar.SECOND,0); 
            }
            // @F2A@H3C
            // Remember that the value for nanoseconds is optional.  If we don't check the
            // length of the string before trying to handle nanoseconds for the timestamp,
            // we will blow up if there is no value available.  An example of a String value
            // as a timestamp that would have this problem is:  "1999-12-31 12:59:59"
            long picos = 0; 
            if(stringLength > 20)
            {                                              // @F2A
                String picosString = s.substring(20).trim() + "00000000000000";            // @F2M
                picos = Long.parseLong(picosString.substring(0, 12))  ;
            }
            else
            {
                picos = 0;  // @F2A
            }

            
            
            Timestamp ts = null;//@dat1
            try //@dat1
            {
              long millis;
              if (jdk14) { millis =calendar.getTimeInMillis(); } else { millis = calendar.getTime().getTime(); }
                if (picos % 1000 == 0) {   //@H3A
                  ts = new Timestamp(millis); //@dat1
                  ts.setNanos((int)(picos / 1000)); 
                } else {
                  AS400JDBCTimestamp aTs = new AS400JDBCTimestamp(millis, 32);
                  aTs.setPicos(picos);
                  ts = aTs; 
                }
                
            }catch(Exception e){
                if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing timestamp "+s, e); //@dat1
                JDError.throwSQLException(null, JDError.EXC_DATA_TYPE_MISMATCH, e, s); //@dat1
                return null; //@dat1
            }


            return ts;
            // @E3D }
        }
        catch(NumberFormatException e)
        {
          if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing timestamp "+s, e);
          JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
        }
        catch(StringIndexOutOfBoundsException e)
        {
          if (JDTrace.isTraceOn()) JDTrace.logException((Object)null, "Error parsing timestamp "+s, e);
          JDError.throwSQLException(JDError.EXC_DATA_TYPE_MISMATCH, s);
        }

        return new Timestamp(0);
    }

    public static String timestampToString(Timestamp ts,
                                           Calendar calendar,
                                           SQLConversionSettings settings)
    {
        return timestampToString(ts, calendar, -1, 26, settings);             // @F4C
    }

    
    /**
     * Convert a timestamp to a string and get rid of trailing zeros. 
     * To be in JDBC timestamp format, you need to leave at least a
     * xx:xx:xx.0
     * @param ts 
     * @param calendar 
     * @param settings 
     * @return trimmed timestamp string
     */
    /*@I2A*/
    public static String timestampToStringTrimTrailingZeros(Timestamp ts,
        Calendar calendar, SQLConversionSettings settings)
    {
      String tsString= timestampToString(ts, calendar, -1, 32, settings);
      int lastZero = 32;
      while (tsString.charAt(lastZero-1)=='0' && lastZero > 21) {
        lastZero --; 
      }
      if (lastZero < 32) {
        tsString=tsString.substring(0,lastZero);
      }
      return tsString; 
    }

    
    public static String timestampToString(Timestamp ts,
        Calendar calendar, 
        int hourIn,
        SQLConversionSettings settings ) {
       return  timestampToString(ts, calendar, hourIn, 26, settings); 
    }

    
    // @F4A - New method - Contains the logic from the old method, with the addition of the hourIn parameter.
    public static String timestampToString(Timestamp ts,
                                           Calendar calendar, 
                                           int hourIn, 
                                           int length,
                                           SQLConversionSettings settings) /*@I2C*/
    {
        // @F3A
        // The native driver outputs timestamps like 2100-01-02-03.45.56.000000, 
        // while we output timestamps like 2100-01-02 03:45:56.000000. 
        // The first is apparently the ISO standard while ours follows Java's Timestamp.toString() method. 
        // This was pointed out by a user who noticed that although he gets a timestamp from our database in 
        // one format, he can't put it back in the database in that same format.
        // @F6A Change back to old format because of service issue.
        
        if(calendar == null) calendar = AS400Calendar.getGregorianInstance(); //@P0A
        else {
          calendar = AS400Calendar.getConversionCalendar(calendar);
        }

        calendar.setTime(ts);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);  //@tim2
        if(hourIn == 24 && hour==0)     //@tim2
        {//@tim2
            //db2 represents midnight as 24:00 and midnight + 1 min as 00:01, but in java, 24 is midnight of
            //the next day (ie calendar.set(2007, 9, 10, 24, 0, 0) toString() -> 2007-10-11 00:00)
            //java changes 1/1/2007 24:00 -> 1/2/2007 00:00
            //for Native jdbc driver compliance, code block at bottom replaces "00" with "24", but need to
            //go back one day to counteract java's conversion.
            calendar.add(Calendar.DATE, -1);
        }//@tim2

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int minute = calendar.get(Calendar.MINUTE); 
        int second = calendar.get(Calendar.SECOND); 
        
        long picos = 0; 
        if (length > 20) {      
          if (ts instanceof AS400JDBCTimestamp) {  
            picos = ((AS400JDBCTimestamp) ts).getPicos();
          } else {
             picos = ts.getNanos() * 1000L; 
          }
        }
        if(hourIn == 24 && hour==0)  { hour = 24;   }

        return buildString(year, month, day, hour, minute, second, picos, length, settings);
    }

    private static String buildString(int year, int month, int day, int hour,
        int minute, int second, long picos, int length,
        SQLConversionSettings settings) {

      StringBuffer buffer = new StringBuffer();

      // @KE3
      // Support the settings.timestampFormat
      char dayHourSep=' '; 
      char hourMinuteSep=':';
      char minuteSecondSep=':'; 
      
      if (settings.getTimestampFormat() == SQLConversionSettings.TIMESTAMP_FORMAT_IBMSQL) {
        dayHourSep='-'; 
        hourMinuteSep='.';
        minuteSecondSep='.'; 
        
      }
      
      buffer.append(JDUtilities.padZeros(year, 4));
      buffer.append('-');
      buffer.append(JDUtilities.padZeros(month + 1, 2));
      buffer.append('-');
      buffer.append(JDUtilities.padZeros(day, 2));
      buffer.append(dayHourSep); //@KEA 

      
      buffer.append(JDUtilities.padZeros(hour, 2));       // @F4C
      
      buffer.append(hourMinuteSep);  
      buffer.append(JDUtilities.padZeros(minute, 2));

      buffer.append(minuteSecondSep);  

      buffer.append(JDUtilities.padZeros(second, 2));
      
      if (length > 20) {      
        buffer.append('.');
        buffer.append(JDUtilities.padZeros(picos, 12)); 
        buffer.setLength(length);  
      }
     
      buffer.setLength(length);  

      return buffer.toString();
      

    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter, boolean ignoreConversionErrors) //@P0C
    throws SQLException
    {
        year_ = (rawBytes[offset] & 0x0f) * 1000
                + (rawBytes[offset+1] & 0x0f) * 100
                + (rawBytes[offset+2] & 0x0f) * 10
                + (rawBytes[offset+3] & 0x0f);

        // @E3D // If the string has a year 1, then it is likely a NULL, so
        // @E3D // just set this to a default date.
        // @E3D if(year_ != 1) {
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
        /*@H3C */ 
        int picoOffset = 20;
        long multiplier = 100000000000L;
        picos_ = 0; 
        while (picoOffset < length_) {
        picos_ += ((long)(rawBytes[offset+picoOffset] & 0x0f) * multiplier);
        multiplier = multiplier / 10;
        picoOffset++;  
        }
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        StringBuffer buffer = new StringBuffer(getString().replace(':', '.'));
        buffer.setCharAt(10, '-');
        if (buffer.length() > length_) { 
          buffer.setLength(length_);
        }
        try
        {
            ccsidConverter.stringToByteArray(buffer.toString(), rawBytes, offset);
        }
        catch(CharConversionException e)
        {
            JDError.throwSQLException(JDError.EXC_INTERNAL, e);    // @E2C
        }
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.TIMESTAMP;
    }

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        if(calendar == null)
        {
            calendar = AS400Calendar.getGregorianInstance(); // @F5A
            calendar.setLenient(false); //@dat1
        }
        else {
          calendar = AS400Calendar.getConversionCalendar(calendar);
        }

        if(object instanceof String)
        {
            // If the user passes in a string, we assume that we can 
            // just get the values from the string and send it to 
            // the database as is.  This means we are able to 
            // insert timestamps that may not exist in the current
            // timezone (such as 2016-03-13 02:15:00.000000 in
            // a timezone with daylight saving time) 
            setTimestampFieldsFromString((String)object); 
        }

        else if(object instanceof Timestamp)
        {    // @F5M
            calendar.setTime((Timestamp) object);
            year_   = calendar.get(Calendar.YEAR);
            month_  = calendar.get(Calendar.MONTH);
            day_    = calendar.get(Calendar.DAY_OF_MONTH);
            hour_   = calendar.get(Calendar.HOUR_OF_DAY);
            minute_ = calendar.get(Calendar.MINUTE);
            second_ = calendar.get(Calendar.SECOND);
            if (object instanceof AS400JDBCTimestamp) {
              picos_  = ((AS400JDBCTimestamp) object).getPicos();
            } else { 
               picos_  = ((long)((Timestamp) object).getNanos())*1000L;
            }
            
        }

        else if(object instanceof java.util.Date)
        {     // @F5C
            calendar.setTime((java.util.Date) object);  // @F5C
            year_   = calendar.get(Calendar.YEAR);
            month_  = calendar.get(Calendar.MONTH);
            day_    = calendar.get(Calendar.DAY_OF_MONTH);
            hour_   = calendar.get(Calendar.HOUR_OF_DAY);
            minute_ = calendar.get(Calendar.MINUTE);
            second_ = calendar.get(Calendar.SECOND);
            picos_  = ((long)calendar.get(Calendar.MILLISECOND)) * 1000000000L;
        }
        else if (object instanceof AS400JDBCFieldedTimestamp) {
          AS400JDBCFieldedTimestamp ts = (AS400JDBCFieldedTimestamp) object; 
          year_   = ts.getYear();
          month_  = ts.getMonth();
          day_    = ts.getDay();
          hour_   = ts.getHour();
          minute_ = ts.getMinute();
          second_ = ts.getSecond();
          picos_  = ts.getPicos();
          length_ = ts.getLength(); 
        }
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

    public String getCreateParameters()
    {
        return null;
    }

    public int getDisplaySize()
    {
        return length_;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.sql.Timestamp";
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
        return "TIMESTAMP";
    }

    public int getMaximumPrecision()
    {
        return length_;
    }

    public int getMaximumScale()
    {
        return getScale(); 
    }

    public int getMinimumScale()
    {
        return getScale(); 
    }

    public int getNativeType()
    {
        return 392;
    }

    public int getPrecision()
    {
        return length_;
    }

    public int getRadix()
    {
        return 10;
    }

    public int getScale()
    {
        int scale = length_ - 20; 
        if (scale >= 0) return scale; 
        else return 0; 
    }

    public int getType()
    {
        return java.sql.Types.TIMESTAMP;
    }

    public String getTypeName()
    {
        return "TIMESTAMP";
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
        return length_;
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
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public InputStream getBinaryStream()
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

    public Blob getBlob()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
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


    public Date getDate(Calendar calendar)
    throws SQLException
    {
        truncated_ = 16;
        outOfBounds_ = false;
        if(calendar == null) calendar = AS400Calendar.getGregorianInstance(); //@P0A
        else {
          calendar = AS400Calendar.getConversionCalendar(calendar);
        }

        calendar.set(year_, month_, day_, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);      //@KBA  added per JTOpen Bug 3818.  According to java.sql.Date, the milliseconds also need to be 'normalized' to zero.
        long millis;
        if (jdk14) { millis =calendar.getTimeInMillis(); } else { millis = calendar.getTime().getTime(); }
        return new Date(millis);
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
        truncated_ = 0; outOfBounds_ = false;
        Calendar calendar = AS400Calendar.getGregorianInstance();
        calendar.set(year_, month_, day_, hour_, minute_, second_);
        long millis;
        if (jdk14) { millis =calendar.getTimeInMillis(); } else { millis = calendar.getTime().getTime(); }
        if (picos_ % 1000 == 0) { 
          Timestamp ts = new Timestamp (millis);
          ts.setNanos((int)(picos_ / 1000));
          return ts;
        } else {
          AS400JDBCTimestamp ts = new AS400JDBCTimestamp(millis); 
          ts.setPicos(picos_); 
          return ts; 
        }
    }
   public Object getBatchableObject() {
     return new AS400JDBCFieldedTimestamp(year_, month_, day_, hour_, minute_, second_, picos_, length_); 
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
        truncated_ = 0; outOfBounds_ = false;
        
        // The former implementation converts this to a timestamp which then
        // uses the timestampToString to convert back. Seems like a lot of overkill.
        //
        // 
        // The logic to create a string was added to the buildStringMethod 
        // and removed from the timestampToString method. 
        return buildString(year_, month_, day_, hour_, minute_, second_, picos_, length_, settings_); 
        
        /*
         * Removed code 
         */
        // Note:  For this conversion, we cannot use a default calendar.  If the
        // calendar implements daylight savings time, then there are some times that
        // do not exist.  For example, 2011-03-13 02:32:17 does not exist.
        // Calendar calendar = AS400Calendar.getGregorianInstance(); @G4C
        /* 
        Calendar calendar = AS400Calendar.getGMTInstance();
        calendar.set(year_, month_, day_, hour_, minute_, second_);
        long millis;
        if (jdk14) { millis =calendar.getTimeInMillis(); } else { millis = calendar.getTime().getTime(); }
    if (picos_ % 1000 == 0) {
      Timestamp ts = new Timestamp(millis);
      ts.setNanos((int)(picos_ / 1000));
      return timestampToString(ts, calendar, hour_, length_, settings_); // @F4C@I2C
    } else {
      AS400JDBCTimestamp ts = new AS400JDBCTimestamp(millis); 
      ts.setPicos(picos_); 
      return timestampToString(ts, calendar, hour_, length_, settings_); // @F4C@I2C
    }
    */
        
    }


    public Time getTime(Calendar calendar)
    throws SQLException
    {
        truncated_ = 18;
        outOfBounds_ = false;
        if(calendar == null) calendar = AS400Calendar.getGregorianInstance(); //@P0A
        else {
          calendar = AS400Calendar.getConversionCalendar(calendar);
        }

        calendar.set(0, 0, 0, hour_, minute_, second_);
        long millis;
        if (jdk14) { millis =calendar.getTimeInMillis(); } else { millis = calendar.getTime().getTime(); }
        return new Time(millis);
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false;
        if(calendar == null) calendar = AS400Calendar.getGregorianInstance(); //@P0A
        else {
          calendar = AS400Calendar.getConversionCalendar(calendar);
        }

        calendar.set(year_, month_, day_, hour_, minute_, second_);
        long millis;
        if (jdk14) { millis =calendar.getTimeInMillis(); } else { millis = calendar.getTime().getTime(); }
    if (picos_ % 1000 == 0) {
      Timestamp ts = new Timestamp(millis);
      ts.setNanos((int)(picos_ / 1000));
      return ts;
    } else {
      AS400JDBCTimestamp ts = new AS400JDBCTimestamp(millis); 
      ts.setPicos(picos_); 
      return ts; 
        }
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
    
    
    
    
    /*
     * Unit test code 
   
    
    public static void main(String[] args) {
      String[][] tests = {
          {"2100-01-02 03:45:56.000000","2100-01-02 03:45:56.0"},
          {"2100-01-02 03:45:56.100000","2100-01-02 03:45:56.1"},
          {"2100-01-02 03:45:56.120000","2100-01-02 03:45:56.12"},
          {"2100-01-02 03:45:56.103000","2100-01-02 03:45:56.103"},
          {"2100-01-02 03:45:56.100400","2100-01-02 03:45:56.1004"},
          {"2100-01-02 03:45:56.100050","2100-01-02 03:45:56.10005"},
          {"2100-01-02 03:45:56.10000600","2100-01-02 03:45:56.100006"},
          {"2100-01-02 03:45:56.100000700","2100-01-02 03:45:56.1000007"},
          {"2100-01-02 03:45:56.100000780","2100-01-02 03:45:56.10000078"},
          {"2100-01-02 03:45:56.100000709","2100-01-02 03:45:56.100000709"},
          {"2100-01-02 03:45:56.1000007001000","2100-01-02 03:45:56.1000007001"},
          {"2100-01-02 03:45:56.1000007000200","2100-01-02 03:45:56.10000070002"},
          {"2100-01-02 03:45:56.1000007000030","2100-01-02 03:45:56.100000700003"},
          {"2100-01-02 03:45:56","2100-01-02 03:45:56.0"},
          {"2100-01-02 03:45:","2100-01-02 03:45:00.0"},
          {"2100-01-02 03:45","2100-01-02 03:45:00.0"},
          {"2100-01-02 03:","2100-01-02 03:00:00.0"},
          {"2100-01-02 03","2100-01-02 03:00:00.0"},
          {"2100-01-02 ","2100-01-02 00:00:00.0"},
          {"2100-01-02","2100-01-02 00:00:00.0"},
          {"1922-03-04","1922-03-04 00:00:00.0"},
      }; 
      int failCount = 0; 
      for (int i =0 ; i < tests.length; i++) {
        String inString = tests[i][0];
        String expected = tests[i][1];
        Timestamp ts=null;
        int inStringLength = inString.length(); 
        if (inStringLength <= 29 && inStringLength > 20) { 
          ts = Timestamp.valueOf(inString);
        } else { 
          
          try {
            ts = stringToTimestamp(inString, null);
          } catch (SQLException e) {
            e.printStackTrace();
          } 
        }
        String outString = timestampToStringTrimTrailingZeros(ts, null);
        if (outString.equals(expected)) { 
        System.out.println(inString+"->"+outString); 
        } else {
          System.out.println("FAILED:  "+inString+"->"+outString+" sb "+expected); 
          failCount++; 
          
        }
      } // for  
      if (failCount > 0) {
        System.out.println("********** ERROR:some testcases failed.  *********** ");
      }
    }
     
*/

    
    public void saveValue() throws SQLException {
      savedValue_ = buildString(year_ , month_, day_, hour_,
          minute_, second_, picos_, length_, 
          settings_);
    }
}

