///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SpooledFileListItem.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import java.util.Calendar;
import java.util.Date;

import com.ibm.as400.access.AS400Calendar;
import com.ibm.as400.access.QSYSObjectPathName;

/**
 Contains spooled file information that was generated by {@link com.ibm.as400.access.list.SpooledFileOpenList SpooledFileOpenList}.
 <p>Some attributes will not be available, depending on the {@link #getFormat format} that was used to generate this item.  The javadoc for each attribute getter indicates which formats will generate valid data for the given attribute.  If an attribute getter that is only valid for a format other than what was used to generate this SpooledFileListItem, the data returned by that getter is not valid.
 **/
public class SpooledFileListItem
{
    /**
     Constant indicating the spooled file is assigned to a specific printer.
     @see  #getPrinterAssignment
     **/
    public static final String ASSIGNED_SPECIFIC = "1";

    /**
     Constant indicating the spooled file is assigned to multiple printers.
     @see  #getPrinterAssignment
     **/
    public static final String ASSIGNED_MULTIPLE = "2";

    /**
     Constant indicating the spooled file is not assigned to a printer.
     @see  #getPrinterAssignment
     **/
    public static final String ASSIGNED_NONE = "3";


    /**
     Constant indicating the spooled file is intended for a diskette device.
     @see  #getDeviceType
     **/
    public static final String DEVICE_TYPE_DISKETTE = "DISKETTE";

    /**
     Constant indicating the spooled file is intended for a printer device.
     @see  #getDeviceType
     **/
    public static final String DEVICE_TYPE_PRINTER = "PRINTER";


    /**
     Constant indicating the spooled file is schedule immediate.  A spooling writer can process the spooled file immediately.
     @see  #getSchedule
     **/
    public static final String SCHEDULE_IMMEDIATE = "1";

    /**
     Constant indicating the spooled file is schedule file end.  A spooling writer cannot process the spooled file until it has been closed.
     @see  #getSchedule
     **/
    public static final String SCHEDULE_FILE_END = "2";

    /**
     Constant indicating the spooled file is schedule job end.  A spooling writer cannot process the spooled file until the job of the spooled file has ended.
     @see  #getSchedule
     **/
    public static final String SCHEDULE_JOB_END = "3";


    /**
     Constant indicating the spooled file is available to be written to an output device by a writer.
     @see  #getStatus
     **/
    public static final String STATUS_READY = "*READY";

    /**
     Constant indicating the spooled file has not been completely processed and is not ready to be selected by a writer.
     @see  #getStatus
     **/
    public static final String STATUS_OPEN = "*OPEN";

    /**
     Constant indicating the spooled file has been processed completely by a program, but SCHEDULE(*JOBEND) was specified.  The job that produced the spooled file has not finished.
     @see  #getStatus
     **/
    public static final String STATUS_CLOSED = "*CLOSED";

    /**
     Constant indicating the spooled file has been written and then saved.  This spooled file remains saved until it is released.
     @see  #getStatus
     **/
    public static final String STATUS_SAVED = "*SAVED";

    /**
     Constant indicating the spooled file currently is being produced by the writer on an output device.
     @see  #getStatus
     **/
    public static final String STATUS_WRITING = "*WRITING";

    /**
     Constant indicating the spooled file has been held.
     @see  #getStatus
     **/
    public static final String STATUS_HELD = "*HELD";

    /**
     Constant indicating the spooled file has a message that needs a reply or needs an action to be taken.
     @see  #getStatus
     **/
    public static final String STATUS_MESSAGE_WAIT = "*MESSAGE";

    /**
     Constant indicating the spooled file is pending (waiting) to be printed.
     @see  #getStatus
     **/
    public static final String STATUS_PENDING = "*PENDING";

    /**
     Constant indicating the spooled file has been completely sent to the printer, but the print complete status has not been sent back.
     @see  #getStatus
     **/
    public static final String STATUS_PRINTING = "*PRINTER";

    /**
     Constant indicating the spooled file is no longer in the system.  A spooled file with this status is only returned in the list of spooled files if the qualified job name was specified.
     @see  #getStatus
     **/
    public static final String STATUS_FINISHED = "*FINISHED";

    /**
     Constant indicating the spooled file is being sent or has been sent to a remote system.
     @see  #getStatus
     **/
    public static final String STATUS_SENDING = "*SENDING";

    /**
     Constant indicating the spooled file has been deferred from printing.
     @see  #getStatus
     **/
    public static final String STATUS_DEFERRED = "*DEFERRED";


    // Job name.
    private String jobName_;
    // User name.
    private String jobUser_;
    // Job number.
    private String jobNumber_;
    // Spooled file name.
    private String name_;
    // Spooled file number.
    private int number_;
    // File status or status or spooled file status.
    private String status_;
    // Date spooled file was opened.
    private String dateOpened_;
    // Time spooled file was opened.
    private String timeOpened_;
    // Spooled file schedule.
    private String schedule_;
    // Job system name.
    private String jobSystemName_;
    // User data.
    private String userData_;
    // Spooled file form type or form type.
    private String formType_;
    // Output queue name.
    private String outputQueueName_;
    // Output queue library name.
    private String outputQueueLibrary_;
    // Auxiliary storage pool.
    private int asp_;
    // Size of spooled file.
    private int size_;
    // Spooled file size multiplier.
    private int sizeMultiplier_;
    // Total pages.
    private int totalPages_;
    // Copies left to print.
    private int copiesLeftToPrint_;
    // Priority.
    private String priority_;
    // Internet print protocol job identifier.
    private int ippJobIdentifier_;

    // Internal job identifier.
    private byte[] internalJobIdentifier_;
    // Internal spooled file identifier.
    private byte[] internalSpooledFileIdentifier_;
    // Current page.
    private int currentPage_;
    // Device type.
    private String deviceType_;
    // Printer assigned.
    private String printerAssignment_;
    // Printer name.
    private String printerName_;

    // Format of receiver variable.
    private String format_;

    SpooledFileListItem(String name, String jobName, String jobUser, String jobNumber, int number, int totalPages, int currentPage, int copiesLeftToPrint, String outputQueueName, String outputQueueLibrary, String userData, String status, String formType, String priority, byte[] internalJobIdentifier, byte[] internalSpooledFileIdentifier, String deviceType, String jobSystemName, String dateOpened, String timeOpened)
    {
        name_ = name;
        jobName_ = jobName;
        jobUser_ = jobUser;
        jobNumber_ = jobNumber;
        number_ = number;
        totalPages_ = totalPages;
        currentPage_ = currentPage;
        copiesLeftToPrint_ = copiesLeftToPrint;
        outputQueueName_ = outputQueueName;
        outputQueueLibrary_ = outputQueueLibrary;
        userData_ = userData;
        status_ = status;
        formType_ = formType;
        priority_ = priority;
        internalJobIdentifier_ = internalJobIdentifier;
        internalSpooledFileIdentifier_ = internalSpooledFileIdentifier;
        deviceType_ = deviceType;
        jobSystemName_ = jobSystemName;
        dateOpened_ = dateOpened;
        timeOpened_ = timeOpened;
        format_ = SpooledFileOpenList.FORMAT_0100;
    }

    SpooledFileListItem(String name, String jobName, String jobUser, String jobNumber, int number, int totalPages, int currentPage, int copiesLeftToPrint, String outputQueueName, String outputQueueLibrary, String userData, String status, String formType, String priority, byte[] internalJobIdentifier, byte[] internalSpooledFileIdentifier, String deviceType, String jobSystemName, String dateOpened, String timeOpened, String printerAssignment, String printerName)
    {
        this(name, jobName, jobUser, jobNumber, number, totalPages, currentPage, copiesLeftToPrint, outputQueueName, outputQueueLibrary, userData, status, formType, priority, internalJobIdentifier, internalSpooledFileIdentifier, deviceType, jobSystemName, dateOpened, timeOpened);
        printerAssignment_ = printerAssignment;
        printerName_ = printerName;
        format_ = SpooledFileOpenList.FORMAT_0200;
    }

    SpooledFileListItem(String jobName, String jobUser, String jobNumber, String name, int number, int status, String dateOpened, String timeOpened, String schedule, String jobSystemName, String userData, String formType, String outputQueueName, String outputQueueLibrary, int asp, int size, int sizeMultiplier, int totalPages, int copiesLeftToPrint, String priority, int ippJobIdentifier, boolean isOSPL400)
    {
        jobName_ = jobName;
        jobUser_ = jobUser;
        jobNumber_ = jobNumber;
        name_ = name;
        number_ = number;
        status_ = mapStatus(status);
        dateOpened_ = dateOpened;
        timeOpened_ = timeOpened;
        schedule_ = schedule;
        jobSystemName_ = jobSystemName;
        userData_ = userData;
        formType_ = formType;
        outputQueueName_ = outputQueueName;
        outputQueueLibrary_ = outputQueueLibrary;
        asp_ = asp;
        size_ = size;
        sizeMultiplier_ = sizeMultiplier;
        totalPages_ = totalPages;
        copiesLeftToPrint_ = copiesLeftToPrint;
        priority_ = priority;
        ippJobIdentifier_ = ippJobIdentifier;
        format_ = isOSPL400 ? SpooledFileOpenList.FORMAT_0400 : SpooledFileOpenList.FORMAT_0300;
    }

    SpooledFileListItem(String jobName, String jobUser, String jobNumber, String name, int number, int status, String dateOpened, String timeOpened, String schedule, String jobSystemName, String userData, String formType, String outputQueueName, String outputQueueLibrary, int asp, int size, int sizeMultiplier, int totalPages, int copiesLeftToPrint, String priority, int ippJobIdentifier) {
        this(jobName, jobUser, jobNumber, name, number, status, dateOpened, timeOpened, schedule, jobSystemName, userData, formType, outputQueueName, outputQueueLibrary, asp, size, sizeMultiplier, totalPages, copiesLeftToPrint, priority, ippJobIdentifier, false);
    }

    /**
     Returns the auxiliary storage pool (ASP) in which the spooled file resides.  Possible values are:
     <ul>
     <li>1 - The system auxiliary storage pool (*SYSTEM).
     <li>2-255 - The number of the auxiliary storage pool.
     </ul>
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The auxiliary storage pool.
     **/
    public int getASP()
    {
        return asp_;
    }

    /**
     Returns the remaining number of copies to be printed.  This attribute applies to printer device type spooled files only.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}
     @return  The number of copies left.
     @see  #getDeviceType
     **/
    public int getCopiesLeftToPrint()
    {
        return copiesLeftToPrint_;
    }

    /**
     Returns the date and time the spooled file was created.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100} (i5/OS V5R2M0 and higher), {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @see  #getDateOpened
     @see  #getTimeOpened
     @return  The date and time formatted into a java.util.Date object.
     **/
    public Date getCreationDate()
    {
        if (dateOpened_ == null) return null;
        Calendar c = AS400Calendar.getGregorianInstance();
        c.clear();
        c.set(Integer.parseInt(dateOpened_.substring(0, 3)) + 1900, // Year.
              Integer.parseInt(dateOpened_.substring(3, 5)) - 1, // Month is zero based.
              Integer.parseInt(dateOpened_.substring(5, 7)), // Day.
              Integer.parseInt(timeOpened_.substring(0, 2)), // Hour.
              Integer.parseInt(timeOpened_.substring(2, 4)), // Minute.
              Integer.parseInt(timeOpened_.substring(4, 6))); // Second.
        return c.getTime();
    }

    /**
     Returns the page number or record number currently being written.  The page number may be lower or higher than the page number actually being printed because of buffering done by the system.  For example, if the spooled file is routed to a diskette unit or the writer is currently printing job separators or file separators for the spooled file, the page number returned may be zero.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}
     @return  The current page number.
     **/
    public int getCurrentPage()
    {
        return currentPage_;
    }

    /**
     Returns the date the spooled file was created.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100} (i5/OS V5R2M0 and higher), {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @see  #getCreationDate
     @return  The date in the format CYYMMDD.
     **/
    public String getDateOpened()
    {
        return dateOpened_;
    }

    /**
     Returns the type of device for which the spooled file is intended.  Possible values are:
     <ul>
     <li>{@link #DEVICE_TYPE_DISKETTE DEVICE_TYPE_DISKETTE}
     <li>{@link #DEVICE_TYPE_PRINTER DEVICE_TYPE_PRINTER}
     </ul>
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}
     @return  The device type.
     **/
    public String getDeviceType()
    {
        if (deviceType_ == null) return null;
        if (deviceType_.equals(DEVICE_TYPE_PRINTER)) return DEVICE_TYPE_PRINTER;
        if (deviceType_.equals(DEVICE_TYPE_DISKETTE)) return DEVICE_TYPE_DISKETTE;
        return deviceType_;
    }

    /**
     Returns the format that was used by SpooledFileOpenList to generate this item.
     @return  The format.
     @see  com.ibm.as400.access.list.SpooledFileListItem#getFormat
     **/
    public String getFormat()
    {
        return format_;
    }

    /**
     Returns the type of forms that should be loaded on the printer before this spooled file is printed.  This attribute applies to printer device type spooled files only.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The form type.
     @see  #getDeviceType
     **/
    public String getFormType()
    {
        return formType_;
    }

    /**
     Returns the internal job identifier for the job that created the spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}
     @return  The 16-byte internal job ID.
     @see  com.ibm.as400.access.Job#setInternalJobIdentifier
     **/
    public byte[] getInternalJobIdentifier()
    {
        return internalJobIdentifier_;
    }

    /**
     Returns the internal spooled file identifier for the spooled file.  This is the input value that other programs use to improve the performance of locating the spooled file on the system.  Only the spooled file APIs use this identifier, not any other interface on the system.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}
     @return  The 16-byte internal spooled file ID.
     **/
    public byte[] getInternalSpooledFileIdentifier()
    {
        return internalSpooledFileIdentifier_;
    }

    /**
     Returns the Internet Print Protocol (IPP) job identifier assigned by the system based on the output queue to which the file was added or moved.  This value ranges from 1 to 2,147,483,647 and is not guaranteed to be unique for a given output queue.  This value will be 0 when retrieved from systems running i5/OS V5R2M0 and earlier.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400},
     @return  The IPP job identifier.
     **/
    public int getIPPJobIdentifier()
    {
        return ippJobIdentifier_;
    }

    /**
     Returns the name of the job that created the spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The job name.
     @see  #getJobNumber
     @see  #getJobUser
     **/
    public String getJobName()
    {
        return jobName_;
    }

    /**
     Returns the number of the job that created the spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The job number.
     @see  #getJobName
     @see  #getJobUser
     **/
    public String getJobNumber()
    {
        return jobNumber_;
    }

    /**
     Returns the name of the system where the job that created the spooled file ran.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100} (i5/OS V5R2M0 and higher), {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200} (i5/OS V5R2M0 and higher), {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The system name.
     **/
    public String getJobSystemName()
    {
        return jobSystemName_;
    }

    /**
     Returns the user of the job that created the spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The user name.
     @see  #getJobName
     @see  #getJobNumber
     **/
    public String getJobUser()
    {
        return jobUser_;
    }

    /**
     Returns the name of the spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The spooled file name.
     **/
    public String getName()
    {
        return name_;
    }

    /**
     Returns the number of the spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The spooled file number.
     **/
    public int getNumber()
    {
        return number_;
    }

    /**
     Returns the fully-qualified integrated file system path of the output queue in which the spooled file is located.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The output queue.
     @see  #getOutputQueueName
     @see  #getOutputQueueLibrary
     **/
    public String getOutputQueue()
    {
        return QSYSObjectPathName.toPath(outputQueueLibrary_, outputQueueName_, "OUTQ");
    }

    /**
     Returns the library of the output queue in which the spooled file is located.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The library.
     @see  #getOutputQueue
     **/
    public String getOutputQueueLibrary()
    {
        return outputQueueLibrary_;
    }

    /**
     Returns the name of the output queue in which the spooled file is located.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The output queue name.
     @see  #getOutputQueue
     **/
    public String getOutputQueueName()
    {
        return outputQueueName_;
    }

    /**
     Returns how the spooled file is assigned.  This attribute applies to printer device type spooled files only.  Possible values are:
     <ul>
     <li>{@link #ASSIGNED_SPECIFIC ASSIGNED_SPECIFIC} - The printer name will be valid.
     <li>{@link #ASSIGNED_MULTIPLE ASSIGNED_MULTIPLE} - The printer name will be blank.
     <li>{@link #ASSIGNED_NONE ASSIGNED_NONE} - The printer name will be blank.
     </ul>
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}
     @return  The printer assignment.
     @see  #getPrinterName
     @see  #getDeviceType
     **/
    public String getPrinterAssignment()
    {
        if (printerAssignment_ == null) return null;
        if (printerAssignment_.equals(ASSIGNED_SPECIFIC)) return ASSIGNED_SPECIFIC;
        if (printerAssignment_.equals(ASSIGNED_MULTIPLE)) return ASSIGNED_MULTIPLE;
        if (printerAssignment_.equals(ASSIGNED_NONE)) return ASSIGNED_NONE;
        return printerAssignment_;
    }

    /**
     Returns the name of the printer the spooled file has been assigned to print on.  This attribute applies to printer device type spooled files only, and is only valid when the printer assignment is ASSIGNED_SPECIFIC.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}
     @return  The printer name.
     @see  #getPrinterAssignment
     @see  #getDeviceType
     **/
    public String getPrinterName()
    {
        return printerName_;
    }

    /**
     Returns the priority of the spooled file.  The priority ranges from 1 (highest) to 9 (lowest).
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The priority.
     **/
    public String getPriority()
    {
        return priority_;
    }

    /**
     Returns the schedule of the spooled file.  Possible values are:
     <ul>
     <li>{@link #SCHEDULE_IMMEDIATE SCHEDULE_IMMEDIATE}
     <li>{@link #SCHEDULE_FILE_END SCHEDULE_FILE_END}
     <li>{@link #SCHEDULE_JOB_END SCHEDULE_JOB_END}
     </ul>
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The schedule.
     **/
    public String getSchedule()
    {
        if (schedule_ == null) return null;
        if (schedule_.equals(SCHEDULE_IMMEDIATE)) return SCHEDULE_IMMEDIATE;
        if (schedule_.equals(SCHEDULE_FILE_END)) return SCHEDULE_FILE_END;
        if (schedule_.equals(SCHEDULE_JOB_END)) return SCHEDULE_JOB_END;
        return schedule_;
    }

    /**
     Returns the spooled file size in bytes.  The size of the spooled file is the data stream size, plus the spooled file's attributes, plus the overhead storage used to store the spooled file's data stream.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The size.
     **/
    public long getSize()
    {
        if (sizeMultiplier_ > 0) return (long)size_*(long)sizeMultiplier_;
        return size_;
    }

    /**
     Returns the status of the spooled file.  Possible values are:
     <ul>
     <li>{@link #STATUS_READY STATUS_READY}
     <li>{@link #STATUS_OPEN STATUS_OPEN}
     <li>{@link #STATUS_CLOSED STATUS_CLOSED}
     <li>{@link #STATUS_SAVED STATUS_SAVED}
     <li>{@link #STATUS_WRITING STATUS_WRITING}
     <li>{@link #STATUS_HELD STATUS_HELD}
     <li>{@link #STATUS_MESSAGE_WAIT STATUS_MESSAGE_WAIT}
     <li>{@link #STATUS_PENDING STATUS_PENDING}
     <li>{@link #STATUS_PRINTING STATUS_PRINTING}
     <li>{@link #STATUS_FINISHED STATUS_FINISHED}
     <li>{@link #STATUS_SENDING STATUS_SENDING}
     <li>{@link #STATUS_DEFERRED STATUS_DEFERRED}
     </ul>
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The status.
     **/
    public String getStatus()
    {
        if (status_ == null) return null;
        if (status_.equals(STATUS_READY)) return STATUS_READY;
        if (status_.equals(STATUS_OPEN)) return STATUS_OPEN;
        if (status_.equals(STATUS_CLOSED)) return STATUS_CLOSED;
        if (status_.equals(STATUS_SAVED)) return STATUS_SAVED;
        if (status_.equals(STATUS_WRITING)) return STATUS_WRITING;
        if (status_.equals(STATUS_HELD)) return STATUS_HELD;
        if (status_.equals(STATUS_MESSAGE_WAIT)) return STATUS_MESSAGE_WAIT;
        if (status_.equals(STATUS_PENDING)) return STATUS_PENDING;
        if (status_.equals(STATUS_PRINTING)) return STATUS_PRINTING;
        if (status_.equals(STATUS_FINISHED)) return STATUS_FINISHED;
        if (status_.equals(STATUS_SENDING)) return STATUS_SENDING;
        if (status_.equals(STATUS_DEFERRED)) return STATUS_DEFERRED;

        return status_;
    }

    /**
     Returns the time the spooled file was created.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100} (i5/OS V5R2M0 and higher), {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @see  #getCreationDate
     @return  The time in the format HHMMSS.
     **/
    public String getTimeOpened()
    {
        return timeOpened_;
    }

    /**
     Returns the total number of pages or number of records for this spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The number of pages.
     **/
    public int getTotalPages()
    {
        return totalPages_;
    }

    /**
     Returns the 10 characters of user-specified data that describe the spooled file.
     <p>Available in format:  {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0100 FORMAT_0100}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0200 FORMAT_0200}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0300 FORMAT_0300}, {@link com.ibm.as400.access.list.SpooledFileOpenList#FORMAT_0400 FORMAT_0400}
     @return  The user data.
     **/
    public String getUserData()
    {
        return userData_;
    }

    // Helper method used to convert the integer status returned by one API format to the String status returned by the other API format.
    private static String mapStatus(int status)
    {
        switch (status)
        {
            case 1:  return STATUS_READY;
            case 2:  return STATUS_OPEN;
            case 3:  return STATUS_CLOSED;
            case 4:  return STATUS_SAVED;
            case 5:  return STATUS_WRITING;
            case 6:  return STATUS_HELD;
            case 7:  return STATUS_MESSAGE_WAIT;
            case 8:  return STATUS_PENDING;
            case 9:  return STATUS_PRINTING;
            case 10:  return STATUS_FINISHED;
            case 11:  return STATUS_SENDING;
            case 12:  return STATUS_DEFERRED;
            default:  return ""; // Shouldn't happen.
        }
    }
}
