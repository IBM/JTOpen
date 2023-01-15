///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfObjectsAuthorityListener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.object;

public interface OpenListOfObjectsAuthorityListener
{
  public int getCallLevel();

  public int getNumberOfObjectAuthorities();

  public int getNumberOfLibraryAuthorities();

  public String getObjectAuthority(int index);

  public String getLibraryAuthority(int index);
}
