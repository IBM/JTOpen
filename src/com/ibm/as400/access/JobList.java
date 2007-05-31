///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  JobList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
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
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 Represents a list of jobs on the system.  By default, all jobs are selected.  To filter the list, use the {@link #addJobSelectionCriteria addJobSelectionCriteria()} method.
 @see com.ibm.as400.access.Job
 **/
public class JobList implements Serializable
{
    static final long serialVersionUID = 5L;

    /**
     Constant indicating that all the jobs are returned.
     @deprecated  Use the selection constant that corresponds to the particular job selection criteria you are filtering.  For example, to select jobs for all job names, do:
     <code>
     JobList list = new JobList(system);
     list.addJobSelectionCriteria(JobList.SELECTION_JOB_NAME, JobList.SELECTION_JOB_NAME_ALL);
     </CODE>
     For backwards compatibility, this has the same effect:
     <code>
     JobList list = new JobList(system);
     list.setName(JobList.SELECTION_JOB_NAME_ALL);
     </code>
     **/
    public static final String ALL = "*ALL";

    /**
     Selection type used for job selection based on job name.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a String.  Possible values are:
     <ul>
     <li>A specific job name.
     <li>A generic name.
     <li>{@link #SELECTION_JOB_NAME_ALL SELECTION_JOB_NAME_ALL}
     <li>{@link #SELECTION_JOB_NAME_CURRENT SELECTION_JOB_NAME_CURRENT}
     <li>{@link #SELECTION_JOB_NAME_ONLY SELECTION_JOB_NAME_ONLY}
     </ul>
     The default is {@link #SELECTION_JOB_NAME_ALL SELECTION_JOB_NAME_ALL}.
     @see  #setName
     @see  com.ibm.as400.access.Job#JOB_NAME
     **/
    public static final int SELECTION_JOB_NAME = 1;

    /**
     Selection value indicating all jobs will be selected regardless of the job name.  The user name and job type fields must be specified.
     @see  #SELECTION_JOB_NAME
     **/
    public static final String SELECTION_JOB_NAME_ALL = "*ALL";

    /**
     Selection value indicating all jobs with the current job's name will be selected.
     @see  #SELECTION_JOB_NAME
     **/
    public static final String SELECTION_JOB_NAME_CURRENT = "*CURRENT";

    /**
     Selection value indicating only the job in which this program is running will be selected.  The user name and job type fields must be blank.
     @see  #SELECTION_JOB_NAME
     **/
    public static final String SELECTION_JOB_NAME_ONLY = "*";

    /**
     Selection type used for job selection based on user name.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a String.  Possible values are:
     <ul>
     <li>A specific user profile name.
     <li>A generic name.
     <li>{@link #SELECTION_USER_NAME_ALL SELECTION_USER_NAME_ALL}
     <li>{@link #SELECTION_USER_NAME_CURRENT SELECTION_USER_NAME_CURRENT}
     </ul>
     The default is {@link #SELECTION_USER_NAME_ALL SELECTION_USER_NAME_ALL}.
     @see  #setUser
     @see  com.ibm.as400.access.Job#USER_NAME
     **/
    public static final int SELECTION_USER_NAME = 2;

    /**
     Selection value indicating all jobs that use the specified job name will be selected, regardless of the user name.  The job name and job number fields must be specified.
     @see  #SELECTION_USER_NAME
     **/
    public static final String SELECTION_USER_NAME_ALL = "*ALL";

    /**
     Selection value indicating all jobs that use the current job's user profile will be selected.
     @see  #SELECTION_USER_NAME
     **/
    public static final String SELECTION_USER_NAME_CURRENT = "*CURRENT";

    /**
     Selection type used for job selection based on job number.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a String.  Possible values are:
     <ul>
     <li>A specific job number.
     <li>{@link #SELECTION_JOB_NUMBER_ALL SELECTION_JOB_NUMBER_ALL}
     </ul>
     The default is {@link #SELECTION_JOB_NUMBER_ALL SELECTION_JOB_NUMBER_ALL}.
     @see  #setNumber
     @see  com.ibm.as400.access.Job#JOB_NUMBER
     **/
    public static final int SELECTION_JOB_NUMBER = 3;

    /**
     Selection value indicating all jobs with the specified job name and user name will be selected, regardless of the job number.  The job name and user name fields must be specified.
     @see  #SELECTION_JOB_NUMBER
     **/
    public static final String SELECTION_JOB_NUMBER_ALL = "*ALL";

    /**
     Selection type used for job selection based on job type.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a String.  Possible values are:
     <ul>
     <li>{@link #SELECTION_JOB_TYPE_ALL SELECTION_JOB_TYPE_ALL}
     <li>One of the following job types:
     <ul>
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_AUTOSTART Job.JOB_TYPE_AUTOSTART}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_BATCH Job.JOB_TYPE_BATCH}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_INTERACTIVE Job.JOB_TYPE_INTERACTIVE}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_SUBSYSTEM_MONITOR Job.JOB_TYPE_SUBSYSTEM_MONITOR}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_SPOOLED_READER Job.JOB_TYPE_SPOOLED_READER}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_SYSTEM Job.JOB_TYPE_SYSTEM}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_SPOOLED_WRITER Job.JOB_TYPE_SPOOLED_WRITER}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_SCPF_SYSTEM Job.JOB_TYPE_SCPF_SYSTEM}
     </ul>
     </ul>
     The default is {@link #SELECTION_JOB_TYPE_ALL SELECTION_JOB_TYPE_ALL}.
     @see  com.ibm.as400.access.Job#JOB_TYPE
     **/
    public static final int SELECTION_JOB_TYPE = 4;

    /**
     Selection value indicating all job types will be selected.
     @see  #SELECTION_JOB_TYPE
     **/
    public static final String SELECTION_JOB_TYPE_ALL = "*";

    /**
     Selection type used for job selection based on primary job status.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a Boolean.  The default is true.
     @see  com.ibm.as400.access.Job#JOB_STATUS
     **/
    public static final int SELECTION_PRIMARY_JOB_STATUS_ACTIVE = 5;

    /**
     Selection type used for job selection based on primary job status.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a Boolean.  The default is true.
     @see  com.ibm.as400.access.Job#JOB_STATUS
     **/
    public static final int SELECTION_PRIMARY_JOB_STATUS_JOBQ = 6;

    /**
     Selection type used for job selection based on primary job status.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a Boolean.  The default is true.
     @see  com.ibm.as400.access.Job#JOB_STATUS
     **/
    public static final int SELECTION_PRIMARY_JOB_STATUS_OUTQ = 7;

    /**
     Selection type used for job selection based on active job status.  Multiple selection values are allowed for this selection type.  The selection value corresponding to this selection type is a String.  See {@link com.ibm.as400.access.Job#ACTIVE_JOB_STATUS Job.ACTIVE_JOB_STATUS} for allowed values.  By default no selection values are specified for this selection type.  This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_ACTIVE is true.
     @see  #SELECTION_PRIMARY_JOB_STATUS_ACTIVE
     @see  com.ibm.as400.access.Job#ACTIVE_JOB_STATUS
     **/
    public static final int SELECTION_ACTIVE_JOB_STATUS = 8;

    /**
     Selection type used for job selection based on a job's status on the job queue.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a Boolean.  The default is true.  This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
     @see  #SELECTION_PRIMARY_JOB_STATUS_JOBQ
     @see  com.ibm.as400.access.Job#JOB_QUEUE_STATUS
     **/
    public static final int SELECTION_JOB_QUEUE_STATUS_SCHEDULE = 9;

    /**
     Selection type used for job selection based on a job's status on the job queue.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a Boolean.  The default is true.  This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
     @see  #SELECTION_PRIMARY_JOB_STATUS_JOBQ
     @see  com.ibm.as400.access.Job#JOB_QUEUE_STATUS
     **/
    public static final int SELECTION_JOB_QUEUE_STATUS_HELD = 10;

    /**
     Selection type used for job selection based on a job's status on the job queue.  Only one selection value is allowed for this selection type.  The selection value corresponding to this selection type is a Boolean.  The default is true.  This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
     @see  #SELECTION_PRIMARY_JOB_STATUS_JOBQ
     @see  com.ibm.as400.access.Job#JOB_QUEUE_STATUS
     **/
    public static final int SELECTION_JOB_QUEUE_STATUS_READY = 11;

    /**
     Selection type used for job selection based on job queue.  Multiple selection values are allowed for this selection type.  The selection value corresponding to this selection type is a String representing the fully-qualified integrated file system name for a server job queue.  By default no selection values are specified for this selection type.  This value is only used when the value for SELECTION_PRIMARY_JOB_STATUS_JOBQ is true.
     @see  #SELECTION_PRIMARY_JOB_STATUS_JOBQ
     @see  com.ibm.as400.access.QSYSObjectPathName
     @see  com.ibm.as400.access.Job#JOB_QUEUE
     **/
    public static final int SELECTION_JOB_QUEUE = 12;

    /**
     Selection type used for job selection based on the user name for a job's initial thread.  Multiple selection values are allowed for this selection type.  The selection value corresponding to this selection type is a String.  By default no selection values are specified for this selection type.
     **/
    public static final int SELECTION_INITIAL_USER = 13;

    /**
     Selection type used for job selection based on the server type.  Multiple selection values are allowed for this selection type.  The selection value corresponding to this selection type is a String.  By default no selection values are specified for this selection type.  Possible values are:
     <ul>
     <li>A server type. See {@link com.ibm.as400.access.Job#SERVER_TYPE Job.SERVER_TYPE}.
     <li>A generic value.
     <li>{@link #SELECTION_SERVER_TYPE_ALL SELECTION_SERVER_TYPE_ALL}
     <li>{@link #SELECTION_SERVER_TYPE_BLANK SELECTION_SERVER_TYPE_BLANK}
     </ul>
     @see  com.ibm.as400.access.Job#SERVER_TYPE
     **/
    public static final int SELECTION_SERVER_TYPE = 14;

    /**
     Selection value indicating all jobs with a server type will be selected.
     @see  #SELECTION_SERVER_TYPE
     **/
    public static final String SELECTION_SERVER_TYPE_ALL = "*ALL";

    /**
     Selection value indicating all jobs without a server type will be selected.
     @see  #SELECTION_SERVER_TYPE
     **/
    public static final String SELECTION_SERVER_TYPE_BLANK = "*BLANK";

    //public static final int ACTIVE_SUBSYSTEM = 15;
    //public static final int MEMORY_POOL = 16;

    /**
     Selection type used for job selection based on the enhanced job type.  Multiple selection values are allowed for this selection type.  The selection value corresponding to this selection type is an Integer.  By default no selection values are specified for this selection type.  Possible values are:
     <ul>
     <li>{@link #SELECTION_JOB_TYPE_ENHANCED_ALL_BATCH SELECTION_JOB_TYPE_ENHANCED_ALL_BATCH}
     <li>{@link #SELECTION_JOB_TYPE_ENHANCED_ALL_INTERACTIVE SELECTION_JOB_TYPE_ENHANCED_ALL_INTERACTIVE}
     <li>{@link #SELECTION_JOB_TYPE_ENHANCED_ALL_PRESTART SELECTION_JOB_TYPE_ENHANCED_ALL_PRESTART}
     <li>Any of the enhanced job types:
     <ul>
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_AUTOSTART Job.JOB_TYPE_ENHANCED_AUTOSTART}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH Job.JOB_TYPE_ENHANCED_BATCH}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_IMMEDIATE Job.JOB_TYPE_ENHANCED_BATCH_IMMEDIATE}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_MRT Job.JOB_TYPE_ENHANCED_BATCH_MRT}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER Job.JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST Job.JOB_TYPE_ENHANCED_COMM_PROCEDURE_START_REQUEST}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE Job.JOB_TYPE_ENHANCED_INTERACTIVE}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_GROUP Job.JOB_TYPE_ENHANCED_INTERACTIVE_GROUP}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ Job.JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP Job.JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART Job.JOB_TYPE_ENHANCED_PRESTART}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_BATCH Job.JOB_TYPE_ENHANCED_PRESTART_BATCH}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_COMM Job.JOB_TYPE_ENHANCED_PRESTART_COMM}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_READER Job.JOB_TYPE_ENHANCED_READER}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_SUBSYSTEM Job.JOB_TYPE_ENHANCED_SUBSYSTEM}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_SYSTEM Job.JOB_TYPE_ENHANCED_SYSTEM}
     <li>{@link com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_WRITER Job.JOB_TYPE_ENHANCED_WRITER}
     </ul>
     </ul>
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED
     **/
    public static final int SELECTION_JOB_TYPE_ENHANCED = 17;

    /**
     Selection value indicating all the batch job types will be selected.
     @see  #SELECTION_JOB_TYPE_ENHANCED
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_IMMEDIATE
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_MRT
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_BATCH_ALTERNATE_SPOOL_USER
     **/
    public static final Integer SELECTION_JOB_TYPE_ENHANCED_ALL_BATCH = new Integer(200);

    /**
     Selection value indicating all the interactive job types will be selected.
     @see  #SELECTION_JOB_TYPE_ENHANCED
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_GROUP
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_INTERACTIVE_SYSREQ_AND_GROUP
     **/
    public static final Integer SELECTION_JOB_TYPE_ENHANCED_ALL_INTERACTIVE = new Integer(900);

    /**
     Selection value indicating all the prestart job types will be selected.
     @see  #SELECTION_JOB_TYPE_ENHANCED
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_BATCH
     @see  com.ibm.as400.access.Job#JOB_TYPE_ENHANCED_PRESTART_COMM
     **/
    public static final Integer SELECTION_JOB_TYPE_ENHANCED_ALL_PRESTART = new Integer(1600);

    // Shared error code parameter.
    private static final ProgramParameter ERROR_CODE = new ProgramParameter(new byte[8]);

    // Holds the lengths for all of the valid sort keys.
    static final IntegerHashtable sortableKeys_ = new IntegerHashtable();
    static
    {
        sortableKeys_.put(Job.ACTIVE_JOB_STATUS, 4);
        sortableKeys_.put(Job.ALLOW_MULTIPLE_THREADS, 1);
        sortableKeys_.put(Job.ACTIVE_JOB_STATUS_FOR_JOBS_ENDING, 4);
        sortableKeys_.put(Job.BREAK_MESSAGE_HANDLING, 10);
        sortableKeys_.put(Job.CCSID, 4); // Binary
        sortableKeys_.put(Job.COUNTRY_ID, 2);
        sortableKeys_.put(Job.CPU_TIME_USED, 4); // Binary
        sortableKeys_.put(Job.CURRENT_USER, 10);
        sortableKeys_.put(Job.COMPLETION_STATUS, 1);
        sortableKeys_.put(Job.CURRENT_SYSTEM_POOL_ID, 4); // Binary
        sortableKeys_.put(Job.CHARACTER_ID_CONTROL, 10);
        sortableKeys_.put(Job.CPU_TIME_USED_LARGE, 8); // Binary
        sortableKeys_.put(Job.CPU_TIME_USED_FOR_DATABASE, 8); // Binary
        sortableKeys_.put(Job.DATE_STARTED, 13);
        sortableKeys_.put(Job.DATE_ENTERED_SYSTEM, 13);
        sortableKeys_.put(Job.SCHEDULE_DATE_GETTER, 8);
        sortableKeys_.put(Job.JOB_QUEUE_DATE, 8);
        sortableKeys_.put(Job.DATE_FORMAT, 4);
        sortableKeys_.put(Job.DATE_SEPARATOR, 1);
        sortableKeys_.put(Job.DBCS_CAPABLE, 1);
        sortableKeys_.put(Job.KEEP_DDM_CONNECTIONS_ACTIVE, 10);
        sortableKeys_.put(Job.DEFAULT_WAIT_TIME, 4); // Binary
        sortableKeys_.put(Job.DEVICE_RECOVERY_ACTION, 13);
        sortableKeys_.put(Job.DEFAULT_CCSID, 4); // Binary
        sortableKeys_.put(Job.DECIMAL_FORMAT, 1);
        sortableKeys_.put(Job.DATE_ENDED, 13);
        sortableKeys_.put(Job.END_SEVERITY, 4); // Binary
        sortableKeys_.put(Job.CONTROLLED_END_REQUESTED, 1);
        sortableKeys_.put(Job.FUNCTION_NAME, 10);
        sortableKeys_.put(Job.FUNCTION_TYPE, 1);
        sortableKeys_.put(Job.SIGNED_ON_JOB, 1);
        sortableKeys_.put(Job.INQUIRY_MESSAGE_REPLY, 10);
        sortableKeys_.put(Job.ACCOUNTING_CODE, 15);
        sortableKeys_.put(Job.JOB_DATE, 7);
        sortableKeys_.put(Job.JOB_DESCRIPTION, 20);
        sortableKeys_.put(Job.JOB_QUEUE, 20);
        sortableKeys_.put(Job.JOB_QUEUE_PRIORITY, 2);
        sortableKeys_.put(Job.JOB_SWITCHES, 8);
        sortableKeys_.put(Job.MESSAGE_QUEUE_ACTION, 10);
        sortableKeys_.put(Job.MESSAGE_QUEUE_MAX_SIZE, 4); // Binary
        sortableKeys_.put(Job.JOB_USER_IDENTITY, 10);
        sortableKeys_.put(Job.JOB_USER_IDENTITY_SETTING, 1);
        sortableKeys_.put(Job.JOB_END_REASON, 4); // Binary
        sortableKeys_.put(Job.JOB_LOG_PENDING, 1);
        sortableKeys_.put(Job.JOB_TYPE_ENHANCED, 4); // Binary
        sortableKeys_.put(Job.JOB_LOG_OUTPUT, 10);
        sortableKeys_.put(Job.LANGUAGE_ID, 3);
        sortableKeys_.put(Job.LOGGING_LEVEL, 1);
        sortableKeys_.put(Job.LOG_CL_PROGRAMS, 10);
        sortableKeys_.put(Job.LOGGING_SEVERITY, 4); // Binary
        sortableKeys_.put(Job.LOGGING_TEXT, 10);
        sortableKeys_.put(Job.MODE, 8);
        sortableKeys_.put(Job.MAX_CPU_TIME, 4); // Binary
        sortableKeys_.put(Job.MAX_TEMP_STORAGE, 4); // Binary
        sortableKeys_.put(Job.MAX_THREADS, 4); // Binary
        sortableKeys_.put(Job.MAX_TEMP_STORAGE_LARGE, 4); // Binary
        sortableKeys_.put(Job.MEMORY_POOL, 10);
        sortableKeys_.put(Job.MESSAGE_REPLY, 1);
        sortableKeys_.put(Job.AUXILIARY_IO_REQUESTS, 4); // Binary
        sortableKeys_.put(Job.INTERACTIVE_TRANSACTIONS, 4); // Binary
        sortableKeys_.put(Job.AUXILIARY_IO_REQUESTS_LARGE, 8); // Binary
        sortableKeys_.put(Job.OUTPUT_QUEUE, 20);
        sortableKeys_.put(Job.OUTPUT_QUEUE_PRIORITY, 2);
        sortableKeys_.put(Job.PRINT_KEY_FORMAT, 10);
        sortableKeys_.put(Job.PRINT_TEXT, 30);
        sortableKeys_.put(Job.PRINTER_DEVICE_NAME, 10);
        sortableKeys_.put(Job.ELIGIBLE_FOR_PURGE, 10);
        sortableKeys_.put(Job.PRODUCT_RETURN_CODE, 4); // Binary
        sortableKeys_.put(Job.PROGRAM_RETURN_CODE, 4); // Binary
        sortableKeys_.put(Job.TOTAL_RESPONSE_TIME, 4); // Binary
        sortableKeys_.put(Job.RUN_PRIORITY, 4); // Binary
        sortableKeys_.put(Job.ROUTING_DATA, 80);
        sortableKeys_.put(Job.SORT_SEQUENCE_TABLE, 20);
        sortableKeys_.put(Job.STATUS_MESSAGE_HANDLING, 10);
        sortableKeys_.put(Job.JOB_QUEUE_STATUS, 10);
        sortableKeys_.put(Job.SUBMITTED_BY_JOB_NAME, 26);
        sortableKeys_.put(Job.SUBSYSTEM, 20);
        sortableKeys_.put(Job.SYSTEM_POOL_ID, 4); // Binary
        sortableKeys_.put(Job.SPECIAL_ENVIRONMENT, 10);
        sortableKeys_.put(Job.SERVER_TYPE, 30);
        sortableKeys_.put(Job.SPOOLED_FILE_ACTION, 10);
        sortableKeys_.put(Job.TIME_SEPARATOR, 1);
        sortableKeys_.put(Job.TIME_SLICE, 4); // Binary
        sortableKeys_.put(Job.TIME_SLICE_END_POOL, 10);
        sortableKeys_.put(Job.TEMP_STORAGE_USED, 4); // Binary
        sortableKeys_.put(Job.THREAD_COUNT, 4); // Binary
        sortableKeys_.put(Job.TEMP_STORAGE_USED_LARGE, 4); // Binary
        sortableKeys_.put(Job.UNIT_OF_WORK_ID, 24);
        sortableKeys_.put(Job.USER_RETURN_CODE, 4); // Binary
    }

    // The system where the jobs are located.
    private AS400 system_;
    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    // Selection variables.
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

    // Length of the job list.
    private int length_;
    // Length of each record in job list.
    private int recordLength_;
    // Handle that references the user space used by the open list APIs.
    private byte[] handle_;
    // If the list info has changed, close the old handle before loading the new one.
    private boolean closeHandle_ = false;

    // Info saved between calls to load() and getJobs().
    private int numKeysReturned_;
    private int[] keyFieldsReturned_;
    private char[] keyTypesReturned_;
    private int[] keyLengthsReturned_;
    private int[] keyOffsetsReturned_;

    // Keys to pre-load.
    private int currentKey_ = 0;
    private int[] keys_ = new int[1];

    // Sort keys.
    private int currentSortKey_ = 0;
    private int[] sortKeys_ = new int[1];
    private boolean[] sortOrders_ = new boolean[1];

    // Used to determine if there are open Enumerations still using us.
    private Vector trackers_;

    /**
     Constructs a JobList object.  The system must be set before retrieving the list of jobs.
     @see  #setSystem
     **/
    public JobList()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing JobList object.");
    }

    /**
     Constructs a JobList object.
     @param  system  The system object representing the system on which the jobs exist.
     **/
    public JobList(AS400 system)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing JobList object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Adds a job attribute that will be retrieved for each job in this job list.  This method allows the Job objects that are retrieved from this JobList to have some of their attributes already filled in, so that a call to {@link com.ibm.as400.access.Job#getValue Job.getValue()} does not result in another API call back to the system for each job in the list.
     <p>The list of job attributes is maintained internally even when this JobList is closed and re-used.  To start over with a new set of job attributes to retrieve, call {@link #clearJobAttributesToRetrieve clearJobAttributesToRetrieve()}.
     @param  attribute  The job attribute to retrieve.  Possible values are all job attributes contained in the {@link com.ibm.as400.access.Job Job} class, <b>excluding</b> the following:
     <ul>
     <li>Job.CLIENT_IP_ADDRESS
     <li>Job.CURRENT_LIBRARY
     <li>Job.CURRENT_LIBRARY_EXISTENCE
     <li>Job.ELAPSED_CPU_PERCENT_USED
     <li>Job.ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
     <li>Job.ELAPSED_CPU_TIME_USED
     <li>Job.ELAPSED_CPU_TIME_USED_FOR_DATABASE
     <li>Job.ELAPSED_DISK_IO
     <li>Job.ELAPSED_DISK_IO_ASYNCH
     <li>Job.ELAPSED_DISK_IO_SYNCH
     <li>Job.ELAPSED_INTERACTIVE_RESPONSE_TIME
     <li>Job.ELAPSED_INTERACTIVE_TRANSACTIONS
     <li>Job.ELAPSED_LOCK_WAIT_TIME
     <li>Job.ELAPSED_PAGE_FAULTS
     <li>Job.ELAPSED_TIME
     <li>Job.PRODUCT_LIBRARIES
     <li>Job.SUBMITTED_BY_JOB_NUMBER
     <li>Job.SUBMITTED_BY_USER
     <li>Job.SYSTEM_LIBRARY_LIST
     <li>Job.USER_LIBRARY_LIST
     </ul>
     To retrieve any of the ELAPSED statistics, use the {@link com.ibm.as400.access.Job#resetStatistics Job.resetStatistics()} and {@link com.ibm.as400.access.Job#loadStatistics Job.loadStatistics()} methods.
     @see  #clearJobAttributesToRetrieve
     @see  com.ibm.as400.access.Job
     **/
    public void addJobAttributeToRetrieve(int attribute)
    {
        if (attribute < 101)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'attribute' is not valid: " + attribute);
            throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        switch (attribute)
        {
            // These are always loaded, so it is unnecessary to try and preload them.
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
                Trace.log(Trace.ERROR, "Value of parameter 'attribute' is not valid: " + attribute);
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
            // Resize.
            int[] temp = keys_;
            keys_ = new int[temp.length * 2];
            System.arraycopy(temp, 0, keys_, 0, temp.length);
        }

        keys_[currentKey_++] = attribute;
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Adds a job attribute used to sort the list.
     <p>The list of job attributes to sort on is maintained internally even when this JobList is closed and re-used.  To start over with a new set of job attributes to sort on, call {@link #clearJobAttributesToSortOn clearJobAttributesToSortOn()}.
     @param  attribute  The job attribute on which to sort.  Possible values are all job attributes contained in the {@link com.ibm.as400.access.Job Job} class, <b>excluding</b> the following:
     <ul>
     <li>Job.CLIENT_IP_ADDRESS
     <li>Job.CURRENT_LIBRARY
     <li>Job.CURRENT_LIBRARY_EXISTENCE
     <li>Job.ELAPSED_CPU_PERCENT_USED
     <li>Job.ELAPSED_CPU_PERCENT_USED_FOR_DATABASE
     <li>Job.ELAPSED_CPU_TIME_USED
     <li>Job.ELAPSED_CPU_TIME_USED_FOR_DATABASE
     <li>Job.ELAPSED_DISK_IO
     <li>Job.ELAPSED_DISK_IO_ASYNCH
     <li>Job.ELAPSED_DISK_IO_SYNCH
     <li>Job.ELAPSED_INTERACTIVE_RESPONSE_TIME
     <li>Job.ELAPSED_INTERACTIVE_TRANSACTIONS
     <li>Job.ELAPSED_LOCK_WAIT_TIME
     <li>Job.ELAPSED_PAGE_FAULTS
     <li>Job.ELAPSED_TIME
     <li>Job.PRODUCT_LIBRARIES
     <li>Job.SUBMITTED_BY_JOB_NUMBER
     <li>Job.SUBMITTED_BY_USER
     <li>Job.SYSTEM_LIBRARY_LIST
     <li>Job.USER_LIBRARY_LIST
     </ul>
     @param  sortOrder  true to sort ascending; false to sort descending.
     @see  #clearJobAttributesToSortOn
     @see  com.ibm.as400.access.Job
     **/
    public void addJobAttributeToSortOn(int attribute, boolean sortOrder)
    {
        if (attribute < 101)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'attribute' is not valid: " + attribute);
            throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        switch (attribute)
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
                Trace.log(Trace.ERROR, "Value of parameter 'attribute' is not valid: " + attribute);
                throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            // The date and time have different key values for getting and setting.
            // case Job.SCHEDULE_DATE:
            // case Job.SCHEDULE_TIME:
            //  attribute = Job.SCHEDULE_DATE_GETTER;
            //  break;
            default:
                break;
        }

        if (currentSortKey_ >= sortKeys_.length)
        {
            int[] temp = sortKeys_;
            sortKeys_ = new int[temp.length * 2];
            System.arraycopy(temp, 0, sortKeys_, 0, temp.length);
            boolean[] tempSort = sortOrders_;
            sortOrders_ = new boolean[tempSort.length * 2];
            System.arraycopy(tempSort, 0, sortOrders_, 0, tempSort.length);
        }
        sortKeys_[currentSortKey_] = attribute;
        sortOrders_[currentSortKey_++] = sortOrder;
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Adds a selection type and value to be used to filter the list of jobs.  If a selection type supports only one value, then the selection value used will be the one that was passed on the most recent call to this method for that selection type.
     <p>By default, all jobs are selected, because most of the filter criteria settings default to true.  To filter the list further, the job selection criteria should be set to false.  For example, to retrieve a list of only active jobs, you would do:
     <pre>
     JobList list = new JobList(system);
     list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_ACTIVE, Boolean.TRUE);
     list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_JOBQ, Boolean.FALSE);
     list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ, Boolean.FALSE);
     Enumeration jobs = list.getJobs();
     </pre>
     <p>The list of job selection criteria is maintained internally even when this JobList is closed and re-used.  To start over with a new set of job selection criteria, call {@link #clearJobSelectionCriteria clearJobSelectionCriteria()}.
     @param  selectionType  The constant indicating which selection type used to filter the list.  Possible values are:
     <ul>
     <li>{@link #SELECTION_JOB_NAME SELECTION_JOB_NAME}
     <li>{@link #SELECTION_USER_NAME SELECTION_USER_NAME}
     <li>{@link #SELECTION_JOB_NUMBER SELECTION_JOB_NUMBER}
     <li>{@link #SELECTION_JOB_TYPE SELECTION_JOB_TYPE}
     <li>{@link #SELECTION_PRIMARY_JOB_STATUS_ACTIVE SELECTION_PRIMARY_JOB_STATUS_ACTIVE}
     <li>{@link #SELECTION_PRIMARY_JOB_STATUS_JOBQ SELECTION_PRIMARY_JOB_STATUS_JOBQ}
     <li>{@link #SELECTION_PRIMARY_JOB_STATUS_OUTQ SELECTION_PRIMARY_JOB_STATUS_OUTQ}
     <li>{@link #SELECTION_ACTIVE_JOB_STATUS SELECTION_ACTIVE_JOB_STATUS}
     <li>{@link #SELECTION_JOB_QUEUE_STATUS_SCHEDULE SELECTION_JOB_QUEUE_STATUS_SCHEDULE}
     <li>{@link #SELECTION_JOB_QUEUE_STATUS_READY SELECTION_JOB_QUEUE_STATUS_READY}
     <li>{@link #SELECTION_JOB_QUEUE_STATUS_HELD SELECTION_JOB_QUEUE_STATUS_HELD}
     <li>{@link #SELECTION_JOB_QUEUE SELECTION_JOB_QUEUE}
     <li>{@link #SELECTION_INITIAL_USER SELECTION_INITIAL_USER}
     <li>{@link #SELECTION_SERVER_TYPE SELECTION_SERVER_TYPE}
     <li>{@link #SELECTION_JOB_TYPE_ENHANCED SELECTION_JOB_TYPE_ENHANCED}
     </ul>
     @param  selectionValue  The value for the selection type. See the individual selection type constants for the appropriate object or constant to use. Some selection types allow multiple selection values to be added.
     @see  #clearJobSelectionCriteria
     @see  com.ibm.as400.access.Job
     **/
    public void addJobSelectionCriteria(int selectionType, Object selectionValue) throws PropertyVetoException
    {
        if (selectionValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'selectionValue' is null.");
            throw new NullPointerException("selectionValue");
        }

        switch (selectionType)
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
                if (currentActiveStatus_ >= activeStatuses_.length)         //JTOpen Bug 1728765
                {
                    String[] temp = activeStatuses_;
                    activeStatuses_ = new String[temp.length * 2];
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
                for (int i = name.length(); i < 10; ++i)
                {
                    buf.append(' ');
                }
                String lib = path.getLibraryName();
                buf.append(lib);

                if (currentJobQueue_ >= jobQueues_.length)      //JTOpen Bug 1728765
                {
                    String[] temp = jobQueues_;
                    jobQueues_ = new String[temp.length * 2];
                    System.arraycopy(temp, 0, jobQueues_, 0, temp.length);
                }
                jobQueues_[currentJobQueue_++] = buf.toString();
                break;
            case SELECTION_INITIAL_USER:
                String profile = ((String)selectionValue).toUpperCase();
                if (currentInitialUser_ >= initialUsers_.length)        //JTOpen Bug 1728765
                {
                    String[] temp = initialUsers_;
                    initialUsers_ = new String[temp.length * 2];
                    System.arraycopy(temp, 0, initialUsers_, 0, temp.length);
                }
                initialUsers_[currentInitialUser_++] = profile;
                break;
            case SELECTION_SERVER_TYPE:
                String type = ((String)selectionValue).toUpperCase();
                if (currentServerType_ >= serverTypes_.length)      //JTOpen Bug 1728765
                {
                    String[] temp = serverTypes_;
                    serverTypes_ = new String[temp.length * 2];
                    System.arraycopy(temp, 0, serverTypes_, 0, temp.length);
                }
                serverTypes_[currentServerType_++] = type;
                break;
            case SELECTION_JOB_TYPE_ENHANCED:
                int val = ((Integer)selectionValue).intValue();
                if (currentEnhancedJobType_ >= enhancedJobTypes_.length)        //JTOpen Bug 1728765
                {
                    int[] temp = enhancedJobTypes_;
                    enhancedJobTypes_ = new int[temp.length * 2];
                    System.arraycopy(temp, 0, enhancedJobTypes_, 0, temp.length);
                }
                enhancedJobTypes_[currentEnhancedJobType_++] = val;
                break;
            default:
                Trace.log(Trace.ERROR, "Value of parameter 'selectionType' is not valid: " + selectionType);
                throw new ExtendedIllegalArgumentException("selectionType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Clears the job attributes to be retrieved.  This removes all of the job attributes that would be retrieved.  Some attributes are always retrieved, regardless if they are in this list or not, such as job name, job number, and user name.
     @see  #addJobAttributeToRetrieve
     **/
    public void clearJobAttributesToRetrieve()
    {
        currentKey_ = 0;
        keys_ = new int[1];
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Clears the job attributes used to sort the list.  This resets all of the job sort parameters to their default values.
     @see  #addJobAttributeToSortOn
     **/
    public void clearJobAttributesToSortOn()
    {
        currentSortKey_ = 0;
        sortKeys_ = new int[1];
        sortOrders_ = new boolean[1];
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Clears the selection types and values used to filter the list of jobs.  This resets all of the job selection parameters to their default values.
     @see  #addJobSelectionCriteria
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
        synchronized (this)
        {
            if (handle_ != null) closeHandle_ = true;
        }
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange()</b> method will be called each time the value of any bound property is changed.
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
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange()</b> method will be called each time the value of any constrained property is changed.
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

    /**
     Closes the job list on the system.  This releases any system resources previously in use by this job list.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #load
     **/
    public synchronized void close() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Closing job list, handle: ", handle_);
        if (handle_ == null) return;
        if (Trace.traceOn_)
        {
            if (trackers_ != null)
            {
                int inUse = 0;
                for (int i = 0; i < trackers_.size(); ++i)
                {
                    Tracker tracker = (Tracker)trackers_.elementAt(i);
                    if (tracker.isSet()) ++inUse;
                    // Force the Enumeration to shut down since the JobList is being closed.
                    tracker.set(false);
                }
                if (inUse > 0)
                {
                    Trace.log(Trace.WARNING, "The job list on the server is possibly in use by " + inUse + " or more enumerations as a result of a call to JobList.getJobs().");
                }
            }
        }
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            new ProgramParameter(handle_),
            ERROR_CODE
        };
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYCLST.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }
        handle_ = null;
        closeHandle_ = false;
    }

    /**
     Closes the job list on the system when this object is garbage collected.
     **/
    protected void finalize() throws Throwable
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Finalize method for job list invoked.");
        if (system_.isConnected(AS400.COMMAND)) close();
        super.finalize();
    }

    /**
     Returns an Enumeration that wraps the list of jobs on the system.  This method calls {@link #load load()} implicitly if needed.  The Enumeration retrieves jobs from the system in blocks as needed when nextElement() is called.  This JobList should not be closed until the program is done processing elements out of the Enumeration.  That is, this method does not retrieve all of the jobs from the system up front -- it retrieves them as needed, which allows for a lower memory footprint versus more calls to the system.  The block size used internally by the Enumeration is set to 1000 jobs.
     @return  An Enumeration of {@link com.ibm.as400.access.Job Job} objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.Job
     **/
    public synchronized Enumeration getJobs() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving job list.");
        // Need to get the length.
        if (handle_ == null || closeHandle_) load();

        // Use a tracker so we know if someone tries to close us, whether or not they have open Enumerations.  It's possible they do, and they are just done with them, but this is mostly for debugging purposes.
        Tracker tracker = new Tracker();

        if (trackers_ == null) trackers_ = new Vector();
        trackers_.addElement(tracker);

        // Remove dead trackers to prevent a memory leak.  JobEnumerations whose hasMoreElements() return false, or those who have been garbage collected, will all have their freed their trackers.
        for (int i = trackers_.size() - 1; i >= 0; --i)
        {
            Tracker t = (Tracker)trackers_.elementAt(i);
            if (!t.isSet()) trackers_.removeElementAt(i);
        }

        return new JobEnumeration(this, length_, tracker);
    }

    /**
     Returns a subset of the list of jobs in the job list.  This method allows the user to retrieve the job list from the system in pieces.  If a call to {@link #load load()} is made (either implicitly or explicitly), then the jobs at a given offset will change, so a subsequent call to getJobs() with the same <i>listOffset</i> and <i>number</i> will most likely not return the same Jobs as the previous call.
     @param  listOffset  The offset into the list of jobs.  This value must be greater than 0 and less than the list length, or specify -1 to retrieve all of the jobs.
     @param  number  The number of jobs to retrieve out of the list, starting at the specified <i>listOffset</i>.  This value must be greater than or equal to 0 and less than or equal to the list length.  If the <i>listOffset</i> is -1, this parameter is ignored.
     @return  The array of retrieved {@link com.ibm.as400.access.Job Job} objects.  The length of this array may not necessarily be equal to <i>number</i>, depending upon the size of the list on the system, and the specified <i>listOffset</i>.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  com.ibm.as400.access.Job
     **/
    public Job[] getJobs(int listOffset, int number) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving job list, list offset: " + listOffset + ", number:", number);
        if (listOffset < -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'listOffset' is not valid:", listOffset);
            throw new ExtendedIllegalArgumentException("listOffset (" + listOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (number < 0 && listOffset != -1)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'number' is not valid:", number);
            throw new ExtendedIllegalArgumentException("number (" + number + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        if (handle_ == null || closeHandle_) load();

        if (number == 0 && listOffset != -1) return new Job[0];

        if (listOffset == -1)
        {
            number = length_;
            listOffset = 0;
        }
        else if (listOffset + number > length_)
        {
            number = length_ - listOffset;
        }

        int lengthOfReceiverVariable = recordLength_ * number;

        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(lengthOfReceiverVariable),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariable)),
            // Request handle, input, char(4).
            new ProgramParameter(handle_),
            // List information, output, char(80).
            new ProgramParameter(80),
            // Number of records to return, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(number)),
            // Starting record, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(listOffset + 1)),
            // Error code, I/0, char(*).
            ERROR_CODE
        };

        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYGTLE.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        byte[] data = parameters[0].getOutputData();
        Converter conv = new Converter(system_.getCcsid(), system_);

        Job[] jobs = new Job[number];
        for (int i = 0, offset = 0; i < jobs.length; ++i, offset += recordLength_)
        {
            String jobName = conv.byteArrayToString(data, offset, 10);
            String userName = conv.byteArrayToString(data, offset + 10, 10);
            String jobNumber = conv.byteArrayToString(data, offset + 20, 6);
            String status = conv.byteArrayToString(data, offset + 42, 10);
            String jobType = conv.byteArrayToString(data, offset + 52, 1);
            String jobSubtype = conv.byteArrayToString(data, offset + 53, 1);

            jobs[i] = new Job(system_, jobName.trim(), userName.trim(), jobNumber.trim(), status, jobType, jobSubtype);

            for (int j = 0; j < numKeysReturned_; ++j)
            {
                int keyOffset = keyOffsetsReturned_[j];

                if (keyTypesReturned_[j] == 'C')
                {
                    String value = conv.byteArrayToString(data, offset + keyOffset, keyLengthsReturned_[j]);
                    jobs[i].setValueInternal(keyFieldsReturned_[j], value);
                }
                else
                {
                    if (keyLengthsReturned_[j] > 4)
                    {
                        jobs[i].setAsLong(keyFieldsReturned_[j], BinaryConverter.byteArrayToLong(data, offset + keyOffset));
                    }
                    else
                    {
                        jobs[i].setAsInt(keyFieldsReturned_[j], BinaryConverter.byteArrayToInt(data, offset + keyOffset));
                    }
                }
            }
        }

        return jobs;
    }

    /**
     Returns the number of jobs in the list.  This method implicitly calls {@link #load load()} if it has not already been called.
     @return  The number of jobs, or 0 if no list was retrieved.
     @see  #load
     **/
    public int getLength()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job list length.");
        try
        {
            if (handle_ == null || closeHandle_) load();
        }
        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Exception caught getting length of job list:", e);
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Length:", length_);
        return length_;
    }

    /**
     Returns the job name that describes which jobs are returned.
     @return  The job name.
     @see  #setName
     **/
    public String getName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting name: " + selectionJobName_);
        return selectionJobName_;
    }

    /**
     Returns the job number that describes which jobs are returned.
     @return  The job number.
     @see  #setNumber
     **/
    public String getNumber()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting number: " + selectionJobNumber_);
        return selectionJobNumber_;
    }

    /**
     Returns the system object representing the system on which the jobs exist.
     @return  The system object representing the system on which the jobs exist.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Returns the user name that describes which jobs are returned.
     @return  The user name.
     @see  #setUser
     **/
    public String getUser()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user: " + selectionUserName_);
        return selectionUserName_;
    }

    /**
     Loads the list of jobs on the system.  This method informs the system to build a list of jobs given the previously added job attributes to select, retrieve, and sort.  This method blocks until the system returns the total number of jobs it has compiled.  A subsequent call to {@link #getJobs getJobs()} will retrieve the actual job information and attributes for each job in the list from the system.
     <p>This method updates the list length.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     @see  #getLength
     **/
    public synchronized void load() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loading job list.");
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect to server before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Close the previous list.
        if (closeHandle_) close();

        byte[] sortInformation = new byte[4 + currentSortKey_ * 12];
        BinaryConverter.intToByteArray(currentSortKey_, sortInformation, 0);
        for (int i = 0, offset = 4; i < currentSortKey_; ++i)
        {
            int fieldLength = sortableKeys_.get(sortKeys_[i]);
            short dataType = (short)4;  // Data type 4 = character data, NLS-sort supported, DBCS treated as single-byte.
            // We'll use 0 (signed binary) for all of the int types:
            switch (sortKeys_[i])
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
                    dataType = (short)0;  // Signed binary.
                    fieldLength = 4;
                    break;
                case Job.TEMP_STORAGE_USED_LARGE:
                case Job.MAX_TEMP_STORAGE_LARGE:
                    dataType = (short)9;  // Unsigned binary.
                    fieldLength = 4;
                    break;
                case Job.CPU_TIME_USED_LARGE:
                case Job.CPU_TIME_USED_FOR_DATABASE:
                case Job.AUXILIARY_IO_REQUESTS_LARGE:
                    dataType = (short)9;  // Unsigned binary.
                    fieldLength = 8;
                    break;
                default:
                    //dataType = (short)4;
                    break;
            }

            int fieldStartingPosition = 0;
            //Sort key field starting position is one based.
            switch (sortKeys_[i])
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
                    fieldStartingPosition = 61;  //OLJB0200 format.
                    for (int j = 0; keys_[j] != sortKeys_[i]; ++j)
                    {
                        fieldStartingPosition += sortableKeys_.get(keys_[j]);
                    }
                    break;
            }
            BinaryConverter.intToByteArray(fieldStartingPosition, sortInformation, 4 + i * 12);
            BinaryConverter.intToByteArray(fieldLength, sortInformation, 8 + i * 12);
            BinaryConverter.shortToByteArray(dataType, sortInformation, 12 + i * 12);
            // 0xF1 = ascending, 0xF2 = descending.
            sortInformation[14 + i * 12] = sortOrders_[i] ? (byte)0xF1 : (byte)0xF2;
        }

        // Figure out our selection criteria.
        int numberOfPrimaryJobStatusEntries = (selectActiveJobs_ ? 1 : 0) + (selectJobQueueJobs_ ? 1 : 0) + (selectOutQueueJobs_ ? 1 : 0);
        int numberOfJobsOnJobQueueStatusEntries = selectJobQueueJobs_ ? (selectHeldJobs_ ? 1 : 0) + (selectScheduledJobs_ ? 1 : 0) + (selectReadyJobs_ ? 1 : 0) : 0;

        byte[] jobSelectionInformation = new byte[108 + numberOfPrimaryJobStatusEntries * 10 + currentActiveStatus_ * 4 + numberOfJobsOnJobQueueStatusEntries * 10 + currentJobQueue_ * 20 + currentInitialUser_ * 10 + currentServerType_ * 30 + currentEnhancedJobType_ * 4];

        // Generate text objects based on system CCSID.
        Converter conv = new Converter(system_.getCcsid(), system_);

        for (int i = 0; i < 26; ++i) jobSelectionInformation[i] = 0x40;
        conv.stringToByteArray(selectionJobName_.toUpperCase(), jobSelectionInformation, 0);
        conv.stringToByteArray(selectionUserName_.toUpperCase(), jobSelectionInformation, 10);
        conv.stringToByteArray(selectionJobNumber_, jobSelectionInformation, 20);
        conv.stringToByteArray(selectionJobType_, jobSelectionInformation, 26);

        int offset = 108;
        if (numberOfPrimaryJobStatusEntries > 0)
        {
            BinaryConverter.intToByteArray(offset, jobSelectionInformation, 28);
            BinaryConverter.intToByteArray(numberOfPrimaryJobStatusEntries, jobSelectionInformation, 32);
            if (selectActiveJobs_)
            {
                // EBCDIC '*ACTIVE'.
                System.arraycopy(new byte[] { 0x5C, (byte)0xC1, (byte)0xC3, (byte)0xE3, (byte)0xC9, (byte)0xE5, (byte)0xC5, (byte)0x40, (byte)0x40, (byte)0x40 }, 0, jobSelectionInformation, offset, 10);
                offset += 10;
            }
            if (selectJobQueueJobs_)
            {
                // EBCDIC '*JOBQ'.
                System.arraycopy(new byte[] { 0x5C, (byte)0xD1, (byte)0xD6, (byte)0xC2, (byte)0xD8, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 }, 0, jobSelectionInformation, offset, 10);
                offset += 10;
            }
            if (selectOutQueueJobs_)
            {
                // EBCDIC '*OUTQ'.
                System.arraycopy(new byte[] { 0x5C, (byte)0xD6, (byte)0xE4, (byte)0xE3, (byte)0xD8, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 }, 0, jobSelectionInformation, offset, 10);
                offset += 10;
            }
        }
        if (currentActiveStatus_ > 0)
        {
            // Make sure ACTIVE_JOB_STATUS key is specified.
            boolean foundKey = false;
            for (int i = 0; i < currentKey_ && !foundKey; ++i)
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

            BinaryConverter.intToByteArray(offset, jobSelectionInformation, 36);
            BinaryConverter.intToByteArray(currentActiveStatus_, jobSelectionInformation, 40);
            for (int i = 0; i < currentActiveStatus_; ++i)
            {
                conv.stringToByteArray(activeStatuses_[i], jobSelectionInformation, offset, 4);
                offset += 4;
            }
        }
        if (numberOfJobsOnJobQueueStatusEntries > 0)
        {
            // Make sure JOB_QUEUE_STATUS key is specified.
            boolean foundKey = false;
            for (int i = 0; i < currentKey_ && !foundKey; ++i)
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

            BinaryConverter.intToByteArray(offset, jobSelectionInformation, 44);
            BinaryConverter.intToByteArray(numberOfJobsOnJobQueueStatusEntries, jobSelectionInformation, 48);
            if (selectHeldJobs_)
            {
                // EBCDIC 'HLD'.
                System.arraycopy(new byte[] { (byte)0xC8, (byte)0xD3, (byte)0xC4, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 }, 0, jobSelectionInformation, offset, 10);
                offset += 10;
            }
            if (selectScheduledJobs_)
            {
                // EBCDIC 'SCD'.
                System.arraycopy(new byte[] { (byte)0xE2, (byte)0xC3, (byte)0xC4, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 }, 0, jobSelectionInformation, offset, 10);
                offset += 10;
            }
            if (selectReadyJobs_)
            {
                // EBCDIC 'RLS'.
                System.arraycopy(new byte[] { (byte)0xD9, (byte)0xD3, (byte)0xE2, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 }, 0, jobSelectionInformation, offset, 10);
                offset += 10;
            }
        }
        if (currentJobQueue_ > 0)
        {
            // Make sure JOB_QUEUE key is specified.
            boolean foundKey = false;
            for (int i = 0; i < currentKey_ && !foundKey; ++i)
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

            BinaryConverter.intToByteArray(offset, jobSelectionInformation, 52);
            BinaryConverter.intToByteArray(currentJobQueue_, jobSelectionInformation, 56);
            for (int i = 0; i < currentJobQueue_; ++i)
            {
                for (int ii = 0; ii < 20; ++ii) jobSelectionInformation[ii + offset] = 0x40;
                conv.stringToByteArray(jobQueues_[i], jobSelectionInformation, offset, 20);
                offset += 20;
            }
        }
        if (currentInitialUser_ > 0)
        {
            // Make sure CURRENT_USER key is specified.
            boolean foundKey = false;
            for (int i = 0; i < currentKey_ && !foundKey; ++i)
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

            BinaryConverter.intToByteArray(offset, jobSelectionInformation, 60);
            BinaryConverter.intToByteArray(currentInitialUser_, jobSelectionInformation, 64);
            for (int i = 0; i < currentInitialUser_; ++i)
            {
                for (int ii = 0; ii < 10; ++ii) jobSelectionInformation[ii + offset] = 0x40;
                conv.stringToByteArray(initialUsers_[i], jobSelectionInformation, offset, 10);
                offset += 10;
            }
        }
        if (currentServerType_ > 0)
        {
            // Make sure SERVER_TYPE key is specified.
            boolean foundKey = false;
            for (int i = 0; i < currentKey_ && !foundKey; ++i)
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

            BinaryConverter.intToByteArray(offset, jobSelectionInformation, 68);
            BinaryConverter.intToByteArray(currentServerType_, jobSelectionInformation, 72);
            for (int i = 0; i < currentServerType_; ++i)
            {
                for (int ii = 0; ii < 30; ++ii) jobSelectionInformation[ii + offset] = 0x40;
                conv.stringToByteArray(serverTypes_[i], jobSelectionInformation, offset, 30);
                offset += 30;
            }
        }
        if (currentEnhancedJobType_ > 0)
        {
            // Make sure JOB_TYPE_ENHANCED key is specified.
            boolean foundKey = false;
            for (int i = 0; i < currentKey_ && !foundKey; ++i)
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

            BinaryConverter.intToByteArray(offset, jobSelectionInformation, 92);
            BinaryConverter.intToByteArray(currentEnhancedJobType_, jobSelectionInformation, 96);
            for (int i = 0; i < currentEnhancedJobType_; ++i)
            {
                BinaryConverter.intToByteArray(enhancedJobTypes_[i], jobSelectionInformation, offset);
                offset += 4;
            }
        }

        int lengthOfReceiverVariableDefinitionInformation = 4 + 20 * currentKey_;

        byte[] keyOfFieldsToBeReturned = new byte[4 * currentKey_];
        for (int i = 0; i < currentKey_; ++i)
        {
            BinaryConverter.intToByteArray(keys_[i], keyOfFieldsToBeReturned, i * 4);
        }

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[]
        {
            // Receiver variable, output, char(*).
            new ProgramParameter(0),
            // Length of receiver variable, input, binary(4).
            new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } ),
            // Format name, input, char(8), EBCDIC 'OLJB0200'.
            new ProgramParameter(new byte[] { (byte)0xD6, (byte)0xD3, (byte)0xD1, (byte)0xC2, (byte)0xF0, (byte)0xF2, (byte)0xF0, (byte)0xF0 } ),
            // Receiver variable definition information, output, char(*).
            new ProgramParameter(lengthOfReceiverVariableDefinitionInformation),
            // Length of receiver variable definition information, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(lengthOfReceiverVariableDefinitionInformation)),
            // List information, output, char(80).
            new ProgramParameter(80),
            // Number of records to return, input, binary(4).
            new ProgramParameter(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF } ),
            // Sort information, input, char(*).
            new ProgramParameter(sortInformation),
            // Job selection information, input, char(*).
            new ProgramParameter(jobSelectionInformation),
            // Size of job selection information, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(jobSelectionInformation.length)),
            // Number of fields to return, input, binary(4).
            new ProgramParameter(BinaryConverter.intToByteArray(currentKey_)),
            // Key of fields to be returned, input, array(*) of binary(4).
            new ProgramParameter(keyOfFieldsToBeReturned),
            // Error code, I/0, char(*).
            ERROR_CODE,
            // Job selection format name, input, char(8), EBCDIC 'OLJS0200'.
            new ProgramParameter(new byte[] { (byte)0xD6, (byte)0xD3, (byte)0xD1, (byte)0xE2, (byte)0xF0, (byte)0xF2, (byte)0xF0, (byte)0xF0 } )
        };

        // Call the program.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLJOB.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        // List information returned.
        byte[] listInformation = parameters[5].getOutputData();
        // Check the list status indicator.
        ListUtilities.checkListStatus(listInformation[30]);

        handle_ = new byte[4];
        System.arraycopy(listInformation, 8, handle_, 0, 4);
        length_ = BinaryConverter.byteArrayToInt(listInformation, 0);
        recordLength_ = BinaryConverter.byteArrayToInt(listInformation, 12);

        // Key information returned.
        byte[] defInfo = parameters[3].getOutputData();
        numKeysReturned_ = BinaryConverter.byteArrayToInt(defInfo, 0);
        keyFieldsReturned_ = new int[numKeysReturned_];
        keyTypesReturned_ = new char[numKeysReturned_];
        keyLengthsReturned_ = new int[numKeysReturned_];
        keyOffsetsReturned_ = new int[numKeysReturned_];

        offset = 4;
        for (int i = 0; i < numKeysReturned_; ++i)
        {
            keyFieldsReturned_[i] = BinaryConverter.byteArrayToInt(defInfo, offset + 4);
            keyTypesReturned_[i] = conv.byteArrayToString(defInfo, offset + 8, 1).charAt(0); // 'C' or 'B'
            keyLengthsReturned_[i] = BinaryConverter.byteArrayToInt(defInfo, offset + 12);
            keyOffsetsReturned_[i] = BinaryConverter.byteArrayToInt(defInfo, offset + 16);
            offset += 20;
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Loaded job list, length: " + length_ + ", record length: " + recordLength_ + ", handle:", handle_);
    }

    /**
     Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
     @param  listener  The listener object.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Removes the VetoableChangeListener.  If the VetoableChangeListener is not on the list, nothing is done.
     @param  listener  The listener object.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
    }

    /**
     Sets the job name that describes which jobs are returned.  The default is SELECTION_JOB_NAME_ALL.  This takes effect the next time the list of jobs is retrieved or refreshed.
     @param  name  The job name, or {@link #SELECTION_JOB_NAME_ALL SELECTION_JOB_NAME_ALL} for all job names.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @see  #addJobSelectionCriteria
     @see  #getName
     **/
    public void setName(String name) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting name: " + name);
        if (name == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'name' is null.");
            throw new NullPointerException("name");
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                selectionJobName_ = name;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = selectionJobName_;
            String newValue = name;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("name", oldValue, newValue);
            }
            synchronized (this)
            {
                selectionJobName_ = name;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("name", oldValue, newValue);
            }
        }
    }

    /**
     Sets the job number that describes which jobs are returned.  The default is SELECTION_JOB_NUMBER_ALL.  This takes effect the next time the list of jobs is retrieved or refreshed.
     @param  number  The job number, or {@link #SELECTION_JOB_NUMBER_ALL SELECTION_JOB_NUMBER_ALL} for all job numbers.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @see  #addJobSelectionCriteria
     @see  #getNumber
     **/
    public void setNumber(String number) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting number: " + number);
        if (number == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'number' is null.");
            throw new NullPointerException("number");
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                selectionJobNumber_ = number;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = selectionJobNumber_;
            String newValue = number;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("number", oldValue, newValue);
            }
            synchronized (this)
            {
                selectionJobNumber_ = number;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("number", oldValue, newValue);
            }
        }
    }

    /**
     Sets the system object representing the system on which the jobs exist.  The system cannot be changed once a connection to the system has been established.
     @param  system  The system object representing the system on which the jobs exists.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting system: " + system);

        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (handle_ != null)
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
     Sets the user name value that describes which jobs are returned.  The default is SELECTION_USER_NAME_ALL.  This takes effect the next time the list of jobs is retrieved or refreshed.
     @param  user  The user name, or {@link #SELECTION_USER_NAME_ALL SELECTION_USER_NAME_ALL} for all user names.
     @exception  PropertyVetoException  If any of the registered listeners vetos the property change.
     @see  #addJobSelectionCriteria
     @see  #getUser
     **/
    public void setUser(String user) throws PropertyVetoException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting user: " + user);
        if (user == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'user' is null.");
            throw new NullPointerException("user");
        }
        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            synchronized (this)
            {
                selectionUserName_ = user;
                if (handle_ != null) closeHandle_ = true;
            }
        }
        else
        {
            String oldValue = selectionUserName_;
            String newValue = user;
            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("user", oldValue, newValue);
            }
            synchronized (this)
            {
                selectionUserName_ = user;
                if (handle_ != null) closeHandle_ = true;
            }
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("user", oldValue, newValue);
            }
        }
    }
}
