///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListQSYSOPRMessages.java
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
 * Represents the information returned by the DSPMSG QSYSOPR command, but uses the OpenListOfMessages classes to obtain it.
**/
public class ListQSYSOPRMessages
{
  private final ListQSYSOPRMessagesImpl impl_ = new ListQSYSOPRMessagesImpl();

  public ListQSYSOPRMessages()
  {
  }

  /**
   * Returns an array of messages, sorted by time, the way DSPMSG does.
   * MessageInfo.toString() prints the message ID and text; MessageInfo.toString2()
   * prints the message details the way F1 on a message does.
   * @param conn The connection to use.
  **/
  public MessageInfo[] getMessages(final CommandConnection conn) throws IOException
  {
    impl_.setMessageInfoListener(impl_);
    return impl_.getMessages(conn);
  }

  public void getMessages(final CommandConnection conn, final MessageInfoListener miListener) throws IOException
  {
    impl_.setMessageInfoListener(miListener);
    impl_.getMessages(conn);
  }
}

