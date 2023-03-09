///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  DeleteUserSpace.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.object;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;


/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qusdltus.htm">QUSDLTUS</a>
**/
public class DeleteUserSpace implements Program
{
  private static final byte[] ZERO = new byte[4];

  private final byte[] tempData_ = new byte[20];

  private String userSpaceName_;
  private String userSpaceLibrary_;

  public DeleteUserSpace(String userSpaceName, String userSpaceLibrary)
  {
    userSpaceName_ = userSpaceName;
    userSpaceLibrary_ = userSpaceLibrary;
  }

  public String getUserSpaceName()
  {
    return userSpaceName_;
  }

  public void setUserSpaceName(String name)
  {
    userSpaceName_ = name;
  }

  public String getUserSpaceLibrary()
  {
    return userSpaceLibrary_;
  }

  public void setUserSpaceLibrary(String lib)
  {
    userSpaceLibrary_ = lib;
  }

  public void newCall()
  {
  }

  public int getNumberOfParameters()
  {
    return 2;
  }

  public int getParameterInputLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 20;
      case 1: return 4;
    }
    return 0;
  }

  public int getParameterOutputLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 1: return 4;
    }
    return 0;
  }

  public int getParameterType(int parmIndex)
  {
    return (parmIndex == 1 ? Parameter.TYPE_INPUT_OUTPUT : Parameter.TYPE_INPUT);
  }

  public byte[] getParameterInputData(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
        Conv.stringToBlankPadEBCDICByteArray(userSpaceName_, tempData_, 0, 10);
        Conv.stringToBlankPadEBCDICByteArray(userSpaceLibrary_, tempData_, 10, 10);
        break;
      case 1:
        return ZERO;
    }
    return tempData_;
  }


  public byte[] getTempDataBuffer()
  {
    return tempData_;
  }

  public void setParameterOutputData(int parmIndex, byte[] tempData, int maxLength)
  {
  }

  public String getProgramName()
  {
    return "QUSDLTUS";
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }
}

