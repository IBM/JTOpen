///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: Job.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
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
import java.text.SimpleDateFormat;


/**
The Job class represents an OS/400 job.  In order to access a job,
the system and either the job name, user name, and job number or
internal job identifier need to be set.  A valid combination of
these must be set by getting or setting any of the job's attributes.

<p>Some of the attributes have associated get and set methods
defined in this class.  These are provided for backwards compatibility
with previous versions of the IBM Toolbox for Java.  The complete
set of attribute values can be accessed using the public constants.

<p>Note: To obtain information about the job in which an
OS/400 program or command runs, do something like the following:
<pre>
AS400 sys = new AS400();
ProgramCall pgm = new ProgramCall(sys);
pgm.setThreadSafe(true); // indicates the program is to be run on-thread
String jobNumber = pgm.getJob().getNumber();
</pre>

@see com.ibm.as400.access.JobList
@see CommandCall#getJob
@see ProgramCall#getJob
**/
public class Job
implements Serializable
{


  static final long serialVersionUID = 5L;

//-----------------------------------------------------------------------------------------
// Private data.
//-----------------------------------------------------------------------------------------

  private boolean cacheChanges_ = true;

  private boolean isConnected_;

  private final JobHashtable values_ = new JobHashtable();
  private JobHashtable cachedChanges_ = null;

  private String name_, user_, number_, status_, type_, subtype_;
  private AS400 system_;
  private String internalJobID_;

  private transient PropertyChangeSupport propertyChangeSupport_;
  private transient VetoableChangeSupport vetoableChangeSupport_;

  public static final int ACCOUNTING_CODE = 1001;
  public static final int ACTIVE_JOB_STATUS = 101;
  public static final int ACTIVE_JOB_STATUS_FOR_JOBS_ENDING = 103;
  public static final int ALLOW_MULTIPLE_THREADS = 102;
  public static final int AUXILIARY_IO_REQUESTS  = 1401;
  public static final int AUXILIARY_IO_REQUESTS_LARGE = 1406;
  public static final int BREAK_MESSAGE_HANDLING = 201;
  public static final int CCSID = 302;
  public static final int CHARACTER_ID_CONTROL = 311;
  public static final int CLIENT_IP_ADDRESS = 318;
  public static final int COMPLETION_STATUS = 306;
  public static final int CONTROLLED_END_REQUESTED = 502; // End status
  public static final int COUNTRY_ID = 303;
  public static final int CPU_TIME_USED = 304;
  public static final int CPU_TIME_USED_LARGE = 312;
  public static final int CPU_TIME_USED_FOR_DATABASE = 313;
  public static final int CURRENT_LIBRARY = 10000; // Cannot preload
  public static final int CURRENT_LIBRARY_EXISTENCE = 10001; // Cannot preload
  public static final int CURRENT_SYSTEM_POOL_ID = 307;
  public static final int CURRENT_USER = 305;
  public static final int DATE_ENDED = 418;
  public static final int DATE_ENTERED_SYSTEM = 402; 
  public static final int DATE_FORMAT = 405;
  public static final int DATE_SEPARATOR = 406;
  public static final int DATE_STARTED = 401;
  public static final int DBCS_CAPABLE = 407;
  public static final int DECIMAL_FORMAT = 413;
  public static final int DEFAULT_CCSID = 412;
  public static final int DEFAULT_WAIT_TIME = 409;
  public static final int DEVICE_RECOVERY_ACTION = 410;
  public static final int ELIGIBLE_FOR_PURGE = 1604;
  public static final int END_SEVERITY = 501;
  public static final int FUNCTION_NAME = 601;
  public static final int FUNCTION_TYPE = 602;
  public static final int INQUIRY_MESSAGE_REPLY = 901;
  public static final int INSTANCE = 21011; // Unit of work ID
  public static final int INTERACTIVE_TRANSACTIONS = 1402;
  public static final int INTERNAL_JOB_ID = 11000; // Always gets loaded
  public static final int JOB_DATE = 1002;
  public static final int JOB_DESCRIPTION = 1003;
  public static final int JOB_END_REASON = 1014;
  public static final int JOB_LOG_PENDING = 1015;
  public static final int JOB_NAME = 11001; // Always gets loaded
  public static final int JOB_NUMBER = 11002; // Always gets loaded
  public static final int JOB_QUEUE = 1004;
  public static final int JOB_QUEUE_DATE = 404;
  public static final int JOB_QUEUE_PRIORITY = 1005;
  public static final int JOB_QUEUE_STATUS = 1903;
  public static final int JOB_STATUS = 11003; // Always gets loaded
  public static final int JOB_SUBTYPE = 11004; // Always gets loaded
  public static final int JOB_SWITCHES = 1006;
  public static final int JOB_TYPE = 11005; // Always gets loaded
  public static final int JOB_TYPE_ENHANCED = 1016;
  public static final int JOB_USER_IDENTITY = 1012;
  public static final int JOB_USER_IDENTITY_SETTING = 1013;
  public static final int KEEP_DDM_CONNECTIONS_ACTIVE = 408; // DDM conversation handling
  public static final int LANGUAGE_ID = 1201;
  public static final int LOCATION_NAME = 21012; // Unit of work ID
  public static final int LOG_CL_PROGRAMS = 1203;
  public static final int LOGGING_LEVEL = 1202;
  public static final int LOGGING_SEVERITY = 1204;
  public static final int LOGGING_TEXT = 1205;
  public static final int MAX_CPU_TIME = 1302;
  public static final int MAX_TEMP_STORAGE = 1303;
  public static final int MEMORY_POOL = 1306;
  public static final int MESSAGE_REPLY = 1307;
  public static final int MESSAGE_QUEUE_ACTION = 1007;
  public static final int MESSAGE_QUEUE_MAX_SIZE = 1008;
  public static final int MODE = 1301;
  public static final int NETWORK_ID = 21013; // Unit of work ID
  public static final int OUTPUT_QUEUE = 1501;
  public static final int OUTPUT_QUEUE_PRIORITY = 1502;
  public static final int PRINT_KEY_FORMAT = 1601;
  public static final int PRINT_TEXT = 1602;
  public static final int PRINTER_DEVICE_NAME = 1603;
  public static final int PRODUCT_LIBRARIES = 10002; // Cannot preload
  public static final int PRODUCT_RETURN_CODE = 1605;
  public static final int PROGRAM_RETURN_CODE = 1606;
  public static final int ROUTING_DATA = 1803;
  public static final int RUN_PRIORITY = 1802;
  public static final int SCHEDULE_DATE = 1920;
  private static final int SCHEDULE_TIME = 1921;
  private static final int SCHEDULE_DATE_GETTER = 403;
  public static final int SEQUENCE_NUMBER = 21014; // Unit of work ID
  public static final int SERVER_TYPE = 1911;
  public static final int SIGNED_ON_JOB = 701;
  public static final int SORT_SEQUENCE_TABLE = 1901;
  public static final int SPECIAL_ENVIRONMENT = 1908;
  //public static final int SQL_SERVER_MODE = 1922;
  public static final int STATUS_MESSAGE_HANDLING = 1902;
  public static final int SUBMITTED_BY_JOB_NAME = 1904;
  public static final int SUBMITTED_BY_JOB_NUMBER = 10005; // Cannot preload
  public static final int SUBMITTED_BY_USER = 10006; // Cannot preload
  public static final int SUBSYSTEM = 1906;
  public static final int SYSTEM_POOL_ID = 1907;
  public static final int SYSTEM_LIBRARY_LIST = 10003; // Cannot preload
  public static final int TEMP_STORAGE_USED = 2004;
  public static final int THREAD_COUNT = 2008;
  public static final int TIME_SEPARATOR = 2001;
  public static final int TIME_SLICE = 2002;
  public static final int TIME_SLICE_END_POOL = 2003;
  public static final int TOTAL_RESPONSE_TIME = 1801;
  private static final int UNIT_OF_WORK_ID = 2101; // This is the real key.
  public static final int USER_LIBRARY_LIST = 10004; // Cannot preload
  public static final int USER_NAME = 11006; // Always gets loaded
  public static final int USER_RETURN_CODE = 2102;



  public static final String SYSTEM_VALUE    = "*SYSVAL";
//    public static final String USER_PROFILE    = "*USRPRF";
  public static final String YES             = "*YES";
  public static final String NO              = "*NO";
  public static final String NONE            = "*NONE";

  public static final String BREAK_MESSAGE_HANDLING_NORMAL            = "*NORMAL";
  public static final String BREAK_MESSAGE_HANDLING_HOLD              = "*HOLD";
  public static final String BREAK_MESSAGE_HANDLING_NOTIFY            = "*NOTIFY";

  public static final String DATE_FORMAT_SYSTEM_VALUE                 = "*SYS";
  public static final String DATE_FORMAT_YMD                          = "*YMD";
  public static final String DATE_FORMAT_MDY                          = "*MDY";
  public static final String DATE_FORMAT_DMY                          = "*DMY";
  public static final String DATE_FORMAT_JULIAN                       = "*JUL";

  public static final String KEEP_DDM_CONNECTIONS_ACTIVE_KEEP        = "*KEEP";
  public static final String KEEP_DDM_CONNECTIONS_ACTIVE_DROP        = "*DROP";

  public static final String DECIMAL_FORMAT_PERIOD                    = "";
  public static final String DECIMAL_FORMAT_COMMA_I                   = "I";
  public static final String DECIMAL_FORMAT_COMMA_J                   = "J";

  public static final String DEVICE_RECOVERY_ACTION_MESSAGE               = "*MSG";
  public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE    = "*DSCMSG";
  public static final String DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST= "*DSCENDRQS";
  public static final String DEVICE_RECOVERY_ACTION_END_JOB               = "*ENDJOB";
  public static final String DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST       = "*ENDJOBNOLIST";

  public static final String INQUIRY_MESSAGE_REPLY_REQUIRED          = "*RQD";
  public static final String INQUIRY_MESSAGE_REPLY_DEFAULT           = "*DFT";
  public static final String INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST = "*SYSRPYL";

  public static final String MESSAGE_QUEUE_ACTION_NO_WRAP               = "*NOWRAP";
  public static final String MESSAGE_QUEUE_ACTION_WRAP                  = "*WRAP";
  public static final String MESSAGE_QUEUE_ACTION_PRINT_WRAP            = "*PRTWRAP";

  public static final String LOGGING_TEXT_MESSAGE               = "*MSG";
  public static final String LOGGING_TEXT_SECLVL                = "*SECLVL";
  public static final String LOGGING_TEXT_NO_LIST               = "*NOLIST";

  public static final String PRINT_KEY_FORMAT_BORDER          = "*PRTBDR";
  public static final String PRINT_KEY_FORMAT_HEADER          = "*PRTHDR";
  public static final String PRINT_KEY_FORMAT_ALL             = "*PRTALL";



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
  Constructs a Job object.
  **/
  public Job()
  {
  }



/**
Constructs a Job object.

@param system The system.
**/
  public Job(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
  }



/**
Constructs a Job object.

@param system       The system.
@param name         The job name.  Specify "*" to indicate the job that this
                    program running in.
@param user         The user name.  This must be blank if name is "*".
@param number       The job number.  This must be blank if name is "*".
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
    if (jobName.equals("*"))
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
    setValueInternal(INTERNAL_JOB_ID, null);
  }



/**
Constructs a Job object.  This sets the job name to "*INT".

@param system           The system.
@param internalJobID    The internal job identifier.
**/
  public Job(AS400 system, String internalJobID)
  {
    if (system == null) throw new NullPointerException("system");
    if (internalJobID == null) throw new NullPointerException("internalJobID");

    system_ = system;
    internalJobID_ = internalJobID;
    setValueInternal(INTERNAL_JOB_ID, internalJobID);
    name_ = "*INT";
    setValueInternal(JOB_NAME, null);
    user_ = "";
    setValueInternal(USER_NAME, null);
    number_ = "";
    setValueInternal(JOB_NUMBER, null);
  }



/**
Constructs a Job object.
**/
//
// This is a package scope constructor!
//
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
Adds a PropertyChangeListener.  The specified PropertyChangeListener's
<b>propertyChange()</b> method will be called each time the value of
any bound property is changed.

@param listener The listener.
*/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (propertyChangeSupport_ == null) propertyChangeSupport_ = new PropertyChangeSupport(this);
    propertyChangeSupport_.addPropertyChangeListener(listener);
  }



/**
Adds a VetoableChangeListener.  The specified VetoableChangeListener's
<b>vetoableChange()</b> method will be called each time the value of
any constrained property is changed.

@param listener The listener.
*/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (vetoableChangeSupport_ == null) vetoableChangeSupport_ = new VetoableChangeSupport(this);
    vetoableChangeSupport_.addVetoableChangeListener(listener);
  }



/**
Commits all uncommitted attribute changes.

@exception AS400Exception                  If the AS/400 system returns an error message.
@exception AS400SecurityException          If a security or authority error occurs.
@exception ConnectionDroppedException      If the connection is dropped unexpectedly.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception InterruptedException            If this thread is interrupted.
@exception IOException                     If an error occurs while communicating with the AS/400.
@exception ObjectDoesNotExistException     If the AS/400 object does not exist.
@exception UnsupportedEncodingException    If the character encoding is not supported.
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
    if (cachedChanges_ == null || cachedChanges_.size_ == 0) return;
    ProgramParameter[] parmList = new ProgramParameter[5];
    int ccsid = system_.getCcsid();
    ConvTable table = ConvTable.getTable(ccsid, null);
    AS400Bin4 bin4 = new AS400Bin4();
    AS400Structure structure = new AS400Structure();
    AS400Text[] member = new AS400Text[3];
    member[0] = new AS400Text(10, ccsid, system_);
    member[1] = new AS400Text(10, ccsid, system_);
    member[2] = new AS400Text(6, ccsid, system_);
    structure.setMembers(member);
    String[] qualifiedJobName = { name_, user_, number_};
    parmList[0] = new ProgramParameter(structure.toBytes(qualifiedJobName));
    AS400Text text = new AS400Text(16, ccsid, system_);
    parmList[1] = new ProgramParameter(text.toBytes(internalJobID_));
    text = new AS400Text(8, ccsid, system_);
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
            bin4.toBytes(data, parm3, offset);
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
    parmList[4] = new ProgramParameter(bin4.toBytes(0)); // error code

    ProgramCall program = new ProgramCall(system_, "/QSYS.LIB/QWTCHGJB.PGM", parmList);
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Setting job information for job "+toString());
    }
    if (!program.run())
    {
      AS400Message[] msgList = program.getMessageList();
      throw new AS400Exception(msgList);
    }
    cachedChanges_ = null;
  }


  /**
   * Ends this job.
   * To end the job controlled, specify -1.
   * To end the job immediately, specify 0.
   * Specify any other amount of delay time (seconds) allowed for the job to cleanup.
  **/
  public void end(int delay) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException
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

  // Helper method.
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


  // Helper method.
  void setAsInt(int key, int val)
  {
    setValueInternal(key, new Integer(val));
  }

  // Helper method.
  void setAsLong(int key, long val)
  {
    setValueInternal(key, new Long(val));
  }
  
  // Helper method.
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
        date = new Date(str);
        break;
    }
    return date;
  }


  private void setAsDate(int key, Date val)
  {
    //setValueInternal(key, val.toString());

    String dateString = null;
    SimpleDateFormat dateFormat;
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


  public Object getValue(int key) throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException
  {
    Object obj = values_.get(key);
    if (obj == null)
    {
      retrieve(key); // Need to retrieve it using QUSRJOBI
      obj = values_.get(key);
      if (obj == null && key == SCHEDULE_DATE)
      {
        Date d = getAsSystemDate(SCHEDULE_DATE_GETTER);
        setValueInternal(SCHEDULE_DATE, d);
      }
    }
    return obj;
  }

  private static String lookupFormatName(int key)
  {
    switch (key)
    {
      case JOB_NAME:
      case USER_NAME:
      case JOB_NUMBER:
      case INTERNAL_JOB_ID:
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
//      case MAX_THREADS:
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

      default:
        return null;
    }
  }

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
//    if (name == "JOBI1000") return 144;
    return -1;
  }

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

/*    name_ = table.byteArrayToString(data, 8, 10);
    user_ = table.byteArrayToString(data, 18, 10);
    number_ = table.byteArrayToString(data, 28, 6);
    internalJobID_ = table.byteArrayToString(data, 34, 16);
*/
    status_ = table.byteArrayToString(data, 50, 10);
    type_ = table.byteArrayToString(data, 60, 1);
    subtype_ = table.byteArrayToString(data, 61, 1);    

/*    setValueInternal(JOB_NAME, name_);
    setValueInternal(USER_NAME, user_);
    setValueInternal(JOB_NUMBER, number_);
    setValueInternal(INTERNAL_JOB_ID, internalJobID_);
*/
    setValueInternal(JOB_STATUS, status_);
    setValueInternal(JOB_TYPE, type_);
    setValueInternal(JOB_SUBTYPE, subtype_);

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
//      setAsInt(MAX_THREADS, BinaryConverter.byteArrayToInt(data, 120));
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
      setValueInternal(JOB_DESCRIPTION, table.byteArrayToString(data, 103, 10));

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
      
//      int offset = 80;
      int currentLibraryExistence = BinaryConverter.byteArrayToInt(data, 72);
      setAsInt(CURRENT_LIBRARY_EXISTENCE, currentLibraryExistence);
      int numberOfSystemLibraries = BinaryConverter.byteArrayToInt(data, 64);
//      String[] systemLibraries = new String[numberOfSystemLibraries];
//      for (int i=0; i<numberOfSystemLibraries; ++i)
//      {
//        systemLibraries[i] = table.byteArrayToString(data, offset, 11).trim();
//        offset += 11;
//      }
//      setValueInternal(SYSTEM_LIBRARY_LIST, systemLibraries);
      setValueInternal(SYSTEM_LIBRARY_LIST, table.byteArrayToString(data, 80, 11*numberOfSystemLibraries));
      int offset = 80 + 11*numberOfSystemLibraries;
      int numberOfProductLibraries = BinaryConverter.byteArrayToInt(data, 68);
//      String[] productLibraries = new String[numberOfProductLibraries];
//      for (int i=0; i<numberOfProductLibraries; ++i)
//      {
//        productLibraries[i] = table.byteArrayToString(data, offset, 11).trim();
//        offset += 11;
//      }
//      setValueInternal(PRODUCT_LIBRARIES, productLibraries);
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
//      String[] userLibraries = new String[numberOfUserLibraries];
//      for (int i=0; i<numberOfUserLibraries; ++i)
//      {
//        userLibraries[i] = table.byteArrayToString(data, offset, 11).trim();
//        offset += 11;
//      }
//      setValueInternal(USER_LIBRARY_LIST, userLibraries);
      setValueInternal(USER_LIBRARY_LIST, table.byteArrayToString(data, offset, 11*numberOfUserLibraries));
    }
  }


  private void retrieve(int key)  throws AS400Exception,
  AS400SecurityException,
  ConnectionDroppedException,
  ErrorCompletingRequestException,
  InterruptedException,
  IOException,
  ObjectDoesNotExistException,
  UnsupportedEncodingException

  {
    // First lookup the format to use for this key
    String format = lookupFormatName(key);
    int ccsid = system_.getCcsid();
    int receiverLength = lookupFormatLength(format);

    AS400Bin4 bin4 = new AS400Bin4();
    ProgramParameter[] parmList = new ProgramParameter[6];
    parmList[0] = new ProgramParameter(receiverLength);           
    parmList[1] = new ProgramParameter(bin4.toBytes(receiverLength));
    AS400Text text = new AS400Text(8, ccsid, system_);
    parmList[2] = new ProgramParameter(text.toBytes(format));
    AS400Text[] member = new AS400Text[3];
    member[0] = new AS400Text(10, ccsid, system_);
    member[1] = new AS400Text(10, ccsid, system_);
    member[2] = new AS400Text(6, ccsid, system_);
    AS400Structure structure = new AS400Structure(member);
    String[] qualifiedJobName = { name_, user_, number_};
    parmList[3] = new ProgramParameter(structure.toBytes(qualifiedJobName));
    text = new AS400Text(16, ccsid, system_);
    parmList[4] = new ProgramParameter(text.toBytes(internalJobID_ == null ? "" : internalJobID_));
    byte[] errorInfo = new byte[32];
    parmList[5] = new ProgramParameter(errorInfo, 0);

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QUSRJOBI.PGM", parmList);
    if (Trace.traceOn_)
    {
      Trace.log(Trace.DIAGNOSTIC, "Retrieving job information for job "+toString());
    }
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] retrievedData = parmList[0].getOutputData();
    parseData(format, retrievedData);
  }


  
  void setValueInternal(int key, Object value)
  {
    values_.put(key, value);
  }


  /**
   * Sets a value for a job attribute.
   * If caching is off, the value is immediately sent to the system.
   * If caching is on, call commitChanges() to send the values to the system.
   * @param attribute The job attribute to change.
   * @param value The new value of the attribute.
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
    if (attribute < 0)
    {
      throw new ExtendedIllegalArgumentException("attribute", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
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
Returns a value which represents how this job handles break messages.

@return How this job handles break messages.  Possible values are:
        <ul>
        <li>{@link #BREAK_MESSAGE_HANDLING_NORMAL Job.BREAK_MESSAGE_HANDLING_NORMAL }
            - The message queue status determines break message handling.
        <li>{@link #BREAK_MESSAGE_HANDLING_HOLD Job.BREAK_MESSAGE_HANDLING_HOLD }
            - The message queue holds break messages until a user or program
            requests them.
        <li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY Job.BREAK_MESSAGE_HANDLING_NOTIFY }
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
Indicates if the attribute value changes are cached.

@return true if attribute value changes are cached,
        false if attribute value changes are committed
        immediatly.
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
        <li>{@link #COMPLETION_STATUS_NOT_COMPLETED Job.COMPLETION_STATUS_NOT_COMPLETED }
            - The job has not completed.
        <li>{@link #COMPLETION_STATUS_COMPLETED_NORMALLY Job.COMPLETION_STATUS_COMPLETED_NORMALLY }
            - The job completed normally.
        <li>{@link #COMPLETION_STATUS_COMPLETED_ABNORMALLY Job.COMPLETION_STATUS_COMPLETED_ABNORMALLY }
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
    return((String)getValue(COMPLETION_STATUS)).trim();
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
        <li>{@link #DATE_FORMAT_YMD Job.DATE_FORMAT_YMD }  - Year, month, and day format.
        <li>{@link #DATE_FORMAT_MDY Job.DATE_FORMAT_MDY }  - Month, day, and year format.
        <li>{@link #DATE_FORMAT_DMY Job.DATE_FORMAT_DMY }  - Day, month, and year format.
        <li>{@link #DATE_FORMAT_JULIAN Job.DATE_FORMAT_JULIAN }  - Julian format (year and day).
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
        <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP Job.KEEP_DDM_CONNECTIONS_ACTIVE_KEEP }  - The system keeps DDM connections active when there
            are no users.
        <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP Job.KEEP_DDM_CONNECTIONS_ACTIVE_DROP }  - The system ends a DDM connection when there are no
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
        <li>{@link #DECIMAL_FORMAT_PERIOD Job.DECIMAL_FORMAT_PERIOD }  - Uses a period for a decimal point, a comma
            for a 3-digit grouping character, and zero-suppresses to the left of
            the decimal point.
        <li>{@link #DECIMAL_FORMAT_COMMA_I Job.DECIMAL_FORMAT_COMMA_I }  - Uses a comma for a decimal point and a period for
            a 3-digit grouping character.  The zero-suppression character is in the
            second character (rather than the first) to the left of the decimal
            notation.  Balances with zero  values to the left of the comma are
            written with one leading zero.
        <li>{@link #DECIMAL_FORMAT_COMMA_J Job.DECIMAL_FORMAT_COMMA_J }  - Uses a comma for a decimal point, a period for a
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
        <li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE Job.DEVICE_RECOVERY_ACTION_MESSAGE }  - Signals the I/O error message to the
            application and lets the application program perform error recovery.
        <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE } Job.DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE }  - Disconnects the
            job when an I/O error occurs.  When the job reconnects, the system sends an
            error message to the application program, indicating the job has reconnected
            and that the workstation device has recovered.
        <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST Job.DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST }  - Disconnects
            the job when an I/O error occurs.  When the job reconnects, the system sends
            the End Request (ENDRQS) command to return control to the previous request
            level.
        <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB Job.DEVICE_RECOVERY_ACTION_END_JOB }  - Ends the job when an I/O error occurs.  A
            message is sent to the job's log and to the history log (QHST) indicating
            the job ended because of a device error.
        <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST Job.DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST }  - Ends the job when an I/O
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
        <li>{@link #FUNCTION_TYPE_BLANK Job.FUNCTION_TYPE_BLANK }  - The system is not performing a logged function.
        <li>{@link #FUNCTION_TYPE_COMMAND Job.FUNCTION_TYPE_COMMAND }  - A command is running interactively, or it is
            in a batch input stream, or it was requested from a system menu.
        <li>{@link #FUNCTION_TYPE_DELAY Job.FUNCTION_TYPE_DELAY }  - The initial thread of the job is processing
            a Delay Job (DLYJOB) command.
        <li>{@link #FUNCTION_TYPE_GROUP Job.FUNCTION_TYPE_GROUP }  - The Transfer Group Job (TFRGRPJOB) command
            suspended the job.
        <li>{@link #FUNCTION_TYPE_INDEX Job.FUNCTION_TYPE_INDEX }  - The initial thread of the job is rebuilding
            an index (access path).
        <li>{@link #FUNCTION_TYPE_IO Job.FUNCTION_TYPE_IO }  - The job is a subsystem monitor that is performing
            input/output (I/O) operations to a work station.
        <li>{@link #FUNCTION_TYPE_LOG Job.FUNCTION_TYPE_LOG }  - The system logs history information in a database
            file.
        <li>{@link #FUNCTION_TYPE_MENU Job.FUNCTION_TYPE_MENU }  - The initial thread of the job is currently
            at a system menu.
        <li>{@link #FUNCTION_TYPE_MRT Job.FUNCTION_TYPE_MRT }  - The job is a multiple requester terminal (MRT)
            job is the {@link #JOB_TYPE job type }  is {@link #JOB_TYPE_BATCH JOB_TYPE_BATCH }
            and the {@link #JOB_SUBTYPE job subtype }  is {@link #JOB_SUBTYPE_MRT JOB_SUBTYPE_MRT } ,
            or it is an interactive job attached to an MRT job if the
            {@link #JOB_TYPE job type }  is {@link #JOB_TYPE_INTERACTIVE JOB_TYPE_INTERACTIVE } .
        <li>{@link #FUNCTION_TYPE_PROCEDURE Job.FUNCTION_TYPE_PROCEDURE }  - The initial thread of the job is running
            a procedure.
        <li>{@link #FUNCTION_TYPE_PROGRAM Job.FUNCTION_TYPE_PROGRAM }  - The initial thread of the job is running
            a program.
        <li>{@link #FUNCTION_TYPE_SPECIAL Job.FUNCTION_TYPE_SPECIAL }  - The function type is special.
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
        <li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED Job.INQUIRY_MESSAGE_REPLY_REQUIRED }  - The job requires an answer for any inquiry
            messages that occur while this job is running.
        <li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT Job.INQUIRY_MESSAGE_REPLY_DEFAULT }  - The system uses the default message reply to
            answer any inquiry messages issued while this job is running.  The default
            reply is either defined in the message description or is the default system
            reply.
        <li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST Job.INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST } The system reply list is
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
**/
  public String getInternalJobID()
  {
    return internalJobID_;
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
    return(String)getValue(ACCOUNTING_CODE);
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
    return((String)getValue(JOB_DESCRIPTION)).trim();
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
        <li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP Job.MESSAGE_QUEUE_ACTION_NO_WRAP }  - Do not wrap. This action causes the job to end.
        <li>{@link #MESSAGE_QUEUE_ACTION_WRAP Job.MESSAGE_QUEUE_ACTION_WRAP }  - Wrap to the beginning and start filling again.
        <li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP Job.MESSAGE_QUEUE_ACTION_PRINT_WRAP }  - Wrap the message queue and print the
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
        <li>{@link #JOB_QUEUE_STATUS_BLANK Job.JOB_QUEUE_STATUS_BLANK }  - The job is not on a job queue.
        <li>{@link #JOB_QUEUE_STATUS_SCHEDULED Job.JOB_QUEUE_STATUS_SCHEDULED }  - The job will run as scheduled.
        <li>{@link #JOB_QUEUE_STATUS_HELD Job.JOB_QUEUE_STATUS_HELD }  - The job is being held on the job queue.
        <li>{@link #JOB_QUEUE_STATUS_RELEASED Job.JOB_QUEUE_STATUS_RELEASED }  - The job is ready to be selected.
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
        CL programs. Possible values are: {@link #YES YES }  and
        {@link #NO NO } .

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
        <li>{@link #LOGGING_TEXT_MESSAGE Job.LOGGING_TEXT_MESSAGE }  - Only the message is written to the job log.
        <li>{@link #LOGGING_TEXT_SECLVL Job.LOGGING_TEXT_SECLVL }  - Both the message and the message help for the
            error message are written to the job log.
        <li>{@link #LOGGING_TEXT_NO_LIST Job.LOGGING_TEXT_NO_LIST }  - If the job ends normally, there is no job log.
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
        <li>{@link #NONE NONE }  - The border and header information is not
            included with output from the Print key.
        <li>{@link #PRINT_KEY_FORMAT_BORDER Job.PRINT_KEY_FORMAT_BORDER }  - The border information
            is included with output from the Print key.
        <li>{@link #PRINT_KEY_FORMAT_HEADER Job.PRINT_KEY_FORMAT_HEADER }  - The header information
            is included with output from the Print key.
        <li>{@link #PRINT_KEY_FORMAT_ALL Job.PRINT_KEY_FORMAT_ALL }  - The border and header information
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
    return((String)getValue(ELIGIBLE_FOR_PURGE)).equals(YES);
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
        The highest priority is 0 and the lowest is 9.

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
    return getAsInt(JOB_QUEUE_PRIORITY);
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
        <li>{@link #JOB_STATUS_ACTIVE Job.JOB_STATUS_ACTIVE }  - The job is active.
        <li>{@link #JOB_STATUS_JOBQ Job.JOB_STATUS_JOBQ }  - The job is currently on a job queue.
        <li>{@link #JOB_STATUS_OUTQ Job.JOB_STATUS_OUTQ }  - The job has completed running, but still has output
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
        <li>{@link #NONE NONE }  -
            This job does not display status messages.
        <li>{@link #STATUS_MESSAGE_HANDLING_NORMAL Job.STATUS_MESSAGE_HANDLING_NORMAL }  -
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
    String name = subsystem.substring(0, 10).trim();
    String lib = subsystem.substring(10, subsystem.length());
    String path = QSYSObjectPathName.toPath(lib, name, "SBSD");
    return path;
  }


/**
Returns additional information about the job type.

@return Additional information about the job type. Possible values are:
        <ul>
        <li>{@link #JOB_SUBTYPE_BLANK Job.JOB_SUBTYPE_BLANK }  - The job has no special subtype or is not a valid job.
        <li>{@link #JOB_SUBTYPE_IMMEDIATE Job.JOB_SUBTYPE_IMMEDIATE }  - The job is an immediate job.
        <li>{@link #JOB_SUBTYPE_PROCEDURE_START_REQUEST Job.JOB_SUBTYPE_PROCEDURE_START_REQUEST }  - The job started
            with a procedure start request.
        <li>{@link #JOB_SUBTYPE_MACHINE_SERVER_JOB Job.JOB_SUBTYPE_MACHINE_SERVER_JOB }  - The job is an AS/400
            Advanced 36 machine server job.
        <li>{@link #JOB_SUBTYPE_PRESTART Job.JOB_SUBTYPE_PRESTART }  - The job is a prestart job.
        <li>{@link #JOB_SUBTYPE_PRINT_DRIVER Job.JOB_SUBTYPE_PRINT_DRIVER }  - The job is a print driver job.
        <li>{@link #JOB_SUBTYPE_MRT Job.JOB_SUBTYPE_MRT }  - The job is a System/36 multiple requester terminal
            (MRT) job.
        <li>{@link #JOB_SUBTYPE_ALTERNATE_SPOOL_USER Job.JOB_SUBTYPE_ALTERNATE_SPOOL_USER }  - Alternate spool user.
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
        <li>{@link #NONE NONE }  -
            A thread in the job does not move to another main storage pool when it reaches
            the end of its time slice.
        <li>{@link #TIME_SLICE_END_POOL_BASE Job.TIME_SLICE_END_POOL_BASE }  -
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
        <li>{@link #JOB_TYPE_NOT_VALID Job.JOB_TYPE_NOT_VALID }  - The job is not a valid job.
        <li>{@link #JOB_TYPE_AUTOSTART Job.JOB_TYPE_AUTOSTART }  - The job is an autostart job.
        <li>{@link #JOB_TYPE_BATCH Job.JOB_TYPE_BATCH }  - The job is a batch job.
        <li>{@link #JOB_TYPE_INTERACTIVE Job.JOB_TYPE_INTERACTIVE }  - The job is an interactive job.
        <li>{@link #JOB_TYPE_SUBSYSTEM_MONITOR Job.JOB_TYPE_SUBSYSTEM_MONITOR }  - The job is a subsystem monitor job.
        <li>{@link #JOB_TYPE_SPOOLED_READER Job.JOB_TYPE_SPOOLED_READER }  - The job is a spooled reader job.
        <li>{@link #JOB_TYPE_SYSTEM Job.JOB_TYPE_SYSTEM }  - The job is a system job.
        <li>{@link #JOB_TYPE_SPOOLED_WRITER Job.JOB_TYPE_SPOOLED_WRITER }  - The job is a spooled writer job.
        <li>{@link #JOB_TYPE_SCPF_SYSTEM Job.JOB_TYPE_SCPF_SYSTEM }  - The job is the SCPF system job.
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
Refreshes the values for all attributes.  This does not cancel
uncommitted changes.
**/
  public void loadInformation()
  {
    // Need to load an attribute from each format.
    try
    {
      retrieve(THREAD_COUNT);           // 150
      retrieve(CURRENT_SYSTEM_POOL_ID); // 200
      retrieve(JOB_DATE);               // 300
      retrieve(SERVER_TYPE);            // 400
      retrieve(LOGGING_TEXT);           // 500
      retrieve(SPECIAL_ENVIRONMENT);    // 600
      retrieve(USER_LIBRARY_LIST);      // 700
    }
    catch (Exception e)
    {
      if (Trace.traceOn_)
      {
        Trace.log(Trace.ERROR, "Error loading job information: "+e);
      }
    }
  }



  /**
   * Releases this job.
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
    // If the user wants to end the remote command server job that is servicing our connection, 
    // they are welcome to "shoot themselves in the foot".
    CommandCall cmd = new CommandCall(system_, toRun);
    if (!cmd.run())
    {
      throw new AS400Exception(cmd.getMessageList());
    }
  }


/**
Removes a PropertyChangeListener.

@param listener The listener.
**/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.removePropertyChangeListener(listener);
  }



/**
Removes a VetoableChangeListener.

@param listener The listener.
**/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.removeVetoableChangeListener(listener);
  }



/**
Sets how this job handles break messages.

@param breakMessageHandling How this job handles break messages.  Possible values are:
                            <ul>
                            <li>{@link #BREAK_MESSAGE_HANDLING_NORMAL Job.BREAK_MESSAGE_HANDLING_NORMAL }
                                - The message queue status determines break message handling.
                            <li>{@link #BREAK_MESSAGE_HANDLING_HOLD Job.BREAK_MESSAGE_HANDLING_HOLD }
                                - The message queue holds break messages until a user or program
                                  requests them.
                            <li>{@link #BREAK_MESSAGE_HANDLING_NOTIFY Job.BREAK_MESSAGE_HANDLING_NOTIFY }
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
Sets the value indicating whether attribute value changes are
committed immediately. The default is true.
If any cached changes are not committed before this method is called with
a value of false, those changes are lost.
@param cacheChanges true to cache attribute value changes,
                    false to commit all attribute value changes
                    immediately.
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
                            <li>{@link #CCSID_SYSTEM_VALUE Job.CCSID_SYSTEM_VALUE }  - The CCSID specified
                                in the system value QCCSID is used.
                            <li>{@link #CCSID_INITIAL_USER Job.CCSID_INITIAL_USER }  - The CCSID specified
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
    setAsIntToChange(CCSID, codedCharacterSetID);
  }



/**
Sets the country ID.

@param countryID    The country ID.  The following special values can be used:
                    <ul>
                    <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
                        system value QCNTRYID is used.
                    <li>{@link #USER_PROFILE Job.USER_PROFILE }  - The
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
<li>{@link #DATE_FORMAT_SYSTEM_VALUE Job.DATE_FORMAT_SYSTEM_VALUE }  - The system value QDATFMT is used.
<li>{@link #DATE_FORMAT_YMD Job.DATE_FORMAT_YMD }  - Year, month, and day format.
<li>{@link #DATE_FORMAT_MDY Job.DATE_FORMAT_MDY }  - Month, day, and year format.
<li>{@link #DATE_FORMAT_DMY Job.DATE_FORMAT_DMY }  - Day, month, and year format.
<li>{@link #DATE_FORMAT_JULIAN Job.DATE_FORMAT_JULIAN }  - Julian format (year and day).
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
                        <li>{@link #DATE_SEPARATOR_SYSTEM_VALUE Job.DATE_SEPARATOR_SYSTEM_VALUE }  - The
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
                                <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_KEEP Job.KEEP_DDM_CONNECTIONS_ACTIVE_KEEP }  - The system keeps DDM connections active when there
                                    are no users.
                                <li>{@link #KEEP_DDM_CONNECTIONS_ACTIVE_DROP Job.KEEP_DDM_CONNECTIONS_ACTIVE_DROP }  - The system ends a DDM connection when there are no
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
                        <li>{@link #DECIMAL_FORMAT_PERIOD Job.DECIMAL_FORMAT_PERIOD }  - Uses a period for a decimal point, a comma
                            for a 3-digit grouping character, and zero-suppresses to the left of
                            the decimal point.
                        <li>{@link #DECIMAL_FORMAT_COMMA_I Job.DECIMAL_FORMAT_COMMA_I }  - Uses a comma for a decimal point and a period for
                            a 3-digit grouping character.  The zero-suppression character is in the
                            second character (rather than the first) to the left of the decimal
                            notation.  Balances with zero  values to the left of the comma are
                            written with one leading zero.
                        <li>{@link #DECIMAL_FORMAT_COMMA_J Job.DECIMAL_FORMAT_COMMA_J }  - Uses a comma for a decimal point, a period for a
                            3-digit grouping character, and zero-suppresses to the left of the decimal
                            point.
                        <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
                            system value QECFMT is used.
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
        !decimalFormat.equals(SYSTEM_VALUE))
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
                            <li>{@link #DEVICE_RECOVERY_ACTION_MESSAGE Job.DEVICE_RECOVERY_ACTION_MESSAGE }  - Signals the I/O error message to the
                                application and lets the application program perform error recovery.
                            <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE Job.DEVICE_RECOVERY_ACTION_DISCONNECT_MESSAGE }  - Disconnects the
                                job when an I/O error occurs.  When the job reconnects, the system sends an
                                error message to the application program, indicating the job has reconnected
                                and that the workstation device has recovered.
                            <li>{@link #DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST Job.DEVICE_RECOVERY_ACTION_DISCONNECT_END_REQUEST }  - Disconnects
                                the job when an I/O error occurs.  When the job reconnects, the system sends
                                the End Request (ENDRQS) command to return control to the previous request
                                level.
                            <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB Job.DEVICE_RECOVERY_ACTION_END_JOB }  - Ends the job when an I/O error occurs.  A
                                message is sent to the job's log and to the history log (QHST) indicating
                                the job ended because of a device error.
                            <li>{@link #DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST Job.DEVICE_RECOVERY_ACTION_END_JOB_NO_LIST }  - Ends the job when an I/O
                                error occurs.  There is no job log produced for the job.  The system sends
                                a message to the QHST log indicating the job ended because of a device error.
                            <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
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
        !deviceRecoveryAction.equals(SYSTEM_VALUE))
    {
      throw new ExtendedIllegalArgumentException("deviceRecoveryAction", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    setValue(DEVICE_RECOVERY_ACTION, deviceRecoveryAction);
  }



/**
Sets how the job answers inquiry messages.

@param inquiryMessageReply  How the job answers inquiry messages.  Possible values are:
                            <ul>
                            <li>{@link #INQUIRY_MESSAGE_REPLY_REQUIRED Job.INQUIRY_MESSAGE_REPLY_REQUIRED }  - The job requires an answer for any inquiry
                                messages that occur while this job is running.
                            <li>{@link #INQUIRY_MESSAGE_REPLY_DEFAULT Job.INQUIRY_MESSAGE_REPLY_DEFAULT }  - The system uses the default message reply to
                                answer any inquiry messages issued while this job is running.  The default
                                reply is either defined in the message description or is the default system
                                reply.
                            <li>{@link #INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST Job.INQUIRY_MESSAGE_REPLY_SYSTEM_REPLY_LIST } The system reply list is
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
**/
  public void setInternalJobID(String internalJobID)
  throws PropertyVetoException
  {
    if (internalJobID == null)
    {
      throw new NullPointerException("internalJobID");
    }

    if (isConnected_)
    {
      throw new ExtendedIllegalStateException("internalJobID", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String old = internalJobID_;

    if (vetoableChangeSupport_ != null)
      vetoableChangeSupport_.fireVetoableChange("internalJobID", old, internalJobID_);

    internalJobID_ = internalJobID;

    if (propertyChangeSupport_ != null)
      propertyChangeSupport_.firePropertyChange("internalJobID", old, internalJobID_);

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
                                    <li>{@link #MESSAGE_QUEUE_ACTION_NO_WRAP Job.MESSAGE_QUEUE_ACTION_NO_WRAP }  - Do not wrap. This action causes the job to end.
                                    <li>{@link #MESSAGE_QUEUE_ACTION_WRAP Job.MESSAGE_QUEUE_ACTION_WRAP }  - Wrap to the beginning and start filling again.
                                    <li>{@link #MESSAGE_QUEUE_ACTION_PRINT_WRAP Job.MESSAGE_QUEUE_ACTION_PRINT_WRAP }  - Wrap the message queue and print the
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
        !jobMessageQueueFullAction.equals(MESSAGE_QUEUE_ACTION_PRINT_WRAP))
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
                        <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
                            system value QLANGID is used.
                        <li>{@link #USER_PROFILE Job.USER_PROFILE }  - The
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
                            CL programs. Possible values are: {@link #YES YES }  and
                            {@link #NO NO } .

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
      throw new NullPointerException("loggingCLPrograms");
    if (!loggingCLPrograms.equals(YES) && !loggingCLPrograms.equals(NO))
      throw new ExtendedIllegalArgumentException("loggingCLPrograms", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);

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
                    <li>{@link #LOGGING_TEXT_MESSAGE Job.LOGGING_TEXT_MESSAGE }  - Only the message is written to the job log.
                    <li>{@link #LOGGING_TEXT_SECLVL Job.LOGGING_TEXT_SECLVL }  - Both the message and the message help for the
                        error message are written to the job log.
                    <li>{@link #LOGGING_TEXT_NO_LIST Job.LOGGING_TEXT_NO_LIST }  - If the job ends normally, there is no job log.
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
Sets the job name.  This does not change the job on
the AS/400.  Instead, it changes the job
this Job object references.   This cannot be changed
if the object has established a connection to the AS/400.

@param name    The job name.  Specify "*" to indicate the job this
               program running in, or "*INT" to indicate that the job
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
Sets the job number.  This does not change the job on
the AS/400.  Instead, it changes the job
this Job object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param number    The job number.  This must be blank if the job name is "*".

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
                            <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
                                system value QPRTDEV is used.
                            <li>{@link #PRINTER_DEVICE_NAME_WORK_STATION Job.PRINTER_DEVICE_NAME_WORK_STATION }  - The
                                default printer device used with this job is the printer device
                                assigned to the work station that is associated with the job.
                            <li>{@link #USER_PROFILE Job.USER_PROFILE }  - The
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
                        <li>{@link #NONE NONE }  - The border and header information is not
                            included with output from the Print key.
                        <li>{@link #PRINT_KEY_FORMAT_BORDER Job.PRINT_KEY_FORMAT_BORDER }  - The border information
                            is included with output from the Print key.
                        <li>{@link #PRINT_KEY_FORMAT_HEADER Job.PRINT_KEY_FORMAT_HEADER }  - The header information
                            is included with output from the Print key.
                        <li>{@link #PRINT_KEY_FORMAT_ALL Job.PRINT_KEY_FORMAT_ALL }  - The border and header information
                            is included with output from the Print key.
                        <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
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

    if (!printKeyFormat.equals(NONE) &&
        !printKeyFormat.equals(PRINT_KEY_FORMAT_BORDER) &&
        !printKeyFormat.equals(PRINT_KEY_FORMAT_HEADER) &&
        !printKeyFormat.equals(PRINT_KEY_FORMAT_ALL) &&
        !printKeyFormat.equals(SYSTEM_VALUE))
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
                    <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
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
    setValueInternal(ELIGIBLE_FOR_PURGE, purge ? YES : NO);
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
                                <li>{@link #NONE NONE }  -
                                    This job does not display status messages.
                                <li>{@link #STATUS_MESSAGE_HANDLING_NORMAL Job.STATUS_MESSAGE_HANDLING_NORMAL }  -
                                    This job displays status messages.
                                <li>{@link #SYSTEM_VALUE Job.SYSTEM_VALUE }  - The
                                    system value QSTSMSG is used.
                                <li>{@link #USER_PROFILE Job.USER_PROFILE }  - The
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
                        <li>{@link #TIME_SEPARATOR_SYSTEM_VALUE Job.TIME_SEPARATOR_SYSTEM_VALUE }  - The
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
                            <li>{@link #NONE NONE }  -
                                A thread in the job does not move to another main storage pool when it reaches
                                the end of its time slice.
                            <li>{@link #TIME_SLICE_END_POOL_BASE Job.TIME_SLICE_END_POOL_BASE }  -
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
Sets the user name.  This does not change the job on
the AS/400.  Instead, it changes the job
this Job object references.  This cannot be changed
if the object has established a connection to the AS/400.

@param user    The user name.  This must be blank if the job name is "*".

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
Returns the string representation in the format
"number/user/name".

@return The string representation.
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
