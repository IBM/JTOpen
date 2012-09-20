///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfSpooledFilesFormatOSPL0300.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;

/**
 * 
 * @deprecated Use classes in package jtopnlite.command.program.print instead
 *
 */
public class OpenListOfSpooledFilesFormatOSPL0300 extends SpooledFileFormatAdapter
{
  private OpenListOfSpooledFilesFormatOSPL0300Listener listener_;
  private final char[] charBuffer_ = new char[10];

  public OpenListOfSpooledFilesFormatOSPL0300(OpenListOfSpooledFilesFormatOSPL0300Listener listener)
  {
    listener_ = listener;
  }

  public OpenListOfSpooledFilesFormatOSPL0300Listener getListener()
  {
    return listener_;
  }

  public void setListener(OpenListOfSpooledFilesFormatOSPL0300Listener listener)
  {
    listener_ = listener;
  }

  String getNameSubclass()
  {
    return "OSPL0300";
  }

  int getTypeSubclass()
  {
    return FORMAT_OSPL0300;
  }

  void formatSubclass(final byte[] data, final int maxLength, final int recordLength)
  {
    if (listener_ == null)
    {
      return;
    }

    int numRead = 0;
    while (numRead+136 <= maxLength)
    {
      String jobName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String jobUser = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String jobNumber = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      String spooledFileName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      int spooledFileNumber = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int fileStatus = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      String dateOpened = Conv.ebcdicByteArrayToString(data, numRead, 7, charBuffer_);
      numRead += 7;
      String timeOpened = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      String spooledFileSchedule = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      String jobSystemName = Conv.ebcdicByteArrayToString(data, numRead, 8, charBuffer_);
      numRead += 8;
      String userData = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String formType = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String outputQueueName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String outputQueueLibrary = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      int asp = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int size = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int multiplier = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      long totalSize = (long)size * (long)multiplier;
      int totalPages = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int copiesLeftToPrint = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      String priority = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      numRead += 3;
      int ippJobIdentifier = Conv.byteArrayToInt(data, numRead);
      listener_.newSpooledFileEntry(jobName, jobUser, jobNumber, spooledFileName, spooledFileNumber,
                                    fileStatus, dateOpened, timeOpened, spooledFileSchedule,
                                    jobSystemName, userData, formType, outputQueueName, outputQueueLibrary,
                                    asp, totalSize, totalPages, copiesLeftToPrint, priority, ippJobIdentifier);
    }
  }
}




