///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: Job.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The Job class represents an AS/400 job. You can get and set the attribute
 * of the job in the system. In Job there is a cache to store the changed value
 * which will be set to the system. You can use <i>cacheChanges</i> and
 * <i>commitChanges</i> to store the change values, or change the value to
 * system directly.
 * Here is a example showing how to use cache when setting value and getting
 * value:<br>
 * <p><blockquote><pre>
 *  try {
 *      // Creates AS400 object.
 *      AS400 as400 = new AS400("systemName");
 *      // Constructs a Job object
 *      Job job = new Job(as400,"QDEV002");
 *      // Gets job information
 *      System.out.println("User of this job : "+job.getUser());
 *      System.out.println("CPU used : "+job.getCPUUsed();
 *      System.out.println("Job enter system date : "+job.getJobEnterSystemDate());
 *      // Sets cache mode
 *      job.setCacheChanges(true);
 *      // Changes will be store in the cache.
 *      job.setRunPriority(66);
 *      job.setDateFormat("*YMD");
 *      // Commit changes, this will set the value to system.
 *      job.commitChanges();
 *      // Set job information to system directly(without cache).
 *      job.setCacheChanges(false);
 *      job.setRunPriority(60);
 *  } catch (Exception e)
 *  {
 *      System.out.println("error : "+e)
 *  }
 * </pre></blockquote></p>
**/
// The javadoc description came out of the AS/400 System API
// Reference in the section on Retrieve Job Information (QUSRJOBI) API
// and Change Job (QWTCHGJB) API
public class Job implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // Constant indicating that the date format is the format CYYMMDD.
  private static final int CYYMMDD_FORMAT = 0; //@B1A

  // Constant indicating that the date format is the format CYYMMDDHHMMSS.
  private static final int CYYMMDDHHMMSS_FORMAT = 1; //@B1A

  // Constant indicating that the date format is the system timestamp format.
  private static final int SYSTEM_TIMESTAMP_FORMAT = 2; //@B1A

  // Constant indicating that the date format is the format HHMMSS.
  private static final int HHMMSS_FORMAT = 3; //@B1A

  // @A1A : change properties.
  private AS400 as400_;
  private String internalJobID_ ="";
  private String jobName_       = "";
  private String jobNameCasePreserved_  = "";                      // @A3A
  private String userName_      = "";
  private String userNameCasePreserved_ = "";                      // @A3A
  private String jobNumber_     = "";
  
  // These are the library lists that are parsed out of format JOBI0700.
  private String[] systemLibraryList_;
  private String[] productLibraries_;
  private String[] currentLibrary_; // Yes, this is supposed to be an array.
  private String[] userLibraryList_;
  
  // The record formats for the job information
  private JobI0150Format format0150_;
  private JobI0200Format format0200_;
  private JobI0300Format format0300_;
  private JobI0400Format format0400_;
  private JobI0500Format format0500_;
  private JobI0600Format format0600_;
  private JobI0700Format format0700_;
  
  // The table that holds all of the changes, if caching is enabled.
  private Hashtable cachedChanges_ = new Hashtable();
  
  //@B1 - The keyTable0100_ is a map between the name of the
  // field that is to be changed and the key code to use
  // on the JOBC0100 format of the QWTCHGJB API.
  // The lengthTable0100_ maps the field name to its length.
  // Note all fields are CHAR(size) except for the ones markes as BIN4.
  private static final Hashtable keyTable0100_ = new Hashtable(48);
  private static final Hashtable lengthTable0100_ = new Hashtable(48);
  private static final void addKey(String key, int value, int size)
  {
    keyTable0100_.put(key, new Integer(value));
    lengthTable0100_.put(key, new Integer(size));
  }
  static
  {
    addKey("breakMessageHandling", 201, 10);
    addKey("codedCharacterSetID", 302, 4); // This is a BIN4
    addKey("countryID", 303, 8);
    addKey("characterIdentifierControl", 311, 10);
    addKey("dateFormat", 405, 4);
    addKey("dateSeparator", 406, 1);
    addKey("DDMConversationHandling", 408, 5); // This is called DDMConversation in the setter
    addKey("defaultWait", 409, 4); // This is a BIN4
    addKey("deviceRecoveryAction", 410, 13);
    addKey("decimalFormat", 413, 8);
    addKey("inquiryMessageReply", 901, 10);
    addKey("jobAccountingCode", 1001, 15);
    addKey("jobDate", 1002, 7);
    addKey("jobQueueName", 1004, 20);
    addKey("jobQueuePriority", 1005, 2);
    addKey("jobSwitches", 1006, 8);
    addKey("jobMessageQueueFullAction", 1007, 10);
    addKey("languageID", 1201, 8);
    addKey("loggingLevel", 1202, 1);
    addKey("loggingOfCLPrograms", 1203, 10);
    addKey("loggingSeverity", 1204, 4); // This is a BIN4
    addKey("loggingText", 1205, 7);
    addKey("outputQueueName", 1501, 20);
    addKey("outputQueuePriority", 1502, 2);
    addKey("printKeyFormat", 1601, 10);
    addKey("printText", 1602, 30);
    addKey("printerDeviceName", 1603, 10);
    addKey("purge", 1604, 4);
    addKey("runPriority", 1802, 4); // This is a BIN4
    addKey("sortSequenceTable", 1901, 20);
    addKey("statusMessageHandling", 1902, 10);
    addKey("serverType", 1911, 30);
    addKey("scheduleDate", 1920, 10);
    addKey("scheduleTime", 1921, 8);
    addKey("serverModeForStructuredQueryLanguage", 1922, 1);
    addKey("timeSeparator", 2001, 1);
    addKey("timeSlice", 2002, 4); // This is a BIN4
    addKey("timeSliceEndPool", 2003, 10);
  }
    
  // The fieldToFormatMap_ maps field names to their respective record formats.
  // It can't be static because the record formats contain AS400 objects, which
  // could vary from instantiation to instantiation of this class.
  private Hashtable fieldToFormatMap_ = new Hashtable();
  private void mapFormat(JobFormat format)
  {
    String[] arr = format.getFieldNames();
    for (int i=0; i<arr.length; ++i)
    {
      fieldToFormatMap_.put(arr[i], format);
    }
  }
  
  // The formatToRecordMap_ maps record formats to the associated record
  // objects in this Job object. The job information data is stored in
  // each Record object whose key is a particular RecordFormat object.
  // Note that the Record objects that hold the values for each
  // format are not defined explicitly as members of this class.
  // They are saved as values inside the hashtable.
  private Hashtable formatToRecordMap_ = new Hashtable(7);
      
  // Flag indicating if the job information changes are to be cached.
  private boolean isCached_ = false;

  
  /**
   * Constructs a Job object.
   *
   * @param system The AS/400 system.
   * @param jobName The job name. It can be "*" or a specific job name. If "*"
   * is specified, the other two parameters, <i>userName</i> and <i>jobNumber</i>, must be blank.
   * @param userName The user profile name.
   * @param jobNumber The job number.
  **/
  public Job(AS400 system,String jobName,String userName,String jobNumber)
  {     
      if(system == null)
          throw new NullPointerException("system");

      if(jobName == null)
          throw new NullPointerException("jobName");

      if(userName == null)
          throw new NullPointerException("userName");

      if(jobNumber == null)
          throw new NullPointerException("jobNumber");

      if(jobName.equals("*"))
      {
          if (userName.trim() != "")
              throw new ExtendedIllegalArgumentException("userName",
              ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
          if (jobNumber.trim() != "")
              throw new ExtendedIllegalArgumentException("jobNumber",
              ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }

      as400_= system;
      initializeFormats();
      
      jobNameCasePreserved_=jobName.trim();                   // @A3A
      jobName_=jobName.trim().toUpperCase();

      userNameCasePreserved_=userName.trim();
      userName_=userName.trim().toUpperCase();                // @A3A

      jobNumber_=jobNumber.trim().toUpperCase();
  }

  /**
   * Constructs a Job object.
   *
   * @param system The AS/400 system.
   * @param internalJobID The internal job identifier.
  **/
  public Job(AS400 system, String internalJobID)
  {
      if(system == null)
          throw new NullPointerException("system");

      if(internalJobID == null)
          throw new NullPointerException("internalJobID");


      as400_= system;
      initializeFormats();
      
      jobName_="*INT";
      internalJobID_=internalJobID.trim().toUpperCase();
  }

  /**
   * Constructs a Job object.
   * <b>Note</b> : This construct method is used to optimize performance of the
   * visual components. This method will not load the job information as default
   * behavior. If you have to use this method, make sure you have called
   * the <i>loadInformation</i> method before you use other method to retrieve
   * value of the job information.
   *
   * @param system The AS/400 system.
   * @param jobName The job name. It can be "*" or a specific job name. If "*"
   * is specified, the other two parameters, <i>userName</i> and <i>jobNumber</i>, must be blank.
   * @param userName The user profile name.
   * @param jobNumber The job number.
   * @param status The job status.
   * @param type The job type.
   * @param subtype The job subtype.
  **/
    Job(AS400 system,
        String jobName,
        String userName,
        String jobNumber,
        String status,
        String type,
        String subtype)
    {
      as400_= system;
      initializeFormats();
      
      jobNameCasePreserved_ =jobName.trim();                 // @A3A
      jobName_=jobName.trim().toUpperCase();

      userNameCasePreserved_=userName.trim();                // @A3A
      userName_=userName.trim().toUpperCase();

      jobNumber_=jobNumber.trim().toUpperCase();
    }

  /**
   * Commits the updated job information to the AS/400.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
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
    if (cachedChanges_.size() == 0) return;
    setChanges(cachedChanges_); //@B1A
    cachedChanges_.clear();
  }

  
  //@B1A
  /**
   * Gets the value for the specified field out of the
   * appropriate record in the format cache. If the particular
   * format has not been loaded yet, it is loaded from the 400.
  **/
  private Object get(String field)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    // Retrieve the info out of our own record
    JobFormat jf = (JobFormat)fieldToFormatMap_.get(field);
    loadInformation(jf); // Load the info.
    return ((Record)formatToRecordMap_.get(jf)).getField(field);
  }
    
  
  /**
   * Returns the number of auxiliary storage input or output requests. This includes
   * both database and nondatabase paging.
   *
   * @return The number of auxiliary storage input or output requests.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("numberOfAuxiliaryIORequests")).intValue(); //@B1A
  }

  
  /**
   * Returns the break message handling method.
   *
   * @return The break message handling method. The possible values are:
   *     <ul>
   *        <li> "*NORMAL" - The message queue status determines break message handling.
   *        <li> "*HOLD" - The message queue holds break mesages until a user or program requests them.
   *        <li> "*NOTIFY" - The system notifies the job's message queue when a message arrives.
   *    </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("breakMessageHandling")).trim(); //@B1A
  }

  
  /**
   * Returns the value indicating whether the changes are cached.
   * @return The value indicating whether the changes are cached.
   *
  **/
  public boolean getCacheChanges()
  {
      return isCached_;
  }

  
  /**
   * Returns the coded character set identifier for this job. This attribute
   * controls the type of CCSID conversion that occurs for display files,
   * printer files, and panel groups.
   * @return The coded character set identifier for this job.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((Integer)get("codedCharacterSetID")).intValue(); //@B1A
  }

  
  /**
   * Returns the completion status of the job.
   *
   * @return The completion status of the job. The possible values are:
   *         <ul>
   *           <li>" "  - The job has not completed.
   *           <li>"0" - The job completed normally.
   *           <li>"1" - The job completed abnormally.
   *         </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("completionStatus")).trim(); //@B1A
  }

  /**
   * Returns the country identifier associated with this job.
   * @return The country identifier associated with this job.
   * @see #setCountryID
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("countryID")).trim(); //@B1A
  }

  
  /**
   * Returns the amount of processing time that the job used,
   * in milliseconds.
   *
   * @return The amount of processing time that the job used,
   *         in milliseconds.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("processingUnitTimeUsed")).intValue(); //@B1A
  }

  
  /**
   * Returns the name of the current library for the initial thread of the job.
   * If no current library exists, an empty String is returned.
   *
   * @return The current library for the initial thread of the job.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String currentLib = "";
      if (getCurrentLibraryExistence()) //@B0- this loads 0700
      {
        currentLib = currentLibrary_[0];
      }
      return currentLib;
  }

  
  /**
   * Returns the current library existence field.
   * @return The current library existence field.
   * Possible values are :
   * <ul>
   *    <li> false - No current library exists.
   *    <li> true - A current library exists.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      if (((Integer)get("currentLibraryExistence")).intValue() == 1) //@B1A
          return true;
      else
          return false;
  }

  
  /**
   * Returns the date and time when the job was placed on the
   * system. This method is the same as getJobEnterSystemDate().
   *
   * @return  The date and time when the job was placed on the system.
   * This is null if the job did not become active.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
    return getJobEnterSystemDate();
  }

  
  /**
   * Returns the date format.
   *
   * @return The date format. The possible values are:
   *         <ul>
   *           <li>"*YMD" - Year, month, and day format.
   *           <li>"*MDY" - Month, day, and Year format.
   *           <li>"*DMY" - Day, month, and Year format.
   *           <li>"*JUL" - Julian format (year and day).
   *         </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("dateFormat")).trim(); //@B1A
  }

  
  /**
   * Returns the date separator. The date separator is used to separate days,
   * months, and years when representing a date.
   *
   * @return The date separator. The possible values are :
   *    <ul>
   *        <li>"/" A slash (/) is used for the date separator.
   *        <li>"-" A dash (-) is used for the date separator.
   *        <li>"." A period (.) is used for the date separator.
   *        <li>" " A blank (/) is used for the date separator.
   *        <li>"," A comma (,) is used for the date separator.
   *    </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("dateSeparator")).trim(); //@B1A
  }

  
  /**
   * Returns the DDM conversation handling. It specifies whether connections using
   * distributed data management (DDM) protocols remain active when they are not being
   * used.
   *
   * @return The DDM conversation handling. The possible values are:
   *    <ul>
   *        <li> "*KEEP" - The system keeps DDM connections active when there are no users.
   *        <li> "*DROP" - The system ends a DDM connection when there are no users.
   *    </ul>
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("DDMConversationHandling")).trim(); //@B1A
  }

  
  /**
   * Returns the decimal format used for this job.
   *
   * @return The decimal format used for this job.
   * The possible values are:
   *    <ul>
   *        <li> " " - Uses a period for a decimal point, a comma for a 3-digit grouping character,
   * and zero-suppress to the left of the decimal point.
   *        <li> "J"- Uses a period for a decimal point, a comma for a 3-digit grouping character,
   * and zero-suppression character is in the second position (rather than the first) to the left of
   * the decimal notation.
   *        <li> "l" - Uses a comma for a decimal point, a period for a 3-digit grouping character,
   * and zero-suppress to the left of the decimal point.
   *     </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("decimalFormat")); //@B1 - don't trim!
  }

  
  /**
   * Returns the default coded character set identifier used for this job.
   *
   * @return The default coded character set identifier used for this job.
   * It will be zero if the job is not an active job.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("defaultCodedCharacterSetIdentifier")).intValue(); //@B1A
  }


  //@A2A
  /**
   * Returns the default wait time (in seconds).
   * @return The default wait time (in seconds).
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((Integer)get("defaultWait")).intValue(); //@B1A
  }


  /**
   * Returns the device recovery action. It is the action taken for interactive jobs
   * when an I/O error occurs for the job's requesting program device.
   *
   * @return The device recovery action. The possible values are:
   * <ul>
   *    <li> "*MSG" - Signals the I/O error message to the application and lets the
   * application program perform error recovery.
   *    <li> "*DSCMSG" - Disconnects the job when an I/O error occurs. When the job
   * reconnects, the system sends an error message to the application program,
   * indicating the job has reconnected and that the work station device has recovered.
   *    <li> "*DSCENDRQS" - Disconnects the job when an I/O error occurs. When the job
   * reconnects, the system sends the End Request (ENDRQS) command to return control
   * to the previous request level.
   *     <li> "*ENDJOB" - Ends the job when an I/O error occurs. A message is sent to
   * the job's log and to the history log (QHST) indicating the job ended because of
   * a device error.
   *    <li> "ENDJOBNOLIST" - Ends the job when an I/O error occurs. There is no job
   * log produced for the job. The system sends a message to the QHST log indicating
   * the job ended because of a device error.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((String)get("deviceRecoveryAction")).trim(); //@B1A
  }

  
  /**
   * Returns the end severity. This is the message severity level of escape messages that
   * can cause a batch job to end. The batch job ends when a request in the batch input
   * stream sends an escape message, whose severity is equal to or greater than this value
   * to the request processing program.
   *
   * @return The end severity.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((Integer)get("endSeverity")).intValue(); //@B1A
  }

  
  /**
   * Returns additional information, as described in the function type field,
   * about the function the initial thread is currently performing. This
   * information is updated only when a command is processed.
   * @return The additional information, as described in the function bype field,
   * about the function that the initial thread is currently performing.
   *
   * @see #getFunctionType
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("functionName")).trim(); //@B1A
  }

  
  /**
   * Returns a value indicating if the initial thread is performing a high-level
   * function and what the function type is.
   *
   * @return The function type. Possible values are:
   *         <ul>
   *           <li>" "  - The system is not doing a logged function.
   *           <li>"C" - A command is running interactively, or it is
   *                     in a batch input stream, or it was requested
   *                     from a system menu.
   *           <li>"D" - The job is processing a Delay Job (DLYJOB)
   *                     command.  The function name contains the
   *                     number of seconds the job is delayed, or the
   *                     time when the job is to resume processing,
   *                     depending on how the command was specified.
   *           <li>"G" - The Transfer Group Job (TFRGRPJOB) command
   *                     suspended the job.  The function name contains
   *                     the group job name.
   *           <li>"I" - The job is rebuilding an index (access path).
   *                     The function name contains the name of the
   *                     logical file whose index is rebuilt.
   *           <li>"L" - The system logs history information in a
   *                     database file.  The function name contains
   *                     the name of the log.
   *           <li>"M" - The job is a multiple requester terminal (MRT)
   *                     job or an interactive job attached to an MRT
   *                     job.
   *           <li>"N" - The job is currently at a system menu.  The
   *                     function name contains the name of the menu.
   *           <li>"O" - The job is a subsystem monitor that is
   *                     performing input/output operations to a
   *                     workstation.  The function name contains
   *                     the name of the workstation device.
   *           <li>"P" - The job is running a program.  The function
   *                     name contains the name of the program.
   *           <li>"R" - The job is running a procedure.  The function
   *                     name contains the name of the procedure.
   *           <li>"*" - This is a special function.  The function
   *                     name contains an entry that further describes
   *                     the special function.
   *         </ul>
   *
   * @see #getFunctionName
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String value = (String)get("functionType"); //@B1A
      if (value.length() > 0 && ((int)value.charAt(0)) == 0)
          value = " ";
      return value;
  }

  
  /**
   * Returns the inquiry message reply method.
   *
   * @return The inquiry message reply method. The possible values are:
   * <ul>
   *     <li> "*RQD" - The job requires an answer for any inquiry messages that
   * occur while this job is running.
   *    <li> "*DFT" - The system uses the default message reply to answer any inquiry
   * message issued while this job is running. The default reply is either defined in
   * the message description or is the default system reply.
   *    <li> "SYSRPYL" - The system reply list is checked to see if there is an entry
   * for an inquiry message issued while this job is running. If a match occurs, the
   * system uses the reply value for that entry. If no entry exists for that message, the
   * system uses an inquiry message.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("inquiryMessageReply")).trim(); //@B1A
  }

  
  /**
   * Returns the number of interactive transactions.
   *
   * @return The number of interactive transactions. It will be zero if the
   * job has no interactions.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((Integer)get("numberOfInteractiveTransactions")).intValue(); //@B1A
  }

  
  /**
   * Returns the job accounting code. The job accounting code is an identifier
   * assigned to the job by the system to collect resource use information for
   * the job when job accounting is active. See AS/400 System API Reference for
   * details.
   *
   * @return The job accounting code.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((String)get("jobAccountingCode")).trim(); //@B1A
  }

  
  /**
   * Returns the date and time the job became active.
   *
   * @return The date and time the job became active.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return parseDate((String)get("dateAndTimeJobBecameActive")); //@B1A
  }

  
  /**
   * Returns the date assigned to the job. This value is for jobs whose status
   * is *JOBQ or *ACTIVE. For jobs with a status of *OUTQ, the value for this
   * field is null.
   *
   * @return The date assigned to the job.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String jobDateString = ((String)get("jobDate"));                 //@B1A
      Date jobDate = null;                                             //@B1A
      if (jobDateString.equalsIgnoreCase("*SYSVAL"))                   //@B1A
      {                                                                //@B1A
        try                                                            //@B1A
        {                                                              //@B1A
          jobDate = (Date)new SystemValue(as400_, "QDATE").getValue(); //@B1A
        }                                                              //@B1A
        catch (Exception e)                                            //@B1A
        {                                                              //@B1A
          Trace.log(Trace.ERROR, "Error in get system date: "+e);      //@B1A
        }                                                              //@B1A
      }                                                                //@B1A
      else                                                             //@B1A
      {                                                                //@B1A
        jobDate = parseDate(jobDateString);                            //@B1A
      }                                                                //@B1A
              
      return jobDate;                                                  //@B1A
  }


 /**
   * Returns the full qualified integrated path of the job description.
   * For example, the return value would look like "QGPL/QDFTJOBD".
   * @return The full qualified integrated path of the job description.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      String jd = ((String)get("jobDescriptionLibraryName")).trim(); //@B1A
      if (jd.length() > 0)                                           //@A2A
        jd += "/";
      jd += ((String)get("jobDescriptionName")).trim(); //@B1A
      return jd;
  }

  
  /**
   * Returns the date and time the job entered the system.
   * @return The date and time the job entered the system.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return parseDate((String)get("dateAndTimeJobEnteredSystem")); //@B1A
  }

  
  /**
   * Returns the job message queue full action. This is the action to take when the message
   * queue is full.
   *
   * @return The job message queue full action. The possible values are:
   * <ul>
   *    <li> "*NOWRAP" - When the job message queue is full, do not wrap. This action causes
   * the job to end.
   *     <li> "*WRAP" - When the job message queue is full, wrap to the beginning and start filling again.
   *     <li> "*PRTWRAP" - When the job message queue is full, wrap the message queue and print the messages
   * that are being overlaid because of the wrapping.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("jobMessageQueueFullAction")).trim(); //@B1A
  }

  
  /**
   * Returns the job message queue maximum size.
   *
   * @return The job message queue maximum size. The range is 2 through 64.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("jobMessageQueueMaximumSize")).intValue(); //@B1A
  }


  /**
   * Returns the date and time the job was put on the job queue.
   *
   * @return The date and time the job was put on the job queue.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
    DateTimeConverter converter = new DateTimeConverter(as400_);
    return converter.convert((byte[])get("dateAndTimeJobWasPutOnThisJobQueue"), "*DTS");
  }


  /**
   * Returns the date and time the job is scheduled to run.
   *
   * @return The date and time the job is scheduled to run.
   * @see #setScheduleDate
   * @see #setScheduleTime
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
    DateTimeConverter converter = new DateTimeConverter(as400_);
    return converter.convert((byte[])get("dateAndTimeJobIsScheduledToRun"), "*DTS");
  }

  
  /**
   * Returns the status of the job on the job queue.
   *
   * @return The status of the job on the job queue, possible values are:
   *         <ul>
   *           <li>" "  - This job was not on a job queue.
   *           <li>"SCD" - This job will run as schduled.
   *           <li>"HLD" - This job is being held on the job queue.
   *           <li>"RLS" - This job is ready to be selected.
   *         </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
    String value = ((String)get("statusOfJobOnTheJobQueue")).trim(); //@B1A
    if (value.equals(""))
        value = " ";
    return value;
  }

  
  /**
   * Returns the current setting of the job switches used for the job.
   *
   * @return The current setting of the job switches used for the job.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("jobSwitches")).trim(); //@B1A
  }

  
  /**
   * Returns the language identifier associated with this job.
   * @return The language identifier associated with this job.
   * @see #setLanguageID
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((String)get("languageID")).trim(); //@B1A
  }

  
  /**
   * Returns the value indicating whether or not messages are logged for
   * CL programs that are run.
   * @return The value indicating whether or not messages are logged for
   * CL programs that are run. Possible balues are: *YES and *NO.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((String)get("loggingOfCLPrograms")).trim(); //@B1A
  }

  
  /**
   * Returns the logging level. It indicates what type of information is logged.
   * @return The logging level. The possible values are :
   * <ul>
   *    <li> 0  No messages are logged.
   *    <li> 1  All messages sent to the job's external message queue with a severity
   *            greater than or equal to the message logging severity are logged.
   *    <li> 2  The following information is logged :
        <UL> 
   *        <li>Level 1 information
   *        <li>Requests or commands from CL programs for which the system issues messages
   *            with a severity code greater than or equal to the logging severity.
            <li>All messages associated with those requests or commands that have a severity
                code greater than or equal to the logging severity.
        </UL> 
   *    <li> 3  The following information is logged :
        <UL>
   *        <LI>Level 1 information
   *        <LI>All requests or commands from CL programs.
   *        <LI>All messages associated with those requests or commands that have a
   *            severity greater than or equal to the logging severity.
            <li>All messages associated with those requests or commands that have a severity
                code greater than or equal to the logging severity.
        </UL>
   *    <li> 4  The following information is logged :
        <UL> 
   *        <li>All requests or commands from CL programs.
   *        <li>All messages with a severity code greater than or equal to the logging severity.
        </UL>
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return Integer.parseInt(((String)get("loggingLevel")).trim()); //@B1A
  }

  
  /**
   * Returns the logging severity. This is the minimum severity level that causes error
   * messages to be logged in the job log.
   * @return The logging severity.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((Integer)get("loggingSeverity")).intValue(); //@B1A
  }

  
  /**
   * Returns the logging text. This is the level of mesage text that is written in the
   * job or displayed to the user when an error message is created according to the
   * first two message logging values.
   *
   * @return The logging text. The possible values are:
   * <ul>
   *    <li> "*MSG" - Only the mesage is written to the job log.
   *    <li> "*SECLVL" - Both the message and the message help for the error message
   * is written to the job log.
   *    <li> "*NOLIST" - If  the job ends normally, there is no job log. If the job
   * ends abnormally (if the job end code is 20 or higher), there is a job log. The
   * messages appearing in the job's log contain both the message and the message help.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("loggingText")).trim(); //@B1A
  }

  
  /**
   * Returns the mode name. This is the mode name of the advanced program-to-program
   * communications device that started the job.
   *
   * @return The mode name. The possible values are:
   * <ul>
   *    <li> "*BLANK" - The mode name is *BLANK.
   *    <li> " " - The mode name is blank.
   *    <li> Mode name - The name of the mode.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
    String value = ((String)get("modeName")).trim(); //@B1A
    if (value.equals(""))
        value = " ";
    return value;
  }

  
  /**
   * Returns the job name.
   *
   * @return The job name.
  **/
  public String getName()
  {
      return jobNameCasePreserved_;                         // @A3C
  }


  /**
   * Returns the job number.
   *
   * @return The job number.
  **/
  public String getNumber()
  {
      return jobNumber_;
  }

  
  /**
   * Returns the number of libraries in the system part of the library list of the initial thread.
   *
   * @return The number of libraries in the system part of the library list of the initial thread.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("numberOfLibrariesInSYSLIBL")).intValue(); //@B1A
  }

  
  /**
   * Returns the number of libraries in the user library list of the initial thread.
   *
   * @return The number of libraries in the user library list of the initial thread.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("numberOfLibrariesInUSRLIBL")).intValue(); //@B1A
  }

  
  /**
   * Returns the number of product libraries found in the library list of the initial thread.
   *
   * @return The number of product libraries found in the library list of the initial thread.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("numberOfProductLibraries")).intValue(); //@B1A
  }

  
  /**
   * Returns the fully-qualified integrated file system path name of the output queue.
   *
   * @return The fully-qualified integrated file system path name of the output queue.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String oqn = ((String)get("outputQueueLibraryName")).trim(); //@B1A
      if (oqn.length() > 0)                                        //@A2A
        oqn += "/";
      oqn += ((String)get("outputQueueName")).trim(); //@B1A
      return oqn;
  }

  
  /**
   * Returns the output priority for spooled files that this job
   * produces.
   *
   * @return The output priority for spooled files that this job produces.
   * The highest priority is 0, and the lower is 9.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public int  getOutputQueuePriority()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
      return Integer.parseInt(((String)get("outputQueuePriority")).trim()); //@B1A
  }

  
  /**
   * Returns the identifier of the system-related pool from which
   * the job's main storage is allocated.
   *
   * @return The identifier of the system-related pool from which
   *         the job's main storage is allocated.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("systemPoolIdentifier")).intValue(); //@B1A
  }

  
  /**
   * Returns the printer device name used for printing output from this job.
   *
   * @return The printer device name used for printing output from this job.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("printerDeviceName")).trim(); //@B1A
  }

  
  /**
   * Returns the print key format. The print key format indicates whether border and
   * header information is provided when the Print key is pressed.
   *
   * @return The print key format. The possible values are:
   * <ul>
   *    <li> "*NONE" - The border and header information is not included with output from the Print key.
   *    <li> "*PRTBDR" - The border information is included with output from the Print key.
   *    <li> "*PRTHDR" - The header information is included with output from the Print key.
   *    <li> "*PRTALL" - The border and header information is included with output from the Print key.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((String)get("printKeyFormat")).trim(); //@B1A
  }

  
  /**
   * Returns the print text. This is the line of text (if any) that is printed at the bottom of each page of
   * printer output for the job.
   *
   * @return The print text.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("printText")).trim(); //@B1A
  }

  
  /**
   * Returns the libraries that contain product information for the initial thread.
   * If there are no libraries in the product library list, an array of
   * size 0 is returned.
   * @return The libraries that contain product information for the initial thread.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      int num = getNumberOfProductLibraries(); //@B0- this loads 0700
      return productLibraries_;
  }

  
  //@A2A
  /**
   * Returns the value which indicates whether the job is eligible to be
   * moved out of main storage and put into auxiliary storage at the end
   * of a time slice or when beginning a long wait (such as waiting for
   * a work station user's response).
   * @return The value which indicates whether the job is to be purged.
   * The possible values are:
   * <ul>
   *    <li> true  - The job is eligible to be purged.
   *    <li> false - The job is not eligible to be purged.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String value = ((String)get("purge")).trim(); //@B1A
      if (value.equalsIgnoreCase("*YES")) //@B1C
          return true;
      else
          return false;
  }

  
  /**
   * Returns the fully-qualified integrated file system path name
   * of the job queue that the job is currently on, or that the job
   * is on when it is active. This value is for jobs whose
   * status is *JOBQ or *ACTIV. For jobs with a status of *OUTQ, the
   * value for this field is blank.
   *
   * @return  The fully-qualified integrated file system path name
   *          of the job queue that the job is currently on, or that
   *          the job is on when it is active.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String jqn = ((String)get("jobQueueLibraryName")).trim(); //@B1A
      if (jqn.length() > 0)                                     //@A2A
        jqn += "/";
      jqn += ((String)get("jobQueueName")).trim(); //@B1A
      return jqn;
  }


  /**
   * Returns the scheduling priority of the job compared to other jobs on the same job queue.
   * The highest priority is 0 and the lowest is 9. This value is for jobs whose status is *JOBQ
   * or *ACTIVE. For jobs with a status of *OUTQ, the value for this field is -1.
   * @return The scheduling priority of the job.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      String value = ((String)get("jobQueuePriority")).trim(); //@B1A
      if(value.length() > 0)                                   //@B0C @B1C
          return Integer.parseInt(value); //@B0C
      else
          return -1;
  }

  
  /**
   * Returns the routing data that is used to determine the routing entry that identifies the program to start for the routing step.
   *
   * @return The routing data that is used to determine the routing entry that identifies the program to start for the routing step.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("routingData")).trim(); //@B1A
  }


  /**
   * Returns the priority at which the job is currently running, relative
   * to other jobs on the system.
   * The run priority ranges from 1(highest) to 99 (lowest).
   *
   * @return The priority at which the job is currently running.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("runPriority")).intValue(); //@B1A
  }

  
  /**
   * Returns the value which indicates whether the job is to be treated like
   * a signed-on user on the system.
   *
   * @return The value which indicates whether the job is to be treated like
   * a signed-on user on the system. The possible values are:
   * <ul>
   *    <li> true  - The job should be treated like a signed-on user.
   *    <li> false - The job should not be treated like a signed-on user.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String value = ((String)get("signedOnJob")).trim(); //@B1A
      if (value.equals("0"))
          return true;
      else
          return false;
  }

  
  /**
   * Returns the full qualified integrated file system path name of sort
   * sequence table associated with this job.
   * @return The full qualified integrated file system path name of sort
   * sequence table associated with this job.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
        String sst = ((String)get("sortSequenceLibrary")).trim(); //@B1A
        if (sst.length() > 0)                                     //@A2A
          sst += "/";
        sst += ((String)get("sortSequence")).trim(); //@B1A
        return sst;
    }

  
  /**
   * Returns the job status.
   *
   * @return The job status. The possible values are:
   * <ul>
   *    <li>"*ACTIVE"  - Active jobs, this includes group jobs, system request jobs,
   *                     and disconnected jobs.
   *    <li>"*JOBQ" - Jobs that are currently on job queues.
   *    <li>"*OUTQ" - Jobs that have completed running but still have output
   *                  on an output queue.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("jobStatus")).trim(); //@B1A
  }

  
  /**
   * Returns the value indicating whether you want status messages displayed for this job.
   *
   * @return The value indicating whether you want status messages displayed for this job.
   * The possible values are:
   * <ul>
   *    <li>"*NONE"  - The job does not display status message.
   *    <li>"*NORMAL" - The job displays status message.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("statusMessageHandling")).trim(); //@B1A
  }

  
  /**
   * Returns the fully-qualified integrated file system path name of the
   * subsystem description in which the job is running.
   *
   * @return The fully-qualified integrated file system path name of the
   *         subsystem description, or "" if the job is not active.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      String sn = ((String)get("subsystemDescriptionLibraryName")).trim(); //@B1A
      if (sn.length() > 0)                                                 //@A2A
        sn += "/";
      sn += ((String)get("subsystemDescriptionName")).trim(); //@B1A
      return sn;
  }

  
  /**
   * Returns additional information about the job type.
   *
   * @return The additional information about the job type. Possible
   *         values are:
   *         <ul>
   *           <li>" "  - The job has no special subtype.
   *           <li>"D" - The job is an immediate job.
   *           <li>"E" - The job started with a procedure start request.
   *           <li>"F" - The job is an Advanced 36 server (M36) job.
   *           <li>"J" - The job is a prestart job.
   *           <li>"P" - The job is a print driver job.
   *           <li>"T" - The job is a System/36 multiple requester
   *                     terminal (MRT) job.
   *           <li>"U" - Alternate spool user.
   *         </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("jobSubtype")).trim(); //@B1A
  }

  
  /**
    * Returns the AS/400 system from which the job information will
    * be retrieved.
    *
    * @return The AS/400 system from which the job information will
    * be retrieved.
    **/
  public AS400 getSystem()
  {
       return as400_;
  }
  
  
  /**
   * Returns the system portion of the library list of the initial thread.
   * If there are no libraries in the system library list, an array of
   * size 0 is returned.
   * @return The system portion of the library list of the initial thread.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      int num = getNumberOfLibrariesInSYSLIBL(); //@B0- this loads 0700
      return systemLibraryList_;
  }

  
  /**
   * Returns the time separator. This is the time separator used to separate hours,
   * minutes, and seconds when representing a time.
   *
   * @return The time separator. The possible values are :
   * <ul>
   *    <li> ":" A colon (:) is used for the time separator.
   *    <li> "." A period (.) is used for the time separator.
   *    <li> " " A blank ( ) is used for the time separator.
   *    <li> "," A comma (,) is used for the time separator.
   * </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("timeSeparator")).trim(); //@B1A
  }


  //@A2A
  /**
   * Gets the time slice (in milliseconds). The time slice is the maximum amount of
   * processor time (in milliseconds) given to threads in this job before other
   * threads in this job are given the opportunity to run.
   * @return The time slice (in milliseconds).
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("timeSlice")).intValue(); //@B1A
  }

  
  //@A2A
  /**
   * Gets the key indicating whether interactive jobs are moved to
   * another main storage pool at the end of the time slice.
   *
   * @return The time slice end pool setting.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("timeSliceEndPool")).trim(); //@B1A
  }


  /**
   * Returns the total amount of response time for the job, in milliseconds.
   *
   * @return The total amount of response time for the job, in milliseconds.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((Integer)get("responseTimeTotal")).intValue(); //@B1A
  }

  
  /**
   * Returns the job type.
   *
   * @return The job type. Possible values are:
   *         <ul>
   *           <li>" "  - The job is not a valid job.
   *           <li>"A" - The job is an autostart job.
   *           <li>"B" - The job is a batch job.
   *           <li>"I" - The job is an interactive job.
   *           <li>"M" - The job is a subsystem monitor job.
   *           <li>"R" - The job is a spooled reader job.
   *           <li>"S" - The job is a system job.
   *           <li>"W" - The job is a spooled writer job.
   *           <li>"X" - The job is a SCPF system job.
   *         </ul>
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      return ((String)get("jobType")).trim(); //@B1A
  }

  
  /**
   * Returns the user profile under which the job runs.
   *
   * @return The user profile under which the job runs.
  **/
  public String getUser()
  {
      return userNameCasePreserved_;                     // @A3C
  }

  
  /**
   * Returns the user portion of the library list for the initial thread.
   * If there are no libraries in the user library list, an array of
   * size 0 is returned.
   * @return The user portion of the library list for the initial thread.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
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
      int num = getNumberOfLibrariesInUSRLIBL(); //@B0- this loads 0700
      return userLibraryList_;
  }

  
  /**
   * Returns the unit of work identifier. The unit of work identifier is used to track jobs
   * across multiple systems. If a job is not associated with a source or
   * target system using advanced program-to-program communications(APPC),
   * this information is not used. Every job on the system is assigned a unit
   * of work identifier. The unit-of-work identifier is made up of :
   * <ul>
   *    <li> Location name -- 8 Characters. The name of the source system that
   *                          originaled the APPC job.
   *    <li> Network ID -- 8 Characters. The network name associated with the
   *                       unit of work.
   *    <li> Instance -- 6 Characters. The value that further identifies the
   *                     source of the job. this is shown as hexadecimal data.
   *    <li> Sequence Number -- 2 Character. A value that identifies a chect-point
   *                            within the application program.
   * </ul>
   *
   * @return The unit of work identifier.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   *
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
      return ((String)get("unitOfWorkID")).trim(); //@B1A
  }

  
  /**
   * The JobFormats can't be static because we need an AS400 object
   * to do the character conversion.
  **/
  private void initializeFormats()
  {
    format0150_ = new JobI0150Format(as400_);
    format0200_ = new JobI0200Format(as400_);
    format0300_ = new JobI0300Format(as400_);
    format0400_ = new JobI0400Format(as400_);
    format0500_ = new JobI0500Format(as400_);
    format0600_ = new JobI0600Format(as400_);
    format0700_ = new JobI0700Format(as400_);
    
    // Map all of the field names to their respective formats
    // so we can easily lookup the format for a given field name out of
    // the hashtable.
    mapFormat(format0150_);
    mapFormat(format0200_);
    mapFormat(format0300_);
    mapFormat(format0400_);
    mapFormat(format0500_);
    mapFormat(format0600_);
    mapFormat(format0700_);
    
  }
    
  
  //@B1C - Changed this method and javadoc
  /**
   * Loads job information from the AS/400. After calling this
   * method, the next time a get() method is called, it will
   * retrieve the information in its format from the AS/400
   * instead of from its own internal cache.
  **/
  public void loadInformation()
  {
    // Refresh all record formats
    formatToRecordMap_.clear();
    // The next time a get() or set() is done, the record data for the
    // particular format will be reloaded.
  }


  //@B1A
  /**
   * Looks up the specified field and loads the information
   * for the format it belongs to.
  **/
  private void loadInformation(String field)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    JobFormat jf = (JobFormat)fieldToFormatMap_.get(field);
    loadInformation(jf);
  }
  
  
  //@B1A
  /**
   * Loads job information from the AS/400 for the specified
   * format. (It does not load all of the formats). If the
   * specified format was previously loaded, this method
   * does nothing. To always load the specified format, you
   * should call loadInformation() first to clear the format
   * cache; or, you could manually remove the specific format
   * from the format cache.
  **/
  private void loadInformation(JobFormat format)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {    
    // Check to see if the format has been loaded already
    if (((Record)formatToRecordMap_.get(format)) != null) return;
    byte[] jobInformationData = null;
    QSYSObjectPathName prgName = new QSYSObjectPathName("QSYS","QUSRJOBI","PGM");
    ProgramParameter[] parmList = new ProgramParameter[6];
    AS400Bin4 bin4 = new AS400Bin4();
    AS400Text text;
    int ccsid = as400_.getCcsid();
        
    int receiverLength = format.getNewRecord().getRecordLength();
    parmList[0] = new ProgramParameter(receiverLength);           
    parmList[1] = new ProgramParameter(bin4.toBytes(receiverLength));

    text = new AS400Text(8, ccsid, as400_);
    parmList[2] = new ProgramParameter(text.toBytes(format.getName()));
        
    AS400Text[] member = new AS400Text[3];
    member[0] = new AS400Text(10, ccsid, as400_);
    member[1] = new AS400Text(10, ccsid, as400_);
    member[2] = new AS400Text(6, ccsid, as400_);
    AS400Structure structure = new AS400Structure(member);
    String[] qualifiedJobName = { jobName_, userName_, jobNumber_ };
    parmList[3] = new ProgramParameter(structure.toBytes(qualifiedJobName));
              
    text = new AS400Text(16, ccsid, as400_);
    parmList[4] = new ProgramParameter(text.toBytes(internalJobID_));
        
    byte[] errorInfo = new byte[32];
    parmList[5] = new ProgramParameter(errorInfo, 0);
       
    ProgramCall program = new ProgramCall(as400_);
    try
    {
      program.setProgram(prgName.getPath(), parmList);
    }
    catch(PropertyVetoException pve) {} // Quiet the compiler

    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Retrieving job information for job "+jobNumber_);
    }
    if (program.run() != true)
    {
      AS400Message[] msgList = program.getMessageList();
      if (Trace.isTraceOn() && Trace.isTraceErrorOn())
      {
        Trace.log(Trace.ERROR, "Error retrieving job information:");
        for (int i=0;i<msgList.length;i++)
        {
          Trace.log(Trace.ERROR, msgList[i].toString());
        }
      }
      throw new AS400Exception(msgList);
    }
    byte[] retrievedData = parmList[0].getOutputData();
    formatToRecordMap_.put(format, format.getNewRecord(retrievedData));
    if (format.getName().equals("JOBI0700"))
      parseLibraryList();
  }


  //@B1A
  /**
    Parses the library names out of the library list on the
    JOBI0700 format.
  **/
  private void parseLibraryList()
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    // First get the libraryList field from the 0700 format
    String entireList = (String)get("libraryList");
    int numSysLibs = getNumberOfLibrariesInSYSLIBL();
    int numProdLibs = getNumberOfProductLibraries();
    int numCurLibs = getCurrentLibraryExistence() ? 1 : 0;
    int numUsrLibs = getNumberOfLibrariesInUSRLIBL();
    systemLibraryList_ = new String[numSysLibs];
    productLibraries_ = new String[numProdLibs];
    currentLibrary_ = new String[numCurLibs];
    userLibraryList_ = new String[numUsrLibs];
    
    StringTokenizer parser = new StringTokenizer(entireList);
    try
    {
      for (int i=0; i<numSysLibs; ++i)
        systemLibraryList_[i] = parser.nextToken();
      for (int i=0; i<numProdLibs; ++i)
        productLibraries_[i] = parser.nextToken();
      for (int i=0; i<numCurLibs; ++i)
        currentLibrary_[i] = parser.nextToken();
      for (int i=0; i<numUsrLibs; ++i)
        userLibraryList_[i] = parser.nextToken();
    }
    catch(NoSuchElementException e)
    {
      int totalLibsInString = parser.countTokens();
      if (Trace.isTraceOn() && Trace.isTraceWarningOn())
      {
        int totalLibsOnAPI = numSysLibs+numProdLibs+numCurLibs+numUsrLibs;
        if (totalLibsInString != totalLibsOnAPI)
        {
          Trace.log(Trace.WARNING, "Number of libraries in library list mismatched.", e);
          Trace.log(Trace.WARNING, "Total number specified on API: "+totalLibsOnAPI);
          Trace.log(Trace.WARNING, "Number returned in libraryList field: "+totalLibsInString);
        }
      }
    }
  }
    
      
  //@B1A
  /**
   * If caching, adds value to the changes cache. If not caching,
   * sets the value to the 400 immediately.
   * Either way, it always updates the value in this object's
   * format cache.
  **/
  private void set(String field, Object value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    // Use 2 separate methods here, because some setters
    // have mismatched field names for the set vs. get API calls.
    // For example, setOutputQueue() uses "outputQueueName"
    // on the set API to set the output queue name and library
    // both; but on the get API (and hence internal format cache)
    // there are 2 fields - "outputQueueName" for the name and
    // "outputQueueLibraryName" for the library.
    setForAPICall(field, value);
    setForInternalCache(field, value);
  }
    
  
  /**
   * Sets the break message handling.
   * @param newHandling The new break message handling.
   * Possible values are :
   * <ul>
   *    <li> *NORMAL The message queue status determines break message handling.
   *    <li> *HOLD   The message queue holds break messages until a user or
   *                 program requests them. The work station user uses the
   *                 Display Message(DSPMSG) command to display the messages
   *                 a program must issue a Receive Message(RCVMSG) command to
   *                 receive a message and handle it.
   *    <li> *NOTIFY The system notifies the job's message queue when a message
   *                 arrives. For interactive jobs, the audible alarm sounds if
   *                 there is one, and the message-waiting light comes on.
   * </ul>
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getBreakMessageHandling
  **/
  public void setBreakMessageHandling(String newHandling)
           throws  AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newHandling == null)
      throw new NullPointerException("newHandling");
    set("breakMessageHandling", newHandling.trim());
  }

  
  /**
   * Sets the value indicating whether the changes are cached. If set to
   * false the value information stored in the cache will be cleared.
   * @param useCache The value indicating whether the changes are cached.
   *
  **/
  public synchronized void setCacheChanges(boolean useCache)
  {
      if (!useCache)
          cachedChanges_.clear();
      isCached_ = useCache;
  }

  
  /**
   * Sets the coded character set identifier.
   * @param newID The new CCSID.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getCodedCharacterSetID
  **/
  public void setCodedCharacterSetID(int newID)
           throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    set("codedCharacterSetID", new Integer(newID));
  }

  
  /**
   * Sets the country identifier.
   * @param newID The new country identifier.
   * The possible values are :
   * <ul>
   *    <li> *SYSVAL    The system value QCNTRYID is used.
   *    <li> country-ID Specify the country identifier to be used by the job.
   * </ul>
   * @see #getCountryID
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getCountryID
  **/
  public void setCountryID(String newID)
           throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newID==null)
    {
        throw new NullPointerException("newID");
    }
    set("countryID", newID.trim());
  }


  /**
   * Sets the date format.
   *
   * @param newFormat The new date format.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getDateFormat
  **/
  public void setDateFormat(String newFormat)
           throws AS400Exception,
                  AS400SecurityException,
                  ConnectionDroppedException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException,
                  UnsupportedEncodingException
  {
    if (newFormat==null)
    {
        throw new NullPointerException("newFormat");
    }
    set("dateFormat", newFormat.trim());
  }

  
  /**
   * Sets the date separator.
   *
   * @param newSeparator The new date separator.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getDateSeparator
  **/
  public void setDateSeparator(String newSeparator)
           throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newSeparator==null)
    {
        throw new NullPointerException("newSeparator");
    }
    set("dateSeparator", newSeparator);
  }

  
  /**
   * Sets the DDM conversation handling.
   *
   * @param newHandling The new DDM conversation handling.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getDDMConversationHandling
  **/
  public void setDDMConversationHandling(String newHandling)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newHandling==null)
    {
        throw new NullPointerException("newHandling");
    }
    set("DDMConversationHandling", newHandling.trim());
  }

  
  /**
   * Sets the decimal format.
   * @param newFormat The new decimal format.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getDecimalFormat
  **/
   public void setDecimalFormat(String newFormat)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newFormat==null)
    {
        throw new NullPointerException("newFormat");
    }
    // Valid values on the API set are "*BLANK", "*SYSVAL", "J", and "I".
    // Note the API get returns a 1-char String.
    set("decimalFormat", newFormat.trim());
  }

  
  /**
   * Sets the default wait time (in seconds).
   *
   * @param newTime The new default wait time (in seconds).
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setDefaultWait(int newTime)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    set("defaultWait", new Integer(newTime));
  }

  
  /**
   * Sets the device recovery action.
   *
   * @param newAction The new device recovery action.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getDeviceRecoveryAction
  **/
  public void setDeviceRecoveryAction(String newAction)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newAction==null)
    {
        throw new NullPointerException("newAction");
    }
    set("deviceRecoveryAction", newAction.trim());
  }


  //@B1A
  /**
   * If caching, adds value to the changes cache. If not caching,
   * sets the value to the 400 immediately. Does NOT update the
   * local value to reflect the change. This method should always
   * be used in conjunction with setForInternalCache().
  **/
  private void setForAPICall(String field, Object value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (!isCached_)
    {
      // Change the info on the 400
      Hashtable temp = new Hashtable();
      temp.put(field, value);
      setChanges(temp);
    }
    else
    {
      // Cache the change
      cachedChanges_.put(field, value);
    }      
  }
  
     
  //@B1A
  /**
   * Updates the value in the format cache. Does NOT set the value
   * to the 400. Does NOT add it to the changes cache. This method
   * should always be used in conjunction with setForAPICall().
  **/
  private void setForInternalCache(String field, Object value)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    // Change the info in our own record
    JobFormat jf = (JobFormat)fieldToFormatMap_.get(field);
    loadInformation(jf); // Make sure we've loaded the info, otherwise
                         // the record in the cache will be null.
    try
    {
      ((Record)formatToRecordMap_.get(jf)).setField(field, value);
    }
    catch(ExtendedIllegalArgumentException e)
    {
      if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
      {
        Trace.log(Trace.DIAGNOSTIC, "Job info field type mismatch. Possibly expected.", e);
      }
      // The length of the field on the set API is probably different
      // than the length of the field on the get API. So we'll just
      // get the new value from the system.
      // Reset this one format so just it will get reloaded.
      formatToRecordMap_.remove(jf); 
      loadInformation(jf);
    }  
  }
  
  
  /**
   * Sets the inquiry message reply.
   *
   * @param newReply The new inquiry message reply.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getInquiryMessageReply
  **/
  public void setInquiryMessageReply(String newReply)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newReply==null)
    {
        throw new NullPointerException("newReply");
    }
    set("inquiryMessageReply", newReply.trim());
  }


  /**
   * Sets the job accounting code.
   *
   * @param newCode The new job accounting code.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getJobAccountingCode
  **/
  public void setJobAccountingCode(String newCode)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newCode==null)
    {
        throw new NullPointerException("newCode");
    }
    set("jobAccountingCode", newCode.trim());
  }

  
  /**
   * Sets the date that is assigned to the job.
   *
   * @param newDate The new job date.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getJobDate
  **/
  public void setJobDate(Date newDate)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newDate==null)
    {
        throw new NullPointerException("newDate");
    }
    String stringValue = parseDateString(newDate, CYYMMDD_FORMAT);
    set("jobDate", stringValue);
  }

  
  /**
   * Sets the job message queue full action.  This is the action to take when the message
     queue is full.  Valid values are:
     <UL>
        <li>*NOWRAP When the job message queue is full, do not wrap.  This action causes the job
            to end.
        <li>*WRAP When the job message queue is full, wrap to the beginning and start filling again.
        <li>*PRTWRAP When the job message queue is full, wrap the message queue and print the
            messages that are being overlaid because of the wrapping.   
     </UL>
   *
   * @param newAction - The new job message queue full action.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getJobMessageQueueFullAction
  **/
  public void setJobMessageQueueFullAction(String newAction)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newAction==null)
    {
        throw new NullPointerException("newAction");
    }
    set("jobMessageQueueFullAction", newAction.trim());
  }


  /**
   * Sets the job switches used by this job.
   *
   * @param newSwitches The new job switches.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getJobSwitches
  **/
  public void setJobSwitches(String newSwitches)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newSwitches==null)
    {
        throw new NullPointerException("newSwitches");
    }
    set("jobSwitches", newSwitches.trim());
  }


  /**
   * Sets the language identifier that is associated
   * with this job. The language identifier is used when *LANGIDUNQ OR *LANGIDSHR
   * is specified on the sort sequence parameter. If the job CCSID is 65525, this
   * parameter is also used to determine the value of the job default CCSID.
   * The possible values are :
   * <ul>
   *    <li> *SYSVAL     The system value QLANGID is used.
   *    <li> language-ID Specify the language identifier to be used by the job.
   * </ul>
   * @param newID The new language identifier.
   * @see #getLanguageID
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getLanguageID
  **/
  public void setLanguageID(String newID)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newID==null)
    {
        throw new NullPointerException("newID");
    }
    set("languageID", newID.trim());
  }


  /**
   * Sets the value indicating whether or not messages are logged for CL programs that are run.
     The possible values are *YES and *NO.
   *
   * @param newPrograms The new logging of CL programs.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getLoggingCLPrograms
  **/
  public void setLoggingCLPrograms(String newPrograms)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newPrograms==null)
    {
        throw new NullPointerException("newPrograms");
    }
    set("loggingOfCLPrograms", newPrograms.trim());
  }

  
  /**
   * Sets the logging level. It indicates what type of information is logged.
   * The possible values are :
   * <ul>
   *    <li> 0  No messages are logged.
   *    <li> 1  All messages sent to the job's external message queue with a severity
   *            greater than or equal to the message logging severity are logged.
   *    <li> 2  The following information is logged :
        <UL> 
   *        <li>Level 1 information
   *        <li>Requests or commands from CL programs for which the system issues messages
   *            with a severity code greater than or equal to the logging severity.
            <li>All messages associated with those requests or commands that have a severity
                code greater than or equal to the logging severity.
        </UL> 
   *    <li> 3  The following information is logged :
        <UL>
   *        <LI>Level 1 information
   *        <LI>All requests or commands from CL programs.
   *        <LI>All messages associated with those requests or commands that have a
   *            severity greater than or equal to the logging severity.
            <li>All messages associated with those requests or commands that have a severity
                code greater than or equal to the logging severity.
        </UL>
   *    <li> 4  The following information is logged :
        <UL> 
   *        <li>All requests or commands from CL programs.
   *        <li>All messages with a severity code greater than or equal to the logging severity.
        </UL>
   * </ul>
   *
   * @param newLevel The new logging level.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getLoggingLevel
  **/
  public void setLoggingLevel(int newLevel)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    set("loggingLevel", Integer.toString(newLevel));
  }


  /**
   * Sets the logging severity. This is the minimum severity level that causes error messages to be
     logged in the job log. 
   *
   * @param newSeverity The new logging severity.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getLoggingSeverity
  **/
  public void setLoggingSeverity(int newSeverity)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    set("loggingSeverity", new Integer(newSeverity));
  }

  
  /**
   * Sets the logging text.  This is the level of message text that is written in the
     job log or displayed to the user when an error message is created.  Possible values are:
     <UL>
        <li>*MSG Only the message is written to the job log.
        <li>*SECLVL  Both the message and the message help for the error message is written to
                     the job log.
         li>*NOLIST If the job ends normally, there is no job log.  If the job ends abnormally,
                    (if the job end code is 20 or higher), there is a job log.  The messages
                    appearing in the job's log contain both the message and the message help. 
     </UL>
   *
   * @param newText The new logging text.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getLoggingText
  **/
  public void setLoggingText(String newText)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newText==null)
    {
        throw new NullPointerException("newText");
    }
    set("loggingText", newText.trim());
  }


  /**
   * Sets the output queue.  This is the default output queue that is used for spooled output
     produced by this job. The default output queue is only for spooled printer files that
     specify *JOB for the output queue.
   * @param newOutq The new output queue.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getOutputQueue
  **/
  public void setOutputQueue(String newOutq)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {

    if (newOutq == null)
    {
        throw new NullPointerException("newOutq");
    }
     int nameIndex = newOutq.indexOf("/");
     String qLib = "          "; // pad to 10 chars for API call
     if (nameIndex > 0)
     {
       qLib = newOutq.substring(0,nameIndex);
       int libLength = qLib.length();
       for (int i=0; i < 10-libLength; ++i) qLib += " ";
     }
     String qName = newOutq.substring(nameIndex+1);
     int nameLength = qName.length();
     for (int i=0; i < 10-nameLength; ++i) qName += " ";

    // Have to manually set the output queue since there is a field
    // mismatch between the set API and the get API
    // "outputQueue" exists as a 20-char field on the set API
    // "outputQueueName" and "outputQueueLibraryName" exist as 10-char
    // fields on the get API
    setForAPICall("outputQueueName", qName+qLib); // name then lib on API call
    setForInternalCache("outputQueueName", qName);
    setForInternalCache("outputQueueLibraryName", qLib);
  }

  
  /**
   * Sets the output queue priority.  The highest priority is 0, and the lowest is 9.
   * @param newPriority The new output queue priority.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getOutputQueuePriority
  **/
  public void setOutputQueuePriority(int newPriority)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
     if (newPriority < 1 || newPriority > 9)                     //@A2A
     {                                                           //@A2A
       throw new ExtendedIllegalArgumentException("newPriority", //@A2A
           ExtendedIllegalArgumentException.RANGE_NOT_VALID);    //@A2A
     }                                                           //@A2A
    set("outputQueuePriority", Integer.toString(newPriority));
  }

  
  /**
   * Sets the printer device name.
   *
   * @param newName The new printer device name.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getPrinterDeviceName
  **/
  public void setPrinterDeviceName (String newName)
              throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newName==null)
    {
        throw new NullPointerException("newName");
    }
    set("printerDeviceName", newName.trim());
  }

  
  /**
   * Sets the print key format.
   *
   * @param newFormat The new print key format.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getPrintKeyFormat
  **/
  public void setPrintKeyFormat(String newFormat)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newFormat==null)
    {
        throw new NullPointerException("newFormat");
    }
    set("printKeyFormat", newFormat.trim());
  }

  
  /**
   * Sets the print text.  This is the line of text (if any) that is printed at the bottom
     of each page of printed output for the job.
   *
   * @param newText The new print text.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getPrintText
  **/
  public void setPrintText (String newText)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newText==null)
    {
        throw new NullPointerException("newText");
    }
    set("printText", newText.trim());
  }


  /**
   * Sets the value indicating whether or not the job is eligible to be moved out of main storage
   * and put into auxiliary storage at the end of a time slice or when
   * entering a long wait (such as waiting for a work station user's response).
   *
   * @param isPurge The new indication whether or not the job is eligible to be
   *                moved out of main storage and put into auxiliary storage.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setPurge(boolean isPurge)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    set("purge", isPurge ? "*YES" : "*NO");
  }

  
  /**
   * Sets the job queue.
   *
   * @param newJobQueue The new job queue.
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getQueue
  **/
  public void setQueue(String newJobQueue)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newJobQueue==null)
    {
        throw new NullPointerException("newQueue");
    }
     int nameIndex = newJobQueue.indexOf("/");
     String qLib = "          "; // pad to 10 chars for API set
     if (nameIndex > 0)
     {
       qLib = newJobQueue.substring(0,nameIndex);
       int libLength = qLib.length();
       for (int i=0; i < 10-libLength; ++i) qLib += " ";
     }
     String qName = newJobQueue.substring(nameIndex+1);
     int nameLength = qName.length();
     for (int i=0; i < 10-nameLength; ++i) qName += " ";

    // Have to manually set the output queue since there is a field
    // mismatch between the set API and the get API
    // "jobQueueName" exists as a 20-char field on the set API
    // "jobQueueName" and "jobQueueLibraryName" exist as 10-char
    // fields on the get API
    setForAPICall("jobQueueName", qName+qLib); // name then lib on API call
    setForInternalCache("jobQueueName", qName);
    setForInternalCache("jobQueueLibraryName", qLib);
  }

 
 /**
   * Sets the job queue priority.  This is the scheduling priority of the job compared
     to other jobs on the same job queue.  This highest priority is 0 and the lowest is 9.
   *
   * @param newPriority The new job queue priority.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getQueuePriority
  **/
  public void setQueuePriority(int newPriority)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
     if (newPriority < 0 || newPriority > 9)                     //@A2A
     {                                                           //@A2A
       throw new ExtendedIllegalArgumentException("newPriority", //@A2A
           ExtendedIllegalArgumentException.RANGE_NOT_VALID);    //@A2A
     }                                                           //@A2A
    set("jobQueuePriority", Integer.toString(newPriority));
  }


  /**
   * Sets the run priority.  This is the priority at which the job is currently running,
     relative to other jobs on the system.  
   *
   * @param newPriority The new run priority. It ranges from 1 (highest) to 99 (lowest).
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getRunPriority
  **/
  public void setRunPriority(int newPriority)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    set("runPriority", new Integer(newPriority));
  }

  
  /**
   * Sets the schedule date.  
   *
   * @param newDate The new schedule date.
   * @see #getScheduleDate
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setScheduleDate(Date newDate)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newDate==null)
    {
        throw new NullPointerException("newDate");
    }
    String stringValue = parseDateString(newDate, CYYMMDD_FORMAT);
    
    // Have to manually set the schedule date since there is a field
    // mismatch between the set API and the get API
    // "scheduleDate" exists on the set API
    // "dateAndTimeJobIsScheduledToRun" exists on the get API
    setForAPICall("scheduleDate", stringValue);
    AS400Text text = new AS400Text(stringValue.length(), as400_.getCcsid(), as400_);
    byte[] dateTimeStamp = text.toBytes(stringValue);
    setForInternalCache("dateAndTimeJobIsScheduledToRun", dateTimeStamp);
  }

  
  /**
   * Sets the schedule date.
   *
   * @param newDate The new schedule date.
   * @see #getScheduleDate
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setScheduleDate(String newDate)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newDate==null)
    {
        throw new NullPointerException("newDate");
    }
    // Have to manually set the schedule date since there is a field
    // mismatch between the set API and the get API
    // "scheduleDate" exists on the set API
    // "dateAndTimeJobIsScheduledToRun" exists on the get API
    String d = newDate.trim();
    setForAPICall("scheduleDate", d);
    AS400Text text = new AS400Text(d.length(), as400_.getCcsid(), as400_);
    byte[] dateTimeStamp = text.toBytes(d);
    setForInternalCache("dateAndTimeJobIsScheduledToRun", dateTimeStamp);
  }

  
  /**
   * Sets the schedule time.
   *
   * @param newTime The new schedule time.
   * @see #getScheduleDate
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setScheduleTime(Date newTime)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newTime==null)
    {
        throw new NullPointerException("newTime");
    }
    String stringValue = parseDateString(newTime, HHMMSS_FORMAT);
    // Have to manually set the schedule date since there is a field
    // mismatch between the set API and the get API
    // "scheduleTime" exists on the set API
    // "dateAndTimeJobIsScheduledToRun" exists on the get API
    setForAPICall("scheduleTime", stringValue);
    AS400Text text = new AS400Text(stringValue.length(), as400_.getCcsid(), as400_);
    byte[] dateTimeStamp = text.toBytes(stringValue);
    setForInternalCache("dateAndTimeJobIsScheduledToRun", dateTimeStamp);
  }

  
  /**
   * Sets the schedule time.
   *
   * @param newTime The new schedule time.
   * @see #getScheduleDate
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setScheduleTime(String newTime)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newTime==null)
    {
        throw new NullPointerException("newTime");
    }
    // Have to manually set the schedule date since there is a field
    // mismatch between the set API and the get API
    // "scheduleTime" exists on the set API
    // "dateAndTimeJobIsScheduledToRun" exists on the get API
    String t = newTime.trim();
    setForAPICall("scheduleTime", t);
    AS400Text text = new AS400Text(t.length(), as400_.getCcsid(), as400_);
    byte[] dateTimeStamp = text.toBytes(t);
    setForInternalCache("dateAndTimeJobIsScheduledToRun", dateTimeStamp);
  }

  
  /**
   * Sets the sort sequence table.
   *
   * @param newTable The new sort sequence table.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getSortSequenceTable
  **/
  public void setSortSequenceTable(String newTable)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
     if (newTable==null)
     {
        throw new NullPointerException("newTable");
     }
     int nameIndex = newTable.indexOf("/");

     String sqtLib = "          "; // pad to 10 chars for API set
     if (nameIndex > -1)
     {
       sqtLib = newTable.substring(0,nameIndex);
       int libLength = sqtLib.length();
       for (int i=0; i < 10-libLength; ++i) sqtLib += " ";
     }
     String sqtName = newTable.substring(nameIndex+1);
     int nameLength = sqtName.length();
     for (int i=0; i < 10-nameLength; ++i) sqtName += " ";

    // Have to manually set the sort sequence table since there is a field
    // mismatch between the set API and the get API
    // "sortSequenceTable" exists as a 20-char field on the set API
    // "sortSequence" and "sortSequenceLibrary" exist as 10-char
    // fields on the get API
    setForAPICall("sortSequenceTable", sqtName+sqtLib); // name then lib on API call
    setForInternalCache("sortSequence", sqtName);
    setForInternalCache("sortSequenceLibrary", sqtLib);
  }

  
  /**
   * Sets the status message handling.  This is the value that indicates whether the status
     messages are displayed.  Possible values are:
     <UL>
     <LI>*NONE - Do not display status messages.
     <LI>*NORMAL - Display status messages.
     </UL>
   *
   * @param newHandling The new status message handling.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getStatusMessageHandling
  **/
  public void setStatusMessageHandling(String newHandling)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newHandling==null)
    {
        throw new NullPointerException("newHandling");
    }
    set("statusMessageHandling", newHandling.trim());
  }

  
  /**
   * Sets the time separator.
   *
   * @param newSeparator The new time separator.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
   * @see #getTimeSeparator
  **/
  public void setTimeSeparator(String newSeparator)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newSeparator==null)
    {
        throw new NullPointerException("newSeparator");
    }
    set("timeSeparator", newSeparator);
  }

  
  /**
   * Sets the time slice (in milliseconds). The time slice is the maximum amount of
   * processor time (in milliseconds) given to threads in this job before other
   * threads in this job are given the opportunity to run.
   *
   * @param newTimeSlice The new time slice.
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setTimeSlice(int newTimeSlice)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    set("timeSlice", new Integer(newTimeSlice));
  }

  
  /**
   * Sets the key whether you want interactive jobs moved to
   * another main storage pool at the end of the time slice.
   *
   * @param newValue The new key. The possible values are:
   * <ul>
   *    <li> "*SYSVAL" - The value in the system value, QTESPOOL, is used.
   *    <li> "*NONE"   - The job does not move to another mainstorage pool when it
   * reaches the end of the time slice.
   *    <li> "*BASE"   - The job moves to the base pool when it reaches the end of the
   * time slice.
   * </ul>
   *
   * @exception AS400Exception                  If the AS/400 system returns an error message.
   * @exception AS400SecurityException          If a security or authority error occurs.
   * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
   * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
   * @exception InterruptedException            If this thread is interrupted.
   * @exception IOException                     If an error occurs while communicating with the AS/400.
   * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
   * @exception UnsupportedEncodingException    If the character encoding is not supported.
  **/
  public void setTimeSliceEndPool(String newTimeSliceEndPool)
            throws AS400Exception,
                   AS400SecurityException,
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
  {
    if (newTimeSliceEndPool==null)
    {
        throw new NullPointerException("newTimeSliceEndPool");
    }
    set("timeSliceEndPool", newTimeSliceEndPool.trim());
  }

  
  /**
   * Returns the string representation of the job.
   * This is in the format "number/user/name".
   *
   * @return The string representation of the job.
  **/
  public String toString()
  {
    if ((jobNumber_.length() > 0)
    &&  (userNameCasePreserved_.length() > 0)        // @A3C
    &&  (jobNameCasePreserved_.length() > 0))         // @A3C
      return jobNumber_ + "/" +
             userNameCasePreserved_ + "/" +          // @A3C
             jobNameCasePreserved_ ;                 // @A3C
    return ("");
  }

  
  //@B1A
    /**
    * Returns the date for a date string in certain format.
    * @param str The date string to be parsed.
    * @param format The format.
    * @return The date for a date string in certain format.
    **/
  private Date parseDate(String str)
  {
        Calendar dateTime = Calendar.getInstance();
        Date date = null;
        dateTime.clear();
        int ccsid = as400_.getCcsid(); //@A4A
        switch (str.trim().length())
        {
            case 7:  // CYYMMDD_FORMAT
                dateTime.set (
                    Integer.parseInt(str.substring(0,3)) + 1900,// year
                    Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                    Integer.parseInt(str.substring(5,7)));       // day
                date = dateTime.getTime();
                break;
            case 13: // CYYMMDDHHMMSS_FORMAT
                dateTime.set (
                    Integer.parseInt(str.substring(0,3)) + 1900,// year
                    Integer.parseInt(str.substring(3,5))-1,     // month is zero based
                    Integer.parseInt(str.substring(5,7)),       // day
                    Integer.parseInt(str.substring(7,9)),       // hour
                    Integer.parseInt(str.substring(9,11)),      // minute
                    Integer.parseInt(str.substring(11,13)));    // second
                date = dateTime.getTime();
                break;
            case HHMMSS_FORMAT :
            default :
                break;
        }
        return date;
  }
  
  
  //@B1A    
    /**
    * Returns the string representation for a date in certain format.
    * @param date The date to be parsed.
    * @param format The format.
    * @return The string representation for a date in certain format.
    **/
  private String parseDateString(Date date,int format)
  {
        String dateString = "";
        SimpleDateFormat dateFormat;
        Calendar dateTime=Calendar.getInstance();
        dateTime.setTime(date);
        
        switch (format)
        {
            case CYYMMDD_FORMAT :
                int year=dateTime.get(Calendar.YEAR)-1900;
                String centuryStr="0"; 
                if(year >= 100)
                {
                   centuryStr="1";
                   year=year-100;
                }
                String yearStr="";
                if (year >= 10)
                    yearStr=Integer.toString(year);
                else
                    yearStr="0"+Integer.toString(year);
                int month=dateTime.get(Calendar.MONTH)+1;
                String monthStr="";
                if (month >= 10)
                    monthStr=Integer.toString(month);
                else
                    monthStr="0"+Integer.toString(month);
                int day=dateTime.get(Calendar.DATE);
                String dayStr="";
                if (day >= 10)
                    dayStr=Integer.toString(day);
                else
                    dayStr="0"+Integer.toString(day);
                dateString = centuryStr+yearStr+monthStr+dayStr;
                break;
                
            case HHMMSS_FORMAT :
                int hour=dateTime.get(Calendar.HOUR_OF_DAY);
                String hourStr="";
                if (hour >= 10)
                    hourStr=Integer.toString(hour);
                else
                    hourStr="0"+Integer.toString(hour);
                int minute=dateTime.get(Calendar.MINUTE);
                String minuteStr="";
                if (minute >= 10)
                    minuteStr=Integer.toString(minute);
                else
                    minuteStr="0"+Integer.toString(minute);
                int second=dateTime.get(Calendar.SECOND);
                String secondStr="";
                if (second >= 10)
                    secondStr=Integer.toString(second);
                else
                    secondStr="0"+Integer.toString(second);
                dateString = hourStr+minuteStr+secondStr;
            case CYYMMDDHHMMSS_FORMAT:
            case SYSTEM_TIMESTAMP_FORMAT:
            default :
                break;
        }
        return dateString;
  } 
    
  
  //@B1A
    /**
     * Sets cached changes to AS/400.
     * @param changes The hashtable that caches changes information.
     *
     * @exception AS400Exception                  If the AS/400 system returns an error message.
     * @exception AS400SecurityException          If a security or authority error occurs.
     * @exception CharConversionException         If the character conversion is not supported.
     * @exception ConnectionDroppedException      If the connection is dropped unexpectedly.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception InterruptedException            If this thread is interrupted.
     * @exception IOException                     If an error occurs while communicating with the AS/400.
     * @exception ObjectDoesNotExistException     If the AS/400 object does not exist.
     * @exception UnsupportedEncodingException    If the character encoding is not supported. 
     **/   
  void setChanges(Hashtable changes)
            throws AS400Exception,
                   AS400SecurityException,
                   CharConversionException, 
                   ConnectionDroppedException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
                   UnsupportedEncodingException
    {
        QSYSObjectPathName prgName = new QSYSObjectPathName("QSYS","QWTCHGJB","PGM");
        ProgramParameter[] parmList = new ProgramParameter[5];
        
        AS400Text text;
        AS400Structure structure = new AS400Structure();
        AS400Text[] member = new AS400Text[3];
        int ccsid = as400_.getCcsid();
        member[0] = new AS400Text(10, ccsid, as400_);
        member[1] = new AS400Text(10, ccsid, as400_);
        member[2] = new AS400Text(6, ccsid, as400_);
        structure.setMembers(member);
        String[] qualifiedJobName = { jobName_, userName_, jobNumber_ };
        
        parmList[0] = new ProgramParameter(structure.toBytes(qualifiedJobName));
        text = new AS400Text(16, ccsid, as400_);
        parmList[1] = new ProgramParameter(text.toBytes(internalJobID_));
        text = new AS400Text(8, ccsid, as400_);
        parmList[2] = new ProgramParameter(text.toBytes("JOBC0100"));
        
        parmList[3] = new ProgramParameter(getChangeInformation(changes));
       
        byte[] errorInfo = new byte[32];
        parmList[4] = new ProgramParameter(errorInfo, 0);   
        
        ProgramCall program = new ProgramCall(as400_);
        try
        {
          program.setProgram(prgName.getPath(), parmList);
        }
        catch(PropertyVetoException pve) {} // Ignore
        if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
        {
          Trace.log(Trace.DIAGNOSTIC, "Setting job information for job "+jobNumber_);
        }
        if (program.run() != true)
        {
           AS400Message[] msgList = program.getMessageList();
           if (Trace.isTraceOn() && Trace.isTraceErrorOn())
           {
             Trace.log(Trace.ERROR, "Error changing job information:");
             for (int i=0;i<msgList.length;i++)
             {
               Trace.log(Trace.ERROR, msgList[i].toString());
             }
           }
           throw new AS400Exception(msgList);
        }
    }

    
    //@B1A
    /**
    * Builds the parameter for the API that sets job information
    * changes.
    *
    * @param changes The hashtable where the change information is cached.
    *
    * @return The parameter that includes all of the field information
    * to be changed.
    **/
    private byte[] getChangeInformation(Hashtable changesTable)
      throws UnsupportedEncodingException, CharConversionException
    {  
        Integer numberOfVariableLengthRecords = new Integer(changesTable.size());
        
        int totalLength = 4; // 4 for the "numberofvariablelengthrecords" field
        JOBC0100Format varlenFormat = new JOBC0100Format();
        int varlenFormatLength = 16; // 16 for the length of the base fields (the first five fields)
        
        // Loop through all of the job info fields that need to be changed
        Enumeration changes = changesTable.keys();
        while(changes.hasMoreElements())
        {
          // Add the length of the base JOBC0100 format to the
          // overall format length
          totalLength += varlenFormatLength;
          // There are still 2 more fields that go into the JOBC0100 format.
          // Get the field name
          String fieldToChange = (String)changes.nextElement();
          // Get the length of the field out of the table
          int lengthOfData = ((Integer)lengthTable0100_.get(fieldToChange)).intValue();
          // Add the length of the "data" field
          totalLength += lengthOfData; // "data" field
          // Add the length of the "reserved2" field
          totalLength += 2; // "reserved2" field
        }
        // Create the master byte array for all of the data
        byte[] totalParmsArray = new byte[totalLength];
        int offset = 0;
        
        // Set the number of variable length records first.
        AS400Bin4 bin4 = new AS400Bin4();
        offset += bin4.toBytes(numberOfVariableLengthRecords, totalParmsArray, offset);
        
        // Loop through all of the fields again, setting the data into the
        // master byte array.
        CharConverter reserved2 = new CharConverter(as400_.getCcsid(), as400_);
        
        Record changesRecord = varlenFormat.getNewRecord();          
        changes = changesTable.keys();
        while(changes.hasMoreElements())
        {
          // Get the field name
          String fieldToChange = (String)changes.nextElement();
          // Get the length of the field out of the table
          int lengthOfData = ((Integer)lengthTable0100_.get(fieldToChange)).intValue();
          // Set the data for the base fields
          changesRecord.setField("lengthOfAttributeInformation", new Integer(18 + lengthOfData));
          // Get the key to use for the field out of the table
          Integer fieldKey = (Integer)keyTable0100_.get(fieldToChange);
          changesRecord.setField("key", fieldKey);
          // Almost all of the fields are character fields, except
          // for the few below, which are Bin4 fields.
          int key = fieldKey.intValue();
          String dataType = (key ==  302 || key ==  409 || key == 1204 ||
                             key == 1802 || key == 2002) ? "B" : "C";
          changesRecord.setField("typeOfData", dataType);
          
          // We don't use this field, so we don't set it
          //changesRecord.setField("reserved1", "");
          
          changesRecord.setField("lengthOfData", new Integer(lengthOfData));
          
          // Get the data out of the record and add it to the master
          // byte array
          byte[] changesRecordData = changesRecord.getContents();
          System.arraycopy(changesRecordData, 0, totalParmsArray, offset, changesRecordData.length);
          offset += changesRecordData.length;
          
          // Get the data for the last 2 fields and add it to the master
          // byte array
          if (dataType.equals("C"))
          {
            AS400Text dataField = new AS400Text(lengthOfData, as400_.getCcsid(), as400_);
            offset += dataField.toBytes(changesTable.get(fieldToChange), totalParmsArray, offset);
          }
          else // must be a Bin4
          {
            offset += bin4.toBytes(changesTable.get(fieldToChange), totalParmsArray, offset);
          }
          // Don't forget the last field must be blanks!
          reserved2.stringToByteArray("  ", totalParmsArray, offset);
          offset += 2;
        }
        if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
        {
          Trace.log(Trace.DIAGNOSTIC, "Job info change record byte array", totalParmsArray);
        }
        // Return the master byte array
        return totalParmsArray;
    }      

    
    //@B1A
    // This format represents each variable length record on the Change Job API.
    // It is part of the 4th parameter on the QWTCHGJB API.
    private class JOBC0100Format extends RecordFormat
    {
      JOBC0100Format()
      {
        // The first five fields are the base fields,
        // the last two fields vary in length and are created
        // dynamically in the code.
        addFieldDescription(new BinaryFieldDescription(new AS400Bin4(), "lengthOfAttributeInformation"));
        addFieldDescription(new BinaryFieldDescription(new AS400Bin4(), "key"));
        addFieldDescription(new CharacterFieldDescription(new AS400Text(1, as400_.getCcsid(), as400_), "typeOfData"));
        addFieldDescription(new CharacterFieldDescription(new AS400Text(3, as400_.getCcsid(), as400_), "reserved1"));
        addFieldDescription(new BinaryFieldDescription(new AS400Bin4(), "lengthOfData"));        
        
        // Data field is created dynamically since it size varies depending
        // on the particular field we are changing.
        // Reserved2 field is also created dynamically and should be blank.
      }
    }
        
}
