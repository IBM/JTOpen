///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobsFormatOLJB0300Listener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public interface OpenListOfJobsFormatOLJB0300Listener extends JobKeyDataListener
{
  public void newJobEntry(String jobNameUsed, String userNameUsed, String jobNumberUsed,
                          String activeJobStatus, String jobType, String jobSubtype);
}
