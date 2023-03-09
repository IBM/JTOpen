///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListJobLogMessages.java
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
 * Represents the information returned by the DSPJOBLOG command, but uses the OpenListOfJobLogMessages classes to obtain it.
**/
public class ListJobLogMessages
{
  private final ListJobLogMessagesImpl impl_ = new ListJobLogMessagesImpl();

  public ListJobLogMessages()
  {
  }

  /**
   * Returns an array of messages, sorted by time, the way DSPJOBLOG does.
   * MessageInfo.toString() prints the message ID and text; MessageInfo.toString2()
   * prints the message details the way F1 on a message does.
   * @param conn The connection to use.
   * @param job The job from which to retrieve log messages.
  **/
  public MessageInfo[] getMessages(final CommandConnection conn, JobInfo job) throws IOException
  {
    return impl_.getMessages(conn, job);
  }
}

