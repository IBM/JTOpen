///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SystemValueList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2006 International Business Machines Corporation and
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
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
 Provided methods for retrieving information about lists of System Values.
 @see SystemValue
**/
public class SystemValueList implements Serializable
{
    static final long serialVersionUID = 4L;

    /**
     Constant indicating the system value's group is *ALC (Allocation).
     **/
    public static final int GROUP_ALC = 0;
    /**
     Constant indicating the system value's group is *ALL (All).
     **/
    public static final int GROUP_ALL = 9;
    /**
     Constant indicating the system value's group is *DATTIM (Date and Time).
     **/
    public static final int GROUP_DATTIM = 1;
    /**
     Constant indicating the system value's group is *EDT (Editing).
     **/
    public static final int GROUP_EDT = 2;
    /**
     Constant indicating the system value's group is *LIBL (Library List).
     **/
    public static final int GROUP_LIBL = 3;
    /**
     Constant indicating the system value's group is *MSG (Message and Logging).
     **/
    public static final int GROUP_MSG = 4;
    /**
     Constant indicating the system value's group is *NET (Net Attribute).
     **/
    public static final int GROUP_NET = 8;
    /**
     Constant indicating the system value's group is *SEC (Security).
     **/
    public static final int GROUP_SEC = 5;
    /**
     Constant indicating the system value's group is *STG (Storage).
     **/
    public static final int GROUP_STG = 6;
    /**
     Constant indicating the system value's group is *SYSCTL (System control).
     **/
    public static final int GROUP_SYSCTL = 7;

    /**
     Constant indicating the returned system value type is String[].
     **/
    public static final int TYPE_ARRAY = 4;
    /**
     Constant indicating the returned system value type is Date.
     **/
    public static final int TYPE_DATE = 5;
    /**
     Constant indicating the returned system value type is BigDecimal.
     **/
    public static final int TYPE_DECIMAL = 2;
    /**
     Constant indicating the returned system value type is Integer.
     **/
    public static final int TYPE_INTEGER = 3;
    /**
     Constant indicating the returned system value type is String.
     **/
    public static final int TYPE_STRING = 1;

    // Constant indicating the system value's type on the system is BINARY.
    static final byte SERVER_TYPE_BINARY = (byte)0xC2;
    // Constant indicating the system value's type on the system is CHAR.
    static final byte SERVER_TYPE_CHAR = (byte)0xC3;

    // Constants for operating system VRM.
    private static final int VRM420 = 0x00040200;
    private static final int VRM430 = 0x00040300;
    private static final int VRM440 = 0x00040400;
    private static final int VRM510 = 0x00050100;
    private static final int VRM520 = 0x00050200;
    private static final int VRM530 = 0x00050300;
    private static final int VRM540 = 0x00050400;
    private static final int VRM610 = 0x00060100;

    // The total number of groups.
    private static final int GROUP_COUNT = 10;

    // The MRI for the group names.
    private static final String[] GROUP_NAMES = new String[]
    {
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIM_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_NAME"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_NAME"),
    };

    // The MRI for the group descriptions.
    private static final String[] GROUP_DESCRIPTIONS = new String[]
    {
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIM_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_DESC"),
        ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_DESC"),
    };

    // The list of all SystemValueInfo objects.
    // For V4R2, there are 125 system values and 35 network attributes.
    // For V4R3, there are an additional 5 system values.
    // For V4R4, there are 2 additional system values and 2 additional network attributes.
    // For V5R1, there are 6 additional system values.
    // For V5R2, there are 2 additional system values.
    // For V5R3, there are 10 additional system values.
    // For V5R4, there are 2 additional system values.
    // For V6R1, there are 6 additional system values.
    // There are at least 195 system values.
    // The optimal hash size is 195/0.75 = 260.
    private static Hashtable list = new Hashtable(260);

    // Provided for convenient lookup of system value groups.
    static Vector[] groups = { new Vector(), new Vector(), new Vector(), new Vector(), new Vector(), new Vector(), new Vector(), new Vector(), new Vector(), new Vector() };

    // Initialize the hashtables.
    static
    {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Populating system value table...");

        // Network attributes.
        SystemValueList.list.put("ALRBCKFP", new SystemValueInfo("ALRBCKFP", SERVER_TYPE_CHAR, 8, 2, TYPE_ARRAY, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRBCKFP_DES")));
        SystemValueList.list.put("ALRCTLD", new SystemValueInfo("ALRCTLD", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRCTLD_DES")));
        SystemValueList.list.put("ALRDFTFP", new SystemValueInfo("ALRDFTFP", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRDFTFP_DES")));
        SystemValueList.list.put("ALRFTR", new SystemValueInfo("ALRFTR", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRFTR_DES")));
        SystemValueList.list.put("ALRHLDCNT", new SystemValueInfo("ALRHLDCNT", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRHLDCNT_DES")));
        SystemValueList.list.put("ALRLOGSTS", new SystemValueInfo("ALRLOGSTS", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRLOGSTS_DES")));
        SystemValueList.list.put("ALRPRIFP", new SystemValueInfo("ALRPRIFP", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRPRIFP_DES")));
        SystemValueList.list.put("ALRRQSFP", new SystemValueInfo("ALRRQSFP", SERVER_TYPE_CHAR, 8, 2, TYPE_ARRAY, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRRQSFP_DES")));
        SystemValueList.list.put("ALRSTS", new SystemValueInfo("ALRSTS", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALRSTS_DES")));
        SystemValueList.list.put("ALWANYNET", new SystemValueInfo("ALWANYNET", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALWANYNET_DES")));
        SystemValueList.list.put("ALWHPRTWR", new SystemValueInfo("ALWHPRTWR", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALWHPRTWR_DES")));
        SystemValueList.list.put("ALWVRTAPPN", new SystemValueInfo("ALWVRTAPPN", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("ALWVRTAPPN_DES")));
        SystemValueList.list.put("DDMACC", new SystemValueInfo("DDMACC", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("DDMACC_DES")));
        SystemValueList.list.put("DFTCNNLST", new SystemValueInfo("DFTCNNLST", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("DFTCNNLST_DES")));
        SystemValueList.list.put("DFTMODE", new SystemValueInfo("DFTMODE", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("DFTMODE_DES")));
        SystemValueList.list.put("DFTNETTYPE", new SystemValueInfo("DFTNETTYPE", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("DFTNETTYPE_DES")));
        SystemValueList.list.put("DTACPR", new SystemValueInfo("DTACPR", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("DTACPR_DES")));
        SystemValueList.list.put("DTACPRINM", new SystemValueInfo("DTACPRINM", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("DTACPRINM_DES")));
        SystemValueList.list.put("HPRPTHTMR", new SystemValueInfo("HPRPTHTMR", SERVER_TYPE_CHAR, 10, 4, TYPE_ARRAY, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("HPRPTHTMR_DES")));
        SystemValueList.list.put("JOBACN", new SystemValueInfo("JOBACN", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("JOBACN_DES")));
        SystemValueList.list.put("LCLCPNAME", new SystemValueInfo("LCLCPNAME", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("LCLCPNAME_DES")));
        SystemValueList.list.put("LCLLOCNAME", new SystemValueInfo("LCLLOCNAME", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("LCLLOCNAME_DES")));
        SystemValueList.list.put("LCLNETID", new SystemValueInfo("LCLNETID", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("LCLNETID_DES")));
        SystemValueList.list.put("MAXHOP", new SystemValueInfo("MAXHOP", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("MAXHOP_DES")));
        SystemValueList.list.put("MAXINTSSN", new SystemValueInfo("MAXINTSSN", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("MAXINTSSN_DES")));
        SystemValueList.list.put("MSGQ", new SystemValueInfo("MSGQ", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("MSGQ_DES")));
        SystemValueList.list.put("NETSERVER", new SystemValueInfo("NETSERVER", SERVER_TYPE_CHAR, 17, 5, TYPE_ARRAY, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("NETSERVER_DES")));
        SystemValueList.list.put("NODETYPE", new SystemValueInfo("NODETYPE", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("NODETYPE_DES")));
        SystemValueList.list.put("NWSDOMAIN", new SystemValueInfo("NWSDOMAIN", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("NWSDOMAIN_DES")));
        SystemValueList.list.put("OUTQ", new SystemValueInfo("OUTQ", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("OUTQ_DES")));
        SystemValueList.list.put("PCSACC", new SystemValueInfo("PCSACC", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("PCSACC_DES")));
        SystemValueList.list.put("PNDSYSNAME", new SystemValueInfo("PNDSYSNAME", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("PNDSYSNAME_DES"),true));
        SystemValueList.list.put("RAR", new SystemValueInfo("RAR", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("RAR_DES")));
        SystemValueList.list.put("SYSNAME", new SystemValueInfo("SYSNAME", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("SYSNAME_DES")));
        SystemValueList.list.put("VRTAUTODEV", new SystemValueInfo("VRTAUTODEV", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, VRM420, ResourceBundleLoader.getSystemValueText("VRTAUTODEV_DES")));

        // V4R4 network attributes.
        SystemValueList.list.put("ALWADDCLU", new SystemValueInfo("ALWADDCLU", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, VRM440, ResourceBundleLoader.getSystemValueText("ALWADDCLU_DES")));
        SystemValueList.list.put("MDMCNTRYID", new SystemValueInfo("MDMCNTRYID", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_NET, VRM440, ResourceBundleLoader.getSystemValueText("MDMCNTRYID_DES")));

        // System values.
        SystemValueList.list.put("QABNORMSW", new SystemValueInfo("QABNORMSW", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QABNORMSW_DES"), true));
        SystemValueList.list.put("QACGLVL", new SystemValueInfo("QACGLVL", SERVER_TYPE_CHAR, 10, 8, TYPE_ARRAY, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QACGLVL_DES")));
        SystemValueList.list.put("QACTJOB", new SystemValueInfo("QACTJOB", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QACTJOB_DES")));
        SystemValueList.list.put("QADLACTJ", new SystemValueInfo("QADLACTJ", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QADLACTJ_DES")));
        SystemValueList.list.put("QADLSPLA", new SystemValueInfo("QADLSPLA", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QADLSPLA_DES")));
        SystemValueList.list.put("QADLTOTJ", new SystemValueInfo("QADLTOTJ", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QADLTOTJ_DES")));
        SystemValueList.list.put("QALWOBJRST", new SystemValueInfo("QALWOBJRST", SERVER_TYPE_CHAR, 10, 15, TYPE_ARRAY, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QALWOBJRST_DES")));
        SystemValueList.list.put("QALWUSRDMN", new SystemValueInfo("QALWUSRDMN", SERVER_TYPE_CHAR, 10, 50, TYPE_ARRAY, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QALWUSRDMN_DES")));
        SystemValueList.list.put("QASTLVL", new SystemValueInfo("QASTLVL", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QASTLVL_DES")));
        SystemValueList.list.put("QATNPGM", new SystemValueInfo("QATNPGM", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QATNPGM_DES")));
        SystemValueList.list.put("QAUDCTL", new SystemValueInfo("QAUDCTL", SERVER_TYPE_CHAR, 10, 5, TYPE_ARRAY, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QAUDCTL_DES")));
        SystemValueList.list.put("QAUDENDACN", new SystemValueInfo("QAUDENDACN", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QAUDENDACN_DES")));
        SystemValueList.list.put("QAUDFRCLVL", new SystemValueInfo("QAUDFRCLVL", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QAUDFRCLVL_DES")));
        SystemValueList.list.put("QAUDLVL", new SystemValueInfo("QAUDLVL", SERVER_TYPE_CHAR, 10, 16, TYPE_ARRAY, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QAUDLVL_DES")));
        SystemValueList.list.put("QAUTOCFG", new SystemValueInfo("QAUTOCFG", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QAUTOCFG_DES")));
        SystemValueList.list.put("QAUTORMT", new SystemValueInfo("QAUTORMT", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QAUTORMT_DES")));
        SystemValueList.list.put("QAUTOSPRPT", new SystemValueInfo("QAUTOSPRPT", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QAUTOSPRPT_DES")));
        SystemValueList.list.put("QAUTOVRT", new SystemValueInfo("QAUTOVRT", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QAUTOVRT_DES")));
        SystemValueList.list.put("QBASACTLVL", new SystemValueInfo("QBASACTLVL", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, VRM420, ResourceBundleLoader.getSystemValueText("QBASACTLVL_DES")));
        SystemValueList.list.put("QBASPOOL", new SystemValueInfo("QBASPOOL", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, VRM420, ResourceBundleLoader.getSystemValueText("QBASPOOL_DES")));
        SystemValueList.list.put("QBOOKPATH", new SystemValueInfo("QBOOKPATH", SERVER_TYPE_CHAR, 63, 5, TYPE_ARRAY, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QBOOKPATH_DES")));
        SystemValueList.list.put("QCCSID", new SystemValueInfo("QCCSID", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QCCSID_DES")));
        SystemValueList.list.put("QCENTURY", new SystemValueInfo("QCENTURY", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QCENTURY_DES")));
        SystemValueList.list.put("QCHRID", new SystemValueInfo("QCHRID", SERVER_TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QCHRID_DES")));
        SystemValueList.list.put("QCMNARB", new SystemValueInfo("QCMNARB", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QCMNARB_DES")));
        SystemValueList.list.put("QCMNRCYLMT", new SystemValueInfo("QCMNRCYLMT", SERVER_TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QCMNRCYLMT_DES")));
        SystemValueList.list.put("QCNTRYID", new SystemValueInfo("QCNTRYID", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QCNTRYID_DES")));
        SystemValueList.list.put("QCONSOLE", new SystemValueInfo("QCONSOLE", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QCONSOLE_DES"), true));
        SystemValueList.list.put("QCRTAUT", new SystemValueInfo("QCRTAUT", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QCRTAUT_DES")));
        SystemValueList.list.put("QCRTOBJAUD", new SystemValueInfo("QCRTOBJAUD", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QCRTOBJAUD_DES")));
        SystemValueList.list.put("QCTLSBSD", new SystemValueInfo("QCTLSBSD", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QCTLSBSD_DES")));
        SystemValueList.list.put("QCURSYM", new SystemValueInfo("QCURSYM", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, VRM420, ResourceBundleLoader.getSystemValueText("QCURSYM_DES")));
        SystemValueList.list.put("QDATE", new SystemValueInfo("QDATE", SERVER_TYPE_CHAR, 7, 1, TYPE_DATE, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QDATE_DES")));
        SystemValueList.list.put("QDATFMT", new SystemValueInfo("QDATFMT", SERVER_TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_EDT, VRM420, ResourceBundleLoader.getSystemValueText("QDATFMT_DES")));
        SystemValueList.list.put("QDATSEP", new SystemValueInfo("QDATSEP", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, VRM420, ResourceBundleLoader.getSystemValueText("QDATSEP_DES")));
        SystemValueList.list.put("QDAY", new SystemValueInfo("QDAY", SERVER_TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QDAY_DES")));
        SystemValueList.list.put("QDAYOFWEEK", new SystemValueInfo("QDAYOFWEEK", SERVER_TYPE_CHAR, 4, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QDAYOFWEEK_DES"), true));
        SystemValueList.list.put("QDBRCVYWT", new SystemValueInfo("QDBRCVYWT", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QDBRCVYWT_DES")));
        SystemValueList.list.put("QDECFMT", new SystemValueInfo("QDECFMT", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, VRM420, ResourceBundleLoader.getSystemValueText("QDECFMT_DES")));
        SystemValueList.list.put("QDEVNAMING", new SystemValueInfo("QDEVNAMING", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QDEVNAMING_DES")));
        SystemValueList.list.put("QDEVRCYACN", new SystemValueInfo("QDEVRCYACN", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QDEVRCYACN_DES")));
        SystemValueList.list.put("QDSCJOBITV", new SystemValueInfo("QDSCJOBITV", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QDSCJOBITV_DES")));
        SystemValueList.list.put("QDSPSGNINF", new SystemValueInfo("QDSPSGNINF", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QDSPSGNINF_DES")));
        SystemValueList.list.put("QDYNPTYSCD", new SystemValueInfo("QDYNPTYSCD", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QDYNPTYSCD_DES")));
        SystemValueList.list.put("QFRCCVNRST", new SystemValueInfo("QFRCCVNRST", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QFRCCVNRST_DES")));
        SystemValueList.list.put("QHOUR", new SystemValueInfo("QHOUR", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QHOUR_DES")));
        SystemValueList.list.put("QHSTLOGSIZ", new SystemValueInfo("QHSTLOGSIZ", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QHSTLOGSIZ_DES")));
        SystemValueList.list.put("QIGC", new SystemValueInfo("QIGC", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QIGC_DES"), true));
        SystemValueList.list.put("QIGCCDEFNT", new SystemValueInfo("QIGCCDEFNT", SERVER_TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QIGCCDEFNT_DES")));
        SystemValueList.list.put("QINACTITV", new SystemValueInfo("QINACTITV", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QINACTITV_DES")));
        SystemValueList.list.put("QINACTMSGQ", new SystemValueInfo("QINACTMSGQ", SERVER_TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QINACTMSGQ_DES")));
        SystemValueList.list.put("QIPLDATTIM", new SystemValueInfo("QIPLDATTIM", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QIPLDATTIM_DES")));
        SystemValueList.list.put("QIPLSTS", new SystemValueInfo("QIPLSTS", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QIPLSTS_DES"), true));
        SystemValueList.list.put("QIPLTYPE", new SystemValueInfo("QIPLTYPE", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QIPLTYPE_DES")));
        SystemValueList.list.put("QJOBMSGQFL", new SystemValueInfo("QJOBMSGQFL", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QJOBMSGQFL_DES")));
        SystemValueList.list.put("QJOBMSGQMX", new SystemValueInfo("QJOBMSGQMX", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QJOBMSGQMX_DES")));
        SystemValueList.list.put("QJOBMSGQSZ", new SystemValueInfo("QJOBMSGQSZ", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QJOBMSGQSZ_DES")));
        SystemValueList.list.put("QJOBMSGQTL", new SystemValueInfo("QJOBMSGQTL", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QJOBMSGQTL_DES")));
        SystemValueList.list.put("QJOBSPLA", new SystemValueInfo("QJOBSPLA", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QJOBSPLA_DES")));
        SystemValueList.list.put("QKBDBUF", new SystemValueInfo("QKBDBUF", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QKBDBUF_DES")));
        SystemValueList.list.put("QKBDTYPE", new SystemValueInfo("QKBDTYPE", SERVER_TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QKBDTYPE_DES")));
        SystemValueList.list.put("QLANGID", new SystemValueInfo("QLANGID", SERVER_TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QLANGID_DES")));
        SystemValueList.list.put("QLEAPADJ", new SystemValueInfo("QLEAPADJ", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QLEAPADJ_DES")));
        SystemValueList.list.put("QLMTDEVSSN", new SystemValueInfo("QLMTDEVSSN", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QLMTDEVSSN_DES")));
        SystemValueList.list.put("QLMTSECOFR", new SystemValueInfo("QLMTSECOFR", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QLMTSECOFR_DES")));
        // This is size 2080 because of data returned on API. Actual size for path name is 1024 chars.
        SystemValueList.list.put("QLOCALE", new SystemValueInfo("QLOCALE", SERVER_TYPE_CHAR, 2080, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QLOCALE_DES")));
        SystemValueList.list.put("QMAXACTLVL", new SystemValueInfo("QMAXACTLVL", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, VRM420, ResourceBundleLoader.getSystemValueText("QMAXACTLVL_DES")));
        SystemValueList.list.put("QMAXSGNACN", new SystemValueInfo("QMAXSGNACN", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QMAXSGNACN_DES")));
        SystemValueList.list.put("QMAXSIGN", new SystemValueInfo("QMAXSIGN", SERVER_TYPE_CHAR, 6, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QMAXSIGN_DES")));
        SystemValueList.list.put("QMCHPOOL", new SystemValueInfo("QMCHPOOL", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, VRM420, ResourceBundleLoader.getSystemValueText("QMCHPOOL_DES")));
        SystemValueList.list.put("QMINUTE", new SystemValueInfo("QMINUTE", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QMINUTE_DES")));
        SystemValueList.list.put("QMODEL", new SystemValueInfo("QMODEL", SERVER_TYPE_CHAR, 4, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QMODEL_DES"),true));
        SystemValueList.list.put("QMONTH", new SystemValueInfo("QMONTH", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QMONTH_DES")));
        SystemValueList.list.put("QPASTHRSVR", new SystemValueInfo("QPASTHRSVR", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QPASTHRSVR_DES")));
        SystemValueList.list.put("QPFRADJ", new SystemValueInfo("QPFRADJ", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QPFRADJ_DES")));
        SystemValueList.list.put("QPRBFTR", new SystemValueInfo("QPRBFTR", SERVER_TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QPRBFTR_DES")));
        SystemValueList.list.put("QPRBHLDITV", new SystemValueInfo("QPRBHLDITV", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QPRBHLDITV_DES")));
        SystemValueList.list.put("QPRTDEV", new SystemValueInfo("QPRTDEV", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QPRTDEV_DES")));
        SystemValueList.list.put("QPRTKEYFMT", new SystemValueInfo("QPRTKEYFMT", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QPRTKEYFMT_DES")));
        SystemValueList.list.put("QPRTTXT", new SystemValueInfo("QPRTTXT", SERVER_TYPE_CHAR, 30, 1, TYPE_STRING, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QPRTTXT_DES")));
        SystemValueList.list.put("QPWDEXPITV", new SystemValueInfo("QPWDEXPITV", SERVER_TYPE_CHAR, 6, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDEXPITV_DES")));
        SystemValueList.list.put("QPWDLMTAJC", new SystemValueInfo("QPWDLMTAJC", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDLMTAJC_DES")));
        SystemValueList.list.put("QPWDLMTCHR", new SystemValueInfo("QPWDLMTCHR", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDLMTCHR_DES")));
        SystemValueList.list.put("QPWDLMTREP", new SystemValueInfo("QPWDLMTREP", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDLMTREP_DES")));
        SystemValueList.list.put("QPWDMAXLEN", new SystemValueInfo("QPWDMAXLEN", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDMAXLEN_DES")));
        SystemValueList.list.put("QPWDMINLEN", new SystemValueInfo("QPWDMINLEN", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDMINLEN_DES")));
        SystemValueList.list.put("QPWDPOSDIF", new SystemValueInfo("QPWDPOSDIF", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDPOSDIF_DES")));
        SystemValueList.list.put("QPWDRQDDGT", new SystemValueInfo("QPWDRQDDGT", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDRQDDGT_DES")));
        SystemValueList.list.put("QPWDRQDDIF", new SystemValueInfo("QPWDRQDDIF", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDRQDDIF_DES")));
        SystemValueList.list.put("QPWDVLDPGM", new SystemValueInfo("QPWDVLDPGM", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QPWDVLDPGM_DES")));
        SystemValueList.list.put("QPWRDWNLMT", new SystemValueInfo("QPWRDWNLMT", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QPWRDWNLMT_DES")));
        SystemValueList.list.put("QPWRRSTIPL", new SystemValueInfo("QPWRRSTIPL", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QPWRRSTIPL_DES")));
        SystemValueList.list.put("QQRYDEGREE", new SystemValueInfo("QQRYDEGREE", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QQRYDEGREE_DES")));
        SystemValueList.list.put("QQRYTIMLMT", new SystemValueInfo("QQRYTIMLMT", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QQRYTIMLMT_DES")));
        SystemValueList.list.put("QRCLSPLSTG", new SystemValueInfo("QRCLSPLSTG", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QRCLSPLSTG_DES")));
        SystemValueList.list.put("QRETSVRSEC", new SystemValueInfo("QRETSVRSEC", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QRETSVRSEC_DES")));
        SystemValueList.list.put("QRMTIPL", new SystemValueInfo("QRMTIPL", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QRMTIPL_DES")));
        SystemValueList.list.put("QRMTSIGN", new SystemValueInfo("QRMTSIGN", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QRMTSIGN_DES")));
        SystemValueList.list.put("QRMTSRVATR", new SystemValueInfo("QRMTSRVATR", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QRMTSRVATR_DES")));
        SystemValueList.list.put("QSCPFCONS", new SystemValueInfo("QSCPFCONS", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSCPFCONS_DES")));
        SystemValueList.list.put("QSECOND", new SystemValueInfo("QSECOND", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QSECOND_DES")));
        SystemValueList.list.put("QSECURITY", new SystemValueInfo("QSECURITY", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QSECURITY_DES")));
        SystemValueList.list.put("QSETJOBATR", new SystemValueInfo("QSETJOBATR", SERVER_TYPE_CHAR, 10, 16, TYPE_ARRAY, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSETJOBATR_DES")));
        SystemValueList.list.put("QSFWERRLOG", new SystemValueInfo("QSFWERRLOG", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QSFWERRLOG_DES")));
        SystemValueList.list.put("QSPCENV", new SystemValueInfo("QSPCENV", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSPCENV_DES")));
        SystemValueList.list.put("QSRLNBR", new SystemValueInfo("QSRLNBR", SERVER_TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSRLNBR_DES"),true));
        SystemValueList.list.put("QSRTSEQ", new SystemValueInfo("QSRTSEQ", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSRTSEQ_DES")));
        SystemValueList.list.put("QSRVDMP", new SystemValueInfo("QSRVDMP", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QSRVDMP_DES")));
        SystemValueList.list.put("QSTGLOWACN", new SystemValueInfo("QSTGLOWACN", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_STG, VRM420, ResourceBundleLoader.getSystemValueText("QSTGLOWACN_DES")));
        SystemValueList.list.put("QSTGLOWLMT", new SystemValueInfo("QSTGLOWLMT", SERVER_TYPE_BINARY, 7, 4, 1, TYPE_DECIMAL, GROUP_STG, VRM420, ResourceBundleLoader.getSystemValueText("QSTGLOWLMT_DES")));
        SystemValueList.list.put("QSTRPRTWTR", new SystemValueInfo("QSTRPRTWTR", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSTRPRTWTR_DES"), true));
        SystemValueList.list.put("QSTRUPPGM", new SystemValueInfo("QSTRUPPGM", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSTRUPPGM_DES")));
        SystemValueList.list.put("QSTSMSG", new SystemValueInfo("QSTSMSG", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_MSG, VRM420, ResourceBundleLoader.getSystemValueText("QSTSMSG_DES")));
        SystemValueList.list.put("QSVRAUTITV", new SystemValueInfo("QSVRAUTITV", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QSVRAUTITV_DES")));
        SystemValueList.list.put("QSYSLIBL", new SystemValueInfo("QSYSLIBL", SERVER_TYPE_CHAR, 10, 15, TYPE_ARRAY, GROUP_LIBL, VRM420, ResourceBundleLoader.getSystemValueText("QSYSLIBL_DES")));
        SystemValueList.list.put("QTIME", new SystemValueInfo("QTIME", SERVER_TYPE_CHAR, 9, 1, TYPE_DATE, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QTIME_DES")));
        SystemValueList.list.put("QTIMSEP", new SystemValueInfo("QTIMSEP", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, VRM420, ResourceBundleLoader.getSystemValueText("QTIMSEP_DES")));
        SystemValueList.list.put("QTOTJOB", new SystemValueInfo("QTOTJOB", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, VRM420, ResourceBundleLoader.getSystemValueText("QTOTJOB_DES")));
        SystemValueList.list.put("QTSEPOOL", new SystemValueInfo("QTSEPOOL", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_STG, VRM420, ResourceBundleLoader.getSystemValueText("QTSEPOOL_DES")));
        SystemValueList.list.put("QUPSDLYTIM", new SystemValueInfo("QUPSDLYTIM", SERVER_TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QUPSDLYTIM_DES")));
        SystemValueList.list.put("QUPSMSGQ", new SystemValueInfo("QUPSMSGQ", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, VRM420, ResourceBundleLoader.getSystemValueText("QUPSMSGQ_DES")));
        SystemValueList.list.put("QUSEADPAUT", new SystemValueInfo("QUSEADPAUT", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM420, ResourceBundleLoader.getSystemValueText("QUSEADPAUT_DES")));
        SystemValueList.list.put("QUSRLIBL", new SystemValueInfo("QUSRLIBL", SERVER_TYPE_CHAR, 10, 25, TYPE_ARRAY, GROUP_LIBL, VRM420, ResourceBundleLoader.getSystemValueText("QUSRLIBL_DES")));
        SystemValueList.list.put("QUTCOFFSET", new SystemValueInfo("QUTCOFFSET", SERVER_TYPE_CHAR, 5, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QUTCOFFSET_DES")));
        SystemValueList.list.put("QYEAR", new SystemValueInfo("QYEAR", SERVER_TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, VRM420, ResourceBundleLoader.getSystemValueText("QYEAR_DES")));

        // V4R3 system values.
        SystemValueList.list.put("QCHRIDCTL", new SystemValueInfo("QCHRIDCTL", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM430, ResourceBundleLoader.getSystemValueText("QCHRIDCTL_DES")));
        SystemValueList.list.put("QDYNPTYADJ", new SystemValueInfo("QDYNPTYADJ", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM430, ResourceBundleLoader.getSystemValueText("QDYNPTYADJ_DES")));
        SystemValueList.list.put("QIGCFNTSIZ", new SystemValueInfo("QIGCFNTSIZ", SERVER_TYPE_BINARY, 4, 1, 1, TYPE_DECIMAL, GROUP_SYSCTL, VRM430, ResourceBundleLoader.getSystemValueText("QIGCFNTSIZ_DES")));
        SystemValueList.list.put("QPRCMLTTSK", new SystemValueInfo("QPRCMLTTSK", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM430, ResourceBundleLoader.getSystemValueText("QPRCMLTTSK_DES")));
        SystemValueList.list.put("QPRCFEAT", new SystemValueInfo("QPRCFEAT", SERVER_TYPE_CHAR, 4, 1, TYPE_STRING, GROUP_SYSCTL, VRM430, ResourceBundleLoader.getSystemValueText("QPRCFEAT_DES"),true));

        // V4R4 system values.
        SystemValueList.list.put("QCFGMSGQ", new SystemValueInfo("QCFGMSGQ", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_MSG, VRM440, ResourceBundleLoader.getSystemValueText("QCFGMSGQ_DES")));
        SystemValueList.list.put("QMLTTHDACN", new SystemValueInfo("QMLTTHDACN", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM440, ResourceBundleLoader.getSystemValueText("QMLTTHDACN_DES")));

        SystemValueList.list.put("QMAXJOB",    new SystemValueInfo("QMAXJOB",    SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC,  VRM510, ResourceBundleLoader.getSystemValueText("QMAXJOB_DES")));
        SystemValueList.list.put("QMAXSPLF",   new SystemValueInfo("QMAXSPLF",   SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC,  VRM510, ResourceBundleLoader.getSystemValueText("QMAXSPLF_DES")));
        SystemValueList.list.put("QVFYOBJRST", new SystemValueInfo("QVFYOBJRST", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING,  GROUP_SEC,  VRM510, ResourceBundleLoader.getSystemValueText("QVFYOBJRST_DES")));
        SystemValueList.list.put("QSHRMEMCTL", new SystemValueInfo("QSHRMEMCTL", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING,  GROUP_SEC,  VRM510, ResourceBundleLoader.getSystemValueText("QSHRMEMCTL_DES")));
        SystemValueList.list.put("QLIBLCKLVL", new SystemValueInfo("QLIBLCKLVL", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING,  GROUP_LIBL, VRM510, ResourceBundleLoader.getSystemValueText("QLIBLCKLVL_DES")));
        SystemValueList.list.put("QPWDLVL", new SystemValueInfo("QPWDLVL", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC,  VRM510, ResourceBundleLoader.getSystemValueText("QPWDLVL_DES")));

        // V5R2 system values.
        SystemValueList.list.put("QSPLFACN",   new SystemValueInfo("QSPLFACN", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_ALC, VRM520, ResourceBundleLoader.getSystemValueText("QSPLFACN_DES")));
        SystemValueList.list.put("QDBFSTCCOL", new SystemValueInfo("QDBFSTCCOL", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, VRM520, ResourceBundleLoader.getSystemValueText("QDBFSTCCOL_DES")));

        // V5R3 system values.
        SystemValueList.list.put("QAUDLVL2",   new SystemValueInfo("QAUDLVL2", SERVER_TYPE_CHAR, 10, 99, TYPE_ARRAY, GROUP_SEC, VRM530, ResourceBundleLoader.getSystemValueText("QAUDLVL2_DES")));
        SystemValueList.list.put("QDATETIME",   new SystemValueInfo("QDATETIME", SERVER_TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_DATTIM, VRM530, ResourceBundleLoader.getSystemValueText("QDATETIME_DES")));
        // The format of the QDATETIME field is YYYYMMDDHHNNSSXXXXXX
        // where YYYY is the year, MM is the month, DD is the day,
        // HH is the hours, NN is the minutes, SS is the seconds,
        // and XXXXXX is the microseconds.

        SystemValueList.list.put("QENDJOBLMT",   new SystemValueInfo("QENDJOBLMT", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, VRM530, ResourceBundleLoader.getSystemValueText("QENDJOBLMT_DES")));
        SystemValueList.list.put("QSAVACCPTH",   new SystemValueInfo("QSAVACCPTH", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM530, ResourceBundleLoader.getSystemValueText("QSAVACCPTH_DES")));
        SystemValueList.list.put("QSCANFS",   new SystemValueInfo("QSCANFS", SERVER_TYPE_CHAR, 10, 20, TYPE_ARRAY, GROUP_SEC, VRM530, ResourceBundleLoader.getSystemValueText("QSCANFS_DES")));
        SystemValueList.list.put("QSCANFSCTL",   new SystemValueInfo("QSCANFSCTL", SERVER_TYPE_CHAR, 10, 20, TYPE_ARRAY, GROUP_SEC, VRM530, ResourceBundleLoader.getSystemValueText("QSCANFSCTL_DES")));
        SystemValueList.list.put("QTIMADJ",   new SystemValueInfo("QTIMADJ", SERVER_TYPE_CHAR, 28, 1, TYPE_STRING, GROUP_DATTIM, VRM530, ResourceBundleLoader.getSystemValueText("QTIMADJ_DES")));
        SystemValueList.list.put("QTIMZON",   new SystemValueInfo("QTIMZON", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_DATTIM, VRM530, ResourceBundleLoader.getSystemValueText("QTIMZON_DES")));
        SystemValueList.list.put("QTHDRSCAFN", new SystemValueInfo("QTHDRSCAFN", SERVER_TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, VRM530, ResourceBundleLoader.getSystemValueText("QTHDRSCAFN_DES")));
        SystemValueList.list.put("QTHDRSCADJ", new SystemValueInfo("QTHDRSCADJ", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM530, ResourceBundleLoader.getSystemValueText("QTHDRSCADJ_DES")));

        // V5R4 system values.
        SystemValueList.list.put("QALWJOBITP", new SystemValueInfo("QALWJOBITP", SERVER_TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, VRM540, ResourceBundleLoader.getSystemValueText("QALWJOBITP_DES")));
        SystemValueList.list.put("QLOGOUTPUT", new SystemValueInfo("QLOGOUTPUT", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_MSG, VRM540, ResourceBundleLoader.getSystemValueText("QLOGOUTPUT_DES")));

        // V6R1 system values.
        SystemValueList.list.put("QPWDRULES", new SystemValueInfo("QPWDRULES", SERVER_TYPE_CHAR, 15, 50, TYPE_ARRAY, GROUP_SEC, VRM610, ResourceBundleLoader.getSystemValueText("QPWDRULES_DES")));
        SystemValueList.list.put("QPWDCHGBLK", new SystemValueInfo("QPWDCHGBLK", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM610, ResourceBundleLoader.getSystemValueText("QPWDCHGBLK_DES")));
        SystemValueList.list.put("QPWDEXPWRN", new SystemValueInfo("QPWDEXPWRN", SERVER_TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC, VRM610, ResourceBundleLoader.getSystemValueText("QPWDEXPWRN_DES")));
        SystemValueList.list.put("QSSLPCL", new SystemValueInfo("QSSLPCL", SERVER_TYPE_CHAR, 10, 10, TYPE_ARRAY, GROUP_SEC, VRM610, ResourceBundleLoader.getSystemValueText("QSSLPCL_DES")));
        SystemValueList.list.put("QSSLCSLCTL", new SystemValueInfo("QSSLCSLCTL", SERVER_TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, VRM610, ResourceBundleLoader.getSystemValueText("QSSLCSLCTL_DES")));
        SystemValueList.list.put("QSSLCSL", new SystemValueInfo("QSSLCSL", SERVER_TYPE_CHAR, 40, 32, TYPE_ARRAY, GROUP_SEC, VRM610, ResourceBundleLoader.getSystemValueText("QSSLCSL_DES")));

        // Populate the group vectors.
        Enumeration elements = SystemValueList.list.elements();
        while (elements.hasMoreElements())
        {
            SystemValueInfo obj = (SystemValueInfo)elements.nextElement();
            SystemValueList.groups[obj.group_].addElement(obj);
            SystemValueList.groups[GROUP_ALL].addElement(obj);
        }
    }

    // The system where the system value list is located.
    private AS400 system_ = null;
    // Flag indicating if a connection been made.
    private boolean connected_ = false;

    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a SystemValueList object.  It creates a default SystemValueList object.  The <i>system</i> property must be set before attempting a connection.
     **/
    public SystemValueList()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValueList object.");
    }

    /**
     Constructs a SystemValueList object. It creates a SystemValueList instance that represents a list of system values on <i>system</i>.
     @param  system  The system that contains the system values.
     **/
    public SystemValueList(AS400 system)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing SystemValueGroup object, system: " + system);
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        system_ = system;
    }

    /**
     Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.  The PropertyChangeListener object is added to a list of PropertyChangeListeners managed by this SystemValue.  It can be removed with removePropertyChangeListener.
     @param  listener  The listener object.
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
     Adds a VetoableChangeListener.  The specified VetoableChangeListener's <b>vetoableChange</b> method will be called each time the value of any constrained property is changed.
     @param  listener  The listener object.
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

    // Makes a connection to the system.  The <i>system</i> property must be set before a connection can be made.
    private void connect() throws AS400SecurityException, IOException
    {
        if (system_ == null)
        {
            Trace.log(Trace.ERROR, "Cannot connect before setting system.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        connected_ = true;
    }

    /**
     Returns a set of SystemValue objects.  Returns the system values that belong to the system value group specified by <i>group</i> and sorted by name.
     @param  group  The system value group.
     @return  A Vector of {@link SystemValue SystemValue} objects.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public Vector getGroup(int group) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (!connected_) connect();

        if (group < 0 || group > GROUP_ALL)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'group' is not valid:", group);
            throw new ExtendedIllegalArgumentException("group", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Get the group vector.
        Vector infos = SystemValueList.groups[group];

        // QFRCCVNRST was in group SYSCTL in release V5R1M0 and below, and moved to group SEC in V5R2M0 and above.  By default we have it in group SEC, so if the IBM i release is V5R1M0 or below, we fix up the group here.
        if (system_.getVRM() <= VRM510)
        {
            switch (group)
            {
                case SystemValueList.GROUP_SEC:
                    // Make a copy of the group and remove QFRCCVNRST.
                    infos = (Vector)infos.clone();
                    infos.removeElement(SystemValueList.lookup("QFRCCVNRST"));
                    break;
                case SystemValueList.GROUP_SYSCTL:
                    // Make a copy of the group and add QFRCCVNRST.
                    infos = (Vector)infos.clone();
                    infos.addElement(SystemValueList.lookup("QFRCCVNRST"));
                    break;
            }
        }

        // Call retrieve() to get the data from the server and create a Vector of corresponding SystemValue objects.
        return sort(SystemValueUtility.retrieve(system_, infos.elements(), getGroupName(group), getGroupDescription(group)));
    }

    /**
     Returns the total number of possible groups.
     @return  The number of groups.
     **/
    public static int getGroupCount()
    {
        return GROUP_COUNT;
    }

    /**
     Returns the description for the specified system value group.
     @param  group  The system value group.
     @return  The description of the system value group.
     **/
    public static String getGroupDescription(int group)
    {
        if (group < 0 || group > GROUP_ALL)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'group' is not valid:", group);
            throw new ExtendedIllegalArgumentException("group", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        return GROUP_DESCRIPTIONS[group];
    }

    /**
     Returns the description for the specified system value group.
     @param  group  The system value group.
     @param  locale  The Locale used to load the appropriate language.
     @return  The description of the system value group.
     **/
    public static String getGroupDescription(int group, Locale locale)
    {
        if (group < 0 || group > GROUP_ALL)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'group' is not valid:", group);
            throw new ExtendedIllegalArgumentException("group", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (locale == null) return GROUP_DESCRIPTIONS[group];
        switch (group)
        {
            case 0: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_DESC", locale);
            case 1: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIM_DESC", locale);
            case 2: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_DESC", locale);
            case 3: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_DESC", locale);
            case 4: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_DESC", locale);
            case 5: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_DESC", locale);
            case 6: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_DESC", locale);
            case 7: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_DESC", locale);
            case 8: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_DESC", locale);
            case 9: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_DESC", locale);
        }
        return GROUP_DESCRIPTIONS[group];
    }

    /**
     Returns the name of the specified system value group.
     @param  group  The system value group.
     @return  The name of the system value group.
     **/
    public static String getGroupName(int group)
    {
        if (group < 0 || group > GROUP_ALL)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'group' is not valid:", group);
            throw new ExtendedIllegalArgumentException("group", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        return GROUP_NAMES[group];
    }

    /**
     Returns the name of the specified system value group.
     @param  group  The system value group.
     @param  locale  The Locale used to load the appropriate language.
     @return  The name of the system value group.
     **/
    public static String getGroupName(int group, Locale locale)
    {
        if (group < 0 || group > GROUP_ALL)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'group' is not valid:", group);
            throw new ExtendedIllegalArgumentException("group", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (locale == null) return GROUP_NAMES[group];
        switch (group)
        {
            case 0: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_NAME", locale);
            case 1: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIM_NAME", locale);
            case 2: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_NAME", locale);
            case 3: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_NAME", locale);
            case 4: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_NAME", locale);
            case 5: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_NAME", locale);
            case 6: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_NAME", locale);
            case 7: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_NAME", locale);
            case 8: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_NAME", locale);
            case 9: return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_NAME", locale);
        }
        return GROUP_NAMES[group];
    }

    /**
     Returns the system object representing the system on which the system value list exists.
     @return  The system object representing the system on which the system value list exists.  If the system has not been set, null is returned.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    // Returns a SystemValueInfo object for the system value specified by <i>name</i>.
    // @param  name  The name of the system value.
    // @return  The SystemValueInfo object corresponding to <i>name</i>.
    static SystemValueInfo lookup(String name)
    {
        SystemValueInfo obj = (SystemValueInfo)SystemValueList.list.get(name);
        if (obj == null)
        {
            Trace.log(Trace.ERROR, "System value was not found: " + name);
            throw new ExtendedIllegalArgumentException(name, ExtendedIllegalArgumentException.FIELD_NOT_FOUND);
        }
        return obj;
    }

    // Returns the locale-specific description for a SystemValue.
    static String lookupDescription(SystemValueInfo info, Locale locale)
    {
        return ResourceBundleLoader.getSystemValueText(info.name_.toUpperCase().trim() + "_DES", locale);
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
     Sets the system object representing the system on which the system value list exists.
     @param  system  The system object representing the system on which the system value list exists.
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
        if (connected_)
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

    // Recursively sorts vectors of SystemValue objects by name.
    // @param  vec  The objects to sort.
    // @return  The Vector of sorted objects.
    static Vector sort(Vector vec)
    {
        int len = vec.size();
        if (len < 2) return vec;
        SystemValue middle = (SystemValue)vec.elementAt(len / 2);
        Vector lessthan = new Vector(len / 2);
        Vector equalto = new Vector(len / 2);
        Vector greaterthan = new Vector(len / 2);
        Enumeration elements = vec.elements();
        while (elements.hasMoreElements())
        {
            SystemValue obj = (SystemValue)elements.nextElement();
            int comparison = obj.getName().compareTo(middle.getName());
            if (comparison < 0) lessthan.addElement(obj);
            else if (comparison > 0) greaterthan.addElement(obj);
            else equalto.addElement(obj);
        }
        lessthan.trimToSize();
        equalto.trimToSize();
        greaterthan.trimToSize();
        Vector lefthalf = sort(lessthan);
        Vector righthalf = sort(greaterthan);
        Vector whole = new Vector(lefthalf.size() + righthalf.size() + equalto.size());
        elements = lefthalf.elements();
        while (elements.hasMoreElements())
        {
            whole.addElement(elements.nextElement());
        }
        elements = equalto.elements();
        while (elements.hasMoreElements())
        {
            whole.addElement(elements.nextElement());
        }
        elements = righthalf.elements();
        while (elements.hasMoreElements())
        {
            whole.addElement(elements.nextElement());
        }
        return whole;
    }

/*    static String getName(Vector systemValues, int position)
    {
        return ((SystemValue)systemValues.elementAt(position)).info_.name_;
    }*/
}
