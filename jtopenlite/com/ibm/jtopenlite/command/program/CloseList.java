///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CloseList.java
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
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qgyclst.htm">QGYCLST</a>
**/
public class CloseList extends ProgramAdapter
{
  private byte[] requestHandle_;

  public CloseList(byte[] requestHandle)
  {
    super("QGY", "QGYCLST", 2);

    requestHandle_ = requestHandle;
  }

  void clearOutputData()
  {
  }

  public byte[] getRequestHandle()
  {
    return requestHandle_;
  }

  public void setRequestHandle(byte[] requestHandle)
  {
    requestHandle_ = requestHandle;
  }

  int getParameterInputLengthSubclass(final int parmIndex)
  {
    return 4;
  }

  int getParameterOutputLengthSubclass(final int parmIndex)
  {
    return parmIndex == 1 ? 4 : 0;
  }

  int getParameterTypeSubclass(final int parmIndex)
  {
    return parmIndex == 1 ? Parameter.TYPE_INPUT_OUTPUT : Parameter.TYPE_INPUT;
  }

//  void writeParameterInputDataSubclass(final HostOutputStream out, final int parmIndex) throws IOException
  byte[] getParameterInputDataSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return requestHandle_;
      case 1: return ZERO;
    }
    return null;
  }

//  void readParameterOutputDataSubclass(final HostInputStream in, final int parmIndex, final int maxLength) throws IOException
  void setParameterOutputDataSubclass(final int parmIndex, final byte[] tempData, final int maxLength)
  {
//    in.skipBytes(maxLength);
  }
}

