///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveJournalReceiverInformatoin.java
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
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/QJORRCVI.htm">QJOURNAL.QjoRtvJrnReceiverInformation</a>
**/
public class RetrieveJournalReceiverInformation extends CallServiceProgramProcedure implements CallServiceProgramParameterFormat
{
  public static final String FORMAT_RRCV0100 = "RRCV0100";

  private int inputLength_ = 512;
  private String receiverName_;
  private String receiverLibrary_;
  private String formatName_ = FORMAT_RRCV0100;
  private RetrieveJournalReceiverInformationListener listener_;

  private final char[] charBuffer_ = new char[30];

  public RetrieveJournalReceiverInformation()
  {
    super("QJOURNAL", "QSYS", "QjoRtvJrnReceiverInformation", RETURN_VALUE_FORMAT_NONE);
    setParameterFormat(this);
  }

  public RetrieveJournalReceiverInformation(String receiverName, String receiverLibrary,
                                            RetrieveJournalReceiverInformationListener listener)
  {
    this(512, receiverName, receiverLibrary, FORMAT_RRCV0100, listener);
  }

  public RetrieveJournalReceiverInformation(int lengthOfReceiverVariable, String receiverName, String receiverLibrary,
                                String format, RetrieveJournalReceiverInformationListener listener)
  {
    this();
    inputLength_ = (lengthOfReceiverVariable < 13 ? 13 : lengthOfReceiverVariable);
    receiverName_ = receiverName;
    receiverLibrary_ = receiverLibrary;
    formatName_ = format;
    listener_ = listener;
  }

  public int getParameterCount()
  {
    return 5;
  }

  public int getParameterLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 1: return 4;
      case 2: return 20;
      case 3: return 8;
      case 4: return 4;
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
        Conv.stringToBlankPadEBCDICByteArray(receiverName_, buffer, offset, 10);
        Conv.stringToBlankPadEBCDICByteArray(receiverLibrary_, buffer, offset+10, 10);
        break;
      case 3:
        Conv.stringToEBCDICByteArray37(formatName_, buffer, offset);
        break;
      case 4:
        Conv.intToByteArray(0, buffer, offset);
        break;
    }
  }

  public void setOutputData(int parmIndex, byte[] buffer, int offset)
  {
    if (parmIndex == 0)
    {
      if (formatName_.equals(FORMAT_RRCV0100) && listener_ != null)
      {
        String journalName = Conv.ebcdicByteArrayToString(buffer, offset+28, 10, charBuffer_);
        String journalLibrary = Conv.ebcdicByteArrayToString(buffer, offset+38, 10, charBuffer_);
        char status = Conv.ebcdicByteToChar(buffer[offset+88]);
        String attachedDateAndTime = Conv.ebcdicByteArrayToString(buffer, offset+95, 13, charBuffer_);
        String detachedDateAndTime = Conv.ebcdicByteArrayToString(buffer, offset+108, 13, charBuffer_);
        long numberOfJournalEntries = Conv.zonedDecimalToLong(buffer, offset+372, 20);
        long firstSequenceNumber = Conv.zonedDecimalToLong(buffer, offset+412, 20);
        long lastSequenceNumber = Conv.zonedDecimalToLong(buffer, offset+432, 20);
        listener_.newReceiverInfo(receiverName_, receiverLibrary_,
                                  journalName, journalLibrary,
                                  numberOfJournalEntries,
                                  firstSequenceNumber, lastSequenceNumber,
                                  status, attachedDateAndTime, detachedDateAndTime);
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

  public String getReceiverName()
  {
    return receiverName_;
  }

  public void setReceiverName(String name)
  {
    receiverName_ = name;
  }

  public String getReceiverLibrary()
  {
    return receiverLibrary_;
  }

  public void setReceiverLibrary(String lib)
  {
    receiverLibrary_ = lib;
  }

  public String getFormatName()
  {
    return formatName_;
  }

  public void setFormatName(String format)
  {
    formatName_ = format;
  }

  public RetrieveJournalReceiverInformationListener getListener()
  {
    return listener_;
  }

  public void setListener(RetrieveJournalReceiverInformationListener listener)
  {
    listener_ = listener;
  }
}


