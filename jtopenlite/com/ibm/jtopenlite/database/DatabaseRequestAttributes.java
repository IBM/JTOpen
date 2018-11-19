///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabaseRequestAttributes.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

public class DatabaseRequestAttributes implements
 DatabaseCloseCursorAttributes,
 DatabaseFetchAttributes,
 DatabaseCreateRequestParameterBlockAttributes,
 DatabaseDeleteRequestParameterBlockAttributes,
 DatabasePackageAttributes,
 DatabaseDescribeAttributes,
 DatabaseDescribeParameterMarkerAttributes,
 DatabaseRetrievePackageAttributes,
 DatabaseExecuteAttributes,
 DatabaseExecuteImmediateAttributes,
 DatabaseExecuteOrOpenAndDescribeAttributes,
 DatabaseOpenAndDescribeAttributes,
 DatabaseOpenDescribeFetchAttributes,
 DatabasePrepareAttributes,
 DatabasePrepareAndDescribeAttributes,
 DatabasePrepareAndExecuteAttributes,
 DatabaseChangeDescriptorAttributes,
 DatabaseDeleteDescriptorAttributes,
 DatabaseStreamFetchAttributes,
 DatabaseEndStreamFetchAttributes,
 DatabaseRetrieveLOBDataAttributes
{
  private String cursorName_;
  private int reuseIndicator_;
  private boolean reuseIndicatorSet_;
  private int translateIndicator_;
  private boolean translateIndicatorSet_;
  private byte[] rleCompressedFunctionParameters_;
  private long blockingFactor_;
  private boolean blockingFactorSet_;
  private int fetchScrollOption_;
  private int fetchScrollOptionRelativeValue_;
  private boolean fetchScrollOptionSet_;
  private long fetchBufferSize_;
  private boolean fetchBufferSizeSet_;

  private String packageLibrary_;
  private String packageName_;
  private String prepareStatementName_;
  private String sqlStatementText_;
  private int prepareOption_;
  private boolean prepareOptionSet_;
  private int openAttributes_;
  private boolean openAttributesSet_;
  private int describeOption_;
  private boolean describeOptionSet_;
  private int scrollableCursorFlag_;
  private boolean scrollableCursorFlagSet_;
  private int holdIndicator_;
  private boolean holdIndicatorSet_;
  private int sqlStatementType_;
  private boolean sqlStatementTypeSet_;
  private int sqlParameterMarkerBlockIndicator_;
  private boolean sqlParameterMarkerBlockIndicatorSet_;
  private int queryTimeoutLimit_;
  private boolean queryTimeoutLimitSet_;
  private int serverSideStaticCursorResultSetSize_;
  private boolean serverSideStaticCursorResultSetSizeSet_;
  private int extendedColumnDescriptorOption_;
  private boolean extendedColumnDescriptorOptionSet_;
  private int resultSetHoldabilityOption_;
  private boolean resultSetHoldabilityOptionSet_;
  private String extendedSQLStatementText_;
  private int variableFieldCompression_;
  private boolean variableFieldCompressionSet_;
  private int returnOptimisticLockingColumns_;
  private boolean returnOptimisticLockingColumnsSet_;

  // Return size is supposed to be a 4-byte unsigned number, which would require a long,
  // but the LIPI says it only contains the values 0 through 15.
  private int returnSize_;
  private boolean returnSizeSet_;

  private byte[] sqlParameterMarkerData_;
  private byte[] sqlExtendedParameterMarkerData_;

  private byte[] sqlParameterMarkerDataFormat_;
  private byte[] extendedSQLParameterMarkerDataFormat_;

  private int syncPointCount_;
  private boolean syncPointCountSet_;

  private int lobLocatorHandle_;
  private boolean lobLocatorHandleSet_;
  private int requestedSize_;
  private boolean requestedSizeSet_;
  private int startOffset_;
  private boolean startOffsetSet_;
  private int compressionIndicator_;
  private boolean compressionIndicatorSet_;
  private int returnCurrentLengthIndicator_;
  private boolean returnCurrentLengthIndicatorSet_;
  private int columnIndex_;
  private boolean columnIndexSet_;


  public DatabaseRequestAttributes()
  {
  }

  public void clear()
  {
    cursorName_ = null;
    reuseIndicator_ = 0;
    reuseIndicatorSet_ = false;
    translateIndicator_ = 0;
    translateIndicatorSet_ = false;
    rleCompressedFunctionParameters_ = null;
    blockingFactor_ = 0;
    blockingFactorSet_ = false;
    fetchScrollOption_ = 0;
    fetchScrollOptionRelativeValue_ = 0;
    fetchScrollOptionSet_ = false;
    fetchBufferSize_ = 0;
    fetchBufferSizeSet_ = false;

    packageLibrary_ = null;
    packageName_ = null;
    prepareStatementName_ = null;
    sqlStatementText_ = null;
    prepareOption_ = 0;
    prepareOptionSet_ = false;
    openAttributes_ = 0;
    openAttributesSet_ = false;
    describeOption_ = 0;
    describeOptionSet_ = false;
    scrollableCursorFlag_ = 0;
    scrollableCursorFlagSet_ = false;
    holdIndicator_ = 0;
    holdIndicatorSet_ = false;
    sqlStatementType_ = 0;
    sqlStatementTypeSet_ = false;
    sqlParameterMarkerBlockIndicator_ = 0;
    sqlParameterMarkerBlockIndicatorSet_ = false;
    queryTimeoutLimit_ = 0;
    queryTimeoutLimitSet_ = false;
    serverSideStaticCursorResultSetSize_ = 0;
    serverSideStaticCursorResultSetSizeSet_ = false;
    extendedColumnDescriptorOption_ = 0;
    extendedColumnDescriptorOptionSet_ = false;
    resultSetHoldabilityOption_ = 0;
    resultSetHoldabilityOptionSet_ = false;
    extendedSQLStatementText_ = null;
    variableFieldCompression_ = 0;
    variableFieldCompressionSet_ = false;
    returnOptimisticLockingColumns_ = 0;
    returnOptimisticLockingColumnsSet_ = false;

    returnSize_ = 0;
    returnSizeSet_ = false;

    sqlParameterMarkerData_ = null;
    sqlExtendedParameterMarkerData_ = null;

    sqlParameterMarkerDataFormat_ = null;
    extendedSQLParameterMarkerDataFormat_ = null;

    syncPointCount_ = 0;
    syncPointCountSet_ = false;

    lobLocatorHandle_ = 0;
    lobLocatorHandleSet_ = false;
    requestedSize_ = 0;
    requestedSizeSet_ = false;
    startOffset_ = 0;
    startOffsetSet_ = false;
    compressionIndicator_ = 0;
    compressionIndicatorSet_ = false;
    returnCurrentLengthIndicator_ = 0;
    returnCurrentLengthIndicatorSet_ = false;
    columnIndex_ = 0;
    columnIndexSet_ = false;
  }

  public DatabaseRequestAttributes copy()
  {
	  DatabaseRequestAttributes newCopy = new DatabaseRequestAttributes();
    newCopy.cursorName_  = cursorName_ ;
    newCopy.reuseIndicator_  = reuseIndicator_ ;
    newCopy.reuseIndicatorSet_  = reuseIndicatorSet_ ;
    newCopy.translateIndicator_  = translateIndicator_ ;
    newCopy.translateIndicatorSet_  = translateIndicatorSet_ ;
    newCopy.rleCompressedFunctionParameters_  = rleCompressedFunctionParameters_ ;
    newCopy.blockingFactor_  = blockingFactor_ ;
    newCopy.blockingFactorSet_  = blockingFactorSet_ ;
    newCopy.fetchScrollOption_  = fetchScrollOption_ ;
    newCopy.fetchScrollOptionRelativeValue_  = fetchScrollOptionRelativeValue_ ;
    newCopy.fetchScrollOptionSet_  = fetchScrollOptionSet_ ;
    newCopy.fetchBufferSize_  = fetchBufferSize_ ;
    newCopy.fetchBufferSizeSet_  = fetchBufferSizeSet_ ;

    newCopy.packageLibrary_  = packageLibrary_ ;
    newCopy.packageName_  = packageName_ ;
    newCopy.prepareStatementName_  = prepareStatementName_ ;
    newCopy.sqlStatementText_  = sqlStatementText_ ;
    newCopy.prepareOption_  = prepareOption_ ;
    newCopy.prepareOptionSet_  = prepareOptionSet_ ;
    newCopy.openAttributes_  = openAttributes_ ;
    newCopy.openAttributesSet_  = openAttributesSet_ ;
    newCopy.describeOption_  = describeOption_ ;
    newCopy.describeOptionSet_  = describeOptionSet_ ;
    newCopy.scrollableCursorFlag_  = scrollableCursorFlag_ ;
    newCopy.scrollableCursorFlagSet_  = scrollableCursorFlagSet_ ;
    newCopy.holdIndicator_  = holdIndicator_ ;
    newCopy.holdIndicatorSet_  = holdIndicatorSet_ ;
    newCopy.sqlStatementType_  = sqlStatementType_ ;
    newCopy.sqlStatementTypeSet_  = sqlStatementTypeSet_ ;
    newCopy.sqlParameterMarkerBlockIndicator_  = sqlParameterMarkerBlockIndicator_ ;
    newCopy.sqlParameterMarkerBlockIndicatorSet_  = sqlParameterMarkerBlockIndicatorSet_ ;
    newCopy.queryTimeoutLimit_  = queryTimeoutLimit_ ;
    newCopy.queryTimeoutLimitSet_  = queryTimeoutLimitSet_ ;
    newCopy.serverSideStaticCursorResultSetSize_  = serverSideStaticCursorResultSetSize_ ;
    newCopy.serverSideStaticCursorResultSetSizeSet_  = serverSideStaticCursorResultSetSizeSet_ ;
    newCopy.extendedColumnDescriptorOption_  = extendedColumnDescriptorOption_ ;
    newCopy.extendedColumnDescriptorOptionSet_  = extendedColumnDescriptorOptionSet_ ;
    newCopy.resultSetHoldabilityOption_  = resultSetHoldabilityOption_ ;
    newCopy.resultSetHoldabilityOptionSet_  = resultSetHoldabilityOptionSet_ ;
    newCopy.extendedSQLStatementText_  = extendedSQLStatementText_ ;
    newCopy.variableFieldCompression_  = variableFieldCompression_ ;
    newCopy.variableFieldCompressionSet_  = variableFieldCompressionSet_ ;
    newCopy.returnOptimisticLockingColumns_  = returnOptimisticLockingColumns_ ;
    newCopy.returnOptimisticLockingColumnsSet_  = returnOptimisticLockingColumnsSet_ ;

    newCopy.returnSize_  = returnSize_ ;
    newCopy.returnSizeSet_  = returnSizeSet_ ;

    newCopy.sqlParameterMarkerData_  = sqlParameterMarkerData_ ;
    newCopy.sqlExtendedParameterMarkerData_  = sqlExtendedParameterMarkerData_ ;

    newCopy.sqlParameterMarkerDataFormat_  = sqlParameterMarkerDataFormat_ ;
    newCopy.extendedSQLParameterMarkerDataFormat_  = extendedSQLParameterMarkerDataFormat_ ;

    newCopy.syncPointCount_  = syncPointCount_ ;
    newCopy.syncPointCountSet_  = syncPointCountSet_ ;

    newCopy.lobLocatorHandle_  = lobLocatorHandle_ ;
    newCopy.lobLocatorHandleSet_  = lobLocatorHandleSet_ ;
    newCopy.requestedSize_  = requestedSize_ ;
    newCopy.requestedSizeSet_  = requestedSizeSet_ ;
    newCopy.startOffset_  = startOffset_ ;
    newCopy.startOffsetSet_  = startOffsetSet_ ;
    newCopy.compressionIndicator_  = compressionIndicator_ ;
    newCopy.compressionIndicatorSet_  = compressionIndicatorSet_ ;
    newCopy.returnCurrentLengthIndicator_  = returnCurrentLengthIndicator_ ;
    newCopy.returnCurrentLengthIndicatorSet_  = returnCurrentLengthIndicatorSet_ ;
    newCopy.columnIndex_  = columnIndex_ ;
    newCopy.columnIndexSet_  = columnIndexSet_ ;
    return newCopy;
  }

  public String getCursorName()
  {
    return cursorName_;
  }

  public boolean isCursorNameSet()
  {
    return cursorName_ != null;
  }

  public void setCursorName(String value)
  {
    cursorName_ = value;
  }

  public int getReuseIndicator()
  {
    return reuseIndicator_;
  }

  public boolean isReuseIndicatorSet()
  {
    return reuseIndicatorSet_;
  }

  public void setReuseIndicator(int value)
  {
    reuseIndicator_ = value;
    reuseIndicatorSet_ = true;
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

  public byte[] getRLECompressedFunctionParameters()
  {
    return rleCompressedFunctionParameters_;
  }

  public boolean isRLECompressedFunctionParametersSet()
  {
    return rleCompressedFunctionParameters_ != null;
  }

  public void setRLECompressedFunctionParameters(byte[] value)
  {
    rleCompressedFunctionParameters_ = value;
  }

  public long getBlockingFactor()
  {
    return blockingFactor_;
  }

  public boolean isBlockingFactorSet()
  {
    return blockingFactorSet_;
  }

  public void setBlockingFactor(long value)
  {
    blockingFactor_ = value;
    blockingFactorSet_ = true;
  }

  public int getFetchScrollOption()
  {
    return fetchScrollOption_;
  }

  public int getFetchScrollOptionRelativeValue()
  {
    return fetchScrollOptionRelativeValue_;
  }

  public boolean isFetchScrollOptionSet()
  {
    return fetchScrollOptionSet_;
  }

  public void setFetchScrollOption(int fetchScrollOption, int fetchScrollOptionRelativeValue)
  {
    fetchScrollOption_ = fetchScrollOption;
    fetchScrollOptionRelativeValue_ = fetchScrollOptionRelativeValue;
    fetchScrollOptionSet_ = true;
  }

  public long getFetchBufferSize()
  {
    return fetchBufferSize_;
  }

  public boolean isFetchBufferSizeSet()
  {
    return fetchBufferSizeSet_;
  }

  public void setFetchBufferSize(long value)
  {
    fetchBufferSize_ = value;
    fetchBufferSizeSet_ = true;
  }

  public String getPackageLibrary()
  {
    return packageLibrary_;
  }

  public boolean isPackageLibrarySet()
  {
    return packageLibrary_ != null;
  }

  public void setPackageLibrary(String name)
  {
    packageLibrary_ = name;
  }

  public String getPackageName()
  {
    return packageName_;
  }

  public boolean isPackageNameSet()
  {
    return packageName_ != null;
  }

  public void setPackageName(String name)
  {
    packageName_ = name;
  }

  public String getPrepareStatementName()
  {
    return prepareStatementName_;
  }

  public boolean isPrepareStatementNameSet()
  {
    return prepareStatementName_ != null;
  }

  public void setPrepareStatementName(String value)
  {
    prepareStatementName_ = value;
  }

  public String getSQLStatementText()
  {
    return sqlStatementText_;
  }

  public boolean isSQLStatementTextSet()
  {
    return sqlStatementText_ != null;
  }

  public void setSQLStatementText(String value)
  {
    sqlStatementText_ = value;
  }

  public int getPrepareOption()
  {
    return prepareOption_;
  }

  public boolean isPrepareOptionSet()
  {
    return prepareOptionSet_;
  }

  public void setPrepareOption(int value)
  {
    prepareOption_ = value;
    prepareOptionSet_ = true;
  }

  public int getOpenAttributes()
  {
    return openAttributes_;
  }

  public boolean isOpenAttributesSet()
  {
    return openAttributesSet_;
  }

  public void setOpenAttributes(int value)
  {
    openAttributes_ = value;
    openAttributesSet_ = true;
  }

  public int getDescribeOption()
  {
    return describeOption_;
  }

  public boolean isDescribeOptionSet()
  {
    return describeOptionSet_;
  }

  public void setDescribeOption(int value)
  {
    describeOption_ = value;
    describeOptionSet_ = true;
  }

  public int getScrollableCursorFlag()
  {
    return scrollableCursorFlag_;
  }

  public boolean isScrollableCursorFlagSet()
  {
    return scrollableCursorFlagSet_;
  }

  public void setScrollableCursorFlag(int value)
  {
    scrollableCursorFlag_ = value;
    scrollableCursorFlagSet_ = true;
  }

  public int getHoldIndicator()
  {
    return holdIndicator_;
  }

  public boolean isHoldIndicatorSet()
  {
    return holdIndicatorSet_;
  }

  public void setHoldIndicator(int value)
  {
    holdIndicator_ = value;
    holdIndicatorSet_ = true;
  }

  public int getSQLStatementType()
  {
    return sqlStatementType_;
  }

  public boolean isSQLStatementTypeSet()
  {
    return sqlStatementTypeSet_;
  }

  public void setSQLStatementType(int value)
  {
    sqlStatementType_ = value;
    sqlStatementTypeSet_ = true;
  }

  public int getSQLParameterMarkerBlockIndicator()
  {
    return sqlParameterMarkerBlockIndicator_;
  }

  public boolean isSQLParameterMarkerBlockIndicatorSet()
  {
    return sqlParameterMarkerBlockIndicatorSet_;
  }

  public void setSQLParameterMarkerBlockIndicator(int value)
  {
    sqlParameterMarkerBlockIndicator_ = value;
    sqlParameterMarkerBlockIndicatorSet_ = true;
  }

  public int getQueryTimeoutLimit()
  {
    return queryTimeoutLimit_;
  }

  public boolean isQueryTimeoutLimitSet()
  {
    return queryTimeoutLimitSet_;
  }

  public void setQueryTimeoutLimit(int value)
  {
    queryTimeoutLimit_ = value;
    queryTimeoutLimitSet_ = true;
  }

  public int getServerSideStaticCursorResultSetSize()
  {
    return serverSideStaticCursorResultSetSize_;
  }

  public boolean isServerSideStaticCursorResultSetSizeSet()
  {
    return serverSideStaticCursorResultSetSizeSet_;
  }

  public void setServerSideStaticCursorResultSetSize(int value)
  {
    serverSideStaticCursorResultSetSize_ = value;
    serverSideStaticCursorResultSetSizeSet_ = true;
  }

  public int getExtendedColumnDescriptorOption()
  {
    return extendedColumnDescriptorOption_;
  }

  public boolean isExtendedColumnDescriptorOptionSet()
  {
    return extendedColumnDescriptorOptionSet_;
  }

  public void setExtendedColumnDescriptorOption(int value)
  {
    extendedColumnDescriptorOption_ = value;
    extendedColumnDescriptorOptionSet_ = true;
  }

  public int getResultSetHoldabilityOption()
  {
    return resultSetHoldabilityOption_;
  }

  public boolean isResultSetHoldabilityOptionSet()
  {
    return resultSetHoldabilityOptionSet_;
  }

  public void setResultSetHoldabilityOption(int value)
  {
    resultSetHoldabilityOption_ = value;
    resultSetHoldabilityOptionSet_ = true;
  }

  public String getExtendedSQLStatementText()
  {
    return extendedSQLStatementText_;
  }

  public boolean isExtendedSQLStatementTextSet()
  {
    return extendedSQLStatementText_ != null;
  }

  public void setExtendedSQLStatementText(String value)
  {
    extendedSQLStatementText_ = value;
  }

  public int getVariableFieldCompression()
  {
    return variableFieldCompression_;
  }

  public boolean isVariableFieldCompressionSet()
  {
    return variableFieldCompressionSet_;
  }

  public void setVariableFieldCompression(int value)
  {
    variableFieldCompression_ = value;
    variableFieldCompressionSet_ = true;
  }

  public int getReturnOptimisticLockingColumns()
  {
    return returnOptimisticLockingColumns_;
  }

  public boolean isReturnOptimisticLockingColumnsSet()
  {
    return returnOptimisticLockingColumnsSet_;
  }

  public void setReturnOptimisticLockingColumns(int value)
  {
    returnOptimisticLockingColumns_ = value;
    returnOptimisticLockingColumnsSet_ = true;
  }

  public int getReturnSize()
  {
    return returnSize_;
  }

  public boolean isReturnSizeSet()
  {
    return returnSizeSet_;
  }

  public void setReturnSize(int value)
  {
    returnSize_ = value;
    returnSizeSet_ = true;
  }

  public byte[] getSQLParameterMarkerData()
  {
    return sqlParameterMarkerData_;
  }

  public boolean isSQLParameterMarkerDataSet()
  {
    return sqlParameterMarkerData_ != null;
  }

  public void setSQLParameterMarkerData(byte[] value)
  {
    sqlParameterMarkerData_ = value;
  }

  public byte[] getSQLExtendedParameterMarkerData()
  {
    return sqlExtendedParameterMarkerData_;
  }

  public boolean isSQLExtendedParameterMarkerDataSet()
  {
    return sqlExtendedParameterMarkerData_ != null;
  }

  public void setSQLExtendedParameterMarkerData(byte[] value)
  {
    sqlExtendedParameterMarkerData_ = value;
  }

  public byte[] getSQLParameterMarkerDataFormat()
  {
    return sqlParameterMarkerDataFormat_;
  }

  public boolean isSQLParameterMarkerDataFormatSet()
  {
    return sqlParameterMarkerDataFormat_ != null;
  }

  public void setSQLParameterMarkerDataFormat(byte[] value)
  {
    sqlParameterMarkerDataFormat_ = value;
  }

  public byte[] getExtendedSQLParameterMarkerDataFormat()
  {
    return extendedSQLParameterMarkerDataFormat_;
  }

  public boolean isExtendedSQLParameterMarkerDataFormatSet()
  {
    return extendedSQLParameterMarkerDataFormat_ != null;
  }

  public void setExtendedSQLParameterMarkerDataFormat(byte[] value)
  {
    extendedSQLParameterMarkerDataFormat_ = value;
  }

  public int getSyncPointCount()
  {
    return syncPointCount_;
  }

  public boolean isSyncPointCountSet()
  {
    return syncPointCountSet_;
  }

  public void setSyncPointCount(int value)
  {
    syncPointCount_ = value;
    syncPointCountSet_ = true;
  }

  public int getLOBLocatorHandle()
  {
    return lobLocatorHandle_;
  }

  public boolean isLOBLocatorHandleSet()
  {
    return lobLocatorHandleSet_;
  }

  public void setLOBLocatorHandle(int value)
  {
    lobLocatorHandle_ = value;
    lobLocatorHandleSet_ = true;
  }

  public int getRequestedSize()
  {
    return requestedSize_;
  }

  public boolean isRequestedSizeSet()
  {
    return requestedSizeSet_;
  }

  public void setRequestedSize(int value)
  {
    requestedSize_ = value;
    requestedSizeSet_ = true;
  }

  public int getStartOffset()
  {
    return startOffset_;
  }

  public boolean isStartOffsetSet()
  {
    return startOffsetSet_;
  }

  public void setStartOffset(int value)
  {
    startOffset_ = value;
    startOffsetSet_ = true;
  }

  public int getCompressionIndicator()
  {
    return compressionIndicator_;
  }

  public boolean isCompressionIndicatorSet()
  {
    return compressionIndicatorSet_;
  }

  public void setCompressionIndicator(int value)
  {
    compressionIndicator_ = value;
    compressionIndicatorSet_ = true;
  }

  public int getReturnCurrentLengthIndicator()
  {
    return returnCurrentLengthIndicator_;
  }

  public boolean isReturnCurrentLengthIndicatorSet()
  {
    return returnCurrentLengthIndicatorSet_;
  }

  public void setReturnCurrentLengthIndicator(int value)
  {
    returnCurrentLengthIndicator_ = value;
    returnCurrentLengthIndicatorSet_ = true;
  }

  public int getColumnIndex()
  {
    return columnIndex_;
  }

  public boolean isColumnIndexSet()
  {
    return columnIndexSet_;
  }

  public void setColumnIndex(int value)
  {
    columnIndex_ = value;
    columnIndexSet_ = true;
  }
}
