///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: RetrieveSystemStatus.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.workmgmt;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;

/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qwcrssts.htm">QWCRSSTS</a>
 * This class fully implements the V5R4 specification of QWCRSSTS.
**/
public class RetrieveSystemStatus implements Program
{
  private static final byte[] ZERO = new byte[4];

  public static final int FORMAT_SSTS0100 = 0;
  public static final int FORMAT_SSTS0200 = 1;
  public static final int FORMAT_SSTS0300 = 2;
  public static final int FORMAT_SSTS0400 = 3;
  public static final int FORMAT_SSTS0500 = 4;

  private int inputLength_;
  private int inputFormat_;
  private String inputReset_;

  private int bytesAvailable_;
  private int bytesReturned_;

  private long currentDateAndTime_;
  private String systemName_;

  // Format 0100.
  private int usersCurrentlySignedOn_;
  private int usersTemporarilySignedOff_;
  private int usersSuspendedBySystemRequest_;
  private int usersSuspendedByGroupJobs_;
  private int usersSignedOffWithPrinterOutputWaitingToPrint_;
  private int batchJobsWaitingForMessages_;
  private int batchJobsRunning_;
  private int batchJobsHeldWhileRunning_;
  private int batchJobsEnding_;
  private int batchJobsWaitingToRunOrAlreadyScheduled_;
  private int batchJobsHeldOnAJobQueue_;
  private int batchJobsOnAHeldJobQueue_;
  private int batchJobsOnAnUnassignedJobQueue_;
  private int batchJobsEndedWithPrinterOutputWaitingToPrint_;

  // Format 0200.
  private String elapsedTime_;
  private int percentProcessingUnitUsed_;
  private int jobsInSystem_;
  private int percentPermanentAddresses_;
  private int percentTemporaryAddresses_;
  private int systemASP_;
  private int percentSystemASPUsed_;
  private int totalAuxiliaryStorage_;
  private int currentUnprotectedStorageUsed_;
  private int maximumUnprotectedStorageUsed_;
  private int percentDBCapability_;
  private int mainStorageSize_;
  private int numberOfPartitions_;
  private int partitionIdentifier_;
  private int currentProcessingCapacity_;
  private byte processorSharingAttribute_;
  public static final byte DEDICATED = (byte)0xF0;
  public static final byte SHARED_CAPPED = (byte)0xF1;
  public static final byte SHARED_UNCAPPED = (byte)0xF2;
  private int numberOfProcessors_;
  private int activeJobsInSystem_;
  private int activeThreadsInSystem_;
  private int maximumJobsInSystem_;
  private int percentTemporary256MBSegmentsUsed_;
  private int percentTemporary4GBSegmentsUsed_;
  private int percentPermanent256MBSegmentsUsed_;
  private int percentPermanent4GBSegmentsUsed_;
  private int percentCurrentInteractivePerformance_;
  private int percentUncappedCPUCapacityUsed_;
  private int percentSharedProcessorPoolUsed_;
  private long mainStorageSizeLong_;

  // Format 0300.
  private int numberOfPools_;
  private RetrieveSystemStatusPoolListener poolListener_;

  // Format 0400.
  private int minimumMachinePoolSize_;
  private int minimumBasePoolSize_;

  private String poolSelectionTypeOfPool_;
  private String poolSelectionSharedPoolName_;
  private int poolSelectionSystemPoolIdentifier_;

  public static final String TYPE_SHARED = "*SHARED";
  public static final String TYPE_SYSTEM = "*SYSTEM";

  public static final String SELECT_ALL = "*ALL";
  public static final String SELECT_MACHINE = "*MACHINE";
  public static final String SELECT_BASE = "*BASE";
  public static final String SELECT_INTERACT = "*INTERACT";
  public static final String SELECT_SPOOL = "*SPOOL";

  // Format 0500.
  private int numberOfSubsystemsAvailable_;
  private int numberOfSubsystemsReturned_;

  private byte[] tempData_;

  public RetrieveSystemStatus(int format, boolean resetStatistics)
  {
    inputFormat_ = format;
    inputReset_ = resetStatistics ? "*YES" : "*NO";
    inputLength_ = getFormatSize();
  }

  public final byte[] getTempDataBuffer()
  {
    int maxSize = 0;
    for (int i=0; i<getNumberOfParameters(); ++i)
    {
      int len = getParameterOutputLength(i);
      if (len > maxSize) maxSize = len;
      len = getParameterInputLength(i);
      if (len > maxSize) maxSize = len;
    }
    if (tempData_ == null || tempData_.length < maxSize)
    {
      tempData_ = new byte[maxSize];
    }
    return tempData_;
  }

  public String getProgramName()
  {
    return "QWCRSSTS";
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }

  public void newCall()
  {
    bytesAvailable_ = 0;
    bytesReturned_ = 0;

    currentDateAndTime_ = 0;
    systemName_ = null;

    // Format 0100.
    usersCurrentlySignedOn_ = 0;
    usersTemporarilySignedOff_ = 0;
    usersSuspendedBySystemRequest_ = 0;
    usersSuspendedByGroupJobs_ = 0;
    usersSignedOffWithPrinterOutputWaitingToPrint_ = 0;
    batchJobsWaitingForMessages_ = 0;
    batchJobsRunning_ = 0;
    batchJobsHeldWhileRunning_ = 0;
    batchJobsEnding_ = 0;
    batchJobsWaitingToRunOrAlreadyScheduled_ = 0;
    batchJobsHeldOnAJobQueue_ = 0;
    batchJobsOnAHeldJobQueue_ = 0;
    batchJobsOnAnUnassignedJobQueue_ = 0;
    batchJobsEndedWithPrinterOutputWaitingToPrint_ = 0;

    // Format 0200.
    elapsedTime_ = null;
    percentProcessingUnitUsed_ = 0;
    jobsInSystem_ = 0;
    percentPermanentAddresses_ = 0;
    percentTemporaryAddresses_ = 0;
    systemASP_ = 0;
    percentSystemASPUsed_ = 0;
    totalAuxiliaryStorage_ = 0;
    currentUnprotectedStorageUsed_ = 0;
    maximumUnprotectedStorageUsed_ = 0;
    percentDBCapability_ = 0;
    mainStorageSize_ = 0;
    numberOfPartitions_ = 0;
    partitionIdentifier_ = 0;
    currentProcessingCapacity_ = 0;
    processorSharingAttribute_ = 0;
    numberOfProcessors_ = 0;
    activeJobsInSystem_ = 0;
    activeThreadsInSystem_ = 0;
    maximumJobsInSystem_ = 0;
    percentTemporary256MBSegmentsUsed_ = 0;
    percentTemporary4GBSegmentsUsed_ = 0;
    percentPermanent256MBSegmentsUsed_ = 0;
    percentPermanent4GBSegmentsUsed_ = 0;
    percentCurrentInteractivePerformance_ = 0;
    percentUncappedCPUCapacityUsed_ = 0;
    percentSharedProcessorPoolUsed_ = 0;
    mainStorageSizeLong_ = 0;

    // Format 0300.
    numberOfPools_ = 0;

    // Format 0400.
    minimumMachinePoolSize_ = 0;
    minimumBasePoolSize_ = 0;

    // Format 0500.
    numberOfSubsystemsAvailable_ = 0;
    numberOfSubsystemsReturned_ = 0;
  }

  public int getNumberOfParameters()
  {
    return poolSelectionTypeOfPool_ == null ? 5 : 7;
  }

  public void setPoolListener(RetrieveSystemStatusPoolListener listener)
  {
    poolListener_ = listener;
  }

  public int getLengthOfReceiverVariable()
  {
    return inputLength_;
  }

  public void setLengthOfReceiverVariable(int length)
  {
    inputLength_ = length <= 0 ? 1 : length;
  }

  public void setFormat(int format)
  {
    inputFormat_ = format;
    if (getFormatSize() > inputLength_) inputLength_ = getFormatSize();
  }

  public void setResetStatistics(boolean resetStatistics)
  {
    inputReset_ = resetStatistics ? "*YES" : "*NO";
  }

  public int getBytesAvailable()
  {
    return bytesAvailable_;
  }

  public int getBytesReturned()
  {
    return bytesReturned_;
  }

  /**
   * All formats.
  **/
  public String getSystemName()
  {
    return systemName_;
  }

  /**
   * All formats.
  **/
  public long getCurrentDateAndTime()
  {
    return currentDateAndTime_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getUsersCurrentlySignedOn()
  {
    return usersCurrentlySignedOn_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getUsersTemporarilySignedOff()
  {
    return usersTemporarilySignedOff_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getUsersSuspendedBySystemRequest()
  {
    return usersSuspendedBySystemRequest_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getUsersSuspendedByGroupJobs()
  {
    return usersSuspendedByGroupJobs_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getUsersSignedOffWithPrinterOutputWaitingToPrint()
  {
    return usersSignedOffWithPrinterOutputWaitingToPrint_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsWaitingForMessages()
  {
    return batchJobsWaitingForMessages_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsRunning()
  {
    return batchJobsRunning_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsHeldWhileRunning()
  {
    return batchJobsHeldWhileRunning_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsEnding()
  {
    return batchJobsEnding_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsWaitingToRunOrAlreadyScheduled()
  {
    return batchJobsWaitingToRunOrAlreadyScheduled_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsHeldOnAJobQueue()
  {
    return batchJobsHeldOnAJobQueue_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsOnAHeldJobQueue()
  {
    return batchJobsOnAHeldJobQueue_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsOnAnUnassignedJobQueue()
  {
    return batchJobsOnAnUnassignedJobQueue_;
  }

  /**
   * FORMAT_SSTS0100.
  **/
  public int getBatchJobsEndedWithPrinterOutputWaitingToPrint()
  {
    return batchJobsEndedWithPrinterOutputWaitingToPrint_;
  }

  /**
   * FORMAT_SSTS0200, FORMAT_SSTS0300.
  **/
  public String getElapsedTime()
  {
    return elapsedTime_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentProcessingUnitUsed()
  {
    return percentProcessingUnitUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getJobsInSystem()
  {
    return jobsInSystem_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentPermanentAddresses()
  {
    return percentPermanentAddresses_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentTemporaryAddresses()
  {
    return percentTemporaryAddresses_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getSystemASP()
  {
    return systemASP_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentSystemASPUsed()
  {
    return percentSystemASPUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getTotalAuxiliaryStorage()
  {
    return totalAuxiliaryStorage_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getCurrentUnprotectedStorageUsed()
  {
    return currentUnprotectedStorageUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getMaximumUnprotectedStorageUsed()
  {
    return maximumUnprotectedStorageUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentDBCapability()
  {
    return percentDBCapability_;
  }

  /**
   * FORMAT_SSTS0200, FORMAT_SSTS0400.
  **/
  public int getMainStorageSize()
  {
    return mainStorageSize_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getNumberOfPartitions()
  {
    return numberOfPartitions_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPartitionIdentifier()
  {
    return partitionIdentifier_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getCurrentProcessingCapacity()
  {
    return currentProcessingCapacity_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public byte getProcessorSharingAttribute()
  {
    return processorSharingAttribute_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getNumberOfProcessors()
  {
    return numberOfProcessors_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getActiveJobsInSystem()
  {
    return activeJobsInSystem_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getActiveThreadsInSystem()
  {
    return activeThreadsInSystem_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getMaximumJobsInSystem()
  {
    return maximumJobsInSystem_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentTemporary256MBSegmentsUsed()
  {
    return percentTemporary256MBSegmentsUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentTemporary4GBSegmentsUsed()
  {
    return percentTemporary4GBSegmentsUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentPermanent256MBSegmentsUsed()
  {
    return percentPermanent256MBSegmentsUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentPermanent4GBSegmentsUsed()
  {
    return percentPermanent4GBSegmentsUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentCurrentInteractivePerformance()
  {
    return percentCurrentInteractivePerformance_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentUncappedCPUCapacityUsed()
  {
    return percentUncappedCPUCapacityUsed_;
  }

  /**
   * FORMAT_SSTS0200.
  **/
  public int getPercentSharedProcessorPoolUsed()
  {
    return percentSharedProcessorPoolUsed_;
  }

  /**
   * FORMAT_SSTS0200, FORMAT_SSTS0400.
  **/
  public long getMainStorageSizeLong()
  {
    return mainStorageSizeLong_;
  }

  /**
   * FORMAT_SSTS0300, FORMAT_SSTS0400.
  **/
  public int getNumberOfPools()
  {
    return numberOfPools_;
  }

  /**
   * FORMAT_SSTS0400.
  **/
  public int getMinimumMachinePoolSize()
  {
    return minimumMachinePoolSize_;
  }

  /**
   * FORMAT_SSTS0400.
  **/
  public int getMinimumBasePoolSize()
  {
    return minimumBasePoolSize_;
  }

  /**
   * FORMAT_SSTS0500.
  **/
  public int getNumberOfSubsystemsAvailable()
  {
    return numberOfSubsystemsAvailable_;
  }

  /**
   * FORMAT_SSTS0500.
  **/
  public int getNumberOfSubsystemsReturned()
  {
    return numberOfSubsystemsReturned_;
  }

  /**
   * FORMAT_SSTS0400, FORMAT_SSTS0500.
  **/
  public void setPoolSelectionInformation(String typeOfPool, String sharedPoolName, int systemPoolIdentifier)
  {
    poolSelectionTypeOfPool_ = typeOfPool;
    poolSelectionSharedPoolName_ = sharedPoolName;
    poolSelectionSystemPoolIdentifier_ = systemPoolIdentifier;
  }

  public int getParameterInputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 8;
      case 3: return 10;
      case 4: return 4;
      case 5: return 24;
      case 6: return 4;
    }
    return 0;
  }

  public int getParameterOutputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 4: return 4;
    }
    return 0;
  }

  private int getFormatSize()
  {
    switch (inputFormat_)
    {
      case FORMAT_SSTS0100: return 80;
      case FORMAT_SSTS0200: return 148;
      case FORMAT_SSTS0300: return 128; // Just a guess. Minimum to hold info for one pool entry.
      case FORMAT_SSTS0400: return 244; // Just a guess. Minimum to hold info for one pool entry.
      case FORMAT_SSTS0500: return 74; // Just a guess. Minimum to hold info for one subsystem entry.
    }
    return 0;
  }

  private String getFormatName()
  {
    switch (inputFormat_)
    {
      case FORMAT_SSTS0100: return "SSTS0100";
      case FORMAT_SSTS0200: return "SSTS0200";
      case FORMAT_SSTS0300: return "SSTS0300";
      case FORMAT_SSTS0400: return "SSTS0400";
      case FORMAT_SSTS0500: return "SSTS0500";
    }
    return null;
  }

  public int getParameterType(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return Parameter.TYPE_OUTPUT;
      case 4: return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  public byte[] getParameterInputData(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 2: Conv.stringToEBCDICByteArray37(getFormatName(), tempData, 0); return tempData;
      case 3: Conv.stringToBlankPadEBCDICByteArray(inputReset_, tempData, 0, 10); return tempData;
      case 4: return ZERO;
      case 5: Conv.stringToBlankPadEBCDICByteArray(poolSelectionTypeOfPool_, tempData, 0, 10); Conv.stringToBlankPadEBCDICByteArray(poolSelectionSharedPoolName_, tempData, 10, 10); Conv.intToByteArray(poolSelectionSystemPoolIdentifier_, tempData, 20); return tempData;
      case 6: Conv.intToByteArray(24, tempData, 0); return tempData;
    }
    return null;
  }

  public void setParameterOutputData(final int parmIndex, final byte[] data, final int maxLength)
  {
    switch (parmIndex)
    {
      case 0:
        bytesAvailable_ = Conv.byteArrayToInt(data, 0);
        bytesReturned_ = Conv.byteArrayToInt(data, 4);
        int numRead = 8;
        final char[] c = new char[50];
        switch (inputFormat_)
        {
          case FORMAT_SSTS0100:
            currentDateAndTime_ = Conv.byteArrayToLong(data, numRead);
            numRead += 8;
            systemName_ = Conv.ebcdicByteArrayToString(data, numRead, 8, c);
            numRead += 8;
            usersCurrentlySignedOn_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            usersTemporarilySignedOff_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            usersSuspendedBySystemRequest_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            usersSuspendedByGroupJobs_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            usersSignedOffWithPrinterOutputWaitingToPrint_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsWaitingForMessages_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsRunning_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsHeldWhileRunning_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsEnding_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsWaitingToRunOrAlreadyScheduled_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsHeldOnAJobQueue_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsOnAHeldJobQueue_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsOnAnUnassignedJobQueue_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            batchJobsEndedWithPrinterOutputWaitingToPrint_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            break;
          case FORMAT_SSTS0200:
            currentDateAndTime_ = Conv.byteArrayToLong(data, numRead);
            numRead += 8;
            systemName_ = Conv.ebcdicByteArrayToString(data, numRead, 8, c);
            numRead += 8;
            elapsedTime_ = Conv.ebcdicByteArrayToString(data, numRead, 6, c);
            numRead += 6;
            numRead += 2;
            percentProcessingUnitUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            jobsInSystem_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentPermanentAddresses_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentTemporaryAddresses_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            systemASP_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentSystemASPUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            totalAuxiliaryStorage_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            currentUnprotectedStorageUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            maximumUnprotectedStorageUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentDBCapability_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            mainStorageSize_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            numberOfPartitions_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            partitionIdentifier_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            numRead += 4;
            currentProcessingCapacity_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            processorSharingAttribute_ = data[numRead];
            numRead += 4;
            numberOfProcessors_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            activeJobsInSystem_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            activeThreadsInSystem_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            maximumJobsInSystem_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentTemporary256MBSegmentsUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentTemporary4GBSegmentsUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentPermanent256MBSegmentsUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentPermanent4GBSegmentsUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentCurrentInteractivePerformance_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentUncappedCPUCapacityUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            percentSharedProcessorPoolUsed_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            if (bytesReturned_ >= numRead+8)
            {
              mainStorageSizeLong_ = Conv.byteArrayToLong(data, numRead);
              numRead += 8;
            }
            break;
          case FORMAT_SSTS0300:
            currentDateAndTime_ = Conv.byteArrayToLong(data, numRead);
            numRead += 8;
            systemName_ = Conv.ebcdicByteArrayToString(data, numRead, 8, c);
            numRead += 8;
            elapsedTime_ = Conv.ebcdicByteArrayToString(data, numRead, 6, c);
            numRead += 6;
            numRead += 2;
            numberOfPools_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            int offsetToPoolInfo = Conv.byteArrayToInt(data, numRead); numRead += 4;
            int lengthOfPoolInfoEntry = Conv.byteArrayToInt(data, numRead); numRead += 4;
            int skip = numRead - offsetToPoolInfo;
            numRead += skip;
            if (poolListener_ != null)
            {
              //final byte[] b10 = new byte[10];
              // final char[] c10 = new char[10];
              while (numRead+lengthOfPoolInfoEntry <= maxLength)
              {
                readBasicPoolInfo(data, numRead, c);
                numRead += 84;
                skip = lengthOfPoolInfoEntry - 84;
                numRead += skip;
              }
            }
            break;
          case FORMAT_SSTS0400:
            currentDateAndTime_ = Conv.byteArrayToLong(data, numRead);
            numRead += 8;
            systemName_ = Conv.ebcdicByteArrayToString(data, numRead, 8, c);
            numRead += 8;
            elapsedTime_ = Conv.ebcdicByteArrayToString(data, numRead, 6, c);
            numRead += 6;
            numRead += 2;
            mainStorageSize_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            minimumMachinePoolSize_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            minimumBasePoolSize_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            numberOfPools_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            offsetToPoolInfo = Conv.byteArrayToInt(data, numRead); numRead += 4;
            lengthOfPoolInfoEntry = Conv.byteArrayToInt(data, numRead); numRead += 4;
            mainStorageSizeLong_ = Conv.byteArrayToLong(data, numRead); numRead += 8;
            skip = numRead - offsetToPoolInfo;
            numRead += skip;
            if (poolListener_ != null)
            {
              while (numRead+lengthOfPoolInfoEntry <= maxLength)
              {
                int systemPoolIdentifier = readBasicPoolInfo(data, numRead, c);
                numRead += 84;
                int definedSize = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int currentThreads = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int currentIneligibleThreads = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int tuningPriority = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int tuningMinimumPoolSizePercent = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int tuningMaximumPoolSizePercent = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int tuningMinimumFaults = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int tuningPerThreadFaults = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int tuningMaximumFaults = Conv.byteArrayToInt(data, numRead); numRead += 4;
                String description = Conv.ebcdicByteArrayToString(data, numRead, 50, c);
                int status = data[numRead] & 0x00FF;
                numRead += 2;
                int tuningMinimumActivityLevel = Conv.byteArrayToInt(data, numRead); numRead += 4;
                int tuningMaximumActivityLevel = Conv.byteArrayToInt(data, numRead); numRead += 4;
                poolListener_.extraPoolInfo(systemPoolIdentifier, definedSize, currentThreads, currentIneligibleThreads,
                                            tuningPriority, tuningMinimumPoolSizePercent, tuningMaximumPoolSizePercent,
                                            tuningMinimumFaults, tuningPerThreadFaults, tuningMaximumFaults, description,
                                            status, tuningMinimumActivityLevel, tuningMaximumActivityLevel);
                skip = lengthOfPoolInfoEntry - 180;
                numRead += skip;
              }
            }
            break;
          case FORMAT_SSTS0500:
            currentDateAndTime_ = Conv.byteArrayToLong(data, numRead);
            numRead += 8;
            systemName_ = Conv.ebcdicByteArrayToString(data, numRead, 8, c);
            numRead += 8;
            final int systemPoolIdentifier = Conv.byteArrayToInt(data, numRead); numRead += 4;
            numberOfSubsystemsAvailable_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            numberOfSubsystemsReturned_ = Conv.byteArrayToInt(data, numRead); numRead += 4;
            int offsetToSubsystemInfo = Conv.byteArrayToInt(data, numRead); numRead += 4;
            int lengthOfSubsystemInfoEntry = Conv.byteArrayToInt(data, numRead); numRead += 4;
            String poolName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
            numRead += 10;
            skip = numRead - offsetToSubsystemInfo;
            numRead += skip;
            if (poolListener_ != null)
            {
              for (int i=0; i<numberOfSubsystemsReturned_; ++i)
              {
                String subsystemName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                numRead += 10;
                String subsystemLibrary = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
                numRead += 10;
                poolListener_.newSubsystemInfo(systemPoolIdentifier, poolName, subsystemName, subsystemLibrary);
                skip = lengthOfSubsystemInfoEntry - 20;
                numRead += skip;
              }
            }
            break;
        }
        break;
      default:
        break;
    }
  }

  private int readBasicPoolInfo(final byte[] data, int numRead, final char[] c)
  {
    int systemPoolIdentifier = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int size = Conv.byteArrayToInt(data, numRead); numRead += 4;
    long poolSize = (size < 0) ? ((long)size) & 0x00000000FFFFFFFFL : size;
    size = Conv.byteArrayToInt(data, numRead); numRead += 4;
    long reservedSize = (size < 0) ? ((long)size) & 0x00000000FFFFFFFFL : size;
    int maximumActiveThreads = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int databaseFaults = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int databasePages = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int nonDatabaseFaults = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int nonDatabasePages = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int activeToWait = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int waitToIneligible = Conv.byteArrayToInt(data, numRead); numRead += 4;
    int activeToIneligible = Conv.byteArrayToInt(data, numRead); numRead += 4;
    String poolName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
    numRead += 10;
    String subsystemName = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
    numRead += 10;
    String subsystemLibrary = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
    numRead += 10;
    String pagingOption = Conv.ebcdicByteArrayToString(data, numRead, 10, c);
    numRead += 10;
    poolListener_.newPoolInfo(systemPoolIdentifier, poolSize, reservedSize, maximumActiveThreads, databaseFaults,
                              databasePages, nonDatabaseFaults, nonDatabasePages, activeToWait, waitToIneligible,
                              activeToIneligible, poolName, subsystemName, subsystemLibrary, pagingOption);
    return systemPoolIdentifier;
  }
}

