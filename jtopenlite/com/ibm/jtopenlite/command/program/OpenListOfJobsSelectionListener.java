///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobsSelectionListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

/**
 * @deprecated Use com.ibm.jtopenlite.command.program.workmgmt.OpenListOfJobsSelectionListener instead
 * 
 */
public interface OpenListOfJobsSelectionListener
{
  public String getJobName();

  public String getUserName();

  public String getJobNumber();

  public String getJobType();

  public int getPrimaryJobStatusCount();

  public String getPrimaryJobStatus(int index);

  public int getActiveJobStatusCount();

  public String getActiveJobStatus(int index);

  public int getJobsOnJobQueueStatusCount();

  public String getJobsOnJobQueueStatus(int index);

  public int getJobQueueNameCount();

  public String getJobQueueName(int index);

  public int getCurrentUserProfileCount();

  public String getCurrentUserProfile(int index);

  public int getServerTypeCount();

  public String getServerType(int index);

  public int getActiveSubsystemCount();

  public String getActiveSubsystem(int index);

  public int getMemoryPoolCount();

  public int getMemoryPool(int index);

  public int getJobTypeEnhancedCount();

  public int getJobTypeEnhanced(int index);

  public int getQualifiedJobNameCount();

  public String getQualifiedJobName(int index);
}
