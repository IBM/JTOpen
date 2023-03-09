///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabaseWarningCallback.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

public interface DatabaseWarningCallback
{
  public void newWarning(int rcClass, int rcClassReturnCode);

  public void noWarnings();

  public void newMessageID(String id);

  public void newMessageText(String text);

  public void newSecondLevelText(String text);
}
