///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: ListFormatListener
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.openlist;

/**
 * Responsible for handling data that has been formatted by a ListEntryFormat.
**/
public interface ListFormatListener
{
  /**
   * Called after the list has been opened, but before GetListEntries is called.
  **/
  public void openComplete();

  /**
   * Called when the list has been built on the server and the total number of records is known.
  **/
  public void totalRecordsInList(int totalRecords);

  /**
   * Indicates to the caller who is iterating through the list of entries that they should stop.
  **/
  public boolean stopProcessing();
}

