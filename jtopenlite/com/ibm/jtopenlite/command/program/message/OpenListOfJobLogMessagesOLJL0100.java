///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfJobLogMessagesOLJL0100.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.message;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.program.HashObject;
import com.ibm.jtopenlite.command.program.openlist.*;
import java.util.*;

public class OpenListOfJobLogMessagesOLJL0100 implements ListEntryFormat<OpenListOfJobLogMessagesOLJL0100Listener>
{
  private final char[] c = new char[10];

  public OpenListOfJobLogMessagesOLJL0100()
  {
  }

  private final byte[] lastMessageIdentifierBytes_ = new byte[7];
  private String lastMessageIdentifier_ = "       ";
  private final byte[] lastMessageFileNameBytes_ = new byte[10];
  private String lastMessageFileName_ = "          ";
  private final byte[] lastMessageFileLibraryBytes_ = new byte[10];
  private String lastMessageFileLibrary_ = "          ";

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

  private final HashObject hashObject_ = new HashObject();
  private final HashMap<HashObject,String> messageTypeCache_ = new HashMap<HashObject,String>();

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

  public void format(final byte[] data, final int maxLength, final int recordLength, OpenListOfJobLogMessagesOLJL0100Listener listener)
  {
    if (listener == null)
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
      //String messageQueueLibrary = Conv.ebcdicByteArrayToString(data, offset+59, 10, c);
      String dateSent = Conv.ebcdicByteArrayToString(data, offset+49, 7, c);
      String timeSent = Conv.ebcdicByteArrayToString(data, offset+56, 6, c);
      String microseconds = Conv.ebcdicByteArrayToString(data, offset+62, 6, c);
      byte[] threadID = new byte[8];
      System.arraycopy(data, offset+68, threadID, 0, 8);
      listener.newMessageEntry(numberOfFieldsReturned, messageSeverity,
                               messageIdentifier, messageType, messageKey, messageFileName,
                               messageFileLibrary, dateSent, timeSent, microseconds, threadID);
      offset = offsetToFieldsReturned;
      for (int i=0; i<numberOfFieldsReturned; ++i)
      {
        int offsetToTheNextFieldInformationReturned = Conv.byteArrayToInt(data, offset);
        int lengthOfFieldInformationReturned = Conv.byteArrayToInt(data, offset+4);
        int identifierField = Conv.byteArrayToInt(data, offset+8);
        String typeOfData = Conv.ebcdicByteArrayToString(data, offset+12, 1, c);
        String statusOfData = Conv.ebcdicByteArrayToString(data, offset+13, 1, c);
        int lengthOfData = Conv.byteArrayToInt(data, offset+28);
        listener.newIdentifierField(identifierField, typeOfData, statusOfData,
                                    lengthOfData, data, offset+32);
        offset = offsetToTheNextFieldInformationReturned;
      }
      offset = offsetToTheNextEntry;
    }
  }
}

