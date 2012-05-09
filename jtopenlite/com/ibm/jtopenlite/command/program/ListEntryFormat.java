///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ListEntryFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;


public interface ListEntryFormat
{
//  void format(HostInputStream in, int maxLength, int recordLength) throws IOException;
  public void format(byte[] data, int maxLength, int recordLength);
}
