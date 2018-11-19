///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  RetrieveUserSpace.java
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
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qusrtvus.htm">QUSRTVUS</a>
**/
public class RetrieveUserSpace implements Program
{


  private final byte[] tempData_ = new byte[20];

  private String userSpaceName_;
  private String userSpaceLibrary_;
  private int startingPosition_;
  private int lengthOfData_;

  private byte[] contents_;

  public RetrieveUserSpace(String userSpaceName, String userSpaceLibrary, int startingPosition, int lengthOfData)
  {
    userSpaceName_ = userSpaceName;
    userSpaceLibrary_ = userSpaceLibrary;
    startingPosition_ = startingPosition;
    lengthOfData_ = lengthOfData;
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

  public int getStartingPosition()
  {
    return startingPosition_;
  }

  public void setStartingPosition(int pos)
  {
    startingPosition_ = pos;
  }

  public int getLengthOfData()
  {
    return lengthOfData_;
  }

  public void setLengthOfData(int len)
  {
    lengthOfData_ = len;
  }

  public byte[] getContents()
  {
    return contents_;
  }

  public void newCall()
  {
    contents_ = null;
  }

  public int getNumberOfParameters()
  {
    return 4;
  }

  public int getParameterInputLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 20;
      case 1: return 4;
      case 2: return 4;
    }
    return 0;
  }

  public int getParameterOutputLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 3: return lengthOfData_;
    }
    return 0;
  }

  public int getParameterType(int parmIndex)
  {
    return (parmIndex == 3 ? Parameter.TYPE_OUTPUT : Parameter.TYPE_INPUT);
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
        Conv.intToByteArray(startingPosition_, tempData_, 0);
        break;
      case 2:
        Conv.intToByteArray(lengthOfData_, tempData_, 0);
        break;
    }
    return tempData_;
  }


  public byte[] getTempDataBuffer()
  {
    return tempData_;
  }

  public void setParameterOutputData(int parmIndex, byte[] tempData, int maxLength)
  {
    contents_ = new byte[lengthOfData_];
    System.arraycopy(tempData, 0, contents_, 0, lengthOfData_);
  }

  public String getProgramName()
  {
    return "QUSRTVUS";
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }
}

