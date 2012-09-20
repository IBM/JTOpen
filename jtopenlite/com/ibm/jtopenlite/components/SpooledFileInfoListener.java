///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  SpooledFileInfoListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.components;

/**
 * Order of operations:
 * <ul>
 * <li>totalRecords()</li>
 * <li>start loop</li>
 * <ul>
 * <li>newSpooledFileInfo()</li>
 * </ul>
 * <li>end loop</li>
 * </ul>
**/
public interface SpooledFileInfoListener
{
  public void totalRecords(int total);

  public void newSpooledFileInfo(SpooledFileInfo info, int index);
}



