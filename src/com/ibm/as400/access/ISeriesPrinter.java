///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ISeriesPrinter.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;
import java.io.*;

/**
 * Represents a printer attached to an iSeries system.
 * This class provides access to the specified printer's attributes via
 * the QGYRPRTA system API.
 * <P>
 * This class uses the remote command host server to obtain printer information.
 * Use the {@link com.ibm.as400.access.Printer Printer} class to retrieve
 * similer information using the network print host server.
 * <P>
 * Example:
 * <PRE>
 * AS400 system = new AS400("mySystem", "myUserID", "myPassword");
 * ISeriesPrinter printer = new ISeriesPrinter(system, "myPrinter");
 * String type = printer.getPrinterDeviceType();
 * if (type == ISeriesPrinter.PRINTER_DEVICE_TYPE_SCS)
 * {
 *   System.out.println(printer.getCopiesLeft());
 * }
 * </PRE>
 * @see com.ibm.as400.access.Printer
**/
/* TBD: Add the following javadoc when ConfigurationDescriptionList class is created:
 * <P>
 * To determine a list of printer device descriptions on the iSeries system,
 * use the {@link com.ibm.as400.access.config.ConfigurationDescriptionList
 * ConfigurationDescriptionList} class.
*/
/* TBD: Add the following javadoc when config classes are created:
 * @see com.ibm.as400.access.config.ConfigurationDescriptionList
 * @see com.ibm.as400.access.config.PrinterDeviceDescription
*/

public class ISeriesPrinter implements Serializable
{
  static final long serialVersionUID = -609299009457592116L;
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
  
  /**
   * Constant representing a value of 0.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_VARIED_OFF = 0;
  
  /**
   * Constant representing a value of 5.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_AS36_DISABLED = 5;
  
  /**
   * Constant representing a value of 10.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_VARY_OFF_PENDING = 10;
  
  /**
   * Constant representing a value of 20.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_VARY_ON_PENDING = 20;
  
  /**
   * Constant representing a value of 30.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_VARIED_ON = 30;
  
  /**
   * Constant representing a value of 40.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_CONNECT_PENDING = 40;
  
  /**
   * Constant representing a value of 50.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_SIGNON_DISPLAY = 50;
  
  /**
   * Constant representing a value of 60.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_ACTIVE = 60;
  
  /**
   * Constant representing a value of 62.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_AS36_ENABLED = 62;
  
  /**
   * Constant representing a value of 63.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_ACTIVE_READER = 63;
  
  /**
   * Constant representing a value of 66.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_ACTIVE_WRITER = 66;
  
  /**
   * Constant representing a value of 70.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_HELD = 70;
  
  /**
   * Constant representing a value of 75.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_POWERED_OFF = 75;
  
  /**
   * Constant representing a value of 80.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_RCYPND = 80;
  
  /**
   * Constant representing a value of 90.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_RCYCNL = 90;
  
  /**
   * Constant representing a value of 95.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_SYSTEM_REQUEST = 95;
  
  /**
   * Constant representing a value of 100.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_FAILED = 100;
  
  /**
   * Constant representing a value of 103.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_FAILED_READER = 103;
  
  /**
   * Constant representing a value of 106.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_FAILED_WRITER = 106;
  
  /**
   * Constant representing a value of 110.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_DIAGNOSTIC_MODE = 110;
  
  /**
   * Constant representing a value of 111.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_DAMAGED = 111;
  
  /**
   * Constant representing a value of 112.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_LOCKED = 112;
  
  /**
   * Constant representing a value of 113.
   * @see #getDeviceStatus
  **/
  public static final int DEVICE_STATUS_UNKNOWN = 113;


  /**
   * Constant representing a value of "*WTR".
   * @see #getFormAlignmentMessageTime
  **/
  public static final String FORM_ALIGNMENT_WRITER = "*WTR";

  /**
   * Constant representing a value of "*FILE".
   * @see #getFormAlignmentMessageTime
  **/
  public static final String FORM_ALIGNMENT_FILE = "*FILE";


  /**
   * Constant representing a value of "*ALL".
   * @see #getFormType
  **/
  public static final String FORM_TYPE_ALL = "*ALL";
  
  /**
   * Constant representing a value of "*FORMS".
   * @see #getFormType
  **/
  public static final String FORM_TYPE_FORMS = "*FORMS";
  
  /**
   * Constant representing a value of "*STD".
   * @see #getFormType
  **/
  public static final String FORM_TYPE_STANDARD = "*STD";

  
  /**
   * Constant representing a value of "*NOMSG".
   * @see #getMessageOption
  **/
  public static final String MESSAGE_OPTION_NONE = "*NOMSG";
  
  /**
   * Constant representing a value of "*MSG".
   * @see #getMessageOption
  **/
  public static final String MESSAGE_OPTION_STANDARD = "*MSG";
  
  /**
   * Constant representing a value of "*INFOMSG".
   * @see #getMessageOption
  **/
  public static final String MESSAGE_OPTION_INFO = "*INFOMSG";
  
  /**
   * Constant representing a value of "*INQMSG".
   * @see #getMessageOption
  **/
  public static final String MESSAGE_OPTION_INQUIRY = "*INQMSG";

  
  /**
   * Constant representing an EBCDIC value of "H".
   * @see #getOutputQueueStatus
  **/
  public static final int OUTPUT_QUEUE_STATUS_HELD = 0x00C8; // 'H'
  
  /**
   * Constant representing an EBCDIC value of "R".
   * @see #getOutputQueueStatus
  **/
  public static final int OUTPUT_QUEUE_STATUS_RELEASED = 0x00D9; // 'R'

  
  /**
   * Constant representing a value of 1.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_UNAVAILABLE = 1;
  
  /**
   * Constant representing a value of 2.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_NOT_YET_AVAILABLE = 2;
  
  /**
   * Constant representing a value of 3.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_STOPPED = 3;
  
  /**
   * Constant representing a value of 4.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_MESSAGE_WAITING = 4;
  
  /**
   * Constant representing a value of 5.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_HELD = 5;
  
  /**
   * Constant representing a value of 6.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_STOP_PENDING = 6;
  
  /**
   * Constant representing a value of 7.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_HOLD_PENDING = 7;
  
  /**
   * Constant representing a value of 8.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_WAITING_FOR_PRINTER = 8;
  
  /**
   * Constant representing a value of 9.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_WAITING_TO_START = 9;
  
  /**
   * Constant representing a value of 10.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_PRINTING = 10;
  
  /**
   * Constant representing a value of 11.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_WAITING_FOR_PRINTER_OUTPUT = 11;
  
  /**
   * Constant representing a value of 12.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_CONNECT_PENDING = 12;
  
  /**
   * Constant representing a value of 13.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_POWERED_OFF = 13;
  
  /**
   * Constant representing a value of 14.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_UNUSABLE = 14;
  
  /**
   * Constant representing a value of 15.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_BEING_SERVICED = 15;
  
  /**
   * Constant representing a value of 999.
   * @see #getOverallStatus
  **/
  public static final int OVERALL_STATUS_UNKNOWN = 999;


  /**
   * Constant representing an EBCDIC value of "N".
   * @see #getEndPendingStatus
   * @see #getHoldPendingStatus
  **/
  public static final int PENDING_STATUS_NONE = 0x00D5; // 'N'
  
  /**
   * Constant representing an EBCDIC value of "I".
   * @see #getEndPendingStatus
   * @see #getHoldPendingStatus
  **/
  public static final int PENDING_STATUS_IMMEDIATE = 0x00C9; // 'I'
  
  /**
   * Constant representing an EBCDIC value of "C".
   * @see #getEndPendingStatus
   * @see #getHoldPendingStatus
  **/
  public static final int PENDING_STATUS_CONTROLLED = 0x00C3; // 'C'
  
  /**
   * Constant representing an EBCDIC value of "P".
   * @see #getEndPendingStatus
   * @see #getHoldPendingStatus
  **/
  public static final int PENDING_STATUS_PAGE_END = 0x00D7; // 'P'

  
  /**
   * Constant representing a value of "*SCS".
   * @see #getPrinterDeviceType
  **/
  public static final String PRINTER_DEVICE_TYPE_SCS = "*SCS";
  
  /**
   * Constant representing a value of "*IPDS".
   * @see #getPrinterDeviceType
  **/
  public static final String PRINTER_DEVICE_TYPE_IPDS = "*IPDS";

  
  /**
   * Constant representing a value of 1.
   * @see #getWriterStatus
  **/
  public static final int WRITER_STATUS_STARTED = 0x01;
  
  /**
   * Constant representing a value of 2.
   * @see #getWriterStatus
  **/
  public static final int WRITER_STATUS_ENDED = 0x02;
  
  /**
   * Constant representing a value of 3.
   * @see #getWriterStatus
  **/
  public static final int WRITER_STATUS_ON_JOB_QUEUE = 0x03;
  
  /**
   * Constant representing a value of 4.
   * @see #getWriterStatus
  **/
  public static final int WRITER_STATUS_HELD = 0x04;
  
  /**
   * Constant representing a value of 5.
   * @see #getWriterStatus
  **/
  public static final int WRITER_STATUS_MESSAGE_WAITING = 0x05;


  /**
   * Constant representing a value of "*NORDYF".
   * @see #getWriterChangeTime
   * @see #getWriterEndTime
  **/
  public static final String WRITER_TIME_ALL_DONE = "*NORDYF";
  
  /**
   * Constant representing a value of "*FILEEND".
   * @see #getWriterChangeTime
   * @see #getWriterEndTime
  **/
  public static final String WRITER_TIME_CURRENT_DONE = "*FILEEND";
  
  /**
   * Constant representing a value of "*NO".
   * @see #getWriterChangeTime
   * @see #getWriterEndTime
  **/
  public static final String WRITER_TIME_WAIT = "*NO";
  
  /**
   * Constant representing a value of "".
   * @see #getWriterChangeTime
   * @see #getWriterEndTime
  **/
  public static final String WRITER_TIME_NONE = "";


  /**
   * Constant representing an EBCDIC value of "Y".
   * @see #getWritingStatus
  **/
  public static final int WRITING_STATUS_WRITING = 0x00E8; // 'Y'
  
  /**
   * Constant representing an EBCDIC value of "N".
   * @see #getWritingStatus
  **/
  public static final int WRITING_STATUS_NOT_WRITING = 0x00D5; // 'N'
  
  /**
   * Constant representing an EBCDIC value of "S".
   * @see #getWritingStatus
  **/
  public static final int WRITING_STATUS_FILE_SEPARATORS = 0x00E2; // 'S'


  private AS400 system_;
  private String name_;
  private boolean refreshed_ = false;

  private boolean writerStarted_;
  private int writerStatus_;
  private int deviceStatus_;
  private int overallStatus_;
  private String textDescription_;
  private String startedByUser_;
  private int writingStatus_;
  private boolean waitingForMessageStatus_;
  private boolean heldStatus_;
  private int endPendingStatus_;
  private int holdPendingStatus_;
  private boolean betweenFilesStatus_;
  private boolean betweenCopiesStatus_;
  private boolean waitingForDataStatus_;
  private boolean waitingForDeviceStatus_;
  private boolean onJobQueueStatus_;
  private String writerJobName_;
  private String writerJobUser_;
  private String writerJobNumber_;
  private String printerDeviceType_;
  private int numberOfSeparators_;
  private int drawerForSeparators_;
  private String alignForms_;
  private String outputQueueName_;
  private String outputQueueLibrary_;
  private int outputQueueStatus_;
  private boolean networkDirectoryPublishingStatus_;
  private String formType_;
  private String messageOption_;
  private String automaticallyEndWriter_;
  private boolean allowDirectPrinting_;
  private String messageQueueName_;
  private String messageQueueLibrary_;
  private String changesTakeEffect_;
  private String nextOutputQueueName_;
  private String nextOutputQueueLibrary_;
  private String nextFormType_;
  private String nextMessageOption_;
  private int nextFileSeparators_;
  private int nextSeparatorDrawer_;
  private String spooledFileName_;
  private String jobName_;
  private String jobUser_;
  private String jobNumber_;
  private int spooledFileNumber_;
  private int pageBeingWritten_;
  private int totalPages_;
  private int copiesLeftToProduce_;
  private int totalCopies_;
  private boolean advancedFunctionPrinting_;
  private byte[] messageKey_;
  private String jobSystemName_;
  private String spooledFileCreateDate_;
  private String spooledFileCreateTime_;

  /**
   * Constructs an ISeriesPrinter object with the specified name. If a printer with the
   * specified name does not exist on the specified system, an exception will be thrown
   * when {@link #refresh refresh()} is called, either implicitly or explicitly.
   * @param system The system.
   * @param name The name of the printer.
  **/
  public ISeriesPrinter(AS400 system, String name)
  {
    if (system == null) throw new NullPointerException("system");
    if (name == null) throw new NullPointerException("name");
    system_ = system;
    name_ = name.toUpperCase().trim();
  }

  /**
   * Returns whether or not the printer writer allows the printer to be allocated
   * to a job that prints directly to the printer.
   * @return true if direct printing is allowed; false if it is not allowed.
  **/
  public boolean allowsDirectPrinting() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return allowDirectPrinting_;
  }

  /**
   * Returns whether or not the specified object is equal to this object.
   * @return true if the specified object is an ISeriesPrinter and the following contract is true:
   * <PRE>
   *   obj.getSystem().equals(this.getSystem()) &&
   *   obj.getName().equals(this.getName())
   * </PRE>
   * Otherwise, false is returned.
  **/
  public boolean equals(Object obj)
  {
    if (obj instanceof ISeriesPrinter)
    {
      ISeriesPrinter p = (ISeriesPrinter)obj;
      return p.system_.equals(system_) && p.name_.equals(name_);
    }
    return false;
  }

  /**
   * Returns the number of copies left to be printed.
   * @return The number of copies left, or 0 if no file is printing.
  **/
  public int getCopiesLeft() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return copiesLeftToProduce_;
  }

  /**
   * Returns the text description of the printer device.
   * @return The text description.
  **/
  public String getDescription() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return textDescription_;
  }

  /**
   * Returns the status of the printer device.
   * Possible values are:
   * <UL>
   * <LI>{@link #DEVICE_STATUS_VARIED_OFF DEVICE_STATUS_VARIED_OFF}
   * <LI>{@link #DEVICE_STATUS_AS36_DISABLED DEVICE_STATUS_AS36_DISABLED}
   * <LI>{@link #DEVICE_STATUS_VARY_OFF_PENDING DEVICE_STATUS_VARY_OFF_PENDING}
   * <LI>{@link #DEVICE_STATUS_VARY_ON_PENDING DEVICE_STATUS_VARY_ON_PENDING}
   * <LI>{@link #DEVICE_STATUS_VARIED_ON DEVICE_STATUS_VARIED_ON}
   * <LI>{@link #DEVICE_STATUS_CONNECT_PENDING DEVICE_STATUS_CONNECT_PENDING}
   * <LI>{@link #DEVICE_STATUS_SIGNON_DISPLAY DEVICE_STATUS_SIGNON_DISPLAY}
   * <LI>{@link #DEVICE_STATUS_ACTIVE DEVICE_STATUS_ACTIVE}
   * <LI>{@link #DEVICE_STATUS_AS36_ENABLED DEVICE_STATUS_AS36_ENABLED}
   * <LI>{@link #DEVICE_STATUS_ACTIVE_READER DEVICE_STATUS_ACTIVE_READER}
   * <LI>{@link #DEVICE_STATUS_ACTIVE_WRITER DEVICE_STATUS_ACTIVE_WRITER}
   * <LI>{@link #DEVICE_STATUS_HELD DEVICE_STATUS_HELD}
   * <LI>{@link #DEVICE_STATUS_POWERED_OFF DEVICE_STATUS_POWERED_OFF}
   * <LI>{@link #DEVICE_STATUS_RCYPND DEVICE_STATUS_RCYPND}
   * <LI>{@link #DEVICE_STATUS_RCYCNL DEVICE_STATUS_RCYCNL}
   * <LI>{@link #DEVICE_STATUS_SYSTEM_REQUEST DEVICE_STATUS_SYSTEM_REQUEST}
   * <LI>{@link #DEVICE_STATUS_FAILED DEVICE_STATUS_FAILED}
   * <LI>{@link #DEVICE_STATUS_FAILED_READER DEVICE_STATUS_FAILED_READER}
   * <LI>{@link #DEVICE_STATUS_FAILED_WRITER DEVICE_STATUS_FAILED_WRITER}
   * <LI>{@link #DEVICE_STATUS_DIAGNOSTIC_MODE DEVICE_STATUS_DIAGNOSTIC_MODE}
   * <LI>{@link #DEVICE_STATUS_DAMAGED DEVICE_STATUS_DAMAGED}
   * <LI>{@link #DEVICE_STATUS_LOCKED DEVICE_STATUS_LOCKED}
   * <LI>{@link #DEVICE_STATUS_UNKNOWN DEVICE_STATUS_UNKNOWN}
   * </UL>
   * @return The printer device status.
  **/
  public int getDeviceStatus() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return deviceStatus_;
  }

  /**
   * Returns whether an End Writer (ENDWTR) command has been issued for this writer.
   * Possible values are:
   * <UL>
   * <LI>{@link #PENDING_STATUS_NONE PENDING_STATUS_NONE} - No ENDWTR command was issued.
   * <LI>{@link #PENDING_STATUS_IMMEDIATE PENDING_STATUS_IMMEDIATE} - The writer ends as soon as its output buffers are empty.
   * <LI>{@link #PENDING_STATUS_CONTROLLED PENDING_STATUS_CONTROLLED} - The writer ends after the current copy of the spooled file has been printed.
   * <LI>{@link #PENDING_STATUS_PAGE_END PENDING_STATUS_PAGE_END} - The writer ends at the end of the page.
   * </UL>
   * @return The pending status.
  **/
  public int getEndPendingStatus() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return endPendingStatus_;
  }

  /**
   * Returns the time at which the forms alignment message will be sent.
   * Possible values are:
   * <UL>
   * <LI>{@link #FORM_ALIGNMENT_WRITER FORM_ALIGNMENT_WRITER}
   * <LI>{@link #FORM_ALIGNMENT_FILE FORM_ALIGNMENT_FORM}
   * </UL>
   * @return The form alignment message setting.
  **/
  public String getFormAlignmentMessageTime() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return alignForms_;
  }

  /**
   * Returns the type of form being used to print the spooled file.
   * Possible values are:
   * <UL>
   * <LI>{@link #FORM_TYPE_ALL FORM_TYPE_ALL}
   * <LI>{@link #FORM_TYPE_FORMS FORM_TYPE_FORMS}
   * <LI>{@link #FORM_TYPE_STANDARD FORM_TYPE_STANDARD}
   * <LI>A user-specified form type.
   * </UL>
   * @return The form type.
  **/
  public String getFormType() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return formType_;
  }

  /**
   * Returns whether a Hold Writer (HLDWTR) command has been issued for this writer.
   * Possible values are:
   * <UL>
   * <LI>{@link #PENDING_STATUS_NONE PENDING_STATUS_NONE} - No HLDWTR command was issued.
   * <LI>{@link #PENDING_STATUS_IMMEDIATE PENDING_STATUS_IMMEDIATE} - The writer is held as soon as its output buffers are empty.
   * <LI>{@link #PENDING_STATUS_CONTROLLED PENDING_STATUS_CONTROLLED} - The writer is held after the current copy of the file has been printed.
   * <LI>{@link #PENDING_STATUS_PAGE_END PENDING_STATUS_PAGE_END} - The writer is held at the end of the page.
   * </UL>
   * @return The pending status.
  **/
  public int getHoldPendingStatus() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return holdPendingStatus_;
  }

  /**
   * Returns the message option for sending a message to the message queue when this form is finished.
   * Possible values are:
   * <UL>
   * <LI>{@link #MESSAGE_OPTION_STANDARD MESSAGE_OPTION_STANDARD} - A message is sent to the message queue.
   * <LI>{@link #MESSAGE_OPTION_NONE MESSAGE_OPTION_NONE} - No message is sent to the message queue.
   * <LI>{@link #MESSAGE_OPTION_INFO MESSAGE_OPTION_INFO} - An informational message is sent to the message queue.
   * <LI>{@link #MESSAGE_OPTION_INQUIRY MESSAGE_OPTION_INQUIRY} - An inquiry message is sent to the message queue.
   * </UL>
   * @return The message option.
  **/
  public String getMessageOption() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return messageOption_;
  }

  /**
   * Returns the fully qualified integrated file system pathname of the message queue associated with this printer.
   * @return The message queue path, or "" if there is no associated message queue.
   * @see com.ibm.as400.access.MessageQueue
  **/
  public String getMessageQueue() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    if (messageQueueLibrary_.length() == 0) return "";
    QSYSObjectPathName path = new QSYSObjectPathName(messageQueueLibrary_, messageQueueName_, "MSGQ");
    return path.getPath();
  }

  /**
   * Returns the name of this printer.
   * @return The name.
   * @see #ISeriesPrinter
  **/
  public String getName()
  {
    return name_;
  }

  /**
   * Returns the number of separator pages to be printed.
   * @return The number of separators, or -1 if the number of separator pages is specified by each file.
  **/
  public int getNumberOfSeparators() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return numberOfSeparators_;
  }

  /**
   * Returns the fully qualified integrated file system pathname of the output queue associated with this printer
   * from which spooled files are selected for printing.
   * @return The output queue path, or "" if there is no associated output queue.
   * @see com.ibm.as400.access.OutputQueue
  **/
  public String getOutputQueue() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    if (outputQueueLibrary_.length() == 0) return "";
    QSYSObjectPathName path = new QSYSObjectPathName(outputQueueLibrary_, outputQueueName_, "OUTQ");
    return path.getPath();
  }

  /**
   * Returns the status of the output queue from which spooled files are being selected for printing.
   * Possible values are:
   * <UL>
   * <LI>{@link #OUTPUT_QUEUE_STATUS_HELD OUTPUT_QUEUE_STATUS_HELD} - The output queue is held.
   * <LI>{@link #OUTPUT_QUEUE_STATUS_RELEASED OUTPUT_QUEUE_STATUS_RELEASED} - The output queue is released.
   * </UL>
   * @return The output queue status.
  **/
  public int getOutputQueueStatus() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return outputQueueStatus_;
  }

  /**
   * Returns the overall status of the logical printer.
   * Possible values are:
   * <UL>
   * <LI>{@link #OVERALL_STATUS_UNAVAILABLE OVERALL_STATUS_UNAVAILABLE}
   * <LI>{@link #OVERALL_STATUS_NOT_YET_AVAILABLE OVERALL_STATUS_NOT_YET_AVAILABLE}
   * <LI>{@link #OVERALL_STATUS_STOPPED OVERALL_STATUS_STOPPED}
   * <LI>{@link #OVERALL_STATUS_MESSAGE_WAITING OVERALL_STATUS_MESSAGE_WAITING}
   * <LI>{@link #OVERALL_STATUS_HELD OVERALL_STATUS_HELD}
   * <LI>{@link #OVERALL_STATUS_STOP_PENDING OVERALL_STATUS_STOP_PENDING}
   * <LI>{@link #OVERALL_STATUS_HOLD_PENDING OVERALL_STATUS_HOLD_PENDING}
   * <LI>{@link #OVERALL_STATUS_WAITING_FOR_PRINTER OVERALL_STATUS_WAITING_FOR_PRINTER}
   * <LI>{@link #OVERALL_STATUS_WAITING_TO_START OVERALL_STATUS_WAITING_TO_START}
   * <LI>{@link #OVERALL_STATUS_PRINTING OVERALL_STATUS_PRINTING}
   * <LI>{@link #OVERALL_STATUS_WAITING_FOR_PRINTER_OUTPUT OVERALL_STATUS_WAITING_FOR_PRINTER_OUTPUT}
   * <LI>{@link #OVERALL_STATUS_CONNECT_PENDING OVERALL_STATUS_CONNECT_PENDING}
   * <LI>{@link #OVERALL_STATUS_POWERED_OFF OVERALL_STATUS_POWERED_OFF}
   * <LI>{@link #OVERALL_STATUS_UNUSABLE OVERALL_STATUS_UNUSABLE}
   * <LI>{@link #OVERALL_STATUS_BEING_SERVICED OVERALL_STATUS_BEING_SERVICED}
   * <LI>{@link #OVERALL_STATUS_UNKNOWN OVERALL_STATUS_UNKNOWN}
   * </UL>
   * @return The overall status.
  **/
  public int getOverallStatus() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return overallStatus_;
  }

  /**
   * Returns the page number in the spooled file that is currently being processed by the writer.
   * The page number returned may be lower or higher than the actual page number being printed
   * because of buffering done by the system.
   * @return The page number being written, or 0 if no spooled file is printing.
  **/
  public int getPageBeingWritten() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return pageBeingWritten_;
  }

  /**
   * Returns the name of the next form type to be printed.
   * Possible values are:
   * <UL>
   * <LI>{@link #FORM_TYPE_ALL FORM_TYPE_ALL} - The writer is changed with the option to print all spooled files of any form type.
   * <LI>{@link #FORM_TYPE_FORMS FORM_TYPE_FORMS} - The writer is changed with the option to print all the spooled files with the same form type before using a different form type.
   * <LI>{@link #FORM_TYPE_STANDARD FORM_TYPE_STANDARD} - The writer is changed with the option to print all the spooled files with a form type of *STD.
   * <LI>A user-specified form type - The writer is changed with the option to print all the spooled files with the form type name that was specified.
   * <LI>"" - No change has been made to this writer.
   * </UL>
   * @return The form type.
   * @see #getFormType
  **/
  public String getPendingFormType() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return nextFormType_;
  }

  /**
   * Returns the message option for sending a message to the message queue when the next form type is finished.
   * Possible values are:
   * <UL>
   * <LI>{@link #MESSAGE_OPTION_STANDARD MESSAGE_OPTION_STANDARD} - A message is sent to the message queue.
   * <LI>{@link #MESSAGE_OPTION_NONE MESSAGE_OPTION_NONE} - No message is sent to the message queue.
   * <LI>{@link #MESSAGE_OPTION_INFO MESSAGE_OPTION_INFO} - An informational message is sent to the message queue.
   * <LI>{@link #MESSAGE_OPTION_INQUIRY MESSAGE_OPTION_INQUIRY} - An inquiry message is sent to the message queue.
   * <LI>"" - No change is pending.
   * </UL>
   * @return The message option.
   * @see #getMessageOption
  **/
  public String getPendingMessageOption() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return nextMessageOption_;
  }

  /**
   * Returns the next number of separator pages to be printed when the change to the writer takes place.
   * @return The number of separators, or -1 if the number of separator pages is specified by each file,
   * or -10 if there is no pending change to the writer.
   * @see #getNumberOfSeparators
  **/
  public int getPendingNumberOfSeparators() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return nextFileSeparators_;
  }

  /**
   * Returns the fully qualified integrated file system pathname of the next output queue.
   * @return The output queue path, or "" if no changes have been made to the writer.
   * @see #getOutputQueue
   * @see com.ibm.as400.access.OutputQueue
  **/
  public String getPendingOutputQueue() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    if (nextOutputQueueLibrary_.length() == 0) return "";
    QSYSObjectPathName path = new QSYSObjectPathName(nextOutputQueueLibrary_, nextOutputQueueName_, "OUTQ");
    return path.getPath();
  }

  /**
   * Returns the drawer from which to take the separator pages if there is a change to the writer.
   * Possible values are:
   * <UL>
   * <LI>1 - The first drawer.
   * <LI>2 - The second drawer.
   * <LI>3 - The third drawer.
   * <LI>-1 - Separator pages print from the same drawer that the spooled file prints from.
   * If you specify a drawer different from the spooled file that contains colored or different
   * type paper, the page separator is more identifiable.
   * <LI>-2 - Separator pages print from the separator drawer specified in the printer device description.
   * <LI>-10 - There is no pending change to the writer.
   * </UL>
   * @return The separator drawer.
   * @see #getSeparatorDrawer
  **/
/* TBD: Add when PrinterDeviceDescription class is created:
   * @see com.ibm.as400.access.config.PrinterDeviceDescription
*/
  public int getPendingSeparatorDrawer() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return nextSeparatorDrawer_;
  }

  /**
   * Returns the type of printer being used to print the spooled file.
   * Possible values are:
   * <UL>
   * <LI>{@link #PRINTER_DEVICE_TYPE_SCS DEVICE_TYPE_SCS} - SNA (Systems Network Architecture) character string.
   * <LI>{@link #PRINTER_DEVICE_TYPE_IPDS DEVICE_TYPE_IPDS} - Intelligent Printer Data Stream (tm).
   * </UL>
   * @return The printer device type.
  **/
  public String getPrinterDeviceType() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return printerDeviceType_;
  }

  /**
   * Returns the drawer from which the job and file separator pages are to be taken.
   * Possible values are:
   * <UL>
   * <LI>1 - The first drawer.
   * <LI>2 - The second drawer.
   * <LI>3 - The third drawer.
   * <LI>-1 - Separator pages print from the same drawer that the spooled file prints from.
   * If you specify a drawer different from the spooled file that contains colored or different
   * type paper, the page separator is more identifiable.
   * <LI>-2 - Separator pages print from the separator drawer specified in the printer device description.
   * </UL>
   * @return The separator drawer.
  **/
/* TBD: Add when PrinterDeviceDescription class is added:
   * @see com.ibm.as400.access.config.PrinterDeviceDescription
*/
  public int getSeparatorDrawer() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return drawerForSeparators_;
  }

  /**
   * Returns the date and time the spooled file was created on the system.
   * @return The spooled file creation date, or null if the iSeries system is not running OS/400 V5R2 or higher.
  **/
  public Date getSpooledFileCreationDate() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (system_.getVRM() >= AS400.generateVRM(5,2,0))
    {
      if (!refreshed_) refresh();
      if (spooledFileCreateDate_.length() == 0) return null;
      Calendar c = Calendar.getInstance();
      c.clear();
      int year = spooledFileCreateDate_.charAt(0) == '1' ? 2000 : 1900;
      year += Integer.parseInt(spooledFileCreateDate_.substring(1,2));
      int month = Integer.parseInt(spooledFileCreateDate_.substring(3,2))-1;
      int day = Integer.parseInt(spooledFileCreateDate_.substring(5,2));
      c.set(Calendar.YEAR, year);
      c.set(Calendar.MONTH, month);
      c.set(Calendar.DAY_OF_MONTH, day);
      int hour = Integer.parseInt(spooledFileCreateTime_.substring(0,2));
      int minute = Integer.parseInt(spooledFileCreateTime_.substring(2,2));
      int second = Integer.parseInt(spooledFileCreateTime_.substring(4,2));
      c.set(Calendar.HOUR, hour);
      c.set(Calendar.MINUTE, minute);
      c.set(Calendar.SECOND, second);
      return c.getTime();
    }
    return null;
  }

  /**
   * Returns the name of the job that created the spooled file currently being processed by the writer.
   * @return The job name, or "" if no spooled file is printing.
   * @see com.ibm.as400.access.Job
  **/
  public String getSpooledFileJobName() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return jobName_;
  }

  /**
   * Returns the number of the job that created the spooled file currently being processed by the writer.
   * @return The job number, or "" if no spooled file is printing.
   * @see com.ibm.as400.access.Job
  **/
  public String getSpooledFileJobNumber() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return jobNumber_;
  }

  /** 
   * Returns the name of the system where the job that created the spooled file ran.
   * @return The system name, or "" if no spooled file is printing, or null if the iSeries system
   * is not running OS/400 V5R2 or higher.
  **/
  public String getSpooledFileJobSystem() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (system_.getVRM() >= AS400.generateVRM(5,2,0))
    {
      if (!refreshed_) refresh();
      return jobSystemName_;
    }
    return null;
  }

  /**
   * Returns the user of the job that created the spooled file currently being processed by the writer.
   * @return The user name, or "" if no spooled file is printing.
   * @see com.ibm.as400.access.Job
   * @see com.ibm.as400.access.User
  **/
  public String getSpooledFileJobUser() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return jobUser_;
  }

  /**
   * Returns the name of the spooled file currently being processed by the writer.
   * @return The spooled file name, or "" if no spooled file is printing.
  **/
  public String getSpooledFileName() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return spooledFileName_;
  }

  /**
   * Returns the number of the spooled file currently being processed by the writer.
   * @return The spooled file number, or 0 if no spooled file is printing.
  **/
  public int getSpooledFileNumber() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return spooledFileNumber_;
  }

  /**
   * Returns the name of the user that started the writer.
   * @return The user name.
   * @see com.ibm.as400.access.User
  **/
  public String getStarterUser() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return startedByUser_;
  }

  /**
   * Returns the system.
   * @return The system.
   * @see #ISeriesPrinter
  **/
  public AS400 getSystem()
  {
    return system_;
  }

  /**
   * Returns the total number of copies to be printed.
   * @return The number of copies.
  **/
  public int getTotalCopies() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return totalCopies_;
  }

  /**
   * Returns the total number of pages in the spooled file.
   * @return The number of pages, or 0 if no spooled file is printing.
  **/
  public int getTotalPages() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return totalPages_;
  }

  /**
   * Returns the time at which the pending changes to the writer take effect.
   * Possible values are:
   * <UL>
   * <LI>{@link #WRITER_TIME_ALL_DONE WRITER_TIME_ALL_DONE}
   * <LI>{@link #WRITER_TIME_CURRENT_DONE WRITER_TIME_CURRENT_DONE}
   * <LI>{@link #WRITER_TIME_NONE WRITER_TIME_NONE}
   * </UL>
   * @return The writer change setting.
  **/
  public String getWriterChangeTime() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return changesTakeEffect_;
  }

  /**
   * Returns when to end the writer if it is to end automatically.
   * Possible values are:
   * <UL>
   * <LI>{@link #WRITER_TIME_ALL_DONE WRITER_TIME_ALL_DONE}
   * <LI>{@link #WRITER_TIME_CURRENT_DONE WRITER_TIME_CURRENT_DONE}
   * <LI>{@link #WRITER_TIME_WAIT WRITER_TIME_WAIT}
   * </UL>
   * @return The writer end time setting.
  **/
  public String getWriterEndTime() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return automaticallyEndWriter_;
  }

  /**
   * Returns the job name of the printer writer.
   * @return The job name.
   * @see #getWriterJobNumber
   * @see #getWriterJobUser
   * @see com.ibm.as400.access.Job
  **/
  public String getWriterJobName() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return writerJobName_;
  }

  /**
   * Returns the job number of the printer writer.
   * @return The job number.
   * @see #getWriterJobName
   * @see #getWriterJobUser
   * @see com.ibm.as400.access.Job
  **/
  public String getWriterJobNumber() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return writerJobNumber_;
  }

  /**
   * Returns the name of the system user.
   * @return The user name.
   * @see #getWriterJobName
   * @see #getWriterJobNumber
   * @see com.ibm.as400.access.Job
   * @see com.ibm.as400.access.User
  **/
  public String getWriterJobUser() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return writerJobUser_;
  }

  /**
   * Returns the key to the message that the writer is waiting for a reply.
   * @return The 4-byte message key, which will consist of all 0's if the writer
   * is not waiting for a reply to an inquiry message.
  **/
  public byte[] getWriterMessageKey() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return messageKey_;
  }

  /**
   * Returns the status of the writer for this printer.
   * Possible values are:
   * <UL>
   * <LI>{@link #WRITER_STATUS_STARTED WRITER_STATUS_STARTED}
   * <LI>{@link #WRITER_STATUS_ENDED WRITER_STATUS_ENDED}
   * <LI>{@link #WRITER_STATUS_ON_JOB_QUEUE WRITER_STATUS_ON_JOB_QUEUE}
   * <LI>{@link #WRITER_STATUS_HELD WRITER_STATUS_HELD}
   * <LI>{@link #WRITER_STATUS_MESSAGE_WAITING WRITER_STATUS_MESSAGE_WAITING}
   * </UL>
   * @return The writer status.
  **/
  public int getWriterStatus() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return writerStatus_;
  }
    
  /**
   * Returns whether the printer writer is in writing status.
   * Possible values are:
   * <UL>
   * <LI>{@link #WRITING_STATUS_WRITING WRITING_STATUS_WRITING} - The writer is in writing status.
   * <LI>{@link #WRITING_STATUS_NOT_WRITING WRITING_STATUS_NOT_WRITING} - The writer is not in writing status.
   * <LI>{@link #WRITING_STATUS_FILE_SEPARATORS WRITING_STATUS_FILE_SEPARATORS} - The writer is writing the file separators.
   * </UL>
   * @return The writing status.
  **/
  public int getWritingStatus() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return writingStatus_;
  }

  /**
   * Returns the hash code value for the <i>name</i> of this ISeriesPrinter object.
   * @return The hash code.
  **/
  public int hashCode()
  {
    return name_.hashCode();
  }

  /**
   * Returns whether the writer is between copies of a multiple copy spooled file.
   * @return true if the writer is between copies, false otherwise.
  **/
  public boolean isBetweenCopies() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return betweenCopiesStatus_;
  }

  /**
   * Returns whether the writer is between spooled files.
   * @return true if the writer is between spooled files, false otherwise.
  **/
  public boolean isBetweenFiles() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return betweenFilesStatus_;
  }

  /**
   * Returns whether the writer is held.
   * @return true if the writer is held, false otherwise.
  **/
  public boolean isHeld() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return heldStatus_;
  }

  /**
   * Returns whether the writer is on a job queue and is not currently running.
   * @return true if the writer is on a job queue, false otherwise.
  **/
  public boolean isOnJobQueue() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return onJobQueueStatus_;
  }

  /**
   * Returns whether the printer is published in the network directory.
   * @return true if the printer is published, false otherwise.
  **/
  public boolean isPublishedInNetworkDirectory() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return networkDirectoryPublishingStatus_;
  }

  /**
   * Returns whether the writer has written all the data currently in the spooled file and is waiting for more data.
   * @return true if the writer is waiting for more data when the writer is producing an open spooled file with SCHEDULE(*IMMED) specified; false otherwise.
  **/
  public boolean isWaitingForData() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return waitingForDataStatus_;
  }

  /**
   * Returns whether the writer is waiting to get the device from a job that is printing directly to the printer.
   * @return true if the writer is waiting for the device, false otherwise.
  **/
  public boolean isWaitingForDevice() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return waitingForDeviceStatus_;
  }

  /**
   * Returns whether the writer is waiting for a reply to an inquiry message.
   * @return true if the writer is waiting for a reply, false otherwise.
  **/
  public boolean isWaitingForMessage() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return waitingForMessageStatus_;
  }

  /**
   * Returns whether the writer is started for this printer.
   * @return true if the writer is started, false otherwise.
  **/
  public boolean isWriterStarted() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return writerStarted_;
  }

  /**
   * Refreshes the information about this printer object from the system.
   * This method is implicitly called by the getXXX() methods on this class the first time.
   * Call this method explicitly to refresh the information returned by the various getXXX() methods.
  **/
  public void refresh() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    ProgramParameter[] parms = new ProgramParameter[5];
    parms[0] = new ProgramParameter(402); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(402)); // length of receiver variable
    parms[2] = new ProgramParameter(conv.stringToByteArray("RPTA0100")); // format name
    AS400Text text10 = new AS400Text(10, ccsid);
    parms[3] = new ProgramParameter(text10.toBytes(name_)); // device name
    parms[4] = new ProgramParameter(new byte[4]); // error code

    // Note this is not a real QGY API, even though it starts with those letters.
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYRPRTA.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    
    byte[] data = parms[0].getOutputData();

    //int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
    //int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
    //name_ = conv.byteArrayToString(data, 8, 10).trim();
    writerStarted_ = (data[18] == (byte)0xF1); // '1' means started
    writerStatus_ = data[19];
    deviceStatus_ = BinaryConverter.byteArrayToInt(data, 20);
    overallStatus_ = BinaryConverter.byteArrayToInt(data, 24);
    textDescription_ = conv.byteArrayToString(data, 28, 50).trim();
    startedByUser_ = conv.byteArrayToString(data, 80, 10).trim();
    writingStatus_ = data[90];
    waitingForMessageStatus_ = (data[91] == (byte)0xE8); // 'Y' or 'N'
    heldStatus_ = (data[92] == (byte)0xE8); // 'Y' or 'N'
    endPendingStatus_ = data[93];
    holdPendingStatus_ = data[94];
    betweenFilesStatus_ = (data[95] == (byte)0xE8); // 'Y' or 'N'
    betweenCopiesStatus_ = (data[96] == (byte)0xE8); // 'Y' or 'N'
    waitingForDataStatus_ = (data[97] == (byte)0xE8); // 'Y' or 'N'
    waitingForDeviceStatus_ = (data[98] == (byte)0xE8); // 'Y' or 'N'
    onJobQueueStatus_ = (data[99] == (byte)0xE8); // 'Y' or 'N'
    writerJobName_ = conv.byteArrayToString(data, 104, 10).trim();
    writerJobUser_ = conv.byteArrayToString(data, 114, 10).trim();
    writerJobNumber_ = conv.byteArrayToString(data, 124, 6);
    printerDeviceType_ = conv.byteArrayToString(data, 130, 10).trim();
    if (printerDeviceType_.equals(PRINTER_DEVICE_TYPE_SCS)) printerDeviceType_ = PRINTER_DEVICE_TYPE_SCS;
    else if (printerDeviceType_.equals(PRINTER_DEVICE_TYPE_IPDS)) printerDeviceType_ = PRINTER_DEVICE_TYPE_IPDS;
    numberOfSeparators_ = BinaryConverter.byteArrayToInt(data, 140);
    drawerForSeparators_ = BinaryConverter.byteArrayToInt(data, 144);
    alignForms_ = conv.byteArrayToString(data, 148, 10).trim();
    if (alignForms_.equals(FORM_ALIGNMENT_WRITER)) alignForms_ = FORM_ALIGNMENT_WRITER;
    else if (alignForms_.equals(FORM_ALIGNMENT_FILE)) alignForms_ = FORM_ALIGNMENT_FILE;
    outputQueueName_ = conv.byteArrayToString(data, 158, 10).trim();
    outputQueueLibrary_ = conv.byteArrayToString(data, 168, 10).trim();
    outputQueueStatus_ = data[178];
    networkDirectoryPublishingStatus_ = (data[179] == (byte)0xF1); // '0' or '1'
    formType_ = conv.byteArrayToString(data, 180, 10).trim();
    if (formType_.equals(FORM_TYPE_ALL)) formType_ = FORM_TYPE_ALL;
    else if (formType_.equals(FORM_TYPE_FORMS)) formType_ = FORM_TYPE_FORMS;
    else if (formType_.equals(FORM_TYPE_STANDARD)) formType_ = FORM_TYPE_STANDARD;
    messageOption_ = conv.byteArrayToString(data, 190, 10).trim();
    if (messageOption_.equals(MESSAGE_OPTION_STANDARD)) messageOption_ = MESSAGE_OPTION_STANDARD;
    else if (messageOption_.equals(MESSAGE_OPTION_NONE)) messageOption_ = MESSAGE_OPTION_NONE;
    else if (messageOption_.equals(MESSAGE_OPTION_INFO)) messageOption_ = MESSAGE_OPTION_INFO;
    else if (messageOption_.equals(MESSAGE_OPTION_INQUIRY)) messageOption_ = MESSAGE_OPTION_INQUIRY;
    automaticallyEndWriter_ = conv.byteArrayToString(data, 200, 10).trim();
    if (automaticallyEndWriter_.equals(WRITER_TIME_ALL_DONE)) automaticallyEndWriter_ = WRITER_TIME_ALL_DONE;
    else if (automaticallyEndWriter_.equals(WRITER_TIME_CURRENT_DONE)) automaticallyEndWriter_ = WRITER_TIME_CURRENT_DONE;
    else if (automaticallyEndWriter_.equals(WRITER_TIME_NONE)) automaticallyEndWriter_ = WRITER_TIME_NONE;
    allowDirectPrinting_ = conv.byteArrayToString(data, 210, 10).trim().equals("*YES"); // "*YES" or "*NO"
    messageQueueName_ = conv.byteArrayToString(data, 220, 10).trim();
    messageQueueLibrary_ = conv.byteArrayToString(data, 230, 10).trim();
    changesTakeEffect_ = conv.byteArrayToString(data, 242, 10).trim();
    if (changesTakeEffect_.equals(WRITER_TIME_ALL_DONE)) changesTakeEffect_ = WRITER_TIME_ALL_DONE;
    else if (changesTakeEffect_.equals(WRITER_TIME_CURRENT_DONE)) changesTakeEffect_ = WRITER_TIME_CURRENT_DONE;
    else if (changesTakeEffect_.equals(WRITER_TIME_NONE)) changesTakeEffect_ = WRITER_TIME_NONE;
    nextOutputQueueName_ = conv.byteArrayToString(data, 252, 10).trim();
    nextOutputQueueLibrary_ = conv.byteArrayToString(data, 262, 10).trim();
    nextFormType_ = conv.byteArrayToString(data, 272, 10).trim();
    if (nextFormType_.equals(FORM_TYPE_ALL)) nextFormType_ = FORM_TYPE_ALL;
    else if (nextFormType_.equals(FORM_TYPE_FORMS)) nextFormType_ = FORM_TYPE_FORMS;
    else if (nextFormType_.equals(FORM_TYPE_STANDARD)) nextFormType_ = FORM_TYPE_STANDARD;
    nextMessageOption_ = conv.byteArrayToString(data, 282, 10).trim();
    if (nextMessageOption_.equals(MESSAGE_OPTION_STANDARD)) nextMessageOption_ = MESSAGE_OPTION_STANDARD;
    else if (nextMessageOption_.equals(MESSAGE_OPTION_NONE)) nextMessageOption_ = MESSAGE_OPTION_NONE;
    else if (nextMessageOption_.equals(MESSAGE_OPTION_INFO)) nextMessageOption_ = MESSAGE_OPTION_INFO;
    else if (nextMessageOption_.equals(MESSAGE_OPTION_INQUIRY)) nextMessageOption_ = MESSAGE_OPTION_INQUIRY;
    nextFileSeparators_ = BinaryConverter.byteArrayToInt(data, 292);
    nextSeparatorDrawer_ = BinaryConverter.byteArrayToInt(data, 296);
    spooledFileName_ = conv.byteArrayToString(data, 300, 10).trim();
    jobName_ = conv.byteArrayToString(data, 310, 10).trim();
    jobUser_ = conv.byteArrayToString(data, 320, 10).trim();
    jobNumber_ = conv.byteArrayToString(data, 330, 6);
    spooledFileNumber_ = BinaryConverter.byteArrayToInt(data, 336);
    pageBeingWritten_ = BinaryConverter.byteArrayToInt(data, 340);
    totalPages_ = BinaryConverter.byteArrayToInt(data, 344);
    copiesLeftToProduce_ = BinaryConverter.byteArrayToInt(data, 348);
    totalCopies_ = BinaryConverter.byteArrayToInt(data, 352);
    advancedFunctionPrinting_ = conv.byteArrayToString(data, 356, 10).trim().equals("*YES"); // '*YES' or '*NO'
    messageKey_ = new byte[4];
    System.arraycopy(data, 366, messageKey_, 0, 4);
    if (system_.getVRM() >= AS400.generateVRM(5,2,0))
    {
      jobSystemName_ = conv.byteArrayToString(data, 381, 8).trim();
      spooledFileCreateDate_ = conv.byteArrayToString(data, 389, 7);
      spooledFileCreateTime_ = conv.byteArrayToString(data, 396, 6);
    }
    refreshed_ = true;
  }

  /**
   * Returns whether the printer supports Advanced Function Printing.
   * @return true if Advanced Function Printing is supported, false otherwise.
  **/
  public boolean supportsAFP() throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException
  {
    if (!refreshed_) refresh();
    return advancedFunctionPrinting_;
  }
}
    


