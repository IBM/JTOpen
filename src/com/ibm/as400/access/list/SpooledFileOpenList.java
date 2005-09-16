///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SpooledFileOpenList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access.list;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.CharConverter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.Trace;

/**
 An {@link com.ibm.as400.access.list.OpenList OpenList} implementation that generates lists of {@link com.ibm.as400.access.list.SpooledFileListItem SpooledFileListItem} objects.
 <pre>
    AS400 system = new AS400("mySystem", "myUserID", "myPassword");
    SpooledFileOpenList list = new SpooledFileOpenList(system);
    // Get all of myUserID's spooled files.
    list.setFilterUsers(new String[] { "*CURRENT" } );
    // Sort the list by job number in ascending order.
    list.addSortField(SpooledFileOpenList.JOB_NUMBER, true);
    list.open();
    Enumeration items = list.getItems();
    while (items.hasMoreElements())
    {
        SpooledFileListItem item = (SpooledFileListItem)items.nextElement();
        System.out.println(item.getJobName() + "/" + item.getJobUser() + "/" + item.getJobNumber() + " - " + item.getName() + ", " + item.getNumber());
    }
    list.close();
 </pre>
 **/
public class SpooledFileOpenList extends OpenList
{
    static final long serialVersionUID = 17674018445884278L;

    // Constant representing the EBCDIC value for "*ALL      ".  Used internally for filtering.
    private static final byte[] ALL = new byte[] { 0x5C, (byte)0xC1, (byte)0xD3, (byte)0xD3, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };

    /**
     Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the OSPL0100 format of the underlying API.
     @see  #setFormat
     **/
    public static final String FORMAT_0100 = "OSPL0100";

    /**
     Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the OSPL0200 format of the underlying API.
     @see  #setFormat
     **/
    public static final String FORMAT_0200 = "OSPL0200";

    /**
     Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the OSPL0300 format of the underlying API.  This is the default format.
     @see  #setFormat
     **/
    public static final String FORMAT_0300 = "OSPL0300";


    // Sorting constants... in no particular order...

    /**
     Sorting constant used to sort the list of spooled files by the job name portion of the job information.
     @see  #addSortField
     **/
    public static final int JOB_NAME = 0;

    /**
     Sorting constant used to sort the list of spooled files by the user name portion of the job information.
     @see  #addSortField
     **/
    public static final int JOB_USER = 1;

    /**
     Sorting constant used to sort the list of spooled files by the job number portion of the job information.
     @see  #addSortField
     **/
    public static final int JOB_NUMBER = 2;

    /**
     Sorting constant used to sort the list of spooled files by spooled file name.
     @see  #addSortField
     **/
    public static final int NAME = 3;

    /**
     Sorting constant used to sort the list of spooled files by spooled file number.
     @see  #addSortField
     **/
    public static final int NUMBER = 4;

    /**
     Sorting constant used to sort the list of spooled files by status.
     @see  #addSortField
     **/
    public static final int STATUS = 5;

    /**
     Sorting constant used to sort the list of spooled files by date.  Only servers running server operating system releases V5R2M0 and higher can sort with this value using FORMAT_0100.
     @see  #addSortField
     **/
    public static final int DATE_OPENED = 6;

    /**
     Sorting constant used to sort the list of spooled files by time.  Only servers running server operating system releases V5R2M0 and higher can sort with this value using FORMAT_0100.
     @see  #addSortField
     **/
    public static final int TIME_OPENED = 7;

    /**
     Sorting constant used to sort the list of spooled files by schedule.  This value is only valid for FORMAT_0300.
     @see  #addSortField
     **/
    public static final int SCHEDULE = 8;

    /**
     Sorting constant used to sort the list of spooled files by system.  Only servers running server operating system releases V5R2M0 and higher can sort with this value using FORMAT_0100 or FORMAT_0200.
     @see  #addSortField
     **/
    public static final int JOB_SYSTEM = 9;

    /**
     Sorting constant used to sort the list of spooled files by user data.
     @see  #addSortField
     **/
    public static final int USER_DATA = 10;

    /**
     Sorting constant used to sort the list of spooled files by form type.
     @see  #addSortField
     **/
    public static final int FORM_TYPE = 11;

    /**
     Sorting constant used to sort the list of spooled files by output queue name.
     @see  #addSortField
     **/
    public static final int OUTPUT_QUEUE_NAME = 12;

    /**
     Sorting constant used to sort the list of spooled files by output queue library.
     @see  #addSortField
     **/
    public static final int OUTPUT_QUEUE_LIBRARY = 13;

    /**
     Sorting constant used to sort the list of spooled files by auxiliary storage pool (ASP).  This value is only valid for FORMAT_0300.
     @see  #addSortField
     **/
    public static final int ASP = 14;

    /**
     Sorting constant used to sort the list of spooled files by size.  This value is only valid for FORMAT_0300.
     @see  #addSortField
     **/
    public static final int SIZE = 15;

    // Sorting constant.  Used internally.  When the user specifies SIZE, we automatically assume SIZE_MULTIPLIER as well.  This value is only valid for FORMAT_0300.
    private static final int SIZE_MULTIPLIER = 16;

    /**
     Sorting constant used to sort the list of spooled files by total number of pages.
     @see  #addSortField
     **/
    public static final int TOTAL_PAGES = 17;

    /**
     Sorting constant used to sort the list of spooled files by number of copies left to print.
     @see  #addSortField
     **/
    public static final int COPIES_LEFT_TO_PRINT = 18;

    /**
     Sorting constant used to sort the list of spooled files by priority.
     @see  #addSortField
     **/
    public static final int PRIORITY = 19;

    /**
     Sorting constant used to sort the list of spooled files by printer name.  This value is only valid for FORMAT_0200.
     @see  #addSortField
     **/
    public static final int PRINTER_NAME = 20;

    /**
     Sorting constant used to sort the list of spooled files by printer assignment.  This value is only valid for FORMAT_0200.
     @see  #addSortField
     **/
    public static final int PRINTER_ASSIGNED = 21;

    /**
     Sorting constant used to sort the list of spooled files by current page number.  This value is only valid for FORMAT_0100 and FORMAT_0200.
     @see  #addSortField
     **/
    public static final int CURRENT_PAGE = 22;

    /**
     Sorting constant used to sort the list of spooled files by device type.  This value is only valid for FORMAT_0100 and FORMAT_0200.
     @see  #addSortField
     **/
    public static final int DEVICE_TYPE = 23;


    // Use an enumeration to represent the format value.
    private int format_ = 3;

    // Job name portion of qualified job name filter.
    private String filterJobName_ = "";
    // User name portion of qualified job name filter.
    private String filterJobUser_ = "";
    // Job number portion of qualified job name filter.
    private String filterJobNumber_ = "";

    // User name filter.
    private String[] filterUsers_;
    // Qualified output queue name filter.
    private QSYSObjectPathName[] filterOutputQueues_;
    // Form type filter.
    private String filterFormType_;
    // User-specified data filter.
    private String filterUserData_;
    // Spooled file status filter.
    private String[] filterStatuses_;
    // Device name or printer device name filter.
    private String[] filterDevices_;
    // System name filter.
    private String filterJobSystemName_;
    // Starting spooled file create data and time filter.
    private Date filterCreationDateStart_;
    // Ending spooled file create data and time filter.
    private Date filterCreationDateEnd_;

    // Keys for sort information.
    private Vector sortKeys_ = new Vector();

    /**
     Constructs a SpooledFileOpenList object with the given system.  By default, this list will generate a list of SpooledFileListItem objects for all spooled files on the system using the default format of {@link #FORMAT_0300 FORMAT_0300}.
     @param  system  The system object representing the server on which the spooled files exist.
     **/
    public SpooledFileOpenList(AS400 system)
    {
        super(system);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing SpooledFileOpenList object.");
    }

    /**
     Constructs a SpooledFileOpenList object with the given system and format.  By default, this list will generate a list of SpooledFileListItem objects for all spooled files on the system.
     @param  system  The system object representing the server on which the spooled files exist.
     @param  format  The format of the underlying API.
     @see  #setFormat
     **/
    public SpooledFileOpenList(AS400 system, String format)
    {
        this(system);
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing SpooledFileOpenList object.");
        setFormat(format);
    }

    /**
     Adds a field on which to sort the list when it is built.  Use one of the sort constants on this class.  By default, no sorting is done.
     @param  field  The field used to sort the list.  Fields which are not applicable to the format used or the server operating system release are silently ignored.
     @param  ascending  true to sort in ascending order on this field (e.g. A-Z or 0-9); false for descending.
     **/
    public void addSortField(int field, boolean ascending)
    {
        // We map the field constant to the appropriate offset in the output record of the API.
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Adding sort field, field: " + field + ", ascending: " + ascending);
        // For 'size of spooled file' automatically add 'spooled file size multiplier'.
        if (field == SIZE) addSortField(SIZE_MULTIPLIER, ascending);
        // '1' is ascending, '2' is descending.
        sortKeys_.addElement(new int[] { field, ascending ? 0xF1 : 0xF2 } );
    }

    /**
     Calls QGY/QGYOLSPL.
     @return  The list information parameter.
     **/
    protected byte[] callOpenListAPI() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Opening spooled file list.");

        // Retrieve server operating system VRM.
        int vrm = system_.getVRM();
        // Convert based on server job CCSID.
        CharConverter conv = new CharConverter(system_.getCcsid(), system_);

        // Figure out our 'sort information' parameter.
        int sortKeysSize = sortKeys_.size();
        byte[] sortInformation = new byte[4 + (12 * sortKeysSize)];
        BinaryConverter.intToByteArray(sortKeysSize, sortInformation, 0);

        int validSortKeysSize = 0;
        for (int i = 0; i < sortKeysSize; ++i)
        {
            int[] pair = (int[])sortKeys_.elementAt(i);
            int field = pair[0];
            int fieldLength = findFieldLength(field, vrm);

            // Ignore any sort fields that don't apply for our current format.
            if (fieldLength > 0)
            {
                BinaryConverter.intToByteArray(findStartingPosition(field), sortInformation, 4 + validSortKeysSize * 12);
                BinaryConverter.intToByteArray(fieldLength, sortInformation, 8 + validSortKeysSize * 12);
                BinaryConverter.shortToByteArray(findDataType(field), sortInformation, 12 + validSortKeysSize * 12);
                sortInformation[14 + validSortKeysSize * 12] = (byte)pair[1];
                ++validSortKeysSize;
            }
            else
            {
                Trace.log(Trace.WARNING, "Sort key not valid, position: " + i + ", field: " + field + "");
            }
        }
        // If any invalid keys were found, shrink the array.
        if (validSortKeysSize != sortKeysSize)
        {
            byte[] oldValue = sortInformation;
            sortInformation = new byte[4 + (12 * validSortKeysSize)];
            BinaryConverter.intToByteArray(validSortKeysSize, sortInformation, 0);
            System.arraycopy(oldValue, 4, sortInformation, 4, 12 * validSortKeysSize);
        }

        // Figure out the length of the 'filter information' parameter and the 'format of filter information' to use.
        int filterInformationLength = 0;
        boolean useOSPF0200 = false;
        if (vrm < 0x00050200 || filterJobSystemName_ == null && filterCreationDateStart_ == null && filterCreationDateEnd_ == null)
        {
            // OSPF0100 filter format.
            filterInformationLength = 92;
            if (filterUsers_ != null && filterUsers_.length > 1)
            {
                filterInformationLength += 12 * (filterUsers_.length - 1);
            }
            if (filterOutputQueues_ != null && filterOutputQueues_.length > 1)
            {
                filterInformationLength += 20 * (filterOutputQueues_.length - 1);
            }
            if (filterStatuses_ != null && filterStatuses_.length > 1)
            {
                filterInformationLength += 12 * (filterStatuses_.length - 1);
            }
            if (filterDevices_ != null && filterDevices_.length > 1)
            {
                filterInformationLength += 12 * (filterDevices_.length - 1);
            }
        }
        else
        {
            // OSPF0200 filter format.
            useOSPF0200 = true;
            filterInformationLength = 110;
            if (filterUsers_ != null)
            {
                filterInformationLength += 10 * filterUsers_.length;
            }
            if (filterOutputQueues_ != null)
            {
                filterInformationLength += 20 * filterOutputQueues_.length;
            }
            if (filterStatuses_ != null)
            {
                filterInformationLength += 10 * filterStatuses_.length;
            }
            if (filterDevices_ != null)
            {
                filterInformationLength += 10 * filterDevices_.length;
            }
        }

        // Figure out the 'filter information' parameter.
        byte[] filterInformation = new byte[filterInformationLength];
        int offset = 0;
        if (!useOSPF0200)
        {
            // Fill in the filter info for the OSPF0100 format.
            if (filterUsers_ == null || filterUsers_.length == 0)
            {
                // Number of user names is one.
                BinaryConverter.intToByteArray(1, filterInformation, offset);
                offset += 4;
                // User name is *ALL.
                System.arraycopy(ALL, 0, filterInformation, offset, 10);
                offset += 12;
            }
            else
            {
                // Number of user names.
                BinaryConverter.intToByteArray(filterUsers_.length, filterInformation, offset);
                offset += 4;
                for (int i = 0; i < filterUsers_.length; ++i)
                {
                    // User name.
                    for (int ii = 0; ii < 10; ++ii) filterInformation[ii + offset] = 0x40;
                    conv.stringToByteArray(filterUsers_[i], filterInformation, offset, 10);
                    offset += 12;
                }
            }
            if (filterOutputQueues_ == null || filterOutputQueues_.length == 0)
            {
                // Number of output queue names is one.
                BinaryConverter.intToByteArray(1, filterInformation, offset);
                offset += 4;
                // Output queue name is *ALL.
                System.arraycopy(ALL, 0, filterInformation, offset, 10);
                offset += 10;
                // Output queue library name is blanks.
                for (int ii = 0; ii < 10; ++ii) filterInformation[ii + offset] = 0x40;
                offset += 10;
            }
            else
            {
                // Number of qualified output queue names.
                BinaryConverter.intToByteArray(filterOutputQueues_.length, filterInformation, offset);
                offset += 4;
                for (int i = 0; i < filterOutputQueues_.length; ++i)
                {
                    for (int ii = 0; ii < 20; ++ii) filterInformation[ii + offset] = 0x40;
                    // Output queue name.
                    conv.stringToByteArray(filterOutputQueues_[i].getObjectName(), filterInformation, offset, 10);
                    offset += 10;
                    // Output queue library name.
                    conv.stringToByteArray(filterOutputQueues_[i].getLibraryName(), filterInformation, offset, 10);
                    offset += 10;
                }
            }
            if (filterFormType_ == null)
            {
                // Form type is *ALL.
                System.arraycopy(ALL, 0, filterInformation, offset, 10);
            }
            else
            {
                // Form type.
                for (int ii = 0; ii < 10; ++ii) filterInformation[ii + offset] = 0x40;
                conv.stringToByteArray(filterFormType_, filterInformation, offset, 10);
            }
            offset += 10;
            if (filterUserData_ == null)
            {
                // User-specified data is *ALL.
                System.arraycopy(ALL, 0, filterInformation, offset, 10);
            }
            else
            {
                // User-specified data.
                for (int ii = 0; ii < 10; ++ii) filterInformation[ii + offset] = 0x40;
                conv.stringToByteArray(filterUserData_, filterInformation, offset, 10);
            }
            offset += 10;
            if (filterStatuses_ == null || filterStatuses_.length == 0)
            {
                // Number of statuses is one.
                BinaryConverter.intToByteArray(1, filterInformation, offset);
                offset += 4;
                // Spooled file status is *ALL.
                System.arraycopy(ALL, 0, filterInformation, offset, 10);
                offset += 12;
            }
            else
            {
                // Number of statuses.
                BinaryConverter.intToByteArray(filterStatuses_.length, filterInformation, offset);
                offset += 4;
                for (int i = 0; i < filterStatuses_.length; ++i)
                {
                    // Spooled file status.
                    for (int ii = 0; ii < 10; ++ii) filterInformation[ii + offset] = 0x40;
                    conv.stringToByteArray(filterStatuses_[i], filterInformation, offset, 10);
                    offset += 12;
                }
            }
            if (filterDevices_ == null || filterDevices_.length == 0)
            {
                // Number of device names is one.
                BinaryConverter.intToByteArray(1, filterInformation, offset);
                offset += 4;
                // Device name is *ALL.
                System.arraycopy(ALL, 0, filterInformation, offset, 10);
                offset += 12;
            }
            else
            {
                // Number of device names.
                BinaryConverter.intToByteArray(filterDevices_.length, filterInformation, offset);
                offset += 4;
                for (int i = 0; i < filterDevices_.length; ++i)
                {
                    // Device name.
                    for (int ii = 0; ii < 10; ++ii) filterInformation[ii + offset] = 0x40;
                    conv.stringToByteArray(filterDevices_[i], filterInformation, offset, 10);
                    offset += 12;
                }
            }
        }
        else
        {
            // Fill in the filter infomation for the OSPF0200 format.

            // Blank fill char portions of filter information.
            for (int ii = 52; ii < 106; ++ii) filterInformation[ii] = 0x40;
            for (int ii = 110; ii < filterInformation.length; ++ii) filterInformation[ii] = 0x40;
            // Start offset past fixed portion of filter information.
            offset = 110;

            // Length of filter information (just the fixed portion).
            BinaryConverter.intToByteArray(106, filterInformation, 0);
            if (filterUsers_ != null && filterUsers_.length > 0)
            {
                // Offset to user name entries.
                BinaryConverter.intToByteArray(offset, filterInformation, 4);
                // Number of user name entries.
                BinaryConverter.intToByteArray(filterUsers_.length, filterInformation, 8);

                for (int i = 0; i < filterUsers_.length; ++i)
                {
                    // User name.
                    conv.stringToByteArray(filterUsers_[i], filterInformation, offset, 10);
                    offset += 10;
                }
            }
            // Length of user name entry.
            BinaryConverter.intToByteArray(10, filterInformation, 12);
            if (filterOutputQueues_ != null && filterOutputQueues_.length > 0)
            {
                // Offset to output queue name entries.
                BinaryConverter.intToByteArray(offset, filterInformation, 16);
                // Number of output queue name entries.
                BinaryConverter.intToByteArray(filterOutputQueues_.length, filterInformation, 20);

                for (int i = 0; i < filterOutputQueues_.length; ++i)
                {
                    // Output queue name.
                    conv.stringToByteArray(filterOutputQueues_[i].getObjectName(), filterInformation, offset, 10);
                    offset += 10;
                    // Output queue library name.
                    conv.stringToByteArray(filterOutputQueues_[i].getLibraryName(), filterInformation, offset, 10);
                    offset += 10;
                }
            }
            // Length of output queue name entry.
            BinaryConverter.intToByteArray(20, filterInformation, 24);
            if (filterStatuses_ != null && filterStatuses_.length > 0)
            {
                // Offset to spooled file status entries.
                BinaryConverter.intToByteArray(offset, filterInformation, 28);
                // Number of spooled file status entries.
                BinaryConverter.intToByteArray(filterStatuses_.length, filterInformation, 32);

                for (int i = 0; i < filterStatuses_.length; ++i)
                {
                    // Spooled file status.
                    conv.stringToByteArray(filterStatuses_[i], filterInformation, offset, 10);
                    offset += 10;
                }
            }
            // Length of spooled file status entry.
            BinaryConverter.intToByteArray(10, filterInformation, 36);
            if (filterDevices_ != null && filterDevices_.length > 0)
            {
                // Offset to printer device name entries.
                BinaryConverter.intToByteArray(offset, filterInformation, 40);
                // Number of printer device name entries.
                BinaryConverter.intToByteArray(filterDevices_.length, filterInformation, 44);

                for (int i = 0; i < filterDevices_.length; ++i)
                {
                    // Printer device name.
                    conv.stringToByteArray(filterDevices_[i], filterInformation, offset, 10);
                    offset += 10;
                }
            }
            // Length of printer device name entry.
            BinaryConverter.intToByteArray(10, filterInformation, 48);
            if (filterFormType_ == null)
            {
                // Form type is *ALL.
                System.arraycopy(ALL, 0, filterInformation, 52, 4);
            }
            else
            {
                // Form type.
                conv.stringToByteArray(filterFormType_, filterInformation, 52, 10);
            }
            if (filterUserData_ == null)
            {
                // User-specified data is *ALL.
                System.arraycopy(ALL, 0, filterInformation, 62, 4);
            }
            else
            {
                // User-specified data.
                conv.stringToByteArray(filterUserData_, filterInformation, 62, 10);
            }
            if (filterJobSystemName_ == null)
            {
                // System name is *ALL.
                System.arraycopy(ALL, 0, filterInformation, 72, 4);
            }
            else
            {
                // System name.
                conv.stringToByteArray(filterJobSystemName_, filterInformation, 72, 8);
            }
            if (filterCreationDateStart_ == null && filterCreationDateEnd_ == null)
            {
                // Starting spooled file create date is *ALL.
                System.arraycopy(ALL, 0, filterInformation, 80, 4);
                // Starting spooled file create time is blank.
                // Ending spooled file create date is blank.
                // Ending spooled file create time is blank.
            }
            else if (filterCreationDateStart_ == null)
            {
                // Starting spooled file create date is *FIRST.
                filterInformation[80] = 0x5C;
                filterInformation[81] = (byte)0xC6;
                filterInformation[82] = (byte)0xC9;
                filterInformation[83] = (byte)0xD9;
                filterInformation[84] = (byte)0xE2;
                filterInformation[85] = (byte)0xE3;
                // Starting spooled file create time is blank.
                // Ending spooled file create date and time.
                formatOS400DateString(filterCreationDateEnd_, filterInformation, 93);
            }
            else if (filterCreationDateEnd_ == null)
            {
                // Starting spooled file create date and time.
                formatOS400DateString(filterCreationDateStart_, filterInformation, 80);
                // Ending spooled file create date is *LAST.
                filterInformation[93] = 0x5C;
                filterInformation[94] = (byte)0xD3;
                filterInformation[95] = (byte)0xC1;
                filterInformation[96] = (byte)0xE2;
                filterInformation[97] = (byte)0xE3;
                // Ending spooled file create time is blank.
            }
            else
            {
                // Starting spooled file create date and time.
                formatOS400DateString(filterCreationDateStart_, filterInformation, 80);
                // Ending spooled file create date and time.
                formatOS400DateString(filterCreationDateEnd_, filterInformation, 93);
            }
        }

        // Figure out the 'qualified job name' parameter.
        byte[] qualifiedJobName = new byte[26];
        for (int ii = 0; ii < 26; ++ii) qualifiedJobName[ii] = 0x40;
        conv.stringToByteArray(filterJobName_, qualifiedJobName, 0, 10);
        conv.stringToByteArray(filterJobUser_, qualifiedJobName, 10, 10);
        conv.stringToByteArray(filterJobNumber_, qualifiedJobName, 20, 6);

        // Format bytes is EBCDIC 'OSPL0X00' where X is 1, 2, or 3.
        byte[] formatBytes = new byte[] { (byte)0xD6, (byte)0xE2, (byte)0xD7, (byte)0xD3, (byte)0xF0, (byte)(0xF0 | format_), (byte)0xF0, (byte)0xF0 };

        // Setup program parameters.
        ProgramParameter[] parameters = new ProgramParameter[useOSPF0200 ? 10 : 9];
        // Receiver variable, output, char(*).
        parameters[0] = new ProgramParameter(0);
        // Length of receiver variable, input, binary(4).
        parameters[1] = new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } );
        // List information, output, char(80).
        parameters[2] = new ProgramParameter(80);
        // Number of records to return, input, binary(4).
        parameters[3] = new ProgramParameter(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF } );
        // Sort information, input, char(*).
        parameters[4] = new ProgramParameter(sortInformation);
        // Filter information, input, char(*).
        parameters[5] = new ProgramParameter(filterInformation);
        // Qualified job name, input, char(26).
        parameters[6] = new ProgramParameter(qualifiedJobName);
        // Format of the generated list, input, char(8).
        parameters[7] = new ProgramParameter(formatBytes);
        // Error code, I/O, char(*).
        parameters[8] = EMPTY_ERROR_CODE_PARM;
        // We default to OSPF0100 to be compatible with pre-V5R2 systems.
        if (useOSPF0200)
        {
            // Format of filter information, input, char(8), EBCDIC 'OSPF0200'.
            parameters[9] = new ProgramParameter(new byte[] { (byte)0xD6, (byte)0xE2, (byte)0xD7, (byte)0xC6, (byte)0xF0, (byte)0xF2, (byte)0xF0, (byte)0xF0 } );
        }

        // Call the program.
        ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QGYOLSPL.PGM", parameters);
        if (!pc.run())
        {
            throw new AS400Exception(pc.getMessageList());
        }

        // List information returned.
        return parameters[2].getOutputData();
    }

    /**
     Clears all sorting information for this list.  No sorting will be done when the list is generated unless {@link #addSortField addSortField()} is called to specify which fields to sort on.
     **/
    public void clearSortFields()
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Clearing sort fields.");
        sortKeys_.removeAllElements();
    }

    // Helper method for sort parameter.
    private final short findDataType(int field)
    {
        // 4 is character.
        // 0 is signed binary.
        switch (field)
        {
            case JOB_NAME: return 4;
            case JOB_USER: return 4;
            case JOB_NUMBER: return 4;
            case NAME: return 4;
            case NUMBER: return 0;
            case STATUS: return format_ == 3 ? (short)0 : (short)4;
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

    // Helper method for sort parameter.  Returns 0 for fields that do not exist on the current format or VRM.
    private final int findFieldLength(int field, int vrm)
    {
        switch (field)
        {
            case JOB_NAME: return 10;
            case JOB_USER: return 10;
            case JOB_NUMBER: return 6;
            case NAME: return 10;
            case NUMBER: return 4;
            case STATUS: return format_ == 3 ? 4 : 10;
            case DATE_OPENED: return format_ != 1 || vrm >= 0x00050200 ? 7 : 0;
            case TIME_OPENED: return format_ != 1 || vrm >= 0x00050200 ? 6 : 0;
            case SCHEDULE: return format_ == 3 ? 1 : 0;
            case JOB_SYSTEM: return format_ == 3 ? 10 : vrm >= 0x00050200 ? 8 : 0;
            case USER_DATA: return 10;
            case FORM_TYPE: return 10;
            case OUTPUT_QUEUE_NAME: return 10;
            case OUTPUT_QUEUE_LIBRARY: return 10;
            case ASP: return format_ == 3 ? 4 : 0;
            case SIZE: return format_ == 3 ? 4 : 0;
            case SIZE_MULTIPLIER: return format_ == 3 ? 4 : 0;
            case TOTAL_PAGES: return 4;
            case COPIES_LEFT_TO_PRINT: return 4;
            case PRIORITY: return format_ == 3 ? 1 : 2;
            case PRINTER_NAME: return format_ == 2 ? 10 : 0;
            case PRINTER_ASSIGNED: return format_ == 2 ? 1 : 0;
            case CURRENT_PAGE: return format_ == 3 ? 0 : 4;
            case DEVICE_TYPE: return format_ == 3 ? 0 : 10;
            default:
                return 0;
        }
    }

    // Helper method for sort parameter.
    private final int findStartingPosition(int field)
    {
        // This is 1-based for whatever reason.
        switch (field)
        {
            case JOB_NAME: return format_ == 3 ? 1 : 11;
            case JOB_USER: return format_ == 3 ? 11 : 21;
            case JOB_NUMBER: return format_ == 3 ? 21 : 31;
            case NAME: return format_ == 3 ? 27 : 1;
            case NUMBER: return 37;
            case STATUS: return format_ == 3 ? 41 : 83;
            case DATE_OPENED: return format_ == 3 ? 45 : format_ == 2 ? 161 : 169;
            case TIME_OPENED: return format_ == 3 ? 52 : format_ == 2 ? 168 : 176;
            case SCHEDULE: return 58;
            case JOB_SYSTEM: return format_ == 3 ? 59 : format_ == 2 ? 193 : 161;
            case USER_DATA: return format_ == 3 ? 69 : 73;
            case FORM_TYPE: return format_ == 3 ? 79 : 93;
            case OUTPUT_QUEUE_NAME: return format_ == 3 ? 89 : 53;
            case OUTPUT_QUEUE_LIBRARY: return format_ == 3 ? 99 : 63;
            case ASP: return 109;
            case SIZE: return 113;
            case SIZE_MULTIPLIER: return 117;
            case TOTAL_PAGES: return format_ == 3 ? 121 : 41;
            case COPIES_LEFT_TO_PRINT: return format_ == 3 ? 125 : 49;
            case PRIORITY: return format_ == 3 ? 129 : 103;
            case PRINTER_NAME: return 175;
            case PRINTER_ASSIGNED: return 174;
            case CURRENT_PAGE: return 45;
            case DEVICE_TYPE: return 137;
            default:
                return 0;
        }
    }

    // Helper method that fills in EBCDIC bytes in the format CYYMMDDHHMMSS.
    private static final void formatOS400DateString(Date d, byte[] data, int offset)
    {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(d);

        // Set C byte.
        int year = cal.get(Calendar.YEAR);
        data[offset] = (byte)(year / 100 - 19 | 0xF0);
        // Set YY bytes.
        int yy = year % 100;
        data[offset + 1] = (byte)(yy / 10 | 0xF0);
        data[offset + 2] = (byte)(yy % 10 | 0xF0);
        // Set MM bytes.
        int month = cal.get(Calendar.MONTH) + 1;
        data[offset + 3] = (byte)(month / 10 | 0xF0);
        data[offset + 4] = (byte)(month % 10 | 0xF0);
        // Set DD bytes.
        int day = cal.get(Calendar.DAY_OF_MONTH);
        data[offset + 5] = (byte)(day / 10 | 0xF0);
        data[offset + 6] = (byte)(day % 10 | 0xF0);
        // Set HH bytes.
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        data[offset + 7] = (byte)(hour / 10 | 0xF0);
        data[offset + 8] = (byte)(hour % 10 | 0xF0);
        // Set MM bytes.
        int minute = cal.get(Calendar.MINUTE);
        data[offset + 9] = (byte)(minute / 10 | 0xF0);
        data[offset + 10] = (byte)(minute % 10 | 0xF0);
        // Set SS bytes.
        int second = cal.get(Calendar.SECOND);
        data[offset + 11] = (byte)(second / 10 | 0xF0);
        data[offset + 12] = (byte)(second % 10 | 0xF0);
    }

    /**
     Formats the data from QGY/QGYOLSPL.
     **/
    protected Object[] formatOutputData(byte[] data, int recordsReturned, int recordLength) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Retrieve server operating system VRM.
        int vrm = system_.getVRM();
        // Convert based on server job CCSID.
        CharConverter conv = new CharConverter(system_.getCcsid(), system_);

        SpooledFileListItem[] sp = new SpooledFileListItem[recordsReturned];
        int offset = 0;
        for (int i = 0; i < recordsReturned; ++i)
        {
            if (format_ != 3)
            {
                // Format OSPL0100 or OSPL0200.
                String spooledFileName = conv.byteArrayToString(data, offset, 10).trim();
                String jobName = conv.byteArrayToString(data, offset + 10, 10).trim();
                String userName = conv.byteArrayToString(data, offset + 20, 10).trim();
                String jobNumber = conv.byteArrayToString(data, offset + 30, 6);
                int spooledFileNumber = BinaryConverter.byteArrayToInt(data, offset + 36);
                int totalPages = BinaryConverter.byteArrayToInt(data, offset + 40);
                // Not in 0300 format.
                int currentPage = BinaryConverter.byteArrayToInt(data, offset + 44);
                int copiesLeftToPrint = BinaryConverter.byteArrayToInt(data, offset + 48);
                String outputQueueName = conv.byteArrayToString(data, offset + 52, 10).trim();
                String outputQueueLibraryName = conv.byteArrayToString(data, offset + 62, 10).trim();
                String userData = conv.byteArrayToString(data, offset + 72, 10);
                // This is a BIN(4) in 0300 format.
                String status = conv.byteArrayToString(data, offset + 82, 10).trim();
                String formType = conv.byteArrayToString(data, offset + 92, 10).trim();
                // This is a CHAR(1) in 0300 format.
                String priority = conv.byteArrayToString(data, offset + 102, 2).trim();
                // Not in 0300 format.
                byte[] internalJobIdentifier = new byte[16];
                System.arraycopy(data, offset + 104, internalJobIdentifier, 0, 16);
                // Not in 0300 format.
                byte[] internalSpooledFileIdentifier = new byte[16];
                System.arraycopy(data, offset + 120, internalSpooledFileIdentifier, 0, 16);
                // Not in 0300 format.
                String deviceType = conv.byteArrayToString(data, offset + 136, 10).trim();
                int offsetToExtension = BinaryConverter.byteArrayToInt(data, offset + 148);
                String jobSystemName = null;
                String dateSpooledFileWasOpened = null;
                String timeSpooledFileWasOpened = null;
                // If there is an extension.
                if (offsetToExtension > 0)
                {
                    // This is a CHAR(10) in 0300 format.
                    jobSystemName = conv.byteArrayToString(data, offset + offsetToExtension, 8).trim();
                    dateSpooledFileWasOpened = conv.byteArrayToString(data, offset + offsetToExtension + 8, 7); 
                    timeSpooledFileWasOpened = conv.byteArrayToString(data, offset + offsetToExtension + 15, 6);
                }
                if (format_ == 1)
                {
                    // Format OSPL0100.
                    sp[i] = new SpooledFileListItem(spooledFileName, jobName, userName, jobNumber, spooledFileNumber, totalPages, currentPage, copiesLeftToPrint, outputQueueName, outputQueueLibraryName, userData, status, formType, priority, internalJobIdentifier, internalSpooledFileIdentifier, deviceType, jobSystemName, dateSpooledFileWasOpened, timeSpooledFileWasOpened);
                }
                else
                {
                    // Format OSPL0200 only.
                    dateSpooledFileWasOpened = conv.byteArrayToString(data, offset + 160, 7);
                    timeSpooledFileWasOpened = conv.byteArrayToString(data, offset + 167, 6);
                    // Not in 0300 format.
                    String printerAssigned = conv.byteArrayToString(data, offset + 173, 1);
                    // Not in 0300 format.
                    String printerName = conv.byteArrayToString(data, offset + 174, 10).trim();

                    sp[i] = new SpooledFileListItem(spooledFileName, jobName, userName, jobNumber, spooledFileNumber, totalPages, currentPage, copiesLeftToPrint, outputQueueName, outputQueueLibraryName, userData, status, formType, priority, internalJobIdentifier, internalSpooledFileIdentifier, deviceType, jobSystemName, dateSpooledFileWasOpened, timeSpooledFileWasOpened, printerAssigned, printerName);
                }
            }
            else
            {
                // Format OSPL0300.
                String jobName = conv.byteArrayToString(data, offset, 10).trim();
                String userName = conv.byteArrayToString(data, offset + 10, 10).trim();
                String jobNumber = conv.byteArrayToString(data, offset + 20, 6);
                String spooledFileName = conv.byteArrayToString(data, offset + 26, 10).trim();
                int spooledFileNumber = BinaryConverter.byteArrayToInt(data, offset + 36);
                // This is a CHAR(10) in 0200 format.
                int fileStatus = BinaryConverter.byteArrayToInt(data, offset + 40);
                String dateSpooledFileWasOpened = conv.byteArrayToString(data, offset + 44, 7);
                String timeSpooledFileWasOpened = conv.byteArrayToString(data, offset + 51, 6);
                // Not in 0200 format.
                String spooledFileSchedule = conv.byteArrayToString(data, offset + 57, 1);
                // This is a CHAR(8) in 0100 format.
                String jobSystemName = conv.byteArrayToString(data, offset + 58, 10).trim();
                String userData = conv.byteArrayToString(data, offset + 68, 10);
                String spooledFileFormType = conv.byteArrayToString(data, offset + 78, 10).trim();
                String outputQueueName = conv.byteArrayToString(data, offset + 88, 10).trim();
                String outputQueueLibraryName = conv.byteArrayToString(data, offset + 98, 10).trim();
                // Not in 0200 format.
                int auxiliaryStoragePool = BinaryConverter.byteArrayToInt(data, offset + 108);
                // Not in 0200 format.
                int sizeOfSpooledFile = BinaryConverter.byteArrayToInt(data, offset + 112);
                // Not in 0200 format.
                int spooledFileSizeMultiplier = BinaryConverter.byteArrayToInt(data, offset + 116);
                int totalPages = BinaryConverter.byteArrayToInt(data, offset + 120);
                int copiesLeftToPrint = BinaryConverter.byteArrayToInt(data, offset + 124);
                // This is a CHAR(2) in 0200 format.
                String priority = conv.byteArrayToString(data, offset + 128, 1);
                int internetPrintProtocolJobIdentifier = 0;
                if (vrm >= 0x00050300)
                {
                    internetPrintProtocolJobIdentifier = BinaryConverter.byteArrayToInt(data, offset + 132);
                }
                sp[i] = new SpooledFileListItem(jobName, userName, jobNumber, spooledFileName, spooledFileNumber, fileStatus, dateSpooledFileWasOpened, timeSpooledFileWasOpened, spooledFileSchedule, jobSystemName, userData, spooledFileFormType, outputQueueName, outputQueueLibraryName, auxiliaryStoragePool, sizeOfSpooledFile, spooledFileSizeMultiplier, totalPages, copiesLeftToPrint, priority, internetPrintProtocolJobIdentifier);
            }
            offset += recordLength;
        }
        return sp;
    }

    /**
     Returns receiver variable size based on format used.
     **/
    protected int getBestGuessReceiverSize(int number)
    {
        switch (format_)
        {
            case 1:  return 192 * number;
            case 2:  return 224 * number;
        }
        return 136 * number;
    }

    /**
     Returns the end creation date being used to filter the list of spooled files.
     @return  The end creation date or null if the end creation date used will be the latest date in the list.
     @see  #getFilterCreationDateStart
     **/
    public Date getFilterCreationDateEnd()
    {
        return filterCreationDateEnd_;
    }

    /**
     Returns the start creation date being used to filter the list of spooled files.
     @return  The start creation date or null if the start creation date used will be the earliest date in the list.
     @see  #getFilterCreationDateEnd
     **/
    public Date getFilterCreationDateStart()
    {
        return filterCreationDateStart_;
    }

    /**
     Returns the printer device names being used to filter the list of spooled files.
     @return  The array of printer device names or null if the list is not being filtered by device.
     **/
    public String[] getFilterDevices()
    {
        return filterDevices_;
    }

    /**
     Returns the form type being used to filter the list of spooled files.
     @return  The form type or null if the list is not being filtered by form type.
     **/
    public String getFilterFormType()
    {
        return filterFormType_;
    }

    /**
     Returns the job name portion of the job information used to determine which spooled files belong in the list.
     @return  The job name or the empty string ("") to indicate any job name.
     @see  #setFilterJobInformation
     **/
    public String getFilterJobName()
    {
        return filterJobName_;
    }

    /**
     Returns the job number portion of the job information used to determine which spooled files belong in the list.
     @return  The job number or the empty string ("") to indicate any job number.
     @see  #setFilterJobInformation
     **/
    public String getFilterJobNumber()
    {
        return filterJobNumber_;
    }

    /**
     Returns the job system name used to determine which spooled files belong in the list.
     @return  The job system name, "*CURRENT" for the current system, or null to indicate the default of "*ALL" meaning the list is not being filtered by job system name.  Note that the job system name filter is only used when connecting to servers running server operating system releases V5R2M0 and higher.
     @see  #setFilterJobSystemName
     **/
    public String getFilterJobSystemName()
    {
        return filterJobSystemName_;
    }

    /**
     Returns the user name portion of the job information used to determine which spooled files belong in the list.
     @return  The user name or the empty string ("") to indicate any user name.
     @see  #setFilterJobInformation
     **/
    public String getFilterJobUser()
    {
        return filterJobUser_;
    }

    /**
     Returns the output queue names being used to filter the list of spooled files.
     @return  The array of fully-qualified integrated file system path names of output queues, or null if the list is not being filtered by output queue.
     **/
    public String[] getFilterOutputQueues()
    {
        if (filterOutputQueues_ == null) return null;
        String[] returnValue = new String[filterOutputQueues_.length];
        for (int i = 0; i < filterOutputQueues_.length; ++i)
        {
            returnValue[i] = filterOutputQueues_[i].getPath();
        }
        return returnValue;
    }

    /**
     Returns the statuses being used to filter the list of spooled files.
     @return  The array of statuses or null if the list is not being filtered by status.
     **/
    public String[] getFilterStatuses()
    {
        return filterStatuses_;
    }

    /**
     Returns the user data being used to filter the list of spooled files.
     @return  The user data or null if the list is not being filtered by user data.
     **/
    public String getFilterUserData()
    {
        return filterUserData_;
    }

    /**
     Returns the user names being used to filter the list of spooled files.
     @return  The array of user names or null if the list is not being filtered by user name.
     **/
    public String[] getFilterUsers()
    {
        return filterUsers_;
    }

    /**
     Returns the format currently in use by this open list.  Possible values are:
     <ul>
     <li>{@link #FORMAT_0100 FORMAT_0100}
     <li>{@link #FORMAT_0200 FORMAT_0200}
     <li>{@link #FORMAT_0300 FORMAT_0300}
     </ul>
     @return  The format.  The default format is FORMAT_0300.
     **/
    public String getFormat()
    {
        switch (format_)
        {
            case 1:  return FORMAT_0100;
            case 2:  return FORMAT_0200;
        }
        return FORMAT_0300;
    }

    /**
     Sets the creation date range used to filter the list of spooled files.  By default, the list is not filtered by creation date.  Note that the creation date filter is only used when connecting to servers running server operating system releases V5R2M0 and higher.
     @param  filterCreationDateStart  The start date.  All spooled files with a creation date and time equal to or later than the start date will be selected.  Specify null to indicate that the earliest creation date and later will be selected, up to the specified <i>end</i> date.
     @param  filterCreationDateEnd  The end date.  All spooled files with a creation date and time equal to or earlier than the end date will be selected.  Specify null to indicate that the latest creation date and earlier will be selected, down to the specified <i>start</i> date.
     @see  #getFilterCreationDateStart
     @see  #getFilterCreationDateEnd
     **/
    public void setFilterCreationDate(Date filterCreationDateStart, Date filterCreationDateEnd)
    {
        filterCreationDateStart_ = filterCreationDateStart;
        filterCreationDateEnd_ = filterCreationDateEnd;
        resetHandle();
    }

    /**
     Sets the printer device names used to filter the list of spooled files.
     @param  devices  The array of printer device names.  Only spooled files that belong to the specified printer devices are returned in the list.  Specify null to clear the status filter, so that spooled files in the list are no longer filtered based on device.
     **/
    public void setFilterDevices(String[] devices)
    {
        filterDevices_ = devices;
        resetHandle();
    }

    /**
     Sets the form type used to filter the list of spooled files.
     @param  formType  The form type.  Only spooled files whose form type matches the specified form type are returned in the list.  Specify "*STD" for the standard form type.  Specify null to clear the form type filter, so that spooled files in the list are no longer filtered based on form type.
     **/
    public void setFilterFormType(String formType)
    {
        filterFormType_ = formType;
        resetHandle();
    }

    /**
     Sets the qualified job information used to filter the list of spooled files.  Specifying null for a parameter resets it to its default value of blank.  Blank is only valid for a parameter if all three parameters are specified as blank, or the filterJobName parameter is specified as "*".
     @param  filterJobName  The job name of the job whose spooled files are to be included in the list.  Specify "*" for the current job.  If "*" is specified, the filterJobUser and filterJobNumber parameters are automatically set to blank.
     @param  filterJobUser  The user name of the job whose spooled files are to be included in the list.
     @param  filterJobNumber  The job number of the job whose spooled files are to be included in the list.
     @see  #getFilterJobName
     @see  #getFilterJobUser
     @see  #getFilterJobNumber
     **/
    public void setFilterJobInformation(String filterJobName, String filterJobUser, String filterJobNumber)
    {
        filterJobName_ = filterJobName == null ? "" : filterJobName.trim();
        filterJobUser_ = filterJobUser == null ? "" : filterJobUser.trim();
        filterJobNumber_ = filterJobNumber == null ? "" : filterJobNumber.trim();

        if (filterJobName_.equals("*"))
        {
            filterJobUser_ = "";
            filterJobNumber_ = "";
        }
        resetHandle();
    }

    /**
     Sets the job system name used to filter the list of spooled files.  Specifying null resets it to its default value of "*ALL".  Note that the job system name filter is only used when connecting to servers running server operating system releases V5R2 and higher.
     @param  systemName  Only spooled files created on <i>systemName</i>will be included in the list.  Specify "*CURRENT" to return only spooled files created on the current system.
     @see  #getFilterJobSystemName
     **/
    public void setFilterJobSystemName(String systemName)
    {
        filterJobSystemName_ = systemName;
        resetHandle();
    }

    /**
     Sets the output queues used to filter the list of spooled files.
     @param  filterOutputQueues  An array of fully-qualified integrated file system path names of output queues.  Only spooled files that reside in the specified output queues are returned in the list.  Specify null to clear the output queue filter, so that the spooled files in the list are no longer filtered based on output queue.
     **/
    public void setFilterOutputQueues(String[] filterOutputQueues)
    {
        if (filterOutputQueues == null)
        {
            filterOutputQueues_ = null;
        }
        else
        {
            filterOutputQueues_ = new QSYSObjectPathName[filterOutputQueues.length];
            for (int i = 0; i < filterOutputQueues.length; ++i)
            {
                filterOutputQueues_[i] = new QSYSObjectPathName(filterOutputQueues[i]);
                // Ensure that the type is correct.
                if (!filterOutputQueues_[i].getObjectType().equals("OUTQ"))
                {
                    Trace.log(Trace.WARNING, "Object type is not valid, path: '" + filterOutputQueues[i] + "'");
                }
            }
        }
        resetHandle();
    }

    /**
     Sets the statuses used to filter the list of spooled files.  Possible status values are:
     <ul>
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_CLOSED SpooledFileListItem.STATUS_CLOSED} - "*CLOSED"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_DEFERRED SpooledFileListItem.STATUS_DEFERRED} - "*DEFERRED"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_SENDING SpooledFileListItem.STATUS_SENDING} - "*SENDING"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_FINISHED SpooledFileListItem.STATUS_FINISHED} - "*FINISHED"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_HELD SpooledFileListItem.STATUS_HELD} - "*HELD"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_MESSAGE_WAIT SpooledFileListItem.STATUS_MESSAGE_WAIT} - "*MESSAGE"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_OPEN SpooledFileListItem.STATUS_OPEN} - "*OPEN"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_PENDING SpooledFileListItem.STATUS_PENDING} - "*PENDING"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_PRINTING SpooledFileListItem.STATUS_PRINTING} - "*PRINTER"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_READY SpooledFileListItem.STATUS_READY} - "*READY"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_SAVED SpooledFileListItem.STATUS_SAVED} - "*SAVED"
     <li>{@link com.ibm.as400.access.list.SpooledFileListItem#STATUS_WRITING SpooledFileListItem.STATUS_WRITING} - "*WRITING"
     </ul> 
     @param  statuses  The array of statuses.  Only spooled files whose status matches one of the specified statuses are returned in the list.  Specify null to clear the status filter, so that spooled files in the list are no longer filtered based on status.
     **/
    public void setFilterStatuses(String[] statuses)
    {
        filterStatuses_ = statuses;
        resetHandle();
    }

    /**
     Sets the user data used to filter the list of spooled files.
     @param  userData  The user data.  Only spooled files whose user data exactly matches the specified user data are returned in the list.  Specify null to clear the user data filter, so that spooled files in the list are no longer filtered based on user data.
     **/
    public void setFilterUserData(String userData)
    {
        filterUserData_ = userData;
        resetHandle();
    }

    /**
     Sets the user names used to filter the list of spooled files.
     @param  users  An array of user names.  Only spooled files that were created by the specified users are returned in the list.  Specify "*CURRENT" as one of the users to mean the current user profile.  Specify null to clear the user filter, so that the spooled files in the list are no longer filtered based on user name.
     **/
    public void setFilterUsers(String[] users)
    {
        filterUsers_ = users;
        resetHandle();
    }

    /**
     Sets the format this list will use on the next call to {@link #open open()}.  Any SpooledFileListItems generated by this list will have attributes associated with the specified format.  The default format is FORMAT_0300.
     @param  format  The format of the underlying API.  Possible values are:
     <ul>
     <li>{@link #FORMAT_0100 FORMAT_0100} - This is faster than FORMAT_0200.
     <li>{@link #FORMAT_0200 FORMAT_0200} - Contains more information than FORMAT_0100.
     <li>{@link #FORMAT_0300 FORMAT_0300} - This is faster than FORMAT_0100.
     </ul>
     **/
    public void setFormat(String format)
    {
        if (format == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'format' is null.");
            throw new NullPointerException("format");
        }
        format = format.toUpperCase().trim();
        if (format.equals(FORMAT_0100))
        {
            format_ = 1;
        }
        else if (format.equals(FORMAT_0200))
        {
            format_ = 2;
        }
        else if (format.equals(FORMAT_0300))
        {
            format_ = 3;
        }
        else
        {
            Trace.log(Trace.ERROR, "Value of parameter 'format' is not valid: " + format);
            throw new ExtendedIllegalArgumentException("format (" + format + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        resetHandle();
    }
}
