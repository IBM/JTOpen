///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListsOfJobsFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.workmgmt;

import com.ibm.jtopenlite.command.program.openlist.*;

public interface OpenListOfJobsFormat<T extends OpenListOfJobsFormatListener> extends ListEntryFormat<T>
{
  public static final int FORMAT_OLJB0100 = 0;
  public static final int FORMAT_OLJB0200 = 1;
  public static final int FORMAT_OLJB0300 = 2;

  public String getName();

  public int getType();

  public int getMinimumRecordLength();

  public void setKeyFields(OpenListOfJobsKeyField[] keyFields);
}
