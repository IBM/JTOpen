///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  NullParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

/**
 * Represents a program parameter that is null.
**/
public class NullParameter extends Parameter
{
  /**
   * The instance to use when you need to specify a null program parameter.
  **/
  public static final NullParameter INSTANCE = new NullParameter();

  private NullParameter()
  {
    super(TYPE_NULL);
  }
}

