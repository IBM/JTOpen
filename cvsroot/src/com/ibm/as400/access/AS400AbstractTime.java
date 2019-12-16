///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400AbstractTime.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2010-2010 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 An abstract base class for converters between IBM i date/time values and corresponding Java objects.
 **/
public abstract class AS400AbstractTime implements AS400DataType
{
  static final long serialVersionUID = 4L;
  static final boolean DEBUG = false;

  static final Locale LOCALE_DEFAULT = Locale.US;  // keep things simple
  static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT-0");

  // Separator characters.  Used by AS400Date and AS400Time.
  static final Character AMPERSAND = new Character('&');
  static final Character BLANK = new Character(' ');
  static final Character COLON = new Character(':');
  static final Character COMMA = new Character(',');
  static final Character HYPHEN = new Character('-');
  static final Character PERIOD = new Character('.');
  static final Character SLASH = new Character('/');

  // Standard XML-Schema format patterns for parsing dates and times.
  // For specifications, see http://www.w3.org/TR/xmlschema-2/
  static final String DATE_PATTERN_XSD = "yyyy-MM-dd";
  static final String TIME_PATTERN_XSD = "HH:mm:ss";
  static final String TIMESTAMP_PATTERN_XSD = "yyyy-MM-dd'T'HH:mm:ss";

  private int length_;  // number of bytes occupied by the IBM i value
  private transient GregorianCalendar calendar_;
  private transient SimpleDateFormat dateFormatter_;
  private transient CharConverter charConverter_;

  private static SimpleDateFormat dateFormatterXSD_;       // used by AS400Date
  private static SimpleDateFormat timeFormatterXSD_;       // used by AS400Time
  private static SimpleDateFormat timestampFormatterXSD_;  // used by AS400Timestamp

  // Map of 'century' digit values to Date objects that specify that start of each century.
  private transient java.util.Date[] centuryMap_;
  // Note: In our code for this class and its subclasses, we will fully qualify references to class java.util.Date and java.sql.Date, in order to eliminate any possible confusion between the two classes.

  private int format_;
  private Character separator_;
  private boolean separatorHasBeenSet_ = false;  // indicates whether separator was explicitly set by the application

  /**
   Constructs an AS400AbstractTime object.  Hide this constructor from applications.
   **/
  AS400AbstractTime()
  {
  }


  // Implements method of interface AS400DataType.
  /**
   Creates a new AS400AbstractTime object that is identical to the current instance.
   @return The new object.
   **/
  public Object clone()
  {
    try
    {
      return super.clone();  // Object.clone does not throw exception
    }
    catch (CloneNotSupportedException e)
    {
      Trace.log(Trace.ERROR, "Unexpected cloning error", e);
      throw new InternalErrorException(InternalErrorException.UNKNOWN);
    }
  }

  // Implements method of interface AS400DataType.
  /**
   Returns the byte length of the data type.
   @return The number of bytes in the IBM i representation of the data type.
   **/
  public int getByteLength()
  {
    return length_;
  }

  // Implements method of interface AS400DataType.
  /**
   Returns a Java object representing the default value of the data type.
   @return The default value of the data type.
   **/
  public abstract Object getDefaultValue();

  // Implements method of interface AS400DataType.
  /**
   Returns an integer constant representing the type of class that implements
   this interface. This is typically faster than using the instanceof operator, and may prove useful
   where code needs a primitive type for ease of calculation.
   Possible values for standard com.ibm.as400.access classes that implement this
   interface are provided as constants in this class. Note that any implementing class provided
   by a third party is not guaranteed to correctly return one of the pre-defined constants.
   @return The type of object implementing this interface.
   **/
  public abstract int getInstanceType();


  // Implements method of interface AS400DataType.
  /**
   Returns the Java class that corresponds with this data type.
   @return The Java class that corresponds with this data type.
   **/
  public abstract Class getJavaType();


  // Utility method used by AS400Timestamp.
  synchronized GregorianCalendar getCalendar()
  {
    if (calendar_ == null)
    {
      if (Trace.traceOn_) {
        Trace.log(Trace.DIAGNOSTIC, "AS400AbstractTime.getCalendar(): Setting internal timezone to " + TIMEZONE_GMT);
      }

      calendar_ = new GregorianCalendar(TIMEZONE_GMT, LOCALE_DEFAULT);
    }
    return calendar_;
  }


  // Utility method used by AS400Date and AS400Timestamp.
  synchronized GregorianCalendar getCalendar(java.util.Date date)
  {
    getCalendar().setTime(date);  // set the specified date into the Calendar object
    return calendar_;
  }

  // Utility method.
  private synchronized GregorianCalendar getCalendar(
                                             int year,
                                             int month,              // 0-based
                                             int dayOfMonth,         // 1-based
                                             int hoursIntoDay,       // 0-based
                                             int minutesIntoHour,    // 0-based
                                             int secondsIntoMinute)  // 0-based
  {
    getCalendar().set(year,               // year
                      month,              // month         (0-based)
                      dayOfMonth,         // day of month  (1-based)
                      hoursIntoDay,       // hour of day   (0-based)
                      minutesIntoHour,    // minute        (0-based)
                      secondsIntoMinute); // second        (0-based)
    return calendar_;
  }

  // Utility method used by subclasses.
  int getLength()
  {
    return length_;
  }

  /**
   Returns the format of this AS400AbstractTime object.
   Note: We don't make this method public here, since not all subclasses surface it to
   @return  The format for this object.
   **/
  int getFormat()
  {
    return format_;
  }


  // Utility method used by subclasses.
  Character getSeparator()
  {
    return separator_;
  }


  // Method needed by DateFieldDescription and TimeFieldDescription.
  synchronized void setSeparator(Character separator)
  {
    separator_ = separator;
    separatorHasBeenSet_ = true;
  }


  // Method needed by DateFieldDescription and TimeFieldDescription.
  /**
   Sets the format of this AS400AbstractTime object.
   Note: We don't make this method public here, since not all subclasses surface it to the user.

   @param format The format for this object.
   **/
  synchronized void setFormat(int format)
  {
    Character sep = ( separatorHasBeenSet_ ? separator_ : defaultSeparatorFor(format) );
    setFormat(format, sep);
  }


  /**
   Sets the format of this AS400AbstractTime object.
   Note: We don't make this method public here, since not all subclasses surface it to the user.

   @param format The format for this object.
   @param separator  The separator.
   Refer to the IBM i programming reference to determine which separators are valid with each format.
   **/
  synchronized void setFormat(int format, Character separator)
  {
    if (!isValidFormat(format)) {
      throw new ExtendedIllegalArgumentException("format (" + format + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    format_ = format;
    separator_ = separator;
    length_ = lengthFor(format_);

    // Discard any previously specified formatter.
    dateFormatter_ = null;
    // Don't create formatter until we actually need it.
  }


  // Implements method of interface AS400DataType.
  /**
   Converts the specified Java object to IBM i format.
   This method performs the same conversion as {@link #toBytes(Object,byte[],int) tobytes(javaValue,as400Value,0)}, except that <tt>as400Value</tt> is created dynamically and returned by this method.
   @param javaValue The object corresponding to the data type.
   @return The IBM i representation of the data type.
   **/
  public byte[] toBytes(Object javaValue)
  {
    byte[] byteVal = new byte[length_];
    toBytes(javaValue, byteVal, 0);
    return byteVal;
  }


  // Implements method of interface AS400DataType.
  /**
   Converts the specified Java object into IBM i format in the specified byte array.
   This method performs the same conversion as {@link #toBytes(Object,byte[],int) tobytes(javaValue,as400Value,0)}.

   @param javaValue The object corresponding to the data type.
   @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   @return Eight (8), the number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value)
  {
    return toBytes(javaValue, as400Value, 0);
  }

  /**
   Converts the specified Java object into IBM i format in the specified byte array.
   @param javaValue The object corresponding to the data type.
   @param as400Value The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
   @param offset The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
   @return Eight (8), the number of bytes in the IBM i representation of the data type.
   **/
  public int toBytes(Object javaValue, byte[] as400Value, int offset)
  {
    String dateString = toString(javaValue); // call the subclass-specific formatter

    try {
      getCharConverter().stringToByteArray(dateString, as400Value, offset);
    }
    catch (CharConversionException e) { // should never happen
      Trace.log(Trace.ERROR, e);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
    }
    return length_;
  }


  // Implements method of interface AS400DataType.
  /**
   Converts the specified IBM i data type to a Java object.
   This method performs the same conversion as {@link #toObject(byte[],int) toObject(as400Value,0)}.
   @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   @return A Java object corresponding to the data type.
   **/
  public Object toObject(byte[] as400Value)
  {
    return toObject(as400Value, 0);
  }

  // Implements method of interface AS400DataType.
  /**
   Converts the specified IBM i data type to a Java object.
   @param as400Value The array containing the data type in IBM i format.  The entire data type must be represented.
   @param offset The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
   @return A Java object corresponding to the data type.
   **/
  public abstract Object toObject(byte[] as400Value, int offset);


  /**
   Converts the specified Java object into a String representation that is consistent with the format of this data type.
   @param javaValue The object corresponding to the data type.
   @return A String representation of the specified value, formatted appropriately for this data type.
   **/
  public abstract String toString(Object javaValue);


  // Utility method used by subclasses.
  CharConverter getCharConverter()
  {
    try
    {
      if (charConverter_ == null) {
        charConverter_ = new CharConverter(37);  // Standard IBM i EBCDIC.
        // Design note: Technically, IBM i date/time fields are, by default, in the job CCSID; but since their characters are invariant, 37 should work.  So we'll keep it simple.
      }
      return charConverter_;
    }
    catch (UnsupportedEncodingException e) { // should never happen
      Trace.log(Trace.ERROR, e);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
    }
  }


  // Utility method used by subclasses.
  synchronized SimpleDateFormat getDateFormatter()
  {
    if (dateFormatter_ == null) {
      dateFormatter_ = new SimpleDateFormat(patternFor(format_, separator_));
      dateFormatter_.setTimeZone(TIMEZONE_GMT);
    }
    return dateFormatter_;
  }


  // Utility method used by AS400Date.
  synchronized SimpleDateFormat getDateFormatter(Integer centuryDigit)
  {
    if (centuryDigit == null) return getDateFormatter();
    else
    {
      getDateFormatter().set2DigitYearStart(getStartDateForCentury(centuryDigit.intValue()));
      return dateFormatter_;
    }

  }


  // Utility method used by AS400Date.
  static SimpleDateFormat getDateFormatterXSD()
  {
    if (dateFormatterXSD_ == null) {
      synchronized (AS400Date.class)
      {
        if (dateFormatterXSD_ == null) {
          dateFormatterXSD_ = new SimpleDateFormat(DATE_PATTERN_XSD);
          dateFormatterXSD_.setTimeZone(TIMEZONE_GMT);
        }
      }
    }
    return dateFormatterXSD_;
  }


  // Utility method used by AS400Time.
  static SimpleDateFormat getTimeFormatterXSD()
  {
    if (timeFormatterXSD_ == null) {
      synchronized (AS400Time.class)
      {
        if (timeFormatterXSD_ == null) {
          timeFormatterXSD_ = new SimpleDateFormat(TIME_PATTERN_XSD);
          timeFormatterXSD_.setTimeZone(TIMEZONE_GMT);
        }
      }
    }
    return timeFormatterXSD_;
  }


  // Utility method used by AS400Timestamp.
  static SimpleDateFormat getTimestampFormatterXSD()
  {
    if (timestampFormatterXSD_ == null) {
      synchronized (AS400Timestamp.class)
      {
        if (timestampFormatterXSD_ == null) {
          timestampFormatterXSD_ = new SimpleDateFormat(TIMESTAMP_PATTERN_XSD);
          // Note: We deal with "nanoseconds" elsewhere, in the AS400Timestamp class.
          timestampFormatterXSD_.setTimeZone(TIMEZONE_GMT);
        }
      }
    }
    return timestampFormatterXSD_;
  }


  // Returns a Date object representing, in the contextual time zone, 00:00:00 on January 1 of the first year in the specified century.
  // Note:  The century-numbering convention of IBM i "date" formats is as follows:
  //     Century '0' indicates years 1900-1999.
  //     Century '1' indicates years 2000-2099.
  //     ... and so on.
  private synchronized java.util.Date getStartDateForCentury(int century)
  {
    if (centuryMap_ == null) {
      centuryMap_ = new java.util.Date[10];
    }

    if (centuryMap_[century] == null)
    {
      centuryMap_[century] = getCalendar(100*(19+century),  // year (e.g. 1900)
                                         0,  // month == January (0-based)
                                         1,  // day of month     (1-based)
                                         0,  // hour of day      (0-based)
                                         0,  // minute           (0-based)
                                         0   // second           (0-based)

                                         ).getTime();
    }
    return centuryMap_[century];
  }

  abstract String patternFor(int format, Character separator);
  abstract Character defaultSeparatorFor(int format);
  abstract boolean isValidFormat(int format);
  abstract int lengthFor(int format);

}
