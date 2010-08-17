///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Date.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Calendar;
import java.text.ParseException;

/**
 Provides a converter between a {@link java.sql.Date java.sql.Date} object and an IBM i <i>date</i> value such as "12/31/97".
 In the IBM i programming reference, this type is referred to as the "<b>Date</b> Data Type".
 <p>
 An IBM i <i>date</i> value simply indicates a year/month/day, and does not indicate a contextual time zone.  Internally, this class interprets all date- and time-related strings as relative to the GMT time zone.
 <p>
 Suggestion: To avoid confusion and unexpected results when crossing time zones:
 <br>Whenever creating or interpreting instances of {@link java.sql.Date java.sql.Date}, {@link java.sql.Time java.sql.Time}, or {@link java.sql.Timestamp java.sql.Timestamp}, always assume that the reference time zone for the object is GMT, and avoid using any deprecated methods.  If it is necessary to convert date/time values between GMT and other time zones, use methods of <tt>Calendar</tt>.  Rather than using <tt>toString()</tt> to display the value of a date/time object, use <tt>DateFormat.format()</tt> after specifying a GMT TimeZone.
 <p>
 Suggestion: To avoid confusion between different kinds of "Date" objects, fully qualify all references to classes <tt>java.<b>util</b>.Date</tt> and <tt>java.<b>sql</b>.Date</tt> (especially if you import both <tt>java.util.*</tt> and <tt>java.sql.*</tt>).
 <p>
 Note: In the descriptions of the "format" constants, all example dates represent the date <b>April 25, 1997</b>.

 @see AS400Time
 @see AS400Timestamp
 **/
public class AS400Date extends AS400AbstractTime
{
  private java.sql.Date defaultValue_;

  /** Date format *MDY (Month/Day/Year).
   <br>Example: 04/25/97
   <br>Range of years: 1940-2039
   <br>Default separator: '/'
   <br>Length: 8 bytes **/
  public static final int FORMAT_MDY = 0;

  /** Date format *DMY (Day/Month/Year).
   <br>Example: 25/04/97
   <br>Range of years: 1940-2039
   <br>Default separator: '/'
   <br>Length: 8 bytes **/
  public static final int FORMAT_DMY = 1;

  /** Date format *YMD (Year/Month/Day).
   <br>Example: 97/04/25
   <br>Range of years: 1940-2039
   <br>Default separator: '/'
   <br>Length: 8 bytes **/
  public static final int FORMAT_YMD = 2;

  /** Date format *JUL (Julian).
   <br>Example: 97/115
   <br>Range of years: 1940-2039
   <br>Default separator: '/'
   <br>Length: 6 bytes **/
  public static final int FORMAT_JUL = 3;

  /** Date format *ISO (International Standards Organization).
   <br>Example: 1997-04-25
   <br>Range of years: 0001-9999
   <br>Default separator: '-'
   <br>Length: 10 bytes **/
  public static final int FORMAT_ISO = 4;

  /** Date format *USA (IBM USA Standard).
   <br>Example: 04/25/1997
   <br>Range of years: 0001-9999
   <br>Default separator: '/'
   <br>Length: 10 bytes **/
  public static final int FORMAT_USA = 5;

  /** Date format *EUR (IBM European Standard).
   <br>Example: 25.04.1997
   <br>Range of years: 0001-9999
   <br>Default separator: '.'
   <br>Length: 10 bytes **/
  public static final int FORMAT_EUR = 6;

  /** Date format *JIS (Japan Industrial Standard).
   <br>Example: 1997-04-25
   <br>Range of years: 0001-9999
   <br>Default separator: '-'
   <br>Length: 10 bytes **/
  public static final int FORMAT_JIS = 7;

  // Externally defined date formats:

  /** Date format *CYMD (Century Year/Month/Day).
   <br>Example: 097/04/25
   <br>Range of years: 1900-2899
   <br>Default separator: '/'
   <br>Length: 9 bytes **/
  public static final int FORMAT_CYMD = 8;

  /** Date format *CMDY (Century Month/Day/Year).
   <br>Example: 004/25/97
   <br>Range of years: 1900-2899
   <br>Default separator: '/'
   <br>Length: 9 bytes **/
  public static final int FORMAT_CMDY = 9;

  /** Date format *CDMY (Century Day/Month/Year).
   <br>Example: 025/04/97
   <br>Range of years: 1900-2899
   <br>Default separator: '/'
   <br>Length: 9 bytes **/
  public static final int FORMAT_CDMY = 10;

  /** Date format *LONGJUL (Long Julian).
   <br>Example: 1997/115
   <br>Range of years: 0001-9999
   <br>Default separator: '/'
   <br>Length: 8 bytes **/
  public static final int FORMAT_LONGJUL = 11;


  /**
   Constructs an AS400Date object.
   The default format ({@link #FORMAT_ISO FORMAT_ISO}) and separator ('-') are used.
   **/
  public AS400Date()
  {
    this(FORMAT_ISO);
  }

  /**
   Constructs an AS400Date object.
   The specified format's default separator is used.
   @param format The date format.
   <br>Valid values are:
   <ul>
   <li>{@link #FORMAT_MDY FORMAT_MDY}
   <li>{@link #FORMAT_DMY FORMAT_DMY}
   <li>{@link #FORMAT_YMD FORMAT_YMD}
   <li>{@link #FORMAT_JUL FORMAT_JUL}
   <li>{@link #FORMAT_ISO FORMAT_ISO}
   <li>{@link #FORMAT_USA FORMAT_USA}
   <li>{@link #FORMAT_EUR FORMAT_EUR}
   <li>{@link #FORMAT_JIS FORMAT_JIS}
   <li>{@link #FORMAT_CYMD FORMAT_CYMD}
   <li>{@link #FORMAT_CMDY FORMAT_CMDY}
   <li>{@link #FORMAT_CDMY FORMAT_CDMY}
   <li>{@link #FORMAT_LONGJUL FORMAT_LONGJUL}
   **/
  public AS400Date(int format)
  {
    if (!isValidFormat(format)) {
      throw new ExtendedIllegalArgumentException("format ("+format+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    setFormat(format, defaultSeparatorFor(format));
  }

  // Overrides method of superclass.
  /**
   Returns a Java object representing the default value of the data type.
   @return A <tt>java.sql.Date</tt> object with a value of January 1, 1970 GMT.
   **/
  public Object getDefaultValue()
  {
    if (defaultValue_ == null) {
      defaultValue_ = new java.sql.Date(0L); // January 1, 1970
    }

    return defaultValue_;
  }


  // Overrides non-public method of superclass, making it public.
  /**
   Gets the format of this AS400Date object.
   @return format  The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_MDY FORMAT_MDY}
   <li>{@link #FORMAT_DMY FORMAT_DMY}
   <li>{@link #FORMAT_YMD FORMAT_YMD}
   <li>{@link #FORMAT_JUL FORMAT_JUL}
   <li>{@link #FORMAT_ISO FORMAT_ISO}
   <li>{@link #FORMAT_USA FORMAT_USA}
   <li>{@link #FORMAT_EUR FORMAT_EUR}
   <li>{@link #FORMAT_JIS FORMAT_JIS}
   <li>{@link #FORMAT_CYMD FORMAT_CYMD}
   <li>{@link #FORMAT_CMDY FORMAT_CMDY}
   <li>{@link #FORMAT_CDMY FORMAT_CDMY}
   <li>{@link #FORMAT_LONGJUL FORMAT_LONGJUL}
   </ul>
   **/
  public int getFormat()
  {
    return super.getFormat();
  }


  /**
   Gets the separator character of this AS400Date object.
   @return separator  The separator character.
   Possible values are:
   <ul>
   <li><tt>/</tt> (slash)</li>
   <li><tt>-</tt> (hyphen)</li>
   <li><tt>.</tt> (period)</li>
   <li><tt>,</tt> (comma)</li>
   <li><tt>&</tt> (ampersand)</li>
   </ul>
   @see #setFormat(int,char)
   **/
  public char getSeparator()
  {
    return super.getSeparator();
  }

  // Implements abstract method of superclass.
  /**
   Returns {@link AS400DataType#TYPE_DATE TYPE_DATE}.
   @return <tt>AS400DataType.TYPE_DATE</tt>.
   **/
  public int getInstanceType()
  {
    return AS400DataType.TYPE_DATE;
  }

  // Implements abstract method of superclass.
  /**
   Returns the Java class that corresponds with this data type.
   @return <tt>java.sql.Date.class</tt>.
   **/
  public Class getJavaType()
  {
    return java.sql.Date.class;
  }


  /**
   Sets the format of this AS400Date object.
   The specified format's default separator character is used.
   @param format  The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_MDY FORMAT_MDY}
   <li>{@link #FORMAT_DMY FORMAT_DMY}
   <li>{@link #FORMAT_YMD FORMAT_YMD}
   <li>{@link #FORMAT_JUL FORMAT_JUL}
   <li>{@link #FORMAT_ISO FORMAT_ISO}
   <li>{@link #FORMAT_USA FORMAT_USA}
   <li>{@link #FORMAT_EUR FORMAT_EUR}
   <li>{@link #FORMAT_JIS FORMAT_JIS}
   <li>{@link #FORMAT_CYMD FORMAT_CYMD}
   <li>{@link #FORMAT_CMDY FORMAT_CMDY}
   <li>{@link #FORMAT_CDMY FORMAT_CDMY}
   <li>{@link #FORMAT_LONGJUL FORMAT_LONGJUL}
   </ul>
   **/
  public void setFormat(int format)
  {
    super.setFormat(format, defaultSeparatorFor(format));
  }


  // Overrides non-public method of superclass, making it public.
  /**
   Sets the format of this AS400Date object.
   @param format The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_MDY FORMAT_MDY}
   <li>{@link #FORMAT_DMY FORMAT_DMY}
   <li>{@link #FORMAT_YMD FORMAT_YMD}
   <li>{@link #FORMAT_JUL FORMAT_JUL}
   <li>{@link #FORMAT_ISO FORMAT_ISO}
   <li>{@link #FORMAT_USA FORMAT_USA}
   <li>{@link #FORMAT_EUR FORMAT_EUR}
   <li>{@link #FORMAT_JIS FORMAT_JIS}
   <li>{@link #FORMAT_CYMD FORMAT_CYMD}
   <li>{@link #FORMAT_CMDY FORMAT_CMDY}
   <li>{@link #FORMAT_CDMY FORMAT_CDMY}
   <li>{@link #FORMAT_LONGJUL FORMAT_LONGJUL}
   </ul>
   @param separator  The separator character.
   Valid values are:
   <ul>
   <li><tt>/</tt> (slash)</li>
   <li><tt>-</tt> (hyphen)</li>
   <li><tt>.</tt> (period)</li>
   <li><tt>,</tt> (comma)</li>
   <li><tt>&</tt> (ampersand)</li>
   </ul>
   Refer to the IBM i programming reference to determine which separator characters are valid with each format.
   **/
  public void setFormat(int format, char separator)
  {
    super.setFormat(format, separator);
  }

  static boolean isYearWithinRange(int year, int format)
  {
    // Valid ranges for 'year' values:
    //     2-digit years (YMD,DMY,MDY,JUL):         1940 to 2039
    //     3-digit years (CYMD,CDMY,CMDY):          1900 to 2899
    //     4-digit years (ISO,USA,EUR,JIS,LONGJUL): 0001 to 9999
    switch (format)
    {
      case FORMAT_MDY:
      case FORMAT_DMY:
      case FORMAT_YMD:
      case FORMAT_JUL:
        if (year < 1940 || year > 2039) return false;
        else return true;

      case FORMAT_CYMD:
      case FORMAT_CMDY:
      case FORMAT_CDMY:
        if (year < 1900 || year > 2899) return false;
        else return true;

      case FORMAT_ISO:
      case FORMAT_USA:
      case FORMAT_EUR:
      case FORMAT_JIS:
      case FORMAT_LONGJUL:
        if (year < 1 || year > 9999) return false;
        else return true;

      default:  // should never happen
        throw new InternalErrorException(InternalErrorException.UNKNOWN, "Unrecognized format: " + format);
    }
  }

  // Overrides method of superclass.  This allows us to be more specific in the javadoc.
  /**
   Converts the specified Java object into IBM i format in the specified byte array.
   @param javaValue The object corresponding to the data type.  It must be an instance of {@link java.sql.Date java.sql.Date}.  Hours, minutes, seconds, and milliseconds are disregarded.
   @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   @return The number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value, int offset)
  {
    return super.toBytes(javaValue, as400Value, offset);
  }

  // Implements abstract method of superclass.
  /**
   Converts the specified IBM i data type to a Java object.
   @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
   @return A {@link java.sql.Date java.sql.Date} object corresponding to the data type.
   The reference time zone for the object is GMT.
   **/
  public Object toObject(byte[] as400Value, int offset)
  {
    try
    {
      String dateString = getCharConverter().byteArrayToString(as400Value, offset, getLength());
      if (DEBUG) System.out.println("AS400Date.toObject(): Returned 'date' string: |" + dateString + "|");

      // Some formats contain a century digit.
      // Deal with the 'century' digit, if one was returned in as400Value.
      Integer centuryDigit = parseCenturyDigit(dateString, getFormat());
      if (centuryDigit != null)
      {
        if (DEBUG) System.out.println("Century digit: " + centuryDigit.toString());
        dateString = dateString.substring(1);  // remove the century digit, so it won't confuse SimpleDateFormat
        if (DEBUG) System.out.println("Stripped date string: |" + dateString + "|");
      }
      else
      {
        // The formats MDY, DMY, YMD, and JUL represent the year as 2 digits.
        // For those formats, we need to deduce the century based on the 'yy' value.
        centuryDigit = disambiguateCentury(dateString, getFormat());
      }


      java.util.Date dateObj = getDateFormatter(centuryDigit).parse(dateString);
      return new java.sql.Date(dateObj.getTime());
    }
    catch (NumberFormatException e) {
      // Assume that the exception is because we got bad input.
      Trace.log(Trace.ERROR, e.getMessage(), as400Value);
      throw new ExtendedIllegalArgumentException("as400Value", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    catch (ParseException e) {
      // Assume that the exception is because we got bad input.
      Trace.log(Trace.ERROR, e.getMessage(), as400Value);
      throw new ExtendedIllegalArgumentException("as400Value", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }


  // Implements abstract method of superclass.
  /**
   Converts the specified Java object into a String representation that is consistent with the format of this data type.
   @param javaValue The object corresponding to the data type. This must be an instance of {@link java.sql.Date java.sql.Date}, and must be within the range specifiable by this data type.
   @return A String representation of the specified value, formatted appropriately for this data type.
   **/
  public String toString(Object javaValue)
  {
    java.sql.Date dateObj = (java.sql.Date)javaValue;  // allow this line to throw ClassCastException and NullPointerException

    // Verify that the 'year' value from the date is within the range of our format.
    int year, era;
    synchronized (this) {
      Calendar cal = getCalendar(dateObj);
      year = cal.get(Calendar.YEAR);
      era  = cal.get(Calendar.ERA);
    }
    if (era == 0) {  // we can't represent years BCE
      throw new ExtendedIllegalArgumentException("javaValue (era=0)", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (!isYearWithinRange(year, getFormat())) {
      Trace.log(Trace.ERROR, "Year " + year + " is outside the range of values for AS400Date format " + getFormat());
      throw new ExtendedIllegalArgumentException("javaValue (year=" + year + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    String dateString = getDateFormatter().format(dateObj);

    // Depending on the format, prepend a "century" digit if needed.
    dateString = addCenturyDigit(dateString, getFormat(), dateObj);
    if (DEBUG) System.out.println("DEBUG AS400Date.toString() for format " + getFormat() + ": " + dateString);
    return dateString;
  }


  // If the format includes a "century" digit, prepend the appropriate century value.
  String addCenturyDigit(String dateString, int format, java.sql.Date dateObj)
  {
    if (DEBUG) System.out.println("DEBUG AS400Date.addCenturyDigit() for format " + getFormat() + "; dateString: |" + dateString + "|");
    switch (format)
    {
      case FORMAT_CYMD:
      case FORMAT_CMDY:
      case FORMAT_CDMY:
        int year = getCalendar(dateObj).get(Calendar.YEAR);
        if (DEBUG) System.out.println("DEBUG AS400Date.addCenturyDigit() for format " + getFormat() + ": year=" + year);
        int century = (year/100) - 19;  // IBM i convention: Years '19xx' are in century '0'
        // Assume that the caller has verified that date is within our year range, and that the era is not BCE.
        if (DEBUG) System.out.println("DEBUG AS400Date.addCenturyDigit() for format " + getFormat() + ": century=" + century);
        return Integer.toString(century) + dateString;

      default:  // none of the above formats
        return dateString;  // nothing to prepend, so return string unaltered
    }
  }


  // Determines the century, if the specified format has only a 2-digit year and no century digit.
  // Returns null if the century is unambiguous in the specified format.
  static Integer disambiguateCentury(String dateString, int format)
  {
    int offsetToYear;  // offset within dateString, to the 'yy'
    switch (format)
    {
      case FORMAT_MDY:                                // mm/dd/yy
      case FORMAT_DMY:      offsetToYear = 6; break;  // dd/mm/yy

      case FORMAT_YMD:                                // yy/mm/dd
      case FORMAT_JUL:      offsetToYear = 0; break;  // yy/ddd

      default:
        return null;  // other formats don't have a 'century' digit
    }

    // Range of years representable in the above 2-digit year formats: 1940-2039
    int year, century;
    year = Integer.parseInt(dateString.substring(offsetToYear, offsetToYear+2));
    if (year < 40) century = 1;   // century 1 is years 2000-2099
    else           century = 0;   // century 0 is years 1900-1999

    return new Integer(century);
  }


  // Parses the leading 'century' digit, if the specified format contains one.
  // Returns null if the format has no century digit.
  static Integer parseCenturyDigit(String dateString, int format)
  {
    switch (format)
    {
      case FORMAT_CYMD:
      case FORMAT_CMDY:
      case FORMAT_CDMY:
        return Integer.valueOf(Character.toString(dateString.charAt(0)));

      default:
        return null;  // other formats don't have a 'century' digit
    }
  }

  // Implements abstract method of superclass.
  String patternFor(int format, char sep)
  {
    switch (format)
    {
      case FORMAT_MDY:      return "MM"+sep+"dd"+sep+"yy";
      case FORMAT_DMY:      return "dd"+sep+"MM"+sep+"yy";
      case FORMAT_YMD:      return "yy"+sep+"MM"+sep+"dd";
      case FORMAT_JUL:      return "yy"+sep+"DDD";

      case FORMAT_ISO:
      case FORMAT_JIS:      return "yyyy"+sep+"MM"+sep+"dd";
      case FORMAT_USA:      return "MM"+sep+"dd"+sep+"yyyy";
      case FORMAT_EUR:      return "dd"+sep+"MM"+sep+"yyyy";

      // For the following 3 formats, we deal with 'century' digit separately.
      // SimpleDateFormat doesn't have a "century digit" pattern.
      case FORMAT_CYMD:     return "yy"+sep+"MM"+sep+"dd";
      case FORMAT_CMDY:     return "MM"+sep+"dd"+sep+"yy";
      case FORMAT_CDMY:     return "dd"+sep+"MM"+sep+"yy";

      case FORMAT_LONGJUL:  return "yyyy"+sep+"DDD";

      default:  // should never happen
        throw new InternalErrorException(InternalErrorException.UNKNOWN, "Unrecognized format: " + format);
    }
  }

  // Implements abstract method of superclass.
  char defaultSeparatorFor(int format)
  {
    switch (format)
    {
      case FORMAT_MDY:
      case FORMAT_DMY:
      case FORMAT_YMD:
      case FORMAT_JUL:
      case FORMAT_USA:
      case FORMAT_CYMD:
      case FORMAT_CMDY:
      case FORMAT_CDMY:
      case FORMAT_LONGJUL:
        return '/';  // slash

      case FORMAT_ISO:
      case FORMAT_JIS:
        return '-';  // hyphen

      case FORMAT_EUR:
        return '.';  // period

      default:  // should never happen
        throw new InternalErrorException(InternalErrorException.UNKNOWN, "Unrecognized format: " + format);
    }
  }

  // Implements abstract method of superclass.
  boolean isValidFormat(int format)
  {
    if (format < FORMAT_MDY || format > FORMAT_LONGJUL) return false;
    else return true;
  }

  // Implements abstract method of superclass.
  boolean isValidSeparator(char separator, int format)
  {
    switch (separator)
    {
      case '/':                  // slash
        switch (format)
        {
          case FORMAT_MDY:
          case FORMAT_DMY:
          case FORMAT_YMD:
          case FORMAT_JUL:
          case FORMAT_USA:
          case FORMAT_CYMD:
          case FORMAT_CMDY:
          case FORMAT_CDMY:
          case FORMAT_LONGJUL:
            return true;
          default:
            return false;
        }

      case '-':                  // hyphen
        switch (format)
        {
          case FORMAT_MDY:
          case FORMAT_DMY:
          case FORMAT_YMD:
          case FORMAT_JUL:
          case FORMAT_ISO:
          case FORMAT_JIS:
          case FORMAT_CYMD:
          case FORMAT_CMDY:
          case FORMAT_CDMY:
          case FORMAT_LONGJUL:
            return true;
          default:
            return false;
        }

      case '.':                  // period
        switch (format)
        {
          case FORMAT_MDY:
          case FORMAT_DMY:
          case FORMAT_YMD:
          case FORMAT_JUL:
          case FORMAT_EUR:
          case FORMAT_CYMD:
          case FORMAT_CMDY:
          case FORMAT_CDMY:
          case FORMAT_LONGJUL:
            return true;
          default:
            return false;
        }

      case ',':                  // comma
      case '&':                  // ampersand
        switch (format)
        {
          case FORMAT_MDY:
          case FORMAT_DMY:
          case FORMAT_YMD:
          case FORMAT_JUL:
          case FORMAT_CYMD:
          case FORMAT_CMDY:
          case FORMAT_CDMY:
          case FORMAT_LONGJUL:
            return true;
          default:
            return false;
        }

      default:                   // none of the above separators
        return false;
    }
  }



  // Implements abstract method of superclass.
  int lengthFor(int format)
  {
    switch (format)
    {
      case FORMAT_MDY:
      case FORMAT_DMY:
      case FORMAT_YMD:
      case FORMAT_LONGJUL:
        return 8;   // field length is 8 bytes

      case FORMAT_JUL:
        return 6;   // field length is 6 bytes

      case FORMAT_ISO:
      case FORMAT_USA:
      case FORMAT_EUR:
      case FORMAT_JIS:
        return 10;  // field length is 10 bytes

      case FORMAT_CYMD:
      case FORMAT_CMDY:
      case FORMAT_CDMY:
        return 9;   // field length is 9 bytes

      default:  // should never happen
        throw new InternalErrorException(InternalErrorException.UNKNOWN, "Unrecognized format: " + format);
    }
  }

}
