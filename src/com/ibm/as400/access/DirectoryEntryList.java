///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DirectoryEntryList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The DirectoryEntryList class is used to retrieve a list of system
 * distribution directory entries.
 * <p>
 * By default, all entries are returned as if by calling addSelection(DirectoryEntryList.USER_PROFILE, "*").
 *
 * <pre>
 * AS400 system = new AS400();
 * DirectoryEntryList list = new DirectoryEntryList(system);
 * // Retrieves all of the entries that map to user profiles that begin with the letter 'B'.
 * list.addSelection(DirectoryEntryList.USER_PROFILE, "B*");
 * DirectoryEntry[] entries = list.getEntries();
 * </pre>
 *
 * Here is an example of using selection IDs:
 * <pre>
 * AS400 system = new AS400();
 * DirectoryEntryList list = new DirectoryEntryList(system);
 * list.addSelection(DirectoryEntryList.LAST_NAME, "SMITH");
 * list.addSelection(DirectoryEntryList.FIRST_NAME, "C*");
 * list.setKey(DirectoryEntryList.FIRST_NAME); // Set the primary sort to be the first name field.
 * DirectoryEntry[] entries = list.getEntries();
 * </pre>
 *
 * @see com.ibm.as400.access.DirectoryEntry
 * @see com.ibm.as400.access.User
**/
public class DirectoryEntryList
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
  
  private static final ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);

  private AS400 system_;
  private String keyValue_ = null;

  private final Hashtable selectionValues_ = new Hashtable();

  /**
   * Constant used to filter the list of directory entries by mailing address.
   * @see com.ibm.as400.access.DirectoryEntry#getMailingAddress1
  **/
  public static final String ADDRESS1 = "ADDR1";
  
  /**
   * Constant used to filter the list of directory entries by mailing address.
   * @see com.ibm.as400.access.DirectoryEntry#getMailingAddress2
  **/
  public static final String ADDRESS2 = "ADDR2";
  
  /**
   * Constant used to filter the list of directory entries by mailing address.
   * @see com.ibm.as400.access.DirectoryEntry#getMailingAddress3
  **/
  public static final String ADDRESS3 = "ADDR3";
  
  /**
   * Constant used to filter the list of directory entries by mailing address.
   * @see com.ibm.as400.access.DirectoryEntry#getMailingAddress4
  **/
  public static final String ADDRESS4 = "ADDR4";
  
  /**
   * Constant used to filter the list of directory entries by building.
   * @see com.ibm.as400.access.DirectoryEntry#getBuilding
  **/
  public static final String BUILDING = "BLDG";
  
  /**
   * Constant used to filter the list of directory entries by company.
   * @see com.ibm.as400.access.DirectoryEntry#getCompany
  **/
  public static final String COMPANY = "CMPNY";
  
  /**
   * Constant used to filter the list of directory entries by department.
   * @see com.ibm.as400.access.DirectoryEntry#getDepartment
  **/
  public static final String DEPARTMENT = "DEPT";
  
  /**
   * Constant used to filter the list of directory entries by fax number.
   * @see com.ibm.as400.access.DirectoryEntry#getFaxNumber
  **/
  public static final String FAX = "FAXTELNBR";
  
  /**
   * Constant used to filter the list of directory entries by first name.
   * @see com.ibm.as400.access.DirectoryEntry#getFirstName
  **/
  public static final String FIRST_NAME = "FSTNAM";
  
  /**
   * Constant used to filter the list of directory entries by first or preferred name.
   * This constant is only used as input to filter the list. Its value cannot be
   * retrieved from a DirectoryEntry object.
  **/
  public static final String FIRST_OR_PREFERRED_NAME = "FSTPREFNAM";
  
  /**
   * Constant used to filter the list of directory entries by full name.
   * @see com.ibm.as400.access.DirectoryEntry#getFullName
  **/
  public static final String FULL_NAME = "FULNAM";
  
  /**
   * Constant used to filter the list of directory entries by job title.
   * @see com.ibm.as400.access.DirectoryEntry#getJobTitle
  **/
  public static final String JOB_TITLE = "TITLE";
  
  /**
   * Constant used to filter the list of directory entries by last name.
   * @see com.ibm.as400.access.DirectoryEntry#getLastName
  **/
  public static final String LAST_NAME = "LSTNAM";
  
  /**
   * Constant used to filter the list of directory entries by location.
   * @see com.ibm.as400.access.DirectoryEntry#getLocation
  **/
  public static final String LOCATION = "LOC";
  
  /**
   * Constant used to filter the list of directory entries by middle name.
   * @see com.ibm.as400.access.DirectoryEntry#getMiddleName
  **/
  public static final String MIDDLE_NAME = "MIDNAM";
  
  /**
   * Constant used to filter the list of directory entries by network user ID.
   * @see com.ibm.as400.access.DirectoryEntry#getNetworkUserID
  **/
  public static final String NETWORK_USER_ID = "NETUSRID";
  
  /**
   * Constant used to filter the list of directory entries by office.
   * @see com.ibm.as400.access.DirectoryEntry#getOffice
  **/
  public static final String OFFICE = "OFC";
  
  /**
   * Constant used to filter the list of directory entries by preferred name.
   * @see com.ibm.as400.access.DirectoryEntry#getPreferredName
  **/
  public static final String PREFERRED_NAME = "PREFNAM";
  
  /**
   * Constant used to filter the list of directory entries by system group.
   * @see com.ibm.as400.access.DirectoryEntry#getSystemGroup
  **/
  public static final String SYSTEM_GROUP = "SYSGRP";
  
  /**
   * Constant used to filter the list of directory entries by system name.
   * @see com.ibm.as400.access.DirectoryEntry#getSystemName
  **/
  public static final String SYSTEM_NAME = "SYSNAME";
  
  /**
   * Constant used to filter the list of directory entries by telephone number.
   * @see com.ibm.as400.access.DirectoryEntry#getTelephoneNumber1
  **/
  public static final String TELEPHONE1 = "TELNBR1";
  
  /**
   * Constant used to filter the list of directory entries by telephone number.
   * @see com.ibm.as400.access.DirectoryEntry#getTelephoneNumber2
  **/
  public static final String TELEPHONE2 = "TELNBR2";
  
  /**
   * Constant used to filter the list of directory entries by text description.
   * @see com.ibm.as400.access.DirectoryEntry#getText
  **/
  public static final String TEXT = "TEXT";
  
  /**
   * Constant used to filter the list of directory entries by directory entry user address.
   * @see com.ibm.as400.access.DirectoryEntry#getUserAddress
  **/
  public static final String USER_ADDRESS = "USRADDR";
  
  /**
   * Constant used to filter the list of directory entries by user description.
   * @see com.ibm.as400.access.DirectoryEntry#getUserDescription
  **/
  public static final String USER_DESCRIPTION = "USRD";
  
  /**
   * Constant used to filter the list of directory entries by directory entry user ID.
   * @see com.ibm.as400.access.DirectoryEntry#getUserID
  **/
  public static final String USER_ID = "USRID";
  
  /**
   * Constant used to filter the list of directory entries by user profile name.
   * @see com.ibm.as400.access.DirectoryEntry#getUserProfile
  **/
  public static final String USER_PROFILE = "USER";


  /**
   * Constructs a DirectoryEntryList.
   * By default, no selection IDs have been added. You must add at least one selection ID in order
   * to retrieve any directory entries. The most common way to retrieve a list of all entries on
   * the system is to add a selection for USER_PROFILE with a value of "*".
   * @param system The system.
  **/
  public DirectoryEntryList(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");
    system_ = system;
  }

  /**
   * Adds a value used to filter the list of directory entries returned by {@link #getEntries getEntries()}.
   * See the list of constants in this class for some possible selection IDs. Other selection IDs
   * can be used, if they are allowed by the QOKSCHD system API.
   * If the specified <i>selectionID</i> has already been added, then the specified <i>value</i> will override
   * the previously added value.
   * @param selectionID The selectionID, e.g. {@link #LAST_NAME LAST_NAME} or {@link #USER_PROFILE USER_PROFILE}.
   * @param value The value used as the filter for the specified selectionID, e.g. "SMITH" or "A*".
   *        The asterisk '*' is allowed as a wildcard character.
   * @see #clearSelection
   * @see #setKey
  **/
  public void addSelection(String selectionID, String value)
  {
    if (selectionID == null) throw new NullPointerException("selectionID");
    if (value == null) throw new NullPointerException("value");
    if (selectionValues_.isEmpty())
    {
      keyValue_ = selectionID;
    }
    selectionValues_.put(selectionID, value);
  }

  /**
   * Removes all selection IDs and values that were previously added by {@link #addSelection addSelection()}.
   * This also clears the key.
   * @see #addSelection
   * @see #setKey
  **/
  public void clearSelection()
  {
    selectionValues_.clear();
    keyValue_ = null;
  }

  /**
   * Retrieves the directory entry information from the server.
   * This method internally calls the OS/400 QOKSCHD system API.
   * @return An array of directory entries.
  **/
  public DirectoryEntry[] getEntries()
  throws AS400Exception,
         AS400SecurityException,
         ErrorCompletingRequestException,
         InterruptedException,
         IOException,
         ObjectDoesNotExistException
  {
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    if (selectionValues_.isEmpty()) addSelection(USER_PROFILE, "*"); // The default.

    ProgramParameter[] parms = new ProgramParameter[9];
    int bufferSize = 33+636*20;
    final int ccsid = system_.getCcsid();
    final ConvTable conv = ConvTable.getTable(ccsid, null);
    final AS400Text text10 = new AS400Text(10, ccsid);
    parms[0] = new ProgramParameter(bufferSize); // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(bufferSize)); // length of receiver variable
    parms[2] = new ProgramParameter(conv.stringToByteArray("SRCV0100")); // format name of receiver variable
    parms[3] = new ProgramParameter(text10.toBytes("*SEARCH")); // function
    parms[4] = new ProgramParameter(new byte[] { (byte)0xF0 }); // '0' to not keep temporary resources, otherwise we'd have to call it with *CLEANUP
    
    // First, calculate the amount of selection bytes.
    int numValues = selectionValues_.size();
    Enumeration enum = selectionValues_.keys();
    int totalSelectionLength = 0;
    while (enum.hasMoreElements())
    {
      String selectionID = (String)enum.nextElement();
      String val = (String)selectionValues_.get(selectionID);
      totalSelectionLength += val.length();
    }
    byte[] request = new byte[130+(numValues*28)+totalSelectionLength];

    // CCSID of data input = 0 (default to job CCSID)
    // character set of data input (ignored unless CCSID is -1)
    // code page of data input (ignored unless CCSID is -1)
    conv.stringToByteArray("*   ", request, 12, 4); // wildcard character (blank means no wildcard searches)
    request[16] = (byte)0xF1; // convert receiver data ('1' = convert it to CCSID specified)
    request[17] = (byte)0xF0; // data to search ('0' = only search local data, not shadowed data)
    request[18] = (byte)0xF1; // run verify ('1' = verify the input parameters are valid)
    request[19] = (byte)0xF0; // continuation handle ('0' for the first search)
    conv.stringToByteArray("                ", request, 20, 16); // resource handle (blank for the first search)
    conv.stringToByteArray("SREQ0101", request, 36, 8); // format name of the search request array
    BinaryConverter.intToByteArray(130, request, 44); // offset to search request array
    BinaryConverter.intToByteArray(numValues, request, 48); // number of elements in search request array
    conv.stringToByteArray("SREQ0103", request, 52, 8); // format name of array of fields to return
    BinaryConverter.intToByteArray(100, request, 60); // offset to array of fields to return
    BinaryConverter.intToByteArray(3, request, 64); // number of elements in fields to return array
    conv.stringToByteArray("SRCV0101", request, 68, 8); // format name of array of users to return
    BinaryConverter.intToByteArray(0xFFFF, request, 76); // number of elements in array of users. Note: Fix this so we can chain.
    conv.stringToByteArray("SRCV0112", request, 80, 8); // format name of array of fields for each user
    conv.stringToByteArray("        ", request, 88, 8); // format name of the order of field names to return (blanks = order is not returned)
    request[96] = (byte)0xF0; // return fields in order specified ('0' = return fields in the predefined order)
    conv.stringToByteArray("   ", request, 97, 3); // reserved
    // Array of fields to return:
    text10.toBytes("*SYSDIR", request, 100); // special value of fields to be returned (SREQ0103)
    text10.toBytes("*SMTP", request, 110);
    text10.toBytes("*ORNAME", request, 120);
    // Search request array:
    int offset = 130;
    
    // Do the key first, then the rest of the selection criteria.
    if (keyValue_ != null)
    {
      String selectionID = keyValue_;
      String val = (String)selectionValues_.remove(selectionID);
      int len = val.length();
      BinaryConverter.intToByteArray(28+len, request, offset);
      request[offset+4] = (byte)0xF1;
      text10.toBytes(selectionID, request, offset+5);
      conv.stringToByteArray("*IBM   ", request, offset+15, 7);
      request[offset+22] = 0x40;
      request[offset+23] = 0x40;
      BinaryConverter.intToByteArray(len, request, offset+24);
      AS400Text textConv = new AS400Text(len, ccsid);
      textConv.toBytes(val, request, offset+28);
      offset += 28+len;
    }

    // Do the rest of the selection criteria.
    enum = selectionValues_.keys();
    while (enum.hasMoreElements())
    {
      String selectionID = (String)enum.nextElement();
      String val = (String)selectionValues_.get(selectionID);
      int len = val.length();
      BinaryConverter.intToByteArray(28+len, request, offset); // SREQ0101 - length of entry
      request[offset+4] = (byte)0xF1; // SREQ0101 - compare value ('1' means 'equal')
      text10.toBytes(selectionID, request, offset+5); // SREQ0101 - field name
      conv.stringToByteArray("*IBM   ", request, offset+15, 7); // SREQ0101 - product ID
      request[offset+22] = 0x40; // SREQ0101 - case of data input (blank means case insensitive search except for SMTPUSRID and SMTPRTE fields)
      request[offset+23] = 0x40; // reserved
      BinaryConverter.intToByteArray(len, request, offset+24); // SREQ0101 - length of value
      AS400Text textConv = new AS400Text(len, ccsid);
      textConv.toBytes(val, request, offset+28); // SREQ0101 - value to match (blank means ignore)
      offset += 28+len;
    }

    parms[5] = new ProgramParameter(request); // request variable
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(request.length)); // length of request variable
    parms[7] = new ProgramParameter(conv.stringToByteArray("SREQ0100")); // format name of request variable
    parms[8] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QOKSCHD.PGM", parms);
    if (!pc.run())
    {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] data = parms[0].getOutputData();
    int numReturned = BinaryConverter.byteArrayToInt(data, 0);
    while ((bufferSize - numReturned) <= 636)
    {
      bufferSize = bufferSize*2;
      try
      {
        parms[0].setOutputDataLength(bufferSize);
        parms[1].setInputData(BinaryConverter.intToByteArray(bufferSize));
      }
      catch(PropertyVetoException pve) {}

      if (!pc.run())
      {
        throw new AS400Exception(pc.getMessageList());
      }
      data = parms[0].getOutputData();
      numReturned = BinaryConverter.byteArrayToInt(data, 0);
    }
    int orderArrayOffset = BinaryConverter.byteArrayToInt(data, 4);
    int userArrayOffset = BinaryConverter.byteArrayToInt(data, 8);
    int numEntries = BinaryConverter.byteArrayToInt(data, 12);
    byte continuationHandle = data[16];
    String resourceHandle = conv.byteArrayToString(data, 17, 16);

    DirectoryEntry[] entries = new DirectoryEntry[numEntries];
    offset = userArrayOffset;
    for (int i=0; i<numEntries; ++i)
    {
      // SRCV0101
      int dataLength = BinaryConverter.byteArrayToInt(data, offset);
      int numFields = BinaryConverter.byteArrayToInt(data, offset+4);
      offset += 8;
      // Each entry is in the format SRCV0112
      String[] fieldValues = new String[numFields];
      for (int j=0; j<numFields; ++j)
      {
        int charset = BinaryConverter.byteArrayToInt(data, offset);
        int codepage = BinaryConverter.byteArrayToInt(data, offset+4);
        int fieldLength = BinaryConverter.byteArrayToInt(data, offset+8);
        fieldValues[j] = conv.byteArrayToString(data, offset+12, fieldLength);
        offset += 12+fieldLength;
      }
      entries[i] = new DirectoryEntry(system_, fieldValues);
    }
    return entries;
  }


  /**
   * Returns the primary selection ID used when searching the system distribution directory.
   * If the key has not been added as a selection ID by {@link #addSelection addSelection()}
   * then it has no effect on how the list is sorted when returned by {@link #getEntries getEntries()}.
   * <p>
   * If no key has been set, then the first entry added via addSelection() is used.
   * @see #setKey
  **/
  public String getKey()
  {
    return keyValue_;
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
   * Sets the primary selection ID used when searching the system distribution directory.
   * If the key has not been added as a selection ID by {@link #addSelection addSelection()}
   * then it has no effect on how the list is sorted when returned by {@link #getEntries getEntries()}.
   * <p>
   * If no key has been set, then the first entry added via addSelection() is used.
   * @see #addSelection
   * @see #getKey
  **/
  public void setKey(String selectionID)
  {
    keyValue_ = selectionID;
  }


  /**
   * Sets the system.
   * @param system The new system.
   * @see #getSystem
  **/
  public void setSystem(AS400 system)
  {
    system_ = system;
  }
}
