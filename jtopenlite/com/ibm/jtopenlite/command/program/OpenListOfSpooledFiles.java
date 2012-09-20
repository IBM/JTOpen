///////////////////////////////////////////////////////////////////////////////
//
// JTOpenLite
//
// Filename:  OpenListOfSpooledFiles.java
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
 * <a href="http://publib.boulder.ibm.com/infocenter/iseries/v5r4/topic/apis/qgyolspl.htm">QGYOLSPL</a>
 * This class fully implements the V5R4 specification of QGYOLSPL.
**/
/**
 * 
 * @deprecated Use classes in package jtopnlite.command.program.print instead
 *
 */
public class OpenListOfSpooledFiles extends ProgramAdapter
{
  private OpenListOfSpooledFilesFormat format_;
  private int inputLength_;
  private int numberOfRecordsToReturn_;
  private SortListener sortListener_;
  private String jobName_;
  private String jobUser_;
  private String jobNumber_;
  private OpenListOfSpooledFilesFilterListener filterListener_;
  private ListInformation info_;

  public OpenListOfSpooledFiles(OpenListOfSpooledFilesFormat format, int lengthOfReceiverVariable, int numberOfRecordsToReturn,
                                SortListener sortInformation,
                                OpenListOfSpooledFilesFilterListener filterInformation,
                                String jobName, String jobUser, String jobNumber)
  {
    super("QGY", "QGYOLSPL", 10);
    format_ = format;
    inputLength_ = lengthOfReceiverVariable <= 0 ? 1 : lengthOfReceiverVariable;
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
    sortListener_ = sortInformation;
    filterListener_ = filterInformation;
    jobName_ = jobName;
    jobUser_ = jobUser;
    jobNumber_ = jobNumber;
  }

  void clearOutputData()
  {
    info_ = null;
  }

  public void setJobName(String name)
  {
    jobName_ = name;
  }

  public String getJobName()
  {
    return jobName_;
  }

  public void setJobUser(String user)
  {
    jobUser_ = user;
  }

  public String getJobUser()
  {
    return jobUser_;
  }

  public void setJobNumber(String number)
  {
    jobNumber_ = number;
  }

  public String getJobNumber()
  {
    return jobNumber_;
  }

  public int getNumberOfRecordsToReturn()
  {
    return numberOfRecordsToReturn_;
  }

  public void setNumberOfRecordsToReturn(int numberOfRecordsToReturn)
  {
    numberOfRecordsToReturn_ = numberOfRecordsToReturn;
  }

  public SortListener getSortListener()
  {
    return sortListener_;
  }

  public void setSortListener(SortListener listener)
  {
    sortListener_ = listener;
  }

  public OpenListOfSpooledFilesFilterListener getFilterListener()
  {
    return filterListener_;
  }

  public void setFilterListener(OpenListOfSpooledFilesFilterListener listener)
  {
    filterListener_ = listener;
  }

  public ListInformation getListInformation()
  {
    return info_;
  }

  int getParameterInputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return 0;
      case 1: return 4;
      case 2: return 0;
      case 3: return 4;
      case 4: return sortListener_ == null ? 4 : 4 + (sortListener_.getNumberOfSortKeys()*12);
      case 5:
        if (filterListener_ != null)
        {
          if (filterListener_ instanceof OpenListOfSpooledFilesFilterOSPF0200Listener)
          {
            OpenListOfSpooledFilesFilterOSPF0200Listener listener = (OpenListOfSpooledFilesFilterOSPF0200Listener)filterListener_;
            return 110 + (listener.getNumberOfUserNames()*10) +
                         (listener.getNumberOfOutputQueues()*20) +
                         (listener.getNumberOfStatuses()*10) +
                         (listener.getNumberOfPrinterDevices()*10);
          }
          else if (filterListener_ instanceof OpenListOfSpooledFilesFilterOSPF0100Listener)
          {
            OpenListOfSpooledFilesFilterOSPF0100Listener listener = (OpenListOfSpooledFilesFilterOSPF0100Listener)filterListener_;
            return 36 + (listener.getNumberOfUserNames()*12) +
                        (listener.getNumberOfOutputQueues()*20) +
                        (listener.getNumberOfStatuses()*12) +
                        (listener.getNumberOfPrinterDevices()*12);
          }
        }
        return 0; //TODO
         // return 92;
      case 6: return 26;
      case 7: return 8;
      case 8: return 4;
      case 9: return 8;
    }
    return 0;
  }

  int getParameterOutputLengthSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0: return inputLength_;
      case 2: return 80;
      case 8: return 4;
    }
    return 0;
  }

  int getParameterTypeSubclass(final int parmIndex)
  {
    switch (parmIndex)
    {
      case 0:
      case 2:
        return Parameter.TYPE_OUTPUT;
      case 8:
        return Parameter.TYPE_INPUT_OUTPUT;
    }
    return Parameter.TYPE_INPUT;
  }

  byte[] getParameterInputDataSubclass(final int parmIndex)
  {
    final byte[] tempData = getTempDataBuffer();
    switch (parmIndex)
    {
      case 1: Conv.intToByteArray(inputLength_, tempData, 0); return tempData;
      case 3: Conv.intToByteArray(numberOfRecordsToReturn_, tempData, 0); return tempData;
      case 4:
        if (sortListener_ == null)
        {
          Conv.intToByteArray(0, tempData, 0); return tempData;
        }
        else
        {
          final int numberOfKeys = sortListener_.getNumberOfSortKeys();
          Conv.intToByteArray(numberOfKeys, tempData, 0);
          int offset = 4;
          for (int i=0; i<numberOfKeys; ++i)
          {
            Conv.intToByteArray(sortListener_.getSortKeyFieldStartingPosition(i), tempData, offset);
            Conv.intToByteArray(sortListener_.getSortKeyFieldLength(i), tempData, offset+4);
            Conv.shortToByteArray(sortListener_.getSortKeyFieldDataType(i), tempData, offset+8);
            tempData[offset+10] = sortListener_.isAscending(i) ? (byte)0xF1 : (byte)0xF2;
            tempData[offset+11] = 0;
            offset += 12;
          }
        }
        return tempData;
      case 5:
        if (filterListener_ != null && filterListener_ instanceof OpenListOfSpooledFilesFilterOSPF0200Listener)
        {
          OpenListOfSpooledFilesFilterOSPF0200Listener listener = (OpenListOfSpooledFilesFilterOSPF0200Listener)filterListener_;
          int offset = 110;
          Conv.intToByteArray(getParameterInputLength(5), tempData, 0);
          final int numUsers = listener.getNumberOfUserNames();
          final int offsetToUsers = numUsers > 0 ? offset : 0;
          Conv.intToByteArray(offsetToUsers, tempData, 4);
          Conv.intToByteArray(numUsers, tempData, 8);
          Conv.intToByteArray(10, tempData, 12);
          offset += numUsers*10;
          final int numOutputQueues = listener.getNumberOfOutputQueues();
          final int offsetToOutputQueues = numOutputQueues > 0 ? offset : 0;
          Conv.intToByteArray(offsetToOutputQueues, tempData, 16);
          Conv.intToByteArray(numOutputQueues, tempData, 20);
          Conv.intToByteArray(20, tempData, 24);
          offset += numOutputQueues*20;
          final int numStatuses = listener.getNumberOfStatuses();
          final int offsetToStatuses = numStatuses > 0 ? offset : 0;
          Conv.intToByteArray(offsetToStatuses, tempData, 28);
          Conv.intToByteArray(numStatuses, tempData, 32);
          Conv.intToByteArray(10, tempData, 36);
          offset += numStatuses*10;
          final int numPrinterDevices = listener.getNumberOfPrinterDevices();
          final int offsetToPrinterDevices = numPrinterDevices > 0 ? offset : 0;
          Conv.intToByteArray(offsetToPrinterDevices, tempData, 40);
          Conv.intToByteArray(numPrinterDevices, tempData, 44);
          Conv.intToByteArray(10, tempData, 48);
          final String formType = listener.getFormType();
          Conv.stringToBlankPadEBCDICByteArray(formType == null ? "*ALL" : formType, tempData, 52, 10);
          final String userSpecifiedData = listener.getUserSpecifiedData();
          Conv.stringToBlankPadEBCDICByteArray(userSpecifiedData == null ? "*ALL" : userSpecifiedData, tempData, 62, 10);
          final String systemName = listener.getSystemName();
          Conv.stringToBlankPadEBCDICByteArray(systemName == null ? "*ALL" : systemName, tempData, 72, 8);
          final String startDate = listener.getStartingSpooledFileCreateDate();
          Conv.stringToBlankPadEBCDICByteArray(startDate == null ? "*ALL" : startDate, tempData, 80, 7);
          final String startTime = listener.getStartingSpooledFileCreateTime();
          Conv.stringToBlankPadEBCDICByteArray(startTime == null ? "" : startTime, tempData, 87, 6);
          final String endDate = listener.getEndingSpooledFileCreateDate();
          Conv.stringToBlankPadEBCDICByteArray(endDate == null ? "" : endDate, tempData, 93, 7);
          final String endTime = listener.getEndingSpooledFileCreateTime();
          Conv.stringToBlankPadEBCDICByteArray(endTime == null ? "" : endTime, tempData, 100, 6);
          Conv.intToByteArray(0, tempData, 106);
          offset = 110;
          for (int i=0; i<numUsers; ++i)
          {
            String user = listener.getUserName(i);
            Conv.stringToBlankPadEBCDICByteArray(user == null ? "" : user, tempData, offset, 10);
            offset += 10;
          }
          for (int i=0; i<numOutputQueues; ++i)
          {
            String name = listener.getOutputQueueName(i);
            String lib = listener.getOutputQueueLibrary(i);
            Conv.stringToBlankPadEBCDICByteArray(name == null ? "" : name, tempData, offset, 10);
            offset += 10;
            Conv.stringToBlankPadEBCDICByteArray(lib == null ? "" : lib, tempData, offset, 10);
            offset += 10;
          }
          for (int i=0; i<numStatuses; ++i)
          {
            String status = listener.getStatus(i);
            Conv.stringToBlankPadEBCDICByteArray(status == null ? "" : status, tempData, offset, 10);
            offset += 10;
          }
          for (int i=0; i<numPrinterDevices; ++i)
          {
            String dev = listener.getPrinterDevice(i);
            Conv.stringToBlankPadEBCDICByteArray(dev == null ? "" : dev, tempData, offset, 10);
            offset += 10;
          }
        }
        else
        {
          OpenListOfSpooledFilesFilterOSPF0100Listener listener = (OpenListOfSpooledFilesFilterOSPF0100Listener)filterListener_;
          int offset = 0;
          int numUsers = 0; 
          if (listener != null) numUsers = listener.getNumberOfUserNames();
          Conv.intToByteArray(numUsers, tempData, offset);
          offset += 4;
          for (int i=0; i<numUsers && listener != null ; ++i)
          {
            String user = listener.getUserName(i);
            Conv.stringToBlankPadEBCDICByteArray(user == null ? "" : user, tempData, offset, 10);
            offset += 10;
            Conv.shortToByteArray(0, tempData, offset);
            offset += 2;
          }
          int numOutputQueues = 0;
          if (listener != null) { 
             numOutputQueues = listener.getNumberOfOutputQueues();
          }
          Conv.intToByteArray(numOutputQueues, tempData, offset);
          offset += 4;
          for (int i=0; i<numOutputQueues && listener != null ; ++i)
          {
            String name = listener.getOutputQueueName(i);
            String lib = listener.getOutputQueueLibrary(i);
            Conv.stringToBlankPadEBCDICByteArray(name == null ? "" : name, tempData, offset, 10);
            offset += 10;
            Conv.stringToBlankPadEBCDICByteArray(lib == null ? "" : lib, tempData, offset, 10);
            offset += 10;
          }
          String formType = null; ; 
          if (listener != null) {  
        	  formType = listener.getFormType();
          }
          Conv.stringToBlankPadEBCDICByteArray(formType == null ? "*ALL" : formType, tempData, offset, 10);
          offset += 10;
          String userSpecifiedData = null; 
          if (listener != null) {
        	  userSpecifiedData = listener.getUserSpecifiedData();
          }
          Conv.stringToBlankPadEBCDICByteArray(userSpecifiedData == null ? "*ALL" : userSpecifiedData, tempData, offset, 10);
          offset += 10;
          int numStatuses = 0; 
          if (listener != null) { 
        	  numStatuses = listener.getNumberOfStatuses();
          }
          Conv.intToByteArray(numStatuses, tempData, offset);
          offset += 4;
          for (int i=0; i<numStatuses && listener != null ; ++i)
          {
            String status = listener.getStatus(i);
            Conv.stringToBlankPadEBCDICByteArray(status == null ? "" : status, tempData, offset, 10);
            offset += 10;
            Conv.shortToByteArray(0, tempData, offset);
            offset += 2;
          }
          int numPrinterDevices = 0; 
          if (listener != null) {
        	  numPrinterDevices= listener.getNumberOfPrinterDevices();
          }
          Conv.intToByteArray(numPrinterDevices, tempData, offset);
          offset += 4;
          for (int i=0; i<numPrinterDevices && listener != null ; ++i)
          {
            String dev = listener.getPrinterDevice(i);
            Conv.stringToBlankPadEBCDICByteArray(dev == null ? "" : dev, tempData, offset, 10);
            offset += 10;
            Conv.shortToByteArray(0, tempData, offset);
            offset += 2;
          }
        }
        return tempData;
      case 6:
        Conv.stringToBlankPadEBCDICByteArray(jobName_ == null ? "" : jobName_, tempData, 0, 10);
        Conv.stringToBlankPadEBCDICByteArray(jobUser_ == null ? "" : jobUser_, tempData, 10, 10);
        Conv.stringToBlankPadEBCDICByteArray(jobNumber_ == null ? "" : jobNumber_, tempData, 20, 6);
        return tempData;
      case 7:
        Conv.stringToBlankPadEBCDICByteArray(format_.getName(), tempData, 0, 8);
        return tempData;
      case 8: return ZERO;
      case 9:
        if (filterListener_ != null && filterListener_ instanceof OpenListOfSpooledFilesFilterOSPF0200Listener)
        {
          Conv.stringToBlankPadEBCDICByteArray("OSPF0200", tempData, 0, 8);
        }
        else
        {
          Conv.stringToBlankPadEBCDICByteArray("OSPF0100", tempData, 0, 8);
        }
        return tempData;
    }
    return null;
  }

  void setParameterOutputDataSubclass(final int parmIndex, final byte[] data, final int maxLength)
  {
    switch (parmIndex)
    {
      case 0:
        format_.format(data, maxLength, maxLength);
        break;
      case 2:
        if (maxLength < 12)
        {
          info_ = null;
        }
        else
        {
          info_ = Util.readOpenListInformationParameter(data, maxLength);
        }
        break;
      default:
        break;
    }
  }
}

