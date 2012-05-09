///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveObjectDescription.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;

/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qusrobjd.htm">QUSROBJD</a>
 * This class fully implements the V5R4 specification of QUSROBJD.
**/
public class RetrieveObjectDescription extends ProgramAdapter
{
  public static final int FORMAT_OBJD0100 = 0;
  public static final int FORMAT_OBJD0200 = 1;
  public static final int FORMAT_OBJD0300 = 2;
  public static final int FORMAT_OBJD0400 = 3;

  private int inputFormat_;
  private int inputLength_;

  private String inputLibrary_;
  private String inputName_;
  private String inputType_;

  // All formats.
  private String objectName_;
  private String objectLibrary_;
  private String objectType_;
  private String returnLibrary_;
  private int objectASPNumber_;
  private String objectOwner_;
  private String objectDomain_;
  private String creationDateAndTime_;
  private String objectChangeDateAndTime_;

  // FORMAT_OBJD0200 and higher.
  private String extendedObjectAttribute_;
  private String textDescription_;
  private String sourceFileName_;
  private String sourceFileLibrary_;
  private String sourceFileMember_;

  // FORMAT_OBJD0300 and higher.
  private String sourceFileUpdatedDateAndTime_;
  private String objectSavedDateAndTime_;
  private String objectRestoredDateAndTime_;
  private String creatorUserProfile_;
  private String systemWhereObjectWasCreated_;
  private String resetDate_;
  private int savedSize_;
  private int saveSequenceNumber_;
  private String storage_;
  private String saveCommand_;
  private String saveVolumeID_;
  private String saveDevice_;
  private String saveFileName_;
  private String saveFileLibrary_;
  private String saveLabel_;
  private String systemLevel_;
  private String compiler_;
  private String objectLevel_;
  private String userChanged_;
  private String licensedProgram_;
  private String ptf_;
  private String apar_;

  // FORMAT_OBJD0400.
  private String lastUsedDate_;
  private String usageInformationUpdated_;
  private int daysUsedCount_;
  private int objectSize_;
  private int objectSizeMultiplier_;
  private String objectCompressionStatus_;
  private String allowChangeByProgram_;
  private String changedByProgram_;
  private String userDefinedAttribute_;
  private String objectOverflowedASPIndicator_;
  private String saveActiveDateAndTime_;
  private String objectAuditingValue_;
  private String primaryGroup_;
  private String journalStatus_;
  private String journalName_;
  private String journalLibrary_;
  private String journalImages_;
  private String journalEntriesToBeOmitted_;
  private String journalStartDateAndTime_;
  private String digitallySigned_;
  private int savedSizeInUnits_;
  private int savedSizeMultiplier_;
  private int libraryASPNumber_;
  private String objectASPDeviceName_;
  private String libraryASPDeviceName_;
  private String digitallySignedBySystemTrustedSource_;
  private String digitallySignedMoreThanOnce_;
  private int primaryAssociatedSpaceSize_;
  private String optimumSpaceAlignment_;
  private String objectASPGroupName_;
  private String libraryASPGroupName_;
  private String startingJournalReceiverNameForApply_;
  private String startingJournalReceiverLibrary_;
  private String startingJournalReceiverLibraryASPDeviceName_;
  private String startingJournalReceiverLibraryASPGroupName_;


  private final char[] c = new char[71]; // Buffer for conversion.

  public RetrieveObjectDescription(String objectLibrary, String objectName, String objectType, int format)
  {
    super("QSYS", "QUSROBJD", 5);
    inputLibrary_ = objectLibrary;
    inputName_ = objectName;
    inputType_ = objectType;
    inputFormat_ = format;
    inputLength_ = getFormatSize();
  }

  void clearOutputData()
  {
    objectName_ = null;
    objectLibrary_ = null;
    objectType_ = null;
    returnLibrary_ = null;
    objectASPNumber_ = 0;
    objectOwner_ = null;
    objectDomain_ = null;
    creationDateAndTime_ = null;
    objectChangeDateAndTime_ = null;

    // FORMAT_OBJD0200 and higher.
    extendedObjectAttribute_ = null;
    textDescription_ = null;
    sourceFileName_ = null;
    sourceFileLibrary_ = null;
    sourceFileMember_ = null;

    // FORMAT_OBJD0300 and higher.
    sourceFileUpdatedDateAndTime_ = null;
    objectSavedDateAndTime_ = null;
    objectRestoredDateAndTime_ = null;
    creatorUserProfile_ = null;
    systemWhereObjectWasCreated_ = null;
    resetDate_ = null;
    savedSize_ = 0;
    saveSequenceNumber_ = 0;
    storage_ = null;
    saveCommand_ = null;
    saveVolumeID_ = null;
    saveDevice_ = null;
    saveFileName_ = null;
    saveFileLibrary_ = null;
    saveLabel_ = null;
    systemLevel_ = null;
    compiler_ = null;
    objectLevel_ = null;
    userChanged_ = null;
    licensedProgram_ = null;
    ptf_ = null;
    apar_ = null;

    // FORMAT_OBJD0400.
    lastUsedDate_ = null;
    usageInformationUpdated_ = null;
    daysUsedCount_ = 0;
    objectSize_ = 0;
    objectSizeMultiplier_ = 0;
    objectCompressionStatus_ = null;
    allowChangeByProgram_ = null;
    changedByProgram_ = null;
    userDefinedAttribute_ = null;
    objectOverflowedASPIndicator_ = null;
    saveActiveDateAndTime_ = null;
    objectAuditingValue_ = null;
    primaryGroup_ = null;
    journalStatus_ = null;
    journalName_ = null;
    journalLibrary_ = null;
    journalImages_ = null;
    journalEntriesToBeOmitted_ = null;
    journalStartDateAndTime_ = null;
    digitallySigned_ = null;
    savedSizeInUnits_ = 0;
    savedSizeMultiplier_ = 0;
    libraryASPNumber_ = 0;
    objectASPDeviceName_ = null;
    libraryASPDeviceName_ = null;
    digitallySignedBySystemTrustedSource_ = null;
    digitallySignedMoreThanOnce_ = null;
    primaryAssociatedSpaceSize_ = 0;
     optimumSpaceAlignment_ = null;
     objectASPGroupName_ = null;
     libraryASPGroupName_ = null;
     startingJournalReceiverNameForApply_ = null;
     startingJournalReceiverLibrary_ = null;
     startingJournalReceiverLibraryASPDeviceName_ = null;
     startingJournalReceiverLibraryASPGroupName_ = null;
  }

  private int getFormatSize()
  {
    switch (inputFormat_)
    {
      case FORMAT_OBJD0100: return 90;
      case FORMAT_OBJD0200: return 180;
      case FORMAT_OBJD0300: return 460;
      case FORMAT_OBJD0400: return 666;
    }
    return 0;
  }

  private String getFormatName()
  {
    switch (inputFormat_)
    {
      case FORMAT_OBJD0100: return "OBJD0100";
      case FORMAT_OBJD0200: return "OBJD0200";
      case FORMAT_OBJD0300: return "OBJD0300";
      case FORMAT_OBJD0400: return "OBJD0400";
    }
    return null;
  }

  public void setFormat(int format)
  {
    inputFormat_ = format;
    inputLength_ = getFormatSize();
  }

  public void setObjectNameToRetrieve(String name)
  {
    inputName_ = name;
  }

  public void setObjectLibraryToRetrieve(String lib)
  {
    inputLibrary_ = lib;
  }

  public void setObjectTypeToRetrieve(String type)
  {
    inputType_ = type;
  }

  /**
   * All formats.
  **/
  public String getObjectName()
  {
    return objectName_;
  }

  /**
   * All formats.
  **/
  public String getObjectLibrary()
  {
    return objectLibrary_;
  }

  /**
   * All formats.
  **/
  public String getObjectType()
  {
    return objectType_;
  }

  /**
   * All formats.
  **/
  public String getReturnLibrary()
  {
    return returnLibrary_;
  }

  /**
   * All formats.
  **/
  public int getObjectASPNumber()
  {
    return objectASPNumber_;
  }

  /**
   * All formats.
  **/
  public String getObjectOwner()
  {
    return objectOwner_;
  }

  /**
   * All formats.
  **/
  public String getObjectDomain()
  {
    return objectDomain_;
  }

  /**
   * All formats.
  **/
  public String getCreationDateAndTime()
  {
    return creationDateAndTime_;
  }

  /**
   * All formats.
  **/
  public String getObjectChangeDateAndTime()
  {
    return objectChangeDateAndTime_;
  }

  /**
   * FORMAT_OBJD0200, FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getExtendedObjectAttribute()
  {
    return extendedObjectAttribute_;
  }

  /**
   * FORMAT_OBJD0200, FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getTextDescription()
  {
    return textDescription_;
  }

  /**
   * FORMAT_OBJD0200, FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSourceFileName()
  {
    return sourceFileName_;
  }

  /**
   * FORMAT_OBJD0200, FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSourceFileLibrary()
  {
    return sourceFileLibrary_;
  }

  /**
   * FORMAT_OBJD0200, FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSourceFileMember()
  {
    return sourceFileMember_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSourceFileUpdatedDateAndTime()
  {
    return sourceFileUpdatedDateAndTime_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getObjectSavedDateAndTime()
  {
    return objectSavedDateAndTime_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getObjectRestoredDateAndTime()
  {
    return objectRestoredDateAndTime_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getCreatorUserProfile()
  {
    return creatorUserProfile_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSystemWhereObjectWasCreated()
  {
    return systemWhereObjectWasCreated_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getResetDate()
  {
    return resetDate_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public int getSavedSize()
  {
    return savedSize_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public int getSaveSequenceNumber()
  {
    return saveSequenceNumber_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getStorage()
  {
    return storage_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSaveCommand()
  {
    return saveCommand_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSaveVolumeID()
  {
    return saveVolumeID_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSaveDevice()
  {
    return saveDevice_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSaveFileName()
  {
    return saveFileName_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSaveFileLibrary()
  {
    return saveFileLibrary_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSaveLabel()
  {
    return saveLabel_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getSystemLevel()
  {
    return systemLevel_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getCompiler()
  {
    return compiler_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getObjectLevel()
  {
    return objectLevel_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getUserChanged()
  {
    return userChanged_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getLicensedProgram()
  {
    return licensedProgram_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getPTF()
  {
    return ptf_;
  }

  /**
   * FORMAT_OBJD0300, FORMAT_OBJD0400.
  **/
  public String getAPAR()
  {
    return apar_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getLastUsedDate()
  {
    return lastUsedDate_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getUsageInformationUpdated()
  {
    return usageInformationUpdated_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public int getDaysUsedCount()
  {
    return daysUsedCount_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public int getObjectSize()
  {
    return objectSize_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public int getObjectSizeMultiplier()
  {
    return objectSizeMultiplier_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getObjectCompressionStatus()
  {
    return objectCompressionStatus_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getAllowChangeByProgram()
  {
    return allowChangeByProgram_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getChangedByProgram()
  {
    return changedByProgram_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getUserDefinedAttribute()
  {
    return userDefinedAttribute_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getObjectOverflowedASPIndicator()
  {
    return objectOverflowedASPIndicator_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getSaveActiveDateAndTime()
  {
    return saveActiveDateAndTime_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getObjectAuditingValue()
  {
    return objectAuditingValue_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getPrimaryGroup()
  {
    return primaryGroup_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getJournalStatus()
  {
    return journalStatus_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getJournalName()
  {
    return journalName_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getJournalLibrary()
  {
    return journalLibrary_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getJournalImages()
  {
    return journalImages_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getJournalEntriesToBeOmitted()
  {
    return journalEntriesToBeOmitted_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getJournalStartDateAndTime()
  {
    return journalStartDateAndTime_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getDigitallySigned()
  {
    return digitallySigned_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public int getSavedSizeInUnits()
  {
    return savedSizeInUnits_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public int getSavedSizeMultiplier()
  {
    return savedSizeMultiplier_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public int getLibraryASPNumber()
  {
    return libraryASPNumber_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getObjectASPDeviceName()
  {
    return objectASPDeviceName_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getLibraryASPDeviceName()
  {
    return libraryASPDeviceName_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getDigitallySignedBySystemTrustedSource()
  {
    return digitallySignedBySystemTrustedSource_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getDigitallySignedMoreThanOnce()
  {
    return digitallySignedMoreThanOnce_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public int getPrimaryAssociatedSpaceSize()
  {
    return primaryAssociatedSpaceSize_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getOptimumSpaceAlignment()
  {
    return optimumSpaceAlignment_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getObjectASPGroupName()
  {
    return objectASPGroupName_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getLibraryASPGroupName()
  {
    return libraryASPGroupName_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getStartingJournalReceiverNameForApply()
  {
    return startingJournalReceiverNameForApply_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getStartingJournalReceiverLibrary()
  {
    return startingJournalReceiverLibrary_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getStartingJournalReceiverLibraryASPDeviceName()
  {
    return startingJournalReceiverLibraryASPDeviceName_;
  }

  /**
   * FORMAT_OBJD0400.
  **/
  public String getStartingJournalReceiverLibraryASPGroupName()
  {
    return startingJournalReceiverLibraryASPGroupName_;
  }



  int getParameterInputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 8;
      case 3: return 20;
      case 4: return 10;
    }
    return 0;
  }

  int getParameterOutputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
    }
    return 0;
  }

  int getParameterTypeSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return Parameter.TYPE_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  byte[] getParameterInputDataSubclass(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 2: Conv.stringToEBCDICByteArray37(getFormatName(), tempData, 0); return tempData;
      case 3: Conv.stringToBlankPadEBCDICByteArray(inputName_, tempData, 0, 10); Conv.stringToBlankPadEBCDICByteArray(inputLibrary_, tempData, 10, 10); return tempData;
      case 4: Conv.stringToBlankPadEBCDICByteArray(inputType_, tempData, 0, 10); return tempData;
    }
    return null;
  }

  private static final String BLANK10 = "          ";
  private static final String ZERO = "0";
  private static final String ONE = "1";

  private static final boolean isBlank10(final byte[] data, final int offset)
  {
//    final int stop = offset+10;
//    for (int i=offset; i<stop; ++i)
//    {
//      if (data[i] != 0x40) return false;
//    }
//    return true;
    return data[offset] == 0x40; // Since this is for QSYS objects, and they cannot start with a space.
  }

  private static final boolean isZeroOrOne(final byte b)
  {
    return (b & 0x00FE) == 0x00F0;
  }

  private static final String getZeroOrOne(final byte b)
  {
    return b == (byte)0xF0 ? ZERO : ONE;
  }

  void setParameterOutputDataSubclass(final int parmIndex, final byte[] data, final int maxLength)
  {
    switch (parmIndex)
    {
      case 0:
        int bytesReturned = Conv.byteArrayToInt(data, 0);
        int bytesAvailable = Conv.byteArrayToInt(data, 4);
        objectName_ = Conv.ebcdicByteArrayToString(data, 8, 10, c);
        objectLibrary_ = Conv.ebcdicByteArrayToString(data, 18, 10, c);
        objectType_ = Conv.ebcdicByteArrayToString(data, 28, 10, c);
        returnLibrary_ = Conv.ebcdicByteArrayToString(data, 38, 10, c);
        objectASPNumber_ = Conv.byteArrayToInt(data, 48);
        objectOwner_ = Conv.ebcdicByteArrayToString(data, 52, 10, c);
        objectDomain_ = Conv.ebcdicByteArrayToString(data, 62, 2, c);
        creationDateAndTime_ = Conv.ebcdicByteArrayToString(data, 64, 13, c);
        objectChangeDateAndTime_ = Conv.ebcdicByteArrayToString(data, 77, 13, c);
        if (inputFormat_ >= FORMAT_OBJD0200)
        {
          extendedObjectAttribute_ = Conv.ebcdicByteArrayToString(data, 90, 10, c);
          textDescription_ = Conv.ebcdicByteArrayToString(data, 100, 50, c);
          sourceFileName_ = isBlank10(data, 150) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 150, 10, c);
          sourceFileLibrary_ = isBlank10(data, 160) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 160, 10, c);
          sourceFileMember_ = isBlank10(data, 170) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 170, 10, c);

          if (inputFormat_ >= FORMAT_OBJD0300)
          {
            sourceFileUpdatedDateAndTime_ = Conv.ebcdicByteArrayToString(data, 180, 13, c);
            objectSavedDateAndTime_ = Conv.ebcdicByteArrayToString(data, 193, 13, c);
            objectRestoredDateAndTime_ = Conv.ebcdicByteArrayToString(data, 206, 13, c);
            creatorUserProfile_ = Conv.ebcdicByteArrayToString(data, 219, 10, c);
            systemWhereObjectWasCreated_ = Conv.ebcdicByteArrayToString(data, 229, 8, c);
            resetDate_ = Conv.ebcdicByteArrayToString(data, 237, 7, c);
            savedSize_ = Conv.byteArrayToInt(data, 244);
            saveSequenceNumber_ = Conv.byteArrayToInt(data, 248);
            storage_ = Conv.ebcdicByteArrayToString(data, 252, 10, c);
            saveCommand_ = Conv.ebcdicByteArrayToString(data, 262, 10, c);
            saveVolumeID_ = Conv.ebcdicByteArrayToString(data, 272, 71, c);
            saveDevice_ = isBlank10(data, 343) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 343, 10, c);
            saveFileName_ = isBlank10(data, 353) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 353, 10, c);
            saveFileLibrary_ = isBlank10(data, 363) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 363, 10, c);
            saveLabel_ = Conv.ebcdicByteArrayToString(data, 373, 17, c);
            systemLevel_ = Conv.ebcdicByteArrayToString(data, 390, 9, c);
            compiler_ = Conv.ebcdicByteArrayToString(data, 399, 16, c);
            objectLevel_ = Conv.ebcdicByteArrayToString(data, 415, 8, c);
            userChanged_ = isZeroOrOne(data[423]) ? getZeroOrOne(data[423]) : Conv.ebcdicByteArrayToString(data, 423, 1, c);
            licensedProgram_ = Conv.ebcdicByteArrayToString(data, 424, 16, c);
            ptf_ = isBlank10(data, 440) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 440, 10, c);
            apar_ = isBlank10(data, 450) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 450, 10, c);

            if (inputFormat_ == FORMAT_OBJD0400)
            {
              lastUsedDate_ = Conv.ebcdicByteArrayToString(data, 460, 7, c);
              usageInformationUpdated_ = Conv.ebcdicByteArrayToString(data, 467, 1, c);
              daysUsedCount_ = Conv.byteArrayToInt(data, 468);
              objectSize_ = Conv.byteArrayToInt(data, 472);
              objectSizeMultiplier_ = Conv.byteArrayToInt(data, 476);
              objectCompressionStatus_ = Conv.ebcdicByteArrayToString(data, 480, 1, c);
              allowChangeByProgram_ = isZeroOrOne(data[481]) ? getZeroOrOne(data[481]) : Conv.ebcdicByteArrayToString(data, 481, 1, c);
              changedByProgram_ = isZeroOrOne(data[482]) ? getZeroOrOne(data[482]) : Conv.ebcdicByteArrayToString(data, 482, 1, c);
              userDefinedAttribute_ = isBlank10(data, 483) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 483, 10, c);
              objectOverflowedASPIndicator_ = Conv.ebcdicByteArrayToString(data, 493, 1, c);
              saveActiveDateAndTime_ = Conv.ebcdicByteArrayToString(data, 494, 13, c);
              objectAuditingValue_ = Conv.ebcdicByteArrayToString(data, 507, 10, c);
              primaryGroup_ = Conv.ebcdicByteArrayToString(data, 517, 10, c);
              journalStatus_ = isZeroOrOne(data[527]) ? getZeroOrOne(data[527]) : Conv.ebcdicByteArrayToString(data, 527, 1, c);
              journalName_ = isBlank10(data, 528) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 528, 10, c);
              journalLibrary_ = isBlank10(data, 538) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 538, 10, c);
              journalImages_ = isZeroOrOne(data[548]) ? getZeroOrOne(data[548]) : Conv.ebcdicByteArrayToString(data, 548, 1, c);
              journalEntriesToBeOmitted_ = isZeroOrOne(data[549]) ? getZeroOrOne(data[549]) : Conv.ebcdicByteArrayToString(data, 549, 1, c);
              journalStartDateAndTime_ = Conv.ebcdicByteArrayToString(data, 550, 13, c);
              digitallySigned_ = isZeroOrOne(data[564]) ? getZeroOrOne(data[564]) : Conv.ebcdicByteArrayToString(data, 563, 1, c);
              savedSizeInUnits_ = Conv.byteArrayToInt(data, 564);
              savedSizeMultiplier_ = Conv.byteArrayToInt(data, 568);
              libraryASPNumber_ = Conv.byteArrayToInt(data, 572);
              objectASPDeviceName_ = isBlank10(data, 576) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 576, 10, c);
              libraryASPDeviceName_ = isBlank10(data, 586) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 586, 10, c);
              digitallySignedBySystemTrustedSource_ = isZeroOrOne(data[596]) ? getZeroOrOne(data[596]) : Conv.ebcdicByteArrayToString(data, 596, 1, c);
              digitallySignedMoreThanOnce_ = isZeroOrOne(data[597]) ? getZeroOrOne(data[597]) : Conv.ebcdicByteArrayToString(data, 597, 1, c);
              primaryAssociatedSpaceSize_ = Conv.byteArrayToInt(data, 600);
              optimumSpaceAlignment_ = Conv.ebcdicByteArrayToString(data, 604, 1, c);
              objectASPGroupName_ = Conv.ebcdicByteArrayToString(data, 605, 10, c);
              libraryASPGroupName_ = Conv.ebcdicByteArrayToString(data, 615, 10, c);
              startingJournalReceiverNameForApply_ = isBlank10(data, 625) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 625, 10, c);
              startingJournalReceiverLibrary_ = isBlank10(data, 635) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 635, 10, c);
              startingJournalReceiverLibraryASPDeviceName_ = isBlank10(data, 645) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 645, 10, c);
              startingJournalReceiverLibraryASPGroupName_ = isBlank10(data, 655) ? BLANK10 : Conv.ebcdicByteArrayToString(data, 655, 10, c);
            }
          }
        }
        break;
      default:
        break;
    }
  }
}

