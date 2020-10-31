///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListProgram.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.openlist;

import com.ibm.jtopenlite.command.*;

/**
 * Any of the various QGY APIs on the system that process lists of data.
**/
public interface OpenListProgram<T extends ListEntryFormat, W extends ListFormatListener> extends Program
{
  public ListInformation getListInformation();

  /**
   * The format listener gets called by the formatter once the output data has been formatted.
  **/
  public W getFormatListener();

  public void setFormatListener(W listener);

  /**
   * The formatter is the class that handles formatting the output data for each entry in the list.
  **/
  public T getFormatter();

  public void setFormatter(T format);
}
