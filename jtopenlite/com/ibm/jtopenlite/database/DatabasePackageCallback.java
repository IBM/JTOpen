///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DatabasePackageCallback.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.database;

public interface DatabasePackageCallback
{
  public void newPackageInfo(int ccsid, String defaultCollection, int numStatements);

  public void newStatementInfo(int statementIndex, int needsDefaultCollection, int type, String name);

  public void statementText(int statementIndex, String text);

  public void statementDataFormat(int statementIndex, byte[] format);

  public void statementParameterMarkerFormat(int statementIndex, byte[] format);
}
