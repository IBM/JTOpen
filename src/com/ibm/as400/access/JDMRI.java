///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDMRI.java
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

public class JDMRI extends ListResourceBundle
{
   // NLS_MESSAGEFORMAT_NONE
   // Each string is assumed NOT to be processed by the MessageFormat class.
   // This means that a single quote must be coded as 1 single quote.

   // NLS_ENCODING=UTF-8
   // Instructs the translation tools to interpret the text as UTF-8.

   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE

           // #TRANNOTE JDBC property names.                               // @E2
      { "PROP_NAME_ACCESS", "access" },
      { "PROP_NAME_BEHAVIOR_OVERRIDE", "behaviorOverride" },    // @J5A
      { "PROP_NAME_BIDI_STRING_TYPE", "bidiStringType" },       //@E6A
      { "PROP_NAME_BIG_DECIMAL", "bigDecimal" },
      { "PROP_NAME_BLOCK_CRITERIA", "blockCriteria" },
      { "PROP_NAME_BLOCK_SIZE", "blockSize" },
      { "PROP_NAME_CURSOR_HOLD", "cursorHold"},
      { "PROP_NAME_CURSOR_SENSITIVITY", "cursorSensitivity" },    // @J4A
      { "PROP_NAME_DATABASE_NAME", "databaseName" },
      { "PROP_NAME_DATA_COMPRESSION", "dataCompression" },
      { "PROP_NAME_DATASOURCE_NAME", "dataSourceName" },
      { "PROP_NAME_DATA_TRUNCATION", "dataTruncation" },
      { "PROP_NAME_DATE_FORMAT", "dateFormat" },
      { "PROP_NAME_DATE_SEPARATOR", "dateSeparator" },
      { "PROP_NAME_DECIMAL_SEPARATOR", "decimalSeparator" },
      { "PROP_NAME_DESCRIPTION", "description" },
      { "PROP_NAME_DRIVER", "driver" },                         // @E4A
      { "PROP_NAME_ERRORS", "errors" },
      { "PROP_NAME_EXTENDED_DYNAMIC", "extendedDynamic" },
      { "PROP_NAME_EXTENDED_METADATA", "extendedMetaData" },    // @J2A
      { "PROP_NAME_FULL_OPEN", "fullOpen" },                    // @W1a
      { "PROP_NAME_KEY_RING_NAME", "keyRingName" },         // @E7A
      { "PROP_NAME_KEY_RING_PASSWORD", "keyRingPassword" }, // @E7A
      { "PROP_NAME_LAZY_CLOSE", "lazyClose" },                  // @E3A
      { "PROP_NAME_LIBRARIES", "libraries" },
      { "PROP_NAME_LOB_THRESHOLD", "lobThreshold" },
      { "PROP_NAME_NAMING", "naming" },
      { "PROP_NAME_PACKAGE", "package" },
      { "PROP_NAME_PACKAGE_ADD", "packageAdd" },
      { "PROP_NAME_PACKAGE_CACHE", "packageCache" },
      { "PROP_NAME_PACKAGE_CLEAR", "packageClear" },
      { "PROP_NAME_PACKAGE_CRITERIA", "packageCriteria" },
      { "PROP_NAME_PACKAGE_ERROR", "packageError" },
      { "PROP_NAME_PACKAGE_LIBRARY", "packageLibrary" },
      { "PROP_NAME_PASSWORD", "password" },
      { "PROP_NAME_PREFETCH", "prefetch" },
      { "PROP_NAME_PROMPT", "prompt" },
      { "PROP_NAME_PROXY_SERVER", "proxyServer" },
      { "PROP_NAME_REMARKS", "remarks" },
      { "PROP_NAME_SAVE_PASSWORD_WHEN_SERIALIZED", "savePasswordWhenSerialized" },   //@J3a
      { "PROP_NAME_SECONDARY_URL", "secondaryUrl" },
      { "PROP_NAME_SECURE", "secure" },
      { "PROP_NAME_SERVER_NAME", "serverName" },
      { "PROP_NAME_SORT", "sort" },
      { "PROP_NAME_SORT_LANGUAGE", "sortLanguage" },
      { "PROP_NAME_SORT_TABLE", "sortTable" },
      { "PROP_NAME_SORT_WEIGHT", "sortWeight" },
      { "PROP_NAME_THREAD_USED", "threadUsed" },
      { "PROP_NAME_TIME_FORMAT", "timeFormat" },
      { "PROP_NAME_TIME_SEPARATOR", "timeSeparator" },
      { "PROP_NAME_TRACE", "trace" },
      { "PROP_NAME_TRACE_SERVER", "serverTrace" },                  // @J1a
      { "PROP_NAME_TRACE_TOOLBOX", "toolboxTrace" },                // @K1A
      { "PROP_NAME_TRANSACTION_ISOLATION", "transactionIsolation" },
      { "PROP_NAME_TRANSLATE_BINARY", "translateBinary" },
      { "PROP_NAME_USER", "user" },
      { "PROP_NAME_KEEP_ALIVE", "keepAlive" },
      { "PROP_NAME_RECEIVE_BUFFER_SIZE", "receiveBufferSize" },
      { "PROP_NAME_SEND_BUFFER_SIZE", "sendBufferSize" },
      { "PROP_NAME_SO_LINGER", "soLinger" },
      { "PROP_NAME_SO_TIMEOUT", "soTimeout" },
      { "PROP_NAME_TCP_NO_DELAY", "tcpNoDelay" },
      { "PROP_NAME_PACKAGE_CCSID", "packageCCSID" },                // @M0A
      { "PROP_NAME_MINIMUM_DIVIDE_SCALE", "minimumDivideScale" },   // @M0A
      { "PROP_NAME_MAXIMUM_PRECISION", "maximumPrecision" },        // @M0A
      { "PROP_NAME_MAXIMUM_SCALE", "maximumScale" },                // @M0A
      { "PROP_NAME_TRANSLATE_HEX", "translateHex" },                // @M0A
      { "PROP_NAME_QAQQINILIB", "qaqqiniLibrary" },                 // @K2A
      { "PROP_NAME_LOGIN_TIMEOUT", "loginTimeout" },                // @K4A
      { "PROP_NAME_TRUE_AUTO_COMMIT", "trueAutoCommit"},                 // @KBA //@true
      { "PROP_NAME_HOLD_LOCATORS", "holdInputLocators"},                 // @KBL
      { "PROP_NAME_BIDI_IMPLICIT_REORDERING", "bidiImplicitReordering"}, //@K24
      { "PROP_NAME_BIDI_NUMERIC_ORDERING", "bidiNumericOrdering"},  //@K24
      { "PROP_NAME_HOLD_STATEMENTS", "holdStatements"}, //@KBL
      { "PROP_NAME_ROLLBACK_CURSOR_HOLD", "rollbackCursorHold"},    //@K94
      { "PROP_NAME_VARIABLE_FIELD_COMPRESSION", "variableFieldCompression"}, //@K54
      { "PROP_NAME_QUERY_OPTIMIZE_GOAL", "queryOptimizeGoal"}, //@540
      { "PROP_NAME_XA_LOOSELY_COUPLED_SUPPORT", "xaLooselyCoupledSupport"}, //@540
      { "PROP_NAME_TRANSLATE_BOOLEAN", "translateBoolean"}, //@PDA
      { "PROP_NAME_METADATA_SOURCE", "metaDataSource"}, //@PDA
      { "PROP_NAME_QUERY_STORAGE_LIMIT", "queryStorageLimit"}, //@550
      { "PROP_NAME_DECFLOAT_ROUNDING_MODE", "decfloatRoundingMode"}, //@DFA //@550
      { "PROP_NAME_AUTOCOMMIT_EXCEPTION", "autocommitException"}, //@CE1
      { "PROP_NAME_AUTO_COMMIT", "autoCommit"}, //@AC1
      { "PROP_NAME_IGNORE_WARNINGS", "ignoreWarnings"}, //@igwrn
      { "PROP_NAME_SECURE_CURRENT_USER", "secureCurrentUser"}, //@pw3
      { "PROP_NAME_CONCURRENT_ACCESS_RESOLUTION", "concurrentAccessResolution"}, //@cc1
      { "PROP_NAME_JVM16_SYNCHRONIZE", "jvm16Synchronize"}, //@dmy
      { "PROP_NAME_SOCKET_TIMEOUT", "socketTimeout"}, //@STIMEOUT
      { "PROP_NAME_MAXIMUM_BLOCKED_INPUT_ROWS", "maximumBlockedInputRows"}, 
      { "PROP_NAME_QUERY_TIMEOUT_MECHANISM", "queryTimeoutMechanism" },                // @M0A
      { "PROP_NAME_QUERY_REPLACE_TRUNCATED_PARAMETER", "queryReplaceTruncatedParameter" },                // @M0A
      { "PROP_NAME_NUMERIC_RANGE_ERROR", "numericRangeError"}, 
      { "PROP_NAME_CHARACTER_TRUNCATION", "characterTruncation"},
      { "PROP_NAME_USE_BLOCK_UPDATE","useBlockUpdate"}, 
      { "PROP_NAME_DESCRIBE_OPTION","describeOption"},
      { "PROP_NAME_DECIMAL_DATA_ERRORS","decimalDataErrors"},
      { "PROP_NAME_TIMESTAMP_FORMAT","timestampFormat"},
      { "PROP_NAME_USE_DRDA_METADATA_VERSION","useDrdaMetadataVersion"},
      { "PROP_NAME_PORTNUMBER","portNumber"},
      { "PROP_NAME_ENABLE_CLIENT_AFFINITIES_LIST","enableClientAffinitiesList"},
      { "PROP_NAME_AFFINITY_FAILBACK_INTERVAL", "affinityFailbackInterval"},
      { "PROP_NAME_CLIENT_REROUTE_ALTERNATE_SERVER_NAME", "clientRerouteAlternateServerName"},
      { "PROP_NAME_CLIENT_REROUTE_ALTERNATE_PORT_NUMBER", "clientRerouteAlternatePortNumber"},
      { "PROP_NAME_MAX_RETRIES_FOR_CLIENT_REROUTE", "maxRetriesForClientReroute" },
      { "PROP_NAME_RETRY_INTERVAL_FOR_CLIENT_REROUTE", "retryIntervalForClientReroute"},
      { "PROP_NAME_ENABLE_SEAMLESS_FAILOVER",  "enableSeamlessFailover"}, 
           // #TRANNOTE JDBC property descriptions.
      { "ACCESS_DESC", "Specifies the level of database access for the connection." },
      { "BEHAVIOR_OVERRIDE_DESC", "Specifies the Toolbox JDBC driver behavior to override." },     //@J5A
      { "BIDI_STRING_TYPE_DESC", "Specifies the output string type of bidi data."}, //@E6A
      { "BIG_DECIMAL_DESC", "Specifies whether an intermediate java.math.BigDecimal object is used for packed and zoned decimal conversions." }, // @E0A
      { "BLOCK_CRITERIA_DESC", "Specifies the criteria for retrieving data from the system in blocks of records." },    //@550
      { "BLOCK_SIZE_DESC", "Specifies the block size (in kilobytes) to retrieve from the system and cache on the client." }, //@550
      { "CURSOR_HOLD_DESC", "Specifies whether to hold the cursor across transactions." },      // @E2
      { "CURSOR_SENSITIVITY_DESC", "Specifies the cursor sensitivity to request from the database." },     //@J4A
      { "DATABASE_NAME_DESC", "Specifies the name of the database." },      // @E2
      { "DATA_COMPRESSION_DESC", "Specifies whether result set data is compressed." },   // @D2A
      { "DATASOURCE_NAME_DESC", "Specifies the name of the data source." },      // @E2
      { "DATA_TRUNCATION_DESC", "Specifies whether data truncation exceptions are thrown." },   // @D1A
      { "DATE_FORMAT_DESC", "Specifies the date format used in date literals within SQL statements." },
      { "DATE_SEPARATOR_DESC", "Specifies the date separator used in date literals within SQL statements." },
      { "DECIMAL_SEPARATOR_DESC", "Specifies the decimal separator used in numeric constants within SQL statements." },
      { "DESCRIPTION_DESC", "Specifies the description of the data source." },      // @E2
      { "DRIVER_DESC", "Specifies the JDBC driver implementation." },      // @E4A
      { "ERRORS_DESC", "Specifies the amount of detail to be returned in the message for errors that occur on the system." },//@550
      { "EXTENDED_DYNAMIC_DESC", "Specifies whether to use extended dynamic support." },
      { "EXTENDED_METADATA_DESC", "Specifies whether to request extended metadata from the system." },     //@J2A   @550
      { "FULL_OPEN_DESC", "Specifies whether to use an optimized query." },              // @W1
      { "KEY_RING_NAME_DESC", "Specifies the key ring class name used for SSL communications with the system." }, //@E7A @550
      { "KEY_RING_PASSWORD_DESC", "Specifies the password for the key ring class used for SSL communications with the system." }, //@E7A @550
      { "LAZY_CLOSE_DESC", "Specifies whether to delay closing cursors until subsequent requests." }, // @E3A
      { "LIBRARIES_DESC", "Specifies the libraries to add to the server job's library list." },
      { "LOB_THRESHOLD_DESC", "Specifies the maximum LOB (large object) size (in kilobytes) that can be retrieved as part of a result set." },
      { "NAMING_DESC", "Specifies the naming convention used when referring to tables." },
      { "PACKAGE_DESC", "Specifies the name of the SQL package." },
      { "PACKAGE_ADD_DESC", "Specifies whether to add statements to an existing SQL package." },
      { "PACKAGE_CACHE_DESC", "Specifies whether to cache SQL packages in memory." },
      { "PACKAGE_CLEAR_DESC", "Specifies whether to clear SQL packages when they become full." },
      { "PACKAGE_CRITERIA_DESC", "Specifies the type of SQL statements to be stored in the SQL package" },
      { "PACKAGE_ERROR_DESC", "Specifies the action to take when SQL package errors occur." },
      { "PACKAGE_LIBRARY_DESC", "Specifies the library for the SQL package." },
      { "PASSWORD_DESC", "Specifies the password for connecting to the system." }, //@550
      { "PORTNUMBER_DESC", "Specifies the port number used to connect to the ZDA server."},
      { "PREFETCH_DESC", "Specifies whether to prefetch data when running a SELECT statement." },
      { "PROMPT_DESC", "Specifies whether the user should be prompted if a user name or password is needed to connect to the system." }, //@550
      { "PROXY_SERVER_DESC", "Specifies the host name and (optionally) port number of the middle-tier machine where the proxy server is running." },  //@A2A
      { "REMARKS_DESC", "Specifies the source of the text for REMARKS columns in ResultSet objects returned by DatabaseMetaData methods." },
      { "SAVE_PASSWORD_WHEN_SERIALIZED", "Specifies whether to save the password when the data source object is serialized." },  //@J3a
      { "SECONDARY_URL_DESC", "Specifies the URL that the proxy server should use when establishing a JDBC connection." },  //@A2A
      { "SECURE_DESC", "Specifies whether a Secure Sockets Layer (SSL) connection is used to communicate with the system." }, //@550
      { "SERVER_NAME_DESC", "Specifies the name of the system."},                  // @E2 @550
      { "SORT_DESC", "Specifies how the system sorts records before sending them to the client." }, //@550
      { "SORT_LANGUAGE_DESC", "Specifies a 3-character language ID to use for selection of a sort sequence." },
      { "SORT_TABLE_DESC", "Specifies the library and file name of a sort sequence table stored on the system." }, //@550
      { "SORT_WEIGHT_DESC", "Specifies how the system treats case while sorting records." },                        //@550
      { "THREAD_USED_DESC", "Specifies whether to use threads in communication with the host servers." },  //@E1A
      { "TIME_FORMAT_DESC", "Specifies the time format used in time literals within SQL statements." },
      { "TIMESTAMP_FORMAT_DESC", "Specifies the timestamp format used by JDBC getString methods." },
      { "TIME_SEPARATOR_DESC", "Specifies the time separator used in time literals within SQL statements." },
      { "TRACE_DESC", "Specifies whether trace messages should be logged." },
      { "TRACE_SERVER_DESC", "Specifies whether the job on the system should be traced." },     //@J1a @550
      { "TRACE_TOOLBOX_DESC", "Specifies what category of a toolbox trace to log." },           //@K1A
      { "TRANSACTION_ISOLATION_DESC", "Specifies the default transaction isolation." },
      { "TRANSLATE_BINARY_DESC", "Specifies whether binary data is translated." },
      { "USER_DESC", "Specifies the user name for connecting to the system." }, //@550
      { "KEEP_ALIVE_DESC", "Specifies the socket keep alive value to use when connecting to the system." }, //@550
      { "RECEIVE_BUFFER_SIZE_DESC", "Specifies the socket receive buffer size to use when connecting to the system." }, //@550
      { "SEND_BUFFER_SIZE_DESC", "Specifies the socket send buffer size to use when connecting to the system." }, //@550
      { "SO_LINGER_DESC", "Specifies the socket linger value to use when connecting to the system." }, //@550
      { "SO_TIMEOUT_DESC", "Specifies the socket timeout value to use when connecting to the system." }, //@550
      { "TCP_NO_DELAY_DESC", "Specifies the socket TCP no delay value to use when connecting to the system." }, //@550
      { "PACKAGE_CCSID_DESC", "Specifies the character encoding to use for the SQL package and any statements sent to the system." }, // @M0A @550
      { "MINIMUM_DIVIDE_SCALE_DESC", "Specifies the minimum scale value for the result of decimal division." },                       // @M0A
      { "MAXIMUM_PRECISION_DESC", "Specifies the maximum decimal precision the database should use." },                               // @M0A
      { "MAXIMUM_SCALE_DESC", "Specifies the maximum scale the database should use." },                                               // @M0A
      { "TRANSLATE_HEX_DESC", "Specifies how hexadecimal constants are interpreted." },                                               // @M0A
      { "QAQQINILIB_DESC", "Specifies a QAQQINI library name." },        //@K2A
      { "LOGIN_TIMEOUT_DESC", "Specifies the maximum time in seconds that this data source can wait while attempting to connect to a database." },  //@K4A
      { "TRUE_AUTO_COMMIT_DESC", "Specifies whether the connection should use true auto commit support."}, //@KBA //@true
      { "HOLD_LOCATORS_DESC", "Specifies if input locators should be of type \"hold\" or \"no hold\"."},  //@KBL
      { "BIDI_IMPLICIT_REORDERING_DESC", "Specifies if bidi implicit LTR-RTL reordering should be used."}, //@K24
      { "BIDI_NUMERIC_ORDERING_DESC", "Specifies if the numeric ordering round trip feature should be used."}, //@K24
      { "HOLD_STATEMENTS_DESC", "Specifies if statements should remain open until a transaction boundary."}, //@KBL
      { "ROLLBACK_CURSOR_HOLD_DESC", "Specifies whether to hold cursors across a rollback."}, //@K94
      { "VARIABLE_FIELD_COMPRESSION_DESC", "Specifies whether variable-length fields should be compressed."}, //@K54
      { "QUERY_OPTIMIZE_GOAL_DESC", "Specifies the goal the system should use with optimization of queries."}, //@540 @550
      { "XA_LOOSELY_COUPLED_SUPPORT_DESC", "Specifies whether lock sharing is allowed for loosely coupled transaction branches."}, //@540
      { "TRANSLATE_BOOLEAN_DESC", "Specifies how Boolean objects are interpreted when setting the value for a character field/parameter."}, //@PDA
      { "METADATA_SOURCE_DESC", "Specifies how to retrieve DatabaseMetaData."}, //@PDA
      { "QUERY_STORAGE_LIMIT_DESC", "Specifies the query storage limit to be used when statements in a connection are executed."}, //@550
      { "DECFLOAT_ROUNDING_MODE_DESC", "Specifies the rounding mode to use when working with decfloat data type."}, //@DFA @550
      { "AUTOCOMMIT_EXCEPTION_DESC", "Specifies whether to throw an SQLException when Connection.commit() or Connection.rollback() is called if autocommit is enabled."}, //@CE1
      { "AUTO_COMMIT_DESC", "Specifies whether auto-commit mode is the default connection mode for new connections."}, //@AC1
      { "IGNORE_WARNINGS_DESC", "Specifies a list of SQL states for which the driver should not create warning objects."}, //@igwrn
      { "SECURE_CURRENT_USER_DESC", "Specifies whether to disallow \"\" and *current as user name and password."}, //@pw3
      { "CONCURRENT_ACCESS_RESOLUTION_DESC", "Specifies whether \"currently committed\" access is used on the connection."}, //@cc1
      { "JVM16_SYNCHRONIZE_DESC", "Specifies whether to enable temporary workaround fix for JVM 1.6."}, //@dmy
      { "SOCKET_TIMEOUT_DESC", "Specifies the socket timeout value in milliseconds."}, //@STIMEOUT
      { "USE_BLOCK_UPDATE_DESC", "Specifies the use of a block update mode when inserting or updating blocks of data into the database."},
      { "MAXIMUM_BLOCKED_INPUT_ROWS_DESC", "Specifies the maximum number of rows to be sent to the database engine when using a blocked insert or update operation." },
      { "QUERY_TIMEOUT_MECHANISM_DESC", "Specifies the method used to implement the query timeout."},
      { "TIMESTAMP_FORMAT_DESC", "Specifies the formatting of timestamps returned by getString methods.", ""},
      { "USE_DRDA_METADATA_VERSION_DESC", "Specifies that the DatabaseMetaData.getDatabaseProductVersion return the DRDA information about the database."},
      { "QUERY_REPLACE_TRUNCATED_PARAMETER_DESC", "Specifies the string value to be used when a query parameter is truncated."},
      { "NUMERIC_RANGE_ERROR_DESC", "Specifies the behavior when a numeric range error occurs."},
      { "CHARACTER_TRUNCATION_DESC", "Specifies the behavior when character truncation occurs."},
      { "SECONDARY_URL_DESC", "Specifies the secondary URL to be used for a connection on the middle-tier's DriverManager in a multiple tier environment."},
      {"DESCRIBE_OPTION_DESC","Specifies the type of describe information returned from ther server."},
      {"DECIMAL_DATA_ERRORS_DESC","Specifies how decimal data errors are handled."},
      {"TIMESTAMP_FORMAT_DESC","Specifies the format for timestamps retrieved via getString."},
      {"USE_DRDA_METADATA_VERSION_DESC","Specifies if the DRDA metadata version information should be returned."},
      {"ENABLE_CLIENT_AFFINITIES_LIST_DESC", "Specifies if alternate servers should be used."},
      {"AFFINITY_FAILBACK_INTERVAL_DESC", "Specifies the length, in seconds, of the interval after which the primary connection will be re-established."},
      {"CLIENT_REROUTE_ALTERNATE_SERVER_NAME_DESC", "Specifies the list of alternate servers."},
      {"CLIENT_REROUTE_ALTERNATE_PORT_NUMBER_DESC", "Specifies the list of alternate port numbers."}, 
      {"MAX_RETRIES_FOR_CLIENT_REROUTE_DESC", "The maximum number of connection retries for automatic client reroute."},
      {"RETRY_INTERVAL_FOR_CLIENT_REROUTE_DESC","The number of seconds between consecutive connection retries."},
      {"ENABLE_SEAMLESS_FAILOVER_DESC","Specifies whether the JTOpen JDBC driver uses seamless failover for client reroute."}, 
      
      // JDBC 2 - Optional Package support - RowSet    @E5
      { "PROP_NAME_RS_COMMAND", "command" },
      { "PROP_NAME_RS_CONCURRENCY", "concurrency" },
      { "PROP_NAME_RS_ESCAPE_PROCESSING", "escapeProcessing" },
      { "PROP_NAME_RS_FETCH_DIRECTION", "fetchDirection" },
      { "PROP_NAME_RS_FETCH_SIZE", "fetchSize" },
      { "PROP_NAME_RS_MAX_FIELD_SIZE", "maxFieldSize" },
      { "PROP_NAME_RS_MAX_ROWS", "maxRows" },
      { "PROP_NAME_RS_QUERY_TIMEOUT", "queryTimeout" },
      { "PROP_NAME_RS_READ_ONLY", "readOnly" },
      { "PROP_NAME_RS_TYPE", "type" },
      { "PROP_NAME_RS_URL", "url" },
      { "PROP_NAME_RS_USE_DATA_SOURCE", "useDataSource" },
      { "PROP_NAME_RS_USERNAME", "username" },

      { "PROP_DESC_RS_COMMAND", "Specifies the command used to populate the rowset."},
      { "PROP_DESC_RS_CONCURRENCY", "Specifies the result set concurrency type."},
      { "PROP_DESC_RS_ESCAPE_PROCESSING", "Specifies whether the escape scanning is enabled for escape substitution processing."},
      { "PROP_DESC_RS_FETCH_DIRECTION", "Specifies the direction in which the rows in a result set are processed."},
      { "PROP_DESC_RS_FETCH_SIZE", "Specifies the number of rows to be fetched from the database."},
      { "PROP_DESC_RS_MAX_FIELD_SIZE", "Specifies the maximum column size for the database field."},
      { "PROP_DESC_RS_MAX_ROWS", "Specifies the maximum row limit for the rowset."},
      { "PROP_DESC_RS_QUERY_TIMEOUT", "Specifies the maximum wait time in seconds for the statement to execute."},
      { "PROP_DESC_RS_READ_ONLY", "Specifies whether the rowset is read-only."},
      { "PROP_DESC_RS_TYPE", "Specifies the result set type."},
      { "PROP_DESC_RS_URL", "Specifies the URL used for getting a connection."},
      { "PROP_DESC_RS_USE_DATA_SOURCE", "Specifies whether the data source is used to make a connection to the database."},

           // #TRANNOTE JDBC exception and warning messages.
      { "JD08001", "The application requester cannot establish the connection." }, //@D3A
      { "JD08004", "The application server rejected the connection." },            //@D3A


   };


}

