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
 The DateTimeConverter class represents a converted date and time.
 The system API QWCCVTDT is used to convert a date and time value
 from one format to another format.
**/
public class DateTimeConverter
{
  private AS400 system_;
  private ProgramCall program_;
  private AS400Text text10_;

  private Calendar calendar_;
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
    format17_ = new DateTime17Format(system_);
    //format16_ = new DateTime16Format(system_);
  }

  /**
   * Converts date and time values from the input format to the requested output format.
   * <p>This method effectively re-arranges the time format and returns it.
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
   * @return The converted date and time value.
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
    if (data == null) throw new NullPointerException("data");
    if (inFormat == null) throw new NullPointerException("inFormat");
    if (outFormat == null) throw new NullPointerException("outFormat");
        
    // Setup the parameters
    // Design note: We don't need to use the optional parms, since we're not converting between different time zones.
    ProgramParameter[] parmlist = new ProgramParameter[5];
    // First parameter is the input format.
    parmlist[0] = new ProgramParameter(text10_.toBytes(inFormat));
    // Second parameter is the input variable.
    parmlist[1] = new ProgramParameter(data);
    // Third parameter is the output format.
    parmlist[2] = new ProgramParameter(text10_.toBytes(outFormat));
    // Fourth parameter is the output variable.
    parmlist[3] = new ProgramParameter(17);
    // Fifth parameter is the error format.
    byte[] errorCode = new byte[70];
    parmlist[4] = new ProgramParameter(errorCode);

    // Set the program name and parameter list
    try
    {
      program_.setProgram("/QSYS.LIB/QWCCVTDT.PGM", parmlist);
    }
    catch(PropertyVetoException pve) {} // Quiet the compiler

    //program_.suggestThreadsafe();  //@A2A

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
    byte[] received = parmlist[3].getOutputData();
    // Leave it up to the user to decide which portion of the byte
    // array they should use.
    // Note:
    //  *DTS uses 0-7
    //  *DOS uses 0-11
    //  *YYMD, *MDYY, *DMYY, *LONGJUL use 0-16 (the whole thing)
    //  anything else uses 0-15
    return received;
  }


  /**
   * Returns a converted Date object.
   * <p><b>Note:</b> When <tt>*CURRENT</tt> is specified, the <tt>data</tt> parameter will not be used.
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
    byte[] converted = convert(data, inFormat, "*YYMD");
    Record rec = format17_.getNewRecord(converted);

    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
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
   * @return The converted date and time.
   * The value is relative to the IBM i system's time zone.
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

    Calendar calendar = getCalendar();
    calendar.setTime(date);

    // Start with *YYMD for conversion. Seems like a good format to use.
    Record rec = format17_.getNewRecord();
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

    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "DateTimeConverter record parsed from Date: "+rec.toString());
    }

    byte[] data = rec.getContents();

    return convert(data, "*YYMD", outFormat);
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


  /**
   * Returns a TimeZone object to represent the time zone for the system.
   * The TimeZone object will have the correct UTC offset for the system.
   * @return A TimeZone object representing the time zone for the system.
   * @exception AS400SecurityException If a security or authority error
   *            occurs.
   * @exception ErrorCompletingRequestException If an error occurs before
   *            the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with
   *            the system.
   * @exception ObjectDoesNotExistException If the object does not exist on the system.
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
   * @exception ObjectDoesNotExistException If the object does not exist on the system.
  **/
  final static TimeZone timeZoneForSystem(AS400 system)
      throws AS400SecurityException,
             ErrorCompletingRequestException,
             InterruptedException,
             IOException,
             ObjectDoesNotExistException
  {
    // To obtain a standard ID for the time zone, simply concatenate "GMT" and the QUTCOFFSET value.
    try
    {
      SystemValue sv = new SystemValue(system, "QUTCOFFSET");
      String utcOffset = (String)sv.getValue();  // returns a value such as "-0500"
      return TimeZone.getTimeZone("GMT" + utcOffset);
    }
    catch (RequestNotSupportedException e)  // this won't happen
    {  // ... but if it does happen, trace it and rethrow as a runtime exception
      Trace.log(Trace.ERROR, e);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e.getMessage());
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
