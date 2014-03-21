///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveJournalEntries.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program.journal;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.program.*;

/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/QJORJRNE.htm">
 * QJOURNAL.QjoRetrieveJournalEntries
 * </a>
**/
public class RetrieveJournalEntries extends CallServiceProgramProcedure implements CallServiceProgramParameterFormat
{
  public static final String FORMAT_RJNE0100 = "RJNE0100";
  public static final String FORMAT_RJNE0200 = "RJNE0200";
  
	/**
	 * KEY_RANGE_OF_JOURNAL_RECEIVERS corresponds to RCVRNG parameter of RCVJRNE
	 * Command. Type is CHAR(40)
	 */
	public static final int KEY_RANGE_OF_JOURNAL_RECEIVERS = 1;

	/**
	 * KEY_STARTING_SEQUENCE_NUMBER corresponds to FROMENT parameter of RCVJRNE
	 * Command. Type is CHAR(20)
	 */
	public static final int KEY_STARTING_SEQUENCE_NUMBER = 2;
	/**
	 * KEY_STARTING_TIME_STAMP corresponds to FROMTIME parameter of RCVJRNE
	 * Command. Type is CHAR(26)
	 */
	public static final int KEY_STARTING_TIME_STAMP = 3;
	/**
	 * KEY_ENDING_SEQUENCE_NUMBER corresponds to TOENT parameter of RCVJRNE
	 * Command. Type is CHAR(20)
	 */
	public static final int KEY_ENDING_SEQUENCE_NUMBER = 4;
	/**
	 * KEY_ENDING_TIME_STAMP corresponds to TOTIME parameter of RCVJRNE Command.
	 * Type is CHAR(26)
	 */
	public static final int KEY_ENDING_TIME_STAMP = 5;
	/**
	 * KEY_NUMBER_OF_ENTRIES corresponds to NBRENT parameter of RCVJRNE Command.
	 * Type is BINARY(4)
	 */
	public static final int KEY_NUMBER_OF_ENTRIES = 6;
	/**
	 * KEY_JOURNAL_CODES corresponds to JRNCDE parameter of RCVJRNE Command.
	 * Type is CHAR(*)
	 */
	public static final int KEY_JOURNAL_CODES = 7;
	/**
	 * KEY_JOURNAL_ENTRY_TYPES corresponds to ENTTYP parameter of RCVJRNE
	 * Command. Type is CHAR(*)
	 */
	public static final int KEY_JOURNAL_ENTRY_TYPES = 8;
	/**
	 * KEY_JOB corresponds to JOB parameter of RCVJRNE Command. Type is CHAR(26)
	 */
	public static final int KEY_JOB = 9;
	/**
	 * KEY_PROGRAM corresponds to PGM parameter of RCVJRNE Command. Type is
	 * CHAR(10)
	 */
	public static final int KEY_PROGRAM = 10;
	/**
	 * KEY_USER_PROFILE corresponds to USRPRF parameter of RCVJRNE Command. Type
	 * is CHAR(10)
	 */
	public static final int KEY_USER_PROFILE = 11;
	/**
	 * KEY_COMMIT_CYCLE_IDENTIFIER corresponds to CMTCYCID parameter of RCVJRNE
	 * Command. Type is CHAR(20)
	 */
	public static final int KEY_COMMIT_CYCLE_IDENTIFIER = 12;
	/**
	 * KEY_DEPENDENT_ENTRIES corresponds to DEPENT parameter of RCVJRNE Command.
	 * Type is CHAR(10)
	 */
	public static final int KEY_DEPENDENT_ENTRIES = 13;
	/**
	 * KEY_INCLUDE_ENTRIES corresponds to INCENT parameter of RCVJRNE Command.
	 * Type is CHAR(10)
	 */
	public static final int KEY_INCLUDE_ENTRIES = 14;
	/**
	 * KEY_NULL_VALUE_INDICATORS_LENGTH corresponds to NULLINDLEN parameter of
	 * RCVJRNE Command. Type is CHAR(10)
	 */
	public static final int KEY_NULL_VALUE_INDICATORS_LENGTH = 15;
	/**
	 * KEY_FILE corresponds to FILE parameter of RCVJRNE Command. Type is
	 * CHAR(*)
	 */
	public static final int KEY_FILE = 16;
	/**
	 * KEY_OBJECT corresponds to OBJ parameter of RCVJRNE Command. Type is
	 * CHAR(*)
	 */
	public static final int KEY_OBJECT = 17;
	/**
	 * KEY_OBJECT_PATH corresponds to OBJPATH parameter of RCVJRNE Command. Type
	 * is CHAR(*)
	 */
	public static final int KEY_OBJECT_PATH = 18;
	/**
	 * KEY_OBJECT_FILE_IDENTIFIER corresponds to OBJFID parameter of RCVJRNE
	 * Command. Type is CHAR(*)
	 */
	public static final int KEY_OBJECT_FILE_IDENTIFIER = 19;
	/**
	 * KEY_DIRECTORY_SUBTREE corresponds to SUBTREE parameter of RCVJRNE
	 * Command. Type is CHAR(5)
	 */
	public static final int KEY_DIRECTORY_SUBTREE = 20;
	/**
	 * KEY_NAME_PATTERN corresponds to PATTERN parameter of RCVJRNE Command.
	 * Type is CHAR(*)
	 */
	public static final int KEY_NAME_PATTERN = 21;
	/**
	 * KEY_FORMAT_MINIMIZED_DATA corresponds to FMTMINDTA parameter of RCVJRNE
	 * Command. Type is CHAR(10)
	 */
	public static final int KEY_FORMAT_MINIMIZED_DATA = 22;

  private int inputLength_;
  private String journalName_;
  private String journalLibrary_;
  private String formatName_;
  private RetrieveJournalEntriesSelectionListener selection_;
  private RetrieveJournalEntriesListener listener_;

  private final char[] charBuffer_ = new char[30];

  public RetrieveJournalEntries()
  {
    super("QJOURNAL", "QSYS", "QjoRetrieveJournalEntries", RETURN_VALUE_FORMAT_NONE);
    setParameterFormat(this);
  }

  public RetrieveJournalEntries(int lengthOfReceiverVariable, String journalName, String journalLibrary,
                                String format, RetrieveJournalEntriesListener listener)
  {
    this();
    inputLength_ = (lengthOfReceiverVariable < 13 ? 13 : lengthOfReceiverVariable);
    journalName_ = journalName;
    journalLibrary_ = journalLibrary;
    formatName_ = format;
    listener_ = listener;
  }

  public RetrieveJournalEntriesSelectionListener getSelectionListener()
  {
    return selection_;
  }

  public void setSelectionListener(RetrieveJournalEntriesSelectionListener selection)
  {
    selection_ = selection;
  }

  public int getParameterCount()
  {
    return 6;
  }

  public int getParameterLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 1: return 4;
      case 2: return 20;
      case 3: return 8;
      case 4:
        int total = 4;
        if (selection_ != null)
        {
          int num = selection_.getNumberOfVariableLengthRecords();
          for (int i=0; i<num; ++i)
          {
            int len = selection_.getVariableLengthRecordDataLength(i);
            total += 12 + len;
          }
        }
        return total;
      case 5: return 4;
    }
    return 0;
  }

  public int getParameterFormat(int parmIndex)
  {
    return PARAMETER_FORMAT_BY_REFERENCE;
  }

  public void fillInputData(int parmIndex, byte[] buffer, int offset)
  {
    switch (parmIndex)
    {
      case 0:
        break;
      case 1:
        Conv.intToByteArray(inputLength_, buffer, offset);
        break;
      case 2:
        Conv.stringToBlankPadEBCDICByteArray(journalName_, buffer, offset, 10);
        Conv.stringToBlankPadEBCDICByteArray(journalLibrary_, buffer, offset+10, 10);
        break;
      case 3:
        Conv.stringToEBCDICByteArray37(formatName_, buffer, offset);
        break;
      case 4:
        if (selection_ == null)
        {
          Conv.intToByteArray(0, buffer, offset);
        }
        else
        {
          int total = selection_.getNumberOfVariableLengthRecords();
          Conv.intToByteArray(total, buffer, offset);
          offset += 4;
          for (int i=0; i<total; ++i)
          {
            int len = selection_.getVariableLengthRecordDataLength(i);
            int recLen = 12+len;
            Conv.intToByteArray(recLen, buffer, offset);
            Conv.intToByteArray(selection_.getVariableLengthRecordKey(i), buffer, offset+4);
            Conv.intToByteArray(len, buffer, offset+8);
            selection_.setVariableLengthRecordData(i, buffer, offset+12);
            offset += recLen;
          }
        }
        break;
      case 5:
        Conv.intToByteArray(0, buffer, offset);
        break;
    }
  }

  public void setOutputData(int parmIndex, byte[] buffer, int offset)
  {
    if (parmIndex == 0)
    {
      if (formatName_.equals(FORMAT_RJNE0100))
      {
        // int bytesReturned = Conv.byteArrayToInt(buffer, offset);
        offset += 4;
        int offsetToFirstJournalEntryHeader = Conv.byteArrayToInt(buffer, offset);
        offset += 4;
        int numberOfEntriesRetrieved = Conv.byteArrayToInt(buffer, offset);
        offset += 4;
        char continuationHandle = Conv.ebcdicByteToChar(buffer[offset]);
        offset++;
        if (listener_ != null)
        {
          listener_.newJournalEntries(numberOfEntriesRetrieved, continuationHandle);
        }
        if (offsetToFirstJournalEntryHeader > 0 && listener_ != null)
        {
          offset = offsetToFirstJournalEntryHeader;
          for (int i=0; i<numberOfEntriesRetrieved; ++i)
          {
            int displacementToNextHeader = Conv.byteArrayToInt(buffer, offset);
            // int displacementToNullValueIndicators = Conv.byteArrayToInt(buffer, offset+4);
            // int displacementToEntrySpecificData = Conv.byteArrayToInt(buffer, offset+8);
            int pointerHandle = Conv.byteArrayToInt(buffer, offset+12);
            long sequenceNumber = Conv.zonedDecimalToLong(buffer, offset+16, 20);
            char journalCode = Conv.ebcdicByteToChar(buffer[offset+36]);
            String entryType = Conv.ebcdicByteArrayToString(buffer, offset+37, 2, charBuffer_);
            String timeStamp = Conv.ebcdicByteArrayToString(buffer, offset+39, 26, charBuffer_);
            String jobName = Conv.ebcdicByteArrayToString(buffer, offset+65, 10, charBuffer_);
            String userName = Conv.ebcdicByteArrayToString(buffer, offset+75, 10, charBuffer_);
            String jobNumber = Conv.ebcdicByteArrayToString(buffer, offset+85, 6, charBuffer_);
            String programName = Conv.ebcdicByteArrayToString(buffer, offset+91, 10, charBuffer_);
            String object = Conv.ebcdicByteArrayToString(buffer, offset+101, 30, charBuffer_);
            int count = Conv.zonedDecimalToInt(buffer, offset+131, 10);
            char indicatorFlag = Conv.ebcdicByteToChar(buffer[offset+141]);
            long commitCycleIdentifier = Conv.zonedDecimalToLong(buffer, offset+142, 20);
            String userProfile = Conv.ebcdicByteArrayToString(buffer, offset+162, 10, charBuffer_);
            String systemName = Conv.ebcdicByteArrayToString(buffer, offset+172, 8, charBuffer_);
            String journalIdentifier = Conv.ebcdicByteArrayToString(buffer, offset+180, 10, charBuffer_); //TODO - Should this be bytes?
            char referentialConstraint = Conv.ebcdicByteToChar(buffer[offset+190]);
            char trigger = Conv.ebcdicByteToChar(buffer[offset+191]);
            char incompleteData = Conv.ebcdicByteToChar(buffer[offset+192]);
            char objectNameIndicator = Conv.ebcdicByteToChar(buffer[offset+193]);
            char ignoreDuringJournalChange = Conv.ebcdicByteToChar(buffer[offset+194]);
            char minimizedEntrySpecificData = Conv.ebcdicByteToChar(buffer[offset+195]);
            listener_.newEntryData(pointerHandle, sequenceNumber, journalCode,
                                   entryType, timeStamp, jobName, userName, jobNumber,
                                   programName, object, count, indicatorFlag, commitCycleIdentifier,
                                   userProfile, systemName, journalIdentifier, referentialConstraint,
                                   trigger, incompleteData, objectNameIndicator, ignoreDuringJournalChange,
                                   minimizedEntrySpecificData);
            offset += displacementToNextHeader;
          }
        }
      }
    }
  }

  public int getLengthOfReceiverVariable()
  {
    return inputLength_;
  }

  public void setLengthOfReceiverVariable(int length)
  {
    inputLength_ = length;
  }

  public String getJournalName()
  {
    return journalName_;
  }

  public void setJournalName(String name)
  {
    journalName_ = name;
  }

  public String getJournalLibrary()
  {
    return journalLibrary_;
  }

  public void setJournalLibrary(String lib)
  {
    journalLibrary_ = lib;
  }

  public String getFormatName()
  {
    return formatName_;
  }

  public void setFormatName(String format)
  {
    formatName_ = format;
  }

  public RetrieveJournalEntriesListener getListener()
  {
    return listener_;
  }

  public void setListener(RetrieveJournalEntriesListener listener)
  {
    listener_ = listener;
  }
}


