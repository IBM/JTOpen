///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveJournalEntriesSelectionListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.journal;

public interface RetrieveJournalEntriesSelectionListener
{
  public int getNumberOfVariableLengthRecords();

  public int getVariableLengthRecordKey(int index);

  public int getVariableLengthRecordDataLength(int index);

  public void setVariableLengthRecordData(byte[] buffer, int offset);
}
