///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JDMRI.java
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
Locale-specific objects for the IBM Toolbox for Java.
**/

public class JDMRI extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


   public Object[][] getContents()
   {
       return resources;
   }

   private final static Object[][] resources= {
           // #
           // # 5769-SS1
           // # (C) Copyright IBM Corp. 1997
           // # All rights reserved.
           // # US Government Users Restricted Rights -
           // # Use, duplication, or disclosure restricted
           // # by GSA ADP Schedule Contract with IBM Corp.
           // #
           // # Licensed Materials - Property of IBM
           // #
           // #TRANNOTE NOTE TO TRANSLATORS: The format of a line of MRI
           // #TRANNOTE    is { "KEY", "value" },
           // #TRANNOTE
           // #TRANNOTE The key must be left alone so translate only the value.
           // #TRANNOTE

           // #TRANNOTE JDBC property names.                               // @E2
      { "PROP_NAME_ACCESS", "access" },
      { "PROP_NAME_BIDI_STRING_TYPE", "bidiStringType" },       //@E6A
      { "PROP_NAME_BIG_DECIMAL", "bigDecimal" },
      { "PROP_NAME_BLOCK_CRITERIA", "blockCriteria" },
      { "PROP_NAME_BLOCK_SIZE", "blockSize" },
      { "PROP_NAME_CURSOR_HOLD", "cursorHold"},
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
      { "PROP_NAME_FULL_OPEN", "fullOpen" },                    // @W1a
      { "PROP_NAME_KEY_RING_NAME", "keyRingName" },		// @E7A
      { "PROP_NAME_KEY_RING_PASSWORD", "keyRingPassword" },	// @E7A
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
      { "PROP_NAME_TRANSACTION_ISOLATION", "transactionIsolation" },
      { "PROP_NAME_TRANSLATE_BINARY", "translateBinary" },
      { "PROP_NAME_USER", "user" },

           // #TRANNOTE JDBC property descriptions.
      { "ACCESS_DESC", "Specifies the level of database access for the connection." },
      { "BIDI_STRING_TYPE_DESC", "Specifies the output string type of bidi data."}, //@E6A
      { "BIG_DECIMAL_DESC", "Specifies whether an intermediate java.math.BigDecimal object is used for packed and zoned decimal conversions." }, // @E0A
      { "BLOCK_CRITERIA_DESC", "Specifies the criteria for retrieving data from the server in blocks of records." },
      { "BLOCK_SIZE_DESC", "Specifies the block size (in kilobytes) to retrieve from the server and cache on the client." },
      { "CURSOR_HOLD_DESC", "Specifies whether to hold the cursor across transactions." },      // @E2
      { "DATABASE_NAME_DESC", "Specifies the name of the database." },      // @E2
      { "DATA_COMPRESSION_DESC", "Specifies whether result set data is compressed." },   // @D2A
      { "DATASOURCE_NAME_DESC", "Specifies the name of the data source." },      // @E2
      { "DATA_TRUNCATION_DESC", "Specifies whether data truncation exceptions are thrown." },   // @D1A
      { "DATE_FORMAT_DESC", "Specifies the date format used in date literals within SQL statements." },
      { "DATE_SEPARATOR_DESC", "Specifies the date separator used in date literals within SQL statements." },
      { "DECIMAL_SEPARATOR_DESC", "Specifies the decimal separator used in numeric constants within SQL statements." },
      { "DESCRIPTION_DESC", "Specifies the description of the data source." },      // @E2
      { "DRIVER_DESC", "Specifies the JDBC driver implementation." },      // @E4A
      { "ERRORS_DESC", "Specifies the amount of detail to be returned in the message for errors that occur on the server." },
      { "EXTENDED_DYNAMIC_DESC", "Specifies whether to use extended dynamic support." },
      { "FULL_OPEN_DESC", "Specifies whether to use an optimized query." },              // @W1
      { "KEY_RING_NAME_DESC", "Specifies the key ring class name used for SSL communications with the server." }, //@E7A
      { "KEY_RING_PASSWORD_DESC", "Specifies the password for the key ring class used for SSL communications with the server." }, //@E7A
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
      { "PASSWORD_DESC", "Specifies the password for connecting to the server." },
      { "PREFETCH_DESC", "Specifies whether to prefetch data when running a SELECT statement." },
      { "PROMPT_DESC", "Specifies whether the user should be prompted if a user name or password is needed to connect to the server." },
      { "PROXY_SERVER_DESC", "Specifies the host name and (optionally) port number of the middle-tier machine where the proxy server is running." },  //@A2A
    //{ "PROXY_SERVER_SECURE_DESC", "Specifies whether a Secure Sockets Layer (SSL) connection is used for communication between the client and the proxy server." },  //@A2A
      { "REMARKS_DESC", "Specifies the source of the text for REMARKS columns in ResultSet objects returned by DatabaseMetaData methods." },
      { "SECONDARY_URL_DESC", "Specifies the URL that the proxy server should use when establishing a JDBC connection." },  //@A2A
      { "SECURE_DESC", "Specifies whether a Secure Sockets Layer (SSL) connection is used to communicate with the server." },
      { "SERVER_NAME_DESC", "Specifies the name of the server."},                  // @E2
      { "SORT_DESC", "Specifies how the server sorts records before sending them to the client." },
      { "SORT_LANGUAGE_DESC", "Specifies a 3-character language ID to use for selection of a sort sequence." },
      { "SORT_TABLE_DESC", "Specifies the library and file name of a sort sequence table stored on the server." },
      { "SORT_WEIGHT_DESC", "Specifies how the server treats case while sorting records." },
      { "THREAD_USED_DESC", "Specifies whether to use threads in communication with the host servers." },  //@E1A
      { "TIME_FORMAT_DESC", "Specifies the time format used in time literals within SQL statements." },
      { "TIME_SEPARATOR_DESC", "Specifies the time separator used in time literals within SQL statements." },
      { "TRACE_DESC", "Specifies whether trace messages should be logged." },
      { "TRACE_SERVER_DESC", "Specifies whether the job on the server should be traced." },     //@J1a
      { "TRANSACTION_ISOLATION_DESC", "Specifies the default transaction isolation." },
      { "TRANSLATE_BINARY_DESC", "Specifies whether binary data is translated." },
      { "USER_DESC", "Specifies the user name for connecting to the server." },

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

