///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfObjectsSelectionListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

/**
 * 
 * @deprecated Use classes in package jtopnlite.command.program.object instead
 *
 */
public interface OpenListOfObjectsSelectionListener
{
  /**
   * Whether the statuses are for selecting objects or omitting objects from the list.
  **/
  public boolean isSelected();

  public int getNumberOfStatuses();

  public String getStatus(int index);
}

