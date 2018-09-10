///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  StringInputParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

import java.io.*;

import com.ibm.jtopenlite.Conv;

/**
 * A specific kind of program parameter that represents a CCSID 37 String value as input.
**/
public class StringInputParameter extends InputParameter
{
  /**
   * Constructs a parameter using the provided value as the input data.
  **/
  public StringInputParameter(String s) throws IOException
  {
    super(Conv.stringToEBCDICByteArray(s, 37));
    // super(s.getBytes("Cp037"));
  }
}
