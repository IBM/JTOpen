///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfMessagesLSTM0100.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;
import java.util.*;

public class OpenListOfMessagesLSTM0100 extends ListEntryFormatAdapter implements OpenListOfMessagesFormat
{
  private OpenListOfMessagesLSTM0100Listener listener_;

  private final char[] c = new char[10];

  public OpenListOfMessagesLSTM0100()
  {
  }

  public OpenListOfMessagesLSTM0100(OpenListOfMessagesLSTM0100Listener listener)
  {
    listener_ = listener;
  }

  synchronized public OpenListOfMessagesLSTM0100Listener getListener()
  {
    return listener_;
  }

  synchronized public void setListener(OpenListOfMessagesLSTM0100Listener listener)
  {
    listener_ = listener;
  }

  private final byte[] lastMessageIdentifierBytes_ = new byte[7];
  private String lastMessageIdentifier_ = "       ";
  private final byte[] lastMessageFileNameBytes_ = new byte[10];
  private String lastMessageFileName_ = "          ";
  private final byte[] lastMessageFileLibraryBytes_ = new byte[10];
  private String lastMessageFileLibrary_ = "          ";
  private final byte[] lastMessageQueueBytes_ = new byte[10];
  private String lastMessageQueue_ = "          ";
  private final byte[] lastMessageQueueLibraryBytes_ = new byte[10];
  private String lastMessageQueueLibrary_ = "          ";

  private static boolean matches(final byte[] data, final int offset, final byte[] data2)
  {
    for (int i=0; i<data2.length; ++i)
    {
      if (data[offset+i] != data2[i]) return false;
    }
    return true;
  }

  private void getMessageIdentifier(final byte[] data, final int offset)
  {
    if (!matches(data, offset, lastMessageIdentifierBytes_))
    {
      System.arraycopy(data, offset, lastMessageIdentifierBytes_, 0, 7);
      lastMessageIdentifier_ = Conv.ebcdicByteArrayToString(data, offset, 7, c);
    }
  }

  private void getMessageFileName(final byte[] data, final int offset)
  {
    if (!matches(data, offset, lastMessageFileNameBytes_))
    {
      System.arraycopy(data, offset, lastMessageFileNameBytes_, 0, 10);
      lastMessageFileName_ = Conv.ebcdicByteArrayToString(data, offset, 10, c);
    }
  }

  private void getMessageFileLibrary(final byte[] data, final int offset)
  {
    if (!matches(data, offset, lastMessageFileLibraryBytes_))
    {
      System.arraycopy(data, offset, lastMessageFileLibraryBytes_, 0, 10);
      lastMessageFileLibrary_ = Conv.ebcdicByteArrayToString(data, offset, 10, c);
    }
  }

  private void getMessageQueue(final byte[] data, final int offset)
  {
    if (!matches(data, offset, lastMessageQueueBytes_))
    {
      System.arraycopy(data, offset, lastMessageQueueBytes_, 0, 10);
      lastMessageQueue_ = Conv.ebcdicByteArrayToString(data, offset, 10, c);
    }
  }

  private void getMessageQueueLibrary(final byte[] data, final int offset)
  {
    if (!matches(data, offset, lastMessageQueueLibraryBytes_))
    {
      System.arraycopy(data, offset, lastMessageQueueLibraryBytes_, 0, 10);
      lastMessageQueueLibrary_ = Conv.ebcdicByteArrayToString(data, offset, 10, c);
    }
  }

  private final HashObject hashObject_ = new HashObject();
  private final HashMap messageTypeCache_ = new HashMap();

  private String getMessageType(final byte[] data, final int offset)
  {
    int num = Conv.byteArrayToShort(data, offset);
    hashObject_.setHash(num);
    String messageType = (String)messageTypeCache_.get(hashObject_);
    if (messageType == null)
    {
      HashObject obj = new HashObject();
      obj.setHash(num);
      messageType = Conv.ebcdicByteArrayToString(data, offset, 2, c);
      messageTypeCache_.put(obj, messageType);
    }
    return messageType;
  }

  synchronized void formatSubclass(final byte[] data, final int maxLength, final int recordLength)
  {
    if (listener_ == null)
    {
      return;
    }

    int offset = 0;
    int offsetToTheNextEntry = 1;
    while (offset < maxLength && offsetToTheNextEntry > 0)
    {
      offsetToTheNextEntry = Conv.byteArrayToInt(data, offset);
      int offsetToFieldsReturned = Conv.byteArrayToInt(data, offset+4);
      int numberOfFieldsReturned = Conv.byteArrayToInt(data, offset+8);
      int messageSeverity = Conv.byteArrayToInt(data, offset+12);
      //String messageIdentifier = Conv.ebcdicByteArrayToString(data, offset+16, 7, c);
      getMessageIdentifier(data, offset+16);
      String messageIdentifier = lastMessageIdentifier_;
      //String messageType = Conv.ebcdicByteArrayToString(data, offset+23, 2, c);
      String messageType = getMessageType(data, offset+23);
      int messageKey = Conv.byteArrayToInt(data, offset+25);
      //String messageFileName = Conv.ebcdicByteArrayToString(data, offset+29, 10, c);
      getMessageFileName(data, offset+29);
      String messageFileName = lastMessageFileName_;
      //String messageFileLibrary = Conv.ebcdicByteArrayToString(data, offset+39, 10, c);
      getMessageFileLibrary(data, offset+39);
      String messageFileLibrary = lastMessageFileLibrary_;
      //String messageQueue = Conv.ebcdicByteArrayToString(data, offset+49, 10, c);
      getMessageQueue(data, offset+49);
      String messageQueue = lastMessageQueue_;
      //String messageQueueLibrary = Conv.ebcdicByteArrayToString(data, offset+59, 10, c);
      getMessageQueueLibrary(data, offset+59);
      String messageQueueLibrary = lastMessageQueueLibrary_;
      String dateSent = Conv.ebcdicByteArrayToString(data, offset+69, 7, c);
      String timeSent = Conv.ebcdicByteArrayToString(data, offset+76, 6, c);
      String microseconds = Conv.ebcdicByteArrayToString(data, offset+82, 6, c);
      listener_.newMessageEntry(numberOfFieldsReturned, messageSeverity,
                                messageIdentifier, messageType, messageKey, messageFileName,
                                messageFileLibrary, messageQueue, messageQueueLibrary, dateSent,
                                timeSent, microseconds);
      offset = offsetToFieldsReturned;
      for (int i=0; i<numberOfFieldsReturned; ++i)
      {
        int offsetToTheNextFieldInformationReturned = Conv.byteArrayToInt(data, offset);
        int lengthOfFieldInformationReturned = Conv.byteArrayToInt(data, offset+4);
        int identifierField = Conv.byteArrayToInt(data, offset+8);
        String typeOfData = Conv.ebcdicByteArrayToString(data, offset+12, 1, c);
        String statusOfData = Conv.ebcdicByteArrayToString(data, offset+13, 1, c);
        int lengthOfData = Conv.byteArrayToInt(data, offset+28);
        listener_.newIdentifierField(identifierField, typeOfData, statusOfData,
                                     lengthOfData, data, offset+32);
        offset = offsetToTheNextFieldInformationReturned;
      }
      offset = offsetToTheNextEntry;
    }
  }
}

