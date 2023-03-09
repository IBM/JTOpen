///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  SortListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public interface SortListener
{
  public int getNumberOfSortKeys();

  public int getSortKeyFieldStartingPosition(int keyIndex);

  public int getSortKeyFieldLength(int keyIndex);

  public int getSortKeyFieldDataType(int keyIndex);

  public boolean isAscending(int keyIndex);
}
