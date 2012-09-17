///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListActiveJobsImpl.java
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
import com.ibm.jtopenlite.command.program.workmgmt.*; 

import java.io.*;

class ListActiveJobsImpl implements OpenListOfJobsFormatOLJB0300Listener, OpenListOfJobsSelectionListener, OpenListOfJobsSortListener, ActiveJobsListener
{
  private final OpenListOfJobsFormatOLJB0300 jobFormat_ = new OpenListOfJobsFormatOLJB0300();
  private final int[] fieldsToReturn_ = new int[] { 1906, 314, 602, 601, 305, 2008, 1802, 312, 1306 }; // Subsystem information, % CPU used, function type, function name, current user profile, thread count, run priority, CPU used total, memory pool name
  private final OpenListOfJobs jobList_ = new OpenListOfJobs(jobFormat_, 120, 1, fieldsToReturn_);
  // private final GetListEntries getJobs_ = new GetListEntries(0, null, 0, 0, 0, jobFormat_);
  // private final CloseList close_ = new CloseList(null);

  private final OpenListHandler handler_ = new OpenListHandler(jobList_, jobFormat_, this);

  private ActiveJobsListener ajListener_;

  private int counter_ = -1;
  private JobInfo[] jobs_;

  ListActiveJobsImpl()
  {
    jobList_.setSelectionListener(this, OpenListOfJobs.SELECTION_OLJS0100);
    jobList_.setSortListener(this);
  }

  public void setActiveJobsListener(ActiveJobsListener listener)
  {
    ajListener_ = listener;
  }

  public long getElapsedTime()
  {
    return jobList_.getElapsedTime();
  }
  public void totalRecords(int totalRecords)
  {
    jobs_ = new JobInfo[totalRecords];
  }

  public boolean stopProcessing()
  {
    return false;
  }

  public void totalRecordsInList(int total)
  {
    ajListener_.totalRecords(total);
  }

  public void openComplete()
  {
    OpenListOfJobsKeyField[] keyDefinitions = jobList_.getKeyFields();
    jobFormat_.setKeyFields(keyDefinitions);
  }


  public synchronized JobInfo[] getJobs(final CommandConnection conn, final boolean reset) throws IOException
  {
    jobs_ = null;
    counter_ = -1;
    jobList_.setResetStatusStatistics(reset);
    handler_.process(conn, 600);
      return jobs_;
    }

  private void sort()
  {
    for (int i=1; i<jobs_.length-1; ++i)
    {
      JobInfo j = jobs_[i];
      if (j.getJobType().equals("SBS"))
      {
        int currentIndex = i;
        // Make sure it's at the top of all the other jobs underneath it.
        JobInfo previous = jobs_[currentIndex-1];
        while (previous.getSubsystem().equals(j.getJobName()))
        {
          jobs_[currentIndex-1] = j;
          jobs_[currentIndex] = previous;
          --currentIndex;
          j = jobs_[currentIndex];
          previous = jobs_[currentIndex-1];
        }
      }
    }
    // Move SYS jobs to the end.
    int marker = -1;
    for (int i=0; marker == -1 && i<jobs_.length; ++i)
    {
      if (!jobs_[i].getJobType().equals("SYS"))
      {
        marker = i;
      }
    }
    while (marker > 0)
    {
      JobInfo saved = jobs_[0];
      for (int i=1; i<jobs_.length; ++i)
      {
        jobs_[i-1] = jobs_[i];
      }
      jobs_[jobs_.length-1] = saved;
      --marker;
    }
  }
  ////////////////////////////////////////
  //
  // Sort methods.
  //
  ////////////////////////////////////////

  public int getNumberOfSortKeys()
  {
    return 2;
  }

  public int getSortKeyFieldStartingPosition(int keyIndex)
  {
    if (keyIndex == 0)
    {
      // Subsystem key.
      return 41;
    }
    return 1; // Job name, user name, job number.
  }

  public int getSortKeyFieldLength(int keyIndex)
  {
    if (keyIndex == 0)
    {
      return 20;
    }
    return 26;
  }

  public int getSortKeyFieldDataType(int keyIndex)
  {
    return 6; // Character with no national language sort sequence applied.
  }

  public boolean isAscending(int keyIndex)
  {
    return true;
  }


  ////////////////////////////////////////
  //
  // Conversion methods.
  //
  ////////////////////////////////////////

  public static String getWRKACTJOBType(String type, String subtype)
  {
    char t = type.charAt(0);
    char s = subtype.charAt(0);
    switch (t)
    {
      case 'A': return "ASJ";
      case 'B':
        switch (s)
        {
          case ' ': return "BCH";
          case 'D': return "BCI";
          case 'E': return "EVK";
          case 'F': return "M36";
          case 'T': return "MRT";
          case 'J': return "PJ ";
          case 'U': return "   ";
        }
        return null;
      case 'I': return "INT";
      case 'W':
        switch (s)
        {
          case 'P': return "PDJ";
          case ' ': return "WTR";
        }
        return null;
      case 'R': return "RDR";
      case 'S':
      case 'X': return "SYS";
      case 'M': return "SBS";
    }
    return null;
  }

  public static String getFunctionPrefix(String data)
  {
    char c = data.charAt(0);
    switch (c)
    {
      case ' ':
      case '\u0000': return "    ";
      case 'C': return "CMD-";
      case 'D': return "DLY-";
      case 'G': return "GRP-";
      case 'I': return "IDX-";
      case 'J': return "JVM-";
      case 'L': return "LOG-"; // LOG - QHST
      case 'M': return "MRT-";
      case 'N': return "MNU-";
      case 'O': return "I/O-";
      case 'P': return "PGM-";
      case 'R': return "PRC-";
      case '*': return "*  -";
    }
    return null;
  }

  public void newJobInfo(JobInfo info, int index)
  {
    jobs_[index] = info;
  }

  public void subsystem(String s, int index)
  {
    jobs_[index].setSubsystem(s);
  }

  public void functionPrefix(String s, int index)
  {
    jobs_[index].setFunctionPrefix(s);
  }

  public void functionName(String s, int index)
  {
    jobs_[index].setFunctionName(s);
  }

  public void currentUser(String s, int index)
  {
    jobs_[index].setCurrentUser(s);
  }

  public void totalCPUUsed(long cpu, int index)
  {
    jobs_[index].setTotalCPUUsed(cpu);
  }

  public void memoryPool(String s, int index)
  {
    jobs_[index].setMemoryPool(s);
  }

  public void cpuPercent(int i, int index)
  {
    jobs_[index].setCPUPercent(i);
  }

  public void threadCount(int i, int index)
  {
    jobs_[index].setThreadCount(i);
  }

  public void runPriority(int i, int index)
  {
    jobs_[index].setRunPriority(i);
  }

  ////////////////////////////////////////
  //
  // List entry format methods.
  //
  ////////////////////////////////////////

  public void newJobEntry(String jobName, String userName, String jobNumber,
                          String status, String type, String subtype)
  {
    String wrkactjobType = getWRKACTJOBType(type, subtype);
    ajListener_.newJobInfo(new JobInfo(jobName, userName, jobNumber, wrkactjobType, status), ++counter_);
  }

  synchronized public void newKeyData(int key, String data, byte[] originalTempData, int originalOffset)
  {
    switch (key)
    {
      case 1906:
        ajListener_.subsystem(data.substring(0,10), counter_);
        break;
      case 602:
        ajListener_.functionPrefix(getFunctionPrefix(data), counter_);
        break;
      case 601:
        ajListener_.functionName(data.charAt(0) == '\u0000' ? "          " : data, counter_);
        break;
      case 305:
        ajListener_.currentUser(data, counter_);
        break;
      case 312:
        ajListener_.totalCPUUsed(Conv.byteArrayToLong(originalTempData, originalOffset), counter_);
        break;
      case 1306:
        ajListener_.memoryPool(data, counter_);
        break;
    }
  }

  public void newKeyData(int key, int data)
  {
    switch (key)
    {
      case 314:
        ajListener_.cpuPercent(data, counter_);
        break;
      case 2008:
        ajListener_.threadCount(data, counter_);
        break;
      case 1802:
        ajListener_.runPriority(data, counter_);
        break;
    }
  }

  ////////////////////////////////////////
  //
  // Selection methods.
  //
  ////////////////////////////////////////

  public String getJobName()
  {
    return "*ALL";
  }

  public String getUserName()
  {
    return "*ALL";
  }

  public String getJobNumber()
  {
    return "*ALL";
  }

  public String getJobType()
  {
    return "*";
  }

  public int getPrimaryJobStatusCount()
  {
    return 0;
  }

  public String getPrimaryJobStatus(int index)
  {
    return null;
  }

  public int getActiveJobStatusCount()
  {
    return 0;
  }

  public String getActiveJobStatus(int index)
  {
    return null;
  }

  public int getJobsOnJobQueueStatusCount()
  {
    return 0;
  }

  public String getJobsOnJobQueueStatus(int index)
  {
    return null;
  }

  public int getJobQueueNameCount()
  {
    return 0;
  }

  public String getJobQueueName(int index)
  {
    return null;
  }

  public int getCurrentUserProfileCount()
  {
    return 0;
  }

  public String getCurrentUserProfile(int index)
  {
    return null;
  }

  public int getServerTypeCount()
  {
    return 0;
  }

  public String getServerType(int index)
  {
    return null;
  }

  public int getActiveSubsystemCount()
  {
    return 0;
  }

  public String getActiveSubsystem(int index)
  {
    return null;
  }

  public int getMemoryPoolCount()
  {
    return 0;
  }

  public int getMemoryPool(int index)
  {
    return 0;
  }

  public int getJobTypeEnhancedCount()
  {
    return 0;
  }

  public int getJobTypeEnhanced(int index)
  {
    return 0;
  }

  public int getQualifiedJobNameCount()
  {
    return 0;
  }

  public String getQualifiedJobName(int index)
  {
    return null;
  }
}
