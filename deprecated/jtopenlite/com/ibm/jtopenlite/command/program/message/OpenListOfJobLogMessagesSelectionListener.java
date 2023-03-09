///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfJobLogMessagesSelectionListener
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.message;

public interface OpenListOfJobLogMessagesSelectionListener
{
  public String getListDirection();

  public String getQualifiedJobName();

  public byte[] getInternalJobIdentifier();

  public int getStartingMessageKey();

  public int getMaximumMessageLength();

  public int getMaximumMessageHelpLength();

  public int getFieldIdentifierCount();

  public int getFieldIdentifier(int index);

  public String getCallMessageQueueName();
}
