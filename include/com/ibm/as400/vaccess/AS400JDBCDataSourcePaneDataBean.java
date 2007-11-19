///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400JDBCDataSourcePaneDataBean.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.ui.framework.java.*;
import com.ibm.as400.access.AS400JDBCDataSource;
import java.util.*;

/**
 * The AS400JDBCDataSourcePaneDataBean class sets data in,
 * and returns data from, the AS400JDBCDataSourcePane component.
 * @deprecated Use Java Swing instead, along with the classes in package <tt>com.ibm.as400.access</tt>
 */

public class AS400JDBCDataSourcePaneDataBean extends Object
    implements DataBean
{    
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // General Tab
    private String m_sDataSourceName;
    private String m_sDescription;
    private String m_sAS400Server;
    
    // Server Tab
    private String m_sSQLlibrary;                                       //@A1A
    private String m_sDefaultLibraries;
    private Object m_oCommitMode;
    private ChoiceDescriptor[] m_cdCommitMode;
    private Object m_oMaxPrecision;                                             //@A2A
    private ChoiceDescriptor[] m_cdMaxPrecision;                                //@A2A
    private Object m_oMaxScale;                                                 //@A2A
    private ChoiceDescriptor[] m_cdMaxScale;                                    //@A2A
    private Object m_oMinDivideScale;                                           //@A2A
    private ChoiceDescriptor[] m_cdMinDivideScale;                              //@A2A
    
    // Package Tab
    private boolean m_bEnableExtDynamic;
    private String m_sPackage;
    private String m_sPackageLibrary;
    private String m_sUsageGroup;
    private String m_sUnusablePkgActionGroup;
    private boolean m_bCachePackageLocally;
    
    // Performance Tab
    private boolean m_bEnableLazyClose;
    private boolean m_bEnablePrefetch;
    private boolean m_bEnableDataCompression;
    private Object m_oRecordBlockingCriteria;
    private ChoiceDescriptor[] m_cdRecordBlockingCriteria;
    private Object m_oRecordBlockingSize;
    private ChoiceDescriptor[] m_cdRecordBlockingSize;
    private Object m_oLOBThreshold;
    private ChoiceDescriptor[] m_cdLOBThreshold;
    
    // Language Tab
    private Object m_oSortType;
    private ChoiceDescriptor[] m_cdSortType;
    private String m_sSortTable;
    private String m_sSortWeightGroup;
    private Object m_oLanguage;
    private ChoiceDescriptor[] m_cdLanguage;
    
    // Other Tab
    private String m_sAccessTypeGroup;
    private String m_sRemarksSourceGroup;
    
    // Translation Tab
    private boolean m_bTranslate65535;
    private boolean m_bTranslateHex;                                           //@A2A
    
    // Format Tab
    private Object m_oNamingConvention;
    private ChoiceDescriptor[] m_cdNamingConvention;
    private Object m_oDecimalSeparator;
    private ChoiceDescriptor[] m_cdDecimalSeparator;
    private Object m_oTimeFormat;
    private ChoiceDescriptor[] m_cdTimeFormat;
    private Object m_oTimeSeparator;
    private ChoiceDescriptor[] m_cdTimeSeparator;
    private Object m_oDateFormat;
    private ChoiceDescriptor[] m_cdDateFormat;
    private Object m_oDateSeparator;
    private ChoiceDescriptor[] m_cdDateSeparator;
    
    // Connection Options Tab
    private String m_sDefaultUserID;
    private boolean m_bUseSSL;

    /////////////////
    // General Tab //
    /////////////////

    /** Returns the value of the "Data source name" field on the 
    General tab.
    @return The data source name.
    */
    public String getDataSourceName()
    {
        return m_sDataSourceName;
    }
    
    /** Sets the value of the "Data source name" field on the
    General tab.
    @param s The data source name.
    */
    public void setDataSourceName(String s)
    {
        m_sDataSourceName = s;
    }

    /** Returns the value of the "Description" field on the 
    General tab.
    @return The data source description.
    */
    public String getDescription()
    {
        return m_sDescription;
    }

    /** Sets the value of the "Description" field on the 
    General tab.
    @param s The data source description.
    */
    public void setDescription(String s)
    {
        m_sDescription = s;
    }
    
    /** Returns the value of the "AS/400 server" field on the General tab.
    @return The server name.
    */
    public String getAS400Server()
    {
        return m_sAS400Server;
    }

    /** Sets the value of the "AS/400 server" field on the General tab.
    @param s The server.
    */
    public void setAS400Server(String s)
    {
        m_sAS400Server = s;
    }
    
    ////////////////
    // Server Tab //
    ////////////////
    
    /* Returns the value of the "Default SQL library" field on the Server tab.
    @return The Default SQL library name.                                   //@A1A
    */
    public String getSQLlibrary()                                           //@A1A
    {                                                                       //@A1A
        return m_sSQLlibrary;                                               //@A1A
    }                                                                       //@A1A

    /* Sets the value of the "Default SQL library" field on the Server tab.
    @param s The Default SQL library name.                                  //@A1A
    */
    public void setSQLlibrary(String s)                                     //@A1A
    {                                                                       //@A1A
        m_sSQLlibrary = s;                                                  //@A1A
    }                                                                       //@A1A
    
    /** Returns the value of the "Library list" field on the
    Server tab.
    @return The server name.
    */
    public String getDefaultLibraries()
    {
        return m_sDefaultLibraries;
    }
    
    /** Sets the value of the "Library list" field on the Server tab.
    @param s The library list.
    */
    public void setDefaultLibraries(String s)
    {
        m_sDefaultLibraries = s;
    }

    /** Returns the value of the "Commit mode" field on the
    Server tab.
    @return The commit mode.
    */
    public Object getCommitMode()
    {
        return m_oCommitMode;
    }

    /** Sets the value of the "Commit mode" field on the Server tab.
    @param o The commit mode.
    */
    public void setCommitMode(Object o)
    {
        m_oCommitMode = o;
    }

    /** Returns the list of values in the "Commit mode" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getCommitModeChoices()
    {
        return m_cdCommitMode;
    }
    
    public Object getMaxPrecision()
    {
        return m_oMaxPrecision;
    }

    public void setMaxPrecision(Object o)
    {
        m_oMaxPrecision = o;
    }

    public ChoiceDescriptor[] getMaxPrecisionChoices()
    {
        return m_cdMaxPrecision;
    }

    public Object getMaxScale()
    {
        return m_oMaxScale;
    }

    public void setMaxScale(Object o)
    {
        m_oMaxScale = o;
    }

    public ChoiceDescriptor[] getMaxScaleChoices()
    {
        return m_cdMaxScale;
    }

    public Object getMinDivideScale()
    {
        return m_oMinDivideScale;
    }

    public void setMinDivideScale(Object o)
    {
        m_oMinDivideScale = o;
    }

    public ChoiceDescriptor[] getMinDivideScaleChoices()
    {
        return m_cdMinDivideScale;
    }

    /////////////////
    // Package Tab //
    /////////////////
    
    /** Indicates whether the "Enable extended dynamic (package)
    support" check box on the Package tab is checked.
    @return True if the check box is checked
    */
    
    public boolean isEnableExtDynamic()
    {
        return m_bEnableExtDynamic;
    }
    
    /** Sets the value of the "Enable extended dynamic (package)
    support" check box on the Package tab.
    @param b The check box state.  True if it should be checked,
    false otherwise.
    */
    public void setEnableExtDynamic(boolean b)
    {
        m_bEnableExtDynamic = b;
    }
    
    /** Returns the value of the "Package" field on the
    Package tab.
    @return The package name.
    */
    public String getPackage()
    {
        return m_sPackage;
    }
    /** Sets the value of the "Package" field on the
    Package tab.
    @param s The package name.
    */
    public void setPackage(String s)
    {
        m_sPackage = s;
    }

    /** Returns the value of the "Package library" field on the
    Package tab.
    @return The package library.
    */
    public String getPackageLibrary()
    {
        return m_sPackageLibrary;
    }

    /** Sets the value of the "Package library" field on the
    Package tab.  
    @param s The package library.
    */
    public void setPackageLibrary(String s)
    {
        m_sPackageLibrary = s;
    }

    /** Indicates whether the "Cache package locally"
    check box on the Package tab is checked.
    @return True if the check box is checked
    */
    public boolean isCachePackageLocally()
    {
        return m_bCachePackageLocally;
    }

    /** Sets the value of the "Cache package locally" check
    box on the Package tab.
    @param b The check box state.  True if it should be checked,
    false otherwise.
    */
    public void setCachePackageLocally(boolean b)
    {
        m_bCachePackageLocally = b;
    }

    /** Returns the value of the "Usage" group on the
    Package tab.
    @return The selected radio button.  AJDSP_USE_RADIOBUTTON
    returned when the "Use" radio button is selected.
    AJDSP_USEADD_RADIOBUTTON returned when the "Use and add"
    radio button is selected.
    */
    public String getUsageGroup()
    {
        return m_sUsageGroup;
    }

    /** Sets the value of the "Usage" group on the 
    Package tab.
    @param s The radio button.  AJDSP_USE_RADIOBUTTON if the
    "Use" radio button should be selected.  AJDSP_USEADD_RADIOBUTTON
    if the "Use and add" radio button should be selected.
    */
    public void setUsageGroup(String s)
    {
        m_sUsageGroup = s;
    }

    /** Returns the value of the "Unusable package" group on the
    Package tab.
    @return The selected radio button.  AJDSP_SEND_EXCEP_RADIOBUTTON
    returned when the "Send exception" radio button is selected.
    AJDSP_POST_WARN_RADIOBUTTON returned when the "Post warning"
    radio button is selected.  AJDSP_IGNORE_RADIOBUTTON returned
    when the "Ignore" radio button is selected.
    */
    public String getUnusablePkgActionGroup()
    {
        return m_sUnusablePkgActionGroup;
    }

    /** Sets the value of the "Unusable package" group on the 
    Package tab.
    @param s The radio button.  AJDSP_SEND_EXCEP_RADIOBUTTON if the
    "Send exception" radio button should be selected.  AJDSP_POST_WARN_RADIOBUTTON
    if the "Post warning" radio button should be selected.  AJDSP_IGNORE_RADIOBUTTON
    if the "Ignore" radio button should be selected.
    */
    public void setUnusablePkgActionGroup(String s)
    {
        m_sUnusablePkgActionGroup = s;
    }
    
    /////////////////////
    // Performance Tab //
    /////////////////////
    
    /** Indicates whether the "Enable lazy close support"
    check box on the Performance tab is checked.
    @return True if the check box is checked
    */
    public boolean isEnableLazyClose()
    {
        return m_bEnableLazyClose;
    }

    /** Sets the value of the "Enable lazy close support" check
    box on the Performance tab.
    @param b The check box state.  True if it should be checked,
    false otherwise.
    */
    public void setEnableLazyClose(boolean b)
    {
        m_bEnableLazyClose = b;
    }

    /** Indicates whether the "Enable pre-fetch"
    check box on the Performance tab is checked.
    @return True if the check box is checked
    */
    public boolean isEnablePrefetch()
    {
        return m_bEnablePrefetch;
    }

    /** Sets the value of the "Enable pre-fetch" check
    box on the Performance tab.
    @param b The check box state.  True if it should be checked,
    false otherwise.
    */
    public void setEnablePrefetch(boolean b)
    {
        m_bEnablePrefetch = b;
    }

    /** Indicates whether the "Enable data compression"
    check box on the Performance tab is checked.
    @return True if the check box is checked
    */
    public boolean isEnableDataCompression()
    {
        return m_bEnableDataCompression;
    }

    /** Sets the value of the "Enable data compression" check
    box on the Performance tab.
    @param b The check box state.  True if it should be checked,
    false otherwise.
    */
    public void setEnableDataCompression(boolean b)
    {
        m_bEnableDataCompression = b;
    }

    /** Returns the value of the "Record blocking criteria" field on the
    Performance tab.
    @return The record blocking criteria.
    */
    public Object getRecordBlockingCriteria()
    {
        return m_oRecordBlockingCriteria;
    }

    /** Sets the value of the "Record Blocking criteria" field on the
    Performance tab.
    @param o The record blocking criteria.
    */
    public void setRecordBlockingCriteria(Object o)
    {
        m_oRecordBlockingCriteria = o;
    }

    /** Returns the list of values in the "Record Blocking criteria"" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getRecordBlockingCriteriaChoices()
    {
        return m_cdRecordBlockingCriteria;
    }

    /** Returns the value of the "Record blocking size" field on the
    Performance tab.
    @return The record blocking criteria.
    */
    public Object getRecordBlockingSize()
    {
        return m_oRecordBlockingSize;
    }

    /** Sets the value of the "Record Blocking size" field on the
    Performance tab.
    @param o The record blocking criteria.
    */
    public void setRecordBlockingSize(Object o)
    {
        m_oRecordBlockingSize = o;
    }

    /** Returns the list of values in the "Record Blocking size"" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getRecordBlockingSizeChoices()
    {
        return m_cdRecordBlockingSize;
    }

    /** Returns the value of the "Large object threshold" field on the
    Performance tab.
    @return The large object threshold.
    */
    public Object getLOBThreshold()
    {
        return m_oLOBThreshold;
    }

    /** Sets the value of the "Large object threshold" field on the
    Performance tab.
    @param o The large object threshold.
    */
    public void setLOBThreshold(Object o)
    {
        m_oLOBThreshold = o;
    }

    /** Returns the list of values in the "Large object threshold" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getLOBThresholdChoices()
    {
        return m_cdLOBThreshold;
    }
    
    //////////////////
    // Language Tab //
    //////////////////
    
    /** Returns the value of the "Sort type" field on the
    Language tab.
    @return The sort type.
    */
    public Object getSortType()
    {
        return m_oSortType;
    }

    /** Sets the value of the "Sort type" field on the
    Language tab.
    @param o The sort type.
    */
    public void setSortType(Object o)
    {
        m_oSortType = o;
    }

    /** Returns the list of values in the "Sort type" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getSortTypeChoices()
    {
        return m_cdSortType;
    }

    /** Returns the value of the "Sort library/table name"
    field on the Language tab.
    @return The qualified sort table name.
    */
    public String getSortTable()
    {
        return m_sSortTable;
    }

    /** Sets the value of the "Sort library/table name"
    field on the Language tab.  
    @param s The qualified sort table name.
    */
    public void setSortTable(String s)
    {
        m_sSortTable = s;
    }
    
    /** Returns the value of the "Sort weight" group on the
    Language tab.
    @return The selected radio button.  AJDSP_SHAREDWEIGHT_RADIOBUTTON
    returned when the "Shared" radio button is selected.
    AJDSP_UNIQUEWEIGHT_RADIOBUTTON returned when the "Unique"
    radio button is selected.
    */
    public String getSortWeightGroup()
    {
        return m_sSortWeightGroup;
    }

    /** Sets the value of the "Sort weight" group on the 
    Language tab.
    @param s The radio button.  AJDSP_SHAREDWEIGHT_RADIOBUTTON if the
    "Shared" radio button should be selected.  AJDSP_UNIQUEWEIGHT_RADIOBUTTON
    if the "Unique" radio button should be selected.
    */
    public void setSortWeightGroup(String s)
    {
        m_sSortWeightGroup = s;
    }

    /** Returns the value of the "Language" field on the
    Language tab.
    @return The language.
    */
    public Object getLanguage()
    {
        return m_oLanguage;
    }

    /** Sets the value of the "Language" field on the
    Language tab.
    @param o The language.
    */
    public void setLanguage(Object o)
    {
        m_oLanguage = o;
    }

    /** Returns the list of values in the "Language"" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getLanguageChoices()
    {
        return m_cdLanguage;
    }
    
    ///////////////
    // Other Tab //
    ///////////////
    
    /** Returns the value of the "Access type" group on the
    Other tab.
    @return The selected radio button.  AJDSP_ACCESSTYPE_RW
    returned when the "read/write" radio button is selected.
    AJDSP_ACCESSTYPE_RC returned when the "read/call"
    radio button is selected.  AJDSP_ACCESSTYPE_RO returned
    when the "read only" radio button is selected.
    */
    public String getAccessTypeGroup()
    {
        return m_sAccessTypeGroup;
    }

    /** Sets the value of the "Access type" group on the 
    Other tab.
    @param s The radio button.  AJDSP_ACCESSTYPE_RW if the
    "read/write" radio button should be selected.
    AJDSP_ACCESSTYPE_RC if the "read/call" radio button
    should be selected.  AJDSP_ACCESSTYPE_RO if the 
    (read only) radio button should be selected.
    */
    public void setAccessTypeGroup(String s)
    {
        m_sAccessTypeGroup = s;
    }

    /** Returns the value of the "Remarks source" group on the
    Other tab.
    @return The selected radio button.  AJDSP_SQLDESC_RADIOBUTTON
    returned when the "SQL" radio button is selected.
    AJDSP_OS400DESC_RADIOBUTTON returned when the "OS/400"
    radio button is selected.
    */
    public String getRemarksSourceGroup()
    {
        return m_sRemarksSourceGroup;
    }

    /** Sets the value of the "Remarks source" group on the 
    Other tab.
    @param s The radio button.  AJDSP_SQLDESC_RADIOBUTTON if the
    "SQL" radio button should be selected.  AJDSP_OS400DESC_RADIOBUTTON
    if the "OS/400" radio button should be selected.
    */
    public void setRemarksSourceGroup(String s)
    {
        m_sRemarksSourceGroup = s;
    }
    
    /////////////////////
    // Translation Tab //
    /////////////////////
    
    /** Indicates whether the "Translate CCSID 65535"
    check box on the Translation tab is checked.
    @return True if the check box is checked
    */
    public boolean isTranslate65535()
    {
        return m_bTranslate65535;
    }

    /** Sets the value of the "Translate CCSID 65535" check
    box on the Translation tab.
    @param b The check box state.  True if it should be checked,
    false otherwise.
    */
    public void setTranslate65535(boolean b)
    {
        m_bTranslate65535 = b;
    }
    
    public boolean isTranslateHex()                                             //@A2A
    {                                                                           //@A2A
        return m_bTranslateHex;                                                 //@A2A
    }                                                                           //@A2A

    public void setTranslateHex(boolean b)                                      //@A2A
    {                                                                           //@A2A
        m_bTranslateHex = b;                                                    //@A2A
    }                                                                           //@A2A
   
    ////////////////
    // Format Tab //
    ////////////////
    
    /** Returns the value of the "Naming convention" field on the
    Format tab.
    @return The naming convention.
    */
    public Object getNamingConvention()
    {
        return m_oNamingConvention;
    }

    /** Sets the value of the "Naming convention" field on the
    Format tab.
    @param o The naming convention.
    */
    public void setNamingConvention(Object o)
    {
        m_oNamingConvention = o;
    }

    /** Returns the list of values in the "Naming convention"" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getNamingConventionChoices()
    {
        return m_cdNamingConvention;
    }

    /** Returns the value of the "Decimal separator" field on the
    Format tab.
    @return The decimal separator.
    */
    public Object getDecimalSeparator()
    {
        return m_oDecimalSeparator;
    }

    /** Sets the value of the "Decimal separator" field on the
    Format tab.
    @param o The decimal separator.
    */
    public void setDecimalSeparator(Object o)
    {
        m_oDecimalSeparator = o;
    }

    /** Returns the list of values in the "Decimal separator"" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getDecimalSeparatorChoices()
    {
        return m_cdDecimalSeparator;
    }

    /** Returns the value of the "Time format" field on the
    Format tab.
    @return The time format.
    */
    public Object getTimeFormat()
    {
        return m_oTimeFormat;
    }

    /** Sets the value of the "Time format" field on the
    Format tab.
    @param o The time format.
    */
    public void setTimeFormat(Object o)
    {
        m_oTimeFormat = o;
    }

    /** Returns the list of values in the "Time format" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getTimeFormatChoices()
    {
        return m_cdTimeFormat;
    }

    /** Returns the value of the "Time separator" field on the
    Format tab.
    @return The time separator.
    */
    public Object getTimeSeparator()
    {
        return m_oTimeSeparator;
    }

    /** Sets the value of the "Time separator" field on the
    Format tab.
    @param o The time separator.
    */
    public void setTimeSeparator(Object o)
    {
        m_oTimeSeparator = o;
    }

    /** Returns the list of values in the "Time separator" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getTimeSeparatorChoices()
    {
        return m_cdTimeSeparator;
    }

    /** Returns the value of the "Date format" field on the
    Format tab.
    @return The date format.
    */
    public Object getDateFormat()
    {
        return m_oDateFormat;
    }

    /** Sets the value of the "Date format" field on the
    Format tab.
    @param o The date format.
    */
    public void setDateFormat(Object o)
    {
        m_oDateFormat = o;
    }

    /** Returns the list of values in the "Date format" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getDateFormatChoices()
    {
        return m_cdDateFormat;
    }

    /** Returns the value of the "Date separator" field on the
    Format tab.
    @return The date separator.
    */
    public Object getDateSeparator()
    {
        return m_oDateSeparator;
    }

    /** Sets the value of the "Date separator" field on the
    Format tab.
    @param o The date separator.
    */
    public void setDateSeparator(Object o)
    {
        m_oDateSeparator = o;
    }

    /** Returns the list of values in the "Date separator" drop down
    list.
    @return The list of values.
    */
    public ChoiceDescriptor[] getDateSeparatorChoices()
    {
        return m_cdDateSeparator;
    }
    
    ////////////////////////////
    // Connection Options Tab //
    ////////////////////////////
    
    /** Returns the value of the "Default user ID" field on the
    Connection Options tab.
    @return The naming convention.
    */
    public String getDefaultUserID()
    {
        return m_sDefaultUserID;
    }

    /** Sets the value of the "Default user ID" field on the
    Connection Options tab.
    @param s The user ID.
    */
    public void setDefaultUserID(String s)
    {
        m_sDefaultUserID = s;
    }

    /** Indicates whether the "SSL"
    check box on the Translation tab is checked.
    @return True if the check box is checked
    */
    public boolean isUseSSL()
    {
        return m_bUseSSL;
    }

    /** Sets the value of the "SSL" check
    box on the Connection Options tab.
    @param b The check box state.  True if it should be checked,
    false otherwise.
    */
    public void setUseSSL(boolean b)
    {
        m_bUseSSL = b;
    }
    
    ///////////////////////////////////////////////////
    // General methods for dealing with all controls //
    ///////////////////////////////////////////////////
    
    /**
    Required by the framework.  Not used.
    */
    public Capabilities getCapabilities()
    {
        return null;
    }

    /**
    Required by the framework.  Not used.
    */
    public void verifyChanges() // Called with "prepareToCommit" on bean...
    {
            
    }

    /**
    Required by the framework.  Not used.
    */
    public void save() // Called with "commit" on bean...
    {
     
    }
   
    /* Sets default values for all fields on all tabs.  Also populates drop down lists.
    */
    public void load()
    {
        String scratchString;
        
        // This method should be called once per pane object.  It loads the pane with
        // default values as well as setting up the combo box choices.
        // General Tab
        m_sDataSourceName = "";
        m_sDescription = "";
        m_sAS400Server = "";
        
        // Server Tab
        m_sSQLlibrary = "";                                                             //@A1A
        m_sDefaultLibraries = "";
        m_oCommitMode = null;
        m_cdCommitMode = new ChoiceDescriptor[5];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_COMMIT_NONE");
        m_cdCommitMode[0] = new ChoiceDescriptor("AJDSP_COMMIT_NONE", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_COMMIT_CS");
        m_cdCommitMode[1] = new ChoiceDescriptor("AJDSP_COMMIT_CS", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_COMMIT_CHG");
        m_cdCommitMode[2] = new ChoiceDescriptor("AJDSP_COMMIT_CHG", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_COMMIT_ALL");
        m_cdCommitMode[3] = new ChoiceDescriptor("AJDSP_COMMIT_ALL", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_COMMIT_RR");
        m_cdCommitMode[4] = new ChoiceDescriptor("AJDSP_COMMIT_RR", scratchString); 
        
        m_oMaxPrecision     = null;                                                         //@A2A
        m_cdMaxPrecision    = new ChoiceDescriptor[2];                                      //@A2A
        m_cdMaxPrecision[0] = new ChoiceDescriptor("AJDSP_MAXPREC_31", "31");               //@A2A
        m_cdMaxPrecision[1] = new ChoiceDescriptor("AJDSP_MAXPREC_63", "63");               //@A2A
        
        m_oMaxScale     = null;                                                             //@A2A
        m_cdMaxScale    = new ChoiceDescriptor[3];                                          //@A2A
        m_cdMaxScale[0] = new ChoiceDescriptor("AJDSP_MAXSCALE_0", "0");                    //@A2A
        m_cdMaxScale[1] = new ChoiceDescriptor("AJDSP_MAXSCALE_31", "31");                  //@A2A
        m_cdMaxScale[2] = new ChoiceDescriptor("AJDSP_MAXSCALE_63", "63");                  //@A2A
        
        m_oMinDivideScale     = null;                                                       //@A2A
        m_cdMinDivideScale    = new ChoiceDescriptor[10];                                   //@A2A
        m_cdMinDivideScale[0] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_0", "0");           //@A2A
        m_cdMinDivideScale[1] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_1", "1");           //@A2A
        m_cdMinDivideScale[2] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_2", "2");           //@A2A
        m_cdMinDivideScale[3] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_3", "3");           //@A2A
        m_cdMinDivideScale[4] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_4", "4");           //@A2A
        m_cdMinDivideScale[5] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_5", "5");           //@A2A
        m_cdMinDivideScale[6] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_6", "6");           //@A2A
        m_cdMinDivideScale[7] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_7", "7");           //@A2A
        m_cdMinDivideScale[8] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_8", "8");           //@A2A
        m_cdMinDivideScale[9] = new ChoiceDescriptor("AJDSP_MINDIVSCALE_9", "9");           //@A2A
        
        // Package Tab
        m_bEnableExtDynamic = false;
        m_sPackage = "";
        m_sPackageLibrary = "";
        m_sUsageGroup = "";
        m_sUnusablePkgActionGroup = "";
        m_bCachePackageLocally = false;
        
        // Performance Tab
        m_bEnableLazyClose = false;
        m_bEnablePrefetch = false;
        m_bEnableDataCompression = false;
        m_oRecordBlockingCriteria = null;
        m_cdRecordBlockingCriteria = new ChoiceDescriptor[3];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_RECBLK_DISABLE");
        m_cdRecordBlockingCriteria[0] = new ChoiceDescriptor("AJDSP_RECBLK_DISABLE", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_RECBLK_FORFETCH");
        m_cdRecordBlockingCriteria[1] = new ChoiceDescriptor("AJDSP_RECBLK_FORFETCH", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_RECBLK_NOTUPDT");
        m_cdRecordBlockingCriteria[2] = new ChoiceDescriptor("AJDSP_RECBLK_NOTUPDT", scratchString); 
        m_oRecordBlockingSize = null;
        m_cdRecordBlockingSize = new ChoiceDescriptor[8];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_0");
        m_cdRecordBlockingSize[0] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_0", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_8");
        m_cdRecordBlockingSize[1] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_8", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_16");
        m_cdRecordBlockingSize[2] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_16", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_32");
        m_cdRecordBlockingSize[3] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_32", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_64");
        m_cdRecordBlockingSize[4] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_64", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_128");
        m_cdRecordBlockingSize[5] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_128", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_256");
        m_cdRecordBlockingSize[6] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_256", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_BLOCK_SIZE_512");
        m_cdRecordBlockingSize[7] = new ChoiceDescriptor("AJDSP_BLOCK_SIZE_512", scratchString); 
        m_oLOBThreshold = null;
        m_cdLOBThreshold = new ChoiceDescriptor[11];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_0");
        m_cdLOBThreshold[0] = new ChoiceDescriptor("AJDSP_LOB_THRESH_0", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_32");
        m_cdLOBThreshold[1] = new ChoiceDescriptor("AJDSP_LOB_THRESH_32", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_64");
        m_cdLOBThreshold[2] = new ChoiceDescriptor("AJDSP_LOB_THRESH_64", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_128");
        m_cdLOBThreshold[3] = new ChoiceDescriptor("AJDSP_LOB_THRESH_128", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_256");
        m_cdLOBThreshold[4] = new ChoiceDescriptor("AJDSP_LOB_THRESH_256", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_512");
        m_cdLOBThreshold[5] = new ChoiceDescriptor("AJDSP_LOB_THRESH_512", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_1024");
        m_cdLOBThreshold[6] = new ChoiceDescriptor("AJDSP_LOB_THRESH_1024", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_2048");
        m_cdLOBThreshold[7] = new ChoiceDescriptor("AJDSP_LOB_THRESH_2048", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_4096");
        m_cdLOBThreshold[8] = new ChoiceDescriptor("AJDSP_LOB_THRESH_4096", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_8192");
        m_cdLOBThreshold[9] = new ChoiceDescriptor("AJDSP_LOB_THRESH_8192", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_LOB_THRESH_15360");
        m_cdLOBThreshold[10] = new ChoiceDescriptor("AJDSP_LOB_THRESH_15360", scratchString);
        
        // Language Tab
        m_oSortType = null;
        m_cdSortType = new ChoiceDescriptor[4];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTHEX");
        m_cdSortType[0] = new ChoiceDescriptor("AJDSP_SORTHEX", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTJOB");
        m_cdSortType[1] = new ChoiceDescriptor("AJDSP_SORTJOB", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLANGID");
        m_cdSortType[2] = new ChoiceDescriptor("AJDSP_SORTLANGID", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTTABLE");
        m_cdSortType[3] = new ChoiceDescriptor("AJDSP_SORTTABLE", scratchString); 
        m_sSortTable = "";
        m_sSortWeightGroup = "";
        m_oLanguage = null;
        m_cdLanguage = new ChoiceDescriptor[59];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_AFR_TEXT");
        m_cdLanguage[0] = new ChoiceDescriptor("AJDSP_SORTLG_AFR_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ARA_TEXT");
        m_cdLanguage[1] = new ChoiceDescriptor("AJDSP_SORTLG_ARA_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_BEL_TEXT");
        m_cdLanguage[2] = new ChoiceDescriptor("AJDSP_SORTLG_BEL_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_BGR_TEXT");
        m_cdLanguage[3] = new ChoiceDescriptor("AJDSP_SORTLG_BGR_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_CAT_TEXT");
        m_cdLanguage[4] = new ChoiceDescriptor("AJDSP_SORTLG_CAT_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_CHS_TEXT");
        m_cdLanguage[5] = new ChoiceDescriptor("AJDSP_SORTLG_CHS_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_CHT_TEXT");
        m_cdLanguage[6] = new ChoiceDescriptor("AJDSP_SORTLG_CHT_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_CSY_TEXT");
        m_cdLanguage[7] = new ChoiceDescriptor("AJDSP_SORTLG_CSY_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_DAN_TEXT");
        m_cdLanguage[8] = new ChoiceDescriptor("AJDSP_SORTLG_DAN_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_DES_TEXT");
        m_cdLanguage[9] = new ChoiceDescriptor("AJDSP_SORTLG_DES_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_DEU_TEXT");
        m_cdLanguage[10] = new ChoiceDescriptor("AJDSP_SORTLG_DEU_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ELL_TEXT");
        m_cdLanguage[11] = new ChoiceDescriptor("AJDSP_SORTLG_ELL_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ENA_TEXT");
        m_cdLanguage[12] = new ChoiceDescriptor("AJDSP_SORTLG_ENA_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ENB_TEXT");
        m_cdLanguage[13] = new ChoiceDescriptor("AJDSP_SORTLG_ENB_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ENG_TEXT");
        m_cdLanguage[14] = new ChoiceDescriptor("AJDSP_SORTLG_ENG_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ENP_TEXT");
        m_cdLanguage[15] = new ChoiceDescriptor("AJDSP_SORTLG_ENP_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ENU_TEXT");
        m_cdLanguage[16] = new ChoiceDescriptor("AJDSP_SORTLG_ENU_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ESP_TEXT");
        m_cdLanguage[17] = new ChoiceDescriptor("AJDSP_SORTLG_ESP_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_EST_TEXT");
        m_cdLanguage[18] = new ChoiceDescriptor("AJDSP_SORTLG_EST_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_FAR_TEXT");
        m_cdLanguage[19] = new ChoiceDescriptor("AJDSP_SORTLG_FAR_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_FIN_TEXT");
        m_cdLanguage[20] = new ChoiceDescriptor("AJDSP_SORTLG_FIN_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_FRA_TEXT");
        m_cdLanguage[21] = new ChoiceDescriptor("AJDSP_SORTLG_FRA_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_FRB_TEXT");
        m_cdLanguage[22] = new ChoiceDescriptor("AJDSP_SORTLG_FRB_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_FRC_TEXT");
        m_cdLanguage[23] = new ChoiceDescriptor("AJDSP_SORTLG_FRC_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_FRS_TEXT");
        m_cdLanguage[24] = new ChoiceDescriptor("AJDSP_SORTLG_FRS_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_GAE_TEXT");
        m_cdLanguage[25] = new ChoiceDescriptor("AJDSP_SORTLG_GAE_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_HEB_TEXT");
        m_cdLanguage[26] = new ChoiceDescriptor("AJDSP_SORTLG_HEB_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_HRV_TEXT");
        m_cdLanguage[27] = new ChoiceDescriptor("AJDSP_SORTLG_HRV_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_HUN_TEXT");
        m_cdLanguage[28] = new ChoiceDescriptor("AJDSP_SORTLG_HUN_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ISL_TEXT");
        m_cdLanguage[29] = new ChoiceDescriptor("AJDSP_SORTLG_ISL_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ITA_TEXT");
        m_cdLanguage[30] = new ChoiceDescriptor("AJDSP_SORTLG_ITA_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ITS_TEXT");
        m_cdLanguage[31] = new ChoiceDescriptor("AJDSP_SORTLG_ITS_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_JPN_TEXT");
        m_cdLanguage[32] = new ChoiceDescriptor("AJDSP_SORTLG_JPN_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_KOR_TEXT");
        m_cdLanguage[33] = new ChoiceDescriptor("AJDSP_SORTLG_KOR_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_LAO_TEXT");
        m_cdLanguage[34] = new ChoiceDescriptor("AJDSP_SORTLG_LAO_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_LTU_TEXT");
        m_cdLanguage[35] = new ChoiceDescriptor("AJDSP_SORTLG_LTU_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_LVA_TEXT");
        m_cdLanguage[36] = new ChoiceDescriptor("AJDSP_SORTLG_LVA_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_MKD_TEXT");
        m_cdLanguage[37] = new ChoiceDescriptor("AJDSP_SORTLG_MKD_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_NLB_TEXT");
        m_cdLanguage[38] = new ChoiceDescriptor("AJDSP_SORTLG_NLB_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_NLD_TEXT");
        m_cdLanguage[39] = new ChoiceDescriptor("AJDSP_SORTLG_NLD_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_NON_TEXT");
        m_cdLanguage[40] = new ChoiceDescriptor("AJDSP_SORTLG_NON_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_NOR_TEXT");
        m_cdLanguage[41] = new ChoiceDescriptor("AJDSP_SORTLG_NOR_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_PLK_TEXT");
        m_cdLanguage[42] = new ChoiceDescriptor("AJDSP_SORTLG_PLK_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_PTB_TEXT");
        m_cdLanguage[43] = new ChoiceDescriptor("AJDSP_SORTLG_PTB_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_PTG_TEXT");
        m_cdLanguage[44] = new ChoiceDescriptor("AJDSP_SORTLG_PTG_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_RMS_TEXT");
        m_cdLanguage[45] = new ChoiceDescriptor("AJDSP_SORTLG_RMS_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_ROM_TEXT");
        m_cdLanguage[46] = new ChoiceDescriptor("AJDSP_SORTLG_ROM_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_RUS_TEXT");
        m_cdLanguage[47] = new ChoiceDescriptor("AJDSP_SORTLG_RUS_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_SKY_TEXT");
        m_cdLanguage[48] = new ChoiceDescriptor("AJDSP_SORTLG_SKY_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_SLO_TEXT");
        m_cdLanguage[49] = new ChoiceDescriptor("AJDSP_SORTLG_SLO_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_SQI_TEXT");
        m_cdLanguage[50] = new ChoiceDescriptor("AJDSP_SORTLG_SQI_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_SRB_TEXT");
        m_cdLanguage[51] = new ChoiceDescriptor("AJDSP_SORTLG_SRB_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_SRL_TEXT");
        m_cdLanguage[52] = new ChoiceDescriptor("AJDSP_SORTLG_SRL_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_SVE_TEXT");
        m_cdLanguage[53] = new ChoiceDescriptor("AJDSP_SORTLG_SVE_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_THA_TEXT");
        m_cdLanguage[54] = new ChoiceDescriptor("AJDSP_SORTLG_THA_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_TRK_TEXT");
        m_cdLanguage[55] = new ChoiceDescriptor("AJDSP_SORTLG_TRK_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_UKR_TEXT");
        m_cdLanguage[56] = new ChoiceDescriptor("AJDSP_SORTLG_UKR_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_URD_TEXT");
        m_cdLanguage[57] = new ChoiceDescriptor("AJDSP_SORTLG_URD_TEXT", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_SORTLG_VIE_TEXT");
        m_cdLanguage[58] = new ChoiceDescriptor("AJDSP_SORTLG_VIE_TEXT", scratchString); 
        
        // Other Tab
        m_sAccessTypeGroup = "";
        m_sRemarksSourceGroup = "";
        
        // Translation Tab
        m_bTranslate65535 = false;
        m_bTranslateHex   = false;                                                      //@A2A
        
        // Format Tab
        m_oNamingConvention = null;
        m_cdNamingConvention = new ChoiceDescriptor[2];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_NAMING_SQL");
        m_cdNamingConvention[0] = new ChoiceDescriptor("AJDSP_NAMING_SQL", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_NAMING_SYSTEM");
        m_cdNamingConvention[1] = new ChoiceDescriptor("AJDSP_NAMING_SYSTEM", scratchString); 
        m_oDecimalSeparator = null;
        m_cdDecimalSeparator = new ChoiceDescriptor[3];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_USE_SERVER_JOB");
        m_cdDecimalSeparator[0] = new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DECIMAL_PERIOD");
        m_cdDecimalSeparator[1] = new ChoiceDescriptor("AJDSP_DECIMAL_PERIOD", scratchString); 
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DECIMAL_COMMA");
        m_cdDecimalSeparator[2] = new ChoiceDescriptor("AJDSP_DECIMAL_COMMA", scratchString);
        m_oTimeFormat = null;
        m_cdTimeFormat = new ChoiceDescriptor[6];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_USE_SERVER_JOB");
        m_cdTimeFormat[0] = new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMEFMT_HMS");
        m_cdTimeFormat[1] = new ChoiceDescriptor("AJDSP_TIMEFMT_HMS", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMEFMT_USA");
        m_cdTimeFormat[2] = new ChoiceDescriptor("AJDSP_TIMEFMT_USA", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMEFMT_ISO");
        m_cdTimeFormat[3] = new ChoiceDescriptor("AJDSP_TIMEFMT_ISO", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMEFMT_EUR");
        m_cdTimeFormat[4] = new ChoiceDescriptor("AJDSP_TIMEFMT_EUR", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMEFMT_JIS");
        m_cdTimeFormat[5] = new ChoiceDescriptor("AJDSP_TIMEFMT_JIS", scratchString);
        m_oTimeSeparator = null;
        m_cdTimeSeparator = new ChoiceDescriptor[5];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_USE_SERVER_JOB");
        m_cdTimeSeparator[0] = new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMESEP_COLON");
        m_cdTimeSeparator[1] = new ChoiceDescriptor("AJDSP_TIMESEP_COLON", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMESEP_PERIOD");
        m_cdTimeSeparator[2] = new ChoiceDescriptor("AJDSP_TIMESEP_PERIOD", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMESEP_COMMA");
        m_cdTimeSeparator[3] = new ChoiceDescriptor("AJDSP_TIMESEP_COMMA", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_TIMESEP_BLANK");
        m_cdTimeSeparator[4] = new ChoiceDescriptor("AJDSP_TIMESEP_BLANK", scratchString);
        m_oDateFormat = null;
        m_cdDateFormat = new ChoiceDescriptor[9];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_USE_SERVER_JOB");
        m_cdDateFormat[0] = new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_JULIAN");
        m_cdDateFormat[1] = new ChoiceDescriptor("AJDSP_DATEFMT_JULIAN", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_MDY");
        m_cdDateFormat[2] = new ChoiceDescriptor("AJDSP_DATEFMT_MDY", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_DMY");
        m_cdDateFormat[3] = new ChoiceDescriptor("AJDSP_DATEFMT_DMY", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_YMD");
        m_cdDateFormat[4] = new ChoiceDescriptor("AJDSP_DATEFMT_YMD", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_USA");
        m_cdDateFormat[5] = new ChoiceDescriptor("AJDSP_DATEFMT_USA", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_ISO");
        m_cdDateFormat[6] = new ChoiceDescriptor("AJDSP_DATEFMT_ISO", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_EUR");
        m_cdDateFormat[7] = new ChoiceDescriptor("AJDSP_DATEFMT_EUR", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATEFMT_JIS");
        m_cdDateFormat[8] = new ChoiceDescriptor("AJDSP_DATEFMT_JIS", scratchString);
        m_oDateSeparator = null;
        m_cdDateSeparator = new ChoiceDescriptor[6];
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_USE_SERVER_JOB");
        m_cdDateSeparator[0] = new ChoiceDescriptor("AJDSP_USE_SERVER_JOB", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATESEP_FORWARDSLASH");
        m_cdDateSeparator[1] = new ChoiceDescriptor("AJDSP_DATESEP_FORWARDSLASH", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATESEP_DASH");
        m_cdDateSeparator[2] = new ChoiceDescriptor("AJDSP_DATESEP_DASH", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATESEP_PERIOD");
        m_cdDateSeparator[3] = new ChoiceDescriptor("AJDSP_DATESEP_PERIOD", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATESEP_COMMA");
        m_cdDateSeparator[4] = new ChoiceDescriptor("AJDSP_DATESEP_COMMA", scratchString);
        scratchString = AS400JDBCDataSourcePane.resource_Loader.getString("AJDSP_DATESEP_BLANK");
        m_cdDateSeparator[5] = new ChoiceDescriptor("AJDSP_DATESEP_BLANK", scratchString);
        
        // Connection Options Tab
        m_sDefaultUserID = "";
        m_bUseSSL = false;
    }
}
