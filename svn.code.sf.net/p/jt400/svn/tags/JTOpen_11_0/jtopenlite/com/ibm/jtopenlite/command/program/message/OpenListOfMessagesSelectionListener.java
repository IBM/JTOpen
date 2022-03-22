///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfMessageSelectionListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.message;

public interface OpenListOfMessagesSelectionListener
{
  public String getListDirection();

  public int getSeverityCriteria();

  public int getMaximumMessageLength();

  public int getMaximumMessageHelpLength();

  public int getSelectionCriteriaCount();

  public String getSelectionCriteria(int index);

  public int getStartingMessageKeyCount();

  public int getStartingMessageKey(int index);

  public int getFieldIdentifierCount();

  public int getFieldIdentifier(int index);
}
