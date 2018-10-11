///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCDataSourceBeanInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


/**
*  The AS400JDBCDataSourceBeanInfo class provides bean information
*  for the AS400JDBCDataSource class.
**/
public class AS400JDBCDataSourceBeanInfo extends SimpleBeanInfo
{
    static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = AS400JDBCDataSource.class;

    // Handles loading the appropriate resource bundle
    // private static ResourceBundleLoader loader_;
    private static EventSetDescriptor[] events_;
    private static PropertyDescriptor[] properties_;

    static
    {
        try
        {
            EventSetDescriptor changed = new EventSetDescriptor(beanClass,
                                                                "propertyChange",
                                                                java.beans.PropertyChangeListener.class,
                                                                "propertyChange");
            changed.setDisplayName(ResourceBundleLoader.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(ResourceBundleLoader.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor[] events = { changed};

            events_ = events;

            // ***** PROPERTIES
            PropertyDescriptor access = new PropertyDescriptor("access", beanClass, "getAccess", "setAccess");
            access.setBound(true);
            access.setConstrained(false);
            access.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ACCESS", null));
            access.setShortDescription(AS400JDBCDriver.getResource("ACCESS_DESC", null));

            PropertyDescriptor behaviorOverride = new PropertyDescriptor("behaviorOverride", beanClass, "getBehaviorOverride", "setBehaviorOverride"); // @J7A
            behaviorOverride.setBound(true);                                                                                     // @J7A
            behaviorOverride.setConstrained(false);                                                                              // @J7A
            behaviorOverride.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BEHAVIOR_OVERRIDE", null));                         // @J7A
            behaviorOverride.setShortDescription(AS400JDBCDriver.getResource("BEHAVIOR_OVERRIDE_DESC", null));                         // @J7A

            PropertyDescriptor bidiStringType = new PropertyDescriptor("bidiStringType", beanClass, "getBidiStringType", "setBidiStringType"); // @A3A
            bidiStringType.setBound(true);                                                                                       // @A3A
            bidiStringType.setConstrained(false);                                                                                // @A3A
            bidiStringType.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIDI_STRING_TYPE", null));                            // @A3A
            bidiStringType.setShortDescription(AS400JDBCDriver.getResource("BIDI_STRING_TYPE_DESC", null));                  // @A3A

            PropertyDescriptor bidiImplicitReordering = new PropertyDescriptor("bidiImplicitReordering", beanClass, "isBidiImplicitReordering", "setBidiImplicitReordering"); // @K24
            bidiImplicitReordering.setBound(true);                                                                                       //@K24
            bidiImplicitReordering.setConstrained(false);                                                                                //@K24
            bidiImplicitReordering.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIDI_IMPLICIT_REORDERING",null));                    //@K24        
            bidiImplicitReordering.setShortDescription(AS400JDBCDriver.getResource("BIDI_IMPLICIT_REORDERING_DESC",null));                    //@K24

            PropertyDescriptor bidiNumericOrdering = new PropertyDescriptor("bidiNumericOrdering", beanClass, "isBidiNumericOrdering", "setBidiNumericOrdering"); // @K24
            bidiNumericOrdering.setBound(true);                                                                                       //@K24
            bidiNumericOrdering.setConstrained(false);                                                                                //@K24
            bidiNumericOrdering.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIDI_NUMERIC_ORDERING",null));                    //@K24        
            bidiNumericOrdering.setShortDescription(AS400JDBCDriver.getResource("BIDI_NUMERIC_ORDERING_DESC",null));                    //@K24

            PropertyDescriptor bigDecimal = new PropertyDescriptor("bigDecimal", beanClass, "isBigDecimal", "setBigDecimal");
            bigDecimal.setBound(true);
            bigDecimal.setConstrained(false);
            bigDecimal.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIG_DECIMAL",null));
            bigDecimal.setShortDescription(AS400JDBCDriver.getResource("BIG_DECIMAL_DESC",null));

            PropertyDescriptor blockCriteria = new PropertyDescriptor("blockCriteria", beanClass, "getBlockCriteria", "setBlockCriteria");
            blockCriteria.setBound(true);
            blockCriteria.setConstrained(false);
            blockCriteria.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BLOCK_CRITERIA",null));
            blockCriteria.setShortDescription(AS400JDBCDriver.getResource("BLOCK_CRITERIA_DESC",null));

            PropertyDescriptor blockSize = new PropertyDescriptor("blockSize", beanClass, "getBlockSize", "setBlockSize");
            blockSize.setBound(true);
            blockSize.setConstrained(false);
            blockSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BLOCK_SIZE",null));
            blockSize.setShortDescription(AS400JDBCDriver.getResource("BLOCK_SIZE_DESC",null));

            PropertyDescriptor cursorHold = new PropertyDescriptor("cursorHold", beanClass, "isCursorHold", "setCursorHold");
            cursorHold.setBound(true);
            cursorHold.setConstrained(false);
            cursorHold.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CURSOR_HOLD",null));
            cursorHold.setShortDescription(AS400JDBCDriver.getResource("CURSOR_HOLD_DESC",null));

            PropertyDescriptor cursorSensitivity = new PropertyDescriptor("cursorSensitivity", beanClass, "getCursorSensitivity", "setCursorSensitivity"); // @J6A
            cursorSensitivity.setBound(true);                                                                                     // @J6A
            cursorSensitivity.setConstrained(false);                                                                              // @J6A
            cursorSensitivity.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CURSOR_SENSITIVITY",null));                         // @J6A
            cursorSensitivity.setShortDescription(AS400JDBCDriver.getResource("CURSOR_SENSITIVITY_DESC",null));                         // @J6A

            PropertyDescriptor databaseName = new PropertyDescriptor("databaseName", beanClass, "getDatabaseName", "setDatabaseName");
            databaseName.setBound(true);
            databaseName.setConstrained(false);
            databaseName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATABASE_NAME",null));
            databaseName.setShortDescription(AS400JDBCDriver.getResource("DATABASE_NAME_DESC",null));

            PropertyDescriptor dataCompression = new PropertyDescriptor("dataCompression", beanClass, "isDataCompression", "setDataCompression");
            dataCompression.setBound(true);
            dataCompression.setConstrained(false);
            dataCompression.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATA_COMPRESSION",null));
            dataCompression.setShortDescription(AS400JDBCDriver.getResource("DATA_COMPRESSION_DESC",null));

            PropertyDescriptor dataSourceName = new PropertyDescriptor("dataSourceName", beanClass, "getDataSourceName", "setDataSourceName");
            dataSourceName.setBound(true);
            dataSourceName.setConstrained(false);
            dataSourceName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATASOURCE_NAME",null));
            dataSourceName.setShortDescription(AS400JDBCDriver.getResource("DATASOURCE_NAME_DESC",null));

            PropertyDescriptor dataTruncation = new PropertyDescriptor("dataTruncation", beanClass, "isDataTruncation", "setDataTruncation");
            dataTruncation.setBound(true);
            dataTruncation.setConstrained(false);
            dataTruncation.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATA_TRUNCATION",null));
            dataTruncation.setShortDescription(AS400JDBCDriver.getResource("DATA_TRUNCATION_DESC",null));

            PropertyDescriptor dateFormat = new PropertyDescriptor("dateFormat", beanClass, "getDateFormat", "setDateFormat");
            dateFormat.setBound(true);
            dateFormat.setConstrained(false);
            dateFormat.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATE_FORMAT",null));
            dateFormat.setShortDescription(AS400JDBCDriver.getResource("DATE_FORMAT_DESC",null));

            PropertyDescriptor dateSeparator = new PropertyDescriptor("dateSeparator", beanClass, "getDateSeparator", "setDateSeparator");
            dateSeparator.setBound(true);
            dateSeparator.setConstrained(false);
            dateSeparator.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATE_SEPARATOR",null));
            dateSeparator.setShortDescription(AS400JDBCDriver.getResource("DATE_SEPARATOR_DESC",null));

            PropertyDescriptor decimalSeparator = new PropertyDescriptor("decimalSeparator", beanClass, "getDecimalSeparator", "setDecimalSeparator");
            decimalSeparator.setBound(true);
            decimalSeparator.setConstrained(false);
            decimalSeparator.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DECIMAL_SEPARATOR",null));
            decimalSeparator.setShortDescription(AS400JDBCDriver.getResource("DECIMAL_SEPARATOR_DESC",null));

            PropertyDescriptor description = new PropertyDescriptor("description", beanClass, "getDescription", "setDescription");
            description.setBound(true);
            description.setConstrained(false);
            description.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DESCRIPTION",null));
            description.setShortDescription(AS400JDBCDriver.getResource("DESCRIPTION_DESC",null));

            PropertyDescriptor driver = new PropertyDescriptor("driver", beanClass, "getDriver", "setDriver");   // @A2A
            driver.setBound(true);                                                                               // @A2A
            driver.setConstrained(false);                                                                        // @A2A
            driver.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DRIVER",null));                              // @A2A
            driver.setShortDescription(AS400JDBCDriver.getResource("DRIVER_DESC",null));                              // @A2A

            PropertyDescriptor errors = new PropertyDescriptor("errors", beanClass, "getErrors", "setErrors");
            errors.setBound(true);
            errors.setConstrained(false);
            errors.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ERRORS",null));
            errors.setShortDescription(AS400JDBCDriver.getResource("ERRORS_DESC",null));

            PropertyDescriptor extendedDynamic = new PropertyDescriptor("extendedDynamic", beanClass, "isExtendedDynamic", "setExtendedDynamic");
            extendedDynamic.setBound(true);
            extendedDynamic.setConstrained(false);
            extendedDynamic.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_EXTENDED_DYNAMIC",null));
            extendedDynamic.setShortDescription(AS400JDBCDriver.getResource("EXTENDED_DYNAMIC_DESC",null));

            PropertyDescriptor extendedMetaData = new PropertyDescriptor("extendedMetaData", beanClass, "isExtendedMetaData", "setExtendedMetaData"); // @J2A
            extendedMetaData.setBound(true);                                                                                     // @J2A
            extendedMetaData.setConstrained(false);                                                                              // @J2A
            extendedMetaData.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_EXTENDED_METADATA",null));                         // @J2A
            extendedMetaData.setShortDescription(AS400JDBCDriver.getResource("EXTENDED_METADATA_DESC",null));                         // @J2A

            PropertyDescriptor extendedMetadata = new PropertyDescriptor("extendedMetadata", beanClass, "isExtendedMetadata", "setExtendedMetadata"); // @J2A
            extendedMetadata.setBound(true);                                                                                     // @J2A
            extendedMetadata.setConstrained(false);                                                                              // @J2A
            extendedMetadata.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_EXTENDED_METADATA",null));                         // @J2A
            extendedMetadata.setShortDescription(AS400JDBCDriver.getResource("EXTENDED_METADATA_DESC",null));                         // @J2A


            PropertyDescriptor fullOpen = new PropertyDescriptor("fullOpen", beanClass, "isFullOpen", "setFullOpen");    // @W1A
            fullOpen.setBound(true);                                                                                     // @W1A
            fullOpen.setConstrained(false);                                                                              // @W1A
            fullOpen.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_FULL_OPEN",null));                                 // @W1A
            fullOpen.setShortDescription(AS400JDBCDriver.getResource("FULL_OPEN_DESC",null));                                 // @W1A

            //@KBL  Added support to specify if input locators should be allocated as type hold or not hold
            PropertyDescriptor holdLocators = new PropertyDescriptor("holdInputLocators", beanClass, "isHoldInputLocators", "setHoldInputLocators");   //@KBL
            holdLocators.setBound(true);                                                                                              //@KBL
            holdLocators.setConstrained(false);                                                                                       //@KBL
            holdLocators.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_HOLD_LOCATORS",null));                                      //@KBL
            holdLocators.setShortDescription(AS400JDBCDriver.getResource("HOLD_LOCATORS_DESC",null));                                      //@KBL

            PropertyDescriptor holdStatements = new PropertyDescriptor("holdStatements", beanClass, "isHoldStatements", "setHoldStatements");    // @KBL
            holdStatements.setBound(true);                                                                                       // @KBL
            holdStatements.setConstrained(false);                                                                                 // @KBL
            holdStatements.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_HOLD_STATEMENTS",null));                                  // @KBL
            holdStatements.setShortDescription(AS400JDBCDriver.getResource("HOLD_STATEMENTS_DESC",null));                                  // @KBL

            PropertyDescriptor lazyClose = new PropertyDescriptor("lazyClose", beanClass, "isLazyClose", "setLazyClose");    // @A1A
            lazyClose.setBound(true);                                                                                       // @A1A
            lazyClose.setConstrained(false);                                                                                 // @A1A
            lazyClose.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LAZY_CLOSE",null));                                  // @A1A
            lazyClose.setShortDescription(AS400JDBCDriver.getResource("LAZY_CLOSE_DESC",null));                                  // @A1A

            PropertyDescriptor libraries = new PropertyDescriptor("libraries", beanClass, "getLibraries", "setLibraries");
            libraries.setBound(true);
            libraries.setConstrained(false);
            libraries.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LIBRARIES",null));
            libraries.setShortDescription(AS400JDBCDriver.getResource("LIBRARIES_DESC",null));

            PropertyDescriptor lobThreshold = new PropertyDescriptor("lobThreshold", beanClass, "getLobThreshold", "setLobThreshold");
            lobThreshold.setBound(true);
            lobThreshold.setConstrained(false);
            lobThreshold.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LOB_THRESHOLD",null));
            lobThreshold.setShortDescription(AS400JDBCDriver.getResource("LOB_THRESHOLD_DESC",null));

            PropertyDescriptor naming = new PropertyDescriptor("naming", beanClass, "getNaming", "setNaming");
            naming.setBound(true);
            naming.setConstrained(false);
            naming.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_NAMING",null));
            naming.setShortDescription(AS400JDBCDriver.getResource("NAMING_DESC",null));

            PropertyDescriptor packageName = new PropertyDescriptor("package", beanClass, "getPackage", "setPackage");
            packageName.setBound(true);
            packageName.setConstrained(false);
            packageName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE",null));
            packageName.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_DESC",null));

            PropertyDescriptor packageAdd = new PropertyDescriptor("packageAdd", beanClass, "isPackageAdd", "setPackageAdd");
            packageAdd.setBound(true);
            packageAdd.setConstrained(false);
            packageAdd.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_ADD",null));
            packageAdd.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_ADD_DESC",null));

            PropertyDescriptor packageCache = new PropertyDescriptor("packageCache", beanClass, "isPackageCache", "setPackageCache");
            packageCache.setBound(true);
            packageCache.setConstrained(false);
            packageCache.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CACHE",null));
            packageCache.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CACHE_DESC",null));

            PropertyDescriptor packageClear = new PropertyDescriptor("packageClear", beanClass, "isPackageClear", "setPackageClear");
            packageClear.setBound(true);
            packageClear.setConstrained(false);
            packageClear.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CLEAR",null));
            packageClear.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CLEAR_DESC",null));

            PropertyDescriptor packageCriteria = new PropertyDescriptor("packageCriteria", beanClass, "getPackageCriteria", "setPackageCriteria");
            packageCriteria.setBound(true);
            packageCriteria.setConstrained(false);
            packageCriteria.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CRITERIA",null));
            packageCriteria.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CRITERIA_DESC",null));

            PropertyDescriptor packageError = new PropertyDescriptor("packageError", beanClass, "getPackageError", "setPackageError");
            packageError.setBound(true);
            packageError.setConstrained(false);
            packageError.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_ERROR",null));
            packageError.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_ERROR_DESC",null));

            PropertyDescriptor packageLibrary = new PropertyDescriptor("packageLibrary", beanClass, "getPackageLibrary", "setPackageLibrary");
            packageLibrary.setBound(true);
            packageLibrary.setConstrained(false);
            packageLibrary.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_LIBRARY",null));
            packageLibrary.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_LIBRARY_DESC",null));

            PropertyDescriptor password = new PropertyDescriptor("password", beanClass, null, "setPassword");
            password.setBound(true);
            password.setConstrained(false);
            password.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PASSWORD",null));
            password.setShortDescription(AS400JDBCDriver.getResource("PASSWORD_DESC",null));

            PropertyDescriptor prefetch = new PropertyDescriptor("prefetch", beanClass, "isPrefetch", "setPrefetch");
            prefetch.setBound(true);
            prefetch.setConstrained(false);
            prefetch.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PREFETCH",null));
            prefetch.setShortDescription(AS400JDBCDriver.getResource("PREFETCH_DESC",null));

            PropertyDescriptor prompt = new PropertyDescriptor("prompt", beanClass, "isPrompt", "setPrompt");
            prompt.setBound(true);
            prompt.setConstrained(false);
            prompt.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PROMPT",null));
            prompt.setShortDescription(AS400JDBCDriver.getResource("PROMPT_DESC",null));

            PropertyDescriptor proxyServer = new PropertyDescriptor("proxyServer", beanClass, "getProxyServer", "setProxyServer");
            proxyServer.setBound(true);
            proxyServer.setConstrained(false);
            proxyServer.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PROXY_SERVER",null));
            proxyServer.setShortDescription(AS400JDBCDriver.getResource("PROXY_SERVER_DESC",null));

            PropertyDescriptor qaqqiniLibrary = new PropertyDescriptor("qaqqiniLibrary", beanClass, "getQaqqiniLibrary", "setQaqqiniLibrary");  
            qaqqiniLibrary.setBound(true);                                                                                                  
            qaqqiniLibrary.setConstrained(false);                                                                                           
            qaqqiniLibrary.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QAQQINILIB",null));                                         
            qaqqiniLibrary.setShortDescription(AS400JDBCDriver.getResource("QAQQINILIB_DESC",null));                                          


            PropertyDescriptor remarks = new PropertyDescriptor("remarks", beanClass, "getRemarks", "setRemarks");
            remarks.setBound(true);
            remarks.setConstrained(false);
            remarks.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_REMARKS",null));
            remarks.setShortDescription(AS400JDBCDriver.getResource("REMARKS_DESC",null));

            // @J3 New property
            PropertyDescriptor savePassword = new PropertyDescriptor("savePasswordWhenSerialized", beanClass, "isSavePasswordWhenSerialized", "setSavePasswordWhenSerialized");
            savePassword.setBound(true);  //@J4C
            savePassword.setConstrained(false);
            savePassword.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SAVE_PASSWORD_WHEN_SERIALIZED",null));
            savePassword.setShortDescription(AS400JDBCDriver.getResource("SAVE_PASSWORD_WHEN_SERIALIZED",null));

            PropertyDescriptor secondaryUrl = new PropertyDescriptor("secondaryUrl", beanClass, "getSecondaryUrl", "setSecondaryUrl");
            secondaryUrl.setBound(true);
            secondaryUrl.setConstrained(false);
            secondaryUrl.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SECONDARY_URL",null));
            secondaryUrl.setShortDescription(AS400JDBCDriver.getResource("SECONDARY_URL_DESC",null));

            PropertyDescriptor secure = new PropertyDescriptor("secure", beanClass, "isSecure", "setSecure");
            secure.setBound(true);
            secure.setConstrained(false);
            secure.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SECURE",null));
            secure.setShortDescription(AS400JDBCDriver.getResource("SECURE_DESC",null));

            PropertyDescriptor serverName = new PropertyDescriptor("serverName", beanClass, "getServerName", "setServerName");
            serverName.setBound(true);
            serverName.setConstrained(false);
            serverName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SERVER_NAME",null));
            serverName.setShortDescription(AS400JDBCDriver.getResource("SERVER_NAME_DESC",null));

            PropertyDescriptor sort = new PropertyDescriptor("sort", beanClass, "getSort", "setSort");
            sort.setBound(true);
            sort.setConstrained(false);
            sort.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT",null));
            sort.setShortDescription(AS400JDBCDriver.getResource("SORT_DESC",null));

            PropertyDescriptor sortLanguage = new PropertyDescriptor("sortLanguage", beanClass, "getSortLanguage", "setSortLanguage");
            sortLanguage.setBound(true);
            sortLanguage.setConstrained(false);
            sortLanguage.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT_LANGUAGE",null));
            sortLanguage.setShortDescription(AS400JDBCDriver.getResource("SORT_LANGUAGE_DESC",null));

            PropertyDescriptor sortTable = new PropertyDescriptor("sortTable", beanClass, "getSortTable", "setSortTable");
            sortTable.setBound(true);
            sortTable.setConstrained(false);
            sortTable.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT_TABLE",null));
            sortTable.setShortDescription(AS400JDBCDriver.getResource("SORT_TABLE_DESC",null));

            PropertyDescriptor sortWeight = new PropertyDescriptor("sortWeight", beanClass, "getSortWeight", "setSortWeight");
            sortWeight.setBound(true);
            sortWeight.setConstrained(false);
            sortWeight.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT_WEIGHT",null));
            sortWeight.setShortDescription(AS400JDBCDriver.getResource("SORT_WEIGHT_DESC",null));

            PropertyDescriptor threadUsed = new PropertyDescriptor("threadUsed", beanClass, "isThreadUsed", "setThreadUsed");
            threadUsed.setBound(true);
            threadUsed.setConstrained(false);
            threadUsed.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_THREAD_USED",null));
            threadUsed.setShortDescription(AS400JDBCDriver.getResource("THREAD_USED_DESC",null));

            PropertyDescriptor timeFormat = new PropertyDescriptor("timeFormat", beanClass, "getTimeFormat", "setTimeFormat");
            timeFormat.setBound(true);
            timeFormat.setConstrained(false);
            timeFormat.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TIME_FORMAT",null));
            timeFormat.setShortDescription(AS400JDBCDriver.getResource("TIME_FORMAT_DESC",null));

            PropertyDescriptor timeSeparator = new PropertyDescriptor("timeSeparator", beanClass, "getTimeSeparator", "setTimeSeparator");
            timeSeparator.setBound(true);
            timeSeparator.setConstrained(false);
            timeSeparator.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TIME_SEPARATOR",null));
            timeSeparator.setShortDescription(AS400JDBCDriver.getResource("TIME_SEPARATOR_DESC",null));

            PropertyDescriptor trace = new PropertyDescriptor("trace", beanClass, "isTrace", "setTrace");   // @w2c
            trace.setBound(true);
            trace.setConstrained(false);
            trace.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE",null));
            trace.setShortDescription(AS400JDBCDriver.getResource("TRACE_DESC",null));

            PropertyDescriptor serverTrace = new PropertyDescriptor("serverTrace", beanClass, "getServerTrace", "setServerTrace");  //@J1a
            serverTrace.setBound(true);                                                                                                   //@J1a
            serverTrace.setConstrained(false);                                                                                            //@J1a
            serverTrace.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE_SERVER",null));                                            //@J1a
            serverTrace.setShortDescription(AS400JDBCDriver.getResource("TRACE_SERVER_DESC",null));                                            //@J1a

            PropertyDescriptor traceServerCategories = new PropertyDescriptor("serverTraceCategories", beanClass, "getServerTraceCategories", "setServerTraceCategories");  //@K4A
            traceServerCategories.setBound(true);                                                                                                   //@K4A
            traceServerCategories.setConstrained(false);                                                                                            //@K4A
            traceServerCategories.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE_SERVER",null));                                            //@K4A
            traceServerCategories.setShortDescription(AS400JDBCDriver.getResource("TRACE_SERVER_DESC",null));                                            //@K4A

            PropertyDescriptor traceToolbox = new PropertyDescriptor("toolboxTraceCategory", beanClass, "getToolboxTraceCategory", "setToolboxTraceCategory");  //@K2A
            traceToolbox.setBound(true);                                                                                                  //@K2A
            traceToolbox.setConstrained(false);                                                                                           //@K2A
            traceToolbox.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE_TOOLBOX",null));                                          //@K2A
            traceToolbox.setShortDescription(AS400JDBCDriver.getResource("TRACE_TOOLBOX_DESC",null));                                          //@K2A

            PropertyDescriptor transactionIsolation = new PropertyDescriptor("transactionIsolation", beanClass, "getTransactionIsolation", "setTransactionIsolation");
            transactionIsolation.setBound(true);
            transactionIsolation.setConstrained(false);
            transactionIsolation.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSACTION_ISOLATION",null));
            transactionIsolation.setShortDescription(AS400JDBCDriver.getResource("TRANSACTION_ISOLATION_DESC",null));

            PropertyDescriptor translateBinary = new PropertyDescriptor("translateBinary", beanClass, "isTranslateBinary", "setTranslateBinary");
            translateBinary.setBound(true);
            translateBinary.setConstrained(false);
            translateBinary.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSLATE_BINARY",null));
            translateBinary.setShortDescription(AS400JDBCDriver.getResource("TRANSLATE_BINARY_DESC",null));

            PropertyDescriptor user = new PropertyDescriptor("user", beanClass, "getUser", "setUser");
            user.setBound(true);
            user.setConstrained(false);
            user.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_USER",null));
            user.setShortDescription(AS400JDBCDriver.getResource("USER_DESC",null));

            PropertyDescriptor keepAlive = new PropertyDescriptor("keepAlive", beanClass, "isKeepAlive", "setKeepAlive");
            keepAlive.setBound(true);
            keepAlive.setConstrained(false);
            keepAlive.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_KEEP_ALIVE",null));
            keepAlive.setShortDescription(AS400JDBCDriver.getResource("KEEP_ALIVE_DESC",null));

            PropertyDescriptor loginTimeout = new PropertyDescriptor("loginTimeout", beanClass, "getLoginTimeout", "setLoginTimeout");  //@K5A
            loginTimeout.setBound(true);                                                                                                //@K5A        
            loginTimeout.setConstrained(false);                                                                                         //@K5A
            loginTimeout.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LOGIN_TIMEOUT",null));                                        //@K5A
            loginTimeout.setShortDescription(AS400JDBCDriver.getResource("LOGIN_TIMEOUT_DESC",null));                                        //@K5A
           
            PropertyDescriptor receiveBufferSize = new PropertyDescriptor("receiveBufferSize", beanClass, "getReceiveBufferSize", "setReceiveBufferSize");
            receiveBufferSize.setBound(true);
            receiveBufferSize.setConstrained(false);
            receiveBufferSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RECEIVE_BUFFER_SIZE",null));
            receiveBufferSize.setShortDescription(AS400JDBCDriver.getResource("RECEIVE_BUFFER_SIZE_DESC",null));

            PropertyDescriptor sendBufferSize = new PropertyDescriptor("sendBufferSize", beanClass, "getSendBufferSize", "setSendBufferSize");
            sendBufferSize.setBound(true);
            sendBufferSize.setConstrained(false);
            sendBufferSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SEND_BUFFER_SIZE",null));
            sendBufferSize.setShortDescription(AS400JDBCDriver.getResource("SEND_BUFFER_SIZE_DESC",null));

            PropertyDescriptor soLinger = new PropertyDescriptor("soLinger", beanClass, "getSoLinger", "setSoLinger");
            soLinger.setBound(true);
            soLinger.setConstrained(false);
            soLinger.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SO_LINGER",null));
            soLinger.setShortDescription(AS400JDBCDriver.getResource("SO_LINGER_DESC",null));

            PropertyDescriptor soTimeout = new PropertyDescriptor("soTimeout", beanClass, "getSoTimeout", "setSoTimeout");
            soTimeout.setBound(true);
            soTimeout.setConstrained(false);
            soTimeout.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SO_TIMEOUT",null));
            soTimeout.setShortDescription(AS400JDBCDriver.getResource("SO_TIMEOUT_DESC",null));

            PropertyDescriptor tcpNoDelay = new PropertyDescriptor("tcpNoDelay", beanClass, "getTcpNoDelay", "setTcpNoDelay");
            tcpNoDelay.setBound(true);
            tcpNoDelay.setConstrained(false);
            tcpNoDelay.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TCP_NO_DELAY",null));
            tcpNoDelay.setShortDescription(AS400JDBCDriver.getResource("TCP_NO_DELAY_DESC",null));

            // @M0A - added for UTF-16 support in the database
            PropertyDescriptor packageCCSID = new PropertyDescriptor("packageCCSID", beanClass, "getPackageCCSID", "setPackageCCSID");
            packageCCSID.setBound(true);
            packageCCSID.setConstrained(false);
            packageCCSID.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CCSID",null));
            packageCCSID.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CCSID_DESC",null));

            // @M0A - added for 63 digit decimal precision support
            PropertyDescriptor minimumDivideScale = new PropertyDescriptor("minimumDivideScale", beanClass, "getMinimumDivideScale", "setMinimumDivideScale");
            minimumDivideScale.setBound(true);
            minimumDivideScale.setConstrained(false);
            minimumDivideScale.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MINIMUM_DIVIDE_SCALE",null));
            minimumDivideScale.setShortDescription(AS400JDBCDriver.getResource("MINIMUM_DIVIDE_SCALE_DESC",null));

            // @A6A 
            PropertyDescriptor maximumBlockedInputRows = new PropertyDescriptor("maximumBlockedInputRows", beanClass, "getMaximumBlockedInputRows", "setMaximumBlockedInputRows");
            maximumBlockedInputRows.setBound(true);
            maximumBlockedInputRows.setConstrained(false);
            maximumBlockedInputRows.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MAXIMUM_BLOCKED_INPUT_ROWS",null));
            maximumBlockedInputRows.setShortDescription(AS400JDBCDriver.getResource("MAXIMUM_BLOCKED_INPUT_ROWS_DESC",null));

            
            
            // @M0A
            PropertyDescriptor maximumPrecision = new PropertyDescriptor("maximumPrecision", beanClass, "getMaximumPrecision", "setMaximumPrecision");
            maximumPrecision.setBound(true);
            maximumPrecision.setConstrained(false);
            maximumPrecision.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MAXIMUM_PRECISION",null));
            maximumPrecision.setShortDescription(AS400JDBCDriver.getResource("MAXIMUM_PRECISION_DESC",null));

            // @M0A
            PropertyDescriptor maximumScale = new PropertyDescriptor("maximumScale", beanClass, "getMaximumScale", "setMaximumScale");
            maximumScale.setBound(true);
            maximumScale.setConstrained(false);
            maximumScale.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MAXIMUM_SCALE",null));
            maximumScale.setShortDescription(AS400JDBCDriver.getResource("MAXIMUM_SCALE_DESC",null));

            // @M0A - added support for hex constant parser option
            PropertyDescriptor translateHex = new PropertyDescriptor("translateHex", beanClass, "getTranslateHex", "setTranslateHex");
            translateHex.setBound(true);                                                                        //@K5C
            translateHex.setConstrained(false);                                                                 //@K5C
            translateHex.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSLATE_HEX",null));                //@K5C
            translateHex.setShortDescription(AS400JDBCDriver.getResource("TRANSLATE_HEX_DESC",null));                //@K5C

            // @KBA - added support for true auto commit
            PropertyDescriptor trueAutoCommit = new PropertyDescriptor("trueAutoCommit", beanClass, "isTrueAutoCommit", "setTrueAutoCommit");   //@KBA //@true
            trueAutoCommit.setBound(true);                                                                      //@KBA //@true
            trueAutoCommit.setConstrained(false);                                                               //@KBA //@true
            trueAutoCommit.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRUE_AUTO_COMMIT",null));                //@KBA //@true
            trueAutoCommit.setShortDescription(AS400JDBCDriver.getResource("TRUE_AUTO_COMMIT_DESC",null));                //@KBA //@true

            //@K94 - added support for holding a cursor across rollbacks
            PropertyDescriptor rollbackCursorHold = new PropertyDescriptor("rollbackCursorHold", beanClass, "isRollbackCursorHold", "setRollbackCursorHold");    //@K94
            rollbackCursorHold.setBound(true);  //@K94
            rollbackCursorHold.setConstrained(false);   //@K94
            rollbackCursorHold.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ROLLBACK_CURSOR_HOLD",null));   //@K94
            rollbackCursorHold.setShortDescription(AS400JDBCDriver.getResource("ROLLBACK_CURSOR_HOLD_DESC",null));   //@K94
                                                                                                                                               
            // @K54 - added support for variable-length field compression
            PropertyDescriptor variableFieldCompression = new PropertyDescriptor("variableFieldCompression", beanClass, "getVariableFieldCompression", "setVariableFieldCompression");   //@K3A
            variableFieldCompression.setBound(true);                                                                      //@K54
            variableFieldCompression.setConstrained(false);                                                               //@K54
            variableFieldCompression.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_VARIABLE_FIELD_COMPRESSION",null));                //@K54
            variableFieldCompression.setShortDescription(AS400JDBCDriver.getResource("VARIABLE_FIELD_COMPRESSION_DESC",null));                //@K54

            //@540 - added support for query optimize goal
            PropertyDescriptor queryOptimizeGoal = new PropertyDescriptor("queryOptimizeGoal", beanClass, "getQueryOptimizeGoal", "setQueryOptimizeGoal"); // @540
            queryOptimizeGoal.setBound(true);                                                                                     // @540
            queryOptimizeGoal.setConstrained(false);                                                                              // @540
            queryOptimizeGoal.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QUERY_OPTIMIZE_GOAL",null));                         // @540
            queryOptimizeGoal.setShortDescription(AS400JDBCDriver.getResource("QUERY_OPTIMIZE_GOAL_DESC",null));                         // @540

            //@540 - added support for XA loosely coupled support
            PropertyDescriptor xaLooselyCoupledSupport = new PropertyDescriptor("xaLooselyCoupledSupport", beanClass, "getXALooselyCoupledSupport", "setXALooselyCoupledSupport"); // @540
            xaLooselyCoupledSupport.setBound(true);                                                                                     // @540
            xaLooselyCoupledSupport.setConstrained(false);                                                                              // @540
            xaLooselyCoupledSupport.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_XA_LOOSELY_COUPLED_SUPPORT",null));                    // @540
            xaLooselyCoupledSupport.setShortDescription(AS400JDBCDriver.getResource("XA_LOOSELY_COUPLED_SUPPORT_DESC",null));                    // @540

            //@PDA - added support for Translate Boolean 
            PropertyDescriptor translateBoolean = new PropertyDescriptor("translateBoolean", beanClass, "isTranslateBoolean", "setTranslateBoolean");
            translateBoolean.setBound(true);
            translateBoolean.setConstrained(false);
            translateBoolean.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSLATE_BOOLEAN",null));
            translateBoolean.setShortDescription(AS400JDBCDriver.getResource("TRANSLATE_BOOLEAN_DESC",null));

            //@PDA - added support for metadata source 
            PropertyDescriptor metaDataSource = new PropertyDescriptor("metaDataSource", beanClass, "getMetaDataSource", "setMetaDataSource");
            metaDataSource.setBound(true);  //@PDC fix name
            metaDataSource.setConstrained(false); //@PDC fix name
            metaDataSource.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_METADATA_SOURCE",null)); //@PDC fix name
            metaDataSource.setShortDescription(AS400JDBCDriver.getResource("METADATA_SOURCE_DESC",null)); //@PDC fix name

            //@550 - added support for query storage limit
            PropertyDescriptor queryStorageLimit = new PropertyDescriptor("queryStorageLimit", beanClass, "getQueryStorageLimit", "setQueryStorageLimit");  //@550
            queryStorageLimit.setBound(true);   //@550
            queryStorageLimit.setConstrained(false);    //@550
            queryStorageLimit.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QUERY_STORAGE_LIMIT",null)); //@550
            queryStorageLimit.setShortDescription(AS400JDBCDriver.getResource("QUERY_STORAGE_LIMIT_DESC",null)); //@550

            //@DFA - added support for decfloat rounding mode
            PropertyDescriptor decfloatRoundingMode = new PropertyDescriptor("decfloatRoundingMode", beanClass, "getDecfloatRoundingMode", "setDecfloatRoundingMode");  //@DFA
            decfloatRoundingMode.setBound(true);   //@DFA
            decfloatRoundingMode.setConstrained(false);    //@DFA
            decfloatRoundingMode.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DECFLOAT_ROUNDING_MODE",null)); //@DFA
            decfloatRoundingMode.setShortDescription(AS400JDBCDriver.getResource("DECFLOAT_ROUNDING_MODE_DESC",null)); //@DFA

            //@CE1 - added support for throwing sqlException when commit is called if autocommit is on
            PropertyDescriptor autocommitException = new PropertyDescriptor("autocommitException", beanClass, "isAutocommitException", "setAutocommitException");  //@CE1
            autocommitException.setBound(true);   //@CE1
            autocommitException.setConstrained(false);    //@CE1
            autocommitException.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_AUTOCOMMIT_EXCEPTION",null)); //@CE1
            autocommitException.setShortDescription(AS400JDBCDriver.getResource("AUTOCOMMIT_EXCEPTION_DESC",null)); //@CE1

            //@AC1 - added support auto commit default on new connections
            PropertyDescriptor autoCommit = new PropertyDescriptor("autoCommit", beanClass, "isAutoCommit", "setAutoCommit");  //@AC1
            autoCommit.setBound(true);   //@AC1
            autoCommit.setConstrained(false);    //@AC1
            autoCommit.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_AUTO_COMMIT",null)); //@AC1
            autoCommit.setShortDescription(AS400JDBCDriver.getResource("AUTO_COMMIT_DESC",null)); //@AC1

            //@igwrn - added support to ignore warnings
            PropertyDescriptor ignoreWarnings = new PropertyDescriptor("ignoreWarnings", beanClass, "getIgnoreWarnings", "setIgnoreWarnings");
            ignoreWarnings.setBound(true);
            ignoreWarnings.setConstrained(false);
            ignoreWarnings.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_IGNORE_WARNINGS",null));
            ignoreWarnings.setShortDescription(AS400JDBCDriver.getResource("IGNORE_WARNINGS_DESC",null));
            
            //@pw3 - added support to allow/disallow "" and *current for user name and password
            PropertyDescriptor secureCurrentUser = new PropertyDescriptor("secureCurrentUser", beanClass, "isSecureCurrentUser", "setSecureCurrentUser");
            secureCurrentUser.setBound(true);
            secureCurrentUser.setConstrained(false);
            secureCurrentUser.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SECURE_CURRENT_USER",null));
            secureCurrentUser.setShortDescription(AS400JDBCDriver.getResource("SECURE_CURRENT_USER_DESC",null));

            //@cc1 - added support for concurrent access resolution
            PropertyDescriptor  concurrentAccessResolution = new PropertyDescriptor("concurrentAccessResolution", beanClass, "getConcurrentAccessResolution", "setConcurrentAccessResolution");
            concurrentAccessResolution.setBound(true);
            concurrentAccessResolution.setConstrained(false);
            concurrentAccessResolution.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CONCURRENT_ACCESS_RESOLUTION",null));
            concurrentAccessResolution.setShortDescription(AS400JDBCDriver.getResource("CONCURRENT_ACCESS_RESOLUTION_DESC",null));

            //@dmy - added support for temp fix for jvm 1.6 memory stomping
            PropertyDescriptor  jvm16Synchronize = new PropertyDescriptor("jvm16Synchronize", beanClass, "isJvm16Synchronize", "setJvm16Synchronize");
            jvm16Synchronize.setBound(true);
            jvm16Synchronize.setConstrained(false);
            jvm16Synchronize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_JVM16_SYNCHRONIZE",null));
            jvm16Synchronize.setShortDescription(AS400JDBCDriver.getResource("JVM16_SYNCHRONIZE_DESC",null));

            //@STIMEOUT - added support for socket timeout
            PropertyDescriptor socketTimeout = new PropertyDescriptor("socketTimeout", beanClass, "getSocketTimeout", "setSocketTimeout"); 
            socketTimeout.setBound(true);                                                                                                       
            socketTimeout.setConstrained(false);                                                                                        
            socketTimeout.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SOCKET_TIMEOUT",null));                                       
            socketTimeout.setShortDescription(AS400JDBCDriver.getResource("SOCKET_TIMEOUT_DESC",null));                                       

            
            // @D4A - added support for query replace truncated parameter
            PropertyDescriptor queryReplaceTruncatedParameter = new PropertyDescriptor("queryReplaceTruncatedParameter", beanClass, "getQueryReplaceTruncatedParameter", "setQueryReplaceTruncatedParameter");
            queryReplaceTruncatedParameter.setBound(true);                                                                        
            queryReplaceTruncatedParameter.setConstrained(false);                                                                 
            queryReplaceTruncatedParameter.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QUERY_REPLACE_TRUNCATED_PARAMETER",null));                
            queryReplaceTruncatedParameter.setShortDescription(AS400JDBCDriver.getResource("QUERY_REPLACE_TRUNCATED_PARAMETER_DESC",null));                

            
            // @D4A - added support for query timeout mechanism
            PropertyDescriptor queryTimeoutMechanism = new PropertyDescriptor("queryTimeoutMechanism", beanClass, "getQueryTimeoutMechanism", "setQueryTimeoutMechanism");
            queryTimeoutMechanism.setBound(true);                                                                        
            queryTimeoutMechanism.setConstrained(false);                                                                 
            queryTimeoutMechanism.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QUERY_TIMEOUT_MECHANISM",null));                
            queryTimeoutMechanism.setShortDescription(AS400JDBCDriver.getResource("QUERY_TIMEOUT_MECHANISM_DESC",null));                


            // @D4A - added support for query replace truncated parameter
            PropertyDescriptor numericRangeError = 
                new PropertyDescriptor("numericRangeError", beanClass, "getNumericRangeError", "setNumericRangeError");
            numericRangeError.setBound(true);                                                                        
            numericRangeError.setConstrained(false);                                                                 
            numericRangeError.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_NUMERIC_RANGE_ERROR",null));                
            numericRangeError.setShortDescription(AS400JDBCDriver.getResource("NUMERIC_RANGE_ERROR_DESC",null));                

            // @D4A - added support for query replace truncated parameter
            PropertyDescriptor characterTruncation = 
                new PropertyDescriptor("characterTruncation", beanClass, "getCharacterTruncation", "setCharacterTruncation");
            characterTruncation.setBound(true);                                                                        
            characterTruncation.setConstrained(false);                                                                 
            characterTruncation.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CHARACTER_TRUNCATION",null));                
            characterTruncation.setShortDescription(AS400JDBCDriver.getResource("CHARACTER_TRUNCATION_DESC",null));                

            // Added missing properties.
            PropertyDescriptor secondaryURL = 
                new PropertyDescriptor("secondaryURL", beanClass, "getSecondaryURL","setSecondaryURL"); 
            secondaryURL.setBound(true);                                                                        
            secondaryURL.setConstrained(false);                                                                 
            secondaryURL.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SECONDARY_URL",null));                
            secondaryURL.setShortDescription(AS400JDBCDriver.getResource("SECONDARY_URL_DESC",null));                

            // Todo:  set properties in MRI FILE
 
 

            PropertyDescriptor packageCcsid = 
            new PropertyDescriptor("packageCcsid", beanClass, "getPackageCcsid","setPackageCcsid"); 
        packageCcsid.setBound(true);                                                                        
        packageCcsid.setConstrained(false);                                                                 
        packageCcsid.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CCSID",null));                
        packageCcsid.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CCSID_DESC",null));                

            PropertyDescriptor toolboxTrace = 
            new PropertyDescriptor("toolboxTrace", beanClass, "getToolboxTrace","setToolboxTrace"); 
        toolboxTrace.setBound(true);                                                                        
        toolboxTrace.setConstrained(false);                                                                 
        toolboxTrace.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE_TOOLBOX",null));                
        toolboxTrace.setShortDescription(AS400JDBCDriver.getResource("TRACE_TOOLBOX_DESC",null));                
            
 
            PropertyDescriptor qaqqinilib = 
            new PropertyDescriptor("qaqqinilib", beanClass, "getQaqqinilib","setQaqqinilib"); 
        qaqqinilib.setBound(true);                                                                        
        qaqqinilib.setConstrained(false);                                                                 
        qaqqinilib.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QAQQINILIB",null));                
        qaqqinilib.setShortDescription(AS400JDBCDriver.getResource("QAQQINILIB_DESC",null));                
        
            PropertyDescriptor trueAutocommit = 
            new PropertyDescriptor("trueAutocommit", beanClass, "isTrueAutocommit","setTrueAutocommit"); 
        trueAutocommit.setBound(true);                                                                        
        trueAutocommit.setConstrained(false);                                                                 
        trueAutocommit.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRUE_AUTO_COMMIT",null));                
        trueAutocommit.setShortDescription(AS400JDBCDriver.getResource("TRUE_AUTO_COMMIT_DESC",null));                
        
            PropertyDescriptor metadataSource = 
            new PropertyDescriptor("metadataSource", beanClass, "getMetadataSource","setMetadataSource"); 
        metadataSource.setBound(true);                                                                        
        metadataSource.setConstrained(false);                                                                 
        metadataSource.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_METADATA_SOURCE",null));                
        metadataSource.setShortDescription(AS400JDBCDriver.getResource("METADATA_SOURCE_DESC",null));                

            PropertyDescriptor useBlockUpdate  = 
            new PropertyDescriptor("useBlockUpdate", beanClass, "isUseBlockUpdate","setUseBlockUpdate"); 
        useBlockUpdate.setBound(true);                                                                        
        useBlockUpdate.setConstrained(false);                                                                 
        useBlockUpdate.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_USE_BLOCK_UPDATE",null));                
        useBlockUpdate.setShortDescription(AS400JDBCDriver.getResource("USE_BLOCK_UPDATE_DESC",null));                

            PropertyDescriptor  describeOption= 
            new PropertyDescriptor("describeOption", beanClass, "getDescribeOption","setDescribeOption"); 
        describeOption.setBound(true);                                                                        
        describeOption.setConstrained(false);                                                                 
        describeOption.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DESCRIBE_OPTION",null));                
        describeOption.setShortDescription(AS400JDBCDriver.getResource("DESCRIBE_OPTION_DESC",null));                

            PropertyDescriptor decimalDataErrors = 
            new PropertyDescriptor("decimalDataErrors", beanClass, "getDecimalDataErrors","setDecimalDataErrors"); 
        decimalDataErrors.setBound(true);                                                                        
        decimalDataErrors.setConstrained(false);                                                                 
        decimalDataErrors.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DECIMAL_DATA_ERRORS",null));                
        decimalDataErrors.setShortDescription(AS400JDBCDriver.getResource("DECIMAL_DATA_ERRORS_DESC",null));                

            PropertyDescriptor  timestampFormat = 
            new PropertyDescriptor("timestampFormat", beanClass, "getTimestampFormat","setTimestampFormat"); 
        timestampFormat.setBound(true);                                                                        
        timestampFormat.setConstrained(false);                                                                 
        timestampFormat.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TIMESTAMP_FORMAT",null));                
        timestampFormat.setShortDescription(AS400JDBCDriver.getResource("TIMESTAMP_FORMAT_DESC",null));                

            PropertyDescriptor  useDrdaMetadataVersion = 
            new PropertyDescriptor("useDrdaMetadataVersion", beanClass, "isUseDrdaMetadataVersion","setUseDrdaMetadataVersion"); 
        useDrdaMetadataVersion.setBound(true);                                                                        
        useDrdaMetadataVersion.setConstrained(false);                                                                 
        useDrdaMetadataVersion.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_USE_DRDA_METADATA_VERSION",null));                
        useDrdaMetadataVersion.setShortDescription(AS400JDBCDriver.getResource("USE_DRDA_METADATA_VERSION_DESC",null));                

        /*@V1A*/
        PropertyDescriptor portNumber = 
        new PropertyDescriptor("portNumber", beanClass, "getPortNumber","setPortNumber"); 
    portNumber.setBound(true);                                                                        
    portNumber.setConstrained(false);                                                                 
    portNumber.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PORTNUMBER",null));                
    portNumber.setShortDescription(AS400JDBCDriver.getResource("PORTNUMBER_DESC",null));                

    PropertyDescriptor enableClientAffinitiesList = 
    new PropertyDescriptor("enableClientAffinitiesList", beanClass, "getEnableClientAffinitiesList","setEnableClientAffinitiesList"); 
enableClientAffinitiesList.setBound(true);                                                                        
enableClientAffinitiesList.setConstrained(false);                                                                 
enableClientAffinitiesList.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ENABLE_CLIENT_AFFINITIES_LIST",null));                
enableClientAffinitiesList.setShortDescription(AS400JDBCDriver.getResource("ENABLE_CLIENT_AFFINITIES_LIST_DESC",null));                


PropertyDescriptor clientRerouteAlternateServerName  = 
new PropertyDescriptor("clientRerouteAlternateServerName", beanClass, 
    "getClientRerouteAlternateServerName","setClientRerouteAlternateServerName"); 
clientRerouteAlternateServerName .setBound(true);                                                                        
clientRerouteAlternateServerName .setConstrained(false);                                                                 
clientRerouteAlternateServerName .setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CLIENT_REROUTE_ALTERNATE_SERVER_NAME",null));                
clientRerouteAlternateServerName .setShortDescription(AS400JDBCDriver.getResource("CLIENT_REROUTE_ALTERNATE_SERVER_NAME_DESC",null));                

PropertyDescriptor clientRerouteAlternatePortNumber  = 
new PropertyDescriptor("clientRerouteAlternatePortNumber", beanClass, 
    "getClientRerouteAlternatePortNumber","setClientRerouteAlternatePortNumber"); 
clientRerouteAlternatePortNumber .setBound(true);                                                                        
clientRerouteAlternatePortNumber .setConstrained(false);                                                                 
clientRerouteAlternatePortNumber .setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CLIENT_REROUTE_ALTERNATE_PORT_NUMBER",null));                
clientRerouteAlternatePortNumber .setShortDescription(AS400JDBCDriver.getResource("CLIENT_REROUTE_ALTERNATE_PORT_NUMBER_DESC",null));                



PropertyDescriptor affinityFailbackInterval  = 
new PropertyDescriptor("affinityFailbackInterval", beanClass, 
    "getAffinityFailbackInterval","setAffinityFailbackInterval"); 
affinityFailbackInterval.setBound(true);                                                                        
affinityFailbackInterval .setConstrained(false);                                                                 
affinityFailbackInterval .setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_AFFINITY_FAILBACK_INTERVAL",null));                
affinityFailbackInterval .setShortDescription(AS400JDBCDriver.getResource("AFFINITY_FAILBACK_INTERVAL_DESC",null));                

PropertyDescriptor maxRetriesForClientReroute  = 
new PropertyDescriptor("maxRetriesForClientReroute", beanClass, 
    "getMaxRetriesForClientReroute","setMaxRetriesForClientReroute"); 
maxRetriesForClientReroute.setBound(true);                                                                        
maxRetriesForClientReroute .setConstrained(false);                                                                 
maxRetriesForClientReroute .setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MAX_RETRIES_FOR_CLIENT_REROUTE",null));                
maxRetriesForClientReroute .setShortDescription(AS400JDBCDriver.getResource("MAX_RETRIES_FOR_CLIENT_REROUTE_DESC",null));                

PropertyDescriptor retryIntervalForClientReroute  = 
new PropertyDescriptor("retryIntervalForClientReroute", beanClass, 
    "getRetryIntervalForClientReroute","setRetryIntervalForClientReroute"); 
retryIntervalForClientReroute.setBound(true);                                                                        
retryIntervalForClientReroute .setConstrained(false);                                                                 
retryIntervalForClientReroute .setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RETRY_INTERVAL_FOR_CLIENT_REROUTE",null));                
retryIntervalForClientReroute .setShortDescription(AS400JDBCDriver.getResource("RETRY_INTERVAL_FOR_CLIENT_REROUTE_DESC",null));                

PropertyDescriptor enableSeamlessFailover  = 
new PropertyDescriptor("enableSeamlessFailover", beanClass, 
    "getEnableSeamlessFailover","setEnableSeamlessFailover"); 
enableSeamlessFailover.setBound(true);                                                                        
enableSeamlessFailover .setConstrained(false);                                                                 
enableSeamlessFailover .setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ENABLE_SEAMLESS_FAILOVER",null));                
enableSeamlessFailover .setShortDescription(AS400JDBCDriver.getResource("ENABLE_SEAMLESS_FAILOVER_DESC",null));                



            
            properties_ = new PropertyDescriptor[] { access, behaviorOverride, bidiStringType, bigDecimal, blockCriteria, blockSize, cursorHold, cursorSensitivity, databaseName, dataCompression, dataSourceName, dataTruncation, dateFormat, dateSeparator, //@A4C @J6C @J7c
                decimalSeparator, description, driver, errors, extendedDynamic, extendedMetaData, extendedMetadata, fullOpen, lazyClose, libraries, lobThreshold, naming, packageName, packageAdd, packageCache, packageClear,              //@W1c @J5C
                packageCriteria, packageError, packageLibrary, password, prefetch, prompt, proxyServer, remarks, savePassword, secondaryUrl, secure, serverName, sort,
                sortLanguage, sortTable, sortWeight, threadUsed, timeFormat, timeSeparator, trace, transactionIsolation, translateBinary, user,
                keepAlive, receiveBufferSize, sendBufferSize, soLinger, soTimeout, tcpNoDelay, packageCCSID, minimumDivideScale, maximumPrecision, maximumScale, translateHex, traceToolbox, qaqqiniLibrary, traceServerCategories, loginTimeout, trueAutoCommit, holdLocators, bidiImplicitReordering, bidiNumericOrdering, holdStatements, rollbackCursorHold, variableFieldCompression,  // @M0C - added package CCSID property and decimal scale & precision properties  //@j1c //@K2A //@K4A //@K5A //@KBC //@K24 //@KLA //@K94  //@K54
                queryOptimizeGoal, xaLooselyCoupledSupport, translateBoolean, 
                metaDataSource, queryStorageLimit, decfloatRoundingMode, 
                autocommitException, autoCommit, ignoreWarnings, secureCurrentUser, 
                concurrentAccessResolution, jvm16Synchronize, socketTimeout, 
                maximumBlockedInputRows, queryReplaceTruncatedParameter, queryTimeoutMechanism,
                numericRangeError, characterTruncation,
                secondaryURL, serverTrace  ,packageCcsid ,toolboxTrace ,qaqqinilib , 
                trueAutocommit ,metadataSource ,useBlockUpdate  ,describeOption,decimalDataErrors , 
                timestampFormat , useDrdaMetadataVersion , portNumber, 
                enableClientAffinitiesList,clientRerouteAlternateServerName,
                clientRerouteAlternatePortNumber, affinityFailbackInterval, 
                maxRetriesForClientReroute, retryIntervalForClientReroute, enableSeamlessFailover
            }; //@540 @550 //@DFA //@pdc //@AC1 //@igwrn //@pw3 //@cc1 //@dmy //@STIMEOUT

        
        
        }
        catch(Exception e)
        { 
            Error error = new Error(e.toString());
            error.initCause(e);
            throw error ;
        }
    }

    /**
      Returns the bean descriptor.
      @return The bean descriptor.
    **/
    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(beanClass);
    }


    /**
      Returns the index of the default event.
      @return The index to the default event.
    **/
    public int getDefaultEventIndex()
    {
        return 0;
    }

    /**
      Returns the index of the default property.
      @return The index to the default property.
    **/
    public int getDefaultPropertyIndex()
    {
        return 0;
    }

    /**
      Returns the descriptors for all events.
      @return The descriptors for all events.
    **/
    public EventSetDescriptor[] getEventSetDescriptors()
    {
        return events_;
    }

    /**
      Returns an image for the icon.
 
      @param icon    The icon size and color.
      @return        The image.
    **/
    public Image getIcon (int icon)
    {
        Image image = null;
        switch(icon)
        {
            case BeanInfo.ICON_MONO_16x16:
            case BeanInfo.ICON_COLOR_16x16:
                image = loadImage ("AS400JDBCDataSource16.gif");
                break;
            case BeanInfo.ICON_MONO_32x32:
            case BeanInfo.ICON_COLOR_32x32:
                image = loadImage ("AS400JDBCDataSource32.gif");
                break;
        }
        return image;
    }

    /**
      Returns the descriptors for all properties.
      @return The descriptors for all properties.
    **/
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return properties_;
    }
}
