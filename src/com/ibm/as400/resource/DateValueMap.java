///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DateValueMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.DateTimeConverter;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;



/**
The DateValueMap class maps between a logical Date value
and a variety of date formats for a physical value.
**/
class DateValueMap 
implements ValueMap, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;


    // Date formats.                                    // physical <--> logical
    public static final int FORMAT_6        = 6;        // HHMMSS <--> Date
    public static final int FORMAT_7        = 7;        // CYYMMDD <--> Date
    public static final int FORMAT_13       = 13;       // CYYMMDDHHMMSS <--> Date
    public static final int FORMAT_DTS      = 99;       // System *DTS (date timestamp) format <--> Date



    // Private data.
    static final Date NO_DATE = new Date(0);

    private static final String DTS             = "*DTS";
    private static final String HHMMSS_ZEROS    = "000000";
    private static final String CYYMMDD_ZEROS   = "0000000";

    private int format_                         = -1;



/**
Constructs a DateValueMap object.

@param format           The format.
**/
    public DateValueMap(int format)
    {
        if ((format != FORMAT_6) 
            && (format != FORMAT_7)
            && (format != FORMAT_13)
            && (format != FORMAT_DTS))
            throw new ExtendedIllegalArgumentException("format", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        format_ = format;
    }



/**
Maps from a logical value to a physical value.

@param logicalValue     The logical value.
@param system           The system.
@return                 The physical value.
**/
    public Object ltop(Object logicalValue, AS400 system)
    {
        if (logicalValue == null)
            throw new NullPointerException("logicalValue");

        switch(format_) {

        case FORMAT_13:
            return dateToString13((Date)logicalValue);

        case FORMAT_7:
            return dateToString13((Date)logicalValue).substring(0,7);

        case FORMAT_6:
            return dateToString13((Date)logicalValue).substring(7);

        case FORMAT_DTS:
            if (system == null)
                throw new NullPointerException("system");

            try {
                DateTimeConverter converter = new DateTimeConverter(system);
                return converter.convert((Date)logicalValue, DTS);
            } 
            catch(Exception e) {
                throw new ExtendedIllegalArgumentException("logicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
        }

        // If the format specified is bad...
        throw new ExtendedIllegalStateException("format", ExtendedIllegalStateException.UNKNOWN);
    }



/**
Maps from a physical value to a logical value.

@param physicalValue    The physical value.
@param system           The system.
@return                 The logical value.
**/
    public Object ptol(Object physicalValue, AS400 system)
    {
        // Validate the physical value.
        if (physicalValue == null)
            throw new NullPointerException("physicalValue");

        switch(format_) {

        case FORMAT_13:
            return string13ToDate((String)physicalValue);

        case FORMAT_7:
            return string13ToDate((String)physicalValue + HHMMSS_ZEROS);

        case FORMAT_6:
            return string13ToDate(CYYMMDD_ZEROS + (String)physicalValue);

        case FORMAT_DTS:
            if (system == null)
                throw new NullPointerException("system");
            try {
                byte[] asBytes = (byte[])physicalValue;

                // If the first byte is an EBCDIC space, assume the rest
                // are and specify a default date.
                if ((asBytes[0] == 0x40) || (asBytes[0] == 0x00))                   // @A1C
                    return NO_DATE;

                DateTimeConverter converter = new DateTimeConverter(system);
                return converter.convert((byte[])physicalValue, DTS);
            }
            catch(Exception e) {
                if (Trace.isTraceOn())
                    Trace.log(Trace.ERROR, "Error converting date from DTS format", e);                   
                throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
        }

        throw new ExtendedIllegalArgumentException("physicalValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }



/**
Converts a Date to a String in the format CYYMMDDHHMMSS.

@param date         The Date.
@return             The String.
**/
    private static String dateToString13(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        StringBuffer buffer13 = new StringBuffer(13);
        int year13 = calendar.get(Calendar.YEAR);
        buffer13.append((year13 < 2000) ? '0' : '1');
        buffer13.append(twoDigits(year13 % 100));
        buffer13.append(twoDigits(calendar.get(Calendar.MONTH) + 1));
        buffer13.append(twoDigits(calendar.get(Calendar.DAY_OF_MONTH)));
        buffer13.append(twoDigits(calendar.get(Calendar.HOUR_OF_DAY)));
        buffer13.append(twoDigits(calendar.get(Calendar.MINUTE)));
        buffer13.append(twoDigits(calendar.get(Calendar.SECOND)));
        return buffer13.toString();
    }


/**
Converts a String in the format CYYMMDDHHMMSS to a Date

@param string13     The String.
@return             The Date.
**/
    private static Date string13ToDate(String string13)
    {
        int length = string13.length();

        Calendar calendar = Calendar.getInstance();

        // If the date is all blanks or if it is a special
        // value, then return a date with all zeros.
        if (length == 0)
            return NO_DATE;
        else if (string13.charAt(0) == '*')
            return NO_DATE;

        int century = Integer.parseInt(string13.substring(0,1));
        int year    = Integer.parseInt(string13.substring(1,3));
        int month   = Integer.parseInt(string13.substring(3,5));
        int day     = Integer.parseInt(string13.substring(5,7));
        int hour    = Integer.parseInt(string13.substring(7,9));
        int minute  = Integer.parseInt(string13.substring(9,11));
        int second  = Integer.parseInt(string13.substring(11,13));

        calendar.set(Calendar.YEAR, year + ((century == 0) ? 1900 : 2000));
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0); // @A1A
        return calendar.getTime();
    }



/**
Returns a 2 digit String representation of the value.  
The value will be 0-padded on the left if needed.

@param value    The value.
@return         The 2 digit String representation.
**/
    private static String twoDigits(int value)
    {
        if (value > 99)
            throw new ExtendedIllegalArgumentException("value", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        String full = "00" + Integer.toString(value);
        return full.substring(full.length() - 2);
    }



}
