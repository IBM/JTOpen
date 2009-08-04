///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  Job.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 The Job class represents a server job.  In order to access a job, the system and either the job name, user name, and job number or internal job identifier need to be set.  A valid combination of these must be set before getting or setting any of the job's attributes.
 <p>Some of the attributes have associated get and set methods defined in this class.  These are provided for backwards compatibility with previous versions of the IBM Toolbox for Java.  The complete set of attribute values can be accessed using the public constants.
 <p>Note:  Most of the "getter" methods will either go to the system to retrieve the job attribute, or will return a cached value if the attribute was previously retrieved or previously set by {@link #setValue setValue()} or one of the other setter methods.  Use {@link #loadInformation loadInformation()} to refresh the attributes from the system.
 <br>For example:
 <pre>
 *  Job job = new Job(system, jobName, userName, jobNumber);
 *  while (job.getStatus().equals(Job.JOB_STATUS_ACTIVE))
 *  {
 *      // Wait a while.
 *      Thread.sleep(1000);
 *      // Refresh the attribute values.
 *      job.loadInformation();
 *  }
 *  System.out.println("Job status is: " + job.getStatus());
 </pre>
 <p>Note: To obtain information about the job in which a program or command runs, do something like the following:
 <pre>
 *  AS400 system = new AS400();
 *  ProgramCall pgm = new ProgramCall(system);
 *  pgm.setThreadSafe(true);  // Indicates the program is to be run on-thread.
 *  String jobNumber = pgm.getServerJob().getNumber();
 </pre>
 @see  com.ibm.as400.access.JobList
 @see  com.ibm.as400.access.CommandCall#getServerJob
 @see  com.ibm.as400.access.ProgramCall#getServerJob
 **/
public class Job implements Serializable
{
    static final long serialVersionUID = 6L;

    private static final byte[] BLANKS16_ = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };

    private static final Boolean ON_THREAD = ProgramCall.THREADSAFE_TRUE;
    private static final Boolean OFF_THREAD = ProgramCall.THREADSAFE_FALSE;

    private boolean cacheChanges_ = true;

    private boolean isConnected_ = false;

    private final JobHashtable values_ = new JobHashtable();
    private JobHashtable cachedChanges_ = null;

    private String name_, user_, number_, status_, type_, subtype_;
    private AS400 system_;
    private String internalJobID_;
    private byte[] realInternalJobID_;

    private transient PropertyChangeSupport propertyChangeListeners_;
    private transient VetoableChangeSupport vetoableChangeListeners_;

    private transient CommandCall cmdCall_;
    private transient ProgramCall pgmCall_;
    private transient ProgramCall pgmCall_onThread_;
    private transient ProgramCall pgmCall_offThread_;
    private transient Object remoteCommandLock_ = new Object();

    /**
     Job attribute representing an identifier assigned to the job by the system to collect resource use information for the job when job accounting is active.  The user who is changing this field must have authority to the CHGACGCDE CL command.  If the user does not have the proper authority, this field is ignored and processing continues.  Possible values are:
     <ul>
     <li>{@link #ACCOUNTING_CODE_BLANK ACCOUNTING_CODE_BLANK}
     <li>An accounting code - The 15 character accounting code used for the next accounting segment.  The accounting code may contain alphabetic or numeric characters.
     </ul>
     <p>Type: String
     @see  #getJobAccountingCode
     @see  #setJobAccountingCode
     **/
    public static final int ACCOUNTING_CODE = 1001;

    /**
     Constant indicating the accounting code is changed to all blanks.
     @see  #ACCOUNTING_CODE
     **/
    public static final String ACCOUNTING_CODE_BLANK = "*BLANK";

    /**
     Job attribute representing the active status of the initial thread of a job.  Only one status is returned.  Possible values are:
     <ul>
     <li>{@link #ACTIVE_JOB_STATUS_NONE ACTIVE_JOB_STATUS_NONE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_CHECKPOINT ACTIVE_JOB_STATUS_WAIT_CHECKPOINT}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_CONDITION ACTIVE_JOB_STATUS_WAIT_CONDITION}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_CPI_COMM ACTIVE_JOB_STATUS_WAIT_CPI_COMM}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DEQUEUE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DEQUEUE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DEQUEUE ACTIVE_JOB_STATUS_WAIT_DEQUEUE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DISKETTE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DISKETTE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DISKETTE ACTIVE_JOB_STATUS_WAIT_DISKETTE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DELAYED ACTIVE_JOB_STATUS_WAIT_DELAYED}
     <li>{@link #ACTIVE_JOB_STATUS_DISCONNECTED ACTIVE_JOB_STATUS_DISCONNECTED}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DISPLAY_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DISPLAY_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DISPLAY ACTIVE_JOB_STATUS_WAIT_DISPLAY}
     <li>{@link #ACTIVE_JOB_STATUS_ENDED ACTIVE_JOB_STATUS_ENDED}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF}
     <li>{@link #ACTIVE_JOB_STATUS_ENDING ACTIVE_JOB_STATUS_ENDING}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_EVENT ACTIVE_JOB_STATUS_WAIT_EVENT}
     <li>{@link #ACTIVE_JOB_STATUS_SUSPENDED ACTIVE_JOB_STATUS_SUSPENDED}
     <li>{@link #ACTIVE_JOB_STATUS_HELD ACTIVE_JOB_STATUS_HELD}
     <li>{@link #ACTIVE_JOB_STATUS_HELD_THREAD ACTIVE_JOB_STATUS_HELD_THREAD}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_ICF_FILE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_ICF_FILE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_ICF_FILE ACTIVE_JOB_STATUS_WAIT_ICF_FILE}
     <li>{@link #ACTIVE_JOB_STATUS_INELIGIBLE ACTIVE_JOB_STATUS_INELIGIBLE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_JAVA_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_JAVA_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_JAVA ACTIVE_JOB_STATUS_WAIT_JAVA}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_LOCK ACTIVE_JOB_STATUS_WAIT_LOCK}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_LOCK_SPACE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_LOCK_SPACE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_LOCK_SPACE ACTIVE_JOB_STATUS_WAIT_LOCK_SPACE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_MESSAGE ACTIVE_JOB_STATUS_WAIT_MESSAGE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_MUTEX ACTIVE_JOB_STATUS_WAIT_MUTEX}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_MIXED_DEVICE_FILE ACTIVE_JOB_STATUS_WAIT_MIXED_DEVICE_FILE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_OSI ACTIVE_JOB_STATUS_WAIT_OSI}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_PRINT_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_PRINT_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_PRINT ACTIVE_JOB_STATUS_WAIT_PRINT}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_PRESTART ACTIVE_JOB_STATUS_WAIT_PRESTART}
     <li>{@link #ACTIVE_JOB_STATUS_RUNNING ACTIVE_JOB_STATUS_RUNNING}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_SELECTION ACTIVE_JOB_STATUS_WAIT_SELECTION}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_SEMAPHORE ACTIVE_JOB_STATUS_WAIT_SEMAPHORE}
     <li>{@link #ACTIVE_JOB_STATUS_STOPPED ACTIVE_JOB_STATUS_STOPPED}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_SIGNAL ACTIVE_JOB_STATUS_WAIT_SIGNAL}
     <li>{@link #ACTIVE_JOB_STATUS_SUSPENDED_SYSTEM_REQUEST ACTIVE_JOB_STATUS_SUSPENDED_SYSTEM_REQUEST}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_SAVE_FILE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_SAVE_FILE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_SAVE_FILE ACTIVE_JOB_STATUS_WAIT_SAVE_FILE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_THREAD ACTIVE_JOB_STATUS_WAIT_THREAD}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL_AND_ACTIVE}
     <li>{@link #ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL}
     </ul>
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int ACTIVE_JOB_STATUS = 101;

    /**
     Constant indicating that a job is in transition or is not active.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_NONE = "    ";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of an I/O operation to a binary synchronous device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE_AND_ACTIVE = "BSCA";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to a binary synchronous device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE = "BSCW";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of an I/O operation to a communications device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE_AND_ACTIVE = "CMNA";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to a communications device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE = "CMNW";

    /**
     Constant indicating that a job is waiting for the completion of save-while-active checkpoint processing in another job.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_CHECKPOINT = "CMTW";

    /**
     Constant indicating that a job is waiting on a handle-based condition.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_CONDITION = "CNDW";

    /**
     Constant indicating that a job is waiting for the completion of a CPI communications call.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_CPI_COMM = "CPCW";

    /**
     Constant indicating that a job is waiting in the pool activity level for completion of a dequeue operation.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DEQUEUE_AND_ACTIVE = "DEQA";

    /**
     Constant indicating that a job is waiting for completion of a dequeue operation.  For example, QSYSARB and subsystem monitors generally wait for work by waiting for a dequeue operation.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DEQUEUE = "DEQW";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of an I/O operation to a diskette unit.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DISKETTE_AND_ACTIVE = "DKTA";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to a diskette unit.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DISKETTE = "DKTW";

    /**
     Constant indicating that a job is waiting for a specified time interval to end, or for a specific delay end time, as specified on the Delay Job (DLYJOB) command.  The FUNCTION_NAME attribute shows either the number of seconds the job is to delay (999999), or the specific time when the job is to resume running.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DELAYED = "DLYW";

    /**
     Constant indicating that a job is waiting for a specified time interval to end, or for a specific delay end time, as specified on the Delay Job (DLYJOB) command.  The FUNCTION_NAME attribute shows either the number of seconds the job is to delay (999999), or the specific time when the job is to resume running.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DELAY = "DLYW";

    /**
     Constant indicating that a job was disconnected from a work station display.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_DISCONNECTED = "DSC ";

    /**
     Constant indicating that a job is waiting in a pool activity level for input from a work station display.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DISPLAY_AND_ACTIVE = "DSPA";

    /**
     Constant indicating that a job is waiting for input from a work station display.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DISPLAY = "DSPW";

    /**
     Constant indicating that a job has been ended with the *IMMED option, or its delay time has ended with the *CNTRLD option.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_ENDED = "END ";

    /**
     Constant indicating that a job is waiting in a pool activity level to try a read operation again on a database file after the end-of-file (EOF) has been reached.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF_AND_ACTIVE = "EOFA";

    /**
     Constant indicating that a job is waiting to try a read operation again on a database file after the end-of-file (EOF) has been reached.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF = "EOFW";

    /**
     Constant indicating that a job is ending for a reason other than running the End Job (ENDJOB) or End Subsystem (ENDSBS) commands, such as a SIGNOFF command, an End Group Job (ENDGRPJOB) command, or an exception that is not handled.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_ENDING = "EOJ ";

    /**
     Constant indicating that a job is waiting for an event.  For example, QLUS and SCPF generally wait for work by waiting for an event.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_EVENT = "EVTW";

    /**
     Constant indicating that a job is suspended by a Transfer Group Job (TFRGRPJOB) command.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_SUSPENDED = "GRP ";

    /**
     Constant indicating that a job is held.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_HELD = "HLD ";

    /**
     Constant indicating that a job is held due to a suspended thread.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_HELD_THREAD = "HLDT";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of an I/O operation to an intersystem communications function (ICF) file.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_ICF_FILE_AND_ACTIVE = "ICFA";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to an intersystem communications function (ICF) file.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_ICF_FILE = "ICFW";

    /**
     Constant indicating that a job is ineligible and not currently in a pool activity level.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_INELIGIBLE = "INEL";

    /**
     Constant indicating that a job is waiting in a pool activity level for a Java program operation to complete.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_JAVA_AND_ACTIVE = "JVAA";

    /**
     Constant indicating that a job is waiting for a Java program operation to complete.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_JAVA = "JVAW";

    /**
     Constant indicating that a job is waiting for a lock.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_LOCK = "LCKW";

    /**
     Constant indicating that a job is waiting in a pool activity level for for a lock space to be attached.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_LOCK_SPACE_AND_ACTIVE = "LSPA";

    /**
     Constant indicating that a job is waiting for a lock space to be attached.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_LOCK_SPACE = "LSPW";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of an I/O operation to multiple files.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES_AND_ACTIVE = "MLTA";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to multiple files.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES = "MLTW";

    /**
     Constant indicating that a job is waiting for a message from a message queue.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_MESSAGE = "MSGW";

    /**
     Constant indicating that a job is waiting for a mutex.  A mutex is a synchronization function that is used to allow multiple jobs or threads to serialize their access to shared data.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_MUTEX = "MTXW";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to a mixed device file.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_MIXED_DEVICE_FILE = "MXDW";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of an I/O operation to an optical device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE_AND_ACTIVE = "OPTA";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to an optical device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE = "OPTW";

    /**
     Constant indicating that a job is waiting for the completion of an OSI Communications Subsystem operation.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_OSI = "OSIW";

    /**
     Constant indicating that a job is waiting in a pool activity level for output to a printer to complete.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_PRINT_AND_ACTIVE = "PRTA";

    /**
     Constant indicating that a job is waiting for output to a printer to complete.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_PRINT = "PRTW";

    /**
     Constant indicating that a prestart job is waiting for a program start request.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_PRESTART = "PSRW";

    /**
     Constant indicating that a job is currently running in a pool activity level.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_RUNNING = "RUN ";

    /**
     Constant indicating that a job is waiting for a selection to complete.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_SELECTION = "SELW";

    /**
     Constant indicating that a job is waiting for a semaphore.  A semaphore is a synchronization function that is used to allow multiple jobs or threads to serialize their access to shared data.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_SEMAPHORE = "SEMW";

    /**
     Constant indicating that a job has stopped as the result of a signal.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_STOPPED = "SIGS";

    /**
     Constant indicating that a job is waiting for a signal.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_SIGNAL = "SIGW";

    /**
     Constant indicating that a job is the suspended half of a system request job pair.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_SUSPENDED_SYSTEM_REQUEST = "SRQ ";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of a save file operation.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_SAVE_FILE_AND_ACTIVE = "SVFA";

    /**
     Constant indicating that a job is waiting for the completion of a save file operation.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_SAVE_FILE = "SVFW";

    /**
     Constant indicating that a job is waiting in a pool activity level for the completion of an I/O operation to a tape device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE_AND_ACTIVE = "TAPA";

    /**
     Constant indicating that a job is waiting for the completion of an I/O operation to a tape device.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE = "TAPW";

    /**
     Constant indicating that a job is waiting for another thread to complete an operation.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_THREAD = "THDW";

    /**
     Constant indicating that a job is waiting in a pool activity level for a time interval to end.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL_AND_ACTIVE = "TIMA";

    /**
     Constant indicating that a job is waiting for a time interval to end.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL = "TIMW";

    /**
     Job attribute representing the status of what the initial thread of a job is currently doing, when the active job status is ACTIVE_JOB_STATUS_ENDED or ACTIVE_JOB_STATUS_ENDING.  This field is blank if the job is not ending currently.  See {@link #ACTIVE_JOB_STATUS ACTIVE_JOB_STATUS} for a list of the possible values.  For example, the active job status would be ACTIVE_JOB_STATUS_ENDING, but the job could be waiting on a lock that could keep the job from ending.  This field would then be ACTIVE_JOB_STATUS_WAIT_LOCK.
     <p>Read-only: true
     <p>Type: String
     <p>Only valid on V5R1 systems and higher.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final int ACTIVE_JOB_STATUS_FOR_JOBS_ENDING = 103;

    /**
     Job attribute representing whether a job allows multiple user threads.  This attribute
     does not prevent the operating system from creating system threads in the job.  Possible values are:
     <ul>
     <li>{@link #ALLOW_MULTIPLE_THREADS_NO ALLOW_MULTIPLE_THREADS_NO}
     <li>{@link #ALLOW_MULTIPLE_THREADS_YES ALLOW_MULTIPLE_THREADS_YES}
     </ul>
     <p>Read-only: true
     <p>Type: String
     <p>Only valid on V5R1 systems and higher.
     **/
    public static final int ALLOW_MULTIPLE_THREADS = 102;

    /**
     Constant indicating that a job does not allow multiple user threads.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ALLOW_MULTIPLE_THREADS_NO = "0";

    /**
     Constant indicating that a job allows multiple user threads.
     @see  #ACTIVE_JOB_STATUS
     **/
    public static final String ALLOW_MULTIPLE_THREADS_YES = "1";

    /**
     Job attribute representing the number of auxiliary I/O requests performed by the job across all routing steps.  This includes both database and nondatabase paging.  If the number of auxiliary I/O requests is greater than or equal to 2,147,483,647, a value of -1 is returned.  Use the AUXILIARY_IO_REQUESTS_LARGE attribute to retrieve values that are greater than or equal to 2,147,483,647.
     <p>Read-only: true
     <p>Type: Integer
     @see  #getAuxiliaryIORequests
     **/
    public static final int AUXILIARY_IO_REQUESTS  = 1401;

    /**
     Job attribute representing the number of auxiliary I/O requests performed by the job across all routing steps.  This includes both database and nondatabase paging.
     <p>Read-only: true
     <p>Type: Long
     <p>Only valid on V5R1 systems and higher.
     @see  #getAuxiliaryIORequests
     **/
    public static final int AUXILIARY_IO_REQUESTS_LARGE = 1406;

    /**
     Job attribute representing how a job handles break messages.  Possible values are:
     <ul>
     <li>{@link #BREAK_MESSAGE_HANDLING_NORMAL BREAK_MESSAGE_HANDLING_NORMAL}
     <li>{@link #BREAK_MESSAGE_HANDLING_HOLD BREAK_MESSAGE_HANDLING_HOLD}
     <li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY BREAK_MESSAGE_HANDLING_NOTIFY}
     </ul>
     <p>Type: String
     @see  #getBreakMessageHandling
     @see  #setBreakMessageHandling
     **/
    public static final int BREAK_MESSAGE_HANDLING = 201;

    /**
     Constant indicating that the message queue status determines break message handling.
     @see  #BREAK_MESSAGE_HANDLING
     **/
    public static final String BREAK_MESSAGE_HANDLING_NORMAL = "*NORMAL";

    /**
     Constant indicating that the message queue holds break messages until a user or program requests them.  The work station user uses the Display Message (DPSMSG) command to display the messages; a program must issue a Receive Message (RCVMSG) command to receive a message and handle it.
     @see  #BREAK_MESSAGE_HANDLING
     **/
    public static final String BREAK_MESSAGE_HANDLING_HOLD = "*HOLD";

    /**
     Constant indicating that the system notifies the job's message queue when a message arrives.  For interactive jobs, the audible alarm sounds if there is one, and the message-waiting light comes on.
     @see  #BREAK_MESSAGE_HANDLING
     **/
    public static final String BREAK_MESSAGE_HANDLING_NOTIFY = "*NOTIFY";

    /**
     Job attribute representing the coded character set identifier used for this job.  Possible values are:
     <ul>
     <li>{@link #CCSID_SYSTEM_VALUE CCSID_SYSTEM_VALUE}
     <li>{@link #CCSID_INITIAL_USER CCSID_INITIAL_USER}
     <li>A coded character set identifier.
     </ul>
     <p>Type: Integer
     @see  #getCodedCharacterSetID
     @see  #setCodedCharacterSetID
     **/
    public static final int CCSID = 302;

    /**
     Constant indicating that the CCSID specified in the system value QCCSID is used.
     @see  #CCSID
     **/
    public static final int CCSID_SYSTEM_VALUE = -1;

    /**
     Constant indicating that the CCSID specified in the user profile under which this thread was initially running is used.
     @see  #CCSID
     **/
    public static final int CCSID_INITIAL_USER = -2;

    /**
     Job attribute representing the character identifier control for a job.  This attribute controls the type of CCSID conversion that occurs for display files, printer files, and panel groups.  The *CHRIDCTL special value must be specified on the CHRID command parameter on the create, change, or override command for display files, printer files, and panel groups before this attribute will be used.  Possible values are:
     <ul>
     <li>{@link #CHARACTER_ID_CONTROL_DEVICE CHARACTER_ID_CONTROL_DEVICE}
     <li>{@link #CHARACTER_ID_CONTROL_JOB CHARACTER_ID_CONTROL_JOB}
     <li>{@link #CHARACTER_ID_CONTROL_SYSTEM_VALUE CHARACTER_ID_CONTROL_SYSTEM_VALUE}
     <li>{@link #CHARACTER_ID_CONTROL_INITIAL_USER CHARACTER_ID_CONTROL_INITIAL_USER}
     </ul>
     <p>Type: String
     **/
    public static final int CHARACTER_ID_CONTROL = 311;

    /**
     Constant indicating to perform the same function for *DEVD as on the CHRID command parameter for display files, printer files, and panel groups.
     @see  #CHARACTER_ID_CONTROL
     **/
    public static final String CHARACTER_ID_CONTROL_DEVICE = "*DEVD";

    /**
     Constant indicating to perform the same function for *JOBCCSID as on the CHRID command parameter for display files, printer files, and panel groups.
     @see  #CHARACTER_ID_CONTROL
     **/
    public static final String CHARACTER_ID_CONTROL_JOB = "*JOBCCSID";

    /**
     Constant indicating the value in the QCHRIDCTL system value will be used.
     @see  #CHARACTER_ID_CONTROL
     **/
    public static final String CHARACTER_ID_CONTROL_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating the CHRIDCTL specified in the user profile under which this thread was initially running will be used.
     @see  #CHARACTER_ID_CONTROL
     **/
    public static final String CHARACTER_ID_CONTROL_INITIAL_USER = "*USRPRF";

    /**
     Job attribute representing the IPv4 address of the client for which this system is doing work.  An address is expressed in standard IPv4 dotted-decimal form www.xxx.yyy.zzz (for example, 130.99.128.1).  This field is not guaranteed to be an IP address.  This field will be blank if the address is not explicitly set to a value by the Change Job (QWTCHGJB) API.
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int CLIENT_IP_ADDRESS = 318;

    /**
     Job attribute representing the completion status for a job.  Possible values are:
     <ul>
     <li>{@link #COMPLETION_STATUS_NOT_COMPLETED COMPLETION_STATUS_NOT_COMPLETED}
     <li>{@link #COMPLETION_STATUS_COMPLETED_NORMALLY COMPLETION_STATUS_COMPLETED_NORMALLY}
     <li>{@link #COMPLETION_STATUS_COMPLETED_ABNORMALLY COMPLETION_STATUS_COMPLETED_ABNORMALLY}
     </ul>
     <p>Read-only: true
     <p>Type: String
     @see  #getCompletionStatus
     **/
    public static final int COMPLETION_STATUS = 306;

    /**
     Constant indicating that the job has not completed.
     @see  #COMPLETION_STATUS
     **/
    public static final String COMPLETION_STATUS_NOT_COMPLETED = " ";

    /**
     Constant indicating that the job completed normally.
     @see  #COMPLETION_STATUS
     **/
    public static final String COMPLETION_STATUS_COMPLETED_NORMALLY = "0";

    /**
     Constant indicating that the job completed abnormally.
     @see  #COMPLETION_STATUS
     **/
    public static final String COMPLETION_STATUS_COMPLETED_ABNORMALLY = "1";

    /**
     Job attribute representing whether or not the system issued a controlled cancellation.  Possible values are:
     <ul>
     <li>{@link #END_STATUS_CANCELLED END_STATUS_CANCELLED}
     <li>{@link #END_STATUS_NOT_CANCELLED END_STATUS_NOT_CANCELLED}
     <li>{@link #END_STATUS_JOB_NOT_RUNNING END_STATUS_JOB_NOT_RUNNING}
     </ul>
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int CONTROLLED_END_REQUESTED = 502;  // End status.

    /**
     Constant indicating that the system, the subsystem in which a job is running, or the job itself is cancelled.
     @see  #CONTROLLED_END_REQUESTED
     **/
    public static final String END_STATUS_CANCELLED = "1";

    /**
     Constant indicating that the system, the subsystem in which a job is running, or the job itself is not cancelled.
     @see  #CONTROLLED_END_REQUESTED
     **/
    public static final String END_STATUS_NOT_CANCELLED = "0";

    /**
     Constant indicating that the job is not running.
     @see  #CONTROLLED_END_REQUESTED
     **/
    public static final String END_STATUS_JOB_NOT_RUNNING = " ";

    // Job attribute representing the total number of disk I/O operations performed by the job across all routing steps.  This is the sum of the asynchronous and synchronous disk I/O.
    // <p>Note:  This field is only valid for Job objects created from a JobList.
    // <p>Read-only:  true
    // <p>Type:  Long
    // public static final int DISK_IO = 415;
    // This attribute should be enabled when JobList is updated to handle the OLJB0300 format for the QGYOLJOB API.

    /**
     Job attribute representing the percentage of processing time used during the elapsed time.  For multiple-processor systems, this is the average across processors.
     <p>Read-only: true
     <p>Type: Integer
     @see  #resetStatistics
     **/
    public static final int ELAPSED_CPU_PERCENT_USED = 314;

    /**
     Job attribute representing the percentage of processing unit used for database processing during the elapsed time.  For multiple-processor systems, this is the average across processors.
     <p>Read-only: true
     <p>Type: Integer
     @see  #resetStatistics
     **/
    public static final int ELAPSED_CPU_PERCENT_USED_FOR_DATABASE = 316;

    /**
     Job attribute representing the amount of processing unit time (in milliseconds) used during the elapsed time.
     <p>Read-only: true
     <p>Type: Long
     @see  #resetStatistics
     **/
    public static final int ELAPSED_CPU_TIME_USED = 315;

    /**
     Job attribute representing the amount of processing unit time (in milliseconds) used for database processing during the elapsed time.
     <p>Read-only: true
     <p>Type: Long
     @see  #resetStatistics
     **/
    public static final int ELAPSED_CPU_TIME_USED_FOR_DATABASE = 317;

    /**
     Job attribute representing the number of disk I/O operations performed by the job during the elapsed time.  This is the sum of the {@link #ELAPSED_DISK_IO_ASYNCH asynchronous} and {@link #ELAPSED_DISK_IO_SYNCH synchronous} disk I/O.
     <p>Read-only: true
     <p>Type: Long
     @see  #resetStatistics
     **/
    public static final int ELAPSED_DISK_IO = 414;

    /**
     Job attribute representing the number of asynchronous (physical) disk I/O operations performed by the job during the elapsed time.  This is the sum of the asynchronous database and nondatabase reads and writes.
     <p>Read-only: true
     <p>Type: Long
     @see  #resetStatistics
     **/
    public static final int ELAPSED_DISK_IO_ASYNCH = 416;

    /**
     Job attribute representing the number of synchronous (physical) disk I/O operations performed by the job during the elapsed time.  This is the sum of the synchronous database and nondatabase reads and writes.
     <p>Read-only: true
     <p>Type: Long
     @see  #resetStatistics
     **/
    public static final int ELAPSED_DISK_IO_SYNCH = 417;

    /**
     Job attribute representing the total interactive response time for the initial thread (in hundredths of seconds) for the job during the elapsed time.  This does not include the time used by the machine, by the attached input/output (I/O) hardware, and by the transmission lines for sending and receiving data.  This field is 0 for noninteractive jobs.
     <p>Read-only: true
     <p>Type: Integer
     @see  #resetStatistics
     **/
    public static final int ELAPSED_INTERACTIVE_RESPONSE_TIME = 904;

    /**
     Job attribute representing the number of user interactions, such as pressing the Enter key or a function key, for the job during the elapsed time for the initial thread.  This field is 0 for noninteractive jobs.
     <p>Read-only: true
     <p>Type: Integer
     @see  #resetStatistics
     **/
    public static final int ELAPSED_INTERACTIVE_TRANSACTIONS = 905;

    /**
     Job attribute representing the amount of time (in milliseconds) that the initial thread has to wait to obtain database, nondatabase, and internal machine locks during the elapsed time.
     <p>Read-only: true
     <p>Type: Long
     <p>Can be loaded by JobList: false
     @see  #resetStatistics
     **/
    public static final int ELAPSED_LOCK_WAIT_TIME = 10008;  // Cannot pre-load.

    /**
     Job attribute representing the number of times an active program referenced an address that is not in main storage for the current routing step during the elapsed time.
     <p>Read-only: true
     <p>Type: Long
     @see  #resetStatistics
     **/
    public static final int ELAPSED_PAGE_FAULTS = 1609;

    /**
     Job attribute representing the time (in milliseconds) that has elapsed between the measurement start time and the current system time.  The measurement start time is reset when the {@link #resetStatistics resetStatistics()} method is called.
     <p>Read-only: true
     <p>Type: Long
     <p>Can be loaded by JobList: false
     @see  #resetStatistics
     **/
    public static final int ELAPSED_TIME = 10007;  // Cannot pre-load as a key.

    /**
     Job attribute representing the country or region identifier associated with this job.  Possible values are:
     <ul>
     <li>{@link #COUNTRY_ID_SYSTEM_VALUE COUNTRY_ID_SYSTEM_VALUE}
     <li>{@link #COUNTRY_ID_INITIAL_USER COUNTRY_ID_INITIAL_USER}
     <li>A country or region identifier.
     </ul>
     <p>Type: String
     @see  #getCountryID
     @see  #setCountryID
     **/
    public static final int COUNTRY_ID = 303;

    /**
     Constant indicating the system value QCNTRYID is used.
     @see  #COUNTRY_ID
     **/
    public static final String COUNTRY_ID_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating the country or region ID specified in the user profile under which this thread was initially running is used.
     @see  #COUNTRY_ID
     **/
    public static final String COUNTRY_ID_INITIAL_USER = "*USRPRF";

    /**
     Job attribute representing the amount of processing unit time (in milliseconds) that the job used.  If the processing unit time used is greater than or equal to 2,147,483,647 milliseconds, a value of -1 is returned.  Use the CPU_TIME_USED_LARGE attribute to retrieve values that are greater than or equal to 2,147,483,647.
     <p>Read-only: true
     <p>Type: Integer
     @see  #getCPUUsed
     **/
    public static final int CPU_TIME_USED = 304;

    /**
     Job attribute representing the amount of processing unit time (in milliseconds) that the job used across all routing steps.
     <p>Read-only: true
     <p>Type: Long
     @see  #getCPUUsed
     **/
    public static final int CPU_TIME_USED_LARGE = 312;

    /**
     Job attribute representing the amount of processing unit time (in milliseconds) that the job used for processing data base requests across all routing steps.
     <p>Read-only: true
     <p>Type: Long
     <p>Only valid on V5R1 systems and higher.
     **/
    public static final int CPU_TIME_USED_FOR_DATABASE = 313;

    /**
     Job attribute representing the name of the current library for the initial thread of the job.  If no current library exists, the CURRENT_LIBRARY_EXISTENCE attribute returns 0 and this attribute returns an empty string ("").
     <p>Read-only: true
     <p>Type: String
     <p>Can be loaded by JobList: false
     @see  #getCurrentLibrary
     **/
    public static final int CURRENT_LIBRARY = 10000;  // Cannot preload.

    /**
     Job attribute representing whether or not a current library exists for the job.  Returns 0 if no current library exists; 1 if a current library exists.
     <p>Read-only: true
     <p>Type: Integer
     <p>Can be loaded by JobList: false
     @see  #getCurrentLibraryExistence
     **/
    public static final int CURRENT_LIBRARY_EXISTENCE = 10001;  // Cannot preload.

    /**
     Job attribute representing the identifier of the system-related pool from which main storage is currently being allocated for the job's initial thread.  These identifiers are not the same as those specified in the subsystem description, but are the same as the system pool identifiers shown on the system status display.  If a thread reaches its time-slice end, the pool the thread is running in can be swiched based on the job's time-slice end pool value.  The current system pool identifier returned  will be the actual pool in which the initial thread of the job is running.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int CURRENT_SYSTEM_POOL_ID = 307;

    /**
     Job attribute representing the user profile that the initial thread of the job for which information is being retrieved is currently running under.  This name may differ from the user portion of the job name.
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int CURRENT_USER = 305;

    /**
     Job attribute representing the date and time when the job completed running on the system.
     <p>Read-only: true
     <p>Type: String in the format CYYMMDDHHMMSS
     **/
    public static final int DATE_ENDED = 418;

    /**
     Job attribute representing the date and time when the job was placed on the system.
     <p>Read-only: true
     <p>Type: String in the format CYYMMDDHHMMSS
     @see  #getDate
     @see  #getJobEnterSystemDate
     **/
    public static final int DATE_ENTERED_SYSTEM = 402;

    /**
     Job attribute representing the format in which dates are presented.  Possible values are:
     <ul>
     <li>{@link #DATE_FORMAT_SYSTEM_VALUE DATE_FORMAT_SYSTEM_VALUE}
     <li>{@link #DATE_FORMAT_YMD DATE_FORMAT_YMD}
     <li>{@link #DATE_FORMAT_MDY DATE_FORMAT_MDY}
     <li>{@link #DATE_FORMAT_DMY DATE_FORMAT_DMY}
     <li>{@link #DATE_FORMAT_JULIAN DATE_FORMAT_JULIAN}
     </ul>
     <p>Type: String
     @see  #getDateFormat
     @see  #setDateFormat
     **/
    public static final int DATE_FORMAT = 405;

    /**
     Constant indicating the system value QDATFMT is used.
     @see  #DATE_FORMAT
     **/
    public static final String DATE_FORMAT_SYSTEM_VALUE = "*SYS";

    /**
     Constant indicating a date format of year, month, and day.
     @see  #DATE_FORMAT
     **/
    public static final String DATE_FORMAT_YMD = "*YMD";

    /**
     Constant indicating a date format of month, day, and year.
     @see  #DATE_FORMAT
     **/
    public static final String DATE_FORMAT_MDY = "*MDY";

    /**
     Constant indicating a date format of day, month, and year.
     @see  #DATE_FORMAT
     **/
    public static final String DATE_FORMAT_DMY = "*DMY";

    /**
     Constant indicating a Julian date format (year and day).
     @see  #DATE_FORMAT
     **/
    public static final String DATE_FORMAT_JULIAN = "*JUL";

    /**
     Job attribute representing the value used to separate days, months, and years when presenting a date.  Possible values are:
     <ul>
     <li>{@link #DATE_SEPARATOR_SYSTEM_VALUE DATE_SEPARATOR_SYSTEM_VALUE}
     <li>{@link #DATE_SEPARATOR_SLASH DATE_SEPARATOR_SLASH}
     <li>{@link #DATE_SEPARATOR_DASH DATE_SEPARATOR_DASH}
     <li>{@link #DATE_SEPARATOR_PERIOD DATE_SEPARATOR_PERIOD}
     <li>{@link #DATE_SEPARATOR_BLANK DATE_SEPARATOR_BLANK}
     <li>{@link #DATE_SEPARATOR_COMMA DATE_SEPARATOR_COMMA}
     </ul>
     <p>Type: String
     @see  #getDateSeparator
     @see  #setDateSeparator
     **/
    public static final int DATE_SEPARATOR = 406;

    /**
     Constant indicating the system value QDATSEP is used for the date separator.
     @see  #DATE_SEPARATOR
     **/
    public static final String DATE_SEPARATOR_SYSTEM_VALUE = "S";

    /**
     Constant indicating a slash (/) is used for the date separator.
     @see  #DATE_SEPARATOR
     **/
    public static final String DATE_SEPARATOR_SLASH = "/";

    /**
     Constant indicating a dash (-) is used for the date separator.
     @see  #DATE_SEPARATOR
     **/
    public static final String DATE_SEPARATOR_DASH = "-";

    /**
     Constant indicating a period (.) is used for the date separator.
     @see  #DATE_SEPARATOR
     **/
    public static final String DATE_SEPARATOR_PERIOD = ".";

    /**
     Constant indicating a blank is used for the date separator.
     @see  #DATE_SEPARATOR
     **/
    public static final String DATE_SEPARATOR_BLANK = " ";

    /**
     Constant indicating a comma (,) is used for the date separator.
     @see  #DATE_SEPARATOR
     **/
    public static final String DATE_SEPARATOR_COMMA = ",";

    /**
     Job attribute representing the date and time when the job began to run on the system.  This is blank if the job did not become active.
     <p>Read-only: true
     <p>Type: String in the format CYYMMDDHHMMSS
     @see  #getJobActiveDate
     **/
    public static final int DATE_STARTED = 401;

    /**
     Job attribute representing whether the job is DBCS-capable or not.  Possible values are:
     <ul>
     <li>{@link #DBCS_CAPABLE_NO DBCS_CAPABLE_NO}
     <li>{@link #DBCS_CAPABLE_YES DBCS_CAPABLE_YES}
     </ul>
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int DBCS_CAPABLE = 407;

    /**
     Constant indicating that the job is not DBCS-capable.
     @see  #DBCS_CAPABLE
     **/
    public static final String DBCS_CAPABLE_NO = "0";

    /**
     Constant indicating that the job is DBCS-capable.
     @see  #DBCS_CAPABLE
     **/
    public static final String DBCS_CAPABLE_YES = "1";

    /**
     Job attribute representing the decimal format used for this job.  Possible values are:
     <ul>
     <li>{@link #DECIMAL_FORMAT_SYSTEM_VALUE DECIMAL_FORMAT_SYSTEM_VALUE}
     <li>{@link #DECIMAL_FORMAT_PERIOD DECIMAL_FORMAT_PERIOD}
     <li>{@link #DECIMAL_FORMAT_COMMA_J DECIMAL_FORMAT_COMMA_J}
     <li>{@link #DECIMAL_FORMAT_COMMA_I DECIMAL_FORMAT_COMMA_I}
     </ul>
     <p>Type: String
     @see  #getDecimalFormat
     @see  #setDecimalFormat
     **/
    public static final int DECIMAL_FORMAT = 413;

    /**
     Constant indicating the value in the system value QDECFMT is used as the decimal format for this job.
     @see  #DECIMAL_FORMAT
     **/
    public static final String DECIMAL_FORMAT_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating a decimal format that uses a period for a decimal point, a comma for a 3-digit grouping character, and zero-suppresses to the left of the decimal point.
     @see  #DECIMAL_FORMAT
     **/
    public static final String DECIMAL_FORMAT_PERIOD = "";

    /**
     Constant indicating a decimal format that uses a comma for a decimal point and a period for a 3-digit grouping character.  The zero-suppression character is in the second position (rather than the first) to the left of the decimal notation.  Balances with zero values to the left of the comma are written with one leading zero (0,04).  This constant also overrides any edit codes that might suppress the leading zero.
     @see  #DECIMAL_FORMAT
     **/
    public static final String DECIMAL_FORMAT_COMMA_J = "J";

    /**
     Constant indicating a decimal format that uses a comma for a decimal point, a period for a 3-digit grouping character, and zero-suppresses to the left of the decimal point.
     @see  #DECIMAL_FORMAT
     **/
    public static final String DECIMAL_FORMAT_COMMA_I = "I";

    /**
     Job attribute representing the default coded character set identifier (CCSID) used for this job.  This attribute returns zero if the job is not an active job.
     <p>Read-only: true
     <p>Type: Integer
     @see  #getDefaultCodedCharacterSetIdentifier
     **/
    public static final int DEFAULT_CCSID = 412;

    /**
     Job attribute representing the default maximum time (in seconds) that a thread in the job waits for a system instruction, such as a LOCK machine interface (MI) instruction, to acquire a resource.  This default wait time is used when a wait time is not otherwise specified for a given situation.  Normally, this is the amount of time the user is willing to wait for the system before the request is ended.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not appy to subsequent routing steps.  The valid range is 1 through 9999999.  A value of -1 represents no maximum wait time (*NOMAX).
     <p>Type: Integer
     @see  #getDefaultWait
     @see  #setDefaultWait
     **/
    public static final int DEFAULT_WAIT_TIME = 409;

    /**
     Job attribute representing the action taken for interactive jobs when an I/O error occurs for the job's requesting program device.  Possible values are:
     <ul>
     <li>{@link #DEVICE_RECOVERY_ACTION_SYSTEM_VALUE DEVICE_RECOVERY_ACTION_SYSTEM_VALUE}
     <li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE DEVICE_RECOVERY_ACTION_MESSAGE}
     <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE}
     <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST}
     <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB DEVICE_RECOVERY_ACTION_END_JOB}
     <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST}
     </ul>
     <p>Type: String
     @see  #getDeviceRecoveryAction
     @see  #setDeviceRecoveryAction
     **/
    public static final int DEVICE_RECOVERY_ACTION = 410;

    /**
     Constant indicating the value in the system value QDEVRCYACN is used as the device recovery action for this job.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public static final String DEVICE_RECOVERY_ACTION_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating a device recovery action that signals the I/O error message to the application and lets the application program perform error recovery.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public static final String DEVICE_RECOVERY_ACTION_MESSAGE = "*MSG";

    /**
     Constant indicating a device recovery action that disconnects the job when an I/O error occurs.  When the job reconnects, the system sends an error message to the application program, indicating the job has reconnected and that the work station device has recovered.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE = "*DSCMSG";

    /**
     Constant indicating a device recovery action that disconnects the job when an I/O error occurs.  When the job reconnects, the system sends the End Request (ENDRQS) command to return control to the previous request level.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST = "*DSCENDRQS";

    /**
     Constant indicating a device recovery action that ends the job when an I/O error occurs.  A message is sent to the job's log and to the history log (QHST) indicating the job ended because of a device error.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public static final String DEVICE_RECOVERY_ACTION_END_JOB = "*ENDJOB";

    /**
     Constant indicating a device recovery action that ends the job when an I/O error occurs.  There is no job log produced for the job.  The system sends a message to the QHST log indicating the job ended because of a device error.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public static final String DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST = "*ENDJOBNOLIST";

    /**
     Job attribute representing whether or not the job is eligible to be moved out of main storage and put into auxiliary storage at the end of a time slice or when entering a long wait (such as waiting for a work station user's response).  This attribute is ignored when more than one thread is active within the job.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not apply to subsequent routing steps.  Possible values are:
     <ul>
     <li>{@link #ELIGIBLE_FOR_PURGE_YES ELIGIBLE_FOR_PURGE_YES}
     <li>{@link #ELIGIBLE_FOR_PURGE_NO ELIGIBLE_FOR_PURGE_NO}
     <li>{@link #ELIGIBLE_FOR_PURGE_IGNORED ELIGIBLE_FOR_PURGE_IGNORED}
     </ul>
     <p>Type: String
     @see  #getPurge
     @see  #setPurge
     **/
    public static final int ELIGIBLE_FOR_PURGE = 1604;

    /**
     Constant indicating that the job is eligible to be moved out of main storage and put into auxiliary storage.  A job with multiple threads, however, is never purged from main storage.
     @see  #ELIGIBLE_FOR_PURGE
     **/
    public static final String ELIGIBLE_FOR_PURGE_YES = "*YES";

    /**
     Constant indicating that a job is not eligible to be moved out of main storage and put into auxiliary storage.  When main storage is needed, however, pages belonging to a thread in the job may be moved to auxiliary storage.  Then, when a thread in the job runs again, its pages are returned to main storage as they are needed.
     @see  #ELIGIBLE_FOR_PURGE
     **/
    public static final String ELIGIBLE_FOR_PURGE_NO = "*NO";

    /**
     Constant indicating that whether a job is eligible for purge or not is ignored because the job type is either *JOBQ or *OUTQ, or the job is not valid.
     @see  #ELIGIBLE_FOR_PURGE
     **/
    public static final String ELIGIBLE_FOR_PURGE_IGNORED = "";

    /**
     Job attribute representing the message severity level of escape messages that can cause a batch job to end.  The batch job ends when a request in the batch input stream sends an escape message, whose severity is equal to or greater than this value, to the request processing program.
     <p>Read-only: true
     <p>Type: Integer
     @see  #getEndSeverity
     **/
    public static final int END_SEVERITY = 501;

    /**
     Job attribute representing additional information (as described by the FUNCTION_TYPE attribute) about the last high-level function initiated by the initial thread.
     <p>Read-only: true
     <p>Type: String
     @see  #getFunctionName
     **/
    public static final int FUNCTION_NAME = 601;

    /**
     Job attribute representing the last high-level function initiated by the initial thread.  This field may not be cleared when a function is completed.  Possible values are:
     <ul>
     <li>{@link #FUNCTION_TYPE_BLANK FUNCTION_TYPE_BLANK}
     <li>{@link #FUNCTION_TYPE_COMMAND FUNCTION_TYPE_COMMAND}
     <li>{@link #FUNCTION_TYPE_DELAY FUNCTION_TYPE_DELAY}
     <li>{@link #FUNCTION_TYPE_GROUP FUNCTION_TYPE_GROUP}
     <li>{@link #FUNCTION_TYPE_INDEX FUNCTION_TYPE_INDEX}
     <li>{@link #FUNCTION_TYPE_JAVA FUNCTION_TYPE_JAVA}
     <li>{@link #FUNCTION_TYPE_LOG FUNCTION_TYPE_LOG}
     <li>{@link #FUNCTION_TYPE_MRT FUNCTION_TYPE_MRT}
     <li>{@link #FUNCTION_TYPE_MENU FUNCTION_TYPE_MENU}
     <li>{@link #FUNCTION_TYPE_IO FUNCTION_TYPE_IO}
     <li>{@link #FUNCTION_TYPE_PROCEDURE FUNCTION_TYPE_PROCEDURE}
     <li>{@link #FUNCTION_TYPE_PROGRAM FUNCTION_TYPE_PROGRAM}
     <li>{@link #FUNCTION_TYPE_SPECIAL FUNCTION_TYPE_SPECIAL}
     </ul>
     <p>Read-only: true
     <p>Type: String
     @see  #getFunctionType
     **/
    public static final int FUNCTION_TYPE = 602;

    /**
     Constant indicating that the system is not performing a logged function.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_BLANK = "";

    /**
     Constant indicating that a command is running interactively, or it is in a batch input stream, or it was requested from a system menu.  Commands in CL programs or REXX procedures are not logged.  The FUNCTION_NAME attribute contains the name of the command and is only updated when a command is processed.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_COMMAND = "C";

    /**
     Constant indicating that the initial thread of the job is processing a Delay Job (DLYJOB) command.  The FUNCTION_NAME attribute contains the number of seconds the job is delayed (up to 999999 seconds), or the time when the job is to resume processing (HH:MM:SS), depending on how you specified the command.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_DELAY = "D";

    /**
     Constant indicating that the Transfer Group Job (TFRGRPJOB) command suspended the job.  The FUNCTION_NAME attribute contains the group job name for that job.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_GROUP = "G";

    /**
     Constant indicating that the initial thread of the job is rebuilding an index (access path).  The FUNCTION_NAME attribute contains the name of the logical file whose index is rebuilt.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_INDEX = "I";

    /**
     Constant indicating that the initial thread of the job is running a Java Vertual Machine (JVM).  The FUNCTION_NAME attribute contains the name of the java class.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_JAVA = "J";

    /**
     Constant indicating that the system logs history information in a database file.  The FUNCTION_NAME attribute contains the name of the log (QHST is the only log currently supported).
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_LOG = "L";

    /**
     Constant indicating that the job is a multiple requester terminal (MRT) job if the {@link #JOB_TYPE JOB_TYPE} is {@link #JOB_TYPE_BATCH JOB_TYPE_BATCH} and the {@link #JOB_SUBTYPE JOB_SUBTYPE} is {@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT}, or it is an interactive job attached to an MRT job if the {@link #JOB_TYPE JOB_TYPE} is {@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE}.
     <p>For MRT jobs, the FUNCTION_NAME attribute contains information in the following format:
     <ul>
     <li>CHAR(2): The number of requesters currently attached to the MRT job.
     <li>CHAR(1): The field is reserved for a / (slash).
     <li>CHAR(2): The maximum number (MRTMAX) of requesters.
     <li>CHAR(1): Reserved.
     <li>CHAR(3): The never-ending program (NEP) indicator.  If an MRT is also an NEP, the MRT stays active even if there are no requesters of the MRT.  A value of NEP indicates a never-ending program.  A value of blanks indicates that it is not a never-ending program.
     <li>CHAR(1): Reserved.
     </ul>
     <p>For interactive jobs attached to an MRT, the FUNCTION_NAME attribute contains the name of the MRT procedure.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_MRT = "M";

    /**
     Constant indicating that the initial thread of the job is currently at a system menu.  The FUNCTION_NAME field contains the name of the menu.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_MENU = "N";

    /**
     Constant indicating that the job is a subsystem monitor that is performing input/output (I/O) operations to a work station.  The FUNCTION_NAME attribute contains the name of the work station device to which the subsystem is performing an input/output operation.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_IO = "O";

    /**
     Constant indicating that the initial thread of the job is running a procedure.  The FUNCTION_NAME attribute contains the name of the procedure.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_PROCEDURE = "R";

    /**
     Constant indicating that the initial thread of the job is running a program.  The FUNCTION_NAME attribute contains the name of the program.
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_PROGRAM = "P";

    /**
     Constant indicating that the function type is a special function.  For this value, the FUNCTION_NAME attribute contains one of the following values:
     <ul>
     <li>ADLACTJOB: Auxiliary storage is being allocated for the number of active jobs specified in the QADLACTJ system value.  This may indicate that the system value for the initial number of active jobs is too low.
     <li>ADLTOTJOB: Auxiliary storage is being allocated for the number of jobs specified in the QADLTOTJ system value.
     <li>CMDENT: The Command Entry display is being used.
     <li>COMMIT: A commit operation is being performed.
     <li>DIRSHD: Directory shadowing.
     <li>DLTSPLF: The system is deleting a spooled file.
     <li>DUMP: A dump is in process.
     <li>JOBIDXRCY: A damaged job index is being recovered.
     <li>JOBLOG: The system is producing a job log.
     <li>PASSTHRU: The job is a pass-through job.
     <li>RCLSPLSTG: Empty spooled database members are being deleted.
     <li>ROLLBACK: A rollback operation is being performed.
     <li>SPLCLNUP: Spool cleanup is in process.
     </ul>
     @see  #FUNCTION_TYPE
     **/
    public static final String FUNCTION_TYPE_SPECIAL = "*";

    /**
     Constant indicating that the initial thread of the job should be used when retrieving the call stack.
     @see  #getCallStack
     **/
    public static final long INITIAL_THREAD = -1;

    /**
     Job attribute representing how the job answers inquiry messages.  Possible values are:
     <ul>
     <li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED INQUIRY_MESSAGE_REPLY_REQUIRED}
     <li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT INQUIRY_MESSAGE_REPLY_DEFAULT}
     <li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST}
     </ul>
     <p>Type: String
     @see  #getInquiryMessageReply
     @see  #setInquiryMessageReply
     **/
    public static final int INQUIRY_MESSAGE_REPLY = 901;

    /**
     Constant indicating that the job requires an answer for any inquiry messages that occur while this job is running.
     @see  #INQUIRY_MESSAGE_REPLY
     **/
    public static final String INQUIRY_MESSAGE_REPLY_REQUIRED = "*RQD";

    /**
     Constant indicating that the system uses the default message reply to answer any inquiry messages issued while this job is running.  The default reply is either defined in the message description or is the default system reply.
     @see  #INQUIRY_MESSAGE_REPLY
     **/
    public static final String INQUIRY_MESSAGE_REPLY_DEFAULT = "*DFT";

    /**
     Constant indicating that the system reply list is checked to see if there is an entry for an inquiry message issued while this job is running.  If a match occurs, the system uses the reply value for that entry.  If no entry exists for that message, the system uses an inquiry message.
     @see  #INQUIRY_MESSAGE_REPLY
     **/
    public static final String INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST = "*SYSRPYL";

    /**
     Job attribute representing the count of operator interactions, such as pressing the Enter key or a function key.  This field is zero for jobs that have no interactions.
     <p>Read-only: true
     <p>Type: Integer
     @see  #getInteractiveTransactions
     **/
    public static final int INTERACTIVE_TRANSACTIONS = 1402;

    /**
     Job attribute representing the value input to other APIs to increase the speed of locating the job on the system.  The identifier is not valid following an initial program load (IPL).  If you attempt to use it after an IPL, an exception occurs.
     <p>Read-only: true
     <p>Type: String
     <p>Can be loaded by JobList: false
     @see  #getInternalJobID
     @deprecated  The internal job identifier should be treated as a byte array of 16 bytes.
     **/
    public static final int INTERNAL_JOB_ID = 11000;  // Always gets loaded.

    /**
     Job attribute representing the value input to other APIs to increase the speed of locating the job on the system.  The identifier is not valid following an initial program load (IPL).  If you attempt to use it after an IPL, an exception occurs.
     <p>Read-only: true
     <p>Type: byte array
     <p>Can be loaded by JobList: false
     @see  #getInternalJobIdentifier
     **/
    public static final int INTERNAL_JOB_IDENTIFIER = 11007;  // Always gets loaded.

    /**
     Job attribute representing the date used for the job.  This value is for jobs whose status is *JOBQ or *ACTIVE.  For jobs with a status of *OUTQ, the value for this field is blank.
     <p>Type: String in the format CYYMMDD
     @see  #getJobDate
     @see  #setJobDate
     **/
    public static final int JOB_DATE = 1002;

    /**
     Job attribute representing a set of job-related attributes used for one or more jobs on the system.  These attributes determine how the job is run on the system.  Multiple jobs can also use the same job description.
     <p>Read-only: true
     <p>Type: String
     @see  #getJobDescription
     **/
    public static final int JOB_DESCRIPTION = 1003;

    /**
     Job attribute representing the most recent action that caused the job to end.  Possible values are:
     <ul>
     <li>0: Job not ending.
     <li>1: Job ending in a normal manner.
     <li>2: Job ended while it was still on a job queue.
     <li>3: System ended abnormally.
     <li>4: Job ending normally after a controlled end was requested.
     <li>5: Job ending immediately.
     <li>6: Job ending abnormally.
     <li>7: Job ended due to the CPU limit being exceeded.
     <li>8: Job ended due to the storage limit being exceeded.
     <li>9: Job ended due to the message severity level bein exceeded.
     <li>10: Job ended due to the disconnect time interval being exceeded.
     <li>11: Job ended due to the inactivity time interval being exceeded.
     <li>12: Job ended due to a device error.
     <li>13: Job ended due to a signal.
     <li>14: Job ended due to an unhandled error.
     </ul>
     <p>Read-only: true
     <p>Type: Integer
     <p>Only valid on V5R1 systems and higher.
     **/
    public static final int JOB_END_REASON = 1014;

    /**
     Job attribute representing how a job log will be produced when the job completes.  This does not affect job logs produced when the message queue is full and the job message queue full action specifies *PRTWRAP.  Messages in the job messages queue are written to a spooled file, from which the job log can be printed, unless the Control Job Log Output (QMHCTLJL) API was used in the job to specify that the messages in the job log are to be written to a database file.
     <p>The job log output value can be changed at any time until the job log has been produced or removed.  To change the job log output value for a job, use the Change Job (QWTCHGJB) API or the Change Job (CHGJOB) command.
     <p>The job log can be displayed at any time until the job log has been produced or removed.  To display a job's job log, use the Display Job Log (DSPJOBLOG) command.
     <p>The job log can be removed when the job has completed and the job log has not yet been produced or removed.  To remove the job log, use the Remove Pending Job Log (QWTRMVJL) API or the End Job (ENDJOB) command.
     <p>Possible values are:
     <ul>
     <li>{@link #JOB_LOG_OUTPUT_SYSTEM_VALUE JOB_LOG_OUTPUT_SYSTEM_VALUE}
     <li>{@link #JOB_LOG_OUTPUT_JOB_LOG_SERVER JOB_LOG_OUTPUT_JOB_LOG_SERVER}
     <li>{@link #JOB_LOG_OUTPUT_JOB_END JOB_LOG_OUTPUT_JOB_END}
     <li>{@link #JOB_LOG_OUTPUT_PENDING JOB_LOG_OUTPUT_PENDING}
     </ul>
     <p>Type: String
     <p>Only valid on V5R4 systems and higher.
     **/
    public static final int JOB_LOG_OUTPUT = 1018;

    /**
     Constant indicating that the value is specifed by the QLOGOUTPUT system value.
     @see  #JOB_LOG_OUTPUT
     **/
    public static final String JOB_LOG_OUTPUT_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating that the job log will be produced by a job log server.  For more information about job log servers, refer to the Start Job Log Server (STRLOGSVR) command.
     @see  #JOB_LOG_OUTPUT
     **/
    public static final String JOB_LOG_OUTPUT_JOB_LOG_SERVER = "*JOBLOGSVR";

    /**
     Constant indicating that the job log will be produced by the job itself.  If the job cannot produce its own job log, the job log will be produced by a job log server.  For example, a job does not produce its own job log when the system is processing a Power Down System (PWRDWNSYS) command.
     @see  #JOB_LOG_OUTPUT
     **/
    public static final String JOB_LOG_OUTPUT_JOB_END = "*JOBEND";

    /**
     Constant indicating that the job log will not be produced.  The job log remains pending until removed.
     @see  #JOB_LOG_OUTPUT
     **/
    public static final String JOB_LOG_OUTPUT_PENDING = "*PND";

    /**
     Job attribute representing whether a job's log has been written or not.  If the system fails while the job was active or the job ends abnormally, the job log may not be written yet.  This flag remains on until the job log has been written.  Possible values are:
     <ul>
     <li>{@link #JOB_LOG_PENDING_NO JOB_LOG_PENDING_NO}
     <li>{@link #JOB_LOG_PENDING_YES JOB_LOG_PENDING_YES}
     </ul>
     <p>Read-only: true
     <p>Type: String
     <p>Only valid on V5R1 systems and higher.
     **/
    public static final int JOB_LOG_PENDING = 1015;

    /**
     Constant indicating that the job log is not pending.
     @see  #JOB_LOG_PENDING
     **/
    public static final String JOB_LOG_PENDING_NO = "0";

    /**
     Constant indicating that the job log is pending and waiting to be written.
     @see  #JOB_LOG_PENDING
     **/
    public static final String JOB_LOG_PENDING_YES = "1";

    /**
     Job attribute representing the name of the job as identified to the system.  For an interactive job, the system assigns the job name of the work station where the job started; for a batch job, you specify the name in the command when you submit the job.  Possible values are:
     <ul>
     <li>A specific job name.
     <li>{@link #JOB_NAME_INTERNAL JOB_NAME_INTERNAL}
     <li>{@link #JOB_NAME_CURRENT JOB_NAME_CURRENT}
     </ul>
     <p>Type: String
     @see  #getName
     @see  #setName
     **/
    public static final int JOB_NAME = 11001;  // Always gets loaded.

    /**
     Constant indicating that the INTERNAL_JOB_ID locates the job.  The user name and job number must be blank.
     @see  #JOB_NAME
     **/
    public static final String JOB_NAME_INTERNAL = "*INT";

    /**
     Constant indicating the job that this program is running in.  The user name and job number must be blank.
     @see  #JOB_NAME
     **/
    public static final String JOB_NAME_CURRENT = "*";

    /**
     Job attribute representing the system-generated job number.  Possible values are:
     <ul>
     <li>A specific job number.
     <li>{@link #JOB_NUMBER_BLANK JOB_NUMBER_BLANK}
     </ul>
     <p>Type: String
     @see  #getNumber
     @see  #setNumber
     **/
    public static final int JOB_NUMBER = 11002;  // Always gets loaded.

    /**
     Constant indicating a blank job number.  This must be used if JOB_NAME_INTERNAL or JOB_NAME_CURRENT is specified for the JOB_NAME.
     @see  #JOB_NUMBER
     **/
    public static final String JOB_NUMBER_BLANK = "";

    /**
     Job attribute representing the name of the job queue that the job is currently on, or that the job was on if it is currently active.  This attribute is valid to be set for jobs whose status is *JOBQ.  Attempting to set this attribute for jobs with a status of *OUTQ or *ACTIVE will cause an error to be signaled.  This attribute is valid to be retrieved for jobs whose status is *JOBQ or *ACTIVE.  Retrieving this attribute for jobs with a status of *OUTQ will return a value of blanks.
     <p>Type: String
     @see  #getQueue
     @see  #setQueue
     **/
    public static final int JOB_QUEUE = 1004;

    /**
     Job attribute representing the date and time this job was put on this job queue.  This field will contain blanks if the job was not on a job queue.
     <p>Read-only: true
     <p>Type: String in the system timestamp format
     @see  #getJobPutOnJobQueueDate
     **/
    public static final int JOB_QUEUE_DATE = 404;

    /**
     Job attribute representing the scheduling priority of the job compared to other jobs on the same job queue.  The highest priority is 0 and the lowest is 9.  This value is valid for jobs whose status is *JOBQ or *ACTIVE.  For jobs with a status of *OUTQ, attempting to set this attribute will cause an error to be signaled and Retrieving this attribute will return a value of blanks.
     <p>Type: String
     @see  #getQueuePriority
     @see  #setQueuePriority
     **/
    public static final int JOB_QUEUE_PRIORITY = 1005;

    /**
     Job attribute representing the status of this job on the job queue.  Possible values are:
     <ul>
     <li>{@link #JOB_QUEUE_STATUS_BLANK JOB_QUEUE_STATUS_BLANK}
     <li>{@link #JOB_QUEUE_STATUS_SCHEDULED JOB_QUEUE_STATUS_SCHEDULED}
     <li>{@link #JOB_QUEUE_STATUS_HELD JOB_QUEUE_STATUS_HELD}
     <li>{@link #JOB_QUEUE_STATUS_READY JOB_QUEUE_STATUS_READY}
     </ul>
     <p>Read-only: true
     <p>Type: String
     @see  #getJobStatusInJobQueue
     **/
    public static final int JOB_QUEUE_STATUS = 1903;

    /**
     Constant indicating that this job was not on a job queue.
     @see  #JOB_QUEUE_STATUS
     **/
    public static final String JOB_QUEUE_STATUS_BLANK = "";

    /**
     Constant indicating that this job will run as scheduled.
     @see  #JOB_QUEUE_STATUS
     **/
    public static final String JOB_QUEUE_STATUS_SCHEDULED = "SCD";

    /**
     Constant indicating that this job is being held on the job queue.
     @see  #JOB_QUEUE_STATUS
     **/
    public static final String JOB_QUEUE_STATUS_HELD = "HLD";

    /**
     Constant indicating that this job is ready to be selected.
     @see  #JOB_QUEUE_STATUS
     **/
    public static final String JOB_QUEUE_STATUS_READY = "RLS";

    /**
     Job attribute representing the status of this job.  Possible values are:
     <ul>
     <li>{@link #JOB_STATUS_ACTIVE JOB_STATUS_ACTIVE}
     <li>{@link #JOB_STATUS_JOBQ JOB_STATUS_JOBQ}
     <li>{@link #JOB_STATUS_OUTQ JOB_STATUS_OUTQ}
     </ul>
     <p>Read-only: true
     <p>Type: String
     @see  #getStatus
     **/
    public static final int JOB_STATUS = 11003;  // Always gets loaded.

    /**
     Constant indicating this job is an active job.  This includes group jobs, system request jobs, and disconnected jobs.
     @see  #JOB_STATUS
     **/
    public static final String JOB_STATUS_ACTIVE = "*ACTIVE";

    /**
     Constant indicating this job is currently on a job queue.
     @see  #JOB_STATUS
     **/
    public static final String JOB_STATUS_JOBQ = "*JOBQ";

    /**
     Constant indicating this job has completed running, but still has output on an output queue.
     @see  #JOB_STATUS
     **/
    public static final String JOB_STATUS_OUTQ = "*OUTQ";

    /**
     Job attribute representing additional information about the job type (if any exists).  Possible values are:
     <ul>
     <li>{@link #JOB_SUBTYPE_BLANK JOB_SUBTYPE_BLANK}
     <li>{@link #JOB_SUBTYPE_IMMEDIATE JOB_SUBTYPE_IMMEDIATE}
     <li>{@link #JOB_SUBTYPE_PROCEDURE_START_REQUEST JOB_SUBTYPE_PROCEDURE_START_REQUEST}
     <li>{@link #JOB_SUBTYPE_MACHINE_SERVER_JOB JOB_SUBTYPE_MACHINE_SERVER_JOB}
     <li>{@link #JOB_SUBTYPE_PRESTART JOB_SUBTYPE_PRESTART}
     <li>{@link #JOB_SUBTYPE_PRINT_DRIVER JOB_SUBTYPE_PRINT_DRIVER}
     <li>{@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT}
     <li>{@link #JOB_SUBTYPE_ALTERNATE_SPOOL_USER JOB_SUBTYPE_ALTERNATE_SPOOL_USER}
     </ul>
     <p>Read-only: true
     <p>Type: String
     @see  #getSubtype
     **/
    public static final int JOB_SUBTYPE = 11004;  // Always gets loaded.

    /**
     Constant indicating that the job has no special subtype or is not a valid job.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_BLANK = "";

    /**
     Constant indicating that the job is an immediate job.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_IMMEDIATE = "D";

    /**
     Constant indicating that the job started with a procedure start request.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_PROCEDURE_START_REQUEST = "E";

    /**
     Constant indicating that the job is an AS/400 Advanced 36 machine server job.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_MACHINE_SERVER_JOB = "F";

    /**
     Constant indicating that the job is a prestart job.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_PRESTART = "J";

    /**
     Constant indicating that the job is a print driver job.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_PRINT_DRIVER = "P";

    /**
     Constant indicating that the job is a System/36 multiple requester terminal (MRT) job.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_MRT = "T";

    /**
     Constant indicating that the job is an alternate spool user job.
     @see  #JOB_SUBTYPE
     **/
    public static final String JOB_SUBTYPE_ALTERNATE_SPOOL_USER = "U";

    /**
     Job attribute representing the current setting of the job switches used by this job.  This value is valid for all job types.
     <p>Type: String
     @see  #getJobSwitches
     @see  #setJobSwitches
     **/
    public static final int JOB_SWITCHES = 1006;

    /**
     Job attribute representing the type of job.  Possible values are:
     <ul>
     <li>{@link #JOB_TYPE_NOT_VALID JOB_TYPE_NOT_VALID}
     <li>{@link #JOB_TYPE_AUTOSTART JOB_TYPE_AUTOSTART}
     <li>{@link #JOB_TYPE_BATCH JOB_TYPE_BATCH}
     <li>{@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE}
     <li>{@link #JOB_TYPE_SUBSYSTEM_MONITOR JOB_TYPE_SUBSYSTEM_MONITOR}
     <li>{@link #JOB_TYPE_SPOOLED_READER JOB_TYPE_SPOOLED_READER}
     <li>{@link #JOB_TYPE_SYSTEM JOB_TYPE_SYSTEM}
     <li>{@link #JOB_TYPE_SPOOLED_WRITER JOB_TYPE_SPOOLED_WRITER}
     <li>{@link #JOB_TYPE_SCPF_SYSTEM JOB_TYPE_SCPF_SYSTEM}
     </ul>
     <p>Read-only: true
     <p>Type: String
     @see  #getType
     **/
    public static final int JOB_TYPE = 11005;  // Always gets loaded.

    /**
     Constant indicating that the job is not a valid job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_NOT_VALID = "";

    /**
     Constant indicating that the job is an autostart job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_AUTOSTART = "A";

    /**
     Constant indicating that the job is a batch job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_BATCH = "B";

    /**
     Constant indicating that the job is an interactive job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_INTERACTIVE = "I";

    /**
     Constant indicating that the job is a subsystem monitor job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_SUBSYSTEM_MONITOR = "M";

    /**
     Constant indicating that the job is a spooled reader job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_SPOOLED_READER = "R";

    /**
     Constant indicating that the job is a system job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_SYSTEM = "S";

    /**
     Constant indicating that the job is a spooled writer job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_SPOOLED_WRITER = "W";

    /**
     Constant indicating that the job is the SCPF system job.
     @see  #JOB_TYPE
     **/
    public static final String JOB_TYPE_SCPF_SYSTEM = "X";

    /**
     Job attribute representing the type of job.  This attribute combines the JOB_TYPE and JOB_SUBTYPE attributes.  Possible values are:
     <ul>
     <li>{@link #JOB_TYPE_ENHANCED_AUTOSTART JOB_TYPE_ENHANCED_AUTOSTART}
     <li>{@link #JOB_TYPE_ENHANCED_BATCH JOB_TYPE_ENHANCED_BATCH}
     <li>{@link #JOB_TYPE_ENHANCED_BATCH_IMMEDIATE JOB_TYPE_ENHANCED_BATCH_IMMEDIATE}
     <li>{@link #JOB_TYPE_ENHANCED_BATCH_MRT JOB_TYPE_ENHANCED_BATCH_MRT}
     <li>{@link #JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER}
     <li>{@link #JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST}
     <li>{@link #JOB_TYPE_ENHANCED_INTERACTIVE JOB_TYPE_ENHANCED_INTERACTIVE}
     <li>{@link #JOB_TYPE_ENHANCED_INTERACTIVE_GROUP JOB_TYPE_ENHANCED_INTERACTIVE_GROUP}
     <li>{@link #JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ JOB_TYPE_ENHANCED_SYSREQ}
     <li>{@link #JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP}
     <li>{@link #JOB_TYPE_ENHANCED_PRESTART JOB_TYPE_ENHANCED_PRESTART}
     <li>{@link #JOB_TYPE_ENHANCED_PRESTART_BATCH JOB_TYPE_ENHANCED_PRESTART_BATCH}
     <li>{@link #JOB_TYPE_ENHANCED_PRESTART_COMM JOB_TYPE_ENHANCED_PRESTART_COMM}
     <li>{@link #JOB_TYPE_ENHANCED_READER JOB_TYPE_ENHANCED_READER}
     <li>{@link #JOB_TYPE_ENHANCED_SUBSYSTEM JOB_TYPE_ENHANCED_SUBSYSTEM}
     <li>{@link #JOB_TYPE_ENHANCED_SYSTEM JOB_TYPE_ENHANCED_SYSTEM}
     <li>{@link #JOB_TYPE_ENHANCED_WRITER JOB_TYPE_ENHANCED_WRITER}
     </ul>
     <p>Read-only: true
     <p>Type: Integer
     <p>Only valid on V5R1 systems and higher.
     **/
    public static final int JOB_TYPE_ENHANCED = 1016;

    /**
     Constant indicating that the job is an autostart job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_AUTOSTART = new Integer(110);

    /**
     Constant indicating that the job is a batch job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH = new Integer(210);

    /**
     Constant indicating that the job is a batch immediate job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH_IMMEDIATE = new Integer(220);

    /**
     Constant indicating that the job is a batch System/36 multiple requester terminal (MRT) job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH_MRT = new Integer(230);

    /**
     Constant indicating that the job is a batch alternate spool user job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER = new Integer(240);

    /**
     Constant indicating that the job is a communications procedure start request job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST = new Integer(310);

    /**
     Constant indicating that the job is an interactive job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE = new Integer(910);

    /**
     Constant indicating that the job is an interactive job that is part of a group.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE_GROUP = new Integer(920);

    /**
     Constant indicating that the job is an interactive job that is part of a system request pair.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ = new Integer(930);

    /**
     Constant indicating that the job is an interactive job that is part of a system request pair and part of a group.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP = new Integer(940);

    /**
     Constant indicating that the job is a prestart job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_PRESTART = new Integer(1610);

    /**
     Constant indicating that the job is a prestart batch job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_PRESTART_BATCH = new Integer(1620);

    /**
     Constant indicating that the job is a prestart communications job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_PRESTART_COMM = new Integer(1630);

    /**
     Constant indicating that the job is a reader job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_READER = new Integer(1810);

    /**
     Constant indicating that the job is a subsystem job.
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_SUBSYSTEM = new Integer(1910);

    /**
     Constant indicating that the job is a system job (all system jobs including SCPF).
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_SYSTEM = new Integer(1920);

    /**
     Constant indicating that the job is a writer job (including both spool writers and print drivers).
     @see  #JOB_TYPE_ENHANCED
     **/
    public static final Integer JOB_TYPE_ENHANCED_WRITER = new Integer(2310);

    /**
     Job attribute representing the user profile name by which the job is known to other jobs on the system.  The job user identity is used for authorization checks when other jobs on the system attempt to operate against the job.  For more detail on how the job user identity is set and used, refer to the Set Job User Identity (QWTSJUID) API.  For jobs that are on a job queue or have completed running, the job user identity is same as the user name from the qualified job name.  This attribute will return blanks for these jobs.  A value of *N is returned if the job user identity is set, but the user profile to which it is set no longer exists.
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int JOB_USER_IDENTITY = 1012;

    /**
     Job attribute representing the method by which the job user identity was set.  Possible values are:
     <ul>
     <li>{@link #JOB_USER_IDENTITY_SETTING_DEFAULT JOB_USER_IDENTITY_SETTING_DEFAULT}
     <li>{@link #JOB_USER_IDENTITY_SETTING_APPLICATION JOB_USER_IDENTITY_SETTING_APPLICATION}
     <li>{@link #JOB_USER_IDENTITY_SETTING_SYSTEM JOB_USER_IDENTITY_SETTING_SYSTEM}
     </ul>
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int JOB_USER_IDENTITY_SETTING = 1013;

    /**
     Constant indicating that the job is currently running single threaded and the job user identity is the name of the user profile under which the job is currently running.  This value is also returned for jobs that are on a job queue or have completed running.  This has the same meaning as a value of *DEFAULT on the Display Job Status Attributes display.
     @see  #JOB_USER_IDENTITY_SETTING
     **/
    public static final String JOB_USER_IDENTITY_SETTING_DEFAULT = "0";

    /**
     Constant indicating that the job user identity was explicitly set by an application using one of the Set Job User Identity APIs, QWTSJUID or QwtSetJuid().  The job may be running either single threaded or multithreaded.  This has the same meaning as a value of *APPLICATION on the Display Job Status Attributes display.
     @see  #JOB_USER_IDENTITY_SETTING
     **/
    public static final String JOB_USER_IDENTITY_SETTING_APPLICATION = "1";

    /**
     Constant indicating that the job is currently running multithreaded and the job user identity was implicitly set by the system when the job became multithreaded.  It was set to the name of the user profile that the job was running under when it became multithreaded.  This has the same meaning as a value of *SYSTEM on the Display Job Status Attributes display.
     @see  #JOB_USER_IDENTITY_SETTING
     **/
    public static final String JOB_USER_IDENTITY_SETTING_SYSTEM = "2";

    /**
     Job attribute representing whether connections using distributed data management (DDM) protocols remain active when they are not being used.  The connections include APPC conversations, active TCP/IP connections, or Opti-Connect connections.  The DDM protocols are used in Distributed Relational Database Architecture (DRDA) applications, DDM applications, or DB2 Multisystem applications.  Possible values are:
     <ul>
     <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP KEEP_DDM_CONNECTIONS_ACTIVE_KEEP}
     <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP KEEP_DDM_CONNECTIONS_ACTIVE_DROP}
     </ul>
     <p>Type: String
     @see  #getDDMConversationHandling
     @see  #setDDMConversationHandling
     **/
    public static final int KEEP_DDM_CONNECTIONS_ACTIVE = 408;  // DDM conversation handling.

    /**
     Constant indicating that the system keeps DDM connections active when there are no users, except for the following:
     <ul>
     <li>The routing step ends on the source system.  The routing step ends when the job ends or when the job is rerouted to another routing step.
     <li>The Reclaim Distributed Data Management Conversation (RCLDDMCNV) command or the Reclaim Resources (RCLRSC) command runs.
     <li>A communications failure or an internal failure occurs.
     <li>A DRDA connection to an application server not running on an IBM i system ends.
     </ul>
     @see  #KEEP_DDM_CONNECTIONS_ACTIVE
     **/
    public static final String KEEP_DDM_CONNECTIONS_ACTIVE_KEEP = "*KEEP";

    /**
     Constant indicating that the system ends a DDM connection when there are no users.  Examples include when an application closes a DDM file, or when a DRDA application runs an SQL DISCONNECT statement.
     @see  #KEEP_DDM_CONNECTIONS_ACTIVE
     **/
    public static final String KEEP_DDM_CONNECTIONS_ACTIVE_DROP = "*DROP";

    /**
     Job attribute representing the language identifier associated with this job.  The language identifier is used when *LANGIDUNQ or *LANGIDSHR is specified on the sort sequence parameter.  If the job CCSID is 65535, this parameter is also used to determine the value of the job default CCSID.  Possible values are:
     <ul>
     <li>{@link #LANGUAGE_ID_SYSTEM_VALUE LANGUAGE_ID_SYSTEM_VALUE}
     <li>{@link #LANGUAGE_ID_INITIAL_USER LANGUAGE_ID_INITIAL_USER}
     <li>The language identifier.
     </ul>
     <p>Type: String
     @see  #getLanguageID
     @see  #setLanguageID
     **/
    public static final int LANGUAGE_ID = 1201;

    /**
     Constant indicating the system value QLANGID is used.
     @see  #LANGUAGE_ID
     **/
    public static final String LANGUAGE_ID_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating the language ID specified in the user profile under which this thread was initially running is used.
     @see  #LANGUAGE_ID
     **/
    public static final String LANGUAGE_ID_INITIAL_USER = "*USRPRF";

    /**
     Job attribute representing whether or not commands are logged for CL programs that are run.  Possible values are:
     <ul>
     <li>{@link #LOG_CL_PROGRAMS_YES LOG_CL_PROGRAMS_YES}
     <li>{@link #LOG_CL_PROGRAMS_NO LOG_CL_PROGRAMS_NO}
     </ul>
     <p>Type: String
     @see  #getLoggingCLPrograms
     @see  #setLoggingCLPrograms
     **/
    public static final int LOG_CL_PROGRAMS = 1203;

    /**
     Constant indicating that commands are logged for CL programs that are run.
     @see  #LOG_CL_PROGRAMS
     **/
    public static final String LOG_CL_PROGRAMS_YES = "*YES";

    /**
     Constant indicating that commands are not logged for CL programs that are run.
     @see  #LOG_CL_PROGRAMS
     **/
    public static final String LOG_CL_PROGRAMS_NO = "*NO";

    /**
     Job attribute representing what type of information is logged.  Possible values are:
     <ul>
     <li>{@link #LOGGING_LEVEL_NONE LOGGING_LEVEL_NONE}
     <li>{@link #LOGGING_LEVEL_MESSAGES_BY_SEVERITY LOGGING_LEVEL_MESSAGES_BY_SEVERITY}
     <li>{@link #LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES}
     <li>{@link #LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES}
     <li>{@link #LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES}
     </ul>
     <p>Type: String
     @see  #getLoggingLevel
     @see  #setLoggingLevel
     **/
    public static final int LOGGING_LEVEL = 1202;

    /**
     Constant indicating that no messages are logged.
     @see  #LOGGING_LEVEL
     **/
    public static final String LOGGING_LEVEL_NONE = "0";

    /**
     Constant indicating that all messages sent to the job's external message queue with a severity greater than or equal to the message logging severity are logged.  This includes the indication of job start, job end, and job completion status.
     @see  #LOGGING_LEVEL
     **/
    public static final String LOGGING_LEVEL_MESSAGES_BY_SEVERITY = "1";

    /**
     Constant indicating that the following information is logged:
     <ul>
     <li>Logging level 1 information.
     <li>Request messages that result in a high-level message with a severity code greater than or equal to the logging severity cause the request message and all associated messages to be logged.  A high-level message is one that is sent to the program message queue of the program that receives the request message.  For example, QCMD is an IBM-supplied request processing program that receives request messages.
     </ul>
     @see  #LOGGING_LEVEL
     **/
    public static final String LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES = "2";

    /**
     Constant indicating that the following information is logged:
     <ul>
     <li>Logging level 1 and 2 information is logged.
     <li>All request messages are logged.
     <li>Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS attribute and the log attribute of the CL program.
     </ul>
     @see  #LOGGING_LEVEL
     **/
    public static final String LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES = "3";

    /**
     Constant indicating that the following information is logged:
     <ul>
     <li>All request messages and all messages with a severity greater than or equal to the message logging severity, including trace messages.
     <li>Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS attribute and the log attribute of the CL program.
     </ul>
     @see  #LOGGING_LEVEL
     **/
    public static final String LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES = "4";

    /**
     Job attribute representing the severity level that is used in conjunction with the logging level to determine which error messages are logged in the job log.  The values range from 00 through 99.
     <p>Type: Integer
     @see  #getLoggingSeverity
     @see  #setLoggingSeverity
     **/
    public static final int LOGGING_SEVERITY = 1204;

    /**
     Job attribute representing the level of message text that is written in the job log when a message is logged according to the LOGGING_LEVEL and LOGGING_SEVERITY.  Possible values are:
     <ul>
     <li>{@link #LOGGING_TEXT_MESSAGE LOGGING_TEXT_MESSAGE}
     <li>{@link #LOGGING_TEXT_SECLVL LOGGING_TEXT_SECLVL}
     <li>{@link #LOGGING_TEXT_NO_LIST LOGGING_TEXT_NO_LIST}
     </ul>
     <p>Type: String
     @see  #getLoggingText
     @see  #setLoggingText
     **/
    public static final int LOGGING_TEXT = 1205;

    /**
     Constant indicating that only the message text is written to the job log.
     @see  #LOGGING_TEXT
     **/
    public static final String LOGGING_TEXT_MESSAGE = "*MSG";

    /**
     Constant indicating that both the message text and the message help (cause and recovery) of the error message are written to the job log.
     @see  #LOGGING_TEXT
     **/
    public static final String LOGGING_TEXT_SECLVL = "*SECLVL";

    /**
     Constant indicating that if the job ends normally, no job log is produced.  If the job ends abnormally (the job end code is 20 or higher), a job log is produced.  The messages that appear in the job log contain both the message text and the message. help.
     @see  #LOGGING_TEXT
     **/
    public static final String LOGGING_TEXT_NO_LIST = "*NOLIST";

    /**
     Job attribute representing the maximum processing unit time (in milliseconds) that the job can use.  If the job consists of multiple routing steps, this is the maximum processing unit time that the current routing step can use.  If the maximum time is exceeded, the job is ended.  A value of -1 is returned if there is no maximum (*NOMAX).  A value of zero is returned if the job is not active.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int MAX_CPU_TIME = 1302;

    /**
     Job attribute representing the maximum amount of auxiliary storage (in kilobytes) that the job can use.  If the job consists of multiple routing steps, this is the maximum temporary storage that the routing step can use.  This temporary storage is used for storage required by the program itself and by implicitly created internal system objects used to support the routing step.  (It does not include storage in the QTEMP library.)  If the maximum temporary storage is exceeded, the job is ended.  This does not apply to the use of permanent storage, which is controlled through the user profile.  A value of -1 is returned if there is no maximum (*NOMAX).
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int MAX_TEMP_STORAGE = 1303;

    /**
     Job attribute representing the maximum amount of auxiliary storage (in megabytes) that the job can use.  If the job consists of multiple routing steps, this is the maximum temporary storage that the routing step can use.  This temporary storage is used for storage required by the program itself and by implicitly created internal system objects used to support the routing step.  (It does not include storage in the QTEMP library.)  If the maximum temporary storage is exceeded, the job is ended.  This does not apply to the use of permanent storage, which is controlled through the user profile.  A value of -1 is returned if there is no maximum (*NOMAX).
     <p>Read-only: true
     <p>Type: Long
     **/
    public static final int MAX_TEMP_STORAGE_LARGE = 1305;

    /**
     Job attribute representing the maximum number of threads that a job can run with at any time.  If multiple threads are initiated simultaneously, this value may be exceeded.  If this maximum value is exceeded, the excess threads will be allowed to run to their normal completion.  Initiation of additional threads will be inhibited until the maximum number of threads in the job drops below this maximum value.  A value of -1 is returned if there is no maximum (*NOMAX).  Depending upon the resources used by the threads and the resources available to the system, the initiation of additional threads may be inhibited before this maximum value is reached.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int MAX_THREADS = 1304;

    /**
     Job attribute representing the name of the memory pool in which the job started running.  The name may be a number, in which case it is a private pool associated with a subsystem.  Possible values are:
     <ul>
     <li>{@link #MEMORY_POOL_MACHINE MEMORY_POOL_MACHINE}
     <li>{@link #MEMORY_POOL_BASE MEMORY_POOL_BASE}
     <li>{@link #MEMORY_POOL_INTERACTIVE MEMORY_POOL_INTERACTIVE}
     <li>{@link #MEMORY_POOL_SPOOL MEMORY_POOL_SPOOL}
     <li>*SHRPOOL1 - *SHRPOOL60: This job is running in the identified shared pool.
     <li>01 - 99: This job is running in the identified private pool.
     </ul>
     <p>Read-only: true
     <p>Type: String
     <p>Only valid on V5R1 systems and higher.
     **/
    public static final int MEMORY_POOL = 1306;

    /**
     Constant indicating that this job is running in the machine pool.
     @see  #MEMORY_POOL
     **/
    public static final String MEMORY_POOL_MACHINE = "*MACHINE";

    /**
     Constant indicating that this job is running in the base system pool, which can be shared with other subsystems.
     @see  #MEMORY_POOL
     **/
    public static final String MEMORY_POOL_BASE = "*BASE";

    /**
     Constant indicating that this job is running in the shared pool used for interactive work.
     @see  #MEMORY_POOL
     **/
    public static final String MEMORY_POOL_INTERACTIVE = "*INTERACT";

    /**
     Constant indicating that this job is running in the shared pool for spooled writers.
     @see  #MEMORY_POOL
     **/
    public static final String MEMORY_POOL_SPOOL = "*SPOOL";

    /**
     Job attribute representing whether the job is waiting for a reply to a specific message.  This value applies only when either the ACTIVE_JOB_STATUS or ACTIVE_JOB_STATUS_FOR_JOBS_ENDING attributes are set to ACTIVE_JOB_STATUS_WAIT_MESSAGE.  Possible values are:
     <ul>
     <li>{@link #MESSAGE_REPLY_NOT_IN_MESSAGE_WAIT MESSAGE_REPLY_NOT_IN_MESSAGE_WAIT}
     <li>{@link #MESSAGE_REPLY_WAITING MESSAGE_REPLY_WAITING}
     <li>{@link #MESSAGE_REPLY_NOT_WAITING MESSAGE_REPLY_NOT_WAITING}
     </ul>
     <p>Read-only: true
     <p>Type: String
     <p>Only valid on V5R1 systems and higher.
     **/
    public static final int MESSAGE_REPLY = 1307;

    /**
     Constant indicating that the job currently is not in message wait status.
     @see  #MESSAGE_REPLY
     **/
    public static final String MESSAGE_REPLY_NOT_IN_MESSAGE_WAIT = "0";

    /**
     Constant indicating that the job is waiting for a reply to a message.
     @see  #MESSAGE_REPLY
     **/
    public static final String MESSAGE_REPLY_WAITING = "1";

    /**
     Constant indicating that the job is not waiting for a reply to a message.
     @see  #MESSAGE_REPLY
     **/
    public static final String MESSAGE_REPLY_NOT_WAITING = "2";

    /**
     Job attribute representing the action to take when the message queue is full.  Possible values are:
     <ul>
     <li>{@link #MESSAGE_QUEUE_ACTION_SYSTEM_VALUE MESSAGE_QUEUE_ACTION_SYSTEM_VALUE}
     <li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP MESSAGE_QUEUE_ACTION_NO_WRAP}
     <li>{@link #MESSAGE_QUEUE_ACTION_WRAP MESSAGE_QUEUE_ACTION_WRAP}
     <li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP MESSAGE_QUEUE_ACTION_PRINT_WRAP}
     </ul>
     <p>Type: String
     @see  #getJobMessageQueueFullAction
     @see  #setJobMessageQueueFullAction
     **/
    public static final int MESSAGE_QUEUE_ACTION = 1007;  // Job message queue full action.

    /**
     Constant indicating the value specified for the QJOBMSGQFL system value is used.
     @see  #MESSAGE_QUEUE_ACTION
     **/
    public static final String MESSAGE_QUEUE_ACTION_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating that when the job message queue is full, do not wrap.  This action causes the job to end.
     @see  #MESSAGE_QUEUE_ACTION
     **/
    public static final String MESSAGE_QUEUE_ACTION_NO_WRAP = "*NOWRAP";

    /**
     Constant indicating that when the job message queue is full, wrap to the beginning and start filling again.
     @see  #MESSAGE_QUEUE_ACTION
     **/
    public static final String MESSAGE_QUEUE_ACTION_WRAP = "*WRAP";

    /**
     Constant indicating that when the job message queue is full, wrap the message queue and print the messages that are being overlaid because of the wrapping.
     @see  #MESSAGE_QUEUE_ACTION
     **/
    public static final String MESSAGE_QUEUE_ACTION_PRINT_WRAP = "*PRTWRAP";

    /**
     Job attribute representing the maximum size (in megabytes) that the job message queue can become.  The range is 2 to 64.
     <p>Read-only: true
     <p>Type: Integer
     @see  #getJobMessageQueueMaximumSize
     **/
    public static final int MESSAGE_QUEUE_MAX_SIZE = 1008;

    /**
     Job attribute representing the mode name of the advanced program-to-program communications (APPC) device that started the job.  Possible values are:
     <ul>
     <li>The mode name is *BLANK.
     <li>The mode name is blanks.
     <li>The name of the mode.
     </ul>
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int MODE = 1301;

    /**
     Job attribute representing the name of the default output queue that is used for spooled output produced by this job.  The default output queue is only for spooled printer files that specify *JOB for the output queue.  Possible values are:
     <ul>
     <li>{@link #OUTPUT_QUEUE_DEVICE OUTPUT_QUEUE_DEVICE}
     <li>{@link #OUTPUT_QUEUE_WORK_STATION OUTPUT_QUEUE_WORK_STATION}
     <li>{@link #OUTPUT_QUEUE_INITIAL_USER OUTPUT_QUEUE_INITIAL_USER}
     <li>Output queue name.
     </ul>
     <p>Type: String
     @see  #getOutputQueue
     @see  #setOutputQueue
     **/
    public static final int OUTPUT_QUEUE = 1501;

    /**
     Constant indicating the device specified on the Create Printer File (CRTPRTF), Change Printer File (CHGPRTF), or Override with Printer File (OVRPRTF) commands is used.
     @see  #OUTPUT_QUEUE
     **/
    public static final String OUTPUT_QUEUE_DEVICE = "*DEV";

    /**
     Constant indicating the default output queue that is used with this job is the output queue that is assigned to the work station associated with the job at the time the job is started.
     @see  #OUTPUT_QUEUE
     **/
    public static final String OUTPUT_QUEUE_WORK_STATION = "*WRKSTN";

    /**
     Constant indicating the output queue name specified in the user profile under which this thread was initially running is used.
     @see  #OUTPUT_QUEUE
     **/
    public static final String OUTPUT_QUEUE_INITIAL_USER = "*USRPRF";

    /**
     Job attribute representing the output priority for spooled output files that this job produces.  The highest priority is 0, and the lowest is 9.
     <p>Type: String
     @see  #getOutputQueuePriority
     @see  #setOutputQueuePriority
     **/
    public static final int OUTPUT_QUEUE_PRIORITY = 1502;

    /**
     Job attribute representing whether border and header information is provided when the Print key is pressed.  Possible values are:
     <ul>
     <li>{@link #PRINT_KEY_FORMAT_SYSTEM_VALUE PRINT_KEY_FORMAT_SYSTEM_VALUE}
     <li>{@link #PRINT_KEY_FORMAT_NONE PRINT_KEY_FORMAT_NONE}
     <li>{@link #PRINT_KEY_FORMAT_BORDER PRINT_KEY_FORMAT_BORDER}
     <li>{@link #PRINT_KEY_FORMAT_HEADER PRINT_KEY_FORMAT_HEADER}
     <li>{@link #PRINT_KEY_FORMAT_ALL PRINT_KEY_FORMAT_ALL}
     </ul>
     <p>Type: String
     @see  #getPrintKeyFormat
     @see  #setPrintKeyFormat
     **/
    public static final int PRINT_KEY_FORMAT = 1601;

    /**
     Constant indicating that the value specified on the system value QPRTKEYFMT determines whether header or border information is printed.
     @see  #PRINT_KEY_FORMAT
     **/
    public static final String PRINT_KEY_FORMAT_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating that the border and header information is not included with output from the Print key.
     @see  #PRINT_KEY_FORMAT
     **/
    public static final String PRINT_KEY_FORMAT_NONE = "*NONE";

    /**
     Constant indicating that the border information is included with output from the Print key.
     @see  #PRINT_KEY_FORMAT
     **/
    public static final String PRINT_KEY_FORMAT_BORDER = "*PRTBDR";

    /**
     Constant indicating that the header information is included with output from the Print key.
     @see  #PRINT_KEY_FORMAT
     **/
    public static final String PRINT_KEY_FORMAT_HEADER = "*PRTHDR";

    /**
     Constant indicating that the border and header information is included with output from the Print key.
     @see  #PRINT_KEY_FORMAT
     **/
    public static final String PRINT_KEY_FORMAT_ALL = "*PRTALL";

    /**
     Job attribute representing the line of text (if any) that is printed at the bottom of each page of printed output for the job.  Possible values are:
     <ul>
     <li>{@link #PRINT_TEXT_SYSTEM_VALUE PRINT_TEXT_SYSTEM_VALUE}
     <li>{@link #PRINT_TEXT_BLANK PRINT_TEXT_BLANK}
     <li>The character string that is printed at the bottom of each page.  A maximum of 30 characters can be entered.
     </ul>
     <p>Type: String
     @see  #getPrintText
     @see  #setPrintText
     **/
    public static final int PRINT_TEXT = 1602;

    /**
     Constant indicating the system value QPRTTXT is used.
     @see  #PRINT_TEXT
     **/
    public static final String PRINT_TEXT_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating that no text is printed on printed output.
     @see  #PRINT_TEXT
     **/
    public static final String PRINT_TEXT_BLANK = "*BLANK";

    /**
     Job attribute representing the printer device used for printing output from this job.  Possible values are:
     <ul>
     <li>{@link #PRINTER_DEVICE_NAME_SYSTEM_VALUE PRINTER_DEVICE_NAME_SYSTEM_VALUE}
     <li>{@link #PRINTER_DEVICE_NAME_WORK_STATION PRINTER_DEVICE_NAME_WORK_STATION}
     <li>{@link #PRINTER_DEVICE_NAME_INITIAL_USER PRINTER_DEVICE_NAME_INITIAL_USER}
     <li>The name of the printer device that is used with this job.
     </ul>
     <p>Type: String
     @see  #getPrinterDeviceName
     @see  #setPrinterDeviceName
     **/
    public static final int PRINTER_DEVICE_NAME = 1603;

    /**
     Constant indicating the value in the system value QPRTDEV is used as the printer device.
     @see  #PRINTER_DEVICE_NAME
     **/
    public static final String PRINTER_DEVICE_NAME_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating that the default printer device used with this job is the printer device assigned to the work station that is associated with the job.
     @see  #PRINTER_DEVICE_NAME
     **/
    public static final String PRINTER_DEVICE_NAME_WORK_STATION = "*WRKSTN";

    /**
     Constant indicating that the printer device name specified in the user profile under which this thread was initially running is used.
     @see  #PRINTER_DEVICE_NAME
     **/
    public static final String PRINTER_DEVICE_NAME_INITIAL_USER = "*USRPRF";

    /**
     Job attribute representing the libraries that contain product information for the initial thread of the job.
     <p>Read-only: true
     <p>Type: String
     <p>Can be loaded by JobList: false
     @see  #getNumberOfProductLibraries
     @see  #getProductLibraries
     **/
    public static final int PRODUCT_LIBRARIES = 10002;  // Cannot preload.

    /**
     Job attribute representing the return code set by the compiler for Integrated Language Environment (ILE) languages.  Refer to the appropriate ILE-conforming language manual for possible values.  This attribute is scoped to the job and represents the most recent return code set by any thread within the job.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int PRODUCT_RETURN_CODE = 1605;

    /**
     Job attribute representing the completion status of the last program that has finished running, if the job contains any RPG, COBOL, data file utility (DFU), or sort utility programs.  If not, a value of 0 is returned.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int PROGRAM_RETURN_CODE = 1606;

    /**
     Job attribute representing the routing data that is used to determine the routing entry that identifies the program to start for the routing step.
     <p>Read-only: true
     <p>Type: String
     @see  #getRoutingData
     **/
    public static final int ROUTING_DATA = 1803;

    /**
     Job attribute representing the priority at which the job is currently running, relative to other jobs on the system.  The run priority ranges from 0 (highest priority) to 99 (lowest priority).
     <p>Type: Integer
     @see  #getRunPriority
     @see  #setRunPriority
     **/
    public static final int RUN_PRIORITY = 1802;

    /**
     Job attribute representing the date on which the submitted job becomes eligible to run.  If your system or your job is configured to use the Julian date format, *MONTHSTR and *MONTHEND are calculated as if the system or job did not use the Julian date format.  Possible values that can be used on {@link #setValue setValue()} are:
     <ul>
     <li>{@link #SCHEDULE_DATE_CURRENT SCHEDULE_DATE_CURRENT}
     <li>{@link #SCHEDULE_DATE_MONTH_START SCHEDULE_DATE_MONTH_START}
     <li>{@link #SCHEDULE_DATE_MONTH_END SCHEDULE_DATE_MONTH_END}
     <li>{@link #SCHEDULE_DATE_MONDAY SCHEDULE_DATE_MONDAY}
     <li>{@link #SCHEDULE_DATE_TUESDAY SCHEDULE_DATE_TUESDAY}
     <li>{@link #SCHEDULE_DATE_WEDNESDAY SCHEDULE_DATE_WEDNESDAY}
     <li>{@link #SCHEDULE_DATE_THURSDAY SCHEDULE_DATE_THURSDAY}
     <li>{@link #SCHEDULE_DATE_FRIDAY SCHEDULE_DATE_FRIDAY}
     <li>{@link #SCHEDULE_DATE_SATURDAY SCHEDULE_DATE_SATURDAY}
     <li>{@link #SCHEDULE_DATE_SUNDAY SCHEDULE_DATE_SUNDAY}
     <li>A date String in the format CYYMMDD.
     </ul>
     <p>Type: String on setValue(); java.util.Date on getValue().
     @see  #getScheduleDate
     @see  #setScheduleDate
     **/
    public static final int SCHEDULE_DATE = 1920;

    /**
     Constant indicating the submitted job becomes eligible to run on the current date.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_CURRENT = "*CURRENT";

    /**
     Constant indicating the submitted job becomes eligible to run on the first day of the month.  If you specify this value and if today is the first day of the month and the time you specify on the SCHEDULE_TIME attribute has not passed, the job becomes eligible to run today.  Otherwise, the job becomes eligible on the first day of the next month.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_MONTH_START = "*MONTHSTR";

    /**
     Constant indicating the submitted job becomes eligible to run on the last day of the month.  If you specify this value and if today is the last day of the month and the time you specify on the SCHEDULE_TIME attribute has not passed, the job becomes eligible to run today.  Otherwise, the job becomes eligible on the last day of the next month.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_MONTH_END = "*MONTHEND";

    /**
     Constant indicating the job becomes eligible to run on Monday.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_MONDAY = "*MON";

    /**
     Constant indicating the job becomes eligible to run on Tuesday.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_TUESDAY = "*TUE";

    /**
     Constant indicating the job becomes eligible to run on Wednesday.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_WEDNESDAY = "*WED";

    /**
     Constant indicating the job becomes eligible to run on Thursday.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_THURSDAY = "*THU";

    /**
     Constant indicating the job becomes eligible to run on Friday.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_FRIDAY = "*FRI";

    /**
     Constant indicating the job becomes eligible to run on Saturday.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_SATURDAY = "*SAT";

    /**
     Constant indicating the job becomes eligible to run on Sunday.
     @see  #SCHEDULE_DATE
     **/
    public static final String SCHEDULE_DATE_SUNDAY = "*SUN";

    /**
     Job attribute representing the time on the scheduled date at which the job becomes eligible to run.  Although the time can be specified to the second, the load on the system may affect the exact time at which the job becomes eligible to run.  Possible values that can be used on {@link #setValue setValue()} are:
     <ul>
     <li>{@link #SCHEDULE_TIME_CURRENT SCHEDULE_TIME_CURRENT}
     <li>A time you want to start the job, specified in a 24-hour format String as HHMMSS.
     </ul>
     <p>Type: String on setValue(); java.util.Date on getValue().
     @see  #getScheduleDate
     @see  #setScheduleTime
     **/
    public static final int SCHEDULE_TIME = 1921;

    /**
     Constant indicating the job is submitted on the current time.
     @see  #SCHEDULE_TIME
     **/
    public static final String SCHEDULE_TIME_CURRENT = "*CURRENT";

    // This is used internally.  It is the key value used to get the schedule date, which is a combination of the schedule date and schedule time that were set.
    static final int SCHEDULE_DATE_GETTER = 403;

    /**
     Job attribute representing the type of server represented by the job.  A value of blanks indicates that the job is not part of a server.
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int SERVER_TYPE = 1911;

    /**
     Job attribute representing whether the job is to be treated like a signed-on user on the system.  Possible values are:
     <ul>
     <li>{@link #SIGNED_ON_JOB_TRUE SIGNED_ON_JOB_TRUE}
     <li>{@link #SIGNED_ON_JOB_FALSE SIGNED_ON_JOB_FALSE}
     </ul>
     <p>Read-only: true
     <p>Type: String
     @see  #getSignedOnJob
     **/
    public static final int SIGNED_ON_JOB = 701;

    /**
     Constant indicating that the job should be treated like a signed-on user.
     @see  #SIGNED_ON_JOB
     **/
    public static final String SIGNED_ON_JOB_TRUE = "0";

    /**
     Constant indicating that the job should not be treated like a signed-on user.
     @see  #SIGNED_ON_JOB
     **/
    public static final String SIGNED_ON_JOB_FALSE = "1";

    /**
     Job attribute representing the sort sequence table associated with this job.  Possible values are:
     <ul>
     <li>{@link #SORT_SEQUENCE_TABLE_SYSTEM_VALUE SORT_SEQUENCE_TABLE_SYSTEM_VALUE}
     <li>{@link #SORT_SEQUENCE_TABLE_INITIAL_USER SORT_SEQUENCE_TABLE_INITIAL_USER}
     <li>{@link #SORT_SEQUENCE_TABLE_NONE SORT_SEQUENCE_TABLE_NONE}
     <li>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT}
     <li>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT}
     <li>A sort sequence table and library.
     </ul>
     <p>Type: String
     @see  #getSortSequenceTable
     @see  #setSortSequenceTable
     **/
    public static final int SORT_SEQUENCE_TABLE = 1901;

    /**
     Constant indicating the system value QSRTSEQ is used.
     @see  #SORT_SEQUENCE_TABLE
     **/
    public static final String SORT_SEQUENCE_TABLE_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating the sort sequence table specified in the user profile under which this thread was initially running is used.
     @see  #SORT_SEQUENCE_TABLE
     **/
    public static final String SORT_SEQUENCE_TABLE_INITIAL_USER = "*USRPRF";

    /**
     Constant indicating that no sort sequence table is used.  The hexadecimal values of the characters are used to determine the sort sequence.
     @see  #SORT_SEQUENCE_TABLE
     **/
    public static final String SORT_SEQUENCE_TABLE_NONE = "*HEX";

    /**
     Constant indicating that the sort sequence table used can contain the same weight for multiple characters, and it is the shared weight sort table associated with the language specified in the LANGUAGE_ID attribute.
     @see  #SORT_SEQUENCE_TABLE
     **/
    public static final String SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT = "*LANGIDSHR";

    /**
     Constant indicating that the sort sequence table used must contain a unique weight for each character in the code page, and it is the unique weight sort table associated with the language specified in the LANGUAGE_ID parameter.
     @see  #SORT_SEQUENCE_TABLE
     **/
    public static final String SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT = "*LANGIDUNQ";

    /**
     Job attribute representing whether a job is running in a particular environment.  Possible values are:
     <ul>
     <li>{@link #SPECIAL_ENVIRONMENT_NONE SPECIAL_ENVIRONMENT_NONE}
     <li>{@link #SPECIAL_ENVIRONMENT_SYSTEM_36 SPECIAL_ENVIRONMENT_SYSTEM_36}
     <li>{@link #SPECIAL_ENVIRONMENT_NOT_ACTIVE SPECIAL_ENVIRONMENT_NOT_ACTIVE}
     </ul>
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int SPECIAL_ENVIRONMENT = 1908;

    /**
     Constant indicating that the job is not running in any special environment.
     @see  #SPECIAL_ENVIRONMENT
     **/
    public static final String SPECIAL_ENVIRONMENT_NONE = "*NONE";

    /**
     Constant indicating that the job is running in the System/36 environment.
     @see  #SPECIAL_ENVIRONMENT
     **/
    public static final String SPECIAL_ENVIRONMENT_SYSTEM_36 = "*S36";

    /**
     Constant indicating that the special environment is ignored because the job is not currently active.
     @see  #SPECIAL_ENVIRONMENT
     **/
    public static final String SPECIAL_ENVIRONMENT_NOT_ACTIVE = "";

    /**
     Job attribute representing whether spooled files can be accessed through job interfaces once a job has completed its normal activity.  Possible values are:
     <ul>
     <li>{@link #SPOOLED_FILE_ACTION_KEEP SPOOLED_FILE_ACTION_KEEP}
     <li>{@link #SPOOLED_FILE_ACTION_DETACH SPOOLED_FILE_ACTION_DETACH}
     <li>{@link #SPOOLED_FILE_ACTION_SYSTEM_VALUE SPOOLED_FILE_ACTION_SYSTEM_VALUE}
     </ul>
     <p>Type: String
     **/
    // Key:  1982
    // Type:  char(10)
    // Description:  Spooled file action
    public static final int SPOOLED_FILE_ACTION = 1982;

    /**
     Constant indicating that when the job completes its activity, as long as at least one spooled file for the job exists in the system auxiliary storage pool (ASP 1) or in a basic user ASP (ASPs 2-32), the spooled files are kept with the job and the status of the job is updated to indicate that the job has completed.  If all remaining spooled files for the job are in independent ASPs (ASPs 33-255), the spooled files will be detached from the job and the job will be removed from the system.
     @see  #SPOOLED_FILE_ACTION
     **/
    public static final String SPOOLED_FILE_ACTION_KEEP = "*KEEP";

    /**
     Constant indicating that spooled files are detached from the job when the job completes its activity.
     @see  #SPOOLED_FILE_ACTION
     **/
    public static final String SPOOLED_FILE_ACTION_DETACH = "*DETACH";

    /**
     Constant indicating the job will take the spooled file action specified by the QSPLFACN system value.
     @see  #SPOOLED_FILE_ACTION
     **/
    public static final String SPOOLED_FILE_ACTION_SYSTEM_VALUE = "*SYSVAL";

    /**
     Job attribute representing whether status messages are displayed for this job.  Possible values are:
     <ul>
     <li>{@link #STATUS_MESSAGE_HANDLING_SYSTEM_VALUE STATUS_MESSAGE_HANDLING_SYSTEM_VALUE}
     <li>{@link #STATUS_MESSAGE_HANDLING_INITIAL_USER STATUS_MESSAGE_HANDLING_INITIAL_USER}
     <li>{@link #STATUS_MESSAGE_HANDLING_NONE STATUS_MESSAGE_HANDLING_NONE}
     <li>{@link #STATUS_MESSAGE_HANDLING_NORMAL STATUS_MESSAGE_HANDLING_NORMAL}
     </ul>
     <p>Type: String
     @see  #getStatusMessageHandling
     @see  #setStatusMessageHandling
     **/
    public static final int STATUS_MESSAGE_HANDLING = 1902;

    /**
     Constant indicating the system value QSTSMSG is used.
     @see  #STATUS_MESSAGE_HANDLING
     **/
    public static final String STATUS_MESSAGE_HANDLING_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating the status message handling that is specified in the user profile under which this thread was initially running is used.
     @see  #STATUS_MESSAGE_HANDLING
     **/
    public static final String STATUS_MESSAGE_HANDLING_INITIAL_USER = "*USRPRF";

    /**
     Constant indicating that this job does not display status messages.
     @see  #STATUS_MESSAGE_HANDLING
     **/
    public static final String STATUS_MESSAGE_HANDLING_NONE = "*NONE";

    /**
     Constant indicating that this job displays status messages.
     @see  #STATUS_MESSAGE_HANDLING
     **/
    public static final String STATUS_MESSAGE_HANDLING_NORMAL = "*NORMAL";

    /**
     Job attribute representing the job name of the submitter's job.  If the job has no submitter, this value is blank.
     <p>Read-only: true
     <p>Type: String
     **/
    public static final int SUBMITTED_BY_JOB_NAME = 1904;


    /**
     Job attribute representing the user name of the submitter.  If the job has no submitter, this value is blank.
     <p>Read-only: true
     <p>Type: String
     <p>Can be loaded by JobList: false
     **/
    public static final int SUBMITTED_BY_USER = 10006;  // Cannot preload.
    /**
     Job attribute representing the job number of the submitter's job.  If the job has no submitter, this value is blank.
     <p>Read-only: true
     <p>Type: String
     <p>Can be loaded by JobList: false
     **/
    public static final int SUBMITTED_BY_JOB_NUMBER = 10005;  // Cannot preload.

    /**
     Job attribute representing the subsystem description in which an active job is running.  This value is only for jobs whose status is *ACTIVE.  For jobs with status of *OUTQ or *JOBQ, this value is blank.
     <p>Read-only: true
     <p>Type: String
     @see  #getSubsystem
     **/
    public static final int SUBSYSTEM = 1906;

    /**
     Job attribute representing the identifier of the system-related pool from which the job's main storage is allocated.  These identifiers are not the same as those specified in the subsystem description, but are the same as the system pool identifiers shown on the system status display.  This is the pool that the threads in the job start in.  Also see the CURRENT_SYSTEM_POOL_ID for more information.
     <p>Read-only: true
     <p>Type: Integer
     @see  #CURRENT_SYSTEM_POOL_ID
     @see  #getPoolIdentifier
     **/
    public static final int SYSTEM_POOL_ID = 1907;

    /**
     Job attribute representing the system portion of the library list of the initial thread of the job.
     <p>Read-only: true
     <p>Type: String
     <p>Can be loaded by JobList: false
     @see  #getNumberOfLibrariesInSYSLIBL
     @see  #getSystemLibraryList
     **/
    public static final int SYSTEM_LIBRARY_LIST = 10003;  // Cannot preload.

    /**
     Job attribute representing the amount of auxiliary storage (in kilobytes) that is currently allocated to this job.  This value will reach a maximum of 2,147,483,647 kilobytes.  If the actual temporary storage used is larger than that value, this attribute will return 2,147,483,647 kilobytes.  It is recomended that the TEMP_STORAGE_USED_LARGE attribute be used to get over the limit.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int TEMP_STORAGE_USED = 2004;

    /**
     Job attribute representing the amount of auxiliary storage (in megabytes) that is currently allocated to this job.
     <p>Read-only: true
     <p>Type: Long
     **/
    public static final int TEMP_STORAGE_USED_LARGE = 2009;

    /**
     Job attribute representing the count of the current number of active threads in the process at the time of the materialization.  An active thread may be either actively running, suspended, or waiting for a resource.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int THREAD_COUNT = 2008;

    /**
     Job attribute representing the value used to separate hours, minutes, and seconds when presenting a time.  Possible values are:
     <ul>
     <li>{@link #TIME_SEPARATOR_SYSTEM_VALUE TIME_SEPARATOR_SYSTEM_VALUE}
     <li>{@link #TIME_SEPARATOR_COLON TIME_SEPARATOR_COLON}
     <li>{@link #TIME_SEPARATOR_PERIOD TIME_SEPARATOR_PERIOD}
     <li>{@link #TIME_SEPARATOR_BLANK TIME_SEPARATOR_BLANK}
     <li>{@link #TIME_SEPARATOR_COMMA TIME_SEPARATOR_COMMA}
     </ul>
     <p>Type: String
     @see  #getTimeSeparator
     @see  #setTimeSeparator
     **/
    public static final int TIME_SEPARATOR = 2001;

    /**
     Constant indicating the time separator specified in the system value QTIMSEP is used.
     @see  #TIME_SEPARATOR
     **/
    public static final String TIME_SEPARATOR_SYSTEM_VALUE = "S";

    /**
     Constant indicating a colon (:) is used for the time separator.
     @see  #TIME_SEPARATOR
     **/
    public static final String TIME_SEPARATOR_COLON = ":";

    /**
     Constant indicating a period (.) is used for the time separator.
     @see  #TIME_SEPARATOR
     **/
    public static final String TIME_SEPARATOR_PERIOD = ".";

    /**
     Constant indicating a blank is used for the time separator.
     @see  #TIME_SEPARATOR
     **/
    public static final String TIME_SEPARATOR_BLANK = " ";

    /**
     Constant indicating a comma (,) is used for the time separator.
     @see  #TIME_SEPARATOR
     **/
    public static final String TIME_SEPARATOR_COMMA = ",";

    /**
     Job attribute representing the maximum amount of processor time (in milliseconds) given to each thread in this job before other threads in this job and in other jobs are given the opportunity to run.  The time slice establishes the amount of time needed by a thread in this job to accomplish a meaningful amount of processing.  At the end of the time slice, the thread might be put in an inactive state so that other threads can become active in the storage pool.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not apply to subsequent routing steps.  Values retrieved range from 8 through 9,999,999 milliseconds (that is, 9999.999 seconds).  Although you can specify a value of less than 8, the system takes a minimum of 8 milliseconds to run a process.
     <p>Type: Integer
     @see  #getTimeSlice
     @see  #setTimeSlice
     **/
    public static final int TIME_SLICE = 2002;

    /**
     Job attribute representing whether a thread in an interactive job moves to another main storage pool at the end of its time slice.  Possible values are:
     <ul>
     <li>{@link #TIME_SLICE_END_POOL_SYSTEM_VALUE TIME_SLICE_END_POOL_SYSTEM_VALUE}
     <li>{@link #TIME_SLICE_END_POOL_NONE TIME_SLICE_END_POOL_NONE}
     <li>{@link #TIME_SLICE_END_POOL_BASE TIME_SLICE_END_POOL_BASE}
     </ul>
     <p>Type: String
     @see  #getTimeSliceEndPool
     @see  #setTimeSliceEndPool
     **/
    public static final int TIME_SLICE_END_POOL = 2003;

    /**
     Constant indicating the value in the system value QTSEPPOOL is used.
     @see  #TIME_SLICE_END_POOL
     **/
    public static final String TIME_SLICE_END_POOL_SYSTEM_VALUE = "*SYSVAL";

    /**
     Constant indicating that a thread in the job does not move to another main storage pool when it reaches the end of its time slice.
     @see  #TIME_SLICE_END_POOL
     **/
    public static final String TIME_SLICE_END_POOL_NONE = "*NONE";

    /**
     Constant indicating that a thread in the job moves to the base pool when it reaches the end of its time slice.
     @see  #TIME_SLICE_END_POOL
     **/
    public static final String TIME_SLICE_END_POOL_BASE = "*BASE";

    /**
     Job attribute representing the total amount of response time for the initial thread, in milliseconds.  This value does not include the time used by the machine, by the attached input/output (I/O) hardware, and by the transmission lines for sending and receiving data.  This value is zero for jobs that have no interactions.  A value of -1 is returned if this field is not large enough to hold the actual result.
     <p>Read-only: true
     <p>Type: Integer
     @see  #getTotalResponseTime
     **/
    public static final int TOTAL_RESPONSE_TIME = 1801;

    /**
     Job attribute representing the unit of work ID used to track jobs across multiple systems.  If a job is not associated with a source or target system using advanced program-to-program communications (APPC), this information is not used.  Every job on the system is assigned a unit of work ID.  The unit-of-work identifier is made up of:
     <ol>
     <li>{@link #LOCATION_NAME LOCATION_NAME}
     <li>{@link #NETWORK_ID NETWORK_ID}
     <li>{@link #INSTANCE INSTANCE}
     <li>{@link #SEQUENCE_NUMBER SEQUENCE_NUMBER}
     </ol>
     <p>Read-only: true
     <p>Type: String
     @see  #getWorkIDUnit
     **/
    public static final int UNIT_OF_WORK_ID = 2101;  // This is the real key.

    /**
     Job attribute representing the location name portion of the unit of work ID.  This portion of the unit-of-work identifier is the name of the source system that originated the APPC job.
     <p>Read-only: true
     <p>Type: String
     @see  #UNIT_OF_WORK_ID
     @see  #getWorkIDUnit
     **/
    public static final int LOCATION_NAME = 21012;  // Unit of work ID.

    /**
     Job attribute representing the network ID portion of the unit of work ID.  This portion of the unit-of-work identifier is the network name associated with the unit of work.
     <p>Read-only: true
     <p>Type: String
     @see  #UNIT_OF_WORK_ID
     @see  #getWorkIDUnit
     **/
    public static final int NETWORK_ID = 21013;  // Unit of work ID.

    /**
     Job attribute representing the instance portion of the unit of work ID.  This portion of the unit-of-work identifier is the value that further identifies the source of the job.  This is shown as hexadecimal data.
     <p>Read-only: true
     <p>Type: String
     @see  #UNIT_OF_WORK_ID
     @see  #getWorkIDUnit
     **/
    public static final int INSTANCE = 21011;  // Unit of work ID.

    /**
     Job attribute representing the sequence number portion of the unit of work ID.  This portion of the unit-of-work identifier is a value that identifies a checkpoint within the application program.
     <p>Read-only: true
     <p>Type: String
     @see  #UNIT_OF_WORK_ID
     @see  #getWorkIDUnit
     **/
    public static final int SEQUENCE_NUMBER = 21014;  // Unit of work ID.

    /**
     Job attribute representing the user portion of the library list for the initial thread of the job.
     <p>Read-only: true
     <p>Type: String
     <p>Can be loaded by JobList: false
     @see  #getNumberOfLibrariesInUSRLIBL
     @see  #getUserLibraryList
     **/
    public static final int USER_LIBRARY_LIST = 10004;  // Cannot preload.

    /**
     Job attribute representing the user name of the job, which is the same as the name of the user profile under which the job was started.  It can come from several different sources, depending on the type of job.  This may be different than the user profile under which the job is currently running.  See the CURRENT_USER attribute for more information.  Possible values are:
     <ul>
     <li>A specific user profile name.
     <li>{@link #USER_NAME_BLANK USER_NAME_BLANK}
     </ul>
     <p>Type: String
     @see  #CURRENT_USER
     @see  #getUser
     @see  #setUser
     **/
    public static final int USER_NAME = 11006;  // Always gets loaded.

    /**
     Constant indicating a blank user name.  This must be used when JOB_NAME_INTERNAL or JOB_NAME_CURRENT is specified for the JOB_NAME.
     @see  #USER_NAME
     **/
    public static final String USER_NAME_BLANK = "";

    /**
     Job attribute representing the user-defined return code set by ILE high-level language constructs.  An example is the program return code in the C language.  This field is scoped to the job and represents the most recent return code set by any thread within the job.
     <p>Read-only: true
     <p>Type: Integer
     **/
    public static final int USER_RETURN_CODE = 2102;

    // Holds the lengths for all of the setter keys.
    static final IntegerHashtable setterKeys_ = new IntegerHashtable();
    static
    {
        setterKeys_.put(BREAK_MESSAGE_HANDLING, 10);
        setterKeys_.put(CCSID, 4); // Binary
        setterKeys_.put(COUNTRY_ID, 8);
        setterKeys_.put(CHARACTER_ID_CONTROL, 10);
        setterKeys_.put(DATE_FORMAT, 4);
        setterKeys_.put(DATE_SEPARATOR, 1);
        setterKeys_.put(KEEP_DDM_CONNECTIONS_ACTIVE, 5);
        setterKeys_.put(DEFAULT_WAIT_TIME, 4); // Binary
        setterKeys_.put(DEVICE_RECOVERY_ACTION, 13);
        setterKeys_.put(DECIMAL_FORMAT, 8);
        setterKeys_.put(INQUIRY_MESSAGE_REPLY, 10);
        setterKeys_.put(ACCOUNTING_CODE, 15);
        setterKeys_.put(JOB_DATE, 7);
        setterKeys_.put(JOB_LOG_OUTPUT, 10);
        setterKeys_.put(JOB_QUEUE, 20);
        setterKeys_.put(JOB_QUEUE_PRIORITY, 2);
        setterKeys_.put(JOB_SWITCHES, 8);
        setterKeys_.put(MESSAGE_QUEUE_ACTION, 10);
        setterKeys_.put(LANGUAGE_ID, 8);
        setterKeys_.put(LOGGING_LEVEL, 1);
        setterKeys_.put(LOG_CL_PROGRAMS, 10);
        setterKeys_.put(LOGGING_SEVERITY, 4); // Binary
        setterKeys_.put(LOGGING_TEXT, 7);
        setterKeys_.put(OUTPUT_QUEUE, 20);
        setterKeys_.put(OUTPUT_QUEUE_PRIORITY, 2);
        setterKeys_.put(PRINT_KEY_FORMAT, 10);
        setterKeys_.put(PRINT_TEXT, 30);
        setterKeys_.put(PRINTER_DEVICE_NAME, 10);
        setterKeys_.put(ELIGIBLE_FOR_PURGE, 4);
        setterKeys_.put(RUN_PRIORITY, 4); // Binary
        setterKeys_.put(SORT_SEQUENCE_TABLE, 20);
        setterKeys_.put(STATUS_MESSAGE_HANDLING, 10);
        setterKeys_.put(SCHEDULE_DATE, 10);
        setterKeys_.put(SCHEDULE_TIME, 8);
        setterKeys_.put(SPOOLED_FILE_ACTION, 10);
        setterKeys_.put(TIME_SEPARATOR, 1);
        setterKeys_.put(TIME_SLICE, 4); // Binary
        setterKeys_.put(TIME_SLICE_END_POOL, 10);
    }

    // Keep a list of which of the changeable attributes is binary.
    private static boolean isTypeBinary(int key)
    {
        switch (key)
        {
            case CCSID:
            case DEFAULT_WAIT_TIME:
            case LOGGING_SEVERITY:
            case RUN_PRIORITY:
            case TIME_SLICE:
                return true;
        }
        return false;
    }

    /**
     Constructs a Job object.  The system and basic job information must be set before connecting to the system.
     @see  #setName
     @see  #setNumber
     @see  #setSystem
     @see  #setUser
     **/
    public Job()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing Job object.");
    }

    /**
     Constructs a Job object.  The job name, user name, and job number of the job that this program is running in will be used.  Typically, that will be the job information for the remote command host server job associated with the specified system object.
     @param  system  The system object representing the system on which the job exists.
     @see  #setName
     @see  #setNumber
     @see  #setUser
     **/
    public Job(AS400 system)
    {
        this(system, JOB_NAME_CURRENT, USER_NAME_BLANK, JOB_NUMBER_BLANK);
    }

    /**
     Constructs a Job object.
     @param  system  The system object representing the system on which the job exists.
     @param  jobName  The job name.  Specify JOB_NAME_CURRENT to indicate the job that this program is running in.
     @param  userName  The user name.  This must be USER_NAME_BLANK if the job name is JOB_NAME_CURRENT.
     @param  jobNumber  The job number.  This must be JOB_NUMBER_BLANK if job name is JOB_NAME_CURRENT.
     **/
    public Job(AS400 system, String jobName, String userName, String jobNumber)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing Job object, system: " + system + ", job name: " + jobName + ", user name: " + userName + ", job number: " + jobNumber);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (jobName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobName' is null.");
            throw new NullPointerException("jobName");
        }
        if (jobName.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'jobName' is not valid: '" + jobName + "'");
            throw new ExtendedIllegalArgumentException("jobName (" + jobName + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (userName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'userName' is null.");
            throw new NullPointerException("userName");
        }
        if (userName.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'userName' is not valid: '" + userName + "'");
            throw new ExtendedIllegalArgumentException("userName (" + userName + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (jobNumber == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobNumber' is null.");
            throw new NullPointerException("jobNumber");
        }
        if (jobNumber.length() > 6)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'jobNumber' is not valid: '" + jobNumber + "'");
            throw new ExtendedIllegalArgumentException("jobNumber (" + jobNumber + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (jobName.equals(JOB_NAME_CURRENT))
        {
            if (userName.trim().length() != 0)
            {
                Trace.log(Trace.ERROR, "Value of parameter 'userName' is not valid: " + userName);
                throw new ExtendedIllegalArgumentException("userName (" + userName + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
            if (jobNumber.trim().length() != 0)
            {
                Trace.log(Trace.ERROR, "Value of parameter 'jobNumber' is not valid: " + jobNumber);
                throw new ExtendedIllegalArgumentException("jobNumber (" + jobNumber + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
        }

        system_ = system;
        name_ = jobName;
        setValueInternal(JOB_NAME, jobName);
        user_ = userName;
        setValueInternal(USER_NAME, userName);
        number_ = jobNumber;
        setValueInternal(JOB_NUMBER, jobNumber);
        internalJobID_ = "";
        realInternalJobID_ = null;
        setValueInternal(INTERNAL_JOB_ID, "");
    }

    /**
     Constructs a Job object.  This sets the job name to JOB_NAME_INTERNAL, the user name to USER_NAME_BLANK, and the job number to JOB_NUMBER_BLANK.
     @param  system  The system object representing the system on which the job exists.
     @param  internalJobID  The internal job identifier.
     @deprecated  The internal job ID should be treated as a byte array of 16 bytes.
     **/
    public Job(AS400 system, String internalJobID)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing Job object, system: " + system + ", job identifier: '" + internalJobID + "'");
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (internalJobID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'internalJobID' is null.");
            throw new NullPointerException("internalJobID");
        }
        if (internalJobID.length() != 16)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'internalJobID' is not valid: '" + internalJobID + "'");
            throw new ExtendedIllegalArgumentException("internalJobID (" + internalJobID + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        system_ = system;
        internalJobID_ = internalJobID;
        realInternalJobID_ = new byte[16];
        for (int i = 0; i < 16; ++i)
        {
            realInternalJobID_[i] = (byte)internalJobID.charAt(i);
        }
        setValueInternal(INTERNAL_JOB_ID, internalJobID);
        setValueInternal(INTERNAL_JOB_IDENTIFIER, realInternalJobID_);
        name_ = JOB_NAME_INTERNAL;
        setValueInternal(JOB_NAME, null);
        user_ = USER_NAME_BLANK;
        setValueInternal(USER_NAME, null);
        number_ = JOB_NUMBER_BLANK;
        setValueInternal(JOB_NUMBER, null);
    }

    /**
     Constructs a Job object.  This sets the job name to JOB_NAME_INTERNAL, the user name to USER_NAME_BLANK, and the job number to JOB_NUMBER_BLANK.
     @param  system  The system.
     @param  internalJobID  The 16-byte internal job identifier.
     **/
    public Job(AS400 system, byte[] internalJobID)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing Job object, system: " + system + ", job identifier:", internalJobID);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (internalJobID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'internalJobID' is null.");
            throw new NullPointerException("internalJobID");
        }
        if (internalJobID.length != 16)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'internalJobID' is not valid: " + internalJobID.length);
            throw new ExtendedIllegalArgumentException("internalJobID.length {" + internalJobID.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        system_ = system;
        realInternalJobID_ = internalJobID;
        char[] oldID = new char[16];
        for (int i = 0; i < 16; ++i)
        {
            oldID[i] = (char)(internalJobID[i] & 0x00FF);
        }
        internalJobID_ = new String(oldID);
        setValueInternal(INTERNAL_JOB_ID, internalJobID);
        setValueInternal(INTERNAL_JOB_IDENTIFIER, realInternalJobID_);
        name_ = JOB_NAME_INTERNAL;
        setValueInternal(JOB_NAME, null);
        user_ = USER_NAME_BLANK;
        setValueInternal(USER_NAME, null);
        number_ = JOB_NUMBER_BLANK;
        setValueInternal(JOB_NUMBER, null);
    }

    // Constructs a Job object.  Package scope constructor.
    Job(AS400 system, String name, String user, String number, String status, String type, String subtype)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing Job object, system: " + system + ", job name: " + name + ", user name: " + user + ", job number: " + number + ", status: " + status + ", type: " + type + ", subtype: " + subtype);
        system_ = system;
        name_ = name;
        setValueInternal(JOB_NAME, name);
        user_ = user;
        setValueInternal(USER_NAME, user);
        number_ = number;
        setValueInternal(JOB_NUMBER, number);
        status_ = status;
        setValueInternal(JOB_STATUS, status);
        type_ = type;
        setValueInternal(JOB_TYPE, type);
        subtype_ = subtype;
        setValueInternal(JOB_SUBTYPE, subtype);
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's {@link java.beans.PropertyChangeListener#propertyChange propertyChange()} method will be called each time the value of any bound property is changed.
     @param  listener  The listener.
     @see  #removePropertyChangeListener
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (propertyChangeListeners_ == null)
            {
                propertyChangeListeners_ = new PropertyChangeSupport(this);
            }
            propertyChangeListeners_.addPropertyChangeListener(listener);
        }
    }

    /**
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's {@link java.beans.VetoableChangeListener#vetoableChange vetoableChange()} method will be called each time the value of any constrained property is changed.
     @param  listener  The listener.
     @see  #removeVetoableChangeListener
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        synchronized (this)
        {
            // If first add.
            if (vetoableChangeListeners_ == null)
            {
                vetoableChangeListeners_ = new VetoableChangeSupport(this);
            }
            vetoableChangeListeners_.addVetoableChangeListener(listener);
        }
    }

    // Convenience method to check state before a connect and set the connected flag.
    private void connect()
    {
        if (!isConnected_)
        {
            // Already validated if we are connected.
            if (system_ == null)
            {
                Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
                throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (name_ == null)
            {
                Trace.log(Trace.ERROR, "Cannot connect to server before setting name.");
                throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (user_ == null)
            {
                Trace.log(Trace.ERROR, "Cannot connect to server before setting user.");
                throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (number_ == null)
            {
                Trace.log(Trace.ERROR, "Cannot connect to server before setting number.");
                throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            isConnected_ = true;
        }
    }

    // Convenience method to create the qualified job name.
    private byte[] createQualifiedJobName() throws IOException
    {
        if (realInternalJobID_ != null)
        {
            // Return EBCDIC value "*INT" blank padded to 26 bytes.
            return new byte[] { 0x5C, (byte)0xC9, (byte)0xD5, (byte)0xE3, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        }
        byte[] qualifiedJobName = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        Converter conv = new Converter(system_.getCcsid(), system_);
        conv.stringToByteArray(name_, qualifiedJobName);
        conv.stringToByteArray(user_, qualifiedJobName, 10);
        conv.stringToByteArray(number_, qualifiedJobName, 20);
        return qualifiedJobName;
    }

    /**
     Commits all uncommitted attribute changes.  Calling this method will set all uncommitted attribute changes to the job on the system.
     <br>Note: To commit the changes, the Toolbox by default calls system API (QWTCHGJB)
     <i>off-thread</i>, that is, via the Remote Command Host Server.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see #commitChanges(boolean)
     **/
    public void commitChanges() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
      commitChanges(false);
    }

    /**
     Commits all uncommitted attribute changes.  Calling this method will set all uncommitted attribute changes to the job on the system.
     <br>When running on an IBM i system: If callOnThread is true,
     then the system API (QWTCHGJB) will be called on-thread, that is, in the same thread
     as the JVM.  If callOnThread is false,
     then the system API will be called off-thread, that is, in the thread
     of the Remote Command Host Server job.
     <br>Caution: The <tt>QWTCHGJB</tt> API is specified as "conditionally
     threadsafe".  Please refer to the IBM i Programmer's Guide for details on
     the threadsafety of specific attribute changes.  Note that this method
     specifies format name JOBC0100 when calling QWTCHGJB.
     <br>Note: This method behaves identically to {@link #commitChanges commitChanges()} if the Java application is running remotely, that is, is not running "natively" on an IBM i system.  When running remotely, the Toolbox submits all program calls through the Remote Command Host Server.
     @param callOnThread Whether to call the system API on-thread or off-thread.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void commitChanges(boolean callOnThread) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Changing job.");
        connect();

        if (cachedChanges_ == null || cachedChanges_.size_ == 0) return;

        int numChanges = cachedChanges_.size_;
        int totalLength = 0;
        int[][] keyTable = cachedChanges_.keys_;

        for (int i = 0; i < keyTable.length; ++i)
        {
            int[] keys = keyTable[i];
            if (keys != null)
            {
                for (int j = 0; j < keys.length; ++j)
                {
                    int dataLength = setterKeys_.get(keys[j]);
                    int pad = (4 - (dataLength % 4)) % 4;
                    totalLength += 16 + dataLength + pad;
                }
            }
        }

        byte[] jobChangeInformation = new byte[4 + totalLength];

        BinaryConverter.intToByteArray(numChanges, jobChangeInformation, 0);
        int ccsid = system_.getCcsid();
        Converter conv = new Converter(ccsid, system_);

        int offset = 4;
        for (int i = 0; i < keyTable.length; ++i)
        {
            int[] keys = keyTable[i];
            if (keys != null)
            {
                for (int j = 0; j < keys.length; ++j)
                {
                    int dataLength = setterKeys_.get(keys[j]);
                    int pad = (4 - (dataLength % 4)) % 4;
                    BinaryConverter.intToByteArray(16 + dataLength + pad, jobChangeInformation, offset);
                    offset += 4;
                    BinaryConverter.intToByteArray(keys[j], jobChangeInformation, offset);
                    offset += 4;

                    boolean isBin = isTypeBinary(keys[j]);
                    jobChangeInformation[offset++] = (isBin) ? (byte)0xC2 : (byte)0xC3;
                    jobChangeInformation[offset++] = 0x40;
                    jobChangeInformation[offset++] = 0x40;
                    jobChangeInformation[offset++] = 0x40;

                    BinaryConverter.intToByteArray(dataLength, jobChangeInformation, offset);
                    offset += 4;
                    Object data = cachedChanges_.get(keys[j]);
                    if (isBin)
                    {
                        BinaryConverter.intToByteArray(((Integer)data).intValue(), jobChangeInformation, offset);
                    }
                    else
                    {
                        try
                        {
                            String stringValue = (String)data;
                            int endPosition = offset + dataLength + pad;
                            for (int pos = offset; pos < endPosition; ++pos)
                            {
                                jobChangeInformation[pos] = 0x40;
                            }
                            conv.stringToByteArray(stringValue, jobChangeInformation, offset, dataLength);
                        }
                        catch (ClassCastException cce)
                        {
                            // Used for system timestamp values like SCHEDULE_DATE.
                            System.arraycopy((byte[])data, 0, jobChangeInformation, offset, 8);
                        }
                    }
                    offset += dataLength + pad;
                }
            }
        }

        ProgramParameter[] parmList = new ProgramParameter[]
        {
            new ProgramParameter(createQualifiedJobName()),
            new ProgramParameter(realInternalJobID_ == null ? BLANKS16_ : realInternalJobID_),
            // Format name, input, char(8), EBCDIC 'JOBC0100'.
            new ProgramParameter(new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC3, (byte)0xF0, (byte)0xF1, (byte)0xF0, (byte)0xF0 } ),
            new ProgramParameter(jobChangeInformation),
            new ProgramParameter(new byte[8])  // Error code.
        };

        // Note: QWTCHGJB is specified to be "conditionally threadsafe".
        // If we call QWTCHGJB on-thread when changing certain attributes,
        // the API call will fail and an AS400Exception will be returned.
        // Therefore, we will disregard the setting of system property
        // "ProgramCall.threadSafe" when calling this particular API.
        ProgramCall program;
        Boolean threadMode;
        if (callOnThread) threadMode = ON_THREAD;
        else threadMode = OFF_THREAD;
        program = getProgramCall("/QSYS.LIB/QWTCHGJB.PGM", parmList, threadMode); // conditionally threadsafe

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting job information for job: " + toString());
        if (!program.run())
        {
            AS400Message[] msgList = program.getMessageList();
            throw new AS400Exception(msgList);
        }
        cachedChanges_ = null;
    }

    /**
     Ends this job.  To end the job controlled, specify -1 for the delay.  To end the job immediately, specify 0 for the delay.  Specify any other amount of delay time (in seconds) allowed for the job to cleanup.
     @param  delay  The delay time in seconds.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #hold
     @see  #release
     **/
    public void end(int delay) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Ending job.");
        if (delay < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'delay' is not valid:", delay);
            throw new ExtendedIllegalArgumentException("delay (" + delay + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        StringBuffer buf = new StringBuffer();
        buf.append("QSYS/ENDJOB JOB(");  // conditionally threadsafe
        buf.append(number_);
        buf.append('/');
        buf.append(user_);
        buf.append('/');
        buf.append(name_);
        buf.append(") OPTION(");
        if (delay == 0)
        {
            buf.append("*IMMED)");
        }
        else
        {
            buf.append("*CNTRLD)");
            if (delay > 0)
            {
                buf.append(" DELAY(");
                buf.append(delay);
                buf.append(")");
            }
        }
        String toRun = buf.toString();
        // If the user wants to end the remote command server job that is servicing our connection, they are welcome to "shoot themselves in the foot".
        CommandCall cmd = getCommandCall(toRun);
        if (!cmd.run())
        {
            throw new AS400Exception(cmd.getMessageList());
        }
    }

    private final CommandCall getCommandCall(String cmd)
    {
      if (cmdCall_ == null) {
        synchronized (remoteCommandLock_) {
          if (cmdCall_ == null) {
            cmdCall_ = new CommandCall(system_);
          }
        }
      }
      try { cmdCall_.setCommand(cmd); } catch (PropertyVetoException e) {}
      return cmdCall_;
    }

    private final ProgramCall getProgramCall(String pgm, ProgramParameter[] parms)
    {
      return getProgramCall(pgm, parms, null);
    }

    private final ProgramCall getProgramCall(String pgm, ProgramParameter[] parms, Boolean callOnThread)
    {
      if (callOnThread == ON_THREAD)
      {
        if (pgmCall_onThread_ == null) {
          synchronized (remoteCommandLock_) {
            if (pgmCall_onThread_ == null) {
              pgmCall_onThread_ = new ProgramCall(system_);
              pgmCall_onThread_.setThreadSafe(true);  // force call to be on-thread
            }
          }
        }
        try { pgmCall_onThread_.setProgram(pgm, parms); } catch (PropertyVetoException e) {}
        return pgmCall_onThread_;
      }
      else if (callOnThread == OFF_THREAD)
      {
        if (pgmCall_offThread_ == null) {
          synchronized (remoteCommandLock_) {
            if (pgmCall_offThread_ == null) {
              pgmCall_offThread_ = new ProgramCall(system_);
              pgmCall_offThread_.setThreadSafe(false);  // force call to be off-thread
            }
          }
        }
        try { pgmCall_offThread_.setProgram(pgm, parms); } catch (PropertyVetoException e) {}
        return pgmCall_offThread_;
      }
      else  // don't specify threadsafety
      {
        if (pgmCall_ == null) {
          synchronized (remoteCommandLock_) {
            if (pgmCall_ == null) {
              pgmCall_ = new ProgramCall(system_);
            }
          }
        }
        try { pgmCall_.setProgram(pgm, parms); } catch (PropertyVetoException e) {}
        return pgmCall_;
      }
    }


    // Helper method.  Used to format some of the attributes that are date Strings into actual Date objects.
    private Date getAsDate(int key) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String str = (String)getValue(key);

        Calendar dateTime = Calendar.getInstance();
        dateTime.clear();

        Date date = null;
        switch (str.trim().length())
        {
            case 7:  // CYYMMDD format.
                dateTime.set(Integer.parseInt(str.substring(0, 3)) + 1900, Integer.parseInt(str.substring(3, 5)) - 1, Integer.parseInt(str.substring(5, 7)));
                date = dateTime.getTime();
                break;
            case 13: // CYYMMDDHHMMSS format.
                dateTime.set(Integer.parseInt(str.substring(0, 3)) + 1900, Integer.parseInt(str.substring(3, 5)) - 1, Integer.parseInt(str.substring(5, 7)), Integer.parseInt(str.substring(7, 9)), Integer.parseInt(str.substring(9, 11)), Integer.parseInt(str.substring(11, 13)));
                date = dateTime.getTime();
                break;
        }
        return date;
    }

    // Helper method.  Used to retrieve an Integer value out of our internal table.
    private int getAsInt(int key) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return Integer.parseInt(getValue(key).toString().trim());
    }

    // Helper method.  Used to convert a system timestamp value into a Date object.
    private Date getAsSystemDate(int key) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // This is in the system timestamp format which requires an extra API call.
        return new DateTimeConverter(system_).convert((byte[])getValue(key), "*DTS");
    }

    /**
     Returns the number of auxiliary I/O requests performed by the job across all routing steps.  This includes both database and nondatabase paging.  If the number of auxiliary I/O requests is greater than or equal to 2,147,483,647, a value of -1 is returned.  Use the AUXILIARY_IO_REQUESTS_LARGE attribute to retrieve values that are greater than or equal to 2,147,483,647.
     @return  The number of auxiliary I/O requests performed by the job across all routing steps.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #AUXILIARY_IO_REQUESTS
     **/
    public int getAuxiliaryIORequests() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(AUXILIARY_IO_REQUESTS);
    }

    /**
     Returns a value which represents how this job handles break messages.
     @return  How this job handles break messages.  Possible values are:
     <ul>
     <li>{@link #BREAK_MESSAGE_HANDLING_NORMAL BREAK_MESSAGE_HANDLING_NORMAL} - The message queue status determines break message handling.
     <li>{@link #BREAK_MESSAGE_HANDLING_HOLD BREAK_MESSAGE_HANDLING_HOLD} - The message queue holds break messages until a user or program requests them.  The work station user uses the Display Message (DPSMSG) command to display the messages; a program must issue a Receive Message (RCVMSG) command to receive a message and handle it.
     <li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY BREAK_MESSAGE_HANDLING_NOTIFY} - The system notifies the job's message queue when a message arrives.  For interactive jobs, the audible alarm sounds if there is one, and the message-waiting light comes on.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #BREAK_MESSAGE_HANDLING
     **/
    public String getBreakMessageHandling() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(BREAK_MESSAGE_HANDLING)).trim();
    }

    /**
     Indicates if the attribute value changes are cached.
     @return  true if attribute value changes are cached, false if attribute value changes are committed immediatly.  The default is true.
     @see  #commitChanges
     @see  #getValue
     @see  #setCacheChanges
     @see  #setValue
     **/
    public boolean getCacheChanges()
    {
        return cacheChanges_;
    }

    /**
     Returns the call stack for the specified thread in this job.  This method does not cache any information and always retrieves its data from the system every time it is called.
     @param  threadID  The thread identifier, or {@link #INITIAL_THREAD Job.INITIAL_THREAD} for the initial thread of this job.
     @return  The array of call stack entries in this job's call stack.  The element at index 0 is the oldest entry in the stack, and the last element in the array is the newest, or highest, entry in the stack.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public CallStackEntry[] getCallStack(long threadID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (threadID < 0 && threadID != INITIAL_THREAD)
        {
            throw new ExtendedIllegalArgumentException("threadID", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (!isConnected_)
        {
            if (system_ == null)
            {
                throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (name_ == null)
            {
                throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (user_ == null)
            {
                throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (number_ == null)
            {
                throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
        }

        ProgramParameter[] parms = new ProgramParameter[6];
        int ccsid = system_.getCcsid();
        CharConverter conv = new CharConverter(ccsid);
        int len = 2000;
        parms[0] = new ProgramParameter(len);
        parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));
        parms[2] = new ProgramParameter(conv.stringToByteArray("CSTK0100"));
        byte[] threadJobId = new byte[56];
        AS400Text text10 = new AS400Text(10, ccsid);
        AS400Text text6 = new AS400Text(6, ccsid);
        byte[] internal = getInternalJobIdentifier();
        if (!name_.equals(JOB_NAME_INTERNAL))
        {
            internal = new byte[16];
            for (int i = 0; i < 16; ++i) internal[i] = 0x40;  // Blanks.
        }
        text10.toBytes(name_, threadJobId, 0);
        text10.toBytes(user_, threadJobId, 10);
        text6.toBytes(number_, threadJobId, 20);
        System.arraycopy(internal, 0, threadJobId, 26, 16);
        int threadType = (threadID == INITIAL_THREAD ? 2 : 0); // 0 = Specified, 1 = Current, 2 = Initial.
        long specifiedThread = (threadID == INITIAL_THREAD ? 0 : threadID);
        BinaryConverter.intToByteArray(threadType, threadJobId, 44);
        BinaryConverter.longToByteArray(specifiedThread, threadJobId, 48);
        parms[3] = new ProgramParameter(threadJobId);
        parms[4] = new ProgramParameter(conv.stringToByteArray("JIDF0100"));
        parms[5] = new ProgramParameter(new byte[4]);

        ProgramCall pc = getProgramCall("/QSYS.LIB/QWVRCSTK.PGM", parms); // threadsafe
        // Note: Even though this API is threadsafe, we won't suggest that it be called on-thread, since all other API's and CL's called by this class are either conditionally threadsafe or non-threadsafe, and we should stay consistent.
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }
        byte[] data = parms[0].getOutputData();
        int bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
        int bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
        while (bytesAvailable > bytesReturned)
        {
            try
            {
                len = bytesAvailable * 2;
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling QWVRCSTK again with receiver size of " + len);
                parms[0].setOutputDataLength(len);
                parms[1].setInputData(BinaryConverter.intToByteArray(len));
            }
            catch (PropertyVetoException pve)
            {
            }
            if (!pc.run())
            {
                throw new AS400Exception(pc.getMessageList());
            }
            data = parms[0].getOutputData();
            bytesReturned = BinaryConverter.byteArrayToInt(data, 0);
            bytesAvailable = BinaryConverter.byteArrayToInt(data, 4);
        }
        int numEntriesAll = BinaryConverter.byteArrayToInt(data, 8);
        int offset = BinaryConverter.byteArrayToInt(data, 12);
        int numEntriesReturned = BinaryConverter.byteArrayToInt(data, 16);
        if (numEntriesReturned != numEntriesAll)
        {
            if (Trace.traceOn_) Trace.log(Trace.WARNING, "Not all call stack entries were returned: total: " + numEntriesAll + ", returned: " + numEntriesReturned);
        }
        threadID = BinaryConverter.byteArrayToLong(data, 20);
        if (data[28] != 0x40)
        {
            if (Trace.traceOn_) Trace.log(Trace.WARNING, "Call stack entry information incomplete due to status: " + Integer.toHexString(0x00FF & data[28]));
        }
        CallStackEntry[] entries = new CallStackEntry[numEntriesReturned];
        final int vrm530 = AS400.generateVRM(5,3,0);
        boolean isV5R3 = system_.getVRM() >= vrm530;
        for (int i = 0; i < numEntriesReturned; ++i)
        {
            int entryLength = BinaryConverter.byteArrayToInt(data, offset);
            int stmtIDDisp = BinaryConverter.byteArrayToInt(data, offset + 4);
            int numStmtID = BinaryConverter.byteArrayToInt(data, offset + 8);
            int procNameDisp = BinaryConverter.byteArrayToInt(data, offset + 12);
            int procNameLen = BinaryConverter.byteArrayToInt(data, offset + 16);
            int reqLevel = BinaryConverter.byteArrayToInt(data, offset + 20);
            String progName = conv.byteArrayToString(data, offset + 24, 10).trim();
            String progLib = conv.byteArrayToString(data, offset + 34, 10).trim();
            int miInstrNum = BinaryConverter.byteArrayToInt(data, offset + 44);
            String modName = conv.byteArrayToString(data, offset + 48, 10).trim();
            String modLib = conv.byteArrayToString(data, offset + 58, 10).trim();
            byte controlBound = data[offset + 68];
            long actGroupNum = BinaryConverter.byteArrayToUnsignedInt(data, offset + 72);
            String actGroupName = conv.byteArrayToString(data, offset + 76, 10).trim();
            String progASPName = conv.byteArrayToString(data, offset + 88, 10).trim();
            String progLibASP = conv.byteArrayToString(data, offset + 98, 10).trim();
            int progASPNum = BinaryConverter.byteArrayToInt(data, offset + 108);
            int progLibASPNum = BinaryConverter.byteArrayToInt(data, offset + 112);
            long actGroupNumLong = (isV5R3 ? BinaryConverter.byteArrayToLong(data, offset + 116) : 0);
            String[] statementIdentifiers = new String[numStmtID];
            for (int c = 0; c < numStmtID; ++c)
            {
                statementIdentifiers[c] = conv.byteArrayToString(data, offset + stmtIDDisp + (c * 10), 10).trim();
            }
            String procName = (procNameDisp > 0 ? conv.byteArrayToString(data, offset + procNameDisp, procNameLen).trim() : null);
            entries[i] = new CallStackEntry(this, threadID, reqLevel, progName, progLib, miInstrNum, modName, modLib, controlBound, actGroupNum, actGroupName, progASPName, progLibASP, progASPNum, progLibASPNum, actGroupNumLong, statementIdentifiers, procName);
            offset += entryLength;
        }
        return entries;
    }

    /**
     Returns the coded character set identifier (CCSID) used for this job.
     @return  The coded character set identifier (CCSID) used for this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #CCSID
     **/
    public int getCodedCharacterSetID() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(CCSID);
    }

    /**
     Returns the completion status of the job.
     @return  The completion status of the job.  Possible values are:
     <ul>
     <li>{@link #COMPLETION_STATUS_NOT_COMPLETED COMPLETION_STATUS_NOT_COMPLETED} - The job has not completed.
     <li>{@link #COMPLETION_STATUS_COMPLETED_NORMALLY COMPLETION_STATUS_COMPLETED_NORMALLY} - The job completed normally.
     <li>{@link #COMPLETION_STATUS_COMPLETED_ABNORMALLY COMPLETION_STATUS_COMPLETED_ABNORMALLY} - The job completed abnormally.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #COMPLETION_STATUS
     **/
    public String getCompletionStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return (String)getValue(COMPLETION_STATUS);
    }

    /**
     Returns the country or region identifier associated with this job.
     @return  The country or region identifier associated with this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #COUNTRY_ID
     **/
    public String getCountryID() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(COUNTRY_ID)).trim();
    }

    /**
     Returns the amount of processing unit time (in milliseconds) that the job used.  If the processing unit time used is greater than or equal to 2,147,483,647 milliseconds, a value of -1 is returned.  Use the CPU_TIME_USED_LARGE attribute to retrieve values that are greater than or equal to 2,147,483,647.
     @return  The amount of processing unit time (in milliseconds) that the job used.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #CPU_TIME_USED
     **/
    public int getCPUUsed() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(CPU_TIME_USED);
    }

    /**
     Returns the name of the current library for the initial thread of the job.  If no current library exists, the CURRENT_LIBRARY_EXISTENCE attribute returns 0 and this attribute returns an empty string ("").
     @return  The name of the current library for the initial thread of the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #CURRENT_LIBRARY
     **/
    public String getCurrentLibrary() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(CURRENT_LIBRARY)).trim();
    }

    /**
     Indicates if a current library exists.
     @return  true if a current library exists, false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #CURRENT_LIBRARY_EXISTENCE
     **/
    public boolean getCurrentLibraryExistence() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(CURRENT_LIBRARY_EXISTENCE) == 1;
    }

    /**
     Returns the date and time when the job was placed on the system.
     @return  The date and time when the job was placed on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_ENTERED_SYSTEM
     **/
    public Date getDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsDate(DATE_ENTERED_SYSTEM);
    }

    /**
     Returns the format in which dates are presented.
     @return  The format in which dates are presented.  Possible values are:
     <ul>
     <li>{@link #DATE_FORMAT_YMD DATE_FORMAT_YMD} - Year, month, and day format.
     <li>{@link #DATE_FORMAT_MDY DATE_FORMAT_MDY} - Month, day, and year format.
     <li>{@link #DATE_FORMAT_DMY DATE_FORMAT_DMY} - Day, month, and year format.
     <li>{@link #DATE_FORMAT_JULIAN DATE_FORMAT_JULIAN} - Julian format (year and day).
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_FORMAT
     **/
    public String getDateFormat() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(DATE_FORMAT)).trim();
    }

    /**
     Returns the value used to separate days, months, and years when presenting a date.
     @return  The value used to separate days, months, and years when presenting a date.  Possible values are:
     <ul>
     <li>{@link #DATE_SEPARATOR_SLASH DATE_SEPARATOR_SLASH} - A slash (/) is used for the date separator.
     <li>{@link #DATE_SEPARATOR_DASH DATE_SEPARATOR_DASH} - A dash (-) is used for the date separator.
     <li>{@link #DATE_SEPARATOR_PERIOD DATE_SEPARATOR_PERIOD} - A period (.) is used for the date separator.
     <li>{@link #DATE_SEPARATOR_BLANK DATE_SEPARATOR_BLANK} - A blank is used for the date separator.
     <li>{@link #DATE_SEPARATOR_COMMA DATE_SEPARATOR_COMMA} - A comma (,) is used for the date separator.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_SEPARATOR
     **/
    public String getDateSeparator() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return (String)getValue(DATE_SEPARATOR);  // Don't trim.
    }

    /**
     Returns whether connections using distributed data management (DDM) protocols remain active when they are not being used.  The connections include APPC conversations, active TCP/IP connections, or Opti-Connect connections.  The DDM protocols are used in Distributed Relational Database Architecture (DRDA) applications, DDM applications, or DB2 Multisystem applications.
     @return  Whether connections using distributed data management (DDM) protocols remain active when they are not being used.  Possible values are:
     <ul>
     <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP KEEP_DDM_CONNECTIONS_ACTIVE_KEEP} - The system keeps DDM connections active when there are no users, except for the following:
     <ul>
     <li>The routing step ends on the source system.  The routing step ends when the job ends or when the job is rerouted to another routing step.
     <li>The Reclaim Distributed Data Management Conversation (RCLDDMCNV) command or the Reclaim Resources (RCLRSC) command runs.
     <li>A communications failure or an internal failure occurs.
     <li>A DRDA connection to an application server not running on an IBM i system ends.
     </ul>
     <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP KEEP_DDM_CONNECTIONS_ACTIVE_DROP} - The system ends a DDM connection when there are no users.  Examples include when an application closes a DDM file, or when a DRDA application runs an SQL DISCONNECT statement.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #KEEP_DDM_CONNECTIONS_ACTIVE
     **/
    public String getDDMConversationHandling() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(KEEP_DDM_CONNECTIONS_ACTIVE)).trim();
    }

    /**
     Returns the decimal format used for this job.
     @return  The decimal format used for this job.  Possible values are:
     <ul>
     <li>{@link #DECIMAL_FORMAT_PERIOD DECIMAL_FORMAT_PERIOD} - Uses a period for a decimal point, a comma for a 3-digit grouping character, and zero-suppresses to the left of the decimal point.
     <li>{@link #DECIMAL_FORMAT_COMMA_J DECIMAL_FORMAT_COMMA_J} - Uses a comma for a decimal point, a period for a 3-digit grouping character, and zero-suppresses to the left of the decimal point.
     <li>{@link #DECIMAL_FORMAT_COMMA_I DECIMAL_FORMAT_COMMA_I} - Uses a comma for a decimal point and a period for a 3-digit grouping character.  The zero-suppression character is in the second position (rather than the first) to the left of the decimal notation.  Balances with zero values to the left of the comma are written with one leading zero (0,04).  This constant also overrides any edit codes that might suppress the leading zero.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DECIMAL_FORMAT
     **/
    public String getDecimalFormat() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(DECIMAL_FORMAT)).trim();
    }

    /**
     Returns the default coded character set identifier (CCSID) used for this job.
     @return  The default coded character set identifier (CCSID) used for this job.  The value will be 0 if the job is not active.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DEFAULT_CCSID
     **/
    public int getDefaultCodedCharacterSetIdentifier() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(DEFAULT_CCSID);
    }

    /**
     Returns the default maximum time (in seconds) that a thread in the job waits for a system instruction, such as a LOCK machine interface (MI) instruction, to acquire a resource.  This default wait time is used when a wait time is not otherwise specified for a given situation.  Normally, this is the amount of time the user is willing to wait for the system before the request is ended.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not appy to subsequent routing steps.  The valid range is 1 through 9999999.  A value of -1 represents no maximum wait time (*NOMAX).
     @return  The default maximum time (in seconds) that a thread in the job waits for a system instruction to acquire a resource.  The value -1 means there is no maximum (*NOMAX).
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DEFAULT_WAIT_TIME
     **/
    public int getDefaultWait() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(DEFAULT_WAIT_TIME);
    }

    /**
     Returns the action taken for interactive jobs when an I/O error occurs for the job's requesting program device.
     @return  The action taken for interactive jobs when an I/O error occurs for the job's requesting program device.  Possible values are:
     <ul>
     <li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE DEVICE_RECOVERY_ACTION_MESSAGE} - Signals the I/O error message to the application and lets the application program perform error recovery.
     <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE} - Disconnects the job when an I/O error occurs.  When the job reconnects, the system sends an error message to the application program, indicating the job has reconnected and that the work station device has recovered.
     <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST} - Disconnects the job when an I/O error occurs.  When the job reconnects, the system sends the End Request (ENDRQS) command to return control to the previous request level.
     <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB DEVICE_RECOVERY_ACTION_END_JOB} - Ends the job when an I/O error occurs.  A message is sent to the job's log and to the history log (QHST) indicating the job ended because of a device error.
     <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST} - Ends the job when an I/O error occurs.  There is no job log produced for the job.  The system sends a message to the QHST log indicating the job ended because of a device error.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public String getDeviceRecoveryAction() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(DEVICE_RECOVERY_ACTION)).trim();
    }

    /**
     Returns the message severity level of escape messages that can cause a batch job to end.  The batch job ends when a request in the batch input stream sends an escape message, whose severity is equal to or greater than this value, to the request processing program.
     @return  The message severity level of escape messages that can cause a batch job to end.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #END_SEVERITY
     **/
    public int getEndSeverity() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(END_SEVERITY);
    }

    /**
     Returns additional information (as described by the FUNCTION_TYPE attribute) about the last high-level function initiated by the initial thread.
     @return  The additional information (as described by the FUNCTION_TYPE attribute) about the last high-level function initiated by the initial thread.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #FUNCTION_NAME
     **/
    public String getFunctionName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(FUNCTION_NAME)).trim();
    }

    /**
     Returns the last high-level function initiated by the initial thread.  This field may not be cleared when a function is completed.
     @return  The last high-level function initiated by the initial thread.  Possible values are:
     <ul>
     <li>{@link #FUNCTION_TYPE_BLANK FUNCTION_TYPE_BLANK} - The system is not performing a logged function.
     <li>{@link #FUNCTION_TYPE_COMMAND FUNCTION_TYPE_COMMAND} - A command is running interactively, or it is in a batch input stream, or it was requested from a system menu.  Commands in CL programs or REXX procedures are not logged.  The FUNCTION_NAME attribute contains the name of the command and is only updated when a command is processed.
     <li>{@link #FUNCTION_TYPE_DELAY FUNCTION_TYPE_DELAY} - The initial thread of the job is processing a Delay Job (DLYJOB) command.  The FUNCTION_NAME attribute contains the number of seconds the job is delayed (up to 999999 seconds), or the time when the job is to resume processing (HH:MM:SS), depending on how you specified the command.
     <li>{@link #FUNCTION_TYPE_GROUP FUNCTION_TYPE_GROUP} - The Transfer Group Job (TFRGRPJOB) command suspended the job.  The FUNCTION_NAME attribute contains the group job name for that job.
     <li>{@link #FUNCTION_TYPE_INDEX FUNCTION_TYPE_INDEX} - The initial thread of the job is rebuilding an index (access path).  The FUNCTION_NAME attribute contains the name of the logical file whose index is rebuilt.
     <li>{@link #FUNCTION_TYPE_JAVA FUNCTION_TYPE_JAVA} - The initial thread of the job is running a Java Vertual Machine (JVM).  The FUNCTION_NAME attribute contains the name of the java class.
     <li>{@link #FUNCTION_TYPE_LOG FUNCTION_TYPE_LOG} - The system logs history information in a database file.  The FUNCTION_NAME attribute contains the name of the log (QHST is the only log currently supported).
     <li>{@link #FUNCTION_TYPE_MRT FUNCTION_TYPE_MRT} - The job is a multiple requester terminal (MRT) job if the {@link #JOB_TYPE JOB_TYPE} is {@link #JOB_TYPE_BATCH JOB_TYPE_BATCH} and the {@link #JOB_SUBTYPE JOB_SUBTYPE} is {@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT}, or it is an interactive job attached to an MRT job if the {@link #JOB_TYPE JOB_TYPE} is {@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE}.
     <p>For MRT jobs, the FUNCTION_NAME attribute contains information in the following format:
     <ul>
     <li>CHAR(2): The number of requesters currently attached to the MRT job.
     <li>CHAR(1): The field is reserved for a / (slash).
     <li>CHAR(2): The maximum number (MRTMAX) of requesters.
     <li>CHAR(1): Reserved.
     <li>CHAR(3): The never-ending program (NEP) indicator.  If an MRT is also an NEP, the MRT stays active even if there are no requesters of the MRT.  A value of NEP indicates a never-ending program.  A value of blanks indicates that it is not a never-ending program.
     <li>CHAR(1): Reserved.
     </ul>
     <p>For interactive jobs attached to an MRT, the FUNCTION_NAME attribute contains the name of the MRT procedure.
     <li>{@link #FUNCTION_TYPE_MENU FUNCTION_TYPE_MENU} - The initial thread of the job is currently at a system menu.  The FUNCTION_NAME field contains the name of the menu.
     <li>{@link #FUNCTION_TYPE_IO FUNCTION_TYPE_IO} - The job is a subsystem monitor that is performing input/output (I/O) operations to a work station.  The FUNCTION_NAME attribute contains the name of the work station device to which the subsystem is performing an input/output operation.
     <li>{@link #FUNCTION_TYPE_PROCEDURE FUNCTION_TYPE_PROCEDURE} - The initial thread of the job is running a procedure.  The FUNCTION_NAME attribute contains the name of the procedure.
     <li>{@link #FUNCTION_TYPE_PROGRAM FUNCTION_TYPE_PROGRAM} - The initial thread of the job is running a program.  The FUNCTION_NAME attribute contains the name of the program.
     <li>{@link #FUNCTION_TYPE_SPECIAL FUNCTION_TYPE_SPECIAL} - The function type is a special function.  For this value, the FUNCTION_NAME attribute contains one of the following values:
     <ul>
     <li>ADLACTJOB: Auxiliary storage is being allocated for the number of active jobs specified in the QADLACTJ system value.  This may indicate that the system value for the initial number of active jobs is too low.
     <li>ADLTOTJOB: Auxiliary storage is being allocated for the number of jobs specified in the QADLTOTJ system value.
     <li>CMDENT: The Command Entry display is being used.
     <li>COMMIT: A commit operation is being performed.
     <li>DIRSHD: Directory shadowing.
     <li>DLTSPLF: The system is deleting a spooled file.
     <li>DUMP: A dump is in process.
     <li>JOBIDXRCY: A damaged job index is being recovered.
     <li>JOBLOG: The system is producing a job log.
     <li>PASSTHRU: The job is a pass-through job.
     <li>RCLSPLSTG: Empty spooled database members are being deleted.
     <li>ROLLBACK: A rollback operation is being performed.
     <li>SPLCLNUP: Spool cleanup is in process.
     </ul>
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #FUNCTION_TYPE
     **/
    public String getFunctionType() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return (String)getValue(FUNCTION_TYPE);  // Don't trim.
    }

    /**
     Returns how the job answers inquiry messages.
     @return  How the job answers inquiry messages.  Possible values are:
     <ul>
     <li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED INQUIRY_MESSAGE_REPLY_REQUIRED} - The job requires an answer for any inquiry messages that occur while this job is running.
     <li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT INQUIRY_MESSAGE_REPLY_DEFAULT} - The system uses the default message reply to answer any inquiry messages issued while this job is running.  The default reply is either defined in the message description or is the default system reply.
     <li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST} - The system reply list is checked to see if there is an entry for an inquiry message issued while this job is running.  If a match occurs, the system uses the reply value for that entry.  If no entry exists for that message, the system uses an inquiry message.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #INQUIRY_MESSAGE_REPLY
     **/
    public String getInquiryMessageReply() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(INQUIRY_MESSAGE_REPLY)).trim();
    }

    /**
     Returns the count of operator interactions, such as pressing the Enter key or a function key.  This field is zero for jobs that have no interactions.
     @return  The count of operator interactions, such as pressing the Enter key or a function key.  This field is zero for jobs that have no interactions.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #INTERACTIVE_TRANSACTIONS
     **/
    public int getInteractiveTransactions() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(INTERACTIVE_TRANSACTIONS);
    }

    /**
     Returns the internal job identifier.  The internal job identifier is a value input to other APIs to increase the speed of locating the job on the system.  The identifier is not valid following an initial program load (IPL).  If you attempt to use it after an IPL, an exception occurs.
     @return  The internal job identifier.
     @deprecated  The internal job identifier should be treated as a byte array of 16 bytes.
     **/
    public String getInternalJobID()
    {
        return internalJobID_;
    }

    /**
     Returns the internal job identifier.  The internal job identifier is a value input to other APIs to increase the speed of locating the job on the system.  The identifier is not valid following an initial program load (IPL).  If you attempt to use it after an IPL, an exception occurs.
     @return  The 16-byte internal job identifier, or null if one has not been set or retrieved from the system.
     @see  #setInternalJobIdentifier
     **/
    public byte[] getInternalJobIdentifier()
    {
        return realInternalJobID_;
    }

    /**
     Returns an identifier assigned to the job by the system to collect resource use information for the job when job accounting is active.
     @return  An identifier assigned to the job by the system to collect resource use information for the job when job accounting is active.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #ACCOUNTING_CODE
     **/
    public String getJobAccountingCode() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(ACCOUNTING_CODE)).trim();
    }

    /**
     Returns the date and time when the job began to run on the system.
     @return  The date and time when the job began to run on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_STARTED
     **/
    public Date getJobActiveDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsDate(DATE_STARTED);
    }

    /**
     Returns the date to be used for the job.  This value is for jobs whose status is *JOBQ or *ACTIVE.  For jobs with a status of *OUTQ, the value for this field is blank.
     @return  The date to be used for the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_DATE
     **/
    public Date getJobDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsDate(JOB_DATE);
    }

    /**
     Returns the fully qualified integrated file system path name for the job description.  The job description is a set of job-related attributes used for one or more jobs on the system.  These attributes determine how the job is run on the system.  Multiple jobs can also use the same job description.
     @return  The fully qualified integrated file system path name for the job description.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_DESCRIPTION
     @see  QSYSObjectPathName
     **/
    public String getJobDescription() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String jobd = ((String)getValue(JOB_DESCRIPTION)).trim();
        if (jobd.length() > 0 && !jobd.startsWith("*"))
        {
            String name = jobd.substring(0, 10).trim();
            String lib = jobd.substring(10, jobd.length());
            String path = QSYSObjectPathName.toPath(lib, name, "JOBD");
            return path;
        }
        return jobd;
    }

    /**
     Returns the date and time when the job completed running on the system.
     @return  The date and time when the job completed running on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_ENDED
     **/
    public Date getJobEndedDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsDate(DATE_ENDED);
    }

    /**
     Returns the date and time when the job was placed on the system.
     @return  The date and time when the job was placed on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_ENTERED_SYSTEM
     **/
    public Date getJobEnterSystemDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsDate(DATE_ENTERED_SYSTEM);
    }

    /**
     Returns the job log.
     @return  The job log.
     **/
    public JobLog getJobLog()
    {
        // Rather than using name_, user_, and number_, I will get their attribute values.  This will work when CURRENT or an internal job id is specified.
        try
        {
            return new JobLog(system_, (String)getValue(JOB_NAME), (String)getValue(USER_NAME), (String)getValue(JOB_NUMBER));
        }
        catch (Exception e)
        {
            if (Trace.traceOn_)
            {
                Trace.log(Trace.ERROR, "Error retrieving values to create job log: "+e);
            }
        }
        return null;
    }

    /**
     Returns the action to take when the message queue is full.
     @return  The action to take when the message queue is full.  Possible values are:
     <ul>
     <li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP MESSAGE_QUEUE_ACTION_NO_WRAP} - When the job message queue is full, do not wrap.  This action causes the job to end.
     <li>{@link #MESSAGE_QUEUE_ACTION_WRAP MESSAGE_QUEUE_ACTION_WRAP} - When the job message queue is full, wrap to the beginning and start filling again.
     <li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP MESSAGE_QUEUE_ACTION_PRINT_WRAP} - When the job message queue is full, wrap the message queue and print the messages that are being overlaid because of the wrapping.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #MESSAGE_QUEUE_ACTION
     **/
    public String getJobMessageQueueFullAction() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(MESSAGE_QUEUE_ACTION)).trim();
    }

    /**
     Returns the maximum size (in megabytes) that the job message queue can become.
     @return  The maximum size (in megabytes) that the job message queue can become.  The range is 2 to 64.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #MESSAGE_QUEUE_MAX_SIZE
     **/
    public int getJobMessageQueueMaximumSize() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(MESSAGE_QUEUE_MAX_SIZE);
    }

    /**
     Returns the date and time this job was put on this job queue.  This field will contain blanks if the job was not on a job queue.
     @return  The date and time this job was put on this job queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_QUEUE_DATE
     **/
    public Date getJobPutOnJobQueueDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsSystemDate(JOB_QUEUE_DATE);
    }

    /**
     Returns the date and time the job is scheduled to become active.
     @return  The date and time the job is scheduled to become active.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SCHEDULE_DATE
     **/
    public Date getScheduleDate() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (cachedChanges_ != null && (cachedChanges_.contains(SCHEDULE_DATE) || cachedChanges_.contains(SCHEDULE_TIME)))
        {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            String scheduleDate = (String)cachedChanges_.get(SCHEDULE_DATE);
            if (scheduleDate != null)
            {
                int century = Integer.parseInt(scheduleDate.substring(0, 1));
                int year = Integer.parseInt(scheduleDate.substring(1, 3));
                int month = Integer.parseInt(scheduleDate.substring(3, 5));
                int day = Integer.parseInt(scheduleDate.substring(5, 7));

                calendar.set(Calendar.YEAR, year + ((century == 0) ? 1900 : 2000));
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
            }
            else
            {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
            }

            String scheduleTime = (String)cachedChanges_.get(SCHEDULE_TIME);
            if (scheduleTime != null)
            {
                int hours = Integer.parseInt(scheduleTime.substring(0, 2));
                int minutes = Integer.parseInt(scheduleTime.substring(2, 4));
                int seconds = Integer.parseInt(scheduleTime.substring(4, 6));

                calendar.set(Calendar.HOUR_OF_DAY, hours);
                calendar.set(Calendar.MINUTE, minutes);
                calendar.set(Calendar.SECOND, seconds);
            }
            else
            {
                calendar.set(Calendar.YEAR, 0);
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 0);
            }
            return calendar.getTime();
        }
        return getAsSystemDate(SCHEDULE_DATE_GETTER);  // System timestamp format again.
    }

    /**
     Returns the status of this job on the job queue.
     @return  The status of this job on the job queue.  Possible values are:
     <ul>
     <li>{@link #JOB_QUEUE_STATUS_BLANK JOB_QUEUE_STATUS_BLANK} - This job was not on a job queue.
     <li>{@link #JOB_QUEUE_STATUS_SCHEDULED JOB_QUEUE_STATUS_SCHEDULED} - This job will run as scheduled.
     <li>{@link #JOB_QUEUE_STATUS_HELD JOB_QUEUE_STATUS_HELD} - This job is being held on the job queue.
     <li>{@link #JOB_QUEUE_STATUS_READY JOB_QUEUE_STATUS_READY} - This job is ready to be selected.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_QUEUE_STATUS
     **/
    public String getJobStatusInJobQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(JOB_QUEUE_STATUS)).trim();
    }

    /**
     Returns the current setting of the job switches used by this job.  This value is returned for all job types.
     @return  The current setting of the job switches used by this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_SWITCHES
     **/
    public String getJobSwitches() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return (String)getValue(JOB_SWITCHES);
    }

    /**
     Returns the language identifier associated with this job.  The language identifier is used when *LANGIDUNQ or *LANGIDSHR is specified on the sort sequence parameter.  If the job CCSID is 65535, this parameter is also used to determine the value of the job default CCSID.
     @return  The language identifier associated with this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LANGUAGE_ID
     **/
    public String getLanguageID() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return (String)getValue(LANGUAGE_ID);
    }

    /**
     Returns a value indicating whether or not commands are logged for CL programs that are run.
     @return  The value indicating whether or not commands are logged for CL programs that are run.  Possible values are:
     <ul>
     <li>{@link #LOG_CL_PROGRAMS_YES LOG_CL_PROGRAMS_YES} - Commands are logged for CL programs that are run.
     <li>{@link #LOG_CL_PROGRAMS_NO LOG_CL_PROGRAMS_NO} - Commands are not logged for CL programs that are run.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOG_CL_PROGRAMS
     **/
    public String getLoggingCLPrograms() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(LOG_CL_PROGRAMS)).trim();
    }

    /**
     Returns what type of information is logged.
     @return  A value indicating what type of information is logged.  Possible values are:
     <ul>
     <li>0 - No messages are logged.
     <li>1 - All messages sent to the job's external message queue with a severity greater than or equal to the message logging severity are logged.  This includes the indication of job start, job end, and job completion status.
     <li>2 - The following information is logged:
     <ul>
     <li>Logging level 1 information.
     <li>Request messages that result in a high-level message with a severity code greater than or equal to the logging severity cause the request message and all associated messages to be logged.  A high-level message is one that is sent to the program message queue of the program that receives the request message.  For example, QCMD is an IBM-supplied request processing program that receives request messages.
     </ul>
     <li>3 - The following information is logged:
     <ul>
     <li>Logging level 1 and 2 information is logged.
     <li>All request messages are logged.
     <li>Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS attribute and the log attribute of the CL program.
     </ul>
     <li>4 - The following information is logged:
     <ul>
     <li>All request messages and all messages with a severity greater than or equal to the message logging severity, including trace messages.
     <li>Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS attribute and the log attribute of the CL program.
     </ul>
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOGGING_LEVEL
     **/
    public int getLoggingLevel() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return Integer.parseInt((String)getValue(LOGGING_LEVEL));
    }

    /**
     Returns the severity level that is used in conjunction with the logging level to determine which error messages are logged in the job log.  The values range from 00 through 99.
     @return  The severity level that is used in conjunction with the logging level to determine which error messages are logged in the job log.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOGGING_SEVERITY
     **/
    public int getLoggingSeverity() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(LOGGING_SEVERITY);
    }

    /**
     Returns the level of message text that is written in the job log when a message is logged according to the LOGGING_LEVEL and LOGGING_SEVERITY.
     @return  The level of message text that is written in the job log when a message is logged according to the LOGGING_LEVEL and LOGGING_SEVERITY.  Possible values are:
     <ul>
     <li>{@link #LOGGING_TEXT_MESSAGE LOGGING_TEXT_MESSAGE} - Only the message text is written to the job log.
     <li>{@link #LOGGING_TEXT_SECLVL LOGGING_TEXT_SECLVL} - Both the message text and the message help (cause and recovery) of the error message are written to the job log.
     <li>{@link #LOGGING_TEXT_NO_LIST LOGGING_TEXT_NO_LIST} - If the job ends normally, no job log is produced.  If the job ends abnormally (the job end code is 20 or higher), a job log is produced.  The messages that appear in the job log contain both the message text and the message.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOGGING_TEXT
     **/
    public String getLoggingText() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(LOGGING_TEXT)).trim();
    }

    /**
     Returns the mode name of the advanced program-to-program communications (APPC) device that started the job.
     @return  The mode name of the advanced program-to-program communications (APPC) device that started the job.  Possible values are:
     <ul>
     <li>The mode name is *BLANK.
     <li>The mode name is blanks.
     <li>The name of the mode.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #MODE
     **/
    public String getModeName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(MODE)).trim();
    }

    /**
     Returns the job name.
     @return  The job name.
     **/
    public String getName()
    {
        return name_;
    }

    /**
     Returns the job number.
     @return  The job number.
     **/
    public String getNumber()
    {
        return number_;
    }

    /**
     Returns the number of libraries in the system portion of the library list of the initial thread of the job.
     @return  The number of libraries in the system portion of the library list of the initial thread of the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SYSTEM_LIBRARY_LIST
     **/
    public int getNumberOfLibrariesInSYSLIBL() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getSystemLibraryList().length;
    }

    /**
     Returns the number of libraries in the user portion of the library list for the initial thread of the job.
     @return  The number of libraries in the user portion of the library list for the initial thread of the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #USER_LIBRARY_LIST
     **/
    public int getNumberOfLibrariesInUSRLIBL() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getUserLibraryList().length;
    }

    /**
     Returns the number of libraries that contain product information for the initial thread of the job.
     @return  The number of libraries that contain product information for the initial thread of the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRODUCT_LIBRARIES
     **/
    public int getNumberOfProductLibraries() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getProductLibraries().length;
    }

    /**
     Returns the fully qualified integrated file system path name of the default output queue that is used for spooled output produced by this job.  The default output queue is only for spooled printer files that specify *JOB for the output queue.
     @return  The fully qualified integrated file system path name of the default output queue that is used for spooled output produced by this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #OUTPUT_QUEUE
     @see  QSYSObjectPathName
     **/
    public String getOutputQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String queue = ((String)getValue(OUTPUT_QUEUE)).trim();
        if (queue.length() > 0 && !queue.startsWith("*"))
        {
            String name = queue.substring(0, 10).trim();
            String lib = queue.substring(10, queue.length());
            String path = QSYSObjectPathName.toPath(lib, name, "OUTQ");
            return path;
        }
        return queue;
    }

    /**
     Returns the output priority for spooled output files that this job produces.  The highest priority is 0, and the lowest is 9.
     @return  The output priority for spooled output files that this job produces.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #OUTPUT_QUEUE_PRIORITY
     **/
    public int getOutputQueuePriority() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(OUTPUT_QUEUE_PRIORITY);
    }

    /**
     Returns the identifier of the system-related pool from which the job's main storage is allocated.  These identifiers are not the same as those specified in the subsystem description, but are the same as the system pool identifiers shown on the system status display.  This is the pool that the threads in the job start in.  Also see the CURRENT_SYSTEM_POOL_ID for more information.
     @return  The identifier of the system-related pool from which the job's main storage is allocated.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SYSTEM_POOL_ID
     **/
    public int getPoolIdentifier() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(SYSTEM_POOL_ID);
    }

    /**
     Returns the printer device used for printing output from this job.
     @return  The printer device used for printing output from this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRINTER_DEVICE_NAME
     **/
    public String getPrinterDeviceName() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(PRINTER_DEVICE_NAME)).trim();
    }

    /**
     Returns a value indicating whether border and header information is provided when the Print key is pressed.
     @return  The value indicating whether border and header information is provided when the Print key is pressed.  Possible values are:
     <ul>
     <li>{@link #PRINT_KEY_FORMAT_NONE PRINT_KEY_FORMAT_NONE} - The border and header information is not included with output from the Print key.
     <li>{@link #PRINT_KEY_FORMAT_BORDER PRINT_KEY_FORMAT_BORDER} - The border information is included with output from the Print key.
     <li>{@link #PRINT_KEY_FORMAT_HEADER PRINT_KEY_FORMAT_HEADER} - The header information is included with output from the Print key.
     <li>{@link #PRINT_KEY_FORMAT_ALL PRINT_KEY_FORMAT_ALL} - The border and header information is included with output from the Print key.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRINT_KEY_FORMAT
     **/
    public String getPrintKeyFormat() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(PRINT_KEY_FORMAT)).trim();
    }

    /**
     Returns the line of text (if any) that is printed at the bottom of each page of printed output for the job.
     @return  The line of text (if any) that is printed at the bottom of each page of printed output for the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRINT_TEXT
     **/
    public String getPrintText() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(PRINT_TEXT)).trim();
    }

    /**
     Returns the libraries that contain product information for the initial thread of the job.
     @return  The libraries that contain product information for the initial thread of the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRODUCT_LIBRARIES
     **/
    public String[] getProductLibraries() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String val = (String)getValue(PRODUCT_LIBRARIES);
        StringTokenizer st = new StringTokenizer(val, " ");
        String[] libraries = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            libraries[i++] = st.nextToken();
        }
        return libraries;
    }

    /**
     Indicates whether or not the job is eligible to be moved out of main storage and put into auxiliary storage at the end of a time slice or when entering a long wait (such as waiting for a work station user's response).  This attribute is ignored when more than one thread is active within the job.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not apply to subsequent routing steps.
     @return  true if the job is eligible to be moved out of main storage and put into auxiliary storage at the end of a time slice or when entering a long wait; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #ELIGIBLE_FOR_PURGE
     **/
    public boolean getPurge() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(ELIGIBLE_FOR_PURGE)).trim().equals(ELIGIBLE_FOR_PURGE_YES);
    }

    /**
     Returns the fully qualified integrated file system path name of the job queue that the job is currently on, or that the job was on if it is currently active.  This value is for jobs whose status is *JOBQ or *ACTIVE.  For jobs with a status of *OUTQ, the value for this field is blank.
     @return  The fully qualified integrated file system path name of the job queue that the job is currently on, or that the job was on if it is currently active.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_QUEUE
     @see  QSYSObjectPathName
     **/
    public String getQueue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String queue = ((String)getValue(JOB_QUEUE)).trim();
        if (queue.length() > 0 && !queue.startsWith("*"))
        {
            String name = queue.substring(0, 10).trim();
            String lib = queue.substring(10, queue.length());
            String path = QSYSObjectPathName.toPath(lib, name, "JOBQ");
            return path;
        }
        return queue;
    }

    /**
     Returns the scheduling priority of the job compared to other jobs on the same job queue.  The highest priority is 0 and the lowest is 9.  This value is valid for jobs whose status is *JOBQ or *ACTIVE.  For jobs with a status of *OUTQ, then the priority returned is -1.
     @return  The scheduling priority of the job compared to other jobs on the same job queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_QUEUE_PRIORITY
     **/
    public int getQueuePriority() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String p = ((String)getValue(JOB_QUEUE_PRIORITY)).trim();
        if (p.length() == 0) return -1;
        return (new Integer(p)).intValue();
    }

    /**
     Returns the routing data that is used to determine the routing entry that identifies the program to start for the routing step.
     @return  The routing data that is used to determine the routing entry that identifies the program to start for the routing step.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #ROUTING_DATA
     **/
    public String getRoutingData() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(ROUTING_DATA)).trim();
    }

    /**
     Returns the priority at which the job is currently running, relative to other jobs on the system.  The run priority ranges from 0 (highest priority) to 99 (lowest priority).
     @return  The priority at which the job is currently running, relative to other jobs on the system.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #RUN_PRIORITY
     **/
    public int getRunPriority() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(RUN_PRIORITY);
    }

    /**
     Indicates whether the job is to be treated like a signed-on user on the system.
     @return  true if the job should be treateded like a signed-on user; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SIGNED_ON_JOB
     **/
    public boolean getSignedOnJob() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(SIGNED_ON_JOB)).trim().equals("0");
    }

    /**
     Returns the name of the sort sequence table associated with this job.
     @return  The name of the sort sequence table associated with this job.  Possible values are:
     <ul>
     <li>{@link #SORT_SEQUENCE_TABLE_NONE SORT_SEQUENCE_TABLE_NONE} - No sort sequence table is used.  The hexadecimal values of the characters are used to determine the sort sequence.
     <li>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT} - The sort sequence table used can contain the same weight for multiple characters, and it is the shared weight sort table associated with the language specified in the LANGUAGE_ID attribute.
     <li>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT} - The sort sequence table used must contain a unique weight for each character in the code page, and it is the unique weight sort table associated with the language specified in the LANGUAGE_ID parameter.
     <li>The fully qualified integrated file system path name of the sort sequence table associated with this job.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SORT_SEQUENCE_TABLE
     @see  QSYSObjectPathName
     **/
    public String getSortSequenceTable() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String table = ((String)getValue(SORT_SEQUENCE_TABLE)).trim();
        if (table.length() > 0 && !table.startsWith("*"))
        {
            String name = table.substring(0, 10).trim();
            String lib = table.substring(10, table.length());
            String path = QSYSObjectPathName.toPath(lib, name, "FILE");
            return path;
        }
        return table;
    }

    /**
     Returns the status of this job.
     @return  The status of this job.  Possible values are:
     <ul>
     <li>{@link #JOB_STATUS_ACTIVE JOB_STATUS_ACTIVE} - This job is an active job.  This includes group jobs, system request jobs, and disconnected jobs.
     <li>{@link #JOB_STATUS_JOBQ JOB_STATUS_JOBQ} - This job is currently on a job queue.
     <li>{@link #JOB_STATUS_OUTQ JOB_STATUS_OUTQ} - This job has completed running, but still has output on an output queue.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_STATUS
     **/
    public String getStatus() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(JOB_STATUS)).trim();
    }

    /**
     Returns whether status messages are displayed for this job.
     @return  A value indicating whether status messages are displayed for this job.  Possible values are:
     <ul>
     <li>{@link #STATUS_MESSAGE_HANDLING_NONE STATUS_MESSAGE_HANDLING_NONE} - This job does not display status messages.
     <li>{@link #STATUS_MESSAGE_HANDLING_NORMAL STATUS_MESSAGE_HANDLING_NORMAL} - This job displays status messages.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #STATUS_MESSAGE_HANDLING
     **/
    public String getStatusMessageHandling() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(STATUS_MESSAGE_HANDLING)).trim();
    }

    /**
     Returns the fully qualified integrated file system path name of the subsystem description for the subsystem in which the job is running.
     @return  The fully qualified integrated file system path name of the subsystem description for the subsystem in which the job is running.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SUBSYSTEM
     @see  QSYSObjectPathName
     **/
    public String getSubsystem() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String subsystem = ((String)getValue(SUBSYSTEM)).trim();
        if (subsystem.length() > 10)
        {
            String name = subsystem.substring(0, 10).trim();
            String lib = subsystem.substring(10, subsystem.length());
            String path = QSYSObjectPathName.toPath(lib, name, "SBSD");
            return path;
        }
        return subsystem;
    }

    /**
     Returns additional information about the job type (if any exists).
     @return  Additional information about the job type (if any exists).  Possible values are:
     <ul>
     <li>{@link #JOB_SUBTYPE_BLANK JOB_SUBTYPE_BLANK} - The job has no special subtype or is not a valid job.
     <li>{@link #JOB_SUBTYPE_IMMEDIATE JOB_SUBTYPE_IMMEDIATE} - The job is an immediate job.
     <li>{@link #JOB_SUBTYPE_PROCEDURE_START_REQUEST JOB_SUBTYPE_PROCEDURE_START_REQUEST} - The job started with a procedure start request.
     <li>{@link #JOB_SUBTYPE_MACHINE_SERVER_JOB JOB_SUBTYPE_MACHINE_SERVER_JOB} - The job is an AS/400 Advanced 36 machine server job.
     <li>{@link #JOB_SUBTYPE_PRESTART JOB_SUBTYPE_PRESTART} - The job is a prestart job.
     <li>{@link #JOB_SUBTYPE_PRINT_DRIVER JOB_SUBTYPE_PRINT_DRIVER} - The job is a print driver job.
     <li>{@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT} - The job is a System/36 multiple requester terminal (MRT) job.
     <li>{@link #JOB_SUBTYPE_ALTERNATE_SPOOL_USER JOB_SUBTYPE_ALTERNATE_SPOOL_USER} - The job is an alternate spool user job.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_SUBTYPE
     **/
    public String getSubtype() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException

    {
        return (String)getValue(JOB_SUBTYPE);  // Don't trim.
    }

    /**
     Returns the system.
     @return  The system.
     **/
    public AS400 getSystem()
    {
        return system_;
    }

    /**
     Returns the system portion of the library list of the initial thread of the job.
     @return  The system portion of the library list of the initial thread of the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SYSTEM_LIBRARY_LIST
     **/
    public String[] getSystemLibraryList() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String val = (String)getValue(SYSTEM_LIBRARY_LIST);
        StringTokenizer st = new StringTokenizer(val, " ");
        String[] libraries = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            libraries[i++] = st.nextToken();
        }
        return libraries;
    }

    /**
     Returns the value used to separate hours, minutes, and seconds when presenting a time.
     @return  The value used to separate hours, minutes, and seconds when presenting a time.  Possible values are:
     <ul>
     <li>{@link #TIME_SEPARATOR_COLON TIME_SEPARATOR_COLON} - A colon (:) is used for the time separator.
     <li>{@link #TIME_SEPARATOR_PERIOD TIME_SEPARATOR_PERIOD} - A period (.) is used for the time separator.
     <li>{@link #TIME_SEPARATOR_BLANK TIME_SEPARATOR_BLANK} - A blank is used for the time separator.
     <li>{@link #TIME_SEPARATOR_COMMA TIME_SEPARATOR_COMMA} - A comma (,) is used for the time separator.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #TIME_SEPARATOR
     **/
    public String getTimeSeparator() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return (String)getValue(TIME_SEPARATOR);  // Don't trim.
    }

    /**
     Returns the maximum amount of processor time (in milliseconds) given to each thread in this job before other threads in this job and in other jobs are given the opportunity to run.  The time slice establishes the amount of time needed by a thread in this job to accomplish a meaningful amount of processing.  At the end of the time slice, the thread might be put in an inactive state so that other threads can become active in the storage pool.  Values retrieved range from 8 through 9,999,999 milliseconds (that is, 9999.999 seconds).  Although you can specify a value of less than 8, the system takes a minimum of 8 milliseconds to run a process.
     @return  The maximum amount of processor time (in milliseconds) given to each thread in this job before other threads in this job and in other jobs are given the opportunity to run.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #TIME_SLICE
     **/
    public int getTimeSlice() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(TIME_SLICE);
    }

    /**
     Returns a value indicating whether a thread in an interactive job moves to another main storage pool at the end of its time slice.
     @return  The value indicating whether a thread in an interactive job moves to another main storage pool at the end of its time slice.  Possible values are:
     <ul>
     <li>{@link #TIME_SLICE_END_POOL_NONE TIME_SLICE_END_POOL_NONE} - A thread in the job does not move to another main storage pool when it reaches the end of its time slice.
     <li>{@link #TIME_SLICE_END_POOL_BASE TIME_SLICE_END_POOL_BASE} - A thread in the job moves to the base pool when it reaches the end of its time slice.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #TIME_SLICE_END_POOL
     **/
    public String getTimeSliceEndPool() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(TIME_SLICE_END_POOL)).trim();
    }

    /**
     Returns the total amount of response time for the initial thread, in milliseconds.  This value does not include the time used by the machine, by the attached input/output (I/O) hardware, and by the transmission lines for sending and receiving data.  This value is zero for jobs that have no interactions.  A value of -1 is returned if this field is not large enough to hold the actual result.
     @return  The total amount of response time for the initial thread, in milliseconds.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #TOTAL_RESPONSE_TIME
     **/
    public int getTotalResponseTime() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return getAsInt(TOTAL_RESPONSE_TIME);
    }

    /**
     Returns the type of job.
     @return  The type of job.  Possible values are:
     <ul>
     <li>{@link #JOB_TYPE_NOT_VALID JOB_TYPE_NOT_VALID} - The job is not a valid job.
     <li>{@link #JOB_TYPE_AUTOSTART JOB_TYPE_AUTOSTART} - The job is an autostart job.
     <li>{@link #JOB_TYPE_BATCH JOB_TYPE_BATCH} - The job is a batch job.
     <li>{@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE} - The job is an interactive job.
     <li>{@link #JOB_TYPE_SUBSYSTEM_MONITOR JOB_TYPE_SUBSYSTEM_MONITOR} - The job is a subsystem monitor job.
     <li>{@link #JOB_TYPE_SPOOLED_READER JOB_TYPE_SPOOLED_READER} - The job is a spooled reader job.
     <li>{@link #JOB_TYPE_SYSTEM JOB_TYPE_SYSTEM} - The job is a system job.
     <li>{@link #JOB_TYPE_SPOOLED_WRITER JOB_TYPE_SPOOLED_WRITER} - The job is a spooled writer job.
     <li>{@link #JOB_TYPE_SCPF_SYSTEM JOB_TYPE_SCPF_SYSTEM} - The job is the SCPF system job.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_TYPE
     **/
    public String getType() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return (String)getValue(JOB_TYPE);  // Don't trim.
    }

    /**
     Returns the user name.
     @return  The user name.
     **/
    public String getUser()
    {
        return user_;
    }

    /**
     Returns the user portion of the library list for the initial thread of the job.
     @return  The user portion of the library list for the initial thread of the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #USER_LIBRARY_LIST
     **/
    public String[] getUserLibraryList() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        String val = (String)getValue(USER_LIBRARY_LIST);
        StringTokenizer st = new StringTokenizer(val, " ");
        String[] libraries = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            libraries[i++] = st.nextToken();
        }
        return libraries;
    }

    /**
     Returns the value for the specified job attribute.  This is a generic way of retrieving job attributes, rather than using the specific getter methods.  This method will either go to the system to retrieve the job attribute, or it will return a cached value if the attribute was previously retrieved or previously set by setValue() or one of the other setter methods.  Use {@link #loadInformation loadInformation()} to refresh the attributes from the system.
     @param  attribute  The job attribute.
     @return  The current value of the attribute.  This method may return null in the rare case that the specified attribute could not be retrieved using the QUSRJOBI system API.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #loadInformation
     @see  #setValue
     **/
    public Object getValue(int attribute) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        Object obj = values_.get(attribute);
        if (obj == null)
        {
            retrieve(attribute);  // Need to retrieve it using QUSRJOBI.
            obj = values_.get(attribute);
            if (obj == null && (attribute == SCHEDULE_DATE || attribute == SCHEDULE_TIME))  // These are only setters.
            {
                Date d = getAsSystemDate(SCHEDULE_DATE_GETTER);
                setValueInternal(SCHEDULE_DATE, d);
                setValueInternal(SCHEDULE_TIME, d);
                return d;
            }
        }
        return obj;
    }

    /**
     Returns the unit of work identifier.  The unit of work identifier is used to track jobs across multiple systems.  If a job is not associated with a source or target system using advanced program-to-program communications (APPC), this information is not used.  Every job on the system is assigned a unit of work identifier.
     @return  The unit of work identifier, which is made up of:
     <ul>
     <li>Location name - 8 Characters.  The name of the source system that originated the APPC job.
     <li>Network ID - 8 Characters.  The network name associated with the unit of work.
     <li>Instance - 6 Characters.  The value that further identifies the source of the job.  This is shown as hexadecimal data.
     <li>Sequence Number - 2 Character.  A value that identifies a check-point within the application program.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOCATION_NAME
     @see  #NETWORK_ID
     @see  #INSTANCE
     @see  #SEQUENCE_NUMBER
     **/
    public String getWorkIDUnit() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        return ((String)getValue(UNIT_OF_WORK_ID)).trim();
    }

    /**
     Holds this job.
     @param  holdSpooledFiles  true to hold this job's spooled files; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @see  #end
     @see  #release
     **/
    public void hold(boolean holdSpooledFiles) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        StringBuffer buf = new StringBuffer();
        buf.append("QSYS/HLDJOB JOB(");  // not threadsafe
        buf.append(number_);
        buf.append('/');
        buf.append(user_);
        buf.append('/');
        buf.append(name_);
        buf.append(") SPLFILE(");
        buf.append(holdSpooledFiles ? "*YES)" : "*NO)");
        buf.append(" DUPJOBOPT(*MSG)");
        String toRun = buf.toString();
        // If the user wants to end the remote command server job that is servicing our connection, they are welcome to "shoot themselves in the foot".
        CommandCall cmd = getCommandCall(toRun);
        if (!cmd.run())
        {
            throw new AS400Exception(cmd.getMessageList());
        }
    }

    // Helper method.  Used to determine if an attribute that is being set is read-only and should not be allowed to be changed.
    private static boolean isReadOnly(int attribute)
    {
        return setterKeys_.get(attribute) == -1;
    }

    /**
     Refreshes the values for all attributes.  This does not cancel uncommitted changes.  To refresh just the elapsed statistics, use {@link #loadStatistics loadStatistics()}.
     @see  #loadInformation(int[])
     @see  #commitChanges
     @see  #loadStatistics
     **/
    public void loadInformation()
    {
        // Need to load an attribute from each format.
        try
        {
            // Clear all values.
            values_.clear();

            // Reset all of the important information
            setValueInternal(INTERNAL_JOB_ID, null);
            setValueInternal(INTERNAL_JOB_IDENTIFIER, null);
            setValueInternal(JOB_NAME, name_);
            setValueInternal(USER_NAME, user_);
            setValueInternal(JOB_NUMBER, number_);
            setValueInternal(JOB_STATUS, status_);
            setValueInternal(JOB_TYPE, type_);
            setValueInternal(JOB_SUBTYPE, subtype_);

            // Retrieve all values.
            retrieve(THREAD_COUNT);           // 150
            retrieve(CURRENT_SYSTEM_POOL_ID); // 200
            retrieve(JOB_DATE);               // 300
            retrieve(SERVER_TYPE);            // 400
            retrieve(LOGGING_TEXT);           // 500
            retrieve(SPECIAL_ENVIRONMENT);    // 600
            retrieve(USER_LIBRARY_LIST);      // 700
            retrieve(ELAPSED_CPU_TIME_USED);  // 1000
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Error loading job information:", e);
        }
    }

    /**
     Refreshes the values for specific attributes.  This does not cancel uncommitted changes.
     <br>Note:  The specified attributes will be refreshed, along with other attributes in their "format group".  For more information about attribute format groups, refer to the specification of the QUSRJOBI API.
     @param  attributes  The attributes to refresh.
     @see  #loadInformation()
     @see  #commitChanges
     @see  #loadStatistics
     **/
    public void loadInformation(int[] attributes)
    {
        // Load only the formats needed for the specified attributes.
        try
        {
            // Determine which formats we need to specify.
            boolean[] formats = new boolean[8];
            for (int i = 0; i < attributes.length; ++i)
            {
                int attr = attributes[i];
                byte[] format = lookupFormatName(attr);
                formats[format[5] & 0x0F] = true;

                // Clear/reset attr values, for consistency with loadInformation().
                values_.remove(attr);  // Clear the value for that attribute.
                switch (attr)
                {
                    case INTERNAL_JOB_ID:
                        setValueInternal(INTERNAL_JOB_ID, null); break;
                    case INTERNAL_JOB_IDENTIFIER:
                        setValueInternal(INTERNAL_JOB_IDENTIFIER, null); break;
                    case JOB_NAME:
                        setValueInternal(JOB_NAME, name_); break;
                    case USER_NAME:
                        setValueInternal(USER_NAME, user_); break;
                    case JOB_NUMBER:
                        setValueInternal(JOB_NUMBER, number_); break;
                    case JOB_STATUS:
                        setValueInternal(JOB_STATUS, status_); break;
                    case JOB_TYPE:
                        setValueInternal(JOB_TYPE, type_); break;
                    case JOB_SUBTYPE:
                        setValueInternal(JOB_SUBTYPE, subtype_); break;
                    default:  // Do nothing.
                }
            }

            // Retrieve values.  For each needed format, just specify any attribute associated with that format.
            if (formats[1]) retrieve(THREAD_COUNT);  // Format JOBI0150.
            if (formats[2]) retrieve(CURRENT_SYSTEM_POOL_ID);  // Format JOBI0200.
            if (formats[3]) retrieve(JOB_DATE);  // Format JOBI0300.
            if (formats[4]) retrieve(SERVER_TYPE);  // Format JOBI0400.
            if (formats[5]) retrieve(LOGGING_TEXT);  // Format JOBI0500.
            if (formats[6]) retrieve(SPECIAL_ENVIRONMENT);  // Format JOBI0600.
            if (formats[7]) retrieve(USER_LIBRARY_LIST);  // Format JOBI0700.
            if (formats[0]) retrieve(ELAPSED_CPU_TIME_USED);  // Format JOBI1000.
        }
        catch (Exception e)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Error loading job information: ", e);
        }
    }

    /**
     Refreshes just the values for the elapsed statistics.  Internally, this calls the QUSRJOBI API using the JOBI1000 format.
     @see  #resetStatistics
     @see  #loadInformation
     @see  #ELAPSED_TIME
     @see  #ELAPSED_DISK_IO
     @see  #ELAPSED_DISK_IO_ASYNCH
     @see  #ELAPSED_DISK_IO_SYNCH
     @see  #ELAPSED_INTERACTIVE_RESPONSE_TIME
     @see  #ELAPSED_INTERACTIVE_TRANSACTIONS
     @see  #ELAPSED_CPU_PERCENT_USED
     @see  #ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
     @see  #ELAPSED_CPU_TIME_USED
     @see  #ELAPSED_CPU_TIME_USED_FOR_DATABASE
     @see  #ELAPSED_LOCK_WAIT_TIME
     @see  #ELAPSED_PAGE_FAULTS
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void loadStatistics() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve(ELAPSED_CPU_TIME_USED);  // 1000
    }

    // Helper method.  Used to determine the length of the output parameter for a given format.
    private static int lookupFormatLength(byte[] format)
    {
        switch (format[5] & 0x0F)
        {
            case 1:  // Format JOBI0150.
                return 144;
            case 2:  // Format JOBI0200.
                return 191;
            case 3:  // Format JOBI0300.
                return 187;
            case 4:  // Format JOBI0400.
                return 564;
            case 5:  // Format JOBI0500.
                return 83;
            case 6:  // Format JOBI0600.
                return 322;
            case 7:  // Format JOBI0700.
                return 3028; // Max length: 80 + (268 libraries * 11).
            case 0:  // Format JOBI1000.
                return 144;
            default:
                return -1;
        }
    }

    // Helper method used to determine the format name based on which job attribute we want to retrieve.
    private static byte[] lookupFormatName(int key)
    {
        switch (key)
        {
            case JOB_NAME:
            case USER_NAME:
            case JOB_NUMBER:
            case INTERNAL_JOB_ID:
            case INTERNAL_JOB_IDENTIFIER:
            case JOB_STATUS:
            case JOB_TYPE:
            case JOB_SUBTYPE:
            case RUN_PRIORITY:
            case TIME_SLICE:
            case DEFAULT_WAIT_TIME:
            case ELIGIBLE_FOR_PURGE:
            case TIME_SLICE_END_POOL:
            case CPU_TIME_USED:
            case SYSTEM_POOL_ID:
            case MAX_CPU_TIME:
            case TEMP_STORAGE_USED:
            case MAX_TEMP_STORAGE:
            case THREAD_COUNT:
            case MAX_THREADS:
            case TEMP_STORAGE_USED_LARGE:
            case MAX_TEMP_STORAGE_LARGE:
            case CPU_TIME_USED_LARGE:
                // Format name, EBCDIC 'JOBI0150'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF0, (byte)0xF1, (byte)0xF5, (byte)0xF0 };

            case AUXILIARY_IO_REQUESTS:
            case INTERACTIVE_TRANSACTIONS:
            case TOTAL_RESPONSE_TIME:
            case FUNCTION_TYPE:
            case FUNCTION_NAME:
            case ACTIVE_JOB_STATUS:
            case CURRENT_SYSTEM_POOL_ID:
            case AUXILIARY_IO_REQUESTS_LARGE:
            case CPU_TIME_USED_FOR_DATABASE:
            case ACTIVE_JOB_STATUS_FOR_JOBS_ENDING:
            case MEMORY_POOL:
            case MESSAGE_REPLY:
                // Format name, EBCDIC 'JOBI0200'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF0, (byte)0xF2, (byte)0xF0, (byte)0xF0 };

            case JOB_QUEUE:
            case JOB_QUEUE_PRIORITY:
            case OUTPUT_QUEUE:
            case OUTPUT_QUEUE_PRIORITY:
            case PRINTER_DEVICE_NAME:
            case JOB_QUEUE_STATUS:
            case JOB_QUEUE_DATE:
            case JOB_DATE:
                // Format name, EBCDIC 'JOBI0300'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF0, (byte)0xF3, (byte)0xF0, (byte)0xF0 };

            case DATE_ENTERED_SYSTEM:
            case DATE_STARTED:
            case ACCOUNTING_CODE:
            case JOB_DESCRIPTION:
            case UNIT_OF_WORK_ID:
            case LOCATION_NAME:
            case NETWORK_ID:
            case INSTANCE:
            case SEQUENCE_NUMBER:
            case MODE:
            case INQUIRY_MESSAGE_REPLY:
            case LOG_CL_PROGRAMS:
            case BREAK_MESSAGE_HANDLING:
            case STATUS_MESSAGE_HANDLING:
            case DEVICE_RECOVERY_ACTION:
            case KEEP_DDM_CONNECTIONS_ACTIVE:
            case DATE_SEPARATOR:
            case DATE_FORMAT:
            case PRINT_TEXT:
            case SUBMITTED_BY_JOB_NAME:
            case SUBMITTED_BY_USER:
            case SUBMITTED_BY_JOB_NUMBER:
            case TIME_SEPARATOR:
            case CCSID:
            case SCHEDULE_DATE:  // In case someone asks for it.
            case SCHEDULE_TIME:  // In case someone asks for it.
            case SCHEDULE_DATE_GETTER:
            case PRINT_KEY_FORMAT:
            case SORT_SEQUENCE_TABLE:
            case LANGUAGE_ID:
            case COUNTRY_ID:
            case COMPLETION_STATUS:
            case SIGNED_ON_JOB:
            case JOB_SWITCHES:
            case MESSAGE_QUEUE_ACTION:
            case MESSAGE_QUEUE_MAX_SIZE:
            case DEFAULT_CCSID:
            case ROUTING_DATA:
            case DECIMAL_FORMAT:
            case CHARACTER_ID_CONTROL:
            case SERVER_TYPE:
            case ALLOW_MULTIPLE_THREADS:
            case JOB_LOG_OUTPUT:
            case JOB_LOG_PENDING:
            case JOB_END_REASON:
            case JOB_TYPE_ENHANCED:
            case DATE_ENDED:
            case SPOOLED_FILE_ACTION:
                // Format name, EBCDIC 'JOBI0400'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF0, (byte)0xF4, (byte)0xF0, (byte)0xF0 };

            case END_SEVERITY:
            case LOGGING_SEVERITY:
            case LOGGING_LEVEL:
            case LOGGING_TEXT:
                // Format name, EBCDIC 'JOBI0500'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF0, (byte)0xF5, (byte)0xF0, (byte)0xF0 };

            case CONTROLLED_END_REQUESTED:
            case SUBSYSTEM:
            case CURRENT_USER:
            case DBCS_CAPABLE:
            case PRODUCT_RETURN_CODE:
            case USER_RETURN_CODE:
            case PROGRAM_RETURN_CODE:
            case SPECIAL_ENVIRONMENT:
            case JOB_USER_IDENTITY:
            case JOB_USER_IDENTITY_SETTING:
            case CLIENT_IP_ADDRESS:
                // Format name, EBCDIC 'JOBI0600'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF0, (byte)0xF6, (byte)0xF0, (byte)0xF0 };

            case CURRENT_LIBRARY_EXISTENCE:
            case SYSTEM_LIBRARY_LIST:
            case PRODUCT_LIBRARIES:
            case CURRENT_LIBRARY:
            case USER_LIBRARY_LIST:
                // Format name, EBCDIC 'JOBI0700'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF0, (byte)0xF7, (byte)0xF0, (byte)0xF0 };

            case -1:
            case ELAPSED_TIME:
            case ELAPSED_DISK_IO:
            case ELAPSED_DISK_IO_ASYNCH:
            case ELAPSED_DISK_IO_SYNCH:
            case ELAPSED_INTERACTIVE_RESPONSE_TIME:
            case ELAPSED_INTERACTIVE_TRANSACTIONS:
            case ELAPSED_CPU_PERCENT_USED:
            case ELAPSED_CPU_PERCENT_USED_FOR_DATABASE:
            case ELAPSED_CPU_TIME_USED:
            case ELAPSED_CPU_TIME_USED_FOR_DATABASE:
            case ELAPSED_LOCK_WAIT_TIME:
            case ELAPSED_PAGE_FAULTS:
                // Format name, EBCDIC 'JOBI1000'.
                return new byte[] { (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xC9, (byte)0xF1, (byte)0xF0, (byte)0xF0, (byte)0xF0 };

            default:
                return null;
        }
    }

    // Helper method.  Used to walk through the output parameter after an API call and set all of the job attribute data into our internal table.
    private void parseData(byte[] format, byte[] data) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        Converter conv = new Converter(system_.getCcsid(), system_);

        // All the formats return these.
        name_ = conv.byteArrayToString(data, 8, 10).trim();
        user_ = conv.byteArrayToString(data, 18, 10).trim();
        number_ = conv.byteArrayToString(data, 28, 6);

        realInternalJobID_ = new byte[16];
        System.arraycopy(data, 34, realInternalJobID_, 0, 16);
        char[] oldID = new char[16];
        for (int i = 0; i < 16; ++i)
        {
            oldID[i] = (char)(realInternalJobID_[i] & 0x00FF);
        }
        internalJobID_ = new String(oldID);

        status_ = conv.byteArrayToString(data, 50, 10).trim();
        type_ = conv.byteArrayToString(data, 60, 1);
        subtype_ = conv.byteArrayToString(data, 61, 1);

        setValueInternal(JOB_NAME, name_);
        setValueInternal(USER_NAME, user_);
        setValueInternal(JOB_NUMBER, number_);

        setValueInternal(INTERNAL_JOB_ID, internalJobID_);
        setValueInternal(INTERNAL_JOB_IDENTIFIER, realInternalJobID_);

        setValueInternal(JOB_STATUS, status_);
        setValueInternal(JOB_TYPE, type_);
        setValueInternal(JOB_SUBTYPE, subtype_);

        byte[] val = new byte[8];
        switch (format[5] & 0x0F)
        {
            case 1:  // Format JOBI0150.
                setAsInt(RUN_PRIORITY, BinaryConverter.byteArrayToInt(data, 64));
                setAsInt(TIME_SLICE, BinaryConverter.byteArrayToInt(data, 68));
                setAsInt(DEFAULT_WAIT_TIME, BinaryConverter.byteArrayToInt(data, 72));
                setValueInternal(ELIGIBLE_FOR_PURGE, conv.byteArrayToString(data, 76, 10));
                setValueInternal(TIME_SLICE_END_POOL, conv.byteArrayToString(data, 86, 10));
                setAsInt(CPU_TIME_USED, BinaryConverter.byteArrayToInt(data, 96));
                setAsInt(SYSTEM_POOL_ID, BinaryConverter.byteArrayToInt(data, 100));
                setAsInt(MAX_CPU_TIME, BinaryConverter.byteArrayToInt(data, 104));
                setAsInt(TEMP_STORAGE_USED, BinaryConverter.byteArrayToInt(data, 108));
                setAsInt(MAX_TEMP_STORAGE, BinaryConverter.byteArrayToInt(data, 112));
                setAsInt(THREAD_COUNT, BinaryConverter.byteArrayToInt(data, 116));
                setAsInt(MAX_THREADS, BinaryConverter.byteArrayToInt(data, 120));
                setAsLong(TEMP_STORAGE_USED_LARGE, BinaryConverter.byteArrayToUnsignedInt(data, 124));
                setAsLong(MAX_TEMP_STORAGE_LARGE, BinaryConverter.byteArrayToUnsignedInt(data, 128));
                setAsLong(CPU_TIME_USED_LARGE, BinaryConverter.byteArrayToLong(data, 136));
                break;
            case 2:  // Format JOBI0200.
                setAsInt(RUN_PRIORITY, BinaryConverter.byteArrayToInt(data, 72));
                setAsInt(SYSTEM_POOL_ID, BinaryConverter.byteArrayToInt(data, 76));
                setAsInt(CPU_TIME_USED, BinaryConverter.byteArrayToInt(data, 80));
                setAsInt(AUXILIARY_IO_REQUESTS, BinaryConverter.byteArrayToInt(data, 84));
                setAsInt(INTERACTIVE_TRANSACTIONS, BinaryConverter.byteArrayToInt(data, 88));
                setAsInt(TOTAL_RESPONSE_TIME, BinaryConverter.byteArrayToInt(data, 92));
                setValueInternal(FUNCTION_TYPE, conv.byteArrayToString(data, 96, 1));
                setValueInternal(FUNCTION_NAME, conv.byteArrayToString(data, 97, 10));
                setValueInternal(ACTIVE_JOB_STATUS, conv.byteArrayToString(data, 107, 4));
                setAsInt(CURRENT_SYSTEM_POOL_ID, BinaryConverter.byteArrayToInt(data, 136));
                setAsInt(THREAD_COUNT, BinaryConverter.byteArrayToInt(data, 140));
                setAsLong(AUXILIARY_IO_REQUESTS_LARGE, BinaryConverter.byteArrayToLong(data, 152));
                setAsLong(CPU_TIME_USED_FOR_DATABASE, BinaryConverter.byteArrayToLong(data, 160));
                setValueInternal(ACTIVE_JOB_STATUS_FOR_JOBS_ENDING, conv.byteArrayToString(data, 176, 4));
                setValueInternal(MEMORY_POOL, conv.byteArrayToString(data, 180, 10));
                setValueInternal(MESSAGE_REPLY, conv.byteArrayToString(data, 190, 1));
                break;
            case 3:  // Format JOBI0300.
                setValueInternal(JOB_QUEUE, conv.byteArrayToString(data, 62, 20));
                setValueInternal(JOB_QUEUE_PRIORITY, conv.byteArrayToString(data, 82, 2));
                setValueInternal(OUTPUT_QUEUE, conv.byteArrayToString(data, 84, 20));
                setValueInternal(OUTPUT_QUEUE_PRIORITY, conv.byteArrayToString(data, 104, 2));
                setValueInternal(PRINTER_DEVICE_NAME, conv.byteArrayToString(data, 106, 10));
                setValueInternal(SUBMITTED_BY_JOB_NAME, conv.byteArrayToString(data, 116, 10));
                setValueInternal(SUBMITTED_BY_USER, conv.byteArrayToString(data, 126, 10));
                setValueInternal(SUBMITTED_BY_JOB_NUMBER, conv.byteArrayToString(data, 136, 6));
                setValueInternal(JOB_QUEUE_STATUS, conv.byteArrayToString(data, 162, 10));
                System.arraycopy(data, 172, val, 0, 8);
                setValueInternal(JOB_QUEUE_DATE, val);
                setValueInternal(JOB_DATE, conv.byteArrayToString(data, 180, 7));
                break;
            case 4:  // Format JOBI0400.
                setValueInternal(DATE_ENTERED_SYSTEM, conv.byteArrayToString(data, 62, 13));
                setValueInternal(DATE_STARTED, conv.byteArrayToString(data, 75, 13));
                setValueInternal(ACCOUNTING_CODE, conv.byteArrayToString(data, 88, 15));
                setValueInternal(JOB_DESCRIPTION, conv.byteArrayToString(data, 103, 20));

                // Unit of work ID.
                setValueInternal(UNIT_OF_WORK_ID, conv.byteArrayToString(data, 123, 24));
                setValueInternal(LOCATION_NAME, conv.byteArrayToString(data, 123, 8));
                setValueInternal(NETWORK_ID, conv.byteArrayToString(data, 131, 8));
                setValueInternal(INSTANCE, conv.byteArrayToString(data, 139, 6));
                setValueInternal(SEQUENCE_NUMBER, conv.byteArrayToString(data, 145, 2));

                setValueInternal(MODE, conv.byteArrayToString(data, 147, 8));
                setValueInternal(INQUIRY_MESSAGE_REPLY, conv.byteArrayToString(data, 155, 10));
                setValueInternal(LOG_CL_PROGRAMS, conv.byteArrayToString(data, 165, 10));
                setValueInternal(BREAK_MESSAGE_HANDLING, conv.byteArrayToString(data, 175, 10));
                setValueInternal(STATUS_MESSAGE_HANDLING, conv.byteArrayToString(data, 185, 10));
                setValueInternal(DEVICE_RECOVERY_ACTION, conv.byteArrayToString(data, 195, 13));
                setValueInternal(KEEP_DDM_CONNECTIONS_ACTIVE, conv.byteArrayToString(data, 208, 10));
                setValueInternal(DATE_SEPARATOR, conv.byteArrayToString(data, 218, 1));
                setValueInternal(DATE_FORMAT, conv.byteArrayToString(data, 219, 4));
                setValueInternal(PRINT_TEXT, conv.byteArrayToString(data, 223, 30));
                setValueInternal(SUBMITTED_BY_JOB_NAME, conv.byteArrayToString(data, 253, 10));
                setValueInternal(SUBMITTED_BY_USER, conv.byteArrayToString(data, 263, 10));
                setValueInternal(SUBMITTED_BY_JOB_NUMBER, conv.byteArrayToString(data, 273, 6));
                setValueInternal(TIME_SEPARATOR, conv.byteArrayToString(data, 299, 1));
                setAsInt(CCSID, BinaryConverter.byteArrayToInt(data, 300));
                System.arraycopy(data, 304, val, 0, 8);
                setValueInternal(SCHEDULE_DATE_GETTER, val);
                setValueInternal(PRINT_KEY_FORMAT, conv.byteArrayToString(data, 312, 10));
                setValueInternal(SORT_SEQUENCE_TABLE, conv.byteArrayToString(data, 322, 20));
                setValueInternal(LANGUAGE_ID, conv.byteArrayToString(data, 342, 3));
                setValueInternal(COUNTRY_ID, conv.byteArrayToString(data, 345, 2));
                setValueInternal(COMPLETION_STATUS, conv.byteArrayToString(data, 347, 1));
                setValueInternal(SIGNED_ON_JOB, conv.byteArrayToString(data, 348, 1));
                setValueInternal(JOB_SWITCHES, conv.byteArrayToString(data, 349, 8));
                setValueInternal(MESSAGE_QUEUE_ACTION, conv.byteArrayToString(data, 357, 10));
                setAsInt(MESSAGE_QUEUE_MAX_SIZE, BinaryConverter.byteArrayToInt(data, 368));
                setAsInt(DEFAULT_CCSID, BinaryConverter.byteArrayToInt(data, 372));
                setValueInternal(ROUTING_DATA, conv.byteArrayToString(data, 376, 80));
                setValueInternal(DECIMAL_FORMAT, conv.byteArrayToString(data, 456, 1));
                setValueInternal(CHARACTER_ID_CONTROL, conv.byteArrayToString(data, 457, 10));
                setValueInternal(SERVER_TYPE, conv.byteArrayToString(data, 467, 30));
                setValueInternal(ALLOW_MULTIPLE_THREADS, conv.byteArrayToString(data, 497, 1));
                setValueInternal(JOB_LOG_PENDING, conv.byteArrayToString(data, 498, 1));
                setAsInt(JOB_END_REASON, BinaryConverter.byteArrayToInt(data, 500));
                setAsInt(JOB_TYPE_ENHANCED, BinaryConverter.byteArrayToInt(data, 504));
                setValueInternal(DATE_ENDED, conv.byteArrayToString(data, 508, 13));
                setValueInternal(SPOOLED_FILE_ACTION, conv.byteArrayToString(data, 522, 10));
                setValueInternal(JOB_LOG_OUTPUT, conv.byteArrayToString(data, 554, 10));
                break;
            case 5:  // Format JOBI0500.
                setAsInt(END_SEVERITY, BinaryConverter.byteArrayToInt(data, 64));
                setAsInt(LOGGING_SEVERITY, BinaryConverter.byteArrayToInt(data, 68));
                setValueInternal(LOGGING_LEVEL, conv.byteArrayToString(data, 72, 1));
                setValueInternal(LOGGING_TEXT, conv.byteArrayToString(data, 73, 10));
                break;
            case 6:  // Format JOBI0600.
                setValueInternal(JOB_SWITCHES, conv.byteArrayToString(data, 62, 8));
                setValueInternal(CONTROLLED_END_REQUESTED, conv.byteArrayToString(data, 70, 1));
                setValueInternal(SUBSYSTEM, conv.byteArrayToString(data, 71, 20));
                setValueInternal(CURRENT_USER, conv.byteArrayToString(data, 91, 10));
                setValueInternal(DBCS_CAPABLE, conv.byteArrayToString(data, 101, 1));
                setAsInt(PRODUCT_RETURN_CODE, BinaryConverter.byteArrayToInt(data, 104));
                setAsInt(USER_RETURN_CODE, BinaryConverter.byteArrayToInt(data, 108));
                setAsInt(PROGRAM_RETURN_CODE, BinaryConverter.byteArrayToInt(data, 112));
                setValueInternal(SPECIAL_ENVIRONMENT, conv.byteArrayToString(data, 116, 10));
                setValueInternal(JOB_USER_IDENTITY, conv.byteArrayToString(data, 296, 10));
                setValueInternal(JOB_USER_IDENTITY_SETTING, conv.byteArrayToString(data, 306, 1));
                setValueInternal(CLIENT_IP_ADDRESS, conv.byteArrayToString(data, 307, 15));
                break;
            case 7:  // Format JOBI0700.
                int currentLibraryExistence = BinaryConverter.byteArrayToInt(data, 72);
                setAsInt(CURRENT_LIBRARY_EXISTENCE, currentLibraryExistence);
                int numberOfSystemLibraries = BinaryConverter.byteArrayToInt(data, 64);
                setValueInternal(SYSTEM_LIBRARY_LIST, conv.byteArrayToString(data, 80, 11 * numberOfSystemLibraries));
                int offset = 80 + 11 * numberOfSystemLibraries;
                int numberOfProductLibraries = BinaryConverter.byteArrayToInt(data, 68);
                setValueInternal(PRODUCT_LIBRARIES, conv.byteArrayToString(data, offset, 11 * numberOfProductLibraries));
                offset += 11 * numberOfProductLibraries;
                if (currentLibraryExistence == 1)
                {
                    setValueInternal(CURRENT_LIBRARY, conv.byteArrayToString(data, offset, 11));
                    offset += 11;
                }
                else
                {
                    setValueInternal(CURRENT_LIBRARY, ""); // Set something so a call to get won't re-retrieve from the system.
                }
                int numberOfUserLibraries = BinaryConverter.byteArrayToInt(data, 76);
                setValueInternal(USER_LIBRARY_LIST, conv.byteArrayToString(data, offset, 11  * numberOfUserLibraries));
                break;
            case 0:  // Format JOBI1000.
                setAsLong(ELAPSED_TIME, BinaryConverter.byteArrayToLong(data, 64));
                setAsLong(ELAPSED_DISK_IO, BinaryConverter.byteArrayToLong(data, 72));
                setAsLong(ELAPSED_DISK_IO_ASYNCH, BinaryConverter.byteArrayToLong(data, 80));
                setAsLong(ELAPSED_DISK_IO_SYNCH, BinaryConverter.byteArrayToLong(data, 88));
                setAsInt(ELAPSED_INTERACTIVE_RESPONSE_TIME, BinaryConverter.byteArrayToInt(data, 96));
                setAsInt(ELAPSED_INTERACTIVE_TRANSACTIONS, BinaryConverter.byteArrayToInt(data, 100));
                setAsInt(ELAPSED_CPU_PERCENT_USED, BinaryConverter.byteArrayToInt(data, 104));
                setAsInt(ELAPSED_CPU_PERCENT_USED_FOR_DATABASE, BinaryConverter.byteArrayToInt(data, 108));
                setAsLong(ELAPSED_CPU_TIME_USED, BinaryConverter.byteArrayToLong(data, 112));
                setAsLong(ELAPSED_CPU_TIME_USED_FOR_DATABASE, BinaryConverter.byteArrayToLong(data, 120));
                setAsLong(ELAPSED_LOCK_WAIT_TIME, BinaryConverter.byteArrayToLong(data, 128));
                setAsLong(ELAPSED_PAGE_FAULTS, BinaryConverter.byteArrayToLong(data, 136));
                break;
        }
    }

    // Called when this object is de-serialized
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "De-serializing Job object.");
        in.defaultReadObject();

        // Re-initialize transient variables.
        remoteCommandLock_ = new Object();
    }

    /**
     Releases this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @see  #end
     @see  #hold
     **/
    public void release() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        StringBuffer buf = new StringBuffer();
        buf.append("QSYS/RLSJOB JOB(");  // not threadsafe
        buf.append(number_);
        buf.append('/');
        buf.append(user_);
        buf.append('/');
        buf.append(name_);
        buf.append(") DUPJOBOPT(*MSG)");
        String toRun = buf.toString();
        CommandCall cmd = getCommandCall(toRun);
        if (!cmd.run())
        {
            throw new AS400Exception(cmd.getMessageList());
        }
    }

    /**
     Removes a PropertyChangeListener.
     @param  listener  The listener.
     @see  #addPropertyChangeListener
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Removes a VetoableChangeListener.
     @param  listener  The listener.
     @see  #addVetoableChangeListener
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
    }

    /**
     Resets the measurement start time used for computing elapsed statistics.
     @see  #loadStatistics
     @see  #ELAPSED_TIME
     @see  #ELAPSED_DISK_IO
     @see  #ELAPSED_DISK_IO_ASYNCH
     @see  #ELAPSED_DISK_IO_SYNCH
     @see  #ELAPSED_INTERACTIVE_RESPONSE_TIME
     @see  #ELAPSED_INTERACTIVE_TRANSACTIONS
     @see  #ELAPSED_CPU_PERCENT_USED
     @see  #ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
     @see  #ELAPSED_CPU_TIME_USED
     @see  #ELAPSED_CPU_TIME_USED_FOR_DATABASE
     @see  #ELAPSED_LOCK_WAIT_TIME
     @see  #ELAPSED_PAGE_FAULTS
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void resetStatistics() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        retrieve(-1);
    }

    // Helper method.  Used to make the QUSRJOBI API call using the correct format based on the specified attribute.
    private void retrieve(int key) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        connect();

        // First lookup the format to use for this key.
        byte[] format = lookupFormatName(key);
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "QUSRJOBI format:", format);
        int receiverLength = lookupFormatLength(format);

        ProgramParameter[] parmList = (key == -1) ? new ProgramParameter[7] : new ProgramParameter[5];
        parmList[0] = new ProgramParameter(receiverLength);
        parmList[1] = new ProgramParameter(BinaryConverter.intToByteArray(receiverLength));
        parmList[2] = new ProgramParameter(format);
        parmList[3] = new ProgramParameter(createQualifiedJobName());
        parmList[4] = new ProgramParameter(realInternalJobID_ == null ? BLANKS16_ : realInternalJobID_);
        if (key == -1)
        {
            parmList[5] = new ProgramParameter(new byte[8]);  // Error code.
            parmList[6] = new ProgramParameter(new byte[] { (byte)0xF1 } );  // '1' to reset performance statistics.
        }

        ProgramCall pc = getProgramCall("/QSYS.LIB/QUSRJOBI.PGM", parmList); // conditionally threadsafe
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving job information for job: " + toString());
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        parseData(format, parmList[0].getOutputData());
    }

    // Helper method.  Used to convert a user-specified Date object into a String for our internal table.
    private void setAsDate(int key, Date val)
    {
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTime(val);

        StringBuffer buf = new StringBuffer();
        switch (setterKeys_.get(key))
        {
            case 7:
                int year = dateTime.get(Calendar.YEAR) - 1900;
                if (year >= 100)
                {
                    buf.append('1');
                    year -= 100;
                }
                else
                {
                    buf.append('0');
                }
                if (year < 10) buf.append('0');
                buf.append(year);

                int month = dateTime.get(Calendar.MONTH) + 1;
                if (month < 10) buf.append('0');
                buf.append(month);

                int day = dateTime.get(Calendar.DATE);
                if (day < 10) buf.append('0');
                buf.append(day);

                break;
            case 6:
                int hour = dateTime.get(Calendar.HOUR_OF_DAY);
                if (hour < 10) buf.append('0');
                buf.append(hour);

                int minute = dateTime.get(Calendar.MINUTE);
                if (minute < 10) buf.append('0');
                buf.append(minute);

                int second = dateTime.get(Calendar.SECOND);
                if (second < 10) buf.append('0');
                buf.append(second);

                break;
        }
        setValueInternal(key, buf.toString());
    }

    // Helper method.  Used to convert a user-specified Date object into a String for our internal table and the table of uncommitted changes.
    private void setAsDateToChange(int key, Date val) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAsDate(key, val);
        if (cachedChanges_ == null) cachedChanges_ = new JobHashtable();
        cachedChanges_.put(key, getValue(key));
        if (!cacheChanges_) commitChanges();
    }

    // Helper method.  Used after an API call to set the attribute values into our internal table.
    final void setAsInt(int key, int val)
    {
        setValueInternal(key, new Integer(val));
    }

    // Helper method.  Used when the user calls a setter to set the attribute value into our internal table as well as the table of uncommitted changes.
    private void setAsIntToChange(int key, int val) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAsInt(key, val);
        // Update values to set upon commit.
        if (cachedChanges_ == null) cachedChanges_ = new JobHashtable();
        cachedChanges_.put(key, getValue(key));
        if (!cacheChanges_) commitChanges();
    }

    // Helper method.  Used after an API call to set the attribute values into our internal table.
    final void setAsLong(int key, long val)
    {
        setValueInternal(key, new Long(val));
    }

    /**
     Sets how this job handles break messages.
     @param  breakMessageHandling  How this job handles break messages.  Possible values are:
     <ul>
     <li>{@link #BREAK_MESSAGE_HANDLING_NORMAL BREAK_MESSAGE_HANDLING_NORMAL} - The message queue status determines break message handling.
     <li>{@link #BREAK_MESSAGE_HANDLING_HOLD BREAK_MESSAGE_HANDLING_HOLD} - The message queue holds break messages until a user or program requests them.  The work station user uses the Display Message (DPSMSG) command to display the messages; a program must issue a Receive Message (RCVMSG) command to receive a message and handle it.
     <li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY BREAK_MESSAGE_HANDLING_NOTIFY} - The system notifies the job's message queue when a message arrives.  For interactive jobs, the audible alarm sounds if there is one, and the message-waiting light comes on.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #BREAK_MESSAGE_HANDLING
     **/
    public void setBreakMessageHandling(String breakMessageHandling) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (breakMessageHandling == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'breakMessageHandling' is null.");
            throw new NullPointerException("breakMessageHandling");
        }

        if (!breakMessageHandling.equals(BREAK_MESSAGE_HANDLING_NORMAL) && !breakMessageHandling.equals(BREAK_MESSAGE_HANDLING_HOLD) && !breakMessageHandling.equals(BREAK_MESSAGE_HANDLING_NOTIFY))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'breakMessageHandling' is not valid: " + breakMessageHandling);
            throw new ExtendedIllegalArgumentException("breakMessageHandling (" + breakMessageHandling + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(BREAK_MESSAGE_HANDLING, breakMessageHandling);
    }

    /**
     Sets the value indicating whether attribute value changes are committed immediately.  The default is true.  If any cached changes are not committed before this method is called with a value of false, those changes are lost.
     @param  cacheChanges  true to cache attribute value changes, false to commit all attribute value changes immediately.
     @see  #commitChanges
     @see  #getCacheChanges
     @see  #getValue
     @see  #setValue
     **/
    public void setCacheChanges(boolean cacheChanges)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting cache changes:", cacheChanges);
        if (!cacheChanges)
        {
            cachedChanges_ = null;
        }
        cacheChanges_ = cacheChanges;
    }

    /**
     Sets the coded character set identifier (CCSID) used for this job.
     @param  codedCharacterSetID  The coded character set identifier (CCSID) used for this job.  Possible values are:
     <ul>
     <li>{@link #CCSID_SYSTEM_VALUE CCSID_SYSTEM_VALUE} - The CCSID specified in the system value QCCSID is used.
     <li>{@link #CCSID_INITIAL_USER CCSID_INITIAL_USER} - The CCSID specified in the user profile under which this thread was initially running is used.
     <li>A coded character set identifier.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #CCSID
     **/
    public void setCodedCharacterSetID(int codedCharacterSetID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAsIntToChange(CCSID, codedCharacterSetID);
    }

    /**
     Sets the country or region identifier associated with this job.
     @param  countryID  The country or region identifier associated with this job.  Possible values are:
     <ul>
     <li>{@link #COUNTRY_ID_SYSTEM_VALUE COUNTRY_ID_SYSTEM_VALUE} - The system value QCNTRYID is used.
     <li>{@link #COUNTRY_ID_INITIAL_USER COUNTRY_ID_INITIAL_USER} - The country or region ID specified in the user profile under which this thread was initially running is used.
     <li>A country or region identifier to be used by the job.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #COUNTRY_ID
     **/
    public void setCountryID(String countryID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (countryID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'countryID' is null.");
            throw new NullPointerException("countryID");
        }
        setValue(COUNTRY_ID, countryID);
    }

    /**
     Sets the format in which dates are presented.
     @param  dateFormat  The format in which dates are presented.  Possible values are:
     <ul>
     <li>{@link #DATE_FORMAT_SYSTEM_VALUE DATE_FORMAT_SYSTEM_VALUE} - The system value QDATFMT is used.
     <li>{@link #DATE_FORMAT_YMD DATE_FORMAT_YMD} - Year, month, and day format.
     <li>{@link #DATE_FORMAT_MDY DATE_FORMAT_MDY} - Month, day, and year format.
     <li>{@link #DATE_FORMAT_DMY DATE_FORMAT_DMY} - Day, month, and year format.
     <li>{@link #DATE_FORMAT_JULIAN DATE_FORMAT_JULIAN} - Julian format (year and day).
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_FORMAT
     **/
    public void setDateFormat(String dateFormat) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dateFormat == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'dateFormat' is null.");
            throw new NullPointerException("dateFormat");
        }

        if (!dateFormat.equals(DATE_FORMAT_SYSTEM_VALUE) && !dateFormat.equals(DATE_FORMAT_YMD) && !dateFormat.equals(DATE_FORMAT_MDY) && !dateFormat.equals(DATE_FORMAT_DMY) && !dateFormat.equals(DATE_FORMAT_JULIAN))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'dateFormat' is not valid: " + dateFormat);
            throw new ExtendedIllegalArgumentException("dateFormat (" + dateFormat + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        setValue(DATE_FORMAT, dateFormat);
    }

    /**
     Sets the value used to separate days, months, and years when presenting a date.
     @param  dateSeparator  The value used to separate days, months, and years when presenting a date.  Possible values are:
     <ul>
     <li>{@link #DATE_SEPARATOR_SYSTEM_VALUE DATE_SEPARATOR_SYSTEM_VALUE} - The system value QDATSEP is used.
     <li>{@link #DATE_SEPARATOR_SLASH DATE_SEPARATOR_SLASH} - A slash (/) is used for the date separator.
     <li>{@link #DATE_SEPARATOR_DASH DATE_SEPARATOR_DASH} - A dash (-) is used for the date separator.
     <li>{@link #DATE_SEPARATOR_PERIOD DATE_SEPARATOR_PERIOD} - A period (.) is used for the date separator.
     <li>{@link #DATE_SEPARATOR_BLANK DATE_SEPARATOR_BLANK} - A blank is used for the date separator.
     <li>{@link #DATE_SEPARATOR_COMMA DATE_SEPARATOR_COMMA} - A comma (,) is used for the date separator.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DATE_SEPARATOR
     **/
    public void setDateSeparator(String dateSeparator) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (dateSeparator == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'dateSeparator' is null.");
            throw new NullPointerException("dateSeparator");
        }

        int len = setterKeys_.get(DATE_SEPARATOR);
        if (dateSeparator.length() > len)
        {
            dateSeparator = dateSeparator.substring(0, len);
        }

        setValue(DATE_SEPARATOR, dateSeparator);
    }

    /**
     Sets whether connections using distributed data management (DDM) protocols remain active when they are not being used.  The connections include APPC conversations, active TCP/IP connections, or Opti-Connect connections.  The DDM protocols are used in Distributed Relational Database Architecture (DRDA) applications, DDM applications, or DB2 Multisystem applications.
     @param  ddmConversationHandling  Whether connections using distributed data management (DDM) protocols remain active when they are not being used.  Possible values are:
     <ul>
     <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP KEEP_DDM_CONNECTIONS_ACTIVE_KEEP} - The system keeps DDM connections active when there are no users, except for the following:
     <ul>
     <li>The routing step ends on the source system.  The routing step ends when the job ends or when the job is rerouted to another routing step.
     <li>The Reclaim Distributed Data Management Conversation (RCLDDMCNV) command or the Reclaim Resources (RCLRSC) command runs.
     <li>A communications failure or an internal failure occurs.
     <li>A DRDA connection to an application server not running on an IBM i system ends.
     </ul>
     <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP KEEP_DDM_CONNECTIONS_ACTIVE_DROP} - The system ends a DDM connection when there are no users.  Examples include when an application closes a DDM file, or when a DRDA application runs an SQL DISCONNECT statement.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #KEEP_DDM_CONNECTIONS_ACTIVE
     **/
    public void setDDMConversationHandling(String ddmConversationHandling) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (ddmConversationHandling == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'ddmConversationHandling' is null.");
            throw new NullPointerException("ddmConversationHandling");
        }

        if (!ddmConversationHandling.equals(KEEP_DDM_CONNECTIONS_ACTIVE_KEEP) && !ddmConversationHandling.equals(KEEP_DDM_CONNECTIONS_ACTIVE_DROP))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ddmConversationHandling' is not valid: " + ddmConversationHandling);
            throw new ExtendedIllegalArgumentException("ddmConversationHandling (" + ddmConversationHandling + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(KEEP_DDM_CONNECTIONS_ACTIVE, ddmConversationHandling);
    }

    /**
     Sets the decimal format used for this job.
     @param  decimalFormat  The decimal format used for this job.  Possible values are:
     <ul>
     <li>{@link #DECIMAL_FORMAT_SYSTEM_VALUE DECIMAL_FORMAT_SYSTEM_VALUE} - The system value QDECFMT is used as the decimal format for this job.
     <li>{@link #DECIMAL_FORMAT_PERIOD DECIMAL_FORMAT_PERIOD} - Uses a period for a decimal point, a comma for a 3-digit grouping character, and zero-suppresses to the left of the decimal point.
     <li>{@link #DECIMAL_FORMAT_COMMA_J DECIMAL_FORMAT_COMMA_J} - Uses a comma for a decimal point, a period for a 3-digit grouping character, and zero-suppresses to the left of the decimal point.
     <li>{@link #DECIMAL_FORMAT_COMMA_I DECIMAL_FORMAT_COMMA_I} - Uses a comma for a decimal point and a period for a 3-digit grouping character.  The zero-suppression character is in the second position (rather than the first) to the left of the decimal notation.  Balances with zero values to the left of the comma are written with one leading zero (0,04).  This constant also overrides any edit codes that might suppress the leading zero.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DECIMAL_FORMAT
     **/
    public void setDecimalFormat(String decimalFormat) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (decimalFormat == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'decimalFormat' is null.");
            throw new NullPointerException("decimalFormat");
        }

        if (!decimalFormat.equals(DECIMAL_FORMAT_PERIOD) && !decimalFormat.equals(DECIMAL_FORMAT_COMMA_I) && !decimalFormat.equals(DECIMAL_FORMAT_COMMA_J) && !decimalFormat.equals(DECIMAL_FORMAT_SYSTEM_VALUE))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'decimalFormat' is not valid: " + decimalFormat);
            throw new ExtendedIllegalArgumentException("decimalFormat (" + decimalFormat + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(DECIMAL_FORMAT, decimalFormat);
    }

    /**
     Sets the default maximum time (in seconds) that a thread in the job waits for a system instruction, such as a LOCK machine interface (MI) instruction, to acquire a resource.  This default wait time is used when a wait time is not otherwise specified for a given situation.  Normally, this is the amount of time the user is willing to wait for the system before the request is ended.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not appy to subsequent routing steps.  The valid range is 1 through 9999999.  A value of -1 represents no maximum wait time (*NOMAX).
     @param  defaultWait  The default maximum time (in seconds) that a thread in the job waits for a system instruction to acquire a resource.  The value -1 means there is no maximum (*NOMAX).  The valid range is 1 through 9999999.  The value 0 is not valid.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DEFAULT_WAIT_TIME
     **/
    public void setDefaultWait(int defaultWait) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAsIntToChange(DEFAULT_WAIT_TIME, defaultWait);
    }

    /**
     Sets the action taken for interactive jobs when an I/O error occurs for the job's requesting program device.
     @param  deviceRecoveryAction  The action taken for interactive jobs when an I/O error occurs for the job's requesting program device.  Possible values are:
     <li>{@link #DEVICE_RECOVERY_ACTION_SYSTEM_VALUE DEVICE_RECOVERY_ACTION_SYSTEM_VALUE} - The value in the system value QDEVRCYACN is used as the device recovery action for this job.
     <li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE DEVICE_RECOVERY_ACTION_MESSAGE} - Signals the I/O error message to the application and lets the application program perform error recovery.
     <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE} - Disconnects the job when an I/O error occurs.  When the job reconnects, the system sends an error message to the application program, indicating the job has reconnected and that the work station device has recovered.
     <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST} - Disconnects the job when an I/O error occurs.  When the job reconnects, the system sends the End Request (ENDRQS) command to return control to the previous request level.
     <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB DEVICE_RECOVERY_ACTION_END_JOB} - Ends the job when an I/O error occurs.  A message is sent to the job's log and to the history log (QHST) indicating the job ended because of a device error.
     <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST} - Ends the job when an I/O error occurs.  There is no job log produced for the job.  The system sends a message to the QHST log indicating the job ended because of a device error.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #DEVICE_RECOVERY_ACTION
     **/
    public void setDeviceRecoveryAction(String deviceRecoveryAction) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (deviceRecoveryAction == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'deviceRecoveryAction' is null.");
            throw new NullPointerException("deviceRecoveryAction");
        }

        if (!deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_MESSAGE) && !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE) && !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST) && !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_END_JOB) && !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST) && !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_SYSTEM_VALUE))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'deviceRecoveryAction' is not valid: " + deviceRecoveryAction);
            throw new ExtendedIllegalArgumentException("deviceRecoveryAction (" + deviceRecoveryAction + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        setValue(DEVICE_RECOVERY_ACTION, deviceRecoveryAction);
    }

    /**
     Sets how the job answers inquiry messages.
     @param  inquiryMessageReply  How the job answers inquiry messages.  Possible values are:
     <ul>
     <li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED INQUIRY_MESSAGE_REPLY_REQUIRED} - The job requires an answer for any inquiry messages that occur while this job is running.
     <li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT INQUIRY_MESSAGE_REPLY_DEFAULT} - The system uses the default message reply to answer any inquiry messages issued while this job is running.  The default reply is either defined in the message description or is the default system reply.
     <li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST} - The system reply list is checked to see if there is an entry for an inquiry message issued while this job is running.  If a match occurs, the system uses the reply value for that entry.  If no entry exists for that message, the system uses an inquiry message.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #INQUIRY_MESSAGE_REPLY
     **/
    public void setInquiryMessageReply(String inquiryMessageReply) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (inquiryMessageReply == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'inquiryMessageReply' is null.");
            throw new NullPointerException("inquiryMessageReply");
        }

        if (!inquiryMessageReply.equals(INQUIRY_MESSAGE_REPLY_REQUIRED) && !inquiryMessageReply.equals(INQUIRY_MESSAGE_REPLY_DEFAULT) && !inquiryMessageReply.equals(INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'inquiryMessageReply' is not valid: " + inquiryMessageReply);
            throw new ExtendedIllegalArgumentException("inquiryMessageReply (" + inquiryMessageReply + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(INQUIRY_MESSAGE_REPLY, inquiryMessageReply);
    }

    /**
     Sets the internal job identifier.  This does not change the job on the system.  Instead, it changes the job this Job object references.  The job name must be set to "*INT" for this to be recognized.  This cannot be changed if the object has established a connection to the system.
     @param  internalJobID  The internal job identifier.
     @exception  PropertyVetoException  If the property change is vetoed.
     @deprecated  The internal job identifier should be treated as a byte array of 16 bytes.
     **/
    public void setInternalJobID(String internalJobID) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting internal job ID: " + internalJobID);
        if (internalJobID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'internalJobID' is null.");
            throw new NullPointerException("internalJobID");
        }
        if (internalJobID.length() != 16)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'internalJobID' is not valid: '" + internalJobID + "'");
            throw new ExtendedIllegalArgumentException("internalJobID (" + internalJobID + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (isConnected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'internalJobID' after connect.");
            throw new ExtendedIllegalStateException("internalJobID", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            internalJobID_ = internalJobID;
            realInternalJobID_ = new byte[16];
            for (int i = 0; i < 16; ++i)
            {
                realInternalJobID_[i] = (byte)internalJobID.charAt(i);
            }
        }
        else
        {
            String oldValue = internalJobID_;
            String newValue = internalJobID;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("internalJobID", oldValue, newValue);
            }
            internalJobID_ = internalJobID;
            realInternalJobID_ = new byte[16];
            for (int i = 0; i < 16; ++i)
            {
                realInternalJobID_[i] = (byte)internalJobID.charAt(i);
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("internalJobID", oldValue, newValue);
            }
        }
    }

    /**
     Sets the internal job identifier.  This does not change the job on the system.  Instead, it changes the job this Job object references.  The job name must be set to "*INT" for this to be recognized.  This cannot be changed if the object has established a connection to the system.
     @param  internalJobID  The 16-byte internal job identifier.
     **/
    public void setInternalJobIdentifier(byte[] internalJobID)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting internal job identifier:", internalJobID);
        if (internalJobID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'internalJobID' is null.");
            throw new NullPointerException("internalJobID");
        }
        if (internalJobID.length != 16)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'internalJobID' is not valid: " + internalJobID.length);
            throw new ExtendedIllegalArgumentException("internalJobID.length {" + internalJobID.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (isConnected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'internalJobID' after connect.");
            throw new ExtendedIllegalStateException("internalJobID", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        realInternalJobID_ = internalJobID;
        char[] oldID = new char[16];
        for (int i = 0; i < 16; ++i)
        {
            oldID[i] = (char)(internalJobID[i] & 0x00FF);
        }
        internalJobID_ = new String(oldID);
    }

    /**
     Sets an identifier assigned to the job by the system to collect resource use information for the job when job accounting is active.  The user who is changing this field must have authority to the CHGACGCDE CL command.  If the user does not have the proper authority, this field is ignored and processing continues.
     @param  jobAccountingCode  An identifier assigned to the job by the system to collect resource use information for the job when job accounting is active.  Possible values are:
     <ul>
     <li>{@link #ACCOUNTING_CODE_BLANK ACCOUNTING_CODE_BLANK} - The accounting code is changed to all blanks.
     <li>Accounting code - The 15 character accounting code used for the next accounting segment.  The accounting code may contain alphabetic or numeric characters.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #ACCOUNTING_CODE
     **/
    public void setJobAccountingCode(String jobAccountingCode) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (jobAccountingCode == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobAccountingCode' is null.");
            throw new NullPointerException("jobAccountingCode");
        }

        setValue(ACCOUNTING_CODE, jobAccountingCode);
    }

    /**
     Sets the date that is assigned to the job.  This value will only be changed for jobs whose status is *JOBQ or *ACTIVE.
     @param  jobDate  The date that is assigned to the job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_DATE
     **/
    public void setJobDate(Date jobDate) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (jobDate == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobAccountingCode' is null.");
            throw new NullPointerException("jobDate");
        }
        setAsDateToChange(JOB_DATE, jobDate);
    }

    /**
     Sets the action to take when the message queue is full.
     @param  jobMessageQueueFullAction  The action to take when the message queue is full.  Possible values are:
     <ul>
     <li>{@link #MESSAGE_QUEUE_ACTION_SYSTEM_VALUE MESSAGE_QUEUE_ACTION_SYSTEM_VALUE} - The value specified for the QJOBMSGQFL system value is used.
     <li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP MESSAGE_QUEUE_ACTION_NO_WRAP} - When the job message queue is full, do not wrap.  This action causes the job to end.
     <li>{@link #MESSAGE_QUEUE_ACTION_WRAP MESSAGE_QUEUE_ACTION_WRAP} - When the job message queue is full, wrap to the beginning and start filling again.
     <li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP MESSAGE_QUEUE_ACTION_PRINT_WRAP} - When the job message queue is full, wrap the message queue and print the messages that are being overlaid because of the wrapping.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #MESSAGE_QUEUE_ACTION
     **/
    public void setJobMessageQueueFullAction(String jobMessageQueueFullAction) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (jobMessageQueueFullAction == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobMessageQueueFullAction' is null.");
            throw new NullPointerException("jobMessageQueueFullAction");
        }

        if (!jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_NO_WRAP) && !jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_WRAP) && !jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_PRINT_WRAP) && !jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_SYSTEM_VALUE))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'jobMessageQueueFullAction' is not valid: " + jobMessageQueueFullAction);
            throw new ExtendedIllegalArgumentException("jobMessageQueueFullAction (" + jobMessageQueueFullAction + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(MESSAGE_QUEUE_ACTION, jobMessageQueueFullAction);
    }

    /**
     Sets the current setting of the job switches that are used by this job.  Specify any combination of eight 0's, 1's, or X's to change the job switch settings.  If a switch is not being changed, enter an X in the position that represents that switch.
     @param  jobSwitches  The current setting of the job switches that are used by this job.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_SWITCHES
     **/
    public void setJobSwitches(String jobSwitches) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (jobSwitches == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobSwitches' is null.");
            throw new NullPointerException("jobSwitches");
        }

        int len = setterKeys_.get(JOB_SWITCHES);
        if (jobSwitches.length() > len)
        {
            jobSwitches = jobSwitches.substring(0, len);
        }

        setValue(JOB_SWITCHES, jobSwitches);
    }

    /**
     Sets the language identifier associated with this job.  The language identifier is used when *LANGIDUNQ or *LANGIDSHR is specified on the sort sequence parameter.  If the job CCSID is 65535, this parameter is also used to determine the value of the job default CCSID.
     @param  languageID  The language identifier associated with this job.  Possible values are:
     <ul>
     <li>{@link #LANGUAGE_ID_SYSTEM_VALUE LANGUAGE_ID_SYSTEM_VALUE} - The system value QLANGID is used.
     <li>{@link #LANGUAGE_ID_INITIAL_USER LANGUAGE_ID_INITIAL_USER} - The language ID specified in the user profile under which this thread was initially running is used.
     <li>The language identifier.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LANGUAGE_ID
     **/
    public void setLanguageID(String languageID) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (languageID == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'languageID' is null.");
            throw new NullPointerException("languageID");
        }
        setValue(LANGUAGE_ID, languageID);
    }

    /**
     Sets whether or not commands are logged for CL programs that are run.
     @param  loggingCLPrograms  The value indicating whether or not commands are logged for CL programs that are run.  Possible values are:
     <ul>
     <li>{@link #LOG_CL_PROGRAMS_YES LOG_CL_PROGRAMS_YES} - Commands are logged for CL programs that are run.
     <li>{@link #LOG_CL_PROGRAMS_NO LOG_CL_PROGRAMS_NO} - Commands are not logged for CL programs that are run.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOG_CL_PROGRAMS
     **/
    public void setLoggingCLPrograms(String loggingCLPrograms) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (loggingCLPrograms == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'loggingCLPrograms' is null.");
            throw new NullPointerException("loggingCLPrograms");
        }
        if (!loggingCLPrograms.equals(LOG_CL_PROGRAMS_YES) && !loggingCLPrograms.equals(LOG_CL_PROGRAMS_NO))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'loggingCLPrograms' is not valid: " + loggingCLPrograms);
            throw new ExtendedIllegalArgumentException("loggingCLPrograms (" + loggingCLPrograms + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(LOG_CL_PROGRAMS, loggingCLPrograms);
    }

    /**
     Sets what type of information is logged.
     @param  loggingLevel  A value indicating what type of information is logged.  Possible values are:
     <ul>
     <li>0 - No messages are logged.
     <li>1 - All messages sent to the job's external message queue with a severity greater than or equal to the message logging severity are logged.  This includes the indication of job start, job end, and job completion status.
     <li>2 - The following information is logged:
     <ul>
     <li>Logging level 1 information.
     <li>Request messages that result in a high-level message with a severity code greater than or equal to the logging severity cause the request message and all associated messages to be logged.  A high-level message is one that is sent to the program message queue of the program that receives the request message.  For example, QCMD is an IBM-supplied request processing program that receives request messages.
     </ul>
     <li>3 - The following information is logged:
     <ul>
     <li>Logging level 1 and 2 information is logged.
     <li>All request messages are logged.
     <li>Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS attribute and the log attribute of the CL program.
     </ul>
     <li>4 - The following information is logged:
     <ul>
     <li>All request messages and all messages with a severity greater than or equal to the message logging severity, including trace messages.
     <li>Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS attribute and the log attribute of the CL program.
     </ul>
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOGGING_LEVEL
     **/
    public void setLoggingLevel(int loggingLevel) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (loggingLevel < 0 || loggingLevel > 4)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'loggingLevel' is not valid:", loggingLevel);
            throw new ExtendedIllegalArgumentException("loggingLevel " + loggingLevel + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        setValue(LOGGING_LEVEL, Integer.toString(loggingLevel));
    }

    /**
     Sets the severity level that is used in conjunction with the logging level to determine which error messages are logged in the job log.  The values range from 00 through 99.
     @param  loggingSeverity  The severity level that is used in conjunction with the logging level to determine which error messages are logged in the job log.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOGGING_SEVERITY
     **/
    public void setLoggingSeverity(int loggingSeverity) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAsIntToChange(LOGGING_SEVERITY, loggingSeverity);
    }

    /**
     Sets the level of message text that is written in the job log when a message is logged according to the LOGGING_LEVEL and LOGGING_SEVERITY.
     @param  loggingText  The level of message text that is written in the job log when a message is logged according to the LOGGING_LEVEL and LOGGING_SEVERITY.  Possible values are:
     <ul>
     <li>{@link #LOGGING_TEXT_MESSAGE LOGGING_TEXT_MESSAGE} - Only the message text is written to the job log.
     <li>{@link #LOGGING_TEXT_SECLVL LOGGING_TEXT_SECLVL} - Both the message text and the message help (cause and recovery) of the error message are written to the job log.
     <li>{@link #LOGGING_TEXT_NO_LIST LOGGING_TEXT_NO_LIST} - If the job ends normally, no job log is produced.  If the job ends abnormally (the job end code is 20 or higher), a job log is produced.  The messages that appear in the job log contain both the message text and the message.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #LOGGING_TEXT
     **/
    public void setLoggingText(String loggingText) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (loggingText == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'loggingText' is null.");
            throw new NullPointerException("loggingText");
        }

        if (!loggingText.equals(LOGGING_TEXT_MESSAGE) && !loggingText.equals(LOGGING_TEXT_SECLVL) && !loggingText.equals(LOGGING_TEXT_NO_LIST))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'loggingText' is not valid: " + loggingText);
            throw new ExtendedIllegalArgumentException("loggingText (" + loggingText + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(LOGGING_TEXT, loggingText);
    }

    /**
     Sets the job name.  This does not change the name of the actual server job.  Instead, it changes the job this Job object references.   This cannot be changed if the object has already established a connection to the system.
     @param  name  The job name.  Specify JOB_NAME_CURRENT to indicate the job this program running in, or JOB_NAME_INTERNAL to indicate that the job is specified using the internal job identifier.
     @exception  PropertyVetoException  If the property change is vetoed.
     **/
    public void setName(String name) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting name: " + name);
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

        if (isConnected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'name' after connect.");
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            name_ = name;
            setValueInternal(JOB_NAME, name);
        }
        else
        {
            String oldValue = name_;
            String newValue = name;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("name", oldValue, newValue);
            }
            name_ = newValue;
            setValueInternal(JOB_NAME, name);
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("name", oldValue, newValue);
            }
        }
    }

    /**
     Sets the job number.  This does not change the name of the actual server job.  Instead, it changes the job this Job object references.  This cannot be changed if the object has already established a connection to the system.
     @param  number  The job number.  This must be JOB_NUMBER_BLANK if the job name is JOB_NAME_CURRENT.
     @exception  PropertyVetoException  If the property change is vetoed.
     **/
    public void setNumber(String number) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting number: " + number);
        if (number == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'number' is null.");
            throw new NullPointerException("number");
        }
        if (number.length() > 6)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'number' is not valid: '" + number + "'");
            throw new ExtendedIllegalArgumentException("number (" + number + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (isConnected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'number' after connect.");
            throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            number_ = number;
            setValueInternal(JOB_NUMBER, number);
        }
        else
        {
            String oldValue = number_;
            String newValue = number;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("number", oldValue, newValue);
            }
            number_ = number;
            setValueInternal(JOB_NUMBER, number);
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("number", oldValue, newValue);
            }
        }
    }

    /**
     Sets the fully qualified integrated file system path name of the default output queue that is used for spooled output produced by this job.  The default output queue is only for spooled printer files that specify *JOB for the output queue.
     @param  outputQueue  The fully qualified integrated file system path name of the default output queue that is used for spooled output produced by this job.  Possible values are:
     <ul>
     <li>{@link #OUTPUT_QUEUE_DEVICE OUTPUT_QUEUE_DEVICE} - The device specified on the Create Printer File (CRTPRTF), Change Printer File (CHGPRTF), or Override with Printer File (OVRPRTF) commands is used.
     <li>{@link #OUTPUT_QUEUE_WORK_STATION OUTPUT_QUEUE_WORK_STATION} - The default output queue that is used with this job is the output queue that is assigned to the work station associated with the job at the time the job is started.
     <li>{@link #OUTPUT_QUEUE_INITIAL_USER OUTPUT_QUEUE_INITIAL_USER} - The output queue name specified in the user profile under which this thread was initially running is used.
     <li>The fully qualified integrated file system path name of the output queue.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #OUTPUT_QUEUE
     @see  QSYSObjectPathName
     **/
    public void setOutputQueue(String outputQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (outputQueue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'outputQueue' is null.");
            throw new NullPointerException("outputQueue");
        }

        if (!outputQueue.startsWith("*"))
        {
            QSYSObjectPathName path = new QSYSObjectPathName(outputQueue, "OUTQ");
            StringBuffer buf = new StringBuffer();
            String name = path.getObjectName();
            buf.append(name);
            for (int i = name.length(); i < 10; ++i)
            {
                buf.append(' ');
            }
            buf.append(path.getLibraryName());
            setValue(OUTPUT_QUEUE, buf.toString());
        }
        else
        {
            setValue(OUTPUT_QUEUE, outputQueue);
        }
    }

    /**
     Sets the output priority for spooled output files that this job produces.  The highest priority is 0, and the lowest is 9.
     @param  outputQueuePriority  The output priority for spooled output files that this job produces.  The valid values are a range from 1 to 9.  The output priority specified cannot be higher than the priority specified in the user profile under which the job is running.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #OUTPUT_QUEUE_PRIORITY
     **/
    public void setOutputQueuePriority(int outputQueuePriority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setValue(OUTPUT_QUEUE_PRIORITY, Integer.toString(outputQueuePriority));
    }

    /**
     Sets the printer device used for printing output from this job.
     @param  printerDeviceName  The printer device used for printing output from this job.  ssible values are:
     <ul>
     <li>{@link #PRINTER_DEVICE_NAME_SYSTEM_VALUE PRINTER_DEVICE_NAME_SYSTEM_VALUE} - The value in the system value QPRTDEV is used as the printer device.
     <li>{@link #PRINTER_DEVICE_NAME_WORK_STATION PRINTER_DEVICE_NAME_WORK_STATION} - The default printer device used with this job is the printer device assigned to the work station that is associated with the job.
     <li>{@link #PRINTER_DEVICE_NAME_INITIAL_USER PRINTER_DEVICE_NAME_INITIAL_USER} - The printer device name specified in the user profile under which this thread was initially running is used.
     <li>The name of the printer device that is used with this job.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRINTER_DEVICE_NAME
     **/
    public void setPrinterDeviceName (String printerDeviceName) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (printerDeviceName == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'printerDeviceName' is null.");
            throw new NullPointerException("printerDeviceName");
        }

        int len = setterKeys_.get(PRINTER_DEVICE_NAME);
        if (printerDeviceName.length() > len)
        {
            printerDeviceName = printerDeviceName.substring(0, len);
        }

        setValue(PRINTER_DEVICE_NAME, printerDeviceName);
    }

    /**
     Sets whether border and header information is provided when the Print key is pressed.
     @param  printKeyFormat  Whether border and header information is provided when the Print key is pressed.  Possible values are:
     <ul>
     <li>{@link #PRINT_KEY_FORMAT_SYSTEM_VALUE PRINT_KEY_FORMAT_SYSTEM_VALUE} - The value specified on the system value QPRTKEYFMT determines whether header or border information is printed.
     <li>{@link #PRINT_KEY_FORMAT_NONE PRINT_KEY_FORMAT_NONE} - The border and header information is not included with output from the Print key.
     <li>{@link #PRINT_KEY_FORMAT_BORDER PRINT_KEY_FORMAT_BORDER} - The border information is included with output from the Print key.
     <li>{@link #PRINT_KEY_FORMAT_HEADER PRINT_KEY_FORMAT_HEADER} - The header information is included with output from the Print key.
     <li>{@link #PRINT_KEY_FORMAT_ALL PRINT_KEY_FORMAT_ALL} - The border and header information is included with output from the Print key.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRINT_KEY_FORMAT
     **/
    public void setPrintKeyFormat(String printKeyFormat) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (printKeyFormat == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'printKeyFormat' is null.");
            throw new NullPointerException("printKeyFormat");
        }

        if (!printKeyFormat.equals(PRINT_KEY_FORMAT_NONE) && !printKeyFormat.equals(PRINT_KEY_FORMAT_BORDER) && !printKeyFormat.equals(PRINT_KEY_FORMAT_HEADER) && !printKeyFormat.equals(PRINT_KEY_FORMAT_ALL) && !printKeyFormat.equals(PRINT_KEY_FORMAT_SYSTEM_VALUE))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'printKeyFormat' is not valid: " + printKeyFormat);
            throw new ExtendedIllegalArgumentException("printKeyFormat (" + printKeyFormat + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(PRINT_KEY_FORMAT, printKeyFormat);
    }

    /**
     Sets the line of text (if any) that is printed at the bottom of each page of printed output for the job.
     @param  printText  The line of text (if any) that is printed at the bottom of each page of printed output for the job.  Possible values are:
     <ul>
     <li>{@link #PRINT_TEXT_SYSTEM_VALUE PRINT_TEXT_SYSTEM_VALUE} - The system value QPRTTXT is used.
     <li>{@link #PRINT_TEXT_BLANK PRINT_TEXT_BLANK} - No text is printed on printed output.
     <li>The character string that is printed at the bottom of each page.  A maximum of 30 characters can be entered.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #PRINT_TEXT
     **/
    public void setPrintText (String printText) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (printText == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'printText' is null.");
            throw new NullPointerException("printText");
        }
        setValue(PRINT_TEXT, printText);
    }

    /**
     Sets the value indicating whether or not the job is eligible to be moved out of main storage and put into auxiliary storage at the end of a time slice or when entering a long wait (such as waiting for a work station user's response).  This attribute is ignored when more than one thread is active within the job.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not apply to subsequent routing steps.
     @param  purge  true to indicate that the job is eligible to be moved out of main storage and put into auxiliary storage at the end of a time slice or when entering a long wait; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #ELIGIBLE_FOR_PURGE
     **/
    public void setPurge(boolean purge) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setValueInternal(ELIGIBLE_FOR_PURGE, purge ? ELIGIBLE_FOR_PURGE_YES : ELIGIBLE_FOR_PURGE_NO);
    }

    /**
     Sets the fully qualified integrated file system path name of the job queue that the job is to be on.  This value is valid for jobs whose status is *JOBQ.  For jobs with a status of *OUTQ or *ACTIVE, an error will be signaled.
     @param  jobQueue  The fully qualified integrated file system path name of the job queue that the job is to be on.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_QUEUE
     @see  QSYSObjectPathName
     **/
    public void setQueue(String jobQueue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (jobQueue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'jobQueue' is null.");
            throw new NullPointerException("jobQueue");
        }

        if (!jobQueue.startsWith("*"))
        {
            QSYSObjectPathName path = new QSYSObjectPathName(jobQueue, "JOBQ");
            StringBuffer buf = new StringBuffer();
            String name = path.getObjectName();
            buf.append(name);
            for (int i = name.length(); i < 10; ++i)
            {
                buf.append(' ');
            }
            buf.append(path.getLibraryName());
            setValue(JOB_QUEUE, buf.toString());
        }
        else
        {
            setValue(JOB_QUEUE, jobQueue);
        }
    }

    /**
     Sets the scheduling priority of the job compared to other jobs on the same job queue.  The highest priority is 0 and the lowest is 9.  This value is valid for jobs whose status is *JOBQ or *ACTIVE.  For jobs with a status of *OUTQ, an error will be signaled.
     @param  queuePriority  The scheduling priority of the job compared to other jobs on the same job queue.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #JOB_QUEUE_PRIORITY
     **/
    public void setQueuePriority(int queuePriority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setValue(JOB_QUEUE_PRIORITY, Integer.toString(queuePriority));
    }

    /**
     Sets the priority at which the job competes for the processing unit relative to the other jobs that are active at the same time.  The run priority ranges from 1 (highest priority) to 99 (lowest priority).  This value represents the relative (not absolute) importance of the job or thread.  For example, a run priority of 25 is not twice as important as a run priority of 50.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not apply to subsequent routing steps.
     @param  runPriority  The run priority of the job is changed.  The range of values is 1 (highest priority) to 99 (lowest priority).  The value may never be higher than the run priority for the job in which the thread is running.  If a priority higher than the job's is entered, an error is returned.  Changing the run priority of the job affects the run priorities of all threads within the job.  For example, the job is running at priority 10, thread A within the job is running at priority 10, and thread B within the job is running at priority 15.  The priority of the job is changed to 20.  The priority of thread A would then be adjusted to 20 and the priority of thread B would be adjusted to 25.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #RUN_PRIORITY
     **/
    public void setRunPriority(int runPriority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAsIntToChange(RUN_PRIORITY, runPriority);
    }

    /**
     Sets the date and time the job is scheduled to become active.
     @param  scheduleDate  The date and time the job is scheduled to become active.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SCHEDULE_DATE
     **/
    public void setScheduleDate(Date scheduleDate) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (scheduleDate == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'scheduleDate' is null.");
            throw new NullPointerException("scheduleDate");
        }

        // The schedule date is weird.
        // Use SCHEDULE_DATE and SCHEDULE_TIME to set it.
        // Use SCHEDULE_DATE_GETTER to retrieve it.

        Calendar dateTime = Calendar.getInstance();
        dateTime.clear();
        dateTime.setTime(scheduleDate);

        StringBuffer buf = new StringBuffer();

        int year = dateTime.get(Calendar.YEAR) - 1900;
        if (year >= 100)
        {
            buf.append('1');
            year -= 100;
        }
        else
        {
            buf.append('0');
        }
        if (year < 10) buf.append('0');
        buf.append(year);

        int month = dateTime.get(Calendar.MONTH) + 1;
        if (month < 10) buf.append('0');
        buf.append(month);

        int day = dateTime.get(Calendar.DATE);
        if (day < 10) buf.append('0');
        buf.append(day);

        String dateToSet = buf.toString();

        buf = new StringBuffer();
        int hour = dateTime.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) buf.append('0');
        buf.append(hour);

        int minute = dateTime.get(Calendar.MINUTE);
        if (minute < 10) buf.append('0');
        buf.append(minute);

        int second = dateTime.get(Calendar.SECOND);
        if (second < 10) buf.append('0');
        buf.append(second);

        String timeToSet = buf.toString();

        setValue(SCHEDULE_DATE, dateToSet);
        setValue(SCHEDULE_TIME, timeToSet);
        setValueInternal(SCHEDULE_DATE_GETTER, null);
    }

    /**
     Sets the date the job is scheduled to become active.
     @param  scheduleDate  The date the job is scheduled to become active, in the format <em>CYYMMDD</em>, where <em>C</em> is the century, <em>YY</em> is the year, <em>MM</em> is the month, and <em>DD</em> is the day.  A 0 for the century flag indicates years 19<em>xx</em> and a 1 indicates years 20<em>xx</em>.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SCHEDULE_DATE
     **/
    public void setScheduleDate(String scheduleDate) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (scheduleDate == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'scheduleDate' is null.");
            throw new NullPointerException("scheduleDate");
        }
        if (scheduleDate.length() != 7)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'scheduleDate' is not valid: " + scheduleDate);
            throw new ExtendedIllegalArgumentException("scheduleDate (" + scheduleDate + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        setValue(SCHEDULE_DATE, scheduleDate);
        setValueInternal(SCHEDULE_DATE_GETTER, null);
    }

    /**
     Sets the date and time the job is scheduled to become active.
     @param  scheduleTime  The date and time the job is scheduled to become active.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SCHEDULE_DATE
     **/
    public void setScheduleTime(Date scheduleTime) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (scheduleTime == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'scheduleTime' is null.");
            throw new NullPointerException("scheduleTime");
        }
        setScheduleDate(scheduleTime);
    }

    /**
     Sets the time the job is scheduled to become active.
     @param  scheduleTime  The time the job is scheduled to become active, in the format <em>HHMMSS</em>, where <em>HH</em> are the hours, <em>MM</em> are the minutes, and <em>SS</em> are the seconds.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SCHEDULE_DATE
     **/
    public void setScheduleTime(String scheduleTime) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (scheduleTime == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'scheduleTime' is null.");
            throw new NullPointerException("scheduleTime");
        }
        if (scheduleTime.length() != 6)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'scheduleTime' is not valid: " + scheduleTime);
            throw new ExtendedIllegalArgumentException("scheduleTime (" + scheduleTime + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        setValue(SCHEDULE_TIME, scheduleTime);
        setValueInternal(SCHEDULE_DATE_GETTER, null);
    }

    /**
     Sets the name of the sort sequence table associated with this job..
     @param  sortSequenceTable  The name of the sort sequence table associated with this job.  Possible values are:
     <ul>
     <li>{@link #SORT_SEQUENCE_TABLE_SYSTEM_VALUE SORT_SEQUENCE_TABLE_SYSTEM_VALUE} - The system value QSRTSEQ is used.
     <li>{@link #SORT_SEQUENCE_TABLE_INITIAL_USER SORT_SEQUENCE_TABLE_INITIAL_USER} - The sort sequence table specified in the user profile under which this thread was initially running is used.
     <li>{@link #SORT_SEQUENCE_TABLE_NONE SORT_SEQUENCE_TABLE_NONE} - No sort sequence table is used.  The hexadecimal values of the characters are used to determine the sort sequence.
     <li>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT} - The sort sequence table used can contain the same weight for multiple characters, and it is the shared weight sort table associated with the language specified in the LANGUAGE_ID attribute.
     <li>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT} - The sort sequence table used must contain a unique weight for each character in the code page, and it is the unique weight sort table associated with the language specified in the LANGUAGE_ID parameter.
     <li>The fully qualified integrated file system path name of the sort sequence table associated with this job.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #SORT_SEQUENCE_TABLE
     @see  QSYSObjectPathName
     **/
    public void setSortSequenceTable(String sortSequenceTable) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (sortSequenceTable == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'scheduleTime' is null.");
            throw new NullPointerException("sortSequenceTable");
        }

        if (!sortSequenceTable.startsWith("*"))
        {
            QSYSObjectPathName path = new QSYSObjectPathName(sortSequenceTable, "FILE");
            StringBuffer buf = new StringBuffer();
            String name = path.getObjectName();
            buf.append(name);
            for (int i = name.length(); i < 10; ++i)
            {
                buf.append(' ');
            }
            buf.append(path.getLibraryName());
            setValue(SORT_SEQUENCE_TABLE, buf.toString());
        }
        else
        {
            setValue(SORT_SEQUENCE_TABLE, sortSequenceTable);
        }
    }

    /**
     Sets the value which indicates whether status messages are displayed for this job.
     @param  statusMessageHandling  The value which indicates whether status messages are displayed for this job.  Possible values are:
     <ul>
     <li>{@link #STATUS_MESSAGE_HANDLING_SYSTEM_VALUE STATUS_MESSAGE_HANDLING_SYSTEM_VALUE} - The system value QSTSMSG is used.
     <li>{@link #STATUS_MESSAGE_HANDLING_INITIAL_USER STATUS_MESSAGE_HANDLING_INITIAL_USER} - The status message handling that is specified in the user profile under which this thread was initially running is used.
     <li>{@link #STATUS_MESSAGE_HANDLING_NONE STATUS_MESSAGE_HANDLING_NONE} - This job does not display status messages.
     <li>{@link #STATUS_MESSAGE_HANDLING_NORMAL STATUS_MESSAGE_HANDLING_NORMAL} - This job displays status messages.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #STATUS_MESSAGE_HANDLING
     **/
    public void setStatusMessageHandling(String statusMessageHandling) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (statusMessageHandling == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'statusMessageHandling' is null.");
            throw new NullPointerException("statusMessageHandling");
        }
        setValue(STATUS_MESSAGE_HANDLING, statusMessageHandling);
    }

    /**
     Sets the system.  This cannot be changed if the object has established a connection to the system.
     @param  system  The system.
     @exception  PropertyVetoException  If the property change is vetoed.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (isConnected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            system_ = system;
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
            }
            system_ = system;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }

    /**
     Sets the value used to separate hours, minutes, and seconds when presenting a time.
     @param  timeSeparator  The value used to separate hours, minutes, and seconds when presenting a time.  Possible values are:
     <ul>
     <li>{@link #TIME_SEPARATOR_SYSTEM_VALUE TIME_SEPARATOR_SYSTEM_VALUE} - The time separator specified in the system value QTIMSEP is used.
     <li>{@link #TIME_SEPARATOR_COLON TIME_SEPARATOR_COLON} - A colon (:) is used for the time separator.
     <li>{@link #TIME_SEPARATOR_PERIOD TIME_SEPARATOR_PERIOD} - A period (.) is used for the time separator.
     <li>{@link #TIME_SEPARATOR_BLANK TIME_SEPARATOR_BLANK} - A blank is used for the time separator.
     <li>{@link #TIME_SEPARATOR_COMMA TIME_SEPARATOR_COMMA} - A comma (,) is used for the time separator.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #TIME_SEPARATOR
     **/
    public void setTimeSeparator(String timeSeparator) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (timeSeparator == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'timeSeparator' is null.");
            throw new NullPointerException("timeSeparator");
        }

        int len = setterKeys_.get(TIME_SEPARATOR);
        if (timeSeparator.length() > len)
        {
            timeSeparator = timeSeparator.substring(0, len);
        }
        setValue(TIME_SEPARATOR, timeSeparator);
    }

    /**
     Sets the maximum amount of processor time (in milliseconds) given to each thread in this job before other threads in this job and in other jobs are given the opportunity to run.  The time slice establishes the amount of time needed by a thread in this job to accomplish a meaningful amount of processing.  At the end of the time slice, the thread might be put in an inactive state so that other threads can become active in the storage pool.  If the job consists of multiple routing steps, a change to this attribute during a routing step does not apply to subsequent routing steps.  Valid values range from 1 through 9,999,999 milliseconds (that is, 9999.999 seconds).  Although you can specify a value of less than 8, the system takes a minimum of 8 milliseconds to run a process.  If you display a job's run attributes, the time slice value is never less than 8.
     @param  timeSlice  The maximum amount of processor time (in milliseconds) given to each thread in this job before other threads in this job and in other jobs are given the opportunity to run.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #TIME_SLICE
     **/
    public void setTimeSlice(int timeSlice) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        setAsIntToChange(TIME_SLICE, timeSlice);
    }

    /**
     Sets the value which indicates whether a thread in an interactive job moves to another main storage pool at the end of its time slice.
     @param  timeSliceEndPool  The value which indicates whether a thread in an interactive job moves to another main storage pool at the end of its time slice.  Possible values are:
     <ul>
     <li>{@link #TIME_SLICE_END_POOL_SYSTEM_VALUE TIME_SLICE_END_POOL_SYSTEM_VALUE} - The value in the system value QTSEPPOOL is used.
     <li>{@link #TIME_SLICE_END_POOL_NONE TIME_SLICE_END_POOL_NONE} - A thread in the job does not move to another main storage pool when it reaches the end of its time slice.
     <li>{@link #TIME_SLICE_END_POOL_BASE TIME_SLICE_END_POOL_BASE} - A thread in the job moves to the base pool when it reaches the end of its time slice.
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #TIME_SLICE_END_POOL
     **/
    public void setTimeSliceEndPool(String timeSliceEndPool) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (timeSliceEndPool == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'timeSliceEndPool' is null.");
            throw new NullPointerException("timeSliceEndPool");
        }
        setValue(TIME_SLICE_END_POOL, timeSliceEndPool);
    }

    /**
     Sets the user name.  This does not change the name of the actual server job.  Instead, it changes the job this Job object references.  This cannot be changed if the object has already established a connection to the system.
     @param  user  The user name.  This must be USER_NAME_BLANK if the job name is JOB_NAME_CURRENT.
     @exception  PropertyVetoException  If the property change is vetoed.
     **/
    public void setUser(String user) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting user: " + user);
        if (user == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'user' is null.");
            throw new NullPointerException("user");
        }
        if (user.length() > 10)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'user' is not valid: '" + user + "'");
            throw new ExtendedIllegalArgumentException("user (" + user + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        if (isConnected_)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'user' after connect.");
            throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            user_ = user;
            setValueInternal(USER_NAME, user);
        }
        else
        {
            String oldValue = user_;
            String newValue = user;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("user", oldValue, newValue);
            }
            user_ = user;
            setValueInternal(USER_NAME, user);
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("user", oldValue, newValue);
            }
        }
    }

    /**
     Sets a value for a job attribute.  If caching is off, the value is immediately sent to the system.  If caching is on, call {@link #commitChanges commitChanges()} to send the uncommitted values to the system.
     @param  attribute  The job attribute to change.
     @param  value  The new value of the attribute.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #commitChanges
     @see  #getValue
     **/
    public void setValue(int attribute, Object value) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting value, attribute: " + attribute + ", value: " + value);
        if (attribute < 0 || isReadOnly(attribute))
        {
            Trace.log(Trace.ERROR, "Value of parameter 'attribute' is not valid:", attribute);
            throw new ExtendedIllegalArgumentException("attribute (" + attribute + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if (attribute == SCHEDULE_DATE || attribute == SCHEDULE_TIME)
        {
            setValueInternal(SCHEDULE_DATE_GETTER, null);
        }

        // Update values to set upon commit.
        if (cachedChanges_ == null) cachedChanges_ = new JobHashtable();
        cachedChanges_.put(attribute, value);
        if (!cacheChanges_) commitChanges();

        values_.put(attribute, value);  // Update getter values.
    }

    // Helper method.  Used to set a value into our internal table.
    // We technically don't need a method for this, but in case in the future we need to do anything besides just putting the value into the hashtable, we can add that logic here.
    final void setValueInternal(int key, Object value)
    {
        values_.put(key, value);
    }

    /**
     Returns the string representation of this Job in the format "number/user/name", or "" if any of these attributes is null.
     @return  The string representation.
     **/
    public String toString()
    {
        if (number_ == null || user_ == null || name_ == null)
        {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(number_);
        buf.append('/');
        buf.append(user_);
        buf.append('/');
        buf.append(name_);
        return buf.toString();
    }
}
