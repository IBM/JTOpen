///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfMessagesLSTM0100Listener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.message;

import com.ibm.jtopenlite.command.program.openlist.*;

public interface OpenListOfMessagesLSTM0100Listener extends ListFormatListener
{
  public void newMessageEntry(int numberOfFieldsReturned, int messageSeverity,
                              String messageIdentifier, String messageType,
                              int messageKey, String messageFileName,
                              String messageFileLibrarySpecifiedAtSendTime,
                              String messageQueueName, String messageQueueLibrary,
                              String dateSent, String timeSent, String microseconds);

  public void newIdentifierField(int identifierField, String typeOfData, String statusOfData,
                              int lengthOfData, byte[] tempData, int offsetOfTempData);
}

