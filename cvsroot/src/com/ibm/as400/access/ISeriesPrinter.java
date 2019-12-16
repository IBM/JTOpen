///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ISeriesPrinter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 Represents a printer attached to an IBM i system.  This class provides access to the specified printer's attributes via the QGYRPRTA system API.
 <p>This class uses the remote command host server to obtain printer information.  Use the {@link com.ibm.as400.access.Printer Printer} class to retrieve similer information using the network print host server.
 <p>Example:
 <pre>
     AS400 system = new AS400("mySystem", "myUserID", "myPassword");
     ISeriesPrinter printer = new ISeriesPrinter(system, "myPrinter");
     String type = printer.getPrinterDeviceType();
     if (type == ISeriesPrinter.PRINTER_DEVICE_TYPE_SCS)
     {
         System.out.println(printer.getCopiesLeft());
     }
 </pre>
 @see  com.ibm.as400.access.Printer
 **/
// TBD: Add the following javadoc when ConfigurationDescriptionList class is created:
//<p>To determine a list of printer device descriptions on the IBM i system, use the {@link com.ibm.as400.access.config.ConfigurationDescriptionList ConfigurationDescriptionList} class.
// TBD: Add the following javadoc when config classes are created:
// @see  com.ibm.as400.access.config.ConfigurationDescriptionList
// @see  com.ibm.as400.access.config.PrinterDeviceDescription
public class ISeriesPrinter implements Serializable
{
    static final long serialVersionUID = -609299009457592116L;

    /**
     Device status constant indicating the device is in the varied off status.  Constant has a value of 0.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_VARIED_OFF = 0;

    /**
     Device status constant indicating the device is in the AS/36 disabled status.  Constant has a value of 5.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_AS36_DISABLED = 5;

    /**
     Device status constant indicating the device is in the vary off pending status.  Constant has a value of 10.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_VARY_OFF_PENDING = 10;

    /**
     Device status constant indicating the device is in the vary on pending status.  Constant has a value of 20.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_VARY_ON_PENDING = 20;

    /**
     Device status constant indicating the device is in the varied on status.  Constant has a value of 30.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_VARIED_ON = 30;

    /**
     Device status constant indicating the device is in the connect pending status.  Constant has a value of 40.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_CONNECT_PENDING = 40;

    /**
     Device status constant indicating the device is in the signon display status.  Constant has a value of 50.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_SIGNON_DISPLAY = 50;

    /**
     Device status constant indicating the device is in the active status.  Constant has a value of 60.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_ACTIVE = 60;

    /**
     Device status constant indicating the device is in the AS/36 enabled status.  Constant has a value of 62.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_AS36_ENABLED = 62;

    /**
     Device status constant indicating the device is in the active reader status.  Constant has a value of 63.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_ACTIVE_READER = 63;

    /**
     Device status constant indicating the device is in the active writer status.  Constant has a value of 66.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_ACTIVE_WRITER = 66;

    /**
     Device status constant indicating the device is in the held status.  Constant has a value of 70.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_HELD = 70;

    /**
     Device status constant indicating the device is in the powered off status.  Constant has a value of 75.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_POWERED_OFF = 75;

    /**
     Device status constant indicating the device is in the RCYPND status.  Constant has a value of 80.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_RCYPND = 80;

    /**
     Device status constant indicating the device is in the RCYCNL status.  Constant has a value of 90.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_RCYCNL = 90;

    /**
     Device status constant indicating the device is in the system request status.  Constant has a value of 95.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_SYSTEM_REQUEST = 95;

    /**
     Device status constant indicating the device is in the failed status.  Constant has a value of 100.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_FAILED = 100;

    /**
     Device status constant indicating the device is in the failed reader status.  Constant has a value of 103.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_FAILED_READER = 103;

    /**
     Device status constant indicating the device is in the failed writer status.  Constant has a value of 106.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_FAILED_WRITER = 106;

    /**
     Device status constant indicating the device is in the diagnostic mode status.  Constant has a value of 110.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_DIAGNOSTIC_MODE = 110;

    /**
     Device status constant indicating the device is in the damaged status.  Constant has a value of 111.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_DAMAGED = 111;

    /**
     Device status constant indicating the device is in the locked status.  Constant has a value of 112.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_LOCKED = 112;

    /**
     Device status constant indicating the device is in the unknown status.  Constant has a value of 113.
     @see  #getDeviceStatus
     **/
    public static final int DEVICE_STATUS_UNKNOWN = 113;


    /**
     Form alignment constant indicating that the writer determines when the message is sent.  Constant has a value of "*WTR".
     @see  #getFormAlignmentMessageTime
     **/
    public static final String FORM_ALIGNMENT_WRITER = "*WTR";

    /**
     Form alignment constant indicating that control of the page alignment is specified by each file.  Constant has a value of "*FILE".
     @see  #getFormAlignmentMessageTime
     **/
    public static final String FORM_ALIGNMENT_FILE = "*FILE";


    /**
     Form type constant indicating that the writer is started with the option to print all spooled files of any form type.  Constant has a value of "*ALL".
     @see  #getFormType
     **/
    public static final String FORM_TYPE_ALL = "*ALL";

    /**
     Form type constant indicating that the writer is started with the option to print all the spooled files with the same form type before using a different form type.  Constant has a value of "*FORMS".
     @see  #getFormType
     **/
    public static final String FORM_TYPE_FORMS = "*FORMS";

    /**
     Form type constant indicating that the writer is started with the option to print all the spooled files with a form type of *STD.  Constant has a value of "*STD".
     @see  #getFormType
     **/
    public static final String FORM_TYPE_STANDARD = "*STD";


    /**
     Message option constant indicating that a message is sent to the message queue.  Constant has a value of "*MSG".
     @see  #getMessageOption
     **/
    public static final String MESSAGE_OPTION_STANDARD = "*MSG";

    /**
     Message option constant indicating that no message is sent to the message queue.  Constant has a value of "*NOMSG".
     @see  #getMessageOption
     **/
    public static final String MESSAGE_OPTION_NONE = "*NOMSG";

    /**
     Message option constant indicating that an informational message is sent to the message queue.  Constant has a value of "*INFOMSG".
     @see  #getMessageOption
     **/
    public static final String MESSAGE_OPTION_INFO = "*INFOMSG";

    /**
     Message option constant indicating that an inquiry message is sent to the message queue.  Constant has a value of "*INQMSG".
     @see  #getMessageOption
     **/
    public static final String MESSAGE_OPTION_INQUIRY = "*INQMSG";


    /**
     Output queue status constant indicating that the output queue is held.  Constant has an EBCDIC value of "H".
     @see  #getOutputQueueStatus
     **/
    public static final int OUTPUT_QUEUE_STATUS_HELD = 0x00C8; // 'H'

    /**
     Output queue status constant indicating that the output queue is released.  Constant has an EBCDIC value of "R".
     @see  #getOutputQueueStatus
     **/
    public static final int OUTPUT_QUEUE_STATUS_RELEASED = 0x00D9; // 'R'


    /**
     Overall status constant indicating that overall status of the logical printer is unavailable.  Constant has a value of 1.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_UNAVAILABLE = 1;

    /**
     Overall status constant indicating that overall status of the logical printer is powered off or not yet available.  Constant has a value of 2.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_NOT_YET_AVAILABLE = 2;

    /**
     Overall status constant indicating that overall status of the logical printer is stopped.  Constant has a value of 3.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_STOPPED = 3;

    /**
     Overall status constant indicating that overall status of the logical printer is message waiting.  Constant has a value of 4.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_MESSAGE_WAITING = 4;

    /**
     Overall status constant indicating that overall status of the logical printer is held.  Constant has a value of 5.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_HELD = 5;

    /**
     Overall status constant indicating that overall status of the logical printer is stop (pending).  Constant has a value of 6.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_STOP_PENDING = 6;

    /**
     Overall status constant indicating that overall status of the logical printer is hold (pending).  Constant has a value of 7.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_HOLD_PENDING = 7;

    /**
     Overall status constant indicating that overall status of the logical printer is waiting for printer.  Constant has a value of 8.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_WAITING_FOR_PRINTER = 8;

    /**
     Overall status constant indicating that overall status of the logical printer is waiting to start.  Constant has a value of 9.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_WAITING_TO_START = 9;

    /**
     Overall status constant indicating that overall status of the logical printer is printing.  Constant has a value of 10.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_PRINTING = 10;

    /**
     Overall status constant indicating that overall status of the logical printer is waiting for printer output.  Constant has a value of 11.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_WAITING_FOR_PRINTER_OUTPUT = 11;

    /**
     Overall status constant indicating that overall status of the logical printer is connect pending.  Constant has a value of 12.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_CONNECT_PENDING = 12;

    /**
     Overall status constant indicating that overall status of the logical printer is powered off.  Constant has a value of 13.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_POWERED_OFF = 13;

    /**
     Overall status constant indicating that overall status of the logical printer is unusable.  Constant has a value of 14.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_UNUSABLE = 14;

    /**
     Overall status constant indicating that overall status of the logical printer is being serviced.  Constant has a value of 15.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_BEING_SERVICED = 15;

    /**
     Overall status constant indicating that overall status of the logical printer is unknown.  Constant has a value of 999.
     @see  #getOverallStatus
     **/
    public static final int OVERALL_STATUS_UNKNOWN = 999;


    /**
     Pending status constant indicating that no command was issued.  Constant has an EBCDIC value of "N".
     @see  #getEndPendingStatus
     @see  #getHoldPendingStatus
     **/
    public static final int PENDING_STATUS_NONE = 0x00D5; // 'N'

    /**
     Pending status constant indicating that the writer ends as soon as its output buffers are empty (*IMMED).  Constant has an EBCDIC value of "I".
     @see  #getEndPendingStatus
     @see  #getHoldPendingStatus
     **/
    public static final int PENDING_STATUS_IMMEDIATE = 0x00C9; // 'I'

    /**
     Pending status constant indicating that the writer ends after the current copy of the spooled file has been printed (*CNTRLD).  Constant has an EBCDIC value of "C".
     @see  #getEndPendingStatus
     @see  #getHoldPendingStatus
     **/
    public static final int PENDING_STATUS_CONTROLLED = 0x00C3; // 'C'

    /**
     Pending status constant indicating that the writer ends at the end of the page (*PAGEEND).  Constant has an EBCDIC value of "P".
     @see  #getEndPendingStatus
     @see  #getHoldPendingStatus
     **/
    public static final int PENDING_STATUS_PAGE_END = 0x00D7; // 'P'


    /**
     Printer device type constant indicating that the type of the printer is SNA (Systems Network Architecture) character string.  Constant has a value of "*SCS".
     @see  #getPrinterDeviceType
     **/
    public static final String PRINTER_DEVICE_TYPE_SCS = "*SCS";

    /**
     Printer device type constant indicating that the type of the printer is Intelligent Printer Data Stream.  Constant has a value of "*IPDS".
     @see  #getPrinterDeviceType
     **/
    public static final String PRINTER_DEVICE_TYPE_IPDS = "*IPDS";


    /**
     Writer status constant indicating that the status of the writer is started.  Constant has a value of 1.
     @see  #getWriterStatus
     **/
    public static final int WRITER_STATUS_STARTED = 0x01;

    /**
     Writer status constant indicating that the status of the writer is ended.  Constant has a value of 2.
     @see  #getWriterStatus
     **/
    public static final int WRITER_STATUS_ENDED = 0x02;

    /**
     Writer status constant indicating that the status of the writer is on job queue.  Constant has a value of 3.
     @see  #getWriterStatus
     **/
    public static final int WRITER_STATUS_ON_JOB_QUEUE = 0x03;

    /**
     Writer status constant indicating that the status of the writer is held.  Constant has a value of 4.
     @see  #getWriterStatus
     **/
    public static final int WRITER_STATUS_HELD = 0x04;

    /**
     Writer status constant indicating that the status of the writer is message waiting.  Constant has a value of 5.
     @see  #getWriterStatus
     **/
    public static final int WRITER_STATUS_MESSAGE_WAITING = 0x05;


    /**
     Writer time constant indicating that the action will occur when there are no ready spooled files.  Constant has a value of "*NORDYF".
     @see  #getWriterChangeTime
     @see  #getWriterEndTime
     **/
    public static final String WRITER_TIME_ALL_DONE = "*NORDYF";

    /**
     Writer time constant indicating that the action will occur when the current spooled file has been printed.  Constant has a value of "*FILEEND".
     @see  #getWriterChangeTime
     @see  #getWriterEndTime
     **/
    public static final String WRITER_TIME_CURRENT_DONE = "*FILEEND";

    /**
     Writer time constant indicating that the action will not occur, it will wait for more spooled files.  Constant has a value of "*NO".
     @see  #getWriterChangeTime
     @see  #getWriterEndTime
     **/
    public static final String WRITER_TIME_WAIT = "*NO";

    /**
     Writer time constant indicating that there are no pending changes to the writer.  Constant has a value of "".
     @see  #getWriterChangeTime
     @see  #getWriterEndTime
     **/
    public static final String WRITER_TIME_NONE = "";


    /**
     Writing status constant indicating that the writer is in writing status.  Constant has an EBCDIC value of "Y".
     @see  #getWritingStatus
     **/
    public static final int WRITING_STATUS_WRITING = 0x00E8; // 'Y'

    /**
     Writing status constant indicating that the writer is not in writing status.  Constant has an EBCDIC value of "N".
     @see  #getWritingStatus
     **/
    public static final int WRITING_STATUS_NOT_WRITING = 0x00D5; // 'N'

    /**
     Writing status constant indicating that the writer is writing the file separators.  Constant has an EBCDIC value of "S".
     @see  #getWritingStatus
     **/
    public static final int WRITING_STATUS_FILE_SEPARATORS = 0x00E2; // 'S'


    // The server where the printer is located.
    private AS400 system_;
    // Printer name.
    private String name_;
    // Flag that indicates that all properties were set by refresh().
    private boolean refreshed_ = false;

    private boolean writerStarted_;
    private int writerStatus_;
    private int deviceStatus_;
    private int overallStatus_;
    private String description_;  // Text description.
    private String starterUser_;  // Started by user.
    private int writingStatus_;
    private boolean waitingForMessage_;  // Waiting for message status.
    private boolean held_;  // Held status.
    private int endPendingStatus_;
    private int holdPendingStatus_;
    private boolean betweenFiles_;  // Between files status.
    private boolean betweenCopies_;  // Between copies status.
    private boolean waitingForData_;  // Waiting for data status.
    private boolean waitingForDevice_;  // Waiting for device status.
    private boolean onJobQueue_;  // On job queue status.
    private String writerJobName_;
    private String writerJobUser_;  // Writer job user name.
    private String writerJobNumber_;
    private String printerDeviceType_;
    private int numberOfSeparators_;
    private int separatorDrawer_;  // Drawer for separators.
    private String formAlignmentMessageTime_;  // Align forms.
    private String outputQueueName_;
    private String outputQueueLibraryName_;
    private int outputQueueStatus_;
    private boolean publishedInNetworkDirectory_;  // Network directory publishing status.
    private String formType_;
    private String messageOption_;
    private String writerEndTime_;  // Automatically end writer.
    private boolean allowsDirectPrinting_;  // Allow direct printing.
    private String messageQueueName_;
    private String messageQueueLibraryName_;
    private String writerChangeTime_;  // Changes take effect.
    private String pendingOutputQueueName_;  // Next output queue name.
    private String pendingOutputQueueLibraryName_;  // Next output queue library name.
    private String pendingFormType_;  // Next form type.
    private String pendingMessageOption_;  // Next message option.
    private int pendingNumberOfSeparators_;  // Next file separators.
    private int pendingSeparatorDrawer_;  // Next separator drawer.
    private String spooledFileName_;
    private String spooledFileJobName_;  // Job name.
    private String spooledFileJobUser_;  // User name.
    private String spooledFileJobNumber_;  // Job number.
    private int spooledFileNumber_;
    private int pageBeingWritten_;
    private int totalPages_;
    private int copiesLeft_;  // Copies left to produce.
    private int totalCopies_;
    private boolean supportsAFP_;  // Advanced function printing.
    private byte[] writerMessageKey_;  // Message key.
    private String spooledFileJobSystem_;  // Job system name.
    private String spooledFileCreationDate_;  // Spooled file create date.
    private String spooledFileCreationTime_;  // Spooled file create time.

    /**
     Constructs an ISeriesPrinter object with the specified name.  If a printer with the specified name does not exist on the specified system, an exception will be thrown when {@link #refresh refresh()} is called, either implicitly or explicitly.
     @param  system  The system object representing the system on which the printer exists.
     @param  name  The name of the printer.
     **/
    public ISeriesPrinter(AS400 system, String name)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing ISeriesPrinter object, system: " + system + ", name: " + name);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (name.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'name' is not valid: '" + name + "'");
            throw new ExtendedIllegalArgumentException("name (" + name + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        system_ = system;
        name_ = name.toUpperCase().trim();
    }

    /**
     Returns whether or not the printer writer allows the printer to be allocated to a job that prints directly to the printer.
     @return  true if direct printing is allowed; false if it is not allowed.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean allowsDirectPrinting() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return allowsDirectPrinting_;
    }

    /**
     Returns whether or not the specified object is equal to this object.
     @return  true if the specified object is an ISeriesPrinter and the following contract is true:
     <pre>
         obj.getSystem().equals(this.getSystem()) && obj.getName().equals(this.getName())
     </pre>
     Otherwise, false is returned.
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
     Returns the number of copies left to be printed.
     @return  The number of copies left, or 0 if no file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getCopiesLeft() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return copiesLeft_;
    }

    /**
     Returns the text description of the printer device.
     @return  The text description.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getDescription() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return description_;
    }

    /**
     Returns the status of the printer device.
     Possible values are:
     <ul>
     <li>{@link #DEVICE_STATUS_VARIED_OFF DEVICE_STATUS_VARIED_OFF}
     <li>{@link #DEVICE_STATUS_AS36_DISABLED DEVICE_STATUS_AS36_DISABLED}
     <li>{@link #DEVICE_STATUS_VARY_OFF_PENDING DEVICE_STATUS_VARY_OFF_PENDING}
     <li>{@link #DEVICE_STATUS_VARY_ON_PENDING DEVICE_STATUS_VARY_ON_PENDING}
     <li>{@link #DEVICE_STATUS_VARIED_ON DEVICE_STATUS_VARIED_ON}
     <li>{@link #DEVICE_STATUS_CONNECT_PENDING DEVICE_STATUS_CONNECT_PENDING}
     <li>{@link #DEVICE_STATUS_SIGNON_DISPLAY DEVICE_STATUS_SIGNON_DISPLAY}
     <li>{@link #DEVICE_STATUS_ACTIVE DEVICE_STATUS_ACTIVE}
     <li>{@link #DEVICE_STATUS_AS36_ENABLED DEVICE_STATUS_AS36_ENABLED}
     <li>{@link #DEVICE_STATUS_ACTIVE_READER DEVICE_STATUS_ACTIVE_READER}
     <li>{@link #DEVICE_STATUS_ACTIVE_WRITER DEVICE_STATUS_ACTIVE_WRITER}
     <li>{@link #DEVICE_STATUS_HELD DEVICE_STATUS_HELD}
     <li>{@link #DEVICE_STATUS_POWERED_OFF DEVICE_STATUS_POWERED_OFF}
     <li>{@link #DEVICE_STATUS_RCYPND DEVICE_STATUS_RCYPND}
     <li>{@link #DEVICE_STATUS_RCYCNL DEVICE_STATUS_RCYCNL}
     <li>{@link #DEVICE_STATUS_SYSTEM_REQUEST DEVICE_STATUS_SYSTEM_REQUEST}
     <li>{@link #DEVICE_STATUS_FAILED DEVICE_STATUS_FAILED}
     <li>{@link #DEVICE_STATUS_FAILED_READER DEVICE_STATUS_FAILED_READER}
     <li>{@link #DEVICE_STATUS_FAILED_WRITER DEVICE_STATUS_FAILED_WRITER}
     <li>{@link #DEVICE_STATUS_DIAGNOSTIC_MODE DEVICE_STATUS_DIAGNOSTIC_MODE}
     <li>{@link #DEVICE_STATUS_DAMAGED DEVICE_STATUS_DAMAGED}
     <li>{@link #DEVICE_STATUS_LOCKED DEVICE_STATUS_LOCKED}
     <li>{@link #DEVICE_STATUS_UNKNOWN DEVICE_STATUS_UNKNOWN}
     </ul>
     @return  The printer device status.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getDeviceStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return deviceStatus_;
    }

    /**
     Returns whether an End Writer (ENDWTR) command has been issued for this writer.
     Possible values are:
     <ul>
     <li>{@link #PENDING_STATUS_NONE PENDING_STATUS_NONE} - No ENDWTR command was issued.
     <li>{@link #PENDING_STATUS_IMMEDIATE PENDING_STATUS_IMMEDIATE} - The writer ends as soon as its output buffers are empty.
     <li>{@link #PENDING_STATUS_CONTROLLED PENDING_STATUS_CONTROLLED} - The writer ends after the current copy of the spooled file has been printed.
     <li>{@link #PENDING_STATUS_PAGE_END PENDING_STATUS_PAGE_END} - The writer ends at the end of the page.
     </ul>
     @return  The pending status.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getEndPendingStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return endPendingStatus_;
    }

    /**
     Returns the time at which the forms alignment message will be sent.
     Possible values are:
     <ul>
     <li>{@link #FORM_ALIGNMENT_WRITER FORM_ALIGNMENT_WRITER}
     <li>{@link #FORM_ALIGNMENT_FILE FORM_ALIGNMENT_FILE}
     </ul>
     @return  The form alignment message setting.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getFormAlignmentMessageTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return formAlignmentMessageTime_;
    }

    /**
     Returns the type of form being used to print the spooled file.
     Possible values are:
     <ul>
     <li>{@link #FORM_TYPE_ALL FORM_TYPE_ALL}
     <li>{@link #FORM_TYPE_FORMS FORM_TYPE_FORMS}
     <li>{@link #FORM_TYPE_STANDARD FORM_TYPE_STANDARD}
     <li>A user-specified form type.
     </ul>
     @return  The form type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getFormType() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return formType_;
    }

    /**
     Returns whether a Hold Writer (HLDWTR) command has been issued for this writer.
     Possible values are:
     <ul>
     <li>{@link #PENDING_STATUS_NONE PENDING_STATUS_NONE} - No HLDWTR command was issued.
     <li>{@link #PENDING_STATUS_IMMEDIATE PENDING_STATUS_IMMEDIATE} - The writer is held as soon as its output buffers are empty.
     <li>{@link #PENDING_STATUS_CONTROLLED PENDING_STATUS_CONTROLLED} - The writer is held after the current copy of the file has been printed.
     <li>{@link #PENDING_STATUS_PAGE_END PENDING_STATUS_PAGE_END} - The writer is held at the end of the page.
     </ul>
     @return  The pending status.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getHoldPendingStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return holdPendingStatus_;
    }

    /**
     Returns the message option for sending a message to the message queue when this form is finished.
     Possible values are:
     <ul>
     <li>{@link #MESSAGE_OPTION_STANDARD MESSAGE_OPTION_STANDARD} - A message is sent to the message queue.
     <li>{@link #MESSAGE_OPTION_NONE MESSAGE_OPTION_NONE} - No message is sent to the message queue.
     <li>{@link #MESSAGE_OPTION_INFO MESSAGE_OPTION_INFO} - An informational message is sent to the message queue.
     <li>{@link #MESSAGE_OPTION_INQUIRY MESSAGE_OPTION_INQUIRY} - An inquiry message is sent to the message queue.
     </ul>
     @return  The message option.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getMessageOption() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return messageOption_;
    }

    /**
     Returns the fully qualified integrated file system pathname of the message queue associated with this printer.
     @return  The message queue path, or "" if there is no associated message queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.MessageQueue
     **/
    public String getMessageQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        if (messageQueueLibraryName_.length() == 0) return "";
        return QSYSObjectPathName.toPath(messageQueueLibraryName_, messageQueueName_, "MSGQ");
    }

    /**
     Returns the printer name.
     @return  The printer name.
     @see  #ISeriesPrinter
     **/
    public String getName()
    {
        return name_;
    }

    /**
     Returns the number of separator pages to be printed.
     @return  The number of separators, or -1 if the number of separator pages is specified by each file.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getNumberOfSeparators() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return numberOfSeparators_;
    }

    /**
     Returns the fully qualified integrated file system pathname of the output queue associated with this printer from which spooled files are selected for printing.
     @return  The output queue path, or "" if there is no associated output queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.OutputQueue
     **/
    public String getOutputQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        if (outputQueueLibraryName_.length() == 0) return "";
        return QSYSObjectPathName.toPath(outputQueueLibraryName_, outputQueueName_, "OUTQ");
    }

    /**
     Returns the status of the output queue from which spooled files are being selected for printing.
     Possible values are:
     <ul>
     <li>{@link #OUTPUT_QUEUE_STATUS_HELD OUTPUT_QUEUE_STATUS_HELD} - The output queue is held.
     <li>{@link #OUTPUT_QUEUE_STATUS_RELEASED OUTPUT_QUEUE_STATUS_RELEASED} - The output queue is released.
     </ul>
     @return  The output queue status.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getOutputQueueStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return outputQueueStatus_;
    }

    /**
     Returns the overall status of the logical printer.
     Possible values are:
     <ul>
     <li>{@link #OVERALL_STATUS_UNAVAILABLE OVERALL_STATUS_UNAVAILABLE}
     <li>{@link #OVERALL_STATUS_NOT_YET_AVAILABLE OVERALL_STATUS_NOT_YET_AVAILABLE}
     <li>{@link #OVERALL_STATUS_STOPPED OVERALL_STATUS_STOPPED}
     <li>{@link #OVERALL_STATUS_MESSAGE_WAITING OVERALL_STATUS_MESSAGE_WAITING}
     <li>{@link #OVERALL_STATUS_HELD OVERALL_STATUS_HELD}
     <li>{@link #OVERALL_STATUS_STOP_PENDING OVERALL_STATUS_STOP_PENDING}
     <li>{@link #OVERALL_STATUS_HOLD_PENDING OVERALL_STATUS_HOLD_PENDING}
     <li>{@link #OVERALL_STATUS_WAITING_FOR_PRINTER OVERALL_STATUS_WAITING_FOR_PRINTER}
     <li>{@link #OVERALL_STATUS_WAITING_TO_START OVERALL_STATUS_WAITING_TO_START}
     <li>{@link #OVERALL_STATUS_PRINTING OVERALL_STATUS_PRINTING}
     <li>{@link #OVERALL_STATUS_WAITING_FOR_PRINTER_OUTPUT OVERALL_STATUS_WAITING_FOR_PRINTER_OUTPUT}
     <li>{@link #OVERALL_STATUS_CONNECT_PENDING OVERALL_STATUS_CONNECT_PENDING}
     <li>{@link #OVERALL_STATUS_POWERED_OFF OVERALL_STATUS_POWERED_OFF}
     <li>{@link #OVERALL_STATUS_UNUSABLE OVERALL_STATUS_UNUSABLE}
     <li>{@link #OVERALL_STATUS_BEING_SERVICED OVERALL_STATUS_BEING_SERVICED}
     <li>{@link #OVERALL_STATUS_UNKNOWN OVERALL_STATUS_UNKNOWN}
     </ul>
     @return  The overall status.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getOverallStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return overallStatus_;
    }

    /**
     Returns the page number in the spooled file that is currently being processed by the writer.
     The page number returned may be lower or higher than the actual page number being printed
     because of buffering done by the system.
     @return  The page number being written, or 0 if no spooled file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getPageBeingWritten() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return pageBeingWritten_;
    }

    /**
     Returns the name of the next form type to be printed.
     Possible values are:
     <ul>
     <li>{@link #FORM_TYPE_ALL FORM_TYPE_ALL} - The writer is changed with the option to print all spooled files of any form type.
     <li>{@link #FORM_TYPE_FORMS FORM_TYPE_FORMS} - The writer is changed with the option to print all the spooled files with the same form type before using a different form type.
     <li>{@link #FORM_TYPE_STANDARD FORM_TYPE_STANDARD} - The writer is changed with the option to print all the spooled files with a form type of *STD.
     <li>A user-specified form type - The writer is changed with the option to print all the spooled files with the form type name that was specified.
     <li>"" - No change has been made to this writer.
     </ul>
     @return  The form type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getFormType
     **/
    public String getPendingFormType() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return pendingFormType_;
    }

    /**
     Returns the message option for sending a message to the message queue when the next form type is finished.
     Possible values are:
     <ul>
     <li>{@link #MESSAGE_OPTION_STANDARD MESSAGE_OPTION_STANDARD} - A message is sent to the message queue.
     <li>{@link #MESSAGE_OPTION_NONE MESSAGE_OPTION_NONE} - No message is sent to the message queue.
     <li>{@link #MESSAGE_OPTION_INFO MESSAGE_OPTION_INFO} - An informational message is sent to the message queue.
     <li>{@link #MESSAGE_OPTION_INQUIRY MESSAGE_OPTION_INQUIRY} - An inquiry message is sent to the message queue.
     <li>"" - No change is pending.
     </ul>
     @return  The message option.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getMessageOption
     **/
    public String getPendingMessageOption() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return pendingMessageOption_;
    }

    /**
     Returns the next number of separator pages to be printed when the change to the writer takes place.
     @return  The number of separators, or -1 if the number of separator pages is specified by each file, or -10 if there is no pending change to the writer.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getNumberOfSeparators
     **/
    public int getPendingNumberOfSeparators() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return pendingNumberOfSeparators_;
    }

    /**
     Returns the fully qualified integrated file system pathname of the next output queue.
     @return  The output queue path, or "" if no changes have been made to the writer.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getOutputQueue
     @see  com.ibm.as400.access.OutputQueue
     **/
    public String getPendingOutputQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        if (pendingOutputQueueLibraryName_.length() == 0) return "";
        return QSYSObjectPathName.toPath(pendingOutputQueueLibraryName_, pendingOutputQueueName_, "OUTQ");
    }

    /**
     Returns the drawer from which to take the separator pages if there is a change to the writer.  Possible values are:
     <ul>
     <li>1 - The first drawer.
     <li>2 - The second drawer.
     <li>3 - The third drawer.
     <li>-1 - Separator pages print from the same drawer that the spooled file prints from.  If you specify a drawer different from the spooled file that contains colored or different type paper, the page separator is more identifiable.
     <li>-2 - Separator pages print from the separator drawer specified in the printer device description.
     <li>-10 - There is no pending change to the writer.
     </ul>
     @return  The separator drawer.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getSeparatorDrawer
     **/
    // TBD: Add when PrinterDeviceDescription class is created:
    // @see  com.ibm.as400.access.config.PrinterDeviceDescription
    public int getPendingSeparatorDrawer() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return pendingSeparatorDrawer_;
    }

    /**
     Returns the type of printer being used to print the spooled file.
     Possible values are:
     <ul>
     <li>{@link #PRINTER_DEVICE_TYPE_SCS DEVICE_TYPE_SCS} - SNA (Systems Network Architecture) character string.
     <li>{@link #PRINTER_DEVICE_TYPE_IPDS DEVICE_TYPE_IPDS} - Intelligent Printer Data Stream (tm).
     </ul>
     @return  The printer device type.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getPrinterDeviceType() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return printerDeviceType_;
    }

    /**
     Returns the drawer from which the job and file separator pages are to be taken.
     Possible values are:
     <ul>
     <li>1 - The first drawer.
     <li>2 - The second drawer.
     <li>3 - The third drawer.
     <li>-1 - Separator pages print from the same drawer that the spooled file prints from.  If you specify a drawer different from the spooled file that contains colored or different type paper, the page separator is more identifiable.
     <li>-2 - Separator pages print from the separator drawer specified in the printer device description.
     </ul>
     @return  The separator drawer.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    // TBD: Add when PrinterDeviceDescription class is added:
    // @see  com.ibm.as400.access.config.PrinterDeviceDescription
    public int getSeparatorDrawer() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return separatorDrawer_;
    }

    /**
     Returns the date and time the spooled file was created on the system.
     @return  The spooled file creation date, or null if no spooled file is printing.  If the system is not running system operating system release V5R2M0 or higher, null is returned.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Date getSpooledFileCreationDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        if (spooledFileCreationDate_ == null || spooledFileCreationDate_.length() == 0) return null;
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Integer.parseInt(spooledFileCreationDate_.substring(0, 3)) + 1900, // Year.
                Integer.parseInt(spooledFileCreationDate_.substring(3, 5)) - 1, // Month is zero based.
                Integer.parseInt(spooledFileCreationDate_.substring(5, 7)), // Day.
                Integer.parseInt(spooledFileCreationTime_.substring(0, 2)), // Hour.
                Integer.parseInt(spooledFileCreationTime_.substring(2, 4)), // Minute.
                Integer.parseInt(spooledFileCreationTime_.substring(4, 6))); // Second.
        return cal.getTime();
    }

    /**
     Returns the name of the job that created the spooled file currently being processed by the writer.
     @return  The job name, or "" if no spooled file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.Job
     **/
    public String getSpooledFileJobName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return spooledFileJobName_;
    }

    /**
     Returns the number of the job that created the spooled file currently being processed by the writer.
     @return  The job number, or "" if no spooled file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.Job
     **/
    public String getSpooledFileJobNumber() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return spooledFileJobNumber_;
    }

    /**
     Returns the name of the system where the job that created the spooled file ran.
     @return  The system name, or "" if no spooled file is printing, or null if the system is not running system operating system release V5R2M0 or higher.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getSpooledFileJobSystem() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return spooledFileJobSystem_;
    }

    /**
     Returns the user of the job that created the spooled file currently being processed by the writer.
     @return  The user name, or "" if no spooled file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.Job
     @see  com.ibm.as400.access.User
     **/
    public String getSpooledFileJobUser() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return spooledFileJobUser_;
    }

    /**
     Returns the name of the spooled file currently being processed by the writer.
     @return  The spooled file name, or "" if no spooled file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getSpooledFileName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return spooledFileName_;
    }

    /**
     Returns the number of the spooled file currently being processed by the writer.
     @return  The spooled file number, or 0 if no spooled file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getSpooledFileNumber() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return spooledFileNumber_;
    }

    /**
     Returns the name of the user that started the writer.
     @return  The user name.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.User
     **/
    public String getStarterUser() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return starterUser_;
    }

    /**
     Returns the system object representing the system on which the printer exists.
     @return  The system object representing the system on which the printer exists.
     @see  #ISeriesPrinter
     **/
    public AS400 getSystem()
    {
        return system_;
    }

    /**
     Returns the total number of copies to be printed.
     @return  The number of copies.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getTotalCopies() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return totalCopies_;
    }

    /**
     Returns the total number of pages in the spooled file.
     @return  The number of pages, or 0 if no spooled file is printing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getTotalPages() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return totalPages_;
    }

    /**
     Returns the time at which the pending changes to the writer take effect.
     Possible values are:
     <ul>
     <li>{@link #WRITER_TIME_ALL_DONE WRITER_TIME_ALL_DONE}
     <li>{@link #WRITER_TIME_CURRENT_DONE WRITER_TIME_CURRENT_DONE}
     <li>{@link #WRITER_TIME_NONE WRITER_TIME_NONE}
     </ul>
     @return  The writer change setting.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getWriterChangeTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerChangeTime_;
    }

    /**
     Returns when to end the writer if it is to end automatically.
     Possible values are:
     <ul>
     <li>{@link #WRITER_TIME_ALL_DONE WRITER_TIME_ALL_DONE}
     <li>{@link #WRITER_TIME_CURRENT_DONE WRITER_TIME_CURRENT_DONE}
     <li>{@link #WRITER_TIME_WAIT WRITER_TIME_WAIT}
     </ul>
     @return  The writer end time setting.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String getWriterEndTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerEndTime_;
    }

    /**
     Returns the job name of the printer writer.
     @return  The job name.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getWriterJobNumber
     @see  #getWriterJobUser
     @see  com.ibm.as400.access.Job
     **/
    public String getWriterJobName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerJobName_;
    }

    /**
     Returns the job number of the printer writer.
     @return  The job number.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getWriterJobName
     @see  #getWriterJobUser
     @see  com.ibm.as400.access.Job
     **/
    public String getWriterJobNumber() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerJobNumber_;
    }

    /**
     Returns the name of the system user.
     @return  The user name.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getWriterJobName
     @see  #getWriterJobNumber
     @see  com.ibm.as400.access.Job
     @see  com.ibm.as400.access.User
     **/
    public String getWriterJobUser() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerJobUser_;
    }

    /**
     Returns the key to the message that the writer is waiting for a reply.
     @return  The 4-byte message key, which will consist of all 0x40's if the writer is not waiting for a reply to an inquiry message.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public byte[] getWriterMessageKey() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerMessageKey_;
    }

    /**
     Returns the status of the writer for this printer.
     Possible values are:
     <ul>
     <li>{@link #WRITER_STATUS_STARTED WRITER_STATUS_STARTED}
     <li>{@link #WRITER_STATUS_ENDED WRITER_STATUS_ENDED}
     <li>{@link #WRITER_STATUS_ON_JOB_QUEUE WRITER_STATUS_ON_JOB_QUEUE}
     <li>{@link #WRITER_STATUS_HELD WRITER_STATUS_HELD}
     <li>{@link #WRITER_STATUS_MESSAGE_WAITING WRITER_STATUS_MESSAGE_WAITING}
     </ul>
     @return  The writer status.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getWriterStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerStatus_;
    }

    /**
     Returns whether the printer writer is in writing status.
     Possible values are:
     <ul>
     <li>{@link #WRITING_STATUS_WRITING WRITING_STATUS_WRITING} - The writer is in writing status.
     <li>{@link #WRITING_STATUS_NOT_WRITING WRITING_STATUS_NOT_WRITING} - The writer is not in writing status.
     <li>{@link #WRITING_STATUS_FILE_SEPARATORS WRITING_STATUS_FILE_SEPARATORS} - The writer is writing the file separators.
     </ul>
     @return  The writing status.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getWritingStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writingStatus_;
    }

    /**
     Returns the hash code value for the <i>name</i> of this ISeriesPrinter object.
     @return  The hash code.
     **/
    public int hashCode()
    {
        return name_.hashCode();
    }

    /**
     Returns whether the writer is between copies of a multiple copy spooled file.
     @return  true if the writer is between copies, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isBetweenCopies() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return betweenCopies_;
    }

    /**
     Returns whether the writer is between spooled files.
     @return  true if the writer is between spooled files, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isBetweenFiles() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return betweenFiles_;
    }

    /**
     Returns whether the writer is held.
     @return  true if the writer is held, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isHeld() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return held_;
    }

    /**
     Returns whether the writer is on a job queue and is not currently running.
     @return  true if the writer is on a job queue, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isOnJobQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return onJobQueue_;
    }

    /**
     Returns whether the printer is published in the network directory.
     @return  true if the printer is published, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isPublishedInNetworkDirectory() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return publishedInNetworkDirectory_;
    }

    /**
     Returns whether the writer has written all the data currently in the spooled file and is waiting for more data.
     @return  true if the writer is waiting for more data when the writer is producing an open spooled file with SCHEDULE(*IMMED) specified; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isWaitingForData() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return waitingForData_;
    }

    /**
     Returns whether the writer is waiting to get the device from a job that is printing directly to the printer.
     @return  true if the writer is waiting for the device, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isWaitingForDevice() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return waitingForDevice_;
    }

    /**
     Returns whether the writer is waiting for a reply to an inquiry message.
     @return  true if the writer is waiting for a reply, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isWaitingForMessage() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return waitingForMessage_;
    }

    /**
     Returns whether the writer is started for this printer.
     @return  true if the writer is started, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isWriterStarted() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return writerStarted_;
    }

    /**
     Refreshes the information about this printer object from the system.  This method is implicitly called by the getXXX() methods on this class the first time.  Call this method explicitly to refresh the information returned by the various getXXX() methods.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void refresh() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Refreshing printer information.");
        // Retrieve server operating system VRM.
        int vrm = system_.getVRM();
        // Convert based on server job CCSID.
        CharConverter conv = new CharConverter(system_.getCcsid(), system_);

        byte[] deviceName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        conv.stringToByteArray(name_, deviceName, 0, 10);

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(402),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x01, (byte)0x92 } ),
            // Format name, input, char(8), EBCDIC 'RPTA0100'.
            new ProgramParameter(new byte[] { (byte)0xD9, (byte)0xD7, (byte)0xE3, (byte)0xC1, (byte)0xF0, (byte)0xF1, (byte)0xF0, (byte)0xF0 } ),
            // Device name, input, char(10).
            new ProgramParameter(deviceName),
            // Error code, I/0, char(*).
            new ProgramParameter(new byte[8])
        };

        // Note this is not an open list API, even though it starts with QGY.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYRPRTA.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        byte[] data = parameters[0].getOutputData();

        //int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
        //int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
        //name_ = conv.byteArrayToString(data, 8, 10).trim();
        writerStarted_ = data[18] == (byte)0xF1;  // '1' means started.
        writerStatus_ = data[19] & 0xFF;
        deviceStatus_ = BinaryConverter.byteArrayToInt(data, 20);
        overallStatus_ = BinaryConverter.byteArrayToInt(data, 24);
        description_ = conv.byteArrayToString(data, 28, 50).trim();
        starterUser_ = conv.byteArrayToString(data, 80, 10).trim();
        writingStatus_ = data[90] & 0xFF;
        waitingForMessage_ = data[91] == (byte)0xE8;  // 'Y' or 'N'
        held_ = data[92] == (byte)0xE8;  // 'Y' or 'N'
        endPendingStatus_ = data[93] & 0xFF;
        holdPendingStatus_ = data[94] & 0xFF;
        betweenFiles_ = data[95] == (byte)0xE8;  // 'Y' or 'N'
        betweenCopies_ = data[96] == (byte)0xE8;  // 'Y' or 'N'
        waitingForData_ = data[97] == (byte)0xE8;  // 'Y' or 'N'
        waitingForDevice_ = data[98] == (byte)0xE8;  // 'Y' or 'N'
        onJobQueue_ = data[99] == (byte)0xE8;  // 'Y' or 'N'
        writerJobName_ = conv.byteArrayToString(data, 104, 10).trim();
        writerJobUser_ = conv.byteArrayToString(data, 114, 10).trim();
        writerJobNumber_ = conv.byteArrayToString(data, 124, 6).trim();
        printerDeviceType_ = conv.byteArrayToString(data, 130, 10).trim();
        if (printerDeviceType_.equals(PRINTER_DEVICE_TYPE_SCS))
        {
            printerDeviceType_ = PRINTER_DEVICE_TYPE_SCS;
        }
        else if (printerDeviceType_.equals(PRINTER_DEVICE_TYPE_IPDS))
        {
            printerDeviceType_ = PRINTER_DEVICE_TYPE_IPDS;
        }
        numberOfSeparators_ = BinaryConverter.byteArrayToInt(data, 140);
        separatorDrawer_ = BinaryConverter.byteArrayToInt(data, 144);
        formAlignmentMessageTime_ = conv.byteArrayToString(data, 148, 10).trim();
        if (formAlignmentMessageTime_.equals(FORM_ALIGNMENT_WRITER))
        {
            formAlignmentMessageTime_ = FORM_ALIGNMENT_WRITER;
        }
        else if (formAlignmentMessageTime_.equals(FORM_ALIGNMENT_FILE))
        {
            formAlignmentMessageTime_ = FORM_ALIGNMENT_FILE;
        }
        outputQueueName_ = conv.byteArrayToString(data, 158, 10).trim();
        outputQueueLibraryName_ = conv.byteArrayToString(data, 168, 10).trim();
        outputQueueStatus_ = data[178] & 0xFF;
        publishedInNetworkDirectory_ = data[179] == (byte)0xF1;  // '0' or '1'
        formType_ = conv.byteArrayToString(data, 180, 10).trim();
        if (formType_.equals(FORM_TYPE_ALL))
        {
            formType_ = FORM_TYPE_ALL;
        }
        else if (formType_.equals(FORM_TYPE_FORMS))
        {
            formType_ = FORM_TYPE_FORMS;
        }
        else if (formType_.equals(FORM_TYPE_STANDARD))
        {
            formType_ = FORM_TYPE_STANDARD;
        }
        messageOption_ = conv.byteArrayToString(data, 190, 10).trim();
        if (messageOption_.equals(MESSAGE_OPTION_STANDARD))
        {
            messageOption_ = MESSAGE_OPTION_STANDARD;
        }
        else if (messageOption_.equals(MESSAGE_OPTION_NONE))
        {
            messageOption_ = MESSAGE_OPTION_NONE;
        }
        else if (messageOption_.equals(MESSAGE_OPTION_INFO))
        {
            messageOption_ = MESSAGE_OPTION_INFO;
        }
        else if (messageOption_.equals(MESSAGE_OPTION_INQUIRY))
        {
            messageOption_ = MESSAGE_OPTION_INQUIRY;
        }
        writerEndTime_ = conv.byteArrayToString(data, 200, 10).trim();
        if (writerEndTime_.equals(WRITER_TIME_ALL_DONE))
        {
            writerEndTime_ = WRITER_TIME_ALL_DONE;
        }
        else if (writerEndTime_.equals(WRITER_TIME_CURRENT_DONE))
        {
            writerEndTime_ = WRITER_TIME_CURRENT_DONE;
        }
        else if (writerEndTime_.equals(WRITER_TIME_NONE))
        {
            writerEndTime_ = WRITER_TIME_NONE;
        }
        allowsDirectPrinting_ = conv.byteArrayToString(data, 210, 10).trim().equals("*YES");  // "*YES" or "*NO"
        messageQueueName_ = conv.byteArrayToString(data, 220, 10).trim();
        messageQueueLibraryName_ = conv.byteArrayToString(data, 230, 10).trim();
        writerChangeTime_ = conv.byteArrayToString(data, 242, 10).trim();
        if (writerChangeTime_.equals(WRITER_TIME_ALL_DONE))
        {
            writerChangeTime_ = WRITER_TIME_ALL_DONE;
        }
        else if (writerChangeTime_.equals(WRITER_TIME_CURRENT_DONE))
        {
            writerChangeTime_ = WRITER_TIME_CURRENT_DONE;
        }
        else if (writerChangeTime_.equals(WRITER_TIME_NONE))
        {
            writerChangeTime_ = WRITER_TIME_NONE;
        }
        pendingOutputQueueName_ = conv.byteArrayToString(data, 252, 10).trim();
        pendingOutputQueueLibraryName_ = conv.byteArrayToString(data, 262, 10).trim();
        pendingFormType_ = conv.byteArrayToString(data, 272, 10).trim();
        if (pendingFormType_.equals(FORM_TYPE_ALL))
        {
            pendingFormType_ = FORM_TYPE_ALL;
        }
        else if (pendingFormType_.equals(FORM_TYPE_FORMS))
        {
            pendingFormType_ = FORM_TYPE_FORMS;
        }
        else if (pendingFormType_.equals(FORM_TYPE_STANDARD))
        {
            pendingFormType_ = FORM_TYPE_STANDARD;
        }
        pendingMessageOption_ = conv.byteArrayToString(data, 282, 10).trim();
        if (pendingMessageOption_.equals(MESSAGE_OPTION_STANDARD))
        {
            pendingMessageOption_ = MESSAGE_OPTION_STANDARD;
        }
        else if (pendingMessageOption_.equals(MESSAGE_OPTION_NONE))
        {
            pendingMessageOption_ = MESSAGE_OPTION_NONE;
        }
        else if (pendingMessageOption_.equals(MESSAGE_OPTION_INFO))
        {
            pendingMessageOption_ = MESSAGE_OPTION_INFO;
        }
        else if (pendingMessageOption_.equals(MESSAGE_OPTION_INQUIRY))
        {
            pendingMessageOption_ = MESSAGE_OPTION_INQUIRY;
        }
        pendingNumberOfSeparators_ = BinaryConverter.byteArrayToInt(data, 292);
        pendingSeparatorDrawer_ = BinaryConverter.byteArrayToInt(data, 296);
        spooledFileName_ = conv.byteArrayToString(data, 300, 10).trim();
        spooledFileJobName_ = conv.byteArrayToString(data, 310, 10).trim();
        spooledFileJobUser_ = conv.byteArrayToString(data, 320, 10).trim();
        spooledFileJobNumber_ = conv.byteArrayToString(data, 330, 6).trim();
        spooledFileNumber_ = BinaryConverter.byteArrayToInt(data, 336);
        pageBeingWritten_ = BinaryConverter.byteArrayToInt(data, 340);
        totalPages_ = BinaryConverter.byteArrayToInt(data, 344);
        copiesLeft_ = BinaryConverter.byteArrayToInt(data, 348);
        totalCopies_ = BinaryConverter.byteArrayToInt(data, 352);
        supportsAFP_ = conv.byteArrayToString(data, 356, 10).trim().equals("*YES");  // '*YES' or '*NO'
        writerMessageKey_ = new byte[4];
        System.arraycopy(data, 366, writerMessageKey_, 0, 4);
        if (vrm >= 0x00050200)
        {
            spooledFileJobSystem_ = conv.byteArrayToString(data, 381, 8).trim();
            spooledFileCreationDate_ = conv.byteArrayToString(data, 389, 7).trim();
            spooledFileCreationTime_ = conv.byteArrayToString(data, 396, 6).trim();
        }
        refreshed_ = true;
    }

    /**
     Returns whether the printer supports Advanced Function Printing.
     @return  true if Advanced Function Printing is supported, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean supportsAFP() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!refreshed_) refresh();
        return supportsAFP_;
    }
}
