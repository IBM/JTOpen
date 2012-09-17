///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CallServiceProgramParameterFormat.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program;

public interface CallServiceProgramParameterFormat
{
  public static final int PARAMETER_FORMAT_BY_VALUE = 1;
  public static final int PARAMETER_FORMAT_BY_REFERENCE = 2;

  public int getParameterCount();

  public int getParameterLength(int index);

  public int getParameterFormat(int index);

  public void fillInputData(int index, byte[] dataBuffer, int offset);

  public void setOutputData(int index, byte[] dataBuffer, int offset);
}

