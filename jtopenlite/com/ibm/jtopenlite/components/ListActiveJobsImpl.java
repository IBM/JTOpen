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
import com.ibm.jtopenlite.command.program.*;
import java.io.*;

class ListActiveJobsImpl implements OpenListOfJobsFormatOLJB0300Listener, OpenListOfJobsSelectionListener, OpenListOfJobsSortListener
{
  private final OpenListOfJobsFormatOLJB0300 jobFormat_ = new OpenListOfJobsFormatOLJB0300();
  private final int[] fieldsToReturn_ = new int[] { 1906, 314, 602, 601, 305, 2008, 1802, 312, 1306 }; // Subsystem information, % CPU used, function type, function name, current user profile, thread count, run priority, CPU used total, memory pool name
  private final OpenListOfJobs jobList_ = new OpenListOfJobs(jobFormat_, 120, 1, fieldsToReturn_);
  private final GetListEntries getJobs_ = new GetListEntries(0, null, 0, 0, 0, jobFormat_);
  private final CloseList close_ = new CloseList(null);

  private int counter_ = -1;
  private JobInfo[] jobs_;

  ListActiveJobsImpl()
  {
    jobList_.setSelectionListener(this, OpenListOfJobs.SELECTION_OLJS0100);
    jobList_.setSortListener(this);
  }

  public long getElapsedTime()
  {
    return jobList_.getElapsedTime();
  }

  public synchronized JobInfo[] getJobs(final CommandConnection conn, final boolean reset) throws IOException
  {
    jobFormat_.setListener(null);

    int receiverSize = 120; // This should be large enough for the initial call.
    int numRecordsToReturn = 1; // Need to return at least one record to get the key definition back.
    jobList_.setResetStatusStatistics(reset);
    CommandResult result = conn.call(jobList_);
    if (!result.succeeded())
    {
      throw new IOException("Job list failed: "+result.toString());
    }

    ListInformation listInfo = jobList_.getListInformation();
    byte[] requestHandle = listInfo.getRequestHandle();
    close_.setRequestHandle(requestHandle);

    try
    {
      OpenListOfJobsKeyField[] keyDefinitions = jobList_.getKeyFields();
      int recordLength = listInfo.getRecordLength();
      // Now, the list is building on the server.
      // Call GetListEntries once to wait for the list to finish building, for example.
      receiverSize = 100; // Should be good enough for the first call.
      numRecordsToReturn = 0; // For some reason, specifying 0 here does not wait until the whole list is built.
      int startingRecord = -1; // Wait until whole list is built before returning.
      getJobs_.setLengthOfReceiverVariable(receiverSize);
      getJobs_.setRequestHandle(requestHandle);
      getJobs_.setRecordLength(recordLength);
      getJobs_.setNumberOfRecordsToReturn(numRecordsToReturn);
      getJobs_.setStartingRecord(startingRecord);
      result = conn.call(getJobs_);
      if (!result.succeeded())
      {
        throw new IOException("Get jobs failed: "+result.toString());
      }

      listInfo = getJobs_.getListInformation();
      int totalRecords = listInfo.getTotalRecords();
      jobs_ = new JobInfo[totalRecords];
      counter_ = -1;

      // Now retrieve each job record in chunks of 300 at a time.
      numRecordsToReturn = 600;
      receiverSize = recordLength * numRecordsToReturn;
      startingRecord = 1;
      getJobs_.setLengthOfReceiverVariable(receiverSize);
      getJobs_.setNumberOfRecordsToReturn(numRecordsToReturn);
      getJobs_.setStartingRecord(startingRecord);
      jobFormat_.setKeyFields(keyDefinitions);
      jobFormat_.setListener(this); // Ready to process.
      while (startingRecord <= totalRecords)
      {
        result = conn.call(getJobs_);
        if (!result.succeeded())
        {
          throw new IOException("Get jobs failed: "+result.toString());
        }
        // Assuming it succeeded...
        listInfo = getJobs_.getListInformation();
        startingRecord += listInfo.getRecordsReturned();
        getJobs_.setStartingRecord(startingRecord);
      }
      sort();
      return jobs_;
    }
    finally
    {
      // All done.
      conn.call(close_);
    }
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

  ////////////////////////////////////////
  //
  // List entry format methods.
  //
  ////////////////////////////////////////

  public void newJobEntry(String jobName, String userName, String jobNumber,
                          String status, String type, String subtype)
  {
    String wrkactjobType = getWRKACTJOBType(type, subtype);
    jobs_[++counter_] = new JobInfo(jobName, userName, jobNumber, wrkactjobType, status);
  }

  synchronized public void newKeyData(int key, String data, byte[] originalTempData, int originalOffset)
  {
    switch (key)
    {
      case 1906:
        jobs_[counter_].setSubsystem(data.substring(0,10));
        break;
      case 602:
        jobs_[counter_].setFunctionPrefix(getFunctionPrefix(data));
        break;
      case 601:
        jobs_[counter_].setFunctionName(data.charAt(0) == '\u0000' ? "          " : data);
        break;
      case 305:
        jobs_[counter_].setCurrentUser(data);
        break;
      case 312:
        jobs_[counter_].setTotalCPUUsed(Conv.byteArrayToLong(originalTempData, originalOffset));
        break;
      case 1306:
        jobs_[counter_].setMemoryPool(data);
        break;
    }
  }

  public void newKeyData(int key, int data)
  {
    switch (key)
    {
      case 314:
        jobs_[counter_].setCPUPercent(data);
        break;
      case 2008:
        jobs_[counter_].setThreadCount(data);
        break;
      case 1802:
        jobs_[counter_].setRunPriority(data);
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
