///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  GetListEntries.java
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
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qgygtle.htm">QGYGTLE</a>
**/
/**
 * 
 * @deprecated Use classes in package jtopnlite.command.program.openlist instead
 *
 */
public class GetListEntries extends ProgramAdapter
{
  private int inputLength_;
  private byte[] requestHandle_;
  private int recordLength_;
  private int numberOfRecordsToReturn_;
  private int startingRecord_;
  private ListEntryFormat formatter_;

  private ListInformation info_;

/*  public GetListEntries(ListInformation prime, int lengthOfReceiverVariable, ListEntryFormat formatter)
  {
    super("QGY", "QGYGTLE", 7);

    requestHandle_ = prime.getRequestHandle();
    recordLength_ = prime.getRecordLength();
    startingRecord_ = 1;
    numberOfRecordsToReturn_ = prime.getTotalRecords();
    inputLength_ = lengthOfReceiverVariable < 8 ? 8 : lengthOfReceiverVariable;
    formatter_ = formatter;
  }
*/
  public GetListEntries(int lengthOfReceiverVariable, byte[] requestHandle, int recordLength, int numberOfRecordsToReturn, int startingRecord, ListEntryFormat formatter)
  {
    super("QGY", "QGYGTLE", 7);

    inputLength_ = lengthOfReceiverVariable < 8 ? 8 : lengthOfReceiverVariable;
    requestHandle_ = requestHandle;
    recordLength_ = recordLength;
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
    startingRecord_ = startingRecord;
    formatter_ = formatter;
  }

  void clearOutputData()
  {
    info_ = null;
  }

  public ListEntryFormat getFormatter()
  {
    return formatter_;
  }

  public void setFormatter(ListEntryFormat formatter)
  {
    formatter_ = formatter;
  }

  public int getLengthOfReceiverVariable()
  {
    return inputLength_;
  }

  public void setLengthOfReceiverVariable(int length)
  {
    inputLength_ = length < 8 ? 8 : length;
  }

  public ListInformation getListInformation()
  {
    return info_;
  }

  public byte[] getRequestHandle()
  {
    return requestHandle_;
  }

  public void setRequestHandle(byte[] requestHandle)
  {
    requestHandle_ = requestHandle;
  }

  public int getRecordLength()
  {
    return recordLength_;
  }

  public void setRecordLength(int recordLength)
  {
    recordLength_ = recordLength;
  }

  public int getNumberOfRecordsToReturn()
  {
    return numberOfRecordsToReturn_;
  }

  public void setNumberOfRecordsToReturn(int numberOfRecordsToReturn)
  {
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
  }

  public int getStartingRecord()
  {
    return startingRecord_;
  }

  public void setStartingRecord(int startingRecord)
  {
    startingRecord_ = startingRecord;
  }

  int getParameterInputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 1:
      case 2:
      case 4:
      case 5:
      case 6:
        return 4;
    }
    return 0;
  }

  int getParameterOutputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 3: return 80;
      case 6: return 4;
    }
    return 0;
  }

  int getParameterTypeSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
      case 3: return Parameter.TYPE_OUTPUT;
      case 6: return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

//  void writeParameterInputDataSubclass(final HostOutputStream out, final int parmIndex) throws IOException
  byte[] getParameterInputDataSubclass(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 2:
        if (requestHandle_.length == 4) return requestHandle_;
        System.arraycopy(requestHandle_, 0, tempData, 0, 4);
        return tempData; //out.write(requestHandle_, 0, 4); break;
      case 4: Conv.intToByteArray(numberOfRecordsToReturn_, tempData, 0); return tempData;
      case 5: Conv.intToByteArray(startingRecord_, tempData, 0); return tempData;
      case 6: return ZERO;
    }
    return null;
  }

//  void readParameterOutputDataSubclass(final HostInputStream in, final int parmIndex, final int maxLength) throws IOException
  void setParameterOutputDataSubclass(final int parmIndex, final byte[] tempData, final int maxLength)
  {
    switch (parmIndex)
    {
      case 0:
        if (formatter_ != null)
        {
          formatter_.format(tempData, maxLength, recordLength_);
        }
//        else
//        {
          //in.skipBytes(maxLength);
//        }
        break;
      case 3:
        if (maxLength < 12)
        {
//          in.skipBytes(maxLength);
          info_ = null;
        }
        else
        {
          info_ = Util.readOpenListInformationParameter(tempData, maxLength);
        }
        break;
      default:
//        in.skipBytes(maxLength);
        break;
    }
  }
}

