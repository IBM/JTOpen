///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Job.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.text.DateFormat;
import java.text.ParseException;

/**
The Job class represents an OS/400 job.  In order to access a job,
the system and either the job name, user name, and job number or
internal job identifier need to be set.  A valid combination of
these must be set by getting or setting any of the job's attributes.

<p>Some of the attributes have associated get and set methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the IBM Toolbox for Java.  The complete
set of attribute values can be accessed using the public constants.

<p>Note: Most of the "getter" methods will either go to the system to retrieve the job attribute, or will return a cached value if the attribute was previously retrieved or previously set by {@link #setValue setValue()} or one of the other setter methods.  Use {@link #loadInformation loadInformation()} to refresh the attributes from the system.
<br>For example:
<pre>
Job job = new Job(system, jobName, userName, jobNumber);
while (job.getStatus().equals(Job.JOB_STATUS_ACTIVE))
{
  Thread.sleep(1000);   // wait a while
  job.loadInformation(); // refresh the attribute values
}
System.out.println("Job status is: " + job.getStatus());
</pre>

<p>Note: To obtain information about the job in which an
OS/400 program or command runs, do something like the following:
<pre>
AS400 sys = new AS400();
ProgramCall pgm = new ProgramCall(sys);
pgm.setThreadSafe(true); // indicates the program is to be run on-thread
String jobNumber = pgm.getServerJob().getNumber();
</pre>

@see com.ibm.as400.access.JobList
@see com.ibm.as400.access.CommandCall#getServerJob
@see com.ibm.as400.access.ProgramCall#getServerJob
**/
public class Job
implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  static final long serialVersionUID = 6L;

  private static final byte[] BLANKS16_ = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
                                                       0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };

//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

  private static final AS400Bin4 bin4_ = new AS400Bin4();

  private boolean cacheChanges_ = true;

  private boolean isConnected_;

  private final JobHashtable values_ = new JobHashtable();
  private JobHashtable cachedChanges_ = null;

  private String name_, user_, number_, status_, type_, subtype_;
  private AS400 system_;
  private String internalJobID_;
  private byte[] realInternalJobID_;

  private transient PropertyChangeSupport propertyChangeSupport_;
  private transient VetoableChangeSupport vetoableChangeSupport_;

  /**
   * Job attribute representing the identifier assigned to a job by the system
   * to collect resource use information for the job when job accounting is
   * active.
   * Special values include:
   * <UL>
   * <LI>{@link #ACCOUNTING_CODE_BLANK ACCOUNTING_CODE_BLANK}
   * </UL>
   * <P>Type: String
   * @see #getJobAccountingCode
   * @see #setJobAccountingCode
  **/
  public static final int ACCOUNTING_CODE = 1001;

  /**
   * Constant indicating the accounting code is changed to all blanks.
   * @see #ACCOUNTING_CODE
  **/
  public static final String ACCOUNTING_CODE_BLANK = "*BLANK";

  /**
   * Job attribute representing the active status of the initial thread of a job.
   * Possible values are:
   * <UL>
   * <LI>{@link #ACTIVE_JOB_STATUS_NONE ACTIVE_JOB_STATUS_NONE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_CHECKPOINT ACTIVE_JOB_STATUS_WAIT_CHECKPOINT}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_CONDITION ACTIVE_JOB_STATUS_WAIT_CONDITION}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_CPI_COMM ACTIVE_JOB_STATUS_WAIT_CPI_COMM}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DEQUEUE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DEQUEUE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DEQUEUE ACTIVE_JOB_STATUS_WAIT_DEQUEUE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DISKETTE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DISKETTE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DISKETTE ACTIVE_JOB_STATUS_WAIT_DISKETTE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DELAYED ACTIVE_JOB_STATUS_WAIT_DELAYED}
   * <LI>{@link #ACTIVE_JOB_STATUS_DISCONNECTED ACTIVE_JOB_STATUS_DISCONNECTED}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DISPLAY_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DISPLAY_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DISPLAY ACTIVE_JOB_STATUS_WAIT_DISPLAY}
   * <LI>{@link #ACTIVE_JOB_STATUS_ENDED ACTIVE_JOB_STATUS_ENDED}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF}
   * <LI>{@link #ACTIVE_JOB_STATUS_ENDING ACTIVE_JOB_STATUS_ENDING}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_EVENT ACTIVE_JOB_STATUS_WAIT_EVENT}
   * <LI>{@link #ACTIVE_JOB_STATUS_SUSPENDED ACTIVE_JOB_STATUS_SUSPENDED}
   * <LI>{@link #ACTIVE_JOB_STATUS_HELD ACTIVE_JOB_STATUS_WAIT_HELD}
   * <LI>{@link #ACTIVE_JOB_STATUS_HELD_THREAD ACTIVE_JOB_STATUS_HELD_THREAD}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_ICF_FILE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_ICF_FILE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_ICF_FILE ACTIVE_JOB_STATUS_WAIT_ICF_FILE}
   * <LI>{@link #ACTIVE_JOB_STATUS_INELIGIBLE ACTIVE_JOB_STATUS_WAIT_INELIGIBLE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_JAVA_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_JAVA_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_JAVA ACTIVE_JOB_STATUS_WAIT_JAVA}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_LOCK ACTIVE_JOB_STATUS_WAIT_LOCK}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_MESSAGE ACTIVE_JOB_STATUS_WAIT_MESSAGE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_MUTEX ACTIVE_JOB_STATUS_WAIT_MUTEX}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_MIXED_DEVICE_FILE ACTIVE_JOB_STATUS_WAIT_MIXED_DEVICE_FILE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_OSI ACTIVE_JOB_STATUS_WAIT_OSI}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_PRINT_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_PRINT_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_PRINT ACTIVE_JOB_STATUS_WAIT_PRINT}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_PRESTART ACTIVE_JOB_STATUS_WAIT_PRESTART}
   * <LI>{@link #ACTIVE_JOB_STATUS_RUNNING ACTIVE_JOB_STATUS_RUNNING}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_SELECTION ACTIVE_JOB_STATUS_WAIT_SELECTION}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_SEMAPHORE ACTIVE_JOB_STATUS_WAIT_SEMAPHORE}
   * <LI>{@link #ACTIVE_JOB_STATUS_STOPPED ACTIVE_JOB_STATUS_STOPPED}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_SIGNAL ACTIVE_JOB_STATUS_WAIT_SIGNAL}
   * <LI>{@link #ACTIVE_JOB_STATUS_SUSPENDED_SYSTEM_REQUEST ACTIVE_JOB_STATUS_SUSPENDED_SYSTEM_REQUEST}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_SAVE_FILE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_SAVE_FILE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_SAVE_FILE ACTIVE_JOB_STATUS_WAIT_SAVE_FILE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_THREAD ACTIVE_JOB_STATUS_WAIT_THREAD}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL_AND_ACTIVE ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL_AND_ACTIVE}
   * <LI>{@link #ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int ACTIVE_JOB_STATUS = 101;

  /**
   * Constant indicating that a job is either in transition or not active.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_NONE = "    ";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for the
   * completion of an I/O operation to a binary synchronous device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE_AND_ACTIVE = "BSCA";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to a binary synchronous device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_BIN_SYNCH_DEVICE = "BSCW";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for the
   * completion of an I/O operation to a communications device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE_AND_ACTIVE = "CMNA";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to a communications device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_COMM_DEVICE = "CMNW";
  
  /**
   * Constant indicating that a job is waiting for the completion of
   * save-while-active checkpoint processing in another job.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_CHECKPOINT = "CMTW";
  
  /**
   * Constant indicating that a job is waiting on a handle-based condition.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_CONDITION = "CNDW";
  
  /**
   * Constant indicating that a job is waiting for the completion of a
   * CPI communications call.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_CPI_COMM = "CPCW";
  
  /**
   * Constant indicating that a job is waiting for a specified time interval to
   * end, or for a specific delay end time, as specified on the Delay Job (DLYJOB)
   * command. The FUNCTION_NAME attribute shows either the number of seconds the
   * job is to delay (999999), or the specific time when the job is to resume
   * running.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DELAYED = "DLYW";

  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of a dequeue operation.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DEQUEUE_AND_ACTIVE = "DEQA";
  
  /**
   * Constant indicating that a job is waiting for the completion of a dequeue
   * operation.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DEQUEUE = "DEQW";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of an I/O operation to a diskette unit.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DISKETTE_AND_ACTIVE = "DKTA";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to a diskette unit.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DISKETTE = "DKTW";
  
  /**
   * Constant indicating that a job is delayed for a time interval to end,
   * or for a specific delay end time, by the Delay Job (DLYJOB) command.
   * The FUNCTION_NAME attribute shows either the number of seconds the job is to
   * delay, or the specific time when the job is to resume running.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DELAY = "DLYW";
  
  /**
   * Constant indicating that a job was disconnected form a work station display.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_DISCONNECTED = "DSC ";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * input from a work station display.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DISPLAY_AND_ACTIVE = "DSPA";
  
  /**
   * Constant indicating that a job is waiting for input from a work station
   * display.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DISPLAY = "DSPW";
  
  /**
   * Constant indicating that a job has been ended with the *IMMED option,
   * or its delay time has ended with the *CNTRLD option.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_ENDED = "END ";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level to
   * try a read operation again on a database file after the end-of-file (EOF)
   * has been reached.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF_AND_ACTIVE = "EOFA";
  
  /**
   * Constant indicating that a job is waiting to try a read operation again
   * on a database file after the end-of-file (EOF) has been reached.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_DATABASE_EOF = "EOFW";
  
  /**
   * Constant indicating that a job is ending for a reason other than running
   * the End Job (ENDJOB) or End Subsystem (ENDSBS) commands, such as a
   * SIGNOFF command, End Group Job (ENDGRPJOB) command, or an exception that
   * is not handled.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_ENDING = "EOJ ";
  
  /**
   * Constant indicating that a job is waiting for an event. For example,
   * QLUS and SCPF generally wait for work by waiting for an event.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_EVENT = "EVTW";
  
  /**
   * Constant indicating that a job is suspended by a Transfer Group Job
   * (TFRGRPJOB) command.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_SUSPENDED = "GRP ";
  
  /**
   * Constant indicating that a job is held.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_HELD = "HLD ";
  
  /**
   * Constant indicating that a job is held due to a suspended thread.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_HELD_THREAD = "HLDT";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of an I/O operation to an intersystem communications
   * function (ICF) file.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_ICF_FILE_AND_ACTIVE = "ICFA";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to an intersystem communications function (ICF) file.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_ICF_FILE = "ICFW";
  
  /**
   * Constant indicating that a job is ineligible and not currently in
   * a pool activity level.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_INELIGIBLE = "INEL";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of a Java program.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_JAVA_AND_ACTIVE = "JVAA";
  
  /**
   * Constant indicating that a job is waiting for the completion of a Java
   * program.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_JAVA = "JVAW";
  
  /**
   * Constant indicating that a job is waiting for a lock.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_LOCK = "LCKW";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of an I/O operation to multiple files.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES_AND_ACTIVE = "MLTA";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to multiple files.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_MULTIPLE_FILES = "MLTW";
  
  /**
   * Constant indicating that a job is waiting for a message from a message
   * queue.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_MESSAGE = "MSGW";
  
  /**
   * Constant indicating that a job is waiting for a mutex. A mutex is a
   * synchronization function that is used to allow multiple jobs or threads
   * to serialize their access to shared data.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_MUTEX = "MTXW";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to a mixed device file.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_MIXED_DEVICE_FILE = "MXDW";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of an I/O operation to an optical device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE_AND_ACTIVE = "OPTA";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to an optical device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_OPTICAL_DEVICE = "OPTW";
  
  /**
   * Constant indicating that a job is waiting for the completion of an OSI
   * Communications Subsystem for OS/400 operation.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_OSI = "OSIW";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of printer output.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_PRINT_AND_ACTIVE = "PRTA";
  
  /**
   * Constant indicating that a job is waiting for the completion of printer
   * output.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_PRINT = "PRTW";
  
  /**
   * Constant indicating that a prestart job is waiting for a program
   * start request.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_PRESTART = "PSRW";
  
  /**
   * Constant indicating that a job is currently running in a pool activity
   * level.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_RUNNING = "RUN ";
  
  /**
   * Constant indicating that a job is waiting for the completion of a selection.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_SELECTION = "SELW";
  
  /**
   * Constant indicating that a job is waiting for a semaphore. A semaphore is
   * a synchronization function that is used to allow multiple jobs or threads
   * to serialize their access to shared data.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_SEMAPHORE = "SEMW";
  
  /**
   * Constant indicating that a job has stopped as the result of a signal.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_STOPPED = "SIGS";
  
  /**
   * Constant indicating that a job is waiting for a signal.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_SIGNAL = "SIGW";
  
  /**
   * Constant indicating that a job is the suspended half of a system request
   * job pair.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_SUSPENDED_SYSTEM_REQUEST = "SRQ ";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of a save file operation.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_SAVE_FILE_AND_ACTIVE = "SVFA";
  
  /**
   * Constant indicating that a job is waiting for the completion of a save
   * file operation.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_SAVE_FILE = "SVFW";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * the completion of an I/O operation to a tape device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE_AND_ACTIVE = "TAPA";
  
  /**
   * Constant indicating that a job is waiting for the completion of an I/O
   * operation to a tape device.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_TAPE_DEVICE = "TAPW";
  
  /**
   * Constant indicating that a job is waiting for another thread to complete
   * an operation.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_THREAD = "THDW";
  
  /**
   * Constant indicating that a job is waiting in a pool activity level for
   * a time interval to end.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL_AND_ACTIVE = "TIMA";
  
  /**
   * Constant indicating that a job is waiting for a time interval to end.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final String ACTIVE_JOB_STATUS_WAIT_TIME_INTERVAL = "TIMW";


  
  /**
   * Job attribute representing the status of what the initial thread of a job
   * is currently doing, when the active job status is ACTIVE_JOB_STATUS_ENDED
   * or ACTIVE_JOB_STATUS_ENDING. For example, the active job status would be
   * ACTIVE_JOB_STATUS_ENDING, but the job could be waiting on a lock
   * that could keep the job from ending. This field would then be
   * ACTIVE_JOB_STATUS_WAIT_LOCK. See {@link #ACTIVE_JOB_STATUS ACTIVE_JOB_STATUS}
   * for a list of the possible values.
   * <P>Read-only: true
   * <P>Type: String
   * <P>Only valid on V5R1 systems and higher.
   * @see #ACTIVE_JOB_STATUS
  **/
  public static final int ACTIVE_JOB_STATUS_FOR_JOBS_ENDING = 103;

  /**
   * Job attribute representing whether a job allows multiple threads. This attribute
   * does not prevent OS/400 from creating system threads in the job.
   * <P>Read-only: true
   * <P>Type: Boolean
   * <P>Only valid on V5R1 systems and higher.
  **/
  public static final int ALLOW_MULTIPLE_THREADS = 102;

  /**
   * Job attribute representing the number of auxiliary I/O requests performed by
   * a job across all routing steps. This includes both database and nondatabase
   * paging. If the number of requests is greater than or equal to 2,147,483,647,
   * a value of -1 is returned. Use the AUXILIARY_IO_REQUESTS_LARGE attribute to retrieve
   * values that are greater than or equal to 2,147,483,647.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #getAuxiliaryIORequests
  **/
  public static final int AUXILIARY_IO_REQUESTS  = 1401;

  /**
   * Job attribute representing the number of auxiliary I/O requests performed by
   * a job across all routing steps. This includes both database and nondatabase
   * paging.
   * <P>Read-only: true
   * <P>Type: Long
   * <P>Only valid on V5R1 systems and higher.
   * @see #getAuxiliaryIORequests
  **/
  public static final int AUXILIARY_IO_REQUESTS_LARGE = 1406;

  /**
   * Job attribute representing how a job handles break messages.
   * Possible values are:
   * <UL>
   * <LI>{@link #BREAK_MESSAGE_HANDLING_NORMAL BREAK_MESSAGE_HANDLING_NORMAL}
   * <LI>{@link #BREAK_MESSAGE_HANDLING_HOLD BREAK_MESSAGE_HANDLING_HOLD}
   * <LI>{@link #BREAK_MESSAGE_HANDLING_NOTIFY BREAK_MESSAGE_HANDLING_NOTIFY}
   * </UL>
   * <P>Type: String
   * @see #getBreakMessageHandling
   * @see #setBreakMessageHandling
  **/
  public static final int BREAK_MESSAGE_HANDLING = 201;

  /**
   * Constant indicating that the message queue status determines break
   * message handling.
   * @see #BREAK_MESSAGE_HANDLING
  **/
  public static final String BREAK_MESSAGE_HANDLING_NORMAL            = "*NORMAL";

  /**
   * Constant indicating that the message queue holds break messages until a
   * user or program requests them. The work station user uses the Display Message
   * (DPSMSG) command to display the messages; a program must issue a Receive Message
   * (RCVMSG) command to receive a message and handle it.
   * @see #BREAK_MESSAGE_HANDLING
  **/
  public static final String BREAK_MESSAGE_HANDLING_HOLD              = "*HOLD";

  /**
   * Constant indicating that the system notifies the job's message queue when a
   * message arrives. For interactive jobs, the audible alarm sounds if there is one,
   * and the message-waiting light comes on.
   * @see #BREAK_MESSAGE_HANDLING
  **/
  public static final String BREAK_MESSAGE_HANDLING_NOTIFY            = "*NOTIFY";

  /**
   * Job attribute representing the coded character set identifier used for a job.
   * Special values include:
   * <UL>
   * <LI>{@link #CCSID_SYSTEM_VALUE CCSID_SYSTEM_VALUE}
   * <LI>{@link #CCSID_INITIAL_USER CCSID_INITIAL_USER}
   * </UL>
   * <P>Type: Integer
   * @see #getCodedCharacterSetID
   * @see #setCodedCharacterSetID
  **/
  public static final int CCSID = 302;

  /**
   * Constant indicating that the CCSID specified in the system value QCCSID is used.
   * @see #CCSID
  **/
  public static final int CCSID_SYSTEM_VALUE = -1;

  /**
   * Constant indicating that the CCSID specified in the user profile under which the
   * initial thread of the job is running is used.
   * @see #CCSID
  **/
  public static final int CCSID_INITIAL_USER = -2;

  /**
   * Job attribute representing the character identifier control for a job.
   * This attribute controls the type of CCSID conversion that occurs
   * for display files, printer files, and panel groups. The *CHRIDCTL
   * special value must be specified on the CHRID command parameter on
   * the create, change, or override command for display files, printer
   * files, and panel groups before this attribute will be used.
   * Special values include:
   * <UL>
   * <LI>{@link #CHARACTER_ID_CONTROL_DEVICE CHARACTER_ID_CONTROL_DEVICE}
   * <LI>{@link #CHARACTER_ID_CONTROL_JOB CHARACTER_ID_CONTROL_JOB}
   * <LI>{@link #CHARACTER_ID_CONTROL_SYSTEM_VALUE CHARACTER_ID_CONTROL_SYSTEM_VALUE}
   * <LI>{@link #CHARACTER_ID_CONTROL_INITIAL_USER CHARACTER_ID_CONTROL_INITIAL_USER}
   * </UL>
   * <P>Type: String
  **/
  public static final int CHARACTER_ID_CONTROL = 311;

  /**
   * Constant indicating to perform the same function for *DEVD as on the CHRID
   * command parameter for display files, printer files, and panel groups.
   * @see #CHARACTER_ID_CONTROL
  **/
  public static final String CHARACTER_ID_CONTROL_DEVICE = "*DEVD";

  /**
   * Constant indicating to perform the same function for *JOBCCSID as on the CHRID
   * command parameter for display files, printer files, and panel groups.
   * @see #CHARACTER_ID_CONTROL
  **/
  public static final String CHARACTER_ID_CONTROL_JOB = "*JOBCCSID";

  /**
   * Constant indicating the value in the QCHRIDCTL system value will be used.
   * @see #CHARACTER_ID_CONTROL
  **/
  public static final String CHARACTER_ID_CONTROL_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Constant indicating the CHRIDCTL specified in the user profile under which 
   * this thread was initially running will be used.
   * @see #CHARACTER_ID_CONTROL
  **/
  public static final String CHARACTER_ID_CONTROL_INITIAL_USER = "*USRPRF";

  /**
   * Job attribute representing the IP address of the client for which this server
   * is doing work. When this field is blank, the job thread is not serving
   * a client. An address is expressed in the form www.xxx.yyy.zzz (for example,
   * 130.99.128.1). This field is not guaranteed to be an IP address. This
   * field is set to the value set by the QWTCHGJB API.
   * <P>Type: String
  **/
  public static final int CLIENT_IP_ADDRESS = 318;

  /**
   * Job attribute representing the completion status for a job.
   * Possible values are:
   * <UL>
   * <LI>{@link #COMPLETION_STATUS_NOT_COMPLETED COMPLETION_STATUS_NOT_COMPLETED}
   * <LI>{@link #COMPLETION_STATUS_COMPLETED_NORMALLY COMPLETION_STATUS_COMPLETED_NORMALLY}
   * <LI>{@link #COMPLETION_STATUS_COMPLETED_ABNORMALLY COMPLETION_STATUS_COMPLETED_ABNORMALLY}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getCompletionStatus
  **/
  public static final int COMPLETION_STATUS = 306;

  /**
   * Constant indicating that the job has not completed.
   * @see #COMPLETION_STATUS
  **/  
  public static final String COMPLETION_STATUS_NOT_COMPLETED = " ";

  /**
   * Constant indicating that the job completed normally.
   * @see #COMPLETION_STATUS
  **/
  public static final String COMPLETION_STATUS_COMPLETED_NORMALLY = "0";

  /**
   * Constant indicating that the job completed abnormally.
   * @see #COMPLETION_STATUS
  **/
  public static final String COMPLETION_STATUS_COMPLETED_ABNORMALLY = "1";

  /**
   * Job attribute representing whether or not the system issued a controlled
   * cancellation. Possible values are:
   * <UL>
   * <LI>{@link #END_STATUS_CANCELLED END_STATUS_CANCELLED}
   * <LI>{@link #END_STATUS_NOT_CANCELLED END_STATUS_NOT_CANCELLED}
   * <LI>{@link #END_STATUS_JOB_NOT_RUNNING END_STATUS_JOB_NOT_RUNNING}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int CONTROLLED_END_REQUESTED = 502; // End status

  /**
   * Job attribute representing the total number of disk I/O operations performed by the job
   * across all routing steps. This is the sum of the asynchronous and synchronous disk I/O.
   * <P>Note: This field is only valid for Job objects created from a JobList.
   * <P>Read-only: true
   * <P>Type: Long
  **/
  //public static final int DISK_IO = 415;
  // This attribute should be enabled when JobList is updated to handle the OLJB0300 format
  // for the QGYOLJOB API.

  /**
   * Job attribute representing the percentage of processing time used during the elapsed time.
   * For multiple-processor systems, this is the average across processors.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #resetStatistics
  **/
  public static final int ELAPSED_CPU_PERCENT_USED = 314;
  
  /**
   * Job attribute representing the percentage of processing time used for database processing during the elapsed time.
   * For multiple-processor systems, this is the average across processors.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #resetStatistics
  **/
  public static final int ELAPSED_CPU_PERCENT_USED_FOR_DATABASE = 316;
  
  /**
   * Job attribute representing the amount of processing unit time (in milliseconds) used during the elapsed time.
   * <P>Read-only: true
   * <P>Type: Long
   * @see #resetStatistics
  **/
  public static final int ELAPSED_CPU_TIME_USED = 315;
  
  /**
   * Job attribute representing the amount of processing unit time (in milliseconds) used for database processing
   * during the elapsed time.
   * <P>Read-only: true
   * <P>Type: Long
   * @see #resetStatistics
  **/
  public static final int ELAPSED_CPU_TIME_USED_FOR_DATABASE = 317;
  
  /**
   * Job attribute representing the number of disk I/O operations performed by the job during the
   * elapsed time. This is the sum of the {@link #ELAPSED_DISK_IO_ASYNCH asynchronous} and
   * {@link #ELAPSED_DISK_IO_SYNCH synchronous} disk I/O.
   * <P>Read-only: true
   * <P>Type: Long
   * @see #resetStatistics
  **/
  public static final int ELAPSED_DISK_IO = 414;
  
  /**
   * Job attribute representing the number of asynchronous (physical) disk I/O operations performed
   * by the job during the elapsed time. This is the sum of the asynchronous database and nondatabase
   * reads and writes.
   * <P>Read-only: true
   * <P>Type: Long
   * @see #resetStatistics
  **/
  public static final int ELAPSED_DISK_IO_ASYNCH = 416;
  
  /**
   * Job attribute representing the number of synchronous (physical) disk I/O operations performed
   * by the job during the elapsed time. This is the sum of the synchronous database and nondatabase
   * reads and writes.
   * <P>Read-only: true
   * <P>Type: Long
   * @see #resetStatistics
  **/
  public static final int ELAPSED_DISK_IO_SYNCH = 417;
  
  /**
   * Job attribute representing the total interactive response time for the initial thread (in hundredths of seconds)
   * for the job during the elapsed time. This does not include the time used by the machine, by the attached
   * input/output (I/O) hardware, and by the transmission lines for sending and receiving data. This field is
   * 0 for noninteractive jobs.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #resetStatistics
  **/
  public static final int ELAPSED_INTERACTIVE_RESPONSE_TIME = 904;
  
  /**
   * Job attribute representing the number of user interactions, such as pressing the Enter key or a function
   * key, for the job during the elapsed time for the initial thread. This field is 0 for noninteractive jobs.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #resetStatistics
  **/
  public static final int ELAPSED_INTERACTIVE_TRANSACTIONS = 905;
  
  /**
   * Job attribute representing the amount of time (in milliseconds) that the initial thread has to wait
   * to obtain database, nondatabase, and internal machine locks during the elapsed time.
   * <P>Read-only: true
   * <P>Type: Long
   * <P>Can be loaded by JobList: false
   * @see #resetStatistics
  **/
  public static final int ELAPSED_LOCK_WAIT_TIME = 10008; // Cannot pre-load.
  
  /**
   * Job attribute representing the number of times an active program referenced an address that is not
   * in main storage during the elapsed time.
   * <P>Read-only: true
   * <P>Type: Long
   * @see #resetStatistics
  **/
  public static final int ELAPSED_PAGE_FAULTS = 1609;

  /**
   * Job attribute representing the time (in milliseconds) that has elapsed between the measurement
   * start time and the current system time. The measurement start time is reset when the 
   * {@link #resetStatistics resetStatistics()} method is called.
   * <P>Read-only: true
   * <P>Type: Long
   * <P>Can be loaded by JobList: false
   * @see #resetStatistics
  **/
  public static final int ELAPSED_TIME = 10007; // Cannot pre-load as a key.

  /**
   * Constant indicating that the system, the subsystem in which a job is running,
   * or the job itself is cancelled.
   * @see #CONTROLLED_END_REQUESTED
  **/
  public static final String END_STATUS_CANCELLED = "1";
  
  /**
   * Constant indicating that the system, the subsystem in which a job is running,
   * or the job itself is not cancelled.
   * @see #CONTROLLED_END_REQUESTED
  **/
  public static final String END_STATUS_NOT_CANCELLED = "0";
  
  /**
   * Constant indicating that the job is not running.
   * @see #CONTROLLED_END_REQUESTED
  **/
  public static final String END_STATUS_JOB_NOT_RUNNING = " ";

  /**
   * Job attribute representing the country identifier associated with a job.
   * Special values include:
   * <UL>
   * <LI>{@link #COUNTRY_ID_SYSTEM_VALUE COUNTRY_ID_SYSTEM_VALUE}
   * <LI>{@link #COUNTRY_ID_INITIAL_USER COUNTRY_ID_INITIAL_USER}
   * </UL>
   * <P>Type: String
   * @see #getCountryID
   * @see #setCountryID
  **/
  public static final int COUNTRY_ID = 303;
  
  /**
   * Constant indicating the system value QCNTRYID is used.
   * @see #COUNTRY_ID
  **/
  public static final String COUNTRY_ID_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Constant indicating the country ID specified in the user profile under which
   * this thread was initially running is used.
   * @see #COUNTRY_ID
  **/
  public static final String COUNTRY_ID_INITIAL_USER = "*USRPRF";

  /**
   * Job attribute representing the amount of processing unit time (in milliseconds)
   * that the job used. If the processing unit time used is greater than or equal
   * to 2,147,483,647 milliseconds, a value of -1 is returned. Use the CPU_TIME_USED_LARGE
   * attribute to retrieve values that are greater than or equal to 2,147,483,647.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #getCPUUsed
  **/
  public static final int CPU_TIME_USED = 304;

  /**
   * Job attribute representing the amount of processing unit time (in milliseconds)
   * that the job used across all routing steps.
   * <P>Read-only: true
   * <P>Type: Long
   * @see #getCPUUsed
  **/
  public static final int CPU_TIME_USED_LARGE = 312;

  /**
   * Job attribute representing the amount of processing unit time (in milliseconds)
   * that the job used for processing data base requests across all routing steps.
   * <P>Read-only: true
   * <P>Type: Long
   * <P>Only valid on V5R1 systems and higher.
  **/
  public static final int CPU_TIME_USED_FOR_DATABASE = 313;

  /**
   * Job attribute representing the name of the current library for the initial thread
   * of the job. If no current library exists, the CURRENT_LIBRARY_EXISTENCE attribute
   * returns 0 and this attribute returns "".
   * <P>Read-only: true
   * <P>Type: String
   * <P>Can be loaded by JobList: false
   * @see #getCurrentLibrary
  **/
  public static final int CURRENT_LIBRARY = 10000; // Cannot preload

  /**
   * Job attribute representing whether or not a current library exists for the job.
   * Returns 0 if no current library exists; 1 if a current library exists.
   * <P>Read-only: true
   * <P>Type: Integer
   * <P>Can be loaded by JobList: false
   * @see #getCurrentLibraryExistence
  **/
  public static final int CURRENT_LIBRARY_EXISTENCE = 10001; // Cannot preload

  /**
   * Job attribute representing the identifier of the system-related pool from which
   * main storage is currently being allocated for the job's initial thread. These
   * identifiers are not the same as those specified in the subsystem description, but
   * are the same as the system pool identifiers shown on the system status display. If
   * a thread reaches its time-slice end pool value, the pool the thread is running in
   * can be swiched based on the job's time-slice end pool value. The current system
   * pool identifier returned by this API will be the actual pool that the initial thread
   * of the job is running in.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int CURRENT_SYSTEM_POOL_ID = 307;

  /**
   * Job attribute representing the user profile that the initial thread of the job
   * for which information is being retrieved is currently running under. This name
   * may differ from the user portion of the job name.
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int CURRENT_USER = 305;

  /**
   * Job attribute representing the date and time when the job completed running
   * on the system.
   * <P>Read-only: true
   * <P>Type: String in the format CYYMMDDHHMMSS
  **/
  public static final int DATE_ENDED = 418;

  /**
   * Job attribute representing the data and time when the job was placed on
   * the system.
   * <P>Read-only: true
   * <P>Type: String in the format CYYMMDDHHMMSS
   * @see #getDate
   * @see #getJobEnterSystemDate
  **/
  public static final int DATE_ENTERED_SYSTEM = 402; 

  /**
   * Job attribute representing the format the date is presented in for a 
   * particular job.
   * Possible values are:
   * <UL>
   * <LI>{@link #DATE_FORMAT_YMD DATE_FORMAT_YMD}
   * <LI>{@link #DATE_FORMAT_MDY DATE_FORMAT_MDY}
   * <LI>{@link #DATE_FORMAT_DMY DATE_FORMAT_DMY}
   * <LI>{@link #DATE_FORMAT_JULIAN DATE_FORMAT_JULIAN}
   * <LI>{@link #DATE_FORMAT_SYSTEM_VALUE DATE_FORMAT_SYSTEM_VALUE}
   * </UL>
   * <P>Type: String
   * @see #getDateFormat
   * @see #setDateFormat
  **/
  public static final int DATE_FORMAT = 405;

  /**
   * Constant indicating a date format of year, month, and day.
   * @see #DATE_FORMAT
  **/
  public static final String DATE_FORMAT_YMD = "*YMD";
  
  /**
   * Constant indicating a date format of month, day, and year.
   * @see #DATE_FORMAT
  **/
  public static final String DATE_FORMAT_MDY = "*MDY";
  
  /**
   * Constant indicating a date format of day, month, and year.
   * @see #DATE_FORMAT
  **/
  public static final String DATE_FORMAT_DMY = "*DMY";
  
  /**
   * Constant indicating a Julian date format (year and day).
   * @see #DATE_FORMAT
  **/
  public static final String DATE_FORMAT_JULIAN = "*JUL";

  /**
   * Constant indicating the system value QDATFMT is used.
   * @see #DATE_FORMAT
  **/
  public static final String DATE_FORMAT_SYSTEM_VALUE = "*SYS";

  /**
   * Job attribute representing the value used to separate days,
   * months, and years when presenting a date for a particular job.
   * Possible values are:
   * <UL>
   * <LI>{@link #DATE_SEPARATOR_SYSTEM_VALUE DATE_SEPARATOR_SYSTEM_VALUE}
   * <LI>{@link #DATE_SEPARATOR_SLASH DATE_SEPARATOR_SLASH}
   * <LI>{@link #DATE_SEPARATOR_DASH DATE_SEPARATOR_DASH}
   * <LI>{@link #DATE_SEPARATOR_PERIOD DATE_SEPARATOR_PERIOD}
   * <LI>{@link #DATE_SEPARATOR_BLANK DATE_SEPARATOR_BLANK}
   * <LI>{@link #DATE_SEPARATOR_COMMA DATE_SEPARATOR_COMMA}
   * </UL>
   * <P>Type: String
   * @see #getDateSeparator
   * @see #setDateSeparator
  **/
  public static final int DATE_SEPARATOR = 406;

  /**
   * Constant indicating the system value QDATSEP is used for the date separator.
   * @see #DATE_SEPARATOR
  **/
  public static final String DATE_SEPARATOR_SYSTEM_VALUE = "S";

  /**
   * Constant indicating a slash (/) is used for the date separator.
   * @see #DATE_SEPARATOR
  **/
  public static final String DATE_SEPARATOR_SLASH = "/";

  /**
   * Constant indicating a dash (-) is used for the date separator.
   * @see #DATE_SEPARATOR
  **/
  public static final String DATE_SEPARATOR_DASH = "-";

  /**
   * Constant indicating a period (.) is used for the date separator.
   * @see #DATE_SEPARATOR
  **/
  public static final String DATE_SEPARATOR_PERIOD = ".";

  /**
   * Constant indicating a blank is used for the date separator.
   * @see #DATE_SEPARATOR
  **/
  public static final String DATE_SEPARATOR_BLANK = " ";

  /**
   * Constant indicating a comma is used for the date separator.
   * @see #DATE_SEPARATOR
  **/
  public static final String DATE_SEPARATOR_COMMA = ",";


  /**
   * Job attribute representing the date and time when a job began to
   * run on the system. This is blank if the job did not become active.
   * <P>Read-only: true
   * <P>Type: String in the format CYYMMDDHHMMSS
   * @see #getJobActiveDate
  **/
  public static final int DATE_STARTED = 401;

  /**
   * Job attribute representing whether the job is DBCS-capable or not.
   * Returns "0" if the job is not DBCS-capable; "1" if the job is DBCS-capable.
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int DBCS_CAPABLE = 407;

  /**
   * Job attribute representing the decimal format used for a job.
   * Possible values are:
   * <UL>
   * <LI>{@link #DECIMAL_FORMAT_PERIOD DECIMAL_FORMAT_PERIOD}
   * <LI>{@link #DECIMAL_FORMAT_COMMA_I DECIMAL_FORMAT_COMMA_I}
   * <LI>{@link #DECIMAL_FORMAT_COMMA_J DECIMAL_FORMAT_COMMA_J}
   * <LI>{@link #DECIMAL_FORMAT_SYSTEM_VALUE DECIMAL_FORMAT_SYSTEM_VALUE}
   * </UL>
   * <P>Type: String
   * @see #getDecimalFormat
   * @see #setDecimalFormat
  **/
  public static final int DECIMAL_FORMAT = 413;

  /**
   * Constant indicating a decimal format that uses a period for a
   * decimal point,a comma for a 3-digit grouping character, and
   * zero-suppress to the left of the decimal point.
   * @see #DECIMAL_FORMAT
  **/
  public static final String DECIMAL_FORMAT_PERIOD = "";

  /**
   * Constant indicating a decimal format that uses a comma for a
   * decimal point, a period for a 3-digit grouping character, and
   * zero-suppresses to the left of the decimal point.
   * @see #DECIMAL_FORMAT
  **/
  public static final String DECIMAL_FORMAT_COMMA_I = "I";

  /** 
   * Constant indicating a decimal format that uses a comma for a
   * decimal point and a period for a 3-digit grouping character.
   * The zero-suppression character is in the second position
   * (rather than the first) to the left of the decimal notation.
   * Balances with zero values to the left of the comma are written
   * with one leading zero (0,04). This constant also overrides any
   * edit codes that might suppress the leading zero.
   * @see #DECIMAL_FORMAT
  **/
  public static final String DECIMAL_FORMAT_COMMA_J = "J";

  /**
   * Constant indicating the value in the system value QDECFMT is used as the 
   * decimal format for this job.
   * @see #DECIMAL_FORMAT
  **/
  public static final String DECIMAL_FORMAT_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Job attribute representing the default coded character set
   * identifier used for a job. This attribute returns zero if the
   * job is not an active job.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #getDefaultCodedCharacterSetIdentifier
  **/
  public static final int DEFAULT_CCSID = 412;

  /**
   * Job attribute representing the default maximum time (in seconds)
   * that a thread in the job waits for a system instruction, such as
   * a LOCK machine interface (MI) instruction, to acquire a resource.
   * A value of -1 represents no maximum wait time (*NOMAX).
   * <P>Type: Integer
   * @see #getDefaultWait
   * @see #setDefaultWait
  **/
  public static final int DEFAULT_WAIT_TIME = 409;

  /**
   * Job attribute representing the action taken for interactive jobs
   * when an I/O error occurs for the job's requesting program device.
   * Possible values are:
   * <UL>
   * <LI>{@link #DEVICE_RECOVERY_ACTION_MESSAGE DEVICE_RECOVERY_ACTION_MESSAGE}
   * <LI>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE}
   * <LI>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST}
   * <LI>{@link #DEVICE_RECOVERY_ACTION_END_JOB DEVICE_RECOVERY_ACTION_END_JOB}
   * <LI>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST}
   * <LI>{@link #DEVICE_RECOVERY_ACTION_SYSTEM_VALUE DEVICE_RECOVERY_ACTION_SYSTEM_VALUE}
   * </UL>
   * <P>Type: String
   * @see #getDeviceRecoveryAction
   * @see #setDeviceRecoveryAction
  **/
  public static final int DEVICE_RECOVERY_ACTION = 410;

  /**
   * Constant indicating a device recovery action that signals the I/O error
   * message to the application and lets the application program perform
   * error recovery.
   * @see #DEVICE_RECOVERY_ACTION
  **/
  public static final String DEVICE_RECOVERY_ACTION_MESSAGE = "*MSG";

  /**
   * Constant indicating a device recovery action that disconnects the job
   * when an I/O error occurs. When the job reconnects, the system sends an
   * error message to the application program, indicating the job has
   * reconnected and that the work station device has recovered.
   * @see #DEVICE_RECOVERY_ACTION
  **/
  public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE = "*DSCMSG";

  /**
   * Constant indicating a device recovery action that disconnects the job
   * when an I/O error occurs. When the job reconnects, the system sends
   * the End Request (ENDRQS) command to return control to the previous
   * request level.
   * @see #DEVICE_RECOVERY_ACTION
  **/
  public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST = "*DSCENDRQS";

  /**
   * Constant indicating a device recovery action that ends the job when an
   * I/O error occurs. A message is sent to the job's log and to the history
   * log (QHST) indicating the job ended because of a device error.
   * @see #DEVICE_RECOVERY_ACTION
  **/
  public static final String DEVICE_RECOVERY_ACTION_END_JOB = "*ENDJOB";

  /**
   * Constant indicating a device recovery action that ends the job when an
   * I/O error occurs. There is no job log produced for the job. The system sends
   * a message to the QHST log indicating the job ended because of a device error.
   * @see #DEVICE_RECOVERY_ACTION
  **/
  public static final String DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST = "*ENDJOBNOLIST";

  /**
   * Constant indicating the value in the system value QDEVRCYACN is used as the
   * device recovery action for this job.
   * @see #DEVICE_RECOVERY_ACTION
  **/
  public static final String DEVICE_RECOVERY_ACTION_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Job attribute representing whether or not a job is eligible to be moved out
   * of main storage and put into auxiliary storage at the end of a time slice or
   * when it is beginning a long wait (such as waiting for a work station user's
   * response). This attribute is ignored when more than one thread is active
   * within the job. Possible values are:
   * <UL>
   * <LI>{@link #ELIGIBLE_FOR_PURGE_YES ELIGIBLE_FOR_PURGE_YES}
   * <LI>{@link #ELIGIBLE_FOR_PURGE_NO ELIGIBLE_FOR_PURGE_NO}
   * <LI>{@link #ELIGIBLE_FOR_PURGE_IGNORED ELIGIBLE_FOR_PURGE_IGNORED}
   * </UL>
   * <P>Type: String
   * @see #getPurge
   * @see #setPurge
  **/
  public static final int ELIGIBLE_FOR_PURGE = 1604;

  /**
   * Constant indicating that a job is eligible to be moved out of main storage and
   * put into auxiliary storage. A job with multiple threads, however, is never
   * purged from main storage.
   * @see #ELIGIBLE_FOR_PURGE
  **/
  public static final String ELIGIBLE_FOR_PURGE_YES = "*YES";

  /**
   * Constant indicating that a job is not eligible to be moved out of main storage and
   * put into auxiliary storage. When main storage is needed, however, pages belonging
   * to a thread in the job may be moved to auxiliary storage. Then, when a thread in the
   * job runs again, its pages are returned to main storage as they are needed.
   * @see #ELIGIBLE_FOR_PURGE
  **/
  public static final String ELIGIBLE_FOR_PURGE_NO = "*NO";

  /**
   * Constant indicating that whether a job is eligible for purge or not is ignored
   * because the job type is either *JOBQ or *OUTQ, or the job is not valid.
   * @see #ELIGIBLE_FOR_PURGE
  **/
  public static final String ELIGIBLE_FOR_PURGE_IGNORED = "";

  /**
   * Job attribute representing the message severity level of escape messages that can
   * cause a batch job to end. The batch job ends when a request in the batch input
   * stream sends an escape message, whose severity is equal to or greater than this
   * value, to the request processing program.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #getEndSeverity
  **/
  public static final int END_SEVERITY = 501;

  /**
   * Job attribute representing additional information (as described by
   * the FUNCTION_TYPE attribute) about the function the initial thread
   * is currently performing. This information is updated only when a
   * command is processed.
   * <P>Read-only: true
   * <P>Type: String
   * @see #getFunctionName
  **/
  public static final int FUNCTION_NAME = 601;

  /**
   * Job attribute representing whether the initial thread is performing
   * a high-level function and what the function type is.
   * Possible values are:
   * <UL>
   * <LI>{@link #FUNCTION_TYPE_BLANK FUNCTION_TYPE_BLANK}
   * <LI>{@link #FUNCTION_TYPE_COMMAND FUNCTION_TYPE_COMMAND}
   * <LI>{@link #FUNCTION_TYPE_DELAY FUNCTION_TYPE_DELAY}
   * <LI>{@link #FUNCTION_TYPE_GROUP FUNCTION_TYPE_GROUP}
   * <LI>{@link #FUNCTION_TYPE_INDEX FUNCTION_TYPE_INDEX}
   * <LI>{@link #FUNCTION_TYPE_IO FUNCTION_TYPE_IO}
   * <LI>{@link #FUNCTION_TYPE_LOG FUNCTION_TYPE_LOG}
   * <LI>{@link #FUNCTION_TYPE_MENU FUNCTION_TYPE_MENU}
   * <LI>{@link #FUNCTION_TYPE_MRT FUNCTION_TYPE_MRT}
   * <LI>{@link #FUNCTION_TYPE_PROCEDURE FUNCTION_TYPE_PROCEDURE}
   * <LI>{@link #FUNCTION_TYPE_PROGRAM FUNCTION_TYPE_PROGRAM}
   * <LI>{@link #FUNCTION_TYPE_SPECIAL FUNCTION_TYPE_SPECIAL}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getFunctionType
  **/
  public static final int FUNCTION_TYPE = 602;

  /**
   * Constant indicating that the system is not doing a logged function.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_BLANK = "";

  /**
   * Constant indicating that a command is running interactively, or it is in a
   * batch input stream, or it was requested from a system menu. Commands in CL
   * programs or REXX procedures are not logged.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_COMMAND = "C";

  /**
   * Constant indicating that the initial thread of the job is processing a
   * Delay Job (DLYJOB) command. The FUNCTION_NAME attribute contains the number of seconds
   * the job is delayed (up to 999999 seconds), or the time when the job is to
   * resume processing (HH:MM:SS), depending n how you specified the command.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_DELAY = "D";

  /**
   * Constant indicating that the Transfer Group Job (TFRGRPJOB) command suspended
   * the job. The FUNCTION_NAME attribute contains the group job name for that job.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_GROUP = "G";

  /**
   * Constant indicating that the initial thread of the job is rebuilding an index
   * (access path). The FUNCTION_NAME attribute contains the name of the logical file
   * whose index is rebuilt.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_INDEX = "I";

  /**
   * Constant indicating that the job is a subsystem monitor that is performing 
   * input/output (I/O) operations to a work station. The FUNCTION_NAME attribute
   * contains the name of the work station device to which the subsystem is performing
   * an input/output operation.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_IO = "O";

  /**
   * Constant indicating that the system logs history information in a database file.
   * The FUNCTION_NAME attribute contains the name of the log (QHST is the only log
   * currently supported).
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_LOG = "L";

  /**
   * Constant indicating that the initial thread of the job is currently at a 
   * system menu. The FUNCTION_NAME field contains the name of the menu.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_MENU = "N";

  /**
   * Constant indicating that the job is a multiple requester terminal (MRT) job if
   * the job type is BATCH and the subtype is MRT, or it is an interactive job
   * attached to an MRT job if the job type is interactive. See the {@link #JOB_TYPE JOB_TYPE} and
   * {@link #JOB_SUBTYPE JOB_SUBTYPE} attributes for how to determine what type of job this is.
   * <P>
   * For MRT jobs, the FUNCTION_NAME attribute contains information in the following format:
   * <UL>
   * <LI>CHAR(2): The number of requesters currently attached to the MRT job.
   * <LI>CHAR(1): The field is reserved for a / (slash).
   * <LI>CHAR(2): The maximum number (MRTMAX) of requesters.
   * <LI>CHAR(1): Reserved.
   * <LI>CHAR(3): The never-ending program (NEP) indicator. If an MRT is also an NEP,
   * the MRT stays active even if there are no requesters of the MRT. A value of NEP
   * indicates a never-ending program. A value of blanks indicates that it is not a
   * never-ending program.
   * <LI>CHAR(1): Reserved.
   * </UL>
   * <P>
   * For interactive jobs attached to an MRT, the FUNCTION_NAME attribute contains the
   * name of the MRT procedure.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_MRT = "M";

  /**
   * Constant indicating that the initial thread of the job is running a procedure.
   * The FUNCTION_NAME attribute contains the name of the procedure.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_PROCEDURE = "R";

  /**
   * Constant indicating that the initial thread of the job is running a program.
   * The FUNCTION_NAME attribute contains the name of the program.
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_PROGRAM = "P";

  /**
   * Constant indicating that the function type is a special function. For this
   * value, the FUNCTION_NAME attribute contains one of the following values:
   * <UL>
   * <LI>ADLACTJOB: Auxiliary storage is being allocated for the number of active jobs
   * specified in the QADLACTJ system value. This may indicate that the system value
   * for the initial number of active jobs is too low.
   * <LI>ADLTOTJOB: Auxiliary storage is being allocated for the number of jobs 
   * specified in the QADLTOTJ system value.
   * <LI>CMDENT: The Command Entry display is being used.
   * <LI>COMMIT: A commit operation is being performed.
   * <LI>DIRSHD: Directory shadowing.
   * <LI>DLTSPLF: The system is deleting a spooled file.
   * <LI>DUMP: A dump is in process.
   * <LI>JOBIDXRCY: A damaged job index is being recovered.
   * <LI>JOBLOG: The system is producing a job log.
   * <LI>PASSTHRU: The job is a pass-through job.
   * <LI>RCLSPLSTG: Empty spooled database members are being deleted.
   * <LI>ROLLBACK: A rollback operation is being performed.
   * <LI>SPLCLNUP: Spool cleanup is in process.
   * </UL>
   * @see #FUNCTION_TYPE
  **/
  public static final String FUNCTION_TYPE_SPECIAL = "*";

  /**
   * Job attribute representing how a job answers inquiry messages.
   * Possible values are:
   * <UL>
   * <LI>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED INQUIRY_MESSAGE_REPLY_REQUIRED}
   * <LI>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT INQUIRY_MESSAGE_REPLY_DEFAULT}
   * <LI>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST}
   * </UL>
   * <P>Type: String
   * @see #getInquiryMessageReply
   * @see #setInquiryMessageReply
  **/
  public static final int INQUIRY_MESSAGE_REPLY = 901;

  /**
   * Constant indicating that the job requires an answer for any inquiry messages
   * that occur while the job is running.
   * @see #INQUIRY_MESSAGE_REPLY
  **/
  public static final String INQUIRY_MESSAGE_REPLY_REQUIRED          = "*RQD";

  /**
   * Constant indicating that the system uses the default message reply to answer
   * any inquiry messages issued while this job is running. The default reply is
   * either in the message description or is the default system reply.
   * @see #INQUIRY_MESSAGE_REPLY
  **/
  public static final String INQUIRY_MESSAGE_REPLY_DEFAULT           = "*DFT";

  /**
   * Constant indicating that the system reply list is checked to see if there is an
   * entry for an inquiry message issued while the job is running. If a match occurs,
   * the system uses the reply value for that entry. If no entry exists for that
   * message, the system uses an inquiry message.
   * @see #INQUIRY_MESSAGE_REPLY
  **/
  public static final String INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST = "*SYSRPYL";

  /**
   * Job attribute representing the instance portion of the unit of work ID.
   * This portion of the unit-of-work identifier is the value that further
   * identifies the source of the job. This is shown as hexadecimal data.
   * <P>Read-only: true
   * <P>Type: String
   * @see #UNIT_OF_WORK_ID
   * @see #getWorkIDUnit
  **/
  public static final int INSTANCE = 21011; // Unit of work ID

  /**
   * Job attribute representing the count of operator interactions, such as pressing
   * the Enter key or a function key. This field is zero for jobs that have no
   * interactions.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #getInteractiveTransactions
  **/
  public static final int INTERACTIVE_TRANSACTIONS = 1402;


  /**
   * Job attribute representing the value input to other APIs to decrease the time
   * it takes to locate the job on the system. The identifier is not valid following
   * an initial program load (IPL). If you attempt to use it after an IPL, an
   * exception occurs.
   * <P>Read-only: true
   * <P>Type: String
   * <P>Can be loaded by JobList: false
   * @see #getInternalJobID
   * @deprecated The internal job identifier should be treated as a byte array of 16 bytes.
  **/
  public static final int INTERNAL_JOB_ID = 11000; // Always gets loaded

  /**
   * Job attribute representing the value input to other APIs to decrease the time
   * it takes to locate the job on the system. The identifier is not valid following
   * an initial program load (IPL). If you attempt to use it after an IPL, an
   * exception occurs.
   * <P>Read-only: true
   * <P>Type: byte array
   * <P>Can be loaded by JobList: false
   * @see #getInternalJobIdentifier
  **/
  public static final int INTERNAL_JOB_IDENTIFIER = 11007; // Always gets loaded

  /**
   * Job attribute representing the date used for the job. This value is for jobs
   * whose status is *JOBQ or *ACTIVE. For jobs with a status of *OUTQ, the value
   * for this field is blank.
   * <P>Type: String in the format CYYMMDD
   * @see #getJobDate
   * @see #setJobDate
  **/
  public static final int JOB_DATE = 1002;

  /**
   * Job attribute representing a set of job-related attributes used for one or more
   * jobs on the system. These attributes determine how the job is run on the system.
   * Multiple jobs can also use the same job description.
   * <P>Read-only: true
   * <P>Type: String
   * @see #getJobDescription
  **/
  public static final int JOB_DESCRIPTION = 1003;

  /**
   * Job attribute representing the most recent action that caused the job to end.
   * Possible values are:
   * <UL>
   * <LI>0: Job not ending.
   * <LI>1: Job ending in a normal manner.
   * <LI>2: Job ended while it was still on a job queue.
   * <LI>3: System ended abnormally.
   * <LI>4: Job ending normally after a controlled end was requested.
   * <LI>5: Job ending immediately.
   * <LI>6: Job ending abnormally.
   * <LI>7: Job ended due to the CPU limit being exceeded.
   * <LI>8: Job ended due to the storage limit being exceeded.
   * <LI>9: Job ended due to the message severity level bein exceeded.
   * <LI>10: Job ended due to the disconnect time interval being exceeded.
   * <LI>11: Job ended due to the inactivity time interval being exceeded.
   * <LI>12: Job ended due to a device error.
   * <LI>13: Job ended due to a signal.
   * <LI>14: Job ended due to an unhandled error.
   * </UL>
   * <P>Read-only: true
   * <P>Type: Integer
   * <P>Only valid on V5R1 systems and higher.
  **/
  public static final int JOB_END_REASON = 1014;

  /**
   * Job attribute representing whether a job's log has been written or not.
   * If the system fails while the job was active or the job ends abnormally,
   * the job log may not be written yet. This flag remains on until the job log
   * has been written. Possible values are:
   * <UL>
   * <LI>0: The job log is not pending.
   * <LI>1: The job log is pending and waiting to be written.
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * <P>Only valid on V5R1 systems and higher.
  **/
  public static final int JOB_LOG_PENDING = 1015;

  /**
   * Job attribute representing the name of the job as identified to the system.
   * For an interactive job, the system assigns the job name of the work station
   * where the job started; for a batch job, you specify the name in the command
   * when you submit the job.
   * Possible values are:
   * <UL>
   * <LI>A specific job name.
   * <LI>{@link #JOB_NAME_INTERNAL JOB_NAME_INTERNAL}
   * <LI>{@link #JOB_NAME_CURRENT JOB_NAME_CURRENT}
   * </UL>
   * <P>Type: String
   * @see #getName
   * @see #setName
  **/
  public static final int JOB_NAME = 11001; // Always gets loaded
  
  /**
   * Constant indicating that the INTERNAL_JOB_ID locates the job. The user
   * name and job number must be blank.
   * @see #JOB_NAME
  **/
  public static final String JOB_NAME_INTERNAL = "*INT";

  /**
   * Constant indicating the job that this program is running in. The user name
   * and job number must be blank.
   * @see #JOB_NAME
  **/
  public static final String JOB_NAME_CURRENT = "*";

  /**
   * Job attribute representing the system-generated job number.
   * Possible values are:
   * <UL>
   * <LI>A specific job number.
   * <LI>{@link #JOB_NUMBER_BLANK JOB_NUMBER_BLANK}
   * </UL>
   * <P>Type: String
   * @see #getNumber
   * @see #setNumber
  **/
  public static final int JOB_NUMBER = 11002; // Always gets loaded

  /**
   * Constant indicating a blank job number. This must be used if JOB_NAME_INTERNAL
   * or JOB_NAME_CURRENT is specified for the JOB_NAME.
   * @see #JOB_NUMBER
  **/
  public static final String JOB_NUMBER_BLANK = "";

  /**
   * Job attribute representing the name of the job queue that the job is
   * currently on, or that the job was on if it is currently active. This value
   * is for jobs whose status is *JOBQ or *ACTIVE. For jobs with a status of
   * *OUTQ, the value for this field is blank.
   * <P>Type: String
   * @see #getQueue
   * @see #setQueue
  **/
  public static final int JOB_QUEUE = 1004;

  /**
   * Job attribute representing the date and time when a job was put on a
   * job queue. This field wil contain blanks if the job was not on a job queue.
   * <P>Read-only: true
   * <P>Type: String in the system timestamp format
   * @see #getJobPutOnJobQueueDate
  **/
  public static final int JOB_QUEUE_DATE = 404;

  /**
   * Job attribute representing the scheduling priority of the job compared to
   * other jobs on the same job queue. The highest priority is 0 and the lowest
   * is 9. This value is for jobs whose status is *JOBQ or *ACTIVE. For jobs
   * with a status of *OUTQ, the value for this field is blank.
   * <P>Type: String
   * @see #getQueuePriority
   * @see #setQueuePriority
  **/
  public static final int JOB_QUEUE_PRIORITY = 1005;

  /**
   * Job attribute representing the status of a job on a job queue.
   * Possible values are:
   * <UL>
   * <LI>{@link #JOB_QUEUE_STATUS_BLANK JOB_QUEUE_STATUS_BLANK}
   * <LI>{@link #JOB_QUEUE_STATUS_SCHEDULED JOB_QUEUE_STATUS_SCHEDULED}
   * <LI>{@link #JOB_QUEUE_STATUS_HELD JOB_QUEUE_STATUS_HELD}
   * <LI>{@link #JOB_QUEUE_STATUS_READY JOB_QUEUE_STATUS_READY}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getJobStatusInJobQueue
  **/
  public static final int JOB_QUEUE_STATUS = 1903;

  /**
   * Constant indicating that the job was not on a job queue.
   * @see #JOB_QUEUE_STATUS
  **/
  public static final String JOB_QUEUE_STATUS_BLANK = "";

  /**
   * Constant indicating that the job will run as scheduled.
   * @see #JOB_QUEUE_STATUS
  **/
  public static final String JOB_QUEUE_STATUS_SCHEDULED = "SCD";

  /**
   * Constant indicating that the job is being held on the job queue.
   * @see #JOB_QUEUE_STATUS
  **/
  public static final String JOB_QUEUE_STATUS_HELD = "HLD";

  /**
   * Constant indicating that the job is ready to be selected.
   * @see #JOB_QUEUE_STATUS
  **/
  public static final String JOB_QUEUE_STATUS_READY = "RLS";

  /**
   * Job attribute representing the status of a job.
   * Possible values are:
   * <UL>
   * <LI>{@link #JOB_STATUS_ACTIVE JOB_STATUS_ACTIVE}
   * <LI>{@link #JOB_STATUS_JOBQ JOB_STATUS_JOBQ}
   * <LI>{@link #JOB_STATUS_OUTQ JOB_STATUS_OUTQ}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getStatus
  **/
  public static final int JOB_STATUS = 11003; // Always gets loaded

  /**
   * Constant indicating a job status of *ACTIVE. This includes group jobs,
   * system request jobs, and disconnected jobs.
   * @see #JOB_STATUS
  **/
  public static final String JOB_STATUS_ACTIVE = "*ACTIVE";

  /**
   * Constant indicating a job status of *JOBQ. This includes jobs that
   * are currently on job queues.
   * @see #JOB_STATUS
  **/
  public static final String JOB_STATUS_JOBQ = "*JOBQ";

  /**
   * Constant indicating a job status of *OUTQ. This includes jobs that have
   * completed running but still have output on an output queue.
   * @see #JOB_STATUS
  **/
  public static final String JOB_STATUS_OUTQ = "*OUTQ";

  /**
   * Job attribute representing additional information about the job type 
   * (if any exists). Possible values are:
   * <UL>
   * <LI>{@link #JOB_SUBTYPE_BLANK JOB_SUBTYPE_BLANK}
   * <LI>{@link #JOB_SUBTYPE_IMMEDIATE JOB_SUBTYPE_IMMEDIATE}
   * <LI>{@link #JOB_SUBTYPE_PROCEDURE_START_REQUEST JOB_SUBTYPE_PROCEDURE_START_REQUEST}
   * <LI>{@link #JOB_SUBTYPE_MACHINE_SERVER_JOB JOB_SUBTYPE_MACHINE_SERVER_JOB}
   * <LI>{@link #JOB_SUBTYPE_PRESTART JOB_SUBTYPE_PRESTART}
   * <LI>{@link #JOB_SUBTYPE_PRINT_DRIVER JOB_SUBTYPE_PRINT_DRIVER}
   * <LI>{@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT}
   * <LI>{@link #JOB_SUBTYPE_ALTERNATE_SPOOL_USER JOB_SUBTYPE_ALTERNATE_SPOOL_USER}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getSubtype
  **/
  public static final int JOB_SUBTYPE = 11004; // Always gets loaded

  /**
   * Constant indicating that a job has no special subtype or the job is not a valid job.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_BLANK = "";

  /**
   * Constant indicating that a job is an immediate job.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_IMMEDIATE = "D";

  /**
   * Constant indicating that a job started with a procedure start request.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_PROCEDURE_START_REQUEST = "E";

  /**
   * Constant indicating that a job is an AS/400 Advanced 36 machine server job.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_MACHINE_SERVER_JOB = "F";

  /**
   * Constant indicating that a job is a prestart job.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_PRESTART = "J";

  /**
   * Constant indicating that a job is a print driver job.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_PRINT_DRIVER = "P";

  /**
   * Constant indicating that a job is a System/36 multiple requester temrinal (MRT) job.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_MRT = "T";

  /**
   * Constant indicating that a job is an alternate spool user job.
   * @see #JOB_SUBTYPE
  **/
  public static final String JOB_SUBTYPE_ALTERNATE_SPOOL_USER = "U";

  /**
   * Job attribute representing the current setting of the job switches used by a job.
   * This value is valid for all job types.
   * <P>Type: String
   * @see #getJobSwitches
   * @see #setJobSwitches
  **/
  public static final int JOB_SWITCHES = 1006;

  /**
   * Job attribute representing the type of job.
   * Possible values are:
   * <UL>
   * <LI>{@link #JOB_TYPE_NOT_VALID JOB_TYPE_NOT_VALID}
   * <LI>{@link #JOB_TYPE_AUTOSTART JOB_TYPE_AUTOSTART}
   * <LI>{@link #JOB_TYPE_BATCH JOB_TYPE_BATCH}
   * <LI>{@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE}
   * <LI>{@link #JOB_TYPE_SUBSYSTEM_MONITOR JOB_TYPE_SUBSYSTEM_MONITOR}
   * <LI>{@link #JOB_TYPE_SPOOLED_READER JOB_TYPE_SPOOLED_READER}
   * <LI>{@link #JOB_TYPE_SYSTEM JOB_TYPE_SYSTEM}
   * <LI>{@link #JOB_TYPE_SPOOLED_WRITER JOB_TYPE_SPOOLED_WRITER}
   * <LI>{@link #JOB_TYPE_SCPF_SYSTEM JOB_TYPE_SCPF_SYSTEM}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getType
  **/
  public static final int JOB_TYPE = 11005; // Always gets loaded

  /** 
   * Constant indicating that a job is not a valid job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_NOT_VALID = "";

  /** 
   * Constant indicating that a job is an autostart job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_AUTOSTART = "A";

  /** 
   * Constant indicating that a job is a batch job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_BATCH = "B";

  /** 
   * Constant indicating that a job is an interactive job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_INTERACTIVE = "I";

  /** 
   * Constant indicating that a job is a subsystem monitor job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_SUBSYSTEM_MONITOR = "M";

  /** 
   * Constant indicating that a job is a spooled reader job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_SPOOLED_READER = "R";

  /** 
   * Constant indicating that a job is a system job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_SYSTEM = "S";

  /** 
   * Constant indicating that a job is a spooled writer job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_SPOOLED_WRITER = "W";

  /** 
   * Constant indicating that a job is the SCPF system job.
   * @see #JOB_TYPE
  **/
  public static final String JOB_TYPE_SCPF_SYSTEM = "X";

  /**
   * Job attribute representing the type of job. This attribute combines the
   * JOB_TYPE and JOB_SUBTYPE attributes. Possible values are:
   * <UL>
   * <LI>{@link #JOB_TYPE_ENHANCED_AUTOSTART JOB_TYPE_ENHANCED_AUTOSTART}
   * <LI>{@link #JOB_TYPE_ENHANCED_BATCH JOB_TYPE_ENHANCED_BATCH}
   * <LI>{@link #JOB_TYPE_ENHANCED_BATCH_IMMEDIATE JOB_TYPE_ENHANCED_BATCH_IMMEDIATE}
   * <LI>{@link #JOB_TYPE_ENHANCED_BATCH_MRT JOB_TYPE_ENHANCED_BATCH_MRT}
   * <LI>{@link #JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER}
   * <LI>{@link #JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST}
   * <LI>{@link #JOB_TYPE_ENHANCED_INTERACTIVE JOB_TYPE_ENHANCED_INTERACTIVE}
   * <LI>{@link #JOB_TYPE_ENHANCED_INTERACTIVE_GROUP JOB_TYPE_ENHANCED_INTERACTIVE_GROUP}
   * <LI>{@link #JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ JOB_TYPE_ENHANCED_SYSREQ}
   * <LI>{@link #JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP}
   * <LI>{@link #JOB_TYPE_ENHANCED_PRESTART JOB_TYPE_ENHANCED_PRESTART}
   * <LI>{@link #JOB_TYPE_ENHANCED_PRESTART_BATCH JOB_TYPE_ENHANCED_PRESTART_BATCH}
   * <LI>{@link #JOB_TYPE_ENHANCED_PRESTART_COMM JOB_TYPE_ENHANCED_PRESTART_COMM}
   * <LI>{@link #JOB_TYPE_ENHANCED_READER JOB_TYPE_ENHANCED_READER}
   * <LI>{@link #JOB_TYPE_ENHANCED_SUBSYSTEM JOB_TYPE_ENHANCED_SUBSYSTEM}
   * <LI>{@link #JOB_TYPE_ENHANCED_SYSTEM JOB_TYPE_ENHANCED_SYSTEM}
   * <LI>{@link #JOB_TYPE_ENHANCED_WRITER JOB_TYPE_ENHANCED_WRITER}
   * </UL>
   * <P>Read-only: true
   * <P>Type: Integer
   * <P>Only valid on V5R1 systems and higher.
  **/
  public static final int JOB_TYPE_ENHANCED = 1016;

  /**
   * Constant indicating that the job is an autostart job.
   * @see #JOB_TYPE_ENHANCED
  **/
  public static final Integer JOB_TYPE_ENHANCED_AUTOSTART = new Integer(110);

    /**
     * Constant indicating that the job is a batch job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH = new Integer(210);

    /**
     * Constant indicating that the job is a batch immediate job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH_IMMEDIATE = new Integer(220);

    /**
     * Constant indicating that the job is a batch System/36 multiple requester terminal (MRT) job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH_MRT = new Integer(230);

    /**
     * Constant indicating that the job is a batch alternate spool user job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER = new Integer(240);

    /**
     * Constant indicating that the job is a communications procedure start request job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST = new Integer(310);

    /**
     * Constant indicating that the job is an interactive job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE = new Integer(910);

    /**
     * Constant indicating that the job is an interactive job that is part of a group.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE_GROUP = new Integer(920);

    /**
     * Constant indicating that the job is an interactive job that is part of a system request pair.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ = new Integer(930);

    /**
     * Constant indicating that the job is an interactive job that is part of a system request pair
     * and part of a group.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP = new Integer(940);

    /**
     * Constant indicating that the job is a prestart job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_PRESTART = new Integer(1610);

    /**
     * Constant indicating that the job is a prestart batch job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_PRESTART_BATCH = new Integer(1620);

    /**
     * Constant indicating that the job is a prestart communications job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_PRESTART_COMM = new Integer(1630);

    /**
     * Constant indicating that the job is a reader job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_READER = new Integer(1810);

    /**
     * Constant indicating that the job is a subsystem job.
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_SUBSYSTEM = new Integer(1910);

    /**
     * Constant indicating that the job is a system job (all system jobs including SCPF).
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_SYSTEM = new Integer(1920);

    /**
     * Constant indicating that the job is a writer job (including both spool writers and print drivers).
     * @see #JOB_TYPE_ENHANCED
    **/
    public static final Integer JOB_TYPE_ENHANCED_WRITER = new Integer(2310);

  /**
   * Job attribute representing the user profile name by which a job is known to
   * other jobs on the system. The job user identity is used for authorization checks
   * when other jobs on the system attempt to operate against the job. For more detail
   * on how the job user identity is set and used, refer to the Set Job User Identity
   * (QWTSJUID) API. A value of *N is returned if the job user identity is set, but the
   * user profile to which it is set no longer exists.
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int JOB_USER_IDENTITY = 1012;

  /**
   * Job attribute representing the method by which the job user identity was set.
   * Possible values are:
   * <UL>
   * <LI>{@link #JOB_USER_IDENTITY_SETTING_DEFAULT JOB_USER_IDENTITY_SETTING_DEFAULT}
   * <LI>{@link #JOB_USER_IDENTITY_SETTING_APPLICATION JOB_USER_IDENTITY_SETTING_APPLICATION}
   * <LI>{@link #JOB_USER_IDENTITY_SETTING_SYSTEM JOB_USER_IDENTITY_SETTING_SYSTEM}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int JOB_USER_IDENTITY_SETTING = 1013;

  /**
   * Constant indicating that a job is currently running single threaded and the job user
   * identity is the name of the user profile under which the job is currently running.
   * This has the same meaning as a value of *DEFAULT on the Display Job Status Attributes
   * display.
   * @see #JOB_USER_IDENTITY_SETTING
  **/
  public static final String JOB_USER_IDENTITY_SETTING_DEFAULT = "0";

  /**
   * Constant indicating that a job user identity was explicitly set by an application
   * using one of the Set Job User Identity APIs, QWTSJUID or QstSetJuid(). The job may
   * be running either single threaded or multithreaded. This has the same meaning as
   * a value of *APPLICATION on the Display Job Status Attributes display.
   * @see #JOB_USER_IDENTITY_SETTING
  **/
  public static final String JOB_USER_IDENTITY_SETTING_APPLICATION = "1";

  /**
   * Constant indicating that a job is currently running multithreaded and the job user
   * identity was implicitly set by the system when the job became multithreaded. It was
   * set to the name of the user profile that the job was running under when it became
   * multithreaded. This has the same meaning as a value of *SYSTEM on the Display Job
   * Status Attributes display.
   * @see #JOB_USER_IDENTITY_SETTING
  **/
  public static final String JOB_USER_IDENTITY_SETTING_SYSTEM = "2";

  /**
   * Job attribute representing whether connections using distributed data management (DDM)
   * protocols remain active when they are not being used. The connections include
   * APPC conversations, active TCP/IP connections, or Opti-Connect connections. The DDM
   * protocols are used in Distributed Relational Database Architecture (DRDA) applications,
   * DDM applications, or DB2 Multisystem applications. Possible values are:
   * <UL>
   * <LI>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP KEEP_DDM_CONNECTIONS_ACTIVE_KEEP}
   * <LI>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP KEEP_DDM_CONNECTIONS_ACTIVE_DROP}
   * </UL>
   * <P>Type: String
   * @see #getDDMConversationHandling
   * @see #setDDMConversationHandling
  **/
  public static final int KEEP_DDM_CONNECTIONS_ACTIVE = 408; // DDM conversation handling

  /**
   * Constant indicating that the system keeps DDM connections active when there are
   * no users, except for the following:
   * <UL>
   * <LI>The routing step ends on the source system. The routing step ends when the
   * job ends or when the job is rerouted to another routing step.
   * <LI>The Reclaim Distributed Data Management Conversation (RCLDDMCNV) command or
   * the Reclaim Resources (RCLRSC) command runs.
   * <LI>A communications failure or an internal failure occurs.
   * <LI>A DRDA connection to an application server not running on the system ends.
   * </UL>
   * @see #KEEP_DDM_CONNECTIONS_ACTIVE
  **/
  public static final String KEEP_DDM_CONNECTIONS_ACTIVE_KEEP = "*KEEP";

  /**
   * Constant indicating that the system ends a DDM connection when there are no users.
   * Examples include when an application closes a DDM file, or when a DRDA application
   * runs an SQL DISCONNECT statement.
   * @see #KEEP_DDM_CONNECTIONS_ACTIVE
  **/
  public static final String KEEP_DDM_CONNECTIONS_ACTIVE_DROP = "*DROP";

  /**
   * Job attribute representing the language identifier associated with a job.
   * Special values include:
   * <UL>
   * <LI>{@link #LANGUAGE_ID_SYSTEM_VALUE LANGUAGE_ID_SYSTEM_VALUE}
   * <LI>{@link #LANGUAGE_ID_INITIAL_USER LANGUAGE_ID_INITIAL_USER}
   * </UL>
   * <P>Type: String
   * @see #getLanguageID
   * @see #setLanguageID
  **/
  public static final int LANGUAGE_ID = 1201;

  /**
   * Constant indicating the system value QLANGID is used.
   * @see #LANGUAGE_ID
  **/
  public static final String LANGUAGE_ID_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Constant indicating the language ID specified in the user profile under which 
   * this thread was initially running is used.
   * @see #LANGUAGE_ID
  **/
  public static final String LANGUAGE_ID_INITIAL_USER = "*USRPRF";

  /**
   * Job attribute representing the location name portion of the unit of work ID.
   * This portion of the unit-of-work identifier is the name of the source system
   * that originated the APPC job.
   * <P>Read-only: true
   * <P>Type: String
   * @see #UNIT_OF_WORK_ID
   * @see #getWorkIDUnit
  **/
  public static final int LOCATION_NAME = 21012; // Unit of work ID

  /**
   * Job attribute representing whether or not commands are logged for CL programs
   * that are run. Possible values are:
   * <UL>
   * <LI>{@link #LOG_CL_PROGRAMS_YES LOG_CL_PROGRAMS_YES}
   * <LI>{@link #LOG_CL_PROGRAMS_NO LOG_CL_PROGRAMS_NO}
   * </UL>
   * <P>Type: String
   * @see #getLoggingCLPrograms
   * @see #setLoggingCLPrograms
  **/
  public static final int LOG_CL_PROGRAMS = 1203;

  /**
   * Constant indicating that commands are logged for CL programs that are run.
   * @see #LOG_CL_PROGRAMS
  **/
  public static final String LOG_CL_PROGRAMS_YES = "*YES";

  /**
   * Constant indicating that commands are not logged for CL programs that are run.
   * @see #LOG_CL_PROGRAMS
  **/
  public static final String LOG_CL_PROGRAMS_NO = "*NO";

  /**
   * Job attribute representing what type of information is logged for a job.
   * Possible values are:
   * <UL>
   * <LI>{@link #LOGGING_LEVEL_NONE LOGGING_LEVEL_NONE}
   * <LI>{@link #LOGGING_LEVEL_MESSAGES_BY_SEVERITY LOGGING_LEVEL_MESSAGES_BY_SEVERITY}
   * <LI>{@link #LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES}
   * <LI>{@link #LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES}
   * <LI>{@link #LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES}
   * </UL>
   * <P>Type: String
   * @see #getLoggingLevel
   * @see #setLoggingLevel
  **/
  public static final int LOGGING_LEVEL = 1202;

  /**
   * Constant indicating that no messages are logged.
   * @see #LOGGING_LEVEL
  **/
  public static final String LOGGING_LEVEL_NONE = "0";

  /**
   * Constant indicating that all messages sent to the job's external message
   * queue with a severity greater than or equal to the message logging severity
   * are logged. This includes the indication of job start, job end, and job
   * completion status.
   * @see #LOGGING_LEVEL
  **/
  public static final String LOGGING_LEVEL_MESSAGES_BY_SEVERITY = "1";

  /**
   * Constant indicating that the following information is logged:
   * <UL>
   * <LI>Level 1 information is logged.
   * <LI>Request messages that result in a high-level message with a severity
   * code greater than or equal to the logging severity cause the request
   * message and all associated messages to be logged. A high-level message
   * is one that is sent to the program message queue of the program that
   * receives the request message. For example, QCMD iss an IBM-supplied request
   * processing program that receives request messages.
   * </UL>
   * @see #LOGGING_LEVEL
  **/
  public static final String LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES = "2";

  /**
   * Constant indicating that the following information is logged:
   * <UL>
   * <LI>Level 1 and 2 information is logged.
   * <LI>All request messages are logged.
   * <LI>Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS
   * attribute and the log attribute of the CL program.
   * </UL>
   * @see #LOGGING_LEVEL
  **/
  public static final String LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES = "3";

  /**
   * Constant indicating that the following information is logged:
   * <UL>
   * <LI>All request messages and all messages with a severity greater than or
   * equal to the message logging severity, including trace messages, are logged.
   * <LI> Commands run by a CL program are logged if it is allowed by the LOG_CL_PROGRAMS
   * attribute and the log attribute of the CL program.
   * </UL>
   * @see #LOGGING_LEVEL
  **/
  public static final String LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES = "4";

  /**
   * Job attribute representing the severity level that is used in conjunction with
   * the logging level to determine which error messages are logged in the job log.
   * The values range from 00 through 99.
   * <P>Type: Integer
   * @see #getLoggingSeverity
   * @see #setLoggingSeverity
  **/
  public static final int LOGGING_SEVERITY = 1204;

  /**
   * Job attribute representing the level of message text that is written in the job
   * log when a message is logged according to the LOGGING_LEVEL and LOGGING_SEVERITY.
   * Possible values are:
   * <UL>
   * <LI>{@link #LOGGING_TEXT_MESSAGE LOGGING_TEXT_MESSAGE}
   * <LI>{@link #LOGGING_TEXT_SECLVL LOGGING_TEXT_SECLVL}
   * <LI>{@link #LOGGING_TEXT_NO_LIST LOGGING_TEXT_NO_LIST}
   * </UL>
   * <P>Type: String
   * @see #getLoggingText
   * @see #setLoggingText
  **/
  public static final int LOGGING_TEXT = 1205;

  /**
   * Constant indicating that only the message text is written to the job log.
   * @see #LOGGING_TEXT
  **/
  public static final String LOGGING_TEXT_MESSAGE = "*MSG";

  /**
   * Constant indicating that both the message text and the message help (cause and
   * recovery) of the error message are written to the job log.
   * @see #LOGGING_TEXT
  **/
  public static final String LOGGING_TEXT_SECLVL = "*SECLVL";

  /**
   * Constant indicating that if the jobs ends normally, no job log is produces.
   * If the job ends abnormally (the job end code is 20 or higher), a job log is
   * produced. The messages that appear in the job log contain both the message
   * text and the message help.
   * @see #LOGGING_TEXT
  **/
  public static final String LOGGING_TEXT_NO_LIST = "*NOLIST";

  /**
   * Job attribute representing the maximum processing unit time (in milliseconds)
   * that the job can use. If the job consists of multiple routing steps, this is
   * the maximum processing unit time that the current routing step can use. If the
   * maximum time is exceeded, the job is ended. A value of -1 is returned if there
   * is no maximum (*NOMAX). A value of 0 is returned if the job is not active.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int MAX_CPU_TIME = 1302;

  /**
   * Job attribute representing the maximum amount of auxiliary storage (in kilobytes)
   * that the job can use. If the job consists of multiple routing steps, this is the
   * maximum temporary storage that the routing step can use. This temporary storage
   * is used for storage required by the program itself and by implicitly created internal
   * system objects used to support the routing step. (It does not include storage in
   * the QTEMP library.) If the maximum temporary storage is exceeded, the job is ended.
   * This does not apply to the use of permanent storage, which is controlled through
   * the user profile. A value of -1 is returned if there is no maximum (*NOMAX).
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int MAX_TEMP_STORAGE = 1303;

  /**
   * Job attribute representing the maximum amount of auxiliary storage (in megabytes)
   * that the job can use. If the job consists of multiple routing steps, this is the
   * maximum temporary storage that the routing step can use. This temporary storage
   * is used for storage required by the program itself and by implicitly created internal
   * system objects used to support the routing step. (It does not include storage in the
   * QTEMP library.) If the maximum temporary storage is exceeded, the job is ended.
   * This does not apply to the use of permanent storage, which is controlled through the
   * user profile. A value of -1 is returned if there is no maximum (*NOMAX).
   * <P>Read-only: true
   * <P>Type: Long
  **/
  public static final int MAX_TEMP_STORAGE_LARGE = 1305;

  /**
   * Job attribute representing the maximum number of threads that a job can run with at
   * any time. If multiple threads are initiated simultaneously, this value may be exceeded.
   * If this maximum value is exceeded, the excess threads will be allowed to run to their
   * normal completion. Initiation of additional threads will be inhibited until the maximum
   * number of threads in the job drops below this maximum value. A value of -1 is returned
   * if there is no maximum (*NOMAX). Depending upon the resources used by the threads and
   * the resources available to the system, the initiation of additional threads may be
   * inhibited before this maximum value is reached.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int MAX_THREADS = 1304;

  /**
   * Job attribute representing the name of the memory pool in which the job started
   * running. The name may be a number, in which case it is a private pool associated
   * with a subsystem. Possible values are:
   * <UL>
   * <LI>{@link #MEMORY_POOL_MACHINE MEMORY_POOL_MACHINE}
   * <LI>{@link #MEMORY_POOL_BASE MEMORY_POOL_BASE}
   * <LI>{@link #MEMORY_POOL_INTERACTIVE MEMORY_POOL_INTERACTIVE}
   * <LI>{@link #MEMORY_POOL_SPOOL MEMORY_POOL_SPOOL}
   * <LI>*SHRPOOL1 - *SHRPOOL60: This job is running in the identified shared pool.
   * <LI>01 - 99: This job is running in the identified private pool.
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * <P>Only valid on V5R1 systems and higher.
  **/
  public static final int MEMORY_POOL = 1306;

  /** 
   * Constant indicating that a job is running in the machine pool.
   * @see #MEMORY_POOL
  **/
  public static final String MEMORY_POOL_MACHINE = "*MACHINE";

  /** 
   * Constant indicating that a job is running in the base system pool, which
   * can be shared with other subsystems.
   * @see #MEMORY_POOL
  **/
  public static final String MEMORY_POOL_BASE = "*BASE";

  /** 
   * Constant indicating that a job is running in the shared pool used for
   * interactive work.
   * @see #MEMORY_POOL
  **/
  public static final String MEMORY_POOL_INTERACTIVE = "*INTERACT";

  /** 
   * Constant indicating that a job is running in the shared pool for spooled writers.
   * @see #MEMORY_POOL
  **/
  public static final String MEMORY_POOL_SPOOL = "*SPOOL";

  /**
   * Job attribute representing whether a job is waiting for a reply to a
   * specific message. This value applies only when either the ACTIVE_JOB_STATUS
   * or ACTIVE_JOB_STATUS_FOR_JOBS_ENDING attributes are set to ACTIVE_JOB_STATUS_WAIT_MESSAGE.
   * Possible values are:
   * <UL>
   * <LI>{@link #MESSAGE_REPLY_NOT_IN_MESSAGE_WAIT MESSAGE_REPLY_NOT_IN_MESSAGE_WAIT}
   * <LI>{@link #MESSAGE_REPLY_WAITING MESSAGE_REPLY_WAITING}
   * <LI>{@link #MESSAGE_REPLY_NOT_WAITING MESSAGE_REPLY_NOT_WAITING}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * <P>Only valid on V5R1 systems and higher.
  **/
  public static final int MESSAGE_REPLY = 1307;

  /**
   * Constant indicating that a job currently is not in message wait status.
   * @see #MESSAGE_REPLY
  **/
  public static final String MESSAGE_REPLY_NOT_IN_MESSAGE_WAIT = "0";

  /**
   * Constant indicating that a job is waiting for a reply to a message.
   * @see #MESSAGE_REPLY
  **/
  public static final String MESSAGE_REPLY_WAITING = "1";

  /**
   * Constant indicating that a job is not waiting for a reply to a message.
   * @see #MESSAGE_REPLY
  **/
  public static final String MESSAGE_REPLY_NOT_WAITING = "2";

  /**
   * Job attribute representing the action to take when the message queue
   * is full. Possible values are:
   * <UL>
   * <LI>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP MESSAGE_QUEUE_ACTION_NO_WRAP}
   * <LI>{@link #MESSAGE_QUEUE_ACTION_WRAP MESSAGE_QUEUE_ACTION_WRAP}
   * <LI>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP MESSAGE_QUEUE_ACTION_PRINT_WRAP}
   * <LI>{@link #MESSAGE_QUEUE_ACTION_SYSTEM_VALUE MESSAGE_QUEUE_ACTION_SYSTEM_VALUE}
   * </UL>
   * <P>Type: String
   * @see #getJobMessageQueueFullAction
   * @see #setJobMessageQueueFullAction
  **/
  public static final int MESSAGE_QUEUE_ACTION = 1007; // job message queue full action
  
  /**
   * Constant indicating that when the job message queue is full, do not wrap. This
   * action causes the job to end.
   * @see #MESSAGE_QUEUE_ACTION
  **/
  public static final String MESSAGE_QUEUE_ACTION_NO_WRAP = "*NOWRAP";

  /**
   * Constant indicating that when the job message queue is full, wrap to the beginning
   * and start filling again.
   * @see #MESSAGE_QUEUE_ACTION
  **/
  public static final String MESSAGE_QUEUE_ACTION_WRAP = "*WRAP";

  /**
   * Constant indicating that when the job message queue is full, wrap the message queue
   * and print the messages that are being overlaid because of the wrapping.
   * @see #MESSAGE_QUEUE_ACTION
  **/
  public static final String MESSAGE_QUEUE_ACTION_PRINT_WRAP = "*PRTWRAP";

  /**
   * Constant indicating the value specified for the QJOBMSGQFL system value is used.
   * @see #MESSAGE_QUEUE_ACTION
  **/
  public static final String MESSAGE_QUEUE_ACTION_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Job attribute representing the maximum size (in megabytes) that the job
   * message queue can become. The range is 2 to 64.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #getJobMessageQueueMaximumSize
  **/
  public static final int MESSAGE_QUEUE_MAX_SIZE = 1008;

  /**
   * Job attribute representing the mode name of the advanced program-to-program
   * communications device that started the job. Possible values are:
   * <UL>
   * <LI>The mode name is "*BLANK".
   * <LI>The mode name is blanks.
   * <LI>The name of the mode.
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int MODE = 1301;

  /**
   * Job attribute representing the network ID portion of the unit of work ID.
   * This portion of the unit-of-work identifier is the network name associated
   * with the unit of work.
   * <P>Read-only: true
   * <P>Type: String
   * @see #UNIT_OF_WORK_ID
   * @see #getWorkIDUnit
  **/
  public static final int NETWORK_ID = 21013; // Unit of work ID

  /**
   * Job attribute representing the name of the default output queue that is used
   * for spooled output produced by this job. The default output queue is only for
   * spooled printer files that specify *JOB for the output queue.
   * Special values include:
   * <UL>
   * <LI>{@link #OUTPUT_QUEUE_DEVICE OUTPUT_QUEUE_DEVICE}
   * <LI>{@link #OUTPUT_QUEUE_WORK_STATION OUTPUT_QUEUE_WORK_STATION}
   * <LI>{@link #OUTPUT_QUEUE_INITIAL_USER OUTPUT_QUEUE_INITIAL_USER}
   * </UL>
   * <P>Type: String
   * @see #getOutputQueue
   * @see #setOutputQueue
  **/
  public static final int OUTPUT_QUEUE = 1501;

  /**
   * Constant indicating the device specified on the Create Printer File (CRTPRTF),
   * Change Printer File (CHGPRTF), or Override with Printer File (OVRPRTF) commands is used.
   * @see #OUTPUT_QUEUE
  **/
  public static final String OUTPUT_QUEUE_DEVICE = "*DEV";

  /**
   * Constant indicating the default output queue that is used with this job is the
   * output queue that is assigned to the work staiton associated with the job at the
   * time the job is started.
   * @see #OUTPUT_QUEUE
  **/
  public static final String OUTPUT_QUEUE_WORK_STATION = "*WRKSTN";

  /**
   * Constant indicating the output queue name specified in the user profile under which this
   * thread was initially running is used.
   * @see #OUTPUT_QUEUE
  **/
  public static final String OUTPUT_QUEUE_INITIAL_USER = "*USRPRF";

  /**
   * Job attribute representing the output priority for spooled output files that
   * this job produces. The highest priority is 0 and the lowest priority is 9.
   * <P>Type: String
   * @see #getOutputQueuePriority
   * @see #setOutputQueuePriority
  **/
  public static final int OUTPUT_QUEUE_PRIORITY = 1502;

  /**
   * Job attribute representing whether border and header information is provided
   * when the Print key is pressed.
   * Possible values are:
   * <UL>
   * <LI>{@link #PRINT_KEY_FORMAT_NONE PRINT_KEY_FORMAT_NONE}
   * <LI>{@link #PRINT_KEY_FORMAT_BORDER PRINT_KEY_FORMAT_BORDER}
   * <LI>{@link #PRINT_KEY_FORMAT_HEADER PRINT_KEY_FORMAT_HEADER}
   * <LI>{@link #PRINT_KEY_FORMAT_ALL PRINT_KEY_FORMAT_ALL}
   * <LI>{@link #PRINT_KEY_FORMAT_SYSTEM_VALUE PRINT_KEY_FORMAT_SYSTEM_VALUE}
   * </UL>
   * <P>Type: String
   * @see #getPrintKeyFormat
   * @see #setPrintKeyFormat
  **/
  public static final int PRINT_KEY_FORMAT = 1601;

  /**
   * Constant indicating that the border and header information is not included
   * with output from the Print key for this job.
   * @see #PRINT_KEY_FORMAT
  **/
  public static final String PRINT_KEY_FORMAT_NONE = "*NONE";

  /**
   * Constant indicating that the border information is included
   * with output from the Print key for this job.
   * @see #PRINT_KEY_FORMAT
  **/
  public static final String PRINT_KEY_FORMAT_BORDER = "*PRTBDR";

  /**
   * Constant indicating that the header information is included
   * with output from the Print key for this job.
   * @see #PRINT_KEY_FORMAT
  **/
  public static final String PRINT_KEY_FORMAT_HEADER = "*PRTHDR";

  /**
   * Constant indicating that the border and header information is included
   * with output from the Print key for this job.
   * @see #PRINT_KEY_FORMAT
  **/
  public static final String PRINT_KEY_FORMAT_ALL = "*PRTALL";

  /**
   * Constant indicating that the value specified on the system value QPRTKEYFMT
   * determines whether header or border information is printed.
   * @see #PRINT_KEY_FORMAT
  **/
  public static final String PRINT_KEY_FORMAT_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Job attribute representing the line of text (if any) that is printed at
   * the bottom of each page of printed output for the job.
   * Special values include:
   * <UL>
   * <LI>{@link #PRINT_TEXT_SYSTEM_VALUE PRINT_TEXT_SYSTEM_VALUE}
   * <LI>{@link #PRINT_TEXT_BLANK PRINT_TEXT_BLANK}
   * </UL>
   * <P>Type: String
   * @see #getPrintText
   * @see #setPrintText
  **/
  public static final int PRINT_TEXT = 1602;

  /**
   * Constant indicating the system value QPRTTXT is used.
   * @see #PRINT_TEXT
  **/
  public static final String PRINT_TEXT_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Constant indicating that no text is printed on printed output.
   * @see #PRINT_TEXT
  **/
  public static final String PRINT_TEXT_BLANK = "*BLANK";

  /**
   * Job attribute representing the printer device used for printing output
   * from this job. Special values include:
   * <UL>
   * <LI>{@link #PRINTER_DEVICE_NAME_SYSTEM_VALUE PRINTER_DEVICE_NAME_SYSTEM_VALUE}
   * <LI>{@link #PRINTER_DEVICE_NAME_WORK_STATION PRINTER_DEVICE_NAME_WORK_STATION}
   * <LI>{@link #PRINTER_DEVICE_NAME_INITIAL_USER PRINTER_DEVICE_NAME_INITIAL_USER}
   * </UL>
   * <P>Type: String
   * @see #getPrinterDeviceName
   * @see #setPrinterDeviceName
  **/
  public static final int PRINTER_DEVICE_NAME = 1603;

  /**
   * Constant indicating the value in the system value QPRTDEV is used as the printer device.
   * @see #PRINTER_DEVICE_NAME
  **/
  public static final String PRINTER_DEVICE_NAME_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Constant indicating that the default printer device used with this job is
   * the printer device assigned to the work station that is associated with the job.
   * @see #PRINTER_DEVICE_NAME
  **/
  public static final String PRINTER_DEVICE_NAME_WORK_STATION = "*WRKSTN";

  /**
   * Constant indicating that the printer device name specified in the user profile under
   * which this thread was initially running is used.
   * @see #PRINTER_DEVICE_NAME
  **/
  public static final String PRINTER_DEVICE_NAME_INITIAL_USER = "*USRPRF";

  /**
   * Job attribute representing the libraries that contain product information
   * for the initial thread of the job, if they exist.
   * <P>Read-only: true
   * <P>Type: String
   * <P>Can be loaded by JobList: false
   * @see #getNumberOfProductLibraries
   * @see #getProductLibraries
  **/
  public static final int PRODUCT_LIBRARIES = 10002; // Cannot preload

  /**
   * Job attribute representing the return code set by the compiler for Integrated
   * Language Environment (ILE) languages. Refer to the appropriate ILE-conforming
   * language manual for possible values. This attribute is scoped to the job and
   * represents the most recent return code set by any thread within the job.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int PRODUCT_RETURN_CODE = 1605;

  /**
   * Job attribute representing the completion status of the last program that has
   * finished running, if the job contains any RPG, COBOL, data file utility (DFU),
   * or sort utilitiy programs. If not, a value of 0 is returned.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int PROGRAM_RETURN_CODE = 1606;

  /**
   * Job attribute representing the routing data that is used to determine the
   * routing entry that identifies the program to start for the routing step.
   * <P>Read-only: true
   * <P>Type: String
   * @see #getRoutingData
  **/
  public static final int ROUTING_DATA = 1803;

  /**
   * Job attribute representing the priority at which the job is currently running,
   * relative to other jobs on the system. The run priority ranges from 1 (highest
   * priority) to 99 (lowest priority). If the run priority is set to -1, the run
   * priority of the thread will be set equal to the priority of the job. The thread
   * cannot have a lower priority than its corresponding job.
   * <P>Type: Integer
   * @see #getRunPriority
   * @see #setRunPriority
  **/
  public static final int RUN_PRIORITY = 1802;

  /**
   * Job attribute representing the date on which the submitted job becomes eligible
   * to run. If your system or your job is configured to use the Julian date format,
   * *MONTHSTR and *MONTHEND are calculated as if the system or job did not use
   * the Julian date format.
   * Possible values that can be used on {@link #setValue setValue()} are:
   * <UL>
   * <LI>{@link #SCHEDULE_DATE_CURRENT SCHEDULE_DATE_CURRENT}
   * <LI>{@link #SCHEDULE_DATE_MONTH_START SCHEDULE_DATE_MONTH_START}
   * <LI>{@link #SCHEDULE_DATE_MONTH_END SCHEDULE_DATE_MONTH_END}
   * <LI>{@link #SCHEDULE_DATE_MONDAY SCHEDULE_DATE_MONDAY}
   * <LI>{@link #SCHEDULE_DATE_TUESDAY SCHEDULE_DATE_TUESDAY}
   * <LI>{@link #SCHEDULE_DATE_WEDNESDAY SCHEDULE_DATE_WEDNESDAY}
   * <LI>{@link #SCHEDULE_DATE_THURSDAY SCHEDULE_DATE_THURSDAY}
   * <LI>{@link #SCHEDULE_DATE_FRIDAY SCHEDULE_DATE_FRIDAY}
   * <LI>{@link #SCHEDULE_DATE_SATURDAY SCHEDULE_DATE_SATURDAY}
   * <LI>{@link #SCHEDULE_DATE_SUNDAY SCHEDULE_DATE_SUNDAY}
   * <LI>A date String in the format CYYMMDD.
   * </UL>
   * <P>Type: String on setValue(); java.util.Date on getValue().
   * @see #getScheduleDate
   * @see #setScheduleDate
  **/
  public static final int SCHEDULE_DATE = 1920;

  /**
   * Constant indicating the submitted job becomes eligible to run on the current date.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_CURRENT = "*CURRENT";

  /**
   * Constant indicating the submitted job becomes eligible to run on the first day of
   * the month. If you specify this value and if today is the first day of the month
   * and the time you specify on the SCHEDULE_TIME attribute has not passed, the job 
   * becomes eligible to run today. Otherwise, the job becomes eligible on the first
   * day of the next month.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_MONTH_START = "*MONTHSTR";

  /**
   * Constant indicating the submitted job becomes eligible to run on the last day of
   * the month. If you specify this value and if today is the last day of the month
   * and the time you specify on the SCHEDULE_TIME attribute has not passed, the job
   * becomes eligible to run today. Otherwise, the job becomes eligible on the last
   * day of the next month.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_MONTH_END = "*MONTHEND";

  /**
   * Constant indicating the job becomes eligible to run on Monday.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_MONDAY = "*MON";

  /**
   * Constant indicating the job becomes eligible to run on Tuesday.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_TUESDAY = "*TUE";

  /**
   * Constant indicating the job becomes eligible to run on Wednesday.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_WEDNESDAY = "*WED";

  /**
   * Constant indicating the job becomes eligible to run on Thursday.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_THURSDAY = "*THU";

  /**
   * Constant indicating the job becomes eligible to run on Friday.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_FRIDAY = "*FRI";

  /**
   * Constant indicating the job becomes eligible to run on Saturday.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_SATURDAY = "*SAT";

  /**
   * Constant indicating the job becomes eligible to run on Sunday.
   * @see #SCHEDULE_DATE
  **/
  public static final String SCHEDULE_DATE_SUNDAY = "*SUN";

  /**
   * Job attribute representing the time on the scheduled date at which
   * the job becomes eligible to run. Although the time can be specified
   * to the second, the load on the system may affect the exact time at
   * which the job becomes eligible to run.
   * Possible values that can be used on {@link #setValue setValue()} are:
   * <UL>
   * <LI>{@link #SCHEDULE_TIME_CURRENT SCHEDULE_TIME_CURRENT}
   * <LI>A time you want to start the job, specified in a 24-hour format
   * String as HHMMSS.
   * </UL>
   * <P>Type: String on setValue(); java.util.Date on getValue().
   * @see #getScheduleDate
   * @see #setScheduleTime
  **/
  public static final int SCHEDULE_TIME = 1921;

  /**
   * Constant indicating the job is submitted on the current time.
   * @see #SCHEDULE_TIME
  **/
  public static final String SCHEDULE_TIME_CURRENT = "*CURRENT";

  // This is used internally. It is the key value used to get
  // the schedule date, which is a combination of the schedule date
  // and schedule time that were set.
  static final int SCHEDULE_DATE_GETTER = 403;

  /**
   * Job attribute representing the sequence number portion of the unit of work ID.
   * This portion of the unit-of-work identifier is a value that identifies a checkpoint
   * within the application program.
   * <P>Read-only: true
   * <P>Type: String
   * @see #UNIT_OF_WORK_ID
   * @see #getWorkIDUnit
  **/
  public static final int SEQUENCE_NUMBER = 21014; // Unit of work ID

  /**
   * Job attribute representing the type of server represented by the job. A value
   * of blanks indicates that the job is not part of a server.
   * <P>Type: String
  **/ 
  public static final int SERVER_TYPE = 1911;

  /**
   * Job attribute representing whether the job is to be treated like a
   * signed-on user on the system.
   * Possible values are:
   * <UL>
   * <LI>{@link #SIGNED_ON_JOB_TRUE SIGNED_ON_JOB_TRUE}
   * <LI>{@link #SIGNED_ON_JOB_FALSE SIGNED_ON_JOB_FALSE}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getSignedOnJob
  **/
  public static final int SIGNED_ON_JOB = 701;

  /**
   * Constant indicating that the job should be treated like a signed-on user.
   * @see #SIGNED_ON_JOB
  **/
  public static final String SIGNED_ON_JOB_TRUE = "0";

  /**
   * Constant indicating that the job should not be treated like a signed-on user.
   * @see #SIGNED_ON_JOB
  **/
  public static final String SIGNED_ON_JOB_FALSE = "1";

  /**
   * Job attribute representing the sort sequence table associated
   * with this job. Possible values are:
   * <UL>
   * <LI>A sort sequence library and table.
   * <LI>{@link #SORT_SEQUENCE_TABLE_NONE SORT_SEQUENCE_TABLE_NONE}
   * <LI>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT}
   * <LI>{@link #SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT}
   * <LI>{@link #SORT_SEQUENCE_TABLE_SYSTEM_VALUE SORT_SEQUENCE_TABLE_SYSTEM_VALUE}
   * <LI>{@link #SORT_SEQUENCE_TABLE_INITIAL_USER SORT_SEQUENCE_TABLE_INITIAL_USER}
   * </UL>
   * <P>Type: String
   * @see #getSortSequenceTable
   * @see #setSortSequenceTable
  **/
  public static final int SORT_SEQUENCE_TABLE = 1901;

  /**
   * Constant indicating that no sort sequence table is used. The hexadecimal
   * values of the characters are used to determine the sort sequence.
   * @see #SORT_SEQUENCE_TABLE
  **/
  public static final String SORT_SEQUENCE_TABLE_NONE = "*HEX";

  /**
   * Constant indicating that the sort sequence table used can contain the same
   * weight for multiple characters, and it is the shared weight sort table
   * associated with the language specified in the LANGUAGE_ID attribute.
   * @see #SORT_SEQUENCE_TABLE
  **/
  public static final String SORT_SEQUENCE_TABLE_LANGUAGE_SHARED_WEIGHT = "*LANGIDSHR";

  /**
   * Constant indicating that the sort sequence table used must contain a unique
   * weight for each character in the code page, and it is the unique weight sort
   * table associated with the language specified in the LANGUAGE_ID parameter.
   * @see #SORT_SEQUENCE_TABLE
  **/
  public static final String SORT_SEQUENCE_TABLE_LANGUAGE_UNIQUE_WEIGHT = "*LANGIDUNQ";

  /**
   * Constant indicating the system value QSRTSEQ is used.
   * @see #SORT_SEQUENCE_TABLE
  **/
  public static final String SORT_SEQUENCE_TABLE_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Constant indicating the sort sequence table specified in the user profile under
   * which tihs thread was initially running is used.
   * @see #SORT_SEQUENCE_TABLE
  **/
  public static final String SORT_SEQUENCE_TABLE_INITIAL_USER = "*USRPRF";

  /**
   * Job attribute representing whether a job is running in a particular environment.
   * Possible values are:
   * <UL>
   * <LI>{@link #SPECIAL_ENVIRONMENT_NONE SPECIAL_ENVIRONMENT_NONE}
   * <LI>{@link #SPECIAL_ENVIRONMENT_SYSTEM_36 SPECIAL_ENVIRONMENT_SYSTEM_36}
   * <LI>{@link #SPECIAL_ENVIRONMENT_NOT_ACTIVE SPECIAL_ENVIRONMENT_NOT_ACTIVE}
   * </UL>
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int SPECIAL_ENVIRONMENT = 1908;

  /**
   * Constant indicating that the job is not running in any special environment.
   * @see #SPECIAL_ENVIRONMENT
  **/
  public static final String SPECIAL_ENVIRONMENT_NONE = "*NONE";

  /**
   * Constant indicating that the job is running in the System/36 environment.
   * @see #SPECIAL_ENVIRONMENT
  **/
  public static final String SPECIAL_ENVIRONMENT_SYSTEM_36 = "*S36";

  /**
   * Constant indicating that the special environment is ignored because
   * the job is not currently active.
   * @see #SPECIAL_ENVIRONMENT
  **/
  public static final String SPECIAL_ENVIRONMENT_NOT_ACTIVE = "";

  
  //public static final int SQL_SERVER_MODE = 1922;
  
  /**
   * Job attribute representing whether you want status messages displayed
   * for this job. Possible values are:
   * <UL>
   * <LI>{@link #STATUS_MESSAGE_HANDLING_NONE STATUS_MESSAGE_HANDLING_NONE}
   * <LI>{@link #STATUS_MESSAGE_HANDLING_NORMAL STATUS_MESSAGE_HANDLING_NORMAL}
   * <LI>{@link #STATUS_MESSAGE_HANDLING_SYSTEM_VALUE STATUS_MESSAGE_HANDLING_SYSTEM_VALUE}
   * <LI>{@link #STATUS_MESSAGE_HANDLING_INITIAL_USER STATUS_MESSAGE_HANDLING_INITIAL_USER}
   * </UL>
   * <P>Type: String
   * @see #getStatusMessageHandling
   * @see #setStatusMessageHandling
  **/
  public static final int STATUS_MESSAGE_HANDLING = 1902;

  /**
   * Constant indicating that the job does not display status messages.
   * @see #STATUS_MESSAGE_HANDLING
  **/
  public static final String STATUS_MESSAGE_HANDLING_NONE = "*NONE";

  /**
   * Constant indicating that the job displays status messages.
   * @see #STATUS_MESSAGE_HANDLING
  **/
  public static final String STATUS_MESSAGE_HANDLING_NORMAL = "*NORMAL";

  /**
   * Constant indicating the system value QSTSMSG is used.
   * @see #STATUS_MESSAGE_HANDLING
  **/
  public static final String STATUS_MESSAGE_HANDLING_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Constant indicating the status message handling that is specified in the
   * user profile under which this thread was initially running is used.
   * @see #STATUS_MESSAGE_HANDLING
  **/
  public static final String STATUS_MESSAGE_HANDLING_INITIAL_USER = "*USRPRF";

  /**
   * Job attribute representing the job name of the submitter's job. If the
   * job has no submitter, this value is blank.
   * <P>Read-only: true
   * <P>Type: String
  **/
  public static final int SUBMITTED_BY_JOB_NAME = 1904;

  /**
   * Job attribute representing the job number of the submitter's job. If the
   * job has no submitter, this value is blank.
   * <P>Read-only: true
   * <P>Type: String
   * <P>Can be loaded by JobList: false
  **/
  public static final int SUBMITTED_BY_JOB_NUMBER = 10005; // Cannot preload

  /**
   * Job attribute representing the user name of the submitter. If the job
   * has no submitter, this value is blank.
   * <P>Read-only: true
   * <P>Type: String
   * <P>Can be loaded by JobList: false
  **/
  public static final int SUBMITTED_BY_USER = 10006; // Cannot preload

  /**
   * Job attribute representing the subsystem description in which an active
   * job is running. This value is only for jobs whose status is *ACTIVE. For
   * jobs with status of *OUTQ or *JOBQ, this value is blank.
   * <P>Read-only: true
   * <P>Type: String
   * @see #getSubsystem
  **/
  public static final int SUBSYSTEM = 1906;

  /**
   * Job attribute representing the identifier of the system-related pool from
   * which the job's main storage is allocated. These identifiers are not the same
   * as those specified in the subsystem description, but are the same as the 
   * system pool identifiers shown on the system status display. This is the pool
   * that the threads in the job start in. Also see the CURRENT_SYSTEM_POOL_ID
   * for more information.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #CURRENT_SYSTEM_POOL_ID
   * @see #getPoolIdentifier
  **/
  public static final int SYSTEM_POOL_ID = 1907;

  /**
   * Job attribute representing the system portion of the library list of the
   * initial thread of the job.
   * <P>Read-only: true
   * <P>Type: String
   * <P>Can be loaded by JobList: false
   * @see #getNumberOfLibrariesInSYSLIBL
   * @see #getSystemLibraryList
  **/
  public static final int SYSTEM_LIBRARY_LIST = 10003; // Cannot preload

  /**
   * Job attribute representing the amount of auxiliary storage (in kilobytes)
   * that is currently allocated to this job. This value will reach a maximum
   * of 2,147,483,647 kilobytes. If the actual temporary storage used is larger
   * than that value, this attribute will return 2,147,483,647 kilobytes. It is
   * recomended that the TEMP_STORAGE_USED_LARGE attribute be used to get over
   * the limit.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int TEMP_STORAGE_USED = 2004;

  /**
   * Job attribute representing the amount of auxiliary storage (in megabytes)
   * that is currently allocated to this job.
   * <P>Read-only: true
   * <P>Type: Long
  **/
  public static final int TEMP_STORAGE_USED_LARGE = 2009;

  /**
   * Job attribute representing the count of the current number of active threads
   * in the process at the time of the materialization. An active thread may be
   * either actively running, suspended, or waiting for a resource.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int THREAD_COUNT = 2008;

  /**
   * Job attribute representing the value used to separate hours, minutes, and
   * seconds when presenting a time for this job.
   * Possible values are:
   * <UL>
   * <LI>{@link #TIME_SEPARATOR_SYSTEM_VALUE TIME_SEPARATOR_SYSTEM_VALUE}
   * <LI>{@link #TIME_SEPARATOR_COLON TIME_SEPARATOR_COLON}
   * <LI>{@link #TIME_SEPARATOR_PERIOD TIME_SEPARATOR_PERIOD}
   * <LI>{@link #TIME_SEPARATOR_BLANK TIME_SEPARATOR_BLANK}
   * <LI>{@link #TIME_SEPARATOR_COMMA TIME_SEPARATOR_COMMA}
   * </UL>
   * <P>Type: String
   * @see #getTimeSeparator
   * @see #setTimeSeparator
  **/
  public static final int TIME_SEPARATOR = 2001;

  /**
   * Constant indicating the time separator specified in the system value QTIMSEP is used.
   * @see #TIME_SEPARATOR
  **/
  public static final String TIME_SEPARATOR_SYSTEM_VALUE = "S";

  /**
   * Constant indicating a colon (:) is used for the time separator.
   * @see #TIME_SEPARATOR
  **/
  public static final String TIME_SEPARATOR_COLON = ":";

  /**
   * Constant indicating a period (.) is used for the time separator.
   * @see #TIME_SEPARATOR
  **/
  public static final String TIME_SEPARATOR_PERIOD = ".";

  /**
   * Constant indicating a blank is used for the time separator.
   * @see #TIME_SEPARATOR
  **/
  public static final String TIME_SEPARATOR_BLANK = " ";

  /**
   * Constant indicating a comma (,) is used for the time separator.
   * @see #TIME_SEPARATOR
  **/
  public static final String TIME_SEPARATOR_COMMA = ",";


  /**
   * Job attribute representing the maximum amount of processor time (in milliseconds)
   * given to each thread in this job before other threads in this job and in other
   * jobs are given the opportunity to run. The time slice establishes the amount of
   * time needed by a thread in this job to accomplish a meaningful amount of processing.
   * At the end of the time slice, the thread might be put in an inactive state so that
   * other threads can become active in the storage pool. Values retrieved range from
   * 8 through 9,999,999 milliseconds (that is, 9999.999 seconds). Although you can specify
   * a value of less than 8, the system takes a minimum of 8 milliseconds to run a process.
   * <P>Type: Integer
   * @see #getTimeSlice
   * @see #setTimeSlice
  **/
  public static final int TIME_SLICE = 2002;

  /**
   * Job attribute representing whether you want a thread in an interactive job moved
   * to another main storage pool at the end of its time slice. Possible values are:
   * <UL>
   * <LI>{@link #TIME_SLICE_END_POOL_NONE TIME_SLICE_END_POOL_NONE}
   * <LI>{@link #TIME_SLICE_END_POOL_BASE TIME_SLICE_END_POOL_BASE}
   * <LI>{@link #TIME_SLICE_END_POOL_SYSTEM_VALUE TIME_SLICE_END_POOL_SYSTEM_VALUE}
   * </UL>
   * <P>Type: String
   * @see #getTimeSliceEndPool
   * @see #setTimeSliceEndPool
  **/
  public static final int TIME_SLICE_END_POOL = 2003;

  /**
   * Constant indicating that a thread in the job does not move to another main storage
   * pool when it reaches the end of its time slice.
   * @see #TIME_SLICE_END_POOL
  **/
  public static final String TIME_SLICE_END_POOL_NONE = "*NONE";

  /**
   * Constant indicating that a thread in the job moves to the base pool when it reaches
   * the end of its time slice.
   * @see #TIME_SLICE_END_POOL
  **/
  public static final String TIME_SLICE_END_POOL_BASE = "*BASE";

  /**
   * Constant indicating the value in the system value QTSEPPOOL is used.
   * @see #TIME_SLICE_END_POOL
  **/
  public static final String TIME_SLICE_END_POOL_SYSTEM_VALUE = "*SYSVAL";

  /**
   * Job attribute representing the total amount of response time for the initial thread,
   * in milliseconds. This value does not include the time used by the machine, by the
   * attached input/output (I/O) hardware, and by the transmission lines for sending and
   * receiving data. This value is 0 for jobs that have no interactions. A value of -1 is
   * returned if this field is not large enough to hold the actual result.
   * <P>Read-only: true
   * <P>Type: Integer
   * @see #getTotalResponseTime
  **/
  public static final int TOTAL_RESPONSE_TIME = 1801;

  /**
   * Job attribute representing the unit of work ID used to track jobs across multiple
   * systems. If a job is not associated with a source or target system using advanced
   * program-to-program communications (APPC), this information is not used. Every
   * job on the system is assigned a unit of work ID. The unit-of-work identifier is
   * made up of:
   * <OL>
   * <LI>{@link #LOCATION_NAME LOCATION_NAME}
   * <LI>{@link #NETWORK_ID NETWORK_ID}
   * <LI>{@link #INSTANCE INSTANCE}
   * <LI>{@link #SEQUENCE_NUMBER SEQUENCE_NUMBER}
   * </OL>
   * <P>Read-only: true
   * <P>Type: String
   * @see #getWorkIDUnit
  **/
  public static final int UNIT_OF_WORK_ID = 2101; // This is the real key.

  /**
   * Job attribute representing the user portion of the library list for the
   * initial thread of a job.
   * <P>Read-only: true
   * <P>Type: String
   * <P>Can be loaded by JobList: false
   * @see #getNumberOfLibrariesInUSRLIBL
   * @see #getUserLibraryList
  **/
  public static final int USER_LIBRARY_LIST = 10004; // Cannot preload
  
  /**
   * Job attribute representing the user name of the job, which is the same as the
   * name of the user profile uner which the job was started. It can come from
   * several different sources, depending on the type of job. This may be different
   * than the user profile under which the job is currently running. See the
   * CURRENT_USER attribute for more information.
   * Possible values are:
   * <UL>
   * <LI>A specific user profile name.
   * <LI>{@link #USER_NAME_BLANK USER_NAME_BLANK}
   * </UL>
   * <P>Type: String
   * @see #CURRENT_USER
   * @see #getUser
   * @see #setUser
  **/
  public static final int USER_NAME = 11006; // Always gets loaded

  /**
   * Constant indicating a blank user name. This must be used when JOB_NAME_INTERNAL
   * or JOB_NAME_CURRENT is specified for the JOB_NAME.
   * @see #USER_NAME
  **/
  public static final String USER_NAME_BLANK = "";

  /**
   * Job attribute representing the user-defined return code set by ILE high-level
   * language constructs. An example is the program return code in the C language.
   * This field is scoped to the job and represents the most recent return code
   * set by any thread within the job.
   * <P>Read-only: true
   * <P>Type: Integer
  **/
  public static final int USER_RETURN_CODE = 2102;



  // Holds the lengths for all of the setter keys  
  static final IntegerHashtable setterKeys_ = new IntegerHashtable();

  static
  {
    setterKeys_.put(BREAK_MESSAGE_HANDLING, 10);
    setterKeys_.put(CCSID, 4); // Binary
    setterKeys_.put(COUNTRY_ID, 8);
    setterKeys_.put(CHARACTER_ID_CONTROL, 10);
    setterKeys_.put(CLIENT_IP_ADDRESS, 15);
    setterKeys_.put(DATE_FORMAT, 4);
    setterKeys_.put(DATE_SEPARATOR, 1);
    setterKeys_.put(KEEP_DDM_CONNECTIONS_ACTIVE, 5);
    setterKeys_.put(DEFAULT_WAIT_TIME, 4); // Binary
    setterKeys_.put(DEVICE_RECOVERY_ACTION, 13);
    setterKeys_.put(DECIMAL_FORMAT, 8);
    setterKeys_.put(INQUIRY_MESSAGE_REPLY, 10);
    setterKeys_.put(ACCOUNTING_CODE, 15);
    setterKeys_.put(JOB_DATE, 7);
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
    setterKeys_.put(SERVER_TYPE, 30);
    setterKeys_.put(SCHEDULE_DATE, 10);
    setterKeys_.put(SCHEDULE_TIME, 8);
    //setterKeys_.put(SQL_SERVER_MODE, 1);
    setterKeys_.put(TIME_SEPARATOR, 1);
    setterKeys_.put(TIME_SLICE, 4); // Binary
    setterKeys_.put(TIME_SLICE_END_POOL, 10);
  }


  /**
   * Constructs a Job object. The system and basic job information must
   * be set before connecting to the server.
   * @see #setName
   * @see #setNumber
   * @see #setSystem
   * @see #setUser
  **/
  public Job()
  {
  }



  /**
   * Constructs a Job object. The job name, user name, and job number
   * of the job that this program is running in will be used. Typically,
   * that will be the job information for the remote command host server
   * job associated with the specified AS400 object.
   * @param system The system.
   * @see #setName
   * @see #setNumber
   * @see #setUser
  **/
  public Job(AS400 system)
  {
    this(system, JOB_NAME_CURRENT, USER_NAME_BLANK, JOB_NUMBER_BLANK); //@E3C
  }



  /**
   * Constructs a Job object.
   * @param system The system.
   * @param name The job name. Specify JOB_NAME_CURRENT to indicate the job that this
   * program is running in.
   * @param user The user name. This must be USER_NAME_BLANK if the job name is JOB_NAME_CURRENT.
   * @param number The job number. This must be JOB_NUMBER_BLANK if job name is JOB_NAME_CURRENT.
  **/
  public Job(AS400 system,
             String jobName,
             String userName,
             String jobNumber)
  {
    if (system == null) throw new NullPointerException("system");
    if (jobName == null) throw new NullPointerException("jobName");
    if (userName == null) throw new NullPointerException("userName");
    if (jobNumber == null) throw new NullPointerException("jobNumber");
    if (jobName.equals(JOB_NAME_CURRENT))
    {
      if (userName.trim().length() != 0) throw new ExtendedIllegalArgumentException("userName", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      if (jobNumber.trim().length() != 0) throw new ExtendedIllegalArgumentException("jobNumber", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
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
   * Constructs a Job object. This sets the job name to JOB_NAME_INTERNAL, the user name to USER_NAME_BLANK,
   * and the job number to JOB_NUMBER_BLANK.
   * @param system The system.
   * @param internalJobID The internal job identifier.
   * @deprecated The internal job ID should be treated as a byte array of 16 bytes.
  **/
  public Job(AS400 system, String internalJobID)
  {
    if (system == null) throw new NullPointerException("system");
    if (internalJobID == null) throw new NullPointerException("internalJobID");
    if (internalJobID.length() != 16) throw new ExtendedIllegalArgumentException("internalJobID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

    system_ = system;
    internalJobID_ = internalJobID;
    realInternalJobID_ = new byte[16];
    for (int i=0; i<16; ++i)
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
   * Constructs a Job object. This sets the job name to JOB_NAME_INTERNAL, the user name to USER_NAME_BLANK,
   * and the job number to JOB_NUMBER_BLANK.
   * @param system The system.
   * @param internalJobID The 16-byte internal job identifier.
  **/
  public Job(AS400 system, byte[] internalJobID)
  {
    if (system == null) throw new NullPointerException("system");
    if (internalJobID == null) throw new NullPointerException("internalJobID");
    if (internalJobID.length != 16) throw new ExtendedIllegalArgumentException("internalJobID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

    system_ = system;
    realInternalJobID_ = internalJobID;
    char[] oldID = new char[16];
    for (int i=0; i<16; ++i)
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



  /**
   * Constructs a Job object.
   * Package scope constructor.
  **/
  Job(AS400 system, String name, String user, String number, String status, String type, String subtype)
  {
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
   * Adds a PropertyChangeListener.  The specified PropertyChangeListener's
   * <b>propertyChange()</b> method will be called each time the value of
   * any bound property is changed.
   * @param listener The listener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (propertyChangeSupport_ == null) propertyChangeSupport_ = new PropertyChangeSupport(this);
    propertyChangeSupport_.addPropertyChangeListener(listener);
  }



  /**
   * Adds a VetoableChangeListener.  The specified VetoableChangeListener's
   * <b>vetoableChange()</b> method will be called each time the value of
   * any constrained property is changed.
   * @param listener The listener.
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (vetoableChangeSupport_ == null) vetoableChangeSupport_ = new VetoableChangeSupport(this);
    vetoableChangeSupport_.addVetoableChangeListener(listener);
  }



  /**
   * Commits all uncommitted attribute changes. Calling this method will set all
   * uncommitted attribute changes to the job on the server.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void commitChanges()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {        
    if (!isConnected_) //@E3A - Already validated if we are connected
    {
      if (system_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
      if (name_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET); //@E3A
      }
      if (user_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET); //@E3A
      }
      if (number_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET); //@E3A
      }
    }
    if (cachedChanges_ == null || cachedChanges_.size_ == 0) return;
    ProgramParameter[] parmList = new ProgramParameter[5];
    int ccsid = system_.getCcsid();
    ConvTable table = ConvTable.getTable(ccsid, null);
    AS400Structure structure = new AS400Structure();
    AS400Text[] member = new AS400Text[3];
    member[0] = new AS400Text(10, ccsid, system_);
    member[1] = new AS400Text(10, ccsid, system_);
    member[2] = new AS400Text(6, ccsid, system_);
    structure.setMembers(member);
    String[] qualifiedJobName = (realInternalJobID_ != null ? new String[] { "*INT", "", "" } :
                                                              new String[] { name_, user_, number_});
    parmList[0] = new ProgramParameter(structure.toBytes(qualifiedJobName));
    //AS400Text text = new AS400Text(16, ccsid, system_);
    //parmList[1] = new ProgramParameter(text.toBytes(internalJobID_));
    parmList[1] = new ProgramParameter(realInternalJobID_ == null ? BLANKS16_ : realInternalJobID_);
    AS400Text text = new AS400Text(8, ccsid, system_);
    parmList[2] = new ProgramParameter(text.toBytes("JOBC0100"));

    int numChanges = cachedChanges_.size_;
    int totalLength = 0;
    int[][] keyTable = cachedChanges_.keys_;

    for (int i=0; i<keyTable.length; ++i)
    {
      int[] keys = keyTable[i];
      if (keys != null)
      {
        for (int j=0; j<keys.length; ++j)
        {
          int dataLength = setterKeys_.get(keys[j]);
          int pad = (4 - (dataLength % 4)) % 4;
          totalLength += 16+dataLength+pad;
        }
      }
    }

    byte[] parm3 = new byte[4+totalLength];

    BinaryConverter.intToByteArray(numChanges, parm3, 0);
    byte[] padBytes = table.stringToByteArray("    ");

    int offset = 4;
    for (int i=0; i<keyTable.length; ++i)
    {
      int[] keys = keyTable[i];
      if (keys != null)
      {
        for (int j=0; j<keys.length; ++j)
        {
          int key = keys[j];
          String type = (key == 302 || key == 409 || key == 1204 || key == 1802 || key == 2002) ? "B" : "C";
          int dataLength = setterKeys_.get(keys[j]);
          int pad = (4 - (dataLength % 4)) % 4;
          int attrLen = 16+dataLength+pad;
          BinaryConverter.intToByteArray(attrLen, parm3, offset);
          offset += 4;
          BinaryConverter.intToByteArray(key, parm3, offset);
          offset += 4;
          table.stringToByteArray(type, parm3, offset);
          offset += 1;
          System.arraycopy(padBytes, 0, parm3, offset, 3);
          offset += 3;
          BinaryConverter.intToByteArray(dataLength, parm3, offset);
          offset += 4;
          Object data = cachedChanges_.get(key);
          if (type == "B")
          {
            bin4_.toBytes(data, parm3, offset);
          }
          else
          {
            AS400Text t = new AS400Text(dataLength, ccsid, system_);
            try
            {
              t.toBytes(data, parm3, offset);
            }
            catch (ClassCastException cce)
            {
              if (data instanceof byte[]) // Used for system timestamp values like SCHEDULE_DATE
              {
                byte[] b = (byte[])data;
                System.arraycopy(b, 0, parm3, offset, 8);
              }
            }
          }
          offset += dataLength;
          if (pad > 0)
          {
            System.arraycopy(padBytes, 0, parm3, offset, pad);
            offset += pad;
          }
        }
      }
    }
    parmList[3] = new ProgramParameter(parm3);
    parmList[4] = new ProgramParameter(new byte[4]); // error code

    ProgramCall program = new ProgramCall(system_, "/QSYS.LIB/QWTCHGJB.PGM", parmList);
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Setting job information for job "+toString());
    }
    isConnected_ = true; //@E3A
    if (!program.run())
    {
      AS400Message[] msgList = program.getMessageList();
      throw new AS400Exception(msgList);
    }
    cachedChanges_ = null;
  }


  /**
   * Ends this job.
   * To end the job controlled, specify -1 for the delay.
   * To end the job immediately, specify 0 for the delay.
   * Specify any other amount of delay time (in seconds) allowed for the job to cleanup.
   * @param delay The delay time in seconds.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #hold
   * @see #release
  **/
  public void end(int delay) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException, UnsupportedEncodingException
  {
    if (delay < -1)
    {
      throw new ExtendedIllegalArgumentException("delay", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    StringBuffer buf = new StringBuffer();
    buf.append("QSYS/ENDJOB JOB(");
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
    // If the user wants to end the remote command server job that is servicing our connection, 
    // they are welcome to "shoot themselves in the foot".
    CommandCall cmd = new CommandCall(system_, toRun);
    if (!cmd.run())
    {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   * Helper method. Used to format some of the attributes that are date Strings into
   * actual Date objects.
  **/
  private Date getAsDate(int key)  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException

  {
    String str = (String)getValue(key);

    Calendar dateTime = Calendar.getInstance();
    Date date = null;
    dateTime.clear();
    int ccsid = system_.getCcsid();
    switch (str.trim().length())
    {
      case 7:  // CYYMMDD_FORMAT
        dateTime.set(Integer.parseInt(str.substring(0,3)) + 1900,// year
                     Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                     Integer.parseInt(str.substring(5,7)));      // day
        date = dateTime.getTime();
        break;
      case 13: // CYYMMDDHHMMSS_FORMAT
        dateTime.set(Integer.parseInt(str.substring(0,3)) + 1900,// year
                     Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                     Integer.parseInt(str.substring(5,7)),       // day
                     Integer.parseInt(str.substring(7,9)),       // hour
                     Integer.parseInt(str.substring(9,11)),      // minute
                     Integer.parseInt(str.substring(11,13)));    // second
        date = dateTime.getTime();
        break;
      default :
        try
        {
          date = DateFormat.getInstance().parse(str);
        }
        catch(ParseException pe)
        {
          if (Trace.traceOn_)
          {
            Trace.log(Trace.ERROR, "Could not parse date string '"+str+"' for key "+key+":", pe);
            date = null;
          }
        }
        break;
    }
    return date;
  }


  /**
   * Helper method. Used to retrieve an Integer value out of our internal table.
  **/
  private int getAsInt(int key)  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException

  {
    return (new Integer(getValue(key).toString().trim())).intValue();
  }

  
  /**
   * Helper method. Used to convert a system timestamp value into a Date object.
  **/
  private Date getAsSystemDate(int key)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    byte[] data = (byte[])getValue(key); // This is in the system timestamp format which requires an extra API call.
    DateTimeConverter conv = new DateTimeConverter(system_);
    Date d = conv.convert(data, "*DTS");
    return d;
  }


/**
Returns the number of auxiliary I/O requests for the initial thread of this job.

@return The number of auxiliary I/O requests.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #AUXILIARY_IO_REQUESTS
**/
  public int getAuxiliaryIORequests()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(AUXILIARY_IO_REQUESTS);

  }


/**
Returns a value which represents how this job handles break messages.

@return How this job handles break messages.  Possible values are:
        <ul>
        <li>{@link #BREAK_MESSAGE_HANDLING_NORMAL BREAK_MESSAGE_HANDLING_NORMAL }
            - The message queue status determines break message handling.
        <li>{@link #BREAK_MESSAGE_HANDLING_HOLD BREAK_MESSAGE_HANDLING_HOLD }
            - The message queue holds break messages until a user or program
            requests them.
        <li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY BREAK_MESSAGE_HANDLING_NOTIFY }
            - The system notifies the job's message queue when a message
            arrives.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #BREAK_MESSAGE_HANDLING
**/
  public String getBreakMessageHandling()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(BREAK_MESSAGE_HANDLING)).trim();
  }



  /**
   * Indicates if the attribute value changes are cached.
   * @return true if attribute value changes are cached,
   * false if attribute value changes are committed immediatly. The
   * default is true.
   * @see #commitChanges
   * @see #getValue
   * @see #setCacheChanges
   * @see #setValue
  **/
  public boolean getCacheChanges()
  {
    return cacheChanges_;
  }



/**
Returns the coded character set identifier (CCSID).

@return The coded character set identifier.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #CCSID
**/
  public int getCodedCharacterSetID()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(CCSID);
  }
             


/**
Returns the completion status of the job.

@return The completion status of the job. Possible values are:
        <ul>
        <li>{@link #COMPLETION_STATUS_NOT_COMPLETED COMPLETION_STATUS_NOT_COMPLETED }
            - The job has not completed.
        <li>{@link #COMPLETION_STATUS_COMPLETED_NORMALLY COMPLETION_STATUS_COMPLETED_NORMALLY }
            - The job completed normally.
        <li>{@link #COMPLETION_STATUS_COMPLETED_ABNORMALLY COMPLETION_STATUS_COMPLETED_ABNORMALLY }
            - The job completed abnormally.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #COMPLETION_STATUS
**/
  public String getCompletionStatus()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return (String)getValue(COMPLETION_STATUS);
  }



/**
Returns the country ID.

@return The country ID.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #COUNTRY_ID
**/
  public String getCountryID()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(COUNTRY_ID)).trim();
  }



/**
Returns the amount of processing time used (in milliseconds) that
the job used.

@return The amount of processing time used (in milliseconds) the
        the job used.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #CPU_TIME_USED
**/
  public int getCPUUsed()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(CPU_TIME_USED);
  }



/**
Returns the name of the current library for the initial thread of the job.

@return The name of the current library for the initial thread of the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #CURRENT_LIBRARY
**/
  public String getCurrentLibrary()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(CURRENT_LIBRARY)).trim();
  }



/**
Indicates if a current library exists.

@return true if a current library exists, false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #CURRENT_LIBRARY_EXISTENCE
**/
  public boolean getCurrentLibraryExistence()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(CURRENT_LIBRARY_EXISTENCE) == 1;
  }



/**
Returns the date and time when the job was placed on the
system.

@return  The date and time when the job was placed on the system.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_ENTERED_SYSTEM
**/
  public Date getDate()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsDate(DATE_ENTERED_SYSTEM);
  }



/**
Returns the format in which dates are presented.

@return The format in which dates are presented.  Possible values are:
        <ul>
        <li>{@link #DATE_FORMAT_YMD DATE_FORMAT_YMD }  - Year, month, and day format.
        <li>{@link #DATE_FORMAT_MDY DATE_FORMAT_MDY }  - Month, day, and year format.
        <li>{@link #DATE_FORMAT_DMY DATE_FORMAT_DMY }  - Day, month, and year format.
        <li>{@link #DATE_FORMAT_JULIAN DATE_FORMAT_JULIAN }  - Julian format (year and day).
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_FORMAT
**/
  public String getDateFormat()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(DATE_FORMAT)).trim();
  }



/**
Returns the date separator. The date separator is used to separate days,
months, and years when representing a date.

@return The date separator.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_SEPARATOR
**/
  public String getDateSeparator()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return(String)getValue(DATE_SEPARATOR); // Don't trim.
  }



/**
Returns whether connections using distributed data management (DDM) protocols
remain active when they are not being used.

@return Whether connections using distributed data management (DDM) protocols
        remain active when they are not being used.  Possible values are:
        <ul>
        <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP KEEP_DDM_CONNECTIONS_ACTIVE_KEEP }  - The system keeps DDM connections active when there
            are no users.
        <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP KEEP_DDM_CONNECTIONS_ACTIVE_DROP }  - The system ends a DDM connection when there are no
            users.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #KEEP_DDM_CONNECTIONS_ACTIVE
**/
  public String getDDMConversationHandling()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(KEEP_DDM_CONNECTIONS_ACTIVE)).trim();
  }



/**
Returns the decimal format used for this job.

@return The decimal format used for this job. Possible values are:
        <ul>
        <li>{@link #DECIMAL_FORMAT_PERIOD DECIMAL_FORMAT_PERIOD }  - Uses a period for a decimal point, a comma
            for a 3-digit grouping character, and zero-suppresses to the left of
            the decimal point.
        <li>{@link #DECIMAL_FORMAT_COMMA_I DECIMAL_FORMAT_COMMA_I }  - Uses a comma for a decimal point and a period for
            a 3-digit grouping character.  The zero-suppression character is in the
            second character (rather than the first) to the left of the decimal
            notation.  Balances with zero  values to the left of the comma are
            written with one leading zero.
        <li>{@link #DECIMAL_FORMAT_COMMA_J DECIMAL_FORMAT_COMMA_J }  - Uses a comma for a decimal point, a period for a
            3-digit grouping character, and zero-suppresses to the left of the decimal
            point.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DECIMAL_FORMAT
**/
  public String getDecimalFormat()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(DECIMAL_FORMAT)).trim();
  }



/**
Returns the default coded character set identifier (CCSID) for this job.

@return The default coded character set identifier (CCSID) for this job.
        The value will be 0 if the job is not active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DEFAULT_CCSID
**/
  public int getDefaultCodedCharacterSetIdentifier()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(DEFAULT_CCSID);
  }



  //@A2A
/**
Returns the default maximum time (in seconds) that a thread in the job waits
for a system instruction.

@return The default maximum time (in seconds) that a thread in the job
        waits for a system instruction.  The value -1 means there is no maximum.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DEFAULT_WAIT_TIME
**/
  public int getDefaultWait()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(DEFAULT_WAIT_TIME);
  }



/**
Returns the action taken for interactive jobs when an I/O error occurs
for the job's requesting program device.

@return The action taken for interactive jobs when an I/O error occurs
        for the job's requesting program device.  Possible values are:
        <ul>
        <li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE DEVICE_RECOVERY_ACTION_MESSAGE }  - Signals the I/O error message to the
            application and lets the application program perform error recovery.
        <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE } DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE }  - Disconnects the
            job when an I/O error occurs.  When the job reconnects, the system sends an
            error message to the application program, indicating the job has reconnected
            and that the workstation device has recovered.
        <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST }  - Disconnects
            the job when an I/O error occurs.  When the job reconnects, the system sends
            the End Request (ENDRQS) command to return control to the previous request
            level.
        <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB DEVICE_RECOVERY_ACTION_END_JOB }  - Ends the job when an I/O error occurs.  A
            message is sent to the job's log and to the history log (QHST) indicating
            the job ended because of a device error.
        <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST }  - Ends the job when an I/O
            error occurs.  There is no job log produced for the job.  The system sends
            a message to the QHST log indicating the job ended because of a device error.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DEVICE_RECOVERY_ACTION
**/
  public String getDeviceRecoveryAction()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(DEVICE_RECOVERY_ACTION)).trim();
  }



/**
Returns the message severity level of escape messages that can cause a batch
job to end.  The batch job ends when a request in the batch input stream sends
an escape message, whose severity is equal to or greater than this value, to the
request processing program.

@return The message severity level of escape messages that can cause a batch
        job to end.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #END_SEVERITY
**/
  public int getEndSeverity()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(END_SEVERITY);
  }



/**
Returns additional information about the function the initial thread is currently
performing.  This information is updated only when a command is processed.

@return The additional information about the function the initial thread is currently
        performing.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #FUNCTION_NAME
**/
  public String getFunctionName()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(FUNCTION_NAME)).trim();
  }



/**
Returns the high-level function type the initial thread is performing,
if any.

@return The high-level function type the initial thread is performing,
        if any.  Possible values are:
        <ul>
        <li>{@link #FUNCTION_TYPE_BLANK FUNCTION_TYPE_BLANK }  - The system is not performing a logged function.
        <li>{@link #FUNCTION_TYPE_COMMAND FUNCTION_TYPE_COMMAND }  - A command is running interactively, or it is
            in a batch input stream, or it was requested from a system menu.
        <li>{@link #FUNCTION_TYPE_DELAY FUNCTION_TYPE_DELAY }  - The initial thread of the job is processing
            a Delay Job (DLYJOB) command.
        <li>{@link #FUNCTION_TYPE_GROUP FUNCTION_TYPE_GROUP }  - The Transfer Group Job (TFRGRPJOB) command
            suspended the job.
        <li>{@link #FUNCTION_TYPE_INDEX FUNCTION_TYPE_INDEX }  - The initial thread of the job is rebuilding
            an index (access path).
        <li>{@link #FUNCTION_TYPE_IO FUNCTION_TYPE_IO }  - The job is a subsystem monitor that is performing
            input/output (I/O) operations to a work station.
        <li>{@link #FUNCTION_TYPE_LOG FUNCTION_TYPE_LOG }  - The system logs history information in a database
            file.
        <li>{@link #FUNCTION_TYPE_MENU FUNCTION_TYPE_MENU }  - The initial thread of the job is currently
            at a system menu.
        <li>{@link #FUNCTION_TYPE_MRT FUNCTION_TYPE_MRT }  - The job is a multiple requester terminal (MRT)
            job is the {@link #JOB_TYPE job type }  is {@link #JOB_TYPE_BATCH JOB_TYPE_BATCH }
            and the {@link #JOB_SUBTYPE job subtype }  is {@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT } ,
            or it is an interactive job attached to an MRT job if the
            {@link #JOB_TYPE job type }  is {@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE } .
        <li>{@link #FUNCTION_TYPE_PROCEDURE FUNCTION_TYPE_PROCEDURE }  - The initial thread of the job is running
            a procedure.
        <li>{@link #FUNCTION_TYPE_PROGRAM FUNCTION_TYPE_PROGRAM }  - The initial thread of the job is running
            a program.
        <li>{@link #FUNCTION_TYPE_SPECIAL FUNCTION_TYPE_SPECIAL }  - The function type is special.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #FUNCTION_TYPE
**/
  public String getFunctionType()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return(String)getValue(FUNCTION_TYPE); // Don't trim.
  }



/**
Returns how the job answers inquiry messages.

@return How the job answers inquiry messages.  Possible values are:
        <ul>
        <li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED INQUIRY_MESSAGE_REPLY_REQUIRED }  - The job requires an answer for any inquiry
            messages that occur while this job is running.
        <li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT INQUIRY_MESSAGE_REPLY_DEFAULT }  - The system uses the default message reply to
            answer any inquiry messages issued while this job is running.  The default
            reply is either defined in the message description or is the default system
            reply.
        <li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST } The system reply list is
            checked to see if there is an entry for an inquiry message issued while this
            job is running.  If a match occurs, the system uses the reply value for that
            entry.  If no entry exists for that message, the system uses an inquiry message.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #INQUIRY_MESSAGE_REPLY
**/
  public String getInquiryMessageReply()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(INQUIRY_MESSAGE_REPLY)).trim();
  }



/**
Returns the number of interactive transactions.

@return The number of interactive transactions.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #INTERACTIVE_TRANSACTIONS
**/
  public int getInteractiveTransactions()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(INTERACTIVE_TRANSACTIONS);
  }



/**
Returns the internal job identifier.

@return The internal job identifier.
@deprecated The internal job identifier should be treated as a byte array of 16 bytes.
**/
  public String getInternalJobID()
  {
    return internalJobID_;
  }

  /**
   * Returns the internal job identifier.
   * @return The 16-byte internal job identifier, or null if one has not been set or retrieved
   * from the system.
   * @see #setInternalJobIdentifier
  **/
  public byte[] getInternalJobIdentifier()
  {
    return realInternalJobID_;
  }

/**
Returns the identifier assigned to the job by the system to collect resource
use information for the job when job accounting is active.

@return The identifier assigned to the job by the system to collect resource
        use information for the job when job accounting is active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #ACCOUNTING_CODE
**/
  public String getJobAccountingCode()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(ACCOUNTING_CODE)).trim(); //@E2C
  }



/**
Returns the date and time when the job began to run on the system.

@return The date and time when the job began to run on the system.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_STARTED
**/
  public Date getJobActiveDate()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsDate(DATE_STARTED);
  }



/**
Returns the date to be used for the job.

@return The date to be used for the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_DATE
**/
  public Date getJobDate()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsDate(JOB_DATE);
  }



/**
Returns the fully qualified integrated path name for the job
description.

@return The fully qualified integrated path name for the job
        description.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_DESCRIPTION
@see QSYSObjectPathName
**/
  public String getJobDescription()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
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
Returns the date and time when the job ended.

@return The date and time when the job ended.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_ENDED
**/
  public Date getJobEndedDate()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsDate(DATE_ENDED);
  }



/**
Returns the date and time when the job was placed on the system.

@return The date and time when the job was placed on the system.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_ENTERED_SYSTEM
**/
  public Date getJobEnterSystemDate()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsDate(DATE_ENTERED_SYSTEM);
  }



/**
Returns the job log.

@return The job log.
**/
  public JobLog getJobLog()
  {
    // Rather than using name_, user_, and number_, I will get
    // their attribute values.  This will work when CURRENT or
    // an internal job id is specified.
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

@return The action to take when the message queue is full.  Possible values are:
        <ul>
        <li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP MESSAGE_QUEUE_ACTION_NO_WRAP }  - Do not wrap. This action causes the job to end.
        <li>{@link #MESSAGE_QUEUE_ACTION_WRAP MESSAGE_QUEUE_ACTION_WRAP }  - Wrap to the beginning and start filling again.
        <li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP MESSAGE_QUEUE_ACTION_PRINT_WRAP }  - Wrap the message queue and print the
            messages that are being overlaid because of the wrapping.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #MESSAGE_QUEUE_ACTION
**/
  public String getJobMessageQueueFullAction()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(MESSAGE_QUEUE_ACTION)).trim();
  }



/**
Returns the maximum size (in megabytes) that the job message queue can become.

@return The maximum size (in megabytes) that the job message queue can become.
        The range is 2 through 64.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #MESSAGE_QUEUE_MAX_SIZE
**/
  public int getJobMessageQueueMaximumSize()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(MESSAGE_QUEUE_MAX_SIZE);
  }



/**
Returns the date and time the job was put on the job queue.

@return The date and time the job was put on the job queue.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_QUEUE_DATE
**/
  public Date getJobPutOnJobQueueDate()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsSystemDate(JOB_QUEUE_DATE);
  }


/**
Returns the date and time the job is scheduled to become active.

@return The date and time the job is scheduled to become active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SCHEDULE_DATE
**/
  public Date getScheduleDate()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (cachedChanges_ != null && (cachedChanges_.contains(SCHEDULE_DATE) || cachedChanges_.contains(SCHEDULE_TIME)))
    {
      Calendar calendar = Calendar.getInstance();
      calendar.clear();
      String scheduleDate = (String)cachedChanges_.get(SCHEDULE_DATE);
      if (scheduleDate != null)
      {
        int century = Integer.parseInt(scheduleDate.substring(0,1));
        int year    = Integer.parseInt(scheduleDate.substring(1,3));
        int month   = Integer.parseInt(scheduleDate.substring(3,5));
        int day     = Integer.parseInt(scheduleDate.substring(5,7));

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
        int hours   = Integer.parseInt(scheduleTime.substring(0,2));
        int minutes = Integer.parseInt(scheduleTime.substring(2,4));
        int seconds = Integer.parseInt(scheduleTime.substring(4,6));

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
    return getAsSystemDate(SCHEDULE_DATE_GETTER); // System timestamp format again.
  }



/**
Returns the status of the job on the job queue.

@return The status of the job on the job queue.  Possible values are:
        <ul>
        <li>{@link #JOB_QUEUE_STATUS_BLANK JOB_QUEUE_STATUS_BLANK }  - The job is not on a job queue.
        <li>{@link #JOB_QUEUE_STATUS_SCHEDULED JOB_QUEUE_STATUS_SCHEDULED }  - The job will run as scheduled.
        <li>{@link #JOB_QUEUE_STATUS_HELD JOB_QUEUE_STATUS_HELD }  - The job is being held on the job queue.
        <li>{@link #JOB_QUEUE_STATUS_READY JOB_QUEUE_STATUS_READY }  - The job is ready to be selected.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_QUEUE_STATUS
**/
  public String getJobStatusInJobQueue()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(JOB_QUEUE_STATUS)).trim();
  }



/**
Returns the current setting of the job switches used by this job.

@return The current setting of the job switches used by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_SWITCHES
**/
  public String getJobSwitches()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return(String)getValue(JOB_SWITCHES);
  }



/**
Returns the language identifier associated with this job.

@return The language identifier associated with this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LANGUAGE_ID
**/
  public String getLanguageID()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return(String)getValue(LANGUAGE_ID);
  }



/**
Returns a value indicating whether or not messages are logged for
CL programs.

@return The value indicating whether or not messages are logged for
        CL programs. Possible values are: {@link #LOG_CL_PROGRAMS_YES LOG_CL_PROGRAMS_YES }  and
        {@link #LOG_CL_PROGRAMS_NO LOG_CL_PROGRAMS_NO }.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOG_CL_PROGRAMS
**/
  public String getLoggingCLPrograms()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(LOG_CL_PROGRAMS)).trim();
  }



// @D1C
/**
Returns the type of information logged.

@return The type of information logged.  Possible values are:
        <ul>
        <li>0  - No messages are logged.
        <li>1  - All messages sent
            to the job's external message queue with a severity greater than or equal to
            the message logging severity are logged.
        <li>2  -
            Requests or commands from CL programs for which the system issues messages with
            a severity code greater than or equal to the logging severity and all messages
            associated with those requests or commands that have a severity code greater
            than or equal to the logging severity are logged.
        <li>3  - All requests or commands from CL programs and all messages
            associated with those requests or commands that have a severity code greater
            than or equal to the logging severity are logged.
        <li>4  - All requests or commands from CL programs and all messages
            with a severity code greater than or equal to the logging severity are logged.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOGGING_LEVEL
**/
  public int getLoggingLevel()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return Integer.parseInt((String)getValue(LOGGING_LEVEL)); // @D1C
  }



/**
Returns the minimum severity level that causes error messages to be logged
in the job log.

@return The minimum severity level that causes error messages to be logged
        in the job log.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOGGING_SEVERITY
**/
  public int getLoggingSeverity()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(LOGGING_SEVERITY);
  }



/**
Returns the level of message text that is written in the job log
or displayed to the user.

@return The level of message text that is written in the job log
        or displayed to the user.  Possible values are:
        <ul>
        <li>{@link #LOGGING_TEXT_MESSAGE LOGGING_TEXT_MESSAGE }  - Only the message is written to the job log.
        <li>{@link #LOGGING_TEXT_SECLVL LOGGING_TEXT_SECLVL }  - Both the message and the message help for the
            error message are written to the job log.
        <li>{@link #LOGGING_TEXT_NO_LIST LOGGING_TEXT_NO_LIST }  - If the job ends normally, there is no job log.
            If the job ends abnormally, there is a job log.  The messages appearing in the
            job log contain both the message and the message help.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOGGING_TEXT
**/
  public String getLoggingText()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(LOGGING_TEXT)).trim();
  }



/**
Returns the mode name of the advanced program-to-program
communications (APPC) device that started the job.

@return The mode name of the advanced program-to-program
        communications (APPC) device that started the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #MODE
**/
  public String getModeName()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(MODE)).trim();
  }



/**
Returns the job name.

@return The job name.
**/
  public String getName()
  {
    return name_;
  }



/**
Returns the job number.

@return The job number.
**/
  public String getNumber()
  {
    return number_;
  }



/**
Returns the number of libraries in the system portion of the library list of the
initial thread.

@return The number of libraries in the system portion of the library list of the
        initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SYSTEM_LIBRARY_LIST
**/
  public int getNumberOfLibrariesInSYSLIBL()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getSystemLibraryList().length;
  }


/**
Returns the number of libraries in the user portion of the library list of
the initial thread.

@return The number of libraries in the user portion of the library list of
the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #USER_LIBRARY_LIST
**/
  public int getNumberOfLibrariesInUSRLIBL()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getUserLibraryList().length;
  }



/**
Returns the number of libraries that contain product information
for the initial thread.

@return The number of libraries that contain product information
        for the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRODUCT_LIBRARIES
**/
  public int getNumberOfProductLibraries()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getProductLibraries().length;
  }



/**
Returns the fully qualified integrated file system path name of the default
output queue that is used for spooled output produced by this job.

@return The fully qualified integrated file system path name of the default
        output queue that is used for spooled output produced by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #OUTPUT_QUEUE
@see QSYSObjectPathName
**/
  public String getOutputQueue()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
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
Returns the output priority for spooled files that this job produces.

@return The output priority for spooled files that this job produces.
        The highest priority is 0 and the lowest is 9.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #OUTPUT_QUEUE_PRIORITY
**/
  public int getOutputQueuePriority()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(OUTPUT_QUEUE_PRIORITY);
  }



/**
Returns the identifier of the system-related pool from which the job's
main storage is allocated.

@return The identifier of the system-related pool from which the job's
        main storage is allocated.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SYSTEM_POOL_ID
**/
  public int getPoolIdentifier()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(SYSTEM_POOL_ID);
  }



/**
Returns the printer device used for printing output from this job.

@return The printer device used for printing output from this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRINTER_DEVICE_NAME
**/
  public String getPrinterDeviceName()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return ((String)getValue(PRINTER_DEVICE_NAME)).trim();
  }



/**
Returns a value indicating whether border and header information is provided when
the Print key is pressed.

@return The value indicating whether border and header information is provided when
        the Print key is pressed.
        <ul>
        <li>{@link #PRINT_KEY_FORMAT_NONE PRINT_KEY_FORMAT_NONE }  - The border and header information is not
            included with output from the Print key.
        <li>{@link #PRINT_KEY_FORMAT_BORDER PRINT_KEY_FORMAT_BORDER }  - The border information
            is included with output from the Print key.
        <li>{@link #PRINT_KEY_FORMAT_HEADER PRINT_KEY_FORMAT_HEADER }  - The header information
            is included with output from the Print key.
        <li>{@link #PRINT_KEY_FORMAT_ALL PRINT_KEY_FORMAT_ALL }  - The border and header information
            is included with output from the Print key.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRINT_KEY_FORMAT
**/
  public String getPrintKeyFormat()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(PRINT_KEY_FORMAT)).trim();
  }



/**
Returns the line of text, if any, that is printed at the
bottom of each page of printed output for the job.

@return The line of text, if any, that is printed at the
        bottom of each page of printed output for the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRINT_TEXT
**/
  public String getPrintText()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(PRINT_TEXT)).trim();
  }



/**
Returns the libraries that contain product information for the initial thread.

@return The libraries that contain product information for the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRODUCT_LIBRARIES
**/
  public String[] getProductLibraries()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    String val = (String)getValue(PRODUCT_LIBRARIES);
    StringTokenizer st = new StringTokenizer(val, " ");
    String[] libraries = new String[st.countTokens()];
    int i=0;
    while (st.hasMoreTokens())
    {
      libraries[i++] = st.nextToken();
    }
    return libraries;
  }



  //@A2A
/**
Indicates whether the job is eligible to be moved out of main storage
and put into auxiliary storage at the end of a time slice or when it is
beginning a long wait.

@return true the job is eligible to be moved out of main storage
        and put into auxiliary storage at the end of a time slice or when it is
        beginning a long wait, or false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #ELIGIBLE_FOR_PURGE
**/
  public boolean getPurge()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(ELIGIBLE_FOR_PURGE)).trim().equals(ELIGIBLE_FOR_PURGE_YES); //@E2C
  }



/**
Returns the fully qualified integrated file system path name
of the job queue that the job is on, or that the job was on if
it is active.

@return The fully qualified integrated file system path name
        of the job queue that the job is on, or that the job was on if
        it is active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_QUEUE
@see QSYSObjectPathName
**/
  public String getQueue()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
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
Returns the scheduling priority of the job compared to other jobs on the same job queue.

@return The scheduling priority of the job compared to other jobs on the same job queue.
        The highest priority is 0 and the lowest is 9. If this job's status is *OUTQ,
        then the priority returned is -1.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_QUEUE_PRIORITY
**/
  public int getQueuePriority()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    String p = ((String)getValue(JOB_QUEUE_PRIORITY)).trim();
    if (p.length() == 0) return -1;
    return (new Integer(p)).intValue();
  }


/**
Returns the routing data that is used to determine the routing entry
that identifies the program to start for the routing step.

@return The routing data that is used to determine the routing entry
that identifies the program to start for the routing step.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #ROUTING_DATA
**/
  public String getRoutingData()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(ROUTING_DATA)).trim();
  }


/**
Returns the priority at which the job is currently running, relative
to other jobs on the system.

@return The priority at which the job is currently running, relative
        to other jobs on the system.  The run priority ranges from 1
        (highest priority) to 99 (lowest priority).

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #RUN_PRIORITY
**/
  public int getRunPriority()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(RUN_PRIORITY);
  }



/**
Indicates whether the job is to be treated like a signed-on user on
the system.

@return true if the job is to be treated like a signed-on user on
        the system, false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SIGNED_ON_JOB
**/
  public boolean getSignedOnJob()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return ((String)getValue(SIGNED_ON_JOB)).trim().equals("0");
  }



/**
Returns the fully qualified integrated file system path name of the
sort sequence table.

@return The fully qualified integrated file system path name of the
        sort sequence table.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SORT_SEQUENCE_TABLE
@see QSYSObjectPathName
**/
  public String getSortSequenceTable()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
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
Returns the job status.

@return The job status. Possible values are:
        <ul>
        <li>{@link #JOB_STATUS_ACTIVE JOB_STATUS_ACTIVE }  - The job is active.
        <li>{@link #JOB_STATUS_JOBQ JOB_STATUS_JOBQ }  - The job is currently on a job queue.
        <li>{@link #JOB_STATUS_OUTQ JOB_STATUS_OUTQ }  - The job has completed running, but still has output
            on an output queue.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_STATUS
**/
  public String getStatus()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(JOB_STATUS)).trim();
  }



/**
Returns a value indicating status messages are displayed for this job.

@return The value indicating status messages are displayed for this job.
        <ul>
        <li>{@link #STATUS_MESSAGE_HANDLING_NONE STATUS_MESSAGE_HANDLING_NONE }  -
            This job does not display status messages.
        <li>{@link #STATUS_MESSAGE_HANDLING_NORMAL STATUS_MESSAGE_HANDLING_NORMAL }  -
            This job displays status messages.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #STATUS_MESSAGE_HANDLING
**/
  public String getStatusMessageHandling()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(STATUS_MESSAGE_HANDLING)).trim();
  }



/**
Returns the fully qualified integrated file system path name of the
subsystem description for the subsystem in which the job is running.

@return The fully qualified integrated file system path name of the
        subsystem description for the subsystem in which the job is
        running.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SUBSYSTEM
@see QSYSObjectPathName
**/
  public String getSubsystem()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
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
Returns additional information about the job type.

@return Additional information about the job type. Possible values are:
        <ul>
        <li>{@link #JOB_SUBTYPE_BLANK JOB_SUBTYPE_BLANK }  - The job has no special subtype or is not a valid job.
        <li>{@link #JOB_SUBTYPE_IMMEDIATE JOB_SUBTYPE_IMMEDIATE }  - The job is an immediate job.
        <li>{@link #JOB_SUBTYPE_PROCEDURE_START_REQUEST JOB_SUBTYPE_PROCEDURE_START_REQUEST }  - The job started
            with a procedure start request.
        <li>{@link #JOB_SUBTYPE_MACHINE_SERVER_JOB JOB_SUBTYPE_MACHINE_SERVER_JOB }  - The job is an AS/400
            Advanced 36 machine server job.
        <li>{@link #JOB_SUBTYPE_PRESTART JOB_SUBTYPE_PRESTART }  - The job is a prestart job.
        <li>{@link #JOB_SUBTYPE_PRINT_DRIVER JOB_SUBTYPE_PRINT_DRIVER }  - The job is a print driver job.
        <li>{@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT }  - The job is a System/36 multiple requester terminal
            (MRT) job.
        <li>{@link #JOB_SUBTYPE_ALTERNATE_SPOOL_USER JOB_SUBTYPE_ALTERNATE_SPOOL_USER }  - Alternate spool user.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_SUBTYPE
**/
  public String getSubtype()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException

  {
    return(String)getValue(JOB_SUBTYPE); // Don't trim.
//    return subtype_;
  }



/**
Returns the system.

@return The system.
**/
  public AS400 getSystem()
  {
    return system_;
  }



/**
Returns the system portion of the library list of the initial thread.

@return The system portion of the library list of the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SYSTEM_LIBRARY_LIST
**/
  public String[] getSystemLibraryList()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    String val = (String)getValue(SYSTEM_LIBRARY_LIST);
    StringTokenizer st = new StringTokenizer(val, " ");
    String[] libraries = new String[st.countTokens()];
    int i=0;
    while (st.hasMoreTokens())
    {
      libraries[i++] = st.nextToken();
    }
    return libraries;
  }



/**
Returns the value used to separate hours, minutes, and seconds when presenting
a time.

@return The value used to separate hours, minutes, and seconds when presenting
        a time.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #TIME_SEPARATOR
**/
  public String getTimeSeparator()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return(String)getValue(TIME_SEPARATOR); // Don't trim.
  }



  //@A2A
/**
Returns the maximum amount of processor time (in milliseconds) given to
each thread in this job before other threads in this job and in other
jobs are given the opportunity to run.

@return The maximum amount of processor time (in milliseconds) given to
        each thread in this job before other threads in this job and in other
        jobs are given the opportunity to run.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #TIME_SLICE
**/
  public int getTimeSlice()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(TIME_SLICE);
  }



  //@A2A
/**
Returns a value indicating whether a thread in an interactive
job moves to another main storage pool at the end of its time slice.

@return The value indicating whether a thread in an interactive
        job moves to another main storage pool at the end of its time slice.
        Possible values are:
        <ul>
        <li>{@link #TIME_SLICE_END_POOL_NONE TIME_SLICE_END_POOL_NONE }  -
            A thread in the job does not move to another main storage pool when it reaches
            the end of its time slice.
        <li>{@link #TIME_SLICE_END_POOL_BASE TIME_SLICE_END_POOL_BASE }  -
            A thread in the job moves to the base pool when it reaches
            the end of its time slice.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #TIME_SLICE_END_POOL
**/
  public String getTimeSliceEndPool()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(TIME_SLICE_END_POOL)).trim();
  }



/**
Returns the total amount of response time (in milliseconds) for the
initial thread.

@return The total amount of response time (in milliseconds) for the
        initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #TOTAL_RESPONSE_TIME
**/
  public int getTotalResponseTime()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return getAsInt(TOTAL_RESPONSE_TIME);
  }



/**
Returns the job type.

@return The job type. Possible values are:
        <ul>
        <li>{@link #JOB_TYPE_NOT_VALID JOB_TYPE_NOT_VALID }  - The job is not a valid job.
        <li>{@link #JOB_TYPE_AUTOSTART JOB_TYPE_AUTOSTART }  - The job is an autostart job.
        <li>{@link #JOB_TYPE_BATCH JOB_TYPE_BATCH }  - The job is a batch job.
        <li>{@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE }  - The job is an interactive job.
        <li>{@link #JOB_TYPE_SUBSYSTEM_MONITOR JOB_TYPE_SUBSYSTEM_MONITOR }  - The job is a subsystem monitor job.
        <li>{@link #JOB_TYPE_SPOOLED_READER JOB_TYPE_SPOOLED_READER }  - The job is a spooled reader job.
        <li>{@link #JOB_TYPE_SYSTEM JOB_TYPE_SYSTEM }  - The job is a system job.
        <li>{@link #JOB_TYPE_SPOOLED_WRITER JOB_TYPE_SPOOLED_WRITER }  - The job is a spooled writer job.
        <li>{@link #JOB_TYPE_SCPF_SYSTEM JOB_TYPE_SCPF_SYSTEM }  - The job is the SCPF system job.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_TYPE
**/
  public String getType()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException

  {
    return(String)getValue(JOB_TYPE); // Don't trim.
    //return type_;
  }



/**
Returns the user name.

@return The user name.
**/
  public String getUser()
  {
    return user_;
  }



/**
Returns the user portion of the library list of
the initial thread.

@return The user portion of the library list of
        the initial thread.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #USER_LIBRARY_LIST
**/
  public String[] getUserLibraryList()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    String val = (String)getValue(USER_LIBRARY_LIST);
    StringTokenizer st = new StringTokenizer(val, " ");
    String[] libraries = new String[st.countTokens()];
    int i=0;
    while (st.hasMoreTokens())
    {
      libraries[i++] = st.nextToken();
    }
    return libraries;
  }


  /**
   * Returns the value for the specified job attribute. This is a generic way of 
   * retrieving job attributes, rather than using the specific getter methods.
   * This method will either go to the system to retrieve the job attribute, or it
   * will return a cached value if the attribute was previously retrieved or previously
   * set by setValue() or one of the other setter methods. Use {@link #loadInformation 
   * loadInformation()} to refresh the attributes from the system.
   * @param attribute The job attribute.
   * @return The current value of the attribute.  This method may return null in the rare
   * case that the specified attribute could not be retrieved using the QUSRJOBI system API.
   * @see #loadInformation
   * @see #setValue
  **/
  public Object getValue(int attribute) throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    Object obj = values_.get(attribute);
    if (obj == null)
    {
      retrieve(attribute); // Need to retrieve it using QUSRJOBI
      obj = values_.get(attribute);
      if (obj == null && (attribute == SCHEDULE_DATE || attribute == SCHEDULE_TIME)) // These are only setters.
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
Returns the unit of work identifier. The unit of work identifier is used to
track jobs across multiple systems. If a job is not associated with a source or
target system using advanced program-to-program communications (APPC),
this information is not used. Every job on the system is assigned a unit
of work identifier.

@return The unit of work identifier, which is made up of:
        <ul>
        <li>Location name - 8 Characters. The name of the source system that
                            originated the APPC job.
        <li>Network ID - 8 Characters. The network name associated with the
                         unit of work.
        <li>Instance - 6 Characters. The value that further identifies the
                        source of the job. This is shown as hexadecimal data.
        <li>Sequence Number - 2 Character. A value that identifies a check-point
                              within the application program.
        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOCATION_NAME
@see #NETWORK_ID
@see #INSTANCE
@see #SEQUENCE_NUMBER
**/
  public String getWorkIDUnit()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    return((String)getValue(UNIT_OF_WORK_ID)).trim();    
  }


  /**
   * Holds this job.
   * @param holdSpooledFiles true to hold this job's spooled files; false otherwise.
   * @see #end
   * @see #release
  **/
  public void hold(boolean holdSpooledFiles) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
  {
    StringBuffer buf = new StringBuffer();
    buf.append("QSYS/HLDJOB JOB(");
    buf.append(number_);
    buf.append('/');
    buf.append(user_);
    buf.append('/');
    buf.append(name_);
    buf.append(") SPLFILE(");
    buf.append(holdSpooledFiles ? "*YES)" : "*NO)");
    buf.append(" DUPJOBOPT(*MSG)");
    String toRun = buf.toString();
    // If the user wants to end the remote command server job that is servicing our connection, 
    // they are welcome to "shoot themselves in the foot".
    CommandCall cmd = new CommandCall(system_, toRun);
    if (!cmd.run())
    {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   * Helper method. Used to determine if an attribute that is being set is read-only
   * and should not be allowed to be changed.
  **/
  private static boolean isReadOnly(int attribute)
  {
    return setterKeys_.get(attribute) == 0;
  }


  /**
   * Refreshes the values for all attributes. This does not cancel uncommitted changes.
   * To refresh just the elapsed statistics, use {@link #loadStatistics loadStatistics()}.
   * @see #loadInformation(int[])
   * @see #commitChanges
   * @see #loadStatistics
  **/
  public void loadInformation()
  {
    // Need to load an attribute from each format.
    try
    {
      // @F0A - begin
      // Clear all values.
      values_.clear();
      
      // Reset all of the important information
//      if (internalJobID_ == null || internalJobID_.equals(""))
//      {
        setValueInternal(INTERNAL_JOB_ID, null);
        setValueInternal(INTERNAL_JOB_IDENTIFIER, null);
        setValueInternal(JOB_NAME, name_);
        setValueInternal(USER_NAME, user_);
        setValueInternal(JOB_NUMBER, number_);
//      }
//      else
//      {
//        setValueInternal(INTERNAL_JOB_ID, internalJobID_);
//        setValueInternal(JOB_NAME, null);
//        setValueInternal(USER_NAME, null);
//        setValueInternal(JOB_NUMBER, null);
//      }
      setValueInternal(JOB_STATUS, status_);
      setValueInternal(JOB_TYPE, type_);
      setValueInternal(JOB_SUBTYPE, subtype_);
      // @F0A - end

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
      if (Trace.traceOn_)
      {
        Trace.log(Trace.ERROR, "Error loading job information: ", e);
      }
    }
  }


  /**
   * Refreshes the values for specific attributes. This does not cancel uncommitted changes.
   * <br>Note: The specified attributes will be refreshed, along with other attributes in their "format group".  For more information about attribute format groups, refer to the specification of the QUSRJOBI API.
   * @param attributes The attributes to refresh.
   * @see #loadInformation()
   * @see #commitChanges
   * @see #loadStatistics
  **/
  public void loadInformation(int[] attributes)
  {
    // Load only the formats needed for the specified attributes.
    try
    {

      // Determine which formats we need to specify.
      Vector formats = new Vector(attributes.length);
      for (int i=0; i<attributes.length; i++)
      {
        int attr = attributes[i];
        String format = lookupFormatName(attr);
        if (!formats.contains(format)) formats.add(format);

        // Clear/reset attr values, for consistency with loadInformation().

        values_.remove(attr);  // clear the value for that attribute
        switch (attr) {
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
          default:  // do nothing
        }
      }

      // Retrieve values.  For each needed format, just specify any attribute associated with that format.
      if (formats.contains("JOBI0150"))  retrieve(THREAD_COUNT);
      if (formats.contains("JOBI0200"))  retrieve(CURRENT_SYSTEM_POOL_ID);
      if (formats.contains("JOBI0300"))  retrieve(JOB_DATE);
      if (formats.contains("JOBI0400"))  retrieve(SERVER_TYPE);
      if (formats.contains("JOBI0500"))  retrieve(LOGGING_TEXT);
      if (formats.contains("JOBI0600"))  retrieve(SPECIAL_ENVIRONMENT);
      if (formats.contains("JOBI0700"))  retrieve(USER_LIBRARY_LIST);
      if (formats.contains("JOBI1000"))  retrieve(ELAPSED_CPU_TIME_USED);
    }
    catch (Exception e)
    {
      if (Trace.traceOn_)
      {
        Trace.log(Trace.ERROR, "Error loading job information: ", e);
      }
    }
  }


  /**
   * Refreshes just the values for the elapsed statistics. Internally, this
   * calls the QUSRJOBI API using the JOBI1000 format.
   * @see #resetStatistics
   * @see #loadInformation
   * @see #ELAPSED_TIME
   * @see #ELAPSED_DISK_IO
   * @see #ELAPSED_DISK_IO_ASYNCH
   * @see #ELAPSED_DISK_IO_SYNCH
   * @see #ELAPSED_INTERACTIVE_RESPONSE_TIME
   * @see #ELAPSED_INTERACTIVE_TRANSACTIONS
   * @see #ELAPSED_CPU_PERCENT_USED
   * @see #ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
   * @see #ELAPSED_CPU_TIME_USED
   * @see #ELAPSED_CPU_TIME_USED_FOR_DATABASE
   * @see #ELAPSED_LOCK_WAIT_TIME
   * @see #ELAPSED_PAGE_FAULTS
  **/
  public void loadStatistics()
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    retrieve(ELAPSED_CPU_TIME_USED);  // 1000
  }


  /**
   * Helper method. Used to determine the length of the output parameter for a given format.
  **/
  private static int lookupFormatLength(String name)
  {
    //if (name == "JOBI0100") return 86;
    if (name == "JOBI0150") return 144;
    if (name == "JOBI0200") return 191;
    if (name == "JOBI0300") return 187;
    if (name == "JOBI0400") return 521;
    if (name == "JOBI0500") return 83;
    if (name == "JOBI0600") return 322;
    if (name == "JOBI0700") return 80+(43*11); // (using max possible num of libraries in each list, it totals to 43)
//    if (name == "JOBI0750") return 100+(43*60);
//    if (name == "JOBI0800") return 96+(10*32); // (10 is used since I don't know the max number of signal monitor entries possible)
//    if (name == "JOBI0900") return 92+(10*80); // (10 is used since I don't know the max number of SQL open cursors possible)
    if (name == "JOBI1000") return 144;
    return -1;
  }


  /**
   * Helper method used to determine the format name based on which job attribute
   * we want to retrieve.
  **/
  private static String lookupFormatName(int key)
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
        return "JOBI0150";

//      case RUN_PRIORITY:
//      case SYSTEM_POOL_ID:
//      case CPU_TIME_USED:
      case AUXILIARY_IO_REQUESTS:
      case INTERACTIVE_TRANSACTIONS:
      case TOTAL_RESPONSE_TIME:
      case FUNCTION_TYPE:
      case FUNCTION_NAME:
      case ACTIVE_JOB_STATUS:
      case CURRENT_SYSTEM_POOL_ID:
//      case THREAD_COUNT:
//      case CPU_TIME_USED_LARGE:
      case AUXILIARY_IO_REQUESTS_LARGE:
      case CPU_TIME_USED_FOR_DATABASE:
//      case PAGE_FAULTS:
      case ACTIVE_JOB_STATUS_FOR_JOBS_ENDING:
      case MEMORY_POOL:
      case MESSAGE_REPLY:
        return "JOBI0200";

      case JOB_QUEUE:
      case JOB_QUEUE_PRIORITY:
      case OUTPUT_QUEUE:
      case OUTPUT_QUEUE_PRIORITY:
      case PRINTER_DEVICE_NAME:
//      case SUBMITTED_BY_JOB_NAME:
//      case SUBMITTED_BY_USER:
//      case SUBMITTED_BY_JOB_NUMBER:
      case JOB_QUEUE_STATUS:
      case JOB_QUEUE_DATE:
      case JOB_DATE:
        return "JOBI0300";

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
      case SCHEDULE_DATE: // In case someone asks for it
      case SCHEDULE_TIME: // In case someone asks for it
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
      case JOB_LOG_PENDING:
      case JOB_END_REASON:
      case JOB_TYPE_ENHANCED:
      case DATE_ENDED:
        return "JOBI0400";

      case END_SEVERITY:
      case LOGGING_SEVERITY:
      case LOGGING_LEVEL:
      case LOGGING_TEXT:
        return "JOBI0500";

//      case JOB_SWITCHES:
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
        return "JOBI0600";

      case CURRENT_LIBRARY_EXISTENCE:
      case SYSTEM_LIBRARY_LIST:
      case PRODUCT_LIBRARIES:
      case CURRENT_LIBRARY:
      case USER_LIBRARY_LIST:
        return "JOBI0700";

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
        return "JOBI1000";

      default:
        return null;
    }
  }


  /**
   * Helper method. Used to walk through the output parameter after an API call and set
   * all of the job attribute data into our internal table.
  **/
  private void parseData(String format, byte[] data) throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException

  {
    int ccsid = system_.getCcsid();
    ConvTable table = ConvTable.getTable(ccsid, null);        

    // All the formats return these

    name_ = table.byteArrayToString(data, 8, 10).trim();
    user_ = table.byteArrayToString(data, 18, 10).trim();
    number_ = table.byteArrayToString(data, 28, 6);

    //internalJobID_ = table.byteArrayToString(data, 34, 16);
    realInternalJobID_ = new byte[16];
    System.arraycopy(data, 34, realInternalJobID_, 0, 16);
    char[] oldID = new char[16];
    for (int i=0; i<16; ++i)
    {
      oldID[i] = (char)(realInternalJobID_[i] & 0x00FF);
    }
    internalJobID_ = new String(oldID);

    status_ = table.byteArrayToString(data, 50, 10).trim();
    type_ = table.byteArrayToString(data, 60, 1);
    subtype_ = table.byteArrayToString(data, 61, 1);    

    setValueInternal(JOB_NAME, name_);
    setValueInternal(USER_NAME, user_);
    setValueInternal(JOB_NUMBER, number_);
    
    setValueInternal(INTERNAL_JOB_ID, internalJobID_);
    setValueInternal(INTERNAL_JOB_IDENTIFIER, realInternalJobID_);

    setValueInternal(JOB_STATUS, status_);
    setValueInternal(JOB_TYPE, type_);
    setValueInternal(JOB_SUBTYPE, subtype_);
//    if (internalJobID_ == null)
//    {
//      setValueInternal(INTERNAL_JOB_ID, table.byteArrayToString(data, 34, 16));
//    }

    if (format == "JOBI0150")
    {
      setAsInt(RUN_PRIORITY, BinaryConverter.byteArrayToInt(data, 64));
      setAsInt(TIME_SLICE, BinaryConverter.byteArrayToInt(data, 68));
      setAsInt(DEFAULT_WAIT_TIME, BinaryConverter.byteArrayToInt(data, 72));
      setValueInternal(ELIGIBLE_FOR_PURGE, table.byteArrayToString(data, 76, 10));
      setValueInternal(TIME_SLICE_END_POOL, table.byteArrayToString(data, 86, 10));
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
    }
    else if (format == "JOBI0200")
    {
//      setValueInternal(SUBSYSTEM, table.byteArrayToString(data, 62, 10));
      setAsInt(RUN_PRIORITY, BinaryConverter.byteArrayToInt(data, 72));
      setAsInt(SYSTEM_POOL_ID, BinaryConverter.byteArrayToInt(data, 76));
      setAsInt(CPU_TIME_USED, BinaryConverter.byteArrayToInt(data, 80));
      setAsInt(AUXILIARY_IO_REQUESTS, BinaryConverter.byteArrayToInt(data, 84));
      setAsInt(INTERACTIVE_TRANSACTIONS, BinaryConverter.byteArrayToInt(data, 88));
      setAsInt(TOTAL_RESPONSE_TIME, BinaryConverter.byteArrayToInt(data, 92));
      setValueInternal(FUNCTION_TYPE, table.byteArrayToString(data, 96, 1));
      setValueInternal(FUNCTION_NAME, table.byteArrayToString(data, 97, 10));
      setValueInternal(ACTIVE_JOB_STATUS, table.byteArrayToString(data, 107, 4));
      setAsInt(CURRENT_SYSTEM_POOL_ID, BinaryConverter.byteArrayToInt(data, 136));
      setAsInt(THREAD_COUNT, BinaryConverter.byteArrayToInt(data, 140));
//      setAsLong(CPU_TIME_USED_LARGE, BinaryConverter.byteArrayToLong(data, 144));
      setAsLong(AUXILIARY_IO_REQUESTS_LARGE, BinaryConverter.byteArrayToLong(data, 152));
      setAsLong(CPU_TIME_USED_FOR_DATABASE, BinaryConverter.byteArrayToLong(data, 160));
//      setAsLong(PAGE_FAULTS, BinaryConverter.byteArrayToLong(data, 168));
      setValueInternal(ACTIVE_JOB_STATUS_FOR_JOBS_ENDING, table.byteArrayToString(data, 176, 4));
      setValueInternal(MEMORY_POOL, table.byteArrayToString(data, 180, 10));
      setValueInternal(MESSAGE_REPLY, table.byteArrayToString(data, 190, 1));
    }
    else if (format == "JOBI0300")
    {
      setValueInternal(JOB_QUEUE, table.byteArrayToString(data, 62, 20));
      setValueInternal(JOB_QUEUE_PRIORITY, table.byteArrayToString(data, 82, 2));
      setValueInternal(OUTPUT_QUEUE, table.byteArrayToString(data, 84, 20));
      setValueInternal(OUTPUT_QUEUE_PRIORITY, table.byteArrayToString(data, 104, 2));
      setValueInternal(PRINTER_DEVICE_NAME, table.byteArrayToString(data, 106, 10));
      setValueInternal(SUBMITTED_BY_JOB_NAME, table.byteArrayToString(data, 116, 10));
      setValueInternal(SUBMITTED_BY_USER, table.byteArrayToString(data, 126, 10));
      setValueInternal(SUBMITTED_BY_JOB_NUMBER, table.byteArrayToString(data, 136, 6));
      setValueInternal(JOB_QUEUE_STATUS, table.byteArrayToString(data, 162, 10));
      byte[] val = new byte[8];
      System.arraycopy(data, 172, val, 0, 8);
      setValueInternal(JOB_QUEUE_DATE, val);
      setValueInternal(JOB_DATE, table.byteArrayToString(data, 180, 7));
    }
    else if (format == "JOBI0400")
    {
      setValueInternal(DATE_ENTERED_SYSTEM, table.byteArrayToString(data, 62, 13));
      setValueInternal(DATE_STARTED, table.byteArrayToString(data, 75, 13));
      setValueInternal(ACCOUNTING_CODE, table.byteArrayToString(data, 88, 15));
      setValueInternal(JOB_DESCRIPTION, table.byteArrayToString(data, 103, 20));

      // Unit of work ID.
      setValueInternal(UNIT_OF_WORK_ID, table.byteArrayToString(data, 123, 24));
      setValueInternal(LOCATION_NAME, table.byteArrayToString(data, 123, 8));
      setValueInternal(NETWORK_ID, table.byteArrayToString(data, 131, 8));
      setValueInternal(INSTANCE, table.byteArrayToString(data, 139, 6));
      setValueInternal(SEQUENCE_NUMBER, table.byteArrayToString(data, 145, 2));

      setValueInternal(MODE, table.byteArrayToString(data, 147, 8));
      setValueInternal(INQUIRY_MESSAGE_REPLY, table.byteArrayToString(data, 155, 10));
      setValueInternal(LOG_CL_PROGRAMS, table.byteArrayToString(data, 165, 10));
      setValueInternal(BREAK_MESSAGE_HANDLING, table.byteArrayToString(data, 175, 10));
      setValueInternal(STATUS_MESSAGE_HANDLING, table.byteArrayToString(data, 185, 10));
      setValueInternal(DEVICE_RECOVERY_ACTION, table.byteArrayToString(data, 195, 13));
      setValueInternal(KEEP_DDM_CONNECTIONS_ACTIVE, table.byteArrayToString(data, 208, 10));
      setValueInternal(DATE_SEPARATOR, table.byteArrayToString(data, 218, 1));
      setValueInternal(DATE_FORMAT, table.byteArrayToString(data, 219, 4));
      setValueInternal(PRINT_TEXT, table.byteArrayToString(data, 223, 30));
      setValueInternal(SUBMITTED_BY_JOB_NAME, table.byteArrayToString(data, 253, 10));
      setValueInternal(SUBMITTED_BY_USER, table.byteArrayToString(data, 263, 10));
      setValueInternal(SUBMITTED_BY_JOB_NUMBER, table.byteArrayToString(data, 273, 6));
      setValueInternal(TIME_SEPARATOR, table.byteArrayToString(data, 299, 1));
      setAsInt(CCSID, BinaryConverter.byteArrayToInt(data, 300));
      byte[] val = new byte[8];
      System.arraycopy(data, 304, val, 0, 8);
      setValueInternal(SCHEDULE_DATE_GETTER, val);
      setValueInternal(PRINT_KEY_FORMAT, table.byteArrayToString(data, 312, 10));
      setValueInternal(SORT_SEQUENCE_TABLE, table.byteArrayToString(data, 322, 20));
      setValueInternal(LANGUAGE_ID, table.byteArrayToString(data, 342, 3));
      setValueInternal(COUNTRY_ID, table.byteArrayToString(data, 345, 2));
      setValueInternal(COMPLETION_STATUS, table.byteArrayToString(data, 347, 1));
      setValueInternal(SIGNED_ON_JOB, table.byteArrayToString(data, 348, 1));
      setValueInternal(JOB_SWITCHES, table.byteArrayToString(data, 349, 8));
      setValueInternal(MESSAGE_QUEUE_ACTION, table.byteArrayToString(data, 357, 10));
      setAsInt(MESSAGE_QUEUE_MAX_SIZE, BinaryConverter.byteArrayToInt(data, 368));
      setAsInt(DEFAULT_CCSID, BinaryConverter.byteArrayToInt(data, 372));
      setValueInternal(ROUTING_DATA, table.byteArrayToString(data, 376, 80));
      setValueInternal(DECIMAL_FORMAT, table.byteArrayToString(data, 456, 1));
      setValueInternal(CHARACTER_ID_CONTROL, table.byteArrayToString(data, 457, 10));
      setValueInternal(SERVER_TYPE, table.byteArrayToString(data, 467, 30));
      setValueInternal(ALLOW_MULTIPLE_THREADS, table.byteArrayToString(data, 497, 1));
      setValueInternal(JOB_LOG_PENDING, table.byteArrayToString(data, 498, 1));
      setAsInt(JOB_END_REASON, BinaryConverter.byteArrayToInt(data, 500));
      setAsInt(JOB_TYPE_ENHANCED, BinaryConverter.byteArrayToInt(data, 504));
      setValueInternal(DATE_ENDED, table.byteArrayToString(data, 508, 13));
    }
    else if (format == "JOBI0500")
    {
      setAsInt(END_SEVERITY, BinaryConverter.byteArrayToInt(data, 64));
      setAsInt(LOGGING_SEVERITY, BinaryConverter.byteArrayToInt(data, 68));
      setValueInternal(LOGGING_LEVEL, table.byteArrayToString(data, 72, 1));
      setValueInternal(LOGGING_TEXT, table.byteArrayToString(data, 73, 10));
    }
    else if (format == "JOBI0600")
    {
      setValueInternal(JOB_SWITCHES, table.byteArrayToString(data, 62, 8));
      setValueInternal(CONTROLLED_END_REQUESTED, table.byteArrayToString(data, 70, 1));
      setValueInternal(SUBSYSTEM, table.byteArrayToString(data, 71, 20));
      setValueInternal(CURRENT_USER, table.byteArrayToString(data, 91, 10));
      setValueInternal(DBCS_CAPABLE, table.byteArrayToString(data, 101, 1));
      setAsInt(PRODUCT_RETURN_CODE, BinaryConverter.byteArrayToInt(data, 104));
      setAsInt(USER_RETURN_CODE, BinaryConverter.byteArrayToInt(data, 108));
      setAsInt(PROGRAM_RETURN_CODE, BinaryConverter.byteArrayToInt(data, 112));
      setValueInternal(SPECIAL_ENVIRONMENT, table.byteArrayToString(data, 116, 10));
      setValueInternal(JOB_USER_IDENTITY, table.byteArrayToString(data, 296, 10));
      setValueInternal(JOB_USER_IDENTITY_SETTING, table.byteArrayToString(data, 306, 1));
      setValueInternal(CLIENT_IP_ADDRESS, table.byteArrayToString(data, 307, 15));
    }
    else if (format == "JOBI0700")
    {
      int currentLibraryExistence = BinaryConverter.byteArrayToInt(data, 72);
      setAsInt(CURRENT_LIBRARY_EXISTENCE, currentLibraryExistence);
      int numberOfSystemLibraries = BinaryConverter.byteArrayToInt(data, 64);
      setValueInternal(SYSTEM_LIBRARY_LIST, table.byteArrayToString(data, 80, 11*numberOfSystemLibraries));
      int offset = 80 + 11*numberOfSystemLibraries;
      int numberOfProductLibraries = BinaryConverter.byteArrayToInt(data, 68);
      setValueInternal(PRODUCT_LIBRARIES, table.byteArrayToString(data, offset, 11*numberOfProductLibraries));
      offset += 11*numberOfProductLibraries;
      if (currentLibraryExistence == 1)
      {
        setValueInternal(CURRENT_LIBRARY, table.byteArrayToString(data, offset, 11));
        offset += 11;
      }
      else
      {
        setValueInternal(CURRENT_LIBRARY, ""); // Set something so a call to get won't re-retrieve from the system.
      }
      int numberOfUserLibraries = BinaryConverter.byteArrayToInt(data, 76);
      setValueInternal(USER_LIBRARY_LIST, table.byteArrayToString(data, offset, 11*numberOfUserLibraries));
    }
    else if (format == "JOBI1000")
    {
      long elapsedTime = BinaryConverter.byteArrayToLong(data, 64);
      setAsLong(ELAPSED_TIME, elapsedTime);
      long diskIOCountTotal = BinaryConverter.byteArrayToLong(data, 72);
      setAsLong(ELAPSED_DISK_IO, diskIOCountTotal);
      long diskIOCountAsynch = BinaryConverter.byteArrayToLong(data, 80);
      setAsLong(ELAPSED_DISK_IO_ASYNCH, diskIOCountAsynch);
      long diskIOCountSynch = BinaryConverter.byteArrayToLong(data, 88);
      setAsLong(ELAPSED_DISK_IO_SYNCH, diskIOCountSynch);
      int interRespTimeTotal = BinaryConverter.byteArrayToInt(data, 96);
      setAsInt(ELAPSED_INTERACTIVE_RESPONSE_TIME, interRespTimeTotal);
      int interTransCount = BinaryConverter.byteArrayToInt(data, 100);
      setAsInt(ELAPSED_INTERACTIVE_TRANSACTIONS, interTransCount);
      int cpuUsedPercent = BinaryConverter.byteArrayToInt(data, 104);
      setAsInt(ELAPSED_CPU_PERCENT_USED, cpuUsedPercent);
      int cpuUsedDBPercent = BinaryConverter.byteArrayToInt(data, 108);
      setAsInt(ELAPSED_CPU_PERCENT_USED_FOR_DATABASE, cpuUsedDBPercent);
      long cpuUsedTime = BinaryConverter.byteArrayToLong(data, 112);
      setAsLong(ELAPSED_CPU_TIME_USED, cpuUsedTime);
      long cpuUsedDBTime = BinaryConverter.byteArrayToLong(data, 120);
      setAsLong(ELAPSED_CPU_TIME_USED_FOR_DATABASE, cpuUsedDBTime);
      long lockWaitTime = BinaryConverter.byteArrayToLong(data, 128);
      setAsLong(ELAPSED_LOCK_WAIT_TIME, lockWaitTime);
      long pageFaultCountTotal = BinaryConverter.byteArrayToLong(data, 136);
      setAsLong(ELAPSED_PAGE_FAULTS, pageFaultCountTotal);
    }

  }


  /**
   * Releases this job.
   * @see #end
   * @see #hold
  **/
  public void release() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
  {
    StringBuffer buf = new StringBuffer();
    buf.append("QSYS/RLSJOB JOB(");
    buf.append(number_);
    buf.append('/');
    buf.append(user_);
    buf.append('/');
    buf.append(name_);
    buf.append(") DUPJOBOPT(*MSG)");
    String toRun = buf.toString();
    CommandCall cmd = new CommandCall(system_, toRun);
    if (!cmd.run())
    {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


  /**
   * Removes a PropertyChangeListener.
   * @param listener The listener.
   * @see #addPropertyChangeListener
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.removePropertyChangeListener(listener);
  }


  /**
   * Removes a VetoableChangeListener.
   * @param listener The listener.
   * @see #addVetoableChangeListener
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.removeVetoableChangeListener(listener);
  }

  /**
   * Resets the measurement start time used for computing elapsed statistics.
   * @see #loadStatistics
   * @see #ELAPSED_TIME
   * @see #ELAPSED_DISK_IO
   * @see #ELAPSED_DISK_IO_ASYNCH
   * @see #ELAPSED_DISK_IO_SYNCH
   * @see #ELAPSED_INTERACTIVE_RESPONSE_TIME
   * @see #ELAPSED_INTERACTIVE_TRANSACTIONS
   * @see #ELAPSED_CPU_PERCENT_USED
   * @see #ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
   * @see #ELAPSED_CPU_TIME_USED
   * @see #ELAPSED_CPU_TIME_USED_FOR_DATABASE
   * @see #ELAPSED_LOCK_WAIT_TIME
   * @see #ELAPSED_PAGE_FAULTS
  **/
  public void resetStatistics()
    throws AS400Exception,
           AS400SecurityException,
           ConnectionDroppedException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException,
           UnsupportedEncodingException
  {
    retrieve(-1);
  }


  /**
   * Helper method. Used to make the QUSRJOBI API call using the correct format based on the
   * specified attribute.
  **/
  private void retrieve(int key)  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException

  {
    if (!isConnected_) //@E3A - Already validated if we are connected
    {
      if (system_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
      if (name_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET); //@E3A
      }
      if (user_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET); //@E3A
      }
      if (number_ == null) //@E3A
      {
        throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET); //@E3A
      }
    }

    // First lookup the format to use for this key
    String format = (key == -1 ? "JOBI1000" : lookupFormatName(key));
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "QUSRJOBI format: " + format);
    int ccsid = system_.getCcsid();
    int receiverLength = lookupFormatLength(format);

    ProgramParameter[] parmList = (key == -1 ? new ProgramParameter[7] : new ProgramParameter[6]);
    parmList[0] = new ProgramParameter(receiverLength);           
    parmList[1] = new ProgramParameter(bin4_.toBytes(receiverLength));
    AS400Text text = new AS400Text(8, ccsid, system_);
    parmList[2] = new ProgramParameter(text.toBytes(format));
    AS400Text[] member = new AS400Text[3];
    member[0] = new AS400Text(10, ccsid, system_);
    member[1] = new AS400Text(10, ccsid, system_);
    member[2] = new AS400Text(6, ccsid, system_);
    AS400Structure structure = new AS400Structure(member);
    String[] qualifiedJobName =  (realInternalJobID_ != null) ?
                                 new String[] { "*INT", "", "" } : new String[] { name_, user_, number_};
    parmList[3] = new ProgramParameter(structure.toBytes(qualifiedJobName));
    text = new AS400Text(16, ccsid, system_);
    parmList[4] = new ProgramParameter(realInternalJobID_ == null ? BLANKS16_ : realInternalJobID_);
    byte[] errorInfo = new byte[32];
    parmList[5] = new ProgramParameter(errorInfo, 0);

    if (key == -1) parmList[6] = new ProgramParameter(new byte[] { (byte)0xF1 }); // '1' to reset performance statistics

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QUSRJOBI.PGM", parmList);
    if (Trace.traceOn_)
    {
      if (key == -1) Trace.log(Trace.DIAGNOSTIC, "Resetting performance statistics for job "+toString());
      else Trace.log(Trace.DIAGNOSTIC, "Retrieving job information for job "+toString());
    }
    isConnected_ = true; //@E3A
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] retrievedData = parmList[0].getOutputData();
    parseData(format, retrievedData);
  }


  
  /**
   * Helper method. Used to convert a user-specified Date object into a String for our
   * internal table.
  **/
  private void setAsDate(int key, Date val)
  {
    //setValueInternal(key, val.toString());

    String dateString = null;
    Calendar dateTime = Calendar.getInstance();
    dateTime.setTime(val);

    int len = setterKeys_.get(key);
    StringBuffer buf = null;
    switch (len)
    {
      case 10:
        buf = new StringBuffer();

      case 7:
        buf = new StringBuffer();
        int year = dateTime.get(Calendar.YEAR)-1900;
        if (year >= 100)
        {
          buf.append('1');
          year -= 100;
        }
        else
        {
          buf.append('0');
        }
        if (year < 10)
        {
          buf.append('0');
        }
        buf.append(year);
        int month = dateTime.get(Calendar.MONTH)+1;
        if (month < 10)
        {
          buf.append('0');
        }
        buf.append(month);
        int day = dateTime.get(Calendar.DATE);
        if (day < 10)
        {
          buf.append('0');
        }
        buf.append(day);
        dateString = buf.toString();
        break;
      case 6:
        buf = new StringBuffer();
        int hour = dateTime.get(Calendar.HOUR_OF_DAY);
        if (hour < 10)
        {
          buf.append('0');
        }
        buf.append(hour);
        int minute = dateTime.get(Calendar.MINUTE);
        if (minute < 10)
        {
          buf.append('0');
        }
        buf.append(minute);
        int second = dateTime.get(Calendar.SECOND);
        if (second < 10)
        {
          buf.append('0');
        }
        buf.append(second);
        dateString = buf.toString();
      default:
        break;
    }
    setValueInternal(key, dateString);
  }


  /**
   * Helper method. Used to convert a user-specified Date object into a String for our
   * internal table and the table of uncommitted changes.
  **/
  private void setAsDateToChange(int key, Date val)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setAsDate(key, val);
    if (cachedChanges_ == null) cachedChanges_ = new JobHashtable();
    cachedChanges_.put(key, getValue(key));
    if (!cacheChanges_)
    {
      commitChanges();
    }
  }


  /**
   * Helper method. Used after an API call to set the attribute values into our internal table.
  **/
  final void setAsInt(int key, int val)
  {
    setValueInternal(key, new Integer(val));
  }


  /**
   * Helper method. Used when the user calls a setter to set the attribute value into our
   * internal table as well as the table of uncommitted changes.
  **/
  private void setAsIntToChange(int key, int val)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setAsInt(key, val);
    // Update values to set upon commit
    if (cachedChanges_ == null) cachedChanges_ = new JobHashtable();
    cachedChanges_.put(key, getValue(key));

    if (!cacheChanges_)
    {
      commitChanges();
    }
  }


  /**
   * Helper method. Used after an API call to set the attribute values into our internal table.
  **/
  final void setAsLong(int key, long val)
  {
    setValueInternal(key, new Long(val));
  }
  

/**
Sets how this job handles break messages.

@param breakMessageHandling How this job handles break messages.  Possible values are:
                            <ul>
                            <li>{@link #BREAK_MESSAGE_HANDLING_NORMAL BREAK_MESSAGE_HANDLING_NORMAL }
                                - The message queue status determines break message handling.
                            <li>{@link #BREAK_MESSAGE_HANDLING_HOLD BREAK_MESSAGE_HANDLING_HOLD }
                                - The message queue holds break messages until a user or program
                                  requests them.
                            <li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY BREAK_MESSAGE_HANDLING_NOTIFY }
                                - The system notifies the job's message queue when a message
                                  arrives.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #BREAK_MESSAGE_HANDLING
**/
  public void setBreakMessageHandling(String breakMessageHandling)
  throws  AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (breakMessageHandling == null) throw new NullPointerException("breakMessageHandling");

    if (!breakMessageHandling.equals(BREAK_MESSAGE_HANDLING_NORMAL) &&
        !breakMessageHandling.equals(BREAK_MESSAGE_HANDLING_HOLD) &&
        !breakMessageHandling.equals(BREAK_MESSAGE_HANDLING_NOTIFY))
    {
      throw new ExtendedIllegalArgumentException("breakMessageHandling", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    setValue(BREAK_MESSAGE_HANDLING, breakMessageHandling);
  }



  /**
   * Sets the value indicating whether attribute value changes are
   * committed immediately. The default is true.
   * If any cached changes are not committed before this method is called with
   * a value of false, those changes are lost.
   * @param cacheChanges true to cache attribute value changes,
   * false to commit all attribute value changes immediately.
   * @see #commitChanges
   * @see #getCacheChanges
   * @see #getValue
   * @see #setValue
  **/
  public void setCacheChanges(boolean cacheChanges)
  {
    if (!cacheChanges)
    {
      cachedChanges_ = null;
    }
    cacheChanges_ = cacheChanges;
  }



/**
Sets the coded character set identifier (CCSID).

@param codedCharacterSetID  The coded character set identifier (CCSID).  The
                            following special values can be used:
                            <ul>
                            <li>{@link #CCSID_SYSTEM_VALUE CCSID_SYSTEM_VALUE }  - The CCSID specified
                                in the system value QCCSID is used.
                            <li>{@link #CCSID_INITIAL_USER CCSID_INITIAL_USER }  - The CCSID specified
                                in the user profile under which this thread was initially running is used.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #CCSID
**/
  public void setCodedCharacterSetID(int codedCharacterSetID)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
//    if (codedCharacterSetID < -2)
//    {
//      throw new ExtendedIllegalArgumentException("codedCharacterSetID", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
//    }
    setAsIntToChange(CCSID, codedCharacterSetID);
  }



/**
Sets the country ID.

@param countryID    The country ID.  The following special values can be used:
                    <ul>
                    <li>{@link #COUNTRY_ID_SYSTEM_VALUE COUNTRY_ID_SYSTEM_VALUE }  - The
                        system value QCNTRYID is used.
                    <li>{@link #COUNTRY_ID_INITIAL_USER COUNTRY_ID_INITIAL_USER }  - The
                        country ID specified in the user profile under which this thread
                        was initially running is used.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #COUNTRY_ID
**/
  public void setCountryID(String countryID)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (countryID == null) throw new NullPointerException("countryID");

    setValue(COUNTRY_ID, countryID);
  }



/**
Sets the format in which dates are presented.

@param dateFormat The format in which dates are presented.  Possible values are:
<ul>
<li>{@link #DATE_FORMAT_SYSTEM_VALUE DATE_FORMAT_SYSTEM_VALUE }  - The system value QDATFMT is used.
<li>{@link #DATE_FORMAT_YMD DATE_FORMAT_YMD }  - Year, month, and day format.
<li>{@link #DATE_FORMAT_MDY DATE_FORMAT_MDY }  - Month, day, and year format.
<li>{@link #DATE_FORMAT_DMY DATE_FORMAT_DMY }  - Day, month, and year format.
<li>{@link #DATE_FORMAT_JULIAN DATE_FORMAT_JULIAN }  - Julian format (year and day).
</ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_FORMAT
**/
  public void setDateFormat(String dateFormat)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (dateFormat == null) throw new NullPointerException("dateFormat");

    if (!dateFormat.equals(DATE_FORMAT_SYSTEM_VALUE) &&
        !dateFormat.equals(DATE_FORMAT_YMD) &&
        !dateFormat.equals(DATE_FORMAT_MDY) &&
        !dateFormat.equals(DATE_FORMAT_DMY) &&
        !dateFormat.equals(DATE_FORMAT_JULIAN))
    {
      throw new ExtendedIllegalArgumentException("dateFormat", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(DATE_FORMAT, dateFormat);
  }



/**
Sets the value used to separate days, months, and years when presenting
a date.

@param dateSeparator    The value used to separate days, months, and years
                        when presenting a date.  The following special value
                        can be used:
                        <ul>
                        <li>{@link #DATE_SEPARATOR_SYSTEM_VALUE DATE_SEPARATOR_SYSTEM_VALUE }  - The
                            system value QDATSEP is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DATE_SEPARATOR
**/
  public void setDateSeparator(String dateSeparator)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (dateSeparator == null) throw new NullPointerException("dateSeparator");

    int len = setterKeys_.get(DATE_SEPARATOR);
    if (dateSeparator.length() > len)
    {
      dateSeparator = dateSeparator.substring(0, len);
    }
    
    setValue(DATE_SEPARATOR, dateSeparator);
  }



/**
Sets whether connections using distributed data management (DDM)
protocols remain active when they are not being used.

@param ddmConversationHandling  Whether connections using distributed data
                                management (DDM) protocols remain active
                                when they are not being used.  Possible values are:
                                <ul>
                                <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP KEEP_DDM_CONNECTIONS_ACTIVE_KEEP }  - The system keeps DDM connections active when there
                                    are no users.
                                <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP KEEP_DDM_CONNECTIONS_ACTIVE_DROP }  - The system ends a DDM connection when there are no
                                    users.
                                </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #KEEP_DDM_CONNECTIONS_ACTIVE
**/
  public void setDDMConversationHandling(String ddmConversationHandling)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (ddmConversationHandling == null) throw new NullPointerException("ddmConversationHandling");

    if (!ddmConversationHandling.equals(KEEP_DDM_CONNECTIONS_ACTIVE_KEEP) &&
        !ddmConversationHandling.equals(KEEP_DDM_CONNECTIONS_ACTIVE_DROP))
    {
      throw new ExtendedIllegalArgumentException("ddmConversationHandling", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(KEEP_DDM_CONNECTIONS_ACTIVE, ddmConversationHandling);
  }



/**
Sets the decimal format used for this job.

@param decimalFormat    The decimal format used for this job.  Possible values are:
                        <ul>
                        <li>{@link #DECIMAL_FORMAT_PERIOD DECIMAL_FORMAT_PERIOD }  - Uses a period for a decimal point, a comma
                            for a 3-digit grouping character, and zero-suppresses to the left of
                            the decimal point.
                        <li>{@link #DECIMAL_FORMAT_COMMA_I DECIMAL_FORMAT_COMMA_I }  - Uses a comma for a decimal point and a period for
                            a 3-digit grouping character.  The zero-suppression character is in the
                            second character (rather than the first) to the left of the decimal
                            notation.  Balances with zero  values to the left of the comma are
                            written with one leading zero.
                        <li>{@link #DECIMAL_FORMAT_COMMA_J DECIMAL_FORMAT_COMMA_J }  - Uses a comma for a decimal point, a period for a
                            3-digit grouping character, and zero-suppresses to the left of the decimal
                            point.
                        <li>{@link #DECIMAL_FORMAT_SYSTEM_VALUE DECIMAL_FORMAT_SYSTEM_VALUE }  - The
                            system value QDECFMT is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DECIMAL_FORMAT
**/
  public void setDecimalFormat(String decimalFormat)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (decimalFormat == null) throw new NullPointerException("decimalFormat");

    if (!decimalFormat.equals(DECIMAL_FORMAT_PERIOD) &&
        !decimalFormat.equals(DECIMAL_FORMAT_COMMA_I) &&
        !decimalFormat.equals(DECIMAL_FORMAT_COMMA_J) &&
        !decimalFormat.equals(DECIMAL_FORMAT_SYSTEM_VALUE))
    {
      throw new ExtendedIllegalArgumentException("decimalFormat", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(DECIMAL_FORMAT, decimalFormat);
  }



/**
Sets the default maximum time (in seconds) that a thread in the job
waits for a system instruction.

@param defaultWait  The default maximum time (in seconds) that a thread in the job
                    waits for a system instruction.  The value -1 means there is no maximum.
                    The value 0 is not valid.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DEFAULT_WAIT_TIME
**/
  public void setDefaultWait(int defaultWait)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setAsIntToChange(DEFAULT_WAIT_TIME, defaultWait);
  }



/**
Sets the action taken for interactive jobs when an I/O error occurs
for the job's requesting program device.

@param deviceRecoveryAction The action taken for interactive jobs when an I/O error occurs
                            for the job's requesting program device.  Possible values are:
                            <li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE DEVICE_RECOVERY_ACTION_MESSAGE }  - Signals the I/O error message to the
                                application and lets the application program perform error recovery.
                            <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE }  - Disconnects the
                                job when an I/O error occurs.  When the job reconnects, the system sends an
                                error message to the application program, indicating the job has reconnected
                                and that the workstation device has recovered.
                            <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST }  - Disconnects
                                the job when an I/O error occurs.  When the job reconnects, the system sends
                                the End Request (ENDRQS) command to return control to the previous request
                                level.
                            <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB DEVICE_RECOVERY_ACTION_END_JOB }  - Ends the job when an I/O error occurs.  A
                                message is sent to the job's log and to the history log (QHST) indicating
                                the job ended because of a device error.
                            <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST }  - Ends the job when an I/O
                                error occurs.  There is no job log produced for the job.  The system sends
                                a message to the QHST log indicating the job ended because of a device error.
                            <li>{@link #DEVICE_RECOVERY_ACTION_SYSTEM_VALUE DEVICE_RECOVERY_ACTION_SYSTEM_VALUE }  - The
                                system value QDEVRCYACN is used.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #DEVICE_RECOVERY_ACTION
**/
  public void setDeviceRecoveryAction(String deviceRecoveryAction)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (deviceRecoveryAction == null) throw new NullPointerException("deviceRecoveryAction");

    if (!deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_MESSAGE) &&
        !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE) &&
        !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST) &&
        !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_END_JOB) &&
        !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST) &&
        !deviceRecoveryAction.equals(DEVICE_RECOVERY_ACTION_SYSTEM_VALUE))
    {
      throw new ExtendedIllegalArgumentException("deviceRecoveryAction", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(DEVICE_RECOVERY_ACTION, deviceRecoveryAction);
  }



/**
Sets how the job answers inquiry messages.

@param inquiryMessageReply  How the job answers inquiry messages.  Possible values are:
                            <ul>
                            <li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED INQUIRY_MESSAGE_REPLY_REQUIRED }  - The job requires an answer for any inquiry
                                messages that occur while this job is running.
                            <li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT INQUIRY_MESSAGE_REPLY_DEFAULT }  - The system uses the default message reply to
                                answer any inquiry messages issued while this job is running.  The default
                                reply is either defined in the message description or is the default system
                                reply.
                            <li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST } The system reply list is
                                checked to see if there is an entry for an inquiry message issued while this
                                job is running.  If a match occurs, the system uses the reply value for that
                                entry.  If no entry exists for that message, the system uses an inquiry message.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #INQUIRY_MESSAGE_REPLY
**/
  public void setInquiryMessageReply(String inquiryMessageReply)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (inquiryMessageReply == null) throw new NullPointerException("inquiryMessageReply");

    if (!inquiryMessageReply.equals(INQUIRY_MESSAGE_REPLY_REQUIRED) &&
        !inquiryMessageReply.equals(INQUIRY_MESSAGE_REPLY_DEFAULT) &&
        !inquiryMessageReply.equals(INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST))
    {
      throw new ExtendedIllegalArgumentException("inquiryMessageReply", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(INQUIRY_MESSAGE_REPLY, inquiryMessageReply);
  }




/**
Sets the internal job identifier.  This does not change
the job on the AS/400.  Instead, it changes the job
this Job object references.  The job name
must be set to "*INT" for this to be recognized.
This cannot be changed if the object has established
a connection to the AS/400.

@param internalJobID    The internal job identifier.

@exception PropertyVetoException    If the property change is vetoed.
@deprecated The internal job identifier should be treated as a byte array of 16 bytes.
**/
  public void setInternalJobID(String internalJobID)
  throws PropertyVetoException
  {
    if (internalJobID == null)
    {
      throw new NullPointerException("internalJobID");
    }
    
    if (internalJobID.length() != 16)
    {
      throw new ExtendedIllegalArgumentException("internalJobID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }

    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("internalJobID", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String old = internalJobID_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("internalJobID", old, internalJobID_);

    internalJobID_ = internalJobID;
    realInternalJobID_ = new byte[16];
    for (int i=0; i<16; ++i)
    {
      realInternalJobID_[i] = (byte)internalJobID.charAt(i);
    }

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("internalJobID", old, internalJobID_);

  }


  /**
   * Sets the internal job identifier.  This does not change
   * the job on the server.  Instead, it changes the job
   * this Job object references.  The job name
   * must be set to "*INT" for this to be recognized.
   * This cannot be changed if the object has established
   * a connection to the server.
   *
   * @param internalJobID  The 16-byte internal job identifier.
   *
  **/
  public void setInternalJobIdentifier(byte[] internalJobID)
  {
    if (internalJobID == null) throw new NullPointerException("internalJobID");
    if (internalJobID.length != 16) throw new ExtendedIllegalArgumentException("internalJobID", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);

    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("internalJobID", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    realInternalJobID_ = internalJobID;
    char[] oldID = new char[16];
    for (int i=0; i<16; ++i)
    {
      oldID[i] = (char)(internalJobID[i] & 0x00FF);
    }
    internalJobID_ = new String(oldID);
  }

/**
Sets the identifier assigned to the job by the system to collect resource
use information for the job when job accounting is active.

@param jobAccountingCode    The identifier assigned to the job by the
                            system to collect resource use information
                            for the job when job accounting is active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #ACCOUNTING_CODE
**/
  public void setJobAccountingCode(String jobAccountingCode)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (jobAccountingCode == null) throw new NullPointerException("jobAccountingCode");

    setValue(ACCOUNTING_CODE, jobAccountingCode);
  }



/**
Sets the date to be used for the job.

@param jobDate The date to be used for the job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_DATE
**/
  public void setJobDate(Date jobDate)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (jobDate == null) throw new NullPointerException("jobDate");

    setAsDateToChange(JOB_DATE, jobDate);
  }


/**
Sets the action to take when the message queue is full.

@param jobMessageQueueFullAction    The action to take when the message queue is full.  Possible values are:
                                    <ul>
                                    <li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP MESSAGE_QUEUE_ACTION_NO_WRAP }  - Do not wrap. This action causes the job to end.
                                    <li>{@link #MESSAGE_QUEUE_ACTION_WRAP MESSAGE_QUEUE_ACTION_WRAP }  - Wrap to the beginning and start filling again.
                                    <li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP MESSAGE_QUEUE_ACTION_PRINT_WRAP }  - Wrap the message queue and print the
                                        messages that are being overlaid because of the wrapping.
                                    <LI>{@link #MESSAGE_QUEUE_ACTION_SYSTEM_VALUE MESSAGE_QUEUE_ACTION_SYSTEM_VALUE} - The QJOBMSGQFL system value is used.
                                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #MESSAGE_QUEUE_ACTION
**/
  public void setJobMessageQueueFullAction(String jobMessageQueueFullAction)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (jobMessageQueueFullAction == null) throw new NullPointerException("jobMessageQueueFullAction");

    if (!jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_NO_WRAP) &&
        !jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_WRAP) &&
        !jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_PRINT_WRAP) &&
        !jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_SYSTEM_VALUE))
    {
      throw new ExtendedIllegalArgumentException("jobMessageQueueFullAction", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(MESSAGE_QUEUE_ACTION, jobMessageQueueFullAction);
  }



/**
Sets the current setting of the job switches used by this job.

@param jobSwitches The current setting of the job switches used by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_SWITCHES
**/
  public void setJobSwitches(String jobSwitches)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (jobSwitches == null) throw new NullPointerException("jobSwitches");

    int len = setterKeys_.get(JOB_SWITCHES);
    if (jobSwitches.length() > len)
    {
      jobSwitches = jobSwitches.substring(0, len);
    }

    setValue(JOB_SWITCHES, jobSwitches);
  }



/**
Sets the language identifier associated with this job.

@param languageID       The language identifier associated with this job.
                        The following special values can be used:
                        <ul>
                        <li>{@link #LANGUAGE_ID_SYSTEM_VALUE LANGUAGE_ID_SYSTEM_VALUE }  - The
                            system value QLANGID is used.
                        <li>{@link #LANGUAGE_ID_INITIAL_USER LANGUAGE_ID_INITIAL_USER }  - The
                            language identifier specified in the user profile in which this thread
                            was initially running is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LANGUAGE_ID
**/
  public void setLanguageID(String languageID)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (languageID == null) throw new NullPointerException("languageID");

    setValue(LANGUAGE_ID, languageID);
  }



/**
Sets whether messages are logged for CL programs.

@param loggingCLPrograms    The value indicating whether or not messages are logged for
                            CL programs. Possible values are: {@link #LOG_CL_PROGRAMS_YES LOG_CL_PROGRAMS_YES }  and
                            {@link #LOG_CL_PROGRAMS_NO LOG_CL_PROGRAMS_NO } .

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOG_CL_PROGRAMS
**/
  public void setLoggingCLPrograms(String loggingCLPrograms)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (loggingCLPrograms == null)
    {
      throw new NullPointerException("loggingCLPrograms");
    }
    if (!loggingCLPrograms.equals(LOG_CL_PROGRAMS_YES) &&
        !loggingCLPrograms.equals(LOG_CL_PROGRAMS_NO))
    {
      throw new ExtendedIllegalArgumentException("loggingCLPrograms", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(LOG_CL_PROGRAMS, loggingCLPrograms);
  }



// @D1C
/**
Sets the type of information that is logged.

@param loggingLevel The type of information that is logged.  Possible values are:
                    <ul>
                    <li>0 - No messages are logged.
                    <li>1 - All messages sent
                        to the job's external message queue with a severity greater than or equal to
                        the message logging severity are logged.
                    <li>2 -
                        Requests or commands from CL programs for which the system issues messages with
                        a severity code greater than or equal to the logging severity and all messages
                        associated with those requests or commands that have a severity code greater
                        than or equal to the logging severity are logged.
                    <li>3 - All requests or commands from CL programs and all messages
                        associated with those requests or commands that have a severity code greater
                        than or equal to the logging severity are logged.
                    <li>4 - All requests or commands from CL programs and all messages
                        with a severity code greater than or equal to the logging severity are logged.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOGGING_LEVEL
**/
  public void setLoggingLevel(int loggingLevel)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (loggingLevel < 0 || loggingLevel > 4)
    {
      throw new ExtendedIllegalArgumentException("loggingLevel", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    setValue(LOGGING_LEVEL, Integer.toString(loggingLevel)); // @D1C
  }



/**
Sets the minimum severity level that causes error messages to be logged
in the job log.

@param loggingSeverity  The minimum severity level that causes error messages to be logged
                        in the job log.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOGGING_SEVERITY
**/
  public void setLoggingSeverity(int loggingSeverity)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setAsIntToChange(LOGGING_SEVERITY, loggingSeverity);
  }



/**
Sets the level of message text that is written in the job log
or displayed to the user.

@param loggingText  The level of message text that is written in the job log
                    or displayed to the user.  Possible values are:
                    <ul>
                    <li>{@link #LOGGING_TEXT_MESSAGE LOGGING_TEXT_MESSAGE }  - Only the message is written to the job log.
                    <li>{@link #LOGGING_TEXT_SECLVL LOGGING_TEXT_SECLVL }  - Both the message and the message help for the
                        error message are written to the job log.
                    <li>{@link #LOGGING_TEXT_NO_LIST LOGGING_TEXT_NO_LIST }  - If the job ends normally, there is no job log.
                        If the job ends abnormally, there is a job log.  The messages appearing in the
                        job log contain both the message and the message help.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #LOGGING_TEXT
**/
  public void setLoggingText(String loggingText)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (loggingText == null) throw new NullPointerException("loggingText");

    if (!loggingText.equals(LOGGING_TEXT_MESSAGE) &&
        !loggingText.equals(LOGGING_TEXT_SECLVL) &&
        !loggingText.equals(LOGGING_TEXT_NO_LIST))
    {
      throw new ExtendedIllegalArgumentException("loggingText", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(LOGGING_TEXT, loggingText);
  }



/**
Sets the job name.  This does not change the name of the actual OS/400 job.
Instead, it changes the job this Job object references.   This cannot be changed
if the object has already established a connection to the server.

@param name    The job name.  Specify JOB_NAME_CURRENT to indicate the job this
               program running in, or JOB_NAME_INTERNAL to indicate that the job
               is specified using the internal job identifier.

@exception PropertyVetoException    If the property change is vetoed.
**/
  public void setName(String name)
  throws PropertyVetoException
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }

    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String old = name_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("name", old, name);

    name_ = name;
    setValueInternal(JOB_NAME, name);

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("name", old, name);
  }



/**
Sets the job number.  This does not change the name of the actual OS/400 job.
Instead, it changes the job this Job object references.  This cannot be changed
if the object has already established a connection to the server.

@param number    The job number.  This must be JOB_NUMBER_BLANK if the job name is JOB_NAME_CURRENT.

@exception PropertyVetoException    If the property change is vetoed.
**/
  public void setNumber(String number)
  throws PropertyVetoException
  {
    if (number == null)
    {
      throw new NullPointerException("number");
    }

    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String old = number_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("number", old, number);

    number_ = number;
    setValueInternal(JOB_NUMBER, number);

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("number", old, number);
  }



/**
Sets the name of the default output queue that is used for
spooled output produced by this job.

@param outputQueue  The fully qualified integrated integrated file system path name of
                    the default output queue that is used for
                    spooled output produced by this job.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #OUTPUT_QUEUE
@see QSYSObjectPathName
**/
  public void setOutputQueue(String outputQueue)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (outputQueue == null) throw new NullPointerException("outputQueue");

    if (!outputQueue.startsWith("*"))
    {
      QSYSObjectPathName path = new QSYSObjectPathName(outputQueue);
      StringBuffer buf = new StringBuffer();
      String name = path.getObjectName();
      buf.append(name);
      for (int i=name.length(); i<10; ++i)
      {
        buf.append(' ');
      }
      String lib = path.getLibraryName();
      buf.append(lib);
      setValue(OUTPUT_QUEUE, buf.toString());
    }
    else
    {
      setValue(OUTPUT_QUEUE, outputQueue);
    }
  }



/**
Sets the output priority for spooled output files that this job
produces.

@param outputQueuePriority  The output priority for spooled output files that this job
                            produces.   The highest priority is 0 and the lowest is 9.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #OUTPUT_QUEUE_PRIORITY
**/
  public void setOutputQueuePriority(int outputQueuePriority)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setValue(OUTPUT_QUEUE_PRIORITY, Integer.toString(outputQueuePriority));
  }



/**
Sets the printer device name used for printing output
from this job.

@param printerDeviceName    The printer device name used for printing output
                            from this job.  The following special values can be used:
                            <ul>
                            <li>{@link #PRINTER_DEVICE_NAME_SYSTEM_VALUE PRINTER_DEVICE_NAME_SYSTEM_VALUE }  - The
                                system value QPRTDEV is used.
                            <li>{@link #PRINTER_DEVICE_NAME_WORK_STATION PRINTER_DEVICE_NAME_WORK_STATION }  - The
                                default printer device used with this job is the printer device
                                assigned to the work station that is associated with the job.
                            <li>{@link #PRINTER_DEVICE_NAME_INITIAL_USER PRINTER_DEVICE_NAME_INITIAL_USER }  - The
                                printer device name specified in the user profile in which this thread
                                was initially running is used.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRINTER_DEVICE_NAME
**/
  public void setPrinterDeviceName (String printerDeviceName)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (printerDeviceName == null) throw new NullPointerException("printerDeviceName");

    int len = setterKeys_.get(PRINTER_DEVICE_NAME);
    if (printerDeviceName.length() > len)
    {
      printerDeviceName = printerDeviceName.substring(0, len); 
    }

    setValue(PRINTER_DEVICE_NAME, printerDeviceName);
  }



/**
Sets whether border and header information is provided when
the Print key is pressed.

@param printKeyFormat   Whether border and header information is provided when
                        the Print key is pressed.  Possible values are:
                        <ul>
                        <li>{@link #PRINT_KEY_FORMAT_NONE PRINT_KEY_FORMAT_NONE }  - The border and header information is not
                            included with output from the Print key.
                        <li>{@link #PRINT_KEY_FORMAT_BORDER PRINT_KEY_FORMAT_BORDER }  - The border information
                            is included with output from the Print key.
                        <li>{@link #PRINT_KEY_FORMAT_HEADER PRINT_KEY_FORMAT_HEADER }  - The header information
                            is included with output from the Print key.
                        <li>{@link #PRINT_KEY_FORMAT_ALL PRINT_KEY_FORMAT_ALL }  - The border and header information
                            is included with output from the Print key.
                        <li>{@link #PRINT_KEY_FORMAT_SYSTEM_VALUE PRINT_KEY_FORMAT_SYSTEM_VALUE }  - The
                            system value QPRTKEYFMT is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRINT_KEY_FORMAT
**/
  public void setPrintKeyFormat(String printKeyFormat)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (printKeyFormat == null) throw new NullPointerException("printKeyFormat");

    if (!printKeyFormat.equals(PRINT_KEY_FORMAT_NONE) &&
        !printKeyFormat.equals(PRINT_KEY_FORMAT_BORDER) &&
        !printKeyFormat.equals(PRINT_KEY_FORMAT_HEADER) &&
        !printKeyFormat.equals(PRINT_KEY_FORMAT_ALL) &&
        !printKeyFormat.equals(PRINT_KEY_FORMAT_SYSTEM_VALUE))
    {
      throw new ExtendedIllegalArgumentException("printKeyFormat", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(PRINT_KEY_FORMAT, printKeyFormat);
  }



/**
Sets the line of text, if any, that is printed at the
bottom of each page of printed output for the job.

@param printText    The line of text, if any, that is printed at the
                    bottom of each page of printed output for the job.
                    The following special value can be used:
                    <ul>
                    <li>{@link #PRINT_TEXT_SYSTEM_VALUE PRINT_TEXT_SYSTEM_VALUE }  - The
                        system value QPRTTXT is used.
                    </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #PRINT_TEXT
**/
  public void setPrintText (String printText)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (printText == null) throw new NullPointerException("printText");

    setValue(PRINT_TEXT, printText);
  }



/**
Sets the value indicating whether the job is eligible to be moved out of main storage
and put into auxiliary storage at the end of a time slice or when it is
beginning a long wait.

@param purge    true to indicate that the job is eligible to be moved out of main storage
                and put into auxiliary storage at the end of a time slice or when it is
                beginning a long wait, false otherwise.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #ELIGIBLE_FOR_PURGE
**/
  public void setPurge(boolean purge)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setValueInternal(ELIGIBLE_FOR_PURGE, purge ? ELIGIBLE_FOR_PURGE_YES : ELIGIBLE_FOR_PURGE_NO);
  }



/**
Sets the job queue that the job is currently on.

@param jobQueue     The fully qualified integrated file system path name
                    of the job queue.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_QUEUE
@see QSYSObjectPathName
**/
  public void setQueue(String jobQueue)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (jobQueue == null) throw new NullPointerException("jobQueue");

    if (!jobQueue.startsWith("*"))
    {
      QSYSObjectPathName path = new QSYSObjectPathName(jobQueue);
      StringBuffer buf = new StringBuffer();
      String name = path.getObjectName();
      buf.append(name);
      for (int i=name.length(); i<10; ++i)
      {
        buf.append(' ');
      }
      String lib = path.getLibraryName();
      buf.append(lib);
      setValue(JOB_QUEUE, buf.toString());
    }
    else
    {
      setValue(JOB_QUEUE, jobQueue);
    }
  }



/**
Sets the scheduling priority of the job compared to other jobs
on the same job queue.

@param queuePriority    The scheduling priority of the job compared to other jobs
                        on the same job queue.  The highest priority is 0 and the
                        lowest is 9.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #JOB_QUEUE_PRIORITY
**/
  public void setQueuePriority(int queuePriority)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setValue(JOB_QUEUE_PRIORITY, Integer.toString(queuePriority));
  }



/**
Sets the priority at which the job is currently running,
relative to other jobs on the system.

@param runPriority  The priority at which the job is currently running,
                    relative to other jobs on the system.  The run priority
                    ranges from 1 (highest priority) to 99 (lowest priority).

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #RUN_PRIORITY
**/
  public void setRunPriority(int runPriority)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setAsIntToChange(RUN_PRIORITY, runPriority);
  }


/**
Sets the date and time the job is scheduled to become active.

@param scheduleDate The date and time the job is scheduled to become active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SCHEDULE_DATE
**/
  public void setScheduleDate(Date scheduleDate)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (scheduleDate == null) throw new NullPointerException("scheduleDate");

    // The schedule date is weird.
    // Use SCHEDULE_DATE and SCHEDULE_TIME to set it.
    // Use SCHEDULE_DATE_GETTER to retrieve it.

    if (scheduleDate == null)
      throw new NullPointerException("scheduleDate");

    Calendar dateTime = Calendar.getInstance();
    dateTime.clear();
    dateTime.setTime(scheduleDate);

    StringBuffer buf = new StringBuffer();
    int year = dateTime.get(Calendar.YEAR)-1900;
    if (year >= 100)
    {
      buf.append('1');
      year -= 100;
    }
    else
    {
      buf.append('0');
    }
    if (year < 10)
    {
      buf.append('0');
    }
    buf.append(year);
    int month = dateTime.get(Calendar.MONTH)+1;
    if (month < 10)
    {
      buf.append('0');
    }
    buf.append(month);
    int day = dateTime.get(Calendar.DATE);
    if (day < 10)
    {
      buf.append('0');
    }
    buf.append(day);
    String dateToSet = buf.toString();

    buf = new StringBuffer();
    int hour = dateTime.get(Calendar.HOUR_OF_DAY);
    if (hour < 10)
    {
      buf.append('0');
    }
    buf.append(hour);
    int minute = dateTime.get(Calendar.MINUTE);
    if (minute < 10)
    {
      buf.append('0');
    }
    buf.append(minute);
    int second = dateTime.get(Calendar.SECOND);
    if (second < 10)
    {
      buf.append('0');
    }
    buf.append(second);
    String timeToSet = buf.toString();

    setValue(SCHEDULE_DATE, dateToSet);
    setValue(SCHEDULE_TIME, timeToSet);
    setValueInternal(SCHEDULE_DATE_GETTER, null);
  }



/**
Sets the date the job is scheduled to become active.

@param scheduleDate     The date the job is scheduled to become active,
                        in the format <em>CYYMMDD</em>, where <em>C</em>
                        is the century, <em>YY</em> is the year, <em>MM</em>
                        is the month, and <em>DD</em> is the day.  A 0 for
                        the century flag indicates years 19<em>xx</em> and a
                        1 indicates years 20<em>xx</em>.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SCHEDULE_DATE
**/
  public void setScheduleDate(String scheduleDate)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (scheduleDate == null)
      throw new NullPointerException("scheduleDate");
    if (scheduleDate.length() != 7)
      throw new ExtendedIllegalArgumentException("scheduleDate", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

/*    Calendar calendar = Calendar.getInstance();

    int century = Integer.parseInt(scheduleDate.substring(0,1));
    int year    = Integer.parseInt(scheduleDate.substring(1,3));
    int month   = Integer.parseInt(scheduleDate.substring(3,5));
    int day     = Integer.parseInt(scheduleDate.substring(5,7));

    calendar.set(Calendar.YEAR, year + ((century == 0) ? 1900 : 2000));
    calendar.set(Calendar.MONTH, month - 1);
    calendar.set(Calendar.DAY_OF_MONTH, day);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);

    setScheduleDate(calendar.getTime());
*/
    setValue(SCHEDULE_DATE, scheduleDate);    
    setValueInternal(SCHEDULE_DATE_GETTER, null);
  }



/**
Sets the date and time the job is scheduled to become active.

@param scheduleDate The date and time the job is scheduled to become active.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SCHEDULE_DATE
**/
  public void setScheduleTime(Date scheduleTime)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (scheduleTime == null) throw new NullPointerException("scheduleTime");

    setScheduleDate(scheduleTime);
  }



/**
Sets the time the job is scheduled to become active.

@param scheduleTime     The time the job is scheduled to become active,
                        in the format <em>HHMMSS</em>, where <em>HH</em> are
                        the hours, <em>MM</em> are the minutes, and <em>SS</em>
                        are the seconds.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SCHEDULE_DATE
**/
  public void setScheduleTime(String scheduleTime)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (scheduleTime == null)
      throw new NullPointerException("scheduleTime");
    if (scheduleTime.length() != 6)
      throw new ExtendedIllegalArgumentException("scheduleTime", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

/*    Calendar calendar = Calendar.getInstance();

    int hours   = Integer.parseInt(scheduleTime.substring(0,2));
    int minutes = Integer.parseInt(scheduleTime.substring(2,4));
    int seconds = Integer.parseInt(scheduleTime.substring(4,6));

    calendar.set(Calendar.YEAR, 0);
    calendar.set(Calendar.MONTH, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 0);
    calendar.set(Calendar.HOUR_OF_DAY, hours);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, seconds);

    setScheduleDate(calendar.getTime());
*/
    setValue(SCHEDULE_TIME, scheduleTime);    
    setValueInternal(SCHEDULE_DATE_GETTER, null);
  }



/**
Sets the sort sequence table.

@param sortSequenceTable    The fully qualified integrated file system path name
                            of the sort sequence table.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #SORT_SEQUENCE_TABLE
@see QSYSObjectPathName
**/
  public void setSortSequenceTable(String sortSequenceTable)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (sortSequenceTable == null) throw new NullPointerException("sortSequenceTable");

    if (!sortSequenceTable.startsWith("*"))
    {
      QSYSObjectPathName path = new QSYSObjectPathName(sortSequenceTable);
      StringBuffer buf = new StringBuffer();
      String name = path.getObjectName();
      buf.append(name);
      for (int i=name.length(); i<10; ++i)
      {
        buf.append(' ');
      }
      String lib = path.getLibraryName();
      buf.append(lib);
      setValue(SORT_SEQUENCE_TABLE, buf.toString());
    }
    else
    {
      setValue(SORT_SEQUENCE_TABLE, sortSequenceTable);
    }
  }



/**
Sets the value which indicates whether status messages are displayed for
this job.

@param statusMessageHandling    The value which indicates whether status messages are displayed for
                                this job. Possible values are:
                                <ul>
                                <li>{@link #STATUS_MESSAGE_HANDLING_NONE STATUS_MESSAGE_HANDLING_NONE }  -
                                    This job does not display status messages.
                                <li>{@link #STATUS_MESSAGE_HANDLING_NORMAL STATUS_MESSAGE_HANDLING_NORMAL }  -
                                    This job displays status messages.
                                <li>{@link #STATUS_MESSAGE_HANDLING_SYSTEM_VALUE STATUS_MESSAGE_HANDLING_SYSTEM_VALUE }  - The
                                    system value QSTSMSG is used.
                                <li>{@link #STATUS_MESSAGE_HANDLING_INITIAL_USER STATUS_MESSAGE_HANDLING_INITIAL_USER }  - The
                                    status message handling that is specified in the user profile under which this thread
                                    was initially running is used.
                                </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #STATUS_MESSAGE_HANDLING
**/
  public void setStatusMessageHandling(String statusMessageHandling)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (statusMessageHandling == null) throw new NullPointerException("statusMessageHandling");

    setValue(STATUS_MESSAGE_HANDLING, statusMessageHandling);
  }



/**
Sets the system.  This cannot be changed if the object
has established a connection to the AS/400.

@param system The system.

@exception PropertyVetoException    If the property change is vetoed.
**/
  public void setSystem(AS400 system)
  throws PropertyVetoException
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }

    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    AS400 old = system_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("system", old, system);

    system_ = system;

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("system", old, system);
  }



/**
Sets the value used to separate hours, minutes, and seconds when presenting
a time.

@param timeSeparator    The value used to separate hours, minutes, and seconds
                        when presenting a time.  The following special value
                        can be used:
                        <ul>
                        <li>{@link #TIME_SEPARATOR_SYSTEM_VALUE TIME_SEPARATOR_SYSTEM_VALUE }  - The
                            system value QTIMSEP is used.
                        </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #TIME_SEPARATOR
**/
  public void setTimeSeparator(String timeSeparator)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (timeSeparator == null) throw new NullPointerException("timeSeparator");

    int len = setterKeys_.get(TIME_SEPARATOR);
    if (timeSeparator.length() > len)
    {
      timeSeparator = timeSeparator.substring(0, len);
    }
    
    setValue(TIME_SEPARATOR, timeSeparator);
  }



/**
Sets the maximum amount of processor time (in milliseconds) given to
each thread in this job before other threads in this job and in other
jobs are given the opportunity to run.

@param timeSlice    The maximum amount of processor time (in milliseconds) given to
                    each thread in this job before other threads in this job and in other
                    jobs are given the opportunity to run.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #TIME_SLICE
**/
  public void setTimeSlice(int timeSlice)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    setAsIntToChange(TIME_SLICE, timeSlice);
  }



/**
Sets the value which indicates whether a thread in an interactive job moves to another main storage
pool at the end of its time slice.

@param timeSliceEndPool     The value which indicates whether a thread in an interactive job
                            moves to another main storage pool at the end of its time slice.
                            Possible values are:
                            <ul>
                            <li>{@link #TIME_SLICE_END_POOL_NONE TIME_SLICE_END_POOL_NONE }  -
                                A thread in the job does not move to another main storage pool when it reaches
                                the end of its time slice.
                            <li>{@link #TIME_SLICE_END_POOL_BASE TIME_SLICE_END_POOL_BASE }  -
                                A thread in the job moves to the base pool when it reaches
                                the end of its time slice.
                            </ul>

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.

@see #TIME_SLICE_END_POOL
**/
  public void setTimeSliceEndPool(String timeSliceEndPool)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (timeSliceEndPool == null) throw new NullPointerException("timeSliceEndPool");

    setValue(TIME_SLICE_END_POOL, timeSliceEndPool);
  }



/**
Sets the user name.  This does not change the name of the actual OS/400 job.
Instead, it changes the job this Job object references.  This cannot be changed
if the object has already established a connection to the server.

@param user    The user name.  This must be USER_NAME_BLANK if the job name is JOB_NAME_CURRENT.

@exception PropertyVetoException    If the property change is vetoed.
**/
  public void setUser(String user)
  throws PropertyVetoException
  {
    if (user == null)
    {
      throw new NullPointerException("user");
    }

    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String old = user_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("user", old, user);

    user_ = user;
    setValueInternal(USER_NAME, user);

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("user", old, user);
  }


  /**
   * Sets a value for a job attribute.
   * If caching is off, the value is immediately sent to the system.
   * If caching is on, call {@link #commitChanges commitChanges()} to send the uncommitted values to the system.
   * @param attribute The job attribute to change.
   * @param value The new value of the attribute.
   * @see #commitChanges
   * @see #getValue
  **/
  public void setValue(int attribute, Object value)
  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    if (attribute < 0 || isReadOnly(attribute))
    {
      throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    
    if (attribute == SCHEDULE_DATE || attribute == SCHEDULE_TIME)
    {
      setValueInternal(SCHEDULE_DATE_GETTER, null);
    }

    // Update values to set upon commit
    if (cachedChanges_ == null) cachedChanges_ = new JobHashtable();
    cachedChanges_.put(attribute, value);

    if (!cacheChanges_)
    {
      commitChanges();
    }
    values_.put(attribute, value); // Update getter values
  }


  /** 
   * Helper method. Used to set a value into our internal table.
   * We technically don't need a method for this, but in case in the future
   * we need to do anything besides just putting the value into the hashtable,
   * we can add that logic here.
  **/
  final void setValueInternal(int key, Object value)
  {
    values_.put(key, value);
  }


  /**
   * Returns the string representation of this Job in the format
   * "number/user/name", or "" if any of these attributes is null.
   * @return The string representation.
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
