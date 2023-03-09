///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveJournalReceiverInformatoinListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.journal;

public interface RetrieveJournalReceiverInformationListener
{
  public void newReceiverInfo(String receiverName, String receiverLibrary,
                              String journalName, String journalLibrary,
                              long numberOfJournalEntries,
                              long firstSequenceNumber, long lastSequenceNumber,
                              char status, String attachedDateAndTime, String detachedDateAndTime);
}

