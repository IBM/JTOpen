///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListDiskStatuses.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.components;

import com.ibm.jtopenlite.command.*;
import com.ibm.jtopenlite.ddm.*;
import java.io.*;

/**
 * Represents the information returned by the WRKDSKSTS command.
**/
public class ListDiskStatuses
{
  private final ListDiskStatusesImpl impl_ = new ListDiskStatusesImpl();

  public ListDiskStatuses()
  {
  }

  public String getElapsedTime()
  {
    return impl_.getElapsedTime();
  }

  public DiskStatus[] getDiskStatuses(final CommandConnection cmdConn, final DDMConnection ddmConn, String workingLibrary) throws IOException
  {
    return impl_.getDiskStatuses(cmdConn, ddmConn, workingLibrary);
  }
}

