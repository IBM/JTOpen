///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfSpooledFilesFormatOSPL0300Listener.java
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
 * @deprecated Use classes in package jtopnlite.command.program.print instead
 *
 */
public interface OpenListOfSpooledFilesFormatOSPL0300Listener
{
  public void newSpooledFileEntry(String jobName, String jobUser, String jobNumber, String spooledFileName,
                                  int spooledFileNumber, int fileStatus, String dateOpened, String timeOpened,
                                  String spooledFileSchedule, String jobSystemName, String userData,
                                  String formType, String outputQueueName, String outputQueueLibrary,
                                  int auxiliaryStoragePool, long size, int totalPages, int copiesLeftToPrint,
                                  String priority, int internetPrintProtocolJobIdentifier);
}
