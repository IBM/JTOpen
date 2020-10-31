///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfSpooledFilesFormatOSPL0300.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.print;

import com.ibm.jtopenlite.*;

public class OpenListOfSpooledFilesFormatOSPL0300 implements OpenListOfSpooledFilesFormat<OpenListOfSpooledFilesFormatOSPL0300Listener>
{
  private final char[] charBuffer_ = new char[10];

  public OpenListOfSpooledFilesFormatOSPL0300()
  {
  }

  public String getName()
  {
    return "OSPL0300";
  }

  public int getType()
  {
    return FORMAT_OSPL0300;
  }

  private final byte[] lastJobNameBytes_ = new byte[10];
  private String lastJobName_ = "          ";
  private final byte[] lastJobUserBytes_ = new byte[10];
  private String lastJobUser_ = "          ";
  private final byte[] lastSpooledFileNameBytes_ = new byte[10];
  private String lastSpooledFileName_ = "          ";
  private final byte[] lastDateOpenedBytes_ = new byte[7];
  private String lastDateOpened_ = "       ";
  private final byte[] lastJobSystemNameBytes_ = new byte[10];
  private String lastJobSystemName_ = "          ";
  private final byte[] lastFormTypeBytes_ = new byte[10];
  private String lastFormType_ = "          ";
  private final byte[] lastOutputQueueNameBytes_ = new byte[10];
  private String lastOutputQueueName_ = "          ";
  private final byte[] lastOutputQueueLibraryBytes_ = new byte[10];
  private String lastOutputQueueLibrary_ = "          ";

  private static boolean matches(final byte[] data, final int offset, final byte[] data2)
  {
    for (int i=0; i<data2.length; ++i)
    {
      if (data[offset+i] != data2[i]) return false;
    }
    return true;
  }

  private void getJobName(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastJobNameBytes_))
    {
      System.arraycopy(data, numRead, lastJobNameBytes_, 0, 10);
      lastJobName_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getJobUser(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastJobUserBytes_))
    {
      System.arraycopy(data, numRead, lastJobUserBytes_, 0, 10);
      lastJobUser_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getSpooledFileName(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastSpooledFileNameBytes_))
    {
      System.arraycopy(data, numRead, lastSpooledFileNameBytes_, 0, 10);
      lastSpooledFileName_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getDateOpened(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastDateOpenedBytes_))
    {
      System.arraycopy(data, numRead, lastDateOpenedBytes_, 0, 7);
      lastDateOpened_ = Conv.ebcdicByteArrayToString(data, numRead, 7, charBuffer_);
    }
  }

  private void getJobSystemName(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastJobSystemNameBytes_))
    {
      System.arraycopy(data, numRead, lastJobSystemNameBytes_, 0, 10);
      lastJobSystemName_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getFormType(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastFormTypeBytes_))
    {
      System.arraycopy(data, numRead, lastFormTypeBytes_, 0, 10);
      lastFormType_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getOutputQueueName(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastOutputQueueNameBytes_))
    {
      System.arraycopy(data, numRead, lastOutputQueueNameBytes_, 0, 10);
      lastOutputQueueName_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  private void getOutputQueueLibrary(final byte[] data, final int numRead)
  {
    if (!matches(data, numRead, lastOutputQueueLibraryBytes_))
    {
      System.arraycopy(data, numRead, lastOutputQueueLibraryBytes_, 0, 10);
      lastOutputQueueLibrary_ = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
    }
  }

  public void format(final byte[] data, final int maxLength, final int recordLength, OpenListOfSpooledFilesFormatOSPL0300Listener listener)
  {
    if (listener == null)
    {
      return;
    }

    int numRead = 0;
    while (numRead+136 <= maxLength)
    {
      //String jobName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getJobName(data, numRead);
      String jobName = lastJobName_;
      numRead += 10;
      //String jobUser = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getJobUser(data, numRead);
      String jobUser = lastJobUser_;
      numRead += 10;
      String jobNumber = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      //String spooledFileName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getSpooledFileName(data, numRead);
      String spooledFileName = lastSpooledFileName_;
      numRead += 10;
      int spooledFileNumber = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      int fileStatus = Conv.byteArrayToInt(data, numRead);
      numRead += 4;
      //String dateOpened = Conv.ebcdicByteArrayToString(data, numRead, 7, charBuffer_);
      getDateOpened(data, numRead);
      String dateOpened = lastDateOpened_;
      numRead += 7;
      String timeOpened = Conv.ebcdicByteArrayToString(data, numRead, 6, charBuffer_);
      numRead += 6;
      String spooledFileSchedule = Conv.ebcdicByteArrayToString(data, numRead, 1, charBuffer_);
      numRead += 1;
      //String jobSystemName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getJobSystemName(data, numRead);
      String jobSystemName = lastJobSystemName_;
      numRead += 10;
      String userData = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      numRead += 10;
      //String formType = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getFormType(data, numRead);
      String formType = lastFormType_;
      numRead += 10;
      //String outputQueueName = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getOutputQueueName(data, numRead);
      String outputQueueName = lastOutputQueueName_;
      numRead += 10;
      //String outputQueueLibrary = Conv.ebcdicByteArrayToString(data, numRead, 10, charBuffer_);
      getOutputQueueLibrary(data, numRead);
      String outputQueueLibrary = lastOutputQueueLibrary_;
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
      numRead += 4;
      listener.newSpooledFileEntry(jobName, jobUser, jobNumber, spooledFileName, spooledFileNumber,
                                   fileStatus, dateOpened, timeOpened, spooledFileSchedule,
                                   jobSystemName, userData, formType, outputQueueName, outputQueueLibrary,
                                   asp, totalSize, totalPages, copiesLeftToPrint, priority, ippJobIdentifier);
    }
  }
}




