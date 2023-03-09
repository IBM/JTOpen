///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  InputOutputParameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

/**
 * Represents a program parameter to be used as both input and output.
**/
public class InputOutputParameter extends Parameter
{
  private int outputLength_;
  private byte[] inputData_;
  private byte[] outputData_;

  /**
   * Constructs a parameter with the provided input data and output length.
  **/
  public InputOutputParameter(byte[] inputData, int outputLength)
  {
    super(TYPE_INPUT_OUTPUT);
    inputData_ = inputData;
    outputLength_ = outputLength;
  }

  /**
   * Returns the input data.
  **/
  public byte[] getInputData()
  {
    return inputData_;
  }

  /**
   * Returns the input length.
  **/
  public int getInputLength()
  {
    return inputData_.length;
  }

  /**
   * Returns the output length.
  **/
  public int getOutputLength()
  {
    return outputLength_;
  }

  /**
   * Returns the maximum of the output length and input length of this parameter.
  **/
  public int getMaxLength()
  {
    return outputLength_ > inputData_.length ? outputLength_ : inputData_.length;
  }

  protected void setOutputData(byte[] data)
  {
    outputData_ = data;
  }

  /**
   * Returns the output data.
  **/
  public byte[] getOutputData()
  {
    return outputData_;
  }
}
