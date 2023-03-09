///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveJournalEntriesListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.journal;

public interface RetrieveJournalEntriesListener
{
  public void newJournalEntries(int numberOfEntriesRetrieved, char continuationHandle);

  public void newEntryData(int pointerHandle, long sequenceNumber, char journalCode,
                           String entryType, String timestamp,
                           String jobName, String userName, String jobNumber,
                           String programName, String object, int count,
                           char indicatorFlag, long commitCycleIdentifier,
                           String userProfile, String systemName,
                           String journalIdentifier, char referentialConstraint,
                           char trigger, char incompleteData,
                           char objectNameIndicator, char ignoreDuringJournalChange,
                           char minimizedEntrySpecificData);

}

