///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  InputParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

/**
 * Represents a program parameter to be used as input.
**/
public class InputParameter extends Parameter
{
  private byte[] data_;

  /**
   * Constructs a parameter with the provided input data.
  **/
  public InputParameter(byte[] inputData)
  {
    super(TYPE_INPUT);
    data_ = inputData;
  }

  /**
   * Returns the input data.
  **/
  public byte[] getInputData()
  {
    return data_;
  }

  /**
   * Returns the input length.
  **/
  public int getInputLength()
  {
    return data_.length;
  }

  /**
   * Returns the maximum length of this parameter, which is the input length.
  **/
  public int getMaxLength()
  {
    return getInputLength();
  }
}

