///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListEntryFormatAdapter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;


public abstract class ListEntryFormatAdapter implements ListEntryFormat
{
//  public final void format(HostInputStream in, final int maxLength, final int recordLength) throws IOException
  public final void format(final byte[] data, final int maxLength, final int recordLength)
  {
    formatSubclass(data, maxLength, recordLength);
  }

//  abstract void formatSubclass(HostInputStream in, int maxLength, int recordLength) throws IOException;
  abstract void formatSubclass(byte[] data, int maxLength, int recordLength);
}

