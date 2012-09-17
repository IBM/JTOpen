///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfJobLogMessages.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.message;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.Parameter;
import com.ibm.jtopenlite.command.program.openlist.*;


/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/QGYOLJBL.htm">QGYOLJBL</a>
 * This class fully implements the V5R4 specification of QGYOLJBL.
**/
public class OpenListOfJobLogMessages implements OpenListProgram<OpenListOfJobLogMessagesOLJL0100,OpenListOfJobLogMessagesOLJL0100Listener>
{
  private static final byte[] ZERO = new byte[4];

  private int inputLength_;
  private int numberOfRecordsToReturn_;
  private OpenListOfJobLogMessagesOLJL0100 formatter_;
  private OpenListOfJobLogMessagesOLJL0100Listener formatListener_;
  private ListInformation info_;
  private OpenListOfJobLogMessagesSelectionListener selectionListener_;

  private byte[] tempData_;

  public OpenListOfJobLogMessages()
  {
  }

  public OpenListOfJobLogMessages(int lengthOfReceiverVariable, int numberOfRecordsToReturn,
                                  OpenListOfJobLogMessagesOLJL0100 format)
  {
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
    formatter_ = format;
  }

  public String getProgramName()
  {
    return "QGYOLJBL";
  }

  public String getProgramLibrary()
  {
    return "QGY";
  }

  public int getNumberOfParameters()
  {
    return 7;
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

  public OpenListOfJobLogMessagesSelectionListener getSelectionListener()
  {
    return selectionListener_;
  }

  public void setSelectionListener(OpenListOfJobLogMessagesSelectionListener listener)
  {
    selectionListener_ = listener;
  }

  public OpenListOfJobLogMessagesOLJL0100 getFormatter()
  {
    return formatter_;
  }

  public void setFormatter(OpenListOfJobLogMessagesOLJL0100 format)
  {
    formatter_ = format;
  }

  public OpenListOfJobLogMessagesOLJL0100Listener getFormatListener()
  {
    return formatListener_;
  }

  public void setFormatListener(OpenListOfJobLogMessagesOLJL0100Listener listener)
  {
    formatListener_ = listener;
  }

  public void newCall()
  {
    info_ = null;
  }

  public ListInformation getListInformation()
  {
    return info_;
  }

  public int getNumberOfRecordsToReturn()
  {
    return numberOfRecordsToReturn_;
  }

  public void setNumberOfRecordsToReturn(int numberOfRecordsToReturn)
  {
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
  }

  public int getParameterInputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 0;
      case 3: return 4;
      case 4:
        int num = 80;
        if (selectionListener_ != null)
        {
          String queue = selectionListener_.getCallMessageQueueName();
          num += 4*selectionListener_.getFieldIdentifierCount() +
                 (queue == null ? 1 : queue.length());

        }
        return num;
      case 5: return 4;
      case 6: return 4;
    }
    return 0;
  }

  public int getParameterOutputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 2: return 80;
      case 6: return 4;
    }
    return 0;
  }

  public int getParameterType(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
      case 2:
        return Parameter.TYPE_OUTPUT;
      case 6:
        return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  public byte[] getParameterInputData(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 3: Conv.intToByteArray(numberOfRecordsToReturn_, tempData, 0); return tempData;
      case 4: // Message selection info.
        final String listDirection = selectionListener_ == null ? "*NEXT" : selectionListener_.getListDirection();
        final String qualifiedJobName = selectionListener_ == null ? "*" : selectionListener_.getQualifiedJobName();
        final byte[] internalJobIdentifier = selectionListener_ == null ? null : selectionListener_.getInternalJobIdentifier();
        final int startingMessageKey = selectionListener_ == null ? 0 : selectionListener_.getStartingMessageKey();
        final int maximumMessageLength = selectionListener_ == null ? 1024 : selectionListener_.getMaximumMessageLength();
        final int maximumMessageHelpLength = selectionListener_ == null ? 1024 : selectionListener_.getMaximumMessageHelpLength();
        final int numberOfFieldsToReturn = selectionListener_ == null ? 1 : selectionListener_.getFieldIdentifierCount();
        final String callMessageQueueName = selectionListener_ == null ? "*" : selectionListener_.getCallMessageQueueName();

        int offsetOfIdentifiersOfFieldsToReturn = 80;
        int offsetOfCallMessageQueueName = offsetOfIdentifiersOfFieldsToReturn+(4*numberOfFieldsToReturn);
        Conv.stringToBlankPadEBCDICByteArray(listDirection, tempData, 0, 10);
        Conv.stringToBlankPadEBCDICByteArray(qualifiedJobName, tempData, 10, 26);
        if (internalJobIdentifier == null)
        {
          for (int i=36; i<52; ++i) tempData[i] = 0x40; // Blanks.
        }
        else
        {
          System.arraycopy(internalJobIdentifier, 0, tempData, 36, 16);
        }
        Conv.intToByteArray(startingMessageKey, tempData, 52);
        Conv.intToByteArray(maximumMessageLength, tempData, 56);
        Conv.intToByteArray(maximumMessageHelpLength, tempData, 60);
        Conv.intToByteArray(offsetOfIdentifiersOfFieldsToReturn, tempData, 64);
        Conv.intToByteArray(numberOfFieldsToReturn, tempData, 68);
        Conv.intToByteArray(offsetOfCallMessageQueueName, tempData, 72);
        Conv.intToByteArray(callMessageQueueName.length(), tempData, 76);
        int offset = 80;
        for (int i=0; i<numberOfFieldsToReturn; ++i)
        {
          Conv.intToByteArray(selectionListener_ == null ? 1001 : selectionListener_.getFieldIdentifier(i), tempData, offset);
          offset += 4;
        }
        Conv.stringToEBCDICByteArray37(callMessageQueueName, tempData, offset);
        return tempData;
      case 5: Conv.intToByteArray(getParameterInputLength(4), tempData, 0); return tempData;
      case 6: return ZERO;
    }
    return null;
  }

  public void setParameterOutputData(final int parmIndex, final byte[] data, final int maxLength)
  {
    int numRead = 0;
    switch (parmIndex)
    {
      case 0:
        if (formatter_ != null)
        {
          formatter_.format(data, maxLength, 0, formatListener_);
        }
        break;
      case 2:
        if (maxLength < 12)
        {
          info_ = null;
        }
        else
        {
          info_ = Util.readOpenListInformationParameter(data, maxLength);
        }
        break;
      default:
        break;
    }
  }
}

