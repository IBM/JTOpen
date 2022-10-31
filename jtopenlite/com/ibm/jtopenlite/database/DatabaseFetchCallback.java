///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabaseFetchCallback.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

public interface DatabaseFetchCallback
{
  /**
   * The implementor can create their own temp byte array for the row size and reuse it each time a fetch is performed.
   * The implementor can choose to ignore this, and simply return null. The database connection checks to see if the
   * buffer returned by this method is not null and large enough to accommodate the row size.
  **/
  public byte[] getTempDataBuffer(int rowSize);

  public void newResultData(int rowCount, int columnCount, int rowSize);

  public void newIndicator(int row, int column, byte[] tempIndicatorData);

  public void newRowData(int row, byte[] tempData);
}
