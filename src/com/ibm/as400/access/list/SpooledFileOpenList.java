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
   * Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects
   * in accordance with, the OSPL0100 format of the underlying API.
   * @see #setFormat
  **/
  public static final String FORMAT_0100 = "OSPL0100";

  /**
   * Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects
   * in accordance with, the OSPL0200 format of the underlying API.
   * @see #setFormat
  **/
  public static final String FORMAT_0200 = "OSPL0200";

  /**
   * Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects
   * in accordance with, the OSPL0300 format of the underlying API. This is the default format.
   * @see #setFormat
  **/
  public static final String FORMAT_0300 = "OSPL0300";

  
  // Sorting constants... in no particular order...

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
  public static final int JOB_SYSTEM = 9;
  
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

  /**
   * Sorting constant used to sort the list of spooled files by printer name.
   * This is only valid for FORMAT_0200.
   * @see #addSortField
  **/
  public static final int PRINTER_NAME = 20;

  /**
   * Sorting constant used to sort the list of spooled files by printer assignment.
   * This is only valid for FORMAT_0200.
   * @see #addSortField
  **/
  public static final int PRINTER_ASSIGNED = 21;

  /**
   * Sorting constant used to sort the list of spooled files by current page number.
   * This is only valid for FORMAT_0100 and FORMAT_0200.
   * @see #addSortField
  **/
  public static final int CURRENT_PAGE = 22;

  /**
   * Sorting constant used to sort the list of spooled files by device type.
   * This is only valid for FORMAT_0100 and FORMAT_0200.
   * @see #addSortField
  **/
  public static final int DEVICE_TYPE = 23;


  
  private String format_ = FORMAT_0300;

  private String jobName_ = "";
  private String jobUser_ = "";
  private String jobNumber_ = "";

  private String[] userFilter_;
  private String[] queueFilter_;
  private String formTypeFilter_;
  private String userDataFilter_;
  private String[] statusFilter_;
  private String[] deviceFilter_;

  private Vector sortKeys_ = new Vector();

  /**
   * Constructs a SpooledFileOpenList object with the given system.
   * By default, this list will generate a list of SpooledFileListItem objects
   * for all spooled files on the system using the default format of {@link #FORMAT_0300 FORMAT_0300}.
   * @param system The system.
  **/
  public SpooledFileOpenList(AS400 system)
  {
    super(system);
  }

  /**
   * Constructs a SpooledFileOpenList object with the given system and format.
   * By default, this list will generate a list of SpooledFileListItem objects
   * for all spooled files on the system.
   * @param system The system.
   * @param format The format.
   * @see #setFormat
  **/
  public SpooledFileOpenList(AS400 system, String format)
  {
    this(system);
    setFormat(format);
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
    int actualSortKeys = numSortKeys;
    // Ignore any sort fields that don't apply for our current format. Their length will be 0.
    for (int i=0; i<numSortKeys; ++i)
    {
      Object[] pair = (Object[])sortKeys_.elementAt(i);
      int field = ((Integer)pair[0]).intValue();
      if (findFieldLength(field) == 0) --actualSortKeys;
    }
    byte[] sortInfo = new byte[4+(12*actualSortKeys)];
    BinaryConverter.intToByteArray(actualSortKeys, sortInfo, 0);
    int offset = 4;
    for (int i=0; i<numSortKeys; ++i)
    {
      Object[] pair = (Object[])sortKeys_.elementAt(i);
      int field = ((Integer)pair[0]).intValue();
      boolean ascend = ((Boolean)pair[1]).booleanValue();
      int startingPosition = findStartingPosition(field);
      int fieldLength = findFieldLength(field);
      if (fieldLength > 0) // Ignore any sort fields that don't apply for our current format.
      {
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
        text10.toBytes(statusFilter_[i], filterInfo, offset); // status
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
    
    parms[7] = new ProgramParameter(conv.stringToByteArray(format_)); // format of the generated list
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
  private final short findDataType(int field)
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
      case STATUS: return format_.equalsIgnoreCase(FORMAT_0300) ? (short)0 : (short)4;
      case DATE_OPENED: return 4;
      case TIME_OPENED: return 4;
      case SCHEDULE: return 4;
      case JOB_SYSTEM: return 4;
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
      case PRINTER_NAME: return 4;
      case PRINTER_ASSIGNED: return 4;
      case CURRENT_PAGE: return 0;
      case DEVICE_TYPE: return 4;
      default:
        return 0;
    }
  }

  // Helper method for sort parameter. Returns 0 for fields that do not 
  // exist on the current format.
  private final int findFieldLength(int field)
  {
    boolean is300 = format_.equalsIgnoreCase(FORMAT_0300);
    switch(field)
    {
      case JOB_NAME: return 10;
      case JOB_USER: return 10;
      case JOB_NUMBER: return 6;
      case NAME: return 10;
      case NUMBER: return 4;
      case STATUS: return is300 ? 4 : 10;
      case DATE_OPENED: return 7;
      case TIME_OPENED: return 6;
      case SCHEDULE: return is300 ? 1 : 0;
      case JOB_SYSTEM: return is300 ? 10 : 8;
      case USER_DATA: return 10;
      case FORM_TYPE: return 10;
      case OUTPUT_QUEUE_NAME: return 10;
      case OUTPUT_QUEUE_LIBRARY: return 10;
      case ASP: return is300 ? 4 : 0;
      case SIZE: return is300 ? 4 : 0;
      case SIZE_MULTIPLIER: return is300 ? 4 : 0;
      case TOTAL_PAGES: return 4;
      case COPIES_LEFT_TO_PRINT: return 4;
      case PRIORITY: return is300 ? 1 : 2;
      case PRINTER_NAME: return is300 ? 10 : 0;
      case PRINTER_ASSIGNED: return 1;
      case CURRENT_PAGE: return is300 ? 0 : 4;
      case DEVICE_TYPE: return is300 ? 0 : 10;
      default:
        return 0;
    }
  }

  // Helper method for sort parameter.
  private final int findStartingPosition(int field)
  {
    boolean is300 = format_.equalsIgnoreCase(FORMAT_0300);
    // This is 1-based for whatever reason.
    switch(field)
    {
      case JOB_NAME: return is300 ? 1 : 11;
      case JOB_USER: return is300 ? 11 : 21;
      case JOB_NUMBER: return is300 ? 21 : 31;
      case NAME: return is300 ? 27 : 1;
      case NUMBER: return 37;
      case STATUS: return is300 ? 41 : 83;
      case DATE_OPENED: return is300 ? 45 : 161;
      case TIME_OPENED: return is300 ? 52 : 168;
      case SCHEDULE: return 58;
      case JOB_SYSTEM: return is300 ? 59 : (format_.equalsIgnoreCase(FORMAT_0100) ? 161 : 192);
      case USER_DATA: return is300 ? 69 : 73;
      case FORM_TYPE: return is300 ? 79 : 93;
      case OUTPUT_QUEUE_NAME: return is300 ? 89 : 53;
      case OUTPUT_QUEUE_LIBRARY: return is300 ? 99 : 63;
      case ASP: return 109;
      case SIZE: return 113;
      case SIZE_MULTIPLIER: return 117;
      case TOTAL_PAGES: return 121;
      case COPIES_LEFT_TO_PRINT: return is300 ? 125 : 49;
      case PRIORITY: return is300 ? 129 : 103;
      case PRINTER_NAME: return 175;
      case PRINTER_ASSIGNED: return 174;
      case CURRENT_PAGE: return 45;
      case DEVICE_TYPE: return 137;
      default:
        return 0;
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
      if (format_.equals(FORMAT_0100) || format_.equals(FORMAT_0200))
      {
        // format OSPL0100 or OSPL0200
        String spooledFileName = conv.byteArrayToString(data, offset, 10).trim();
        String jobName = conv.byteArrayToString(data, offset+10, 10).trim();
        String jobUser = conv.byteArrayToString(data, offset+20, 10).trim();
        String jobNumber = conv.byteArrayToString(data, offset+30, 6);
        int spooledFileNumber = BinaryConverter.byteArrayToInt(data, offset+36);
        int totalPages = BinaryConverter.byteArrayToInt(data, offset+40);
        int currentPage = BinaryConverter.byteArrayToInt(data, offset+44); // Not in 0300 format.
        int copiesLeftToPrint = BinaryConverter.byteArrayToInt(data, offset+48);
        String outputQueueName = conv.byteArrayToString(data, offset+52, 10).trim();
        String outputQueueLib = conv.byteArrayToString(data, offset+62, 10).trim();
        String userData = conv.byteArrayToString(data, offset+72, 10);
        String status = conv.byteArrayToString(data, offset+82, 10).trim(); // This is a BIN(4) in 0300 format.
        String formType = conv.byteArrayToString(data, offset+92, 10).trim();
        String priority = conv.byteArrayToString(data, offset+102, 2).trim(); // This is a CHAR(1) in 0300 format.
        byte[] internalJobID = new byte[16]; // Not in 0300 format.
        System.arraycopy(data, offset+104, internalJobID, 0, 16);
        byte[] internalSplID = new byte[16]; // Not in 0300 format.
        System.arraycopy(data, offset+120, internalSplID, 0, 16);
        String deviceType = conv.byteArrayToString(data, offset+136, 10).trim(); // Not in 0300 format.
        int offsetToExtension = BinaryConverter.byteArrayToInt(data, offset+148);
        String jobSystemName = null;
        if (offsetToExtension > 0)
        {
          // There is an extension.
          int lengthOfExtension = BinaryConverter.byteArrayToInt(data, offset+152);
          jobSystemName = conv.byteArrayToString(data, offset+offsetToExtension, 8).trim(); // This is a CHAR(10) in 0300 format.
        }
        if (format_.equals(FORMAT_0200))
        {
          // format OSPL0200 only
          String dateOpened = conv.byteArrayToString(data, offset+160, 7);
          String timeOpened = conv.byteArrayToString(data, offset+167, 6);
          String printerAssigned = conv.byteArrayToString(data, offset+173, 1); // Not in 0300 format.
          String printerName = conv.byteArrayToString(data, offset+174, 10).trim(); // Not in 0300 format.
        
          sp[i] = new SpooledFileListItem(spooledFileName, jobName, jobUser, jobNumber, spooledFileNumber,
                                          totalPages, currentPage, copiesLeftToPrint, outputQueueName,
                                          outputQueueLib, userData, status, formType, priority,
                                          internalJobID, internalSplID, deviceType, jobSystemName,
                                          dateOpened, timeOpened, printerAssigned, printerName);
        }
        else
        {
          sp[i] = new SpooledFileListItem(spooledFileName, jobName, jobUser, jobNumber, spooledFileNumber,
                                          totalPages, currentPage, copiesLeftToPrint, outputQueueName,
                                          outputQueueLib, userData, status, formType, priority,
                                          internalJobID, internalSplID, deviceType, jobSystemName);
        }
      }
      else
      {
        // format OSPL0300
        String jobName = conv.byteArrayToString(data, offset+0, 10).trim();
        String jobUser = conv.byteArrayToString(data, offset+10, 10).trim();
        String jobNumber = conv.byteArrayToString(data, offset+20, 6);
        String spooledFileName = conv.byteArrayToString(data, offset+26, 10).trim();
        int spooledFileNumber = BinaryConverter.byteArrayToInt(data, offset+36);
        int status = BinaryConverter.byteArrayToInt(data, offset+40); // This is a CHAR(10) in 0200 format.
        String dateOpened = conv.byteArrayToString(data, offset+44, 7);
        String timeOpened = conv.byteArrayToString(data, offset+51, 6);
        String spooledFileSchedule = conv.byteArrayToString(data, offset+57, 1); // Not in 0200 format.
        String jobSystemName = conv.byteArrayToString(data, offset+58, 10).trim(); // This is a CHAR(8) in 0100 format.
        String userData = conv.byteArrayToString(data, offset+68, 10);
        String formType = conv.byteArrayToString(data, offset+78, 10).trim();
        String outputQueueName = conv.byteArrayToString(data, offset+88, 10).trim();
        String outputQueueLib = conv.byteArrayToString(data, offset+98, 10).trim();
        int asp = BinaryConverter.byteArrayToInt(data, offset+108); // Not in 0200 format.
        int size = BinaryConverter.byteArrayToInt(data, offset+112); // Not in 0200 format.
        int sizeMult = BinaryConverter.byteArrayToInt(data, offset+116); // Not in 0200 format.
        int totalPages = BinaryConverter.byteArrayToInt(data, offset+120);
        int copiesLeftToPrint = BinaryConverter.byteArrayToInt(data, offset+124);
        String priority = conv.byteArrayToString(data, offset+128, 1); // This is a CHAR(2) in 0200 format.

        sp[i] = new SpooledFileListItem(jobName, jobUser, jobNumber, spooledFileName, spooledFileNumber,
                                status, dateOpened, timeOpened, spooledFileSchedule, jobSystemName,
                                userData, formType, outputQueueName, outputQueueLib, asp, size, sizeMult,
                                totalPages, copiesLeftToPrint, priority);

      }
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
   * Returns the job name portion of the job information used to determine which spooled files belong in the list.
   * @return The job name, or "" to indicate any job name.
   * @see #setFilterJobInformation
  **/
  public String getFilterJobName()
  {
    return jobName_;
  }

  /**
   * Returns the job number portion of the job information used to determine which spooled files belong in the list.
   * @return The job number, or "" to indicate any job number.
   * @see #setFilterJobInformation
  **/
  public String getFilterJobNumber()
  {
    return jobNumber_;
  }

  /**
   * Returns the user name portion of the job information used to determine which spooled files belong in the list.
   * @return The user name, or "" to indicate any user name.
   * @see #setFilterJobInformation
  **/
  public String getFilterJobUser()
  {
    return jobUser_;
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
  public String[] getFilterStatuses()
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
   * Returns the format currently in use by this open list.
   * Possible values are:
   * <UL>
   * <LI>{@link #FORMAT_0100 FORMAT_0100}
   * <LI>{@link #FORMAT_0200 FORMAT_0200}
   * <LI>{@link #FORMAT_0300 FORMAT_0300}
   * </UL>
   * @return The format. The default format is FORMAT_0300.
  **/
  public String getFormat()
  {
    return format_;
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
   * Sets the qualified job information used to filter the list of spooled files. Specifying null
   * for a parameter resets it to its default value of blank. Blank is only valid for a parameter
   * if all three parameters are specified as blank, or the jobName parameter is specified as "*".
   * @param jobName The job name of the job whose spooled files are to be included in the list. Specify "*"
   * for the current job. If "*" is specified, the jobUser and jobNumber parameters are automatically
   * set to blank.
   * @param jobUser The user name of the job whose spooled files are to be included in the list.
   * @param jobNumber The job number of the job whose spooled files are to be included in the list.
   * @see #getFilterJobName
   * @see #getFilterJobUser
   * @see #getFilterJobNumber
  **/
  public void setFilterJobInformation(String jobName, String jobUser, String jobNumber)
  {
    if (jobName == null) jobName_ = "";
    else jobName_ = jobName.trim();
    if (jobUser == null) jobUser_ = "";
    else jobUser_ = jobUser.trim();
    if (jobNumber == null) jobNumber_ = "";
    else jobNumber_ = jobNumber.trim();

    if (jobName_.equals("*"))
    {
      jobUser_ = "";
      jobNumber_ = "";
    }
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
  public void setFilterStatuses(String[] statuses)
  {
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

  /**
   * Sets the format this list will use on the next call to {@link #open open()}. Any
   * SpooledFileListItems generated by this list will have attributes associated with
   * the specified format. The default format is FORMAT_0300.
   * @param format The format. Possible values are:
   * <UL>
   * <LI>{@link #FORMAT_0100 FORMAT_0100} - This is faster than FORMAT_0200.
   * <LI>{@link #FORMAT_0200 FORMAT_0200} - This is faster than FORMAT_0300.
   * <LI>{@link #FORMAT_0300 FORMAT_0300} - This causes the list to be built slower,
   * but returns the most useful information.
   * </UL>
  **/
  public void setFormat(String format)
  {
    if (format == null) throw new NullPointerException("format");
    format_ = format;
    resetHandle();
  }
}


