///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobsSortListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

/**
 * @deprecated Use com.ibm.jtopenlite.command.program.workmgmt.OpenListOfJobsSortListener instead
 */
public interface OpenListOfJobsSortListener
{
  public int getNumberOfSortKeys();

  public int getSortKeyFieldStartingPosition(int keyIndex);

  public int getSortKeyFieldLength(int keyIndex);

  public int getSortKeyFieldDataType(int keyIndex);

  public boolean isAscending(int keyIndex);
}
