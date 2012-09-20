///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListSpooledFilesImpl.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;
import com.ibm.jtopenlite.command.program.openlist.*;
import com.ibm.jtopenlite.command.program.print.*;
import java.io.*;

class ListSpooledFilesImpl implements OpenListOfSpooledFilesFormatOSPL0300Listener, OpenListOfSpooledFilesFilterOSPF0100Listener, SpooledFileInfoListener
{
  private final OpenListOfSpooledFilesFormatOSPL0300 spooledFileFormat_ = new OpenListOfSpooledFilesFormatOSPL0300();
  private final OpenListOfSpooledFiles spooledFileList_ = new OpenListOfSpooledFiles(spooledFileFormat_, 1000, 1, null, this, null, null, null);
  private final OpenListHandler handler_ = new OpenListHandler(spooledFileList_, spooledFileFormat_, this);

  private SpooledFileInfoListener sfiListener_;
  private int counter_ = -1;
  private SpooledFileInfo[] spooledFiles_;
  private final char[] charBuffer_ = new char[4096];

  private String userName_;

  public ListSpooledFilesImpl()
  {
  }

  public void setSpooledFileInfoListener(SpooledFileInfoListener listener)
  {
    sfiListener_ = listener;
  }

  public void openComplete()
  {
  }

  public void totalRecordsInList(int total)
  {
    sfiListener_.totalRecords(total);
  }

  public SpooledFileInfo[] getSpooledFiles(final CommandConnection conn, String user) throws IOException
  {
    spooledFiles_ = null;
    counter_ = -1;
    userName_ = user;
    handler_.process(conn, 1000);
    return spooledFiles_;
  }

  public boolean stopProcessing()
  {
    return false;
  }

  public void totalRecords(int totalRecords)
  {
    spooledFiles_ = new SpooledFileInfo[totalRecords];
  }

  public void newSpooledFileInfo(SpooledFileInfo info, int index)
  {
    spooledFiles_[index] = info;
  }

  // OSPL0300 listener.

  public void newSpooledFileEntry(String jobName, String jobUser, String jobNumber, String spooledFileName,
                                  int spooledFileNumber, int fileStatus, String dateOpened, String timeOpened,
                                  String spooledFileSchedule, String jobSystemName, String userData,
                                  String formType, String outputQueueName, String outputQueueLibrary,
                                  int auxiliaryStoragePool, long size, int totalPages, int copiesLeftToPrint,
                                  String priority, int internetPrintProtocolJobIdentifier)
  {
    sfiListener_.newSpooledFileInfo(new SpooledFileInfo(jobName, jobUser, jobNumber, spooledFileName,
                                                    spooledFileNumber, fileStatus, dateOpened, timeOpened,
                                                    userData, formType, outputQueueName, outputQueueLibrary,
                                                    auxiliaryStoragePool, size, totalPages, copiesLeftToPrint, priority),
                                    ++counter_);
  }


  // Filter listener.

  public int getNumberOfUserNames()
  {
    return 1;
  }

  public String getUserName(int index)
  {
    return userName_ == null ? "*CURRENT" : userName_;
  }

  public int getNumberOfOutputQueues()
  {
    return 1;
  }

  public String getOutputQueueName(int index)
  {
    return "*ALL";
  }

  public String getOutputQueueLibrary(int index)
  {
    return "";
  }

  public String getFormType()
  {
    return null;
  }

  public String getUserSpecifiedData()
  {
    return null;
  }

  public int getNumberOfStatuses()
  {
    return 1;
  }

  public String getStatus(int index)
  {
    return "*ALL";
  }

  public int getNumberOfPrinterDevices()
  {
    return 1;
  }

  public String getPrinterDevice(int index)
  {
    return "*ALL";
  }
}