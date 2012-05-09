///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfJobsFormatOLJB0200Listener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public interface OpenListOfJobsFormatOLJB0200Listener extends JobKeyDataListener
{
  public void newJobEntry(String jobNameUsed, String userNameUsed, String jobNumberUsed,
                          byte[] internalJobIdentifier, String status, String jobType, String jobSubtype,
                          boolean jobInformationAvailable);
}
