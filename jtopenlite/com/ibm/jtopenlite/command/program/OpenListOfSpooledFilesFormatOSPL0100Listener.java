///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfSpooledFilesFormatOSPL0100Listener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public interface OpenListOfSpooledFilesFormatOSPL0100Listener
{
  public void newSpooledFileEntry(String spooledFileName, String jobName, String jobUser, String jobNumber,
                                  int spooledFileNumber, int totalPages, int currentPage, int copiesLeftToPrint,
                                  String outputQueueName, String outputQueueLibrary, String userData,
                                  String status, String formType, String priority,
                                  byte[] internalJobIdentifier, byte[] internalSpooledFileIdentifier,
                                  String deviceType, String jobSystemName, String dateOpened, String timeOpened);
}
