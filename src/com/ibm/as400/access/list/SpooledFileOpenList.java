///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SpooledFileOpenList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import com.ibm.as400.access.*;
import java.io.*;
import java.util.*;

/**
 * An {@link com.ibm.as400.access.list.OpenList OpenList} implementation that generates lists of {@link com.ibm.as400.access.list.SpooledFileListItem SpooledFileListItem} objects.
 * <pre>
 *   AS400 system = new AS400("mySystem", "myUserID", "myPassword");
 *   SpooledFileOpenList list = new SpooledFileOpenList(system);
 *   list.setFilterUsers(new String[] { "*CURRENT" }); // Get all of myUserID's spooled files.
 *   list.addSortField(SpooledFileOpenList.JOB_NUMBER, true); // Sort the list by job number in ascending order.
 *   list.open();
 *   Enumeration enum = list.getItems();
 *   while (enum.hasMoreElements())
 *   {
 *     SpooledFileListItem item = (SpooledFileListItem)enum.nextElement();
 *     System.out.println(item.getJobName()+"/"+item.getJobUser()+"/"+item.getJobNumber()+" - "+item.getName()+", "+item.getNumber());
 *   }
 *   list.close();
 * </pre>
**/
public class SpooledFileOpenList extends OpenList
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  /**
   * Constant representing the EBCDIC value for "*ALL      ". Used internally for filtering.
  **/
  private static final byte[] ALL = new byte[] { 0x5C, (byte)0xC1, (byte)0xD3, (byte)0xD3, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };

  /**
   * Sorting constant used to sort the list of spooled files by the job name portion of the job information.
   * @see #addSortField
  **/
  public static final int JOB_NAME = 0;
  
  /**
   * Sorting constant used to sort the list of spooled files by the user name portion of the job information.
   * @see #addSortField
  **/
  public static final int JOB_USER = 1;
  
  /**
   * Sorting constant used to sort the list of spooled files by the job number portion of the job information.
   * @see #addSortField
  **/
  public static final int JOB_NUMBER = 2;
  
  /**
   * Sorting constant used to sort the list of spooled files by spooled file name.
   * @see #addSortField
  **/
  public static final int NAME = 3;
  
  /**
   * Sorting constant used to sort the list of spooled files by spooled file number.
   * @see #addSortField
  **/
  public static final int NUMBER = 4;
  
  /**
   * Sorting constant used to sort the list of spooled files by status.
   * @see #addSortField
  **/
  public static final int STATUS = 5;
  
  /**
   * Sorting constant used to sort the list of spooled files by date.
   * @see #addSortField
  **/
  public static final int DATE_OPENED = 6;
  
  /**
   * Sorting constant used to sort the list of spooled files by time.
   * @see #addSortField
  **/
  public static final int TIME_OPENED = 7;
  
  /**
   * Sorting constant used to sort the list of spooled files by schedule.
   * @see #addSortField
  **/
  public static final int SCHEDULE = 8;
  
  /**
   * Sorting constant used to sort the list of spooled files by system.
   * @see #addSortField
  **/
  public static final int ORIGINATING_SYSTEM = 9;
  
  /**
   * Sorting constant used to sort the list of spooled files by user data.
   * @see #addSortField
  **/
  public static final int USER_DATA = 10;
  
  /**
   * Sorting constant used to sort the list of spooled files by form type.
   * @see #addSortField
  **/
  public static final int FORM_TYPE = 11;
  
  /**
   * Sorting constant used to sort the list of spooled files by output queue name.
   * @see #addSortField
  **/
  public static final int OUTPUT_QUEUE_NAME = 12;
  
  /**
   * Sorting constant used to sort the list of spooled files by output queue library.
   * @see #addSortField
  **/
  public static final int OUTPUT_QUEUE_LIBRARY = 13;
  
  /**
   * Sorting constant used to sort the list of spooled files by auxiliary storage pool (ASP).
   * @see #addSortField
  **/
  public static final int ASP = 14;
  
  /**
   * Sorting constant used to sort the list of spooled files by size.
   * @see #addSortField
  **/
  public static final int SIZE = 15;
  
  /**
   * Sorting constant. Used internally.  When the user specifies SIZE,
   * we automatically assume SIZE_MULTIPLIER as well.
   * @see #addSortField
  **/
  private static final int SIZE_MULTIPLIER = 16;
  
  /**
   * Sorting constant used to sort the list of spooled files by total number of pages.
   * @see #addSortField
  **/
  public static final int TOTAL_PAGES = 17;
  
  /**
   * Sorting constant used to sort the list of spooled files by number of copies left to print.
   * @see #addSortField
  **/
  public static final int COPIES_LEFT_TO_PRINT = 18;
  
  /**
   * Sorting constant used to sort the list of spooled files by priority.
   * @see #addSortField
  **/
  public static final int PRIORITY = 19;

  
  private String jobName_ = "";
  private String jobUser_ = "";
  private String jobNumber_ = "";

  private String[] userFilter_;
  private String[] queueFilter_;
  private String formTypeFilter_;
  private String userDataFilter_;
  private int[] statusFilter_;
  private String[] deviceFilter_;

  private Vector sortKeys_ = new Vector();

  private static final String[] statusFilterStrings_ = new String[]
  {
    "",
    "*READY",
    "*OPEN",
    "*CLOSED",
    "*SAVED",
    "*WRITING",
    "*HELD",
    "*MESSAGE",
    "*PENDING",
    "*PRINTING",
    "*FINISHED",
    "*SENDING",
    "*DEFERRED"
  };

  /**
   * Constructs a SpooledFileOpenList object with the given system. The job name, job user,
   * and job number criteria all default to blank, so that this list will generate a list
   * of SpooledFileListItem objects for all spooled files on the system.
   * @param system The system.
  **/
  public SpooledFileOpenList(AS400 system)
  {
    super(system);
  }

  /**
   * Constructs a SpooledFileOpenList object with the given system and job information.
   * Only spooled files whose job information matches the specified job information will
   * be returned in the list.
   * @param system The system.
   * @param jobName Only spooled files with this job name will be returned. Use "" to mean any job name.
   * @param jobUser Only spooled files with this job user will be returned. Use "" to mean any user name.
   * @param jobNumber Only spooled files with this job number will be returned. Use "" to mean any job number.
  **/
  public SpooledFileOpenList(AS400 system, String jobName, String jobUser, String jobNumber)
  {
    super(system);
    if (jobName == null) throw new NullPointerException("jobName");
    if (jobUser == null) throw new NullPointerException("jobUser");
    if (jobNumber == null) throw new NullPointerException("jobNumber");
    jobName_ = jobName;
    jobUser_ = jobUser;
    jobNumber_ = jobNumber;
  }

  /**
   * Adds a field on which to sort the list when it is built. Use one
   * of the sort constants on this class. By default, no sorting is done.
   * @param field The field used to sort the list.
   * @param ascending true to sort in ascending order on this field (e.g. A-Z or 0-9); false for descending.
  **/
  public void addSortField(int field, boolean ascending)
  {
    // We map the field constant to the appropriate offset in the output record of the API.
    if (field == SIZE) sortKeys_.addElement(new Object[] { new Integer(SIZE_MULTIPLIER), new Boolean(ascending) });
    sortKeys_.addElement(new Object[] { new Integer(field), new Boolean(ascending) });
  }

  /**
   * Calls QGY/QGYOLSPL.
  **/
  protected byte[] callOpenListAPI() throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    // Generate text objects based on system CCSID
    final int ccsid = system_.getCcsid();
    CharConverter conv = new CharConverter(ccsid);
    AS400Text text10 = new AS400Text(10, ccsid, system_);
    AS400Text text6 = new AS400Text(6, ccsid, system_);

    // Setup program parameters
    ProgramParameter[] parms = new ProgramParameter[9];
    parms[0] = new ProgramParameter(1); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(1)); // length of receiver variable
    parms[2] = new ProgramParameter(80); // list information
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(-1)); // number of records to return
    
    // Figure out our sort parameter.
    int numSortKeys = sortKeys_.size();
    byte[] sortInfo = new byte[4+(12*numSortKeys)];
    BinaryConverter.intToByteArray(numSortKeys, sortInfo, 0);
    int offset = 4;
    for (int i=0; i<numSortKeys; ++i)
    {
      Object[] pair = (Object[])sortKeys_.elementAt(i);
      int field = ((Integer)pair[0]).intValue();
      boolean ascend = ((Boolean)pair[1]).booleanValue();
      int startingPosition = findStartingPosition(field);
      int fieldLength = findFieldLength(field);
      short dataType = findDataType(field);
      byte sortOrder = ascend ? (byte)0xF1 : (byte)0xF2; // '1' is ascending, '2' is descending.
      BinaryConverter.intToByteArray(startingPosition, sortInfo, offset);
      offset += 4;
      BinaryConverter.intToByteArray(fieldLength, sortInfo, offset);
      offset += 4;
      BinaryConverter.shortToByteArray(dataType, sortInfo, offset);
      offset += 2;
      sortInfo[offset] = sortOrder;
      offset += 2;
    }
    parms[4] = new ProgramParameter(sortInfo); // sort information
    
    // Figure out our filter parameter.
    int filterInfoLength = 92;
    if (userFilter_ != null && userFilter_.length > 1) filterInfoLength += 12*(userFilter_.length-1);
    if (queueFilter_ != null && queueFilter_.length > 1) filterInfoLength += 20*(queueFilter_.length-1);
    if (statusFilter_ != null && statusFilter_.length > 1) filterInfoLength += 12*(statusFilter_.length-1);
    if (deviceFilter_ != null && deviceFilter_.length > 1) filterInfoLength += 12*(deviceFilter_.length-1);

    byte[] filterInfo = new byte[filterInfoLength];
    offset = 0;
    if (userFilter_ == null || userFilter_.length == 0)
    {
      // Assume *ALL
      BinaryConverter.intToByteArray(1, filterInfo, 0); // number of user names
      offset += 4;
      System.arraycopy(ALL, 0, filterInfo, offset, 10);
      offset += 12;
    }
    else
    {  
      BinaryConverter.intToByteArray(userFilter_.length, filterInfo, offset); // number of user names
      offset += 4;
      for (int i=0; i<userFilter_.length; ++i)
      {
        text10.toBytes(userFilter_[i], filterInfo, offset); // user names
        offset += 12;
      }
    }
    if (queueFilter_ == null || queueFilter_.length == 0)
    {
      BinaryConverter.intToByteArray(1, filterInfo, offset); // number of output queues
      offset += 4;
      System.arraycopy(ALL, 0, filterInfo, offset, 10);
      offset += 10;
      text10.toBytes("", filterInfo, offset); // output queue library name
      offset += 10;
    }
    else
    {
      BinaryConverter.intToByteArray(queueFilter_.length, filterInfo, offset); // number of qualified output queue names
      offset += 4;
      for (int i=0; i<queueFilter_.length; ++i)
      {
        QSYSObjectPathName pn = new QSYSObjectPathName(queueFilter_[i]); // number of output queues
        text10.toBytes(pn.getObjectName(), filterInfo, offset); // output queue name
        offset += 10;
        text10.toBytes(pn.getLibraryName(), filterInfo, offset); // output queue library name
        offset += 10;
      }
    }
    if (formTypeFilter_ == null)
    {
      System.arraycopy(ALL, 0, filterInfo, offset, 10);
    }
    else
    {
      text10.toBytes(formTypeFilter_, filterInfo, offset); // form type
    }
    offset += 10;
    if (userDataFilter_ == null)
    {
      System.arraycopy(ALL, 0, filterInfo, offset, 10);
    }
    else
    {
      text10.toBytes(userDataFilter_, filterInfo, offset); // user-specified data
    }
    offset += 10;
    if (statusFilter_ == null || statusFilter_.length == 0)
    {
      BinaryConverter.intToByteArray(1, filterInfo, offset); // number of statuses
      offset += 4;
      System.arraycopy(ALL, 0, filterInfo, offset, 10);
      offset += 12;
    }
    else
    {
      BinaryConverter.intToByteArray(statusFilter_.length, filterInfo, offset); // number of statuses
      offset += 4;
      for (int i=0; i<statusFilter_.length; ++i)
      {
        text10.toBytes(statusFilterStrings_[statusFilter_[i]], filterInfo, offset); // status
        offset += 12;
      }
    }
    if (deviceFilter_ == null || deviceFilter_.length == 0)
    {
      BinaryConverter.intToByteArray(1, filterInfo, offset); // number of devices
      offset += 4;
      System.arraycopy(ALL, 0, filterInfo, offset, 10);
      offset += 12;
    }
    else
    {
      BinaryConverter.intToByteArray(deviceFilter_.length, filterInfo, offset); // number of devices
      offset += 4;
      for (int i=0; i<deviceFilter_.length; ++i)
      {
        text10.toBytes(deviceFilter_[i], filterInfo, offset); // device
        offset += 12;
      }
    }
    parms[5] = new ProgramParameter(filterInfo); // filter information
    
    // Figure out our job parameter.
    byte[] nameUserNumber = new byte[26];
    text10.toBytes(jobName_, nameUserNumber, 0);
    text10.toBytes(jobUser_, nameUserNumber, 10);
    text6.toBytes(jobNumber_, nameUserNumber, 20);
    parms[6] = new ProgramParameter(nameUserNumber); // qualified job name
    
    parms[7] = new ProgramParameter(conv.stringToByteArray("OSPL0300")); // format of the generated list
    parms[8] = EMPTY_ERROR_CODE_PARM;
    // We default to OSPF0100 to be compatible with pre-V5R2 systems.
//    parms[9] = new ProgramParameter(); // format of filter information

    // Call the program
    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLSPL.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    // List information returned
    byte[] listInformation = parms[2].getOutputData();
    return listInformation;
  }

  /**
   * Clears all sorting information for this list. No sorting will be done when the list
   * is generated unless {@link #addSortField addSortField()} is called to specify which
   * fields to sort on.
  **/
  public void clearSortFields()
  {
    sortKeys_.removeAllElements();
  }

  // Helper method for sort parameter.
  private static final short findDataType(int field)
  {
    // 4 is character.
    // 0 is signed binary.
    switch(field)
    {
      case JOB_NAME: return 4;
      case JOB_USER: return 4;
      case JOB_NUMBER: return 4;
      case NAME: return 4;
      case NUMBER: return 0;
      case STATUS: return 0;
      case DATE_OPENED: return 4;
      case TIME_OPENED: return 4;
      case SCHEDULE: return 4;
      case ORIGINATING_SYSTEM: return 4;
      case USER_DATA: return 4;
      case FORM_TYPE: return 4;
      case OUTPUT_QUEUE_NAME: return 4;
      case OUTPUT_QUEUE_LIBRARY: return 4;
      case ASP: return 0;
      case SIZE: return 0;
      case SIZE_MULTIPLIER: return 0;
      case TOTAL_PAGES: return 0;
      case COPIES_LEFT_TO_PRINT: return 0;
      case PRIORITY: return 4;
      default:
        throw new RuntimeException();
    }
  }

  // Helper method for sort parameter.
  private static final int findFieldLength(int field)
  {
    switch(field)
    {
      case JOB_NAME: return 10;
      case JOB_USER: return 10;
      case JOB_NUMBER: return 6;
      case NAME: return 10;
      case NUMBER: return 4;
      case STATUS: return 4;
      case DATE_OPENED: return 7;
      case TIME_OPENED: return 6;
      case SCHEDULE: return 1;
      case ORIGINATING_SYSTEM: return 10;
      case USER_DATA: return 10;
      case FORM_TYPE: return 10;
      case OUTPUT_QUEUE_NAME: return 10;
      case OUTPUT_QUEUE_LIBRARY: return 10;
      case ASP: return 4;
      case SIZE: return 4;
      case SIZE_MULTIPLIER: return 4;
      case TOTAL_PAGES: return 4;
      case COPIES_LEFT_TO_PRINT: return 4;
      case PRIORITY: return 1;
      default:
        throw new RuntimeException();
    }
  }

  // Helper method for sort parameter.
  private static final int findStartingPosition(int field)
  {
    // This is 1-based for whatever reason.
    switch(field)
    {
      case JOB_NAME: return 1;
      case JOB_USER: return 11;
      case JOB_NUMBER: return 21;
      case NAME: return 27;
      case NUMBER: return 37;
      case STATUS: return 41;
      case DATE_OPENED: return 45;
      case TIME_OPENED: return 52;
      case SCHEDULE: return 58;
      case ORIGINATING_SYSTEM: return 59;
      case USER_DATA: return 69;
      case FORM_TYPE: return 79;
      case OUTPUT_QUEUE_NAME: return 89;
      case OUTPUT_QUEUE_LIBRARY: return 99;
      case ASP: return 109;
      case SIZE: return 113;
      case SIZE_MULTIPLIER: return 117;
      case TOTAL_PAGES: return 121;
      case COPIES_LEFT_TO_PRINT: return 125;
      case PRIORITY: return 129;
      default:
        throw new RuntimeException();
    }
  }

  /**
   * Formats the data from QGY/QGYOLSPL using format OSPL0300.
  **/
  protected Object[] formatOutputData(byte[] data, int recordsReturned, int recordLength) throws AS400Exception, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    int ccsid = system_.getCcsid();
    CharConverter conv = new CharConverter(ccsid);

    SpooledFileListItem[] sp = new SpooledFileListItem[recordsReturned];
    int offset = 0;
    for (int i=0; i<recordsReturned; ++i)
    {
      // format OSPL0200
/*      String spooledFileName = conv.byteArrayToString(data, 0, 10).trim();
      String jobName = conv.byteArrayToString(data, 10, 10).trim();
      String jobUser = conv.byteArrayToString(data, 20, 10).trim();
      String jobNumber = conv.byteArrayToString(data, 30, 6);
      int spooledFileNumber = BinaryConverter.byteArrayToInt(data, 36);
      int totalPages = BinaryConverter.byteArrayToInt(data, 40);
      int currentPage = BinaryConverter.byteArrayToInt(data, 44);
      int copiesLeftToPrint = BinaryConverter.byteArrayToInt(data, 48);
      String outputQueueName = conv.byteArrayToString(data, 52, 10).trim();
      String outputQueueLib = conv.byteArrayToString(data, 62, 10).trim();
      String userData = conv.byteArrayToString(data, 72, 10);
      String status = conv.byteArrayToString(data, 82, 10).trim();
      String formType = conv.byteArrayToString(data, 92, 10).trim();
      String priority = conv.byteArrayToString(data, 102, 2);
      byte[] internalJobID = new byte[16];
      System.arraycopy(data, 104, internalJobID, 0, 16);
      byte[] internalSplID = new byte[16];
      System.arraycopy(data, 120, internalSplID, 0, 16);
      String deviceType = conv.byteArrayToString(data, 136, 10).trim();
      String dateOpened = conv.byteArrayToString(data, 160, 7);
      String timeOpened = conv.byteArrayToString(data, 167, 6);
      String printerAssigned = conv.byteArrayToString(data, 173, 1);
      String printerName = conv.byteArrayToString(data, 174, 10).trim();
*/
      // format OSPL0300
      String jobName = conv.byteArrayToString(data, offset+0, 10).trim();
      String jobUser = conv.byteArrayToString(data, offset+10, 10).trim();
      String jobNumber = conv.byteArrayToString(data, offset+20, 6);
      String spooledFileName = conv.byteArrayToString(data, offset+26, 10).trim();
      int spooledFileNumber = BinaryConverter.byteArrayToInt(data, offset+36);
      int status = BinaryConverter.byteArrayToInt(data, offset+40);
      String dateOpened = conv.byteArrayToString(data, offset+44, 7);
      String timeOpened = conv.byteArrayToString(data, offset+51, 6);
      String spooledFileSchedule = conv.byteArrayToString(data, offset+57, 1);
      String spooledFileSystem = conv.byteArrayToString(data, offset+58, 10).trim();
      String userData = conv.byteArrayToString(data, offset+68, 10);
      String formType = conv.byteArrayToString(data, offset+78, 10).trim();
      String outputQueueName = conv.byteArrayToString(data, offset+88, 10).trim();
      String outputQueueLib = conv.byteArrayToString(data, offset+98, 10).trim();
      int asp = BinaryConverter.byteArrayToInt(data, offset+108);
      int size = BinaryConverter.byteArrayToInt(data, offset+112);
      int sizeMult = BinaryConverter.byteArrayToInt(data, offset+116);
      int totalPages = BinaryConverter.byteArrayToInt(data, offset+120);
      int copiesLeftToPrint = BinaryConverter.byteArrayToInt(data, offset+124);
      String priority = conv.byteArrayToString(data, offset+128, 1);

      sp[i] = new SpooledFileListItem(jobName, jobUser, jobNumber, spooledFileName, spooledFileNumber,
                              status, dateOpened, timeOpened, spooledFileSchedule, spooledFileSystem,
                              userData, formType, outputQueueName, outputQueueLib, asp, size, sizeMult,
                              totalPages, copiesLeftToPrint, priority);

      offset += recordLength;
    }
    return sp;
  }

  /**
   * Returns 100 plus (136*number).
  **/
  protected int getBestGuessReceiverSize(int number)
  {
    return 100+(136*number);
  }

  /**
   * Returns the printer device names being used to filter the list of spooled files.
   * @return The array of printer device names, or null if the list is not being filtered
   * by device.
  **/
  public String[] getFilterDevices()
  {
    return deviceFilter_;
  }

  /**
   * Returns the form type being used to filter the list of spooled files.
   * @return The form type, or null if the list is not being filtered by form type.
  **/
  public String getFilterFormType()
  {
    return formTypeFilter_;
  }

  /**
   * Returns the output queue names being used to filter the list of spooled files.
   * @return The array of fully-qualified integrated file system path names of output queues, or
   * null if the list is not being filtered by output queue.
  **/
  public String[] getFilterOutputQueues()
  {
    return queueFilter_;
  }

  /**
   * Returns the statuses being used to filter the list of spooled files.
   * @return The array of statuses, or null if the list is not being filtered by status.
  **/
  public int[] getFilterStatuses()
  {
    return statusFilter_;
  }

  /**
   * Returns the user data being used to filter the list of spooled files.
   * @return The user data, or null if the list is not being filtered by user data.
  **/
  public String getFilterUserData()
  {
    return userDataFilter_;
  }

  /**
   * Returns the user names being used to filter the list of spooled files.
   * @return The array of user names, or null if the list is not being filtered by user name.
  **/
  public String[] getFilterUsers()
  {
    return userFilter_;
  }

  /**
   * Returns the job name portion of the job information used to determine which spooled files belong in the list.
   * @return The job name, or "" to indicate any job name.
  **/
  public String getJobName()
  {
    return jobName_;
  }

  /**
   * Returns the job number portion of the job information used to determine which spooled files belong in the list.
   * @return The job number, or "" to indicate any job number.
  **/
  public String getJobNumber()
  {
    return jobNumber_;
  }

  /**
   * Returns the user name portion of the job information used to determine which spooled files belong in the list.
   * @return The user name, or "" to indicate any user name.
  **/
  public String getJobUser()
  {
    return jobUser_;
  }

  /**
   * Sets the printer device names used to filter the list of spooled files.
   * param devices The array of printer device names. Only spooled files that belong to the specified
   * printer devices are returned in the list. Specify null to clear the status filter, so that spooled files
   * in the list are no longer filtered based on device.
  **/
  public void setFilterDevices(String[] devices)
  {
    deviceFilter_ = devices;
    resetHandle();
  }

  /**
   * Sets the form type used to filter the list of spooled files.
   * @param formType The form type. Only spooled files whose form type matches the specified form type
   * are returned in the list. Specify "*STD" for the standard form type. Specify null to clear the form
   * type filter, so that spooled files in the list are no longer filtered based on form type.
  **/
  public void setFilterFormType(String formType)
  {
    formTypeFilter_ = formType;
    resetHandle();
  }

  /**
   * Sets the output queues used to filter the list of spooled files.
   * @param queues An array of fully-qualified integrated file system path names of output queues. Only spooled
   * files that reside in the specified output queues are returned in the list. Specify null to clear
   * the output queue filter, so that the spooled files in the list are no longer filtered based on
   * output queue.
  **/
  public void setFilterOutputQueues(String[] queues)
  {
    queueFilter_ = queues;
    resetHandle();
  }

  /**
   * Sets the statuses used to filter the list of spooled files.
   * Possible status values are:
   * <UL>
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_CLOSED SpooledFileListItem.STATUS_CLOSED} - "*CLOSED"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_DEFERRED SpooledFileListItem.STATUS_DEFERRED} - "*DEFERRED"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_SENDING SpooledFileListItem.STATUS_SENDING} - "*SENDING"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_FINISHED SpooledFileListItem.STATUS_FINISHED} - "*FINISHED"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_HELD SpooledFileListItem.STATUS_HELD} - "*HELD"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_MESSAGE_WAIT SpooledFileListItem.STATUS_MESSAGE_WAIT} - "*MESSAGE"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_OPEN SpooledFileListItem.STATUS_OPEN} - "*OPEN"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_PENDING SpooledFileListItem.STATUS_PENDING} - "*PENDING"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_PRINTING SpooledFileListItem.STATUS_PRINTING} - "*PRINTING"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_READY SpooledFileListItem.STATUS_READY} - "*READY"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_SAVED SpooledFileListItem.STATUS_SAVED} - "*SAVED"
   * <LI>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_WRITING SpooledFileListItem.STATUS_WRITING} - "*WRITING"
   * </UL> 
   * @param statuses The array of statuses. Only spooled files whose status matches one of the specified
   * statuses are returned in the list. Specify null to clear the status filter, so that spooled files
   * in the list are no longer filtered based on status.
  **/
  public void setFilterStatuses(int[] statuses)
  {
    if (statuses != null && statuses.length > 0)
    {
      for (int i=0; i<statuses.length; ++i)
      {
        if (statuses[i] < 1 || statuses[i] > 12) throw new ExtendedIllegalArgumentException("statuses", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }
    }
    statusFilter_ = statuses;
    resetHandle();
  }

  /**
   * Sets the user data used to filter the list of spooled files.
   * @param userData The user data. Only spooled files whose user data exactly matches the specified
   * user data are returned in the list. Specify null to clear the user data filter, so that spooled files
   * in the list are no longer filtered based on user data.
  **/
  public void setFilterUserData(String userData)
  {
    userDataFilter_ = userData;
    resetHandle();
  }

  /**
   * Sets the user names used to filter the list of spooled files.
   * @param users An array of user names. Only spooled files that were created by the specified users
   * are returned in the list. Specify "*CURRENT" as one of the users to mean the current user profile.
   * Specify null to clear the user filter, so that the spooled files in the list are no longer
   * filtered based on user name.
  **/
  public void setFilterUsers(String[] users)
  {
    userFilter_ = users;
    resetHandle();
  }
}


