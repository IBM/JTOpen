///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListQSYSOPRMessagesImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;
import com.ibm.jtopenlite.command.program.*;
import java.io.*;

class ListQSYSOPRMessagesImpl implements OpenListOfMessagesLSTM0100Listener, OpenListOfMessagesSelectionListener
{
  private final OpenListOfMessagesLSTM0100 messageFormat_ = new OpenListOfMessagesLSTM0100();
  private final OpenListOfMessages messageList_ = new OpenListOfMessages(1200, 1, "0", "1", "QSYSOPR", "QSYS", messageFormat_);
  private final GetListEntries getEntries_ = new GetListEntries(0, null, 0, 0, 0, messageFormat_);
  private final CloseList close_ = new CloseList(null);

  private int counter_ = -1;
  private MessageInfo[] messages_;
  private final char[] charBuffer_ = new char[4096];

  public ListQSYSOPRMessagesImpl()
  {
    messageList_.setSelectionListener(this);
  }

  public synchronized MessageInfo[] getMessages(final CommandConnection conn) throws IOException
  {
    messageFormat_.setListener(null);

    CommandResult result = conn.call(messageList_);
    if (!result.succeeded())
    {
      throw new IOException("Message list failed: "+result.toString());
    }

    ListInformation listInfo = messageList_.getListInformation();
//    System.out.println(listInfo.getTotalRecords()+","+listInfo.getRecordsReturned()+","+listInfo.getRecordLength()+","+listInfo.getCompleteType()+","+listInfo.getStatus());
    byte[] requestHandle = listInfo.getRequestHandle();
    close_.setRequestHandle(requestHandle);

    try
    {
      //int recordLength = listInfo.getRecordLength();
      // Now, the list is building on the server.
      // Call GetListEntries once to wait for the list to finish building, for example.
      int receiverSize = 100; // Should be good enough for the first call.
      int numRecordsToReturn = 0; // For some reason, specifying 0 here does not wait until the whole list is built.
      int startingRecord = -1; // Wait until whole list is built before returning.
      getEntries_.setLengthOfReceiverVariable(receiverSize);
      getEntries_.setRequestHandle(requestHandle);
      //getEntries_.setRecordLength(recordLength);
      getEntries_.setNumberOfRecordsToReturn(numRecordsToReturn);
      getEntries_.setStartingRecord(startingRecord);
      result = conn.call(getEntries_);
      if (!result.succeeded())
      {
        throw new IOException("Get entries failed: "+result.toString());
      }

      listInfo = getEntries_.getListInformation();
//      System.out.println(listInfo.getTotalRecords()+","+listInfo.getRecordsReturned()+","+listInfo.getRecordLength()+","+listInfo.getCompleteType()+","+listInfo.getStatus());
      int totalRecords = listInfo.getTotalRecords();
      messages_ = new MessageInfo[totalRecords];
      counter_ = -1;

      // Now retrieve each job record in chunks of 300 at a time.
      numRecordsToReturn = 1000;
//      System.out.println(recordLength+" * "+numRecordsToReturn);
      //receiverSize = recordLength * numRecordsToReturn;
      receiverSize = 100000;
      startingRecord = 1;
      getEntries_.setLengthOfReceiverVariable(receiverSize);
      getEntries_.setNumberOfRecordsToReturn(numRecordsToReturn);
      getEntries_.setStartingRecord(startingRecord);
      messageFormat_.setListener(this); // Ready to process.
      while (startingRecord <= totalRecords)
      {
        result = conn.call(getEntries_);
        if (!result.succeeded())
        {
          throw new IOException("Get entries failed: "+result.toString());
        }
        // Assuming it succeeded...
        listInfo = getEntries_.getListInformation();
        startingRecord += listInfo.getRecordsReturned();
        getEntries_.setStartingRecord(startingRecord);
      }
      return messages_;
    }
    finally
    {
      // All done.
      conn.call(close_);
    }
  }

  // LSTM0100 listener.

  public void newMessageEntry(int numberOfFieldsReturned, int messageSeverity,
                              String messageIdentifier, String messageType,
                              int messageKey, String messageFileName,
                              String messageFileLibrarySpecifiedAtSendTime,
                              String messageQueueName, String messageQueueLibrary,
                              String dateSent, String timeSent, String microseconds)
  {
    messages_[++counter_] = new MessageInfo(messageSeverity, messageIdentifier, messageType, messageKey, dateSent, timeSent, microseconds);
//    System.out.println(++counter_+" -------- "+numberOfFieldsReturned+","+messageSeverity+","+messageIdentifier+","+messageType+","+messageKey+","+messageFileName+","+messageFileLibrarySpecifiedAtSendTime+","+messageQueueName+","+messageQueueLibrary+","+dateSent+","+timeSent+","+microseconds);
  }


  public void newIdentifierField(int identifierField, String typeOfData, String statusOfData,
                              int lengthOfData, byte[] tempData, int offsetOfTempData)
  {
//    System.out.println("New identifier: "+identifierField+","+typeOfData+","+statusOfData+","+lengthOfData+","+offsetOfTempData);
    switch (identifierField)
    {
      case 1001:
        messages_[counter_].setReplyStatus(Conv.ebcdicByteArrayToString(tempData, offsetOfTempData, lengthOfData, charBuffer_));
        break;
      case 302:
        messages_[counter_].setText(Conv.ebcdicByteArrayToString(tempData, offsetOfTempData, lengthOfData, charBuffer_));
        break;
    }
  }

  // Selection listener.

  public String getListDirection()
  {
    return "*NEXT";
  }

  public int getSeverityCriteria()
  {
    return 0;
  }

  public int getMaximumMessageLength()
  {
    return 8192;
  }

  public int getMaximumMessageHelpLength()
  {
    return 0;
  }

  public int getSelectionCriteriaCount()
  {
    return 1;
  }

  public String getSelectionCriteria(int index)
  {
    return "*ALL";
  }

  public int getStartingMessageKeyCount()
  {
    return 1;
  }

  public int getStartingMessageKey(int index)
  {
    return 0;
  }

  private static final int[] fields_ = new int[] { 1001, 302 };

  public int getFieldIdentifierCount()
  {
    return fields_.length;
  }

  public int getFieldIdentifier(int index)
  {
    return fields_[index];
  }

}
