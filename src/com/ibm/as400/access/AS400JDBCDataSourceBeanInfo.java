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
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;


/**
*  The AS400JDBCDataSourceBeanInfo class provides bean information
*  for the AS400JDBCDataSource class.
**/
public class AS400JDBCDataSourceBeanInfo extends SimpleBeanInfo
{
    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";


    // Class this bean info represents.
    private final static Class beanClass = AS400JDBCDataSource.class;

    // Handles loading the appropriate resource bundle
    private static ResourceBundleLoader loader_;
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
            changed.setDisplayName(loader_.getText("EVT_NAME_PROPERTY_CHANGE"));
            changed.setShortDescription(loader_.getText("EVT_DESC_PROPERTY_CHANGE"));

            EventSetDescriptor[] events = { changed};

            events_ = events;

            // ***** PROPERTIES
            PropertyDescriptor access = new PropertyDescriptor("access", beanClass, "getAccess", "setAccess");
            access.setBound(true);
            access.setConstrained(false);
            access.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ACCESS"));
            access.setShortDescription(AS400JDBCDriver.getResource("ACCESS_DESC"));

            PropertyDescriptor behaviorOverride = new PropertyDescriptor("behaviorOverride", beanClass, "getBehaviorOverride", "setBehaviorOverride"); // @J7A
            behaviorOverride.setBound(true);                                                                                     // @J7A
            behaviorOverride.setConstrained(false);                                                                              // @J7A
            behaviorOverride.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BEHAVIOR_OVERRIDE"));                         // @J7A
            behaviorOverride.setShortDescription(AS400JDBCDriver.getResource("BEHAVIOR_OVERRIDE_DESC"));                         // @J7A

            PropertyDescriptor bidiStringType = new PropertyDescriptor("bidiStringType", beanClass, "getBidiStringType", "setBidiStringType"); // @A3A
            bidiStringType.setBound(true);                                                                                       // @A3A
            bidiStringType.setConstrained(false);                                                                                // @A3A
            bidiStringType.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIDI_STRING_TYPE"));                            // @A3A
            bidiStringType.setShortDescription(AS400JDBCDriver.getResource("BIDI_STRING_TYPE_DESC"));                  // @A3A

            PropertyDescriptor bidiImplicitReordering = new PropertyDescriptor("bidiImplicitReordering", beanClass, "isBidiImplicitReordering", "setBidiImplicitReordering"); // @K24
            bidiImplicitReordering.setBound(true);                                                                                       //@K24
            bidiImplicitReordering.setConstrained(false);                                                                                //@K24
            bidiImplicitReordering.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIDI_IMPLICIT_REORDERING"));                    //@K24        
            bidiImplicitReordering.setShortDescription(AS400JDBCDriver.getResource("BIDI_IMPLICIT_REORDERING_DESC"));                    //@K24

            PropertyDescriptor bidiNumericOrdering = new PropertyDescriptor("bidiNumericOrdering", beanClass, "isBidiNumericOrdering", "setBidiNumericOrdering"); // @K24
            bidiNumericOrdering.setBound(true);                                                                                       //@K24
            bidiNumericOrdering.setConstrained(false);                                                                                //@K24
            bidiNumericOrdering.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIDI_NUMERIC_ORDERING"));                    //@K24        
            bidiNumericOrdering.setShortDescription(AS400JDBCDriver.getResource("BIDI_NUMERIC_ORDERING_DESC"));                    //@K24

            PropertyDescriptor bigDecimal = new PropertyDescriptor("bigDecimal", beanClass, "isBigDecimal", "setBigDecimal");
            bigDecimal.setBound(true);
            bigDecimal.setConstrained(false);
            bigDecimal.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BIG_DECIMAL"));
            bigDecimal.setShortDescription(AS400JDBCDriver.getResource("BIG_DECIMAL_DESC"));

            PropertyDescriptor blockCriteria = new PropertyDescriptor("blockCriteria", beanClass, "getBlockCriteria", "setBlockCriteria");
            blockCriteria.setBound(true);
            blockCriteria.setConstrained(false);
            blockCriteria.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BLOCK_CRITERIA"));
            blockCriteria.setShortDescription(AS400JDBCDriver.getResource("BLOCK_CRITERIA_DESC"));

            PropertyDescriptor blockSize = new PropertyDescriptor("blockSize", beanClass, "getBlockSize", "setBlockSize");
            blockSize.setBound(true);
            blockSize.setConstrained(false);
            blockSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_BLOCK_SIZE"));
            blockSize.setShortDescription(AS400JDBCDriver.getResource("BLOCK_SIZE_DESC"));

            PropertyDescriptor cursorHold = new PropertyDescriptor("cursorHold", beanClass, "isCursorHold", "setCursorHold");
            cursorHold.setBound(true);
            cursorHold.setConstrained(false);
            cursorHold.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CURSOR_HOLD"));
            cursorHold.setShortDescription(AS400JDBCDriver.getResource("CURSOR_HOLD_DESC"));

            PropertyDescriptor cursorSensitivity = new PropertyDescriptor("cursorSensitivity", beanClass, "getCursorSensitivity", "setCursorSensitivity"); // @J6A
            cursorSensitivity.setBound(true);                                                                                     // @J6A
            cursorSensitivity.setConstrained(false);                                                                              // @J6A
            cursorSensitivity.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_CURSOR_SENSITIVITY"));                         // @J6A
            cursorSensitivity.setShortDescription(AS400JDBCDriver.getResource("CURSOR_SENSITIVITY_DESC"));                         // @J6A

            PropertyDescriptor databaseName = new PropertyDescriptor("databaseName", beanClass, "getDatabaseName", "setDatabaseName");
            databaseName.setBound(true);
            databaseName.setConstrained(false);
            databaseName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATABASE_NAME"));
            databaseName.setShortDescription(AS400JDBCDriver.getResource("DATABASE_NAME_DESC"));

            PropertyDescriptor dataCompression = new PropertyDescriptor("dataCompression", beanClass, "isDataCompression", "setDataCompression");
            dataCompression.setBound(true);
            dataCompression.setConstrained(false);
            dataCompression.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATA_COMPRESSION"));
            dataCompression.setShortDescription(AS400JDBCDriver.getResource("DATA_COMPRESSION_DESC"));

            PropertyDescriptor dataSourceName = new PropertyDescriptor("dataSourceName", beanClass, "getDataSourceName", "setDataSourceName");
            dataSourceName.setBound(true);
            dataSourceName.setConstrained(false);
            dataSourceName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATASOURCE_NAME"));
            dataSourceName.setShortDescription(AS400JDBCDriver.getResource("DATASOURCE_NAME_DESC"));

            PropertyDescriptor dataTruncation = new PropertyDescriptor("dataTruncation", beanClass, "isDataTruncation", "setDataTruncation");
            dataTruncation.setBound(true);
            dataTruncation.setConstrained(false);
            dataTruncation.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATA_TRUNCATION"));
            dataTruncation.setShortDescription(AS400JDBCDriver.getResource("DATA_TRUNCATION_DESC"));

            PropertyDescriptor dateFormat = new PropertyDescriptor("dateFormat", beanClass, "getDateFormat", "setDateFormat");
            dateFormat.setBound(true);
            dateFormat.setConstrained(false);
            dateFormat.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATE_FORMAT"));
            dateFormat.setShortDescription(AS400JDBCDriver.getResource("DATE_FORMAT_DESC"));

            PropertyDescriptor dateSeparator = new PropertyDescriptor("dateSeparator", beanClass, "getDateSeparator", "setDateSeparator");
            dateSeparator.setBound(true);
            dateSeparator.setConstrained(false);
            dateSeparator.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DATE_SEPARATOR"));
            dateSeparator.setShortDescription(AS400JDBCDriver.getResource("DATE_SEPARATOR_DESC"));

            PropertyDescriptor decimalSeparator = new PropertyDescriptor("decimalSeparator", beanClass, "getDecimalSeparator", "setDecimalSeparator");
            decimalSeparator.setBound(true);
            decimalSeparator.setConstrained(false);
            decimalSeparator.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DECIMAL_SEPARATOR"));
            decimalSeparator.setShortDescription(AS400JDBCDriver.getResource("DECIMAL_SEPARATOR_DESC"));

            PropertyDescriptor description = new PropertyDescriptor("description", beanClass, "getDescription", "setDescription");
            description.setBound(true);
            description.setConstrained(false);
            description.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DESCRIPTION"));
            description.setShortDescription(AS400JDBCDriver.getResource("DESCRIPTION_DESC"));

            PropertyDescriptor driver = new PropertyDescriptor("driver", beanClass, "getDriver", "setDriver");   // @A2A
            driver.setBound(true);                                                                               // @A2A
            driver.setConstrained(false);                                                                        // @A2A
            driver.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_DRIVER"));                              // @A2A
            driver.setShortDescription(AS400JDBCDriver.getResource("DRIVER_DESC"));                              // @A2A

            PropertyDescriptor errors = new PropertyDescriptor("errors", beanClass, "getErrors", "setErrors");
            errors.setBound(true);
            errors.setConstrained(false);
            errors.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ERRORS"));
            errors.setShortDescription(AS400JDBCDriver.getResource("ERRORS_DESC"));

            PropertyDescriptor extendedDynamic = new PropertyDescriptor("extendedDynamic", beanClass, "isExtendedDynamic", "setExtendedDynamic");
            extendedDynamic.setBound(true);
            extendedDynamic.setConstrained(false);
            extendedDynamic.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_EXTENDED_DYNAMIC"));
            extendedDynamic.setShortDescription(AS400JDBCDriver.getResource("EXTENDED_DYNAMIC_DESC"));

            PropertyDescriptor extendedMetaData = new PropertyDescriptor("extendedMetaData", beanClass, "isExtendedMetaData", "setExtendedMetaData"); // @J2A
            extendedMetaData.setBound(true);                                                                                     // @J2A
            extendedMetaData.setConstrained(false);                                                                              // @J2A
            extendedMetaData.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_EXTENDED_METADATA"));                         // @J2A
            extendedMetaData.setShortDescription(AS400JDBCDriver.getResource("EXTENDED_METADATA_DESC"));                         // @J2A

            PropertyDescriptor fullOpen = new PropertyDescriptor("fullOpen", beanClass, "isFullOpen", "setFullOpen");    // @W1A
            fullOpen.setBound(true);                                                                                     // @W1A
            fullOpen.setConstrained(false);                                                                              // @W1A
            fullOpen.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_FULL_OPEN"));                                 // @W1A
            fullOpen.setShortDescription(AS400JDBCDriver.getResource("FULL_OPEN_DESC"));                                 // @W1A

            //@KBL  Added support to specify if input locators should be allocated as type hold or not hold
            PropertyDescriptor holdLocators = new PropertyDescriptor("holdInputLocators", beanClass, "isHoldInputLocators", "setHoldInputLocators");   //@KBL
            holdLocators.setBound(true);                                                                                              //@KBL
            holdLocators.setConstrained(false);                                                                                       //@KBL
            holdLocators.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_HOLD_LOCATORS"));                                      //@KBL
            holdLocators.setShortDescription(AS400JDBCDriver.getResource("HOLD_LOCATORS_DESC"));                                      //@KBL

            PropertyDescriptor holdStatements = new PropertyDescriptor("holdStatements", beanClass, "isHoldStatements", "setHoldStatements");    // @KBL
            holdStatements.setBound(true);                                                                                       // @KBL
            holdStatements.setConstrained(false);                                                                                 // @KBL
            holdStatements.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_HOLD_STATEMENTS"));                                  // @KBL
            holdStatements.setShortDescription(AS400JDBCDriver.getResource("HOLD_STATEMENTS_DESC"));                                  // @KBL

            PropertyDescriptor lazyClose = new PropertyDescriptor("lazyClose", beanClass, "isLazyClose", "setLazyClose");    // @A1A
            lazyClose.setBound(true);                                                                                       // @A1A
            lazyClose.setConstrained(false);                                                                                 // @A1A
            lazyClose.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LAZY_CLOSE"));                                  // @A1A
            lazyClose.setShortDescription(AS400JDBCDriver.getResource("LAZY_CLOSE_DESC"));                                  // @A1A

            PropertyDescriptor libraries = new PropertyDescriptor("libraries", beanClass, "getLibraries", "setLibraries");
            libraries.setBound(true);
            libraries.setConstrained(false);
            libraries.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LIBRARIES"));
            libraries.setShortDescription(AS400JDBCDriver.getResource("LIBRARIES_DESC"));

            PropertyDescriptor lobThreshold = new PropertyDescriptor("lobThreshold", beanClass, "getLobThreshold", "setLobThreshold");
            lobThreshold.setBound(true);
            lobThreshold.setConstrained(false);
            lobThreshold.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LOB_THRESHOLD"));
            lobThreshold.setShortDescription(AS400JDBCDriver.getResource("LOB_THRESHOLD_DESC"));

            PropertyDescriptor naming = new PropertyDescriptor("naming", beanClass, "getNaming", "setNaming");
            naming.setBound(true);
            naming.setConstrained(false);
            naming.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_NAMING"));
            naming.setShortDescription(AS400JDBCDriver.getResource("NAMING_DESC"));

            PropertyDescriptor packageName = new PropertyDescriptor("package", beanClass, "getPackage", "setPackage");
            packageName.setBound(true);
            packageName.setConstrained(false);
            packageName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE"));
            packageName.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_DESC"));

            PropertyDescriptor packageAdd = new PropertyDescriptor("packageAdd", beanClass, "isPackageAdd", "setPackageAdd");
            packageAdd.setBound(true);
            packageAdd.setConstrained(false);
            packageAdd.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_ADD"));
            packageAdd.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_ADD_DESC"));

            PropertyDescriptor packageCache = new PropertyDescriptor("packageCache", beanClass, "isPackageCache", "setPackageCache");
            packageCache.setBound(true);
            packageCache.setConstrained(false);
            packageCache.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CACHE"));
            packageCache.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CACHE_DESC"));

            PropertyDescriptor packageClear = new PropertyDescriptor("packageClear", beanClass, "isPackageClear", "setPackageClear");
            packageClear.setBound(true);
            packageClear.setConstrained(false);
            packageClear.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CLEAR"));
            packageClear.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CLEAR_DESC"));

            PropertyDescriptor packageCriteria = new PropertyDescriptor("packageCriteria", beanClass, "getPackageCriteria", "setPackageCriteria");
            packageCriteria.setBound(true);
            packageCriteria.setConstrained(false);
            packageCriteria.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CRITERIA"));
            packageCriteria.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CRITERIA_DESC"));

            PropertyDescriptor packageError = new PropertyDescriptor("packageError", beanClass, "getPackageError", "setPackageError");
            packageError.setBound(true);
            packageError.setConstrained(false);
            packageError.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_ERROR"));
            packageError.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_ERROR_DESC"));

            PropertyDescriptor packageLibrary = new PropertyDescriptor("packageLibrary", beanClass, "getPackageLibrary", "setPackageLibrary");
            packageLibrary.setBound(true);
            packageLibrary.setConstrained(false);
            packageLibrary.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_LIBRARY"));
            packageLibrary.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_LIBRARY_DESC"));

            PropertyDescriptor password = new PropertyDescriptor("password", beanClass, null, "setPassword");
            password.setBound(true);
            password.setConstrained(false);
            password.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PASSWORD"));
            password.setShortDescription(AS400JDBCDriver.getResource("PASSWORD_DESC"));

            PropertyDescriptor prefetch = new PropertyDescriptor("prefetch", beanClass, "isPrefetch", "setPrefetch");
            prefetch.setBound(true);
            prefetch.setConstrained(false);
            prefetch.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PREFETCH"));
            prefetch.setShortDescription(AS400JDBCDriver.getResource("PREFETCH_DESC"));

            PropertyDescriptor prompt = new PropertyDescriptor("prompt", beanClass, "isPrompt", "setPrompt");
            prompt.setBound(true);
            prompt.setConstrained(false);
            prompt.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PROMPT"));
            prompt.setShortDescription(AS400JDBCDriver.getResource("PROMPT_DESC"));

            PropertyDescriptor proxyServer = new PropertyDescriptor("proxyServer", beanClass, "getProxyServer", "setProxyServer");
            proxyServer.setBound(true);
            proxyServer.setConstrained(false);
            proxyServer.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PROXY_SERVER"));
            proxyServer.setShortDescription(AS400JDBCDriver.getResource("PROXY_SERVER_DESC"));

            PropertyDescriptor qaqqiniLibrary = new PropertyDescriptor("qaqqiniLibrary", beanClass, "getQaqqiniLibrary", "setQaqqiniLibrary");  //@K3A
            qaqqiniLibrary.setBound(true);                                                                                                  //@K3A
            qaqqiniLibrary.setConstrained(false);                                                                                           //@K3A
            qaqqiniLibrary.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QAQQINILIB"));                                          //@K3A
            qaqqiniLibrary.setShortDescription(AS400JDBCDriver.getResource("QAQQINILIB_DESC"));                                          //@K3A


            PropertyDescriptor remarks = new PropertyDescriptor("remarks", beanClass, "getRemarks", "setRemarks");
            remarks.setBound(true);
            remarks.setConstrained(false);
            remarks.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_REMARKS"));
            remarks.setShortDescription(AS400JDBCDriver.getResource("REMARKS_DESC"));

            // @J3 New property
            PropertyDescriptor savePassword = new PropertyDescriptor("savePasswordWhenSerialized", beanClass, "isSavePasswordWhenSerialized", "setSavePasswordWhenSerialized");
            savePassword.setBound(true);  //@J4C
            savePassword.setConstrained(false);
            savePassword.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SAVE_PASSWORD_WHEN_SERIALIZED"));
            savePassword.setShortDescription(AS400JDBCDriver.getResource("SAVE_PASSWORD_WHEN_SERIALIZED"));

            PropertyDescriptor secondaryUrl = new PropertyDescriptor("secondaryUrl", beanClass, "getSecondaryUrl", "setSecondaryUrl");
            secondaryUrl.setBound(true);
            secondaryUrl.setConstrained(false);
            secondaryUrl.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SECONDARY_URL"));
            secondaryUrl.setShortDescription(AS400JDBCDriver.getResource("SECONDARY_URL_DESC"));

            PropertyDescriptor secure = new PropertyDescriptor("secure", beanClass, "isSecure", "setSecure");
            secure.setBound(true);
            secure.setConstrained(false);
            secure.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SECURE"));
            secure.setShortDescription(AS400JDBCDriver.getResource("SECURE_DESC"));

            PropertyDescriptor serverName = new PropertyDescriptor("serverName", beanClass, "getServerName", "setServerName");
            serverName.setBound(true);
            serverName.setConstrained(false);
            serverName.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SERVER_NAME"));
            serverName.setShortDescription(AS400JDBCDriver.getResource("SERVER_NAME_DESC"));

            PropertyDescriptor sort = new PropertyDescriptor("sort", beanClass, "getSort", "setSort");
            sort.setBound(true);
            sort.setConstrained(false);
            sort.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT"));
            sort.setShortDescription(AS400JDBCDriver.getResource("SORT_DESC"));

            PropertyDescriptor sortLanguage = new PropertyDescriptor("sortLanguage", beanClass, "getSortLanguage", "setSortLanguage");
            sortLanguage.setBound(true);
            sortLanguage.setConstrained(false);
            sortLanguage.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT_LANGUAGE"));
            sortLanguage.setShortDescription(AS400JDBCDriver.getResource("SORT_LANGUAGE_DESC"));

            PropertyDescriptor sortTable = new PropertyDescriptor("sortTable", beanClass, "getSortTable", "setSortTable");
            sortTable.setBound(true);
            sortTable.setConstrained(false);
            sortTable.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT_TABLE"));
            sortTable.setShortDescription(AS400JDBCDriver.getResource("SORT_TABLE_DESC"));

            PropertyDescriptor sortWeight = new PropertyDescriptor("sortWeight", beanClass, "getSortWeight", "setSortWeight");
            sortWeight.setBound(true);
            sortWeight.setConstrained(false);
            sortWeight.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SORT_WEIGHT"));
            sortWeight.setShortDescription(AS400JDBCDriver.getResource("SORT_WEIGHT_DESC"));

            PropertyDescriptor threadUsed = new PropertyDescriptor("threadUsed", beanClass, "isThreadUsed", "setThreadUsed");
            threadUsed.setBound(true);
            threadUsed.setConstrained(false);
            threadUsed.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_THREAD_USED"));
            threadUsed.setShortDescription(AS400JDBCDriver.getResource("THREAD_USED_DESC"));

            PropertyDescriptor timeFormat = new PropertyDescriptor("timeFormat", beanClass, "getTimeFormat", "setTimeFormat");
            timeFormat.setBound(true);
            timeFormat.setConstrained(false);
            timeFormat.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TIME_FORMAT"));
            timeFormat.setShortDescription(AS400JDBCDriver.getResource("TIME_FORMAT_DESC"));

            PropertyDescriptor timeSeparator = new PropertyDescriptor("timeSeparator", beanClass, "getTimeSeparator", "setTimeSeparator");
            timeSeparator.setBound(true);
            timeSeparator.setConstrained(false);
            timeSeparator.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TIME_SEPARATOR"));
            timeSeparator.setShortDescription(AS400JDBCDriver.getResource("TIME_SEPARATOR_DESC"));

            PropertyDescriptor trace = new PropertyDescriptor("trace", beanClass, "isTrace", "setTrace");   // @w2c
            trace.setBound(true);
            trace.setConstrained(false);
            trace.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE"));
            trace.setShortDescription(AS400JDBCDriver.getResource("TRACE_DESC"));

            PropertyDescriptor traceServer = new PropertyDescriptor("serverTrace", beanClass, "getServerTraceCategories", "setServerTraceCategories");  //@J1a
            traceServer.setBound(true);                                                                                                   //@J1a
            traceServer.setConstrained(false);                                                                                            //@J1a
            traceServer.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE_SERVER"));                                            //@J1a
            traceServer.setShortDescription(AS400JDBCDriver.getResource("TRACE_SERVER_DESC"));                                            //@J1a

            PropertyDescriptor traceServerCategories = new PropertyDescriptor("serverTraceCategories", beanClass, "getServerTraceCategories", "setServerTraceCategories");  //@K4A
            traceServerCategories.setBound(true);                                                                                                   //@K4A
            traceServerCategories.setConstrained(false);                                                                                            //@K4A
            traceServerCategories.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE_SERVER"));                                            //@K4A
            traceServerCategories.setShortDescription(AS400JDBCDriver.getResource("TRACE_SERVER_DESC"));                                            //@K4A

            PropertyDescriptor traceToolbox = new PropertyDescriptor("toolboxTrace", beanClass, "getToolboxTraceCategory", "setToolboxTraceCategory");  //@K2A
            traceToolbox.setBound(true);                                                                                                  //@K2A
            traceToolbox.setConstrained(false);                                                                                           //@K2A
            traceToolbox.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRACE_TOOLBOX"));                                          //@K2A
            traceToolbox.setShortDescription(AS400JDBCDriver.getResource("TRACE_TOOLBOX_DESC"));                                          //@K2A

            PropertyDescriptor transactionIsolation = new PropertyDescriptor("transactionIsolation", beanClass, "getTransactionIsolation", "setTransactionIsolation");
            transactionIsolation.setBound(true);
            transactionIsolation.setConstrained(false);
            transactionIsolation.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSACTION_ISOLATION"));
            transactionIsolation.setShortDescription(AS400JDBCDriver.getResource("TRANSACTION_ISOLATION_DESC"));

            PropertyDescriptor translateBinary = new PropertyDescriptor("translateBinary", beanClass, "isTranslateBinary", "setTranslateBinary");
            translateBinary.setBound(true);
            translateBinary.setConstrained(false);
            translateBinary.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSLATE_BINARY"));
            translateBinary.setShortDescription(AS400JDBCDriver.getResource("TRANSLATE_BINARY_DESC"));

            PropertyDescriptor user = new PropertyDescriptor("user", beanClass, "getUser", "setUser");
            user.setBound(true);
            user.setConstrained(false);
            user.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_USER"));
            user.setShortDescription(AS400JDBCDriver.getResource("USER_DESC"));

            PropertyDescriptor keepAlive = new PropertyDescriptor("keepAlive", beanClass, "getKeepAlive", "setKeepAlive");
            keepAlive.setBound(true);
            keepAlive.setConstrained(false);
            keepAlive.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_KEEP_ALIVE"));
            keepAlive.setShortDescription(AS400JDBCDriver.getResource("KEEP_ALIVE_DESC"));

            PropertyDescriptor loginTimeout = new PropertyDescriptor("loginTimeout", beanClass, "getLoginTimeout", "setLoginTimeout");  //@K5A
            loginTimeout.setBound(true);                                                                                                //@K5A        
            loginTimeout.setConstrained(false);                                                                                         //@K5A
            loginTimeout.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_LOGIN_TIMEOUT"));                                        //@K5A
            loginTimeout.setShortDescription(AS400JDBCDriver.getResource("LOGIN_TIMEOUT_DESC"));                                        //@K5A
            PropertyDescriptor receiveBufferSize = new PropertyDescriptor("receiveBufferSize", beanClass, "getReceiveBufferSize", "setReceiveBufferSize");
            receiveBufferSize.setBound(true);
            receiveBufferSize.setConstrained(false);
            receiveBufferSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_RECEIVE_BUFFER_SIZE"));
            receiveBufferSize.setShortDescription(AS400JDBCDriver.getResource("RECEIVE_BUFFER_SIZE_DESC"));

            PropertyDescriptor sendBufferSize = new PropertyDescriptor("sendBufferSize", beanClass, "getSendBufferSize", "setSendBufferSize");
            sendBufferSize.setBound(true);
            sendBufferSize.setConstrained(false);
            sendBufferSize.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SEND_BUFFER_SIZE"));
            sendBufferSize.setShortDescription(AS400JDBCDriver.getResource("SEND_BUFFER_SIZE_DESC"));

            PropertyDescriptor soLinger = new PropertyDescriptor("soLinger", beanClass, "getSoLinger", "setSoLinger");
            soLinger.setBound(true);
            soLinger.setConstrained(false);
            soLinger.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SO_LINGER"));
            soLinger.setShortDescription(AS400JDBCDriver.getResource("SO_LINGER_DESC"));

            PropertyDescriptor soTimeout = new PropertyDescriptor("soTimeout", beanClass, "getSoTimeout", "setSoTimeout");
            soTimeout.setBound(true);
            soTimeout.setConstrained(false);
            soTimeout.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_SO_TIMEOUT"));
            soTimeout.setShortDescription(AS400JDBCDriver.getResource("SO_TIMEOUT_DESC"));

            PropertyDescriptor tcpNoDelay = new PropertyDescriptor("tcpNoDelay", beanClass, "getTcpNoDelay", "setTcpNoDelay");
            tcpNoDelay.setBound(true);
            tcpNoDelay.setConstrained(false);
            tcpNoDelay.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TCP_NO_DELAY"));
            tcpNoDelay.setShortDescription(AS400JDBCDriver.getResource("TCP_NO_DELAY_DESC"));

            // @M0A - added for UTF-16 support in the database
            PropertyDescriptor packageCCSID = new PropertyDescriptor("packageCCSID", beanClass, "getPackageCCSID", "setPackageCCSID");
            packageCCSID.setBound(true);
            packageCCSID.setConstrained(false);
            packageCCSID.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_PACKAGE_CCSID"));
            packageCCSID.setShortDescription(AS400JDBCDriver.getResource("PACKAGE_CCSID_DESC"));

            // @M0A - added for 63 digit decimal precision support
            PropertyDescriptor minimumDivideScale = new PropertyDescriptor("minimumDivideScale", beanClass, "getMinimumDivideScale", "setMinimumDivideScale");
            minimumDivideScale.setBound(true);
            minimumDivideScale.setConstrained(false);
            minimumDivideScale.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MINIMUM_DIVIDE_SCALE"));
            minimumDivideScale.setShortDescription(AS400JDBCDriver.getResource("MINIMUM_DIVIDE_SCALE_DESC"));

            // @M0A
            PropertyDescriptor maximumPrecision = new PropertyDescriptor("maximumPrecision", beanClass, "getMaximumPrecision", "setMaximumPrecision");
            maximumPrecision.setBound(true);
            maximumPrecision.setConstrained(false);
            maximumPrecision.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MAXIMUM_PRECISION"));
            maximumPrecision.setShortDescription(AS400JDBCDriver.getResource("MAXIMUM_PRECISION_DESC"));

            // @M0A
            PropertyDescriptor maximumScale = new PropertyDescriptor("maximumScale", beanClass, "getMaximumScale", "setMaximumScale");
            maximumScale.setBound(true);
            maximumScale.setConstrained(false);
            maximumScale.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_MAXIMUM_SCALE"));
            maximumScale.setShortDescription(AS400JDBCDriver.getResource("MAXIMUM_SCALE_DESC"));

            // @M0A - added support for hex constant parser option
            PropertyDescriptor translateHex = new PropertyDescriptor("translateHex", beanClass, "getTranslateHex", "setTranslateHex");
            translateHex.setBound(true);                                                                        //@K5C
            translateHex.setConstrained(false);                                                                 //@K5C
            translateHex.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSLATE_HEX"));                //@K5C
            translateHex.setShortDescription(AS400JDBCDriver.getResource("TRANSLATE_HEX_DESC"));                //@K5C

            // @KBA - added support for true auto commit
            PropertyDescriptor autoCommit = new PropertyDescriptor("trueAutoCommit", beanClass, "isTrueAutoCommit", "setTrueAutoCommit");   //@KBA
            autoCommit.setBound(true);                                                                      //@KBA
            autoCommit.setConstrained(false);                                                               //@KBA
            autoCommit.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_AUTO_COMMIT"));                //@KBA
            autoCommit.setShortDescription(AS400JDBCDriver.getResource("AUTO_COMMIT_DESC"));                //@KBA

            //@K94 - added support for holding a cursor across rollbacks
            PropertyDescriptor rollbackCursorHold = new PropertyDescriptor("rollbackCursorHold", beanClass, "isRollbackCursorHold", "setRollbackCursorHold");    //@K94
            rollbackCursorHold.setBound(true);  //@K94
            rollbackCursorHold.setConstrained(false);   //@K94
            rollbackCursorHold.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_ROLLBACK_CURSOR_HOLD"));   //@K94
            rollbackCursorHold.setShortDescription(AS400JDBCDriver.getResource("ROLLBACK_CURSOR_HOLD_DESC"));   //@K94
                                                                                                                                               
            // @K54 - added support for variable-length field compression
            PropertyDescriptor variableFieldCompression = new PropertyDescriptor("variableFieldCompression", beanClass, "isVariableFieldCompression", "setVariableFieldCompression");   //@K54
            variableFieldCompression.setBound(true);                                                                      //@K54
            variableFieldCompression.setConstrained(false);                                                               //@K54
            variableFieldCompression.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_VARIABLE_FIELD_COMPRESSION"));                //@K54
            variableFieldCompression.setShortDescription(AS400JDBCDriver.getResource("VARIABLE_FIELD_COMPRESSION_DESC"));                //@K54

            //@540 - added support for query optimize goal
            PropertyDescriptor queryOptimizeGoal = new PropertyDescriptor("queryOptimizeGoal", beanClass, "getQueryOptimizeGoal", "setQueryOptimizeGoal"); // @540
            queryOptimizeGoal.setBound(true);                                                                                     // @540
            queryOptimizeGoal.setConstrained(false);                                                                              // @540
            queryOptimizeGoal.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_QUERY_OPTIMIZE_GOAL"));                         // @540
            queryOptimizeGoal.setShortDescription(AS400JDBCDriver.getResource("QUERY_OPTIMIZE_GOAL_DESC"));                         // @540

            //@540 - added support for XA loosely coupled support
            PropertyDescriptor xaLooselyCoupledSupport = new PropertyDescriptor("xaLooselyCoupledSupport", beanClass, "getXALooselyCoupledSupport", "setXALooselyCoupledSupport"); // @540
            xaLooselyCoupledSupport.setBound(true);                                                                                     // @540
            xaLooselyCoupledSupport.setConstrained(false);                                                                              // @540
            xaLooselyCoupledSupport.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_XA_LOOSELY_COUPLED_SUPPORT"));                    // @540
            xaLooselyCoupledSupport.setShortDescription(AS400JDBCDriver.getResource("XA_LOOSELY_COUPLED_SUPPORT_DESC"));                    // @540

            //@PDA - added support for Translate Boolean 
            PropertyDescriptor translateBoolean = new PropertyDescriptor("translateBoolean", beanClass, "isTranslateBoolean", "setTranslateBoolean");
            translateBoolean.setBound(true);
            translateBoolean.setConstrained(false);
            translateBoolean.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_TRANSLATE_BOOLEAN"));
            translateBoolean.setShortDescription(AS400JDBCDriver.getResource("TRANSLATE_BOOLEAN_DESC"));
            
            //@PDA - added support for metadata source 
            PropertyDescriptor metaDataSource = new PropertyDescriptor("metaDataSource", beanClass, "getMetaDataSource", "setMetaDataSource");
            translateBoolean.setBound(true);
            translateBoolean.setConstrained(false);
            translateBoolean.setDisplayName(AS400JDBCDriver.getResource("PROP_NAME_METADATA_SOURCE"));
            translateBoolean.setShortDescription(AS400JDBCDriver.getResource("METADATA_SOURCE_DESC"));

            
            properties_ = new PropertyDescriptor[] { access, behaviorOverride, bidiStringType, bigDecimal, blockCriteria, blockSize, cursorHold, cursorSensitivity, databaseName, dataCompression, dataSourceName, dataTruncation, dateFormat, dateSeparator, //@A4C @J6C @J7c
                decimalSeparator, description, driver, errors, extendedDynamic, extendedMetaData, fullOpen, lazyClose, libraries, lobThreshold, naming, packageName, packageAdd, packageCache, packageClear,              //@W1c @J5C
                packageCriteria, packageError, packageLibrary, password, prefetch, prompt, proxyServer, remarks, savePassword, secondaryUrl, secure, serverName, sort,
                sortLanguage, sortTable, sortWeight, threadUsed, timeFormat, timeSeparator, trace, traceServer, transactionIsolation, translateBinary, user,
                keepAlive, receiveBufferSize, sendBufferSize, soLinger, soTimeout, tcpNoDelay, packageCCSID, minimumDivideScale, maximumPrecision, maximumScale, translateHex, traceToolbox, qaqqiniLibrary, traceServerCategories, loginTimeout, autoCommit, holdLocators, bidiImplicitReordering, bidiNumericOrdering, holdStatements, rollbackCursorHold, variableFieldCompression,  // @M0C - added package CCSID property and decimal scale & precision properties  //@j1c //@K2A //@K3A //@K4A //@K5A //@KBC //@K24 //@KLA //@K94  //@K54
                queryOptimizeGoal, xaLooselyCoupledSupport, translateBoolean, metaDataSource}; //@540
        }
        catch(Exception e)
        {
            throw new Error(e.toString());
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
