///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DDMCallbackEvent.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ddm;

/**
 * Contains a reference to the file that generated the event.
**/
public class DDMCallbackEvent
{
  public static final int EVENT_WRITE = 0;
  public static final int EVENT_UPDATE = 1;
  public static final int EVENT_READ = 2;

  private DDMFile file_;
  private int type_ = EVENT_WRITE;

  DDMCallbackEvent(DDMFile f)
  {
    file_ = f;
  }

  void setEventType(int i)
  {
    type_ = i;
  }

  /**
   * Returns the type of event.
   * Possible values are {@link #EVENT_WRITE EVENT_WRITE}, {@link #EVENT_UPDATE EVENT_UPDATE}, or
   * {@link #EVENT_READ EVENT_READ}.
  **/
  public int getEventType()
  {
    return type_;
  }

  public DDMFile getFile()
  {
    return file_;
  }
}
