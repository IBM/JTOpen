///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveSystemStatusPoolListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public interface RetrieveSystemStatusPoolListener
{
  /**
   * FORMAT_SSTS0300, FORMAT_SSTS0400.
  **/
  public void newPoolInfo(int systemPoolIdentifier, int poolSize, int reservedSize, int maximumActiveThreads,
                          int databaseFaults, int databasePages, int nonDatabaseFaults, int nonDatabasePages,
                          int activeToWait, int waitToIneligible, int activeToIneligible,
                          String poolName, String subsystemName, String subsystemLibrary, String pagingOption);

  /**
   * FORMAT_SSTS0400.
  **/
  public void extraPoolInfo(int systemPoolIdentifier, int definedSize, int currentThreads, int currentIneligibleThreads,
                            int tuningPriority, int tuningMinimumPoolSizePercent, int tuningMaximumPoolSizePercent,
                            int tuningMinimumFaults, int tuningPerThreadFaults, int tuningMaximumFaults,
                            String description, int status, int tuningMinimumActivityLevel, int tuningMaximumActivityLevel);

  /**
   * FORMAT_SSTS0500.
  **/
  public void newSubsystemInfo(int systemPoolIdentifier, String poolName, String subsystemName, String subsystemLibrary);
}
