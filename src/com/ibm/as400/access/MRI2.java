///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: MRI2.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
Locale-specific objects for the AS/400 Toolbox for Java.
**/
//
//        Implementation note:
//        This MRI contains ONLY resources that are NOT needed
//        in the proxy jar file.  Resources needed in the proxy jar
//        file belong in MRI class.
//
public class MRI2 extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {

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


           // #TRANNOTE #####################################################
           // #TRANNOTE Property values.
           // #TRANNOTE Start with PROP_VALUE_ prefix.
           // #TRANNOTE #####################################################

           // #TRANNOTE Existence option and share option property values for
           // #TRANNOTE existenceOption and shareOption properties.  These are used by
           // #TRANNOTE IFSExistenceOptionEditor and IFSShareOptionEditor.
           // #TRANNOTE Fail if the file exists, create the file if it doesn't
      { "EDIT_FAIL_CREATE", "fail or create" },

           // #TRANNOTE Open the file if it exists, create the file if it doesn't
      { "EDIT_OPEN_CREATE", "open or create" },

           // #TRANNOTE Open the file if it exists, fail if it doesn't
      { "EDIT_OPEN_FAIL", "open or fail" },

           // #TRANNOTE Replace file if it exists, create the file if it doesn't
      { "EDIT_REPLACE_CREATE", "replace or create" },

           // #TRANNOTE Replace the file if it exists, fail if it doesn't
      { "EDIT_REPLACE_FAIL", "replace or fail" },

           // #TRANNOTE Allow read/write access by other users.
      { "EDIT_SHARE_ALL", "share with all" },

           // #TRANNOTE Allow read only access by other users.
      { "EDIT_SHARE_READERS", "share with readers" },

           // #TRANNOTE Allow write only access by other users.
      { "EDIT_SHARE_WRITERS", "share with writers" },

           // #TRANNOTE Don't allow any access to other users.
      { "EDIT_SHARE_NONE", "share with none" },


           // #TRANNOTE Font Pel Density Filter property values for the
           // #TRANNOTE fontPelDensityFilter property.
      { "EDIT_FONTPELDENSITY_NONE", "No filter" },
      { "EDIT_FONTPELDENSITY_240", "240 dots per inch" },
      { "EDIT_FONTPELDENSITY_300", "300 dots per inch" },



           // #TRANNOTE #####################################################
           // #TRANNOTE Short descriptions and display names for events.
           // #TRANNOTE Descriptions start with EVT_DESC_ prefix, display
           // #TRANNOTE names start with EVT_NAME.
           // #TRANNOTE #####################################################

      { "EVT_DESC_AS400CERTIFICATE_EVENT", "An AS/400 certificate event has occurred." },
      { "EVT_NAME_AS400CERTIFICATE_EVENT", "AS400Certificate" },
      { "EVT_DESC_SV_EVENT", "A system value event has occurred." },

           // #TRANNOTE A system value event has occurred.
      { "EVT_NAME_SV_EVENT", "systemValueEvent" },

           // #TRANNOTE #####################################################
           // #TRANNOTE Common exception messages.
           // #TRANNOTE #####################################################
           // #TRANNOTE Each key starts with the prefix EXC_ and
           // #TRANNOTE then a short identifier to describe the
           // #TRANNOTE message.
           // #TRANNOTE
      { "EXC_CERTIFICATE_ALREADY_ADDED", "Certificate association already exists." },
      { "EXC_CERTIFICATE_NOT_FOUND", "Certificate was not found." },
      { "EXC_CERTIFICATE_NOT_VALID", "Certificate or certificate type is not valid." },



           // #TRANNOTE #####################################################
           // #TRANNOTE Short descriptions and display names for job lists.
           // #TRANNOTE Descriptions start with JOB_LIST_ prefix.
           // #TRANNOTE #####################################################

      { "JOB_LIST_COMMAND", "Command" },
      { "JOB_LIST_DELAY", "Delay" },
      { "JOB_LIST_GROUPJOB", "Group job" },
      { "JOB_LIST_LOG", "Log" },
      { "JOB_LIST_MENU", "Menu" },

      { "JOB_LIST_SYSTEM", "System" },
      { "JOB_LIST_BATCH", "Batch" },
      { "JOB_LIST_INTERACTIVE", "Interactive" },
      { "JOB_LIST_SYSTEM_SCPF", "System (SCPF)" },
      { "JOB_LIST_SUBSYSTEM_MONITOR", "Subsystem Monitor" },
      { "JOB_LIST_AUTOSTART", "Autostart" },
      { "JOB_LIST_WRITER", "Writer" },
      { "JOB_LIST_READER", "Reader" },

      { "JOB_LIST_IMMEDIATE", "Immediate" },
      { "JOB_LIST_EVOKE", "Evoke" },
      { "JOB_LIST_A36", "Advanced 36 Machine Server" },
      { "JOB_LIST_PRESTART", "Prestart" },
      { "JOB_LIST_PRINT_DRIVER", "Print Driver" },
      { "JOB_LIST_A36_MRT", "System/36 Multiple Requester Terminal" },
      { "JOB_LIST_ALTERNATE_SPOOL_USER", "Alternate Spool User" },

      { "JOB_LIST_SCHEDULED", "scheduled" },
      { "JOB_LIST_HELD", "held" },
      { "JOB_LIST_READY_TO_SELECT", "ready to select" },


           // #TRANNOTE #####################################################
           // #TRANNOTE System Status MRI.
           // #TRANNOTE #####################################################

      { "PROP_NAME_SS_DTG", "dateAndTimeStatusGathered" },
      { "PROP_DESC_SS_DTG", "The date and time status gathered." },

      { "PROP_NAME_SS_UCSO", "usersCurrentSignedOn" },
      { "PROP_DESC_SS_UCSO", "Number of users currently signed on the AS/400." },

      { "PROP_NAME_SS_UTSO", "usersTemporarilySignedOff" },
      { "PROP_DESC_SS_UTSO", "Number of users temporarily signed off the AS/400." },

      { "PROP_NAME_SS_USBS", "usersSuspendedBySystem" },
      { "PROP_DESC_SS_USBS", "Number of user jobs suspended by the system." },

      { "PROP_NAME_SS_USOWP", "usersSignedOffWithPrinter" },
      { "PROP_DESC_SS_USOWP", "Number of users signed off with printer output waiting to print." },

      { "PROP_NAME_SS_BJWM", "batchJobsWaitingForMessage" },
      { "PROP_DESC_SS_BJWM", "Number of batch job waiting for a message." },

      { "PROP_NAME_SS_BJR", "batchJobsRunning" },
      { "PROP_DESC_SS_BJR", "Number of batch jobs running." },

      { "PROP_NAME_SS_BJHR", "batchJobsHeldWhileRunning" },
      { "PROP_DESC_SS_BJHR", "Number of batch jobs held while running." },

      { "PROP_NAME_SS_BJE", "batchJobsEnding" },
      { "PROP_DESC_SS_BJE", "Number of batch jobs ending." },

      { "PROP_NAME_SS_BJWR", "batchJobsWaitingToRunOrAlreadyScheduled" },
      { "PROP_DESC_SS_BJWR", "Number of batch jobs waiting to run or already scheduled to run." },

      { "PROP_NAME_SS_BJH", "batchJobsHeldOnJobQueue" },
      { "PROP_DESC_SS_BJH", "Number of batch jobs held on job queue." },

      { "PROP_NAME_SS_BJU", "batchJobsOnUnassignedJobQueue" },
      { "PROP_DESC_SS_BJU", "Number batch jobs on unassigned job queue." },

      { "PROP_NAME_SS_ET", "elapsedTime" },
      { "PROP_DESC_SS_ET", "The elapsed time." },

      { "PROP_NAME_SS_MUS", "maximumUnprotectedStorageUsed" },
      { "PROP_DESC_SS_MUS", "Maximum unprotected storage used." },

      { "PROP_NAME_SS_PPA", "percentPermanentAddresses" },
      { "PROP_DESC_SS_PPA", "Percent of permanent addresses used." },

      { "PROP_NAME_SS_PPU", "percentProcessingUnitUsed" },
      { "PROP_DESC_SS_PPU", "Percent of processing units used." },

      { "PROP_NAME_SS_SASP", "percentSystemASPUsed" },
      { "PROP_DESC_SS_SASP", "Percent of system ASP used." },

      { "PROP_NAME_SS_PTA", "percentTemporaryAddresses" },
      { "PROP_DESC_SS_PTA", "Percent of temporary addresses used." },

      { "PROP_NAME_SS_PN", "poolsNumber" },
      { "PROP_DESC_SS_PN", "Number of pools." },

      { "PROP_NAME_SS_RSF", "restrictedStateFlag" },
      { "PROP_DESC_SS_RSF", "The restricted state flag." },

      { "PROP_NAME_SS_SYSTEMASP", "systemASP" },
      { "PROP_DESC_SS_SYSTEMASP", "The system ASP." },

      { "PROP_NAME_SS_SYSPOOL", "systemPools" },
      { "PROP_DESC_SS_SYSPOOL", "The system pools." },

      { "PROP_NAME_SS_TAS", "totalAuxiliaryStorage" },
      { "PROP_DESC_SS_TAS", "The total auxiliary storage." },


           // #TRANNOTE #####################################################
           // #TRANNOTE System Pool MRI.
           // #TRANNOTE #####################################################

      { "PROP_NAME_SP_POOLNAME", "poolName" },
      { "PROP_DESC_SP_POOLNAME", "The system pool name." },

      { "PROP_NAME_SP_POOLID", "poolIdentifier" },
      { "PROP_DESC_SP_POOLID", "The system pool identifier." },

      { "PROP_NAME_SP_ATE", "activeToIneligible" },
      { "PROP_DESC_SP_ATE", "Transition of threads from an active condition to a ineligible condition." },

      { "PROP_NAME_SP_ATW", "activeToWait" },
      { "PROP_DESC_SP_ATW", "Transition of threads from an active condition to a waiting condition." },

      { "PROP_NAME_SP_DBFAULTS", "databaseFaults" },
      { "PROP_DESC_SP_DBFAULTS", "Number of database page faults." },

      { "PROP_NAME_SP_DBPAGES", "databasePages" },
      { "PROP_DESC_SP_DBPAGES", "Number of database pages." },

      { "PROP_NAME_SP_MAXAT", "maximumActiveThreads" },
      { "PROP_DESC_SP_MAXAT", "Maximum number of active threads." },

      { "PROP_NAME_SP_NONDBFLTS", "nonDatabaseFaults" },
      { "PROP_DESC_SP_NONDBFLTS", "Number of non database faults." },

      { "PROP_NAME_SP_NONDBPGS", "nonDatabasePages" },
      { "PROP_DESC_SP_NONDBPGS", "Number of non database pages." },

      { "PROP_NAME_SP_PAGINGOPTION", "pagingOption" },
      { "PROP_DESC_SP_PAGINGOPTION", "The paging option." },

      { "PROP_NAME_SP_POOLSIZE", "poolSize" },
      { "PROP_DESC_SP_POOLSIZE", "Amount of main storage in the pool." },

      { "PROP_NAME_SP_RSVDSIZE", "reservedSize" },
      { "PROP_DESC_SP_RSVDSIZE", "Amount of main storage in the pool reserved for system use." },

      { "PROP_NAME_SP_SUBSYSNAME", "subsystemName" },
      { "PROP_DESC_SP_SUBSYSNAME", "Name of the subsystem that the storage pool is associated." },

      { "PROP_NAME_SP_WTI", "waitToIneligible" },
      { "PROP_DESC_SP_WTI", "Transition of threads from a waiting condition to a ineligible condition." },

      { "PROP_NAME_SP_MAXFAULTS", "maximumFaults" },
      { "PROP_DESC_SP_MAXFAULTS", "Maximum faults to use for the storage pool." },

      { "PROP_NAME_SP_MAXPOOLSIZE", "maximumPoolSize" },
      { "PROP_DESC_SP_MAXPOOLSIZE", "Maximum amount of storage to allocate to a storage pool." },

      { "PROP_NAME_SP_MSGLOGGING", "messageLogging" },
      { "PROP_DESC_SP_MSGLOGGING", "Messages are written to the job log for the current job and to the QHST message log." },

      { "PROP_NAME_SP_MINFAULTS", "minimunFaults" },
      { "PROP_DESC_SP_MINFAULTS", "Minimum faults to use for the storage pool." },

      { "PROP_NAME_SP_MINPOOLSIZE", "minimunPoolSize" },
      { "PROP_DESC_SP_MINPOOLSIZE", "Minimum amount of storage to allocate to a storage pool." },

      { "PROP_NAME_SP_PERTHRDFLTS", "perThreadFaults" },
      { "PROP_DESC_SP_PERTHRDFLTS", "Faults for each active thread in the storage pool." },

      { "PROP_NAME_SP_POOLACTLVL", "poolActivityLevel" },
      { "PROP_DESC_SP_POOLACTLVL", "Activity level for the pool." },

      { "PROP_NAME_SP_PRIORITY", "priority" },
      { "PROP_DESC_SP_PRIORITY", "Priority of a pool relative to the priority of the other storage pools." },




      // Message File Bean MRI             @D1A
      { "PROP_NAME_MF_HELP_TEXT_FORMATTING", "helpTextFormatting" },
      { "PROP_DESC_MF_HELP_TEXT_FORMATTING", "The type of help text formatting." },



      // Proxy support MRI.
      // #TRANNOTE ################################################################
      // #TRANNOTE The following are error and informational (verbose) messages.
      // #TRANNOTE ################################################################
      { "PROXY_ALREADY_LISTENING",          "An active proxy server is already listening to port &0." },
      { "PROXY_CONFIGURATION_UPDATED",      "The configuration has been updated."},
      { "PROXY_CONFIGURATION_NOT_LOADED",   "Configuration not loaded: &0"},
      { "PROXY_JDBC_DRIVER_NOT_REGISTERED", "JDBC driver not registered: &0"},
      { "PROXY_JDBC_DRIVER_REGISTERED",     "Registered JDBC driver &0."},
      { "PROXY_OPTION_NOT_VALID",           "Option not valid: &0" },
      { "PROXY_OPTION_VALUE_NOT_VALID",     "Value for option &0 not valid: &1" },
      { "PROXY_PEER_NOT_RESPONDING",        "Peer proxy server &0 is not responding." },
      { "PROXY_SERVER_CONTAINER",           "Proxy server" },
      { "PROXY_SERVER_END",                 "Proxy server ended as requested by &0." },
      { "PROXY_SERVER_END_REJECTED",        "Proxy server end request from &0 was rejected." },
      { "PROXY_SERVER_ENDED",               "&0 ended." },
      { "PROXY_SERVER_LISTENING",           "&0 listening to port &1." },
      { "PROXY_SERVER_OPTIONSLC",           "options" },
      { "PROXY_SERVER_OPTIONSUC",           "Options" },
      { "PROXY_SERVER_SHORTCUTS",           "Shortcuts" },                              // @D3A
      { "PROXY_SERVER_SECURE_CONTAINER",    "Secure proxy server" },
      { "PROXY_SERVER_STARTED",             "Proxy server started." },
      { "PROXY_SERVER_USAGE",               "Usage" },
      { "PROXY_VALUE_NO_OPTION",            "Value with no option ignored: &0" },       // @D2A



           // #TRANNOTE #####################################################
           // #TRANNOTE System pool MRI.
           // #TRANNOTE Start with SYSTEM_POOL_ prefix.
           // #TRANNOTE #####################################################

      //@A3A
      { "SYSTEM_POOL_MACHINE", "The machine pool." },
      { "SYSTEM_POOL_BASE", "The base system pool, which can be shared with other subsystems." },
      { "SYSTEM_POOL_INTERACT", "The shared pool used for interactive work." },
      { "SYSTEM_POOL_SPOOL", "The shared pool used for specified writers." },
      { "SYSTEM_POOL_OTHER", "A shared pool." },



           // #TRANNOTE #####################################################
           // #TRANNOTE MRI For JavaApplicationCall
           // #TRANNOTE #####################################################
      //@D1D
      { "PROP_NAME_JAC_CLASSPATH",  "classpath" },
      { "PROP_DESC_JAC_CLASSPATH",  "The CLASSPATH environment variable." },

      { "PROP_NAME_JAC_SECCHKLVL",  "classpathSecurityCheckLevel" },
      { "PROP_DESC_JAC_SECCHKLVL",  "The CLASSPATH security check level." },

      { "PROP_NAME_JAC_GCINIT",     "garbageCollectionInitialSize" },
      { "PROP_DESC_JAC_GCINIT",     "The initial size of the heap." },

      { "PROP_NAME_JAC_GCMAX",      "garbageCollectionMaximumSize" },
      { "PROP_DESC_JAC_GCMAX",      "The maximum size of the heap." },

      { "PROP_NAME_JAC_GCFREQ",     "garbageCollectionFrequency" },
      { "PROP_DESC_JAC_GCFREQ",     "The relative frequency in which garbage collection runs." },

      { "PROP_NAME_JAC_GCPRIORITY", "garbageCollectionPriority" },
      { "PROP_DESC_JAC_GCPRIORITY", "The priority of the garbage collection thread." },

      { "PROP_NAME_JAC_INTERPRET",  "interpret" },
      { "PROP_DESC_JAC_INTERPRET",  "Run classes in interpret mode." },

      { "PROP_NAME_JAC_JAVAAPP",    "javaApplication" },
      { "PROP_DESC_JAC_JAVAAPP",    "The Java application to run." },

      { "PROP_NAME_JAC_OPTIMIZE",   "optimization" },
      { "PROP_DESC_JAC_OPTIMIZE",   "Level of optimization of Java classes." },

      { "PROP_NAME_JAC_OPTION",     "option" },
      { "PROP_DESC_JAC_OPTION",     "Java virtual machine options." },

      { "PROP_NAME_JAC_PARAMETERS", "parameters" },
      { "PROP_DESC_JAC_PARAMETERS", "Parameters for the Java application" },

      { "PROP_NAME_JAC_PORTSEARCH", "portSearch" },
      { "PROP_DESC_JAC_PORTSEARCH", "Perform a port search to find a free port." },



      { "PROP_NAME_SAVE_FILE_PUBLIC_AUTHORITY", "saveFilePublicAuthority" },
      { "PROP_DESC_SAVE_FILE_PUBLIC_AUTHORITY", "Authority value for *PUBLIC." },
      { "CREATE_SAVE_FILE_FAILED", "The CRTSAVFIL command failed: " },

           // #TRANNOTE The properties of ProductLicense.                //@C2A
      { "PROP_DESC_LICENSE_PRODUCT", "The product name." },              //@C2A
      { "PROP_NAME_LICENSE_PRODUCT", "product" },                        //@C2A
      { "PROP_DESC_LICENSE_FEATURE", "The product feature name." },      //@C2A
      { "PROP_NAME_LICENSE_FEATURE", "feature" },                        //@C2A
      { "PROP_DESC_LICENSE_RELEASE", "The product release." },           //@C2A
      { "PROP_NAME_LICENSE_RELEASE", "release" },                        //@C2A

           // #TRANNOTE A license event has occurred.
      { "EVT_DESC_LICENSE_EVENT", "A product license event has occurred." }, //@C2A
      { "EVT_NAME_LICENSE_EVENT", "productLicenseEvent" },                   //@C2A


   };

}

