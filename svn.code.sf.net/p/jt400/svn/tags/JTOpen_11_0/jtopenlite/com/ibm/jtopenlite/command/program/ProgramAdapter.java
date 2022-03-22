///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  ProgramAdapter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.command.*;

/**
 * Base class for all program call classes in this package.
**/
public abstract class ProgramAdapter implements Program
{
  static final byte[] ZERO = new byte[4];

  private String library_;
  private String name_;
  private int numParms_;

  private byte[] tempData_;

  protected ProgramAdapter(String library, String name, int numberOfParameters)
  {
    library_ = library;
    name_ = name;
    numParms_ = numberOfParameters;
  }

  public final void newCall()
  {
    clearOutputData();
  }

  abstract void clearOutputData();

  public final String getProgramLibrary()
  {
    return library_;
  }

  public final String getProgramName()
  {
    return name_;
  }

  public final int getNumberOfParameters()
  {
    return getNumberOfParametersSubclass();
  }

  int getNumberOfParametersSubclass()
  {
    return numParms_;
  }

  public final int getParameterType(int parmIndex)
  {
    return getParameterTypeSubclass(parmIndex);
  }

  abstract int getParameterTypeSubclass(int parmIndex);

  public final int getParameterInputLength(int parmIndex)
  {
    return getParameterInputLengthSubclass(parmIndex);
  }

  abstract int getParameterInputLengthSubclass(int parmIndex);

  public final int getParameterOutputLength(int parmIndex)
  {
    return getParameterOutputLengthSubclass(parmIndex);
  }

  abstract int getParameterOutputLengthSubclass(int parmIndex);

//  public final void writeParameterInputData(HostOutputStream out, int parmIndex) throws IOException
//  {
//    writeParameterInputDataSubclass(out, parmIndex);
//  }

  public final byte[] getParameterInputData(int parmIndex)
  {
    return getParameterInputDataSubclass(parmIndex);
  }

//  abstract void writeParameterInputDataSubclass(HostOutputStream out, int parmIndex) throws IOException;

  abstract byte[] getParameterInputDataSubclass(int parmIndex);

//  public final void readParameterOutputData(HostInputStream in, int parmIndex, int maxLength) throws IOException
//  {
//    readParameterOutputDataSubclass(in, parmIndex, maxLength);
//  }

  public final void setParameterOutputData(int parmIndex, byte[] tempData, int maxLength)
  {
    setParameterOutputDataSubclass(parmIndex, tempData, maxLength);
  }

//  abstract void readParameterOutputDataSubclass(HostInputStream in, int parmIndex, int maxLength) throws IOException;

  abstract void setParameterOutputDataSubclass(int parmIndex, byte[] tempData, int maxLength);

  public final byte[] getTempDataBuffer()
  {
    int maxSize = 0;
    for (int i=0; i<getNumberOfParameters(); ++i)
    {
      int len = getParameterOutputLength(i);
      if (len > maxSize) maxSize = len;
      len = getParameterInputLength(i);
      if (len > maxSize) maxSize = len;
    }
    if (tempData_ == null || tempData_.length < maxSize)
    {
      tempData_ = new byte[maxSize];
    }
    return tempData_;
  }
}

