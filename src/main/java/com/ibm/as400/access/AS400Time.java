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
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.TimeZone;

/**
 Provides a converter between a {@link java.sql.Time java.sql.Time} object and an IBM i <i>time</i> 
 value such as "23:59:59" or "11:59 PM".
 In the IBM i programming reference, this type is referred to as the "<b>Time</b> Data Type", 
 or DDS data type <tt>T</tt>.
 <p>
 An IBM i <i>time</i> value simply indicates an hour/minute/second within some (unspecified) 
 24-hour period, and does not indicate a contextual day, month, year, or time zone.  
 Internally, this class interprets all date- and time-related strings as relative to the 
 server's  time zone.
 <p>
 Suggestion: To avoid confusion and unexpected results when crossing time zones:
 <br>Whenever creating or interpreting instances of {@link java.sql.Date java.sql.Date}, 
 {@link java.sql.Time java.sql.Time}, or {@link java.sql.Timestamp java.sql.Timestamp}, 
 always assume that the reference time zone for the object is the server's time zone, and 
 avoid using any deprecated methods.  If it is necessary to convert date/time values 
 between the server's time zone and other time zones, use methods of <tt>Calendar</tt>.  
 Rather than using <tt>toString()</tt> to display the value of a date/time object, 
 use <tt>DateFormat.format()</tt> after specifying the server timezone.
 
 For example:
 <code>
 import java.text.SimpleDateFormat;
 java.sql.Time time1;  // value to be generated by AS400Time
 SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
 // Set the formatter's time zone to the server timezone.
 formatter.setTimeZone(as400.getTimeZone());
 ...
 System.out.println("Time value: " + formatter.format(time1));
 </code>

 @see AS400Timestamp
 @see AS400Date
 **/
public class AS400Time extends AS400AbstractTime
{
  static final long serialVersionUID = 4L;

  private java.sql.Time defaultValue_;
  private static Hashtable formatsMap_;

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


  // Note to maintenance programmer: Update these values when adding new formats.
  private static final int FORMAT_RANGE_MINIMUM = FORMAT_HMS;
  private static final int FORMAT_RANGE_MAXIMUM = FORMAT_JIS;



  /**
   Constructs an AS400Time object.  This uses the GMT time zone. 
   The timezone is used when converting from a string to a "Time" object. 
   The time object will use the timezone of the current JVM. 
   Format {@link #FORMAT_ISO FORMAT_ISO} and separator '.' are used.
   **/
  public AS400Time()
  {
    // Note: The default internal format for IBM i time variables is *ISO.
    this(FORMAT_ISO);
  }

  /**
  Constructs an AS400Time object.
  Format {@link #FORMAT_ISO FORMAT_ISO} and separator '.' are used.
   * @param timeZone 
  **/
 public AS400Time(TimeZone timeZone)
 {
   // Note: The default internal format for IBM i time variables is *ISO.
   this(timeZone, FORMAT_ISO);
 }

 
 /**
 Constructs an AS400Time object.
 The specified format's default separator is used.
 The default GMT timezone is used 
 @param format  The format for this object.
 For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
 **/
public AS400Time(int format)
{
  super(); 
  setFormat(format, defaultSeparatorFor(format));
}




  /**
   Constructs an AS400Time object.
   The specified format's default separator is used.
   * @param timeZone 
   @param format  The format for this object.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   **/
  public AS400Time(TimeZone timeZone, int format)
  {
    super(timeZone); 
    setFormat(format, defaultSeparatorFor(format));
  }

  /**
  Constructs an AS400Time object.
  The specified format's default separator is used.
  The default GMT time zone will be used. 
  @param format  The format for this object.
  Valid values are:
  <ul>
  <li>{@link #FORMAT_HMS FORMAT_HMS}
  <li>{@link #FORMAT_ISO FORMAT_ISO}
  <li>{@link #FORMAT_USA FORMAT_USA}
  <li>{@link #FORMAT_EUR FORMAT_EUR}
  <li>{@link #FORMAT_JIS FORMAT_JIS}
  </ul>
  @param separator  The separator character.
  Valid values are:
  <ul>
  <li>' ' <i>(blank)</i>
  <li>':' <i>(colon)</i>
  <li>'.' <i>(period)</i>
  <li>',' <i>(comma)</i>
  <li>'&amp;' <i>(ampersand)</i>
  <li>(null)
  </ul>
  A null value indicates "no separator".
  Refer to the IBM i programming reference to determine which separator characters are valid with each format.
  **/
 public AS400Time(int format, Character separator)
 {
   super(); 
   setFormat(format, separator);
 }


  
  /**
   Constructs an AS400Time object.
   The specified format's default separator is used.
   * @param timeZone 
   @param format  The format for this object.
   Valid values are:
   <ul>
   <li>{@link #FORMAT_HMS FORMAT_HMS}
   <li>{@link #FORMAT_ISO FORMAT_ISO}
   <li>{@link #FORMAT_USA FORMAT_USA}
   <li>{@link #FORMAT_EUR FORMAT_EUR}
   <li>{@link #FORMAT_JIS FORMAT_JIS}
   </ul>
   @param separator  The separator character.
   Valid values are:
   <ul>
   <li>' ' <i>(blank)</i>
   <li>':' <i>(colon)</i>
   <li>'.' <i>(period)</i>
   <li>',' <i>(comma)</i>
   <li>'&amp;' <i>(ampersand)</i>
   <li>(null)
   </ul>
   A null value indicates "no separator".
   Refer to the IBM i programming reference to determine which separator characters are valid with each format.
   **/
  public AS400Time(TimeZone timeZone, int format, Character separator)
  {
    super(timeZone); 
    setFormat(format, separator);
  }


  // Overrides non-public method of superclass, making it public.
  /**
   Gets the format of this AS400Time object.
   @return format  The format for this object.
   For a list of possible values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   **/
  public int getFormat()
  {
    return super.getFormat();
  }


  /**
   Gets the separator character of this AS400Time object.
   @return separator  The separator character.
   For a list of possible values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   If the format contains no separators, null is returned.
   @see #setFormat(int,Character)
   **/
  public Character getSeparator()
  {
    return super.getSeparator();
  }


  /**
   Sets the format of this AS400Time object.
   The specified format's default separator is used.
   @param format  The format for this object.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
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
   For a list of valid values, refer to {@link #toFormat(String) toFormat(String)}.
   **/
  void setFormat(String format)
  {
    super.setFormat(toFormat(format));
  }


  // Method used by TimeFieldDescription.
  /**
   Sets the separator character of this AS400Time object.
   @param separator  The separator character.
   **/
  void setSeparator(Character separator)
  {
    super.setSeparator(separator);
  }


  // Overrides non-public method of superclass, making it public.
  /**
   Sets the format of this AS400Time object.
   @param format The format for this object.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   @param separator  The separator character.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   A null value indicates "no separator".
   Refer to the IBM i programming reference to determine which separator characters are valid with each format.
   **/
  public void setFormat(int format, Character separator)
  {
     super.setFormat(format, separator);
  }


  /**
   Sets the format of this AS400Time object.
   @param format The format for this object.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   @param separator  The separator character.
   @deprecated Use {@link #setFormat(int,Character) setFormat(int,Character)} instead.
   **/
  public void setFormat(int format, char separator)
  {
     super.setFormat(format, Character.valueOf(separator));
  }

  private static Hashtable getFormatsMap()
  {
    if (formatsMap_ == null)
    {
      synchronized (AS400Time.class)
      {
        if (formatsMap_ == null)
        {
          formatsMap_ = new Hashtable(12);
          formatsMap_.put("HMS",     Integer.valueOf(FORMAT_HMS));
          formatsMap_.put("ISO",     Integer.valueOf(FORMAT_ISO));
          formatsMap_.put("USA",     Integer.valueOf(FORMAT_USA));
          formatsMap_.put("EUR",     Integer.valueOf(FORMAT_EUR));
          formatsMap_.put("JIS",     Integer.valueOf(FORMAT_JIS));
        }
      }
    }
    return formatsMap_;
  }

  /**
   Returns the integer format value that corresponds to specified format name.
   If null is specified, the default format (FORMAT_ISO) is returned.
   This method is provided for use by the PCML infrastructure.
   @param formatName  The format name.
   Valid values are:
   <ul>
    <li>HMS
    <li>ISO
    <li>USA
    <li>EUR
    <li>JIS
   </ul>
   @return the format value.  For example, if formatName is "ISO", then {@link #FORMAT_ISO FORMAT_ISO} is returned.
   **/
  public static int toFormat(String formatName)
  {
    if (formatName == null || formatName.length() == 0) {
      if (Trace.traceOn_) {
        Trace.log(Trace.DIAGNOSTIC, "AS400Time.toFormat("+formatName+"): Returning default time format.");
      }
      return FORMAT_ISO;
    }

    Integer formatInt = (Integer)getFormatsMap().get(formatName.trim().toUpperCase());

    if (formatInt == null) {
      throw new ExtendedIllegalArgumentException("format ("+formatName+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
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
   @param javaValue The object corresponding to the data type.  It must be an instance of {@link java.sql.Time java.sql.Time}.  
   The range of valid values is {@link #MIN_VALUE MIN_VALUE} through {@link #MAX_VALUE MAX_VALUE}.  
   Year, month, day-of-month, and fractional seconds are disregarded.
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
   The reference time zone for the object is the timezone specified on the constructor. 
   **/
  public Object toObject(byte[] as400Value, int offset)
  {
    if (as400Value == null) throw new NullPointerException("as400Value");
    String timeString = getCharConverter().byteArrayToString(as400Value, offset, getLength());
    // Parse the string, and create a java.sql.Time object.
    return parse(timeString);
  }




  // Implements abstract method of superclass.
  /**
   Converts the specified Java object into a String representation that is consistent with the format of this data type.
   @param javaValue The object corresponding to the data type. This must be an instance of {@link java.sql.Time java.sql.Time}, 
   and must be within the range representable by this data type.  Any timezone context is disregarded.
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

  /**
   Converts a string representation of a time, to a Java object.
   @param source A time value expressed as a string in the format specified for this AS400Time object.
   @return A {@link java.sql.Time java.sql.Time} object representing the specified time.
  The reference timezone is the timezone specified for the object.
   **/
  public java.sql.Time parse(String source)
  {
    if (source == null) throw new NullPointerException("source");
    try
    {
      SimpleDateFormat formatter = getDateFormatter(); 
      java.util.Date dateObj = formatter.parse(source);
      long milliseconds = dateObj.getTime(); 
      java.sql.Time time = new java.sql.Time(milliseconds); // argument is "milliseconds into day" 
      // Convert to the base time type. 
      return time; 
    }
    catch (Exception e) {
      // Assume that the exception is because we got bad input.
      Trace.log(Trace.ERROR, e.getMessage(), source);
      Trace.log(Trace.ERROR, "Time string is expected to be in format: " + patternFor(getFormat(), getSeparator()));
      throw new ExtendedIllegalArgumentException("source ("+source+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }

  /**
  Converts the specified HMS representation of a time, to a Java object.
  This method is provided for use by the PCML infrastructure;
  in particular, when parsing 'init=' values for 'time' data elements.
  This method assumes that the reference timezone is GMT
  @param source A time value expressed as a string in format <tt>HH:mm:ss</tt>.
  @return A {@link java.sql.Time java.sql.Time} object representing the specified time.
  **/
 public static java.sql.Time parseXsdString(String source)
 {
    return parseXsdString(source, AS400AbstractTime.TIMEZONE_GMT); 
 }
  
  /**
   Converts the specified HMS representation of a time, to a Java object.
   This method is provided for use by the PCML infrastructure;
   in particular, when parsing 'init=' values for 'time' data elements.
   @param source A time value expressed as a string in format <tt>HH:mm:ss</tt>.
   @param timeZone The time zone used to interpret the value. 
   @return A {@link java.sql.Time java.sql.Time} object representing the specified time.
   **/
  public static java.sql.Time parseXsdString(String source, TimeZone timeZone)
  {
    if (source == null) throw new NullPointerException("source");
    try
    {
      java.util.Date simpleDateObj = getTimeFormatterXSD(timeZone).parse(source);
      long milliseconds = simpleDateObj.getTime(); 
      java.sql.Time returnTime = new java.sql.Time(milliseconds);  
      return returnTime; 
    }
    catch (ParseException e) {
      // Assume that the exception is because we got bad input.
      Trace.log(Trace.ERROR, e.getMessage(), source);
      Trace.log(Trace.ERROR, "Value is expected to be in standard XML Schema 'time' format: " + TIME_PATTERN_XSD);
      throw new ExtendedIllegalArgumentException("source ("+source+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
  }

 /**
   Converts the specified Java object into a String representation that is consistent with the format of this data type.
   This method is provided for use by the PCML infrastructure.
   This version uses the GMT timezone. 
   @param javaValue The object corresponding to the data type. This must be an instance of {@link java.sql.Time java.sql.Time}, and must be within the range specifiable by this data type.
   @return The time expressed as a string in format <tt>HH:mm:ss</tt>.
   **/
  public static String toXsdString(Object javaValue)
  {
    return toXsdString(javaValue, TIMEZONE_GMT ); 
  } 

  /**
   Converts the specified Java object into a String representation that is consistent with the format of this data type.
   This method is provided for use by the PCML infrastructure.
   @param javaValue The object corresponding to the data type. This must be an instance of 
   {@link java.sql.Time java.sql.Time}, and must be within the range specifiable by this data type.
   @param timeZone  The time zone used to interpret the value. 
   @return The time expressed as a string in format <tt>HH:mm:ss</tt>.
   **/
  public static String toXsdString(Object javaValue, TimeZone timeZone)
  {
    if (javaValue == null) throw new NullPointerException("javaValue");
    java.sql.Time timeObj;
    try { timeObj = (java.sql.Time)javaValue; }
    catch (ClassCastException e) {
      Trace.log(Trace.ERROR, "javaValue is of type " + javaValue.getClass().getName());
      throw e;
    }

    return getTimeFormatterXSD(timeZone).format(timeObj);
  }

  // Implements abstract method of superclass.
  String patternFor(int format, Character separator)
  {
    String sep = ( separator == null ? "" : separator.toString());
    switch (format)
    {
      case FORMAT_USA:
        String sep2 = ( sep.equals("") ? "" : " " );
        return "hh" + sep + "mm" + sep2 + "a";  // hh:mm AM <or> hh:mm PM

      default:
        return "HH" + sep + "mm" + sep + "ss";
    }
  }

  // Implements abstract method of superclass.
  Character defaultSeparatorFor(int format)
  {
    if (!isValidFormat(format)) {
      throw new ExtendedIllegalArgumentException("format ("+format+")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    switch (format)
    {
      case FORMAT_HMS:
      case FORMAT_USA:
      case FORMAT_JIS:
        return COLON;  // ':'

      case FORMAT_ISO:
      case FORMAT_EUR:
        return PERIOD;  // '.'

      default:  // none of the above formats
        // Should never happen.
        throw new InternalErrorException(InternalErrorException.UNKNOWN, "Unrecognized format: " + format, null);
    }
  }

  // Implements abstract method of superclass.
  boolean isValidFormat(int format)
  {
    return validateFormat(format);
  }


  /**
   Validates the specified format value.
   This method is provided for use by the PCML infrastructure.
   @param format The format.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   @return true if the format is valid; false otherwise.
   **/
  public static boolean validateFormat(int format)
  {
    if (format < FORMAT_RANGE_MINIMUM || format > FORMAT_RANGE_MAXIMUM) return false;
    else return true;
  }

  /**
   Returns the number of bytes occupied on the IBM i system by a field of this type.
   This method is provided for use by the PCML infrastructure.
   @param format The format.  This argument is ignored.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   @param separator The separator character.  This argument is ignored.
   For a list of valid values, refer to {@link #AS400Time(int,Character) AS400Time(int,Character)}.
   @return the number of bytes occupied.
   **/
  public static int getByteLength(int format, Character separator)
  {
    if (separator == null) return 6;  // field size (without separators) is 6 bytes

    else return 8;                    // field size (with separators) is 8 bytes
  }

  // Implements abstract method of superclass.
  int lengthFor(int format)
  {
    return getByteLength(format, getSeparator());
  }


}
