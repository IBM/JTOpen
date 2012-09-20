///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  MessageInfoListener.java
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
 * <li>newMessageInfo()</li>
 * <li>replyStatus() and/or messageText()</li>
 * </ul>
 * <li>end loop</li>
 * </ul>
**/
public interface MessageInfoListener
{
  public void totalRecords(int total);

  public void newMessageInfo(MessageInfo info, int index);

  public void replyStatus(String status, int index);

  public void messageText(String text, int index);

  /**
   * Return true to indicate you no longer want to process further messages.
  **/
  public boolean done();
}



