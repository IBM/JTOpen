///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OutputParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

/**
 * Represents a program parameter to be used as output.
**/
public class OutputParameter extends Parameter
{
  private int length_;
  private byte[] data_;

  /**
   * Constructs a parameter with the specified output length.
  **/
  public OutputParameter(int outputLength)
  {
    super(TYPE_OUTPUT);
    length_ = outputLength;
  }

  /**
   * Returns the output length.
  **/
  public int getOutputLength()
  {
    return length_;
  }

  /**
   * Returns the maximum length of this parameter, which is the output length.
  **/
  public int getMaxLength()
  {
    return getOutputLength();
  }

  protected void setOutputData(byte[] data)
  {
    data_ = data;
  }

  /**
   * Returns the output data.
  **/
  public byte[] getOutputData()
  {
    return data_;
  }
}
