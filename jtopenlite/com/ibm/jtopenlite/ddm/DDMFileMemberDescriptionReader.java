///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMFileMemberDescriptionReader.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.ddm;

import com.ibm.jtopenlite.*;
import java.util.*;

final class DDMFileMemberDescriptionReader implements DDMReadCallback
{
  private final int serverCCSID_;

  private boolean eof_ = false;

  private final ArrayList<DDMFileMemberDescription> memberDescriptions_ = new ArrayList<DDMFileMemberDescription>();

  DDMFileMemberDescriptionReader(final int serverCCSID)
  {
    serverCCSID_ = serverCCSID;
  }

  List<DDMFileMemberDescription> getMemberDescriptions()
  {
    return memberDescriptions_;
  }

  public void newRecord(final DDMCallbackEvent event, final DDMDataBuffer dataBuffer)
  {
    if (eof_) return;
    final byte[] tempData = dataBuffer.getRecordDataBuffer();
    //final int recordNumber = dataBuffer.getRecordNumber();

    String fileName = Conv.ebcdicByteArrayToString(tempData, 13, 10).trim(); // MBFILE
    String libName = Conv.ebcdicByteArrayToString(tempData, 23, 10).trim(); // MBLIB
    String fileType = Conv.ebcdicByteArrayToString(tempData, 33, 1); // MBFTYP
    String fileAttrib = Conv.ebcdicByteArrayToString(tempData, 41, 6).trim(); // MBFATR
    String systemName = Conv.ebcdicByteArrayToString(tempData, 47, 8).trim(); // MBSYSN
    //int aspID = Integer.valueOf(Conv.packedDecimalToString(tempData, 55, 3, 0)); // MBASP
    String dataType = Conv.ebcdicByteArrayToString(tempData, 61, 1); // MBDTAT
    //int maxFileWaitTime = Integer.valueOf(Conv.packedDecimalToString(tempData, 62, 5, 0)); //MBWAIT
    //int maxRecordWaitTime = Integer.valueOf(Conv.packedDecimalToString(tempData, 65, 5, 0)); // MBWATR
    //String rfLevelCheck = Conv.ebcdicByteArrayToString(tempData, 69, 1); // MBLVLC
    String description = Conv.ebcdicByteArrayToString(tempData, 70, 50).trim(); // MBTXT
    //int numRecordFormats = Integer.valueOf(Conv.packedDecimalToString(tempData, 120, 5, 0)); // MBNOFM
    String memberName = Conv.ebcdicByteArrayToString(tempData, 163, 10).trim(); // MBNAME
    String memberDescription = Conv.ebcdicByteArrayToString(tempData, 193, 50).trim(); // MBMTXT
    long recordCapacity = Long.valueOf(Conv.packedDecimalToString(tempData, 337, 10, 0)); // MBRCDC
    long currentRecords = Long.valueOf(Conv.packedDecimalToString(tempData, 343, 10, 0)); // MBNRCD
    long deletedRecords = Long.valueOf(Conv.packedDecimalToString(tempData, 349, 10, 0)); // MBNDTR

    memberDescriptions_.add(new DDMFileMemberDescription(fileName, libName, fileType, fileAttrib,
                                                         systemName, dataType, description, memberName,
                                                         memberDescription, recordCapacity, currentRecords, deletedRecords));
  }

  public void recordNotFound(final DDMCallbackEvent event)
  {
    eof_ = true;
  }

  public void endOfFile(final DDMCallbackEvent event)
  {
    eof_ = true;
  }

  final boolean eof()
  {
    return eof_;
  }
}
