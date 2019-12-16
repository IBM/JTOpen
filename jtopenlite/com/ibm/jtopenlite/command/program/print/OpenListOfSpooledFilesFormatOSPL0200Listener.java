///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfSpooledFilesFormatOSPL0200Listener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.print;

public interface OpenListOfSpooledFilesFormatOSPL0200Listener extends OpenListOfSpooledFilesFormatListener
{
  public void newSpooledFileEntry(String spooledFileName, String jobName, String jobUser, String jobNumber,
                                  int spooledFileNumber, int totalPages, int currentPage, int copiesLeftToPrint,
                                  String outputQueueName, String outputQueueLibrary, String userData,
                                  String status, String formType, String priority,
                                  byte[] internalJobIdentifier, byte[] internalSpooledFileIdentifier,
                                  String deviceType, String dateOpened, String timeOpened,
                                  String printerAssigned, String printerName, String jobSystemName);
}
