///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabaseServerAttributes.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

public class DatabaseServerAttributes implements
  AttributeTranslateIndicator
{

  public static final int CC_NONE=0;
  public static final int CC_CS = 1;
  public static final int CC_CHG = 2; 
  public static final int CC_ALL = 3; 
  public static final int CC_RR = 4; 
  public static final int AUTOCOMMIT_OFF = 0xd5;
  public static final int AUTOCOMMIT_ON = 0xe8;


  private int defaultClientCCSID_;
  private boolean defaultClientCCSIDSet_;
  private String languageFeatureCode_;
  private String clientFunctionalLevel_;
  private int nlssIdentifier_;
  private boolean nlssIdentifierSet_;
  private String nlssIdentifierLanguageID_;
  private String nlssIdentifierLanguageTableName_;
  private String nlssIdentifierLanguageTableLibrary_;
  private int translateIndicator_;
  private boolean translateIndicatorSet_;
  private int drdaPackageSize_;
  private boolean drdaPackageSizeSet_;
  private int dateFormatParserOption_;
  private boolean dateFormatParserOptionSet_;
  private int dateSeparatorParserOption_;
  private boolean dateSeparatorParserOptionSet_;
  private int timeFormatParserOption_;
  private boolean timeFormatParserOptionSet_;
  private int timeSeparatorParserOption_;
  private boolean timeSeparatorParserOptionSet_;
  private int decimalSeparatorParserOption_;
  private boolean decimalSeparatorParserOptionSet_;
  private int namingConventionParserOption_;
  private boolean namingConventionParserOptionSet_;
  private int ignoreDecimalDataErrorParserOption_;
  private boolean ignoreDecimalDataErrorParserOptionSet_;
  private int commitmentControlLevelParserOption_;
  private boolean commitmentControlLevelParserOptionSet_;
  private String defaultSQLLibraryName_;
  private int asciiCCSIDForTranslationTable_;
  private boolean asciiCCSIDForTranslationTableSet_;
  private int ambiguousSelectOption_;
  private boolean ambiguousSelectOptionSet_;
  private int packageAddStatementAllowed_;
  private boolean packageAddStatementAllowedSet_;
  // Skipped the "Data Source Name" related parameters, JTOpen doesn't do these either.
  private int useExtendedFormats_;
  private boolean useExtendedFormatsSet_;
  private int lobFieldThreshold_;
  private boolean lobFieldThresholdSet_;
  private int dataCompressionParameter_;
  private boolean dataCompressionParameterSet_;
  private int trueAutoCommitIndicator_;
  private boolean trueAutoCommitIndicatorSet_;
  private int clientSupportInformation_;
  private boolean clientSupportInformationSet_;
  private String rdbName_;
  private int maximumDecimalPrecision_;
  private int maximumDecimalScale_;
  private int minimumDivideScale_;
  private boolean decimalPrecisionAndScaleAttributesSet_;
  private int hexadecimalConstantParserOption_;
  private boolean hexadecimalConstantParserOptionSet_;
  private int inputLocatorType_;
  private boolean inputLocatorTypeSet_;
  private int locatorPersistence_;
  private boolean locatorPersistenceSet_;
  private byte[] ewlmCorrelator_;
  private byte[] rleCompression_;
  private int optimizationGoalIndicator_;
  private boolean optimizationGoalIndicatorSet_;
  private int queryStorageLimit_;
  private boolean queryStorageLimitSet_;
  private int decimalFloatingPointRoundingModeOption_;
  private boolean decimalFloatingPointRoundingModeOptionSet_;
  private int decimalFloatingPointErrorReportingOption_;
  private boolean decimalFloatingPointErrorReportingOptionSet_;
  private String clientAccountingInformation_;
  private String clientApplicationName_;
  private String clientUserIdentifier_;
  private String clientWorkstationName_;
  private String clientProgramIdentifier_;
  private String interfaceType_;
  private String interfaceName_;
  private String interfaceLevel_;
  private int closeOnEOF_;
  private boolean closeOnEOFSet_;

  // These are set based on attributes in the reply from the server, they cannot be set by a client.
  private int serverCCSID_;
  private boolean serverCCSIDSet_;
  private String serverFunctionalLevel_;
  private String serverJobName_;
  private String serverJobUser_;
  private String serverJobNumber_;
  private boolean serverJobSet_;


  public DatabaseServerAttributes()
  {
  }

  public void clear()
  {
    defaultClientCCSID_ = 0;
    defaultClientCCSIDSet_ = false;
    languageFeatureCode_ = null;
    clientFunctionalLevel_ = null;
    nlssIdentifier_ = 0;
    nlssIdentifierSet_ = false;
    nlssIdentifierLanguageID_ = null;
    nlssIdentifierLanguageTableName_ = null;
    nlssIdentifierLanguageTableLibrary_ = null;
    translateIndicator_ = 0;
    translateIndicatorSet_ = false;
    drdaPackageSize_ = 0;
    drdaPackageSizeSet_ = false;
    dateFormatParserOption_ = 0;
    dateFormatParserOptionSet_ = false;
    dateSeparatorParserOption_ = 0;
    dateSeparatorParserOptionSet_ = false;
    timeFormatParserOption_ = 0;
    timeFormatParserOptionSet_ = false;
    timeSeparatorParserOption_ = 0;
    timeSeparatorParserOptionSet_ = false;
    decimalSeparatorParserOption_ = 0;
    decimalSeparatorParserOptionSet_ = false;
    namingConventionParserOption_ = 0;
    namingConventionParserOptionSet_ = false;
    ignoreDecimalDataErrorParserOption_ = 0;
    ignoreDecimalDataErrorParserOptionSet_ = false;
    commitmentControlLevelParserOption_ = 0;
    commitmentControlLevelParserOptionSet_ = false;
    defaultSQLLibraryName_ = null;
    asciiCCSIDForTranslationTable_ = 0;
    asciiCCSIDForTranslationTableSet_ = false;
    ambiguousSelectOption_ = 0;
    ambiguousSelectOptionSet_ = false;
    packageAddStatementAllowed_ = 0;
    packageAddStatementAllowedSet_ = false;
    useExtendedFormats_ = 0;
    useExtendedFormatsSet_ = false;
    lobFieldThreshold_ = 0;
    lobFieldThresholdSet_ = false;
    dataCompressionParameter_ = 0;
    dataCompressionParameterSet_ = false;
    trueAutoCommitIndicator_ = 0;
    trueAutoCommitIndicatorSet_ = false;
    clientSupportInformation_ = 0;
    clientSupportInformationSet_ = false;
    rdbName_ = null;
    maximumDecimalPrecision_ = 0;
    maximumDecimalScale_ = 0;
    minimumDivideScale_ = 0;
    decimalPrecisionAndScaleAttributesSet_ = false;
    hexadecimalConstantParserOption_ = 0;
    hexadecimalConstantParserOptionSet_ = false;
    inputLocatorType_ = 0;
    inputLocatorTypeSet_ = false;
    locatorPersistence_ = 0;
    locatorPersistenceSet_ = false;
    ewlmCorrelator_ = null;
    rleCompression_ = null;
    optimizationGoalIndicator_ = 0;
    optimizationGoalIndicatorSet_ = false;
    queryStorageLimit_ = 0;
    queryStorageLimitSet_ = false;
    decimalFloatingPointRoundingModeOption_ = 0;
    decimalFloatingPointRoundingModeOptionSet_ = false;
    decimalFloatingPointErrorReportingOption_ = 0;
    decimalFloatingPointErrorReportingOptionSet_ = false;
    clientAccountingInformation_ = null;
    clientApplicationName_ = null;
    clientUserIdentifier_ = null;
    clientWorkstationName_ = null;
    clientProgramIdentifier_ = null;
    interfaceType_ = null;
    interfaceName_ = null;
    interfaceLevel_ = null;
    closeOnEOF_ = 0;
    closeOnEOFSet_ = false;


    serverCCSID_ = 0;
    serverCCSIDSet_ = false;
    serverFunctionalLevel_ = null;
    serverJobName_ = null;
    serverJobUser_ = null;
    serverJobNumber_ = null;
    serverJobSet_ = false;
  }

  public int getDefaultClientCCSID()
  {
    return defaultClientCCSID_;
  }

  public boolean isDefaultClientCCSIDSet()
  {
    return defaultClientCCSIDSet_;
  }

  public void setDefaultClientCCSID(int value)
  {
    defaultClientCCSID_ = value;
    defaultClientCCSIDSet_ = true;
  }

  public String getLanguageFeatureCode()
  {
    return languageFeatureCode_;
  }

  public boolean isLanguageFeatureCodeSet()
  {
    return languageFeatureCode_ != null;
  }

  public void setLanguageFeatureCode(String value)
  {
    languageFeatureCode_ = value;
  }

  public String getClientFunctionalLevel()
  {
    return clientFunctionalLevel_;
  }

  public boolean isClientFunctionalLevelSet()
  {
    return clientFunctionalLevel_ != null;
  }

  public void setClientFunctionalLevel(String value)
  {
    clientFunctionalLevel_ = value;
  }

  public int getNLSSIdentifier()
  {
    return nlssIdentifier_;
  }

  public boolean isNLSSIdentifierSet()
  {
    return nlssIdentifierSet_;
  }

  public void setNLSSIdentifier(int value)
  {
    nlssIdentifier_ = value;
    nlssIdentifierSet_ = true;
  }

  public String getNLSSIdentifierLanguageID()
  {
    return nlssIdentifierLanguageID_;
  }

  public void setNLSSIdentifierLanguageID(String value)
  {
    nlssIdentifierLanguageID_ = value;
  }

  public String getNLSSIdentifierLanguageTableName()
  {
    return nlssIdentifierLanguageTableName_;
  }

  public void setNLSSIdentifierLanguageTableName(String value)
  {
    nlssIdentifierLanguageTableName_ = value;
  }

  public String getNLSSIdentifierLanguageTableLibrary()
  {
    return nlssIdentifierLanguageTableLibrary_;
  }

  public void setNLSSIdentifierLanguageTableLibrary(String value)
  {
    nlssIdentifierLanguageTableLibrary_ = value;
  }

  public int getTranslateIndicator()
  {
    return translateIndicator_;
  }

  public boolean isTranslateIndicatorSet()
  {
    return translateIndicatorSet_;
  }

  public void setTranslateIndicator(int value)
  {
    translateIndicator_ = value;
    translateIndicatorSet_ = true;
  }

  public int getDRDAPackageSize()
  {
    return drdaPackageSize_;
  }

  public boolean isDRDAPackageSizeSet()
  {
    return drdaPackageSizeSet_;
  }

  public void setDRDAPackageSize(int value)
  {
    drdaPackageSize_ = value;
    drdaPackageSizeSet_ = true;
  }

  public int getDateFormatParserOption()
  {
    return dateFormatParserOption_;
  }

  public boolean isDateFormatParserOptionSet()
  {
    return dateFormatParserOptionSet_;
  }

  public void setDateFormatParserOption(int value)
  {
    dateFormatParserOption_ = value;
    dateFormatParserOptionSet_ = true;
  }

  public int getDateSeparatorParserOption()
  {
    return dateSeparatorParserOption_;
  }

  public boolean isDateSeparatorParserOptionSet()
  {
    return dateSeparatorParserOptionSet_;
  }

  public void setDateSeparatorParserOption(int value)
  {
    dateSeparatorParserOption_ = value;
    dateSeparatorParserOptionSet_ = true;
  }

  public int getTimeFormatParserOption()
  {
    return timeFormatParserOption_;
  }

  public boolean isTimeFormatParserOptionSet()
  {
    return timeFormatParserOptionSet_;
  }

  public void setTimeFormatParserOption(int value)
  {
    timeFormatParserOption_ = value;
    timeFormatParserOptionSet_ = true;
  }

  public int getTimeSeparatorParserOption()
  {
    return timeSeparatorParserOption_;
  }

  public boolean isTimeSeparatorParserOptionSet()
  {
    return timeSeparatorParserOptionSet_;
  }

  public void setTimeSeparatorParserOption(int value)
  {
    timeSeparatorParserOption_ = value;
    timeSeparatorParserOptionSet_ = true;
  }

  public int getDecimalSeparatorParserOption()
  {
    return decimalSeparatorParserOption_;
  }

  public boolean isDecimalSeparatorParserOptionSet()
  {
    return decimalSeparatorParserOptionSet_;
  }

  public void setDecimalSeparatorParserOption(int value)
  {
    decimalSeparatorParserOption_ = value;
    decimalSeparatorParserOptionSet_ = true;
  }

  public int getNamingConventionParserOption()
  {
    return namingConventionParserOption_;
  }

  public boolean isNamingConventionParserOptionSet()
  {
    return namingConventionParserOptionSet_;
  }

  public void setNamingConventionParserOption(int value)
  {
    namingConventionParserOption_ = value;
    namingConventionParserOptionSet_ = true;
  }

  public int getIgnoreDecimalDataErrorParserOption()
  {
    return ignoreDecimalDataErrorParserOption_;
  }

  public boolean isIgnoreDecimalDataErrorParserOptionSet()
  {
    return ignoreDecimalDataErrorParserOptionSet_;
  }

  public void setIgnoreDecimalDataErrorParserOption(int value)
  {
    ignoreDecimalDataErrorParserOption_ = value;
    ignoreDecimalDataErrorParserOptionSet_ = true;
  }

  public int getCommitmentControlLevelParserOption()
  {
    return commitmentControlLevelParserOption_;
  }

  public boolean isCommitmentControlLevelParserOptionSet()
  {
    return commitmentControlLevelParserOptionSet_;
  }

  public void setCommitmentControlLevelParserOption(int value)
  {
    commitmentControlLevelParserOption_ = value;
    commitmentControlLevelParserOptionSet_ = true;
  }

  public String getDefaultSQLLibraryName()
  {
    return defaultSQLLibraryName_;
  }

  public boolean isDefaultSQLLibraryNameSet()
  {
    return defaultSQLLibraryName_ != null;
  }

  public void setDefaultSQLLibraryName(String value)
  {
    defaultSQLLibraryName_ = value;
  }

  public int getASCIICCSIDForTranslationTable()
  {
    return asciiCCSIDForTranslationTable_;
  }

  public boolean isASCIICCSIDForTranslationTableSet()
  {
    return asciiCCSIDForTranslationTableSet_;
  }

  public void setASCIICCSIDForTranslationTable(int value)
  {
    asciiCCSIDForTranslationTable_ = value;
    asciiCCSIDForTranslationTableSet_ = true;
  }

  public int getAmbiguousSelectOption()
  {
    return ambiguousSelectOption_;
  }

  public boolean isAmbiguousSelectOptionSet()
  {
    return ambiguousSelectOptionSet_;
  }

  public void setAmbiguousSelectOption(int value)
  {
    ambiguousSelectOption_ = value;
    ambiguousSelectOptionSet_ = true;
  }

  public int getPackageAddStatementAllowed()
  {
    return packageAddStatementAllowed_;
  }

  public boolean isPackageAddStatementAllowedSet()
  {
    return packageAddStatementAllowedSet_;
  }

  public void setPackageAddStatementAllowed(int value)
  {
    packageAddStatementAllowed_ = value;
    packageAddStatementAllowedSet_ = true;
  }

  public int getUseExtendedFormats()
  {
    return useExtendedFormats_;
  }

  public boolean isUseExtendedFormatsSet()
  {
    return useExtendedFormatsSet_;
  }

  public void setUseExtendedFormats(int value)
  {
    useExtendedFormats_ = value;
    useExtendedFormatsSet_ = true;
  }

  public int getLOBFieldThreshold()
  {
    return lobFieldThreshold_;
  }

  public boolean isLOBFieldThresholdSet()
  {
    return lobFieldThresholdSet_;
  }

  public void setLOBFieldThreshold(int value)
  {
    lobFieldThreshold_ = value;
    lobFieldThresholdSet_ = true;
  }

  public int getDataCompressionParameter()
  {
    return dataCompressionParameter_;
  }

  public boolean isDataCompressionParameterSet()
  {
    return dataCompressionParameterSet_;
  }

  public void setDataCompressionParameter(int value)
  {
    dataCompressionParameter_ = value;
    dataCompressionParameterSet_ = true;
  }

  public int getTrueAutoCommitIndicator()
  {
    return trueAutoCommitIndicator_;
  }

  public boolean isTrueAutoCommitIndicatorSet()
  {
    return trueAutoCommitIndicatorSet_;
  }

  public void setTrueAutoCommitIndicator(int value)
  {
    trueAutoCommitIndicator_ = value;
    trueAutoCommitIndicatorSet_ = true;
  }

  public int getClientSupportInformation()
  {
    return clientSupportInformation_;
  }

  public boolean isClientSupportInformationSet()
  {
    return clientSupportInformationSet_;
  }

  public void setClientSupportInformation(int value)
  {
    clientSupportInformation_ = value;
    clientSupportInformationSet_ = true;
  }

  public String getRDBName()
  {
    return rdbName_;
  }

  public boolean isRDBNameSet()
  {
    return rdbName_ != null;
  }

  public void setRDBName(String value)
  {
    rdbName_ = value;
  }

  public int getMaximumDecimalPrecision()
  {
    return maximumDecimalPrecision_;
  }

  public int getMaximumDecimalScale()
  {
    return maximumDecimalScale_;
  }

  public int getMinimumDivideScale()
  {
    return minimumDivideScale_;
  }

  public boolean isDecimalPrecisionAndScaleAttributesSet()
  {
    return decimalPrecisionAndScaleAttributesSet_;
  }

  public void setDecimalPrecisionAndScaleAttributes(int maximumDecimalPrecision, int maximumDecimalScale, int minimumDivideScale)
  {
    maximumDecimalPrecision_ = maximumDecimalPrecision;
    maximumDecimalScale_ = maximumDecimalScale;
    minimumDivideScale_ = minimumDivideScale;
    decimalPrecisionAndScaleAttributesSet_ = true;
  }

  public int getHexadecimalConstantParserOption()
  {
    return hexadecimalConstantParserOption_;
  }

  public boolean isHexadecimalConstantParserOptionSet()
  {
    return hexadecimalConstantParserOptionSet_;
  }

  public void setHexadecimalConstantParserOption(int value)
  {
    hexadecimalConstantParserOption_ = value;
    hexadecimalConstantParserOptionSet_ = true;
  }

  public int getInputLocatorType()
  {
    return inputLocatorType_;
  }

  public boolean isInputLocatorTypeSet()
  {
    return inputLocatorTypeSet_;
  }

  public void setInputLocatorType(int value)
  {
    inputLocatorType_ = value;
    inputLocatorTypeSet_ = true;
  }

  public int getLocatorPersistence()
  {
    return locatorPersistence_;
  }

  public boolean isLocatorPersistenceSet()
  {
    return locatorPersistenceSet_;
  }

  public void setLocatorPersistence(int value)
  {
    locatorPersistence_ = value;
    locatorPersistenceSet_ = true;
  }

  public byte[] getEWLMCorrelator()
  {
    return ewlmCorrelator_;
  }

  public boolean isEWLMCorrelatorSet()
  {
    return ewlmCorrelator_ != null;
  }

  public void setEWLMCorrelator(byte[] value)
  {
    ewlmCorrelator_ = value;
  }

  public byte[] getRLECompression()
  {
    return rleCompression_;
  }

  public boolean isRLECompressionSet()
  {
    return rleCompression_ != null;
  }

  public void setRLECompression(byte[] value)
  {
    rleCompression_ = value;
  }

  public int getOptimizationGoalIndicator()
  {
    return optimizationGoalIndicator_;
  }

  public boolean isOptimizationGoalIndicatorSet()
  {
    return optimizationGoalIndicatorSet_;
  }

  public void setOptimizationGoalIndicator(int value)
  {
    optimizationGoalIndicator_ = value;
    optimizationGoalIndicatorSet_ = true;
  }

  public int getQueryStorageLimit()
  {
    return queryStorageLimit_;
  }

  public boolean isQueryStorageLimitSet()
  {
    return queryStorageLimitSet_;
  }

  public void setQueryStorageLimit(int value)
  {
    queryStorageLimit_ = value;
    queryStorageLimitSet_ = true;
  }

  public int getDecimalFloatingPointRoundingModeOption()
  {
    return decimalFloatingPointRoundingModeOption_;
  }

  public boolean isDecimalFloatingPointRoundingModeOptionSet()
  {
    return decimalFloatingPointRoundingModeOptionSet_;
  }

  public void setDecimalFloatingPointRoundingModeOption(int value)
  {
    decimalFloatingPointRoundingModeOption_ = value;
    decimalFloatingPointRoundingModeOptionSet_ = true;
  }

  public int getDecimalFloatingPointErrorReportingOption()
  {
    return decimalFloatingPointErrorReportingOption_;
  }

  public boolean isDecimalFloatingPointErrorReportingOptionSet()
  {
    return decimalFloatingPointErrorReportingOptionSet_;
  }

  public void setDecimalFloatingPointErrorReportingOption(int value)
  {
    decimalFloatingPointErrorReportingOption_ = value;
    decimalFloatingPointErrorReportingOptionSet_ = true;
  }

  public String getClientAccountingInformation()
  {
    return clientAccountingInformation_;
  }

  public boolean isClientAccountingInformationSet()
  {
    return clientAccountingInformation_ != null;
  }

  public void setClientAccountingInformation(String value)
  {
    clientAccountingInformation_ = value;
  }

  public String getClientApplicationName()
  {
    return clientApplicationName_;
  }

  public boolean isClientApplicationNameSet()
  {
    return clientApplicationName_ != null;
  }

  public void setClientApplicationName(String value)
  {
    clientApplicationName_ = value;
  }

  public String getClientUserIdentifier()
  {
    return clientUserIdentifier_;
  }

  public boolean isClientUserIdentifierSet()
  {
    return clientUserIdentifier_ != null;
  }

  public void setClientUserIdentifier(String value)
  {
    clientUserIdentifier_ = value;
  }

  public String getClientWorkstationName()
  {
    return clientWorkstationName_;
  }

  public boolean isClientWorkstationNameSet()
  {
    return clientWorkstationName_ != null;
  }

  public void setClientWorkstationName(String value)
  {
    clientWorkstationName_ = value;
  }

  public String getClientProgramIdentifier()
  {
    return clientProgramIdentifier_;
  }

  public boolean isClientProgramIdentifierSet()
  {
    return clientProgramIdentifier_ != null;
  }

  public void setClientProgramIdentifier(String value)
  {
    clientProgramIdentifier_ = value;
  }

  public String getInterfaceType()
  {
    return interfaceType_;
  }

  public boolean isInterfaceTypeSet()
  {
    return interfaceType_ != null;
  }

  public void setInterfaceType(String value)
  {
    interfaceType_ = value;
  }

  public String getInterfaceName()
  {
    return interfaceName_;
  }

  public boolean isInterfaceNameSet()
  {
    return interfaceName_ != null;
  }

  public void setInterfaceName(String value)
  {
    interfaceName_ = value;
  }

  public String getInterfaceLevel()
  {
    return interfaceLevel_;
  }

  public boolean isInterfaceLevelSet()
  {
    return interfaceLevel_ != null;
  }

  public void setInterfaceLevel(String value)
  {
    interfaceLevel_ = value;
  }

  public int getCloseOnEOF()
  {
    return closeOnEOF_;
  }

  public boolean isCloseOnEOFSet()
  {
    return closeOnEOFSet_;
  }

  public void setCloseOnEOF(int value)
  {
    closeOnEOF_ = value;
    closeOnEOFSet_ = true;
  }

  public int getServerCCSID()
  {
    return serverCCSID_;
  }

  public boolean isServerCCSIDSet()
  {
    return serverCCSIDSet_;
  }

  void setServerCCSID(int value)
  {
    serverCCSID_ = value;
    serverCCSIDSet_ = true;
  }

  public String getServerFunctionalLevel()
  {
    return serverFunctionalLevel_;
  }

  public boolean isServerFunctionalLevelSet()
  {
    return serverFunctionalLevel_ != null;
  }

  void setServerFunctionalLevel(String value)
  {
    serverFunctionalLevel_ = value;
  }

  public String getServerJobName()
  {
    return serverJobName_;
  }

  public String getServerJobUser()
  {
    return serverJobUser_;
  }

  public String getServerJobNumber()
  {
    return serverJobNumber_;
  }

  public boolean isServerJobSet()
  {
    return serverJobSet_;
  }

  void setServerJob(String jobName, String jobUser, String jobNumber)
  {
    serverJobName_ = jobName;
    serverJobUser_ = jobUser;
    serverJobNumber_ = jobNumber;
    serverJobSet_ = true;
  }
}
