///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SVMRI.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.ListResourceBundle;

/**
Locale-specific objects for the IBM Toolbox for Java.
**/
public class SVMRI extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

    public Object[][] getContents()
    {
        return resources;
    }

    private static final Object[][] resources = {
           // #TRANNOTE Before you add a new resource, please make
           // #TRANNOTE sure you are not duplicating another.  The
           // #TRANNOTE goal is to keep the amount of translatable
           // #TRANNOTE text down.
           // #TRANNOTE
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE
           // #TRANNOTE

           // #TRANNOTE #####################################################
           // #TRANNOTE Text for system value's description.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts a short identifier to descript system
           // #TRANNOTE value, then a postfix _DES.
           // #TRANNOTE
        { "QABNORMSW_DES" , "Previous end of system indicator" },
        { "QACGLVL_DES" , "Accounting level" },
        { "QACTJOB_DES" , "Initial number of active jobs" },
        { "QADLACTJ_DES" , "Additional number of active jobs" },
        { "QADLSPLA_DES" , "Spooling control block additional storage" },
        { "QADLTOTJ_DES" , "Additional number of total jobs" },
        { "QALWOBJRST_DES" , "Allow object restore option" },
        { "QALWUSRDMN_DES" , "Allow user domain objects in libraries" },
        { "QASTLVL_DES" , "User assistance level" },
        { "QATNPGM_DES" , "Attention program" },
        { "QAUDCTL_DES" , "Auditing control" },
        { "QAUDENDACN_DES" , "Auditing end action" },
        { "QAUDFRCLVL_DES" , "Force auditing data" },
        { "QAUDLVL_DES" , "Security auditing level" },
        { "QAUTOCFG_DES" , "Autoconfigure devices" },
        { "QAUTORMT_DES" , "Autoconfigure of remote controllers" },
        { "QAUTOSPRPT_DES" , "Automatic system disabled reporting" },
        { "QAUTOVRT_DES" , "Autoconfigure virtual devices" },
        { "QBASACTLVL_DES" , "Base storage pool activity level" },
        { "QBASPOOL_DES" , "Base storage pool minimum size" },
        { "QBOOKPATH_DES" , "Book and bookshelf search path" },
        { "QCCSID_DES" , "Coded character set identifier" },
        { "QCENTURY_DES" , "Century" },
        { "QCFGMSGQ_DES" , "Configuration message queue" },              //@D1a
        { "QCHRID_DES" , "Graphic character set and code page" },
        { "QCHRIDCTL_DES" , "Character identifier control" },
        { "QCMNARB_DES" , "Comunication arbiters" },
        { "QCMNRCYLMT_DES" , "Communications recovery limits" },
        { "QCNTRYID_DES" , "Country identifier" },
        { "QCONSOLE_DES" , "Console name" },
        { "QCRTAUT_DES" , "Create default public authority" },
        { "QCRTOBJAUD_DES" , "Create object auditing" },
        { "QCTLSBSD_DES" , "Controlling subsystem" },
        { "QCURSYM_DES" , "Currency symbol" },
        { "QDATE_DES" , "System date" },
        { "QDATFMT_DES" , "Date format" },
        { "QDATSEP_DES" , "Date separator" },
        { "QDAY_DES" , "Day" },
        { "QDAYOFWEEK_DES" , "Day of week" },
        { "QDBRCVYWT_DES" , "Database recovery wait indicator" },
        { "QDECFMT_DES" , "Decimal format" },
        { "QDEVNAMING_DES" , "Device naming conventions" },
        { "QDEVRCYACN_DES" , "Device I/O error action" },
        { "QDSCJOBITV_DES" , "Time interval before disconnected jobs end" },
        { "QDSPSGNINF_DES" , "Sign-on display information control" },
        { "QDYNPTYADJ_DES" , "Dynamic priority adjustment" },
        { "QDYNPTYSCD_DES" , "Dynamic priority scheduler" },
        { "QFRCCVNRST_DES" , "Force conversion on restore" },
        { "QHOUR_DES" , "Hour of the day" },
        { "QHSTLOGSIZ_DES" , "Maximum history log records" },
        { "QIGC_DES" , "DBCS version installed indicator" },
        { "QIGCCDEFNT_DES" , "Double byte code font" },
        { "QIGCFNTSIZ_DES" , "Double byte coded font point size" },
        { "QINACTITV_DES" , "Inactive job time-out" },
        { "QINACTMSGQ_DES" , "Inactive job message queue" },
        { "QIPLDATTIM_DES" , "Date and time to automatically IPL" },
        { "QIPLSTS_DES" , "IPL status indicator" },
        { "QIPLTYPE_DES" , "Type of IPL to perform" },
        { "QJOBMSGQFL_DES" , "Job message queue full action" },
        { "QJOBMSGQMX_DES" , "Maximum size of job message queue" },
        { "QJOBMSGQSZ_DES" , "Job message queue initial size" },
        { "QJOBMSGQTL_DES" , "Job message queue maximum initial size" },
        { "QJOBSPLA_DES" , "Spooling control block initial size" },
        { "QKBDBUF_DES" , "Type ahead and/or attention key option" },
        { "QKBDTYPE_DES" , "Keyboard language character set" },
        { "QLANGID_DES" , "Language identifier" },
        { "QLEAPADJ_DES" , "Leap year adjustment" },
        { "QLIBLCKLVL_DES" , "Library locking level" },              //@D2a
        { "QLMTDEVSSN_DES" , "Limit device sessions" },
        { "QLMTSECOFR_DES" , "Limit security officer device access" },
        { "QLOCALE_DES" , "Locale path name" },
        { "QMAXACTLVL_DES" , "Maximum activity level of server" },
        { "QMAXJOB_DES" , "Maximum number of jobs" },              //@D2a
        { "QMAXSGNACN_DES" , "Action to take for failed signon attempts" },
        { "QMAXSIGN_DES" , "Maximum sign-on attempts allowed" },
        { "QMAXSPLF_DES" , "Maximum number of spooled files" },     //@D2a
        { "QMCHPOOL_DES" , "Machine storage pool size" },
        { "QMINUTE_DES" , "Minute of the hour" },
        { "QMLTTHDACN_DES" , "Multithreaded job action" },              //@D1a
        { "QMODEL_DES" , "System model number" },
        { "QMONTH_DES" , "Month of the year" },
        { "QPASTHRSVR_DES" , "Pass-through servers" },
        { "QPFRADJ_DES" , "Performance adjustment" },
        { "QPRBFTR_DES" , "Problem log filter" },
        { "QPRBHLDITV_DES" , "Problem log hold interval" },
        { "QPRCMLTTSK_DES" , "Processor multitasking" },
        { "QPRCFEAT_DES" , "Processor feature" },
        { "QPRTDEV_DES" , "Printer device description" },
        { "QPRTKEYFMT_DES" , "Print header and/or border information" },
        { "QPRTTXT_DES" , "Print text" },
        { "QPWDEXPITV_DES" , "Password expiration interval" },
        { "QPWDLMTAJC_DES" , "Limit adjacent digits in password" },
        { "QPWDLMTCHR_DES" , "Limit characters in password" },
        { "QPWDLMTREP_DES" , "Limit repeating characters in password" },
        { "QPWDLVL_DES"    , "Password Level" },                         //@D2a
        { "QPWDMAXLEN_DES" , "Maximum password length" },
        { "QPWDMINLEN_DES" , "Minimum password length" },
        { "QPWDPOSDIF_DES" , "Limit password character positions" },
        { "QPWDRQDDGT_DES" , "Require digit in password" },
        { "QPWDRQDDIF_DES" , "Duplicate password control" },
        { "QPWDVLDPGM_DES" , "Password validation program" },
        { "QPWRDWNLMT_DES" , "Maximum time for PWRDWNSYS *IMMED" },
        { "QPWRRSTIPL_DES" , "Automatic IPL after power restored" },
        { "QQRYDEGREE_DES" , "Parallel processing degree" },
        { "QQRYTIMLMT_DES" , "Query processing time limit" },
        { "QRCLSPLSTG_DES" , "Reclaim spool storage" },
        { "QRETSVRSEC_DES" , "Retain server security data" },
        { "QRMTIPL_DES" , "Remote power on and IPL" },
        { "QRMTSRVATR_DES" , "Remote Service attribute" },
        { "QRMTSIGN_DES" , "Remote sign-on control" },
        { "QSCPFCONS_DES" , "IPL action with console problem" },
        { "QSECOND_DES" , "Second of the minute" },
        { "QSECURITY_DES" , "System security level" },
        { "QSETJOBATR_DES" , "Set job attributes from locale" }, //@A2A
        { "QSFWERRLOG_DES" , "Software error logging" },
        { "QSHRMEMCTL_DES" , "Shared memory control" },              //@D2a
        { "QSPCENV_DES" , "Special environment" },
        { "QSRLNBR_DES" , "System serial number" },
        { "QSRTSEQ_DES" , "Sort sequence" },
        { "QSRVDMP_DES" , "Service dump control" },
        { "QSTGLOWACN_DES" , "Auxiliary storage lower limit action" },
        { "QSTGLOWLMT_DES" , "Auxiliary storage lower limit" },
        { "QSTRPRTWTR_DES" , "Start print writers at IPL" },
        { "QSTRUPPGM_DES" , "Startup program" },
        { "QSTSMSG_DES" , "Display status messages" },
        { "QSVRAUTITV_DES" , "Server authentication interval" }, //@A2A
        { "QSYSLIBL_DES" , "System part of the library list" },
        { "QTIME_DES" , "Time of day" },
        { "QTIMSEP_DES" , "Time separator" },
        { "QTOTJOB_DES" , "Initial total number of jobs" },
        { "QTSEPOOL_DES" , "Time slice end pool" },
        { "QUPSDLYTIM_DES" , "Uninterruptible power supply delay time" },
        { "QUPSMSGQ_DES" , "Uninterruptible power supply message queue" },
        { "QUSEADPAUT_DES" , "Use adopted authority" },
        { "QUSRLIBL_DES" , "User part of the library list" },
        { "QUTCOFFSET_DES" , "Coordinated universal time offset" },
        { "QVFYOBJRST_DES" , "Verify object on restore" },              //@D2a
        { "QYEAR_DES" , "Year" },
        { "ALRBCKFP_DES" , "Alert backup focal point" },
        { "ALRCTLD_DES" , "Alert controller" },
        { "ALRDFTFP_DES" , "Alert focal point" },
        { "ALRFTR_DES" , "Alert filter" },
        { "ALRHLDCNT_DES" , "Alert hold count" },
        { "ALRLOGSTS_DES" , "Alert logging status" },
        { "ALRPRIFP_DES" , "Alert primary focal point" },
        { "ALRRQSFP_DES" , "Alert focal point to request" },
        { "ALRSTS_DES" , "Alert status" },
        { "ALWADDCLU_DES" , "Allow add to cluster" },            //@D1a
        { "ALWANYNET_DES" , "Allow AnyNet support" },
        { "ALWHPRTWR_DES" , "Allow HPR tower support" },
        { "ALWVRTAPPN_DES" , "Allow virtual APPN support" },
        { "VRTAUTODEV_DES" , "Virtual controller autocreate device" },
        { "DDMACC_DES" , "DDM request access" },
        { "DFTCNNLST_DES" , "Default ISDN connection list" },
        { "DFTMODE_DES" , "Default mode" },
        { "DFTNETTYPE_DES" , "ISDN network type" },
        { "DTACPR_DES" , "Data compression" },
        { "DTACPRINM_DES" , "Intermediate data compression" }, //@A2C
        { "HPRPTHTMR_DES" , "HPR path switch timers" },
        { "JOBACN_DES" , "Job action" },
        { "LCLCPNAME_DES" , "Local control point" },
        { "LCLLOCNAME_DES" , "Local location" },
        { "LCLNETID_DES" , "Local network identifier" },
        { "MAXINTSSN_DES" , "Maximum sessions" },
        { "MAXHOP_DES" , "Maximum hop count" },
        { "MDMCNTRYID_DES" , "Modem country identifier" },            //@D1a
        { "MSGQ_DES" , "Message queue" },
        { "NETSERVER_DES" , "Server network identifier" },
        { "NODETYPE_DES" , "APPN node type" },
        { "NWSDOMAIN_DES" , "Network server domain" },
        { "OUTQ_DES" , "Output queue" },
        { "PNDSYSNAME_DES" , "Pending system name" },
        { "PCSACC_DES" , "Client Access" },
        { "RAR_DES" , "Addition resistance" },
        { "SYSNAME_DES" , "Current system name" },

           // #TRANNOTE #####################################################
           // #TRANNOTE Text for system values group's name.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_VALUE_GROUP_, a short
           // #TRANNOTE identifier to describe the system value group and
           // #TRANNOTE a postfix _NAME.
           // #TRANNOTE
        { "SYSTEM_VALUE_GROUP_ALL_NAME" , "All" },
        { "SYSTEM_VALUE_GROUP_ALC_NAME" , "Allocation" },
        { "SYSTEM_VALUE_GROUP_DATTIM_NAME" , "Date and Time" },
        { "SYSTEM_VALUE_GROUP_EDT_NAME" , "Editing" },
        { "SYSTEM_VALUE_GROUP_LIBL_NAME" , "Library List" },
        { "SYSTEM_VALUE_GROUP_MSG_NAME" , "Message and Logging" },
        { "SYSTEM_VALUE_GROUP_SEC_NAME" , "Security" },
        { "SYSTEM_VALUE_GROUP_STG_NAME" , "Storage" },
        { "SYSTEM_VALUE_GROUP_SYSCTL_NAME" , "System Control" },
        { "SYSTEM_VALUE_GROUP_NET_NAME" , "Network Attributes" },

        // #TRANNOTE #####################################################
           // #TRANNOTE Text for system values group's description.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_VALUE_GROUP_, a short
           // #TRANNOTE identifier to describe the system value group and
           // #TRANNOTE a postfix _DESC.
           // #TRANNOTE

        { "SYSTEM_VALUE_GROUP_ALL_DESC" , "All system values in the system" },
        { "SYSTEM_VALUE_GROUP_ALC_DESC" , "Allocation system values" },
        { "SYSTEM_VALUE_GROUP_DATTIM_DESC" , "Date and time system values" },
        { "SYSTEM_VALUE_GROUP_EDT_DESC" , "Editing system values" },
        { "SYSTEM_VALUE_GROUP_LIBL_DESC" , "Library list system values" },
        { "SYSTEM_VALUE_GROUP_MSG_DESC" , "Message and logging system values" },
        { "SYSTEM_VALUE_GROUP_SEC_DESC" , "Security system values" },
        { "SYSTEM_VALUE_GROUP_STG_DESC" , "Storage system values" },
        { "SYSTEM_VALUE_GROUP_SYSCTL_DESC" , "System control system values" },
        { "SYSTEM_VALUE_GROUP_NET_DESC" , "Network attributes of the system" },

           // #TRANNOTE #####################################################
           // #TRANNOTE Text for system values various items
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix SYSTEM_VALUE_, and a
           // #TRANNOTE description of the value.
           // #TRANNOTE
          { "SYSTEM_VALUE_USER_DEFINED" , "User Defined" }


    };


}
