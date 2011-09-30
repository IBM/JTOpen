///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RJob.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.resource;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Calendar;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.Trace;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;



/**
The RJob class represents a server job.  In order to access a job,
the system and either the job name, user name, and job number or
internal job identifier need to be set.  A valid combination of
these must be set by getting or setting any of the job's attributes.

<a name="default">
<p>If you do not specify any of the job name, user name, job number,
or internal job identifier properties, the default job is used.
The default job is the host server job for remote program calls.
</a>

<a name="attributeIDs"><p>The following attribute IDs are supported:
<ul>
<li>{@link #ACCOUNTING_CODE ACCOUNTING_CODE}
<li>{@link #ACTIVE_JOB_STATUS ACTIVE_JOB_STATUS}
<li>{@link #AUXILIARY_IO_REQUESTS AUXILIARY_IO_REQUESTS}
<li>{@link #BREAK_MESSAGE_HANDLING BREAK_MESSAGE_HANDLING}
<li>{@link #CCSID CCSID}
<li>{@link #COMPLETION_STATUS COMPLETION_STATUS}
<li>{@link #CONTROLLED_END_REQUESTED CONTROLLED_END_REQUESTED}
<li>{@link #COUNTRY_ID COUNTRY_ID}
<li>{@link #CPU_TIME_USED CPU_TIME_USED}
<li>{@link #CURRENT_LIBRARY CURRENT_LIBRARY}
<li>{@link #CURRENT_LIBRARY_EXISTENCE CURRENT_LIBRARY_EXISTENCE}
<li>{@link #CURRENT_SYSTEM_POOL_ID CURRENT_SYSTEM_POOL_ID}
<li>{@link #CURRENT_USER CURRENT_USER}
<li>{@link #DATE_ENTERED_SYSTEM DATE_ENTERED_SYSTEM}
<li>{@link #DATE_FORMAT DATE_FORMAT}
<li>{@link #DATE_SEPARATOR DATE_SEPARATOR}
<li>{@link #DATE_STARTED DATE_STARTED}
<li>{@link #DBCS_CAPABLE DBCS_CAPABLE}
<li>{@link #DECIMAL_FORMAT DECIMAL_FORMAT}
<li>{@link #DEFAULT_CCSID DEFAULT_CCSID}
<li>{@link #DEFAULT_WAIT_TIME DEFAULT_WAIT_TIME}
<li>{@link #DEVICE_RECOVERY_ACTION DEVICE_RECOVERY_ACTION}
<li>{@link #ELIGIBLE_FOR_PURGE ELIGIBLE_FOR_PURGE}
<li>{@link #END_SEVERITY END_SEVERITY}
<li>{@link #FUNCTION_NAME FUNCTION_NAME}
<li>{@link #FUNCTION_TYPE FUNCTION_TYPE}
<li>{@link #INQUIRY_MESSAGE_REPLY INQUIRY_MESSAGE_REPLY}
<li>{@link #INSTANCE INSTANCE}
<li>{@link #INTERACTIVE_TRANSACTIONS INTERACTIVE_TRANSACTIONS}
<li>{@link #INTERNAL_JOB_ID INTERNAL_JOB_ID}
<li>{@link #JOB_DATE JOB_DATE}
<li>{@link #JOB_DESCRIPTION JOB_DESCRIPTION}
<li>{@link #JOB_NAME JOB_NAME}
<li>{@link #JOB_NUMBER JOB_NUMBER}
<li>{@link #JOB_QUEUE JOB_QUEUE}
<li>{@link #JOB_QUEUE_DATE JOB_QUEUE_DATE}
<li>{@link #JOB_QUEUE_PRIORITY JOB_QUEUE_PRIORITY}
<li>{@link #JOB_QUEUE_STATUS JOB_QUEUE_STATUS}
<li>{@link #JOB_STATUS JOB_STATUS}
<li>{@link #JOB_SUBTYPE JOB_SUBTYPE}
<li>{@link #JOB_SWITCHES JOB_SWITCHES}
<li>{@link #JOB_TYPE JOB_TYPE}
<li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE KEEP_DDM_CONNECTIONS_ACTIVE}
<li>{@link #LANGUAGE_ID LANGUAGE_ID}
<li>{@link #LOCATION_NAME LOCATION_NAME}
<li>{@link #LOG_CL_PROGRAMS LOG_CL_PROGRAMS}
<li>{@link #LOGGING_LEVEL LOGGING_LEVEL}
<li>{@link #LOGGING_SEVERITY LOGGING_SEVERITY}
<li>{@link #LOGGING_TEXT LOGGING_TEXT}
<li>{@link #MAX_CPU_TIME MAX_CPU_TIME}
<li>{@link #MAX_TEMP_STORAGE MAX_TEMP_STORAGE}
<li>{@link #MESSAGE_QUEUE_ACTION MESSAGE_QUEUE_ACTION}
<li>{@link #MESSAGE_QUEUE_MAX_SIZE MESSAGE_QUEUE_MAX_SIZE}
<li>{@link #MODE MODE}
<li>{@link #NETWORK_ID NETWORK_ID}
<li>{@link #OUTPUT_QUEUE OUTPUT_QUEUE}
<li>{@link #OUTPUT_QUEUE_PRIORITY OUTPUT_QUEUE_PRIORITY}
<li>{@link #PRINT_KEY_FORMAT PRINT_KEY_FORMAT}
<li>{@link #PRINT_TEXT PRINT_TEXT}
<li>{@link #PRINTER_DEVICE_NAME PRINTER_DEVICE_NAME}
<li>{@link #PRODUCT_LIBRARIES PRODUCT_LIBRARIES}
<li>{@link #PRODUCT_RETURN_CODE PRODUCT_RETURN_CODE}
<li>{@link #PROGRAM_RETURN_CODE PROGRAM_RETURN_CODE}
<li>{@link #ROUTING_DATA ROUTING_DATA}
<li>{@link #RUN_PRIORITY RUN_PRIORITY}
<li>{@link #SCHEDULE_DATE SCHEDULE_DATE}
<li>{@link #SEQUENCE_NUMBER SEQUENCE_NUMBER}
<li>{@link #SERVER_TYPE SERVER_TYPE}
<li>{@link #SIGNED_ON_JOB SIGNED_ON_JOB}
<li>{@link #SORT_SEQUENCE_TABLE SORT_SEQUENCE_TABLE}
<li>{@link #SPECIAL_ENVIRONMENT SPECIAL_ENVIRONMENT}
<li>{@link #STATUS_MESSAGE_HANDLING STATUS_MESSAGE_HANDLING}
<li>{@link #SUBMITTED_BY_JOB_NAME SUBMITTED_BY_JOB_NAME}
<li>{@link #SUBMITTED_BY_JOB_NUMBER SUBMITTED_BY_JOB_NUMBER}
<li>{@link #SUBMITTED_BY_USER SUBMITTED_BY_USER}
<li>{@link #SUBSYSTEM SUBSYSTEM}
<li>{@link #SYSTEM_POOL_ID SYSTEM_POOL_ID}
<li>{@link #SYSTEM_LIBRARY_LIST SYSTEM_LIBRARY_LIST}
<li>{@link #TEMP_STORAGE_USED TEMP_STORAGE_USED}
<li>{@link #THREAD_COUNT THREAD_COUNT}
<li>{@link #TIME_SEPARATOR TIME_SEPARATOR}
<li>{@link #TIME_SLICE TIME_SLICE}
<li>{@link #TIME_SLICE_END_POOL TIME_SLICE_END_POOL}
<li>{@link #TOTAL_RESPONSE_TIME TOTAL_RESPONSE_TIME}
<li>{@link #USER_LIBRARY_LIST USER_LIBRARY_LIST}
<li>{@link #USER_NAME USER_NAME}
<li>{@link #USER_RETURN_CODE USER_RETURN_CODE}
</ul>
</a>

<p>Use any of these attribute IDs with
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue(java.lang.Object) getAttributeValue()}
and {@link com.ibm.as400.resource.ChangeableResource#setAttributeValue(java.lang.Object, java.lang.Object) setAttributeValue()}
to access the attribute values for an RJob.

<blockquote><pre>
// Create an RJob object to refer to a specific job.
AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
RJob job = new RJob(system, "AJOBNAME", "AUSERID", "AJOBNUMBER");
<br>
// Get the job subtype.
String jobSubtype = (String)job.getAttributeValue(RJob.JOB_SUBTYPE);
<br>
// Set the date format for a job to Julian.
job.setAttributeValue(RJob.DATE_FORMAT, RJob.DATE_FORMAT_JULIAN);
<br>
// Commit the attribute change.
job.commitAttributeChanges();
</pre></blockquote>

@deprecated Use
{@link com.ibm.as400.access.Job Job} instead, as this package may be removed in the future.
@see RJobList
**/
public class RJob
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    static final long serialVersionUID = 4L;


//-----------------------------------------------------------------------------------------
// Presentation.
//-----------------------------------------------------------------------------------------

    private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.resource.ResourceMRI");
    private static final String         ICON_BASE_NAME_     = "RJob";
    private static final String         PRESENTATION_KEY_   = "JOB";



//-----------------------------------------------------------------------------------------
// Attribute values.
//-----------------------------------------------------------------------------------------

/**
Attribute value for system value.
**/
    public static final String SYSTEM_VALUE    = "*SYSVAL";

/**
Attribute value for user profile.
**/
    public static final String USER_PROFILE    = "*USRPRF";

/**
Attribute value for yes.
**/
    public static final String YES             = "*YES";

/**
Attribute value for no.
**/
    public static final String NO              = "*NO";

/**
Attribute value for none.
**/
    public static final String NONE            = "*NONE";

/**
Attribute value for no date.
**/
    public static final Date NO_DATE           = DateValueMap.NO_DATE;


//-----------------------------------------------------------------------------------------
// Attribute IDs.
//
// * If you add an attribute here, make sure and add it to the class javadoc.
//-----------------------------------------------------------------------------------------

    // Private data.
            static ResourceMetaDataTable        attributes_             = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);
    private static ProgramMap                   getterMap_              = new ProgramMap();
    private static ProgramKeys                  setterKeys_             = new ProgramKeys();

    private static ArrayTypeValueMap            arrayTypeValueMapString_= new ArrayTypeValueMap(String.class);
    private static IntegerValueMap              integerValueMap_        = new IntegerValueMap();
    private static DateValueMap                 dateValueMap7_          = new DateValueMap(DateValueMap.FORMAT_6);
    private static DateValueMap                 dateValueMap6_          = new DateValueMap(DateValueMap.FORMAT_7);
    private static DateValueMap                 dateValueMap13_         = new DateValueMap(DateValueMap.FORMAT_13);
    private static DateValueMap                 dateValueMapDts_        = new DateValueMap(DateValueMap.FORMAT_DTS);

    private static final String                 JOBI0100_               = "qusrjobi_jobi0100";
    private static final String                 JOBI0150_               = "qusrjobi_jobi0150";
    private static final String                 JOBI0200_               = "qusrjobi_jobi0200";
    private static final String                 JOBI0300_               = "qusrjobi_jobi0300";
    private static final String                 JOBI0400_               = "qusrjobi_jobi0400";
    private static final String                 JOBI0500_               = "qusrjobi_jobi0500";
    private static final String                 JOBI0600_               = "qusrjobi_jobi0600";
    private static final String                 JOBI0700_               = "qusrjobi_jobi0700";
    private static final String                 JOBI0800_               = "qusrjobi_jobi0800";
    private static final String                 JOBI0900_               = "qusrjobi_jobi0900";



/**
Attribute ID for accounting code.  This identifies a String attribute, which
represents the identifier assigned to the job by the system to collect resource
use information for the job when job accounting is active.
**/
    public static final String ACCOUNTING_CODE                          = "ACCOUNTING_CODE";

    static {
        attributes_.add(ACCOUNTING_CODE, String.class, false);
        getterMap_.add(ACCOUNTING_CODE, JOBI0400_, "receiverVariable.jobAccountingCode");
        setterKeys_.add(ACCOUNTING_CODE, 1001, ProgramKeys.CHAR, 15);
    }



/**
Attribute ID for active job status.  This identifies a read-only String attribute,
which represents the active status of the initial thread.
**/
    public static final String ACTIVE_JOB_STATUS                        = "ACTIVE_JOB_STATUS";

    static {
        attributes_.add(ACTIVE_JOB_STATUS, String.class, true);
        getterMap_.add(ACTIVE_JOB_STATUS, JOBI0200_, "receiverVariable.activeJobStatus");
    }



/**
Attribute ID for auxiliary I/O requests.  This identifies a read-only Integer attribute,
which represents the number of auxiliary I/O requests for the initial thread of the job.
**/
    public static final String AUXILIARY_IO_REQUESTS                    = "AUXILIARY_IO_REQUESTS";

    static {
        attributes_.add(AUXILIARY_IO_REQUESTS, Integer.class, true);
        getterMap_.add(AUXILIARY_IO_REQUESTS, JOBI0200_, "receiverVariable.numberOfAuxiliaryIORequests");
    }



/**
Attribute ID for break message handling.  This identifies a String attribute,
which represents how this job handles break messages.  Possible values are:
<ul>
<li>{@link #BREAK_MESSAGE_HANDLING_NORMAL BREAK_MESSAGE_HANDLING_NORMAL}
    - The message queue status determines break message handling.
<li>{@link #BREAK_MESSAGE_HANDLING_HOLD BREAK_MESSAGE_HANDLING_HOLD}
    - The message queue holds break messages until a user or program
      requests them.
<li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY BREAK_MESSAGE_HANDLING_NOTIFY}
    - The system notifies the job's message queue when a message
      arrives.
</ul>
**/
    public static final String BREAK_MESSAGE_HANDLING                   = "BREAK_MESSAGE_HANDLING";

    /**
    Attribute value indicating that the message queue status determines break message handling.

    @see #BREAK_MESSAGE_HANDLING
    **/
    public static final String BREAK_MESSAGE_HANDLING_NORMAL            = "*NORMAL";

    /**
    Attribute value indicating that the message queue holds break messages until a user or program
    requests them.

    @see #BREAK_MESSAGE_HANDLING
    **/
    public static final String BREAK_MESSAGE_HANDLING_HOLD              = "*HOLD";

    /**
    Attribute value indicating that the system notifies the job's message queue when a message
    arrives.

    @see #BREAK_MESSAGE_HANDLING
    **/
    public static final String BREAK_MESSAGE_HANDLING_NOTIFY            = "*NOTIFY";

    static {
        attributes_.add(BREAK_MESSAGE_HANDLING, String.class, false,
                        new Object[] {BREAK_MESSAGE_HANDLING_NORMAL,
                            BREAK_MESSAGE_HANDLING_HOLD,
                            BREAK_MESSAGE_HANDLING_NOTIFY}, null, true);
        getterMap_.add(BREAK_MESSAGE_HANDLING, JOBI0400_, "receiverVariable.breakMessageHandling");
        setterKeys_.add(BREAK_MESSAGE_HANDLING, 201, ProgramKeys.CHAR, 10);
    }



/**
Attribute ID for coded character set identifier (CCSID).
This identifies an Integer attribute.

<p>The following special values can be used when setting the CCSID:
<ul>
<li>{@link #CCSID_SYSTEM_VALUE CCSID_SYSTEM_VALUE} - The CCSID specified
    in the system value QCCSID is used.
<li>{@link #CCSID_INITIAL_USER CCSID_INITIAL_USER} - The CCSID specified
    in the user profile under which this thread was initially running is
    used.
</ul>
**/
    public static final String CCSID                                    = "CCSID";

    /**
    Attribute value indicating that the CCSID specified
    in the system value QCCSID is used.

    @see #CCSID
    **/
    public static final int CCSID_SYSTEM_VALUE                          = -1;

    /**
    Attribute value indicating that the CCSID specified
    in the user profile under which this thread was initially running is
    used.

    @see #CCSID
    **/
    public static final int CCSID_INITIAL_USER                          = -2;

    static {
        attributes_.add(CCSID, Integer.class, false,
                        new Object[] {new Integer(CCSID_SYSTEM_VALUE),
                                     new Integer(CCSID_INITIAL_USER)}, null, false);
        getterMap_.add(CCSID, JOBI0400_, "receiverVariable.codedCharacterSetID");
        setterKeys_.add(CCSID, 302, ProgramKeys.BINARY);
    }



/**
Attribute ID for completion status.  This identifies a read-only String attribute,
which represents the completion status of the job.  Possible values are:
<ul>
<li>{@link #COMPLETION_STATUS_NOT_COMPLETED COMPLETION_STATUS_NOT_COMPLETED}
    - The job has not completed.
<li>{@link #COMPLETION_STATUS_COMPLETED_NORMALLY COMPLETION_STATUS_COMPLETED_NORMALLY}
    - The job completed normally.
<li>{@link #COMPLETION_STATUS_COMPLETED_ABNORMALLY COMPLETION_STATUS_COMPLETED_ABNORMALLY}
    - The job completed abnormally.
</ul>
**/
    public static final String COMPLETION_STATUS                 = "COMPLETION_STATUS";

    /**
    Attribute value indicating that the job has not completed.

    @see #COMPLETION_STATUS
    **/
    public static final String COMPLETION_STATUS_NOT_COMPLETED              = "";

    /**
    Attribute value indicating that the job completed normally.

    @see #COMPLETION_STATUS
    **/
    public static final String COMPLETION_STATUS_COMPLETED_NORMALLY         = "0";

    /**
    Attribute value indicating that the job completed abnormally.

    @see #COMPLETION_STATUS
    **/
    public static final String COMPLETION_STATUS_COMPLETED_ABNORMALLY       = "1";

    static {
        attributes_.add(COMPLETION_STATUS, String.class, true,
                        new Object[] {COMPLETION_STATUS_NOT_COMPLETED,
                            COMPLETION_STATUS_COMPLETED_NORMALLY,
                            COMPLETION_STATUS_COMPLETED_ABNORMALLY}, null, true);
        getterMap_.add(COMPLETION_STATUS, JOBI0400_, "receiverVariable.completionStatus");
    }



/**
Attribute ID for controlled end requested.  This identifies a read-only String attribute,
which indicates whether or not the system issued a controlled cancelation.  Possible
values are:
<ul>
<li>{@link #CONTROLLED_END_REQUESTED_CANCELED CONTROLLED_END_REQUESTED_CANCELED}
    - The system, the subsystem in which the job is running,
      or the job itself is canceled.
<li>{@link #CONTROLLED_END_REQUESTED_NOT_CANCELED CONTROLLED_END_REQUESTED_NOT_CANCELED}
    - The system, subsystem, or job is not canceled.
<li>{@link #CONTROLLED_END_REQUESTED_NOT_RUNNING CONTROLLED_END_REQUESTED_NOT_RUNNING}
    - The job is not running.
</ul>
**/
    public static final String CONTROLLED_END_REQUESTED                 = "CONTROLLED_END_REQUESTED";

    /**
    Attribute value indicating that the system, the subsystem in which the job is running,
    or the job itself is canceled.

    @see #CONTROLLED_END_REQUESTED
    **/
    public static final String CONTROLLED_END_REQUESTED_CANCELED        = "1";

    /**
    Attribute value indicating that the system, subsystem, or job is not canceled.

    @see #CONTROLLED_END_REQUESTED
    **/
    public static final String CONTROLLED_END_REQUESTED_NOT_CANCELED    = "0";

    /**
    Attribute value indicating that the job is not running.

    @see #CONTROLLED_END_REQUESTED
    **/
    public static final String CONTROLLED_END_REQUESTED_NOT_RUNNING     = "";

    static {
        attributes_.add(CONTROLLED_END_REQUESTED, String.class, true,
                        new Object[] {CONTROLLED_END_REQUESTED_CANCELED,
                            CONTROLLED_END_REQUESTED_NOT_CANCELED,
                            CONTROLLED_END_REQUESTED_NOT_RUNNING}, null, true);
        getterMap_.add(CONTROLLED_END_REQUESTED, JOBI0600_, "receiverVariable.endStatus");
    }



/**
Attribute ID for country ID.  This identifies a String attribute.

<p>The following special values can be used when setting the country ID:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QCNTRYID is used.
<li>{@link #USER_PROFILE USER_PROFILE} - The
    country ID specified in the user profile under which this thread
    was initially running is used.
</ul>
**/
    public static final String COUNTRY_ID                               = "COUNTRY_ID";

    static {
        attributes_.add(COUNTRY_ID, String.class, false,
                        new Object[] {SYSTEM_VALUE, USER_PROFILE}, null, false);
        getterMap_.add(COUNTRY_ID, JOBI0400_, "receiverVariable.countryID");
        setterKeys_.add(COUNTRY_ID, 303, ProgramKeys.CHAR, 10);
    }



/**
Attribute ID for CPU time used.  This identifies a read-only Integer attribute,
which represents the amount of processing unit time (in milliseconds) that the
job used.
**/
    public static final String CPU_TIME_USED                            = "CPU_TIME_USED";

    static {
        attributes_.add(CPU_TIME_USED, Integer.class, true);
        getterMap_.add(CPU_TIME_USED, JOBI0150_, "receiverVariable.processingUnitTimeUsed");
        getterMap_.add(CPU_TIME_USED, JOBI0200_, "receiverVariable.processingUnitTimeUsed");
    }



/**
Attribute ID for current library.  This identifies a read-only String
attribute, which represents the name of the current library
for the initial thread of the job.
**/
    public static final String CURRENT_LIBRARY                        = "CURRENT_LIBRARY";

    static {
        attributes_.add(CURRENT_LIBRARY, String.class, true);
        getterMap_.add(CURRENT_LIBRARY, JOBI0700_, "receiverVariable.currentLibrary", "receiverVariable.currentLibraryExistence", new CurrentLibraryValueMap_());
    }

    private static class CurrentLibraryValueMap_ extends AbstractValueMap
    {
        public Object ptol(Object physicalValue)
        {
            String[] logicalValue = (String[])arrayTypeValueMapString_.ptol(physicalValue);
            if (logicalValue.length == 0)
                return "";
            else
                return logicalValue[0];
        }
    }



/**
Attribute ID for current library existence.  This identifies a
read-only Boolean attribute, which indicates if a current library
exists.
**/
    public static final String CURRENT_LIBRARY_EXISTENCE                        = "CURRENT_LIBRARY_EXISTENCE";

    static {
        attributes_.add(CURRENT_LIBRARY_EXISTENCE, Boolean.class, true);
        getterMap_.add(CURRENT_LIBRARY_EXISTENCE, JOBI0700_, "receiverVariable.currentLibraryExistence", new BooleanValueMap(new Integer(0), new Integer(1)));
    }



/**
Attribute ID for current system pool ID.  This identifies a read-only
Integer attribute, which represents the identifier of the system-related
pool from which main storage is currently being allocated for the job's
initial thread.
**/
    public static final String CURRENT_SYSTEM_POOL_ID                   = "CURRENT_SYSTEM_POOL_ID";

    static {
        attributes_.add(CURRENT_SYSTEM_POOL_ID, Integer.class, true);
        getterMap_.add(CURRENT_SYSTEM_POOL_ID, JOBI0200_, "receiverVariable.currentSystemPoolIdentifier");
    }



/**
Attribute ID for current user.  This identifies a read-only String
attribute, which represents the user profile under which the initial
thread of the current job is running.
**/
    public static final String CURRENT_USER                             = "CURRENT_USER";

    static {
        attributes_.add(CURRENT_USER, String.class, true);
        getterMap_.add(CURRENT_USER, JOBI0600_, "receiverVariable.currentUserProfile");
    }



/**
Attribute ID for date entered system.  This identifies a read-only
Date attribute, which represents the date and time when the job was
placed on the system.  The Date value is converted using the default Java locale.
**/
    public static final String DATE_ENTERED_SYSTEM                      = "DATE_ENTERED_SYSTEM";

    static {
        attributes_.add(DATE_ENTERED_SYSTEM, Date.class, true);
        getterMap_.add(DATE_ENTERED_SYSTEM, JOBI0400_, "receiverVariable.dateAndTimeJobEnteredSystem", dateValueMap13_);
    }



/**
Attribute ID for date format.  This identifies a String attribute, which
represents the format in which dates are presented.  Possible values are:
<ul>
<li>{@link #DATE_FORMAT_YMD DATE_FORMAT_YMD} - Year, month, and day format.
<li>{@link #DATE_FORMAT_MDY DATE_FORMAT_MDY} - Month, day, and year format.
<li>{@link #DATE_FORMAT_DMY DATE_FORMAT_DMY} - Day, month, and year format.
<li>{@link #DATE_FORMAT_JULIAN DATE_FORMAT_JULIAN} - Julian format (year and day).
</ul>

<p>The following special values can be used when setting the date format:
<ul>
<li>{@link #DATE_FORMAT_SYSTEM_VALUE DATE_FORMAT_SYSTEM_VALUE} - The
    system value QDATFMT is used.
</ul>
**/
    public static final String DATE_FORMAT                              = "DATE_FORMAT";

    /**
    Attribute value indicating that the system value QDATFMT is used.

    @see #DATE_FORMAT
    **/
    public static final String DATE_FORMAT_SYSTEM_VALUE                 = "*SYS";

    /**
    Attribute value indicating the year, month, and day date format.

    @see #DATE_FORMAT
    **/
    public static final String DATE_FORMAT_YMD                          = "*YMD";

    /**
    Attribute value indicating the month, day, and year date format.

    @see #DATE_FORMAT
    **/
    public static final String DATE_FORMAT_MDY                          = "*MDY";

    /**
    Attribute value indicating the day, month, and year date format.

    @see #DATE_FORMAT
    **/
    public static final String DATE_FORMAT_DMY                          = "*DMY";

    /**
    Attribute value indicating the Julian date format.

    @see #DATE_FORMAT
    **/
    public static final String DATE_FORMAT_JULIAN                       = "*JUL";

    static {
        attributes_.add(DATE_FORMAT, String.class, false,
                        new Object[] { DATE_FORMAT_YMD,
                            DATE_FORMAT_MDY,
                            DATE_FORMAT_DMY,
                            DATE_FORMAT_JULIAN,
                            DATE_FORMAT_SYSTEM_VALUE }, null, true);
        getterMap_.add(DATE_FORMAT, JOBI0400_, "receiverVariable.dateFormat");
        setterKeys_.add(DATE_FORMAT, 405, ProgramKeys.CHAR, 4);
    }



/**
Attribute ID for date separator.  This identifies a String attribute, which
represents the value used to separate days, months, and years when presenting
a date.

<p>The following special value can be used when setting the date separator:
<ul>
<li>{@link #DATE_SEPARATOR_SYSTEM_VALUE DATE_SEPARATOR_SYSTEM_VALUE} - The
    system value QDATSEP is used.
</ul>
**/
    public static final String DATE_SEPARATOR                           = "DATE_SEPARATOR";

    /**
    Attribute value indicating that the system value QDATSEP is used.

    @see #DATE_SEPARATOR
    **/
    public static final String DATE_SEPARATOR_SYSTEM_VALUE              = "S";

    static {
        attributes_.add(DATE_SEPARATOR, String.class, false,
                        new Object[] {DATE_SEPARATOR_SYSTEM_VALUE}, null, false);
        getterMap_.add(DATE_SEPARATOR, JOBI0400_, "receiverVariable.dateSeparator", new DateSeparatorValueMap_());
        setterKeys_.add(DATE_SEPARATOR, 406, ProgramKeys.CHAR, 1);
    }

    private static class DateSeparatorValueMap_ extends AbstractValueMap
    {
        public Object ptol(Object physicalValue, AS400 system)
        {
            // PCML trims blanks, however a blank is a valid date separator.
            // Preserve it.
            if (((String)physicalValue).length() == 0)
                return " ";
            else
                return super.ptol(physicalValue);
        }
    }



/**
Attribute ID for date started.  This identifies a read-only Date attribute,
which represents the date and time when the job began to run on the system.
The Date value is converted using the default Java locale.
**/
    public static final String DATE_STARTED                             = "DATE_STARTED";

    static {
        attributes_.add(DATE_STARTED, Date.class, true);
        getterMap_.add(DATE_STARTED, JOBI0400_, "receiverVariable.dateAndTimeJobBecameActive", dateValueMap13_);
    }



/**
Attribute ID for DBCS capable.  This identifies a read-only Boolean attribute,
which indicates whether the job is DBCS capable.
**/
    public static final String DBCS_CAPABLE                             = "DBCS_CAPABLE";

    static {
        attributes_.add(DBCS_CAPABLE, Boolean.class, true);
        getterMap_.add(DBCS_CAPABLE, JOBI0600_, "receiverVariable.dbcsCapable", new BooleanValueMap(new String[] {"0", ""}, new String[] { "1" }));
    }



/**
Attribute ID for decimal format.  This identifies a String attribute, which
represents the decimal format used for this job. Possible values are:
<ul>
<li>{@link #DECIMAL_FORMAT_PERIOD DECIMAL_FORMAT_PERIOD} - Uses a period
    for a decimal point, a comma for a 3-digit grouping character, and zero-suppresses
    to the left of the decimal point.
<li>{@link #DECIMAL_FORMAT_COMMA_I DECIMAL_FORMAT_COMMA_I} - Uses a comma for
    a decimal point and a period for a 3-digit grouping character.  The zero-suppression
    character is in the second character (rather than the first) to the left of the decimal
    notation.  Balances with zero  values to the left of the comma are written with one
    leading zero.
<li>{@link #DECIMAL_FORMAT_COMMA_J DECIMAL_FORMAT_COMMA_J} - Uses a comma for a decimal
    point, a period for a 3-digit grouping character, and zero-suppresses to the left of the
    decimal point.
</ul>

<p>The following special value can be used when setting the decimal separator:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QDECFMT is used.
</ul>
**/
    public static final String DECIMAL_FORMAT                           = "DECIMAL_FORMAT";

    /**
    Attribute value indicating the date format that uses a period for a decimal point, a comma
    for a 3-digit grouping character, and zero-suppresses to the left of
    the decimal point.

    @see #DECIMAL_FORMAT
    **/
    public static final String DECIMAL_FORMAT_PERIOD                    = "";

    /**
    Attribute value indicating the date format that uses a comma for a decimal point
    and a period for a 3-digit grouping character.  The zero-suppression character is
    in the second character (rather than the first) to the left of the decimal
    notation.  Balances with zero  values to the left of the comma are
    written with one leading zero.

    @see #DECIMAL_FORMAT
    **/
    public static final String DECIMAL_FORMAT_COMMA_I                   = "I";

    /**
    Attribute value indicating the date format that uses a comma for a decimal point,
    a period for a 3-digit grouping character, and zero-suppresses to the left of the
    decimal point.

    @see #DECIMAL_FORMAT
    **/
    public static final String DECIMAL_FORMAT_COMMA_J                   = "J";

    static {
        attributes_.add(DECIMAL_FORMAT, String.class, false,
                        new Object[] { SYSTEM_VALUE,
                            DECIMAL_FORMAT_PERIOD,
                            DECIMAL_FORMAT_COMMA_I,
                            DECIMAL_FORMAT_COMMA_J }, null, true);
        getterMap_.add(DECIMAL_FORMAT, JOBI0400_, "receiverVariable.decimalFormat");
        setterKeys_.add(DECIMAL_FORMAT, 413, ProgramKeys.CHAR, 8);
    }



/**
Attribute ID for default coded character set identifier (CCSID).  This identifies a
read-only Integer attribute, which represents the default CCSID for this job.
The value will be 0 if the job is not active.
**/
    public static final String DEFAULT_CCSID                            = "DEFAULT_CCSID";

    static {
        attributes_.add(DEFAULT_CCSID, Integer.class, true);
        getterMap_.add(DEFAULT_CCSID, JOBI0400_, "receiverVariable.defaultCodedCharacterSetIdentifier");
    }



/**
Attribute ID for default wait time.  This identifies an Integer attribute,
which represents the default maximum time (in seconds) that a thread in the job
waits for a system instruction.  The value -1 means there is no maximum.
The value 0 is not valid.
**/
    public static final String DEFAULT_WAIT_TIME                        = "DEFAULT_WAIT_TIME";

    static {
        attributes_.add(DEFAULT_WAIT_TIME, Integer.class, false);
        getterMap_.add(DEFAULT_WAIT_TIME, JOBI0100_, "receiverVariable.defaultWait");
        getterMap_.add(DEFAULT_WAIT_TIME, JOBI0150_, "receiverVariable.defaultWait");
        setterKeys_.add(DEFAULT_WAIT_TIME, 409, ProgramKeys.BINARY);
    }



/**
Attribute ID for device recovery action.  This identifies a String attribute,
which represents the action taken for interactive jobs when an I/O error occurs
for the job's requesting program device.  Possible values are:
<ul>
<li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE DEVICE_RECOVERY_ACTION_MESSAGE} -
    Signals the I/O error message to the application and lets the application program
    perform error recovery.
<li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE} -
    Disconnects the job when an I/O error occurs.  When the job reconnects, the system
    sends an error message to the application program, indicating the job has reconnected
    and that the workstation device has recovered.
<li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST} -
    Disconnects the job when an I/O error occurs.  When the job reconnects, the system
    sends the End Request (ENDRQS) command to return control to the previous request
    level.
<li>{@link #DEVICE_RECOVERY_ACTION_END_JOB DEVICE_RECOVERY_ACTION_END_JOB} -
    Ends the job when an I/O error occurs.  A message is sent to the job's log and
    to the history log (QHST) indicating the job ended because of a device error.
<li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST} -
    Ends the job when an I/O error occurs.  There is no job log produced for the job.
    The system sends a message to the QHST log indicating the job ended because of a device error.
</ul>

<p>The following special values can be used when setting the device recovery action:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QDEVRCYACN is used.
</ul>
**/
    public static final String DEVICE_RECOVERY_ACTION                       = "DEVICE_RECOVERY_ACTION";

    /**
    Attribute value indicating the device recovery action that signals the I/O
    error message to the application and lets the application program perform
    error recovery.

    @see #DEVICE_RECOVERY_ACTION
    **/
    public static final String DEVICE_RECOVERY_ACTION_MESSAGE               = "*MSG";

    /**
    Attribute value indicating the device recovery action that disconnects the
    job when an I/O error occurs.

    @see #DEVICE_RECOVERY_ACTION
    **/
    public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE    = "*DSCMSG";

    /**
    Attribute value indicating the device recovery action that disconnects the
    job when an I/O error occurs.  When the job reconnects, the system
    sends the End Request (ENDRQS) command to return control to the previous request
    level.

    @see #DEVICE_RECOVERY_ACTION
    **/
    public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST= "*DSCENDRQS";

    /**
    Attribute value indicating the device recovery action that ends the job when
    an I/O error occurs.  A message is sent to the job's log and
    to the history log (QHST) indicating the job ended because of a device error.

    @see #DEVICE_RECOVERY_ACTION
    **/
    public static final String DEVICE_RECOVERY_ACTION_END_JOB               = "*ENDJOB";

    /**
    Attribute value indicating the device recovery action that ends the job when
    an I/O error occurs.  There is no job log produced for the job.
    The system sends a message to the QHST log indicating the job ended because of a device error.

    @see #DEVICE_RECOVERY_ACTION
    **/
    public static final String DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST       = "*ENDJOBNOLIST";

    static {
        attributes_.add(DEVICE_RECOVERY_ACTION, String.class, false,
                        new Object[] { DEVICE_RECOVERY_ACTION_MESSAGE,
                                       DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE,
                                       DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST,
                                       DEVICE_RECOVERY_ACTION_END_JOB,
                                       DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST,
                                       SYSTEM_VALUE }, null, true);
        getterMap_.add(DEVICE_RECOVERY_ACTION, JOBI0400_, "receiverVariable.deviceRecoveryAction");
        setterKeys_.add(DEVICE_RECOVERY_ACTION, 410, ProgramKeys.CHAR, 13);
    }



/**
Attribute ID for eligible for purge.  This identifies a Boolean attribute,
which indicates whether the job is eligible to be moved out of main storage
and put into auxiliary storage at the end of a time slice or when it is
beginning a long wait.
**/
    public static final String ELIGIBLE_FOR_PURGE                       = "ELIGIBLE_FOR_PURGE";

    static {
        attributes_.add(ELIGIBLE_FOR_PURGE, Boolean.class, false);
        ValueMap valueMap = new BooleanValueMap(new String[] {NO, ""}, new String[] { YES });
        getterMap_.add(ELIGIBLE_FOR_PURGE, JOBI0100_, "receiverVariable.purge", valueMap);
        getterMap_.add(ELIGIBLE_FOR_PURGE, JOBI0150_, "receiverVariable.purge", valueMap);
        setterKeys_.add(ELIGIBLE_FOR_PURGE, 1604, ProgramKeys.CHAR, 4, valueMap);
    }



/**
Attribute ID for end severity.  This identifies a read-only Integer attribute,
which represents the message severity level of escape messages that can cause a batch
job to end.  The batch job ends when a request in the batch input stream sends
an escape message, whose severity is equal to or greater than this value, to the
request processing program.
**/
    public static final String END_SEVERITY                             = "END_SEVERITY";

    static {
        attributes_.add(END_SEVERITY, Integer.class, true);
        getterMap_.add(END_SEVERITY, JOBI0500_, "receiverVariable.endSeverity");
    }


/**
Attribute ID for function name.  This identifies a read-only String attribute, which
represents additional information about the function the initial thread is currently
performing.  This information is updated only when a command is processed.
**/
    public static final String FUNCTION_NAME                            = "FUNCTION_NAME";

    static {
        attributes_.add(FUNCTION_NAME, String.class, true);
        getterMap_.add(FUNCTION_NAME, JOBI0200_, "receiverVariable.functionName");
    }


/**
Attribute ID for function type.  This identifies a read-only String attribute,
which represents the high-level function type the initial thread is performing,
if any.  Possible values are:
<ul>
<li>{@link #FUNCTION_TYPE_BLANK FUNCTION_TYPE_BLANK} - The system is not performing a logged function.
<li>{@link #FUNCTION_TYPE_COMMAND FUNCTION_TYPE_COMMAND} - A command is running interactively, or it is
    in a batch input stream, or it was requested from a system menu.
<li>{@link #FUNCTION_TYPE_DELAY FUNCTION_TYPE_DELAY} - The initial thread of the job is processing
    a Delay Job (DLYJOB) command.
<li>{@link #FUNCTION_TYPE_GROUP FUNCTION_TYPE_GROUP} - The Transfer Group Job (TFRGRPJOB) command
    suspended the job.
<li>{@link #FUNCTION_TYPE_INDEX FUNCTION_TYPE_INDEX} - The initial thread of the job is rebuilding
    an index (access path).
<li>{@link #FUNCTION_TYPE_IO FUNCTION_TYPE_IO} - The job is a subsystem monitor that is performing
    input/output (I/O) operations to a work station.
<li>{@link #FUNCTION_TYPE_LOG FUNCTION_TYPE_LOG} - The system logs history information in a database
    file.
<li>{@link #FUNCTION_TYPE_MENU FUNCTION_TYPE_MENU} - The initial thread of the job is currently
    at a system menu.
<li>{@link #FUNCTION_TYPE_MRT FUNCTION_TYPE_MRT} - The job is a multiple requester terminal (MRT)
    job is the {@link #JOB_TYPE job type} is {@link #JOB_TYPE_BATCH JOB_TYPE_BATCH}
    and the {@link #JOB_SUBTYPE job subtype} is {@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT},
    or it is an interactive job attached to an MRT job if the
    {@link #JOB_TYPE job type} is {@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE}.
<li>{@link #FUNCTION_TYPE_PROCEDURE FUNCTION_TYPE_PROCEDURE} - The initial thread of the job is running
    a procedure.
<li>{@link #FUNCTION_TYPE_PROGRAM FUNCTION_TYPE_PROGRAM} - The initial thread of the job is running
    a program.
<li>{@link #FUNCTION_TYPE_SPECIAL FUNCTION_TYPE_SPECIAL} - The function type is special.
</ul>
**/
    public static final String FUNCTION_TYPE                            = "FUNCTION_TYPE";

    /**
    Attribute value indicating that the system is not performing a logged function.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_BLANK                      = "";

    /**
    Attribute value indicating that a command is running interactively, or it is
    in a batch input stream, or it was requested from a system menu.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_COMMAND                    = "C";

    /**
    Attribute value indicating that the initial thread of the job is processing
    a Delay Job (DLYJOB) command.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_DELAY                      = "D";

    /**
    Attribute value indicating that the Transfer Group Job (TFRGRPJOB) command
    suspended the job.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_GROUP                      = "G";

    /**
    Attribute value indicating that the initial thread of the job is rebuilding an index
    (access path).

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_INDEX                      = "I";

    /**
    Attribute value indicating that the system logs history information in a database
    file.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_LOG                        = "L";

    /**
    Attribute value indicating that the job is a multiple requester terminal (MRT)
    job is the {@link #JOB_TYPE job type} is {@link #JOB_TYPE_BATCH JOB_TYPE_BATCH}
    and the {@link #JOB_SUBTYPE job subtype} is {@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT},
    or it is an interactive job attached to an MRT job if the
    {@link #JOB_TYPE job type} is {@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE}.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_MRT                        = "M";

    /**
    Attribute value indicating that the initial thread of the job is currently
    at a system menu.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_MENU                       = "N";

    /**
    Attribute value indicating that the job is a subsystem monitor that is performing
    input/output (I/O) operations to a work station.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_IO                         = "O";

    /**
    Attribute value indicating that the initial thread of the job is running
    a program.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_PROGRAM                    = "P";

    /**
    Attribute value indicating that the initial thread of the job is running
    a procedure.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_PROCEDURE                  = "R";

    /**
    Attribute value indicating that the function type is special.

    @see #FUNCTION_TYPE
    **/
    public static final String FUNCTION_TYPE_SPECIAL                    = "*";

    static {
        attributes_.add(FUNCTION_TYPE, String.class, true,
                        new String[] { FUNCTION_TYPE_BLANK,
                            FUNCTION_TYPE_COMMAND,
                            FUNCTION_TYPE_DELAY,
                            FUNCTION_TYPE_GROUP,
                            FUNCTION_TYPE_INDEX,
                            FUNCTION_TYPE_IO,
                            FUNCTION_TYPE_LOG,
                            FUNCTION_TYPE_MENU,
                            FUNCTION_TYPE_MRT,
                            FUNCTION_TYPE_PROCEDURE,
                            FUNCTION_TYPE_PROGRAM,
                            FUNCTION_TYPE_SPECIAL }, null, true);
        getterMap_.add(FUNCTION_TYPE, JOBI0200_, "receiverVariable.functionType");
    }


/**
Attribute ID for inquiry message reply.  This identifies a String attribute, which
represents how the job answers inquiry messages.  Possible values are:
<ul>
<li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED INQUIRY_MESSAGE_REPLY_REQUIRED} -
    The job requires an answer for any inquiry
    messages that occur while this job is running.
<li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT INQUIRY_MESSAGE_REPLY_DEFAULT} -
    The system uses the default message reply to
    answer any inquiry messages issued while this job is running.  The default
    reply is either defined in the message description or is the default system
    reply.
<li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST} -
    The system reply list is
    checked to see if there is an entry for an inquiry message issued while this
    job is running.  If a match occurs, the system uses the reply value for that
    entry.  If no entry exists for that message, the system uses an inquiry message.
</ul>
**/
    public static final String INQUIRY_MESSAGE_REPLY                   = "INQUIRY_MESSAGE_REPLY";

    /**
    Attribute value indicating that the job requires an answer for any inquiry
    messages that occur while this job is running.

    @see #INQUIRY_MESSAGE_REPLY
    **/
    public static final String INQUIRY_MESSAGE_REPLY_REQUIRED          = "*RQD";

    /**
    Attribute value indicating that the system uses the default message reply to
    answer any inquiry messages issued while this job is running.  The default
    reply is either defined in the message description or is the default system
    reply.

    @see #INQUIRY_MESSAGE_REPLY
    **/
    public static final String INQUIRY_MESSAGE_REPLY_DEFAULT           = "*DFT";

    /**
    Attribute value indicating that the system reply list is
    checked to see if there is an entry for an inquiry message issued while this
    job is running.  If a match occurs, the system uses the reply value for that
    entry.  If no entry exists for that message, the system uses an inquiry message.

    @see #INQUIRY_MESSAGE_REPLY
    **/
    public static final String INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST = "*SYSRPYL";

    static {
        attributes_.add(INQUIRY_MESSAGE_REPLY, String.class, false,
                        new String[] { INQUIRY_MESSAGE_REPLY_REQUIRED,
                            INQUIRY_MESSAGE_REPLY_DEFAULT,
                            INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST }, null, true);
        getterMap_.add(INQUIRY_MESSAGE_REPLY, JOBI0400_, "receiverVariable.inquiryMessageReply");
        setterKeys_.add(INQUIRY_MESSAGE_REPLY, 901, ProgramKeys.CHAR, 10);
    }



/**
Attribute ID for instance.  This identifies a read-only byte[] attribute, which
further identifies the source that originated the APPC job.  This attribute is
part of the unit of work ID, which is used to track jobs across multiple systems.
This is applicable only when the job is associated with a source or target system
using advanced program-to-program  communications (APPC).
**/
    public static final String INSTANCE                                 = "INSTANCE";

    static {
        attributes_.add(INSTANCE, byte[].class, true);
        getterMap_.add(INSTANCE, JOBI0400_, "receiverVariable.unitOfWorkID.instance");
    }


/**
Attribute ID for interactive transactions.  This identifies a read-only Integer attribute,
which represents the number of interactive transactions.
**/
    public static final String INTERACTIVE_TRANSACTIONS                 = "INTERACTIVE_TRANSACTIONS";

    static {
        attributes_.add(INTERACTIVE_TRANSACTIONS, Integer.class, true);
        getterMap_.add(INTERACTIVE_TRANSACTIONS, JOBI0200_, "receiverVariable.numberOfInteractiveTransactions");
    }



/**
Attribute ID for internal job identifier.  This identifies a read-only byte array attribute,
which represents the internal job identifier of the job as identified to the system.
**/
    public static final String INTERNAL_JOB_ID                     = "INTERNAL_JOB_ID";

    static {
        attributes_.add(INTERNAL_JOB_ID, byte[].class, true);
        getterMap_.add(INTERNAL_JOB_ID, JOBI0100_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0150_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0200_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0300_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0400_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0500_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0600_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0700_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0800_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(INTERNAL_JOB_ID, JOBI0900_, "receiverVariable.qualifiedJobName.jobName");
    }


/**
Attribute ID for job date.  This identifies a Date attribute, which represents
the date to be used for the job.  The value {@link #NO_DATE NO_DATE}
indicates that the job uses the system date.
The Date value is converted using the default Java locale.
**/
    public static final String JOB_DATE                                 = "JOB_DATE";

    static {
        attributes_.add(JOB_DATE, Date.class, false);
        DateValueMap valueMap = new DateValueMap(DateValueMap.FORMAT_7);
        getterMap_.add(JOB_DATE, JOBI0300_, "receiverVariable.jobDate", valueMap);
        setterKeys_.add(JOB_DATE, 1002, ProgramKeys.CHAR, 7, valueMap);
    }



/**
Attribute ID for job description.  This identifies a read-only String attribute,
which represents the fully qualified integrated file system path name of the job
description.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String JOB_DESCRIPTION                     = "JOB_DESCRIPTION";

    static {
        attributes_.add(JOB_DESCRIPTION, String.class, true);
        getterMap_.add(JOB_DESCRIPTION, JOBI0400_, "receiverVariable.jobDescription",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "JOBD"));
    }



/**
Attribute ID for job name.  This identifies a read-only String attribute,
which represents the name of the job as identified to the system.
**/
    public static final String JOB_NAME                     = "JOB_NAME";

    static {
        attributes_.add(JOB_NAME, String.class, true);
        getterMap_.add(JOB_NAME, JOBI0100_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0150_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0200_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0300_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0400_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0500_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0600_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0700_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0800_, "receiverVariable.qualifiedJobName.jobName");
        getterMap_.add(JOB_NAME, JOBI0900_, "receiverVariable.qualifiedJobName.jobName");
    }



/**
Attribute ID for job number.  This identifies a read-only String attribute,
which represents the system-generated job number.
**/
    public static final String JOB_NUMBER                     = "JOB_NUMBER";

    static {
        attributes_.add(JOB_NUMBER, String.class, true);
        getterMap_.add(JOB_NUMBER, JOBI0100_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0150_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0200_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0300_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0400_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0500_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0600_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0700_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0800_, "receiverVariable.qualifiedJobName.jobNumber");
        getterMap_.add(JOB_NUMBER, JOBI0900_, "receiverVariable.qualifiedJobName.jobNumber");
    }



/**
Attribute ID for job queue.  This identifies a String attribute,
which represents the fully qualified integrated file system path name
of the job queue that the job is on, or that
the job was on if it is currently active.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String JOB_QUEUE             = "JOB_QUEUE";

    static {
        attributes_.add(JOB_QUEUE, String.class, false);
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "JOBQ");
        getterMap_.add(JOB_QUEUE, JOBI0300_, "receiverVariable.jobQueue", valueMap);
        setterKeys_.add(JOB_QUEUE, 1004, ProgramKeys.CHAR, 20, valueMap);
    }



/**
Attribute ID for job queue date.  This identifies a read-only
Date attribute, which represents the date and time when the job was
put on the job queue.  The Date value is converted using the default Java locale.
**/
    public static final String JOB_QUEUE_DATE                      = "JOB_QUEUE_DATE";

    static {
        attributes_.add(JOB_QUEUE_DATE, Date.class, true);
        getterMap_.add(JOB_QUEUE_DATE, JOBI0300_, "receiverVariable.dateAndTimeJobWasPutOnThisJobQueue", dateValueMapDts_);
    }



/**
Attribute ID for job queue priority.  This identifies an Integer attribute,
which represents the scheduling priority of the job compared to other jobs
on the same job queue.  The highest priority is 0 and the lowest is 9.
**/
    public static final String JOB_QUEUE_PRIORITY             = "JOB_QUEUE_PRIORITY";

    static {
        attributes_.add(JOB_QUEUE_PRIORITY, Integer.class, false);
        getterMap_.add(JOB_QUEUE_PRIORITY, JOBI0300_, "receiverVariable.jobQueuePriority", integerValueMap_);
        setterKeys_.add(JOB_QUEUE_PRIORITY, 1005, ProgramKeys.CHAR, 2, integerValueMap_);
    }



/**
Attribute ID for job queue status.  This identifies a read-only String attribute,
which represents the status of the job on the job queue.  Possible values are:
<ul>
<li>{@link #JOB_QUEUE_STATUS_BLANK JOB_QUEUE_STATUS_BLANK} - The job is not on a job queue.
<li>{@link #JOB_QUEUE_STATUS_SCHEDULED JOB_QUEUE_STATUS_SCHEDULED} - The job will run as scheduled.
<li>{@link #JOB_QUEUE_STATUS_HELD JOB_QUEUE_STATUS_HELD} - The job is being held on the job queue.
<li>{@link #JOB_QUEUE_STATUS_RELEASED JOB_QUEUE_STATUS_RELEASED} - The job is ready to be selected.
</ul>
**/
    public static final String JOB_QUEUE_STATUS               = "JOB_QUEUE_STATUS";

    /**
    Attribute value indicating that the job is not on a job queue.

    @see #JOB_QUEUE_STATUS
    **/
    public static final String JOB_QUEUE_STATUS_BLANK         = "";

    /**
    Attribute value indicating that the job will run as scheduled.

    @see #JOB_QUEUE_STATUS
    **/
    public static final String JOB_QUEUE_STATUS_SCHEDULED     = "SCD";

    /**
    Attribute value indicating that the job is being held on the job queue.

    @see #JOB_QUEUE_STATUS
    **/
    public static final String JOB_QUEUE_STATUS_HELD          = "HLD";

    /**
    Attribute value indicating that the job is ready to be selected.

    @see #JOB_QUEUE_STATUS
    **/
    public static final String JOB_QUEUE_STATUS_RELEASED      = "RLS";

    static {
        attributes_.add(JOB_QUEUE_STATUS, String.class, true,
                        new String[] { JOB_QUEUE_STATUS_BLANK,
                            JOB_QUEUE_STATUS_SCHEDULED,
                            JOB_QUEUE_STATUS_HELD,
                            JOB_QUEUE_STATUS_RELEASED }, null, true);
        getterMap_.add(JOB_QUEUE_STATUS, JOBI0300_, "receiverVariable.statusOfJobOnThejobQueue");
    }



/**
Attribute ID for job status.  This identifies a read-only String attribute,
which represents the status of the job.  Possible values are:
<ul>
<li>{@link #JOB_STATUS_ACTIVE JOB_STATUS_ACTIVE} - The job is active.
<li>{@link #JOB_STATUS_JOBQ JOB_STATUS_JOBQ} - The job is currently on a job queue.
<li>{@link #JOB_STATUS_OUTQ JOB_STATUS_OUTQ} - The job has completed running, but still has output
    on an output queue.
</ul>
**/
    public static final String JOB_STATUS                     = "JOB_STATUS";

    /**
    Attribute value indicating that the job is active.

    @see #JOB_STATUS
    **/
    public static final String JOB_STATUS_ACTIVE              = "*ACTIVE";

    /**
    Attribute value indicating that the job is currently on a job queue.

    @see #JOB_STATUS
    **/
    public static final String JOB_STATUS_JOBQ                = "*JOBQ";

    /**
    Attribute value indicating that the job has completed running.

    @see #JOB_STATUS
    **/
    public static final String JOB_STATUS_OUTQ                = "*OUTQ";

    static {
        attributes_.add(JOB_STATUS, String.class, true,
                        new String[] { JOB_STATUS_ACTIVE,
                            JOB_STATUS_JOBQ, JOB_STATUS_OUTQ }, null, true);
        getterMap_.add(JOB_STATUS, JOBI0400_, "receiverVariable.jobStatus");
    }



/**
Attribute ID for job subtype.  This identifies a read-only String attribute,
which represents additional information about the job type.  Possible values are:
<ul>
<li>{@link #JOB_SUBTYPE_BLANK JOB_SUBTYPE_BLANK} - The job has no special subtype or is not a valid job.
<li>{@link #JOB_SUBTYPE_IMMEDIATE JOB_SUBTYPE_IMMEDIATE} - The job is an immediate job.
<li>{@link #JOB_SUBTYPE_PROCEDURE_START_REQUEST JOB_SUBTYPE_PROCEDURE_START_REQUEST} - The job started
    with a procedure start request.
<li>{@link #JOB_SUBTYPE_MACHINE_SERVER_JOB JOB_SUBTYPE_MACHINE_SERVER_JOB} - The job is an 
    Advanced 36 machine server job.
<li>{@link #JOB_SUBTYPE_PRESTART JOB_SUBTYPE_PRESTART} - The job is a prestart job.
<li>{@link #JOB_SUBTYPE_PRINT_DRIVER JOB_SUBTYPE_PRINT_DRIVER} - The job is a print driver job.
<li>{@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT} - The job is a System/36 multiple requester terminal
    (MRT) job.
<li>{@link #JOB_SUBTYPE_ALTERNATE_SPOOL_USER JOB_SUBTYPE_ALTERNATE_SPOOL_USER} - Alternate spool user.
</ul>
**/
    public static final String JOB_SUBTYPE                    = "JOB_SUBTYPE";

    /**
    Attribute value indicating that the job has no special subtype or is not a valid job.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_BLANK                    = "";

    /**
    Attribute value indicating that the job is an immediate job.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_IMMEDIATE                = "D";

    /**
    Attribute value indicating that the job started
    with a procedure start request.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_PROCEDURE_START_REQUEST  = "E";

    /**
    Attribute value indicating that the job is an 
    Advanced 36 machine server job.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_MACHINE_SERVER_JOB       = "F";

    /**
    Attribute value indicating that the job is a prestart job.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_PRESTART                 = "J";

    /**
    Attribute value indicating that the job is a print driver job.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_PRINT_DRIVER             = "P";

    /**
    Attribute value indicating that the job is a System/36 multiple requester terminal
    (MRT) job.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_MRT                      = "T";

    /**
    Attribute value indicating alternate spool user.

    @see #JOB_SUBTYPE
    **/
    public static final String JOB_SUBTYPE_ALTERNATE_SPOOL_USER     = "U";

    static {
        attributes_.add(JOB_SUBTYPE, String.class, true,
                        new String[] { JOB_SUBTYPE_BLANK,
                            JOB_SUBTYPE_IMMEDIATE,
                            JOB_SUBTYPE_PROCEDURE_START_REQUEST,
                            JOB_SUBTYPE_MACHINE_SERVER_JOB,
                            JOB_SUBTYPE_PRESTART,
                            JOB_SUBTYPE_PRINT_DRIVER,
                            JOB_SUBTYPE_MRT,
                            JOB_SUBTYPE_ALTERNATE_SPOOL_USER }, null, true);
        getterMap_.add(JOB_SUBTYPE, JOBI0100_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0150_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0200_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0300_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0400_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0500_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0600_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0700_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0800_, "receiverVariable.jobSubtype");
        getterMap_.add(JOB_SUBTYPE, JOBI0900_, "receiverVariable.jobSubtype");
    }



/**
Attribute ID for switch settings.  This identifies a String attribute,
which represents the current setting of the job switches used by this job.
**/
    public static final String JOB_SWITCHES             = "JOB_SWITCHES";

    static {
        attributes_.add(JOB_SWITCHES, String.class, false);
        getterMap_.add(JOB_SWITCHES, JOBI0400_, "receiverVariable.jobSwitches");
        setterKeys_.add(JOB_SWITCHES, 1006, ProgramKeys.CHAR, 8);
    }



/**
Attribute ID for job type.  This identifies a read-only String attribute,
which represents the job type.  Possible values are:
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
**/
    public static final String JOB_TYPE                    = "JOB_TYPE";

    /**
    Attribute value indicating that the job is not a valid job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_NOT_VALID          = "";

    /**
    Attribute value indicating that the job is an autostart job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_AUTOSTART          = "A";

    /**
    Attribute value indicating that the job is a batch job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_BATCH              = "B";

    /**
    Attribute value indicating that the job is an interactive job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_INTERACTIVE        = "I";

    /**
    Attribute value indicating that the job is a subsystem monitor job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_SUBSYSTEM_MONITOR  = "M";

    /**
    Attribute value indicating that the job is a spooled reader job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_SPOOLED_READER     = "R";

    /**
    Attribute value indicating that the job is a system job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_SYSTEM             = "S";

    /**
    Attribute value indicating that the job is a spooled writer job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_SPOOLED_WRITER     = "W";

    /**
    Attribute value indicating that the job is the SCPF system job.

    @see #JOB_TYPE
    **/
    public static final String JOB_TYPE_SCPF_SYSTEM        = "X";

    static {
        attributes_.add(JOB_TYPE, String.class, true,
                        new String[] { JOB_TYPE_NOT_VALID,
                            JOB_TYPE_AUTOSTART,
                            JOB_TYPE_BATCH,
                            JOB_TYPE_INTERACTIVE,
                            JOB_TYPE_SUBSYSTEM_MONITOR,
                            JOB_TYPE_SPOOLED_READER,
                            JOB_TYPE_SYSTEM,
                            JOB_TYPE_SPOOLED_WRITER,
                            JOB_TYPE_SCPF_SYSTEM }, null, true);
        getterMap_.add(JOB_TYPE, JOBI0100_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0150_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0200_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0300_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0400_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0500_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0600_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0700_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0800_, "receiverVariable.jobType");
        getterMap_.add(JOB_TYPE, JOBI0900_, "receiverVariable.jobType");
    }



/**
Attribute ID for keep DDM connections active.  This identifies a String attribute,
which represents whether connections using distributed data management (DDM)
protocols remain active when they are not being used.  Possible values are:
<ul>
<li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP KEEP_DDM_CONNECTIONS_ACTIVE_KEEP} -
    The system keeps DDM connections active when there are no users.
<li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP KEEP_DDM_CONNECTIONS_ACTIVE_DROP} -
    The system ends a DDM connection when there are no users.
</ul>
**/
    public static final String KEEP_DDM_CONNECTIONS_ACTIVE             = "KEEP_DDM_CONNECTIONS_ACTIVE";

    /**
    Attribute value indicating that the system keeps DDM connections active when there are no users.

    @see #KEEP_DDM_CONNECTIONS_ACTIVE
    **/
    public static final String KEEP_DDM_CONNECTIONS_ACTIVE_KEEP        = "*KEEP";

    /**
    Attribute value indicating that the system ends a DDM connection when there are no users.

    @see #KEEP_DDM_CONNECTIONS_ACTIVE
    **/
    public static final String KEEP_DDM_CONNECTIONS_ACTIVE_DROP        = "*DROP";

    static {
        attributes_.add(KEEP_DDM_CONNECTIONS_ACTIVE, String.class, false,
                        new String[] { KEEP_DDM_CONNECTIONS_ACTIVE_KEEP,
                            KEEP_DDM_CONNECTIONS_ACTIVE_DROP }, null, true);
        getterMap_.add(KEEP_DDM_CONNECTIONS_ACTIVE, JOBI0400_, "receiverVariable.ddmConversationHandling");
        setterKeys_.add(KEEP_DDM_CONNECTIONS_ACTIVE, 408, ProgramKeys.CHAR, 5);
    }



/**
Attribute ID for language ID.  This identifies a String attribute,
which represents the language identifier associated with this
job.

<p>The following special values can be used when setting the
language ID:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QLANGID is used.
<li>{@link #USER_PROFILE USER_PROFILE} - The
    language identifier specified in the user profile in which this thread
    was initially running is used.
</ul>
**/
    public static final String LANGUAGE_ID              = "LANGUAGE_ID";

    static {
        attributes_.add(LANGUAGE_ID, String.class, false,
                        new String[] { SYSTEM_VALUE, USER_PROFILE }, null, false);
        getterMap_.add(LANGUAGE_ID, JOBI0400_, "receiverVariable.languageID");
        setterKeys_.add(LANGUAGE_ID, 1201, ProgramKeys.CHAR, 8);
    }



/**
Attribute ID for location name.  This identifies a read-only String attribute,
which represents the name of the source system that originated the
APPC job.  This attribute is part of the unit of work ID, which is used to track
jobs across multiple systems.  This is applicable only when the job is associated
with a source or target system using advanced program-to-program  communications
(APPC).
**/
    public static final String LOCATION_NAME             = "LOCATION_NAME";

    static {
        attributes_.add(LOCATION_NAME, String.class, true);
        getterMap_.add(LOCATION_NAME, JOBI0400_, "receiverVariable.unitOfWorkID.locationName");
    }



/**
Attribute ID for logging CL programs.  This identifies a Boolean attribute,
which indicates whether messages are logged for CL programs.
**/
    public static final String LOG_CL_PROGRAMS                       = "LOG_CL_PROGRAMS";

    static {
        attributes_.add(LOG_CL_PROGRAMS, Boolean.class, false);
        ValueMap valueMap = new BooleanValueMap(NO, YES);
        getterMap_.add(LOG_CL_PROGRAMS, JOBI0400_, "receiverVariable.loggingOfCLPrograms", valueMap);
        setterKeys_.add(LOG_CL_PROGRAMS, 1203, ProgramKeys.CHAR, 10, valueMap);
    }



/**
Attribute ID for logging level.  This identifies a String attribute,
which represents the type of information that is logged.  Possible values are:
<ul>
<li>{@link #LOGGING_LEVEL_NONE LOGGING_LEVEL_NONE} - No messages are logged.
<li>{@link #LOGGING_LEVEL_MESSAGES_BY_SEVERITY LOGGING_LEVEL_MESSAGES_BY_SEVERITY} - All messages sent
    to the job's external message queue with a severity greater than or equal to
    the {@link #LOGGING_SEVERITY message logging severity} are logged.
<li>{@link #LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES} -
    Requests or commands from CL programs for which the system issues messages with
    a severity code greater than or equal to the {@link #LOGGING_SEVERITY 
    logging severity} and all messages
    associated with those requests or commands that have a severity code greater
    than or equal to the {@link #LOGGING_SEVERITY logging severity}
    are logged.
<li>{@link #LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES} -
    All requests or commands from CL programs and all messages
    associated with those requests or commands that have a severity code greater
    than or equal to the {@link #LOGGING_SEVERITY logging severity}
    are logged.
<li>{@link #LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES} -
    All requests or commands from CL programs and all messages
    with a severity code greater than or equal to the {@link #LOGGING_SEVERITY 
    logging severity} are logged.
</ul>
**/
    public static final String LOGGING_LEVEL                       = "LOGGING_LEVEL";

    /**
    Attribute value indicating that no messages are logged.

    @see #LOGGING_LEVEL
    **/
    public static final String LOGGING_LEVEL_NONE                                           = "0";

    /**
    Attribute value indicating that all messages sent
    to the job's external message queue with a severity greater than or equal to
    the {@link #LOGGING_SEVERITY message logging severity} are logged.

    @see #LOGGING_LEVEL
    **/
    public static final String LOGGING_LEVEL_MESSAGES_BY_SEVERITY                           = "1";

    /**
    Attribute value indicating that requests or commands from CL programs for which
    the system issues messages with a severity code greater than or equal to the
    {@link #LOGGING_SEVERITY logging severity} and all messages associated with
    those requests or commands that have a severity code greater than or equal to the
    {@link #LOGGING_SEVERITY logging severity} are logged.

    @see #LOGGING_LEVEL
    **/
    public static final String LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES   = "2";

    /**
    Attribute value indicating that all requests or commands from CL programs and all messages
    associated with those requests or commands that have a severity code greater
    than or equal to the {@link #LOGGING_SEVERITY logging severity}
    are logged.

    @see #LOGGING_LEVEL
    **/
    public static final String LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES           = "3";

    /**
    Attribute value indicating that all requests or commands from CL programs and all messages
    with a severity code greater than or equal to the {@link #LOGGING_SEVERITY 
    logging severity} are logged.

    @see #LOGGING_LEVEL
    **/
    public static final String LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES                      = "4";

    static {
        attributes_.add(LOGGING_LEVEL, String.class, false,
                        new String[] { LOGGING_LEVEL_NONE,
                            LOGGING_LEVEL_MESSAGES_BY_SEVERITY,
                            LOGGING_LEVEL_REQUESTS_BY_SEVERITY_AND_ASSOCIATED_MESSAGES,
                            LOGGING_LEVEL_ALL_REQUESTS_AND_ASSOCIATED_MESSAGES,
                            LOGGING_LEVEL_ALL_REQUESTS_AND_MESSAGES }, null, true);
        getterMap_.add(LOGGING_LEVEL, JOBI0500_, "receiverVariable.loggingLevel");
        setterKeys_.add(LOGGING_LEVEL, 1202, ProgramKeys.CHAR, 1);
    }



/**
Attribute ID for logging severity.  This identifies an Integer attribute,
which represents the minimum severity level that causes error messages to be logged
in the job log.
**/
    public static final String LOGGING_SEVERITY                       = "LOGGING_SEVERITY";

    static {
        attributes_.add(LOGGING_SEVERITY, Integer.class, false);
        getterMap_.add(LOGGING_SEVERITY, JOBI0500_, "receiverVariable.loggingSeverity");
        setterKeys_.add(LOGGING_SEVERITY, 1204, ProgramKeys.BINARY);
    }



/**
Attribute ID for logging text.  This identifies a String attribute,
which represents the level of message text that is written in the job log
or displayed to the user.  Possible values are:
<ul>
<li>{@link #LOGGING_TEXT_MESSAGE LOGGING_TEXT_MESSAGE} - Only the message is written to the job log.
<li>{@link #LOGGING_TEXT_SECLVL LOGGING_TEXT_SECLVL} - Both the message and the message help for the
    error message are written to the job log.
<li>{@link #LOGGING_TEXT_NO_LIST LOGGING_TEXT_NO_LIST} - If the job ends normally, there is no job log.
    If the job ends abnormally, there is a job log.  The messages appearing in the
    job log contain both the message and the message help.
</ul>
**/
    public static final String LOGGING_TEXT                       = "LOGGING_TEXT";

    /**
    Attribute value indicating that only the message is written to the job log.

    @see #LOGGING_TEXT
    **/
    public static final String LOGGING_TEXT_MESSAGE               = "*MSG";

    /**
    Attribute value indicating that both the message and the message help for the
    error message are written to the job log.

    @see #LOGGING_TEXT
    **/
    public static final String LOGGING_TEXT_SECLVL                = "*SECLVL";

    /**
    Attribute value indicating that if the job ends normally, there is no job log.
    If the job ends abnormally, there is a job log.  The messages appearing in the
    job log contain both the message and the message help.

    @see #LOGGING_TEXT
    **/
    public static final String LOGGING_TEXT_NO_LIST               = "*NOLIST";

    static {
        attributes_.add(LOGGING_TEXT, String.class, false,
                        new String[] { LOGGING_TEXT_MESSAGE,
                            LOGGING_TEXT_SECLVL,
                            LOGGING_TEXT_NO_LIST }, null, true);
        getterMap_.add(LOGGING_TEXT, JOBI0500_, "receiverVariable.loggingText");
        setterKeys_.add(LOGGING_TEXT, 1205, ProgramKeys.CHAR, 7);
    }



/**
Attribute ID for maximum CPU time.  This identifies a read-only Integer attribute,
which represents the maximum processing unit time (in milliseconds) that the job
can use.  If the job consists of multiple routing steps, this is the maximum
processing unit time that the current routing step can use.  If the maximum time
is exceeded, the job is ended.  A value of -1 indicates that there is no maximum.
A value of 0 indicates that the job is not active.
**/
    public static final String MAX_CPU_TIME                       = "MAX_CPU_TIME";

    static {
        attributes_.add(MAX_CPU_TIME, Integer.class, true);
        getterMap_.add(MAX_CPU_TIME, JOBI0150_, "receiverVariable.maximumProcessingUnitTime");
    }



/**
Attribute ID for maximum temporary storage.  This identifies a read-only Integer attribute,
which represents the maximum amount of auxiliary storage (in megabytes) that the job can
use.  If the job consists of multiple routing steps, this is the maximum temporary
storage that the current routing step can use.  If the maximum temporary storage is
exceeded, the job is ended.  This does not apply to the use of permanent storage, which
is controlled through the user profile. A value of -1 indicates that there is no maximum.
**/
    public static final String MAX_TEMP_STORAGE                       = "MAX_TEMP_STORAGE";

    static {
        attributes_.add(MAX_TEMP_STORAGE, Integer.class, true);
        getterMap_.add(MAX_TEMP_STORAGE, JOBI0150_, "receiverVariable.maximumTemporaryStorageInMegabytes");
    }



/**
Attribute ID for message queue action.  This identifies a String attribute,
which represents the action to take when the message queue is full.  Possible values are:
<ul>
<li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP MESSAGE_QUEUE_ACTION_NO_WRAP} -
    Do not wrap. This action causes the job to end.
<li>{@link #MESSAGE_QUEUE_ACTION_WRAP MESSAGE_QUEUE_ACTION_WRAP} -
    Wrap to the beginning and start filling again.
<li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP MESSAGE_QUEUE_ACTION_PRINT_WRAP} -
    Wrap the message queue and print the
    messages that are being overlaid because of the wrapping.
</ul>
**/
    public static final String MESSAGE_QUEUE_ACTION                       = "MESSAGE_QUEUE_ACTION";

    /**
    Attribute value indicating that the message queue does not wrap.
    This action causes the job to end when the message queue is full.

    @see #MESSAGE_QUEUE_ACTION
    **/
    public static final String MESSAGE_QUEUE_ACTION_NO_WRAP               = "*NOWRAP";

    /**
    Attribute value indicating that the message queue wraps to the beginning
    and starts filling again when the message queue is full.

    @see #MESSAGE_QUEUE_ACTION
    **/
    public static final String MESSAGE_QUEUE_ACTION_WRAP                  = "*WRAP";

    /**
    Attribute value indicating that the message queue wraps and prints the
    messages that are being overlaid because of the wrapping.

    @see #MESSAGE_QUEUE_ACTION
    **/
    public static final String MESSAGE_QUEUE_ACTION_PRINT_WRAP            = "*PRTWRAP";

    static {
        attributes_.add(MESSAGE_QUEUE_ACTION, String.class, false,
                        new String[] { MESSAGE_QUEUE_ACTION_NO_WRAP,
                            MESSAGE_QUEUE_ACTION_WRAP,
                            MESSAGE_QUEUE_ACTION_PRINT_WRAP }, null, true);
        getterMap_.add(MESSAGE_QUEUE_ACTION, JOBI0400_, "receiverVariable.jobMessageQueueFullAction");
        setterKeys_.add(MESSAGE_QUEUE_ACTION, 1007, ProgramKeys.CHAR, 10);
    }



/**
Attribute ID for message queue maximum size.  This identifies a read-only Integer attribute,
which represents the maximum size (in megabytes) of the job's message queue.
The range is 2 to 64.
**/
    public static final String MESSAGE_QUEUE_MAX_SIZE                       = "MESSAGE_QUEUE_MAX_SIZE";

    static {
        attributes_.add(MESSAGE_QUEUE_MAX_SIZE, Integer.class, true);
        getterMap_.add(MESSAGE_QUEUE_MAX_SIZE, JOBI0400_, "receiverVariable.jobMessageQueueMaximumSize");
    }



/**
Attribute ID for mode.  This identifies a read-only String attribute,
which represents the mode name of the advanced program-to-program
communications (APPC) device that started the job.
**/
    public static final String MODE                       = "MODE";

    static {
        attributes_.add(MODE, String.class, true);
        getterMap_.add(MODE, JOBI0400_, "receiverVariable.modeName");
    }



/**
Attribute ID for network ID.  This identifies a read-only String attribute,
which represents the network name.  This attribute is part of the unit of work ID,
which is used to track jobs across multiple systems.  This is applicable only when
the job is associated with a source or target system using advanced program-to-program
communications (APPC).
**/
    public static final String NETWORK_ID             = "NETWORK_ID";

    static {
        attributes_.add(NETWORK_ID, String.class, true);
        getterMap_.add(NETWORK_ID, JOBI0400_, "receiverVariable.unitOfWorkID.networkID");
    }



/**
Attribute ID for output queue.  This identifies a String attribute,
which represents the fully qualified integrated file system path name
of the default output queue that is used for
spooled output produced by this job.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String OUTPUT_QUEUE             = "OUTPUT_QUEUE";

    static {
        attributes_.add(OUTPUT_QUEUE, String.class, false);
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "OUTQ");
        getterMap_.add(OUTPUT_QUEUE, JOBI0300_, "receiverVariable.outputQueue", valueMap);
        setterKeys_.add(OUTPUT_QUEUE, 1501, ProgramKeys.CHAR, 20, valueMap);
    }



/**
Attribute ID for output queue priority.  This identifies an Integer attribute,
which represents the output priority for spooled output files that this job
produces.  The highest priority is 0 and the lowest is 9.
**/
    public static final String OUTPUT_QUEUE_PRIORITY             = "OUTPUT_QUEUE_PRIORITY";

    static {
        attributes_.add(OUTPUT_QUEUE_PRIORITY, Integer.class, false);
        getterMap_.add(OUTPUT_QUEUE_PRIORITY, JOBI0300_, "receiverVariable.outputQueuePriority", integerValueMap_);
        setterKeys_.add(OUTPUT_QUEUE_PRIORITY, 1502, ProgramKeys.CHAR, 2, integerValueMap_);
    }



/**
Attribute ID for print key format.  This identifies a String attribute,
which represents whether border and header information is provided when
the Print key is pressed.  Possible values are:
<ul>
<li>{@link #NONE NONE} - The border and header information is not
    included with output from the Print key.
<li>{@link #PRINT_KEY_FORMAT_BORDER PRINT_KEY_FORMAT_BORDER} - The border information
    is included with output from the Print key.
<li>{@link #PRINT_KEY_FORMAT_HEADER PRINT_KEY_FORMAT_HEADER} - The header information
    is included with output from the Print key.
<li>{@link #PRINT_KEY_FORMAT_ALL PRINT_KEY_FORMAT_ALL} - The border and header information
    is included with output from the Print key.
</ul>

<p>The following special value can be used when setting the
print key format:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QPRTKEYFMT is used.
</ul>
**/
    public static final String PRINT_KEY_FORMAT                 = "PRINT_KEY_FORMAT";

    /**
    Attribute value indicating that the border information
    is included with output from the Print key.

    @see #PRINT_KEY_FORMAT
    **/
    public static final String PRINT_KEY_FORMAT_BORDER          = "*PRTBDR";

    /**
    Attribute value indicating that the header information
    is included with output from the Print key.

    @see #PRINT_KEY_FORMAT
    **/
    public static final String PRINT_KEY_FORMAT_HEADER          = "*PRTHDR";

    /**
    Attribute value indicating that the border and header information
    is included with output from the Print key.

    @see #PRINT_KEY_FORMAT
    **/
    public static final String PRINT_KEY_FORMAT_ALL             = "*PRTALL";

    static {
        attributes_.add(PRINT_KEY_FORMAT, String.class, false,
                        new String[] { NONE,
                            PRINT_KEY_FORMAT_BORDER,
                            PRINT_KEY_FORMAT_HEADER,
                            PRINT_KEY_FORMAT_ALL,
                            SYSTEM_VALUE }, null, true);
        getterMap_.add(PRINT_KEY_FORMAT, JOBI0400_, "receiverVariable.printKeyFormat");
        setterKeys_.add(PRINT_KEY_FORMAT, 1601, ProgramKeys.CHAR, 10);
    }




/**
Attribute ID for print text.  This identifies a String attribute,
which represents the line of text, if any, that is printed at the
bottom of each page of printed output for the job.

<p>The following special value can be used when setting the
print key format:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QPRTTXT is used.
</ul>
**/
    public static final String PRINT_TEXT               = "PRINT_TEXT";

    static {
        attributes_.add(PRINT_TEXT, String.class, false,
                        new String[] { SYSTEM_VALUE }, null, false);
        getterMap_.add(PRINT_TEXT, JOBI0400_, "receiverVariable.printText");
        setterKeys_.add(PRINT_TEXT, 1602, ProgramKeys.CHAR, 30);
    }



/**
Attribute ID for printer device name.  This identifies a String attribute,
which represents the printer device used for printing output
from this job.

<p>The following special values can be used when setting the
printer:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QPRTDEV is used.
<li>{@link #PRINTER_DEVICE_NAME_WORK_STATION PRINTER_DEVICE_NAME_WORK_STATION} - The
    default printer device used with this job is the printer device
    assigned to the work station that is associated with the job.
<li>{@link #USER_PROFILE USER_PROFILE} - The
    printer device name specified in the user profile in which this thread
    was initially running is used.
</ul>
**/
    public static final String PRINTER_DEVICE_NAME                  = "PRINTER_DEVICE_NAME";

    /**
    Attribute value indicating that the default printer device used with this
    job is the printer device assigned to the work station that is associated with the job.

    @see #PRINTER_DEVICE_NAME
    **/
    public static final String PRINTER_DEVICE_NAME_WORK_STATION     = "*WRKSTN";

    static {
        attributes_.add(PRINTER_DEVICE_NAME, String.class, false,
                        new String[] { SYSTEM_VALUE,
                            PRINTER_DEVICE_NAME_WORK_STATION,
                            USER_PROFILE }, null, false);
        getterMap_.add(PRINTER_DEVICE_NAME, JOBI0300_, "receiverVariable.printerDeviceName");
        setterKeys_.add(PRINTER_DEVICE_NAME, 1603, ProgramKeys.CHAR, 10);
    }



/**
Attribute ID for product libraries.  This identifies a read-only String[]
attribute, which represents the libraries that contain product information
for the initial thread of this job.
**/
    public static final String PRODUCT_LIBRARIES   = "PRODUCT_LIBRARIES";

    static {
        attributes_.add(PRODUCT_LIBRARIES, String[].class, true);
        getterMap_.add(PRODUCT_LIBRARIES, JOBI0700_, "receiverVariable.productLibraries", "receiverVariable.numberOfProductLibraries", arrayTypeValueMapString_);
    }



/**
Attribute ID for product return code.  This identifies a read-only Integer attribute,
which represents the return code set by the compiler for Integrated Language Environment
(ILE) languages.
**/
    public static final String PRODUCT_RETURN_CODE             = "PRODUCT_RETURN_CODE";

    static {
        attributes_.add(PRODUCT_RETURN_CODE, Integer.class, true);
        getterMap_.add(PRODUCT_RETURN_CODE, JOBI0600_, "receiverVariable.productReturnCode");
    }



/**
Attribute ID for program return code.  This identifies a read-only Integer attribute,
which represents the completion status of the last RPG, COBOL, data file utility (DFU),
or sort utility program that has finished running.
**/
    public static final String PROGRAM_RETURN_CODE             = "PROGRAM_RETURN_CODE";

    static {
        attributes_.add(PROGRAM_RETURN_CODE, Integer.class, true);
        getterMap_.add(PROGRAM_RETURN_CODE, JOBI0600_, "receiverVariable.programReturnCode");
    }



/**
Attribute ID for routing data.  This identifies a read-only String attribute,
which represents the routing data that is used to determine the routing entry
that identifies the program to start for the routing step.
**/
    public static final String ROUTING_DATA             = "ROUTING_DATA";

    static {
        attributes_.add(ROUTING_DATA, String.class, true);
        getterMap_.add(ROUTING_DATA, JOBI0400_, "receiverVariable.routingData");
    }



/**
Attribute ID for run priority.  This identifies an Integer attribute,
which represents the priority at which the job is currently running,
relative to other jobs on the system.  The run priority ranges from
1 (highest priority) to 99 (lowest priority).
**/
    public static final String RUN_PRIORITY             = "RUN_PRIORITY";

    static {
        attributes_.add(RUN_PRIORITY, Integer.class, false);
        getterMap_.add(RUN_PRIORITY, JOBI0200_, "receiverVariable.runPriority");
        setterKeys_.add(RUN_PRIORITY, 1802, ProgramKeys.BINARY);
    }



/**
Attribute ID for schedule date.  This identifies a Date attribute,
which represents the date and time the job is scheduled to become active.
The Date value is converted using the default Java locale.

<p>The following special values can be used when setting the schedule date:
<ul>
<li>{@link #SCHEDULE_DATE_CURRENT SCHEDULE_DATE_CURRENT} - The
    submitted job becomes eligible to run at the current date.
<li>{@link #SCHEDULE_DATE_MONTH_START SCHEDULE_DATE_MONTH_START} - The
    submitted job becomes eligible to run on the first day of
    the month.
<li>{@link #SCHEDULE_DATE_MONTH_END SCHEDULE_DATE_MONTH_END} - The
    submitted job becomes eligible to run on the last day of
    the month.
<li>{@link #SCHEDULE_DATE_MONDAY SCHEDULE_DATE_MONDAY} - The
    submitted job becomes eligible to run on Monday.
<li>{@link #SCHEDULE_DATE_TUESDAY SCHEDULE_DATE_TUESDAY} - The
    submitted job becomes eligible to run on Tuesday.
<li>{@link #SCHEDULE_DATE_WEDNESDAY SCHEDULE_DATE_WEDNESDAY} - The
    submitted job becomes eligible to run on Wednesday.
<li>{@link #SCHEDULE_DATE_THURSDAY SCHEDULE_DATE_THURSDAY} - The
    submitted job becomes eligible to run on Thursday.
<li>{@link #SCHEDULE_DATE_FRIDAY SCHEDULE_DATE_FRIDAY} - The
    submitted job becomes eligible to run on Friday.
<li>{@link #SCHEDULE_DATE_SATURDAY SCHEDULE_DATE_SATURDAY} - The
    submitted job becomes eligible to run on Saturday.
<li>{@link #SCHEDULE_DATE_SUNDAY SCHEDULE_DATE_SUNDAY} - The
    submitted job becomes eligible to run on Sunday.
</ul>
**/
    public static final String SCHEDULE_DATE             = "SCHEDULE_DATE";

    /**
    Attribute value indicating that the submitted job becomes eligible to run at the current date.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_CURRENT      = computeSpecialDate(0);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on the first day of
    the month.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_MONTH_START = computeSpecialDate(10);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on the last day of
    the month.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_MONTH_END   = computeSpecialDate(20);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on Monday.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_MONDAY      = computeSpecialDate(1);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on Tuesday.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_TUESDAY     = computeSpecialDate(2);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on Wednesday.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_WEDNESDAY   = computeSpecialDate(3);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on Thursday.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_THURSDAY    = computeSpecialDate(4);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on Friday.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_FRIDAY      = computeSpecialDate(5);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on Saturday.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_SATURDAY    = computeSpecialDate(6);

    /**
    Attribute value indicating that the submitted job becomes eligible to run on Sunday.

    @see #SCHEDULE_DATE
    **/
    public static final Date SCHEDULE_DATE_SUNDAY      = computeSpecialDate(7);

    static {
        attributes_.add(SCHEDULE_DATE, Date.class, false,
                        new Date[] { SCHEDULE_DATE_CURRENT,
                            SCHEDULE_DATE_MONTH_START, SCHEDULE_DATE_MONTH_END,
                            SCHEDULE_DATE_MONDAY, SCHEDULE_DATE_TUESDAY,
                            SCHEDULE_DATE_WEDNESDAY, SCHEDULE_DATE_THURSDAY,
                            SCHEDULE_DATE_FRIDAY, SCHEDULE_DATE_SATURDAY,
                            SCHEDULE_DATE_SUNDAY }, null, false, false,
                        // Hardcode the presentation keys, otherwise the literal
                        // values (which are strange dates) will be used.
                        new String[] { "CURRENT", "MONTHSTR", "MONTHEND",
                            "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" });
        getterMap_.add(SCHEDULE_DATE, JOBI0400_, "receiverVariable.dateAndTimeJobIsScheduledToRun", dateValueMapDts_);
        setterKeys_.add(SCHEDULE_DATE, 1920, ProgramKeys.CHAR, 10, new ScheduleDateValueMap_(DateValueMap.FORMAT_13));
        setterKeys_.add(SCHEDULE_DATE, 1921, ProgramKeys.CHAR, 8, new ScheduleDateValueMap_(DateValueMap.FORMAT_6));
    }

    private static Date computeSpecialDate(int second)
    {
        Calendar calendar = AS400Calendar.getGregorianInstance();
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTime();
    }

    private static class ScheduleDateValueMap_ extends DateValueMap
    {
        private static Hashtable constantMap_ = new Hashtable();

        static {
            constantMap_.put(SCHEDULE_DATE_CURRENT,         "*CURRENT");
            constantMap_.put(SCHEDULE_DATE_MONTH_START,     "*MONTHSTR");
            constantMap_.put(SCHEDULE_DATE_MONTH_END,       "*MONTHEND");
            constantMap_.put(SCHEDULE_DATE_MONDAY,          "*MON");
            constantMap_.put(SCHEDULE_DATE_TUESDAY,         "*TUE");
            constantMap_.put(SCHEDULE_DATE_WEDNESDAY,       "*WED");
            constantMap_.put(SCHEDULE_DATE_THURSDAY,        "*THU");
            constantMap_.put(SCHEDULE_DATE_FRIDAY,          "*FRI");
            constantMap_.put(SCHEDULE_DATE_SATURDAY,        "*SAT");
            constantMap_.put(SCHEDULE_DATE_SUNDAY,          "*SUN");
        }

        private boolean time_;

        public ScheduleDateValueMap_(int type)
        {
            super(type);
            time_ = (type == FORMAT_6);
        }

        public Object ltop(Object logicalValue, AS400 system)
        {
            if (constantMap_.containsKey(logicalValue)) {
                if ((time_) && (logicalValue.equals(SCHEDULE_DATE_CURRENT)))
                    return "*CURRENT";
                else if (!time_)
                    return constantMap_.get(logicalValue);
            }
            return super.ltop(logicalValue, system);
        }
    }




/**
Attribute ID for sequence number.  This identifies a read-only String attribute,
which represents the sequence number.  This attribute is part of the unit of work ID,
which is used to track jobs across multiple systems.  This is applicable only when
the job is associated with a source or target system using advanced program-to-program
communications (APPC).
**/
    public static final String SEQUENCE_NUMBER             = "SEQUENCE_NUMBER";

    static {
        attributes_.add(SEQUENCE_NUMBER, String.class, true);
        getterMap_.add(SEQUENCE_NUMBER, JOBI0400_, "receiverVariable.unitOfWorkID.sequenceNumber");
    }



/**
Attribute ID for server type.  This identifies a read-only String attribute,
which represents the type of server represented by the job, if the job
is part of a server.
**/
    public static final String SERVER_TYPE             = "SERVER_TYPE";

    static {
        attributes_.add(SERVER_TYPE, String.class, true);
        getterMap_.add(SERVER_TYPE, JOBI0400_, "receiverVariable.serverType");
    }



/**
Attribute ID for signed on job.  This identifies a read-only Boolean attribute,
which indicates whether the job is to be treated like a signed-on user on
the system.
**/
    public static final String SIGNED_ON_JOB             = "SIGNED_ON_JOB";

    static {
        attributes_.add(SIGNED_ON_JOB, Boolean.class, true);
        getterMap_.add(SIGNED_ON_JOB, JOBI0400_, "receiverVariable.signedOnJob", new BooleanValueMap(new Object[] { "1" }, new Object[] { "0", "" }));
    }



/**
Attribute ID for sort sequence table.  This identifies a String attribute,
which represents the fully qualified integrated file system path name of the
sort sequence table.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String SORT_SEQUENCE_TABLE             = "SORT_SEQUENCE_TABLE";

    static {
        attributes_.add(SORT_SEQUENCE_TABLE, String.class, false);
        ValueMap valueMap = new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "FILE");
        getterMap_.add(SORT_SEQUENCE_TABLE, JOBI0400_, "receiverVariable.sortSequence", valueMap);
        setterKeys_.add(SORT_SEQUENCE_TABLE, 1901, ProgramKeys.CHAR, 20, valueMap);
    }



/**
Attribute ID for special environment.  This identifies a read-only String attribute,
which indicates whether the job is running in a particular environment.
Possible values are:
<ul>
<li>{@link #NONE NONE} - The job is not running in any special environment.
<li>{@link #SPECIAL_ENVIRONMENT_SYSTEM_36 SPECIAL_ENVIRONMENT_SYSTEM_36} -
    The job is running in the System/36 environment.
<li>{@link #SPECIAL_ENVIRONMENT_NOT_ACTIVE SPECIAL_ENVIRONMENT_NOT_ACTIVE} -
    The job is not currently active.
</ul>
**/
    public static final String SPECIAL_ENVIRONMENT             = "SPECIAL_ENVIRONMENT";

    /**
    Attribute value indicating that the job is running in the System/36 environment.

    @see #SPECIAL_ENVIRONMENT
    **/
    public static final String SPECIAL_ENVIRONMENT_SYSTEM_36   = "*S36";

    /**
    Attribute value indicating that the job is not currently active.

    @see #SPECIAL_ENVIRONMENT
    **/
    public static final String SPECIAL_ENVIRONMENT_NOT_ACTIVE  = "";

    static {
        attributes_.add(SPECIAL_ENVIRONMENT, String.class, true,
                        new String[] { NONE,
                            SPECIAL_ENVIRONMENT_SYSTEM_36,
                            SPECIAL_ENVIRONMENT_NOT_ACTIVE }, null, true);
        getterMap_.add(SPECIAL_ENVIRONMENT, JOBI0600_, "receiverVariable.specialEnvironment");
    }



/**
Attribute ID for status message handling.  This identifies a String attribute,
which indicates whether status messages are displayed for this job.  Possible
values are:
<ul>
<li>{@link #NONE NONE} -
    This job does not display status messages.
<li>{@link #STATUS_MESSAGE_HANDLING_NORMAL STATUS_MESSAGE_HANDLING_NORMAL} -
    This job displays status messages.
</ul>

<p>The following special values can be used when setting the status message
handling:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QSTSMSG is used.
<li>{@link #USER_PROFILE USER_PROFILE} - The
    status message handling that is specified in the user profile under which this thread
    was initially running is used.
</ul>
**/
    public static final String STATUS_MESSAGE_HANDLING              = "STATUS_MESSAGE_HANDLING";

    /**
    Attribute value indicating that this job displays status messages.

    @see #STATUS_MESSAGE_HANDLING
    **/
    public static final String STATUS_MESSAGE_HANDLING_NORMAL       = "*NORMAL";

    static {
        attributes_.add(STATUS_MESSAGE_HANDLING, String.class, false,
                        new String[] { NONE,
                            STATUS_MESSAGE_HANDLING_NORMAL,
                            SYSTEM_VALUE,
                            USER_PROFILE }, null, true);
        getterMap_.add(STATUS_MESSAGE_HANDLING, JOBI0400_, "receiverVariable.statusMessageHandling");
        setterKeys_.add(STATUS_MESSAGE_HANDLING, 1902, ProgramKeys.CHAR, 10);
    }



/**
Attribute ID for submitted by job name.  This identifies a read-only String attribute,
which represents the job name of the submitter's job.
**/
    public static final String SUBMITTED_BY_JOB_NAME         = "SUBMITTED_BY_JOB_NAME";

    static {
        attributes_.add(SUBMITTED_BY_JOB_NAME, String.class, true);
        getterMap_.add(SUBMITTED_BY_JOB_NAME, JOBI0300_, "receiverVariable.submittersJob.jobName");
    }



/**
Attribute ID for submitted by job number.  This identifies a read-only String attribute,
which represents the job number of the submitter's job.
**/
    public static final String SUBMITTED_BY_JOB_NUMBER         = "SUBMITTED_BY_JOB_NUMBER";

    static {
        attributes_.add(SUBMITTED_BY_JOB_NUMBER, String.class, true);
        getterMap_.add(SUBMITTED_BY_JOB_NUMBER, JOBI0300_, "receiverVariable.submittersJob.jobNumber");
    }



/**
Attribute ID for submitted by user.  This identifies a read-only String attribute,
which represents the user name of the submitter's job.
**/
    public static final String SUBMITTED_BY_USER         = "SUBMITTED_BY_USER";

    static {
        attributes_.add(SUBMITTED_BY_USER, String.class, true);
        getterMap_.add(SUBMITTED_BY_USER, JOBI0300_, "receiverVariable.submittersJob.userName");
    }



/**
Attribute ID for subsystem.  This identifies a read-only String attribute,
which represents the fully qualified integrated file system path name of
the subsystem in which an active job is running.

@see com.ibm.as400.access.QSYSObjectPathName
**/
    public static final String SUBSYSTEM             = "SUBSYSTEM";

    static {
        attributes_.add(SUBSYSTEM, String.class, true);
        getterMap_.add(SUBSYSTEM, JOBI0600_, "receiverVariable.subsystemDescription",
                       new QualifiedValueMap(QualifiedValueMap.FORMAT_20, "SBSD"));
    }



/**
Attribute ID for system library list.  This identifies a read-only String[]
attribute, which represents the system portion of the library list of
the initial thread.
**/
    public static final String SYSTEM_LIBRARY_LIST   = "SYSTEM_LIBRARY_LIST";

    static {
        attributes_.add(SYSTEM_LIBRARY_LIST, String[].class, true);
        getterMap_.add(SYSTEM_LIBRARY_LIST, JOBI0700_, "receiverVariable.systemLibraryList", "receiverVariable.numberOfLibrariesInSystemLibraryList", arrayTypeValueMapString_);
    }



/**
Attribute ID for system pool ID.  This identifies a read-only Integer attribute,
which represents the identifier of the system-related pool from which the
job's main storage is allocated.
**/
    public static final String SYSTEM_POOL_ID             = "SYSTEM_POOL_ID";

    static {
        attributes_.add(SYSTEM_POOL_ID, Integer.class, true);
        getterMap_.add(SYSTEM_POOL_ID, JOBI0150_, "receiverVariable.systemPoolIdentifier");
        getterMap_.add(SYSTEM_POOL_ID, JOBI0200_, "receiverVariable.systemPoolIdentifier");
    }



/**
Attribute ID for temporary storage used.  This identifies a read-only Integer attribute,
which represents the amount of auxiliary storage (in megabytes) that is currently
allocated to this job.
**/
    public static final String TEMP_STORAGE_USED             = "TEMP_STORAGE_USED";

    static {
        attributes_.add(TEMP_STORAGE_USED, Integer.class, true);
        getterMap_.add(TEMP_STORAGE_USED, JOBI0150_, "receiverVariable.temporaryStorageUsedInMegabytes");
    }



/**
Attribute ID for thread count.  This identifies a read-only Integer attribute,
which represents the current number of active threads in the process.
**/
    public static final String THREAD_COUNT             = "THREAD_COUNT";

    static {
        attributes_.add(THREAD_COUNT, Integer.class, true);
        getterMap_.add(THREAD_COUNT, JOBI0150_, "receiverVariable.threadCount");
        getterMap_.add(THREAD_COUNT, JOBI0200_, "receiverVariable.threadCount");
    }



/**
Attribute ID for time separator.  This identifies a String attribute, which
represents the value used to separate hours, minutes, and seconds when presenting
a time.

<p>The following special value can be used when setting the time separator:
<ul>
<li>{@link #TIME_SEPARATOR_SYSTEM_VALUE TIME_SEPARATOR_SYSTEM_VALUE} - The
    system value QTIMSEP is used.
</ul>
**/
    public static final String TIME_SEPARATOR                           = "TIME_SEPARATOR";

    /**
    Attribute value indicating that the system value QTIMSEP is used.

    @see #TIME_SEPARATOR
    **/
    public static final String TIME_SEPARATOR_SYSTEM_VALUE              = "S";

    static {
        attributes_.add(TIME_SEPARATOR, String.class, false,
                        new String[] { TIME_SEPARATOR_SYSTEM_VALUE }, null, false);
        getterMap_.add(TIME_SEPARATOR, JOBI0400_, "receiverVariable.timeSeparator");
        setterKeys_.add(TIME_SEPARATOR, 2001, ProgramKeys.CHAR, 1);
    }



/**
Attribute ID for time slice.  This identifies an Integer attribute, which
represents the maximum amount of processor time (in milliseconds) given to
each thread in this job before other threads in this job and in other
jobs are given the opportunity to run.
**/
    public static final String TIME_SLICE                           = "TIME_SLICE";

    static {
        attributes_.add(TIME_SLICE, Integer.class, false);
        getterMap_.add(TIME_SLICE, JOBI0100_, "receiverVariable.timeSlice");
        setterKeys_.add(TIME_SLICE, 2002, ProgramKeys.BINARY);
    }



/**
Attribute ID for time slice end pool.  This identifies a String attribute, which
indicates whether a thread in an interactive job moves to another main storage
pool at the end of its time slice.  Possible values are:
<ul>
<li>{@link #NONE NONE} -
    A thread in the job does not move to another main storage pool when it reaches
    the end of its time slice.
<li>{@link #TIME_SLICE_END_POOL_BASE TIME_SLICE_END_POOL_BASE} -
    A thread in the job moves to the base pool when it reaches
    the end of its time slice.
</ul>

<p>The following special value can be used when setting the time slice end pool:
<ul>
<li>{@link #SYSTEM_VALUE SYSTEM_VALUE} - The
    system value QTSEPOOL is used.
</ul>
**/
    public static final String TIME_SLICE_END_POOL                           = "TIME_SLICE_END_POOL";

    /**
    Attribute value indicating that a thread in the job moves to the base pool when it reaches
    the end of its time slice.

    @see #TIME_SLICE_END_POOL
    **/
    public static final String TIME_SLICE_END_POOL_BASE                      = "*BASE";

    static {
        attributes_.add(TIME_SLICE_END_POOL, String.class, false,
                        new String[] { NONE,
                            TIME_SLICE_END_POOL_BASE,
                            SYSTEM_VALUE }, null, true);
        getterMap_.add(TIME_SLICE_END_POOL, JOBI0150_, "receiverVariable.timeSliceEndPool");
        setterKeys_.add(TIME_SLICE_END_POOL, 2003, ProgramKeys.CHAR, 10);
    }



/**
Attribute ID for total response time.  This identifies a read-only Integer attribute,
which represents the total amount of response time (in milliseconds) for the
initial thread.
**/
    public static final String TOTAL_RESPONSE_TIME                           = "TOTAL_RESPONSE_TIME";

    static {
        attributes_.add(TOTAL_RESPONSE_TIME, Integer.class, true);
        getterMap_.add(TOTAL_RESPONSE_TIME, JOBI0200_, "receiverVariable.responseTimeTotal");
    }



/**
Attribute ID for user library list.  This identifies a read-only String[]
attribute, which represents the user portion of the library list of
the initial thread.
**/
    public static final String USER_LIBRARY_LIST   = "USER_LIBRARY_LIST";

    static {
        attributes_.add(USER_LIBRARY_LIST, String[].class, true);
        getterMap_.add(USER_LIBRARY_LIST, JOBI0700_, "receiverVariable.userLibraryList", "receiverVariable.numberOfLibrariesInUserLibraryList", arrayTypeValueMapString_);
    }



/**
Attribute ID for user name.  This identifies a read-only String attribute,
which represents the user name of the job, which is the same as the name
of the user profile under which the job was started.
**/
    public static final String USER_NAME                     = "USER_NAME";

    static {
        attributes_.add(USER_NAME, String.class, true);
        getterMap_.add(USER_NAME, JOBI0100_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0150_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0200_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0300_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0400_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0500_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0600_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0700_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0800_, "receiverVariable.qualifiedJobName.userName");
        getterMap_.add(USER_NAME, JOBI0900_, "receiverVariable.qualifiedJobName.userName");
    }



/**
Attribute ID for user return code.  This identifies a read-only Integer attribute,
which represents the user-defined return code set by ILE high-level language
constructs.
**/
    public static final String USER_RETURN_CODE                     = "USER_RETURN_CODE";

    static {
        attributes_.add(USER_RETURN_CODE, Integer.class, true);
        getterMap_.add(USER_RETURN_CODE, JOBI0600_, "receiverVariable.userReturnCode");
    }



//-----------------------------------------------------------------------------------------
// PCML document initialization.
//-----------------------------------------------------------------------------------------

    private static final String             DOCUMENT_NAME_      = "com.ibm.as400.resource.RJob";
    private static ProgramCallDocument      staticDocument_     = null;

    static {
        // Create a static version of the PCML document, then clone it for each document.
        // This will improve performance, since we will only have to deserialize the PCML
        // object once.
        try {
            staticDocument_ = new ProgramCallDocument();
            staticDocument_.setDocument(DOCUMENT_NAME_);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error instantiating ProgramCallDocument", e);
        }
    }


//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

    private static final byte[]                 BLANK_INTERNAL_JOB_ID_      = new byte[] {
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40,
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40,
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40,
                                                                                (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40
                                                                              };
    private byte[]                  internalJobID_      = null;
    private String                  name_               = "*";
    private String                  number_             = "";
    private String                  user_               = "";

    private ProgramAttributeGetter          attributeGetter_    = null;
    private ProgramKeyAttributeSetter       attributeSetter_    = null;


//-----------------------------------------------------------------------------------------
// Constructors.
//-----------------------------------------------------------------------------------------

/**
Constructs an RJob object.
**/
    public RJob()
    {
        super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
    }



/**
Constructs an RJob object.

@param system The system.
**/
    public RJob(AS400 system)
    {
        this();

        try {
            setSystem(system);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }
    }



/**
Constructs an RJob object.

@param system       The system.
@param name         The job name.  Specify "*" to indicate <a href="#default">the default job</a>.
@param user         The user name.  This must be blank if name is "*".
@param number       The job number.  This must be blank if name is "*".
**/
    public RJob(AS400 system,
               String name,
               String user,
               String number)
    {
        this();

        try {
            setSystem(system);
            setName(name);
            setUser(user);
            setNumber(number);
        }
        catch(PropertyVetoException e) {
            // Ignore.
        }

        if(name.equals("*")) {
            if (user.trim() != "")
                throw new ExtendedIllegalArgumentException("user", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            if (number.trim() != "")
                throw new ExtendedIllegalArgumentException("number", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
    }



/**
Constructs an RJob object.  This sets the job name to "*INT".

@param system           The system.
@param internalJobID    The internal job identifier.
**/
    public RJob(AS400 system, byte[] internalJobID)
    {
        this();

        try {
            setSystem(system);
            setName("*INT");
            setInternalJobID(internalJobID);
        }
        catch(PropertyVetoException e) {
            // Ignore.

        }
    }



// @A2C
/**
Commits the specified attribute changes.

@param attributeIDs     The attribute IDs for the specified attribute changes.
@param values           The specified attribute changes
@param bidiStringTypes  The bidi string types as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
                        
@exception ResourceException                If an error occurs.
**/
    protected void commitAttributeChanges(Object[] attributeIDs, Object[] values, int[] bidiStringTypes)
    throws ResourceException
    {
        super.commitAttributeChanges(attributeIDs, values, bidiStringTypes);

        // Establish the connection if needed.
        if (! isConnectionEstablished())
            establishConnection();

        attributeSetter_.setValues(attributeIDs, values, bidiStringTypes);
    }



/**
Computes the resource key.

@param system       The system.
@param name         The job name.
@param user         The user name.
@param number       The job number.
@param internalJobID The internal job identifier.
**/
    static Object computeResourceKey(AS400 system, String name, String user, String number, byte[] internalJobID)
    {
        if (internalJobID == null) {
           StringBuffer buffer = new StringBuffer();
            buffer.append(RJob.class);
            buffer.append(':');
            buffer.append(system.getSystemName());
            buffer.append(':');
            buffer.append(system.getUserId());
            buffer.append(':');
            buffer.append(number);
            buffer.append('/');
            buffer.append(user);
            buffer.append('/');
            buffer.append(name);
            return buffer.toString();
        }
        else
            return internalJobID;
    }



/**
Creates the resource using the specified attribute values.  This method
is not supported for this class and always throws a ResourceException.

@exception ResourceException                If an error occurs.
**/
    public void createResource(Object[] attributeIDs, Object[] values)
    throws ResourceException
    {
        throw new ResourceException(ResourceException.OPERATION_NOT_SUPPORTED);
    }



/**
Ends the job controlled.  The program running in the job
is allowed to perform some cleanup and end of job processing.

@exception ResourceException    If an error occurs.
**/
    public void end()
        throws ResourceException
    {
        endInternal(-1);
    }



/**
Ends the job.

@param delayTime    The amount of time (in seconds) allowed for the job to complete
                    its cleanup and end of job processing during a controlled end.
                    If the cleanup is not completed before the end of the delay time,
                    the job is ended immediately.  Specify 0 to end the job
                    immediately.

@exception ResourceException    If an error occurs.
**/
    public void end(int delayTime)
        throws ResourceException
    {
        // Validate the parameters.
        if (delayTime < 0)
            throw new ExtendedIllegalArgumentException("delayTime", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

        endInternal(delayTime);
    }



    private void endInternal(int delayTime)
    throws ResourceException
    {
        // Validate the properties.
        if (name_ == null)
            throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        if (number_ == null)
            throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        if (user_ == null)
            throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);

        // Establish the connection if needed.
        if (!isConnectionEstablished())
            establishConnection();

        // Issue the ENDJOB CL command.
        AS400 sys = null;
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("ENDJOB JOB(");
            buffer.append(number_);
            buffer.append('/');
            buffer.append(user_);
            buffer.append('/');
            buffer.append(name_);
            buffer.append(") OPTION(");
            if (delayTime == 0) {
                buffer.append("*IMMED)");
            }
            else {
                buffer.append("*CNTRLD)");
                if (delayTime > 0) {
                    buffer.append(" DELAY(");
                    buffer.append(delayTime);
                    buffer.append(")");
                }
            }
            String endJob = buffer.toString();

            if (Trace.isTraceOn())
                Trace.log(Trace.INFORMATION, "Ending the job:" + endJob);

            // Use a separate connection, in case the job we're trying to
            // end is the Remote Command Call host server.             @A1a
            sys = new AS400(getSystem());                            //@A1a
            CommandCall commandCall = new CommandCall(sys, endJob);  //@A1c
            boolean success = commandCall.run();
            if (!success)
                throw new ResourceException(commandCall.getMessageList());
        }
        catch(Exception e) {
            throw new ResourceException(e);
        }
        finally {
            if (sys != null)  sys.disconnectAllServices();
        }
    }



/**
Establishes the connection to the system.

<p>The method is called by the resource framework automatically
when the connection needs to be established.

@exception ResourceException                If an error occurs.
**/
    protected void establishConnection()
    throws ResourceException
    {
        // Call the superclass.
        super.establishConnection();

        // Validate if we can establish the connection.
        if (internalJobID_ == null) {
            if (name_ == null)
                throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            if (number_ == null)
                throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            if (user_ == null)
                throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Initialize the PCML document.
        ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
        AS400 system = getSystem();
        try {
            document.setSystem(system);

            byte[] actualInternalJobID = (internalJobID_ == null) ? BLANK_INTERNAL_JOB_ID_ : internalJobID_;
            document.setValue("qusrjobi_jobi0100.internalJobIdentifier", actualInternalJobID);
            document.setValue("qusrjobi_jobi0150.internalJobIdentifier", actualInternalJobID);
            document.setValue("qusrjobi_jobi0200.internalJobIdentifier", actualInternalJobID);
            document.setValue("qusrjobi_jobi0300.internalJobIdentifier", actualInternalJobID);
            document.setValue("qusrjobi_jobi0400.internalJobIdentifier", actualInternalJobID);
            document.setValue("qusrjobi_jobi0500.internalJobIdentifier", actualInternalJobID);
            document.setValue("qusrjobi_jobi0600.internalJobIdentifier", actualInternalJobID);
            document.setValue("qusrjobi_jobi0700.internalJobIdentifier", actualInternalJobID);
            document.setValue("qwtchgjb.internalJobIdentifier", actualInternalJobID);

            String actualName = (internalJobID_ == null) ? name_.toUpperCase() : "*INT";
            document.setValue("qusrjobi_jobi0100.qualifiedJobName.jobName", actualName);
            document.setValue("qusrjobi_jobi0150.qualifiedJobName.jobName", actualName);
            document.setValue("qusrjobi_jobi0200.qualifiedJobName.jobName", actualName);
            document.setValue("qusrjobi_jobi0300.qualifiedJobName.jobName", actualName);
            document.setValue("qusrjobi_jobi0400.qualifiedJobName.jobName", actualName);
            document.setValue("qusrjobi_jobi0500.qualifiedJobName.jobName", actualName);
            document.setValue("qusrjobi_jobi0600.qualifiedJobName.jobName", actualName);
            document.setValue("qusrjobi_jobi0700.qualifiedJobName.jobName", actualName);
            document.setValue("qwtchgjb.qualifiedJobName.jobName", actualName);

            String actualNumber = (internalJobID_ == null) ? number_ : "";
            document.setValue("qusrjobi_jobi0100.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qusrjobi_jobi0150.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qusrjobi_jobi0200.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qusrjobi_jobi0300.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qusrjobi_jobi0400.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qusrjobi_jobi0500.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qusrjobi_jobi0600.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qusrjobi_jobi0700.qualifiedJobName.jobNumber", actualNumber);
            document.setValue("qwtchgjb.qualifiedJobName.jobNumber", actualNumber);

            String actualUser = (internalJobID_ == null) ? user_.toUpperCase() : "";
            document.setValue("qusrjobi_jobi0100.qualifiedJobName.userName", actualUser);
            document.setValue("qusrjobi_jobi0150.qualifiedJobName.userName", actualUser);
            document.setValue("qusrjobi_jobi0200.qualifiedJobName.userName", actualUser);
            document.setValue("qusrjobi_jobi0300.qualifiedJobName.userName", actualUser);
            document.setValue("qusrjobi_jobi0400.qualifiedJobName.userName", actualUser);
            document.setValue("qusrjobi_jobi0500.qualifiedJobName.userName", actualUser);
            document.setValue("qusrjobi_jobi0600.qualifiedJobName.userName", actualUser);
            document.setValue("qusrjobi_jobi0700.qualifiedJobName.userName", actualUser);
            document.setValue("qwtchgjb.qualifiedJobName.userName", actualUser);
        }
        catch(PcmlException e) {
            if (Trace.isTraceOn())
                Trace.log(Trace.ERROR, "Error setting PCML document values", e);
        }

        // Initialize the attribute getter.
        attributeGetter_ = new ProgramAttributeGetter(system, document, getterMap_);

        // Initialize the attribute setter.
        attributeSetter_ = new ProgramKeyAttributeSetter(system, document, "qwtchgjb", "jobChangeInformation", setterKeys_);
    }



/**
Freezes any property changes.  After this is called, property
changes should not be made.  Properties are not the same thing
as attributes.  Properties are basic pieces of information
which must be set to make the object usable, such as the system,
job name, job number, and user name.

<p>The method is called by the resource framework automatically
when the properties need to be frozen.

@exception ResourceException                If an error occurs.
**/
    protected void freezeProperties()
    throws ResourceException
    {
        // Validate if we can establish the connection.
        if (internalJobID_ == null) {
            if (name_ == null)
                throw new ExtendedIllegalStateException("name", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            if (number_ == null)
                throw new ExtendedIllegalStateException("number", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            if (user_ == null)
                throw new ExtendedIllegalStateException("user", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        // Update the resource key.
        if (getResourceKey() == null)
            setResourceKey(computeResourceKey(getSystem(), name_, user_, number_, internalJobID_));

        // Call the superclass.
        super.freezeProperties();
    }



// @A2C
/**
Returns the unchanged value of an attribute.   If the attribute
value has an uncommitted change, this returns the unchanged value.
If the attribute value does not have an uncommitted change, this
returns the same value as <b>getAttributeValue()</b>.

@param attributeID  Identifies the attribute.
@param bidiStringType   The bidi string type as defined by the CDRA (Character Data 
                        Representataion Architecture). See 
                        {@link com.ibm.as400.access.BidiStringType BidiStringType}
                        for more information and valid values. 
@return             The attribute value, or null if the attribute
                    value is not available.

@exception ResourceException                If an error occurs.
**/
    public Object getAttributeUnchangedValue(Object attributeID, int bidiStringType)
    throws ResourceException
    {
        Object value = super.getAttributeUnchangedValue(attributeID, bidiStringType);
        if (value == null) {

            // Establish the connection if needed.
            if (! isConnectionEstablished())
                establishConnection();

            value = attributeGetter_.getValue(attributeID, bidiStringType);
        }
        return value;
    }



/**
Returns the internal job identifier.

@return The internal job identifier, or null if none has been set.
**/
    public byte[] getInternalJobID()
    {
        return internalJobID_;
    }



/**
Returns the job name.

@return The job name, or "*" if none has been set.
**/
    public String getName()
    {
        return name_;
    }



/**
Returns the job number.

@return The job number, or "" if none has been set.
**/
    public String getNumber()
    {
        return number_;
    }



/**
Returns the user name.

@return The user name, or "" if none has been set.
**/
    public String getUser()
    {
        return user_;
    }



// @A2A
/**
Indicates if this resource is enabled for bidirectional character conversion.
This always returns true.

@return Always true.
**/
    protected boolean isBidiEnabled()
    {
        return true;
    }



/**
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.  This method fires an attributeValuesRefreshed()
ResourceEvent.

@exception ResourceException                If an error occurs.
**/
    public void refreshAttributeValues()
    throws ResourceException
    {
        super.refreshAttributeValues();

        if (attributeGetter_ != null)
            attributeGetter_.clearBuffer();
    }



/**
Sets the internal job identifier.  This does not change
the job on the system.  Instead, it changes the job
that this object references.  The job name
must be set to "*INT" for this to be recognized.
This cannot be changed if the object has established
a connection to the system.

@param internalJobID    The internal job identifier.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setInternalJobID(byte[] internalJobID)
        throws PropertyVetoException
    {
        if (internalJobID == null)
            throw new NullPointerException("internalJobID");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        byte[] oldValue = internalJobID_;
        fireVetoableChange("internalJobID", oldValue, internalJobID);
        internalJobID_ = internalJobID;
        firePropertyChange("internalJobID", oldValue, internalJobID);

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setName("*INT");
        presentation.setFullName(toString());
    }



/**
Sets the job name.  This does not change the job on
the system.  Instead, it changes the job
that this object references.   This cannot be changed
if the object has established a connection to the system.

@param name    The job name.  Specify "*" to indicate the job this
               program running in, or "*INT" to indicate that the job
               is specified using the internal job identifier.

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setName(String name)
        throws PropertyVetoException
    {
        if (name == null)
            throw new NullPointerException("name");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = name_;
        fireVetoableChange("name", oldValue, name);
        name_ = name;
        firePropertyChange("name", oldValue, name);

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setName(name_);
        presentation.setFullName(toString());
    }



/**
Sets the job number.  This does not change the job on
the system.  Instead, it changes the job
that this object references.  This cannot be changed
if the object has established a connection to the system.

@param number    The job number.  This must be blank if the job name is "*".

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setNumber(String number)
        throws PropertyVetoException
    {
        if (number == null)
            throw new NullPointerException("number");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = number_;
        fireVetoableChange("number", oldValue, number);
        number_ = number;
        firePropertyChange("number", oldValue, number);

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setFullName(toString());
    }



/**
Sets the user name.  This does not change the job on
the system.  Instead, it changes the job
that this object references.  This cannot be changed
if the object has established a connection to the system.

@param user    The user name.  This must be blank if the job name is "*".

@exception PropertyVetoException    If the property change is vetoed.
**/
    public void setUser(String user)
        throws PropertyVetoException
    {
        if (user == null)
            throw new NullPointerException("user");
        if (arePropertiesFrozen())
            throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);

        String oldValue = user_;
        fireVetoableChange("user", oldValue, user);
        user_ = user;
        firePropertyChange("user", oldValue, user);

        // Update the presentation.
        Presentation presentation = getPresentation();
        presentation.setFullName(toString());
    }



/**
Returns the string representation in the format
"number/user/name".

@return The string representation.
**/
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(number_);
        buffer.append('/');
        buffer.append(user_);
        buffer.append('/');
        buffer.append(name_);
        return buffer.toString();
    }



}
