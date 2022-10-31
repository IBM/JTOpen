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
/**
 * Listener interface used to pass parameters to a call to 
 * RetrieveJournalEntries, which uses the QjoRetrieveJournalEntries API.
 *
 */
public interface RetrieveJournalEntriesSelectionListener
{
  public int getNumberOfVariableLengthRecords();
  public int getVariableLengthRecordKey(int index);

  public int getVariableLengthRecordDataLength(int index);
  public void setVariableLengthRecordData(int index, byte[] buffer, int offset);
}
