///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: JDProperties.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.Serializable;
import java.io.IOException;                                // @W2a
import java.sql.DriverPropertyInfo;
import java.util.Enumeration;
import java.util.Properties;



/**
<p>A class representing the properties passed as connection
attributes for the driver.
**/
//
// Implementation notes:
//
// 1. If adding or changing any properties, please update the
//    following:
//
//    __ Update JDProperties.java (this source file).  NEW PROPERTIES
//       MUST BE ADDED TO THE END OF THE ARRAY (see @W2)
//
//    __ Add an entry to JDBCProperties.html.  Contact ID to
//       change this file.
//
//    __ Add entries to JDMRI.java.  The description entry should
//       match the first sentence in the description in
//       JDBCProperties.html.
//
//    __ Add getXXX/setXXX methods to AS400JDBCDataSource.java.
//
//    __ Add a property to AS400JDBCDataSourceBeanInfo.java.
//
//    __ Update the testcase JDDriverGetPropertyInfo.java to
//       reflect the new number of properties.
//
//    __ Test serialization
//
// 2. We only store the key to the descriptions.  We only
//    load the actual descriptions when the caller asks
//    for them.  This saves the descriptions from being
//    loaded in all cases.
//
class JDProperties implements Serializable
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;


    // Callers should access the properties using one of the
    // following indicies.
    //
    // @W2 New properties must be added to the end of the list!!!
    //
    static final int              ACCESS                  = 0;
    static final int              BLOCK_SIZE              = 1;
    static final int              BLOCK_CRITERIA          = 2;
    static final int              DATE_FORMAT             = 3;
    static final int              DATE_SEPARATOR          = 4;
    static final int              DECIMAL_SEPARATOR       = 5;
    static final int              ERRORS                  = 6;
    static final int              EXTENDED_DYNAMIC        = 7;
    static final int              LIBRARIES               = 8;
    static final int              NAMING                  = 9;
    static final int              PACKAGE                 = 10;
    static final int              PACKAGE_ADD             = 11;
    static final int              PACKAGE_CACHE           = 12;
    static final int              PACKAGE_CLEAR           = 13;
    static final int              PACKAGE_ERROR           = 14;
    static final int              PACKAGE_LIBRARY         = 15;
    static final int              PASSWORD                = 16;
    static final int              PREFETCH                = 17;
    static final int              PROMPT                  = 18;
    static final int              REMARKS                 = 19;
    static final int              SORT                    = 20;
    static final int              SORT_LANGUAGE           = 21;
    static final int              SORT_TABLE              = 22;
    static final int              SORT_WEIGHT             = 23;
    static final int              TIME_FORMAT             = 24;
    static final int              TIME_SEPARATOR          = 25;
    static final int              TRACE                   = 26;
    static final int              TRANSACTION_ISOLATION   = 27;
    static final int              TRANSLATE_BINARY        = 28;
    static final int              USER                    = 29;
    static final int              PACKAGE_CRITERIA        = 30;   // @A0A
    static final int              LOB_THRESHOLD           = 31;
    static final int              SECURE                  = 32;
    static final int              DATA_TRUNCATION         = 33;   // @C1A
    static final int              PROXY_SERVER            = 34;   // @A3A
    //static final int            PROXY_SERVER_SECURE     = 35;   // @A3A
    static final int              SECONDARY_URL           = 35;   // @A3A
    static final int              DATA_COMPRESSION        = 36;   // @D0A
    static final int              BIG_DECIMAL             = 37;   // @E0A
    static final int              THREAD_USED             = 38;   // @E1A
    static final int              CURSOR_HOLD             = 39;   // @D1A
    static final int              LAZY_CLOSE              = 40;   // @E2A
    static final int              DRIVER                  = 41;   // @E3A
    static final int              BIDI_STRING_TYPE        = 42;   // @E9A
    static final int              KEY_RING_NAME           = 43;   // @F1A
    static final int              KEY_RING_PASSWORD       = 44;   // @F1A
    static final int              FULL_OPEN               = 45;   // @W1a
    static final int              TRACE_SERVER            = 46;   // @j1a
    static final int              DATABASE_NAME           = 47;   // @j2a
    static final int              EXTENDED_METADATA       = 48;   // @F5A
    static final int              CURSOR_SENSITIVITY      = 49;   // @F6A
    static final int              BEHAVIOR_OVERRIDE       = 50;   // @F7A
    static final int              PACKAGE_CCSID           = 51;   // @M0A - support sending SQL to server in UTF-16 and storing it in a UTF-16 package
    static final int              MINIMUM_DIVIDE_SCALE    = 52;   // @M0A - support 63 digit decimal precision
    static final int              MAXIMUM_PRECISION       = 53;
    static final int              MAXIMUM_SCALE           = 54;
    static final int              TRANSLATE_HEX           = 55;   // @M0A - support hex constant parser option
    static final int              TRACE_TOOLBOX           = 56;   // @K1A - support to allow a toolbox trace

    // @W2 always add to the end of the array!

    private static final int    NUMBER_OF_ATTRIBUTES_ = 57;    // @A0C @C1C @A3A @D0C @E0C
                                                               // @E1C @D1c @E2C @E3C @E9C @F1C
                                                               // @W1c @j1c @J2c @F5C @F6C @F7c @M0C @K1C



    // Property names.
    private static final String ACCESS_                 = "access";
    private static final String BEHAVIOR_OVERRIDE_      = "behavior override";      // @F7A
    private static final String BIDI_STRING_TYPE_       = "bidi string type";       // @E9A
    private static final String BIG_DECIMAL_            = "big decimal";            // @E0A
    private static final String BLOCK_SIZE_             = "block size";
    private static final String BLOCK_CRITERIA_         = "block criteria";
    private static final String CURSOR_HOLD_            = "cursor hold";            // @D1
    private static final String CURSORHOLD_             = "CURSORHOLD";             // @D1
    private static final String CURSOR_SENSITIVITY_     = "cursor sensitivity";     // @F6A
    private static final String DATA_COMPRESSION_       = "data compression";       // @D0A
    private static final String DATA_TRUNCATION_        = "data truncation";        // @C1A
    private static final String DATABASE_NAME_          = "database name";          // @J2A
    private static final String DATE_FORMAT_            = "date format";
    private static final String DATE_SEPARATOR_         = "date separator";
    private static final String DECIMAL_SEPARATOR_      = "decimal separator";
    private static final String DRIVER_                 = "driver";                 // @E3A
    private static final String ERRORS_                 = "errors";
    private static final String EXTENDED_DYNAMIC_       = "extended dynamic";
    private static final String EXTENDED_METADATA_      = "extended metadata";      // @F5A
    private static final String FULL_OPEN_              = "full open";              // @W1a
    private static final String KEY_RING_NAME_          = "key ring name";          // @F1A
    private static final String KEY_RING_PASSWORD_      = "key ring password";      // @F1A
    private static final String LAZY_CLOSE_             = "lazy close";             // @E2A
    private static final String LIBRARIES_              = "libraries";
    private static final String LOB_THRESHOLD_          = "lob threshold";
    private static final String MAXIMUM_PRECISION_      = "maximum precision";      // @M0A
    private static final String MAXIMUM_SCALE_          = "maximum scale";          // @M0A
    private static final String MINIMUM_DIVIDE_SCALE_   = "minimum divide scale";   // @M0A
    private static final String NAMING_                 = "naming";
    private static final String PACKAGE_                = "package";
    private static final String PACKAGE_ADD_            = "package add";
    private static final String PACKAGE_CACHE_          = "package cache";
    private static final String PACKAGE_CCSID_          = "package ccsid";          // @M0A
    private static final String PACKAGE_CLEAR_          = "package clear";
    private static final String PACKAGE_CRITERIA_       = "package criteria";       // @A0A
    private static final String PACKAGE_ERROR_          = "package error";
    private static final String PACKAGE_LIBRARY_        = "package library";
    private static final String PASSWORD_               = "password";
    private static final String PREFETCH_               = "prefetch";
    private static final String PROMPT_                 = "prompt";
    private static final String PROXY_SERVER_           = "proxy server";           // @A3A
    //private static final String PROXY_SERVER_SECURE_    = "proxy server secure";    // @A3A
    private static final String REMARKS_                = "remarks";
    static final String SECONDARY_URL_                  = "secondary URL";          // @A3A
    private static final String SECURE_                 = "secure";
    private static final String SORT_                   = "sort";
    private static final String SORT_LANGUAGE_          = "sort language";
    private static final String SORT_TABLE_             = "sort table";
    private static final String SORT_WEIGHT_            = "sort weight";
    private static final String THREAD_USED_            = "thread used";            // @E1A
    private static final String TIME_FORMAT_            = "time format";
    private static final String TIME_SEPARATOR_         = "time separator";
    private static final String TRACE_                  = "trace";
    private static final String TRACE_SERVER_           = "server trace";           // @j1a
    private static final String TRACE_TOOLBOX_          = "toolbox trace";          // @K1A
    private static final String TRANSACTION_ISOLATION_  = "transaction isolation";
    private static final String TRANSLATE_BINARY_       = "translate binary";
    private static final String TRANSLATE_HEX_          = "translate hex";          // @M0A
    private static final String USER_                   = "user";




    // Common String objects.  Using these will theoretically
    // cut down on the number of String allocations.
    //
    private static final String COMMA_                  = ",";
    private static final String EMPTY_                  = "";
    private static final String EUR_                    = "eur";
    private static final String FALSE_                  = "false";
    private static final String ISO_                    = "iso";
    private static final String JIS_                    = "jis";
    private static final String NONE_                   = "none";
    private static final String PERIOD_                 = ".";
    private static final String SPACE_                  = "b";
    private static final String SQL_                    = "sql";
    private static final String SYSTEM_                 = "system";
    private static final String TRUE_                   = "true";
    private static final String USA_                    = "usa";



    // Callers compare property values against valid choices
    // using the following constants.
    //
    static final String         ACCESS_ALL                      = "all";
    static final String         ACCESS_READ_CALL                = "read call";
    static final String         ACCESS_READ_ONLY                = "read only";

    static final String         BLOCK_CRITERIA_NONE             = "0";
    static final String         BLOCK_CRITERIA_IF_FETCH         = "1";
    static final String         BLOCK_CRITERIA_UNLESS_UPDATE    = "2";

    static final String         BLOCK_SIZE_0                    = "0";
    static final String         BLOCK_SIZE_8                    = "8";
    static final String         BLOCK_SIZE_16                   = "16";
    static final String         BLOCK_SIZE_32                   = "32";
    static final String         BLOCK_SIZE_64                   = "64";
    static final String         BLOCK_SIZE_128                  = "128";
    static final String         BLOCK_SIZE_256                  = "256";
    static final String         BLOCK_SIZE_512                  = "512";

    static final String         CURSOR_SENSITIVITY_ASENSITIVE   = "asensitive";   //@F6A
    static final String         CURSOR_SENSITIVITY_INSENSITIVE  = "insensitive";   //@F6A
    static final String         CURSOR_SENSITIVITY_SENSITIVE    = "sensitive";   //@F6A

    static final String         DATE_FORMAT_JULIAN              = "julian";
    static final String         DATE_FORMAT_MDY                 = "mdy";
    static final String         DATE_FORMAT_DMY                 = "dmy";
    static final String         DATE_FORMAT_YMD                 = "ymd";
    static final String         DATE_FORMAT_USA                 = "usa";
    static final String         DATE_FORMAT_ISO                 = ISO_;
    static final String         DATE_FORMAT_EUR                 = EUR_;
    static final String         DATE_FORMAT_JIS                 = JIS_;
    static final String         DATE_FORMAT_NOTSET              = EMPTY_;

    static final String         DATE_SEPARATOR_SLASH            = "/";
    static final String         DATE_SEPARATOR_DASH             = "-";
    static final String         DATE_SEPARATOR_PERIOD           = PERIOD_;
    static final String         DATE_SEPARATOR_COMMA            = COMMA_;
    static final String         DATE_SEPARATOR_SPACE            = SPACE_;
    static final String         DATE_SEPARATOR_NOTSET           = EMPTY_;

    static final String         DECIMAL_SEPARATOR_COMMA         = COMMA_;
    static final String         DECIMAL_SEPARATOR_PERIOD        = PERIOD_;
    static final String         DECIMAL_SEPARATOR_NOTSET        = EMPTY_;

    //@F4D static final String         DRIVER_DEFAULT                  = "default";        // @E3A
    static final String         DRIVER_NATIVE                   = "native";         // @E3A
    static final String         DRIVER_TOOLBOX                  = "toolbox";        // @E3A

    static final String         ERRORS_BASIC                    = "basic";
    static final String         ERRORS_FULL                     = "full";

    static final String         NAMING_SQL                      = SQL_;
    static final String         NAMING_SYSTEM                   = SYSTEM_;

    static final String         PACKAGE_ERROR_EXCEPTION         = "exception";
    static final String         PACKAGE_ERROR_NONE              = NONE_;
    static final String         PACKAGE_ERROR_WARNING           = "warning";

    static final String         REMARKS_SQL                     = SQL_;
    static final String         REMARKS_SYSTEM                  = SYSTEM_;

    static final String         SORT_HEX                        = "hex";
    static final String         SORT_JOB                        = "job";
    static final String         SORT_LANGUAGE1                  = "language";
    static final String         SORT_TABLE1                     = "table";

    static final String         SORT_LANGUAGE_ENGLISH_UNITED_STATES     = "ENU";        // @E4A

    static final String         SORT_WEIGHT_SHARED              = "shared";
    static final String         SORT_WEIGHT_UNIQUE              = "unique";

    static final String         TIME_FORMAT_HMS                 = "hms";
    static final String         TIME_FORMAT_USA                 = USA_;
    static final String         TIME_FORMAT_ISO                 = ISO_;
    static final String         TIME_FORMAT_EUR                 = EUR_;
    static final String         TIME_FORMAT_JIS                 = JIS_;
    static final String         TIME_FORMAT_NOTSET              = EMPTY_;

    static final String         TIME_SEPARATOR_COLON            = ":";
    static final String         TIME_SEPARATOR_PERIOD           = PERIOD_;
    static final String         TIME_SEPARATOR_COMMA            = COMMA_;
    static final String         TIME_SEPARATOR_SPACE            = SPACE_;
    static final String         TIME_SEPARATOR_NOTSET           = EMPTY_;

    static final String         TRANSACTION_ISOLATION_NONE                  = NONE_;
    static final String         TRANSACTION_ISOLATION_READ_COMMITTED        = "read committed";
    static final String         TRANSACTION_ISOLATION_READ_UNCOMMITTED      = "read uncommitted";
    static final String         TRANSACTION_ISOLATION_REPEATABLE_READ       = "repeatable read";
    static final String         TRANSACTION_ISOLATION_SERIALIZABLE          = "serializable";

    static final String         PACKAGE_CRITERIA_DEFAULT           = "default";   // @A0A
    static final String         PACKAGE_CRITERIA_SELECT            = "select";    // @A0A

    static final String              CURSORHOLD_FALSE             = "FALSE";    // @D1
    static final String              CURSORHOLD_TRUE              = "TRUE";     // @D1
    private static final String      CURSORHOLD_NO                = "0";        // @D1
    private static final String      CURSORHOLD_YES               = "1";        // @D1

    static final String              TRACE_SET_ON                 = "TRUE";           // @E7
    static final String              TRACE_SET_OFF                = "FALSE";          // @E7
    static final String              TRACE_NOT_SPECIFIED          = "NOT SPECIFIED";  // @E7

    static final String              NOT_SPECIFIED               = "";               // @E8A

    static final String              BIDI_STRING_TYPE_NOTSET     = EMPTY_;      // @E9A
    static final String              BIDI_STRING_TYPE_DEFAULT    = "0";              // @E9A
    static final String              BIDI_STRING_TYPE_ST4    = "4";             // @E9A
    static final     String             BIDI_STRING_TYPE_ST5        = "5";              // @E9A
    static final String              BIDI_STRING_TYPE_ST6        = "6";              // @E9A
    static final String              BIDI_STRING_TYPE_ST7        = "7";              // @E9A
    static final String              BIDI_STRING_TYPE_ST8        = "8";              // @E9A
    static final String              BIDI_STRING_TYPE_ST9        = "9";              // @E9A
    static final String              BIDI_STRING_TYPE_ST10       = "10";             // @E9A
    static final String              BIDI_STRING_TYPE_ST11       = "11";             // @E9A

    static final String              PACKAGE_CCSID_UCS2          = "13488";          // @M0A - support sending SQL statements in UTF-16
    static final String              PACKAGE_CCSID_UTF16         = "1200";           // and consequently storing them in package in that CCSID


    static final String              MINIMUM_DIVIDE_SCALE_0      = "0";              // @M0A - support 63 digit decimal precision
    static final String              MINIMUM_DIVIDE_SCALE_1      = "1";
    static final String              MINIMUM_DIVIDE_SCALE_2      = "2";
    static final String              MINIMUM_DIVIDE_SCALE_3      = "3";
    static final String              MINIMUM_DIVIDE_SCALE_4      = "4";
    static final String              MINIMUM_DIVIDE_SCALE_5      = "5";
    static final String              MINIMUM_DIVIDE_SCALE_6      = "6";
    static final String              MINIMUM_DIVIDE_SCALE_7      = "7";
    static final String              MINIMUM_DIVIDE_SCALE_8      = "8";
    static final String              MINIMUM_DIVIDE_SCALE_9      = "9";

    static final String              MAXIMUM_PRECISION_31        = "31";
    static final String              MAXIMUM_PRECISION_63        = "63";

    static final String              MAXIMUM_SCALE_0             = "0";
    static final String              MAXIMUM_SCALE_31            = "31";
    static final String              MAXIMUM_SCALE_63            = "63";

    static final String              TRANSLATE_HEX_BINARY        = "binary";         // @M0A - support hex constant parser option
    static final String              TRANSLATE_HEX_CHARACTER     = "character";

    static final String              TRACE_TOOLBOX_DATASTREAM    = "datastream";     // @K1A
    static final String              TRACE_TOOLBOX_DIAGNOSTIC    = "diagnostic";     // @K1A
    static final String              TRACE_TOOLBOX_ERROR         = "error";          // @K1A
    static final String              TRACE_TOOLBOX_INFORMATION   = "information";    // @K1A
    static final String              TRACE_TOOLBOX_WARNING       = "warning";        // @K1A
    static final String              TRACE_TOOLBOX_CONVERSION    = "conversion";     // @K1A
    static final String              TRACE_TOOLBOX_PROXY         = "proxy";          // @K1A
    static final String              TRACE_TOOLBOX_PCML          = "pcml";           // @K1A
    static final String              TRACE_TOOLBOX_JDBC          = "jdbc";           // @K1A
    static final String              TRACE_TOOLBOX_ALL           = "all";            // @K1A
    static final String              TRACE_TOOLBOX_THREAD        = "thread";         // @K1A
    static final String              TRACE_TOOLBOX_NONE          = NONE_;            // @K1A
    static final String              TRACE_TOOLBOX_NOT_SET       = EMPTY_;


    // Static data.
    private static DriverPropertyInfo[] dpi_;
    private static String[]             defaults_;



    // Private data.
    private boolean             extra_;
    private String[]            values_;
    private Properties          info_;   // @A3A



    /**
    Static initializer.
    **/
    static
    {
        // Initialize.
        dpi_ = new DriverPropertyInfo[NUMBER_OF_ATTRIBUTES_];
        defaults_ = new String[NUMBER_OF_ATTRIBUTES_];
        int i;

        // Access.
        i = ACCESS;
        dpi_[i] = new DriverPropertyInfo (ACCESS_, "");
        dpi_[i].description = "ACCESS_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[3];
        dpi_[i].choices[0]  = ACCESS_ALL;
        dpi_[i].choices[1]  = ACCESS_READ_CALL;
        dpi_[i].choices[2]  = ACCESS_READ_ONLY;
        defaults_[i]        = ACCESS_ALL;

        // @f7a
        // Behavior Override.  This property is a bit mask.  The following have
        // been defined:
        //   1 - (v5r2f) Don't throw exception of executeQuery() does not return a result set
        i = BEHAVIOR_OVERRIDE;
        dpi_[i] = new DriverPropertyInfo (BEHAVIOR_OVERRIDE_, "");
        dpi_[i].description = "BEHAVIOR_OVERRIDE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = "0x00";

        // Bidi string type.  @E9A
        i = BIDI_STRING_TYPE;
        dpi_[i] = new DriverPropertyInfo (BIDI_STRING_TYPE_, "");
        dpi_[i].description = "BIDI_STRING_TYPE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[10];
        dpi_[i].choices[0]  = BIDI_STRING_TYPE_NOTSET;
        dpi_[i].choices[1]  = BIDI_STRING_TYPE_DEFAULT;
        dpi_[i].choices[2]  = BIDI_STRING_TYPE_ST4;
        dpi_[i].choices[3]  = BIDI_STRING_TYPE_ST5;
        dpi_[i].choices[4]  = BIDI_STRING_TYPE_ST6;
        dpi_[i].choices[5]  = BIDI_STRING_TYPE_ST7;
        dpi_[i].choices[6]  = BIDI_STRING_TYPE_ST8;
        dpi_[i].choices[7]  = BIDI_STRING_TYPE_ST9;
        dpi_[i].choices[8]  = BIDI_STRING_TYPE_ST10;
        dpi_[i].choices[9]  = BIDI_STRING_TYPE_ST11;
        defaults_[i]        = BIDI_STRING_TYPE_NOTSET;

        // Big decimal.  @E0A
        i = BIG_DECIMAL;
        dpi_[i] = new DriverPropertyInfo (BIG_DECIMAL_, "");
        dpi_[i].description = "BIG_DECIMAL_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = FALSE_;
        dpi_[i].choices[1]  = TRUE_;
        defaults_[i]        = TRUE_;

        // Block criteria.
        i = BLOCK_CRITERIA;
        dpi_[i] = new DriverPropertyInfo (BLOCK_CRITERIA_, "");
        dpi_[i].description = "BLOCK_CRITERIA_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[3];
        dpi_[i].choices[0]  = BLOCK_CRITERIA_NONE;
        dpi_[i].choices[1]  = BLOCK_CRITERIA_IF_FETCH;
        dpi_[i].choices[2]  = BLOCK_CRITERIA_UNLESS_UPDATE;
        defaults_[i]        = BLOCK_CRITERIA_UNLESS_UPDATE;

        // Block size.
        i = BLOCK_SIZE;
        dpi_[i] = new DriverPropertyInfo (BLOCK_SIZE_, "");
        dpi_[i].description = "BLOCK_SIZE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[8];
        dpi_[i].choices[0]  = BLOCK_SIZE_0;
        dpi_[i].choices[1]  = BLOCK_SIZE_8;
        dpi_[i].choices[2]  = BLOCK_SIZE_16;
        dpi_[i].choices[3]  = BLOCK_SIZE_32;
        dpi_[i].choices[4]  = BLOCK_SIZE_64;
        dpi_[i].choices[5]  = BLOCK_SIZE_128;
        dpi_[i].choices[6]  = BLOCK_SIZE_256;
        dpi_[i].choices[7]  = BLOCK_SIZE_512;
        defaults_[i]        = BLOCK_SIZE_32;

        // Cursor Hold.  @D1
        i = CURSOR_HOLD;
        dpi_[i] = new DriverPropertyInfo (CURSOR_HOLD_, "");
        dpi_[i].description = "CURSOR_HOLD_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = TRUE_;

        // Cursor sensitivity  @F6
        i = CURSOR_SENSITIVITY;
        dpi_[i] = new DriverPropertyInfo (CURSOR_SENSITIVITY_, "");
        dpi_[i].description = "CURSOR_SENSITIVITY_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[3];
        dpi_[i].choices[0]  = CURSOR_SENSITIVITY_ASENSITIVE;
        dpi_[i].choices[1]  = CURSOR_SENSITIVITY_INSENSITIVE;
        dpi_[i].choices[2]  = CURSOR_SENSITIVITY_SENSITIVE;
        defaults_[i]        = CURSOR_SENSITIVITY_ASENSITIVE;

        // Data compression.  @D0A
        i = DATA_COMPRESSION;
        dpi_[i] = new DriverPropertyInfo (DATA_COMPRESSION_, "");
        dpi_[i].description = "DATA_COMPRESSION_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = FALSE_;
        dpi_[i].choices[1]  = TRUE_;
        defaults_[i]        = TRUE_;        // @F3C

        // Data truncation.  @C1A
        i = DATA_TRUNCATION;
        dpi_[i] = new DriverPropertyInfo (DATA_TRUNCATION_, "");
        dpi_[i].description = "DATA_TRUNCATION_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = TRUE_;        // @F2C


        // Database Name.         //@J2A
        i = DATABASE_NAME;
        dpi_[i] = new DriverPropertyInfo (DATABASE_NAME_, "");
        dpi_[i].description = "DATABASE_NAME_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;


        // Date format.  The order that the choices are listed
        // is significant - the index matches the server value.
        // These also correspond to the constants defined in
        // SQLConversionSettings.
        i = DATE_FORMAT;
        dpi_[i] = new DriverPropertyInfo (DATE_FORMAT_, "");
        dpi_[i].description = "DATE_FORMAT_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[8];
        dpi_[i].choices[0]  = DATE_FORMAT_JULIAN;
        dpi_[i].choices[1]  = DATE_FORMAT_MDY;
        dpi_[i].choices[2]  = DATE_FORMAT_DMY;
        dpi_[i].choices[3]  = DATE_FORMAT_YMD;
        dpi_[i].choices[4]  = DATE_FORMAT_USA;
        dpi_[i].choices[5]  = DATE_FORMAT_ISO;
        dpi_[i].choices[6]  = DATE_FORMAT_EUR;
        dpi_[i].choices[7]  = DATE_FORMAT_JIS;
        defaults_[i]        = DATE_FORMAT_NOTSET;

        // Date separator.  The order that the choices are listed
        // is significant - the index matches the server value.
        i = DATE_SEPARATOR;
        dpi_[i] = new DriverPropertyInfo (DATE_SEPARATOR_, "");
        dpi_[i].description = "DATE_SEPARATOR_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[5];
        dpi_[i].choices[0]  = DATE_SEPARATOR_SLASH;
        dpi_[i].choices[1]  = DATE_SEPARATOR_DASH;
        dpi_[i].choices[2]  = DATE_SEPARATOR_PERIOD;
        dpi_[i].choices[3]  = DATE_SEPARATOR_COMMA;
        dpi_[i].choices[4]  = DATE_SEPARATOR_SPACE;
        defaults_[i]        = DATE_SEPARATOR_NOTSET;

        // Decimal separator.  The order that the choices are listed
        // is significant - the index matches the server value.
        i = DECIMAL_SEPARATOR;
        dpi_[i] = new DriverPropertyInfo (DECIMAL_SEPARATOR_, "");
        dpi_[i].description = "DECIMAL_SEPARATOR_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = DECIMAL_SEPARATOR_PERIOD;
        dpi_[i].choices[1]  = DECIMAL_SEPARATOR_COMMA;
        defaults_[i]        = DECIMAL_SEPARATOR_NOTSET;

        // Driver. @E3A
        i = DRIVER;
        dpi_[i] = new DriverPropertyInfo(DRIVER_, "");
        dpi_[i].description = "DRIVER_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        //@F4D dpi_[i].choices[0]  = DRIVER_DEFAULT;
        dpi_[i].choices[0]  = DRIVER_TOOLBOX;
        dpi_[i].choices[1]  = DRIVER_NATIVE;
        defaults_[i]        = DRIVER_TOOLBOX;     //@F4C

        // Extended dynamic.
        i = EXTENDED_DYNAMIC;
        dpi_[i] = new DriverPropertyInfo (EXTENDED_DYNAMIC_, "");
        dpi_[i].description = "EXTENDED_DYNAMIC_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = FALSE_;


        // Extended metadata.   @F5A
        i = EXTENDED_METADATA;
        dpi_[i] = new DriverPropertyInfo (EXTENDED_METADATA_, "");
        dpi_[i].description = "EXTENDED_METADATA";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = FALSE_;
        dpi_[i].choices[1]  = TRUE_;
        defaults_[i]        = FALSE_;


        // Errors.
        i = ERRORS;
        dpi_[i] = new DriverPropertyInfo (ERRORS_, "");
        dpi_[i].description = "ERRORS_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[1]  = ERRORS_BASIC;
        dpi_[i].choices[0]  = ERRORS_FULL;
        defaults_[i]        = ERRORS_BASIC;

        // KeyRingName.         //@F1A
        i = KEY_RING_NAME;
        dpi_[i] = new DriverPropertyInfo (KEY_RING_NAME_, "");
        dpi_[i].description = "KEY_RING_NAME_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;


        // KeyRingPassword.          //@F1A
        i = KEY_RING_PASSWORD;
        dpi_[i] = new DriverPropertyInfo (KEY_RING_PASSWORD_, "");
        dpi_[i].description = "KEY_RING_PASSWORD_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;


        // Full Open.   @W1a
        i = FULL_OPEN;
        dpi_[i] = new DriverPropertyInfo (FULL_OPEN_, "");
        dpi_[i].description = "FULL_OPEN_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = FALSE_;
        dpi_[i].choices[1]  = TRUE_;
        defaults_[i]        = FALSE_;


        // Lazy close.  @E2A
        i = LAZY_CLOSE;
        dpi_[i] = new DriverPropertyInfo (LAZY_CLOSE_, "");
        dpi_[i].description = "LAZY_CLOSE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = FALSE_;
        dpi_[i].choices[1]  = TRUE_;
        defaults_[i]        = FALSE_;   //@E6C

        // Libraries.
        i = LIBRARIES;
        dpi_[i] = new DriverPropertyInfo (LIBRARIES_, "");
        dpi_[i].description = "LIBRARIES_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // LOB threshold.
        i = LOB_THRESHOLD;
        dpi_[i] = new DriverPropertyInfo (LOB_THRESHOLD_, "");
        dpi_[i].description = "LOB_THRESHOLD_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices          = new String[0];
        defaults_[i]        = "32768";

        // Naming.  The order that the choices are listed
        // is significant - the index matches the server value.
        i = NAMING;
        dpi_[i] = new DriverPropertyInfo (NAMING_, "");
        dpi_[i].description = "NAMING_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = NAMING_SQL;
        dpi_[i].choices[1]  = NAMING_SYSTEM;
        defaults_[i]        = NAMING_SQL;

        // Package.
        i = PACKAGE;
        dpi_[i] = new DriverPropertyInfo (PACKAGE_, "");
        dpi_[i].description = "PACKAGE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // Package add.
        i = PACKAGE_ADD;
        dpi_[i] = new DriverPropertyInfo (PACKAGE_ADD_, "");
        dpi_[i].description = "PACKAGE_ADD_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = TRUE_;

        // Package cache.
        i = PACKAGE_CACHE;
        dpi_[i] = new DriverPropertyInfo (PACKAGE_CACHE_, "");
        dpi_[i].description = "PACKAGE_CACHE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = FALSE_;

        // Package clear.
        i = PACKAGE_CLEAR;
        dpi_[i] = new DriverPropertyInfo (PACKAGE_CLEAR_, "");
        dpi_[i].description = "PACKAGE_CLEAR_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = FALSE_;

        // @A0A
        // Package criteria.
        i = PACKAGE_CRITERIA;
        dpi_[i] = new DriverPropertyInfo (PACKAGE_CRITERIA_, "");
        dpi_[i].description = "PACKAGE_CRITERIA_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = PACKAGE_CRITERIA_DEFAULT;
        dpi_[i].choices[1]  = PACKAGE_CRITERIA_SELECT;
        defaults_[i]        = PACKAGE_CRITERIA_DEFAULT;
        // End of @A0A

        // Package error.
        i = PACKAGE_ERROR;
        dpi_[i] = new DriverPropertyInfo (PACKAGE_ERROR_, "");
        dpi_[i].description = "PACKAGE_ERROR_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[3];
        dpi_[i].choices[0]  = PACKAGE_ERROR_NONE;
        dpi_[i].choices[1]  = PACKAGE_ERROR_WARNING;
        dpi_[i].choices[2]  = PACKAGE_ERROR_EXCEPTION;
        defaults_[i]        = PACKAGE_ERROR_WARNING;

        // Package library.
        i = PACKAGE_LIBRARY;
        dpi_[i] = new DriverPropertyInfo (PACKAGE_LIBRARY_, "");
        dpi_[i].description = "PACKAGE_LIBRARY_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // Password.
        i = PASSWORD;
        dpi_[i] = new DriverPropertyInfo (PASSWORD_, "");
        dpi_[i].description = "PASSWORD_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // Prefetch.
        i = PREFETCH;
        dpi_[i] = new DriverPropertyInfo (PREFETCH_, "");
        dpi_[i].description = "PREFETCH_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = TRUE_;

        // Prompt.
        i = PROMPT;
        dpi_[i] = new DriverPropertyInfo (PROMPT_, "");
        dpi_[i].description = "PROMPT_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = NOT_SPECIFIED;    // @E8C

        // Proxy server.    //@A3A
        i = PROXY_SERVER;
        dpi_[i] = new DriverPropertyInfo (PROXY_SERVER_, "");
        dpi_[i].description = "PROXY_SERVER_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // Proxy server secure.    //@A3A
        //i = PROXY_SERVER_SECURE;
        //dpi_[i] = new DriverPropertyInfo (PROXY_SERVER_SECURE_, "");
        //dpi_[i].description = "PROXY_SERVER_SECURE_DESC";
        //dpi_[i].required    = false;
        //dpi_[i].choices     = new String[2];
        //dpi_[i].choices[0]  = TRUE_;
        //dpi_[i].choices[1]  = FALSE_;
        //defaults_[i]        = FALSE_;

        // Remarks.
        i = REMARKS;
        dpi_[i] = new DriverPropertyInfo (REMARKS_, "");
        dpi_[i].description = "REMARKS_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = REMARKS_SQL;
        dpi_[i].choices[1]  = REMARKS_SYSTEM;
        defaults_[i]        = REMARKS_SYSTEM;

        // Secondary URL.    //@A3A
        i = SECONDARY_URL;
        dpi_[i] = new DriverPropertyInfo (SECONDARY_URL_, "");
        dpi_[i].description = "SECONDARY_URL_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // Secure.
        i = SECURE;
        dpi_[i] = new DriverPropertyInfo (SECURE_, "");
        dpi_[i].description = "SECURE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices          = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = FALSE_;

        // Sort.
        i = SORT;
        dpi_[i] = new DriverPropertyInfo (SORT_, "");
        dpi_[i].description = "SORT_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[4];
        dpi_[i].choices[0]  = SORT_HEX;
        dpi_[i].choices[1]  = SORT_JOB;
        dpi_[i].choices[2]  = SORT_LANGUAGE1;
        dpi_[i].choices[3]  = SORT_TABLE1;
        defaults_[i]        = SORT_JOB;

        // Sort language.
        //
        // We set the default to Engligh United States.  At first, we set              @E4A
        // it to "   " (3 spaces), but that causes an host server error.               @E4A
        // It would probably be better to choose a default based on the client         @E4A
        // locale, but that may prove to be a high-maintenance mapping,                @E4A
        // as locales are added to Java and languages are added to the AS/400.         @E4A
        i = SORT_LANGUAGE;
        dpi_[i] = new DriverPropertyInfo (SORT_LANGUAGE_, "");
        dpi_[i].description = "SORT_LANGUAGE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = SORT_LANGUAGE_ENGLISH_UNITED_STATES;                  // @E4C

        // Sort table.
        i = SORT_TABLE;
        dpi_[i] = new DriverPropertyInfo (SORT_TABLE_, "");
        dpi_[i].description = "SORT_TABLE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // Sort weight.
        i = SORT_WEIGHT;
        dpi_[i] = new DriverPropertyInfo (SORT_WEIGHT_, "");
        dpi_[i].description = "SORT_WEIGHT_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = SORT_WEIGHT_SHARED;
        dpi_[i].choices[1]  = SORT_WEIGHT_UNIQUE;
        defaults_[i]        = SORT_WEIGHT_SHARED;

        // Thread used.                                          // @E1C
        i = THREAD_USED;
        dpi_[i] = new DriverPropertyInfo (THREAD_USED_, "");
        dpi_[i].description = "THREAD_USED_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = TRUE_;

        // Time format.  The order that the choices are listed
        // is significant - the index matches the server value.
        // These also correspond to the constants defined in
        // SQLConversionSettings.
        i = TIME_FORMAT;
        dpi_[i] = new DriverPropertyInfo (TIME_FORMAT_, "");
        dpi_[i].description = "TIME_FORMAT_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[5];
        dpi_[i].choices[0]  = TIME_FORMAT_HMS;
        dpi_[i].choices[1]  = TIME_FORMAT_USA;
        dpi_[i].choices[2]  = TIME_FORMAT_ISO;
        dpi_[i].choices[3]  = TIME_FORMAT_EUR;
        dpi_[i].choices[4]  = TIME_FORMAT_JIS;
        defaults_[i]        = TIME_FORMAT_NOTSET;

        // Time separator.  The order that the choices are listed
        // is significant - the index matches the server value.
        i = TIME_SEPARATOR;
        dpi_[i] = new DriverPropertyInfo (TIME_SEPARATOR_, "");
        dpi_[i].description = "TIME_SEPARATOR_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[4];
        dpi_[i].choices[0]  = TIME_SEPARATOR_COLON;
        dpi_[i].choices[1]  = TIME_SEPARATOR_PERIOD;
        dpi_[i].choices[2]  = TIME_SEPARATOR_COMMA;
        dpi_[i].choices[3]  = TIME_SEPARATOR_SPACE;
        defaults_[i]        = TIME_SEPARATOR_NOTSET;

        // Trace.
        i = TRACE;
        dpi_[i] = new DriverPropertyInfo (TRACE_, "");
        dpi_[i].description = "TRACE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = FALSE_;

        // @j1a
        // Trace Server
        i = TRACE_SERVER;
        dpi_[i] = new DriverPropertyInfo (TRACE_SERVER_, "");
        dpi_[i].description = "TRACE_SERVER_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = "0x00";

        // @K1A
        // Trace Toolbox  
        i = TRACE_TOOLBOX;
        dpi_[i] = new DriverPropertyInfo (TRACE_TOOLBOX_, "");
        dpi_[i].description = "TRACE_TOOLBOX_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[12];
        dpi_[i].choices[0]  = TRACE_TOOLBOX_NONE;
        dpi_[i].choices[1]  = TRACE_TOOLBOX_DATASTREAM;
        dpi_[i].choices[2]  = TRACE_TOOLBOX_DIAGNOSTIC;
        dpi_[i].choices[3]  = TRACE_TOOLBOX_ERROR;
        dpi_[i].choices[4]  = TRACE_TOOLBOX_INFORMATION;
        dpi_[i].choices[5]  = TRACE_TOOLBOX_WARNING;
        dpi_[i].choices[6]  = TRACE_TOOLBOX_CONVERSION;
        dpi_[i].choices[7]  = TRACE_TOOLBOX_PROXY;
        dpi_[i].choices[8]  = TRACE_TOOLBOX_PCML;
        dpi_[i].choices[9]  = TRACE_TOOLBOX_JDBC;
        dpi_[i].choices[10] = TRACE_TOOLBOX_ALL;
        dpi_[i].choices[11] = TRACE_TOOLBOX_THREAD;
        defaults_[i]        = TRACE_TOOLBOX_NOT_SET;

        // Transaction isolation.
        i = TRANSACTION_ISOLATION;
        dpi_[i] = new DriverPropertyInfo (TRANSACTION_ISOLATION_, "");
        dpi_[i].description = "TRANSACTION_ISOLATION_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[5];
        dpi_[i].choices[0]  = TRANSACTION_ISOLATION_NONE;
        dpi_[i].choices[1]  = TRANSACTION_ISOLATION_READ_COMMITTED;
        dpi_[i].choices[2]  = TRANSACTION_ISOLATION_READ_UNCOMMITTED;
        dpi_[i].choices[3]  = TRANSACTION_ISOLATION_REPEATABLE_READ;
        dpi_[i].choices[4]  = TRANSACTION_ISOLATION_SERIALIZABLE;
        defaults_[i]        = TRANSACTION_ISOLATION_READ_UNCOMMITTED;

        // Translate binary.
        i = TRANSLATE_BINARY;
        dpi_[i] = new DriverPropertyInfo (TRANSLATE_BINARY_, "");
        dpi_[i].description = "TRANSLATE_BINARY_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRUE_;
        dpi_[i].choices[1]  = FALSE_;
        defaults_[i]        = FALSE_;

        // User.
        i = USER;
        dpi_[i] = new DriverPropertyInfo (USER_, "");
        dpi_[i].description = "USER_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[0];
        defaults_[i]        = EMPTY_;

        // @M0A - Support sending statements in UTF-16 and storing them in a UTF-16 package
        i = PACKAGE_CCSID;
        dpi_[i] = new DriverPropertyInfo(PACKAGE_CCSID_, "");
        dpi_[i].description = "PACKAGE_CCSID_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = PACKAGE_CCSID_UCS2;
        dpi_[i].choices[1]  = PACKAGE_CCSID_UTF16;
        defaults_[i]        = PACKAGE_CCSID_UCS2;

        // @M0A - 63 digit decimal precision
        i = MINIMUM_DIVIDE_SCALE;
        dpi_[i] = new DriverPropertyInfo(MINIMUM_DIVIDE_SCALE_, "");
        dpi_[i].description = "MINIMUM_DIVIDE_SCALE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[10];
        dpi_[i].choices[0]  = MINIMUM_DIVIDE_SCALE_0;
        dpi_[i].choices[1]  = MINIMUM_DIVIDE_SCALE_1;
        dpi_[i].choices[2]  = MINIMUM_DIVIDE_SCALE_2;
        dpi_[i].choices[3]  = MINIMUM_DIVIDE_SCALE_3;
        dpi_[i].choices[4]  = MINIMUM_DIVIDE_SCALE_4;
        dpi_[i].choices[5]  = MINIMUM_DIVIDE_SCALE_5;
        dpi_[i].choices[6]  = MINIMUM_DIVIDE_SCALE_6;
        dpi_[i].choices[7]  = MINIMUM_DIVIDE_SCALE_7;
        dpi_[i].choices[8]  = MINIMUM_DIVIDE_SCALE_8;
        dpi_[i].choices[8]  = MINIMUM_DIVIDE_SCALE_9;
        defaults_[i]        = MINIMUM_DIVIDE_SCALE_0;

        i = MAXIMUM_PRECISION;
        dpi_[i] = new DriverPropertyInfo(MAXIMUM_PRECISION_, "");
        dpi_[i].description = "MAXIMUM_PRECISION_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = MAXIMUM_PRECISION_31;
        dpi_[i].choices[1]  = MAXIMUM_PRECISION_63;
        defaults_[i]        = MAXIMUM_PRECISION_31;

        i = MAXIMUM_SCALE;
        dpi_[i] = new DriverPropertyInfo(MAXIMUM_SCALE_, "");
        dpi_[i].description = "MAXIMUM_SCALE_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[64];
        for(int j=0; j<64; ++j) dpi_[i].choices[j] = Integer.toString(j);
        defaults_[i]        = MAXIMUM_SCALE_31;

        // @M0A - support hex constant parser option
        i = TRANSLATE_HEX;
        dpi_[i] = new DriverPropertyInfo(TRANSLATE_HEX_, "");
        dpi_[i].description = "TRANSLATE_HEX_DESC";
        dpi_[i].required    = false;
        dpi_[i].choices     = new String[2];
        dpi_[i].choices[0]  = TRANSLATE_HEX_CHARACTER;
        dpi_[i].choices[1]  = TRANSLATE_HEX_BINARY;
        defaults_[i]        = TRANSLATE_HEX_CHARACTER;
    }



    /**
    Constructor.
    
    @param  urlProperties   The URL properties.
    @param  info            The info properties.
    **/
    JDProperties (Properties urlProperties, Properties info)
    {
        // Initialize the values.
        info_ = info;
        values_ = new String[NUMBER_OF_ATTRIBUTES_];
        for(int i = 0; i < NUMBER_OF_ATTRIBUTES_; ++i)
            setString (i, getProperty (urlProperties, info, dpi_[i].name));

        // Check both sets of properties for any extra
        // properties.
        extra_ = false;

        Enumeration propertyNames;
        boolean found;
        String propertyName;

        if(urlProperties != null)
        {
            propertyNames = urlProperties.propertyNames ();
            while((propertyNames.hasMoreElements ()) /* @C4D && (extra_ == false) */)
            {
                propertyName = (String) propertyNames.nextElement ();
                if(propertyName.length () > 0)
                {
                    found = false;
                    for(int j = 0; (j < NUMBER_OF_ATTRIBUTES_) && (! found); ++j)
                        if(propertyName.equalsIgnoreCase (dpi_[j].name))
                            found = true;

                    if(! found)                       // @D1 - Websphere uses "CURSORHOLD"
                    {
                        if(propertyName.equalsIgnoreCase(CURSORHOLD_))
                        {
                            String value = getProperty (urlProperties, info, CURSORHOLD_);

                            if(JDTrace.isTraceOn())
                                JDTrace.logInformation(this, propertyName + ": " + value);

                            if(value.equalsIgnoreCase(CURSORHOLD_YES))
                                setString(CURSOR_HOLD, TRUE_);
                            else if(value.equalsIgnoreCase(CURSORHOLD_NO))
                                setString(CURSOR_HOLD, FALSE_);
                        }
                        else
                        {
                            extra_ = true;
                            if(JDTrace.isTraceOn())
                                JDTrace.logInformation (this, "Extra property: \""
                                                        + propertyName + "\"");
                        }
                    }
                    //if (! found) {                  // @D1
                    //    extra_ = true;
                    //    if (JDTrace.isTraceOn())
                    //        JDTrace.logInformation (this, "Extra property: \""
                    //            + propertyName + "\"");
                    //}
                }
            }
        }

        if(info != null)
        {
            propertyNames = info.propertyNames ();
            while((propertyNames.hasMoreElements ()) /* @C4D && (extra_ == false) */)
            {
                propertyName = (String) propertyNames.nextElement ();
                if(propertyName.length () > 0)
                {
                    found = false;
                    for(int j = 0; (j < NUMBER_OF_ATTRIBUTES_) && (! found); ++j)
                        if(propertyName.equalsIgnoreCase (dpi_[j].name))
                            found = true;

                    if(! found)                  // @D1 - Websphere uses "CURSORHOLD"
                    {
                        if(propertyName.equalsIgnoreCase(CURSORHOLD_))
                        {
                            String value = getProperty (urlProperties, info, CURSORHOLD_);

                            if(JDTrace.isTraceOn())
                                JDTrace.logInformation(this, propertyName + ": " + value);

                            if(value.equalsIgnoreCase(CURSORHOLD_YES))
                                setString(CURSOR_HOLD, TRUE_);
                            else if(value.equalsIgnoreCase(CURSORHOLD_NO))
                                setString(CURSOR_HOLD, FALSE_);
                        }
                        else
                        {
                            extra_ = true;
                            if(JDTrace.isTraceOn())
                                JDTrace.logInformation (this, "Extra property: \""
                                                        + propertyName + "\"");
                        }
                    }
                    //if (! found) {                    // @D1
                    //    extra_ = true;
                    //    if (JDTrace.isTraceOn())
                    //        JDTrace.logInformation (this, "Extra property: \""
                    //            + propertyName + "\"");
                    //}
                }
            }
        }
    }



    /**
    Is the value of the specified property set to the specified
    value?  The comparison is case insensitive.
    
    @param      index   Property index.
    @param      value   Value to compare to.
    @return     true or false.
    **/
    boolean equals (int index, String value)
    {
        return getString (index).equalsIgnoreCase (value);
    }




    /**
    Get the driver property info.
    
    @return     The info.
    **/
    DriverPropertyInfo[] getInfo ()
    {
        // Make a complete copy of the table so that if the
        // caller modifies it, it will not affect the connection.
        //
        DriverPropertyInfo[] dpi = new DriverPropertyInfo[NUMBER_OF_ATTRIBUTES_];

        for(int i = 0; i < NUMBER_OF_ATTRIBUTES_; ++i)
        {

            if(i != PASSWORD && i != KEY_RING_PASSWORD) //@F1C
                dpi[i] = new DriverPropertyInfo (dpi_[i].name, values_[i]);
            else
                dpi[i] = new DriverPropertyInfo (dpi_[i].name, "");

            dpi[i].required = dpi_[i].required;
            dpi[i].choices = new String[dpi_[i].choices.length];
            for(int j = 0; j < dpi_[i].choices.length; ++j)
                dpi[i].choices[j] = dpi_[i].choices[j];

            // Load the actual description from the resource bundle.
            dpi[i].description = AS400JDBCDriver.getResource (dpi_[i].description);
        }

        return dpi;
    }



    /**
    Get the value of the specified property as a boolean.  This
    is intended for properties that take "true" and "false" as
    values.
    
    @param      index   Property index.
    @return     The value.
    **/
    boolean getBoolean (int index)
    {
        return Boolean.valueOf (values_[index]).booleanValue();
    }



    /**
    Get the index of the value of the specified property.  This
    is the index within the list of choices.  If the property is
    not specified, then the index of the default value will be
    returned.  If the value does not match a choice, then return -1.
    
    @param      index   Property index.
    @return     The index of the value, or -1.
    **/
    int getIndex (int index)
    {
        for(int i = 0; i < dpi_[index].choices.length; ++i)
            if(values_[index].equalsIgnoreCase (dpi_[index].choices[i]))
                return i;
        return -1;
    }



    /**
    Get the value of the specified property as an int.  This
    is intended for properties that take integers as values.
    
    @param      index   Property index.
    @return     The value.
    **/
    int getInt (int index)
    {
        try
        {                                                               // @C2A
            return Integer.parseInt (values_[index]);
        }                                                                   // @C2A
        catch(NumberFormatException e)
        {                                   // @C2A
            return 0;                                                       // @C2A
        }                                                                   // @C2A
    }



    // @A3A
    /**
    Get the original "info" Properties object, that was passed in as the
    second argument to the constructor of this object.
    
    @return     The original "info" Properties object.
    **/
    Properties getOriginalInfo ()
    {
        return info_;
    }



    /**
    Returns the value of the specified property.  The
    URL properties are searched first, then the info
    properties.
    
    @param  urlProperties   The URL properties.
    @param  info            The info properties.
    @param  propertyName    The property name.
    **/
    private static String getProperty (Properties urlProperties,
                                       Properties info,
                                       String propertyName)
    {
        String value = null;
        if(urlProperties != null)
            value = urlProperties.getProperty (propertyName);
        if((value == null) && (info != null))
            value = info.getProperty (propertyName);

        return value;
    }



    /**
    Get the value of the specified property.  If the property is
    not specified, then the default value will be returned.  If
    choices are allowed, then the value will be compared against
    the choices, and if none match, the default value will be
    returned.
    
    @param      index   Property index.
    @return     The value.
    **/
    String getString (int index)
    {
        String value = values_[index];

        if(index == PASSWORD || index == KEY_RING_PASSWORD) //@F1C
            values_[index] = "";

        return value.trim();
    }



    /**
    Indicates if any extra properties are specified.
    
    @param      properties   The properties.
    @return     true or false
    **/
    boolean isExtraPropertySpecified ()
    {
        return extra_;
    }



    /**
    Indicates if the trace property is set.  This needs to be
    detected before an object of this class is even instantiated,
    which is why it is static.
    
    @param      urlProperties   The URL properties.
    @param      info            The info properties.
    @return     Whether the trace property was set to true, false, or not specified when constructed.
    **/
    static String isTraceSet (Properties urlProperties, Properties info)   //@E7C
    {
        if(getProperty (urlProperties, info, TRACE_) == null)
            return TRACE_NOT_SPECIFIED;
        else if(Boolean.valueOf(getProperty (urlProperties, info, TRACE_)).booleanValue())
            return TRACE_SET_ON;
        else
            return TRACE_SET_OFF;
        //@E7D return Boolean.valueOf (getProperty (urlProperties, info, TRACE_)).booleanValue();
    }

    //@K1A
    /**
    Indicates if a toolbox trace category is set.  
    
    @param    urlProperties    The URL properties.
    @param    info             The info properties.
    @return   The category the toolbox trace was set to when constructed.
    **/
    static String isToolboxTraceSet (Properties urlProperties, Properties info)
    {
        if(getProperty (urlProperties, info, TRACE_TOOLBOX_) == null)
            return TRACE_TOOLBOX_NOT_SET;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_DATASTREAM))
            return TRACE_TOOLBOX_DATASTREAM;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_DIAGNOSTIC))
            return TRACE_TOOLBOX_DIAGNOSTIC;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_ERROR))
            return TRACE_TOOLBOX_ERROR;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_INFORMATION))
            return TRACE_TOOLBOX_INFORMATION;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_WARNING))
            return TRACE_TOOLBOX_WARNING;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_CONVERSION))
            return TRACE_TOOLBOX_CONVERSION;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_PROXY))
            return TRACE_TOOLBOX_PROXY;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_PCML))
            return TRACE_TOOLBOX_PCML;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_JDBC))
            return TRACE_TOOLBOX_JDBC;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_ALL))
            return TRACE_TOOLBOX_ALL;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_THREAD))
            return TRACE_TOOLBOX_THREAD;
        else if(getProperty (urlProperties, info, TRACE_TOOLBOX_).equalsIgnoreCase(TRACE_TOOLBOX_NONE))
            return TRACE_TOOLBOX_NONE;
        else 
            return TRACE_TOOLBOX_NOT_SET;
    }


    // @W2 new method
    //
    // Fix up the properties array when the object is re-inflated.  Choices:
    //   1) Objects match (the number of properties in the array is the same) --
    //      don't do anything.
    //   2) Old data into a new object -- fix up the array.  In this case the
    //      array (values_) created during re-serialization is too small.  Take
    //      the fullOpen case.  If a v4r5 object is serialized then the values_
    //      array contains only 44 elements.  When that array is put in a v5r1 object
    //      the object has the ability to give out fullOpen (element 45).  This
    //      results in a indexOutOfBounds exception since we try to pull the 45th
    //      element out of an array of size 44.  To fix this condition a new array
    //      will be built.  The first N element will come from the old array.  The
    //      last element(s) will be filled in with defaults.
    //   3) Data into an older object -- don't do anything.  In this case the array
    //      will be too big but that won't hurt anything.  Take again the full open
    //      case.  Suppose a v5r1 object (45 elements) is serialized then de-serialized
    //      into a v4r5 object.  Now values_ has one too many elements.  That doesn't
    //      hurt anything since the code cannot get to the last element.
    //
    // This scheme works ONLY IF NEW ELEMENTS ARE ALWAYS ADDED TO THE END OF THE ARRAY!!!!
    //
    // @param in The input stream from which to deserialize the object.
    // @exception ClassNotFoundException If the class being deserialized is not found.
    // @exception IOException If an error occurs while communicating with the AS/400.
    //
    private void readObject(java.io.ObjectInputStream in)
    throws ClassNotFoundException,
    IOException
    {
        in.defaultReadObject();

        if(values_.length < NUMBER_OF_ATTRIBUTES_)
        {
            String[] temp = new String[NUMBER_OF_ATTRIBUTES_];

            for(int i = 0; i < values_.length; i++)
                temp[i] = values_[i];

            for(int i = values_.length; i < NUMBER_OF_ATTRIBUTES_; i++)
                temp[i] = defaults_[i];

            values_ = temp;
        }
    }






    /**
    Set the value of the specified property.
    If choices are allowed, then the value will be compared against
    the choices, and if none match, the property will be set to
    the default value.
    
    @param      index   Property index.
    @param      value   The value.
    **/
    void setString (int index, String value)
    {
        // If no property was provided, then set the choice
        // to the default.
        if((value == null) || (value.length() == 0))               // @E5C
            values_[index] = defaults_[index];
        else
            values_[index] = value;

        // If choices are provided, for a specified index,
        // then validate the current choice.
        if(dpi_[index].choices.length > 0)
        {
            boolean valid = false;
            for(int i = 0; i < dpi_[index].choices.length; ++i)
            {
                if(values_[index].equalsIgnoreCase (dpi_[index].choices[i]))
                {
                    valid = true;
                    break;
                }
            }

            // If not valid, then set the current choice to
            // the default.
            if(! valid)
                values_[index] = defaults_[index];
        }

        if(JDTrace.isTraceOn())
            JDTrace.logProperty (this, dpi_[index].name,
                                 ((index != PASSWORD && index != KEY_RING_PASSWORD) ? values_[index] : ""));  //@F1C
    }



    /**
    Returns the string representation of the object.
    
    @return The string representation.
    **/
    //
    // Implementatation note:  This is necessary only for tracing.
    //
    public String toString ()
    {
        return "";
    }



}
