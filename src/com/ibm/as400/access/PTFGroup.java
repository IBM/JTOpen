///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PTFGroup.java
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
import java.util.Calendar;
import java.util.Date;


/**
* Lists information for a specific PTF group on a system. 
* Specifically you can retrieve a list of the related PTF groups, and a list of PTFs.
**/
public class PTFGroup
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

    // Also use this to synchronize access to the user space
    static final String USER_SPACE_NAME = "JT4PTF    QTEMP     ";

    static final String USER_SPACE_PATH = "/QSYS.LIB/QTEMP.LIB/JT4PTF.USRSPC";

    private AS400 system_;
    private String PTFGroupName_;
    private boolean includeRelatedPTFGroups_ = false;

    // GRPR0500 format
    private String relatedPTFGroupName_;
    private String PTFgroupDescription_;
    private int PTFGroupLevel_;
    private int PTFGroupStatus_;


    /**
    * Constant indicating the PTF Group status cannong be resolved because a related PTF group
    * is either not found on the system or is in error.
    **/
    public static final int PTF_GROUP_STATUS_UNKNOWN = 0;

    /**
    * Constant indicating all PTFs in the PTF group and related PTF groups are for products
    * that are not installed or supported on this system.
    **/
    public static final int PTF_GROUP_STATUS_NOT_APPLICABLE = 1;

    /**
    * Constant indicating there are no PTFs in the PTF group or related PTF groups that are for installed
    * products on this system.  There is at least one PTF that is for a product, release, option, and load 
    * identifier that is supported on this system.
    **/
    public static final int PTF_GROUP_STATUS_SUPPORTED_ONLY = 2;

    /**
    * Constant indicating there is at least one PTF that is for an installed product on this system, and not
    * all the PTFs or their superseding PTFs are temporarily or permanently applied.
    **/
    public static final int PTF_GROUP_STATUS_NOT_INSTALLED = 3;

    /**
    * Constant indicating all PTFs for products that are installed on this system are temporarily or
    * permanently applied.  If a PTF is superseded, a superseding PTF is either temporarily or
    * permanently applied.
    **/
    public static final int PTF_GROUP_STATUS_INSTALLED = 4;

    /**
    * Constant indicating the PTF group information is in error.  Either delete the PTF group or replace
    * the PTF group information that is currently on the system.
    **/
    public static final int PTF_GROUP_STATUS_ERROR = 5;

    /**
    * Constant indicating the PTF group is not found on the system.  This status will only be returned
    * when using format GRPR0500.
    **/
    public static final int PTF_GROUP_STATUS_NOT_FOUND = 6;


    /**
    * Constructs a PTFGroup object.
    * @param system The AS400 system.
    * @param ptfGroupName The name of the PTF Group you wish to get details about.
    **/
    PTFGroup(AS400 system, String ptfGroupName)
    {
        if (system == null) throw new NullPointerException("system");
        if (ptfGroupName == null) throw new NullPointerException("ptfGroupName");
        system_ = system;
        PTFGroupName_ = ptfGroupName.trim();
    }

    /**
     * This constructor is used by ListPTFGroups.
    **/
    PTFGroup(AS400 system, String ptfGroupName, String ptfDescription, int ptfLevel, int ptfStatus)
    {
        this(system, ptfGroupName);
        PTFGroupLevel_ = ptfLevel;
        PTFgroupDescription_ = ptfDescription;
        PTFGroupStatus_ = ptfStatus;
    }

    /**
     * Returns the system.
     * @return The system.
    **/
    public AS400 getSystem()
    {
        return system_;
    }

    /**
     * Returns a list of Related PTF Groups.
    **/
    public PTFGroup[] getRelatedPTFGroups()
    throws AS400Exception,
    AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        int len = 164;
        int ccsid = system_.getCcsid();
        ConvTable conv = ConvTable.getTable(ccsid, null);

        ProgramParameter[] parms = new ProgramParameter[5];
        parms[0] = new ProgramParameter(conv.stringToByteArray(USER_SPACE_NAME));        //qualified user space name
        try {parms[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        byte[] groupInfo = new byte[69];
        AS400Text text60 = new AS400Text(60, ccsid, system_);
        AS400Text text1 = new AS400Text(1, ccsid, system_);
        BinaryConverter.intToByteArray(69, groupInfo, 0);
        text60.toBytes(PTFGroupName_, groupInfo, 4);
        BinaryConverter.intToByteArray(ccsid, groupInfo, 64);
        groupInfo[68] = includeRelatedPTFGroups_ ? (byte)0xF1 : (byte)0xF0; // '1' or '0'
        parms[1] = new ProgramParameter(groupInfo);        // PTF Group Information 
        try{parms[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        parms[2] = new ProgramParameter(conv.stringToByteArray("GRPR0500"));            //FORMAT
        try{parms[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(ccsid));     //CCSID
        try{parms[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        parms[4] = new ProgramParameter(new byte[4]);                               // error code
        try{parms[4].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}

        ServiceProgramCall pc = new ServiceProgramCall(system_, "/QSYS.LIB/QPZGROUP.SRVPGM", "QpzListPtfGroupDetails", ServiceProgramCall.NO_RETURN_VALUE, parms);
        byte[] buf = null;
        synchronized(USER_SPACE_NAME)       
        {
            UserSpace us = new UserSpace(system_, USER_SPACE_PATH);
            us.setMustUseProgramCall(true);
            us.create(256*1024, true, "", (byte)0, "User space for PTF Group", "*EXCLUDE");
            try
            {
                if (!pc.run())
                {
                    throw new AS400Exception(pc.getMessageList());
                }
                int size = us.getLength();
                buf = new byte[size];
                us.read(buf, 0);
            }
            finally
            {
                us.close();
            }
        }
        int startingOffset = BinaryConverter.byteArrayToInt(buf, 124);      
        int numEntries = BinaryConverter.byteArrayToInt(buf, 132);
        int entrySize = BinaryConverter.byteArrayToInt(buf, 136);
        int entryCCSID = BinaryConverter.byteArrayToInt(buf, 140);
        conv = ConvTable.getTable(entryCCSID, null);
        PTFGroup[] ptfs = new PTFGroup[numEntries];
        int offset = 0;
        for (int i=0; i<numEntries; ++i)
        {
            offset = startingOffset + (i*entrySize);
            relatedPTFGroupName_ = conv.byteArrayToString(buf, offset, 60);
            offset += 60;
            PTFgroupDescription_ = conv.byteArrayToString(buf, offset, 100);
            offset += 100;
            PTFGroupLevel_ = BinaryConverter.byteArrayToInt(buf, offset);
            offset += 4;
            PTFGroupStatus_ = BinaryConverter.byteArrayToInt(buf, offset);
            offset +=4;
            ptfs[i] = new PTFGroup(system_, relatedPTFGroupName_, PTFgroupDescription_, PTFGroupLevel_, PTFGroupStatus_);
        }
        return ptfs;
    }

    /**
    * Returns the description for the PTF group.
    * @return the description
    **/
    public String getPTFGroupDescription()
    throws AS400Exception,
    AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        return PTFgroupDescription_;
    }

    /**
    * Returns the PTF group name.
    * @return the name.
    **/
    public String getPTFGroupName()
    throws AS400Exception,
    AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        return PTFGroupName_;
    }

    /**
    * Returns the PTF Group Level.  
    * @return the PTF Group level, or 0 if the group level cannot be determined.
    **/
    public int getPTFGroupLevel()
    throws AS400Exception,
    AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        return PTFGroupLevel_;
    }

    /**
    * Indicates the overall status of the PTF group on this system.  
    * @return the PTF group status.  Possible Values are:
    * <UL>
     * <LI>{@link #PTF_GROUP_STATUS_UNKNOWN PTF_GROUP_STATUS_UNKNOWN}
     * <LI>{@link #PTF_GROUP_STATUS_NOT_APPLICABLE PTF_GROUP_STATUS_NOT_APPLICABLE}
     * <LI>{@link #PTF_GROUP_STATUS_SUPPORTED_ONLY PTF_GROUP_STATUS_SUPPORTED_ONLY}
     * <LI>{@link #PTF_GROUP_STATUS_NOT_INSTALLED PTF_GROUP_STATUS_NOT_INSTALLED}
     * <LI>{@link #PTF_GROUP_STATUS_INSTALLED PTF_GROUP_STATUS_INSTALLED}
     * <LI>{@link #PTF_GROUP_STATUS_ERROR PTF_GROUP_STATUS_ERROR}
     * <LI>{@link #PTF_GROUP_STATUS_NOT_FOUND PTF_GROUP_STATUS_NOT_FOUND}
     * <UL>
    **/
    public int getPTFGroupStatus()
    throws AS400Exception,
    AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {
        return PTFGroupStatus_;
    }

    /**
    * Specifies whether information from all related PTF groups should be included when the list of
    * PTFs or related PTF groups are returned.  By default the information is not included.
    * @param value true if information from all related PTF groups should be included, false otherwise.
    **/
    public void includeRelatedPTFGroups(boolean value)
    {
        includeRelatedPTFGroups_ = value;
    }

    /**
    * Indicates if information from all related PTF groups are included when the list of 
    * PTFs or related PTF groups are returned.
    * @return true if information from all related PTF groups is included, false otherwise.
    **/
    public boolean areRelatedPTFGroupsIncluded()
    {
        return includeRelatedPTFGroups_;
    }


    public PTF[] getPTFs()
    throws AS400Exception,
    AS400SecurityException,
    ErrorCompletingRequestException,
    InterruptedException,
    IOException,
    ObjectDoesNotExistException
    {

        int len = 73;
        int ccsid = system_.getCcsid();
        ConvTable conv = ConvTable.getTable(ccsid, null);

        ProgramParameter[] parms = new ProgramParameter[5];
        parms[0] = new ProgramParameter(conv.stringToByteArray(USER_SPACE_NAME));        //qualified user space name
        try{parms[0].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        byte[] groupInfo = new byte[69];
        AS400Text text60 = new AS400Text(60, ccsid, system_);
        AS400Text text1 = new AS400Text(1, ccsid, system_);
        BinaryConverter.intToByteArray(69, groupInfo, 0);
        text60.toBytes(PTFGroupName_, groupInfo, 4);
        BinaryConverter.intToByteArray(ccsid, groupInfo, 64);
        groupInfo[68] = includeRelatedPTFGroups_ ? (byte)0xF1 : (byte)0xF0; // '1' or '0'
        parms[1] = new ProgramParameter(groupInfo);       // PTF group information
        try{parms[1].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        parms[2] = new ProgramParameter(conv.stringToByteArray("GRPR0300"));            //FORMAT
        try{parms[2].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(ccsid));     //CCSID
        try{parms[3].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}
        parms[4] = new ProgramParameter(new byte[4]);                               // error code
        try{parms[4].setParameterType(ProgramParameter.PASS_BY_REFERENCE);}catch (PropertyVetoException pve){}

        ServiceProgramCall pc = new ServiceProgramCall(system_, "/QSYS.LIB/QPZGROUP.SRVPGM", "QpzListPtfGroupDetails", ServiceProgramCall.NO_RETURN_VALUE, parms);
        byte[] buf = null;
        synchronized(USER_SPACE_NAME)       
        {
            UserSpace us = new UserSpace(system_, USER_SPACE_PATH);
            us.setMustUseProgramCall(true);
            us.create(256*1024, true, "", (byte)0, "User space for PTF Group", "*EXCLUDE");
            try
            {
                if (!pc.run())
                {
                    throw new AS400Exception(pc.getMessageList());
                }
                int size = us.getLength();
                buf = new byte[size];
                us.read(buf, 0);
            }
            finally
            {
                us.close();
            }
        }
        int startingOffset = BinaryConverter.byteArrayToInt(buf, 124);      
        int numEntries = BinaryConverter.byteArrayToInt(buf, 132);
        int entrySize = BinaryConverter.byteArrayToInt(buf, 136);
        int entryCCSID = BinaryConverter.byteArrayToInt(buf, 140);
        conv = ConvTable.getTable(entryCCSID, null);
        PTF[] ptfs = new PTF[numEntries];
        int offset = 0;
        for (int i=0; i<numEntries; ++i)
        {
            offset = startingOffset + (i*entrySize);
            String PTFId_ = conv.byteArrayToString(buf, offset, 7);
            offset += 7;
            String productId_ = conv.byteArrayToString(buf, offset, 7);
            offset +=7;
            String release_ = conv.byteArrayToString(buf, offset, 6);
            offset +=6;
            String productOption_ = conv.byteArrayToString(buf, offset, 4);
            offset +=4;
            String productLoadId_ = conv.byteArrayToString(buf, offset, 4);
            offset +=4;
            String minimumLevel_ = conv.byteArrayToString(buf, offset, 2);
            offset +=2;
            String maximumLevel_ = conv.byteArrayToString(buf, offset, 2);
            offset +=2;
            String loadedStatus_ = conv.byteArrayToString(buf, offset++, 1);
            int IPLaction_ = (int)(buf[offset++] & 0x000F); // EBCDIC 0xF0 = '0', 0xF1 = '1', etc.
            String actionPending_ = conv.byteArrayToString(buf, offset++, 1);
            String actionRequired_ = conv.byteArrayToString(buf, offset++, 1);
            String coverLetterStatus_ = conv.byteArrayToString(buf, offset++, 1);
            String onOrderStatus_ = conv.byteArrayToString(buf, offset++, 1);
            String saveFileStatus_ = conv.byteArrayToString(buf, offset++, 1);
            String saveFileName_ = conv.byteArrayToString(buf, offset, 10);
            offset +=10;
            String saveFileLibraryName_ = conv.byteArrayToString(buf, offset, 10);
            offset +=10;
            String supersededByPTFId_ = conv.byteArrayToString(buf, offset, 7);
            offset +=7;
            String latestSupersedingPTFId_ = conv.byteArrayToString(buf, offset, 7);
            offset +=7;
            String productStatus_ = conv.byteArrayToString(buf, offset++, 1);
            ptfs[i] = new PTF(system_, PTFId_, productId_, release_, productOption_, productLoadId_, minimumLevel_, maximumLevel_, loadedStatus_, IPLaction_, actionPending_, actionRequired_, coverLetterStatus_, onOrderStatus_, saveFileStatus_, saveFileName_, saveFileLibraryName_, supersededByPTFId_, latestSupersedingPTFId_, productStatus_);
        }
        return ptfs;
    }
}
