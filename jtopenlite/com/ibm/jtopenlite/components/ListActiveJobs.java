///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListActiveJobs.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.command.*;
import java.io.*;

/**
 * Represents the information returned by the WRKACTJOB command, but uses the OpenListOfJobs classes to obtain it.
**/
public class ListActiveJobs
{
  private final ListActiveJobsImpl impl_ = new ListActiveJobsImpl();

  public ListActiveJobs()
  {
  }

  /**
   * Returns the elapsed time since job statistics were reset, in milliseconds.
  **/
  public long getElapsedTime()
  {
    return impl_.getElapsedTime();
  }

  /**
   * Returns an array of active jobs, sorted by subsystem and job name, the way WRKACTJOB does.
   * JobInfo.toString() prints the fields the way WRKACTJOB does.
   * @param conn The connection to use.
   * @param reset Indicates if the job statistics should be reset on this invocation, like F10 in WRKACTJOB does.
  **/
  public JobInfo[] getJobs(final CommandConnection conn, final boolean reset) throws IOException
  {
    return impl_.getJobs(conn, reset);
  }
}

