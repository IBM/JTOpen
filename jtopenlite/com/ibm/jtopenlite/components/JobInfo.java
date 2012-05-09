///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  JobInfo.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

/**
 * Represents job information returned by the ListActiveJobs class.
 * The toString(), toString2(), and toString3() methods will print
 * the various fields in a format similar to what WRKACTJOB does.
**/
public class JobInfo
{
  private String jobName_;
  private String userName_;
  private String jobNumber_;
  private String jobType_;
  private String status_;
  private String subsystem_;
  private String functionPrefix_;
  private String functionName_;
  private String currentUser_;
  private int cpu_; // Percentage in tenths.
  private int threadCount_;
  private int runPriority_;
  private long totalCPU_; // In milliseconds.
  private String memoryPool_;

  JobInfo(String jobName, String userName, String jobNumber, String jobType, String status)
  {
    jobName_ = jobName;
    userName_ = userName;
    jobNumber_ = jobNumber;
    jobType_ = jobType;
    status_ = status;
  }

  public String getJobName()
  {
    return jobName_;
  }

  public String getUserName()
  {
    return userName_;
  }

  void setCurrentUser(String s)
  {
    currentUser_ = s;
  }

  public String getCurrentUser()
  {
    return currentUser_;
  }

  public String getJobNumber()
  {
    return jobNumber_;
  }

  public String getJobType()
  {
    return jobType_;
  }

  public String getStatus()
  {
    return status_;
  }

  void setSubsystem(String s)
  {
    subsystem_ = s;
  }

  public String getSubsystem()
  {
    return subsystem_;
  }

  void setFunctionPrefix(String s)
  {
    functionPrefix_ = s;
  }

  void setFunctionName(String s)
  {
    functionName_ = s;
  }

  public String getFunction()
  {
    return functionPrefix_+functionName_;
  }

  void setCPUPercent(int data)
  {
    cpu_ = data;
  }

  public float getCPUPercent()
  {
    return (cpu_)/10.0f;
  }

  void setThreadCount(int i)
  {
    threadCount_ = i;
  }

  public int getThreadCount()
  {
    return threadCount_;
  }

  void setRunPriority(int i)
  {
    runPriority_ = i;
  }

  public int getRunPriority()
  {
    return runPriority_;
  }

  void setTotalCPUUsed(long total)
  {
    totalCPU_ = total;
  }

  public long getTotalCPUUsed()
  {
    return totalCPU_;
  }

  void setMemoryPool(String s)
  {
    memoryPool_ = s;
  }

  public String getMemoryPool()
  {
    return memoryPool_;
  }

  public int getMemoryPoolID()
  {
    if (memoryPool_.equals("*BASE     "))
    {
      return 2; // Always.
    }
    if (memoryPool_.equals("*INTERACT "))
    {
      return 4; // Always??
    }
    if (memoryPool_.equals("*SPOOL    "))
    {
      return 3; // Always??
    }
    if (memoryPool_.equals("*MACHINE  "))
    {
      return 1; // Always.
    }
    return 0; // Unknown.
  }

  private String getCPUString()
  {
    int num = cpu_/10;
    int dec = cpu_ % 10;
    String cpu = num == 0 ? "   ." : (num+".");
    cpu = cpu + dec;
    while (cpu.length() < 5) cpu = " "+cpu;
    return cpu;
  }

  public String toString()
  {
    boolean isSubsystem = jobType_.equals("SBS") || jobType_.equals("SYS");
    String cpu = getCPUString();
    return (isSubsystem ? "" : "  ")+jobName_+(isSubsystem ? "   " : " ")+currentUser_+" "+jobType_+" "+cpu+" "+getFunction()+" "+status_;
  }

  private String getTotalCPUString()
  {
    int total = (int)(totalCPU_/100L);
    int num = total/10;
    int dec = total % 10;
    String cpu = num == 0 ? "      ." : (num+".");
    cpu = cpu + dec;
    while (cpu.length() < 8) cpu = " "+cpu;
    return cpu;
  }

  public String toString2()
  {
    boolean isSubsystem = jobType_.equals("SBS") || jobType_.equals("SYS");
    String cpu = getTotalCPUString();
    return (isSubsystem ? "" : "  ")+jobName_+(isSubsystem ? "   " : " ")+jobType_+" "+getMemoryPoolID()+" "+(runPriority_ < 10 ? " " : "")+runPriority_+" "+cpu;
  }

  private String getThreadString()
  {
    String s = String.valueOf(threadCount_);
    while (s.length() < 8) s = " "+s;
    return s;
  }

  public String toString3()
  {
    boolean isSubsystem = jobType_.equals("SBS") || jobType_.equals("SYS");
    String cpu = getCPUString();
    String thread = getThreadString();
    return (isSubsystem ? "" : "  ")+jobName_+(isSubsystem ? "   " : " ")+userName_+" "+jobNumber_+" "+jobType_+" "+cpu+" "+thread;
  }
}
