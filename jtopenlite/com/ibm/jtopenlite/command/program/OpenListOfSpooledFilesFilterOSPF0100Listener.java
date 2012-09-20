///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfSpooledFilesFilterOSPF0100Listener.java
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
public interface OpenListOfSpooledFilesFilterOSPF0100Listener extends OpenListOfSpooledFilesFilterListener
{
  public int getNumberOfUserNames();

  public String getUserName(int index);

  public int getNumberOfOutputQueues();

  public String getOutputQueueName(int index);

  public String getOutputQueueLibrary(int index);

  public String getFormType();

  public String getUserSpecifiedData();

  public int getNumberOfStatuses();

  public String getStatus(int index);

  public int getNumberOfPrinterDevices();

  public String getPrinterDevice(int index);
}
