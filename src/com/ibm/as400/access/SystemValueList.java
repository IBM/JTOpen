///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SystemValueList.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.io.IOException;
import java.net.UnknownHostException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.PropertyVetoException;
import java.io.ObjectInputStream;

public class SystemValueList implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


  /**
   * Constant indicating the system value's type in AS400 is BINARY.
  **/
  static final char AS400TYPE_BINARY = 'B';
  /**
   * Constant indicating the system value's type in AS400 is CHAR.
  **/
  static final char AS400TYPE_CHAR = 'C';


  /**
   * Constant indicating the system value's group is *ALC (Allocation).
  **/
  public static final int GROUP_ALC = 0;
  /**
   * Constant indicating the system value's group is *ALL (All).
  **/
  public static final int GROUP_ALL = 9;
  /**
   * Constant indicating the system value's group is *DATTIM (Date and Time).
  **/
  public static final int GROUP_DATTIM = 1;
  /**
   * Constant indicating the system value's group is *EDT (Editing).
  **/
  public static final int GROUP_EDT = 2;
  /**
   * Constant indicating the system value's group is *LIBL (Library List).
  **/
  public static final int GROUP_LIBL = 3;
  /**
   * Constant indicating the system value's group is *MSG (Message and Logging).
  **/
  public static final int GROUP_MSG = 4;
  /**
   * Constant indicating the system value's group is *NET (Net Attribute).
  **/
  public static final int GROUP_NET = 8;
  /**
   * Constant indicating the system value's group is *SEC (Security).
  **/
  public static final int GROUP_SEC = 5;
  /**
   * Constant indicating the system value's group is *STG (Storage).
  **/
  public static final int GROUP_STG = 6;
  /**
   * Constant indicating the system value's group is *SYSCTL (System control).
  **/
  public static final int GROUP_SYSCTL = 7;


  /**
   * Constant indicating the returned system value type is String[].
  **/
  public static final int TYPE_ARRAY = 4;
  /**
   * Constant indicating the returned system value type is Date.
  **/
  public static final int TYPE_DATE = 5;
  /**
   * Constant indicating the returned system value type is BigDecimal.
  **/
  public static final int TYPE_DECIMAL = 2;
  /**
   * Constant indicating the returned system value type is Integer.
  **/
  public static final int TYPE_INTEGER = 3;
  /**
   * Constant indicating the returned system value type is String.
  **/
  public static final int TYPE_STRING = 1;



  /**
   * The total number of groups.
  **/
  static final int groupCount_ = 10;


  /**
    * The MRI for the group names.
  **/
  static final String[] groupNames_ = new String[]
  {
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIM_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_NAME")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_NAME")).trim(),
  };

  /**
   * The MRI for the group descriptions.
  **/
  static final String[] groupDescriptions_ = new String[]
  {
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIM_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_DESC")).trim(),
    ((String)ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_DESC")).trim(),
  };

  private static Hashtable list_ = null; // The list of all SystemValueInfo objects
  /*@C0D private*/ static Hashtable groups_ = null; // Provided for convenient lookup of system value groups

  // Initialize the hashtables
  static
  {
    // For V4R2, there are 125 system values and 35 network attributes.
    // For V4R3, there are an additional 5 system values.
    // For V4R4, there are 2 additional system values and 2 additional network attributes.
    // @D2 For V5R2, there are xx additional system values.

    list_ = new Hashtable(169); // There are at least 169 system values @C1C
    groups_ = new Hashtable(groupCount_); // There are 10 groups @D4C
    Vector[] groupVector = new Vector[groupCount_]; // The group type is the index into this array of Vectors @D4C
                                           // Each vector contains a list of system values that belong to that group.
    // Do V4R2 system values
    if (Trace.isTraceOn() && Trace.isTraceDiagnosticOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Populating system value table...");
    }

    int vrm320 = AS400.generateVRM(3, 2, 0);
    int vrm420 = AS400.generateVRM(4, 2, 0);
    int vrm430 = AS400.generateVRM(4, 3, 0);
    int vrm440 = AS400.generateVRM(4, 4, 0); //@B0A
    int vrm510 = AS400.generateVRM(5, 1, 0); //@D2a

    // network attributes
    list_.put("ALRBCKFP", new SystemValueInfo("ALRBCKFP", AS400TYPE_CHAR, 8, 2, TYPE_ARRAY, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRBCKFP_DES")).trim()));
    list_.put("ALRCTLD", new SystemValueInfo("ALRCTLD", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRCTLD_DES")).trim()));
    list_.put("ALRDFTFP", new SystemValueInfo("ALRDFTFP", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRDFTFP_DES")).trim()));
    list_.put("ALRFTR", new SystemValueInfo("ALRFTR", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRFTR_DES")).trim()));
    list_.put("ALRHLDCNT", new SystemValueInfo("ALRHLDCNT", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRHLDCNT_DES")).trim()));
    list_.put("ALRLOGSTS", new SystemValueInfo("ALRLOGSTS", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRLOGSTS_DES")).trim()));
    list_.put("ALRPRIFP", new SystemValueInfo("ALRPRIFP", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRPRIFP_DES")).trim()));
    list_.put("ALRRQSFP", new SystemValueInfo("ALRRQSFP", AS400TYPE_CHAR, 8, 2, TYPE_ARRAY, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRRQSFP_DES")).trim()));
    list_.put("ALRSTS", new SystemValueInfo("ALRSTS", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALRSTS_DES")).trim()));
    list_.put("ALWANYNET", new SystemValueInfo("ALWANYNET", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALWANYNET_DES")).trim()));
    list_.put("ALWHPRTWR", new SystemValueInfo("ALWHPRTWR", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALWHPRTWR_DES")).trim()));
    list_.put("ALWVRTAPPN", new SystemValueInfo("ALWVRTAPPN", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("ALWVRTAPPN_DES")).trim()));
    list_.put("DDMACC", new SystemValueInfo("DDMACC", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("DDMACC_DES")).trim()));
    list_.put("DFTCNNLST", new SystemValueInfo("DFTCNNLST", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("DFTCNNLST_DES")).trim()));
    list_.put("DFTMODE", new SystemValueInfo("DFTMODE", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("DFTMODE_DES")).trim()));
    list_.put("DFTNETTYPE", new SystemValueInfo("DFTNETTYPE", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("DFTNETTYPE_DES")).trim()));
    list_.put("DTACPR", new SystemValueInfo("DTACPR", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("DTACPR_DES")).trim()));
    list_.put("DTACPRINM", new SystemValueInfo("DTACPRINM", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("DTACPRINM_DES")).trim()));
    list_.put("HPRPTHTMR", new SystemValueInfo("HPRPTHTMR", AS400TYPE_CHAR, 10, 4, TYPE_ARRAY, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("HPRPTHTMR_DES")).trim()));
    list_.put("JOBACN", new SystemValueInfo("JOBACN", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("JOBACN_DES")).trim()));
    list_.put("LCLCPNAME", new SystemValueInfo("LCLCPNAME", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("LCLCPNAME_DES")).trim()));
    list_.put("LCLLOCNAME", new SystemValueInfo("LCLLOCNAME", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("LCLLOCNAME_DES")).trim()));
    list_.put("LCLNETID", new SystemValueInfo("LCLNETID", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("LCLNETID_DES")).trim()));
    list_.put("MAXHOP", new SystemValueInfo("MAXHOP", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("MAXHOP_DES")).trim()));
    list_.put("MAXINTSSN", new SystemValueInfo("MAXINTSSN", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("MAXINTSSN_DES")).trim()));
    list_.put("MSGQ", new SystemValueInfo("MSGQ", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("MSGQ_DES")).trim()));
    list_.put("NETSERVER", new SystemValueInfo("NETSERVER", AS400TYPE_CHAR, 17, 5, TYPE_ARRAY, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("NETSERVER_DES")).trim()));
    list_.put("NODETYPE", new SystemValueInfo("NODETYPE", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("NODETYPE_DES")).trim()));
    list_.put("NWSDOMAIN", new SystemValueInfo("NWSDOMAIN", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("NWSDOMAIN_DES")).trim()));
    list_.put("OUTQ", new SystemValueInfo("OUTQ", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("OUTQ_DES")).trim()));
    list_.put("PCSACC", new SystemValueInfo("PCSACC", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("PCSACC_DES")).trim()));
    list_.put("PNDSYSNAME", new SystemValueInfo("PNDSYSNAME", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("PNDSYSNAME_DES")).trim(),true));
    list_.put("RAR", new SystemValueInfo("RAR", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("RAR_DES")).trim()));
    list_.put("SYSNAME", new SystemValueInfo("SYSNAME", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("SYSNAME_DES")).trim()));
    list_.put("VRTAUTODEV", new SystemValueInfo("VRTAUTODEV", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_NET, vrm420, ((String)ResourceBundleLoader.getSystemValueText("VRTAUTODEV_DES")).trim()));

    // V4R4 network attributes
    list_.put("ALWADDCLU", new SystemValueInfo("ALWADDCLU", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_NET, vrm440, ((String)ResourceBundleLoader.getSystemValueText("ALWADDCLU_DES")).trim() )); //@B0A @D1c
    list_.put("MDMCNTRYID", new SystemValueInfo("MDMCNTRYID", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_NET, vrm440, ((String)ResourceBundleLoader.getSystemValueText("MDMCNTRYID_DES")).trim())); //@B0A @D1c

    // system values
    list_.put("QABNORMSW", new SystemValueInfo("QABNORMSW", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QABNORMSW_DES")).trim(), true));
    list_.put("QACGLVL", new SystemValueInfo("QACGLVL", AS400TYPE_CHAR, 10, 8, TYPE_ARRAY, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QACGLVL_DES")).trim()));
    list_.put("QACTJOB", new SystemValueInfo("QACTJOB", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QACTJOB_DES")).trim()));
    list_.put("QADLACTJ", new SystemValueInfo("QADLACTJ", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QADLACTJ_DES")).trim()));
    list_.put("QADLSPLA", new SystemValueInfo("QADLSPLA", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QADLSPLA_DES")).trim()));
    list_.put("QADLTOTJ", new SystemValueInfo("QADLTOTJ", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QADLTOTJ_DES")).trim()));
    list_.put("QALWOBJRST", new SystemValueInfo("QALWOBJRST", AS400TYPE_CHAR, 10, 15, TYPE_ARRAY, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QALWOBJRST_DES")).trim()));
    list_.put("QALWUSRDMN", new SystemValueInfo("QALWUSRDMN", AS400TYPE_CHAR, 10, 50, TYPE_ARRAY, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QALWUSRDMN_DES")).trim()));
    list_.put("QASTLVL", new SystemValueInfo("QASTLVL", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QASTLVL_DES")).trim()));
    list_.put("QATNPGM", new SystemValueInfo("QATNPGM", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QATNPGM_DES")).trim()));
    list_.put("QAUDCTL", new SystemValueInfo("QAUDCTL", AS400TYPE_CHAR, 10, 5, TYPE_ARRAY, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUDCTL_DES")).trim()));
    list_.put("QAUDENDACN", new SystemValueInfo("QAUDENDACN", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUDENDACN_DES")).trim()));
    list_.put("QAUDFRCLVL", new SystemValueInfo("QAUDFRCLVL", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUDFRCLVL_DES")).trim()));
    list_.put("QAUDLVL", new SystemValueInfo("QAUDLVL", AS400TYPE_CHAR, 10, 16, TYPE_ARRAY, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUDLVL_DES")).trim()));
    list_.put("QAUTOCFG", new SystemValueInfo("QAUTOCFG", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUTOCFG_DES")).trim()));
    list_.put("QAUTORMT", new SystemValueInfo("QAUTORMT", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUTORMT_DES")).trim()));
    list_.put("QAUTOSPRPT", new SystemValueInfo("QAUTOSPRPT", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUTOSPRPT_DES")).trim()));
    list_.put("QAUTOVRT", new SystemValueInfo("QAUTOVRT", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QAUTOVRT_DES")).trim()));
    list_.put("QBASACTLVL", new SystemValueInfo("QBASACTLVL", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QBASACTLVL_DES")).trim()));
    list_.put("QBASPOOL", new SystemValueInfo("QBASPOOL", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QBASPOOL_DES")).trim()));
    list_.put("QBOOKPATH", new SystemValueInfo("QBOOKPATH", AS400TYPE_CHAR, 63, 5, TYPE_ARRAY, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QBOOKPATH_DES")).trim()));
    list_.put("QCCSID", new SystemValueInfo("QCCSID", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCCSID_DES")).trim()));
    list_.put("QCENTURY", new SystemValueInfo("QCENTURY", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCENTURY_DES")).trim()));
    list_.put("QCHRID", new SystemValueInfo("QCHRID", AS400TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCHRID_DES")).trim()));
    list_.put("QCMNARB", new SystemValueInfo("QCMNARB", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCMNARB_DES")).trim()));
    list_.put("QCMNRCYLMT", new SystemValueInfo("QCMNRCYLMT", AS400TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCMNRCYLMT_DES")).trim()));
    list_.put("QCNTRYID", new SystemValueInfo("QCNTRYID", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCNTRYID_DES")).trim()));
    list_.put("QCONSOLE", new SystemValueInfo("QCONSOLE", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCONSOLE_DES")).trim(), true));
    list_.put("QCRTAUT", new SystemValueInfo("QCRTAUT", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCRTAUT_DES")).trim()));
    list_.put("QCRTOBJAUD", new SystemValueInfo("QCRTOBJAUD", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCRTOBJAUD_DES")).trim()));
    list_.put("QCTLSBSD", new SystemValueInfo("QCTLSBSD", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCTLSBSD_DES")).trim()));
    list_.put("QCURSYM", new SystemValueInfo("QCURSYM", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QCURSYM_DES")).trim()));
    list_.put("QDATE", new SystemValueInfo("QDATE", AS400TYPE_CHAR, 7, 1, TYPE_DATE, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDATE_DES")).trim()));
    list_.put("QDATFMT", new SystemValueInfo("QDATFMT", AS400TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_EDT, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDATFMT_DES")).trim()));
    list_.put("QDATSEP", new SystemValueInfo("QDATSEP", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDATSEP_DES")).trim()));
    list_.put("QDAY", new SystemValueInfo("QDAY", AS400TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDAY_DES")).trim()));
    list_.put("QDAYOFWEEK", new SystemValueInfo("QDAYOFWEEK", AS400TYPE_CHAR, 4, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDAYOFWEEK_DES")).trim(), true));
    list_.put("QDBRCVYWT", new SystemValueInfo("QDBRCVYWT", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDBRCVYWT_DES")).trim()));
    list_.put("QDECFMT", new SystemValueInfo("QDECFMT", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDECFMT_DES")).trim()));
    list_.put("QDEVNAMING", new SystemValueInfo("QDEVNAMING", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDEVNAMING_DES")).trim()));
    list_.put("QDEVRCYACN", new SystemValueInfo("QDEVRCYACN", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDEVRCYACN_DES")).trim()));
    list_.put("QDSCJOBITV", new SystemValueInfo("QDSCJOBITV", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDSCJOBITV_DES")).trim()));
    list_.put("QDSPSGNINF", new SystemValueInfo("QDSPSGNINF", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDSPSGNINF_DES")).trim()));
    list_.put("QDYNPTYSCD", new SystemValueInfo("QDYNPTYSCD", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QDYNPTYSCD_DES")).trim()));
    list_.put("QFRCCVNRST", new SystemValueInfo("QFRCCVNRST", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QFRCCVNRST_DES")).trim()));
    list_.put("QHOUR", new SystemValueInfo("QHOUR", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QHOUR_DES")).trim()));
    list_.put("QHSTLOGSIZ", new SystemValueInfo("QHSTLOGSIZ", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QHSTLOGSIZ_DES")).trim()));
    list_.put("QIGC", new SystemValueInfo("QIGC", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QIGC_DES")).trim(), true));
    list_.put("QIGCCDEFNT", new SystemValueInfo("QIGCCDEFNT", AS400TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QIGCCDEFNT_DES")).trim()));
    list_.put("QINACTITV", new SystemValueInfo("QINACTITV", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QINACTITV_DES")).trim()));
    list_.put("QINACTMSGQ", new SystemValueInfo("QINACTMSGQ", AS400TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QINACTMSGQ_DES")).trim()));
    list_.put("QIPLDATTIM", new SystemValueInfo("QIPLDATTIM", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QIPLDATTIM_DES")).trim()));
    list_.put("QIPLSTS", new SystemValueInfo("QIPLSTS", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QIPLSTS_DES")).trim(), true));
    list_.put("QIPLTYPE", new SystemValueInfo("QIPLTYPE", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QIPLTYPE_DES")).trim()));
    list_.put("QJOBMSGQFL", new SystemValueInfo("QJOBMSGQFL", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QJOBMSGQFL_DES")).trim()));
    list_.put("QJOBMSGQMX", new SystemValueInfo("QJOBMSGQMX", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QJOBMSGQMX_DES")).trim()));
    list_.put("QJOBMSGQSZ", new SystemValueInfo("QJOBMSGQSZ", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QJOBMSGQSZ_DES")).trim()));
    list_.put("QJOBMSGQTL", new SystemValueInfo("QJOBMSGQTL", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QJOBMSGQTL_DES")).trim()));
    list_.put("QJOBSPLA", new SystemValueInfo("QJOBSPLA", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QJOBSPLA_DES")).trim()));
    list_.put("QKBDBUF", new SystemValueInfo("QKBDBUF", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QKBDBUF_DES")).trim()));
    list_.put("QKBDTYPE", new SystemValueInfo("QKBDTYPE", AS400TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QKBDTYPE_DES")).trim()));
    list_.put("QLANGID", new SystemValueInfo("QLANGID", AS400TYPE_CHAR, 3, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QLANGID_DES")).trim()));
    list_.put("QLEAPADJ", new SystemValueInfo("QLEAPADJ", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QLEAPADJ_DES")).trim()));
    list_.put("QLMTDEVSSN", new SystemValueInfo("QLMTDEVSSN", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QLMTDEVSSN_DES")).trim()));
    list_.put("QLMTSECOFR", new SystemValueInfo("QLMTSECOFR", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QLMTSECOFR_DES")).trim()));
    list_.put("QLOCALE", new SystemValueInfo("QLOCALE", AS400TYPE_CHAR, 2080, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QLOCALE_DES")).trim())); // This is size 2080 because of data returned on API. Actual size for path name is 1024 chars.
    list_.put("QMAXACTLVL", new SystemValueInfo("QMAXACTLVL", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QMAXACTLVL_DES")).trim()));
    list_.put("QMAXSGNACN", new SystemValueInfo("QMAXSGNACN", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QMAXSGNACN_DES")).trim()));
    list_.put("QMAXSIGN", new SystemValueInfo("QMAXSIGN", AS400TYPE_CHAR, 6, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QMAXSIGN_DES")).trim()));
    list_.put("QMCHPOOL", new SystemValueInfo("QMCHPOOL", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_STG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QMCHPOOL_DES")).trim()));
    list_.put("QMINUTE", new SystemValueInfo("QMINUTE", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QMINUTE_DES")).trim()));
    list_.put("QMODEL", new SystemValueInfo("QMODEL", AS400TYPE_CHAR, 4, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QMODEL_DES")).trim(),true));
    list_.put("QMONTH", new SystemValueInfo("QMONTH", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QMONTH_DES")).trim()));
    list_.put("QPASTHRSVR", new SystemValueInfo("QPASTHRSVR", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPASTHRSVR_DES")).trim()));
    list_.put("QPFRADJ", new SystemValueInfo("QPFRADJ", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPFRADJ_DES")).trim()));
    list_.put("QPRBFTR", new SystemValueInfo("QPRBFTR", AS400TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPRBFTR_DES")).trim()));
    list_.put("QPRBHLDITV", new SystemValueInfo("QPRBHLDITV", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPRBHLDITV_DES")).trim()));
    list_.put("QPRTDEV", new SystemValueInfo("QPRTDEV", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPRTDEV_DES")).trim()));
    list_.put("QPRTKEYFMT", new SystemValueInfo("QPRTKEYFMT", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPRTKEYFMT_DES")).trim()));
    list_.put("QPRTTXT", new SystemValueInfo("QPRTTXT", AS400TYPE_CHAR, 30, 1, TYPE_STRING, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPRTTXT_DES")).trim()));
    list_.put("QPWDEXPITV", new SystemValueInfo("QPWDEXPITV", AS400TYPE_CHAR, 6, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDEXPITV_DES")).trim()));
    list_.put("QPWDLMTAJC", new SystemValueInfo("QPWDLMTAJC", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDLMTAJC_DES")).trim()));
    list_.put("QPWDLMTCHR", new SystemValueInfo("QPWDLMTCHR", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDLMTCHR_DES")).trim()));
    list_.put("QPWDLMTREP", new SystemValueInfo("QPWDLMTREP", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDLMTREP_DES")).trim()));
    list_.put("QPWDMAXLEN", new SystemValueInfo("QPWDMAXLEN", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDMAXLEN_DES")).trim()));
    list_.put("QPWDMINLEN", new SystemValueInfo("QPWDMINLEN", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDMINLEN_DES")).trim()));
    list_.put("QPWDPOSDIF", new SystemValueInfo("QPWDPOSDIF", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDPOSDIF_DES")).trim()));
    list_.put("QPWDRQDDGT", new SystemValueInfo("QPWDRQDDGT", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDRQDDGT_DES")).trim()));
    list_.put("QPWDRQDDIF", new SystemValueInfo("QPWDRQDDIF", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDRQDDIF_DES")).trim()));
    list_.put("QPWDVLDPGM", new SystemValueInfo("QPWDVLDPGM", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWDVLDPGM_DES")).trim()));
    list_.put("QPWRDWNLMT", new SystemValueInfo("QPWRDWNLMT", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWRDWNLMT_DES")).trim()));
    list_.put("QPWRRSTIPL", new SystemValueInfo("QPWRRSTIPL", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QPWRRSTIPL_DES")).trim()));
    list_.put("QQRYDEGREE", new SystemValueInfo("QQRYDEGREE", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QQRYDEGREE_DES")).trim()));
    list_.put("QQRYTIMLMT", new SystemValueInfo("QQRYTIMLMT", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QQRYTIMLMT_DES")).trim()));
    list_.put("QRCLSPLSTG", new SystemValueInfo("QRCLSPLSTG", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QRCLSPLSTG_DES")).trim()));
    list_.put("QRETSVRSEC", new SystemValueInfo("QRETSVRSEC", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QRETSVRSEC_DES")).trim()));
    list_.put("QRMTIPL", new SystemValueInfo("QRMTIPL", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QRMTIPL_DES")).trim()));
    list_.put("QRMTSIGN", new SystemValueInfo("QRMTSIGN", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QRMTSIGN_DES")).trim()));
    list_.put("QRMTSRVATR", new SystemValueInfo("QRMTSRVATR", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QRMTSRVATR_DES")).trim()));
    list_.put("QSCPFCONS", new SystemValueInfo("QSCPFCONS", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSCPFCONS_DES")).trim()));
    list_.put("QSECOND", new SystemValueInfo("QSECOND", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSECOND_DES")).trim()));
    list_.put("QSECURITY", new SystemValueInfo("QSECURITY", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSECURITY_DES")).trim()));
    list_.put("QSETJOBATR", new SystemValueInfo("QSETJOBATR", AS400TYPE_CHAR, 10, 16, TYPE_ARRAY, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSETJOBATR_DES")).trim()));
    list_.put("QSFWERRLOG", new SystemValueInfo("QSFWERRLOG", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSFWERRLOG_DES")).trim()));
    list_.put("QSPCENV", new SystemValueInfo("QSPCENV", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSPCENV_DES")).trim()));
    list_.put("QSRLNBR", new SystemValueInfo("QSRLNBR", AS400TYPE_CHAR, 8, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSRLNBR_DES")).trim(),true));
    list_.put("QSRTSEQ", new SystemValueInfo("QSRTSEQ", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSRTSEQ_DES")).trim()));
    list_.put("QSRVDMP", new SystemValueInfo("QSRVDMP", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSRVDMP_DES")).trim()));
    list_.put("QSTGLOWACN", new SystemValueInfo("QSTGLOWACN", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_STG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSTGLOWACN_DES")).trim()));
    list_.put("QSTGLOWLMT", new SystemValueInfo("QSTGLOWLMT", AS400TYPE_BINARY, 7, 4, 1, TYPE_DECIMAL, GROUP_STG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSTGLOWLMT_DES")).trim()));
    list_.put("QSTRPRTWTR", new SystemValueInfo("QSTRPRTWTR", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSTRPRTWTR_DES")).trim(), true));
    list_.put("QSTRUPPGM", new SystemValueInfo("QSTRUPPGM", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSTRUPPGM_DES")).trim()));
    list_.put("QSTSMSG", new SystemValueInfo("QSTSMSG", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_MSG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSTSMSG_DES")).trim()));
    list_.put("QSVRAUTITV", new SystemValueInfo("QSVRAUTITV", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSVRAUTITV_DES")).trim()));
    list_.put("QSYSLIBL", new SystemValueInfo("QSYSLIBL", AS400TYPE_CHAR, 10, 15, TYPE_ARRAY, GROUP_LIBL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QSYSLIBL_DES")).trim()));
    list_.put("QTIME", new SystemValueInfo("QTIME", AS400TYPE_CHAR, 9, 1, TYPE_DATE, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QTIME_DES")).trim()));
    list_.put("QTIMSEP", new SystemValueInfo("QTIMSEP", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_EDT, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QTIMSEP_DES")).trim()));
    list_.put("QTOTJOB", new SystemValueInfo("QTOTJOB", AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QTOTJOB_DES")).trim()));
    list_.put("QTSEPOOL", new SystemValueInfo("QTSEPOOL", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_STG, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QTSEPOOL_DES")).trim()));
    list_.put("QUPSDLYTIM", new SystemValueInfo("QUPSDLYTIM", AS400TYPE_CHAR, 10, 2, TYPE_ARRAY, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QUPSDLYTIM_DES")).trim()));
    list_.put("QUPSMSGQ", new SystemValueInfo("QUPSMSGQ", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_SYSCTL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QUPSMSGQ_DES")).trim()));
    list_.put("QUSEADPAUT", new SystemValueInfo("QUSEADPAUT", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SEC, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QUSEADPAUT_DES")).trim()));
    list_.put("QUSRLIBL", new SystemValueInfo("QUSRLIBL", AS400TYPE_CHAR, 10, 25, TYPE_ARRAY, GROUP_LIBL, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QUSRLIBL_DES")).trim()));
    list_.put("QUTCOFFSET", new SystemValueInfo("QUTCOFFSET", AS400TYPE_CHAR, 5, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QUTCOFFSET_DES")).trim()));
    list_.put("QYEAR", new SystemValueInfo("QYEAR", AS400TYPE_CHAR, 2, 1, TYPE_STRING, GROUP_DATTIM, vrm420, ((String)ResourceBundleLoader.getSystemValueText("QYEAR_DES")).trim()));

    // V4R3 system values
    list_.put("QCHRIDCTL", new SystemValueInfo("QCHRIDCTL", AS400TYPE_CHAR, 10, 1, TYPE_STRING, GROUP_SYSCTL, vrm430, ((String)ResourceBundleLoader.getSystemValueText("QCHRIDCTL_DES")).trim()));
    list_.put("QDYNPTYADJ", new SystemValueInfo("QDYNPTYADJ", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm430, ((String)ResourceBundleLoader.getSystemValueText("QDYNPTYADJ_DES")).trim()));
    list_.put("QIGCFNTSIZ", new SystemValueInfo("QIGCFNTSIZ", AS400TYPE_BINARY, 4, 1, 1, TYPE_DECIMAL, GROUP_SYSCTL, vrm430, ((String)ResourceBundleLoader.getSystemValueText("QIGCFNTSIZ_DES")).trim()));
    list_.put("QPRCMLTTSK", new SystemValueInfo("QPRCMLTTSK", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm430, ((String)ResourceBundleLoader.getSystemValueText("QPRCMLTTSK_DES")).trim()));
    list_.put("QPRCFEAT", new SystemValueInfo("QPRCFEAT", AS400TYPE_CHAR, 4, 1, TYPE_STRING, GROUP_SYSCTL, vrm430, ((String)ResourceBundleLoader.getSystemValueText("QPRCFEAT_DES")).trim(),true));

    // V4R4 system values
    list_.put("QCFGMSGQ", new SystemValueInfo("QCFGMSGQ", AS400TYPE_CHAR, 20, 1, TYPE_STRING, GROUP_MSG, vrm440, ((String)ResourceBundleLoader.getSystemValueText("QCFGMSGQ_DES")).trim())); //@B0A D1C
    list_.put("QMLTTHDACN", new SystemValueInfo("QMLTTHDACN", AS400TYPE_CHAR, 1, 1, TYPE_STRING, GROUP_SYSCTL, vrm440, ((String)ResourceBundleLoader.getSystemValueText("QMLTTHDACN_DES")).trim())); //@B0A D1C

    //@D5M
    list_.put("QMAXJOB",    new SystemValueInfo("QMAXJOB",    AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC,  vrm510, ((String)ResourceBundleLoader.getSystemValueText("QMAXJOB_DES")).trim()));     //@D2a
    list_.put("QMAXSPLF",   new SystemValueInfo("QMAXSPLF",   AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_ALC,  vrm510, ((String)ResourceBundleLoader.getSystemValueText("QMAXSPLF_DES")).trim()));    //@D2a
    list_.put("QVFYOBJRST", new SystemValueInfo("QVFYOBJRST", AS400TYPE_CHAR,   1, 1, TYPE_STRING,  GROUP_SEC,  vrm510, ((String)ResourceBundleLoader.getSystemValueText("QVFYOBJRST_DES")).trim()));  //@D2a
    list_.put("QSHRMEMCTL", new SystemValueInfo("QSHRMEMCTL", AS400TYPE_CHAR,   1, 1, TYPE_STRING,  GROUP_SEC,  vrm510, ((String)ResourceBundleLoader.getSystemValueText("QSHRMEMCTL_DES")).trim()));  //@D2a
    list_.put("QLIBLCKLVL", new SystemValueInfo("QLIBLCKLVL", AS400TYPE_CHAR,   1, 1, TYPE_STRING,  GROUP_LIBL, vrm510, ((String)ResourceBundleLoader.getSystemValueText("QLIBLCKLVL_DES")).trim()));  //@D2a
    list_.put("QPWDLVL",    new SystemValueInfo("QPWDLVL",    AS400TYPE_BINARY, 4, 1, TYPE_INTEGER, GROUP_SEC,  vrm510, ((String)ResourceBundleLoader.getSystemValueText("QPWDLVL_DES")).trim()));     //@D2a

    // Create the group vectors
    for (int i=0; i<groupVector.length; ++i) //@D4C
    {
      groupVector[i] = new Vector();
    }
    Enumeration enum = list_.elements();
    while (enum.hasMoreElements())
    {
      SystemValueInfo obj = (SystemValueInfo)enum.nextElement();
      groupVector[obj.group_].addElement(obj);
      groupVector[GROUP_ALL].addElement(obj); //@D3A
    }

    // Populate the group hashtable
    for (int i=0; i<groupVector.length; ++i) //@D4C
    {
      groups_.put(getGroupName(i), groupVector[i]);
    }
  }   // end static initialization


  private AS400 system_ = null;  // The AS/400 this SystemValueList belongs to.
  private boolean connected_ = false; // Has a connection been made yet?

  transient PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
  transient VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);


  /**
  Constructs a SystemValueList object.
  It creates a default SystemValueList object. The <i>system</i> property
  must be set before attempting a connection.
  **/
  public SystemValueList()
  {
  }


  /**
  Constructs a SystemValueList object.
  It creates a SystemValueList instance that represents a list of system
  values on <i>system</i>.
    @param system The AS/400 that contains the system values.
  **/
  public SystemValueList(AS400 system)
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    system_ = system;
  }


  /**
  Adds a PropertyChangeListener.
    @see #removePropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@C1A
    changes_.addPropertyChangeListener(listener);
  }


  /**
  Adds the VetoableChangeListener.
    @see #removeVetoableChangeListener
    @param listener The VetoableChangeListener.
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@C1A
    vetos_.addVetoableChangeListener(listener);
  }


  /**
  Makes a connection to the AS/400.
  The <i>system</i> property must be set before a connection can be made.
  **/
  private void connect()
      throws AS400SecurityException,
             IOException,
             UnknownHostException
  {
    if (system_ == null)
    {
      throw new ExtendedIllegalStateException("system",
          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

/*@D4D    // Need to remove all of the system values that aren't supported
    // by our AS400's release level.
    int vrm = system_.getVRM();
    Enumeration enum = list_.elements();
    while (enum.hasMoreElements())
    {
      SystemValueInfo obj = (SystemValueInfo)enum.nextElement();
      if (obj.release_ > vrm)
      {
        // Remove the sysval from the list of all sysvals
        list_.remove(obj.name_);
        // Remove the sysval from the list of sysvals of its group
        Vector vec = (Vector)groups_.get(getGroupName(obj.group_));
        vec.removeElement(obj);
      }
    }
*///@D4D
    connected_ = true;
  }


  /**
  Returns a set of SystemValue objects.
  Returns the system values that belong to the system value
  group specified by <i>group</i> and sorted by name.
    @param group The system value group.
    @return A Vector of SystemValue objects.
    @exception AS400SecurityException If a security or authority error occurs.
    @exception ErrorCompletingRequestException If an error occurs before the request is completed.
    @exception InterruptedException If this thread is interrupted.
    @exception IOException If an error occurs while communicating with the AS/400.
    @exception ObjectDoesNotExistException If the AS/400 object does not exist.
    @exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public Vector getGroup(int group)
            throws AS400SecurityException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException,
//@C0D                   PropertyVetoException,
                   UnknownHostException
  {
    if (!connected_)
      connect();

    if (group < 0 || group > GROUP_ALL)
    {
      throw new ExtendedIllegalArgumentException("group",
          ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    Vector vec = null;
//@D3D    if (group == GROUP_ALL)
//@D3D    {
      // Call retrieve() to get the data from the AS/400 and
      // create a Vector of corresponding SystemValue objects
//@D3D      vec = SystemValueUtility.retrieve(system_, list_.elements(), getGroupName(group), getGroupDescription(group)); //@C2C
//@D3D    }
//@D3D    else
//@D3D    {
      // Get the group vector
      Vector grp = (Vector)groups_.get(getGroupName(group));
      vec = SystemValueUtility.retrieve(system_, grp.elements(), getGroupName(group), getGroupDescription(group)); //@C2C
//@D3D    }
    return sort(vec);
  }


  /**
  Returns the total number of possible groups.
    @return The number of groups.
  **/
  public static int getGroupCount()
  {
    return groupCount_;
  }

  /**
  Returns the description for the specified system value group.
    @param group The system value group.
    @return The description of the system value group.
  **/
  public static String getGroupDescription(int group)
  {
    if (group < 0 || group > GROUP_ALL)
    {
      throw new ExtendedIllegalArgumentException("group",
          ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    return groupDescriptions_[group];
  }


  /**
  Returns the description for the specified system value group.
    @param group The system value group.
    @param locale The locale used to load the translated group description.
    @return The description of the system value group.
  **/
  public static String getGroupDescription(int group, Locale locale)
  {
    if (group < 0 || group > GROUP_ALL)
    {
      throw new ExtendedIllegalArgumentException("group",
          ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (locale == null)
    {
      throw new NullPointerException("locale");
    }
    
    switch(group)
    {
      case GROUP_ALC:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_DESC", locale);
      case GROUP_DATTIM:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIME_DESC", locale);
      case GROUP_EDT:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_DESC", locale);
      case GROUP_LIBL:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_DESC", locale);
      case GROUP_MSG:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_DESC", locale);
      case GROUP_SEC:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_DESC", locale);
      case GROUP_STG:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_DESC", locale);
      case GROUP_SYSCTL:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_DESC", locale);
      case GROUP_NET:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_DESC", locale);
      case GROUP_ALL:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_DESC", locale);
      default: // This should not happen
        break;
    }
    return null;
  }


  /**
  Returns the name of the specified system value group.
    @param group The system value group.
    @return The name of the system value group.
  **/
  public static String getGroupName(int group)
  {
    if (group < 0 || group > GROUP_ALL)
    {
      throw new ExtendedIllegalArgumentException("group",
          ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    return groupNames_[group];
  }


  /**
  Returns the name of the specified system value group.
    @param group The system value group.
    @param locale The locale used to load the translated group name.
    @return The name of the system value group.
  **/
  public static String getGroupName(int group, Locale locale)
  {
    if (group < 0 || group > GROUP_ALL)
    {
      throw new ExtendedIllegalArgumentException("group",
          ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (locale == null)
    {
      throw new NullPointerException("locale");
    }
    
    switch(group)
    {
      case GROUP_ALC:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALC_NAME", locale);
      case GROUP_DATTIM:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_DATTIME_NAME", locale);
      case GROUP_EDT:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_EDT_NAME", locale);
      case GROUP_LIBL:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_LIBL_NAME", locale);
      case GROUP_MSG:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_MSG_NAME", locale);
      case GROUP_SEC:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SEC_NAME", locale);
      case GROUP_STG:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_STG_NAME", locale);
      case GROUP_SYSCTL:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_SYSCTL_NAME", locale);
      case GROUP_NET:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_NET_NAME", locale);
      case GROUP_ALL:
        return ResourceBundleLoader.getSystemValueText("SYSTEM_VALUE_GROUP_ALL_NAME", locale);
      default: // This should not happen
        break;
    }
    return null;
  }

  /**
  Returns the system.
    @return The AS/400.
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
  Returns a SystemValueInfo object for the system value specified by <i>name</i>.
    @param name The name of the system value.
    @return The SystemValueInfo object corresponding to <i>name</i>.
  **/
  static SystemValueInfo lookup(String name)
  {
    SystemValueInfo obj = (SystemValueInfo)list_.get(name);
    if (obj == null)
    {
      throw new ExtendedIllegalArgumentException(name,
          ExtendedIllegalArgumentException.FIELD_NOT_FOUND);
    }
    return obj;
  }


  /**
  Provided to initialize transient data if this object is de-serialized.
  **/
  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    changes_ = new PropertyChangeSupport(this);
    vetos_ = new VetoableChangeSupport(this);
  }


  /**
  Removes this listener from being notified when a bound property changes.
    @see #addPropertyChangeListener
    @param listener The PropertyChangeListener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@C1A
    changes_.removePropertyChangeListener(listener);
  }


  /**
  Removes this listener from being notified when a constrained property changes.
    @see #addVetoableChangeListener
    @param listener The VetoableChangeListener.
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null) throw new NullPointerException("listener"); //@C1A
    vetos_.removeVetoableChangeListener(listener);
  }


  /**
  Sets the system.
    @param system The AS/400.
    @exception PropertyVetoException If the change is vetoed.
  **/
  public void setSystem(AS400 system) throws PropertyVetoException
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    if (connected_)
    {
      throw new ExtendedIllegalStateException("system",
          ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }
    AS400 old = system_;
    vetos_.fireVetoableChange("system", old, system);
    system_ = system;
    changes_.firePropertyChange("system", old, system_);
  }


  /**
  Recursively sorts vectors of SystemValue objects by name.
    @param vec The objects to sort.
    @return The Vector of sorted objects.
  **/
  /*@C0D private*/ static Vector sort(Vector vec) //@C0C - made static
  {
    int len = vec.size();
    if (len < 2)
      return vec;
    SystemValue middle = (SystemValue)vec.elementAt(len/2);
    Vector lessthan = new Vector(len/2);
    Vector equalto = new Vector(len/2);
    Vector greaterthan = new Vector(len/2);
    Enumeration enum = vec.elements();
    while(enum.hasMoreElements())
    {
      SystemValue obj = (SystemValue)enum.nextElement();
      int comparison = obj.getName().compareTo(middle.getName());
      if (comparison < 0)
        lessthan.addElement(obj);
      else if (comparison > 0)
        greaterthan.addElement(obj);
      else
        equalto.addElement(obj);
    }
    lessthan.trimToSize();
    equalto.trimToSize();
    greaterthan.trimToSize();
    Vector lefthalf = sort(lessthan);
    Vector righthalf = sort(greaterthan);
    Vector whole = new Vector(lefthalf.size()+righthalf.size()+equalto.size());
    enum = lefthalf.elements();
    while (enum.hasMoreElements())
    {
      whole.addElement(enum.nextElement());
    }
    enum = equalto.elements();
    while (enum.hasMoreElements())
    {
      whole.addElement(enum.nextElement());
    }
    enum = righthalf.elements();
    while (enum.hasMoreElements())
    {
      whole.addElement(enum.nextElement());
    }
    return whole;
  }
}



