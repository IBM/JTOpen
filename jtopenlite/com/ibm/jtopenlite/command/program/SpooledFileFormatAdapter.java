///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  SpooledFileFormatAdapter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

public abstract class SpooledFileFormatAdapter extends ListEntryFormatAdapter implements OpenListOfSpooledFilesFormat
{
  public final String getName()
  {
    return getNameSubclass();
  }

  abstract String getNameSubclass();

  public final int getType()
  {
    return getTypeSubclass();
  }

  abstract int getTypeSubclass();
}

