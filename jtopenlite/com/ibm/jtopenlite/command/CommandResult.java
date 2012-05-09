///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CommandResult.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

import com.ibm.jtopenlite.*;

/**
 * The result of calling a program or command.
 * @see CommandConnection#call
 * @see CommandConnection#execute
**/
public class CommandResult
{
  private boolean success_;
  private Message[] messages_;
  private int rc_;

  CommandResult(boolean success, Message[] messages, int rc)
  {
    success_ = success;
    messages_ = messages;
    rc_ = rc;
  }

  /**
   * Returns true if the call was successful.
  **/
  public boolean succeeded()
  {
    return success_;
  }

  /**
   * Returns the return code from the call, if any.
  **/
  public int getReturnCode()
  {
    return rc_;
  }

  /**
   * Returns any messages that were issued during the call.
  **/
  public Message[] getMessages()
  {
    return messages_;
  }

  public String toString()
  {
    // Use string buffer to improve performance 
    StringBuffer s = new StringBuffer(""+success_);
    s.append("; rc=0x"); 
    s.append(Integer.toHexString(rc_));
    if (messages_ != null)
    {
      for (int i=0; i<messages_.length; ++i)
      {
        s.append("\n");
        s.append(messages_[i].toString());
      }
    }
    return s.toString();
  }
}
