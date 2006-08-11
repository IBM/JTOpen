///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: MRI2.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
Locale-specific objects for the IBM Toolbox for Java.
**/
//
//        Implementation note:
//        This MRI contains ONLY resources that are NOT needed
//        in the proxy jar file.  Resources needed in the proxy jar
//        file belong in MRI class.
//
public class MRI2 extends ListResourceBundle
{
   // NLS_MESSAGEFORMAT_NONE
   // Each string is assumed NOT to be processed by the MessageFormat class.
   // This means that a single quote must be coded as 1 single quote.

  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";


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

      { "EVT_DESC_AS400CERTIFICATE_EVENT", "A certificate event has occurred." },
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
      { "EXC_MAX_CONN_REACHED", "Maximum configured number of connections has been reached." }, //@F2A
      { "EXC_CONFLICT_POOL_SIZES", "The minimum and maximum number of connections do not agree." }, //@B0A


           // #TRANNOTE #####################################################
           // #TRANNOTE System Status MRI.
           // #TRANNOTE #####################################################

      { "PROP_NAME_SS_DTG", "dateAndTimeStatusGathered" },
      { "PROP_DESC_SS_DTG", "The date and time status gathered." },

      { "PROP_NAME_SS_UCSO", "usersCurrentSignedOn" },
      { "PROP_DESC_SS_UCSO", "Number of users currently signed on the system." },       //@550

      { "PROP_NAME_SS_UTSO", "usersTemporarilySignedOff" },
      { "PROP_DESC_SS_UTSO", "Number of users temporarily signed off the system." },    //@550

      { "PROP_NAME_SS_USBS", "usersSuspendedBySystem" },
      { "PROP_DESC_SS_USBS", "Number of user jobs suspended by the system." }, //@550

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

      // Job MRI @D9a
      { "PROP_NAME_JOB_NUMBER", "number" },
      { "PROP_DESC_JOB_NUMBER", "Job number." },
      { "PROP_NAME_JOB_USER", "user" },
      { "PROP_DESC_JOB_USER", "Job user." },




      // Proxy support MRI.
      // #TRANNOTE ################################################################
      // #TRANNOTE The following are error and informational (verbose) messages.
      // #TRANNOTE ################################################################
      { "PROXY_ALREADY_LISTENING",          "An active server is already listening to port &0." },             // @M1C
      { "PROXY_CONFIGURATION_UPDATED",      "The configuration has been updated."},
      { "PROXY_CONFIGURATION_NOT_LOADED",   "Configuration not loaded: &0"},
      { "PROXY_JDBC_DRIVER_NOT_REGISTERED", "JDBC driver not registered: &0"},
      { "PROXY_JDBC_DRIVER_REGISTERED",     "Registered JDBC driver: &0."},             // @E2C
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
      { "PROXY_SERVER_NO_KEYRING",          "The -keyringName or -keyringPassword option were not properly specified." },  //$E9A
      { "PROXY_SERVER_KEYRING_EXPLAIN",     "The CLASSPATH contains the SSLight classes. To use SSL with the proxy, the " + //$E9A
                                            "-keyringName and -keyringPassword options must both be specified.  "},        //$E9A



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

         // #TRANNOTE #####################################################
         // #TRANNOTE MRI For ConnectionPoolProperties
         // #TRANNOTE #####################################################     // @E3
      { "PROP_NAME_CPP_CLEANUP_INTERVAL", "cleanupInterval" },
      { "PROP_NAME_CPP_MAX_CONNECTIONS", "maxConnections" },
      { "PROP_NAME_CPP_MAX_INACTIVITY", "maxInactivity" },
      { "PROP_NAME_CPP_MAX_LIFETIME", "maxLifetime" },
      { "PROP_NAME_CPP_MAX_USE_COUNT", "maxUseCount" },
      { "PROP_NAME_CPP_MAX_USE_TIME", "maxUseTime" },

      { "PROP_DESC_CPP_CLEANUP_INTERVAL", "The cleanup time interval for the connection pool." },
      { "PROP_DESC_CPP_MAX_CONNECTIONS", "The maximum number of connections a pool can have." },
      { "PROP_DESC_CPP_MAX_INACTIVITY", "The maximum amount of time a connection can be inactive." },
      { "PROP_DESC_CPP_MAX_LIFETIME", "The maximum amount of time a connection can exist." },
      { "PROP_DESC_CPP_MAX_USE_COUNT", "The maximum number of times a connection can be used." },
      { "PROP_DESC_CPP_MAX_USE_TIME", "The maximum amount of time a connection can be used." },

         // #TRANNOTE #####################################################
         // #TRANNOTE MRI For ConnectionPool
         // #TRANNOTE #####################################################      // @E3
      { "PROP_NAME_CP_DATA_SOURCE", "dataSource"},
      { "PROP_NAME_CP_PROPERTIES", "properties"},
      { "PROP_NAME_CP_RUN_MAINTENANCE", "runMaintenance"},
      { "PROP_NAME_CP_THREAD_USED", "threadUsed"},                               // @E5

      { "PROP_DESC_CP_DATA_SOURCE", "The data source used to make JDBC connections."},
      { "PROP_DESC_CP_PROPERTIES", "The connection pool properties."},
      { "PROP_DESC_CP_RUN_MAINTENANCE", "Specifies whether the maintenance daemon is used."},
      { "PROP_DESC_CP_THREAD_USED", "Specifies whether threads is used."},       // @E5

      // #TRANNOTE The properties for ConnectionPoolDataSource. @B0A
      { "PROP_NAME_CPDS_INIT_POOL_SIZE", "initialPoolSize"},
      { "PROP_DESC_CPDS_INIT_POOL_SIZE", "The initial number of connections in the pool."},
      { "PROP_NAME_CPDS_MAX_IDLE_TIME", "maxIdleTime"},
      { "PROP_DESC_CPDS_MAX_IDLE_TIME", "The maximum amount of time a connection can be idle."},
      { "PROP_NAME_CPDS_MAX_POOL_SIZE", "maxPoolSize"},
      { "PROP_DESC_CPDS_MAX_POOL_SIZE", "The maximum number of connections in the pool."},

      // #TRANNOTE Statement caching is not supported this way (yet)
      //           so we don't need this MRI.
      //      { "PROP_NAME_CPDS_MAX_STATEMENTS", "maxStatements"},
      //      { "PROP_DESC_CPDS_MAX_STATEMENTS", "The maximum number of statements any one pooled connection can have."},
      { "PROP_NAME_CPDS_MIN_POOL_SIZE", "minPoolSize"},
      { "PROP_DESC_CPDS_MIN_POOL_SIZE", "The minium number of available connections in the pool."},
      { "PROP_NAME_CPDS_PROP_CYCLE", "propertyCycle"},
      { "PROP_DESC_CPDS_PROP_CYCLE", "The cleanup time interval for the connection pool."},


      { "PROP_NAME_SAVE_FILE_PUBLIC_AUTHORITY", "saveFilePublicAuthority" },
      { "PROP_DESC_SAVE_FILE_PUBLIC_AUTHORITY", "Authority value for *PUBLIC." },
      { "CREATE_SAVE_FILE_FAILED", "The CRTSAVFIL command failed: " },

           // #TRANNOTE The properties of ProductLicense.                 // @C2A
      { "PROP_DESC_LICENSE_PRODUCTID", "The product identifier." },       // @F7C
      { "PROP_NAME_LICENSE_PRODUCTID", "productID" },                     // @F7C
      { "PROP_DESC_LICENSE_FEATUREID", "The product feature." },          // @C2A
      { "PROP_NAME_LICENSE_FEATUREID",  "feature" },                      // @C2A
      { "PROP_DESC_LICENSE_RELEASELEVEL", "The product release level." }, // @F7C
      { "PROP_NAME_LICENSE_RELEASELEVEL", "releaseLevel" },               // @F7C

           // #TRANNOTE A license event has occurred.
      { "EVT_DESC_LICENSE_EVENT", "A product license event has occurred." }, //@C2A
      { "EVT_NAME_LICENSE_EVENT", "productLicenseEvent" },                   //@C2A



           // #TRANNOTE #####################################################
           // #TRANNOTE MRI For CommandCall and ProgramCall
           // #TRANNOTE #####################################################
      //@E4A
      { "PROP_NAME_CMD_THREADSAFE",  "threadSafe" },
      { "PROP_DESC_CMD_THREADSAFE",  "The thread-safety of the command or program." },


      // #TRANNOTE #####################################################
      // #TRANNOTE MRI For AS400ConnectionPool and ConnectionList
      // #TRANNOTE #####################################################     //@E6A

      // #TRANNOTE creating connection list for system/userid
      { "AS400CP_CONNLIST", "creating connection list for &0/&1" },

      // #TRANNOTE returning connection to connection pool system/userid
      { "AS400CP_RETCONN", "returning connection to connection pool &0/&1" },

      { "AS400CP_SHUTDOWN", "the connection pool is shutting down" },

      { "AS400CP_SHUTDOWNCOMP", "shutdown of connection pool completed" },

      // #TRANNOTE cleaning up connections for system/userid
      { "CL_CLEANUP", "cleaning up connections for &0/&1" },

      { "CL_CLEANUPCOMP", "cleanup completed" },

      { "CL_CLEANUPEXP", "connection limit reached, cleaning up expired connections" },

      { "CL_CLEANUPOLD", "connection limit reached, cleaning up oldest connections" },

      // #TRANNOTE connection created for system/userid
      { "CL_CREATED", "connection created for &0/&1" },

      // #TRANNOTE creating a new connection for system/userid
      { "CL_CREATING", "creating a new connection for &0/&1" },

      // #TRANNOTE removing oldest connection for system/userid
      { "CL_REMOLD", "removing oldest connection for &0/&1" },

      // #TRANNOTE completed removing oldest connection for system/userid
      { "CL_REMOLDCOMP", "completed removing oldest connection for &0/&1" },

      // #TRANNOTE removing connection that exceeded maximum inactivity time for system/userid    // @E8C
      { "CL_REMUNUSED", "removing connection that exceeded maximum inactivity time for &0/&1" },  // @E8C

      // #TRANNOTE replacing connection that exceeded maximum lifetime for system/userid
      { "CL_REPLIFE", "replacing connection that exceeded maximum lifetime for &0/&1" },

      // #TRANNOTE replacing connection that exceeded maximum use count for system/userid
      { "CL_REPUSE", "replacing connection that exceeded maximum use count for &0/&1" },

      // #TRANNOTE filling numberOfConnections connections to system/userid      // @E8A
      { "AS400CP_FILLING", "filling &0 connections to &1/&2" },                  // @E8A

      { "AS400CP_FILLEXC", "filling of connections failed with an exception" },  // @E8A

         // #TRANNOTE #####################################################
         // #TRANNOTE MRI For AS400JPing
         // #TRANNOTE #####################################################      // @E7A
      { "PROP_NAME_AJP_FAILED", "Failed to make connection to server application:  &0&1"},
      { "PROP_NAME_AJP_SUCCESS", "Successfully connected to server application:  &0&1"},

         // #TRANNOTE #####################################################
         // #TRANNOTE MRI For AS400JDBCDataSource
         // #TRANNOTE #####################################################     // @F1A

      { "AS400_JDBC_DS_CONN_CREATED", "connection created" },
      { "AS400_JDBC_DS_PASSWORD_SET", "password is set" },



      // @F3A @F6C
      // Localized object types - These are used in QSYSObjectTypeTable.
      //
      // #TRANNOTE ################################################################
      // #TRANNOTE i5/OS object types.
      // #TRANNOTE
      // #TRANNOTE These are MRI for object types and subtypes.  They will be
      // #TRANNOTE concatenated with a hyphen in the form "Type - Subtype".
      // #TRANNOTE Each type is an MRI string with the key TYPE_type.  The
      // #TRANNOTE subtype are listed separately with the key TYPE_type_subtype.
      // #TRANNOTE
      // #TRANNOTE ################################################################
      { "TYPE_ALRTBL",                      "Alert table" },
      { "TYPE_AUTL",                        "Authorization list" },
      { "TYPE_BLKSF",                       "Block special file" },
      { "TYPE_BNDDIR",                      "Binding directory" },
      { "TYPE_CFGL",                        "Configuration list" },
      { "TYPE_CFGL_APPNDIR",                "APPN directory search filter" },
      { "TYPE_CFGL_APPNLCL",                "APPN local location" },
      { "TYPE_CFGL_APPNRMT",                "APPN remote location" },
      { "TYPE_CFGL_APPNSSN",                "APPN session end point filter" },
      { "TYPE_CFGL_ASYNCADR",               "Asynchronous network address" },
      { "TYPE_CFGL_ASYNCLOC",               "Asynchronous remote location" },
      { "TYPE_CFGL_SNAPASTHR",              "SNA pass-through" },
      { "TYPE_CHTFMT",                      "Chart format" },
      { "TYPE_CHRSF",                       "Character special file" },
      { "TYPE_CLD",                         "C/400 locale description" },
      { "TYPE_CLS",                         "Class" },
      { "TYPE_CMD",                         "Command" },
      { "TYPE_CNNL",                        "Connection list" },
      { "TYPE_COSD",                        "Class-of-service description" },
      { "TYPE_CRG",                         "Cluster resource group" },
      { "TYPE_CRQD",                        "Change request description" },
      { "TYPE_CSI",                         "Communications side information" },
      { "TYPE_CSPMAP",                      "Cross-system product map" },
      { "TYPE_CSPTBL",                      "Cross-system product table" },
      { "TYPE_CTLD",                        "Controller description" },
      { "TYPE_CTLD_APPC",                   "APPC" },
      { "TYPE_CTLD_ASC",                    "Asynchronous" },
      { "TYPE_CTLD_BSC",                    "Binary synchronous" },
      { "TYPE_CTLD_FNC",                    "Finance" },
      { "TYPE_CTLD_HOST",                   "SNA host" },
      { "TYPE_CTLD_LWS",                    "Local workstation" },
      { "TYPE_CTLD_NET",                    "Network" },
      { "TYPE_CTLD_RTL",                    "Retail" },
      { "TYPE_CTLD_RWS",                    "Remote workstation" },
      { "TYPE_CTLD_TAP",                    "Tape" },
      { "TYPE_CTLD_VWS",                    "Virtual workstation" },
      { "TYPE_DDIR",                        "Distributed directory" },
      { "TYPE_DEVD",                        "Device description" },
      { "TYPE_DEVD_APPC",                   "APPC" },
      { "TYPE_DEVD_ASC",                    "Asynchronous" },
      { "TYPE_DEVD_ASP",                    "Disk pool" },                          // @F6C
      { "TYPE_DEVD_BSC",                    "Binary synchronous" },
      { "TYPE_DEVD_CRP",                    "Cryptographic" },
      { "TYPE_DEVD_DKT",                    "Diskette" },
      { "TYPE_DEVD_DSPLCL",                 "Local display" },
      { "TYPE_DEVD_DSPRMT",                 "Remote display" },
      { "TYPE_DEVD_DSPSNP",                 "SNA pass-through display" },
      { "TYPE_DEVD_DSPVRT",                 "Virtual display" },                    // @F6C
      { "TYPE_DEVD_FNC",                    "Finance" },
      { "TYPE_DEVD_HOST",                   "SNA host" },
      { "TYPE_DEVD_INTR",                   "Intrasystem" },
      { "TYPE_DEVD_MLB",                    "Media library" },
      { "TYPE_DEVD_NET",                    "Network" },
      { "TYPE_DEVD_OPT",                    "Optical" },
      { "TYPE_DEVD_PRTLCL",                 "Local printer" },
      { "TYPE_DEVD_PRTLAN",                 "LAN printer" },
      { "TYPE_DEVD_PRTRMT",                 "Remote printer" },
      { "TYPE_DEVD_PRTSNP",                 "SNA pass-through printer" },
      { "TYPE_DEVD_PRTVRT",                 "Virtual printer" },
      { "TYPE_DEVD_RTL",                    "Retail" },
      { "TYPE_DEVD_SNPTUP",                 "SNA pass-through upstream" },
      { "TYPE_DEVD_SNPTDN",                 "SNA pass-through downstream" },
      { "TYPE_DEVD_SNUF",                   "SNA upline facility" },
      { "TYPE_DEVD_TAP",                    "Tape" },
      { "TYPE_DIR",                         "Directory" },
      { "TYPE_DOC",                         "Document" },
      { "TYPE_DSTMF",                       "Distributed stream file" },
      { "TYPE_DTAARA",                      "Data area" },
      { "TYPE_DTADCT",                      "Data dictionary" },
      { "TYPE_DTAQ",                        "Data queue" },
      { "TYPE_DTAQ_DDMDTAQUE",              "DDM" },
      { "TYPE_EDTD",                        "Edit description" },
      { "TYPE_EXITRG",                      "Exit registration" },
      { "TYPE_FCT",                         "Forms control table" },
      { "TYPE_FIFO",                        "First-in-first-out special file" },
      { "TYPE_FILE",                        "File" },
      { "TYPE_FILE_PF",                     "Physical" },
      { "TYPE_FILE_LF",                     "Logical" },
      { "TYPE_FILE_BSCF38",                 "Binary synchronous (S/38)" },
      { "TYPE_FILE_CMNF38",                 "Communications (S/38)" },
      { "TYPE_FILE_CRDF38",                 "Card (S/38)" },
      { "TYPE_FILE_DFU",                    "DFU" },
      { "TYPE_FILE_DFUEXC",                 "DFU (S/38)" },
      { "TYPE_FILE_DFUNOTEXC",              "DFU (S/38)" },
      { "TYPE_FILE_DSPF",                   "Display" },
      { "TYPE_FILE_DSPF36",                 "Display (S/36)" },
      { "TYPE_FILE_DSPF38",                 "Display (S/38)" },
      { "TYPE_FILE_DDMF",                   "DDM" },
      { "TYPE_FILE_DKTF",                   "Diskette" },
      { "TYPE_FILE_ICFF",                   "Intersystem communication" },
      { "TYPE_FILE_LF38",                   "Logical (S/38)" },
      { "TYPE_FILE_MXDF38",                 "Mixed (S/38)" },
      { "TYPE_FILE_PF38",                   "Physical (S/38)" },
      { "TYPE_FILE_PRTF",                   "Printer" },
      { "TYPE_FILE_PRTF38",                 "Printer (S/38)" },
      { "TYPE_FILE_SAVF",                   "Save" },
      { "TYPE_FILE_TAPF",                   "Tape" },
      { "TYPE_FLR",                         "Folder" },
      { "TYPE_FNTRSC",                      "Font resource" },                                  // @F6C
      { "TYPE_FNTRSC_CDEFNT",               "Coded font" },
      { "TYPE_FNTRSC_FNTCHRSET",            "Font character set" },
      { "TYPE_FNTRSC_CDEPAG",               "Code page" },
      { "TYPE_FNTTBL",                      "Font mapping table" },
      { "TYPE_FORMDF",                      "Form definition" },
      { "TYPE_FTR",                         "Filter" },
      { "TYPE_FTR_ALR",                     "Alert" },
      { "TYPE_FTR_PRB",                     "Problem" },
      { "TYPE_GSS",                         "Symbol set" },
      { "TYPE_GSS_VSS",                     "Vector" },
      { "TYPE_GSS_ISS",                     "Image" },
      { "TYPE_IGCDCT",                      "DBCS conversion dictionary" },
      { "TYPE_IGCSRT",                      "DBCS sort table" },
      { "TYPE_IGCTBL",                      "DBCS font table" },
      { "TYPE_IMGCLG",                      "Optical image catalog" },                     // @M2a
      { "TYPE_IPXD",                        "Internetwork packet exchange description" },
      { "TYPE_JOBD",                        "Job description" },
      { "TYPE_JOBQ",                        "Job queue" },
      { "TYPE_JOBSCD",                      "Job schedule" },
      { "TYPE_JRN",                         "Journal" },
      { "TYPE_JRNRCV",                      "Journal receiver" },
      { "TYPE_LIB",                         "Library" },
      { "TYPE_LIB_PROD",                    "" }, // We don't want the text "Production" here.  @F6C
      { "TYPE_LIB_TEST",                    "Test" },
      { "TYPE_LIND",                        "Line description" },
      { "TYPE_LIND_ASC",                    "Asynchronous" },
      { "TYPE_LIND_BSC",                    "Binary synchronous" },
      { "TYPE_LIND_DDI",                    "Distributed data interface" },
      { "TYPE_LIND_ETH",                    "Ethernet" },
      { "TYPE_LIND_FAX",                    "Facsimile (fax)" },
      { "TYPE_LIND_FR",                     "Frame relay" },
      { "TYPE_LIND_IDLC",                   "ISDN data link control" },
      { "TYPE_LIND_NET",                    "Network" },
      { "TYPE_LIND_PPP",                    "Point-to-point protocol" },
      { "TYPE_LIND_SDLC",                   "Synchronous data link control" },
      { "TYPE_LIND_TDLC",                   "Twinaxial data link control" },
      { "TYPE_LIND_TRN",                    "Token-Ring" },
      { "TYPE_LIND_WLS",                    "Wireless" },
      { "TYPE_LIND_X25",                    "X.25" },
      { "TYPE_LOCALE",                      "Locale" },
      { "TYPE_MEDDFN",                      "Media definition" },
      { "TYPE_MENU",                        "Menu" },
      { "TYPE_MENU_UIM",                    "UIM" },
      { "TYPE_MENU_DSPF",                   "Display file" },
      { "TYPE_MENU_PGM",                    "Program" },
      { "TYPE_MGTCOL",                      "Management collection" },
      { "TYPE_MGTCOL_PFR",                  "Collection Services performance data" },
      { "TYPE_MGTCOL_PFRHST",               "Archived performance data" },
      { "TYPE_MGTCOL_PFRDTL",               "System Monitor performance data" },
      { "TYPE_MODD",                        "Mode description" },
      { "TYPE_MODULE",                      "Module" },
      { "TYPE_MODULE_CLE",                  "C" },
      { "TYPE_MODULE_CLLE",                 "CL" },
      { "TYPE_MODULE_RPGLE",                "RPG" },
      { "TYPE_MODULE_CBLLE",                "COBOL" },
      { "TYPE_MODULE_CPPLE",                "C++" },
      { "TYPE_MSGF",                        "Message file" },
      { "TYPE_MSGQ",                        "Message queue" },
      { "TYPE_M36",                         "AS/400 Advanced 36 machine" },
      { "TYPE_M36CFG",                      "AS/400 Advanced 36 machine configuration" },
      { "TYPE_NODGRP",                      "Node group" },
      { "TYPE_NODL",                        "Node list" },
      { "TYPE_NTBD",                        "NetBIOS description" },
      { "TYPE_NWID",                        "Network interface description" },
      { "TYPE_NWID_ATM",                    "Asynchronous transfer mode" },
      { "TYPE_NWID_FR",                     "Frame relay" },
      { "TYPE_NWID_ISDN",                   "ISDN" },
      { "TYPE_NWSCFG",                      "Network server configuration" }, //@K2A
      { "TYPE_NWSD",                        "Network server description" },
      { "TYPE_NWSD_WINDOWSNT",              "Windows" },
      { "TYPE_OUTQ",                        "Output queue" },
      { "TYPE_OVL",                         "Overlay" },
      { "TYPE_PAGDFN",                      "Page definition" },
      { "TYPE_PAGSEG",                      "Page segment" },
      { "TYPE_PDFMAP",                      "PDF map" },    //@K1A
      { "TYPE_PDG",                         "Print descriptor group" },
      { "TYPE_PGM",                         "Program" },
      { "TYPE_PGM_ASM38",                   "Assembler (S/38)" },
      { "TYPE_PGM_BAS",                     "BASIC (OPM)" },
      { "TYPE_PGM_BAS38",                   "BASIC (S/38)" },
      { "TYPE_PGM_C",                       "C (OPM)" },
      { "TYPE_PGM_CBL",                     "COBOL (OPM)" },
      { "TYPE_PGM_CBLLE",                   "COBOL (ILE)" },
      { "TYPE_PGM_CBL36",                   "COBOL (S/36)" },
      { "TYPE_PGM_CBL38",                   "COBOL (S/38)" },
      { "TYPE_PGM_CLE",                     "C (ILE)" },
      { "TYPE_PGM_CLLE",                    "CL (ILE)" },
      { "TYPE_PGM_CLP",                     "CL (OPM)" },
      { "TYPE_PGM_CLP38",                   "CL (S/38)" },
      { "TYPE_PGM_CPPLE",                   "C++ (ILE)" },
      { "TYPE_PGM_CSP",                     "CSP (OPM)" },
      { "TYPE_PGM_DFU",                     "DFU (OPM)" },
      { "TYPE_PGM_DFUEXC",                  "DFU (S/38)" },
      { "TYPE_PGM_DFUNOTEXC",               "DFU (S/38)" },
      { "TYPE_PGM_FTN",                     "FORTRAN (OPM)" },
      { "TYPE_PGM_PAS",                     "Pascal (OPM)" },
      { "TYPE_PGM_PAS38",                   "Pascal (S/38)" },
      { "TYPE_PGM_PLI",                     "PL/I (OPM)" },
      { "TYPE_PGM_PLI38",                   "PL/I (S/38)" },                        // @F6C
      { "TYPE_PGM_RMC",                     "RM/COBOL (OPM)" },
      { "TYPE_PGM_RPG",                     "RPG (OPM)" },
      { "TYPE_PGM_RPGLE",                   "RPG (ILE)" },
      { "TYPE_PGM_RPG36",                   "RPG (S/36)" },
      { "TYPE_PGM_RPG38",                   "RPG (S/38)" },
      { "TYPE_PNLGRP",                      "Panel group" },
      { "TYPE_PRDAVL",                      "Product availability" },
      { "TYPE_PRDDFN",                      "Product definition" },
      { "TYPE_PRDLOD",                      "Product load" },
      { "TYPE_PSFCFG",                      "PSF configuration" },
      { "TYPE_QMFORM",                      "Query management report form" },
      { "TYPE_QMQRY",                       "Query" },
      { "TYPE_QMQRY_PROMPT",                "Prompted" },
      { "TYPE_QMQRY_SQL",                   "SQL" },
      { "TYPE_QRYDFN",                      "Query definition" },
      { "TYPE_RCT",                         "Reference code translate table" },
      { "TYPE_SBSD",                        "Subsystem description" },
      // @F6D { "TYPE_SBSSTS",                      "Subsystem status" },
      { "TYPE_SCHIDX",                      "Search index" },
      { "TYPE_SOCKET",                      "Local socket" },
      { "TYPE_SPADCT",                      "Spelling aid dictionary" },
      { "TYPE_SPADCT_AFRIKAAN",             "Afrikaans" },
      { "TYPE_SPADCT_AKTUEEL",              "Obsolete (Pre-Reform) Dutch" },
      { "TYPE_SPADCT_BRASIL",               "Brazilian Portuguese" },
      { "TYPE_SPADCT_CATALA",               "Catalan" },
      { "TYPE_SPADCT_DANSK",                "Danish" },
      { "TYPE_SPADCT_DEUTSCH",              "German" },
      { "TYPE_SPADCT_DEUTSCH2",             "German Reform" },
      { "TYPE_SPADCT_DSCHWEIZ",             "Swiss-German" },
      { "TYPE_SPADCT_ESPANA",               "Spanish" },
      { "TYPE_SPADCT_FRANCAIS",             "French" },
      { "TYPE_SPADCT_FRA2",                 "French Canadian" },
      { "TYPE_SPADCT_GREEK",                "Greek" },
      { "TYPE_SPADCT_ISLENSK",              "Icelandic" },
      { "TYPE_SPADCT_ITALIANO",             "Italian" },
      { "TYPE_SPADCT_LEGAL",                "US Legal" },
      { "TYPE_SPADCT_MEDICAL",              "US Medical" },
      { "TYPE_SPADCT_NEDERLND",             "Dutch" },
      { "TYPE_SPADCT_NEDPLUS",              "Dutch Reform Permissive" },
      { "TYPE_SPADCT_NORBOK",               "Bokmal Norwegian" },
      { "TYPE_SPADCT_NORNYN",               "Nynorsk Norwegian" },
      { "TYPE_SPADCT_PORTUGAL",             "Portuguese" },
      { "TYPE_SPADCT_RUSSIAN",              "Russian" },
      { "TYPE_SPADCT_SUOMI",                "Finnish" },
      { "TYPE_SPADCT_SVENSK",               "Swedish" },
      { "TYPE_SPADCT_UK",                   "UK English" },
      { "TYPE_SPADCT_US",                   "US English" },
      { "TYPE_SQLPKG",                      "SQL package" },
      { "TYPE_SQLUDT",                      "User-defined SQL type" },
      { "TYPE_SRVPGM",                      "Service program" },
      { "TYPE_SRVPGM_CLE",                  "C" },
      { "TYPE_SRVPGM_CLLE",                 "CL" },
      { "TYPE_SRVPGM_RPGLE",                "RPG" },
      { "TYPE_SRVPGM_CBLLE",                "COBOL" },
      { "TYPE_SRVPGM_CPPLE",                "C++" },
      { "TYPE_SSND",                        "Session description" },
      { "TYPE_STMF",                        "Bytestream file" },
      { "TYPE_SVRSTG",                      "Server storage space" },
      { "TYPE_SYMLNK",                      "Symbolic link" },
      { "TYPE_S36",                         "S/36 environment configuration" },             // @F6C
      { "TYPE_TBL",                         "Table" },
      { "TYPE_TIMZON",                      "Time zone description" },                      //@K1A
      { "TYPE_USRIDX",                      "User index" },
      { "TYPE_USRPRF",                      "User profile" },
      { "TYPE_USRQ",                        "User queue" },
      { "TYPE_USRSPC",                      "User space" },
      { "TYPE_VLDL",                        "Validation list" },
      { "TYPE_WSCST",                       "Workstation customizing object" },
        // @F3A


      // @F4A
      // #TRANNOTE #####################################################
      // #TRANNOTE NetServer component MRI.
      // #TRANNOTE #####################################################

      { "PROP_DESC_NAME",                              "The name of the object." },
      { "PROP_NAME_NAME",                              "name" },
      { "PROP_DESC_ID",                                "The ID of the object." },
      { "PROP_NAME_ID",                                "ID" },

      { "NETSERVER_DESCRIPTION",                       "NetServer" },

         // #TRANNOTE The properties of NetServer.
      { "NETSERVER_ALLOW_SYSTEM_NAME_NAME",            "Allow system name" },
      { "NETSERVER_ALLOW_SYSTEM_NAME_PENDING_NAME",    "Allow system name (pending)" },
      { "NETSERVER_AUTHENTICATION_METHOD_NAME",        "Authentication method" }, // @F8A
      { "NETSERVER_AUTHENTICATION_METHOD_PENDING_NAME", "Authentication method (pending)" }, // @F8A
      { "NETSERVER_AUTOSTART_NAME",                    "Autostart" },
      { "NETSERVER_BROWSING_INTERVAL_NAME",            "Browsing interval" },
      { "NETSERVER_BROWSING_INTERVAL_PENDING_NAME",    "Browsing interval (pending)" },
      { "NETSERVER_CCSID_NAME",                        "CCSID" },
      { "NETSERVER_CCSID_PENDING_NAME",                "CCSID (pending)" },
      { "NETSERVER_DESCRIPTION_NAME",                  "Description" },
      { "NETSERVER_DESCRIPTION_PENDING_NAME",          "Description (pending)" },
      { "NETSERVER_DOMAIN_NAME",                       "Domain name" },
      { "NETSERVER_DOMAIN_PENDING_NAME",               "Domain name (pending)" },
      { "NETSERVER_GUEST_SUPPORT_NAME",                "Guest support" },
      { "NETSERVER_GUEST_SUPPORT_PENDING_NAME",        "Guest support (pending)" },
      { "NETSERVER_GUEST_USER_PROFILE_NAME",           "Guest user profile" },
      { "NETSERVER_GUEST_USER_PROFILE_PENDING_NAME",   "Guest user profile (pending)" },
      { "NETSERVER_IDLE_TIMEOUT_NAME",                 "Idle timeout" },
      { "NETSERVER_IDLE_TIMEOUT_PENDING_NAME",         "Idle timeout (pending)" },
      { "NETSERVER_LOGON_SUPPORT_NAME",                "Logon support" },
      { "NETSERVER_LOGON_SUPPORT_PENDING_NAME",        "Logon support (pending)" },
      { "NETSERVER_NAME_NAME",                         "Name" },
      { "NETSERVER_NAME_PENDING_NAME",                 "Name (pending)" },
      { "NETSERVER_OPPORTUNISTIC_LOCK_TIMEOUT_NAME",   "Opportunistic lock timeout" },
      { "NETSERVER_OPPORTUNISTIC_LOCK_TIMEOUT_PENDING_NAME", "Opportunistic lock timeout (pending)" },
      { "NETSERVER_WINS_ENABLEMENT_NAME",              "WINS enablement" },
      { "NETSERVER_WINS_ENABLEMENT_PENDING_NAME",      "WINS enablement (pending)" },
      { "NETSERVER_WINS_PRIMARY_ADDRESS_NAME",         "Address of primary WINS server" },
      { "NETSERVER_WINS_PRIMARY_ADDRESS_PENDING_NAME", "Address of primary WINS server (pending)" },
      { "NETSERVER_WINS_SCOPE_ID_NAME",                "WINS scope ID" },
      { "NETSERVER_WINS_SCOPE_ID_PENDING_NAME",        "WINS scope ID (pending)" },
      { "NETSERVER_WINS_SECONDARY_ADDRESS_NAME",       "Address of secondary WINS server" },
      { "NETSERVER_WINS_SECONDARY_ADDRESS_PENDING_NAME","Address of secondary WINS server (pending)" },

         // #TRANNOTE The properties of NetServerConnection.
      { "NETSERVER_CONNECTION_DESCRIPTION",            "Connection" },
      { "NETSERVER_TYPE_NAME",                         "Connection type" },
      { "NETSERVER_TYPE_0_NAME",                       "Disk driver" },
      { "NETSERVER_TYPE_1_NAME",                       "Spooled output queue" },
      { "NETSERVER_FILES_OPEN_COUNT_NAME",             "Number of files open" },
      { "NETSERVER_USER_COUNT_NAME",                   "Number of users" },
      { "NETSERVER_CONNECT_TIME_NAME",                 "Connection time" },
      { "NETSERVER_USER_NAME",                         "User name" },

         // #TRANNOTE The properties of NetServerFileShare.
      { "NETSERVER_FILESHARE_DESCRIPTION",             "File share" },
      { "NETSERVER_PATH_NAME",                         "Path" },
      { "NETSERVER_PATH_LENGTH_NAME",                  "Path length" },
      { "NETSERVER_PERMISSION_NAME",                   "Permission" },
      { "NETSERVER_PERMISSION_1_NAME",                 "Read-only" },
      { "NETSERVER_PERMISSION_2_NAME",                 "Read/write" },
      { "NETSERVER_MAXIMUM_USERS_NAME",                "Maximum number of users" },

         // #TRANNOTE The properties of NetServerPrintShare.
      { "NETSERVER_PRINTSHARE_DESCRIPTION",            "Print share" },
      { "NETSERVER_OUTPUT_QUEUE_LIBRARY_NAME",         "Output queue library" },
      { "NETSERVER_OUTPUT_QUEUE_NAME_NAME",            "Output queue name" },
      { "NETSERVER_PRINT_DRIVER_TYPE_NAME",            "Print driver type" },
      { "NETSERVER_SPOOLED_FILE_TYPE_NAME",            "Spooled file type" },
      { "NETSERVER_SPOOLED_FILE_TYPE_1_NAME",          "User ASCII" },
      { "NETSERVER_SPOOLED_FILE_TYPE_2_NAME",          "Advanced function printing" },
      { "NETSERVER_SPOOLED_FILE_TYPE_3_NAME",          "SNA character string" },
      { "NETSERVER_SPOOLED_FILE_TYPE_4_NAME",          "Automatic type sensing" },

         // #TRANNOTE The properties of NetServerSession.
      { "NETSERVER_SESSION_DESCRIPTION",               "Session" },
      { "NETSERVER_CONNECTION_COUNT_NAME",             "Number of connections" },
      { "NETSERVER_FILES_OPEN_COUNT_NAME",             "Number of files open" },
      { "NETSERVER_SESSION_COUNT_NAME",                "Number of sessions" },
      { "NETSERVER_SESSION_TIME_NAME",                 "Session time" },
      { "NETSERVER_IDLE_TIME_NAME",                    "Idle time" },
      { "NETSERVER_IS_GUEST_NAME",                     "Is guest" },
      { "NETSERVER_IS_ENCRYPT_PASSWORD_NAME",          "Is encrypted password used" },

         // #TRANNOTE The properties of NetServerShare.
      { "NETSERVER_SHARE_DESCRIPTION",                 "Share" },


      { "LM_EXCEPTION",     "A license error occurred.  The primary return code is &0.  The secondary return code is &1." }, // @F5A

      // @M1A
      // ME support MRI.   
      // #TRANNOTE ################################################################
      // #TRANNOTE The following are error and informational (verbose) messages.
      // #TRANNOTE ################################################################
      { "ME_ALREADY_LISTENING",          "An active server is already listening to port &0." },
      { "ME_CONNECTION_ACCEPTED",        "&0 accepted connection requested by &1 as connection &2." },
      { "ME_JDBC_DRIVER_NOT_REGISTERED", "JDBC driver not registered: &0"},
      { "ME_JDBC_DRIVER_REGISTERED",     "Registered JDBC driver: &0."},             
      { "ME_OPTION_NOT_VALID",           "Option not valid: &0" },
      { "ME_OPTION_VALUE_NOT_VALID",     "Value for option &0 not valid: &1" },
      { "ME_SERVER_CONTAINER",           "MEServer" },
      { "ME_SERVER_ENDED",               "&0 ended." },
      { "ME_SERVER_LISTENING",           "&0 listening to port &1." },
      { "ME_SERVER_OPTIONSLC",           "options" },
      { "ME_SERVER_OPTIONSUC",           "Options" },
      { "ME_SERVER_SHORTCUTS",           "Shortcuts" },                              
      { "ME_SERVER_STARTED",             "MEServer started." },
      { "ME_CONNECTION_CLOSED",          "Connection &0 closed."},
      { "ME_SERVER_USAGE",               "Usage" },
      { "ME_VALUE_NO_OPTION",            "Value with no option ignored: &0" },      

      { "ME_PCML_LOADING", "Loading new PCML document: &0"},
      { "ME_PCML_ERROR", "Error loading PCML." },
      { "ME_PCML_CACHE", "Using previously cached PCML document: &0"},

      // @M3A @N0A
      // #TRANNOTE ################################################################
      // #TRANNOTE The following are MRI strings for the command documentation generator utility.
      // #TRANNOTE ################################################################
      { "GENCMDDOC_ALLOW_ALL",         "All environments (*ALL)" },
      { "GENCMDDOC_ALLOW_COMPILED_CL_OR_REXX1", "Compiled CL or interpreted REXX (*BPGM *IPGM *BMOD *IMOD *BREXX *IREXX)" },
      { "GENCMDDOC_ALLOW_COMPILED_CL_OR_REXX2", "Compiled CL program or interpreted REXX (*BPGM *IPGM *BREXX *IREXX)" },
      { "GENCMDDOC_ALLOW_INTERACTIVE1",      "Interactive environments (*INTERACT *IPGM *IMOD *IREXX *EXEC)" },
      { "GENCMDDOC_ALLOW_INTERACTIVE2",      "Interactive environments (*INTERACT *IPGM *IREXX *EXEC)" },
      { "GENCMDDOC_ALLOW_JOB_BATCH",         "Batch job (*BATCH)" },
      { "GENCMDDOC_ALLOW_JOB_INTERACTIVE",   "Interactive job (*INTERACT)" },
      { "GENCMDDOC_ALLOW_MODULE_BATCH",      "Batch ILE CL module (*BMOD)" },
      { "GENCMDDOC_ALLOW_MODULE_INTERACTIVE", "Interactive ILE CL module (*IMOD)" },
      { "GENCMDDOC_ALLOW_PROGRAM_BATCH",     "Batch program (*BPGM)" },
      { "GENCMDDOC_ALLOW_PROGRAM_INTERACTIVE", "Interactive program (*IPGM)" },
      { "GENCMDDOC_ALLOW_REXX_BATCH",        "Batch REXX procedure (*BREXX)" },
      { "GENCMDDOC_ALLOW_REXX_INTERACTIVE",  "Interactive REXX procedure (*IREXX)" },
      { "GENCMDDOC_ALLOW_USING_COMMAND_API", "Using QCMDEXEC, QCAEXEC, or QCAPCMD API (*EXEC)" },
      { "GENCMDDOC_CHOICES",                 "Choices" },
      { "GENCMDDOC_DESCRIBE_COMMAND",        "Describe the function provided by the command." },
      { "GENCMDDOC_DESCRIBE_EXAMPLE_1",      "Describe a simple invocation of the command." },
      { "GENCMDDOC_DESCRIBE_EXAMPLE_2",      "Describe a more complex invocation of the command." },
      { "GENCMDDOC_DESCRIBE_OTHER_RESTRICTION",  "Describe other command-level restrictions." },        
      { "GENCMDDOC_DESCRIBE_PARAMETER",      "Describe the function provided by the parameter." }, 
      { "GENCMDDOC_DESCRIBE_PARAMETER_DEFAULT",  "Describe the function provided by the default parameter value." },
      { "GENCMDDOC_DESCRIBE_PARAMETER_VALUE_WITH_RANGE", "Describe the function provided by the range-limited parameter value." },
      { "GENCMDDOC_DESCRIBE_PREDEFINED_PARAMETER_VALUE", "Describe the function provided by the pre-defined parameter value." },
      { "GENCMDDOC_DESCRIBE_USERDEFINED_PARAMETER_VALUE", "Describe the function provided by the user-defined parameter value." },
      { "GENCMDDOC_ELEMENT",                 "Element" },
      { "GENCMDDOC_ERROR_MESSAGES_COMMENT_1", "List the *ESCAPE, *STATUS, and *NOTIFY messages signalled from the command." },     
      { "GENCMDDOC_ERROR_MESSAGES_COMMENT_2", "The following are generic messages defined in message file QCPFMSG." },        
      { "GENCMDDOC_ERROR_MESSAGES_COMMENT_3", "Modify this list to match the list of error messages for the command." },        
      { "GENCMDDOC_ERROR_MESSAGES_HEADING",   "Error messages for &1" },        
      { "GENCMDDOC_ERRORS",                  "Error messages" },
      { "GENCMDDOC_EXAMPLE_1_TITLE",         "Example 1: Simple Command Example" },        
      { "GENCMDDOC_EXAMPLE_2_TITLE",         "Example 2: More Complex Command Example" },        
      { "GENCMDDOC_EXAMPLES",                "Examples" },
      { "GENCMDDOC_EXAMPLES_HEADING",        "Examples for &1" },        
      { "GENCMDDOC_HELP",                    "Help" },
      { "GENCMDDOC_HELP_FOR_COMMAND",        "Help for command" },                 
      { "GENCMDDOC_HELP_FOR_PARAMETER",      "Help for parameter" },        
      { "GENCMDDOC_INTRO_COMMAND_HELP",      "The &1 command <...>" },
      { "GENCMDDOC_INTRO_PARAMETER_HELP",    "Specifies <...>" },
      { "GENCMDDOC_INTRO_EXAMPLE_HELP",      "This command <...>" },
      { "GENCMDDOC_KEY",                     "Key" },
      { "GENCMDDOC_KEYWORD",                 "Keyword" },
      { "GENCMDDOC_LIST_OTHER_AUT",          "List object or data authorities required to run the command." },        
      { "GENCMDDOC_LIST_SPECIAL_AUT",        "List all special authorities required to run the command." },        
      { "GENCMDDOC_LIST_THREADSAFE_RESTRICTIONS",  "If conditionally threadsafe, list threadsafe conditions or restrictions." },    
      { "GENCMDDOC_MULTIPLE_ELEMENT_VALUES_ALLOWED", "You can specify &1 values for this element." },
      { "GENCMDDOC_MULTIPLE_PARAMETER_VALUES_ALLOWED", "You can specify &1 values for this parameter." },
      { "GENCMDDOC_NAME_LOWERCASE",          "name" },
      { "GENCMDDOC_NO_PARAMETERS",           "There are no parameters for this command." },      { "GENCMDDOC_OPTIONAL",                "Optional" },
      { "GENCMDDOC_NONE",                    "None" },
      { "GENCMDDOC_NOTES",                   "Notes" },
      { "GENCMDDOC_PARAMETERS",              "Parameters" },
      { "GENCMDDOC_POSITIONAL",              "Positional" },
      { "GENCMDDOC_QUALIFIER",               "Qualifier" },
      { "GENCMDDOC_REQUIRED",                "Required" },
      { "GENCMDDOC_REQUIRED_PARAMETER",      "This is a required parameter." }, 
      { "GENCMDDOC_RESTRICTION_AUT",         "You must have <...>" },        
      { "GENCMDDOC_RESTRICTION_COMMENT",     "Parameter-level restrictions belong in parameter help sections, not here." },        
      { "GENCMDDOC_RESTRICTION_THREADSAFE",  "This command is conditionally threadsafe, <...>" },        
      { "GENCMDDOC_RESTRICTIONS_HEADING",    "Restrictions" },
      { "GENCMDDOC_SPECIFY_CL_VARIABLE_NAME",  "Specify the CL variable <...>" },      
      { "GENCMDDOC_SPECIFY_COMMAND_STRING",  "Specify the command <...>" },      
      { "GENCMDDOC_SPECIFY_DATE",            "Specify the date <...>" },      
      { "GENCMDDOC_SPECIFY_GENERIC_NAME",    "Specify the generic name of <...>" },      
      { "GENCMDDOC_SPECIFY_NAME",            "Specify the name of <...>" },      
      { "GENCMDDOC_SPECIFY_NUMBER",          "Specify the number of <...>" },      
      { "GENCMDDOC_SPECIFY_PATH_NAME",       "Specify the path name of <...>" },      
      { "GENCMDDOC_SPECIFY_TIME",            "Specify the time <...>" },      
      { "GENCMDDOC_SPECIFY_VALUE",           "Specify the <...>" },
      { "GENCMDDOC_THREADSAFE",              "Threadsafe" },
      { "GENCMDDOC_THREADSAFE_CONDITIONAL",  "Conditional" },
      { "GENCMDDOC_TOP_OF_PAGE",             "Top" },
      { "GENCMDDOC_TYPE_CL_VARIABLE_NAME",   "CL variable name" },
      { "GENCMDDOC_TYPE_COMMAND_STRING",     "Command string" },
      { "GENCMDDOC_TYPE_COMMUNICATIONS_NAME", "Communications name" },
      { "GENCMDDOC_TYPE_DATE",               "Date" },
      { "GENCMDDOC_TYPE_DECIMAL_NUMBER",     "Decimal number" },
      { "GENCMDDOC_TYPE_ELEMENT_LIST",       "Element list" },
      { "GENCMDDOC_TYPE_GENERIC_NAME",       "Generic name" },
      { "GENCMDDOC_TYPE_INTEGER",            "Integer" },
      { "GENCMDDOC_TYPE_NOT_RESTRICTED",     "Not restricted" },
      { "GENCMDDOC_TYPE_PATH_NAME",          "Path name" },
      { "GENCMDDOC_TYPE_QUALIFIED_JOB_NAME", "Qualified job name" },
      { "GENCMDDOC_TYPE_QUALIFIED_OBJECT_NAME", "Qualified object name" },
      { "GENCMDDOC_TYPE_QUALIFIER_LIST",     "Qualifier list" },
      { "GENCMDDOC_TYPE_SIMPLE_NAME",        "Simple name" },
      { "GENCMDDOC_TYPE_TIME",               "Time" },
      { "GENCMDDOC_TYPE_VALUE_LOGICAL",      "Logical value" },
      { "GENCMDDOC_TYPE_VALUE_CHARACTER",    "Character value" },
      { "GENCMDDOC_TYPE_VALUE_HEX",          "Hexadecimal value" },
      { "GENCMDDOC_TYPE_UNSIGNED_INTEGER",   "Unsigned integer" },
      { "GENCMDDOC_UNKNOWN",                 "Unknown" },
      { "GENCMDDOC_VALUE_CHARACTER",         "character-value" },      
      { "GENCMDDOC_VALUE_CL_VARIABLE_NAME",  "CL-variable-name" },     
      { "GENCMDDOC_VALUE_COMMAND_STRING",    "command-string" },       
      { "GENCMDDOC_VALUE_COMMUNICATIONS_NAME",  "communications-name" },  
      { "GENCMDDOC_VALUE_DATE",              "date" },                 
      { "GENCMDDOC_VALUE_DECIMAL_NUMBER",    "decimal-number" },       
      { "GENCMDDOC_VALUE_GENERIC_NAME",      "generic-name" },         
      { "GENCMDDOC_VALUE_HEX",               "hexadecimal-value" },            
      { "GENCMDDOC_VALUE_INTEGER",           "integer" },              
      { "GENCMDDOC_VALUE_LOGICAL",           "logical-value" },        
      { "GENCMDDOC_VALUE_NAME",              "name" },         
      { "GENCMDDOC_VALUE_NOT_RESTRICTED",    "unrestricted-value" },       
      { "GENCMDDOC_VALUE_PATH_NAME",         "path-name" },            
      { "GENCMDDOC_VALUE_SIMPLE_NAME",       "simple-name" },          
      { "GENCMDDOC_VALUE_TIME",              "time" },                 
      { "GENCMDDOC_VALUE_UNSIGNED_INTEGER",  "unsigned-integer" },
      { "GENCMDDOC_VALUES_OTHER",            "Other values" },
      { "GENCMDDOC_VALUES_OTHER_REPEAT",     "Other values (up to &1 repetitions)" },
      { "GENCMDDOC_VALUES_REPEAT",           "Values (up to &1 repetitions)" },
      { "GENCMDDOC_VALUES_SINGLE",           "Single values" },
      { "GENCMDDOC_WHERE_ALLOWED_TO_RUN",    "Where allowed to run" }

   };

}

