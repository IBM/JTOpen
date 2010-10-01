///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400Time.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.text.ParseException;
import java.util.Hashtable;

/**
 Provides a converter between a {@link java.sql.Time java.sql.Time} object and an IBM i <i>time</i> value such as "23:59:59" or "11:59 PM".
 In the IBM i programming reference, this type is referred to as the "<b>Time</b> Data Type", or DDS data type <tt>T</tt>.
 <p>
 An IBM i <i>time</i> value simply indicates an hour/minute/second within some (unspecified) 24-hour period, and does not indicate a contextual day, month, year, or time zone.  Internally, this class interprets all date- and time-related strings as relative to the GMT time zone.
 <p>
 Suggestion: To avoid confusion and unexpected results when crossing time zones:
 <br>Whenever creating or interpreting instances of {@link java.sql.Date java.sql.Date}, {@link java.sql.Time java.sql.Time}, or {@link java.sql.Timestamp java.sql.Timestamp}, always assume that the reference time zone for the object is GMT, and avoid using any deprecated methods.  If it is necessary to convert date/time values between GMT and other time zones, use methods of <tt>Calendar</tt>.  Rather than using <tt>toString()</tt> to display the value of a date/time object, use <tt>DateFormat.format()</tt> after specifying a GMT TimeZone.

 @see AS400Timestamp
 @see AS400Date
 **/
public class AS400Time extends AS400AbstractTime
{
  static final long serialVersionUID = 4L;

  private java.sql.Time defaultValue_;
  private static Hashtable formatsTable_;

  private static final int  SIZE = 8; // IBM i "time" values are 8 bytes long

  /** The minimum value representable by this date type.
   This value represents the time 00:00:00. **/
  public  static final java.sql.Time MIN_VALUE = new java.sql.Time(0L);

  private static final long MILLISECONDS_IN_A_DAY = 24*60*60*1000; // milliseconds in 24 hours

   /** The maximum value representable by this date type.
   This value represents the time 23:59:59.999.
   **/
  public  static final java.sql.Time MAX_VALUE = new java.sql.Time(MILLISECONDS_IN_A_DAY - 1);

  /** Format HMS: <i>hh:mm:ss</i>
   <br>Default separator: ':' **/
  public static final int FORMAT_HMS = 100;  // valid separators: { : . , & }

  /** Format ISO: <i>hh.mm.ss</i>
   <br>Default separator: '.' **/
  public static final int FORMAT_ISO = 101;  // valid separators: { . }

  /** Format USA: <i>hh:mm AM</i> or <i>hh:mm PM</i>
   <br>Default separator: ':'
   <br>Note: Unlike the other formats, this format has a granularity of minutes rather than seconds. **/
  public static final int FORMAT_USA = 102;  // valid separators: { : }

  /** Format EUR: <i>hh.mm.ss</i>
   <br>Default separator: '.' **/
  public static final int FORMAT_EUR = 103;  // valid separators: { . }

  /** Format JIS: <i>hh:mm:ss</i>
   <br>Default separator: ':' **/
  public static final int FORMAT_JIS = 104;  // valid separators: { : }


  /**
   Constructs an AS400Time object.
   The format is set to {@link #FORMAT_ISO FORMAT_ISO}, and the separator is '.'.
   **/
  public AS400Time()
  {
    // Note: According to the spec, the default internal format for time variables is *ISO.
    this(FORMAT_ISO);
  }


  /**
   Constructs an AS400Time object.
   The specified format's default separator is used.
   @param format  The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_HMS FORMAT_HMS}</li>
   <li>{@link #FORMAT_ISO FORMAT_ISO}</li>
   <li>{@link #FORMAT_USA FORMAT_USA}</li>
   <li>{@link #FORMAT_EUR FORMAT_EUR}</li>
   <li>{@link #FORMAT_JIS FORMAT_JIS}</li>
   </ul>
   **/
  public AS400Time(int format)
  {
    setFormat(format, defaultSeparatorFor(format));
  }


  /**
   Constructs an AS400Time object.
   The specified format's default separator is used.
   @param format  The format for this object.
   Valid values are:
   <ul>
    <li><tt>HMS</tt>
    <li><tt>ISO</tt>
    <li><tt>USA</tt>
    <li><tt>EUR</tt>
    <li><tt>JIS</tt>
   </ul>
   **/
  public AS400Time(String format)
  {
    int formatInt = toFormat(format);
    setFormat(formatInt, defaultSeparatorFor(formatInt));
  }


  /**
   Constructs an AS400Time object.
   @param format  The format for this object.
   Valid values are:
   <ul>
    <li><tt>HMS</tt>
    <li><tt>ISO</tt>
    <li><tt>USA</tt>
    <li><tt>EUR</tt>
    <li><tt>JIS</tt>
   </ul>
   @param separator  The separator character.
   **/
  public AS400Time(String format, char separator)
  {
    int formatInt = toFormat(format);
    setFormat(formatInt, separator);
  }


  // Overrides non-public method of superclass, making it public.
  /**
   Gets the format of this AS400Time object.
   @return format  The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_HMS FORMAT_HMS}</li>
   <li>{@link #FORMAT_ISO FORMAT_ISO}</li>
   <li>{@link #FORMAT_USA FORMAT_USA}</li>
   <li>{@link #FORMAT_EUR FORMAT_EUR}</li>
   <li>{@link #FORMAT_JIS FORMAT_JIS}</li>
   </ul>
   **/
  public int getFormat()
  {
    return super.getFormat();
  }


  /**
   Gets the separator character of this AS400Time object.
   @return separator  The separator character.
   Valid values are:
   <ul>
   <li><tt>:</tt> (colon)</li>
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


  /**
   Sets the format of this AS400Time object.
   The specified format's default separator is used.
   @param format  The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_HMS FORMAT_HMS}</li>
   <li>{@link #FORMAT_ISO FORMAT_ISO}</li>
   <li>{@link #FORMAT_USA FORMAT_USA}</li>
   <li>{@link #FORMAT_EUR FORMAT_EUR}</li>
   <li>{@link #FORMAT_JIS FORMAT_JIS}</li>
   </ul>
   **/
  public void setFormat(int format)
  {
    super.setFormat(format, defaultSeparatorFor(format));
  }


  // Method used by TimeFieldDescription.
  /**
   Sets the format of this AS400Time object.
   The specified format's default separator character is used.
   @param format  The format for this object, expressed as a string.
   Valid values are:
    HMS
    ISO
    USA
    EUR
    JIS
   **/
  void setFormat(String format)
  {
    int formatInt = toFormat(format);
    super.setFormat(formatInt);
  }


  // Method used by TimeFieldDescription.
  /**
   Sets the separator of this AS400Date object.
   @param separator  The separator character.
   **/
  void setSeparator(char separator)
  {
    super.setSeparator(separator);
  }


  // Overrides non-public method of superclass, making it public.
  /**
   Sets the format of this AS400Time object.
   @param format The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_HMS FORMAT_HMS}</li>
   <li>{@link #FORMAT_ISO FORMAT_ISO}</li>
   <li>{@link #FORMAT_USA FORMAT_USA}</li>
   <li>{@link #FORMAT_EUR FORMAT_EUR}</li>
   <li>{@link #FORMAT_JIS FORMAT_JIS}</li>
   </ul>
   @param separator  The separator character.
   Valid values are:
   <ul>
   <li><tt>:</tt> (colon)</li>
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

  private static synchronized Hashtable getFormatsTable()
  {
    if (formatsTable_ == null)
    {
      formatsTable_ = new Hashtable(12);
      formatsTable_.put("HMS",     new Integer(FORMAT_HMS));
      formatsTable_.put("ISO",     new Integer(FORMAT_ISO));
      formatsTable_.put("USA",     new Integer(FORMAT_USA));
      formatsTable_.put("EUR",     new Integer(FORMAT_EUR));
      formatsTable_.put("JIS",     new Integer(FORMAT_JIS));
    }
    return formatsTable_;
  }

  // Returns the corresponding integer format value for the string representation of a format.
  static int toFormat(String format)
  {
    // Assume the caller has verified that the argument is non-null.

    Integer formatInt = (Integer)getFormatsTable().get(format.trim().toUpperCase());

    if (formatInt == null) {
      throw new ExtendedIllegalArgumentException("format ("+format+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    return formatInt.intValue();
  }


  // Overrides method of superclass.
  /**
   Returns a Java object representing the default value of the data type.
   @return A {@link java.sql.Time java.sql.Time} object representing time 00:00:00 GMT (on January 1, 1970).
   **/
  public Object getDefaultValue()
  {
    if (defaultValue_ == null) {
      defaultValue_ = new java.sql.Time(0L); // 00:00:00 GMT
    }

    return defaultValue_;
  }

  // Implements abstract method of superclass.
  /**
   Returns {@link AS400DataType#TYPE_TIME TYPE_TIME}.
   @return <tt>AS400DataType.TYPE_TIME</tt>.
   **/
  public int getInstanceType()
  {
    return AS400DataType.TYPE_TIME;
  }

  // Implements abstract method of superclass.
  /**
   Returns the Java class that corresponds with this data type.
   @return <tt>java.sql.Time.class</tt>.
   **/
  public Class getJavaType()
  {
    return java.sql.Time.class;
  }


  // Overrides method of superclass.  This allows us to be more specific in the javadoc.
  /**
   Converts the specified Java object into IBM i format in the specified byte array.
   @param javaValue The object corresponding to the data type.  It must be an instance of {@link java.sql.Time java.sql.Time}.  The range of valid values is {@link #MIN_VALUE MIN_VALUE} through {@link #MAX_VALUE MAX_VALUE}.  Year, month, and date are disregarded.
   @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   @return Eight (8), the number of bytes in the IBM i representation of the data type.
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
   @return a {@link java.sql.Time java.sql.Time} object, representing the number of milliseconds into the day.
   The reference time zone for the object is GMT.
   **/
  public Object toObject(byte[] as400Value, int offset)
  {
    java.sql.Time timeObj = null;
    try
    {
      String timeString = getCharConverter().byteArrayToString(as400Value, offset, getLength());
      if (DEBUG) System.out.println("DEBUG AS400Time.toObject(): timeString == |" + timeString + "|");

      // Compose the String representation into a Date object.
      java.util.Date dateObj = getDateFormatter().parse(timeString);
      timeObj = new java.sql.Time(dateObj.getTime()); // argument is "milliseconds into day"
    }
    catch (ParseException e) {
      // Assume that the exception is because we got bad input.
      Trace.log(Trace.ERROR, e.getMessage(), as400Value);
      throw new ExtendedIllegalArgumentException("as400Value", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    return timeObj;
  }


  // Implements abstract method of superclass.
  /**
   Converts the specified Java object into a String representation that is consistent with the format of this data type.
   @param javaValue The object corresponding to the data type. This must be an instance of {@link java.sql.Time java.sql.Time}, and must be within the range representable by this data type.  Any timezone context is disregarded.
   @return A String representation of the specified value, formatted appropriately for this data type.
   **/
  public String toString(Object javaValue)
  {
    if (javaValue == null) throw new NullPointerException("javaValue");
    java.sql.Time timeObj;
    try { timeObj = (java.sql.Time)javaValue; }
    catch (ClassCastException e) {
      Trace.log(Trace.ERROR, "javaValue is of type " + javaValue.getClass().getName());
      throw e;
    }

    return getDateFormatter().format(timeObj);
  }

  // Implements abstract method of superclass.
  String patternFor(int format, char sep)
  {
    switch (format)
    {
      case FORMAT_USA:
        return "hh" + sep + "mm a";  // hh:mm AM <or> hh:mm PM

      default:
        return "HH" + sep + "mm" + sep + "ss";
    }
  }

  // Implements abstract method of superclass.
  char defaultSeparatorFor(int format)
  {
    if (!isValidFormat(format)) {
      throw new ExtendedIllegalArgumentException("format ("+format+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    switch (format)
    {
      case FORMAT_HMS:
      case FORMAT_USA:
      case FORMAT_JIS:
        return ':';  // colon

      case FORMAT_ISO:
      case FORMAT_EUR:
        return '.';  // period

      default:  // none of the above formats
        // Should never happen.
        throw new InternalErrorException(InternalErrorException.UNKNOWN, "Unrecognized format: " + format);
    }
  }

  // Implements abstract method of superclass.
  boolean isValidFormat(int format)
  {
    if (format < FORMAT_HMS || format > FORMAT_JIS) return false;
    else return true;
  }

  // Implements abstract method of superclass.
  boolean isValidSeparator(char separator, int format)
  {
    switch (separator)
    {
      case ':':                  // colon
        switch (format)
        {
          case FORMAT_HMS:
          case FORMAT_USA:
          case FORMAT_JIS:
            return true;
          default:
            return false;
        }

      case '.':                  // period
        switch (format)
        {
          case FORMAT_HMS:
          case FORMAT_ISO:
          case FORMAT_EUR:
            return true;
          default:
            return false;
        }

      case ',':                  // comma
      case '&':                  // ampersand
        switch (format)
        {
          case FORMAT_HMS:
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
    return SIZE;
  }


}
