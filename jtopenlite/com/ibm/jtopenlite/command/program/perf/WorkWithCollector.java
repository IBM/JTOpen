///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename: WorkWithCollector.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program.perf;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;


/**
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/QPMWKCOL.htm">QPMWKCOL</a>
**/
public class WorkWithCollector implements Program
{
  private static final byte[] ZERO = new byte[4];

  public static final String ACTION_START = "*START";
  public static final String ACTION_END = "*END";
  public static final String ACTION_CHANGE = "*CHANGE";

  public static final String RESOURCE_JOB = "*JOB";
  public static final String RESOURCE_POOL = "*POOL";
  public static final String RESOURCE_DISK = "*DISK";
  public static final String RESOURCE_IOP = "*IOP";
  public static final String RESOURCE_COMM = "*COMM";

  public static final int COLLECT_15 = 15;
  public static final int COLLECT_30 = 30;
  public static final int COLLECT_60 = 60;
  public static final int COLLECT_120 = 120;
  public static final int COLLECT_240 = 240;

  private String typeOfActionToPerform_;
  private String typeOfResource_;
  private int timeBetweenCollections_;
  private String userSpaceName_;
  private String userSpaceLibrary_;

  private int firstSequenceNumber_ = -1;

  private final byte[] tempData_ = new byte[20];

  public WorkWithCollector(String typeOfActionToPerform, String typeOfResource,
                           int timeBetweenCollections, String userSpaceName, String userSpaceLibrary)
  {
    typeOfActionToPerform_ = typeOfActionToPerform;
    typeOfResource_ = typeOfResource;
    timeBetweenCollections_ = timeBetweenCollections;
    userSpaceName_ = userSpaceName;
    userSpaceLibrary_ = userSpaceLibrary;
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }

  public String getProgramName()
  {
    return "QPMWKCOL";
  }

  public void newCall()
  {
    firstSequenceNumber_ = -1;
  }

  public void setTypeOfActionToPerform(String action)
  {
    typeOfActionToPerform_ = action;
  }

  public String getTypeOfActionToPerform()
  {
    return typeOfActionToPerform_;
  }

  public void setTypeOfResource(String resource)
  {
    typeOfResource_ = resource;
  }

  public String getTypeOfResource()
  {
    return typeOfResource_;
  }

  public void setTimeBetweenCollections(int seconds)
  {
    timeBetweenCollections_ = seconds;
  }

  public int getTimeBetweenCollections()
  {
    return timeBetweenCollections_;
  }

  public void setUserSpaceName(String userSpace)
  {
    userSpaceName_ = userSpace;
  }

  public String getUserSpaceName()
  {
    return userSpaceName_;
  }

  public void setUserSpaceLibrary(String library)
  {
    userSpaceLibrary_ = library;
  }

  public String getUserSpaceLibrary()
  {
    return userSpaceLibrary_;
  }

  public int getFirstSequenceNumber()
  {
    return firstSequenceNumber_;
  }

  public int getNumberOfParameters()
  {
    return 6;
  }

  public int getParameterInputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 10;
      case 1: return 10;
      case 2: return 4;
      case 3: return 20;
      case 5: return 4;
    }
    return 0;
  }

  public int getParameterOutputLength(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 4: return 4;
      case 5: return 4;
    }
    return 0;
  }

  public int getParameterType(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 4: return Parameter.TYPE_OUTPUT;
      case 5: return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  public byte[] getTempDataBuffer()
  {
    return tempData_;
  }

  public byte[] getParameterInputData(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: Conv.stringToBlankPadEBCDICByteArray(typeOfActionToPerform_, tempData_, 0, 10); return tempData_;
      case 1: Conv.stringToBlankPadEBCDICByteArray(typeOfResource_, tempData_, 0, 10); return tempData_;
      case 2: Conv.intToByteArray(timeBetweenCollections_, tempData_, 0); return tempData_;
      case 3:
        Conv.stringToBlankPadEBCDICByteArray(userSpaceName_, tempData_, 0, 10);
        Conv.stringToBlankPadEBCDICByteArray(userSpaceLibrary_, tempData_, 10, 10);
        return tempData_;
      case 5: return ZERO;
    }
    return null;
  }

  public void setParameterOutputData(final int parmIndex, final byte[] data, final int maxLength)
  {
    switch (parmIndex)
    {
      case 4:
        firstSequenceNumber_ = Conv.byteArrayToInt(data, 0);
        break;
      default:
        break;
    }
  }
}

