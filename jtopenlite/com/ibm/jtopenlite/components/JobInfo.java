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

import java.util.*;

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

  public static void arrangeBySubsystem(JobInfo[] jobs)
  {
    Arrays.sort(jobs, comparator_);
/*    for (int i=1; i<jobs.length-1; ++i)
    {
      JobInfo j = jobs[i];
      if (j.getJobType().equals("SBS"))
      {
        int currentIndex = i;
        // Make sure it's at the top of all the other jobs underneath it.
        JobInfo previous = jobs[currentIndex-1];
        while (previous.getSubsystem().equals(j.getJobName()))
        {
          jobs[currentIndex-1] = j;
          jobs[currentIndex] = previous;
          --currentIndex;
          j = jobs[currentIndex];
          previous = jobs[currentIndex-1];
        }
      }
    }
    // Move SYS jobs to the end.
    int marker = -1;
    for (int i=0; marker == -1 && i<jobs.length; ++i)
    {
      if (!jobs[i].getJobType().equals("SYS"))
      {
        marker = i;
      }
    }
    while (marker > 0)
    {
      JobInfo saved = jobs[0];
      for (int i=1; i<jobs.length; ++i)
      {
        jobs[i-1] = jobs[i];
      }
      jobs[jobs.length-1] = saved;
      --marker;
    }
*/
  }

  private static final boolean isDigit(final char c)
  {
    return (c <= '9' && c >= '0');
  }

  private static final int compareStrings(String s1, String s2)
  {
    for (int i=0; i<s1.length() && i<s2.length(); ++i)
    {
      char c1 = s1.charAt(i);
      char c2 = s2.charAt(i);
      if (c1 != c2)
      {
        // The iSeries sorts digits after letters, for some reason.
        if (isDigit(c1))
        {
          if (isDigit(c2))
          {
            return c1-c2;
          }
          return 1;
        }
        if (isDigit(c2))
        {
          return -1;
        }
        return c1-c2;
      }
    }
    return s1.length()-s2.length();
  }


  private static final Comparator<JobInfo> comparator_ = new Comparator<JobInfo>()
  {
    public int compare(JobInfo j1, JobInfo j2)
    {
      String n1 = j1.getJobName().trim();
      String n2 = j2.getJobName().trim();
      String t1 = j1.getJobType().trim();
      String t2 = j2.getJobType().trim();
      String s1 = j1.getSubsystem();
      String s2 = j2.getSubsystem();
      s1 = (s1 == null ? "" : s1.trim());
      s2 = (s2 == null ? "" : s2.trim());
      if (t1.equals("SBS"))
      {
        if (t2.equals("SBS")) return compareStrings(n1,n2); //n1.compareTo(n2);
        if (t2.equals("SYS")) return -1;
        if (n1.equals(s2)) return -1;
        return compareStrings(n1,s2); //n1.compareTo(s2);
      }
      if (t2.equals("SBS"))
      {
        if (t1.equals("SYS")) return 1;
        if (n2.equals(s1)) return 1;
        return compareStrings(s1,n2); //s1.compareTo(n2);
      }
      if (t1.equals("SYS") && t2.equals("SYS"))
      {
        return compareStrings(n1,n2); //n1.compareTo(n2);
      }
      if (t1.equals("SYS"))
      {
        return 1;
      }
      if (t2.equals("SYS"))
      {
        return -1;
      }
      if (s1.equals(s2))
      {
        return compareStrings(n1,n2); //n1.compareTo(n2);
      }
      return compareStrings(s1,s2); //s1.compareTo(s2);
    }
  };

  public static void arrangeBySubsystem(List<JobInfo> jobs)
  {
    Collections.sort(jobs, comparator_);


/*    for (int i=1; i<jobs.size()-1; ++i)
    {
      JobInfo j = jobs.get(i);
      if (j.getJobType().equals("SBS"))
      {
        int currentIndex = i;
        // Make sure it's at the top of all the other jobs underneath it.
        JobInfo previous = jobs.get(currentIndex-1);
        while (previous.getSubsystem().equals(j.getJobName()))
        {
          jobs.set(currentIndex-1, j);
          jobs.set(currentIndex, previous);
          --currentIndex;
          j = jobs.get(currentIndex);
          previous = jobs.get(currentIndex-1);
        }
      }
    }
    // Move SYS jobs to the end.
    int marker = -1;
    for (int i=0; marker == -1 && i<jobs.size(); ++i)
    {
      if (!jobs.get(i).getJobType().equals("SYS"))
      {
        marker = i;
      }
    }
    while (marker > 0)
    {
      JobInfo saved = jobs.get(0);
      for (int i=1; i<jobs.size(); ++i)
      {
        jobs.set(i-1, jobs.get(i));
      }
      jobs.set(jobs.size()-1, saved);
      --marker;
    }
*/
  }

  public String getJobName()
  {
    return jobName_;
  }

  public String getUserName()
  {
    return userName_;
  }

  public void setCurrentUser(String s)
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

  public void setSubsystem(String s)
  {
    subsystem_ = s;
  }

  public String getSubsystem()
  {
    return subsystem_;
  }

  public void setFunctionPrefix(String s)
  {
    functionPrefix_ = (s == null ? "" : s);
  }

  public void setFunctionName(String s)
  {
    functionName_ = (s == null ? "" : s);
  }

  public String getFunction()
  {
    return functionPrefix_+functionName_;
  }

  public void setCPUPercent(int data)
  {
    cpu_ = data;
  }

  public int getCPUPercentInTenths()
  {
    return cpu_;
  }

  public float getCPUPercent()
  {
    return ((float)cpu_)/10.0f;
  }

  public void setThreadCount(int i)
  {
    threadCount_ = i;
  }

  public int getThreadCount()
  {
    return threadCount_;
  }

  public void setRunPriority(int i)
  {
    runPriority_ = i;
  }

  public int getRunPriority()
  {
    return runPriority_;
  }

  public void setTotalCPUUsed(long total)
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
    if (memoryPool_ == null) return 0;

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
