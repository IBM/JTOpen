///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DiskStatus.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.components;

/**
 * Order of operations:
 * <ul>
 * <li>totalRecords()</li>
 * <li>start loop</li>
 * <ul>
 * <li>newJobInfo()</li>
 * <li>other various setters</li>
 * </ul>
 * <li>end loop</li>
 * </ul>
**/
public interface ActiveJobsListener
{
  public void totalRecords(int total);

  public void newJobInfo(JobInfo info, int index);

  public void subsystem(String s, int index);

  public void functionPrefix(String s, int index);

  public void functionName(String s, int index);

  public void currentUser(String s, int index);

  public void totalCPUUsed(long cpu, int index);

  public void memoryPool(String s, int index);

  public void cpuPercent(int i, int index);

  public void threadCount(int i, int index);

  public void runPriority(int i, int index);
}



