///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListJobLogMessagesImpl.java
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
import com.ibm.jtopenlite.command.program.message.*;
import com.ibm.jtopenlite.command.program.openlist.*;

import java.io.*;

class ListJobLogMessagesImpl implements OpenListOfJobLogMessagesOLJL0100Listener, OpenListOfJobLogMessagesSelectionListener
{
  private final OpenListOfJobLogMessagesOLJL0100 messageFormat_ = new OpenListOfJobLogMessagesOLJL0100();
  private final OpenListOfJobLogMessages messageList_ = new OpenListOfJobLogMessages(1000, 1, messageFormat_);
  private final OpenListHandler handler_ = new OpenListHandler(messageList_, messageFormat_, this);

  private int counter_ = -1;
  private MessageInfo[] messages_;
  private final char[] charBuffer_ = new char[4096];

  private String jobName_;

  public ListJobLogMessagesImpl()
  {
    messageList_.setSelectionListener(this);
  }

  public void openComplete()
  {
  }

  public void totalRecordsInList(int total)
  {
    messages_ = new MessageInfo[total];
    counter_ = -1;
  }

  public boolean stopProcessing()
  {
    return false;
  }

  public synchronized MessageInfo[] getMessages(final CommandConnection conn, JobInfo job) throws IOException
  {
    messages_ = null;
    counter_ = -1;
    jobName_ = job.getJobName();
    while (jobName_.length() < 10) jobName_ = jobName_+" ";
    jobName_ = jobName_ + job.getUserName();
    while (jobName_.length() < 20) jobName_ = jobName_+" ";
    jobName_ = jobName_ + job.getJobNumber();

    handler_.process(conn, 1000);
    return messages_;
  }

  // OLJL0100 listener.

  public void newMessageEntry(int numberOfFieldsReturned, int messageSeverity,
                              String messageIdentifier, String messageType,
                              int messageKey, String messageFileName,
                              String messageFileLibrarySpecifiedAtSendTime,
                              String dateSent, String timeSent, String microseconds, byte[] threadID)
  {
    messages_[++counter_] = new MessageInfo(messageSeverity, messageIdentifier, messageType, messageKey, dateSent, timeSent, microseconds);
  }


  public void newIdentifierField(int identifierField, String typeOfData, String statusOfData,
                              int lengthOfData, byte[] tempData, int offsetOfTempData)
  {
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

  public String getQualifiedJobName()
  {
    return jobName_;
  }

  public byte[] getInternalJobIdentifier()
  {
    return null;
  }

  public int getStartingMessageKey()
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

  private static final int[] fields_ = new int[] { 1001, 302 };

  public int getFieldIdentifierCount()
  {
    return fields_.length;
  }

  public int getFieldIdentifier(int index)
  {
    return fields_[index];
  }

  public String getCallMessageQueueName()
  {
    return "*";
  }
}