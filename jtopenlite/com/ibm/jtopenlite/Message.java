///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Message.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite;

/**
 * Represents a System i message.
**/
public class Message
{
  private String id_;
  private String text_;
  private int severity_;

  /**
   * Constructs a new message with the provided ID and message text.
  **/
  public Message(String id, String text)
  {
    id_ = id;
    text_ = text;
  }

  /**
   * Constructs a new message with the provided ID, message text, and severity.
  **/
  public Message(String id, String text, int severity)
  {
    id_ = id;
    text_ = text;
    severity_ = severity;
  }

  /**
   * Returns the severity of this message, or 0 if unknown.
  **/
  public int getSeverity()
  {
    return severity_;
  }

  /**
   * Returns the message ID of this message.
  **/
  public String getID()
  {
    return id_;
  }

  /**
   * Returns the message text of this message.
  **/
  public String getText()
  {
    return text_;
  }

  /**
   * Returns a String representation of this message which consists of the message ID and message text.
  **/
  public String toString()
  {
    return id_+": "+text_;
  }
}
