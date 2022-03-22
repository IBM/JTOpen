///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: CloseList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.openlist;


import com.ibm.jtopenlite.command.*;


/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qgyclst.htm">QGYCLST</a>
**/
public class CloseList implements Program
{
  private static final byte[] ZERO = new byte[4];
  private final byte[] tempData_ = new byte[10];

  private byte[] requestHandle_;

  public CloseList()
  {
  }

  public CloseList(byte[] requestHandle)
  {
//    super("QGY", "QGYCLST", 2);

    requestHandle_ = requestHandle;
  }

  public byte[] getTempDataBuffer()
  {
    return tempData_;
  }

  public String getProgramName()
  {
    return "QGYCLST";
  }

  public String getProgramLibrary()
  {
    return "QGY";
  }

  public int getNumberOfParameters()
  {
    return 2;
  }

  public void newCall()
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

  public int getParameterInputLength(final int parmIndex)
  {
    return 4;
  }

  public int getParameterOutputLength(final int parmIndex)
  {
    return parmIndex == 1 ? 4 : 0;
  }

  public int getParameterType(final int parmIndex)
  {
    return parmIndex == 1 ? Parameter.TYPE_INPUT_OUTPUT : Parameter.TYPE_INPUT;
  }

//  void writeParameterInputDataSubclass(final HostOutputStream out, final int parmIndex) throws IOException
  public byte[] getParameterInputData(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return requestHandle_;
      case 1: return ZERO;
    }
    return null;
  }

//  void readParameterOutputDataSubclass(final HostInputStream in, final int parmIndex, final int maxLength) throws IOException
  public void setParameterOutputData(final int parmIndex, final byte[] tempData, final int maxLength)
  {
//    in.skipBytes(maxLength);
  }
}

