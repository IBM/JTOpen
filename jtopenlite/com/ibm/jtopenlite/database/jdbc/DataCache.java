///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DataCache.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database.jdbc;

// Unnecessary include
// import java.util.*;
import java.sql.*;

final class DataCache
{
  private int rowCount_;
  private int columnCount_;
  private int rowSize_;

//  private int startingRow_; // Relative row number. In fact, should always be zero?
//  private boolean startingRowSet_;
  private byte[] data_;

  private boolean[] nullMap_;

  private int currentRow_ = -1;

  DataCache()
  {
  }

  byte[] getData()
  {
    return data_;
  }

  int getRowOffset()
  {
    return currentRow_*rowSize_;
  }

  void init(int rowCount, int columnCount, int rowSize)
  {
    rowCount_ = rowCount;
    columnCount_ = columnCount;
    rowSize_ = rowSize;
    int totalSize = rowCount * rowSize;
    if (data_ == null || data_.length < totalSize)
    {
      data_ = new byte[totalSize];
    }
    int totalNulls = rowCount * columnCount;
    if (nullMap_ == null || nullMap_.length < totalNulls)
    {
      nullMap_ = new boolean[totalNulls];
    }
//    startingRowSet_ = false;
    currentRow_ = -1;
  }

  void setNull(int row, int column, boolean b)
  {
//    if (!startingRowSet_)
//    {
//      startingRow_ = row + (currentRow_ < 0 ? 0 : currentRow_);
//      startingRowSet_ = true;
//    }
    int offset = row*columnCount_;
    nullMap_[offset+column] = b;
  }

  //
  // Is the column null.  The column number is 0 based
  // 
  boolean isNull(int column) throws SQLException 
  {
    // Validate the column number
   if (column < 0 || column >= columnCount_) {
       throw new SQLException("Descriptor index ("+column+"/"+columnCount_+")not valid."); 
   } 
    int offset = currentRow_*columnCount_;
    return nullMap_[offset+column];
  }

  void setRow(int row, byte[] tempData)
  {
//    if (!startingRowSet_)
//    {
//      startingRow_ = row + (currentRow_ < 0 ? 0 : currentRow_);
//      startingRowSet_ = true;
//    }
    int offset = row * rowSize_;
    System.arraycopy(tempData, 0, data_, offset, rowSize_);
//    System.out.println("setRow: "+row);
  }

//  int getStartingRow()
//  {
//    return startingRow_;
//  }

  int getNumRows()
  {
    return rowCount_;
  }

  int getCurrentRow()
  {
    return currentRow_;
  }

  int nextRow()
  {
    return ++currentRow_;
  }
}

