///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JobList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
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
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;


/**
 * The JobList class represents a list of OS/400 jobs. By default, all jobs are selected. To filter the list,
 * use the {@link #addJobSelectionCriteria addJobSelectionCriteria()} method.
 *
 * @see com.ibm.as400.access.Job
**/
public class JobList implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

  static final long serialVersionUID = 5L;

  /**
   * Constant indicating that all the jobs are returned.
   * @deprecated Use the selection constant that corresponds to the
   * particular job selection criteria you are filtering.
   * For example, to select jobs for all job names, do:
   * <CODE>
   * JobList list = new JobList(system);
   * list.addJobSelectionCriteria(JobList.SELECTION_JOB_NAME, JobList.SELECTION_JOB_NAME_ALL);
   * </CODE>
   * For backwards compatibility, this has the same effect:
   * <CODE>
   * JobList list = new JobList(system);
   * list.setName(JobList.SELECTION_JOB_NAME_ALL);
   * </CODE>
  **/
  public static final String ALL = "*ALL";
  
  /**
   * Selection type used for job selection based on job name.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a String.
   * Possible values are:
   * <UL>
   * <LI>A specific job name.
   * <LI>A generic name.
   * <LI>{@link #SELECTION_JOB_NAME_ALL SELECTION_JOB_NAME_ALL}
   * <LI>{@link #SELECTION_JOB_NAME_CURRENT SELECTION_JOB_NAME_CURRENT}
   * <LI>{@link #SELECTION_JOB_NAME_ONLY SELECTION_JOB_NAME_ONLY}
   * </UL>
   * The default is {@link #SELECTION_JOB_NAME_ALL SELECTION_JOB_NAME_ALL}.
   * @see #setName
   * @see com.ibm.as400.access.Job#JOB_NAME
  **/
  public static final int SELECTION_JOB_NAME =                      1;

  /**
   * Selection value indicating all jobs will be selected regardless of the job name.
   * The user name and job type fields must be specified.
   * @see #SELECTION_JOB_NAME
  **/
  public static final String SELECTION_JOB_NAME_ALL = "*ALL";

  /**
   * Selection value indicating all jobs with the current job's name will be selected.
   * @see #SELECTION_JOB_NAME
  **/
  public static final String SELECTION_JOB_NAME_CURRENT = "*CURRENT";

  /**
   * Selection value indicating only the job in which this program is running will
   * be selected. The user name and job type fields must be blank.
   * @see #SELECTION_JOB_NAME
  **/
  public static final String SELECTION_JOB_NAME_ONLY = "*";

  /**
   * Selection type used for job selection based on user name.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a String.
   * Possible values are:
   * <UL>
   * <LI>A specific user profile name.
   * <LI>A generic name.
   * <LI>{@link #SELECTION_USER_NAME_ALL SELECTION_USER_NAME_ALL}
   * <LI>{@link #SELECTION_USER_NAME_CURRENT SELECTION_USER_NAME_CURRENT}
   * </UL>
   * The default is {@link #SELECTION_USER_NAME_ALL SELECTION_USER_NAME_ALL}.
   * @see #setUser
   * @see com.ibm.as400.access.Job#USER_NAME
  **/
  public static final int SELECTION_USER_NAME =                     2;
  
  /**
   * Selection value indicating all jobs that use the specified job name will
   * be selected, regardless of the user name. The job name and job number
   * fields must be specified.
   * @see #SELECTION_USER_NAME
  **/
  public static final String SELECTION_USER_NAME_ALL = "*ALL";

  /**
   * Selection value indicating all jobs that use the current job's user profile
   * will be selected.
   * @see #SELECTION_USER_NAME
  **/
  public static final String SELECTION_USER_NAME_CURRENT = "*CURRENT";

  /**
   * Selection type used for job selection based on job number.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a String.
   * Possible values are:
   * <UL>
   * <LI>A specific job number.
   * <LI>{@link #SELECTION_JOB_NUMBER_ALL SELECTION_JOB_NUMBER_ALL}
   * </UL>
   * The default is {@link #SELECTION_JOB_NUMBER_ALL SELECTION_JOB_NUMBER_ALL}.
   * @see #setNumber
   * @see com.ibm.as400.access.Job#JOB_NUMBER
  **/
  public static final int SELECTION_JOB_NUMBER =                    3;
  
  /**
   * Selection value indicating all jobs with the specified job name and user name
   * will be selected, regardless of the job number. The job name and user name
   * fields must be specified.
   * @see #SELECTION_JOB_NUMBER
  **/
  public static final String SELECTION_JOB_NUMBER_ALL = "*ALL";

  /**
   * Selection type used for job selection based on job type.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a String.
   * Possible values are:
   * <UL>
   * <LI>{@link #SELECTION_JOB_TYPE_ALL SELECTION_JOB_TYPE_ALL}
   * <LI>One of the following job types:
   *   <UL>
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_AUTOSTART Job.JOB_TYPE_AUTOSTART}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_BATCH Job.JOB_TYPE_BATCH}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_INTERACTIVE Job.JOB_TYPE_INTERACTIVE}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_SUBSYSTEM_MONITOR Job.JOB_TYPE_SUBSYSTEM_MONITOR}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_SPOOLED_READER Job.JOB_TYPE_SPOOLED_READER}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_SYSTEM Job.JOB_TYPE_SYSTEM}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_SPOOLED_WRITER Job.JOB_TYPE_SPOOLED_WRITER}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_SCPF_SYSTEM Job.JOB_TYPE_SCPF_SYSTEM}
   *   </UL>
   * </UL>
   * The default is {@link #SELECTION_JOB_TYPE_ALL SELECTION_JOB_TYPE_ALL}.
   * @see com.ibm.as400.access.Job#JOB_TYPE
  **/
  public static final int SELECTION_JOB_TYPE =                      4;
  
  /**
   * Selection value indicating all job types will be selected.
   * @see #SELECTION_JOB_TYPE
  **/
  public static final String SELECTION_JOB_TYPE_ALL = "*";

  /**
   * Selection type used for job selection based on primary job status.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a Boolean.
   * The default is true.
   * @see com.ibm.as400.access.Job#JOB_STATUS
  **/
  public static final int SELECTION_PRIMARY_JOB_STATUS_ACTIVE =     5;
  
  /**
   * Selection type used for job selection based on primary job status.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a Boolean.
   * The default is true.
   * @see com.ibm.as400.access.Job#JOB_STATUS
  **/
  public static final int SELECTION_PRIMARY_JOB_STATUS_JOBQ =  6;
  
  /**
   * Selection type used for job selection based on primary job status.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a Boolean.
   * The default is true.
    * @see com.ibm.as400.access.Job#JOB_STATUS
  **/
  public static final int SELECTION_PRIMARY_JOB_STATUS_OUTQ =  7;
  
  /**
   * Selection type used for job selection based on active job status.
   * Multiple selection values are allowed for this selection type.
   * The selection value corresponding to this selection type is a String.
   * See {@link com.ibm.as400.access.Job#ACTIVE_JOB_STATUS Job.ACTIVE_JOB_STATUS} for allowed values.
   * By default no selection values are specified for this selection type.
   * This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_ACTIVE is true.
   * @see #SELECTION_PRIMARY_JOB_STATUS_ACTIVE
   * @see com.ibm.as400.access.Job#ACTIVE_JOB_STATUS
  **/
  public static final int SELECTION_ACTIVE_JOB_STATUS =             8;
  
  /**
   * Selection type used for job selection based on a job's status on the job queue.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a Boolean.
   * The default is true.
   * This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
   * @see #SELECTION_PRIMARY_JOB_STATUS_JOBQ
   * @see com.ibm.as400.access.Job#JOB_QUEUE_STATUS
  **/
  public static final int SELECTION_JOB_QUEUE_STATUS_SCHEDULE =    9;
  
  /**
   * Selection type used for job selection based on a job's status on the job queue.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a Boolean.
   * The default is true.
   * This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
   * @see #SELECTION_PRIMARY_JOB_STATUS_JOBQ
   * @see com.ibm.as400.access.Job#JOB_QUEUE_STATUS
  **/
  public static final int SELECTION_JOB_QUEUE_STATUS_HELD =        10;
  
  /**
   * Selection type used for job selection based on a job's status on the job queue.
   * Only one selection value is allowed for this selection type.
   * The selection value corresponding to this selection type is a Boolean.
   * The default is true.
   * This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
   * @see #SELECTION_PRIMARY_JOB_STATUS_JOBQ
   * @see com.ibm.as400.access.Job#JOB_QUEUE_STATUS
  **/
  public static final int SELECTION_JOB_QUEUE_STATUS_READY =       11;

  /**
   * Selection type used for job selection based on job queue.
   * Multiple selection values are allowed for this selection type.
   * The selection value corresponding to this selection type is a String
   * representing the fully-qualified integrated file system name for an OS/400 job queue.
   * By default no selection values are specified for this selection type.
   * This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
   * @see #SELECTION_PRIMARY_JOB_STATUS_JOBQ
   * @see com.ibm.as400.access.QSYSObjectPathName
   * @see com.ibm.as400.access.Job#JOB_QUEUE
  **/
  public static final int SELECTION_JOB_QUEUE =                    12;
  
  /**
   * Selection type used for job selection based on the user name for a job's initial thread.
   * Multiple selection values are allowed for this selection type.
   * The selection value corresponding to this selection type is a String.
   * By default no selection values are specified for this selection type.
  **/
  public static final int SELECTION_INITIAL_USER =  13;
  
  /**
   * Selection type used for job selection based on the server type.
   * Multiple selection values are allowed for this selection type.
   * The selection value corresponding to this selection type is a String.
   * By default no selection values are specified for this selection type.
   * Possible values are:
   * <UL>
   * <LI>A server type. See {@link com.ibm.as400.access.Job#SERVER_TYPE Job.SERVER_TYPE}.
   * <LI>A generic value.
   * <LI>{@link #SELECTION_SERVER_TYPE_ALL SELECTION_SERVER_TYPE_ALL}
   * <LI>{@link #SELECTION_SERVER_TYPE_BLANK SELECTION_SERVER_TYPE_BLANK}
   * </UL>
   * @see com.ibm.as400.access.Job#SERVER_TYPE
  **/
  public static final int SELECTION_SERVER_TYPE =                  14;
  
  /** 
   * Selection value indicating all jobs with a server type will be selected.
   * @see #SELECTION_SERVER_TYPE
  **/
  public static final String SELECTION_SERVER_TYPE_ALL = "*ALL";

  /**
   * Selection value indicating all jobs without a server type will be selected.
   * @see #SELECTION_SERVER_TYPE
  **/
  public static final String SELECTION_SERVER_TYPE_BLANK = "*BLANK";

  //public static final int ACTIVE_SUBSYSTEM =             15;
  //public static final int MEMORY_POOL =                  16;
  
  /**
   * Selection type used for job selection based on the enhanced job type.
   * Multiple selection values are allowed for this selection type.
   * The selection value corresponding to this selection type is an Integer.
   * By default no selection values are specified for this selection type.
   * Possible values are:
   * <UL>
   * <LI>{@link #SELECTION_JOB_TYPE_ENHANCED_ALL_BATCH SELECTION_JOB_TYPE_ENHANCED_ALL_BATCH}
   * <LI>{@link #SELECTION_JOB_TYPE_ENHANCED_ALL_INTERACTIVE SELECTION_JOB_TYPE_ENHANCED_ALL_INTERACTIVE}
   * <LI>{@link #SELECTION_JOB_TYPE_ENHANCED_ALL_PRESTART SELECTION_JOB_TYPE_ENHANCED_ALL_PRESTART}
   * <LI>Any of the enhanced job types:
   *   <UL>
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_AUTOSTART Job.JOB_TYPE_ENHANCED_AUTOSTART}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH Job.JOB_TYPE_ENHANCED_BATCH}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_IMMEDIATE Job.JOB_TYPE_ENHANCED_BATCH_IMMEDIATE}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_MRT Job.JOB_TYPE_ENHANCED_BATCH_MRT}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER Job.JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST Job.JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE Job.JOB_TYPE_ENHANCED_INTERACTIVE}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_GROUP Job.JOB_TYPE_ENHANCED_INTERACTIVE_GROUP}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ Job.JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP Job.JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART Job.JOB_TYPE_ENHANCED_PRESTART}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_BATCH Job.JOB_TYPE_ENHANCED_PRESTART_BATCH}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_COMM Job.JOB_TYPE_ENHANCED_PRESTART_COMM}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_READER Job.JOB_TYPE_ENHANCED_READER}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_SUBSYSTEM Job.JOB_TYPE_ENHANCED_SUBSYSTEM}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_SYSTEM Job.JOB_TYPE_ENHANCED_SYSTEM}
   *   <LI>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_WRITER Job.JOB_TYPE_ENHANCED_WRITER}
   *   </UL>
   * </UL>   
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED
  **/
  public static final int SELECTION_JOB_TYPE_ENHANCED =            17;

  /**
   * Selection value indicating all the batch job types will be selected.
   * @see #SELECTION_JOB_TYPE_ENHANCED
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_IMMEDIATE
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_MRT
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER
  **/
  public static final Integer SELECTION_JOB_TYPE_ENHANCED_ALL_BATCH = new Integer(200);

  /**
   * Selection value indicating all the interactive job types will be selected.
   * @see #SELECTION_JOB_TYPE_ENHANCED
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_GROUP
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP
  **/
  public static final Integer SELECTION_JOB_TYPE_ENHANCED_ALL_INTERACTIVE = new Integer(900);

  /**
   * Selection value indicating all the prestart job types will be selected.
   * @see #SELECTION_JOB_TYPE_ENHANCED
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_BATCH
   * @see com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_COMM
  **/
  public static final Integer SELECTION_JOB_TYPE_ENHANCED_ALL_PRESTART = new Integer(1600);


  private AS400 system_;
  private transient PropertyChangeSupport propertyChangeSupport_;
  private transient VetoableChangeSupport vetoableChangeSupport_;

  // Selection variables  
  private String selectionJobName_ = SELECTION_JOB_NAME_ALL;
  private String selectionUserName_ = SELECTION_USER_NAME_ALL;
  private String selectionJobNumber_ = SELECTION_JOB_NUMBER_ALL;
  private String selectionJobType_ = SELECTION_JOB_TYPE_ALL;
  private boolean selectActiveJobs_ = true;
  private boolean selectJobQueueJobs_ = true;
  private boolean selectOutQueueJobs_ = true;
  private String[] activeStatuses_ = new String[1];
  private int currentActiveStatus_ = 0;
  private boolean selectHeldJobs_ = true;
  private boolean selectScheduledJobs_ = true;
  private boolean selectReadyJobs_ = true;
  private String[] jobQueues_ = new String[1];
  private int currentJobQueue_ = 0;
  private String[] initialUsers_ = new String[1];
  private int currentInitialUser_ = 0;
  private String[] serverTypes_ = new String[1];
  private int currentServerType_ = 0;
  private int[] enhancedJobTypes_ = new int[1];
  private int currentEnhancedJobType_ = 0;

  private int length_;
  private byte[] handle_; // handle that references the user space used by the open list APIs
  private byte[] handleToClose_; // used to close a previously opened list
  private boolean isConnected_;
  
  private static final AS400Bin4 bin4_ = new AS400Bin4();
  private static final AS400Bin2 bin2_ = new AS400Bin2();
  private static final Integer zero_ = new Integer(0);
  private static final ProgramParameter errorCode_ = new ProgramParameter(bin4_.toBytes(0));

  // Info saved between calls to load() and getJobs()
  private int numKeysReturned_;
  private int[] keyFieldsReturned_;
  private char[] keyTypesReturned_;
  private int[] keyLengthsReturned_;
  private int[] keyOffsetsReturned_;

  // Keys to pre-load
  private int currentKey_ = 0;
  private int[] keys_ = new int[1];
  
  // Sort keys
  private int currentSortKey_ = 0;
  private int[] sortKeys_ = new int[1];
  private boolean[] sortOrders_ = new boolean[1];
  
  private Vector trackers_; // Used to determine if there are open Enumerations still using us.

  /**
   * Constructs a JobList object. The system must be set before retrieving the list of jobs.
   * @see #setSystem
  **/
  public JobList()
  {
  }



/**
   * Constructs a JobList object.
   * @param system The system.
**/
  public JobList(AS400 system)
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }

    system_ = system;
  }


  /**
   * Adds a job attribute that will be retrieved for each job in this job list.
   * This method allows the Job objects that are retrieved from this JobList
   * to have some of their attributes already filled in, so that a call to
   * {@link com.ibm.as400.access.Job#getValue Job.getValue()} does not result in another API call back to the server
   * for each job in the list.
   * <P>
   * The list of job attributes is maintained internally even when this JobList is closed and re-used.
   * To start over with a new set of job attributes to retrieve, call {@link #clearJobAttributesToRetrieve clearJobAttributesToRetrieve()}.
   * <P>
   * @param attribute The job attribute to retrieve.
   * Possible values are all job attributes contained in the {@link com.ibm.as400.access.Job Job} class,
   * <b>excluding</b> the following:
   * <UL>
   * <LI>Job.CLIENT_IP_ADDRESS
   * <LI>Job.CURRENT_LIBRARY
   * <LI>Job.CURRENT_LIBRARY_EXISTENCE
   * <LI>Job.ELAPSED_CPU_PERCENT_USED
   * <LI>Job.ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
   * <LI>Job.ELAPSED_CPU_TIME_USED
   * <LI>Job.ELAPSED_CPU_TIME_USED_FOR_DATABASE
   * <LI>Job.ELAPSED_DISK_IO
   * <LI>Job.ELAPSED_DISK_IO_ASYNCH
   * <LI>Job.ELAPSED_DISK_IO_SYNCH
   * <LI>Job.ELAPSED_INTERACTIVE_RESPONSE_TIME
   * <LI>Job.ELAPSED_INTERACTIVE_TRANSACTIONS
   * <LI>Job.ELAPSED_LOCK_WAIT_TIME
   * <LI>Job.ELAPSED_PAGE_FAULTS
   * <LI>Job.ELAPSED_TIME
   * <LI>Job.PRODUCT_LIBRARIES
   * <LI>Job.SUBMITTED_BY_JOB_NUMBER
   * <LI>Job.SUBMITTED_BY_USER
   * <LI>Job.SYSTEM_LIBRARY_LIST
   * <LI>Job.USER_LIBRARY_LIST
   * </UL>
   * To retrieve any of the ELAPSED statistics, use the {@link com.ibm.as400.access.Job#resetStatistics Job.resetStatistics()} and
   * {@link com.ibm.as400.access.Job#loadStatistics Job.loadStatistics()} methods.
   * @see #clearJobAttributesToRetrieve
   * @see com.ibm.as400.access.Job
  **/
  public void addJobAttributeToRetrieve(int attribute)
  {
    if (attribute < 101)
    {
      throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    switch(attribute)
    {
      // These are always loaded, so the user was crazy to try and preload them.
      case Job.JOB_NAME:
      case Job.USER_NAME:
      case Job.JOB_NUMBER:
      case Job.JOB_TYPE:
      case Job.JOB_SUBTYPE:
      case Job.JOB_STATUS:
      case Job.INTERNAL_JOB_ID:
      case Job.INTERNAL_JOB_IDENTIFIER:
        return;
      // These cannot be retrieved this way. You have to make another call to the QUSRJOBI API to get them.
      case Job.CURRENT_LIBRARY:
      case Job.CURRENT_LIBRARY_EXISTENCE:
      case Job.ELAPSED_CPU_PERCENT_USED:
      case Job.ELAPSED_CPU_PERCENT_USED_FOR_DATABASE:
      case Job.ELAPSED_CPU_TIME_USED:
      case Job.ELAPSED_CPU_TIME_USED_FOR_DATABASE:
      case Job.ELAPSED_DISK_IO:
      case Job.ELAPSED_DISK_IO_ASYNCH:
      case Job.ELAPSED_DISK_IO_SYNCH:
      case Job.ELAPSED_INTERACTIVE_RESPONSE_TIME:
      case Job.ELAPSED_INTERACTIVE_TRANSACTIONS:
      case Job.ELAPSED_LOCK_WAIT_TIME:
      case Job.ELAPSED_PAGE_FAULTS:
      case Job.ELAPSED_TIME:
      case Job.PRODUCT_LIBRARIES:
      case Job.SUBMITTED_BY_JOB_NUMBER:
      case Job.SUBMITTED_BY_USER:
      case Job.SYSTEM_LIBRARY_LIST:
      case Job.USER_LIBRARY_LIST:
        throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      // The date and time have different key values for getting and setting.
      case Job.SCHEDULE_DATE:
      case Job.SCHEDULE_TIME:
        attribute = Job.SCHEDULE_DATE_GETTER;
        break;
      case Job.INSTANCE:
      case Job.LOCATION_NAME:
      case Job.NETWORK_ID:
      case Job.SEQUENCE_NUMBER:
        attribute = Job.UNIT_OF_WORK_ID;
        break;
      default:
        break;
    }

    if (currentKey_ >= keys_.length)
    {
      // Resize
      int[] temp = keys_;
      keys_ = new int[temp.length*2];
      System.arraycopy(temp, 0, keys_, 0, temp.length);
    }

    keys_[currentKey_++] = attribute;
    resetHandle();
  }

  
  /**
   * Adds a selection type and value to be used to filter the list of jobs. If a selection type
   * supports only one value, then the selection value used will be the one that was passed on
   * the most recent call to this method for that selection type.
   *<P>
   * By default, all jobs are selected, because most of the filter criteria settings default to true.
   * To filter the list further, the job selection criteria
   * should be set to false. For example, to retrieve a list of only active jobs, you would do:
   * <PRE>
   * JobList list = new JobList(system);
   * list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_ACTIVE, Boolean.TRUE);
   * list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_JOBQ, Boolean.FALSE);
   * list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ, Boolean.FALSE);
   * Enumeration jobs = list.getJobs();
   * </PRE>
   * <P>
   * The list of job selection criteria is maintained internally even when this JobList is closed and re-used.
   * To start over with a new set of job selection criteria, call {@link #clearJobSelectionCriteria clearJobSelectionCriteria()}.
   * @param selectionType The constant indicating which selection type used to filter the list.
   * Possible values are:
   * <UL>
   * <LI>{@link #SELECTION_JOB_NAME SELECTION_JOB_NAME}
   * <LI>{@link #SELECTION_USER_NAME SELECTION_USER_NAME}
   * <LI>{@link #SELECTION_JOB_NUMBER SELECTION_JOB_NUMBER}
   * <LI>{@link #SELECTION_JOB_TYPE SELECTION_JOB_TYPE}
   * <LI>{@link #SELECTION_PRIMARY_JOB_STATUS_ACTIVE SELECTION_PRIMARY_JOB_STATUS_ACTIVE}
   * <LI>{@link #SELECTION_PRIMARY_JOB_STATUS_JOBQ SELECTION_PRIMARY_JOB_STATUS_JOBQ}
   * <LI>{@link #SELECTION_PRIMARY_JOB_STATUS_OUTQ SELECTION_PRIMARY_JOB_STATUS_OUTQ}
   * <LI>{@link #SELECTION_ACTIVE_JOB_STATUS SELECTION_ACTIVE_JOB_STATUS}
   * <LI>{@link #SELECTION_JOB_QUEUE_STATUS_SCHEDULE SELECTION_JOB_QUEUE_STATUS_SCHEDULE}
   * <LI>{@link #SELECTION_JOB_QUEUE_STATUS_READY SELECTION_JOB_QUEUE_STATUS_READY}
   * <LI>{@link #SELECTION_JOB_QUEUE_STATUS_HELD SELECTION_JOB_QUEUE_STATUS_HELD}
   * <LI>{@link #SELECTION_JOB_QUEUE SELECTION_JOB_QUEUE}
   * <LI>{@link #SELECTION_INITIAL_USER SELECTION_INITIAL_USER}
   * <LI>{@link #SELECTION_SERVER_TYPE SELECTION_SERVER_TYPE}
   * <LI>{@link #SELECTION_JOB_TYPE_ENHANCED SELECTION_JOB_TYPE_ENHANCED}
   * </UL>
   * @param selectionValue The value for the selection type. See the individual selection type
   * constants for the appropriate object or constant to use. Some selection types allow multiple
   * selection values to be added.
   * @see #clearJobSelectionCriteria
   * @see com.ibm.as400.access.Job
  **/
  public void addJobSelectionCriteria(int selectionType, Object selectionValue) throws PropertyVetoException
  {
    if (selectionType < 0 || selectionType > SELECTION_JOB_TYPE_ENHANCED)
    {
      throw new ExtendedIllegalArgumentException("selectionType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (selectionValue == null) throw new NullPointerException("selectionValue");

    switch(selectionType)
    {
      case SELECTION_JOB_NAME:
        setName((String)selectionValue);
        break;
      case SELECTION_USER_NAME:
        setUser((String)selectionValue);
        break;
      case SELECTION_JOB_NUMBER:
        setNumber((String)selectionValue);
        break;
      case SELECTION_JOB_TYPE:
        selectionJobType_ = ((String)selectionValue).toUpperCase();
        break;
      case SELECTION_PRIMARY_JOB_STATUS_ACTIVE:
        selectActiveJobs_ = ((Boolean)selectionValue).booleanValue();
        break;
      case SELECTION_PRIMARY_JOB_STATUS_JOBQ:
        selectJobQueueJobs_ = ((Boolean)selectionValue).booleanValue();
        break;
      case SELECTION_PRIMARY_JOB_STATUS_OUTQ:
        selectOutQueueJobs_ = ((Boolean)selectionValue).booleanValue();
        break;
      case SELECTION_ACTIVE_JOB_STATUS:
        String status = ((String)selectionValue).toUpperCase();
        if (currentActiveStatus_ > activeStatuses_.length)
        {
          String[] temp = activeStatuses_;
          activeStatuses_ = new String[temp.length*2];
          System.arraycopy(temp, 0, activeStatuses_, 0, temp.length);
        }
        activeStatuses_[currentActiveStatus_++] = status;
        break;
      case SELECTION_JOB_QUEUE_STATUS_SCHEDULE:
        selectScheduledJobs_ = ((Boolean)selectionValue).booleanValue();
        break;
      case SELECTION_JOB_QUEUE_STATUS_HELD:
        selectHeldJobs_ = ((Boolean)selectionValue).booleanValue();
        break;
      case SELECTION_JOB_QUEUE_STATUS_READY:
        selectReadyJobs_ = ((Boolean)selectionValue).booleanValue();
        break;
      case SELECTION_JOB_QUEUE:
        String queue = (String)selectionValue;
        QSYSObjectPathName path = new QSYSObjectPathName(queue);
        StringBuffer buf = new StringBuffer();
        String name = path.getObjectName();
        buf.append(name);
        for (int i=name.length(); i<10; ++i)
        {
          buf.append(' ');
        }
        String lib = path.getLibraryName();
        buf.append(lib);
        
        if (currentJobQueue_ > jobQueues_.length)
        {
          String[] temp = jobQueues_;
          jobQueues_ = new String[temp.length*2];
          System.arraycopy(temp, 0, jobQueues_, 0, temp.length);
        }
        jobQueues_[currentJobQueue_++] = buf.toString();
        break;
      case SELECTION_INITIAL_USER:
        String profile = ((String)selectionValue).toUpperCase();
        if (currentInitialUser_ > initialUsers_.length)
        {
          String[] temp = initialUsers_;
          initialUsers_ = new String[temp.length*2];
          System.arraycopy(temp, 0, initialUsers_, 0, temp.length);
        }
        initialUsers_[currentInitialUser_++] = profile;
        break;
      case SELECTION_SERVER_TYPE:
        String type = ((String)selectionValue).toUpperCase();
        if (currentServerType_ > serverTypes_.length)
        {
          String[] temp = serverTypes_;
          serverTypes_ = new String[temp.length*2];
          System.arraycopy(temp, 0, serverTypes_, 0, temp.length);
        }
        serverTypes_[currentServerType_++] = type;
        break;
      case SELECTION_JOB_TYPE_ENHANCED:
        int val = ((Integer)selectionValue).intValue();
        if (currentEnhancedJobType_ > enhancedJobTypes_.length)
        {
          int[] temp = enhancedJobTypes_;
          enhancedJobTypes_ = new int[temp.length*2];
          System.arraycopy(temp, 0, enhancedJobTypes_, 0, temp.length);
        }
        enhancedJobTypes_[currentEnhancedJobType_++] = val;
        break;
      default:
        throw new ExtendedIllegalArgumentException("selectionType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    resetHandle();
  }


  /**
   * Adds a job attribute used to sort the list.
   * <P>
   * The list of job attributes to sort on is maintained internally even when this JobList is closed and re-used.
   * To start over with a new set of job attributes to sort on, call {@link #clearJobAttributesToSortOn clearJobAttributesToSortOn()}.
   * @param attribute The job attribute on which to sort.
   * Possible values are all job attributes contained in the {@link com.ibm.as400.access.Job Job} class,
   * <b>excluding</b> the following:
   * <UL>
   * <LI>Job.CLIENT_IP_ADDRESS
   * <LI>Job.CURRENT_LIBRARY
   * <LI>Job.CURRENT_LIBRARY_EXISTENCE
   * <LI>Job.ELAPSED_CPU_PERCENT_USED
   * <LI>Job.ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
   * <LI>Job.ELAPSED_CPU_TIME_USED
   * <LI>Job.ELAPSED_CPU_TIME_USED_FOR_DATABASE
   * <LI>Job.ELAPSED_DISK_IO
   * <LI>Job.ELAPSED_DISK_IO_ASYNCH
   * <LI>Job.ELAPSED_DISK_IO_SYNCH
   * <LI>Job.ELAPSED_INTERACTIVE_RESPONSE_TIME
   * <LI>Job.ELAPSED_INTERACTIVE_TRANSACTIONS
   * <LI>Job.ELAPSED_LOCK_WAIT_TIME
   * <LI>Job.ELAPSED_PAGE_FAULTS
   * <LI>Job.ELAPSED_TIME
   * <LI>Job.PRODUCT_LIBRARIES
   * <LI>Job.SUBMITTED_BY_JOB_NUMBER
   * <LI>Job.SUBMITTED_BY_USER
   * <LI>Job.SYSTEM_LIBRARY_LIST
   * <LI>Job.USER_LIBRARY_LIST
   * </UL>
   * @param sortOrder true to sort ascending; false to sort descending.
   * @see #clearJobAttributesToSortOn
   * @see com.ibm.as400.access.Job
  **/
  public void addJobAttributeToSortOn(int attribute, boolean sortOrder)
  {
    if (attribute < 101)
    {
      throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    switch(attribute)
    {
      // These cannot be retrieved this way. You have to make another call to the QUSRJOBI API to get them.
      case Job.CURRENT_LIBRARY:
      case Job.CURRENT_LIBRARY_EXISTENCE:
      case Job.ELAPSED_CPU_PERCENT_USED:
      case Job.ELAPSED_CPU_PERCENT_USED_FOR_DATABASE:
      case Job.ELAPSED_CPU_TIME_USED:
      case Job.ELAPSED_CPU_TIME_USED_FOR_DATABASE:
      case Job.ELAPSED_DISK_IO:
      case Job.ELAPSED_DISK_IO_ASYNCH:
      case Job.ELAPSED_DISK_IO_SYNCH:
      case Job.ELAPSED_INTERACTIVE_RESPONSE_TIME:
      case Job.ELAPSED_INTERACTIVE_TRANSACTIONS:
      case Job.ELAPSED_LOCK_WAIT_TIME:
      case Job.ELAPSED_PAGE_FAULTS:
      case Job.ELAPSED_TIME:
      case Job.PRODUCT_LIBRARIES:
      case Job.SUBMITTED_BY_JOB_NUMBER:
      case Job.SUBMITTED_BY_USER:
      case Job.SYSTEM_LIBRARY_LIST:
      case Job.USER_LIBRARY_LIST:
        throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      // The date and time have different key values for getting and setting.
//      case Job.SCHEDULE_DATE:
//      case Job.SCHEDULE_TIME:
//        attribute = Job.SCHEDULE_DATE_GETTER;
//        break;
      default:
        break;
    }

    if (currentSortKey_ >= sortKeys_.length)
    {
      int[] temp = sortKeys_;
      sortKeys_ = new int[temp.length*2];
      System.arraycopy(temp, 0, sortKeys_, 0, temp.length);
      boolean[] tempSort = sortOrders_;
      sortOrders_ = new boolean[tempSort.length*2];
      System.arraycopy(tempSort, 0, sortOrders_, 0, tempSort.length);
    }
    sortKeys_[currentSortKey_] = attribute;
    sortOrders_[currentSortKey_++] = sortOrder;
    resetHandle();
  }
  

  /**
   * Clears the job attributes used to sort the list. This resets all of the 
   * job sort parameters to their default values.
   * @see #addJobAttributeToSortOn
  **/
  public void clearJobAttributesToSortOn()
  {
    currentSortKey_ = 0;
    sortKeys_ = new int[1];
    sortOrders_ = new boolean[1];
    resetHandle();
  }
  
  
  /**
   * Clears the job attributes to be retrieved. This removes all of the job
   * attributes that would be retrieved. Some attributes are always
   * retrieved, regardless if they are in this list or not, such as job name, job number, and user name.
   * @see #addJobAttributeToRetrieve
  **/
  public void clearJobAttributesToRetrieve()
  {
    currentKey_ = 0;
    keys_ = new int[1];
    resetHandle();
  }
  
  
  /**
   * Clears the selection types and values used to filter the list of jobs.
   * This resets all of the job selection parameters to their default values.
   * @see #addJobSelectionCriteria
  **/
  public void clearJobSelectionCriteria() throws PropertyVetoException
  {
    // In case someone wants to veto us.
    setName(SELECTION_JOB_NAME_ALL);
    setUser(SELECTION_USER_NAME_ALL);
    setNumber(SELECTION_JOB_NUMBER_ALL);

    selectionJobType_ = SELECTION_JOB_TYPE_ALL;
    selectActiveJobs_ = true;
    selectJobQueueJobs_ = true;
    selectOutQueueJobs_ = true;
    activeStatuses_ = new String[1];
    currentActiveStatus_ = 0;
    selectHeldJobs_ = true;
    selectScheduledJobs_ = true;
    selectReadyJobs_ = true;
    jobQueues_ = new String[1];
    currentJobQueue_ = 0;
    initialUsers_ = new String[1];
    currentInitialUser_ = 0;
    serverTypes_ = new String[1];
    currentServerType_ = 0;
    enhancedJobTypes_ = new int[1];
    currentEnhancedJobType_ = 0;
    resetHandle();
  }
   

/**
   * Adds a PropertyChangeListener.  The specified PropertyChangeListener's
   * <b>propertyChange()</b> method will be called each time the value of
   * any bound property is changed.
   * @param listener The listener.
   * @see #removePropertyChangeListener
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (propertyChangeSupport_ == null) propertyChangeSupport_ = new PropertyChangeSupport(this);
    propertyChangeSupport_.addPropertyChangeListener(listener);
  }



/**
   * Adds a VetoableChangeListener.  The specified VetoableChangeListener's
   * <b>vetoableChange()</b> method will be called each time the value of
   * any constrained property is changed.
   * @param listener The listener.
   * @see #removeVetoableChangeListener
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (vetoableChangeSupport_ == null) vetoableChangeSupport_ = new VetoableChangeSupport(this);
    vetoableChangeSupport_.addVetoableChangeListener(listener);
  }



  /**
   * Closes the job list on the system.
   * This releases any system resources previously in use by this job list.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
  **/
  public synchronized void close() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!isConnected_)
    {
      return;
    }
    if (handleToClose_ != null && (handle_ == null || handle_ == handleToClose_))
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
    }
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Closing job list with handle: ", handle_);
      if (trackers_ != null)
      {
        int inUse = 0;
        for (int i=0; i<trackers_.size(); ++i)
        {
          Tracker tracker = (Tracker)trackers_.elementAt(i);
          if (tracker.isSet()) ++inUse;
          tracker.set(false); // Force the Enumeration to shut down since the JobList is being closed.
        }
        if (inUse > 0)
        {
          Trace.log(Trace.WARNING, "The job list on the server is possibly in use by "+inUse+" or more enumerations as a result of a call to JobList.getJobs().");
        }
      }
    }
    ProgramParameter[] parms = new ProgramParameter[]
    {
      new ProgramParameter(handle_),
      errorCode_
    };
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }
    isConnected_ = false;
    handle_ = null;
    if (handleToClose_ != null) // Just in case.
    {
      handle_ = handleToClose_;
      handleToClose_ = null;
      close();
    }
  }


  /**
   * Closes the job list on the system when this object is garbage collected.
  **/
  protected void finalize() throws Throwable
  {
    try
    {
      close();
    }
    catch(Exception e)
    {
    }
    super.finalize();
  }


/**
Returns an Enumeration that wraps the list of jobs on the server.
This method calls {@link #load load()} implicitly if needed.
The Enumeration retrieves jobs from the server in blocks as needed when
nextElement() is called.  This JobList should not be closed until the
program is done processing elements out of the Enumeration.  That is, this
method does not retrieve all of the jobs from the server up front -- it retrieves
them as needed, which allows for a lower memory footprint versus
more calls to the server. The block size used internally by the Enumeration is set to
1000 jobs.

@return An Enumeration of {@link com.ibm.as400.access.Job Job} objects.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception ServerStartupException          If the AS/400 server cannot be started.
@exception UnknownHostException            If the AS/400 system cannot be located.
@see com.ibm.as400.access.Job
**/
  public synchronized Enumeration getJobs() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (handle_ == null)
    {
      load(); // Need to get the length_
    }
    
    // Use a tracker so we know if someone tries to close us, whether or not they
    // have open Enumerations.  It's possible they do, and they are just done with
    // them, but this is mostly for debugging purposes.
    Tracker tracker = new Tracker();
    tracker.set(true);
    
    if (trackers_ == null) trackers_ = new Vector();
    trackers_.addElement(tracker);

    // Remove dead trackers to prevent a memory leak.
    // JobEnumerations whose hasMoreElements() return false, or those who
    // have been garbage collected, will all have their freed their trackers.
    for (int i=trackers_.size()-1; i >= 0; --i)
    {
      Tracker t = (Tracker)trackers_.elementAt(i);
      if (!t.isSet()) trackers_.removeElementAt(i);
    }

    return new JobEnumeration(this, length_, tracker);
  }

  
  /**
   * Returns a subset of the list of jobs in the job list.
   * This method allows the user to retrieve the job list from the server
   * in pieces. If a call to {@link #load load()} is made (either implicitly or explicitly),
   * then the jobs at a given offset will change, so a subsequent call to
   * getJobs() with the same <i>listOffset</i> and <i>number</i>
   * will most likely not return the same Jobs as the previous call.
   * @param listOffset The offset into the list of jobs. This value must be greater than 0 and
   * less than the list length, or specify -1 to retrieve all of the jobs.
   * @param number The number of jobs to retrieve out of the list, starting at the specified
   * <i>listOffset</i>. This value must be greater than or equal to 0 and less than or equal
   * to the list length. If the <i>listOffset</i> is -1, this parameter is ignored.
   * @return The array of retrieved {@link com.ibm.as400.access.Job Job} objects.
   * The length of this array may not necessarily be equal to <i>number</i>, depending upon the size
   * of the list on the server, and the specified <i>listOffset</i>.
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @see com.ibm.as400.access.Job
  **/
  public Job[] getJobs(int listOffset, int number) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (listOffset < -1)
    {
      throw new ExtendedIllegalArgumentException("listOffset", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }

    if (number < 0 && listOffset != -1)
    {
      throw new ExtendedIllegalArgumentException("number", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
    }
    
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    if (number == 0 && listOffset != -1)
    {
      return new Job[0];
    }

    if (handle_ == null)
    {
      load();
    }

    if (listOffset == -1) number = length_;

    if (number == 0)
    {
      return new Job[0];
    }

    int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    
    int numKeys = numKeysReturned_;
    int total = 0;
    for (int i=0; i<numKeys; ++i)
    {
      int kl = keyLengthsReturned_[i];
      total += kl;
      total += (4 - (kl % 4)) % 4; // Need to pad to the end of the boundary
    }

    ProgramParameter[] parms2 = new ProgramParameter[7];
    int len = (60+total*numKeys)*number; // Boundaries of 4
    parms2[0] = new ProgramParameter(len); // receiver variable
    parms2[1] = new ProgramParameter(BinaryConverter.intToByteArray(len)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_);
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(BinaryConverter.intToByteArray(number)); // number of records to return
    parms2[5] = new ProgramParameter(BinaryConverter.intToByteArray(listOffset == -1 ? -1 : listOffset+1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }
    
    byte[] listInfo = parms2[3].getOutputData();
    int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
    int recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    while (listOffset == -1 && totalRecords > recordsReturned)
    {
      len = len*(1+(totalRecords/(recordsReturned+1)));
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Calling JobList QGYGTLE again with an updated length of "+len+".");
      try
      {
        parms2[0].setOutputDataLength(len);
        parms2[1].setInputData(BinaryConverter.intToByteArray(len));
      }
      catch(PropertyVetoException pve) {}
      if (!pc2.run())
      {
        throw new AS400Exception(pc2.getMessageList());
      }
      listInfo = parms2[3].getOutputData();
      totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
      recordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    }

    
    ListUtilities.checkListStatus(listInfo[30]);  // check the list status indicator
    byte[] data = parms2[0].getOutputData();

    Job[] jobs = new Job[recordsReturned];
    int offset = 0;
    for (int i=0; i<jobs.length; ++i) // each job
    {
      String jobName = conv.byteArrayToString(data, offset, 10);
      String userName = conv.byteArrayToString(data, offset+10, 10);
      String jobNumber = conv.byteArrayToString(data, offset+20, 6);
      String status = conv.byteArrayToString(data, offset+42, 10);
      String jobType = conv.byteArrayToString(data, offset+52, 1);
      String jobSubtype = conv.byteArrayToString(data, offset+53, 1);

      jobs[i] = new Job(system_, jobName.trim(), userName.trim(), jobNumber.trim(), status, jobType, jobSubtype);

      for (int j=0; j<numKeys; ++j)
      {
        int keyOffset = keyOffsetsReturned_[j];
        
        if (keyTypesReturned_[j] == 'C')
        {
          ConvTable valConv = ConvTable.getTable(ccsid, null);
          String value = valConv.byteArrayToString(data, offset+keyOffset, keyLengthsReturned_[j]);
          jobs[i].setValueInternal(keyFieldsReturned_[j], value);
        }
        else
        {
          if (keyLengthsReturned_[j] > 4)
          {
            jobs[i].setAsLong(keyFieldsReturned_[j], BinaryConverter.byteArrayToLong(data, offset+keyOffset));
          }
          else
          {
            jobs[i].setAsInt(keyFieldsReturned_[j], BinaryConverter.byteArrayToInt(data, offset+keyOffset));
          }
        }
      }
      offset += total + 60;
    }

    return jobs;
  }


/**
Returns the number of jobs in the list. This method implicitly calls {@link #load load()} 
if it has not already been called.
@return The number of jobs, or 0 if no list was retrieved.
@see #load
**/
  public int getLength()
  {
    try
    {
      if (handle_ == null)
      {
        load();
      }
    }
    catch(Exception e)
    {
      if (Trace.traceOn_)
      {
        Trace.log(Trace.ERROR, "Exception caught on JobList getLength():", e);
      }
    }

    return length_;
  }



/**
   * Returns the job name that describes which jobs are returned.
   * @return The job name.
   * @see #setName
**/
  public String getName()
  {
    return selectionJobName_;
  }



/**
   * Returns the job number that describes which jobs are returned.
   * @return The job number.
   * @see #setNumber
**/
  public String getNumber()
  {
    return selectionJobNumber_;
  }



/**
   * Returns the system.
   * @return The system.
   * @see #setSystem
**/
  public AS400 getSystem()
  {
    return system_;
  }



/**
   * Returns the user name that describes which jobs are returned.
   * @return The user name.
   * @see #setUser
**/
  public String getUser()
  {
    return selectionUserName_;
  }



  /**
   * Loads the list of jobs on the system. This method informs the
   * system to build a list of jobs given the previously added job
   * attributes to select, retrieve, and sort. This method blocks until the system returns
   * the total number of jobs it has compiled. A subsequent call to
   * {@link #getJobs getJobs()} will retrieve the actual job information
   * and attributes for each job in the list from the system.
   * <p>This method updates the list length.
   *
   * @exception AS400Exception                  If the system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the system.
   * @exception ObjectDoesNotExistException     If the object does not exist on the system.
   * @exception ServerStartupException          If the server cannot be started.
   * @exception UnknownHostException            If the system cannot be located.
   * @see #getLength
  **/
  public synchronized void load() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    // Close the previous list
    if (handle_ != null || handleToClose_ != null)
    {
      close();
    }
    
    // Generate text objects based on system CCSID
    final int ccsid = system_.getCcsid();
    ConvTable conv = ConvTable.getTable(ccsid, null);
    AS400Text text1 = new AS400Text(1, ccsid, system_);
    AS400Text text6 = new AS400Text(6, ccsid, system_);
    AS400Text text10 = new AS400Text(10, ccsid, system_);

    // Figure out our selection criteria.
    String[] primaryJobStatuses = null;
    if (selectActiveJobs_ || selectJobQueueJobs_ || selectOutQueueJobs_)
    {
      int size = (selectActiveJobs_ ? 1:0)+(selectJobQueueJobs_ ? 1:0)+(selectOutQueueJobs_ ? 1:0);
      primaryJobStatuses = new String[size];
      int i = 0;
      if (selectActiveJobs_)
      {
        primaryJobStatuses[i++] = "*ACTIVE";
      }
      if (selectJobQueueJobs_)
      {
        primaryJobStatuses[i++] = "*JOBQ";
      }
      if (selectOutQueueJobs_)
      {
        primaryJobStatuses[i] = "*OUTQ";
      }
    }
    String[] jobQueueStatuses = null;
    if (selectJobQueueJobs_ && (selectHeldJobs_ || selectScheduledJobs_ || selectReadyJobs_))
    {
      int size = (selectHeldJobs_ ? 1:0)+(selectScheduledJobs_ ? 1:0)+(selectReadyJobs_ ? 1:0);
      jobQueueStatuses = new String[size];
      int i = 0;
      if (selectHeldJobs_)
      {
        jobQueueStatuses[i++] = "HLD";
      }
      if (selectScheduledJobs_)
      {
        jobQueueStatuses[i++] = "SCD";
      }
      if (selectReadyJobs_)
      {
        jobQueueStatuses[i++] = "RLS";
      }
    }

    byte[] selectionInfo = new byte[108 +
                                    (primaryJobStatuses == null ? 0 : primaryJobStatuses.length*10) +
                                    currentActiveStatus_*4 +
                                    (jobQueueStatuses == null ? 0 : jobQueueStatuses.length*10) +
                                    currentJobQueue_*20 +
                                    currentInitialUser_*10 +
                                    currentServerType_*30 +
                                    currentEnhancedJobType_*4];
    text10.toBytes(selectionJobName_.toUpperCase(), selectionInfo, 0);
    text10.toBytes(selectionUserName_.toUpperCase(), selectionInfo, 10);
    text6.toBytes(selectionJobNumber_, selectionInfo, 20);
    text1.toBytes(selectionJobType_, selectionInfo, 26);
    int offset = 108;
    if (primaryJobStatuses != null)
    {
      BinaryConverter.intToByteArray(offset, selectionInfo, 28);
      BinaryConverter.intToByteArray(primaryJobStatuses.length, selectionInfo, 32);
      for (int i=0; i<primaryJobStatuses.length; ++i)
      {
        text10.toBytes(primaryJobStatuses[i], selectionInfo, offset);
        offset += 10;
      }
    }
    if (currentActiveStatus_ > 0)
    {
      // Make sure ACTIVE_JOB_STATUS key is specified.
      boolean foundKey = false;
      for (int i=0; i<currentKey_ && !foundKey; ++i)
      {
        if (keys_[i] == Job.ACTIVE_JOB_STATUS)
        {
          foundKey = true;
        }
      }
      if (!foundKey)
      {
        addJobAttributeToRetrieve(Job.ACTIVE_JOB_STATUS);
      }
      
      BinaryConverter.intToByteArray(offset, selectionInfo, 36);
      BinaryConverter.intToByteArray(currentActiveStatus_, selectionInfo, 40);
      AS400Text text4 = new AS400Text(4, ccsid, system_);
      for (int i=0; i<currentActiveStatus_; ++i)
      {
        text4.toBytes(activeStatuses_[i], selectionInfo, offset);
        offset += 4;
      }
    }
    if (jobQueueStatuses != null)
    {
      // Make sure JOB_QUEUE_STATUS key is specified.
      boolean foundKey = false;
      for (int i=0; i<currentKey_ && !foundKey; ++i)
      {
        if (keys_[i] == Job.JOB_QUEUE_STATUS)
        {
          foundKey = true;
        }
      }
      if (!foundKey)
      {
        addJobAttributeToRetrieve(Job.JOB_QUEUE_STATUS);
      }
      
      BinaryConverter.intToByteArray(offset, selectionInfo, 44);
      BinaryConverter.intToByteArray(jobQueueStatuses.length, selectionInfo, 48);
      for (int i=0; i<jobQueueStatuses.length; ++i)
      {
        text10.toBytes(jobQueueStatuses[i], selectionInfo, offset);
        offset += 10;
      }
    }
    if (currentJobQueue_ > 0)
    {
      // Make sure JOB_QUEUE key is specified.
      boolean foundKey = false;
      for (int i=0; i<currentKey_ && !foundKey; ++i)
      {
        if (keys_[i] == Job.JOB_QUEUE)
        {
          foundKey = true;
        }
      }
      if (!foundKey)
      {
        addJobAttributeToRetrieve(Job.JOB_QUEUE);
      }
      
      BinaryConverter.intToByteArray(offset, selectionInfo, 52);
      BinaryConverter.intToByteArray(currentJobQueue_, selectionInfo, 56);
      AS400Text text20 = new AS400Text(20, ccsid, system_);
      for (int i=0; i<currentJobQueue_; ++i)
      {
        text20.toBytes(jobQueues_[i], selectionInfo, offset);
        offset += 20;
      }
    }
    if (currentInitialUser_ > 0)
    {
      // Make sure CURRENT_USER key is specified.
      boolean foundKey = false;
      for (int i=0; i<currentKey_ && !foundKey; ++i)
      {
        if (keys_[i] == Job.CURRENT_USER)
        {
          foundKey = true;
        }
      }
      if (!foundKey)
      {
        addJobAttributeToRetrieve(Job.CURRENT_USER);
      }
      
      BinaryConverter.intToByteArray(offset, selectionInfo, 60);
      BinaryConverter.intToByteArray(currentInitialUser_, selectionInfo, 64);
      for (int i=0; i<currentInitialUser_; ++i)
      {
        text10.toBytes(initialUsers_[i], selectionInfo, offset);
        offset += 10;
      }
    }
    if (currentServerType_ > 0)
    {
      // Make sure SERVER_TYPE key is specified.
      boolean foundKey = false;
      for (int i=0; i<currentKey_ && !foundKey; ++i)
      {
        if (keys_[i] == Job.SERVER_TYPE)
        {
          foundKey = true;
        }
      }
      if (!foundKey)
      {
        addJobAttributeToRetrieve(Job.SERVER_TYPE);
      }
      
      BinaryConverter.intToByteArray(offset, selectionInfo, 68);
      BinaryConverter.intToByteArray(currentServerType_, selectionInfo, 72);
      AS400Text text30 = new AS400Text(30, ccsid, system_);
      for (int i=0; i<currentServerType_; ++i)
      {
        text30.toBytes(serverTypes_[i], selectionInfo, offset);
        offset += 30;
      }
    }
    if (currentEnhancedJobType_ > 0)
    {
      // Make sure JOB_TYPE_ENHANCED key is specified.
      boolean foundKey = false;
      for (int i=0; i<currentKey_ && !foundKey; ++i)
      {
        if (keys_[i] == Job.JOB_TYPE_ENHANCED)
        {
          foundKey = true;
        }
      }
      if (!foundKey)
      {
        addJobAttributeToRetrieve(Job.JOB_TYPE_ENHANCED);
      }
      
      BinaryConverter.intToByteArray(offset, selectionInfo, 92);
      BinaryConverter.intToByteArray(currentEnhancedJobType_, selectionInfo, 96);
      for (int i=0; i<currentEnhancedJobType_; ++i)
      {
        BinaryConverter.intToByteArray(enhancedJobTypes_[i], selectionInfo, offset);
        offset += 4;
      }
    }

    
    // Key values to retrieve.
    int numKeys = currentKey_;
    int[] keys = keys_;
    
    byte[] keyData = new byte[4*numKeys];
    for (int i=0; i<numKeys; ++i)
    {
      BinaryConverter.intToByteArray(keys[i], keyData, i*4);
    }

    // Setup program parameters
    ProgramParameter[] parms = new ProgramParameter[14];
    int len = 20*numKeys + 64 + (primaryJobStatuses == null ? 0 : primaryJobStatuses.length*10);
    parms[0] = new ProgramParameter(len); // receiver variable  - this should really be 56, but RCHASF6D doesn't like that number for some odd reason.
    parms[1] = new ProgramParameter(bin4_.toBytes(len)); // length of receiver variable
    parms[2] = new ProgramParameter(conv.stringToByteArray("OLJB0200"));
    parms[3] = new ProgramParameter(4+20*numKeys); // receiver variable definition information
    parms[4] = new ProgramParameter(bin4_.toBytes(4+20*numKeys)); // length of receiver variable definition information
    parms[5] = new ProgramParameter(80); // list information
    parms[6] = new ProgramParameter(bin4_.toBytes(1)); // number of records to return (have to specify at least 1... for some reason 0 doesn't work)
    
    byte[] sortInfo = null;
    int numSortKeys = currentSortKey_;
    if (numSortKeys > 0)
    {
      sortInfo = new byte[4+numSortKeys*12];
      BinaryConverter.intToByteArray(numSortKeys, sortInfo, 0);
      offset = 4;
      for (int i=0; i<numSortKeys; ++i)
      {
        int key = sortKeys_[i];
        boolean order = sortOrders_[i];
        int fieldLength = Job.setterKeys_.get(key);
        int fieldStartingPosition = 0;
        short dataType = (short)4; // Data type 4 = character data, NLS-sort supported, DBCS treated as single-byte.
        // We'll use 0 (signed binary) for all of the int types:
        switch(key)
        {
          case Job.CCSID:
          case Job.CPU_TIME_USED:
          case Job.DEFAULT_WAIT_TIME:
          case Job.END_SEVERITY:
          case Job.MESSAGE_QUEUE_MAX_SIZE:
          case Job.JOB_END_REASON:
          case Job.JOB_TYPE_ENHANCED:
          case Job.LOGGING_SEVERITY:
          case Job.MAX_CPU_TIME:
          case Job.MAX_TEMP_STORAGE:
          //case Job.MAX_THREADS:
          case Job.AUXILIARY_IO_REQUESTS:
          case Job.INTERACTIVE_TRANSACTIONS:
          //case Job.NUM_DATABASE_LOCK_WAITS:
          //case Job.NUM_INTERNAL_MACHINE_LOCK_WAITS:
          //case Job.NUM_NONDATABASE_LOCK_WAITS:
          case Job.PRODUCT_RETURN_CODE:
          case Job.PROGRAM_RETURN_CODE:
          //case Job.PROCESS_ID:
          case Job.TOTAL_RESPONSE_TIME:
          case Job.RUN_PRIORITY:
          case Job.SYSTEM_POOL_ID:
          //case Job.SIGNAL_STATUS:
          case Job.TIME_SLICE:
          case Job.TEMP_STORAGE_USED:
          case Job.USER_RETURN_CODE:
            dataType = (short)0; // signed binary
            fieldLength = 4;
            break;
          case Job.TEMP_STORAGE_USED_LARGE:
          case Job.MAX_TEMP_STORAGE_LARGE:
            dataType = (short)9; // unsigned binary
            fieldLength = 4;
            break;
          case Job.CPU_TIME_USED_LARGE:
          case Job.CPU_TIME_USED_FOR_DATABASE:
          case Job.AUXILIARY_IO_REQUESTS_LARGE:
            dataType = (short)9; // unsigned binary
            fieldLength = 8;
            break;
          default:
            //dataType = (short)4;
            break;
        }
        
        // Why are these offsets 1-based? The API documentation is poor.
        switch(key)
        {
          case Job.JOB_NAME:
            fieldStartingPosition = 1;
            fieldLength = 10;
            break;
          case Job.USER_NAME:
            fieldStartingPosition = 11;
            fieldLength = 10;
            break;
          case Job.JOB_NUMBER:
            fieldStartingPosition = 21;
            fieldLength = 6;
            break;
          case Job.INTERNAL_JOB_ID:
          case Job.INTERNAL_JOB_IDENTIFIER:
            fieldStartingPosition = 27;
            fieldLength = 16;
            break;
          case Job.JOB_STATUS:
            fieldStartingPosition = 43;
            fieldLength = 10;
            break;
          case Job.JOB_TYPE:
            fieldStartingPosition = 53;
            fieldLength = 1;
            break;
          case Job.JOB_SUBTYPE:
            fieldStartingPosition = 54;
            fieldLength = 1;
            break;
          default:
            fieldStartingPosition = 61; //OLJB0200 format
            for (int j=0; keys_[j] != key; ++j)
            {
              fieldStartingPosition += Job.setterKeys_.get(keys_[j]);
            }
            break;
        }
        BinaryConverter.intToByteArray(fieldStartingPosition, sortInfo, offset);
        offset += 4;
        BinaryConverter.intToByteArray(fieldLength, sortInfo, offset);
        offset += 4;
        BinaryConverter.shortToByteArray(dataType, sortInfo, offset);
        offset += 2; 
        // '1' = ascending, '2' = descending (0xF1 = 1 and 0xF2 = 2)
        sortInfo[offset] = order ? (byte)0xF1 : (byte)0xF2;
        offset += 2;
      }
        
    }
    else
    {
      sortInfo = bin4_.toBytes(0); // sort none
      //Note: To sort the jobs by order in job queue, 
      // use -1 for the number of keys to sort on. This also
      // requires 1005, 404, and 403 to be specified as keys.
    }
    parms[7] = new ProgramParameter(sortInfo); // sort information
    parms[8] = new ProgramParameter(selectionInfo); // job selection information
    parms[9] = new ProgramParameter(bin4_.toBytes(selectionInfo.length)); // format OLJS0200
    parms[10] = new ProgramParameter(bin4_.toBytes(numKeys)); // number of key fields
    parms[11] = new ProgramParameter(keyData); // array of key fields to be returned
    parms[12] = errorCode_;
    parms[13] = new ProgramParameter(conv.stringToByteArray("OLJS0200"));
    
    // Call the program
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLJOB.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    isConnected_ = true;
    
    // Key information returned
    byte[] defInfo = parms[3].getOutputData();
    numKeysReturned_ = BinaryConverter.byteArrayToInt(defInfo, 0);
    keyFieldsReturned_ = new int[numKeysReturned_];
    keyTypesReturned_ = new char[numKeysReturned_];
    keyLengthsReturned_ = new int[numKeysReturned_];
    keyOffsetsReturned_ = new int[numKeysReturned_];

    offset = 4;
    for (int i=0; i<numKeysReturned_; ++i)
    {
      keyFieldsReturned_[i] = BinaryConverter.byteArrayToInt(defInfo, offset+4);
      keyTypesReturned_[i] = conv.byteArrayToString(defInfo, offset+8, 1).charAt(0); // 'C' or 'B'
      keyLengthsReturned_[i] = BinaryConverter.byteArrayToInt(defInfo, offset+12);
      keyOffsetsReturned_[i] = BinaryConverter.byteArrayToInt(defInfo, offset+16);
      offset += 20;
    }


    // List information returned
    byte[] listInformation = parms[5].getOutputData();
    handle_ = new byte[4];
    System.arraycopy(listInformation, 8, handle_, 0, 4);

    // This second program call is to retrieve the number of jobs in the list.
    // It will wait until the server has fully populated the list before it
    // returns.
    ProgramParameter[] parms2 = new ProgramParameter[7];
    parms2[0] = new ProgramParameter(1); // receiver variable
    parms2[1] = new ProgramParameter(bin4_.toBytes(1)); // length of receiver variable
    parms2[2] = new ProgramParameter(handle_); // request handle
    parms2[3] = new ProgramParameter(80); // list information
    parms2[4] = new ProgramParameter(bin4_.toBytes(0)); // number of records to return
    parms2[5] = new ProgramParameter(bin4_.toBytes(-1)); // starting record
    parms2[6] = errorCode_;

    ProgramCall pc2 = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parms2);
    if (!pc2.run())
    {
      throw new AS400Exception(pc2.getMessageList());
    }
    byte[] listInfo2 = parms2[3].getOutputData();
    ListUtilities.checkListStatus(listInfo2[30]);  // check the list status indicator
    length_ = bin4_.toInt(listInfo2, 0);
    
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Loaded job list with length = "+length_+" and handle: ", handle_);
    }
  }


/**
Removes a PropertyChangeListener.
@param listener The listener.
@see #addPropertyChangeListener
**/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.removePropertyChangeListener(listener);
  }



/**
Removes a VetoableChangeListener.
@param listener The listener.
@see #addVetoableChangeListener
**/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener");
    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.removeVetoableChangeListener(listener);
  }


  // Resets the handle to indicate we should close the list the next time
  // we do something, usually as a result of one of the selection criteria
  // being changed since that should build a new list on the server.
  private synchronized void resetHandle()
  {
    if (handleToClose_ == null) handleToClose_ = handle_; // Close the old list on the next load
    handle_ = null;
  }
  

/**
Sets the job name that describes which jobs are returned.
The default is SELECTION_JOB_NAME_ALL.  This takes effect the next time the
list of jobs is retrieved or refreshed.
@param name The job name, or {@link #SELECTION_JOB_NAME_ALL SELECTION_JOB_NAME_ALL} for all job names.
@exception PropertyVetoException If the change is vetoed.
@see #addJobSelectionCriteria
@see #getName
**/
  public void setName(String name) throws PropertyVetoException
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
/*    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
*/
    String oldValue = selectionJobName_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("name", oldValue, name);
    
    selectionJobName_ = name;
    resetHandle();

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("name", oldValue, name);
  }


/**
Sets the job number that describes which jobs are returned.
The default is SELECTION_JOB_NUMBER_ALL.  This takes effect the next time the
list of jobs is retrieved or refreshed.
@param number The job number, or {@link #SELECTION_JOB_NUMBER_ALL SELECTION_JOB_NUMBER_ALL} for all job numbers.
@exception PropertyVetoException If the change is vetoed.
@see #addJobSelectionCriteria
@see #getNumber
**/
  public void setNumber(String number) throws PropertyVetoException
  {
    if (number == null)
    {
      throw new NullPointerException("number");
    }
/*    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
*/
    String oldValue = selectionJobNumber_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("number", oldValue, number);

    selectionJobNumber_ = number;
    resetHandle();

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("number", oldValue, number);
  }



/**
Sets the system.  This cannot be changed if the object
has established a connection to the server.
@param system The system.
@exception PropertyVetoException    If the property change is vetoed.
@see #getSystem
**/
  public void setSystem(AS400 system) throws PropertyVetoException
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    AS400 oldValue = system_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("system", oldValue, system);

    system_ = system;

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("system", oldValue, system);
  }



/**
Sets the user name value that describes which jobs are returned.
The default is SELECTION_USER_NAME_ALL.  This takes effect the next time the
list of jobs is retrieved or refreshed.
@param user The user name, or {@link #SELECTION_USER_NAME_ALL SELECTION_USER_NAME_ALL} for all user names.
@exception PropertyVetoException If the change is vetoed.
@see #addJobSelectionCriteria
@see #getUser
**/
  public void setUser(String user) throws PropertyVetoException
  {
    if (user == null)
    {
      throw new NullPointerException("user");
    }
/*    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
*/
    String oldValue = selectionUserName_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("user", oldValue, user);

    selectionUserName_ = user;
    resetHandle();
    
    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("user", oldValue, user);
  }
}

