///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMFileMemberDescription.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.ddm;

import com.ibm.jtopenlite.*;

/**
 * Represents information about a file member.
**/
public class DDMFileMemberDescription
{
  private final String library_;
  private final String file_;
  private final String member_;
  private final String fileType_;
  private final String fileAttrib_;
  private final String dataType_;
  private final String fileText_;
  private final String memberText_;
  private final String systemName_;
  private final long recordCapacity_;
  private final long currentRecords_;
  private final long deletedRecords_;

  DDMFileMemberDescription(String fileName, String libName, String fileType, String fileAttrib,
                           String systemName, String dataType, String description, String memberName,
                           String memberDescription, long recordCapacity, long currentRecords, long deletedRecords)
  {
    library_ = libName;
    file_ = fileName;
    member_ = memberName;
    fileType_ = fileType;
    fileAttrib_ = fileAttrib;
    dataType_ = dataType;
    fileText_ = description;
    memberText_ = memberDescription;
    systemName_ = systemName;
    recordCapacity_ = recordCapacity;
    currentRecords_ = currentRecords;
    deletedRecords_ = deletedRecords;
  }

  /**
   * Returns the name (MBNAME) of the member.
  **/
  public String getMember()
  {
    return member_;
  }

  /**
   * Returns the library (MBLIB) in which the file resides.
  **/
  public String getLibrary()
  {
    return library_;
  }

  /**
   * Returns the name of the file (MBFILE).
  **/
  public String getFile()
  {
    return file_;
  }

  /**
   * Returns the file type (MBFTYP).
  **/
  public String getFileType()
  {
    return fileType_;
  }

  /**
   * Returns the file attribute (MBFATR).
  **/
  public String getFileAttribute()
  {
    return fileAttrib_;
  }

  /**
   * Returns the data type (MBDTAT).
  **/
  public String getDataType()
  {
    return dataType_;
  }

  /**
   * Returns the source system name (MBSYSN).
  **/
  public String getSystemName()
  {
    return systemName_;
  }

  /**
   * Returns the text description (MBTEXT) of the file.
  **/
  public String getFileText()
  {
    return fileText_;
  }

  /**
   * Returns the text description (MBMTXT) of the member.
  **/
  public String getMemberText()
  {
    return memberText_;
  }

  /**
   * Returns the record capacity (MBRCDC) of the member.
  **/
  public long getRecordCapacity()
  {
    return recordCapacity_;
  }

  /**
   * Returns the current number of records (MBNRCD) in the member.
  **/
  public long getRecordCount()
  {
    return currentRecords_;
  }

  /**
   * Returns the number of deleted records (MBNDTR) in the member.
  **/
  public long getDeletedRecordCount()
  {
    return deletedRecords_;
  }
}
