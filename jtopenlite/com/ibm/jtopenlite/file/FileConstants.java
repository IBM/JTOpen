///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  FileConstants.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.file;

public final class FileConstants
{
  private FileConstants()
  {
  }

  public static final int RC_FILE_NOT_FOUND = 2;
  public static final int RC_ACCESS_DENIED = 13;

  public static String returnCodeToString(int rc)
  {
    switch (rc)
    {
      case RC_FILE_NOT_FOUND: return "File not found";
      case RC_ACCESS_DENIED: return "Access denied";
    }
    return "Unknown return code";
  }
}
