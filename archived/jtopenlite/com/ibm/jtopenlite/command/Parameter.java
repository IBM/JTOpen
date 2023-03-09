///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  Parameter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command;

import com.ibm.jtopenlite.*;
import java.io.*;

/**
 * Abstract parent class for all program parameters.
**/
public abstract class Parameter
{
  /**
   * Constant representing a null parameter.
  **/
  public static final int TYPE_NULL = 0;

  /**
   * Constant representing an input parameter.
  **/
  public static final int TYPE_INPUT = 1;

  /**
   * Constant representing an output parameter.
  **/
  public static final int TYPE_OUTPUT = 2;

  /**
   * Constant representing an input/output parameter.
  **/
  public static final int TYPE_INPUT_OUTPUT = 3;

  private int type_;

  protected Parameter(int type)
  {
    type_ = type;
  }

  /**
   * Returns the input data for this parameter.
   * The default is null.
  **/
  public byte[] getInputData()
  {
    return null;
  }

  /**
   * Returns the input length of this parameter.
   * The default is 0.
  **/
  public int getInputLength()
  {
    return 0;
  }

  /**
   * Returns the output length of this parameter.
   * The default is 0.
  **/
  public int getOutputLength()
  {
    return 0;
  }

  /**
   * Returns the maximum length of this parameter.
   * The default is 0.
  **/
  public int getMaxLength()
  {
    return 0;
  }

  protected void setOutputData(byte[] data)
  {
  }

  /**
   * Returns the output data for this parameter.
   * The default is null.
  **/
  public byte[] getOutputData()
  {
    return null;
  }

  /**
   * Returns true if the type of this parameter is input or input/output.
  **/
  public boolean isInput()
  {
    return type_ == TYPE_INPUT || type_ == TYPE_INPUT_OUTPUT;
  }

  /**
   * Returns true if the type of this parameter is output or input/output.
  **/
  public boolean isOutput()
  {
    return type_ == TYPE_OUTPUT || type_ == TYPE_INPUT_OUTPUT;
  }

  /**
   * Returns the type of this parameter.
  **/
  public int getType()
  {
    return type_;
  }

  /**
   * Convenience method to retrieve the 4-byte integer value in the output data at the specified offset.
  **/
  public int parseInt(int offset)
  {
    return Conv.byteArrayToInt(getOutputData(), offset);
  }

  /**
   * Convenience method to retrieve the CCSID 37 String in the output data at the specified offset and length.
  **/
  public String parseString(int offset, int length) throws IOException
  {
    // return new String(getOutputData(), offset, length, "Cp037");
    return Conv.ebcdicByteArrayToString(getOutputData(), offset, length);
  }
}

