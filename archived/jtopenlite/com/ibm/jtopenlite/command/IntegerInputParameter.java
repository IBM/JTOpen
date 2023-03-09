///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  IntegerInputParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

import com.ibm.jtopenlite.*;

/**
 * A specific kind of program parameter that represents a 4-byte integer value used as input.
**/
public class IntegerInputParameter extends InputParameter
{
  /**
   * Constructs a parameter using the provided value as the input data.
  **/
  public IntegerInputParameter(int val)
  {
    super(Conv.intToByteArray(val));
  }
}
