///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListInformation.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

/**
 * 
 * @deprecated Use classes in package jtopnlite.command.program.openlist instead
 *
 */
public class ListInformation
{
  public static final int TYPE_COMPLETE = 0;
  public static final int TYPE_INCOMPLETE = 1;
  public static final int TYPE_PARTIAL = 2;
  public static final int TYPE_UNKNOWN = 3;

  public static final int STATUS_PENDING = 4;
  public static final int STATUS_BUILDING = 5;
  public static final int STATUS_BUILT = 6;
  public static final int STATUS_ERROR = 7;
  public static final int STATUS_PRIMED = 8;
  public static final int STATUS_OVERFLOW = 9;
  public static final int STATUS_UNKNOWN = 10;

  private int totalRecords_;
  private int recordsReturned_;
  private byte[] requestHandle_;
  private int recordLength_;
  private int infoComplete_;
  private String created_;
  private int status_;
  private int lengthOfInfoReturned_;
  private int firstRecord_;

  ListInformation(int total, int returned, byte[] handle,
                  int length, int complete, String date,
                  int status, int lengthOfInfo, int first)
  {
    totalRecords_ = total;
    recordsReturned_ = returned;
    requestHandle_ = handle;
    recordLength_ = length;
    infoComplete_ = complete;
    created_ = date;
    status_ = status;
    lengthOfInfoReturned_ = lengthOfInfo;
    firstRecord_ = first;
  }

  public int getTotalRecords()
  {
    return totalRecords_;
  }

  public int getRecordsReturned()
  {
    return recordsReturned_;
  }

  public byte[] getRequestHandle()
  {
    return requestHandle_;
  }

  public int getRecordLength()
  {
    return recordLength_;
  }

  public int getCompleteType()
  {
    return infoComplete_;
  }

  public String getDateAndTimeCreated()
  {
    return created_;
  }

  public int getStatus()
  {
    return status_;
  }

  public int getLengthOfInformationReturned()
  {
    return lengthOfInfoReturned_;
  }

  public int getFirstRecord()
  {
    return firstRecord_;
  }
}
