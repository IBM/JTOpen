///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: VJob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.Trace;
import com.ibm.as400.resource.ResourceException;
import com.ibm.as400.resource.RJob;
import com.ibm.as400.resource.RJobLog;
import com.ibm.as400.resource.RQueuedMessage;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.tree.TreeNode;




/**
The VJob class defines the representation of a job on an
AS/400 for use in various models and panes in this package.
You must explicitly call load() to load the information from
the AS/400.

<p>Most errors are reported as ErrorEvents rather than
throwing exceptions.  Users should listen for ErrorEvents
in order to diagnose and recover from error conditions.

<p>VJob objects generate the following events:
<ul>
    <li>ErrorEvent
    <li>PropertyChangeEvent
    <li>VObjectEvent
    <li>WorkingEvent
</ul>
**/

public class VJob
implements VNode, Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    // MRI
    private static String           description_                = ResourceLoader.getText ("JOB_DESCRIPTION");
    private static Icon             icon16_                     = ResourceLoader.getIcon ("VJob16.gif", description_);
    private static Icon             icon32_                     = ResourceLoader.getIcon ("VJob32.gif", description_);
    private static String           dateColumnHeader_           = ResourceLoader.getText ("MESSAGE_DATE");
    private static String           fromProgramColumnHeader_    = ResourceLoader.getText ("MESSAGE_FROM_PROGRAM");
    private static String           nameColumnHeader_           = ResourceLoader.getText ("MESSAGE_ID");
    private static String           textColumnHeader_           = ResourceLoader.getText ("MESSAGE_TEXT");
    private static String           typeColumnHeader_           = ResourceLoader.getText ("MESSAGE_TYPE");
    private static String           severityColumnHeader_       = ResourceLoader.getText ("MESSAGE_SEVERITY");


    // Properties.
    private VNode                   parent_     = null;
    private RJob                    job_        = null;
    private RJobLog                 jobLog_     = null;
    // @A1A : Added action.
    transient private VAction[]               actions_; //@A3C


    // Static data.
    private static TableColumnModel detailsColumnModel_     = null;



    // Private data.
    transient private VObject[]         detailsChildren_;
    transient private boolean           loaded_; //@A2A
    transient private VPropertiesPane   propertiesPane_;


    // Event support.
    transient private ErrorEventSupport           errorEventSupport_    /*@A3D= new ErrorEventSupport(this)*/;
    transient private PropertyChangeSupport       propertyChangeSupport_/*@A3D= new PropertyChangeSupport(this)*/;
    transient private VetoableChangeSupport       vetoableChangeSupport_/*@A3D= new VetoableChangeSupport(this)*/;
    transient private VObjectEventSupport         objectEventSupport_   /*@A3D= new VObjectEventSupport(this)*/;
    transient private WorkingEventSupport         workingEventSupport_  /*@A3D= new WorkingEventSupport(this)*/;


// @A1A : Added properties.
/**
Property identifier for the break message handling.
**/
    public static final Object        BREAK_MESSAGE_HANDLING_PROPERTY    = "BreakMessageHandling";
/**
Property identifier for the coded character set identifier. 
**/
    public static final Object        CCSID_PROPERTY                   = "CCSID";
/**
Property identifier for the completion status. 
**/
    public static final Object        COMPLETION_STATUS_PROPERTY        = "CompletionStatus";
/**
Property identifier for the country identifier. 
**/
    public static final Object        COUNTRY_ID_PROPERTY               = "CountryID";

/**
Property identifier for the CPU used.
**/
    public static final Object        CPUUSED_PROPERTY                  = "CPUUsed";

/**
Property identifier for the current library existence.
**/
    public static final Object        CURRENT_LIBRARY_EXISTENCE_PROPERTY    = "CurrentLibraryExistence";

/**
Property identifier for the current library if one exists. 
**/
    public static final Object        CURRENT_LIBRARY_PROPERTY          = "CurrentLibrary";

/**
Property identifier for the date format. 
**/
    public static final Object        DATE_FORMAT_PROPERTY              = "DateFormat";

/**
Property identifier for the date and time the job become active. 
**/
    public static final Object        DATE_JOB_BECAME_ACTIVE_PROPERTY     = "DateJobBecomeActive";

/**
Property identifier for the date and time the job entered system. 
**/
    public static final Object        DATE_JOB_ENTERED_SYSTEM_PROPERTY      = "DateJobEnterSystem";

/**
Property identifier for the date and time the job is scheduled to run. 
**/
    public static final Object        DATE_JOB_SCHEDULE_TO_RUN_PROPERTY         = "DateScheduleRun";

/**
Property identifier for the date.
**/
    public static final Object        DATE_PROPERTY                    = "Date";

/**
Property identifier for the date and time the job was put on this job queue. 
**/
    public static final Object        DATE_PUT_ON_JOB_QUEUE_PROPERTY       = "DatePutOnJobQueue";

/**
Property identifier for the date separator. 
**/
    public static final Object        DATE_SEPARATOR_PROPERTY           = "DateSeparator";

/**
Property identifier for the DDM conversation handling. 
**/
    public static final Object        DDM_CONVERSATION_HANDLING_PROPERTY    = "DDMConversationHandling";

/**
Property identifier for the decimal format. 
**/
    public static final Object        DECIMAL_FORMAT_PROPERTY           = "DecimalFormat";

/**
Property identifier for the default coded character set identifier. 
**/
    public static final Object        DEFAULT_CCSID_PROPERTY            = "DefaultCCSID";

/**
Property identifier for the device recovery action. 
**/
    public static final Object        DEVICE_RECOVERY_ACTION_PROPERTY    = "DeviceRecoveryAction";

/**
Property identifier for the end severity. 
**/
    public static final Object        END_SEVERITY_PROPERTY             = "EndSeverity";

/**
Property identifier for the function. 
**/
    public static final Object        FUNCTION_PROPERTY                = "Function";

/**
Property identifier for the inquiry message reply. 
**/
    public static final Object        INQUIRY_MESSAGE_REPLY_PROPERTY     = "InquiryMessageReply";

/**
Property identifier for the job accounting code. 
**/
    public static final Object        JOB_ACCOUNTING_CODE_PROPERTY       = "JobAccountingCode";

/**
Property identifier for the job date. 
**/
    public static final Object        JOB_DATE_PROPERTY                 = "JobDate";

/**
Property identifier for the job description.
**/
    public static final Object        JOB_DESCRIPTION_PROPERTY          = "JobDescription";

/**
Property identifier for the job message queue action.
**/
    public static final Object        JOB_MESSAGE_QUEUE_FULL_ACTION_PROPERTY   = "JobMessageQueueFullAction";

    /**
Property identifier for the job message maximum size.
**/
    public static final Object        JOB_MESSAGE_QUEUE_MAXIMUM_SIZE_PROPERTY  = "JobMessageQueueMaximumSize";

/**
Property identifier for the job queue priority. 
**/
    public static final Object        JOB_QUEUE_PRIORITY_PROPERTY        = "JobQueuePriority";

    /**
Property identifier for the job queue.
**/
    public static final Object        JOB_QUEUE_PROPERTY                = "JobQueue";

    /**
Property identifier for the job switches. 
**/
    public static final Object        JOB_SWITCHES_PROPERTY             = "JobSwitches";

/**
Property identifier for the language identifier. 
**/
    public static final Object        LANGUAGE_ID_PROPERTY              = "LanguageID";

/**
Property identifier for the logging of CL programs . 
**/
    public static final Object        LOGGING_CL_PROGRAMS_PROPERTY       = "LoggingCLPrograms";

/**
Property identifier for the logging level. 
**/
    public static final Object        LOGGING_LEVEL_PROPERTY            = "LoggingLevel";

/**
Property identifier for the logging severity. 
**/
    public static final Object        LOGGING_SEVERITY_PROPERTY         = "LoggingSeverity";

    /**
Property identifier for the logging text. 
**/
    public static final Object        LOGGING_TEXT_PROPERTY             = "LoggingText";

    /**
Property identifier for the mode name. 
**/
    public static final Object        MODE_NAME_PROPERTY                = "ModeName";

/**
Property identifier for the number. 
**/
    public static final Object        NUMBER_PROPERTY                  = "Number";

/**
Property identifier for the number of libraries in SYSLIBL.
**/
    public static final Object        NUMBER_OF_LIBRARIES_IN_SYSLIBL_PROPERTY = "NumberOfLibrariesInSYSLIBL";

/**
Property identifier for the number of libraries in USRLIBL.
**/
    public static final Object        NUMBER_OF_LIBRARIES_IN_USRLIBL_PROPERTY = "NumberOfLibrariesInUSRLIBL";

/**
Property identifier for the number of product libaries.
**/
    public static final Object        NUMBER_OF_PRODUCT_LIBRARIES_PROPERTY   = "NumberOfProductLibraries";

/**
Property identifier for the output queue priority. 
**/
    public static final Object        OUTPUT_QUEUE_PRIORITY_PROPERTY         = "OutputQueue";

/**
Property identifier for the output queue. 
**/
    public static final Object        OUTPUT_QUEUE_PROPERTY         = "OutputQueue";

    /**
Property identifier for the print key format. 
**/
    public static final Object        PRINT_KEY_FORMAT_PROPERTY      = "PrintKeyFormat";

/**
Property identifier for the print text. 
**/
    public static final Object        PRINT_TEXT_PROPERTY           = "PrintText";

/**
Property identifier for the printer device name. 
**/
    public static final Object        PRINTER_DEVICE_NAME_PROPERTY   = "PrinterDeviceName";

/**
Property identifier for the product libraries if they exist. 
**/
    public static final Object        PRODUCT_LIBRARIES_PROPERTY    = "ProductLibraries";

    /**
Property identifier for the routing data. 
**/
    public static final Object        ROUTING_DATA_PROPERTY         = "RoutingData";

/**
Property identifier for the signed-on job. 
**/
    public static final Object        SIGNED_ON_JOB_PROPERTY         = "SignedOnJob";

/**
Property identifier for the sort sequence table. 
**/
    public static final Object        SORT_SEQUENCE_TABLE_PROPERTY   = "SortSequenceTable";

/**
Property identifier for the status of the job on the job queue. 
**/
    public static final Object        STATUS_OF_JOB_ON_JOB_QUEUE_PROPERTY    = "StatusOfJobOnJobQueue";

/**
Property identifier for the status message handling. 
**/
    public static final Object        STATUS_MESSAGE_HANDLING_PROPERTY    = "StatusMessageHandling";

/**
Property identifier for the status. 
**/
    public static final Object        STATUS_PROPERTY              = "Status";

/**
Property identifier for the subsystem. 
**/
    public static final Object        SUBSYSTEM_PROPERTY           = "Subsystem";

/**
Property identifier for the subtype. 
**/
    public static final Object        SUBTYPE_PROPERTY             = "Subtype";

    /**
Property identifier for the system library list (for each library in the list). 
**/
    public static final Object        SYSTEM_LIBRARY_LIST_PROPERTY   = "SystemLibraryList";

/**
Property identifier for the time separator. 
**/
    public static final Object        TIME_SEPARATOR_PROPERTY       = "TimeSeparator";

/**
Property identifier for the type. 
**/
    public static final Object        TYPE_PROPERTY                = "Type";

/**
Property identifier for the unit of work identifier. 
**/
    public static final Object        WORK_ID_UNIT_PROPERTY          = "WorkIDUnit";

/**
Property identifier for the user library list
**/
    public static final Object        USER_LIBRARY_LIST_PROPERTY     = "UserLibraryList";

/**
Property identifier for the user. 
**/
    public static final Object        USER_PROPERTY                = "User";



    // Map vaccess property identifiers to resource attribute IDs:
    private static final Hashtable map_ = new Hashtable();
    static {
        map_.put(BREAK_MESSAGE_HANDLING_PROPERTY,           RJob.BREAK_MESSAGE_HANDLING);
        map_.put(CCSID_PROPERTY                   , RJob.CCSID);
        map_.put(COMPLETION_STATUS_PROPERTY        , RJob.COMPLETION_STATUS);
        map_.put(COUNTRY_ID_PROPERTY               , RJob.COUNTRY_ID);
        map_.put(CPUUSED_PROPERTY,                          RJob.CPU_TIME_USED);
        map_.put(CURRENT_LIBRARY_EXISTENCE_PROPERTY    , RJob.CURRENT_LIBRARY_EXISTENCE);
        map_.put(CURRENT_LIBRARY_PROPERTY          , RJob.CURRENT_LIBRARY);
        map_.put(DATE_FORMAT_PROPERTY              , RJob.DATE_FORMAT);
        map_.put(DATE_JOB_BECAME_ACTIVE_PROPERTY     , RJob.DATE_STARTED);
        map_.put(DATE_JOB_ENTERED_SYSTEM_PROPERTY      , RJob.DATE_ENTERED_SYSTEM);
        map_.put(DATE_JOB_SCHEDULE_TO_RUN_PROPERTY         , RJob.SCHEDULE_DATE);
        map_.put(DATE_PROPERTY                    , RJob.DATE_ENTERED_SYSTEM);
        map_.put(DATE_PUT_ON_JOB_QUEUE_PROPERTY       , RJob.JOB_QUEUE_DATE);
        map_.put(DATE_SEPARATOR_PROPERTY           , RJob.DATE_SEPARATOR);
        map_.put(DDM_CONVERSATION_HANDLING_PROPERTY    , RJob.KEEP_DDM_CONNECTIONS_ACTIVE);
        map_.put(DECIMAL_FORMAT_PROPERTY           , RJob.DECIMAL_FORMAT);
        map_.put(DEFAULT_CCSID_PROPERTY            , RJob.DEFAULT_CCSID);
        map_.put(DEVICE_RECOVERY_ACTION_PROPERTY    , RJob.DEVICE_RECOVERY_ACTION);
        map_.put(END_SEVERITY_PROPERTY             , RJob.END_SEVERITY);
        map_.put(FUNCTION_PROPERTY                , RJob.FUNCTION_NAME);
        map_.put(INQUIRY_MESSAGE_REPLY_PROPERTY     , RJob.INQUIRY_MESSAGE_REPLY);
        map_.put(JOB_ACCOUNTING_CODE_PROPERTY       , RJob.ACCOUNTING_CODE);
        map_.put(JOB_DATE_PROPERTY                 , RJob.JOB_DATE );
        map_.put(JOB_DESCRIPTION_PROPERTY          , RJob.JOB_DESCRIPTION);
        map_.put(JOB_MESSAGE_QUEUE_FULL_ACTION_PROPERTY   , RJob.MESSAGE_QUEUE_ACTION);
        map_.put(JOB_MESSAGE_QUEUE_MAXIMUM_SIZE_PROPERTY  , RJob.MESSAGE_QUEUE_MAX_SIZE);
        map_.put(JOB_QUEUE_PRIORITY_PROPERTY        , RJob.JOB_QUEUE_PRIORITY);
        map_.put(JOB_QUEUE_PROPERTY                , RJob.JOB_QUEUE );
        map_.put(JOB_SWITCHES_PROPERTY             , RJob.JOB_SWITCHES);
        map_.put(LANGUAGE_ID_PROPERTY              , RJob.LANGUAGE_ID);
        map_.put(LOGGING_CL_PROGRAMS_PROPERTY       , RJob.LOG_CL_PROGRAMS );
        map_.put(LOGGING_LEVEL_PROPERTY            , RJob.LOGGING_LEVEL);
        map_.put(LOGGING_SEVERITY_PROPERTY         , RJob.LOGGING_SEVERITY);
        map_.put(LOGGING_TEXT_PROPERTY             , RJob.LOGGING_TEXT);
        map_.put(MODE_NAME_PROPERTY                , RJob.MODE);
        map_.put(NUMBER_PROPERTY                  , RJob.JOB_NUMBER);
        map_.put(NUMBER_OF_LIBRARIES_IN_SYSLIBL_PROPERTY , RJob.SYSTEM_LIBRARY_LIST);
        map_.put(NUMBER_OF_LIBRARIES_IN_USRLIBL_PROPERTY , RJob.USER_LIBRARY_LIST);
        map_.put(NUMBER_OF_PRODUCT_LIBRARIES_PROPERTY   , RJob.PRODUCT_LIBRARIES);
        map_.put(OUTPUT_QUEUE_PRIORITY_PROPERTY         , RJob.OUTPUT_QUEUE_PRIORITY );
        map_.put(OUTPUT_QUEUE_PROPERTY         , RJob.OUTPUT_QUEUE);
        map_.put(PRINT_KEY_FORMAT_PROPERTY      , RJob.PRINT_KEY_FORMAT);
        map_.put(PRINT_TEXT_PROPERTY           , RJob.PRINT_TEXT);
        map_.put(PRINTER_DEVICE_NAME_PROPERTY   , RJob.PRINTER_DEVICE_NAME);
        map_.put(PRODUCT_LIBRARIES_PROPERTY    , RJob.PRODUCT_LIBRARIES);
        map_.put(ROUTING_DATA_PROPERTY         , RJob.ROUTING_DATA);
        map_.put(SIGNED_ON_JOB_PROPERTY         , RJob.SIGNED_ON_JOB);
        map_.put(SORT_SEQUENCE_TABLE_PROPERTY   , RJob.SORT_SEQUENCE_TABLE);
        map_.put(STATUS_OF_JOB_ON_JOB_QUEUE_PROPERTY    , RJob.JOB_QUEUE_STATUS);
        map_.put(STATUS_MESSAGE_HANDLING_PROPERTY    , RJob.STATUS_MESSAGE_HANDLING);
        map_.put(STATUS_PROPERTY              , RJob.JOB_STATUS);
        map_.put(SUBSYSTEM_PROPERTY           , RJob.SUBSYSTEM);
        map_.put(SUBTYPE_PROPERTY             , RJob.JOB_SUBTYPE);
        map_.put(SYSTEM_LIBRARY_LIST_PROPERTY   , RJob.SYSTEM_LIBRARY_LIST);
        map_.put(TIME_SEPARATOR_PROPERTY       , RJob.TIME_SEPARATOR);
        map_.put(TYPE_PROPERTY                , RJob.JOB_TYPE);
        map_.put(WORK_ID_UNIT_PROPERTY          , RJob.LOCATION_NAME);
        map_.put(USER_LIBRARY_LIST_PROPERTY     , RJob.USER_LIBRARY_LIST);
        map_.put(USER_PROPERTY                , RJob.USER_NAME);
    }




/**
Static initializer.
**/
//
// Implementation note:
//
// * The column widths are completely arbitrary.
//
    static
    {
        detailsColumnModel_ = new DefaultTableColumnModel ();
        int columnIndex = 0;

        // Name column.
        VTableColumn nameColumn = new VTableColumn (columnIndex++, VJobLogMessage.NAME_PROPERTY);
        nameColumn.setCellRenderer (new VObjectCellRenderer ());
        nameColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        nameColumn.setHeaderValue (nameColumnHeader_);
        nameColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (nameColumn);

        // Text column.
        VTableColumn textColumn = new VTableColumn (columnIndex++, VJobLogMessage.TEXT_PROPERTY);
        textColumn.setCellRenderer (new VObjectCellRenderer ());
        textColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        textColumn.setHeaderValue (textColumnHeader_);
        textColumn.setPreferredCharWidth (80);
        detailsColumnModel_.addColumn (textColumn);

        // Severity column.
        VTableColumn severityColumn = new VTableColumn (columnIndex++, VJobLogMessage.SEVERITY_PROPERTY);
        severityColumn.setCellRenderer (new VObjectCellRenderer (SwingConstants.RIGHT));
        severityColumn.setHeaderRenderer (new VObjectHeaderRenderer (SwingConstants.RIGHT));
        severityColumn.setHeaderValue (severityColumnHeader_);
        severityColumn.setPreferredCharWidth (8);
        detailsColumnModel_.addColumn (severityColumn);

        // Type column.
        VTableColumn typeColumn = new VTableColumn (columnIndex++, VJobLogMessage.TYPE_PROPERTY);
        typeColumn.setCellRenderer (new VObjectCellRenderer ());
        typeColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        typeColumn.setHeaderValue (typeColumnHeader_);
        typeColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (typeColumn);

        // Date column.
        VTableColumn dateColumn = new VTableColumn (columnIndex++, VJobLogMessage.DATE_PROPERTY);
        dateColumn.setCellRenderer (new VObjectCellRenderer ());
        dateColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        dateColumn.setHeaderValue (dateColumnHeader_);
        dateColumn.setPreferredCharWidth (20);
        detailsColumnModel_.addColumn (dateColumn);

        // From program column.
        VTableColumn fromProgramColumn = new VTableColumn (columnIndex++, VJobLogMessage.FROM_PROGRAM_PROPERTY);
        fromProgramColumn.setCellRenderer (new VObjectCellRenderer ());
        fromProgramColumn.setHeaderRenderer (new VObjectHeaderRenderer ());
        fromProgramColumn.setHeaderValue (fromProgramColumnHeader_);
        fromProgramColumn.setPreferredCharWidth (10);
        detailsColumnModel_.addColumn (fromProgramColumn);

    }



    // Properties pane layout.
    private static ResourceProperties   properties_         = null;
    static {
        // Resource properties pane layout.
        properties_ = new ResourceProperties();
        properties_.addProperties(new Object[] { RJob.JOB_TYPE, RJob.JOB_SUBTYPE, RJob.JOB_STATUS,
                                      RJob.DATE_ENTERED_SYSTEM, RJob.JOB_DATE, 
                                      RJob.JOB_DESCRIPTION,
                                      RJob.JOB_QUEUE, RJob.JOB_QUEUE_PRIORITY, RJob.JOB_QUEUE_STATUS });
        properties_.addProperties(properties_.addTab(ResourceLoader.getText ("TAB_PRINTER_OUTPUT")), 
                                  new Object[] { RJob.OUTPUT_QUEUE, RJob.OUTPUT_QUEUE_PRIORITY, 
                                      RJob.PRINTER_DEVICE_NAME, RJob.PRINT_KEY_FORMAT, RJob.PRINT_TEXT });
        properties_.addProperties(properties_.addTab(ResourceLoader.getText ("TAB_ACTIVE")), 
                                  new Object[] { RJob.RUN_PRIORITY, RJob.SUBSYSTEM, RJob.SYSTEM_POOL_ID,
                                      RJob.AUXILIARY_IO_REQUESTS, RJob.INTERACTIVE_TRANSACTIONS,
                                      RJob.TOTAL_RESPONSE_TIME, RJob.DEFAULT_WAIT_TIME, RJob.ELIGIBLE_FOR_PURGE, 
                                      RJob.JOB_SWITCHES, RJob.TIME_SLICE,
                                      RJob.TIME_SLICE_END_POOL  });
        properties_.addProperties(properties_.addTab(ResourceLoader.getText ("TAB_MESSAGE")), 
                                  new Object[] { RJob.INQUIRY_MESSAGE_REPLY, RJob.BREAK_MESSAGE_HANDLING, 
                                      RJob.STATUS_MESSAGE_HANDLING, RJob.LOG_CL_PROGRAMS, RJob.LOGGING_SEVERITY,
                                      RJob.LOGGING_LEVEL, RJob.LOGGING_TEXT, RJob.MESSAGE_QUEUE_ACTION });
        properties_.addProperties(properties_.addTab(ResourceLoader.getText ("TAB_DATETIME")), 
                                  new Object[] { RJob.JOB_QUEUE_DATE, RJob.DATE_ENTERED_SYSTEM, RJob.DATE_FORMAT, 
                                      RJob.DATE_SEPARATOR, RJob.TIME_SEPARATOR, RJob.SCHEDULE_DATE });
        properties_.addProperties(properties_.addTab(ResourceLoader.getText ("TAB_LANGUAGE")), 
                                  new Object[] { RJob.CCSID, RJob.DEFAULT_CCSID, RJob.COUNTRY_ID, 
                                      RJob.LANGUAGE_ID, RJob.SORT_SEQUENCE_TABLE });
        properties_.addProperties(properties_.addTab(ResourceLoader.getText ("TAB_LIBRARY_LIST")), 
                                  new Object[] { RJob.SYSTEM_LIBRARY_LIST, RJob.USER_LIBRARY_LIST, 
                                      RJob.PRODUCT_LIBRARIES, 
                                      RJob.CURRENT_LIBRARY });
        properties_.addProperties(properties_.addTab(ResourceLoader.getText ("TAB_OTHER")), 
                                  new Object[] { RJob.LOCATION_NAME, RJob.MODE, RJob.DEVICE_RECOVERY_ACTION, 
                                      RJob.KEEP_DDM_CONNECTIONS_ACTIVE,  RJob.COMPLETION_STATUS,
                                      RJob.ROUTING_DATA, 
                                      RJob.DECIMAL_FORMAT, RJob.ACCOUNTING_CODE, RJob.FUNCTION_NAME,
                                      RJob.FUNCTION_TYPE, RJob.SIGNED_ON_JOB });

    }


/**
Constructs a VJob object.
**/
    public VJob ()
    {
        job_    = new RJob();
        jobLog_ = new RJobLog ();
        initializeTransient ();
    }



/**
Constructs a VJob object.

@param system       The AS/400 system on which the job resides.
@param job          The job.
**/
    public VJob (AS400 system, Job job)
    {
        if (system == null)
            throw new NullPointerException ("system");
        if (job == null)
            throw new NullPointerException ("job");

        job_     = new RJob(system, job.getName(), job.getUser(), job.getNumber());
        jobLog_  = new RJobLog (system);

        try {
            jobLog_.setName (job.getName ());
            jobLog_.setNumber (job.getNumber ());
            jobLog_.setUser (job.getUser ());
        }
        catch (PropertyVetoException e) {
            // Ignore.
            Trace.log(Trace.ERROR,"VJob constructor : "+e);
        }

        initializeTransient ();
    }



/**
Constructs a VJob object.

@param system       The AS/400 system on which the job resides.
@param job          The job.
**/
    public VJob (AS400 system, RJob job)
    {
        if (system == null)
            throw new NullPointerException ("system");
        if (job == null)
            throw new NullPointerException ("job");

        job_     = job;
        jobLog_  = new RJobLog (system);

        try {
            jobLog_.setName (job.getName ());
            jobLog_.setNumber (job.getNumber ());
            jobLog_.setUser (job.getUser ());
        }
        catch (PropertyVetoException e) {
            // Ignore.
            Trace.log(Trace.ERROR,"VJob constructor : "+e);
        }

        initializeTransient ();
    }



/**
Constructs a VJob object.

@param parent   The parent.
@param system   The AS/400 system on which the job resides.
@param job      The job.
**/
    public VJob (VNode parent, AS400 system, Job job)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");
        if (job == null)
            throw new NullPointerException ("job");

        parent_ = parent;
        job_    = new RJob(system, job.getName(), job.getUser(), job.getNumber());
        jobLog_ = new RJobLog (system);

        try {
            jobLog_.setName (job.getName ());
            jobLog_.setNumber (job.getNumber ());
            jobLog_.setUser (job.getUser ());
        }
        catch (PropertyVetoException e) {
            // Ignore.
            Trace.log(Trace.ERROR,"VJob constructor : "+e);
        }

        initializeTransient ();
    }



/**
Constructs a VJob object.

@param parent   The parent.
@param system   The AS/400 system on which the job resides.
@param job      The job.
**/
    public VJob (VNode parent, AS400 system, RJob job)
    {
        if (parent == null)
            throw new NullPointerException ("parent");
        if (system == null)
            throw new NullPointerException ("system");
        if (job == null)
            throw new NullPointerException ("job");

        parent_ = parent;
        job_    = job;
        jobLog_ = new RJobLog (system);

        try {
            jobLog_.setName (job.getName ());
            jobLog_.setNumber (job.getNumber ());
            jobLog_.setUser (job.getUser ());
        }
        catch (PropertyVetoException e) {
            // Ignore.
            Trace.log(Trace.ERROR,"VJob constructor : "+e);
        }

        initializeTransient ();
    }



/**
Adds a listener to be notified when an error occurs.

@param  listener    The listener.
**/
    public void addErrorListener (ErrorListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        errorEventSupport_.addErrorListener (listener);
    }



/**
Adds a listener to be notified when the value of any
bound property changes.

@param  listener  The listener.
**/
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        propertyChangeSupport_.addPropertyChangeListener (listener);
    }



/**
Adds a listener to be notified when the value of any
constrained property changes.

@param  listener  The listener.
**/
    public void addVetoableChangeListener (VetoableChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        vetoableChangeSupport_.addVetoableChangeListener (listener);
    }



/**
Adds a listener to be notified when a VObject is changed,
created, or deleted.

@param  listener    The listener.
**/
    public void addVObjectListener (VObjectListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        objectEventSupport_.addVObjectListener (listener);
    }



/**
Adds a listener to be notified when work starts and stops
on potentially long-running operations.

@param  listener    The listener.
**/
    public void addWorkingListener (WorkingListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        workingEventSupport_.addWorkingListener (listener);
    }



    /**
     Returns the children of the node.
     @return The children.
    **/
    public Enumeration children ()
    {
      return new VEnumeration (this);
    }


    /**
     Returns the list of actions that can be performed.
     @return The list of actions.
    **/
    public VAction[] getActions ()
    {
      return actions_;
    }



/**
Indiciates if the node allows children.

@return  Always false.
**/
    public boolean getAllowsChildren ()
    {
        return false;
    }



/**
Returns the child node at the specified index.

@param  index   The index.
@return         Always null.
**/
    public TreeNode getChildAt (int index)
    {
        return null;
    }



/**
Returns the number of children.

@return  Always 0.
**/
    public int getChildCount ()
    {
        return 0;
    }



/**
Returns the default action.

@return Always null.  There is no default action.
**/
    public VAction getDefaultAction ()
    {
        return null;
    }



/**
Returns the child for the details at the specified index.

@param  index   The index.
@return         The child, or null if the index is not
                valid.
**/
    public VObject getDetailsChildAt (int index)
    {
        if (!loaded_)
            return null;
        if (detailsChildren_ == null)
            loadFirst();
        if ((index < 0) || (index >= detailsChildren_.length))
            return null;
        if (detailsChildren_[index] == null)
            loadSingle(index);
        return detailsChildren_[index];
    }



/**
Returns the number of children for the details.

@return  The number of children for the details.
**/
    public /* @A2D synchronized */ int getDetailsChildCount ()
    {
        if (!loaded_)
            return 0;
        if (detailsChildren_ == null)
            loadFirst();
        return detailsChildren_.length;
    }



/**
Returns the table column model to use in the details
when representing the children.  This column model
describes the details values for the children.

@return The details column model.
**/
    public TableColumnModel getDetailsColumnModel ()
    {
        return detailsColumnModel_;
    }



/**
Returns the index of the specified child for the details.

@param  detailsChild   The details child.
@return                The index, or -1 if the child is not found
                       in the details.
**/
    public /* @A2D synchronized */ int getDetailsIndex (VObject detailsChild)
    {
        if (detailsChildren_ == null)
            loadFirst();
        if (detailsChild == null)
            return -1;
        for (int i = 0; i < detailsChildren_.length; ++i) //@A2C
            if (detailsChildren_[i] == detailsChild)
                return i;
        return -1;
    }



/**
Returns the icon.

@param  size    The icon size, either 16 or 32.  If any other
                value is given, then return a default.
@param  open    This parameter has no effect.
@return         The icon.
**/
    public Icon getIcon (int size, boolean open)
    {
        if (size == 32)
            return icon32_;
        else
            return icon16_;
    }



/**
Returns the index of the specified child.

@param  child   The child.
@return         Always -1.
**/
    public int getIndex (TreeNode child)
    {
        return -1;
    }



/**
Returns the job.

@return The job, or null if it has not been set.
**/
    public Job getJob ()
    {
        Job job = new Job();
        try {
            if (job_.getSystem() != null)
                job.setSystem(job_.getSystem());
            if (job_.getName() != null)
                job.setName(job_.getName());
            if (job_.getUser() != null)
                job.setUser(job_.getUser());
            if (job_.getNumber() != null)
                job.setNumber(job_.getNumber());
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
        return job;
    }




/**
Returns the job.

@return The job, or null if it has not been set.
**/
    public RJob getRJob ()
    {
        return job_;
    }




    //@A1A Get Job type field from type and subtype value.
    private String getJobType(char type,char subtype)
    {
        switch (type)
        {
            case 'A' :
                switch (subtype)
                {
                    case ' ' :
                        return "ASJ";
                    default :
                        break;
                }
            case 'B' :
                switch (subtype)
                {
                    case ' ' :
                        return "BCH";
                    case 'D' :
                        return "BCI";
                    case 'E' :
                        return "EVK";
                    case 'F' :
                        return "M36";
                    case 'T' :
                        return "MRT";
                    case 'J' :
                        return "PJ";
                    case 'U' :
                        return "";
                    default :
                        break;
                }
            case 'I' :
                switch (subtype)
                {
                    case ' ' :
                        return "INT";
                    default :
                        break;
                }
            case 'W' :
                switch (subtype)
                {
                    case ' ' :
                        return "WTR";
                    case 'P' :
                        return "PDJ";
                    default :
                        break;
                }
            case 'R' :
                switch (subtype)
                {
                    case ' ' :
                        return "RDR";
                    default :
                        break;
                }
            case 'M' :
                switch (subtype)
                {
                    case ' ' :
                        return "SBS";
                    default :
                        break;
                }
            case 'S' :
            case 'X' :
                switch (subtype)
                {
                    case ' ' :
                        return "SYS";
                    default :
                        break;
                }
            default :
                break;
        }
        return "";
    }


/**
Returns the parent node.

@return The parent node, or null if there is no parent.
**/
    public TreeNode getParent ()
    {
        return parent_;
    }



/**
Returns the properties pane.

@return The properties pane.
**/
    public VPropertiesPane getPropertiesPane ()
    {
        return propertiesPane_;
    }



/**
Returns a property value.

@param      propertyIdentifier  The property identifier.  The choices are
                                <ul>
                                  <li>BREAK_MESSAGE_HANDLING_PROPERTY
                                  <li>CCSID_PROPERTY
                                  <li>COMPLETION_STATUS_PROPERTY
                                  <li>COUNTRY_ID_PROPERTY
                                  <li>CPUUSED_PROPERTY
                                  <li>CURRENT_LIBRARY_EXISTENCE_PROPERTY
                                  <li>CURRENT_LIBRARY_PROPERTY
                                  <li>DATE_FORMAT_PROPERTY
                                  <li>DATE_JOB_BECAME_ACTIVE_PROPERTY
                                  <li>DATE_JOB_ENTERED_SYSTEM_PROPERTY
                                  <li>DATE_JOB_SCHEDULE_TO_RUN_PROPERTY
                                  <li>DATE_PROPERTY
                                  <li>DATE_PUT_ON_JOB_QUEUE_PROPERTY
                                  <li>DATE_SEPARATOR_PROPERTY
                                  <li>DDM_CONVERSATION_HANDLING_PROPERTY
                                  <li>DECIMAL_FORMAT_PROPERTY
                                  <li>DEFAULT_CCSID_PROPERTY
                                  <li>DESCRIPTION_PROPERTY
                                  <li>DEVICE_RECOVERY_ACTION_PROPERTY
                                  <li>END_SEVERITY_PROPERTY
                                  <li>FUNCTION_PROPERTY
                                  <li>INQUIRY_MESSAGE_REPLY_PROPERTY
                                  <li>JOB_ACCOUNTING_CODE_PROPERTY
                                  <li>JOB_DATE_PROPERTY
                                  <li>JOB_DESCRIPTION_PROPERTY
                                  <li>JOB_MESSAGE_QUEUE_FULL_ACTION_PROPERTY
                                  <li>JOB_MESSAGE_QUEUE_MAXIMUM_SIZE_PROPERTY
                                  <li>JOB_QUEUE_PRIORITY_PROPERTY
                                  <li>JOB_QUEUE_PROPERTY
                                  <li>JOB_SWITCHES_PROPERTY
                                  <li>LANGUAGE_ID_PROPERTY
                                  <li>LOGGING_CL_PROGRAMS_PROPERTY
                                  <li>LOGGING_LEVEL_PROPERTY
                                  <li>LOGGING_SEVERITY_PROPERTY
                                  <li>LOGGING_TEXT_PROPERTY
                                  <li>MODE_NAME_PROPERTY
                                  <li>NAME_PROPERTY
                                  <li>NUMBER_PROPERTY
                                  <li>NUMBER_OF_LIBRARIES_IN_SYSLIBL_PROPERTY
                                  <li>NUMBER_OF_LIBRARIES_IN_USRLIBL_PROPERTY
                                  <li>NUMBER_OF_PRODUCT_LIBRARIES_PROPERTY
                                  <li>OUTPUT_QUEUE_PRIORITY_PROPERTY
                                  <li>OUTPUT_QUEUE_PROPERTY
                                  <li>PRINT_KEY_FORMAT_PROPERTY
                                  <li>PRINT_TEXT_PROPERTY
                                  <li>PRINTER_DEVICE_NAME_PROPERTY
                                  <li>PRODUCT_LIBRARIES_PROPERTY
                                  <li>ROUTING_DATA_PROPERTY
                                  <li>SIGNED_ON_JOB_PROPERTY
                                  <li>SORT_SEQUENCE_TABLE_PROPERTY
                                  <li>STATUS_MESSAGE_HANDLING_PROPERTY
                                  <li>STATUS_OF_JOB_ON_JOB_QUEUE_PROPERTY
                                  <li>STATUS_PROPERTY
                                  <li>SUBSYSTEM_PROPERTY
                                  <li>SUBTYPE_PROPERTY
                                  <li>SYSTEM_LIBRARY_LIST_PROPERTY
                                  <li>TIME_SEPARATOR_PROPERTY
                                  <li>TYPE_PROPERTY
                                  <li>WORK_ID_UNIT_PROPERTY
                                  <li>USER_LIBRARY_LIST_PROPERTY
                                  <li>USER_PROPERTY
                                </ul>
@return                         The property value, or null if the
                                property identifier is not recognized.
**/
    public Object getPropertyValue (Object propertyIdentifier)
    {
        // Get the name.
        if (propertyIdentifier == NAME_PROPERTY)
            return this;

        // Get the description.
        else if (propertyIdentifier == DESCRIPTION_PROPERTY)            
            return description_;

        else if (job_ == null)
            return "";

        else if (propertyIdentifier == FUNCTION_PROPERTY) {
            try {
                StringBuffer buffer = new StringBuffer();
                buffer.append(job_.getAttributeValue(RJob.FUNCTION_TYPE));
                buffer.append('-');
                buffer.append(job_.getAttributeValue(RJob.FUNCTION_NAME));
                return buffer.toString();
            }
            catch(Exception e) {
                errorEventSupport_.fireError(e);
                return "";
            }
        }

        else if (propertyIdentifier == TYPE_PROPERTY) {
            char type,subtype;
            try {
                String typeAsString = ((String)job_.getAttributeValue(RJob.JOB_TYPE)).trim().toUpperCase();
                if (typeAsString.length()!=0)
                    type = typeAsString.charAt(0);
                else 
                    type = ' ';
                String subtypeAsString = ((String)job_.getAttributeValue(RJob.JOB_SUBTYPE)).trim().toUpperCase();
                if (subtypeAsString.length()!=0)
                    subtype = subtypeAsString.charAt(0);
                else 
                    subtype = ' ';
            }
            catch(Exception e) {
                errorEventSupport_.fireError(e);
                type = ' ';
                subtype = ' ';
            }
            return getJobType(type,subtype);
        }

        else if (propertyIdentifier == null)
            return null;

        else {
            Object attributeID = map_.get(propertyIdentifier);
            if (attributeID == null)
                return null;
            try {
                return job_.getAttributeValue(attributeID);
            }
            catch(Exception e) {
                errorEventSupport_.fireError(e);
                return "";
            }
        }
    }



/**
Returns the AS/400 system on which the job resides.

@return The AS/400 system on which the job resides.
**/
    public AS400 getSystem ()
    {
        return jobLog_.getSystem ();
    }



    //@A1A separate name from full queue path.
    private String getQueueName(String fullQueuePath)
    {
        if (fullQueuePath.trim().equals("/"))
            return "";
        int separator = fullQueuePath.indexOf('/');
        if (separator > 0)
            return fullQueuePath.substring(separator+1);
        else 
            return fullQueuePath;
    }



/**
Returns the text.  This is the job name.

@return The text which is the job name.
**/
    public String getText ()
    {
        if (job_ != null)
            return job_.getName();
        else
            return "";
    }



/**
Initializes the transient data.
**/
    private void initializeTransient ()
    {
        //@A3C: Uncommented the event support section.
        // Initialize the event support.
        errorEventSupport_    = new ErrorEventSupport(this);
        propertyChangeSupport_= new PropertyChangeSupport(this);
        vetoableChangeSupport_= new VetoableChangeSupport(this);
        objectEventSupport_   = new VObjectEventSupport(this);
        workingEventSupport_  = new WorkingEventSupport(this);

        // Initialize the private data.
        detailsChildren_        = new VObject[0];
        loaded_ = false; //@A2A

        // Initialize the properties pane.
        propertiesPane_ = new VResourcePropertiesPane(this, job_, properties_);
        propertiesPane_.addErrorListener (errorEventSupport_);
        propertiesPane_.addVObjectListener (objectEventSupport_);
        propertiesPane_.addWorkingListener (workingEventSupport_);

        // This is no longer needed, since the properties pane is editable.
/*        actions_ = new VAction[1];
        actions_[0]= new VJobModifyAction(this,job_);
        actions_[0].addErrorListener((ErrorListener)errorEventSupport_);
        actions_[0].addVObjectListener((VObjectListener)objectEventSupport_);
        actions_[0].addWorkingListener((WorkingListener)workingEventSupport_);*/
    }



/**
Indicates if the node is a leaf.

@return  Always true.
**/
    public boolean isLeaf ()
    {
        return true;
    }



/**
Indicates if the details children are sortable.

@return Always false.
**/
//
// Implementation note: We do not allow sorting because of the fact
// that we load the list incrementally.
//
    public boolean isSortable ()
    {
        return false;
    }



/**
Loads information about the object from the AS/400.
**/
    public void load()
    {
        // Stop listening to the previous children.
        if (detailsChildren_ != null) {
            for (int i = 0; i < detailsChildren_.length; ++i) { //@A2C
                if (detailsChildren_[i] != null) {
                    detailsChildren_[i].removeErrorListener (errorEventSupport_);
                    detailsChildren_[i].removeVObjectListener (objectEventSupport_);
                    detailsChildren_[i].removeWorkingListener (workingEventSupport_);
                }
            }
        }

        detailsChildren_ = null;
        loaded_ = true; //@A2A
    }



    private void loadFirst()
    {
        workingEventSupport_.fireStartWorking ();
        
        try {
            jobLog_.open();
            jobLog_.waitForComplete();
            detailsChildren_ = new VJobLogMessage[(int)jobLog_.getListLength()];
        } 
        catch (Exception e)  {
            errorEventSupport_.fireError(e);
            detailsChildren_ = new VJobLogMessage[0];
        }

        workingEventSupport_.fireStopWorking ();
    }



    private void loadSingle(int index)
    {
        workingEventSupport_.fireStartWorking ();
        
        try {
            RQueuedMessage message = (RQueuedMessage)jobLog_.resourceAt(index);
            detailsChildren_[index] = new VJobLogMessage (message);
            detailsChildren_[index].addErrorListener (errorEventSupport_);
            detailsChildren_[index].addVObjectListener (objectEventSupport_);
            detailsChildren_[index].addWorkingListener (workingEventSupport_);
        } 
        catch (Exception e)  {
            errorEventSupport_.fireError(e);
        }

        workingEventSupport_.fireStopWorking ();
    }



/**
Restores the state of the object from an input stream.
This is used when deserializing an object.

@param in   The input stream.
**/
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject ();
        initializeTransient ();
    }




/**
Removes an error listener.

@param  listener    The listener.
**/
    public void removeErrorListener (ErrorListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        errorEventSupport_.removeErrorListener (listener);
    }



/**
Removes a property change listener.

@param  listener  The listener.
**/
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        propertyChangeSupport_.removePropertyChangeListener (listener);
    }



/**
Removes a vetoable change listener.

@param  listener  The listener.
**/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        vetoableChangeSupport_.removeVetoableChangeListener (listener);
    }



/**
Removes a VObjectListener.

@param  listener    The listener.
**/
    public void removeVObjectListener (VObjectListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        objectEventSupport_.removeVObjectListener (listener);
    }



/**
Removes a working listener.

@param  listener    The listener.
**/
    public void removeWorkingListener (WorkingListener listener)
    {
      if (listener == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
          throw new NullPointerException("listener");
      }
        workingEventSupport_.removeWorkingListener (listener);
    }



/**
Sets the job.

@param job The job.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setJob (Job job)
        throws PropertyVetoException
    {
        if (job == null)
            throw new NullPointerException ("job");

        Job oldValue = getJob();
        Job newValue = job;
        vetoableChangeSupport_.fireVetoableChange ("job", oldValue, newValue);

        if (oldValue != newValue) {

            job_ = new RJob(job.getSystem(), job.getName(), job.getUser(), job.getNumber());
            jobLog_ = new RJobLog(job.getSystem(), job.getName(), job.getUser(), job.getNumber());
        }

        propertyChangeSupport_.firePropertyChange ("job", oldValue, newValue);
    }



/**
Sets the job.

@param job The job.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setRJob (RJob job)
        throws PropertyVetoException
    {
        if (job == null)
            throw new NullPointerException ("job");

        RJob oldValue = job_;
        RJob newValue = job;
        vetoableChangeSupport_.fireVetoableChange ("job", oldValue, newValue);

        if (oldValue != newValue) {

            job_ = job;

            try {
                jobLog_.setName (job.getName ());
                jobLog_.setNumber (job.getNumber ());
                jobLog_.setUser (job.getUser ());
            }
            catch (PropertyVetoException e) {
                // Ignore.
                Trace.log(Trace.ERROR,"VJob setJob : "+e);
            }
        }

        propertyChangeSupport_.firePropertyChange ("job", oldValue, newValue);
    }



/**
Sets the AS/400 on which the job resides.

@param system The AS/400 on which the job resides.

@exception PropertyVetoException If the change is vetoed.
**/
    public void setSystem (AS400 system)
        throws PropertyVetoException
    {
        if (system == null)
            throw new NullPointerException ("system");

        AS400 oldValue = jobLog_.getSystem ();
        AS400 newValue = system;
        vetoableChangeSupport_.fireVetoableChange ("system", oldValue, newValue);

        if (oldValue != newValue) {

            try {
                jobLog_.setSystem (system);
            }
            catch (PropertyVetoException e) {
                // Ignore.
                Trace.log(Trace.ERROR,"VJob setSystem : "+e);
            }
        }

        propertyChangeSupport_.firePropertyChange ("system", oldValue, newValue);
    }



/**
Sorts the children for the details.  Since sorting is not supported,
this method does nothing.

@param  propertyIdentifiers The property identifiers.
@param  orders              The sort orders for each property
                            identifier. true for ascending order;
                            false for descending order.
**/
    public void sortDetailsChildren (Object[] propertyIdentifiers,
                                                  boolean[] orders)
    {
      // No sorting here!
    }



/**
Returns the string representation of the job name.

@return The string representation of the job name.
**/
    public String toString ()
    {
        return getText ();
    }
}
