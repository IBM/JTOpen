///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Timestamp.java
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
 Provides a converter between a {@link java.sql.Timestamp java.sql.Timestamp} object and an IBM i <i>timestamp</i> value such as "1997-12-31-23.59.59.999999".
 In the IBM i programming reference, this type is referred to as the "<b>Timestamp</b> Data Type".
 <p>
 The minimum value for an IBM i timestamp is <tt>0001-01-01-00.00.00.000000</tt>, and the maximum value is <tt>9999-12-31-24.00.00.000000</tt>.
 <p>
 An IBM i <i>timestamp</i> value simply indicates a year/month/day/hour/minute/second/microsecond, and does not indicate a contextual time zone.  Internally, this class interprets all date- and time-related strings as relative to the GMT time zone.
 <p>
 Suggestion: To avoid confusion and unexpected results when crossing time zones:
 <br>Whenever creating or interpreting instances of {@link java.sql.Date java.sql.Date}, {@link java.sql.Time java.sql.Time}, or {@link java.sql.Timestamp java.sql.Timestamp}, always assume that the reference time zone for the object is GMT, and avoid using any deprecated methods.  If it is necessary to convert date/time values between GMT and other time zones, use methods of <tt>Calendar</tt>.  Rather than using <tt>toString()</tt> to display the value of a date/time object, use <tt>DateFormat.format()</tt> after specifying a GMT TimeZone.

 @see AS400Date
 @see AS400Time
 **/
public class AS400Timestamp extends AS400AbstractTime
{
  private java.sql.Timestamp defaultValue_;

  // Timestamp format.  (This is the only standard format for IBM i "timestamp" fields.)
  // Example: 1997-04-25-23.59.59.999999
  // Length: 26 bytes


  // Design note: According to the IBM i datatype spec:
  // "Microseconds (.mmmmmm) are optional for timestamp literals and if not provided will be padded on the right with zeros. Leading zeros are required for all timestamp data."
  // For simplicity, we will assume that the "timestamp" fields encountered by the Toolbox, will always occupy exactly 26 bytes on the system, and will always specify microseconds.


  /**
   Constructs an AS400Timestamp object.
   **/
  public AS400Timestamp()
  {
    setFormat(0, '-');  // this data type has only one format
  }


  // Overrides method of superclass.
  /**
   Returns a Java object representing the default value of the data type.
   @return a <tt>java.sql.Timestamp</tt> object with a value of January 1, 1970, 00:00:00.000000 GMT
   **/
  public Object getDefaultValue()
  {
    // Design note: According to the IBM i datatype spec:
    // "The default initialization value for a timestamp is midnight of January 1, 0001 (0001-01-01-00.00.00.000000)."
    // However, for simplicity, we will stay consistent with our other "date" classes on the default value, until/unless we get a requirement to do otherwise.

    if (defaultValue_ == null) {
      defaultValue_ = new java.sql.Timestamp(0L); // January 1, 1970, 00:00:00.000000 GMT
    }

    return defaultValue_;
  }


  // Implements abstract method of superclass.
  /**
   Returns {@link AS400DataType#TYPE_TIMESTAMP TYPE_TIMESTAMP}.
   @return <tt>AS400DataType.TYPE_TIMESTAMP</tt>.
   **/
  public int getInstanceType()
  {
    return AS400DataType.TYPE_TIMESTAMP;
  }

  // Implements abstract method of superclass.
  /**
   Returns the Java class that corresponds with this data type.
   @return <tt>java.sql.Timestamp.class</tt>.
   **/
  public Class getJavaType()
  {
    return java.sql.Timestamp.class;
  }


  // Overrides method of superclass.  This allows us to be more specific in the javadoc.
  /**
   Converts the specified Java object into IBM i format in the specified byte array.
   @param javaValue The object corresponding to the data type.  It must be an instance of {@link java.sql.Timestamp java.sql.Timestamp}.
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
   @return a {@link java.sql.Timestamp java.sql.Timestamp} object corresponding to the data type.
   The reference time zone for the object is GMT.
   **/
  public Object toObject(byte[] as400Value, int offset)
  {
    try
    {
      String dateString = getCharConverter().byteArrayToString(as400Value, offset, getLength());
      if (DEBUG) System.out.println("AS400Timestamp.toObject(): Date string: |" + dateString + "|");

      // Our SimpleDateFormat formatter doesn't handle microseconds.
      // Strip out the fractional seconds before parsing.
      // The IBM i "timestamp" format is:  yyyy-mm-dd-hh.mm.ss.mmmmmm
      java.util.Date truncatedDateObj = getDateFormatter().parse(dateString.substring(0,19)); // ignore microseconds

      // Now add the microseconds back in.
      int micros = Integer.parseInt(dateString.substring(20));  // microseconds
      java.sql.Timestamp dateObj = new java.sql.Timestamp(truncatedDateObj.getTime());
      dateObj.setNanos(1000*micros); // nanoseconds == 1000*microseconds
      if (DEBUG) System.out.println("DEBUG AS400Timestamp.toObject(): dateString == |" + dateString + "|; nanoseconds == " + 1000*micros);
      return dateObj;
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
   @param javaValue The object corresponding to the data type. This must be an instance of {@link java.sql.Timestamp java.sql.Timestamp}, and must be within the range specifiable by this data type.
   @return A String representation of the specified value, formatted appropriately for this data type.
   @throws ExtendedIllegalArgumentException if the specified date is outside of the range representable by this data type.
   **/
  public String toString(Object javaValue)
  {
    java.sql.Timestamp timeObj = (java.sql.Timestamp)javaValue;  // allow this line to throw ClassCastException and NullPointerException

    // Verify that the 'year' value from the date is within the range of our format.

    int year, era;
    synchronized (this) {
      Calendar cal = getCalendar(timeObj);
      year = cal.get(Calendar.YEAR);
      era = cal.get(Calendar.ERA);
    }
    if (year < 1 || year > 9999) {
      throw new ExtendedIllegalArgumentException("javaValue (year=" + year + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    if (era == 0) {  // we can't represent years BCE
      throw new ExtendedIllegalArgumentException("javaValue (era=0)", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    String micros = to6Digits(timeObj.getNanos()/1000); // microseconds
    return ( getDateFormatter().format(timeObj) + "." + micros );  // append microseconds
  }

  // Implements abstract method of superclass.
  String patternFor(int format, char sep)
  {
    // SimpleDateFormat has a "milliseconds" pattern, but no "microseconds" or "nanoseconds" pattern.
    // So to generate a pattern consumable by SimpleDateFormat, we omit the fractional seconds entirely here.  We re-append them elsewhere in the code.
    return "yyyy-MM-dd-HH.mm.ss";
  }

  // Implements abstract method of superclass.
  char defaultSeparatorFor(int format)
  {
    return '-';
  }

  // Implements abstract method of superclass.
  boolean isValidFormat(int format)
  {
    return true;
  }

  // Implements abstract method of superclass.
  boolean isValidSeparator(char separator, int format)
  {
    return true;
  }



  // Implements abstract method of superclass.
  int lengthFor(int format)
  {
    return 26;  // all IBM i "timestamp" values occupy exactly 26 bytes
  }


  // Utility method.
  // Creates a 6-digit decimal string to represent an integer, prepending 0's as needed.  The value must be in the range 0 - 999999.
  static final String to6Digits(int value)
  {
    if (value < 0 || value > 999999) {
      throw new InternalErrorException(InternalErrorException.UNKNOWN, "to6Digits("+value+")");
    }
    StringBuffer buf = new StringBuffer(Integer.toString(value));
    int zerosToPrepend = 6 - buf.length();
    for (int i=0; i<zerosToPrepend; i++) {
      buf.insert(0, '0');
    }
    return buf.toString();
  }

}
