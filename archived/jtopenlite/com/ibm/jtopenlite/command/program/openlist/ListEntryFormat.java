///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: ListEntryFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.openlist;



/**
 * Responsible for formatting output data returned by a call to an OpenListProgram.
**/
public interface ListEntryFormat<T extends ListFormatListener>
{
  public void format(byte[] data, int maxLength, int recordLength, T listener);
}
