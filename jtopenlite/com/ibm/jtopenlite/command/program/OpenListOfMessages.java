///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfMessages.java
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
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/QGYOLMSG.htm">QGYOLMSG</a>
 * This class fully implements the V5R4 specification of QGYOLMSG.
**/
public class OpenListOfMessages extends ProgramAdapter
{
  private int inputLength_;
  private int numberOfRecordsToReturn_;
  private String sortInformation_;
  private String userOrQueueIndicator_;
  private String userOrQueueName_;
  private String queueLibrary_;
  private OpenListOfMessagesFormat inputFormat_;
  private ListInformation info_;
  private OpenListOfMessagesSelectionListener selectionListener_;

  private int numberOfQueuesUsed_;
  private String messageQueue1_;
  private String messageQueue2_;

  public OpenListOfMessages(int lengthOfReceiverVariable, int numberOfRecordsToReturn, String sortInformation,
                            String userOrQueueIndicator, String userOrQueueName, String queueLibrary,
                            OpenListOfMessagesFormat format)
  {
    super("QGY", "QGYOLMSG", 10);
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
    sortInformation_ = sortInformation;
    userOrQueueIndicator_ = userOrQueueIndicator;
    userOrQueueName_ = userOrQueueName;
    queueLibrary_ = queueLibrary;
    inputFormat_ = format;
  }

  public OpenListOfMessagesSelectionListener getSelectionListener()
  {
    return selectionListener_;
  }

  public void setSelectionListener(OpenListOfMessagesSelectionListener listener)
  {
    selectionListener_ = listener;
  }

  public OpenListOfMessagesFormat getInputFormat()
  {
    return inputFormat_;
  }

  public void setInputFormat(OpenListOfMessagesFormat format)
  {
    inputFormat_ = format;
  }

  void clearOutputData()
  {
    info_ = null;
    numberOfQueuesUsed_ = 0;
    messageQueue1_ = null;
    messageQueue2_ = null;
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

  public String getSortInformation()
  {
    return sortInformation_;
  }

  public void setSortInformation(String s)
  {
    sortInformation_ = s;
  }

  public String getUserOrQueueIndicator()
  {
    return userOrQueueIndicator_;
  }

  public void setUserOrQueueIndicator(String s)
  {
    userOrQueueIndicator_ = s;
  }

  public String getUserOrQueueName()
  {
    return userOrQueueName_;
  }

  public void setUserOrQueueName(String s)
  {
    userOrQueueName_ = s;
  }

  public String getQueueLibrary()
  {
    return queueLibrary_;
  }

  public void setQueueLibrary(String s)
  {
    queueLibrary_ = s;
  }

  public int getNumberOfQueuesUsed()
  {
    return numberOfQueuesUsed_;
  }

  public String getMessageQueue1()
  {
    return messageQueue1_;
  }

  public String getMessageQueue2()
  {
    return messageQueue2_;
  }

  int getParameterInputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 0;
      case 3: return 4;
      case 4: return 1;
      case 5:
        int num = 44;
        if (selectionListener_ != null)
        {
          num += 10*selectionListener_.getSelectionCriteriaCount() +
                  4*selectionListener_.getStartingMessageKeyCount() +
                  4*selectionListener_.getFieldIdentifierCount();
        }
        if (num < 62) num = 62;
        return num;
      case 6: return 4;
      case 7: return 21;
      case 8: return 0;
      case 9: return 4;
    }
    return 0;
  }

  int getParameterOutputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 2: return 80;
      case 8: return 44;
      case 9: return 4;
    }
    return 0;
  }

  int getParameterTypeSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
      case 2:
      case 8:
        return Parameter.TYPE_OUTPUT;
      case 9:
        return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  byte[] getParameterInputDataSubclass(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 3: Conv.intToByteArray(numberOfRecordsToReturn_, tempData, 0); return tempData;
      case 4: Conv.stringToEBCDICByteArray37(sortInformation_, tempData, 0); return tempData;
      case 5: // Message selection info.
        final String listDirection = selectionListener_ == null ? "*NEXT" : selectionListener_.getListDirection();
        final int severityCriteria = selectionListener_ == null ? 0 : selectionListener_.getSeverityCriteria();
        final int maximumMessageLength = selectionListener_ == null ? 1024 : selectionListener_.getMaximumMessageLength();
        final int maximumMessageHelpLength = selectionListener_ == null ? 1024 : selectionListener_.getMaximumMessageHelpLength();
        final int numberOfSelectionCriteria = selectionListener_ == null ? 1 : selectionListener_.getSelectionCriteriaCount();
        final int numberOfStartingMessageKeys = selectionListener_ == null ? 1 : selectionListener_.getStartingMessageKeyCount();
        final int numberOfFieldsToReturn = selectionListener_ == null ? 1 : selectionListener_.getFieldIdentifierCount();

        int offsetOfSelectionCriteria = 44;
        int offsetOfStartingMessageKeys = offsetOfSelectionCriteria+(10*numberOfSelectionCriteria);
        int offsetOfIdentifiersOfFieldsToReturn = offsetOfStartingMessageKeys+(4*numberOfStartingMessageKeys);
        Conv.stringToBlankPadEBCDICByteArray(listDirection, tempData, 0, 10);
        Conv.intToByteArray(severityCriteria, tempData, 12);
        Conv.intToByteArray(maximumMessageLength, tempData, 16);
        Conv.intToByteArray(maximumMessageHelpLength, tempData, 20);
        Conv.intToByteArray(offsetOfSelectionCriteria, tempData, 24);
        Conv.intToByteArray(numberOfSelectionCriteria, tempData, 28);
        Conv.intToByteArray(offsetOfStartingMessageKeys, tempData, 32);
        Conv.intToByteArray(offsetOfIdentifiersOfFieldsToReturn, tempData, 36);
        Conv.intToByteArray(numberOfFieldsToReturn, tempData, 40);
        int offset = 44;
        for (int i=0; i<numberOfSelectionCriteria; ++i)
        {
          Conv.stringToBlankPadEBCDICByteArray(selectionListener_ == null ? "*ALL" : selectionListener_.getSelectionCriteria(i), tempData, offset, 10);
          offset += 10;
        }
        for (int i=0; i<numberOfStartingMessageKeys; ++i)
        {
//          Conv.stringToBlankPadEBCDICByteArray(selectionListener_ == null ? "0000" : selectionListener_.getStartingMessageKey(i), tempData, offset, 4);
          Conv.intToByteArray(selectionListener_ == null ? 0x0000 : selectionListener_.getStartingMessageKey(i), tempData, offset);
          offset += 4;
        }
        for (int i=0; i<numberOfFieldsToReturn; ++i)
        {
          Conv.intToByteArray(selectionListener_ == null ? 1001 : selectionListener_.getFieldIdentifier(i), tempData, offset);
          offset += 4;
        }
        return tempData;
      case 6: Conv.intToByteArray(getParameterInputLength(5), tempData, 0); return tempData;
      case 7:
        Conv.stringToBlankPadEBCDICByteArray(userOrQueueIndicator_, tempData, 0, 1);
        Conv.stringToBlankPadEBCDICByteArray(userOrQueueName_, tempData, 1, 10);
        Conv.stringToBlankPadEBCDICByteArray(queueLibrary_, tempData, 11, 10);
        return tempData;
      case 9: return ZERO;
    }
    return null;
  }

  void setParameterOutputDataSubclass(final int parmIndex, final byte[] data, final int maxLength)
  {
    // TODO:  Unnecessary variable
    // int numRead = 0;
    switch (parmIndex)
    {
      case 0:
        inputFormat_.format(data, maxLength, 0);
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
      case 8:
        if (maxLength >= 44)
        {
          numberOfQueuesUsed_ = Conv.byteArrayToInt(data, 0);
          messageQueue1_ = Conv.ebcdicByteArrayToString(data, 4, 20);
          messageQueue2_ = Conv.ebcdicByteArrayToString(data, 24, 20);
        }
        break;
      default:
        break;
    }
  }
}

