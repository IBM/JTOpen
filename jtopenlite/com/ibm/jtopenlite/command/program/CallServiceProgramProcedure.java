///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  CallServiceProgramProcedure
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////
package com.ibm.jtopenlite.command.program;

import com.ibm.jtopenlite.*;
import com.ibm.jtopenlite.command.*;


/**
 * Service program call - <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qzruclsp.htm">QZRUCLSP</a>
 * This class fully implements the V5R4 specification of QZRUCLSP.
**/
public class CallServiceProgramProcedure implements Program
{
  public static final int RETURN_VALUE_FORMAT_NONE = 0;
  public static final int RETURN_VALUE_FORMAT_INTEGER = 1;
  public static final int RETURN_VALUE_FORMAT_POINTER = 2;
  public static final int RETURN_VALUE_FORMAT_INTEGER_AND_ERROR_NUMBER = 3;

  private static final byte[] ZERO = new byte[8];

  private String serviceProgramName_;
  private String serviceProgramLibrary_;
  private String exportName_;
  private int returnValueFormat_;
  private CallServiceProgramParameterFormat parameterFormat_;

  private byte[] tempData_;

  private int returnValueInteger_;
  private byte[] returnValuePointer_;
  private int returnValueErrno_;

  public CallServiceProgramProcedure()
  {
  }

  public CallServiceProgramProcedure(String serviceProgramName, String serviceProgramLibrary,
                                     String exportName, int returnValueFormat)
  {
    serviceProgramName_ = serviceProgramName;
    serviceProgramLibrary_ = serviceProgramLibrary;
    exportName_ = exportName;
    returnValueFormat_ = returnValueFormat;
  }

  public String getServiceProgramName()
  {
    return serviceProgramName_;
  }

  public void setServiceProgramName(String name)
  {
    serviceProgramName_ = name;
  }

  public String getServiceProgramLibrary()
  {
    return serviceProgramLibrary_;
  }

  public void setServiceProgramLibrary(String lib)
  {
    serviceProgramLibrary_ = lib;
  }

  public String getExportName()
  {
    return exportName_;
  }

  public void setExportName(String name)
  {
    exportName_ = name;
  }

  public int getReturnValueFormat()
  {
    return returnValueFormat_;
  }

  public void setReturnValueFormat(int retval)
  {
    returnValueFormat_ = retval;
  }

  public CallServiceProgramParameterFormat getParameterFormat()
  {
    return parameterFormat_;
  }

  public void setParameterFormat(CallServiceProgramParameterFormat format)
  {
    parameterFormat_ = format;
  }

  public String getProgramName()
  {
    return "QZRUCLSP";
  }

  public String getProgramLibrary()
  {
    return "QSYS";
  }

  public int getNumberOfParameters()
  {
    return 7 + parameterFormat_.getParameterCount();
  }

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

  public void newCall()
  {
    returnValueInteger_ = 0;
    returnValuePointer_ = null;
    returnValueErrno_ = 0;
  }

  public int getReturnValueInteger()
  {
    return returnValueInteger_;
  }

  public int getReturnValueErrorNumber()
  {
    return returnValueErrno_;
  }

  public byte[] getReturnValuePointer()
  {
    return returnValuePointer_;
  }

  public int getParameterInputLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 20;
      case 1: return exportName_.length()+1;
      case 2: return 4;
      case 3: return parameterFormat_.getParameterCount()*4;
      case 4: return 4;
      case 5:
        // Align to 16-byte boundary for those programs that need it.
        int total = 20; // Service program.
        total += exportName_.length() + 1; // Export name.
        total += 4; // Return value format.
        total += (parameterFormat_.getParameterCount()*4);
        total += 4; // Number of parameters.
        total += ZERO.length;
        int val = 0;
        switch (returnValueFormat_)
        {
          case 0:
            val = 4;
            break;
          case 1:
            val = 4;
            break;
          case 2:
            val = 16;
            break;
          case 3:
            val = 8;
            break;
        }
        total += val;
        int mod = total % 16;
        int extra = (mod == 0 ? 0 : 16-mod);
        return ZERO.length+extra;

      case 7: return parameterFormat_.getParameterLength(0);
      case 8: return parameterFormat_.getParameterLength(1);
      case 9: return parameterFormat_.getParameterLength(2);
      case 10: return parameterFormat_.getParameterLength(3);
      case 11: return parameterFormat_.getParameterLength(4);
      case 12: return parameterFormat_.getParameterLength(5);
      case 13: return parameterFormat_.getParameterLength(6);
    }
    return 0;
  }

  public int getParameterOutputLength(int parmIndex)
  {
    switch (parmIndex)
    {
      case 5: return getParameterInputLength(parmIndex);
      case 6:
        switch (returnValueFormat_)
        {
          case 0: return 4; // Ignored.
          case 1: return 4; // Integer.
          case 2: return 16; // Pointer.
          case 3: return 8; // Integer + ERRNO.
        }
      case 7: return parameterFormat_.getParameterLength(0);
      case 8: return parameterFormat_.getParameterLength(1);
      case 9: return parameterFormat_.getParameterLength(2);
      case 10: return parameterFormat_.getParameterLength(3);
      case 11: return parameterFormat_.getParameterLength(4);
      case 12: return parameterFormat_.getParameterLength(5);
      case 13: return parameterFormat_.getParameterLength(6);
    }
    return 0;
  }

  public int getParameterType(int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
        return Parameter.TYPE_INPUT;
      case 6:
        return Parameter.TYPE_OUTPUT;
    }
    return Parameter.TYPE_INPUT_OUTPUT;
  }

  public byte[] getParameterInputData(int parmIndex)
  {
    byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 0:
        Conv.stringToBlankPadEBCDICByteArray(serviceProgramName_, tempData, 0, 10);
        Conv.stringToBlankPadEBCDICByteArray(serviceProgramLibrary_, tempData, 10, 10);
        break;
      case 1:
        int len = Conv.stringToEBCDICByteArray37(exportName_, tempData, 0);
        tempData[len] = 0;
        break;
      case 2:
        Conv.intToByteArray(returnValueFormat_, tempData, 0);
        break;
      case 3:
        for (int i=0; i<parameterFormat_.getParameterCount(); ++i)
        {
          Conv.intToByteArray(parameterFormat_.getParameterFormat(i), tempData, i*4);
        }
        break;
      case 4:
        Conv.intToByteArray(parameterFormat_.getParameterCount(), tempData, 0);
        break;
      case 5:
        for (int i=0; i<16; ++i) tempData[i] = 0;
        break;
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
        parameterFormat_.fillInputData(parmIndex-7, tempData, 0);
        break;
    }
    return tempData;
  }


  public void setParameterOutputData(int parmIndex, byte[] tempData, int maxLength)
  {
    switch (parmIndex)
    {
      case 6:
        switch (returnValueFormat_)
        {
          case 0: // Ignore.
            break;
          case 1: // Integer.
            returnValueInteger_ = Conv.byteArrayToInt(tempData, 0);
            break;
          case 2: // Pointer.
            returnValuePointer_ = new byte[16];
            System.arraycopy(tempData, 0, returnValuePointer_, 0, 16);
            break;
          case 3: // Integer + ERRNO.
            returnValueInteger_ = Conv.byteArrayToInt(tempData, 0);
            returnValueErrno_ = Conv.byteArrayToInt(tempData, 4);
            break;
        }
        break;
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
        parameterFormat_.setOutputData(parmIndex-7, tempData, 0);
        break;
    }
  }
}
