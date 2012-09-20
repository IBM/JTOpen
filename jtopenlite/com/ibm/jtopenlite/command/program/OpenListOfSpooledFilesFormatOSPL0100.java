///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfSpooledFilesFormatOSPL0100.java
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
public class OpenListOfSpooledFilesFormatOSPL0100 extends SpooledFileFormatAdapter
{
  private OpenListOfSpooledFilesFormatOSPL0100Listener listener_;
  private final char[] charBuffer_ = new char[10];

  public OpenListOfSpooledFilesFormatOSPL0100(OpenListOfSpooledFilesFormatOSPL0100Listener listener)
  {
    listener_ = listener;
  }

  public OpenListOfSpooledFilesFormatOSPL0100Listener getListener()
  {
    return listener_;
  }

  public void setListener(OpenListOfSpooledFilesFormatOSPL0100Listener listener)
  {
    listener_ = listener;
  }

  String getNameSubclass()
  {
    return "OSPL0100";
  }

  int getTypeSubclass()
  {
    return FORMAT_OSPL0100;
  }

  void formatSubclass(final byte[] data, final int maxLength, final int recordLength)
  {
    if (listener_ == null)
    {
      return;
    }

    int numRead = 0;
    while (numRead+156 <= maxLength)
    {
      int current = numRead;
      String spooledFileName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String jobName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String jobUser = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String jobNumber = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      int spooledFileNumber = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int totalPages = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int currentPage = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int copiesLeftToPrint = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      String outputQueueName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String outputQueueLibrary = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String userData = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String status = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String formType = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      String priority = Conv.ebcdicByteArrayToString(data, numRead, 2, charBuffer_);
      numRead += 2;
      byte[] internalJobIdentifier = new byte[16];
      System.arraycopy(data, numRead, internalJobIdentifier, 0, 16);
      numRead += 16;
      byte[] internalSpooledFileIdentifier = new byte[16];
      System.arraycopy(data, numRead, internalSpooledFileIdentifier, 0, 16);
      numRead += 16;
      String deviceType = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      numRead += 2;
      int offsetToExtension = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int lengthOfExtension = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      numRead += 4;
      String jobSystemName = null;
      String dateOpened = null;
      String timeOpened = null;
      if (offsetToExtension > 0 && lengthOfExtension > 0)
      {
        numRead += (numRead-current-offsetToExtension);
        jobSystemName = Conv.ebcdicByteArrayToString(data, numRead, 8, charBuffer_);
        numRead += 8;
        dateOpened = Conv.ebcdicByteArrayToString(data, numRead, 7, charBuffer_);
        numRead += 7;
        timeOpened = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      }
      listener_.newSpooledFileEntry(spooledFileName, jobName, jobUser, jobNumber, spooledFileNumber,
                                    totalPages, currentPage, copiesLeftToPrint,
                                    outputQueueName, outputQueueLibrary, userData,
                                    status, formType, priority,
                                    internalJobIdentifier, internalSpooledFileIdentifier,
                                    deviceType, jobSystemName, dateOpened, timeOpened);
    }
  }
}




