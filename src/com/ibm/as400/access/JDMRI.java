///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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
Locale-specific objects for the AS/400 Toolbox for Java.
**/

public class JDMRI extends ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


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



           // #TRANNOTE JDBC property descriptions.
      { "ACCESS_DESC", "Specifies the level of database access for the connection." },
      { "BLOCK_CRITERIA_DESC", "Specifies the criteria for retrieving data from the AS/400 server in blocks of records." },
      { "BLOCK_SIZE_DESC", "Specifies the block size (in kilobytes) to retrieve from the AS/400 server and cache on the client." },
      { "DATA_COMPRESSION_DESC", "Specifies whether result set data is compressed." },   // @D2A
      { "DATA_TRUNCATION_DESC", "Specifies whether data truncation exceptions are thrown." },   // @D1A
      { "DATE_FORMAT_DESC", "Specifies the date format used in date literals within SQL statements." },
      { "DATE_SEPARATOR_DESC", " Specifies the date separator used in date literals within SQL statements." },
      { "DECIMAL_SEPARATOR_DESC", "Specifies the decimal separator used in numeric constants within SQL statements." },
      { "ERRORS_DESC", "Specifies the amount of detail to be returned in the message for errors that occur on the AS/400 server." },
      { "EXTENDED_DYNAMIC_DESC", "Specifies whether to use extended dynamic support." },
      { "LIBRARIES_DESC", "Specifies the AS/400 libraries to add to the server job's library list." },
      { "LOB_THRESHOLD_DESC", "Specifies the maximum LOB (large object) size (in kilobytes) that can be retrieved as part of a result set." },
      { "NAMING_DESC", "Specifies the naming convention used when referring to tables." },
      { "PACKAGE_DESC", "Specifies the name of the SQL package." },
      { "PACKAGE_ADD_DESC", "Specifies whether to add statements to an existing SQL package." },
      { "PACKAGE_CACHE_DESC", "Specifies whether to cache SQL packages in memory." },
      { "PACKAGE_CLEAR_DESC", "Specifies whether to clear SQL packages when they become full." },
      { "PACKAGE_CRITERIA_DESC", "Specifies the type of SQL statements to be stored in the SQL package" },
      { "PACKAGE_ERROR_DESC", "Specifies the action to take when SQL package errors occur." },
      { "PACKAGE_LIBRARY_DESC", "Specifies the library for the SQL package." },
      { "PASSWORD_DESC", "Specifies the password for connecting to the AS/400 server." },
      { "PREFETCH_DESC", "Specifies whether to prefetch data when running a SELECT statement." },
      { "PROMPT_DESC", "Specifies whether the user should be prompted if a user name or password is needed to connect to the AS/400 server." },
      { "PROXY_SERVER_DESC", "Specifies the host name and (optionally) port number of the middle-tier machine where the proxy server is running." },  //@A2A
    //{ "PROXY_SERVER_SECURE_DESC", "Specifies whether a Secure Sockets Layer (SSL) connection is used for communication between the client and the proxy server." },  //@A2A
      { "REMARKS_DESC", "Specifies the source of the text for REMARKS columns in ResultSet objects returned by DatabaseMetaData methods." },
      { "SECONDARY_URL_DESC", "Specifies the URL that the proxy server should use when establishing a JDBC connection." },  //@A2A
      { "SECURE_DESC", "Specifies whether a Secure Sockets Layer (SSL) connection is used to communicate with the server." },
      { "SORT_DESC", "Specifies how the server sorts records before sending them to the client." },
      { "SORT_LANGUAGE_DESC", "Specifies a 3-character language ID to use for selection of a sort sequence." },
      { "SORT_TABLE_DESC", "Specifies the library and file name of a sort sequence table stored on the AS/400 server." },
      { "SORT_WEIGHT_DESC", "Specifies how the server treats case while sorting records." },
      { "TIME_FORMAT_DESC", "Specifies the time format used in time literals within SQL tatements." },
      { "TIME_SEPARATOR_DESC", "Specifies the time separator used in time literals within SQL statements." },
      { "TRACE_DESC", "Specifies whether trace messages should be logged." },
      { "TRANSACTION_ISOLATION_DESC", "Specifies the default transaction isolation." },
      { "TRANSLATE_BINARY_DESC", "Specifies whether binary data is translated." },
      { "USER_DESC", "Specifies the user name for connecting to the AS/400 server." },



           // #TRANNOTE JDBC exception and warning messages.
      { "JD08001", "The application requester cannot establish the connection." }, //@D3A
      { "JD08004", "The application server rejected the connection." },            //@D3A


   };


}

