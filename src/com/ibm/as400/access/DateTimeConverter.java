///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: DateTimeConverter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
// 2008-05-21 @A1 Changes for *CURRENT when returning Date objects.  Adjust the
//            AS400 system time to the local client time.
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone; //@A1A


/**
 A utility for converting date/time values.
 The system API <tt>QWCCVTDT</tt> is used to convert a date and time value
 from one format to another format.

 @see AS400Date
 @see AS400Time
 @see AS400Timestamp
**/
public class DateTimeConverter
{
  private AS400 system_;
  private ProgramCall program_;
  private AS400Text text10_;

  private Calendar calendar_;
  //private Calendar calendarGMT_;
  private DateTime17Format format17_;
  //private DateTime16Format format16_;
  private TimeZone systemTimeZone_;  // the time zone for the IBM i server

  /**
   * Constructs a DateTimeConverter object.
   * @param system The system.
  **/
  public DateTimeConverter(AS400 system)
  {
    if (system == null)
      throw new NullPointerException("system");
    system_ = system;
    program_ = new ProgramCall(system_);
    text10_ = new AS400Text(10, system_.getCcsid(), system_);
    ///format17_ = new DateTime17Format(system_);
    //format16_ = new DateTime16Format(system_);
  }


  /**
   * Converts date and time values from the input format to the requested output format.
   * The system API <tt>QWCCVTDT</tt> (Convert Date and Time Format) is called to perform the conversion.
   * <p>This method effectively just re-arranges the time format and returns it.
   * The input and output values are relative to the same time zone.
   * Therefore, no adjustments are made based on time-zone.  
   *
   * @param data The date and time value to be converted.
   * @param inFormat The input date and time format.
   * Possible values are:
     <UL>
     <LI>*CURRENT
     <LI>*DTS
     <LI>*JOB
     <LI>*SYSVAL
     <LI>*YMD
     <LI>*YYMD
     <LI>*MDY
     <LI>*MDYY
     <LI>*DMY
     <LI>*DMYY
     <LI>*JUL
     <LI>*LONGJUL
     </UL>
   * @param outFormat The output date and time format.
   * Possible values are:
     <UL>
     <LI>*DTS
     <LI>*JOB
     <LI>*SYSVAL
     <LI>*YMD
     <LI>*YYMD
     <LI>*MDY
     <LI>*MDYY
     <LI>*DMY
     <LI>*DMYY
     <LI>*JUL
     <LI>*LONGJUL
     <LI>*DOS
     </UL>
   * @return The converted date and time value, left-justified in the array.
   * Refer to the specification for the QWCCVTDT API to determine how many of the
   * returned bytes are meaningful.
   * 
   * @exception AS400SecurityException If a security or authority error
   *            occurs.
   * @exception ErrorCompletingRequestException If an error occurs before
   *            the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with
   *            the system.
   * @exception ObjectDoesNotExistException If the object does not exist on the system.
  **/
  public byte[] convert(byte[] data, String inFormat, String outFormat)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    return convert(data, inFormat, outFormat, null);
  }

  private byte[] convert(byte[] data, String inFormat, String outFormat, String timezone)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    if (data == null) throw new NullPointerException("data");
    if (inFormat == null) throw new NullPointerException("inFormat");
    if (outFormat == null) throw new NullPointerException("outFormat");
        
    // Set up the parameters.
    // Design note: We don't need to use the optional parms, since we're not converting between different time zones.
    ProgramParameter[] parmList;
    if (timezone == null) {
      parmList = new ProgramParameter[5];
      setRequiredParameters(parmList, data, inFormat, outFormat);
    }
    else {
      parmList = new ProgramParameter[10];
      setRequiredParameters(parmList, data, inFormat, outFormat);
      setOptionalParameters(parmList, timezone);
    }

    // Set the program name and parameter list
    try
    {
      program_.setProgram("/QSYS.LIB/QWCCVTDT.PGM", parmList);
    }
    catch(PropertyVetoException pve) {} // Quiet the compiler

    program_.suggestThreadsafe();  //@A2A

    // Run the program
    if (!program_.run())
    {
      // Note that there was an error
      Trace.log(Trace.ERROR, "DateTimeConverter call to QWCCVTDT failed.");

      // Show the messages
      AS400Message[] messageList = program_.getMessageList();
      for (int i=0; i<messageList.length; ++i)
      {
        Trace.log(Trace.ERROR, messageList[i].toString());
      }
      throw new AS400Exception(messageList);
    }
    byte[] received = parmList[3].getOutputData();

    // Note:
    // We leave it up to the user to decide which portion of the byte array to use.
    // Different formats use different numbers of bytes:
    //  *DTS uses bytes 0-7.
    //  *DOS uses bytes 0-11.
    //  *YYMD, *MDYY, *DMYY, *LONGJUL use 0-16 (the whole thing).
    //  Anything else uses bytes 0-15.

    return received;
  }


  // Sets the required parameters for the QWCCVTDT API.
  private void setRequiredParameters(ProgramParameter[] parmList, byte[] data, String inFormat, String outFormat)
  {
    // First parameter is the input format.
    parmList[0] = new ProgramParameter(text10_.toBytes(inFormat));
    // Second parameter is the input variable.
    parmList[1] = new ProgramParameter(data);
    // Third parameter is the output format.
    parmList[2] = new ProgramParameter(text10_.toBytes(outFormat));
    // Fourth parameter is the output variable.
    parmList[3] = new ProgramParameter(17);
    // Fifth parameter is the error format.
    parmList[4] = new ErrorCodeParameter(); // Note: Original implementation allocated 70 bytes for this parameter.
  }


  // Sets the optional parameters for the QWCCVTDT API.
  private void setOptionalParameters(ProgramParameter[] parmList, String timezone)
  {
    // Note: QWCCVTDT optional parameter groups 1 and 2 were added in V5R3.

    // Sixth parameter is the input time zone.
    byte[] timezoneBytes = text10_.toBytes(timezone);
    parmList[5] = new ProgramParameter(timezoneBytes);
    // Seventh parameter is the output time zone.
    parmList[6] = new ProgramParameter(timezoneBytes);
    // Eighth parameter is the time zone information (output).
    parmList[7] = new ProgramParameter(0);  // a zero-length array
    // Ninth parameter is the length of time zone information.
    parmList[8] = new ProgramParameter(BinaryConverter.intToByteArray(0)); // 0
    // Tenth parameter is the precision indicator.
    parmList[9] = new ProgramParameter(new byte[] { (byte) 0xF0 }); // milliseconds

    // Design note:  When the "precision indicator" parameter is set to "milliseconds" (which is the default), and the time value being converted has greater precision (e.g. microseconds), then QWCCVTDT will round up or down to the nearest millisecond.

    // Note: We don't use the "input time indicator" parameter.
  }


  /**
   * Returns a converted Date object.
   * The system API <tt>QWCCVTDT</tt> (Convert Date and Time Format) is called to perform the conversion.
   * @param data The date and time value to be converted.
   * The value is assumed to be relative to the IBM i system's time zone.
   * @param inFormat The format of the date and time value being provided.
   * Possible values are:
       <UL>
       <LI>*CURRENT
       <LI>*DTS
       <LI>*JOB
       <LI>*SYSVAL
       <LI>*YMD
       <LI>*YYMD
       <LI>*MDY
       <LI>*MDYY
       <LI>*DMY
       <LI>*DMYY
       <LI>*JUL
       <LI>*LONGJUL
       </UL>
   * <b>Note:</b> When <tt>*CURRENT</tt> is specified, the <tt>data</tt> parameter is disregarded.
   * @return The converted date and time.
   * @exception AS400SecurityException If a security or authority error
   *            occurs.
   * @exception ErrorCompletingRequestException If an error occurs before
   *            the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with
   *            the system.
   * @exception ObjectDoesNotExistException If the object does not exist on the system.
  **/
  public Date convert(byte[] data, String inFormat)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    if (data == null) throw new NullPointerException("data");
    if (inFormat == null) throw new NullPointerException("inFormat");

    // Use output format *YYMD which gives us a full Date: YYYYMMDD.
    byte[] converted = convert(data, inFormat, "*YYMD", null); // call QWCCVTDT
    Record rec = getFormat17().getNewRecord(converted);

    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "DateTimeConverter record parsed from bytes: "+rec.toString());
    }

    Calendar calendar = getCalendar();
    calendar.set(Integer.parseInt(((String)rec.getField("year")).trim()),
                 Integer.parseInt(((String)rec.getField("month")).trim())-1,
                 Integer.parseInt(((String)rec.getField("day")).trim()),
                 Integer.parseInt(((String)rec.getField("hour")).trim()),
                 Integer.parseInt(((String)rec.getField("minute")).trim()),
                 Integer.parseInt(((String)rec.getField("second")).trim()));
    calendar.set(Calendar.MILLISECOND, Integer.parseInt(((String)rec.getField("millisecond")).trim()));

    return calendar.getTime();
  }


// For testing and/or future enhancement:
//  /**
//   * Returns a converted Date object, relative to the GMT time zone.
//   * The system API <tt>QWCCVTDT</tt> (Convert Date and Time Format) is called to perform the conversion.
//   * @param data The date and time value to be converted.
//   * The value is assumed to be relative to the IBM i system's time zone.
//   * @param inFormat The format of the date and time value being provided.
//   * Possible values are:
//       <UL>
//       <LI>*CURRENT
//       <LI>*DTS
//       <LI>*JOB
//       <LI>*SYSVAL
//       <LI>*YMD
//       <LI>*YYMD
//       <LI>*MDY
//       <LI>*MDYY
//       <LI>*DMY
//       <LI>*DMYY
//       <LI>*JUL
//       <LI>*LONGJUL
//       </UL>
//   * <b>Note:</b> When <tt>*CURRENT</tt> is specified, the <tt>data</tt> parameter is disregarded.
//   * @return The converted date and time.
//   * @exception AS400SecurityException If a security or authority error
//   *            occurs.
//   * @exception ErrorCompletingRequestException If an error occurs before
//   *            the request is completed.
//   * @exception InterruptedException If this thread is interrupted.
//   * @exception IOException If an error occurs while communicating with
//   *            the system.
//   * @exception ObjectDoesNotExistException If the object does not exist on the system.
//  **/
//  public Date convertGMT(byte[] data, String inFormat)
//      throws AS400SecurityException,
//             ErrorCompletingRequestException,
//             InterruptedException,
//             IOException,
//             ObjectDoesNotExistException
//  {
//    if (data == null) throw new NullPointerException("data");
//    if (inFormat == null) throw new NullPointerException("inFormat");
//
//    // Use output format *YYMD which gives us a full Date: YYYYMMDD.
//    byte[] converted = convert(data, inFormat, "*YYMD", "*UTC"); // call QWCCVTDT
//    Record rec = getFormat17().getNewRecord(converted);
//
//    if (Trace.traceOn_)
//    {
//      Trace.log(Trace.DIAGNOSTIC, "DateTimeConverter record parsed from bytes: "+rec.toString());
//    }
//
//    Calendar calendar = getCalendarGMT();
//    calendar.set(Integer.parseInt(((String)rec.getField("year")).trim()),
//                 Integer.parseInt(((String)rec.getField("month")).trim())-1,
//                 Integer.parseInt(((String)rec.getField("day")).trim()),
//                 Integer.parseInt(((String)rec.getField("hour")).trim()),
//                 Integer.parseInt(((String)rec.getField("minute")).trim()),
//                 Integer.parseInt(((String)rec.getField("second")).trim()));
//    calendar.set(Calendar.MILLISECOND, Integer.parseInt(((String)rec.getField("millisecond")).trim()));
//
//    return calendar.getTime();
//  }

  /**
   * Returns the converted date and time in a byte array.
   * @param date The Date object to be converted.
   * @param outFormat The format of the returned date and time value.
   * Possible values are:
       <UL>
       <LI>*DTS
       <LI>*JOB
       <LI>*SYSVAL
       <LI>*YMD
       <LI>*YYMD
       <LI>*MDY
       <LI>*MDYY
       <LI>*DMY
       <LI>*DMYY
       <LI>*JUL
       <LI>*LONGJUL
       </UL>
   * @return The converted date and time value, left-justified in the array.
   * The context of the value is the IBM i system's time zone.
   * Refer to the specification for the QWCCVTDT API to determine how many of the
   * returned bytes are meaningful.
   * 
   * @exception AS400SecurityException If a security or authority error
   *            occurs.
   * @exception ErrorCompletingRequestException If an error occurs before
   *            the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with
   *            the system.
   * @exception ObjectDoesNotExistException If the object does not exist on the system.
  **/
  public byte[] convert(Date date, String outFormat)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    if (date == null) throw new NullPointerException("date");
    if (outFormat == null) throw new NullPointerException("outFormat");

    byte[] data = dateToBytes(date);

    return convert(data, "*YYMD", outFormat, null);
  }


// For testing and/or future enhancement:
//  /**
//   * Returns the converted date and time in a byte array, relative to the GMT time zone.
//   * @param date The Date object to be converted.
//   * @param outFormat The format of the returned date and time value.
//   * Possible values are:
//       <UL>
//       <LI>*DTS
//       <LI>*JOB
//       <LI>*SYSVAL
//       <LI>*YMD
//       <LI>*YYMD
//       <LI>*MDY
//       <LI>*MDYY
//       <LI>*DMY
//       <LI>*DMYY
//       <LI>*JUL
//       <LI>*LONGJUL
//       </UL>
//   * @return The converted date and time value, left-justified in the array.
//   * The context of the value is the GMT time zone.
//   * Refer to the specification for the QWCCVTDT API to determine how many of the
//   * returned bytes are meaningful.
//   * 
//   * @exception AS400SecurityException If a security or authority error
//   *            occurs.
//   * @exception ErrorCompletingRequestException If an error occurs before
//   *            the request is completed.
//   * @exception InterruptedException If this thread is interrupted.
//   * @exception IOException If an error occurs while communicating with
//   *            the system.
//   * @exception ObjectDoesNotExistException If the object does not exist on the system.
//  **/
//  public byte[] convertGMT(Date date, String outFormat)
//      throws AS400SecurityException,
//             ErrorCompletingRequestException,
//             InterruptedException,
//             IOException,
//             ObjectDoesNotExistException
//  {
//    if (date == null) throw new NullPointerException("date");
//    if (outFormat == null) throw new NullPointerException("outFormat");
//
//    byte[] data = dateToBytes(date);
//    return convert(data, "*YYMD", outFormat, "*UTC");  // "*UTC" == GMT timezone
//  }


  // Converts a Date value to an IBM i byte sequence in *YYMD format.
  private byte[] dateToBytes(Date date)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    Calendar calendar = getCalendar();
    calendar.setTime(date);

    // Start with *YYMD for conversion. Seems like a good format to use.
    Record rec = getFormat17().getNewRecord();
    rec.setField("year", Integer.toString(calendar.get(Calendar.YEAR)));

    // Need to pad each number with 0s if necessary, so it will fill the format
    int month = calendar.get(Calendar.MONTH)+1;
    String monthStr = (month < 10 ? "0" : "") + month;
    rec.setField("month", monthStr);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    String dayStr = (day < 10 ? "0" : "") + day;
    rec.setField("day", dayStr);
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    String hourStr = (hour < 10 ? "0" : "") + hour;
    rec.setField("hour", hourStr);
    int minute = calendar.get(Calendar.MINUTE);
    String minuteStr = (minute < 10 ? "0" : "") + minute;
    rec.setField("minute", minuteStr);
    int second = calendar.get(Calendar.SECOND);
    String secondStr = (second < 10 ? "0" : "") + second;
    rec.setField("second", secondStr);
    int ms = calendar.get(Calendar.MILLISECOND);
    String msStr = (ms < 100 ? "0" : "") + (ms < 10 ? "0" : "") + ms;
    rec.setField("millisecond", msStr);

    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "DateTimeConverter record parsed from Date: "+rec.toString());
    }

    return rec.getContents();
  }


  // Utility method.
  // Returns a Calendar object for the current system, set to the IBM i system's time zone.
  private final Calendar getCalendar()
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    if (calendar_ == null) {
      // Create a Calendar object, based in the system's time zone.
      calendar_ = Calendar.getInstance(getSystemTimeZone());
    }
    else calendar_.clear();
    return calendar_;
  }


// For testing and/or future enhancement:
//  // Utility method.
//  // Returns a Calendar object set to the GMT time zone.
//  private final Calendar getCalendarGMT()
//  {
//    if (calendarGMT_ == null) {
//      // Create a Calendar object, based in the GMT time zone.
//      calendarGMT_ = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));
//    }
//    else calendarGMT_.clear();
//    return calendarGMT_;
//  }


  // Utility method.
  private synchronized DateTime17Format getFormat17()
  {
    if (format17_ == null) {
      format17_ = new DateTime17Format(system_);
    }
    return format17_;
  }


  /**
   * Returns a TimeZone object to represent the time zone for the system.
   * The TimeZone object will have the correct UTC offset for the system.
   * @return A TimeZone object representing the system's configured time zone setting.
   * @exception AS400SecurityException If a security or authority error
   *            occurs.
   * @exception ErrorCompletingRequestException If an error occurs before
   *            the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with
   *            the system.
   * @exception ObjectDoesNotExistException If the API used to retrieve the information does not exist on the system.
  **/
  final TimeZone getSystemTimeZone()
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    if (systemTimeZone_ == null) {
      systemTimeZone_ = timeZoneForSystem(system_);
    }
    return systemTimeZone_;
  }


  /**
   * Returns a TimeZone object to represent the time zone for the specified system.
   * The TimeZone object will have the correct UTC offset for the system.
   * @param system The IBM i system.
   * @return A TimeZone object representing the time zone for the system.
   * @exception AS400SecurityException If a security or authority error
   *            occurs.
   * @exception ErrorCompletingRequestException If an error occurs before
   *            the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with
   *            the system.
   * @exception ObjectDoesNotExistException If the API used to retrieve the information does not exist on the system.
   * @see AS400#getTimeZone()
  **/
  public static TimeZone timeZoneForSystem(AS400 system)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
	  
	// Using the UTC offset does not properly account for the use of daylight savings time.  We use
    // the qtimezone value to adjust for those systems that use daylight savings time. 
	//   
    // To obtain a standard ID for the time zone, simply concatenate "GMT" and the QUTCOFFSET value.
	
    String utcOffset = null;
    try
    {
      SystemValue sv = new SystemValue(system, "QUTCOFFSET");
      utcOffset = (String)sv.getValue();  // returns a value such as "-0500"
      if (utcOffset == null || utcOffset.length() == 0)
      {
        if (Trace.traceOn_) {
          Trace.log(Trace.DIAGNOSTIC, "QUTCOFFSET is not set. Assuming server is in the same time zone as client application.");
        }
        return TimeZone.getDefault();
      }
      else return TimeZone.getTimeZone("GMT" + utcOffset);
    }
    catch (RequestNotSupportedException e)  // this won't happen
    {  // ... but if it does happen, trace it and rethrow as a runtime exception
      Trace.log(Trace.ERROR, e);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
    }
    catch (RuntimeException e)
    {
      // Note: We've observed the following error during testing:
      // java.lang.NullPointerException at java.util.TimeZone.parseCustomTimeZone()
      if (Trace.traceOn_) {
        Trace.log(Trace.WARNING, "["+e.toString()+"] Unable to determine time zone of system. " +
                  "QUTCOFFSET value is " + utcOffset + ". " +
                  "Assuming server is in the same time zone as client application.");
      }
      return TimeZone.getDefault();
    }
  }


  // Utility method, for use by other Toolbox classes.
  // Trims-down a QWCCVTDT-generated EBCDIC date string, for use in a CL command parameter.
  // Depending on the specified format, this method strips extra leading and trailing digits that aren't expected by CL commands.
  static final String formatDateForCommandParameter(byte[] dateBytes, String dateFormat)
    throws UnsupportedEncodingException
  {
    int expectedLength = 0;
    String outVal = null;

    // Convert EBCDIC characters to a Unicode String.
    final String dateString = CharConverter.byteArrayToString(37, dateBytes);

    if (dateFormat.equals("*DTS")) expectedLength = 8;
    else if (dateFormat.equals("*YYMD") ||
             dateFormat.equals("*MDYY") ||
             dateFormat.equals("*DMYY") ||
             dateFormat.equals("*LONGJUL"))
    {
      expectedLength = 17;  // for details, refer to the API spec for QWCCVTDT
    }
    else if (dateFormat.equals("*MDY") ||
             dateFormat.equals("*YMD") ||
             dateFormat.equals("*DMY"))
    {
      // For these formats, CL commands expect 6 digits, with no leading "century guard digit".
      outVal = dateString.substring(1,7);
    }
    else if (dateFormat.equals("*JUL"))
    {
      // For *JUL, CL commands expect 5 digits, with no leading "century guard digit".
      outVal = dateString.substring(1,6);
    }
    else
    {
      expectedLength = 16;  // for details, refer to the API spec for QWCCVTDT
    }

    if (outVal != null) return outVal;
    else
    {
      if (dateString.length() == expectedLength) return dateString;  // no truncation needed
      else return dateString.substring(0,expectedLength);  // truncate extra trailing digits
    }
  }


  /**
   * Specifies the 17-byte character date and time value format returned
   * on the QWCCVTDT API.
  **/
  private static class DateTime17Format extends RecordFormat
  {
    private static final long serialVersionUID = 1L;

    DateTime17Format(AS400 system)
    {
      int ccsid = system.getCcsid();
      AS400Text text2 = new AS400Text(2, ccsid, system);
      addFieldDescription(new CharacterFieldDescription(new AS400Text(4, ccsid, system), "year"));
      addFieldDescription(new CharacterFieldDescription(text2, "month"));
      addFieldDescription(new CharacterFieldDescription(text2, "day"));
      addFieldDescription(new CharacterFieldDescription(text2, "hour"));
      addFieldDescription(new CharacterFieldDescription(text2, "minute"));
      addFieldDescription(new CharacterFieldDescription(text2, "second"));
      addFieldDescription(new CharacterFieldDescription(new AS400Text(3, ccsid, system), "millisecond"));
    }
  }


  // /**
  // * Specifies the 16-byte character date and time value format returned
  // * on the QWCCVTDT API.
  //**/
  //private static class DateTime16Format extends RecordFormat
  //{
  //  DateTime16Format(AS400 system)
  //  {
  //    int ccsid = system.getCcsid();
  //    AS400Text text2 = new AS400Text(2, ccsid, system);
  //    addFieldDescription(new CharacterFieldDescription(new AS400Text(1, ccsid, system), "century"));
  //    addFieldDescription(new CharacterFieldDescription(text2, "year"));
  //    addFieldDescription(new CharacterFieldDescription(text2, "month"));
  //    addFieldDescription(new CharacterFieldDescription(text2, "day"));
  //    addFieldDescription(new CharacterFieldDescription(text2, "hour"));
  //    addFieldDescription(new CharacterFieldDescription(text2, "minute"));
  //    addFieldDescription(new CharacterFieldDescription(text2, "second"));
  //    addFieldDescription(new CharacterFieldDescription(new AS400Text(3, ccsid, system), "millisecond"));
  //  }
  //}

}
