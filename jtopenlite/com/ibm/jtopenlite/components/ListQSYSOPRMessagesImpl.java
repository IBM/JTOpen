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
import com.ibm.jtopenlite.command.program.openlist.*;
import com.ibm.jtopenlite.command.program.message.*;
import java.io.*;

class ListQSYSOPRMessagesImpl implements OpenListOfMessagesLSTM0100Listener, OpenListOfMessagesSelectionListener, MessageInfoListener
{
  private final OpenListOfMessagesLSTM0100 messageFormat_ = new OpenListOfMessagesLSTM0100();
  private final OpenListOfMessages messageList_ = new OpenListOfMessages(1200, 1, "0", "1", "QSYSOPR", "QSYS", messageFormat_);
  private final OpenListHandler handler_ = new OpenListHandler(messageList_, messageFormat_, this);

  private int counter_ = -1;
  private MessageInfo[] messages_;
  private final char[] charBuffer_ = new char[4096];

  private MessageInfoListener miListener_;

  public ListQSYSOPRMessagesImpl()
  {
    messageList_.setSelectionListener(this);
  }

  public void setMessageInfoListener(MessageInfoListener listener)
  {
    miListener_ = listener;
  }

  public void openComplete()
  {
  }

  public void totalRecordsInList(int totalRecords)
  {
    miListener_.totalRecords(totalRecords);
  }

  public void totalRecords(int totalRecords)
  {
    messages_ = new MessageInfo[totalRecords];
    counter_ = -1;
  }

  public boolean stopProcessing()
  {
    return miListener_.done();
  }

  public synchronized MessageInfo[] getMessages(final CommandConnection conn) throws IOException
  {
	    messages_ = null;
	    counter_ = -1;
	    handler_.process(conn, 1000);
	    return messages_;
  }

  public boolean done()
  {
    return false;
  }

  public void newMessageInfo(MessageInfo info, int index)
  {
    messages_[index] = info;
  }

  public void replyStatus(String status, int index)
  {
    messages_[index].setReplyStatus(status);
  }

  public void messageText(String text, int index)
  {
    messages_[index].setText(text);
  }

  // LSTM0100 listener.

  public void newMessageEntry(int numberOfFieldsReturned, int messageSeverity,
                              String messageIdentifier, String messageType,
                              int messageKey, String messageFileName,
                              String messageFileLibrarySpecifiedAtSendTime,
                              String messageQueueName, String messageQueueLibrary,
                              String dateSent, String timeSent, String microseconds)
  {
//    messages_[++counter_] = new MessageInfo(messageSeverity, messageIdentifier, messageType, messageKey, dateSent, timeSent, microseconds);
    miListener_.newMessageInfo(new MessageInfo(messageSeverity, messageIdentifier, messageType, messageKey, dateSent, timeSent, microseconds), ++counter_);
  }


  public void newIdentifierField(int identifierField, String typeOfData, String statusOfData,
                              int lengthOfData, byte[] tempData, int offsetOfTempData)
  {
//    System.out.println("New identifier: "+identifierField+","+typeOfData+","+statusOfData+","+lengthOfData+","+offsetOfTempData);
    switch (identifierField)
    {
      case 1001:
//        messages_[counter_].setReplyStatus(Conv.ebcdicByteArrayToString(tempData, offsetOfTempData, lengthOfData, charBuffer_));
        miListener_.replyStatus(Conv.ebcdicByteArrayToString(tempData, offsetOfTempData, lengthOfData, charBuffer_), counter_);
        break;
      case 302:
//        messages_[counter_].setText(Conv.ebcdicByteArrayToString(tempData, offsetOfTempData, lengthOfData, charBuffer_));
        miListener_.messageText(Conv.ebcdicByteArrayToString(tempData, offsetOfTempData, lengthOfData, charBuffer_), counter_);
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
