///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DateTimeConverter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 The DateTimeConverter class represents a converted date and time.
 The AS/400 System API QWCCVTDT is used to convert a date and time value
 from one format to another format.
**/
public class DateTimeConverter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private AS400 system_;
  private ProgramCall program_;
  private AS400Text text10_;
  private Calendar calendar_ = Calendar.getInstance();
  private DateTime17Format format17_;
  private DateTime16Format format16_;

  /**
   * Constructs a DateTimeConverter object.
   * @param system The AS/400 system.
  **/
  public DateTimeConverter(AS400 system)
  {
    if (system == null)
      throw new NullPointerException("system");
    system_ = system;
    program_ = new ProgramCall(system_);
    text10_ = new AS400Text(10, system_.getCcsid(), system_);
    format17_ = new DateTime17Format(system_);
    format16_ = new DateTime16Format(system_);
  }

  /**
   * Converts date and time values from the input format to the requested output format.
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
   *            the AS/400.
   * @exception ObjectDoesNotExistException If the AS/400 object does not
   *            exist.
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

    program_.setThreadSafe(true);  //@A2A

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
   * @param data The date and time value to be converted.
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
   *            the AS/400.
   * @exception ObjectDoesNotExistException If the AS/400 object does not
   *            exist.
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

    // Use *YYMD which gives us a full Date: YYYYMMDD.
    byte[] converted = convert(data, inFormat, "*YYMD");
    calendar_.clear();
    Record rec = format17_.getNewRecord(converted);

    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "DateTimeConverter record parsed from bytes: "+rec.toString());
    }

    calendar_.set(Integer.parseInt(((String)rec.getField("year")).trim()),
                  Integer.parseInt(((String)rec.getField("month")).trim())-1,
                  Integer.parseInt(((String)rec.getField("day")).trim()),
                  Integer.parseInt(((String)rec.getField("hour")).trim()),
                  Integer.parseInt(((String)rec.getField("minute")).trim()),
                  Integer.parseInt(((String)rec.getField("second")).trim()));
    calendar_.set(Calendar.MILLISECOND, Integer.parseInt(((String)rec.getField("millisecond")).trim()));

    return calendar_.getTime();
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
   * @exception AS400SecurityException If a security or authority error
   *            occurs.
   * @exception ErrorCompletingRequestException If an error occurs before
   *            the request is completed.
   * @exception InterruptedException If this thread is interrupted.
   * @exception IOException If an error occurs while communicating with
   *            the AS/400.
   * @exception ObjectDoesNotExistException If the AS/400 object does not
   *            exist.
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

    calendar_.clear();
    calendar_.setTime(date);

    // Use *YYMD for conversion. Seems like a good format to use.
    Record rec = format17_.getNewRecord();
    rec.setField("year", Integer.toString(calendar_.get(Calendar.YEAR)));

    // Need to pad each number with 0s if necessary, so it will fill the format
    int month = calendar_.get(Calendar.MONTH)+1;
    String monthStr = (month < 10 ? "0" : "") + month;
    rec.setField("month", monthStr);
    int day = calendar_.get(Calendar.DAY_OF_MONTH);
    String dayStr = (day < 10 ? "0" : "") + day;
    rec.setField("day", dayStr);
    int hour = calendar_.get(Calendar.HOUR_OF_DAY);
    String hourStr = (hour < 10 ? "0" : "") + hour;
    rec.setField("hour", hourStr);
    int minute = calendar_.get(Calendar.MINUTE);
    String minuteStr = (minute < 10 ? "0" : "") + minute;
    rec.setField("minute", minuteStr);
    int second = calendar_.get(Calendar.SECOND);
    String secondStr = (second < 10 ? "0" : "") + second;
    rec.setField("second", secondStr);
    int ms = calendar_.get(Calendar.MILLISECOND);
    String msStr = (ms < 100 ? "0" : "") + (ms < 10 ? "0" : "") + ms;
    rec.setField("millisecond", msStr);

    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "DateTimeConverter record parsed from Date: "+rec.toString());
    }

    byte[] data = rec.getContents();

    return convert(data, "*YYMD", outFormat);
  }


  /**
   * Specifies the 17-byte character date and time value format returned
   * on the QWCCVTDT API.
  **/
  private class DateTime17Format extends RecordFormat
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


  /**
   * Specifies the 16-byte character date and time value format returned
   * on the QWCCVTDT API.
  **/
  private class DateTime16Format extends RecordFormat
  {
    DateTime16Format(AS400 system)
    {
      int ccsid = system.getCcsid();
      AS400Text text2 = new AS400Text(2, ccsid, system);
      addFieldDescription(new CharacterFieldDescription(new AS400Text(1, ccsid, system), "century"));
      addFieldDescription(new CharacterFieldDescription(text2, "year"));
      addFieldDescription(new CharacterFieldDescription(text2, "month"));
      addFieldDescription(new CharacterFieldDescription(text2, "day"));
      addFieldDescription(new CharacterFieldDescription(text2, "hour"));
      addFieldDescription(new CharacterFieldDescription(text2, "minute"));
      addFieldDescription(new CharacterFieldDescription(text2, "second"));
      addFieldDescription(new CharacterFieldDescription(new AS400Text(3, ccsid, system), "millisecond"));
    }
  }
}

