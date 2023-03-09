///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: OpenListOfSpooledFilesFilterOSPF0200Listener.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.print;

public interface OpenListOfSpooledFilesFilterOSPF0200Listener extends OpenListOfSpooledFilesFilterOSPF0100Listener
{
  public String getSystemName();

  public String getStartingSpooledFileCreateDate();

  public String getStartingSpooledFileCreateTime();

  public String getEndingSpooledFileCreateDate();

  public String getEndingSpooledFileCreateTime();
}
